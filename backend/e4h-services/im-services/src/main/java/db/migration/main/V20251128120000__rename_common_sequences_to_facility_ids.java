package db.migration.main;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Migration to bulk rename sequences from tenantId-based names to facilityId-based names.
 *
 * Example:
 *   Old sequence name:  seq_egov_common_as_garajan
 *   Extracted tenantId: as.garajan        (rest of name, "_" -> ".")
 *   facilityId lookup:  fac/2025/41923    (from facility_tenant_id_map.hfr_or_nin_id = tenantId)
 *   New sequence name:  seq_egov_common_fac_2025_41923   ("/" -> "_" in facilityId)
 *
 * Steps:
 *   1. Fetch all sequences from the DB.
 *   2. Filter those starting with "seq_egov_common_".
 *   3. For each, derive tenantId and lookup facilityId from facility_tenant_id_map.
 *   4. Derive new sequence name and perform ALTER SEQUENCE ... RENAME TO ...
 *   5. Log all actions to console and to a dedicated log file under ./logs.
 */
@Slf4j
public class V20251128120000__rename_common_sequences_to_facility_ids extends BaseJavaMigration {

    private static final String SEQUENCE_PREFIX = "seq_egov_common_";
    private static final String LOG_FILE_PREFIX = "seq_rename_to_facility_";

    private PrintWriter migrationLogger;
    private Path logFilePath;

    @Override
    public boolean canExecuteInTransaction() {
        // Renaming many sequences is safer outside a single large transaction.
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("Starting migration: Renaming common sequences to facility-based sequences");

        String logFileName = LOG_FILE_PREFIX + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".log";
        Path logsDir = Paths.get("logs");
        Files.createDirectories(logsDir);
        logFilePath = logsDir.resolve(logFileName).toAbsolutePath().normalize();

        try (PrintWriter logger = initializeMigrationLogger(logFilePath);
             Connection connection = context.getConfiguration().getDataSource().getConnection()) {
            this.migrationLogger = logger;
            logSectionHeader();

            List<String> allSequences = fetchAllSequenceNames(connection);
            Map<String, String> sequenceRenameMap = findSequencesToRename(connection, allSequences);
            if (sequenceRenameMap.isEmpty()) {
                log.info("No sequences found with prefix '{}'. Nothing to rename.", SEQUENCE_PREFIX);
                logToFile("No sequences found with prefix '%s'. Nothing to rename.", SEQUENCE_PREFIX);
                logSectionFooter();
                return;
            }

            log.info("Found {} sequences to consider for renaming", sequenceRenameMap.size());
            logToFile("Found %d sequences to consider for renaming", sequenceRenameMap.size());

            int successCount = 0;
            int skippedCount = 0;
            List<String> failures = new ArrayList<>();

            for (Map.Entry<String, String> entry : sequenceRenameMap.entrySet()) {
                String oldSeqName = entry.getKey();
                String newSeqName = entry.getValue();

                try {
                    renameSequence(connection, oldSeqName, newSeqName);
                    successCount++;
                    log.info("Renamed sequence {} -> {}", oldSeqName, newSeqName);
                    logToFile("SUCCESS: Renamed %s -> %s", oldSeqName, newSeqName);
                } catch (Exception e) {
                    String errorMsg = String.format(Locale.ROOT,
                            "FAILURE: Could not rename %s -> %s : %s", oldSeqName, newSeqName, e.getMessage());
                    log.error(errorMsg, e);
                    logToFile(errorMsg);
                    failures.add(errorMsg);
                }
            }

            log.info("Sequence rename summary: success={}, skipped={}, failures={}",
                    successCount, skippedCount, failures.size());
            logToFile("Summary: success=%d, skipped=%d, failures=%d",
                    successCount, skippedCount, failures.size());
            if (!failures.isEmpty()) {
                logToFile("Failure details:");
                for (String failure : failures) {
                    logToFile("  - %s", failure);
                }
            }
            logSectionFooter();
        } finally {
            if (migrationLogger != null) {
                migrationLogger.flush();
                migrationLogger.close();
                migrationLogger = null;
            }
        }

        log.info("Migration completed. Log file: {}", logFilePath);
    }

