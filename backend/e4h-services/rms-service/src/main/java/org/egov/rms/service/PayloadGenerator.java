package org.egov.rms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.egov.rms.model.FacilityDetails;
import org.egov.rms.model.IMServiceRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayloadGenerator {

    private final RMSConfiguration config;
    private final FacilityServiceClient facilityServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * Generates IM service ticket payload from alert
     */
    public IMServiceRequest generateTicketPayload(Alert alert, RequestInfo requestInfo) {
        log.debug("Generating ticket payload for alert: {}", alert.getId());

        try {
            // Fetch facility details
//            FacilityDetails facilityDetails = facilityServiceClient.getFacilityByHfrId(
//                    alert.getHfrId(), config.getDefaultTenantId());
//
//            if (facilityDetails == null) {
//                log.warn("Facility not found for hfrId: {}", alert.getHfrId());
//                return null;
//            }

            // Map alert type/subtype to IM service incident type/subtype
            String incidentType = mapAlertTypeToIncidentType(alert.getAlertType());
            String incidentSubType = mapAlertSubTypeToIncidentSubType(alert.getAlertSubType(), alert.getAlertType());

            // Build incident payload
            IMServiceRequest.Incident incident = IMServiceRequest.Incident.builder()
                    .incidentType(incidentType)
                    .incidentSubType(incidentSubType)
                    .tenantId("pg.bagalkot")
                    .district("BAGALKOTE") // TODO: Extract from mapping or facility data
                    .block("BAGALKOT") // TODO: Extract from mapping or facility data
                    .phcType("pg.bagalkot")
                    .phcSubType("Urban Primary Health Center") // TODO: Extract from facility type
                    .comments(buildComments(alert, null))
                    .systemFunctional("FUNCTIONAL")
                    .source("RMS")
                    .additionalDetail(buildAdditionalDetail(alert, null))
                    .reporter(buildReporter(requestInfo))
                    .build();

            // Build workflow
            IMServiceRequest.Workflow workflow = IMServiceRequest.Workflow.builder()
                    .action("APPLY")
                    .comments("Auto-generated ticket from RMS service")
                    .build();

            return IMServiceRequest.builder()
                    .requestInfo(requestInfo)
                    .incident(incident)
                    .workflow(workflow)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error generating ticket payload for alert: {}", alert.getId(), e);
            return null;
        }
    }

    /**
     * Maps RMS alert type to IM service incident type
     */
    private String mapAlertTypeToIncidentType(Alert.AlertType alertType) {
        switch (alertType) {
            case PANEL:
                return "Panel";
            case INVERTER:
                return "Inverter";
            case BATTERY:
                return "Battery";
            case GRID:
                return "Grid";
            default:
                return "GIPB";
        }
    }

    /**
     * Maps RMS alert sub-type to IM service incident sub-type
     */
    private String mapAlertSubTypeToIncidentSubType(Alert.AlertSubType alertSubType, Alert.AlertType alertType) {
        switch (alertSubType) {
            case LOW_GENERATION:
                return "LowGeneration";
            case SHUTDOWN:
                return "ShutdownInverter";
            case HIGH_VOLTAGE:
                return "VoltageInverter";
            case BURNT_DISCONNECTED:
                return "Overcharge";
            case DEEP_DISCHARGING:
                return "DeepDischarge";
            case OVERCHARGING:
                return "Overcharge";
            case VOLTAGE_VARIATION_LOW:
                return "LowVoltage";
            case VOLTAGE_VARIATION_HIGH:
                return "HighVoltage";
            default:
                return "RMS Data Alert";
        }
    }

    /**
     * Builds verbose, readable comments for the ticket from metadata in active_alerts
     */
    private String buildComments(Alert alert, FacilityDetails facilityDetails) {
        StringBuilder comments = new StringBuilder();
        
        // Add alert type and subtype information - using explicit string concatenation
        String alertTypeStr = formatAlertType(alert.getAlertType());
        String alertSubTypeStr = formatAlertSubType(alert.getAlertSubType());
        
        comments.append("RMS Alert Details");
        comments.append("\n");
        comments.append("==================");
        comments.append("\n");
        comments.append("\n");
        comments.append("Alert Type");
        comments.append(": ");
        comments.append(alertTypeStr);
        comments.append("\n");
        comments.append("Alert Sub-Type");
        comments.append(": ");
        comments.append(alertSubTypeStr);
        comments.append("\n");
        comments.append("\n");
        
        // Add facility information
        comments.append("Facility Information");
        comments.append("\n");
        comments.append("---------------------");
        comments.append("\n");
        String facilityId = alert.getFacilityId() != null ? alert.getFacilityId() : "N/A";
        comments.append("Facility ID");
        comments.append(": ");
        comments.append(facilityId);
        comments.append("\n");
        
        if (alert.getHfrId() != null && !alert.getHfrId().isEmpty()) {
            comments.append("HFR ID");
            comments.append(": ");
            comments.append(alert.getHfrId());
            comments.append("\n");
        }
        
        String detectedAt = alert.getDetectedAt() != null ? alert.getDetectedAt().toString() : "N/A";
        comments.append("Alert Detected At");
        comments.append(": ");
        comments.append(detectedAt);
        comments.append("\n");
        comments.append("\n");
        
        // Parse and format metadata
        if (alert.getMetadata() != null && !alert.getMetadata().isEmpty()) {
            comments.append("Technical Details");
            comments.append("\n");
            comments.append("------------------");
            comments.append("\n");
            
            try {
                // Parse JSON metadata - filter out boolean false values
                Map<String, Object> metadataMap = objectMapper.readValue(alert.getMetadata(), Map.class);
                
                // Add facility name if available
                Object facilityNameObj = metadataMap.get("facilityName");
                if (facilityNameObj != null && !isBooleanFalse(facilityNameObj)) {
                    String facilityName = safeToString(facilityNameObj);
                    comments.append("Facility Name");
                    comments.append(": ");
                    comments.append(facilityName);
                    comments.append("\n");
                }
                
                // Format metadata based on alert type
                switch (alert.getAlertType()) {
                    case PANEL:
                        formatPanelMetadata(comments, metadataMap, alert.getAlertSubType());
                        break;
                    case INVERTER:
                        formatInverterMetadata(comments, metadataMap, alert.getAlertSubType());
                        break;
                    case BATTERY:
                        formatBatteryMetadata(comments, metadataMap, alert.getAlertSubType());
                        break;
                    case GRID:
                        formatGridMetadata(comments, metadataMap, alert.getAlertSubType());
                        break;
                }
                
            } catch (Exception e) {
                log.warn("Error parsing metadata JSON, using raw metadata: {}", e.getMessage());
                comments.append("Raw Metadata");
                comments.append(": ");
                comments.append(alert.getMetadata());
                comments.append("\n");
            }
        } else {
            comments.append("No additional metadata available.");
            comments.append("\n");
        }
        
        String result = comments.toString();
        log.debug("Generated comment for alert {}: {}", alert.getId(), result);
        return result;
    }

    /**
     * Safely converts an object to string, filtering out boolean false and null values
     */
    private String safeToString(Object value) {
        if (value == null) {
            return "N/A";
        }
        if (value instanceof Boolean && !((Boolean) value)) {
            return ""; // Skip boolean false values
        }
        return value.toString();
    }

    /**
     * Checks if an object is a boolean false value
     */
    private boolean isBooleanFalse(Object value) {
        return value instanceof Boolean && !((Boolean) value);
    }

    /**
     * Formats alert type to readable string
     */
    private String formatAlertType(Alert.AlertType alertType) {
        switch (alertType) {
            case PANEL:
                return "Solar Panel";
            case INVERTER:
                return "Inverter/UPS";
            case BATTERY:
                return "Battery";
            case GRID:
                return "Grid Power";
            default:
                return alertType.name();
        }
    }

    /**
     * Formats alert subtype to readable string
     */
    private String formatAlertSubType(Alert.AlertSubType alertSubType) {
        switch (alertSubType) {
            case LOW_GENERATION:
                return "Low Generation";
            case SHUTDOWN:
                return "Device Shutdown/No Signal";
            case HIGH_VOLTAGE:
                return "High Voltage";
            case BURNT_DISCONNECTED:
                return "Battery Burnt/Disconnected";
            case DEEP_DISCHARGING:
                return "Deep Discharging";
            case OVERCHARGING:
                return "Overcharging";
            case VOLTAGE_VARIATION_LOW:
                return "Low Voltage";
            case VOLTAGE_VARIATION_HIGH:
                return "High Voltage";
            default:
                return alertSubType.name();
        }
    }

    /**
     * Formats panel-related metadata
     */
    private void formatPanelMetadata(StringBuilder comments, Map<String, Object> metadata, Alert.AlertSubType subType) {
        if (subType == Alert.AlertSubType.LOW_GENERATION) {
            Object solarPercent = metadata.get("solarPercent");
            if (solarPercent != null && !isBooleanFalse(solarPercent)) {
                String percentStr = safeToString(solarPercent);
                comments.append("Solar Generation Percentage");
                comments.append(": ");
                comments.append(percentStr);
                comments.append("%");
                comments.append("\n");
                comments.append("Issue");
                comments.append(": ");
                comments.append("Solar panel energy consumption is below 10% of total consumption.");
                comments.append("\n");
            }
            Object solarConsumption = metadata.get("solarConsumption");
            if (solarConsumption != null && !isBooleanFalse(solarConsumption)) {
                String consumptionStr = safeToString(solarConsumption);
                comments.append("Solar Consumption");
                comments.append(": ");
                comments.append(consumptionStr);
                comments.append(" kWh");
                comments.append("\n");
            }
            Object gridConsumption = metadata.get("gridConsumption");
            if (gridConsumption != null && !isBooleanFalse(gridConsumption)) {
                String consumptionStr = safeToString(gridConsumption);
                comments.append("Grid Consumption");
                comments.append(": ");
                comments.append(consumptionStr);
                comments.append(" kWh");
                comments.append("\n");
            }
        }
    }

    /**
     * Formats inverter-related metadata
     */
    private void formatInverterMetadata(StringBuilder comments, Map<String, Object> metadata, Alert.AlertSubType subType) {
        if (subType == Alert.AlertSubType.SHUTDOWN) {
            comments.append("Issue");
            comments.append(": ");
            comments.append("No signal or communication detected from RMS device for more than 2 consecutive days.");
            comments.append("\n");
            comments.append("Status");
            comments.append(": ");
            comments.append("Device appears to be offline or experiencing communication issues.");
            comments.append("\n");
        } else if (subType == Alert.AlertSubType.HIGH_VOLTAGE) {
            Object voltage = metadata.get("voltage");
            if (voltage != null && !isBooleanFalse(voltage)) {
                String voltageStr = safeToString(voltage);
                comments.append("Detected Voltage");
                comments.append(": ");
                comments.append(voltageStr);
                comments.append("V");
                comments.append("\n");
                comments.append("Threshold");
                comments.append(": ");
                comments.append("250V");
                comments.append("\n");
                comments.append("Issue");
                comments.append(": ");
                comments.append("UPS/PCU voltage is above safe operating threshold.");
                comments.append("\n");
            }
        }
    }

    /**
     * Formats battery-related metadata
     */
    private void formatBatteryMetadata(StringBuilder comments, Map<String, Object> metadata, Alert.AlertSubType subType) {
        if (subType == Alert.AlertSubType.BURNT_DISCONNECTED) {
            Object batteryVoltage = metadata.get("batteryVoltage");
            if (batteryVoltage != null && !isBooleanFalse(batteryVoltage)) {
                String voltageStr = safeToString(batteryVoltage);
                comments.append("Battery Voltage");
                comments.append(": ");
                comments.append(voltageStr);
                comments.append("V");
                comments.append("\n");
                comments.append("Issue");
                comments.append(": ");
                comments.append("Battery voltage detected as 0V - battery may be burnt, disconnected, or completely discharged.");
                comments.append("\n");
            }
        } else if (subType == Alert.AlertSubType.DEEP_DISCHARGING || subType == Alert.AlertSubType.OVERCHARGING) {
            Object batteryCharging = metadata.get("batteryCharging");
            if (batteryCharging != null && !isBooleanFalse(batteryCharging)) {
                String chargingStr = safeToString(batteryCharging);
                comments.append("Battery Charging");
                comments.append(": ");
                comments.append(chargingStr);
                comments.append(" kWh");
                comments.append("\n");
            }
            Object batteryDischarging = metadata.get("batteryDischarging");
            if (batteryDischarging != null && !isBooleanFalse(batteryDischarging)) {
                String dischargingStr = safeToString(batteryDischarging);
                comments.append("Battery Discharging");
                comments.append(": ");
                comments.append(dischargingStr);
                comments.append(" kWh");
                comments.append("\n");
            }
            Object batteryHealthInfo = metadata.get("batteryHealthInfo");
            if (batteryHealthInfo != null && !isBooleanFalse(batteryHealthInfo)) {
                String healthStr = safeToString(batteryHealthInfo);
                comments.append("Battery Health Status");
                comments.append(": ");
                comments.append(healthStr);
                comments.append("\n");
            }
            if (subType == Alert.AlertSubType.DEEP_DISCHARGING) {
                comments.append("Issue");
                comments.append(": ");
                comments.append("Battery is being discharged more than it is being charged, leading to degradation.");
                comments.append("\n");
            } else {
                comments.append("Issue");
                comments.append(": ");
                comments.append("Battery is being overcharged, which can cause damage and safety risks.");
                comments.append("\n");
            }
        }
    }

    /**
     * Formats grid-related metadata
     */
    private void formatGridMetadata(StringBuilder comments, Map<String, Object> metadata, Alert.AlertSubType subType) {
        if (subType == Alert.AlertSubType.VOLTAGE_VARIATION_LOW) {
            Object minVoltage = metadata.get("minVoltage");
            if (minVoltage != null && !isBooleanFalse(minVoltage)) {
                String voltageStr = safeToString(minVoltage);
                comments.append("Detected Grid Voltage");
                comments.append(": ");
                comments.append(voltageStr);
                comments.append("V");
                comments.append("\n");
                comments.append("Threshold");
                comments.append(": ");
                comments.append("200V");
                comments.append("\n");
                comments.append("Issue");
                comments.append(": ");
                comments.append("Grid voltage is below safe operating threshold.");
                comments.append("\n");
            }
        } else if (subType == Alert.AlertSubType.VOLTAGE_VARIATION_HIGH) {
            Object maxVoltage = metadata.get("maxVoltage");
            if (maxVoltage != null && !isBooleanFalse(maxVoltage)) {
                String voltageStr = safeToString(maxVoltage);
                comments.append("Detected Grid Voltage");
                comments.append(": ");
                comments.append(voltageStr);
                comments.append("V");
                comments.append("\n");
                comments.append("Threshold");
                comments.append(": ");
                comments.append("250V");
                comments.append("\n");
                comments.append("Issue");
                comments.append(": ");
                comments.append("Grid voltage is above safe operating threshold.");
                comments.append("\n");
            }
        }
    }

    /**
     * Builds additional detail JSON
     */
    private Map<String, Object> buildAdditionalDetail(Alert alert, FacilityDetails facilityDetails) {
        Map<String, Object> additionalDetail = new HashMap<>();
        additionalDetail.put("fileStoreId", new java.util.ArrayList<>());
        additionalDetail.put("reopenreason", new java.util.ArrayList<>());
        additionalDetail.put("rejectReason", new java.util.ArrayList<>());
        additionalDetail.put("sendBackReason", new java.util.ArrayList<>());
        additionalDetail.put("sendBackSubReason", new java.util.ArrayList<>());
        additionalDetail.put("rmsAlertId", alert.getId());
        additionalDetail.put("rmsFacilityId", alert.getFacilityId());
        additionalDetail.put("rmsMetadata", alert.getMetadata());
        return additionalDetail;
    }

    /**
     * Builds reporter user from system user
     */
    private IMServiceRequest.User buildReporter(RequestInfo requestInfo) {
        return IMServiceRequest.User.builder()
                .uuid("d2a12218-c71b-4aa3-b7e4-a811c522ef0c")
                .tenantId("pg")
                .build();
    }
}

