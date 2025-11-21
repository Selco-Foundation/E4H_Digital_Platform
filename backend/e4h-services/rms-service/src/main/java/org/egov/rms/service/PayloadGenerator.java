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
                    .tenantId(config.getDefaultTenantId())
                    .district("Raichur") // TODO: Extract from mapping or facility data
                    .block("Lingasugur") // TODO: Extract from mapping or facility data
                    .phcType(config.getDefaultTenantId())
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
                return "Cables";
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
                return "Low Generation (RMS data)";
            case SHUTDOWN:
                return "ShutdownInverter";
            case HIGH_VOLTAGE:
                return "VoltageInverter";
            case BURNT_DISCONNECTED:
                return "BurntorDamaged";
            case DEEP_DISCHARGING:
                return "DeepDischarge";
            case OVERCHARGING:
                return "DeepDischarge";
            case VOLTAGE_VARIATION_LOW:
                return "VoltageInverter";
            case VOLTAGE_VARIATION_HIGH:
                return "highvoltage";
            default:
                return "RMS Data Alert";
        }
    }

    /**
     * Builds comments for the ticket explaining what caused the trigger
     */
    private String buildComments(Alert alert, FacilityDetails facilityDetails) {
        StringBuilder comments = new StringBuilder();
        
        // Explain what caused the trigger based on alert type
        switch (alert.getAlertType()) {
            case PANEL:
                if (alert.getAlertSubType() == Alert.AlertSubType.LOW_GENERATION) {
                    comments.append("Panel Low Generation Alert:\n");
                    comments.append("Solar panel energy consumption has been less than 10% of total consumption ");
                    comments.append("(Solar + Grid) for 7 consecutive days.\n\n");
                    comments.append("This indicates that the solar panels are not generating sufficient power, ");
                    comments.append("requiring the facility to rely primarily on grid power.\n\n");
                }
                break;
            case INVERTER:
                if (alert.getAlertSubType() == Alert.AlertSubType.SHUTDOWN) {
                    comments.append("Inverter Shutdown Alert:\n");
                    comments.append("No signal or communication with RMS device detected for more than 2 consecutive days.\n\n");
                    comments.append("This indicates the inverter device may be offline, disconnected, or experiencing ");
                    comments.append("communication issues with the monitoring system.\n\n");
                } else if (alert.getAlertSubType() == Alert.AlertSubType.HIGH_VOLTAGE) {
                    comments.append("Inverter High Voltage Alert:\n");
                    comments.append("UPS/PCU voltage detected above 250V in single-phase AC system.\n\n");
                    comments.append("This indicates the inverter is experiencing high voltage conditions which can ");
                    comments.append("damage connected equipment and pose safety risks. Immediate attention is required.\n\n");
                    
                    // Extract voltage from metadata if available
                    if (alert.getMetadata() != null && alert.getMetadata().contains("\"voltage\"")) {
                        try {
                            // Simple extraction - voltage value should be in metadata
                            int voltageStart = alert.getMetadata().indexOf("\"voltage\":") + 10;
                            int voltageEnd = alert.getMetadata().indexOf(",", voltageStart);
                            if (voltageEnd == -1) voltageEnd = alert.getMetadata().indexOf("}", voltageStart);
                            if (voltageEnd > voltageStart) {
                                String voltageStr = alert.getMetadata().substring(voltageStart, voltageEnd).trim();
                                comments.append("Detected Voltage: ").append(voltageStr).append("V\n");
                                comments.append("Threshold: 250V\n");
                            }
                        } catch (Exception e) {
                            // Ignore parsing errors
                        }
                    }
                }
                break;
            case BATTERY:
                if (alert.getAlertSubType() == Alert.AlertSubType.BURNT_DISCONNECTED) {
                    comments.append("Battery Burnt/Disconnected Alert:\n");
                    comments.append("Battery voltage detected as 0V, indicating the battery may be burnt, ");
                    comments.append("disconnected, or completely discharged.\n\n");
                    comments.append("This condition requires immediate attention as the battery is not providing ");
                    comments.append("any power backup, leaving the facility dependent solely on grid power.\n\n");
                    
                    // Extract voltage from metadata if available
                    if (alert.getMetadata() != null && alert.getMetadata().contains("\"batteryVoltage\"")) {
                        try {
                            int voltageStart = alert.getMetadata().indexOf("\"batteryVoltage\":") + 17;
                            int voltageEnd = alert.getMetadata().indexOf(",", voltageStart);
                            if (voltageEnd == -1) voltageEnd = alert.getMetadata().indexOf("}", voltageStart);
                            if (voltageEnd > voltageStart) {
                                String voltageStr = alert.getMetadata().substring(voltageStart, voltageEnd).trim();
                                comments.append("Detected Battery Voltage: ").append(voltageStr).append("V\n");
                            }
                        } catch (Exception e) {
                            // Ignore parsing errors
                        }
                    }
                } else if (alert.getAlertSubType() == Alert.AlertSubType.DEEP_DISCHARGING || 
                          alert.getAlertSubType() == Alert.AlertSubType.OVERCHARGING) {
                    comments.append("Battery Deep Discharging/Overcharging Alert:\n");
                    comments.append("Abnormal battery charging vs discharging pattern detected over 2-3 days.\n\n");
                    
                    if (alert.getAlertSubType() == Alert.AlertSubType.DEEP_DISCHARGING) {
                        comments.append("The battery is experiencing deep discharging, where it is being ");
                        comments.append("discharged more than it is being charged. This can lead to battery ");
                        comments.append("degradation and reduced lifespan.\n\n");
                    } else {
                        comments.append("The battery is experiencing overcharging, where it is being charged ");
                        comments.append("excessively. This can cause battery damage, overheating, and safety risks.\n\n");
                    }
                    
                    // Extract battery health info from metadata
                    if (alert.getMetadata() != null) {
                        try {
                            if (alert.getMetadata().contains("\"batteryCharging\"")) {
                                int chargingStart = alert.getMetadata().indexOf("\"batteryCharging\":") + 18;
                                int chargingEnd = alert.getMetadata().indexOf(",", chargingStart);
                                if (chargingEnd == -1) chargingEnd = alert.getMetadata().indexOf("}", chargingStart);
                                if (chargingEnd > chargingStart) {
                                    String chargingStr = alert.getMetadata().substring(chargingStart, chargingEnd).trim();
                                    comments.append("Battery Charging: ").append(chargingStr).append(" kWh\n");
                                }
                            }
                            
                            if (alert.getMetadata().contains("\"batteryDischarging\"")) {
                                int dischargingStart = alert.getMetadata().indexOf("\"batteryDischarging\":") + 21;
                                int dischargingEnd = alert.getMetadata().indexOf(",", dischargingStart);
                                if (dischargingEnd == -1) dischargingEnd = alert.getMetadata().indexOf("}", dischargingStart);
                                if (dischargingEnd > dischargingStart) {
                                    String dischargingStr = alert.getMetadata().substring(dischargingStart, dischargingEnd).trim();
                                    comments.append("Battery Discharging: ").append(dischargingStr).append(" kWh\n");
                                }
                            }
                            
                            if (alert.getMetadata().contains("\"batteryHealthInfo\"")) {
                                int infoStart = alert.getMetadata().indexOf("\"batteryHealthInfo\":\"") + 20;
                                int infoEnd = alert.getMetadata().indexOf("\"", infoStart);
                                if (infoEnd > infoStart) {
                                    String infoStr = alert.getMetadata().substring(infoStart, infoEnd);
                                    comments.append("Abnormality Type: ").append(infoStr).append("\n");
                                }
                            }
                        } catch (Exception e) {
                            // Ignore parsing errors
                        }
                    }
                }
                break;
            case GRID:
                if (alert.getAlertSubType() == Alert.AlertSubType.VOLTAGE_VARIATION_LOW) {
                    comments.append("Grid Low Voltage Alert:\n");
                    comments.append("Grid meter voltage detected below 200V.\n\n");
                    comments.append("Low grid voltage can cause equipment malfunction, reduced efficiency, ");
                    comments.append("and potential damage to electrical devices. This condition requires ");
                    comments.append("immediate attention to ensure proper power supply to the facility.\n\n");
                    
                    // Extract voltage from metadata if available
                    if (alert.getMetadata() != null) {
                        try {
                            if (alert.getMetadata().contains("\"minVoltage\"")) {
                                int voltageStart = alert.getMetadata().indexOf("\"minVoltage\":") + 13;
                                int voltageEnd = alert.getMetadata().indexOf(",", voltageStart);
                                if (voltageEnd == -1) voltageEnd = alert.getMetadata().indexOf("}", voltageStart);
                                if (voltageEnd > voltageStart) {
                                    String voltageStr = alert.getMetadata().substring(voltageStart, voltageEnd).trim();
                                    comments.append("Detected Grid Voltage: ").append(voltageStr).append("V\n");
                                    comments.append("Threshold: 200V\n");
                                }
                            }
                        } catch (Exception e) {
                            // Ignore parsing errors
                        }
                    }
                } else if (alert.getAlertSubType() == Alert.AlertSubType.VOLTAGE_VARIATION_HIGH) {
                    comments.append("Grid High Voltage Alert:\n");
                    comments.append("Grid meter voltage detected above 250V.\n\n");
                    comments.append("High grid voltage can cause equipment damage, overheating, and safety risks. ");
                    comments.append("This condition requires immediate attention to prevent damage to electrical ");
                    comments.append("devices and ensure safe operation of the facility.\n\n");
                    
                    // Extract voltage from metadata if available
                    if (alert.getMetadata() != null) {
                        try {
                            if (alert.getMetadata().contains("\"maxVoltage\"")) {
                                int voltageStart = alert.getMetadata().indexOf("\"maxVoltage\":") + 13;
                                int voltageEnd = alert.getMetadata().indexOf(",", voltageStart);
                                if (voltageEnd == -1) voltageEnd = alert.getMetadata().indexOf("}", voltageStart);
                                if (voltageEnd > voltageStart) {
                                    String voltageStr = alert.getMetadata().substring(voltageStart, voltageEnd).trim();
                                    comments.append("Detected Grid Voltage: ").append(voltageStr).append("V\n");
                                    comments.append("Threshold: 250V\n");
                                }
                            }
                        } catch (Exception e) {
                            // Ignore parsing errors
                        }
                    }
                }
                break;
        }
        
        // Add facility and detection info
        comments.append("Facility ID: ").append(alert.getFacilityId()).append("\n");
        if (alert.getHfrId() != null && !alert.getHfrId().isEmpty()) {
            comments.append("HFR ID: ").append(alert.getHfrId()).append("\n");
        }
        comments.append("Alert Detected at: ").append(alert.getDetectedAt()).append("\n");
        
        // Add detailed metadata if available
        if (alert.getMetadata() != null && !alert.getMetadata().isEmpty()) {
            comments.append("\nTechnical Details:\n");
            comments.append(alert.getMetadata());
        }
        
        return comments.toString();
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

