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
                    .district("BAGALKOTE") // TODO: Extract from mapping or facility data
                    .block("Bagalkot") // TODO: Extract from mapping or facility data
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
                return "Shutdown (RMS data)";
            case HIGH_VOLTAGE:
                return "High Voltage (RMS data)";
            case BURNT_DISCONNECTED:
                return "Burnt/Disconnected (RMS data)";
            case DEEP_DISCHARGING:
                return "Deep Discharging / Overcharging (RMS data)";
            case OVERCHARGING:
                return "Deep Discharging / Overcharging (RMS data)";
            case VOLTAGE_VARIATION_LOW:
                return "Voltage Variation - Low (RMS data)";
            case VOLTAGE_VARIATION_HIGH:
                return "Voltage Variation - High (RMS data)";
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
                }
                break;
            case BATTERY:
                if (alert.getAlertSubType() == Alert.AlertSubType.BURNT_DISCONNECTED) {
                    comments.append("Battery Alert:\n");
                    comments.append("Battery voltage detected as 0, indicating the battery may be burnt or disconnected.\n\n");
                }
                break;
            case GRID:
                comments.append("Grid Voltage Alert:\n");
                if (alert.getAlertSubType() == Alert.AlertSubType.VOLTAGE_VARIATION_LOW) {
                    comments.append("Grid voltage is below 200V (Low Voltage).\n\n");
                } else if (alert.getAlertSubType() == Alert.AlertSubType.VOLTAGE_VARIATION_HIGH) {
                    comments.append("Grid voltage is above 250V (High Voltage).\n\n");
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

