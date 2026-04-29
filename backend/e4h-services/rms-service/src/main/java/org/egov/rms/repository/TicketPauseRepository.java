package org.egov.rms.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.model.PausedFacilityItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class TicketPauseRepository {

    private final JdbcTemplate jdbcTemplate;

    public TicketPauseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsertPause(String facilityId, String facilityName, String boundaryCode,
                            Instant pausedUntil, String reason, String requestedBy, String tenantId) {
        log.debug("Upserting pause record: facilityId={}, boundaryCode={}, pausedUntil={}, tenantId={}",
                facilityId, boundaryCode, pausedUntil, tenantId);
        String sql = "INSERT INTO rms_ticket_pause_config " +
                "(id, facility_id, facility_name, boundary_code, paused_until, reason, requested_by, tenant_id, is_active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (facility_id) DO UPDATE SET " +
                "facility_name = EXCLUDED.facility_name, " +
                "boundary_code = EXCLUDED.boundary_code, " +
                "paused_until = EXCLUDED.paused_until, " +
                "reason = EXCLUDED.reason, " +
                "requested_by = EXCLUDED.requested_by, " +
                "tenant_id = EXCLUDED.tenant_id, " +
                "is_active = TRUE, " +
                "updated_at = CURRENT_TIMESTAMP";
        jdbcTemplate.update(sql,
                UUID.randomUUID().toString(),
                facilityId,
                facilityName,
                boundaryCode,
                Timestamp.from(pausedUntil),
                reason,
                requestedBy,
                tenantId
        );
    }

    public int deactivatePause(String facilityId) {
        log.debug("Deactivating pause record: facilityId={}", facilityId);
        String sql = "UPDATE rms_ticket_pause_config SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP " +
                "WHERE facility_id = ? AND is_active = TRUE";
        return jdbcTemplate.update(sql, facilityId);
    }

    public Optional<TicketPauseRecord> findActivePauseByFacility(String facilityId, Instant now) {
        log.debug("Finding active pause: facilityId={}, now={}", facilityId, now);
        String sql = "SELECT facility_id, facility_name, boundary_code, paused_until, reason, requested_by, updated_at " +
                "FROM rms_ticket_pause_config " +
                "WHERE facility_id = ? AND is_active = TRUE AND paused_until > ? " +
                "LIMIT 1";
        List<TicketPauseRecord> rows = jdbcTemplate.query(sql, new TicketPauseRecordRowMapper(),
                facilityId, Timestamp.from(now));
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<PausedFacilityItem> listActivePausedFacilities(List<String> boundaryCodes, int offset, int limit) {
        log.debug("Listing active paused facilities: boundaryFiltersCount={}, offset={}, limit={}",
                boundaryCodes != null ? boundaryCodes.size() : 0, offset, limit);
        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
            log.warn("Boundary filter list is empty for listActivePausedFacilities; returning empty result");
            return List.of();
        }
        StringBuilder sql = new StringBuilder(
                "SELECT facility_id, facility_name, boundary_code, paused_until, reason, requested_by, updated_at " +
                        "FROM rms_ticket_pause_config " +
                        "WHERE is_active = TRUE AND paused_until > CURRENT_TIMESTAMP ");
        List<Object> args = new ArrayList<>();
        appendBoundaryFilter(sql, args, boundaryCodes);
        sql.append(" ORDER BY paused_until ASC OFFSET ? LIMIT ?");
        args.add(offset);
        args.add(limit);
        return jdbcTemplate.query(sql.toString(), new PausedFacilityItemRowMapper(), args.toArray());
    }

    public long countActivePausedFacilities(List<String> boundaryCodes) {
        log.debug("Counting active paused facilities: boundaryFiltersCount={}",
                boundaryCodes != null ? boundaryCodes.size() : 0);
        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
            log.warn("Boundary filter list is empty for countActivePausedFacilities; returning 0");
            return 0L;
        }
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM rms_ticket_pause_config " +
                        "WHERE is_active = TRUE AND paused_until > CURRENT_TIMESTAMP ");
        List<Object> args = new ArrayList<>();
        appendBoundaryFilter(sql, args, boundaryCodes);
        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, args.toArray());
        return count == null ? 0L : count;
    }

    private void appendBoundaryFilter(StringBuilder sql, List<Object> args, List<String> boundaryCodes) {
        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
            return;
        }
        sql.append(" AND (");
        for (int i = 0; i < boundaryCodes.size(); i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("(boundary_code = ? OR boundary_code LIKE ? ESCAPE '\\')");
            String code = boundaryCodes.get(i);
            String escapedCode = escapeLikeValue(code);
            args.add(code);
            args.add(escapedCode + "\\_%");
        }
        sql.append(") ");
    }

    private String escapeLikeValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        return rawValue
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketPauseRecord {
        private String facilityId;
        private String facilityName;
        private String boundaryCode;
        private Instant pausedUntil;
        private String reason;
        private String requestedBy;
        private Instant updatedAt;
    }

    private static class TicketPauseRecordRowMapper implements RowMapper<TicketPauseRecord> {
        @Override
        public TicketPauseRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TicketPauseRecord.builder()
                    .facilityId(rs.getString("facility_id"))
                    .facilityName(rs.getString("facility_name"))
                    .boundaryCode(rs.getString("boundary_code"))
                    .pausedUntil(rs.getTimestamp("paused_until").toInstant())
                    .reason(rs.getString("reason"))
                    .requestedBy(rs.getString("requested_by"))
                    .updatedAt(rs.getTimestamp("updated_at").toInstant())
                    .build();
        }
    }

    private static class PausedFacilityItemRowMapper implements RowMapper<PausedFacilityItem> {
        @Override
        public PausedFacilityItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            Instant pausedUntil = rs.getTimestamp("paused_until").toInstant();
            long daysLeft = Math.max(0, ChronoUnit.DAYS.between(Instant.now(), pausedUntil));
            return PausedFacilityItem.builder()
                    .facilityId(rs.getString("facility_id"))
                    .facilityName(rs.getString("facility_name"))
                    .boundaryCode(rs.getString("boundary_code"))
                    .pausedUntil(pausedUntil)
                    .daysLeft(daysLeft)
                    .reason(rs.getString("reason"))
                    .pausedBy(rs.getString("requested_by"))
                    .updatedAt(rs.getTimestamp("updated_at").toInstant())
                    .build();
        }
    }
}

