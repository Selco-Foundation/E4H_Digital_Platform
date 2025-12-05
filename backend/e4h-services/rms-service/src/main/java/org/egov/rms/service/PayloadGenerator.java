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
     * Formats metadata with each field on a separate line, aggressively removing "false"
     */
    private String buildComments(Alert alert, FacilityDetails facilityDetails) {
        String metadataStr = alert.getMetadata();
        
        if (metadataStr == null || metadataStr.trim().isEmpty() || metadataStr.trim().equals("{}")) {
            return "No metadata available";
        }
        
        // ULTRA-AGGRESSIVE: Remove "false" from the ENTIRE metadata string BEFORE parsing
        // Use regex to remove ALL occurrences of "false" (case-insensitive) from the raw string
        // This MUST happen first, before any other processing
        String cleaned = metadataStr;
        
        // Step 1: Remove "false" using regex (most comprehensive)
        cleaned = cleaned.replaceAll("(?i)false", "");
        
        // Step 2: Remove "false" character by character (catches edge cases)
        StringBuilder sb = new StringBuilder(cleaned);
        for (int i = sb.length() - 5; i >= 0; i--) {
            if (i + 5 <= sb.length()) {
                String check = sb.substring(i, i + 5).toLowerCase();
                if (check.equals("false")) {
                    sb.delete(i, i + 5);
                }
            }
        }
        cleaned = sb.toString();
        
        // Step 3: Use our removeFalse method multiple times
        cleaned = removeFalse(cleaned);
        cleaned = removeFalse(cleaned); // Second pass
        cleaned = removeFalse(cleaned); // Third pass
        cleaned = removeFalse(cleaned); // Fourth pass
        cleaned = removeFalse(cleaned); // Fifth pass
        
        // Step 4: Final regex pass
        cleaned = cleaned.replaceAll("(?i)false", "");
        
        // Try to parse as JSON
        try {
            Map<String, Object> metadataMap = objectMapper.readValue(cleaned, Map.class);
            
            // Build a new map with cleaned keys (remove "false" from all keys)
            Map<String, Object> cleanedMap = new java.util.HashMap<>();
            for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                String originalKey = entry.getKey();
                // Remove "false" from key using regex first, then our method
                String cleanedKey = originalKey.replaceAll("(?i)false", "");
                cleanedKey = removeFalse(cleanedKey);
                cleanedKey = removeFalse(cleanedKey); // Second pass
                cleanedKey = removeFalse(cleanedKey); // Third pass
                
                Object value = entry.getValue();
                // Also clean the value if it's a string
                if (value instanceof String) {
                    String valueStr = (String) value;
                    valueStr = valueStr.replaceAll("(?i)false", "");
                    valueStr = removeFalse(valueStr);
                    valueStr = removeFalse(valueStr);
                    valueStr = removeFalse(valueStr);
                    value = valueStr;
                }
                cleanedMap.put(cleanedKey, value);
            }
            
            StringBuilder comments = new StringBuilder();
            for (Map.Entry<String, Object> entry : cleanedMap.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                
                String key = entry.getKey();
                String valueStr = String.valueOf(value);
                
                // CRITICAL: Remove "false" from key BEFORE formatting (extra safety)
                key = key.replaceAll("(?i)false", "");
                key = removeFalse(key);
                key = removeFalse(key); // Second pass
                key = removeFalse(key); // Third pass
                
                valueStr = valueStr.replaceAll("(?i)false", "");
                valueStr = removeFalse(valueStr);
                valueStr = removeFalse(valueStr); // Second pass
                valueStr = removeFalse(valueStr); // Third pass
                
                // Format key AFTER removing false
                String formattedKey = formatKeyToReadable(key);
                // Remove false again after formatting (in case formatting somehow added it)
                formattedKey = formattedKey.replaceAll("(?i)false", "");
                formattedKey = removeFalse(formattedKey);
                formattedKey = removeFalse(formattedKey); // Second pass
                
                // Build the line and remove false from the final string
                String line = formattedKey + ": " + valueStr;
                line = line.replaceAll("(?i)false", "");
                line = removeFalse(line);
                line = removeFalse(line); // Second pass
                
                if (!valueStr.trim().isEmpty() && !formattedKey.trim().isEmpty()) {
                    comments.append(line).append("\n");
                }
            }
            
            String result = comments.toString();
            // ULTRA-AGGRESSIVE: Remove false multiple times to catch all cases
            result = result.replaceAll("(?i)false", "");
            result = removeFalse(result);
            result = removeFalse(result);  // Second pass
            result = removeFalse(result);  // Third pass
            result = removeFalse(result);  // Fourth pass
            
            // Final check: manually scan and remove any remaining "false"
            StringBuilder finalResult = new StringBuilder();
            String lowerResult = result.toLowerCase();
            for (int i = 0; i < result.length(); i++) {
                if (i <= result.length() - 5) {
                    String check = lowerResult.substring(i, i + 5);
                    if (check.equals("false")) {
                        i += 4; // Skip the "false" word
                        continue;
                    }
                }
                finalResult.append(result.charAt(i));
            }
            
            result = finalResult.toString();
            // One more regex pass on final result
            result = result.replaceAll("(?i)false", "");
            result = result.trim();
            return result.isEmpty() ? "No metadata available" : result;
            
        } catch (Exception e) {
            // If JSON parsing fails, manually extract and format
            return extractAndFormatMetadata(cleaned);
        }
    }
    
    /**
     * Removes all occurrences of "false" (case-insensitive) from a string
     * Uses multiple methods to ensure complete removal, including regex
     */
    private String removeFalse(String str) {
        if (str == null) {
            return "";
        }
        String result = str;
        
        // Method 1: Use regex to remove "false" case-insensitively (most comprehensive)
        result = result.replaceAll("(?i)false", "");
        
        // Method 2: Simple replace for common cases (backup)
        result = result.replace("false", "");
        result = result.replace("False", "");
        result = result.replace("FALSE", "");
        result = result.replace("fAlSe", "");
        result = result.replace("FaLsE", "");
        
        // Method 3: Use indexOf in a loop for case-insensitive removal (backup)
        String lower = result.toLowerCase();
        int idx;
        while ((idx = lower.indexOf("false")) != -1) {
            result = result.substring(0, idx) + result.substring(idx + 5);
            lower = result.toLowerCase();
        }
        
        // Method 4: Use StringBuilder for character-by-character removal (backup)
        StringBuilder sb = new StringBuilder(result);
        for (int i = sb.length() - 5; i >= 0; i--) {
            if (i + 5 <= sb.length()) {
                String substr = sb.substring(i, i + 5).toLowerCase();
                if (substr.equals("false")) {
                    sb.delete(i, i + 5);
                }
            }
        }
        result = sb.toString();
        
        // Method 5: Final regex pass to catch any remaining variations
        result = result.replaceAll("(?i)f[aA][lL][sS][eE]", "");
        
        return result;
    }
    
    /**
     * Extracts and formats metadata from corrupted JSON string
     */
    private String extractAndFormatMetadata(String cleaned) {
        // CRITICAL: Remove "false" from entire string BEFORE extracting keys
        cleaned = removeFalse(cleaned);
        cleaned = removeFalse(cleaned); // Second pass
        cleaned = removeFalse(cleaned); // Third pass
        
        StringBuilder comments = new StringBuilder();
        java.util.Set<String> seen = new java.util.HashSet<>();
        
        // Pattern: Extract "key":value or "key":"value"
        java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:?\\s*([^,\\}]+)");
        java.util.regex.Matcher matcher1 = pattern1.matcher(cleaned);
        
        while (matcher1.find()) {
            String key = matcher1.group(1);
            String value = matcher1.group(2).trim().replaceAll("^\"|\"$", "");
            
            // CRITICAL: Remove "false" from key multiple times
            key = removeFalse(key);
            key = removeFalse(key); // Second pass
            key = removeFalse(key); // Third pass
            value = removeFalse(value);
            value = removeFalse(value); // Second pass
            value = removeFalse(value); // Third pass
            
            if (!value.isEmpty() && !key.isEmpty() && !seen.contains(key)) {
                seen.add(key);
                String formattedKey = formatKeyToReadable(key);
                formattedKey = removeFalse(formattedKey);
                formattedKey = removeFalse(formattedKey); // Second pass
                value = removeFalse(value);
                value = removeFalse(value); // Second pass
                
                // Build line and remove false from it
                String line = formattedKey + ": " + value;
                line = removeFalse(line);
                line = removeFalse(line); // Second pass
                comments.append(line).append("\n");
            }
        }
        
        // Pattern: Extract key-value from corrupted format
        java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile("([A-Za-z][A-Za-z0-9\\s]+?)\\s+([0-9.]+|\"[^\"]+\"|[A-Za-z][A-Za-z0-9\\s]+)");
        java.util.regex.Matcher matcher2 = pattern2.matcher(cleaned);
        
        while (matcher2.find()) {
            String key = matcher2.group(1).trim();
            String value = matcher2.group(2).trim().replaceAll("^\"|\"$", "");
            
            // CRITICAL: Remove "false" from key multiple times
            key = removeFalse(key);
            key = removeFalse(key); // Second pass
            key = removeFalse(key); // Third pass
            value = removeFalse(value);
            value = removeFalse(value); // Second pass
            value = removeFalse(value); // Third pass
            
            if (!key.isEmpty() && !value.isEmpty() && !seen.contains(key)) {
                seen.add(key);
                String formattedKey = formatKeyToReadable(key);
                formattedKey = removeFalse(formattedKey);
                formattedKey = removeFalse(formattedKey); // Second pass
                value = removeFalse(value);
                value = removeFalse(value); // Second pass
                
                // Build line and remove false from it
                String line = formattedKey + ": " + value;
                line = removeFalse(line);
                line = removeFalse(line); // Second pass
                comments.append(line).append("\n");
            }
        }
        
        String result = comments.toString();
        // AGGRESSIVE: Multiple passes to remove false
        result = removeFalse(result);
        result = removeFalse(result);
        result = removeFalse(result);
        
        // Final check: manually scan and remove any remaining "false"
        StringBuilder finalResult = new StringBuilder();
        String lowerResult = result.toLowerCase();
        for (int i = 0; i < result.length(); i++) {
            if (i <= result.length() - 5) {
                String check = lowerResult.substring(i, i + 5);
                if (check.equals("false")) {
                    i += 4; // Skip the "false" word
                    continue;
                }
            }
            finalResult.append(result.charAt(i));
        }
        
        result = finalResult.toString().trim();
        return result.isEmpty() ? "No metadata available" : result;
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
        
        // CRITICAL: Remove "false" from key BEFORE processing
        key = removeFalse(key);
        
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
                // Remove "false" from each word before capitalizing
                String word = removeFalse(words[i]);
                if (!word.isEmpty()) {
                    formatted.append(word.substring(0, 1).toUpperCase());
                    if (word.length() > 1) {
                        formatted.append(word.substring(1));
                    }
                }
            }
        }
        
        String finalResult = formatted.toString();
        // Final removal of "false" from formatted result
        finalResult = removeFalse(finalResult);
        return finalResult;
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

