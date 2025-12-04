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
        String metadataStr = alert.getMetadata();
        if (metadataStr != null && !metadataStr.trim().isEmpty()) {
            log.debug("Processing metadata for alert {}: {}", alert.getId(), metadataStr);
            
            try {
                // Parse JSON metadata
                Map<String, Object> metadataMap = objectMapper.readValue(metadataStr, Map.class);
                
                log.debug("Parsed metadata map for alert {}: {} entries", alert.getId(), metadataMap.size());
                
                if (metadataMap == null || metadataMap.isEmpty()) {
                    log.warn("Metadata map is empty for alert {}", alert.getId());
                    comments.append("Metadata: ").append(metadataStr).append("\n");
                } else {
                    // Iterate through all metadata fields and display as plain text
                    for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        
                        // Skip only null values
                        if (value == null) {
                            log.debug("Skipping null value for key: {}", key);
                            continue;
                        }
                        
                        // Format key to readable text (convert camelCase to Title Case)
                        String readableKey = formatKeyToReadable(key);
                        String valueStr = convertValueToString(value);
                        
                        // Append as "Key: Value" on each line (include even if value is empty string)
                        comments.append(readableKey);
                        comments.append(": ");
                        comments.append(valueStr);
                        comments.append("\n");
                    }
                }
                
                log.debug("Built comments for alert {}: {} characters", alert.getId(), comments.length());
                
            } catch (Exception e) {
                log.error("Error parsing metadata JSON for alert {}: {}", alert.getId(), e.getMessage(), e);
                // Fallback: use raw metadata
                comments.append("Metadata: ");
                comments.append(metadataStr);
                comments.append("\n");
            }
        } else {
            log.warn("No metadata available for alert {}", alert.getId());
            comments.append("No metadata available");
            comments.append("\n");
        }
        
        // Strip the word "false" from the comment string and replace with blank space
        String result = comments.toString();
        if (result == null || result.trim().isEmpty()) {
            log.warn("Comments are empty for alert {}, using fallback", alert.getId());
            result = "No metadata available\n";
        }
        
        // Replace the string "false" (case-insensitive) with a space
        result = result.replaceAll("(?i)\\bfalse\\b", " ");
        // Clean up multiple consecutive spaces (but preserve newlines)
        result = result.replaceAll("[ \\t]+", " ");
        // Clean up any space before colon
        result = result.replace(" :", ":");
        // Ensure space after colon (but not if it already has one)
        result = result.replaceAll(":(?! )", ": ");
        // Remove duplicate space after colon if created
        result = result.replace(":  ", ": ");
        
        log.debug("Final comments for alert {}: {} characters", alert.getId(), result.length());
        return result;
    }
    
    /**
     * Converts a value to string, handling all types including boolean false
     */
    private String convertValueToString(Object value) {
        if (value == null) {
            return "";
        }
        // For boolean false, return empty string (will still show the key)
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "true" : "";
        }
        // For all other types, convert to string
        return value.toString();
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

