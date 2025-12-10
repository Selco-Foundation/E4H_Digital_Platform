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
        
        String normalizedId = tenantId.toLowerCase().trim();
        
        // Handle state codes with underscores (e.g., "INDIA_KARNATAKA" -> "Karnataka")
        if (normalizedId.contains("_")) {
            String[] parts = normalizedId.split("_");
            if (parts.length > 1) {
                // Extract the state part (last part after underscore)
                String statePart = parts[parts.length - 1];
                return getStateNameFromCode(statePart);
            }
        }
        
        // Handle state codes with dots (e.g., "in.pg" -> "Karnataka")
        if (normalizedId.contains(".")) {
            String[] parts = normalizedId.split("\\.");
            if (parts.length > 1) {
                // Extract the state part (last part after dot)
                String statePart = parts[parts.length - 1];
                return getStateNameFromCode(statePart);
            }
        }
        
        // Direct state code lookup
        return getStateNameFromCode(normalizedId);
    }
    
    /**
     * Get state name from state code
     */
    private String getStateNameFromCode(String stateCode) {
        switch (stateCode) {
            case "pg":
            case "karnataka":
                return "Karnataka";
            case "sk":
            case "sikkim":
                return "Sikkim";
            case "mz":
            case "mizoram":
                return "Mizoram";
            case "or":
            case "odisha":
                return "Odisha";
            case "as":
            case "assam":
                return "Assam";
            case "mn":
            case "manipur":
                return "Manipur";
            case "nl":
            case "nagaland":
                return "Nagaland";
            case "gj":
            case "gujarat":
                return "Gujarat";
            case "mh":
            case "maharashtra":
                return "Maharashtra";
            case "ml":
            case "meghalaya":
                return "Meghalaya";
            case "in":
            case "india":
                return "India";
            default: 
                // If it's already a readable state name, capitalize it properly
                if (stateCode.length() > 0) {
                    return capitalizeWords(stateCode);
                }
                return stateCode.toUpperCase();
        }
    }
    
    /**
     * Capitalize words in a string (e.g., "karnataka" -> "Karnataka")
     */
    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        String[] words = str.split("\\s+|_");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            if (!words[i].isEmpty()) {
                result.append(words[i].substring(0, 1).toUpperCase());
                if (words[i].length() > 1) {
                    result.append(words[i].substring(1).toLowerCase());
                }
            }
        }
        return result.toString();
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
     * Generate Saura eMitra URL
     * Since all states now use tenantId "in", all states use the common login link
     */
    public String generateSauraEmitraUrl() {
        return sauraEmitraBaseUrl + "/digit-ui";
    }

    /**
     * Generate state-specific dashboard URL
     * Since all states now use tenantId "in", all states use the common dashboard URL
     */
    public String generateStateDashboardUrl() {
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
        boolean isUpArrow = increase;
        
        // Determine color based on whether change is good or bad
        // Functional: increase = good (green), decrease = bad (red)
        // Non-Functional: increase = bad (red), decrease = good (green)
        String color;
        if (isFunctional) {
            color = increase ? "%2316a34a" : "%23dc2626"; // Green for increase, red for decrease
        } else {
            color = increase ? "%23dc2626" : "%2316a34a"; // Red for increase, green for decrease
        }
        
        // Arrow class matches the actual arrow direction (up or down)
        String arrowClass = increase ? "up" : "down";
        
        // Create SVG data URIs with appropriate colors
        String upArrowSvg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='" + color + "' d='M6 2L2 8h8z'/%3E%3C/svg%3E";
        String downArrowSvg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='" + color + "' d='M6 10L2 4h8z'/%3E%3C/svg%3E";
        
        // Generate arrow HTML based on actual direction (up if increase, down if decrease)
        String arrow = isUpArrow ? 
            "<img src=\"" + upArrowSvg + "\" alt=\"↑\" style=\"vertical-align:middle;height:12px;width:12px;display:inline-block;\" />" :
            "<img src=\"" + downArrowSvg + "\" alt=\"↓\" style=\"vertical-align:middle;height:12px;width:12px;display:inline-block;\" />";

        return ArrowData.builder().arrow(arrow).arrowClass(arrowClass).build();
    }
}
