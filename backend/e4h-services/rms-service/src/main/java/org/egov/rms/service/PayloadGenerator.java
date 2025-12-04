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
                    .tenantId("in")
                    .district("BAGALKOTE") // TODO: Extract from mapping or facility data
                    .block("BAGALKOT") // TODO: Extract from mapping or facility data
                    .comments(buildComments(alert, null))
                    .systemFunctional("FUNCTIONAL")
                    .boundaryCode("India_Karnataka_Bagalkote_Bagalkot_FAC/2025/5329")
                    .source("RMS")
                    .reporterType("RMS")
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
                return "PANEL";
            case INVERTER:
                return "INVERTER";
            case BATTERY:
                return "BATTERY";
            case GRID:
                return "GRID";
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
     * Builds simple comments for the ticket from metadata in active_alerts
     * Just converts JSON metadata to plain text, one field per line
     */
    private String buildComments(Alert alert, FacilityDetails facilityDetails) {
        StringBuilder comments = new StringBuilder();
        
        // Parse and format metadata as plain text
        if (alert.getMetadata() != null && !alert.getMetadata().isEmpty()) {
            try {
                // Parse JSON metadata
                Map<String, Object> metadataMap = objectMapper.readValue(alert.getMetadata(), Map.class);
                
                // Iterate through all metadata fields and display as plain text
                for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    
                    // Skip null values and boolean false
                    if (value == null || isBooleanFalse(value)) {
                        continue;
                    }
                    
                    // Format key to readable text (convert camelCase to Title Case)
                    String readableKey = formatKeyToReadable(key);
                    String valueStr = safeToString(value);
                    
                    // Append as "Key: Value" on each line
                    comments.append(readableKey);
                    comments.append(": ");
                    comments.append(valueStr);
                    comments.append("\n");
                }
                
            } catch (Exception e) {
                log.warn("Error parsing metadata JSON, using raw metadata: {}", e.getMessage());
                comments.append("Metadata: ");
                comments.append(alert.getMetadata());
                comments.append("\n");
            }
                    } else {
            comments.append("No metadata available");
            comments.append("\n");
        }
        
        // Strip the word "false" from the comment string and replace with blank space
        String result = comments.toString();
        result = result.replace("false", " ");
        // Clean up multiple consecutive spaces (but preserve newlines)
        result = result.replaceAll("[ \\t]+", " ");
        // Clean up any space before colon
        result = result.replace(" :", ":");
        
        return result;
    }
    
    /**
     * Converts camelCase or snake_case keys to readable Title Case
     */
    private String formatKeyToReadable(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }
        
        // Replace underscores with spaces
        String result = key.replace("_", " ");
        
        // Convert camelCase to Title Case
        result = result.replaceAll("([a-z])([A-Z])", "$1 $2");
        
        // Capitalize first letter of each word
        String[] words = result.split(" ");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                formatted.append(" ");
            }
            if (!words[i].isEmpty()) {
                formatted.append(words[i].substring(0, 1).toUpperCase());
                if (words[i].length() > 1) {
                    formatted.append(words[i].substring(1));
                }
            }
        }
        
        return formatted.toString();
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

