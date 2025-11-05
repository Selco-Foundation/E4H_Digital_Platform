package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.egov.rms.model.RMSFacilityData;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
     * Rule: Solar consumption < 10% of total for 7 consecutive days
     */
    public List<Alert> applyPanelRules(List<RMSFacilityData> facilities) {
        log.info("Applying panel-level anomaly rules to {} facilities", facilities.size());
        List<Alert> alerts = new ArrayList<>();

        for (RMSFacilityData facility : facilities) {
            if (facility.getSolarPercent() == null || facility.getSolarPercent().isEmpty()) {
                continue;
            }

            // Check if all days have solar consumption < threshold
            boolean allDaysBelowThreshold = facility.getSolarPercent().stream()
                    .allMatch(percent -> percent < config.getSolarThresholdPercent());

            if (allDaysBelowThreshold && facility.getSolarPercent().size() >= 7) {
                Alert alert = Alert.builder()
                        .id(UUID.randomUUID().toString())
                        .facilityId(facility.getFacilityId())
                        .hfrId(facility.getHfrId())
                        .alertType(Alert.AlertType.PANEL)
                        .alertSubType(Alert.AlertSubType.LOW_GENERATION)
                        .status(Alert.AlertStatus.ACTIVE)
                        .detectedAt(Instant.now())
                        .metadata(buildMetadata(facility, "solarPercent", facility.getSolarPercent()))
                        .build();

                alerts.add(alert);
                log.debug("Panel low generation alert created for facility: {}", facility.getFacilityId());
            }
        }

        log.info("Generated {} panel-level alerts", alerts.size());
        return alerts;
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
                if (facility.getLastSyncTime() != null && 
                    facility.getStatusOfDevice() != null &&
                    "Inactive".equalsIgnoreCase(facility.getStatusOfDevice())) {
                    
                    Instant cutoffTime = Instant.now().minus(config.getInverterNoSignalDays(), 
                            java.time.temporal.ChronoUnit.DAYS);
                    
                    if (facility.getLastSyncTime().isBefore(cutoffTime)) {
                        Alert alert = Alert.builder()
                                .id(UUID.randomUUID().toString())
                                .facilityId(facility.getFacilityId())
                                .hfrId(facility.getHfrId())
                                .alertType(Alert.AlertType.INVERTER)
                                .alertSubType(Alert.AlertSubType.SHUTDOWN)
                                .status(Alert.AlertStatus.ACTIVE)
                                .detectedAt(Instant.now())
                                .metadata(buildMetadata(facility, "lastSyncTime", facility.getLastSyncTime()))
                                .build();

                        alerts.add(alert);
                        log.debug("Inverter shutdown alert created for facility: {}", facility.getFacilityId());
                    }
                }
            } else {
                // Rule: High voltage
                if (facility.getVoltageReadings() != null && !facility.getVoltageReadings().isEmpty()) {
                    boolean hasHighVoltage = facility.getVoltageReadings().stream()
                            .anyMatch(reading -> {
                                if (reading.size() >= 2 && reading.get(1) instanceof Number) {
                                    double voltage = ((Number) reading.get(1)).doubleValue();
                                    return voltage > config.getInverterHighVoltageThreshold();
                                }
                                return false;
                            });

                    if (hasHighVoltage) {
                        Alert alert = Alert.builder()
                                .id(UUID.randomUUID().toString())
                                .facilityId(facility.getFacilityId())
                                .hfrId(facility.getHfrId())
                                .alertType(Alert.AlertType.INVERTER)
                                .alertSubType(Alert.AlertSubType.HIGH_VOLTAGE)
                                .status(Alert.AlertStatus.ACTIVE)
                                .detectedAt(Instant.now())
                                .metadata(buildMetadata(facility, "voltageReadings", facility.getVoltageReadings()))
                                .build();

                        alerts.add(alert);
                        log.debug("Inverter high voltage alert created for facility: {}", facility.getFacilityId());
                    }
                }
            }
        }

        log.info("Generated {} inverter-level alerts", alerts.size());
        return alerts;
    }

    /**
     * Applies battery-level anomaly rules
     * Rule 1: Battery voltage = 0
     * Rule 2: Deep discharging / Overcharging (to be implemented with additional data)
     */
    public List<Alert> applyBatteryRules(List<RMSFacilityData> facilities) {
        log.info("Applying battery-level anomaly rules to {} facilities", facilities.size());
        List<Alert> alerts = new ArrayList<>();

        for (RMSFacilityData facility : facilities) {
            // Rule: Battery voltage = 0
            if (facility.getBatteryReadings() != null && !facility.getBatteryReadings().isEmpty()) {
                boolean hasZeroVoltage = facility.getBatteryReadings().stream()
                        .anyMatch(reading -> {
                            if (reading.size() >= 2 && reading.get(1) instanceof Number) {
                                double voltage = ((Number) reading.get(1)).doubleValue();
                                return voltage == 0.0;
                            }
                            return false;
                        });

                if (hasZeroVoltage) {
                    Alert alert = Alert.builder()
                            .id(UUID.randomUUID().toString())
                            .facilityId(facility.getFacilityId())
                            .hfrId(facility.getHfrId())
                            .alertType(Alert.AlertType.BATTERY)
                            .alertSubType(Alert.AlertSubType.BURNT_DISCONNECTED)
                            .status(Alert.AlertStatus.ACTIVE)
                            .detectedAt(Instant.now())
                            .metadata(buildMetadata(facility, "batteryReadings", facility.getBatteryReadings()))
                            .build();

                    alerts.add(alert);
                    log.debug("Battery burnt/disconnected alert created for facility: {}", facility.getFacilityId());
                }
            }
        }

        log.info("Generated {} battery-level alerts", alerts.size());
        return alerts;
    }

    /**
     * Applies grid-level anomaly rules
     * Rule: Grid voltage < 200V (Low) or > 250V (High)
     */
    public List<Alert> applyGridRules(List<RMSFacilityData> facilities) {
        log.info("Applying grid-level anomaly rules to {} facilities", facilities.size());
        List<Alert> alerts = new ArrayList<>();

        for (RMSFacilityData facility : facilities) {
            // Check low voltage
            if (facility.getMinVoltage() != null && 
                facility.getMinVoltage() < config.getGridVoltageLowThreshold()) {
                
                Alert alert = Alert.builder()
                        .id(UUID.randomUUID().toString())
                        .facilityId(facility.getFacilityId())
                        .hfrId(facility.getHfrId())
                        .alertType(Alert.AlertType.GRID)
                        .alertSubType(Alert.AlertSubType.VOLTAGE_VARIATION_LOW)
                        .status(Alert.AlertStatus.ACTIVE)
                        .detectedAt(Instant.now())
                        .metadata(buildMetadata(facility, "minVoltage", facility.getMinVoltage()))
                        .build();

                alerts.add(alert);
                log.debug("Grid low voltage alert created for facility: {}", facility.getFacilityId());
            }

            // Check high voltage
            if (facility.getMaxVoltage() != null && 
                facility.getMaxVoltage() > config.getGridVoltageHighThreshold()) {
                
                Alert alert = Alert.builder()
                        .id(UUID.randomUUID().toString())
                        .facilityId(facility.getFacilityId())
                        .hfrId(facility.getHfrId())
                        .alertType(Alert.AlertType.GRID)
                        .alertSubType(Alert.AlertSubType.VOLTAGE_VARIATION_HIGH)
                        .status(Alert.AlertStatus.ACTIVE)
                        .detectedAt(Instant.now())
                        .metadata(buildMetadata(facility, "maxVoltage", facility.getMaxVoltage()))
                        .build();

                alerts.add(alert);
                log.debug("Grid high voltage alert created for facility: {}", facility.getFacilityId());
            }
        }

        log.info("Generated {} grid-level alerts", alerts.size());
        return alerts;
    }

    /**
     * Builds metadata JSON string for alert
     */
    private String buildMetadata(RMSFacilityData facility, String key, Object value) {
        try {
            return String.format("{\"facilityName\":\"%s\",\"%s\":%s}", 
                    facility.getFacilityName() != null ? facility.getFacilityName() : "",
                    key, 
                    value instanceof String ? "\"" + value + "\"" : value);
        } catch (Exception e) {
            log.warn("Error building metadata for alert", e);
            return "{}";
        }
    }
}

