package org.selco.e4h.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Common utility class to consolidate duplicate methods across services
 */
@Slf4j
@Component
public class CommonUtility {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // Configure ObjectMapper to handle potential serialization issues
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Get state display name from tenant ID
     */
    public String getStateDisplayName(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return "Unknown";
        }
        
        switch (tenantId.toLowerCase()) {
            case "pg": return "Karnataka";
            case "sk": return "Sikkim";
            case "mz": return "Mizoram";
            case "or": return "Odisha";
            case "as": return "Assam";
            case "mn": return "Manipur";
            case "nl": return "Nagaland";
            case "gj": return "Gujarat";
            case "mh": return "Maharashtra";
            case "ml": return "Meghalaya";
            case "in": return "India";
            default: return tenantId.toUpperCase();
        }
    }

    /**
     * Escape HTML special characters
     */
    public String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    /**
     * Load logo image and encode as base64 data URI
     */
    public String loadLogoAsBase64(String logoFileName) {
        try {
            log.info("Loading logo file: {}", logoFileName);
            ClassPathResource logoResource = new ClassPathResource("templates/" + logoFileName);
            
            if (!logoResource.exists()) {
                log.error("Logo file does not exist: templates/{}", logoFileName);
                return getPlaceholderLogo();
            }
            
            byte[] logoBytes = logoResource.getInputStream().readAllBytes();
            log.info("Successfully loaded logo: {} ({} bytes)", logoFileName, logoBytes.length);
            
            String base64Logo = Base64.getEncoder().encodeToString(logoBytes);
            
            // Determine MIME type based on file extension
            String mimeType = logoFileName.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
            
            // Return data URI
            String dataUri = "data:" + mimeType + ";base64," + base64Logo;
            log.debug("Generated data URI for {}: {} characters", logoFileName, dataUri.length());
            
            return dataUri;
            
        } catch (Exception e) {
            log.error("Failed to load logo: {}", logoFileName, e);
            return getPlaceholderLogo();
        }
    }

    /**
     * Get placeholder logo when real logo fails to load
     */
    private String getPlaceholderLogo() {
        log.warn("Using placeholder logo due to loading failure");
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
    }

    /**
     * Generate Saura eMitra URL for specific state
     */
    public String generateSauraEmitraUrl(String tenantId) {
        if (tenantId == null || "in".equals(tenantId)) {
            return "https://saura-emitra.selcofoundation.org/digit-ui";
        }
        
        switch (tenantId.toLowerCase()) {
            case "pg":
                return "https://saura-emitra.selcofoundation.org/digit-ui"; // Karnataka
            case "sk":
                return "https://saura-emitra.selcofoundation.org/sikkim";
            case "mz":
                return "https://saura-emitra.selcofoundation.org/mizoram";
            case "or":
                return "https://saura-emitra.selcofoundation.org/odisha";
            case "as":
                return "https://saura-emitra.selcofoundation.org/assam";
            case "mn":
                return "https://saura-emitra.selcofoundation.org/manipur";
            case "nl":
                return "https://saura-emitra.selcofoundation.org/nagaland";
            case "gj":
                return "https://saura-emitra.selcofoundation.org/gujarat";
            case "mh":
                return "https://saura-emitra.selcofoundation.org/maharashtra";
            case "ml":
                return "https://saura-emitra.selcofoundation.org/meghalaya";
            default:
                return "https://saura-emitra.selcofoundation.org/digit-ui";
        }
    }

    /**
     * Generate state-specific dashboard URL
     */
    public String generateStateDashboardUrl(String tenantId) {
        // Use the E4H Kibana dashboard URL
        return "https://e4h-dev.selcofoundation.org/kibana/";
    }

    /**
     * Convert RequestInfo object to JSON string for filestore service
     */
    public String convertRequestInfoToJson(RequestInfo requestInfo) {
        try {
            return objectMapper.writeValueAsString(requestInfo);
        } catch (Exception e) {
            log.warn("Failed to serialize RequestInfo to JSON, using default: {}", e.getMessage());
            // Return a default RequestInfo JSON if serialization fails
            return createDefaultRequestInfoJson();
        }
    }

    /**
     * Create a default RequestInfo JSON string
     */
    private String createDefaultRequestInfoJson() {
        return "{\"apiId\":\"im-services-analytics\",\"ver\":\"1.0\",\"ts\":" + System.currentTimeMillis() + 
               ",\"action\":\"_create\",\"did\":\"1\",\"key\":\"\",\"msgId\":\"20170310130900|en_IN\"," +
               "\"requesterId\":\"\",\"authToken\":\"\",\"userInfo\":{\"id\":1,\"uuid\":\"system\"," +
               "\"type\":\"SYSTEM\",\"tenantId\":\"in\",\"roles\":[{\"name\":\"System\",\"code\":\"SYSTEM\"," +
               "\"tenantId\":\"in\"}]}}";
    }

    /**
     * Generate download URL using actual file store ID
     */
    public String generateDownloadUrl(String fileStoreId, String tenantId, String fileStoreBaseUrl, String downloadEndpoint) {
        return fileStoreBaseUrl + downloadEndpoint + "?tenantId=" + tenantId + "&fileStoreId=" + fileStoreId;
    }

    /**
     * Format workflow state for display based on role and escalation level
     */
    public String formatWorkflowStateForDisplay(String workflowState, String escalationLevel, String recipientRole) {
        if (workflowState == null) {
            return "Unknown";
        }
        
        // Role-specific formatting rules
        switch (workflowState) {
            case "OUT_OF_WARRANTY":
            case "PENDING_ASSIGNMENT_OUT_OF_WARRANTY":
                return "Out of Warranty - Pending State POC";
            
            case "OUT_OF_WARRANTY_PENDING_STATE_POC":
                return "Out of Warranty - Pending State POC";
            
            case "PENDINGFORASSIGNMENT":
                // Different display based on role
                if ("CENTRAL_POC".equals(recipientRole) && "LEVEL_TWO".equals(escalationLevel)) {
                    return "CRM - Pending Assignment";
                }
                return "Pending Assignment";
            
            case "PENDING_ASSIGNMENT_SPARE_PART_NEEDED":
                // Different display based on role
                if ("CENTRAL_POC".equals(recipientRole)) {
                    return "CRM - Spare Part Change";
                } else if ("STATE_POC".equals(recipientRole)) {
                    return "Spare Part Change - Pending With CRM";
                }
                return "Spare Part Change";
            
            case "PENDING_RESOLUTION_SPARE_PART_NEEDED":
                // Different display based on role
                if ("CENTRAL_POC".equals(recipientRole)) {
                    return "CRM - Spare Part Change";
                } else if ("STATE_POC".equals(recipientRole)) {
                    return "Spare Part Change - Pending with Vendor";
                }
                return "Spare Part Change - Pending with Vendor";
            
            case "PENDINGRESOLUTION":
                if ("CENTRAL_POC".equals(recipientRole)) {
                    return "Vendor - Within Warranty";
                }
                return "Pending Resolution";
            
            case "PENDING_RESOLUTION_OUT_OF_WARRANTY":
                if ("CENTRAL_POC".equals(recipientRole)) {
                    return "Vendor - Out of Warranty";
                }
                return "Out of Warranty - Pending with Vendor";
            
            default:
                // Generic formatting: Convert PENDING_ASSIGNMENT to "Pending Assignment"
                return java.util.Arrays.stream(workflowState.split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(java.util.stream.Collectors.joining(" "));
        }
    }
}
