package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.egov.rms.model.RMSFacilityData;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEngineService {

    private final RMSConfiguration config;

    /**
     * Applies panel-level anomaly rules
     * Rule: Solar consumption &lt; threshold% for 7 consecutive days, and (when gating is enabled)
     * the center must not be idle: {@code last_sync_time} within {@code rms.rule.panel.idle.max.hours}
     * (default 72h). This avoids tickets for centers with stale telemetry while still requiring a 7-day
     * low-solar pattern from the graph API.
     */
    public List<Alert> applyPanelRules(List<RMSFacilityData> facilities) {
        log.info("Applying panel-level anomaly rules to {} facilities", facilities.size());
        List<Alert> alerts = new ArrayList<>();
        boolean activityGating = config.isPanelLowSolarActivityGatingEnabled();
        int idleMaxHours = config.getPanelIdleMaxHours();
        Instant idleCutoff = Instant.now().minus(idleMaxHours, ChronoUnit.HOURS);

        for (RMSFacilityData facility : facilities) {
            if (facility.getSolarPercent() == null || facility.getSolarPercent().isEmpty()) {
                continue;
            }

            String facilityId = facility.getFacilityId() != null ? facility.getFacilityId() : facility.getCenterId();

            // API already filters for < threshold%; we verify all days and 7+ days of data
            boolean allDaysBelowThreshold = facility.getSolarPercent().stream()
                    .allMatch(percent -> percent <= config.getSolarThresholdPercent());

            int daysCount = facility.getSolarPercent().size();

            if (!allDaysBelowThreshold || daysCount < 7) {
                continue;
            }

            if (activityGating) {
                Instant lastSync = facility.getLastSyncTime();
                if (lastSync == null) {
                    log.debug("Skipping panel low generation for facility {} — no lastSyncTime (cannot verify recent activity)",
                            facilityId);
                    continue;
                }
                if (lastSync.isBefore(idleCutoff)) {
                    log.debug("Skipping panel low generation for facility {} — idle: lastSync {} is older than {}h",
                            facilityId, lastSync, idleMaxHours);
                    continue;
                }
            }

            String metadata = buildPanelMetadata(facility);

            Alert alert = Alert.builder()
                    .id(UUID.randomUUID().toString())
                    .facilityId(facilityId)
                    .hfrId(facility.getHfrId())
                    .alertType(Alert.AlertType.PANEL)
                    .alertSubType(Alert.AlertSubType.LOW_GENERATION)
                    .status(Alert.AlertStatus.ACTIVE)
                    .detectedAt(Instant.now())
                    .metadata(metadata)
                    .build();

            alerts.add(alert);
            log.debug("Panel low generation alert created for facility: {} ({} days of low generation)", 
                    facilityId, daysCount);
        }

        log.info("Generated {} panel-level alerts", alerts.size());
        return alerts;
    }

    /**
     * Builds detailed metadata for inverter high voltage alerts
     */
    private String buildInverterVoltageMetadata(RMSFacilityData facility, String facilityName) {
        try {
            String name = facilityName != null ? facilityName : 
                    (facility.getFacilityName() != null ? facility.getFacilityName() : 
                     (facility.getCenterName() != null ? facility.getCenterName() : ""));
            
            // Escape facility name for JSON
            String escapedName = name.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            
            StringBuilder metadata = new StringBuilder();
            metadata.append("{\"facilityName\":\"").append(escapedName).append("\"");
            
            if (facility.getVoltage() != null) {
                metadata.append(",\"voltage\":").append(facility.getVoltage());
                metadata.append(",\"voltageThreshold\":").append(config.getInverterHighVoltageThreshold());
                metadata.append(",\"excessVoltage\":").append(
                        facility.getVoltage() - config.getInverterHighVoltageThreshold());
            }
            
            metadata.append("}");
            return metadata.toString();
        } catch (Exception e) {
            log.warn("Error building inverter voltage metadata", e);
            return "{\"error\":\"Failed to build metadata\"}";
        }
    }

    /**
     * Builds detailed metadata for panel alerts
     */
    private String buildPanelMetadata(RMSFacilityData facility) {
        try {
            String name = facility.getFacilityName() != null ? facility.getFacilityName() : 
                    (facility.getCenterName() != null ? facility.getCenterName() : "");
            
            // Escape facility name for JSON
            String escapedName = name.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            
            StringBuilder metadata = new StringBuilder();
            metadata.append("{\"facilityName\":\"").append(escapedName).append("\"");
            
            if (facility.getSolarPercent() != null && !facility.getSolarPercent().isEmpty()) {
                metadata.append(",\"solarPercent\":[");
                for (int i = 0; i < facility.getSolarPercent().size(); i++) {
                    if (i > 0) metadata.append(",");
                    metadata.append(facility.getSolarPercent().get(i));
                }
                metadata.append("]");
                metadata.append(",\"daysCount\":").append(facility.getSolarPercent().size());
                metadata.append(",\"averageSolarPercent\":").append(
                        facility.getSolarPercent().stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0.0));
            }
            
            if (facility.getSolarConsumption() != null && !facility.getSolarConsumption().isEmpty()) {
                metadata.append(",\"solarConsumption\":[");
                for (int i = 0; i < facility.getSolarConsumption().size(); i++) {
                    if (i > 0) metadata.append(",");
                    metadata.append(facility.getSolarConsumption().get(i));
                }
                metadata.append("]");
            }
            
            if (facility.getGridConsumption() != null && !facility.getGridConsumption().isEmpty()) {
                metadata.append(",\"gridConsumption\":[");
                for (int i = 0; i < facility.getGridConsumption().size(); i++) {
                    if (i > 0) metadata.append(",");
                    metadata.append(facility.getGridConsumption().get(i));
                }
                metadata.append("]");
            }

            if (facility.getLastSyncTime() != null) {
                metadata.append(",\"lastSyncTime\":\"").append(facility.getLastSyncTime().toString()).append("\"");
            }
            if (facility.getStatusOfDevice() != null && !facility.getStatusOfDevice().isEmpty()) {
                String escapedStatus = facility.getStatusOfDevice().replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t");
                metadata.append(",\"statusOfDevice\":\"").append(escapedStatus).append("\"");
            }
            
            metadata.append("}");
            return metadata.toString();
        } catch (Exception e) {
            log.warn("Error building panel metadata", e);
            return "{\"error\":\"Failed to build metadata\"}";
        }
    }

    /**
     * Applies inverter-level anomaly rules
     * Rule 1: No signal for 2+ days
     * Rule 2: High voltage > 250V
     */
    public List<Alert> applyInverterRules(List<RMSFacilityData> facilities, boolean checkNoSignal) {
        log.info("Applying inverter-level anomaly rules to {} facilities (checkNoSignal: {})", 
                facilities.size(), checkNoSignal);
        List<Alert> alerts = new ArrayList<>();

        for (RMSFacilityData facility : facilities) {
            if (checkNoSignal) {
                // Rule: No signal for configured days
                // Validate that lastSyncTime is actually 2+ days old
                String facilityId = facility.getFacilityId() != null ? 
                        facility.getFacilityId() : facility.getCenterId();
                String facilityName = facility.getFacilityName() != null ? 
                        facility.getFacilityName() : facility.getCenterName();
                
                if (facilityId != null && facility.getLastSyncTime() != null) {
                    // Calculate days difference between lastSyncTime and now using manual calculation
                    Instant now = Instant.now();
                    long millisDifference = now.toEpochMilli() - facility.getLastSyncTime().toEpochMilli();
                    long daysDifference = millisDifference / (1000 * 60 * 60 * 24); // Convert milliseconds to days
                    
                    // Only create alert if device has been inactive for configured days (default: 2)
                    if (daysDifference >= config.getInverterNoSignalDays()) {
                        Alert alert = Alert.builder()
                                .id(UUID.randomUUID().toString())
                                .facilityId(facilityId)
                                .hfrId(facility.getHfrId())
                                .alertType(Alert.AlertType.INVERTER)
                                .alertSubType(Alert.AlertSubType.SHUTDOWN)
                                .status(Alert.AlertStatus.ACTIVE)
                                .detectedAt(Instant.now())
                                .metadata(buildMetadata(facility, facilityName, "lastSyncTime", facility.getLastSyncTime()))
                                .build();

                        alerts.add(alert);
                        log.debug("Inverter shutdown alert created for facility: {} (lastSyncTime: {}, days without signal: {})", 
                                facilityId, facility.getLastSyncTime(), daysDifference);
                    } else {
                        log.debug("Skipping facility {} - lastSyncTime is only {} days old (required: {} days). Last sync: {}", 
                                facilityId, daysDifference, config.getInverterNoSignalDays(), facility.getLastSyncTime());
                    }
                }
            } else {
                // Rule: High voltage (> 250V)
                // Note: API already filters by voltage > threshold, so all facilities here meet the criteria
                // We just need to create alerts for them
                String facilityId = facility.getFacilityId() != null ? 
                        facility.getFacilityId() : facility.getCenterId();
                String facilityName = facility.getFacilityName() != null ? 
                        facility.getFacilityName() : facility.getCenterName();
                
                if (facilityId != null) {
                    // Build detailed metadata with voltage information
                    String metadata = buildInverterVoltageMetadata(facility, facilityName);
                    
                    Alert alert = Alert.builder()
                            .id(UUID.randomUUID().toString())
                            .facilityId(facilityId)
                            .hfrId(facility.getHfrId())
                            .alertType(Alert.AlertType.INVERTER)
                            .alertSubType(Alert.AlertSubType.HIGH_VOLTAGE)
                            .status(Alert.AlertStatus.ACTIVE)
                            .detectedAt(Instant.now())
                            .metadata(metadata)
                            .build();

                    alerts.add(alert);
                    log.debug("Inverter high voltage alert created for facility: {} (voltage: {}V)", 
                            facilityId, facility.getVoltage());
                }
            }
        }

        log.info("Generated {} inverter-level alerts", alerts.size());
        return alerts;
    }

    /**
     * Applies battery-level anomaly rules for deep discharge/overcharge
     * Rule: Abnormal charging vs discharging pattern over 2-3 days
     * Note: API already identifies abnormal patterns via batteryHealth.info field
     */
    public List<Alert> applyBatteryDeepDischargeRules(List<RMSFacilityData> facilities) {
        log.info("Applying battery deep discharge/overcharge rules to {} facilities", facilities.size());
        boolean batteryDeepDischargeTicketsEnabled = config.isBatteryDeepDischargeTicketsEnabled();
        if (!batteryDeepDischargeTicketsEnabled) {
            log.info("Battery deep discharge ticket generation is disabled via rms.rule.battery.deep.discharge.tickets.enabled");
        }
        List<Alert> alerts = new ArrayList<>();

        for (RMSFacilityData facility : facilities) {
            // Rule: Deep discharge or overcharge
            // Note: API already identifies abnormal patterns, so all facilities here meet the criteria
            String facilityId = facility.getFacilityId() != null ? 
                    facility.getFacilityId() : facility.getCenterId();
            String facilityName = facility.getFacilityName() != null ? 
                    facility.getFacilityName() : facility.getCenterName();
            
            if (facilityId != null && facility.getBatteryHealthInfo() != null) {
                // Determine alert subtype based on battery health info
                Alert.AlertSubType alertSubType = Alert.AlertSubType.DEEP_DISCHARGING;
                if (facility.getBatteryHealthInfo().equalsIgnoreCase("overcharge") ||
                        facility.getBatteryHealthInfo().equalsIgnoreCase("poorHealth")) {
                    alertSubType = Alert.AlertSubType.OVERCHARGING;
                } else if (facility.getBatteryHealthInfo().equalsIgnoreCase("deepDischarge") || 
                          facility.getBatteryHealthInfo().toLowerCase().contains("discharge")) {
                    alertSubType = Alert.AlertSubType.DEEP_DISCHARGING;
                }

                if (!batteryDeepDischargeTicketsEnabled && alertSubType == Alert.AlertSubType.DEEP_DISCHARGING) {
                    log.debug("Skipping battery deep discharge alert for facility {} - generation is disabled", facilityId);
                    continue;
                }

                // Build detailed metadata with battery health information
                String metadata = buildBatteryDeepDischargeMetadata(facility, facilityName);
                
                Alert alert = Alert.builder()
                        .id(UUID.randomUUID().toString())
                        .facilityId(facilityId)
                        .hfrId(facility.getHfrId())
                        .alertType(Alert.AlertType.BATTERY)
                        .alertSubType(alertSubType)
                        .status(Alert.AlertStatus.ACTIVE)
                        .detectedAt(Instant.now())
                        .metadata(metadata)
                        .build();

                alerts.add(alert);
                log.debug("Battery deep discharge/overcharge alert created for facility: {} (info: {})", 
                        facilityId, facility.getBatteryHealthInfo());
            }
        }

        log.info("Generated {} battery deep discharge/overcharge alerts", alerts.size());
        return alerts;
    }

    /**
     * Applies battery-level anomaly rules
     * Rule: Battery voltage = 0
     * Note: API already filters by voltage = 0, so all facilities here meet the criteria
     */
    public List<Alert> applyBatteryRules(List<RMSFacilityData> facilities) {
        log.info("Applying battery-level anomaly rules to {} facilities", facilities.size());
        List<Alert> alerts = new ArrayList<>();

        for (RMSFacilityData facility : facilities) {
            // Rule: Battery voltage = 0
            // Note: API already filters for voltage = 0, so all facilities here meet the criteria
            String facilityId = facility.getFacilityId() != null ? 
                    facility.getFacilityId() : facility.getCenterId();
            String facilityName = facility.getFacilityName() != null ? 
                    facility.getFacilityName() : facility.getCenterName();
            
            if (facilityId != null) {
                // Build detailed metadata with battery voltage information
                String metadata = buildBatteryMetadata(facility, facilityName);
                
                Alert alert = Alert.builder()
                        .id(UUID.randomUUID().toString())
                        .facilityId(facilityId)
                        .hfrId(facility.getHfrId())
                        .alertType(Alert.AlertType.BATTERY)
                        .alertSubType(Alert.AlertSubType.BURNT_DISCONNECTED)
                        .status(Alert.AlertStatus.ACTIVE)
                        .detectedAt(Instant.now())
                        .metadata(metadata)
                        .build();

                alerts.add(alert);
                log.debug("Battery burnt/disconnected alert created for facility: {} (voltage: {}V)", 
                        facilityId, facility.getBatteryVoltage());
            }
        }

        log.info("Generated {} battery-level alerts", alerts.size());
        return alerts;
    }

    /**
     * Builds detailed metadata for battery deep discharge/overcharge alerts
     */
    private String buildBatteryDeepDischargeMetadata(RMSFacilityData facility, String facilityName) {
        try {
            String name = facilityName != null ? facilityName : 
                    (facility.getFacilityName() != null ? facility.getFacilityName() : 
                     (facility.getCenterName() != null ? facility.getCenterName() : ""));
            
            // Escape facility name for JSON
            String escapedName = name.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            
            StringBuilder metadata = new StringBuilder();
            metadata.append("{\"facilityName\":\"").append(escapedName).append("\"");
            
            if (facility.getBatteryHealthInfo() != null) {
                String escapedHealthInfo = facility.getBatteryHealthInfo().replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t");
                metadata.append(",\"batteryHealthInfo\":\"").append(escapedHealthInfo).append("\"");
            }
            
            if (facility.getBatteryCharging() != null) {
                metadata.append(",\"batteryCharging\":").append(facility.getBatteryCharging());
            }
            
            if (facility.getBatteryDischarging() != null) {
                metadata.append(",\"batteryDischarging\":").append(facility.getBatteryDischarging());
            }
            
            metadata.append("}");
            return metadata.toString();
        } catch (Exception e) {
            log.warn("Error building battery deep discharge metadata", e);
            return "{\"error\":\"Failed to build metadata\"}";
        }
    }

    /**
     * Builds detailed metadata for battery alerts
     */
    private String buildBatteryMetadata(RMSFacilityData facility, String facilityName) {
        try {
            String name = facilityName != null ? facilityName : 
                    (facility.getFacilityName() != null ? facility.getFacilityName() : 
                     (facility.getCenterName() != null ? facility.getCenterName() : ""));
            
            // Escape facility name for JSON
            String escapedName = name.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            
            StringBuilder metadata = new StringBuilder();
            metadata.append("{\"facilityName\":\"").append(escapedName).append("\"");
            
            if (facility.getBatteryVoltage() != null) {
                metadata.append(",\"batteryVoltage\":").append(facility.getBatteryVoltage());
            }
            
            metadata.append("}");
            return metadata.toString();
        } catch (Exception e) {
            log.warn("Error building battery metadata", e);
            return "{\"error\":\"Failed to build metadata\"}";
        }
    }

    /**
     * Applies grid-level anomaly rules
     * Rule: Grid voltage in [reverseMin, reverseMax] (Reverse Voltage, default 50V-150V)
     *       or &gt; high threshold (High Voltage)
     * Note: API filters return candidate facilities; we still validate ranges here.
     */
    public List<Alert> applyGridRules(List<RMSFacilityData> facilities) {
        log.info("Applying grid-level anomaly rules to {} facilities", facilities.size());
        boolean gridHighVoltageTicketsEnabled = config.isGridHighVoltageTicketsEnabled();
        if (!gridHighVoltageTicketsEnabled) {
            log.info("Grid high voltage ticket generation is disabled via rms.rule.grid.high.voltage.tickets.enabled");
        }
        double reverseMin = config.getGridVoltageReverseMinThreshold();
        double reverseMax = config.getGridVoltageReverseMaxThreshold();
        List<Alert> alerts = new ArrayList<>();

        for (RMSFacilityData facility : facilities) {
            String facilityId = facility.getFacilityId() != null ? 
                    facility.getFacilityId() : facility.getCenterId();
            String facilityName = facility.getFacilityName() != null ? 
                    facility.getFacilityName() : facility.getCenterName();
            
            if (facilityId == null) {
                continue;
            }
            
            // Reverse voltage: grid voltage falls within [reverseMin, reverseMax] inclusive.
            // minVoltage is populated by DataCollectorService for low/reverse-side candidates.
            Double reverseVoltage = facility.getMinVoltage();
            if ( gridHighVoltageTicketsEnabled && reverseVoltage != null && reverseVoltage >= reverseMin && reverseVoltage <= reverseMax) {
                Alert alert = Alert.builder()
                        .id(UUID.randomUUID().toString())
                        .facilityId(facilityId)
                        .hfrId(facility.getHfrId())
                        .alertType(Alert.AlertType.GRID)
                        .alertSubType(Alert.AlertSubType.VOLTAGE_VARIATION_REVERSE)
                        .status(Alert.AlertStatus.ACTIVE)
                        .detectedAt(Instant.now())
                        .metadata(buildGridReverseVoltageMetadata(facility, facilityName, reverseVoltage))
                        .build();

                alerts.add(alert);
                log.debug("Grid reverse voltage alert created for facility: {} (voltage: {}V, range: {}-{}V)",
                        facilityId, reverseVoltage, reverseMin, reverseMax);
            } else if (reverseVoltage != null) {
                log.debug("Skipping facility {} - voltage {}V outside reverse voltage range [{}, {}]",
                        facilityId, reverseVoltage, reverseMin, reverseMax);
            }

            // Check high voltage (maxVoltage is set for high voltage facilities)
            if (gridHighVoltageTicketsEnabled && facility.getMaxVoltage() != null) {
                Alert alert = Alert.builder()
                        .id(UUID.randomUUID().toString())
                        .facilityId(facilityId)
                        .hfrId(facility.getHfrId())
                        .alertType(Alert.AlertType.GRID)
                        .alertSubType(Alert.AlertSubType.VOLTAGE_VARIATION_HIGH)
                        .status(Alert.AlertStatus.ACTIVE)
                        .detectedAt(Instant.now())
                        .metadata(buildGridVoltageMetadata(facility, facilityName, facility.getMaxVoltage(), false))
                        .build();

                alerts.add(alert);
                log.debug("Grid high voltage alert created for facility: {} (voltage: {}V)", 
                        facilityId, facility.getMaxVoltage());
            }
        }

        log.info("Generated {} grid-level alerts", alerts.size());
        return alerts;
    }

    /**
     * Builds detailed metadata for grid high voltage alerts
     */
    private String buildGridVoltageMetadata(RMSFacilityData facility, String facilityName, Double voltage, boolean isLow) {
        try {
            String name = facilityName != null ? facilityName :
                    (facility.getFacilityName() != null ? facility.getFacilityName() :
                     (facility.getCenterName() != null ? facility.getCenterName() : ""));

            String escapedName = escapeForJson(name);

            StringBuilder metadata = new StringBuilder();
            metadata.append("{\"facilityName\":\"").append(escapedName).append("\"");
            
            if (voltage != null) {
                if (isLow) {
                    metadata.append(",\"voltage\":").append(voltage);
                    metadata.append(",\"reverseMinThreshold\":").append(config.getGridVoltageReverseMinThreshold());
                    metadata.append(",\"reverseMaxThreshold\":").append(config.getGridVoltageReverseMaxThreshold());
                } else {
                    metadata.append(",\"maxVoltage\":").append(voltage);
                    metadata.append(",\"threshold\":").append(config.getGridVoltageHighThreshold());
                }
            }
            
            metadata.append("}");
            return metadata.toString();
        } catch (Exception e) {
            log.warn("Error building grid voltage metadata", e);
            return "{\"error\":\"Failed to build metadata\"}";
        }
    }

    /**
     * Builds detailed metadata for grid reverse voltage alerts (voltage in [min, max]).
     */
    private String buildGridReverseVoltageMetadata(RMSFacilityData facility, String facilityName, Double voltage) {
        try {
            String name = facilityName != null ? facilityName :
                    (facility.getFacilityName() != null ? facility.getFacilityName() :
                     (facility.getCenterName() != null ? facility.getCenterName() : ""));

            String escapedName = escapeForJson(name);

            StringBuilder metadata = new StringBuilder();
            metadata.append("{\"facilityName\":\"").append(escapedName).append("\"");
            if (voltage != null) {
                metadata.append(",\"voltage\":").append(voltage);
            }
            metadata.append(",\"reverseMinThreshold\":").append(config.getGridVoltageReverseMinThreshold());
            metadata.append(",\"reverseMaxThreshold\":").append(config.getGridVoltageReverseMaxThreshold());
            metadata.append("}");
            return metadata.toString();
        } catch (Exception e) {
            log.warn("Error building grid reverse voltage metadata", e);
            return "{\"error\":\"Failed to build metadata\"}";
        }
    }

    private String escapeForJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Builds metadata JSON string for alert
     */
    private String buildMetadata(RMSFacilityData facility, String facilityName, String key, Object value) {
        try {
            String name = facilityName != null ? facilityName : 
                    (facility.getFacilityName() != null ? facility.getFacilityName() : 
                     (facility.getCenterName() != null ? facility.getCenterName() : ""));
            
            // Properly format the value for JSON
            String jsonValue;
            if (value == null) {
                jsonValue = "null";
            } else if (value instanceof String) {
                // Escape quotes and special characters in string
                String escaped = ((String) value).replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t");
                jsonValue = "\"" + escaped + "\"";
            } else if (value instanceof Instant) {
                // Convert Instant to ISO-8601 string and quote it
                jsonValue = "\"" + value.toString() + "\"";
            } else if (value instanceof Number || value instanceof Boolean) {
                // Numbers and booleans don't need quotes
                jsonValue = value.toString();
            } else {
                // For other types, convert to string and quote it
                jsonValue = "\"" + value.toString().replace("\\", "\\\\")
                        .replace("\"", "\\\"") + "\"";
            }
            
            // Escape facility name for JSON
            String escapedName = name.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            
            return String.format("{\"facilityName\":\"%s\",\"%s\":%s}", 
                    escapedName, key, jsonValue);
        } catch (Exception e) {
            log.warn("Error building metadata for alert", e);
            return "{}";
        }
    }

    /**
     * Builds metadata JSON string for alert (backward compatibility)
     */
    private String buildMetadata(RMSFacilityData facility, String key, Object value) {
        String facilityName = facility.getFacilityName() != null ? 
                facility.getFacilityName() : facility.getCenterName();
        return buildMetadata(facility, facilityName, key, value);
    }
}

