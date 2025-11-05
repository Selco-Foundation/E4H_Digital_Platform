package org.egov.rms.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.rms.model.Alert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class AlertRepository {

    private final JdbcTemplate jdbcTemplate;

    public AlertRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Saves an alert to active_alerts table
     */
    public void saveAlert(Alert alert) {
        String sql = "INSERT INTO active_alerts (id, facility_id, hfr_id, alert_type, alert_sub_type, " +
                "status, detected_at, resolved_at, last_suppressed_at, ticket_id, metadata, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?) " +
                "ON CONFLICT (facility_id, alert_type, alert_sub_type) " +
                "DO UPDATE SET status = EXCLUDED.status, " +
                "detected_at = EXCLUDED.detected_at, " +
                "last_suppressed_at = EXCLUDED.last_suppressed_at, " +
                "ticket_id = EXCLUDED.ticket_id, " +
                "metadata = EXCLUDED.metadata, " +
                "updated_at = CURRENT_TIMESTAMP";

        jdbcTemplate.update(sql,
                alert.getId(),
                alert.getFacilityId(),
                alert.getHfrId(),
                alert.getAlertType().name(),
                alert.getAlertSubType().name(),
                alert.getStatus().name(),
                alert.getDetectedAt() != null ? Timestamp.from(alert.getDetectedAt()) : null,
                alert.getResolvedAt() != null ? Timestamp.from(alert.getResolvedAt()) : null,
                alert.getLastSuppressedAt() != null ? Timestamp.from(alert.getLastSuppressedAt()) : null,
                alert.getTicketId(),
                alert.getMetadata(),
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
        );
    }

    /**
     * Finds an active alert by facility, type, and sub-type
     */
    public Optional<Alert> findActiveAlert(String facilityId, Alert.AlertType alertType, Alert.AlertSubType alertSubType) {
        String sql = "SELECT * FROM active_alerts " +
                "WHERE facility_id = ? AND alert_type = ? AND alert_sub_type = ? AND status = 'ACTIVE'";

        List<Alert> alerts = jdbcTemplate.query(sql, new AlertRowMapper(),
                facilityId, alertType.name(), alertSubType.name());

        return alerts.isEmpty() ? Optional.empty() : Optional.of(alerts.get(0));
    }

    /**
     * Checks if alert should be suppressed based on suppression window
     */
    public boolean shouldSuppress(String facilityId, Alert.AlertType alertType, Alert.AlertSubType alertSubType, 
                                  int suppressionWindowHours) {
        String sql = "SELECT last_suppressed_at FROM active_alerts " +
                "WHERE facility_id = ? AND alert_type = ? AND alert_sub_type = ? AND status = 'ACTIVE'";

        List<Timestamp> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getTimestamp("last_suppressed_at"),
                facilityId, alertType.name(), alertSubType.name());

        if (results.isEmpty() || results.get(0) == null) {
            return false;
        }

        Instant lastSuppressed = results.get(0).toInstant();
        Instant cutoffTime = Instant.now().minusSeconds(suppressionWindowHours * 3600L);
        return lastSuppressed.isAfter(cutoffTime);
    }

    /**
     * Updates alert suppression timestamp
     */
    public void updateSuppressionTime(String facilityId, Alert.AlertType alertType, Alert.AlertSubType alertSubType) {
        String sql = "UPDATE active_alerts SET last_suppressed_at = CURRENT_TIMESTAMP, " +
                "status = 'SUPPRESSED', updated_at = CURRENT_TIMESTAMP " +
                "WHERE facility_id = ? AND alert_type = ? AND alert_sub_type = ?";

        jdbcTemplate.update(sql, facilityId, alertType.name(), alertSubType.name());
    }

    /**
     * Updates alert with ticket ID
     */
    public void updateTicketId(String alertId, String ticketId) {
        String sql = "UPDATE active_alerts SET ticket_id = ?, status = 'TICKET_CREATED', updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";

        jdbcTemplate.update(sql, ticketId, alertId);
    }

    /**
     * Resolves an alert
     */
    public void resolveAlert(String alertId) {
        String sql = "UPDATE active_alerts SET status = 'RESOLVED', resolved_at = CURRENT_TIMESTAMP, " +
                "updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        jdbcTemplate.update(sql, alertId);
    }

    /**
     * Gets all active alerts
     */
    public List<Alert> getAllActiveAlerts() {
        String sql = "SELECT * FROM active_alerts WHERE status = 'ACTIVE' OR status = 'TICKET_CREATED'";
        return jdbcTemplate.query(sql, new AlertRowMapper());
    }

    /**
     * RowMapper for Alert
     */
    private static class AlertRowMapper implements RowMapper<Alert> {
        @Override
        public Alert mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Alert.builder()
                    .id(rs.getString("id"))
                    .facilityId(rs.getString("facility_id"))
                    .hfrId(rs.getString("hfr_id"))
                    .alertType(Alert.AlertType.valueOf(rs.getString("alert_type")))
                    .alertSubType(Alert.AlertSubType.valueOf(rs.getString("alert_sub_type")))
                    .status(Alert.AlertStatus.valueOf(rs.getString("status")))
                    .detectedAt(rs.getTimestamp("detected_at") != null ? 
                            rs.getTimestamp("detected_at").toInstant() : null)
                    .resolvedAt(rs.getTimestamp("resolved_at") != null ? 
                            rs.getTimestamp("resolved_at").toInstant() : null)
                    .lastSuppressedAt(rs.getTimestamp("last_suppressed_at") != null ? 
                            rs.getTimestamp("last_suppressed_at").toInstant() : null)
                    .ticketId(rs.getString("ticket_id"))
                    .metadata(rs.getString("metadata"))
                    .build();
        }
    }
}

