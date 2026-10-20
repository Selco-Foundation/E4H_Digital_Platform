package db.migration.main;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * For each amc_configuration_id in amc_configuration_assignments that has exactly 2
 * assigned_user rows, ensures every scheduled_visits row under that configuration has
 * a matching scheduled_visit_assignments row for both users, creating whichever is missing.
 */
@Slf4j
public class V20260821140000__backfill_scheduled_visit_assignments extends BaseJavaMigration {

    private static final String SELECT_CONFIG_ASSIGNMENTS =
            "SELECT amc_configuration_id, assigned_user FROM amc_configuration_assignments";

    private static final String SELECT_VISITS_FOR_CONFIG =
            "SELECT id, tenant_id, created_by FROM scheduled_visits WHERE amc_configuration_id = ?";

    private static final String SELECT_EXISTING_ASSIGNMENTS_FOR_VISIT =
            "SELECT assigned_user FROM scheduled_visit_assignments WHERE scheduled_visit_id = ?";

    private static final String INSERT_ASSIGNMENT =
            "INSERT INTO scheduled_visit_assignments "
                    + "(id, tenant_id, scheduled_visit_id, assigned_user, is_active, created_by, created_time) "
                    + "VALUES (?, ?, ?, ?, TRUE, ?, ?)";

    @Override
    public boolean canExecuteInTransaction() {
        return false;
    }

    @Override
    public void migrate(Context context) throws Exception {
        log.info("Starting migration: backfilling scheduled_visit_assignments from amc_configuration_assignments");

        int configsProcessed = 0;
        int configsSkipped = 0;
        int visitsChecked = 0;
        int rowsCreated = 0;
        int warnings = 0;

        try (Connection connection = context.getConfiguration().getDataSource().getConnection()) {
            Map<String, List<String>> configToUsers = loadConfigAssignedUsers(connection);

            for (Map.Entry<String, List<String>> entry : configToUsers.entrySet()) {
                String amcConfigurationId = entry.getKey();
                List<String> assignedUsers = entry.getValue();

                if (assignedUsers.size() != 2) {
                    log.warn("amc_configuration_id={} has {} assigned_user row(s) in amc_configuration_assignments, expected 2 - skipping",
                            amcConfigurationId, assignedUsers.size());
                    warnings++;
                    configsSkipped++;
                    continue;
                }

                log.info("Processing amc_configuration_id={}: expected users={}", amcConfigurationId, assignedUsers);
                configsProcessed++;

                List<VisitRow> visits = loadVisitsForConfig(connection, amcConfigurationId);
                log.info("amc_configuration_id={}: found {} scheduled visits", amcConfigurationId, visits.size());

                for (VisitRow visit : visits) {
                    visitsChecked++;
                    try {
                        Set<String> existingUsers = loadExistingAssignedUsers(connection, visit.id);

                        if (existingUsers.isEmpty()) {
                            log.warn("scheduled_visit_id={} has 0 existing scheduled_visit_assignments rows - creating both expected users",
                                    visit.id);
                            warnings++;
                        }

                        boolean anyCreated = false;
                        for (String expectedUser : assignedUsers) {
                            if (existingUsers.contains(expectedUser)) {
                                continue;
                            }
                            insertAssignment(connection, visit, expectedUser);
                            rowsCreated++;
                            anyCreated = true;
                            log.info("scheduled_visit_id={}: created row for assigned_user={}", visit.id, expectedUser);
                        }

                        if (!anyCreated) {
                            log.info("scheduled_visit_id={}: both expected users already present - nothing to create", visit.id);
                        }
                    } catch (Exception e) {
                        warnings++;
                        log.warn("scheduled_visit_id={}: failed to process - {}", visit.id, e.getMessage(), e);
                    }
                }
            }
        }

        log.info("Migration completed: configsProcessed={}, configsSkipped={}, visitsChecked={}, rowsCreated={}, warnings={}",
                configsProcessed, configsSkipped, visitsChecked, rowsCreated, warnings);
    }

    private Map<String, List<String>> loadConfigAssignedUsers(Connection connection) throws Exception {
        Map<String, List<String>> configToUsers = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_CONFIG_ASSIGNMENTS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String amcConfigurationId = rs.getString("amc_configuration_id");
                String assignedUser = rs.getString("assigned_user");
                configToUsers.computeIfAbsent(amcConfigurationId, k -> new ArrayList<>()).add(assignedUser);
            }
        }
        return configToUsers;
    }

    private List<VisitRow> loadVisitsForConfig(Connection connection, String amcConfigurationId) throws Exception {
        List<VisitRow> visits = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_VISITS_FOR_CONFIG)) {
            ps.setString(1, amcConfigurationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    visits.add(new VisitRow(rs.getString("id"), rs.getString("tenant_id"), rs.getString("created_by")));
                }
            }
        }
        return visits;
    }

    private Set<String> loadExistingAssignedUsers(Connection connection, String scheduledVisitId) throws Exception {
        Set<String> existingUsers = new HashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_EXISTING_ASSIGNMENTS_FOR_VISIT)) {
            ps.setString(1, scheduledVisitId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existingUsers.add(rs.getString("assigned_user"));
                }
            }
        }
        return existingUsers;
    }

    private void insertAssignment(Connection connection, VisitRow visit, String assignedUser) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_ASSIGNMENT)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, visit.tenantId);
            ps.setString(3, visit.id);
            ps.setString(4, assignedUser);
            ps.setString(5, visit.createdBy);
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }

    private static class VisitRow {
        private final String id;
        private final String tenantId;
        private final String createdBy;

        private VisitRow(String id, String tenantId, String createdBy) {
            this.id = id;
            this.tenantId = tenantId;
            this.createdBy = createdBy;
        }
    }
}
