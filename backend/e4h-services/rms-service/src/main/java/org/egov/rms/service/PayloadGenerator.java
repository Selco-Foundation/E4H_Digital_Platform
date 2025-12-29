package org.egov.rms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
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
            FacilityDetails facilityDetails = facilityServiceClient.getFacilityByHfrId(
                    alert.getHfrId(), config.getDefaultTenantId());

            if (facilityDetails == null) {
                log.warn("Facility not found for hfrId: {}", alert.getHfrId());
                return null;
            }

            // Map alert type/subtype to IM service incident type/subtype
            String incidentType = mapAlertTypeToIncidentType(alert.getAlertType());
            String incidentSubType = mapAlertSubTypeToIncidentSubType(alert.getAlertSubType(), alert.getAlertType());

            // Extract boundaryCode from facility details, fallback to hardcoded value if not available
            String boundaryCode = facilityDetails.getBoundaryCode();
            if (boundaryCode == null || boundaryCode.trim().isEmpty()) {
                log.warn("BoundaryCode not found for facility hfrId: {}, using fallback", alert.getHfrId());
                boundaryCode = "India_Karnataka_Bagalkote_Bagalkot_FAC/2025/5329";
            }

            // Build incident payload
            IMServiceRequest.Incident incident = IMServiceRequest.Incident.builder()
                    .incidentType(incidentType)
                    .incidentSubType(incidentSubType)
                    .tenantId("in")
                    .comments(buildComments(alert, facilityDetails))
                    .systemFunctional("FUNCTIONAL")
                    .boundaryCode(boundaryCode)
                    .source("RMS")
                    .reporterType("RMS")
                    .additionalDetail(buildAdditionalDetail(alert, facilityDetails))
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
     * Formats metadata with each field on a separate line, filtering out boolean false values
     */
    private String buildComments(Alert alert, FacilityDetails facilityDetails) {
        String metadataStr = alert.getMetadata();
        
        // Add detailed logging
        log.info("=== BUILDING COMMENTS ===");
        log.info("Alert ID: {}", alert.getId());
        log.info("Metadata string: {}", metadataStr);
        log.info("Metadata is null: {}", metadataStr == null);
        log.info("Metadata is empty: {}", metadataStr != null && metadataStr.trim().isEmpty());
        log.info("Metadata equals '{}': {}", metadataStr != null && metadataStr.trim().equals("{}"));
        
        if (metadataStr == null || metadataStr.trim().isEmpty() || metadataStr.trim().equals("{}")) {
            log.error("ERROR: Returning 'No metadata available' - metadataStr: '{}' for alert {}", metadataStr, alert.getId());
            return "No metadata available";
        }
        
        log.info("Raw metadata string (length: {}): {}", metadataStr.length(), metadataStr);
        
        // Parse JSON first (database structure is correct, so parse directly)
        try {
            Map<String, Object> metadataMap = objectMapper.readValue(metadataStr, Map.class);
            log.debug("Parsed metadata map: {}", metadataMap);
            
            // Build comments, filtering out null, boolean false values, and facilityName
            StringBuilder comments = new StringBuilder();
            for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                Object value = entry.getValue();
                
                String key = entry.getKey();
                
                // Skip null values, boolean false, and facilityName
                if (value == null || (value instanceof Boolean && !((Boolean) value))) {
                    continue;
                }
                
                // Skip facilityName field as it should not appear in comments
                if ("facilityName".equalsIgnoreCase(key) || "facility_name".equalsIgnoreCase(key)) {
                    continue;
                }
                String valueStr;
                
                // Handle string values - remove escaped quotes if present
                if (value instanceof String) {
                    valueStr = (String) value;
                    // Remove surrounding quotes if the string value itself contains quotes
                    if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
                        valueStr = valueStr.substring(1, valueStr.length() - 1);
                    }
                } else {
                    valueStr = String.valueOf(value);
                }
                
                // Format key to readable format
                String formattedKey = formatKeyToReadable(key);
                
                // Build the line
                String line = formattedKey + ": " + valueStr;
                comments.append(line).append("\n");
            }
            
            String result = comments.toString().trim();
            return result.isEmpty() ? "No metadata available" : result;
            
        } catch (Exception e) {
            log.error("Error parsing metadata JSON: {}", metadataStr, e);
            // If JSON parsing fails, manually extract and format
            return extractAndFormatMetadata(metadataStr);
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
        log.info("Extracting metadata from corrupted JSON string: {}", cleaned);
        
        // ULTRA-AGGRESSIVE: Remove "false" from entire string BEFORE extracting keys
        cleaned = cleaned.replaceAll("(?i)false", "");
        cleaned = removeFalse(cleaned);
        cleaned = removeFalse(cleaned); // Second pass
        cleaned = removeFalse(cleaned); // Third pass
        cleaned = removeFalse(cleaned); // Fourth pass
        cleaned = cleaned.replaceAll("(?i)false", ""); // Final regex pass
        
        StringBuilder comments = new StringBuilder();
        java.util.Set<String> seen = new java.util.HashSet<>();
        
        // Pattern: Extract "key":value or "key":"value"
        java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:?\\s*([^,\\}]+)");
        java.util.regex.Matcher matcher1 = pattern1.matcher(cleaned);
        
            while (matcher1.find()) {
            String key = matcher1.group(1);
            String value = matcher1.group(2).trim().replaceAll("^\"|\"$", "");
            
            // Skip facilityName field as it should not appear in comments
            if ("facilityName".equalsIgnoreCase(key) || "facility_name".equalsIgnoreCase(key)) {
                continue;
            }
            
            // ULTRA-AGGRESSIVE: Remove "false" from key using regex first
            key = key.replaceAll("(?i)false", "");
            key = removeFalse(key);
            key = removeFalse(key); // Second pass
            key = removeFalse(key); // Third pass
            key = removeFalse(key); // Fourth pass
            key = key.replaceAll("(?i)false", ""); // Final regex pass
            
            value = value.replaceAll("(?i)false", "");
            value = removeFalse(value);
            value = removeFalse(value); // Second pass
            value = removeFalse(value); // Third pass
            value = removeFalse(value); // Fourth pass
            value = value.replaceAll("(?i)false", ""); // Final regex pass
            
            if (!value.isEmpty() && !key.isEmpty() && !seen.contains(key)) {
                seen.add(key);
                String formattedKey = formatKeyToReadable(key);
                formattedKey = formattedKey.replaceAll("(?i)false", "");
                formattedKey = removeFalse(formattedKey);
                formattedKey = removeFalse(formattedKey); // Second pass
                formattedKey = formattedKey.replaceAll("(?i)false", ""); // Final regex pass
                
                value = value.replaceAll("(?i)false", "");
                value = removeFalse(value);
                value = removeFalse(value); // Second pass
                value = value.replaceAll("(?i)false", ""); // Final regex pass
                
                // Build line and remove false from it
                String line = formattedKey + ": " + value;
                line = line.replaceAll("(?i)false", "");
                line = removeFalse(line);
                line = removeFalse(line); // Second pass
                line = line.replaceAll("(?i)false", ""); // Final regex pass
                comments.append(line).append("\n");
            }
        }
        
        // Pattern: Extract key-value from corrupted format (like "Thresholdfalse 250")
        java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile("([A-Za-z][A-Za-z0-9\\s]*?)\\s+([0-9.]+|\"[^\"]+\"|[A-Za-z][A-Za-z0-9\\s]*)");
        java.util.regex.Matcher matcher2 = pattern2.matcher(cleaned);
        
        while (matcher2.find()) {
            String key = matcher2.group(1).trim();
            String value = matcher2.group(2).trim().replaceAll("^\"|\"$", "");
            
            // Skip facilityName field as it should not appear in comments
            if ("facilityName".equalsIgnoreCase(key) || "facility_name".equalsIgnoreCase(key)) {
                continue;
            }
            
            // ULTRA-AGGRESSIVE: Remove "false" from key using regex first
            key = key.replaceAll("(?i)false", "");
            key = removeFalse(key);
            key = removeFalse(key); // Second pass
            key = removeFalse(key); // Third pass
            key = removeFalse(key); // Fourth pass
            key = key.replaceAll("(?i)false", ""); // Final regex pass
            
            value = value.replaceAll("(?i)false", "");
            value = removeFalse(value);
            value = removeFalse(value); // Second pass
            value = removeFalse(value); // Third pass
            value = removeFalse(value); // Fourth pass
            value = value.replaceAll("(?i)false", ""); // Final regex pass
            
            if (!key.isEmpty() && !value.isEmpty() && !seen.contains(key)) {
                seen.add(key);
                String formattedKey = formatKeyToReadable(key);
                formattedKey = formattedKey.replaceAll("(?i)false", "");
                formattedKey = removeFalse(formattedKey);
                formattedKey = removeFalse(formattedKey); // Second pass
                formattedKey = formattedKey.replaceAll("(?i)false", ""); // Final regex pass
                
                value = value.replaceAll("(?i)false", "");
                value = removeFalse(value);
                value = removeFalse(value); // Second pass
                value = value.replaceAll("(?i)false", ""); // Final regex pass
                
                // Build line and remove false from it
                String line = formattedKey + ": " + value;
                line = line.replaceAll("(?i)false", "");
                line = removeFalse(line);
                line = removeFalse(line); // Second pass
                line = line.replaceAll("(?i)false", ""); // Final regex pass
                comments.append(line).append("\n");
            }
        }
        
        String result = comments.toString();
        // ULTRA-AGGRESSIVE: Multiple passes to remove false
        result = result.replaceAll("(?i)false", "");
        result = removeFalse(result);
        result = removeFalse(result);
        result = removeFalse(result);
        result = removeFalse(result); // Fourth pass
        result = result.replaceAll("(?i)false", ""); // Final regex pass
        
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
        
        log.info("Final extracted metadata result: {}", result);
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
        
        // ULTRA-AGGRESSIVE: Remove "false" from key BEFORE processing using regex first
        key = key.replaceAll("(?i)false", "");
        key = removeFalse(key);
        key = removeFalse(key); // Second pass
        key = key.replaceAll("(?i)false", ""); // Final regex pass
        
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
                // Remove "false" from each word before capitalizing using regex first
                String word = words[i].replaceAll("(?i)false", "");
                word = removeFalse(word);
                word = removeFalse(word); // Second pass
                word = word.replaceAll("(?i)false", ""); // Final regex pass
                if (!word.isEmpty()) {
                    formatted.append(word.substring(0, 1).toUpperCase());
                    if (word.length() > 1) {
                        formatted.append(word.substring(1));
                    }
                }
            }
        }
        
        String finalResult = formatted.toString();
        // Final removal of "false" from formatted result using regex first
        finalResult = finalResult.replaceAll("(?i)false", "");
        finalResult = removeFalse(finalResult);
        finalResult = removeFalse(finalResult); // Second pass
        finalResult = finalResult.replaceAll("(?i)false", ""); // Final regex pass
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

