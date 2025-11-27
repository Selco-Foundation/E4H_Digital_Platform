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
import java.util.Map;
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
     * Checks if an alert already has a ticket created
     * Returns true if a ticket exists for the given facility, alert type, and sub-type
     * This method only checks active_alerts table - use hasOpenTicket() for status check
     */
    public boolean hasExistingTicket(String facilityId, Alert.AlertType alertType, Alert.AlertSubType alertSubType) {
        String sql = "SELECT COUNT(*) FROM active_alerts " +
                "WHERE facility_id = ? AND alert_type = ? AND alert_sub_type = ? " +
                "AND ticket_id IS NOT NULL AND ticket_id != ''";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                facilityId, alertType.name(), alertSubType.name());

        return count != null && count > 0;
    }

    /**
     * Checks if there's an open ticket in eg_incident_v2 for the given facility, alert type, and sub-type
     * Returns true if there's an open ticket, false if ticket is closed or doesn't exist
     * 
     * Open statuses: PENDINGFORASSIGNMENT, PENDING_ASSIGNMENT_SPARE_PART_NEEDED, 
     *                PENDING_ASSIGNMENT_OUT_OF_WARRANTY, PENDING_RESOLUTION_SPARE_PART_NEEDED,
     *                PENDING_RESOLUTION_OUT_OF_WARRANTY, PENDINGRESOLUTION
     * Closed statuses: RESOLVED, CLOSEDAFTERRESOLUTION, REJECTED, CLOSEDAFTERREJECTION
     */
    public boolean hasOpenTicket(String facilityId, Alert.AlertType alertType, Alert.AlertSubType alertSubType) {
        try {
            // First, get the ticket_id from active_alerts
            String getTicketIdSql = "SELECT ticket_id FROM active_alerts " +
                    "WHERE facility_id = ? AND alert_type = ? AND alert_sub_type = ? " +
                    "AND ticket_id IS NOT NULL AND ticket_id != '' " +
                    "LIMIT 1";

            List<String> ticketIds = jdbcTemplate.queryForList(getTicketIdSql, String.class,
                    facilityId, alertType.name(), alertSubType.name());

            if (ticketIds == null || ticketIds.isEmpty()) {
                log.debug("No ticket_id found in active_alerts for facility: {}, type: {}, subType: {}",
                        facilityId, alertType, alertSubType);
                return false;
            }

            String ticketId = ticketIds.get(0);
            log.debug("Found ticket_id {} in active_alerts, checking status in eg_incident_v2", ticketId);

            // Check if ticket exists and is open in eg_incident_v2
            // Map alert type/subtype to incident type/subtype (this might need adjustment based on your mapping)
            String checkStatusSql = "SELECT applicationstatus FROM eg_incident_v2 " +
                    "WHERE incidentid = ? " +
                    "LIMIT 1";

            List<String> statuses = jdbcTemplate.queryForList(checkStatusSql, String.class, ticketId);

            if (statuses == null || statuses.isEmpty()) {
                log.debug("Ticket {} not found in eg_incident_v2 - allowing new ticket creation", ticketId);
                return false; // Ticket doesn't exist in incident table, allow creation
            }

            String applicationStatus = statuses.get(0);
            
            // Define open and closed statuses
            String[] openStatuses = {
                "PENDINGFORASSIGNMENT",
                "PENDING_ASSIGNMENT_SPARE_PART_NEEDED",
                "PENDING_ASSIGNMENT_OUT_OF_WARRANTY",
                "PENDING_RESOLUTION_SPARE_PART_NEEDED",
                "PENDING_RESOLUTION_OUT_OF_WARRANTY",
                "PENDINGRESOLUTION"
            };

            String[] closedStatuses = {
                "RESOLVED",
                "CLOSEDAFTERRESOLUTION",
                "REJECTED",
                "CLOSEDAFTERREJECTION"
            };

            // Check if status is open
            for (String openStatus : openStatuses) {
                if (openStatus.equalsIgnoreCase(applicationStatus)) {
                    log.info("Ticket {} has open status: {} - preventing duplicate ticket creation", 
                            ticketId, applicationStatus);
                    return true;
                }
            }

            // Check if status is closed
            for (String closedStatus : closedStatuses) {
                if (closedStatus.equalsIgnoreCase(applicationStatus)) {
                    log.info("Ticket {} has closed status: {} - allowing new ticket creation", 
                            ticketId, applicationStatus);
                    return false; // Ticket is closed, allow new ticket
                }
            }

            // If status is neither open nor closed (unknown status), treat as open to be safe
            log.warn("Ticket {} has unknown status: {} - treating as open to prevent duplicates", 
                    ticketId, applicationStatus);
            return true;

        } catch (Exception e) {
            log.error("Error checking ticket status in eg_incident_v2 for facility: {}, type: {}, subType: {}", 
                    facilityId, alertType, alertSubType, e);
            // On error, default to checking active_alerts only (fallback behavior)
            return hasExistingTicket(facilityId, alertType, alertSubType);
        }
    }

    /**
     * Gets all active alerts
     */
    public List<Alert> getAllActiveAlerts() {
        String sql = "SELECT * FROM active_alerts WHERE status = 'ACTIVE' OR status = 'TICKET_CREATED'";
        return jdbcTemplate.query(sql, new AlertRowMapper());
    }

    /**
     * Gets alerts from alert_history that don't have tickets, filtered by alert type and sub-type
     * Used when trigger endpoint is called to process existing alerts without syncing from servers
     * Returns the most recent alert for each unique facility_id, alert_type, alert_sub_type combination
     */
    public List<Alert> getAlertsFromHistoryWithoutTickets(Alert.AlertType alertType, Alert.AlertSubType alertSubType) {
        String sql = "SELECT DISTINCT ON (facility_id, alert_type, alert_sub_type) " +
                "alert_id as id, facility_id, hfr_id, alert_type, alert_sub_type, status, " +
                "detected_at, resolved_at, NULL::timestamp as last_suppressed_at, ticket_id, " +
                "COALESCE(metadata::text, '') as metadata " +
                "FROM alert_history " +
                "WHERE alert_type = ? AND alert_sub_type = ? " +
                "AND (ticket_id IS NULL OR ticket_id = '') " +
                "ORDER BY facility_id, alert_type, alert_sub_type, detected_at DESC";

        return jdbcTemplate.query(sql, new AlertRowMapper(), alertType.name(), alertSubType.name());
    }

    /**
     * Gets all alerts from alert_history that don't have tickets, filtered by alert type
     * Used when trigger endpoint is called to process existing alerts without syncing from servers
     */
    public List<Alert> getAlertsFromHistoryWithoutTicketsByType(Alert.AlertType alertType) {
        String sql = "SELECT DISTINCT ON (facility_id, alert_type, alert_sub_type) " +
                "alert_id as id, facility_id, hfr_id, alert_type, alert_sub_type, status, " +
                "detected_at, resolved_at, NULL::timestamp as last_suppressed_at, ticket_id, " +
                "COALESCE(metadata::text, '') as metadata " +
                "FROM alert_history " +
                "WHERE alert_type = ? " +
                "AND (ticket_id IS NULL OR ticket_id = '') " +
                "ORDER BY facility_id, alert_type, alert_sub_type, detected_at DESC";

        return jdbcTemplate.query(sql, new AlertRowMapper(), alertType.name());
    }

    /**
     * Gets ALL alerts from active_alerts table that don't have tickets
     * Used when trigger endpoint is called to process existing alerts without syncing from servers
     * Returns all alerts without tickets
     */
    public List<Alert> getAllAlertsFromHistoryWithoutTickets() {
        try {
            // First, let's check how many total alerts exist in active_alerts table
            String totalCountSql = "SELECT COUNT(*) FROM active_alerts";
            Integer totalInTable = jdbcTemplate.queryForObject(totalCountSql, Integer.class);
            log.info("Total alerts in active_alerts table: {}", totalInTable);
            
            // Check how many have tickets
            String withTicketsSql = "SELECT COUNT(*) FROM active_alerts WHERE ticket_id IS NOT NULL AND ticket_id != ''";
            Integer withTickets = jdbcTemplate.queryForObject(withTicketsSql, Integer.class);
            log.info("Alerts in active_alerts with tickets: {}", withTickets);
            
            // Check how many don't have tickets
            String countSql = "SELECT COUNT(*) FROM active_alerts WHERE (ticket_id IS NULL OR ticket_id = '')";
            Integer totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
            log.info("Total alerts in active_alerts without tickets: {}", totalCount);
            
            // Get all alerts from active_alerts without tickets
            String sql = "SELECT id, facility_id, hfr_id, alert_type, alert_sub_type, status, " +
                    "detected_at, resolved_at, last_suppressed_at, ticket_id, " +
                    "COALESCE(metadata::text, '') as metadata " +
                    "FROM active_alerts " +
                    "WHERE (ticket_id IS NULL OR ticket_id = '') " +
                    "ORDER BY detected_at DESC";

            List<Alert> alerts = jdbcTemplate.query(sql, new AlertRowMapper());
            log.info("Retrieved {} alerts from active_alerts without tickets", alerts.size());
            
            // Log details of each alert for debugging
            if (alerts.isEmpty()) {
                log.warn("No alerts retrieved from active_alerts! This might indicate a query issue or data mismatch.");
                // Try a simpler query to see if we can get any data
                String simpleSql = "SELECT * FROM active_alerts LIMIT 5";
                List<Map<String, Object>> rawData = jdbcTemplate.queryForList(simpleSql);
                log.info("Sample raw data from active_alerts (first 5 rows): {}", rawData);
            } else {
                for (Alert alert : alerts) {
                    log.info("Alert from active_alerts - ID: {}, Facility: {}, Type: {}, SubType: {}, TicketID: {}", 
                            alert.getId(), alert.getFacilityId(), alert.getAlertType(), 
                            alert.getAlertSubType(), alert.getTicketId());
                }
            }
            
            return alerts;
        } catch (Exception e) {
            log.error("Error retrieving alerts from active_alerts", e);
            return new java.util.ArrayList<>();
        }
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

