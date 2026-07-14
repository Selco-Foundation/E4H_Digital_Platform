package org.egov.rms.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.rms.model.CenterIdToHfrIdMapping;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class CenterIdMappingRepository {

    private final JdbcTemplate jdbcTemplate;

    public CenterIdMappingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Saves or updates a center ID to HFR ID mapping
     */
    public void saveOrUpdateMapping(CenterIdToHfrIdMapping mapping) {
        String sql = "INSERT INTO center_id_to_hfr_id_mapping " +
                "(id, center_id, device_id, device_instance_id, hfr_id, nin_id, facility_name, " +
                "is_active, last_sync_time, last_validated_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (center_id) DO UPDATE SET " +
                "device_id = EXCLUDED.device_id, " +
                "device_instance_id = EXCLUDED.device_instance_id, " +
                "hfr_id = EXCLUDED.hfr_id, " +
                "nin_id = EXCLUDED.nin_id, " +
                "facility_name = EXCLUDED.facility_name, " +
                "is_active = EXCLUDED.is_active, " +
                "last_sync_time = EXCLUDED.last_sync_time, " +
                "last_validated_at = EXCLUDED.last_validated_at, " +
                "updated_at = CURRENT_TIMESTAMP";

        String id = mapping.getId() != null ? mapping.getId() : UUID.randomUUID().toString();
        Instant now = Instant.now();

        jdbcTemplate.update(sql,
                id,
                mapping.getCenterId(),
                mapping.getDeviceId(),
                mapping.getDeviceInstanceId(),
                mapping.getHfrId(),
                mapping.getNinId(),
                mapping.getFacilityName(),
                mapping.getIsActive() != null ? mapping.getIsActive() : true,
                mapping.getLastSyncTime() != null ? Timestamp.from(mapping.getLastSyncTime()) : Timestamp.from(now),
                mapping.getLastValidatedAt() != null ? Timestamp.from(mapping.getLastValidatedAt()) : null,
                mapping.getCreatedAt() != null ? Timestamp.from(mapping.getCreatedAt()) : Timestamp.from(now),
                Timestamp.from(now)
        );
    }

    /**
     * Resolve center_id by HFR. When facilityName is provided and multiple active rows
     * share the same HFR (wrong remaps + correct site), prefer the Elmeasure name that
     * best matches the registry facility name. Falls back to LIMIT 1 when name is blank
     * or no name match is found.
     */
    public Optional<String> findCenterIdByHfrId(String hfrId) {
        return findCenterIdByHfrId(hfrId, null);
    }

    public Optional<String> findCenterIdByHfrId(String hfrId, String facilityName) {
        if (hfrId == null || hfrId.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT center_id, facility_name FROM center_id_to_hfr_id_mapping "
                + "WHERE hfr_id = ? AND is_active = true";
        List<CenterCandidate> candidates = jdbcTemplate.query(sql,
                (rs, rowNum) -> new CenterCandidate(rs.getString("center_id"), rs.getString("facility_name")),
                hfrId.trim());
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        if (candidates.size() == 1 || facilityName == null || facilityName.isBlank()) {
            if (candidates.size() > 1) {
                log.warn("Multiple centers for hfrId={} and no facilityName — using first row centerId={}",
                        hfrId, candidates.get(0).centerId());
            }
            return Optional.ofNullable(candidates.get(0).centerId());
        }

        String needle = normalizeFacilityName(facilityName);
        CenterCandidate best = null;
        int bestScore = 0;
        for (CenterCandidate c : candidates) {
            int score = nameMatchScore(needle, normalizeFacilityName(c.facilityName()));
            if (score > bestScore) {
                bestScore = score;
                best = c;
            }
        }
        if (best != null && bestScore > 0) {
            log.info("Resolved hfrId={} facilityName='{}' to centerId={} elmeasureName='{}' score={}",
                    hfrId, facilityName, best.centerId(), best.facilityName(), bestScore);
            return Optional.ofNullable(best.centerId());
        }

        log.warn("No name match for hfrId={} facilityName='{}' among {} centers — using first row centerId={}",
                hfrId, facilityName, candidates.size(), candidates.get(0).centerId());
        return Optional.ofNullable(candidates.get(0).centerId());
    }

    /**
     * Score how well a registry facility name matches an Elmeasure facility_name.
     * Higher is better; 0 means no usable match.
     */
    static int nameMatchScore(String registryNorm, String elmeasureNorm) {
        if (registryNorm == null || registryNorm.isEmpty()
                || elmeasureNorm == null || elmeasureNorm.isEmpty()) {
            return 0;
        }
        if (registryNorm.equals(elmeasureNorm)) {
            return 1000 + registryNorm.length();
        }
        if (elmeasureNorm.contains(registryNorm)) {
            return 500 + registryNorm.length();
        }
        if (registryNorm.contains(elmeasureNorm)) {
            return 400 + elmeasureNorm.length();
        }
        // token overlap (e.g. "Themra CHC" vs "Themra 1 CHC" after normalize)
        String[] a = registryNorm.split(" ");
        String[] b = elmeasureNorm.split(" ");
        int hits = 0;
        for (String t : a) {
            if (t.length() < 3) {
                continue;
            }
            for (String u : b) {
                if (t.equals(u)) {
                    hits++;
                    break;
                }
            }
        }
        return hits > 0 ? 100 * hits + registryNorm.length() : 0;
    }

    /**
     * Lowercase and strip common facility-type suffixes/noise so registry vs Elmeasure names align.
     */
    static String normalizeFacilityName(String name) {
        if (name == null) {
            return "";
        }
        String n = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\b(phc|chc|mphc|uphc|hwc|sd|sc|sub|center|centre|clinic|new|primary|health)\\b", " ")
                .replaceAll("\\d+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return n;
    }

    private record CenterCandidate(String centerId, String facilityName) {
    }

    public Optional<String> findHfrIdByCenterId(String centerId) {
        String sql = "SELECT hfr_id FROM center_id_to_hfr_id_mapping " +
                "WHERE center_id = ? AND is_active = true";

        List<String> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getString("hfr_id"),
                centerId);

        return results.isEmpty() ? Optional.empty() : Optional.ofNullable(results.get(0));
    }

    /**
     * Finds mapping by Center ID
     */
    public Optional<CenterIdToHfrIdMapping> findByCenterId(String centerId) {
        String sql = "SELECT * FROM center_id_to_hfr_id_mapping WHERE center_id = ?";

        List<CenterIdToHfrIdMapping> mappings = jdbcTemplate.query(sql, new MappingRowMapper(), centerId);

        return mappings.isEmpty() ? Optional.empty() : Optional.of(mappings.get(0));
    }

    /**
     * Finds all active mappings that need validation (older than 7 days)
     */
    public List<CenterIdToHfrIdMapping> findMappingsNeedingValidation(int daysOld) {
        String sql = "SELECT * FROM center_id_to_hfr_id_mapping " +
                "WHERE is_active = true " +
                "AND (last_validated_at IS NULL OR last_validated_at < NOW() - INTERVAL '" + daysOld + " days') " +
                "ORDER BY last_validated_at NULLS FIRST";

        return jdbcTemplate.query(sql, new MappingRowMapper());
    }

    /**
     * Updates validation timestamp
     */
    public void updateValidationTimestamp(String centerId) {
        String sql = "UPDATE center_id_to_hfr_id_mapping " +
                "SET last_validated_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP " +
                "WHERE center_id = ?";

        jdbcTemplate.update(sql, centerId);
    }

    /**
     * Marks mapping as inactive
     */
    public void markInactive(String centerId) {
        String sql = "UPDATE center_id_to_hfr_id_mapping " +
                "SET is_active = false, updated_at = CURRENT_TIMESTAMP " +
                "WHERE center_id = ?";

        jdbcTemplate.update(sql, centerId);
    }

    /**
     * Gets all active mappings
     */
    public List<CenterIdToHfrIdMapping> getAllActiveMappings() {
        String sql = "SELECT * FROM center_id_to_hfr_id_mapping WHERE is_active = true";
        return jdbcTemplate.query(sql, new MappingRowMapper());
    }

    /**
     * RowMapper for CenterIdToHfrIdMapping
     */
    private static class MappingRowMapper implements RowMapper<CenterIdToHfrIdMapping> {
        @Override
        public CenterIdToHfrIdMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
            return CenterIdToHfrIdMapping.builder()
                    .id(rs.getString("id"))
                    .centerId(rs.getString("center_id"))
                    .deviceId(rs.getString("device_id"))
                    .deviceInstanceId(rs.getString("device_instance_id"))
                    .hfrId(rs.getString("hfr_id"))
                    .ninId(rs.getString("nin_id"))
                    .facilityName(rs.getString("facility_name"))
                    .isActive(rs.getBoolean("is_active"))
                    .lastSyncTime(rs.getTimestamp("last_sync_time") != null ?
                            rs.getTimestamp("last_sync_time").toInstant() : null)
                    .lastValidatedAt(rs.getTimestamp("last_validated_at") != null ?
                            rs.getTimestamp("last_validated_at").toInstant() : null)
                    .createdAt(rs.getTimestamp("created_at") != null ?
                            rs.getTimestamp("created_at").toInstant() : null)
                    .updatedAt(rs.getTimestamp("updated_at") != null ?
                            rs.getTimestamp("updated_at").toInstant() : null)
                    .build();
        }
    }
}