    /**
     * Fetch all sequence names from the current database.
     */
    private List<String> fetchAllSequenceNames(Connection connection) throws SQLException {
        List<String> sequenceNames = new ArrayList<>();

        // Prefer DatabaseMetaData when available; fall back to querying information_schema.sequences
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"SEQUENCE"})) {
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                if (name != null) {
                    sequenceNames.add(name);
                }
            }
        }

        if (!sequenceNames.isEmpty()) {
            return sequenceNames;
        }

        // Fallback for drivers that don't expose sequences via DatabaseMetaData
        String sql = "SELECT sequence_name FROM information_schema.sequences";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("sequence_name");
                if (name != null) {
                    sequenceNames.add(name);
                }
            }
        }

        return sequenceNames;
    }

    /**
     * Lookup facilityId for the given tenantId using facility_tenant_id_map.
     *
     * Uses tenant_id column to match the extracted tenant identifier.
     */
    private Map<String, String> fetchFacilityIdsForTenants(Connection connection, Set<String> tenantIds) {
        Map<String, String> tenantToFacility = new HashMap<>();
        if (tenantIds == null || tenantIds.isEmpty()) {
            return tenantToFacility;
        }

        // Build a single bulk query with IN (...) placeholders to reduce DB round trips
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT tenant_id, facility_id FROM facility_tenant_id_map WHERE tenant_id IN (");
        int index = 0;
        for (int i = 0; i < tenantIds.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        sb.append(')');

        String sql = sb.toString();

        try (java.sql.PreparedStatement ps = connection.prepareStatement(sql)) {
            for (String tenantId : tenantIds) {
                ps.setString(++index, tenantId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenantId = rs.getString("tenant_id");
                    String facilityId = rs.getString("facility_id");
                    if (tenantId != null && facilityId != null) {
                        tenantToFacility.put(tenantId, facilityId);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching facilityIds for tenantIds {}: {}", tenantIds, e.getMessage(), e);
            logToFile("ERROR: Failed to fetch facilityIds for tenantIds %s : %s", tenantIds, e.getMessage());
        }

        return tenantToFacility;
    }

    /**
     * Scan all sequences, filter those starting with SEQUENCE_PREFIX and build a map
     * of oldName -> newName based on facility_tenant_id_map, using bulk lookups.
     */
    private Map<String, String> findSequencesToRename(Connection connection, List<String> allSequences) {
        Map<String, String> renameMap = new HashMap<>();
        logToFile("Total sequences found in DB: %d", allSequences.size());

        // Build a set of existing sequence names for quick existence checks
        Set<String> existingSequences = new HashSet<>(allSequences);

        // First pass: collect tenantIds per sequence
        Map<String, String> seqToTenantId = new HashMap<>();
        Set<String> tenantIds = new HashSet<>();

        for (String seqName : allSequences) {
            if (!seqName.startsWith(SEQUENCE_PREFIX)) {
                continue;
            }

            String suffix = seqName.substring(SEQUENCE_PREFIX.length());
            if (suffix.isEmpty()) {
                logToFile("SKIP: Sequence %s has empty suffix after prefix", seqName);
                continue;
            }

            // Step 3: turn "as_garajan" into "as.garajan"
            String tenantId = suffix.replace('_', '.');
            seqToTenantId.put(seqName, tenantId);
            tenantIds.add(tenantId);
        }

        if (seqToTenantId.isEmpty()) {
            return renameMap;
        }

        // Bulk fetch facilityIds for all tenantIds in one go
        Map<String, String> tenantToFacility = fetchFacilityIdsForTenants(connection, tenantIds);

        // Second pass: build rename map using the pre-fetched facilityIds
        for (Map.Entry<String, String> entry : seqToTenantId.entrySet()) {
            String seqName = entry.getKey();
            String tenantId = entry.getValue();

            String facilityId = tenantToFacility.get(tenantId);
            if (facilityId == null || facilityId.isEmpty()) {
                log.warn("No facilityId found for tenantId {} (sequence: {}), skipping", tenantId, seqName);
                logToFile("SKIP: No facilityId found for tenantId %s (sequence: %s)", tenantId, seqName);
                continue;
            }

            // Step 4: "fac/2025/41923" -> "fac_2025_41923"
            String sanitizedFacilityId = facilityId.replace('/', '_');
            String newSeqName = SEQUENCE_PREFIX + sanitizedFacilityId;

            // Avoid planning a rename to a sequence name that already exists
            if (existingSequences.contains(newSeqName)) {
                logToFile("SKIP: Target sequence %s already exists in DB (from tenantId=%s, facilityId=%s)",
                        newSeqName, tenantId, facilityId);
                continue;
            }

            logToFile("PLAN: %s -> %s (tenantId=%s, facilityId=%s)",
                    seqName, newSeqName, tenantId, facilityId);
            renameMap.put(seqName, newSeqName);
        }

        return renameMap;
    }

    /**
     * Perform the actual ALTER SEQUENCE ... RENAME TO ... operation.
     */
    private void renameSequence(Connection connection, String oldName, String newName) throws SQLException {
        String sql = "ALTER SEQUENCE " + oldName + " RENAME TO " + newName;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private PrintWriter initializeMigrationLogger(Path logPath) throws Exception {
        Files.createDirectories(logPath.getParent());
        FileWriter fileWriter = new FileWriter(logPath.toFile(), true);
        log.info("Migration log file created at {}", logPath);
        return new PrintWriter(fileWriter, true);
    }

    private void logToFile(String format, Object... args) {
        if (migrationLogger != null) {
            migrationLogger.printf(format + "%n", args);
            migrationLogger.flush();
        }
    }

    private void logSectionHeader() {
        logToFile("========================================");
        logToFile("SEQUENCE RENAME TO FACILITY ID MIGRATION LOG");
        logToFile("Log File: %s", logFilePath);
        logToFile("Start Time: %s", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        logToFile("========================================");
    }

    private void logSectionFooter() {
        logToFile("----------------------------------------");
        logToFile("End Time: %s", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        logToFile("Log File: %s", logFilePath);
        logToFile("========================================");
    }
}


