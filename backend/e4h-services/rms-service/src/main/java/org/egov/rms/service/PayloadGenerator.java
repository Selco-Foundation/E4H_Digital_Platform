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
                    .district("Bagalkote") // TODO: Extract from mapping or facility data
                    .block("Bagalkot") // TODO: Extract from mapping or facility data
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
     * Formats metadata with each field on a separate line, fixing corrupted JSONB conversion
     */
    private String buildComments(Alert alert, FacilityDetails facilityDetails) {
        String metadataStr = alert.getMetadata();
        
        if (metadataStr == null || metadataStr.trim().isEmpty() || metadataStr.trim().equals("{}")) {
            return "No metadata available";
        }
        
        // Step 1: Fix corrupted JSON patterns caused by JSONB::text conversion
        // Pattern: "key"falsevalue should become "key":value
        String fixedJson = metadataStr
            .replaceAll("\"([^\"]+)\"false([^,\"\\}\\]]+)", "\"$1\":$2")  // Fix "key"falsevalue -> "key":value
            .replaceAll("\"([^\"]+)\"false\"([^\"]+)\"", "\"$1\":\"$2\"")  // Fix "key"false"value" -> "key":"value"
            .replaceAll("([0-9.]+),\"([^\"]+)\"false", "$1,\"$2\":")  // Fix number,"key"false
            .replaceAll(",\"([^\"]+)\"false([^,\"\\}\\]]+)", ",\"$1\":$2")  // Fix ,"key"falsevalue
            .replaceAll(":\\s*false", ":")  // Remove :false
            .replaceAll(",\\s*false", ",")  // Remove ,false
            .replaceAll("false\\s*,", ",")  // Remove false,
            .replaceAll("false\\s*:", ":")  // Remove false:
            .replaceAll("false\\s*}", "}")  // Remove false}
            .replaceAll("false", "");  // Remove any remaining false
        
        // Step 2: Try to parse the fixed JSON
        try {
            Map<String, Object> metadataMap = objectMapper.readValue(fixedJson, Map.class);
            
            // Build formatted output with each field on a separate line
            StringBuilder comments = new StringBuilder();
            for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                
                // Format key to readable text
                String key = formatKeyToReadable(entry.getKey());
                String valueStr = String.valueOf(value);
                
                // Skip if value is empty
                if (valueStr.trim().isEmpty()) {
                    continue;
                }
                
                // Append as "Key: Value" on each line
                comments.append(key).append(": ").append(valueStr).append("\n");
            }
            
            String result = comments.toString().trim();
            return result.isEmpty() ? "No metadata available" : result;
            
        } catch (Exception e) {
            // If parsing still fails, extract key-value pairs manually using regex
            StringBuilder comments = new StringBuilder();
            
            // Extract patterns like "key":value or "key":"value"
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:\\s*([^,\\}]+)");
            java.util.regex.Matcher matcher = pattern.matcher(fixedJson);
            
            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2).trim();
                
                // Remove quotes from value if present
                value = value.replaceAll("^\"|\"$", "");
                
                // Skip if empty
                if (value.isEmpty()) {
                    continue;
                }
                
                // Format key and append
                String formattedKey = formatKeyToReadable(key);
                comments.append(formattedKey).append(": ").append(value).append("\n");
            }
            
            String result = comments.toString().trim();
            return result.isEmpty() ? "No metadata available" : result;
        }
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
                .uuid("fb022833-743d-43cb-adfa-312fbd13f438")
                .tenantId("in")
                .build();
    }
}

