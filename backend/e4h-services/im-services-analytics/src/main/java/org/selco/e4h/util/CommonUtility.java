package org.selco.e4h.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.web.models.ArrowData;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${kibana.dashboard.url}")
    private String kibanaDashboardUrl;
    
    @Value("${saura.emitra.base.url}")
    private String sauraEmitraBaseUrl;
    
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
            return sauraEmitraBaseUrl + "/digit-ui";
        }

        switch (tenantId.toLowerCase()) {
            case "pg":
                return sauraEmitraBaseUrl + "/digit-ui"; // Karnataka
            case "sk":
                return sauraEmitraBaseUrl + "/sikkim";
            case "mz":
                return sauraEmitraBaseUrl + "/mizoram";
            case "or":
                return sauraEmitraBaseUrl + "/odisha";
            case "as":
                return sauraEmitraBaseUrl + "/assam";
            case "mn":
                return sauraEmitraBaseUrl + "/manipur";
            case "nl":
                return sauraEmitraBaseUrl + "/nagaland";
            case "gj":
                return sauraEmitraBaseUrl + "/gujarat";
            case "mh":
                return sauraEmitraBaseUrl + "/maharashtra";
            case "ml":
                return sauraEmitraBaseUrl + "/meghalaya";
            default:
                return sauraEmitraBaseUrl + "/digit-ui";
        }
    }

    /**
     * Generate state-specific dashboard URL
     */
    public String generateStateDashboardUrl(String tenantId) {
        // Use the configured Kibana dashboard URL from application properties
        return kibanaDashboardUrl;
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
            case "PENDING_ASSIGNMENT_OUT_OF_WARRANTY":
                return "Out of Warranty - Pending State POC";

            case "PENDINGFORASSIGNMENT":
                return "Pending Assignment - with CRM";

            case "PENDING_ASSIGNMENT_SPARE_PART_NEEDED":
                return "Spare Part Change - with CRM";

            case "PENDING_RESOLUTION_SPARE_PART_NEEDED":
                return "Spare Part Change - pending for resolution with Vendor";

            case "PENDINGRESOLUTION":
                return "Pending for Resolution - with Vendor";

            case "PENDING_RESOLUTION_OUT_OF_WARRANTY":
                return "Out of Warranty - pending for resolution with Vendor";

            default:
                // Don't show workflow states that are not configured in MDMS
                return null;
        }
    }

    /**
     * Calculate arrow direction and class for percentage changes in weekly reports
     * Uses SVG data URIs for better email client compatibility with proper colors
     * 
     * @param startPct Starting percentage
     * @param endPct Ending percentage
     * @param isFunctional True for functional metrics, false for non-functional metrics
     * @return ArrowData containing arrow HTML and CSS class
     */
    public ArrowData calculateArrow(double startPct, double endPct, boolean isFunctional) {
        double change = endPct - startPct;
        
        // No arrow if change is less than 0.1%
        if (Math.abs(change) < 0.1) {
            return ArrowData.builder().arrow("").arrowClass("").build();
        }

        boolean increase = change > 0;
        
        // Determine arrow class based on whether change is good or bad
        // For functional: increase (up), decrease (down)
        // For non-functional: increase (down), decrease (up)
        String arrowClass;
        if (isFunctional) {
            arrowClass = increase ? "up" : "down";
        } else {
            arrowClass = increase ? "down" : "up";
        }
        
        // Use green for "up" (good), red for "down" (bad)
        String color = arrowClass.equals("up") ? "%2316a34a" : "%23dc2626"; // #16a34a (green) or #dc2626 (red)
        
        // Create SVG data URIs with appropriate colors
        String upArrowSvg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='" + color + "' d='M6 2L2 8h8z'/%3E%3C/svg%3E";
        String downArrowSvg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='" + color + "' d='M6 10L2 4h8z'/%3E%3C/svg%3E";
        
        // Generate arrow HTML based on direction
        String arrow = increase ? 
            "<img src=\"" + upArrowSvg + "\" alt=\"↑\" style=\"vertical-align:middle;height:12px;width:12px;display:inline-block;\" />" :
            "<img src=\"" + downArrowSvg + "\" alt=\"↓\" style=\"vertical-align:middle;height:12px;width:12px;display:inline-block;\" />";

        return ArrowData.builder().arrow(arrow).arrowClass(arrowClass).build();
    }
}
