package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.web.models.EscalationTicket;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.TimeZone;
import java.util.Base64;

/**
 * Service to generate dynamic email templates with role-based sections
 * Supports single template file with dynamic sections based on escalation levels and roles
 * Formats workflow state display names automatically
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicEmailTemplateService {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static final String TEMPLATE_PATH = "templates/role_based_escalation_email.html";

    private final ConsumerConfiguration consumerConfiguration;
    
    static {
        // Set timezone to IST for date formatting
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }
    
    /**
     * Generate role-based dynamic email HTML with multiple escalation levels
     * Automatically detects role based on which escalation levels have data
     */
    public String generateRoleBasedEscalationEmailHTML(Map<String, List<EscalationTicket>> ticketsByLevel, 
                                                      String recipientName, String recipientRole, 
                                                      String boundaryLevel, String tenantId, 
                                                      RequestInfo requestInfo) {
        try {
            log.info("Generating role-based escalation email for role: {}, levels: {}, recipient: {}", 
                recipientRole, ticketsByLevel.keySet(), recipientName);
            
            // Load base template
            String template = loadTemplate();
            
            // Prepare template variables
            Map<String, String> templateVariables = prepareRoleBasedTemplateVariables(
                ticketsByLevel, recipientName, recipientRole, boundaryLevel, tenantId, requestInfo);
            
            // Replace template variables
            String html = replaceTemplateVariables(template, templateVariables);
            
            log.info("Successfully generated role-based escalation email HTML for role: {}", recipientRole);
            return html;
            
        } catch (Exception e) {
            log.error("Error generating role-based escalation email HTML", e);
            return generateFallbackEmail(ticketsByLevel, recipientName, boundaryLevel);
        }
    }
    
    /**
     * Generate role-based dynamic email HTML with file store IDs for download functionality
     */
    public String generateRoleBasedEscalationEmailHTML(Map<String, List<EscalationTicket>> ticketsByLevel, 
                                                      String recipientName, String recipientRole, 
                                                      String boundaryLevel, String tenantId, 
                                                      RequestInfo requestInfo, Map<String, String> fileStoreIdsByLevel) {
        try {
            log.info("Generating role-based escalation email for role: {}, levels: {}, recipient: {} with file store IDs", 
                recipientRole, ticketsByLevel.keySet(), recipientName);
            
            // Load base template
            String template = loadTemplate();
            
            // Prepare template variables with file store IDs
            Map<String, String> templateVariables = prepareRoleBasedTemplateVariables(
                ticketsByLevel, recipientName, recipientRole, boundaryLevel, tenantId, requestInfo, fileStoreIdsByLevel);
            
            // Replace template variables
            String html = replaceTemplateVariables(template, templateVariables);
            
            log.info("Successfully generated role-based escalation email HTML for role: {} with download functionality", recipientRole);
            return html;
            
        } catch (Exception e) {
            log.error("Error generating role-based escalation email HTML with file store IDs", e);
            return generateFallbackEmail(ticketsByLevel, recipientName, boundaryLevel);
        }
    }
    
    
    /**
     * Load HTML template from classpath
     */
    private String loadTemplate() throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load template from: {}", TEMPLATE_PATH, e);
            throw e;
        }
    }
    
    /**
     * Load logo image and encode as base64 data URI
     */
    private String loadLogoAsBase64(String logoFileName) {
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
     * Prepare role-based template variables with dynamic sections
     */
    private Map<String, String> prepareRoleBasedTemplateVariables(Map<String, List<EscalationTicket>> ticketsByLevel, 
                                                                 String recipientName, String recipientRole,
                                                                 String boundaryLevel, String tenantId,
                                                                 RequestInfo requestInfo) {
        return prepareRoleBasedTemplateVariables(ticketsByLevel, recipientName, recipientRole, boundaryLevel, tenantId, requestInfo, null);
    }
    
    /**
     * Prepare role-based template variables with dynamic sections and file store IDs
     */
    private Map<String, String> prepareRoleBasedTemplateVariables(Map<String, List<EscalationTicket>> ticketsByLevel, 
                                                                 String recipientName, String recipientRole,
                                                                 String boundaryLevel, String tenantId,
                                                                 RequestInfo requestInfo, Map<String, String> fileStoreIdsByLevel) {
        Map<String, String> variables = new HashMap<>();
        
        // Basic variables
        variables.put("NAME", escapeHtml(recipientName));
        variables.put("STATE_NAME", escapeHtml(getStateDisplayName(tenantId)));
        variables.put("AS_OF_DATE", DATE_FORMAT.format(new Date()));
        variables.put("BOUNDARY_LEVEL", boundaryLevel);
        
        // Calculate total tickets
        int totalTickets = ticketsByLevel.values().stream().mapToInt(List::size).sum();
        variables.put("TOTAL_TICKETS", String.valueOf(totalTickets));
        
        // Load and embed logos as base64 data URIs
        variables.put("SELCO_LOGO", loadLogoAsBase64("selcofoundation.png"));
        variables.put("SAURA_LOGO", loadLogoAsBase64("SauraEmitra.png"));
        
        // Generate dynamic escalation sections based on role and available levels
        String escalationSections = generateRoleBasedEscalationSections(
            ticketsByLevel, recipientRole, tenantId, requestInfo, fileStoreIdsByLevel);
        variables.put("ESCALATION_SECTIONS", escalationSections);
        
        // Generate state-specific dashboard URL
        variables.put("DASHBOARD_URL", generateStateDashboardUrl(tenantId));
        
        return variables;
    }
    
    
    private String generateRoleBasedEscalationSections(Map<String, List<EscalationTicket>> ticketsByLevel,
                                                      String recipientRole, String tenantId,
                                                      RequestInfo requestInfo, Map<String, String> fileStoreIdsByLevel) {
        StringBuilder sections = new StringBuilder();
        
        // Special handling for CENTRAL_POC: Combine LEVEL_ZERO and LEVEL_ONE into L1 section
        if ("CENTRAL_POC".equals(recipientRole)) {
            // Combine LEVEL_ZERO and LEVEL_ONE tickets for L1 section
            List<EscalationTicket> l1Tickets = new ArrayList<>();
            if (ticketsByLevel.get("LEVEL_ZERO") != null) {
                l1Tickets.addAll(ticketsByLevel.get("LEVEL_ZERO"));
            }
            if (ticketsByLevel.get("LEVEL_ONE") != null) {
                l1Tickets.addAll(ticketsByLevel.get("LEVEL_ONE"));
            }
            
            // Generate L1 section with combined tickets (only if tickets exist)
            if (!l1Tickets.isEmpty()) {
                String l1FileStoreId = fileStoreIdsByLevel != null ? fileStoreIdsByLevel.get("LEVEL_ONE") : null;
                String l1Section = generateEscalationSection("LEVEL_ONE", l1Tickets, recipientRole, tenantId, requestInfo, l1FileStoreId);
                if (l1Section != null && !l1Section.isEmpty()) {
                    sections.append(l1Section);
                    sections.append("<div class=\"sp-20\"></div>\n");
                }
            }
            
            // Generate L2 section if exists and has tickets
            if (ticketsByLevel.containsKey("LEVEL_TWO")) {
                List<EscalationTicket> l2Tickets = ticketsByLevel.get("LEVEL_TWO");
                if (l2Tickets != null && !l2Tickets.isEmpty()) {
                    String l2FileStoreId = fileStoreIdsByLevel != null ? fileStoreIdsByLevel.get("LEVEL_TWO") : null;
                    String l2Section = generateEscalationSection("LEVEL_TWO", l2Tickets, recipientRole, tenantId, requestInfo, l2FileStoreId);
                    if (l2Section != null && !l2Section.isEmpty()) {
                        sections.append(l2Section);
                        sections.append("<div class=\"sp-20\"></div>\n");
                    }
                }
            }
        } else {
            // Default behavior for other roles
            List<String> sortedLevels = ticketsByLevel.keySet().stream()
                .sorted((a, b) -> getLevelOrder(a) - getLevelOrder(b))
                .collect(Collectors.toList());
            
            for (String level : sortedLevels) {
                List<EscalationTicket> tickets = ticketsByLevel.get(level);
                if (tickets != null && !tickets.isEmpty()) {
                    // Generate section for this level (only if tickets exist)
                    String fileStoreId = fileStoreIdsByLevel != null ? fileStoreIdsByLevel.get(level) : null;
                    String section = generateEscalationSection(level, tickets, recipientRole, tenantId, requestInfo, fileStoreId);
                    if (section != null && !section.isEmpty()) {
                        sections.append(section);
                        sections.append("<div class=\"sp-20\"></div>\n"); // Spacing between sections
                    }
                }
            }
        }
        
        return sections.toString();
    }
    
    /**
     * Generate HTML section for a specific escalation level with file store ID
     */
    private String generateEscalationSection(String level, List<EscalationTicket> tickets,
                                           String recipientRole, String tenantId,
                                           RequestInfo requestInfo, String fileStoreId) {
        StringBuilder section = new StringBuilder();
        
        // Determine section title and subtext based on level and role
        String sectionTitle = getSectionTitle(level, recipientRole);
        
        // Skip sections with null titles (e.g., CENTRAL_POC LEVEL_ZERO)
        if (sectionTitle == null) {
            return "";
        }
        
        // Skip sections with no tickets (empty ticket list)
        if (tickets == null || tickets.isEmpty()) {
            return "";
        }
        
        String sectionSubtext = getSectionSubtext(level, recipientRole);
        String callToAction = getCallToAction(level, recipientRole, tenantId);
        
        section.append("<table role=\"presentation\" width=\"100%\" class=\"bubble p-20\">\n");
        section.append("  <tr><td align=\"center\"><p class=\"h2\">").append(sectionTitle).append("</p></td></tr>\n");
        section.append("  <tr><td class=\"sp-8\"></td></tr>\n");
        section.append("  <tr><td class=\"center small\">").append(sectionSubtext).append("</td></tr>\n");
        section.append("  <tr><td class=\"sp-12\"></td></tr>\n");
        
        // Generate ticket rows based on workflow states
        String ticketRows = generateTicketRows(level, tickets, recipientRole, requestInfo);
        section.append(ticketRows);
        
        section.append("  <tr><td class=\"sp-16\"></td></tr>\n");
        
        // Download button - only show if file store ID is available
        if (fileStoreId != null && !fileStoreId.isEmpty()) {
            String downloadUrl = generateDownloadUrl(fileStoreId, tenantId);
            section.append("  <tr>\n");
            section.append("    <td align=\"center\">\n");
            section.append("      <a href=\"").append(downloadUrl).append("\" target=\"_blank\" rel=\"noopener\" style=\"display:inline-block;background:#FFFFFF;color:#f08400;border:1.5px solid #f07400;border-radius:12px;padding:12px 18px;font:600 14px/20px Arial,Helvetica,sans-serif;text-decoration:none;\">Download Ticket Details</a>\n");
            section.append("    </td>\n");
            section.append("  </tr>\n");
        } else {
            // Show message if no file available
            section.append("  <tr>\n");
            section.append("    <td align=\"center\">\n");
            section.append("      <p class=\"text small muted\">CSV file available as email attachment</p>\n");
            section.append("    </td>\n");
            section.append("  </tr>\n");
        }
        
        section.append("  <tr><td class=\"sp-12\"></td></tr>\n");
        section.append("  <tr>\n");
        section.append("    <td><p class=\"text small\">").append(callToAction).append("</p></td>\n");
        section.append("  </tr>\n");
        section.append("</table>\n");
        
        return section.toString();
    }
    
    /**
     * Generate ticket rows grouped by workflow state
     */
    private String generateTicketRows(String level, List<EscalationTicket> tickets,
                                     String recipientRole, RequestInfo requestInfo) {
        StringBuilder rows = new StringBuilder();
        
        // Group tickets by workflow state
        Map<String, Long> stateCounts = tickets.stream()
            .collect(Collectors.groupingBy(
                ticket -> ticket.getApplicationStatus() != null ? ticket.getApplicationStatus() : "Unknown",
                Collectors.counting()
            ));
        
        // Get display names for workflow states
        for (Map.Entry<String, Long> entry : stateCounts.entrySet()) {
            String workflowState = entry.getKey();
            Long count = entry.getValue();
            
            String displayName = formatWorkflowStateForDisplay(workflowState, level, recipientRole);
            
            rows.append("  <tr>\n");
            rows.append("    <td>\n");
            rows.append("      <table role=\"presentation\" width=\"100%\">\n");
            rows.append("        <tr>\n");
            rows.append("          <td class=\"row label\" style=\"width:70%;\">").append(displayName).append("</td>\n");
            rows.append("          <td class=\"row right\" style=\"width:30%;\"><span class=\"badge\">").append(count).append("</span></td>\n");
            rows.append("        </tr>\n");
            rows.append("      </table>\n");
            rows.append("    </td>\n");
            rows.append("  </tr>\n");
        }
        
        // If no tickets, show zero for common states
        if (stateCounts.isEmpty()) {
            rows.append("  <tr>\n");
            rows.append("    <td>\n");
            rows.append("      <table role=\"presentation\" width=\"100%\">\n");
            rows.append("        <tr>\n");
            rows.append("          <td class=\"row label\" style=\"width:70%;\">No tickets requiring attention</td>\n");
            rows.append("          <td class=\"row right\" style=\"width:30%;\"><span class=\"badge\">0</span></td>\n");
            rows.append("        </tr>\n");
            rows.append("      </table>\n");
            rows.append("    </td>\n");
            rows.append("  </tr>\n");
        }
        
        return rows.toString();
    }
    
    /**
     * Get section title based on escalation level and role
     */
    private String getSectionTitle(String level, String recipientRole) {
        if ("LEVEL_ZERO".equals(level)) {
            // For CENTRAL_POC, don't show "My Tickets" section
            if ("CENTRAL_POC".equals(recipientRole)) {
                return null; // This will skip the section
            }
            return "My Tickets";
        } else if ("LEVEL_ONE".equals(level)) {
            return "L1 Escalation";
        } else if ("LEVEL_TWO".equals(level)) {
            return "L2 Escalation";
        }
        return level + " Escalation";
    }
    
    /**
     * Get section subtext based on escalation level and role
     */
    private String getSectionSubtext(String level, String recipientRole) {
        if ("LEVEL_ZERO".equals(level)) {
            return "Number of tickets are nearing their SLA and are pending on you for action:";
        } else if ("LEVEL_ONE".equals(level)) {
            if ("CENTRAL_POC".equals(recipientRole)) {
                return "Number of tickets are nearing their SLA and are pending on you for action:";
            }
            return "Number of tickets that have breached their SLA:";
        } else if ("LEVEL_TWO".equals(level)) {
            if ("CENTRAL_ONM_PROJECT_MANAGER".equals(recipientRole)) {
                return "Number of tickets that have breached their SLA and aged more than 2 business days:";
            }
            return "Number of tickets that have breached their SLA:";
        }
        return "Number of tickets requiring attention:";
    }
    
    /**
     * Get call to action text based on escalation level and role
     */
    private String getCallToAction(String level, String recipientRole, String tenantId) {
        String sauraEmitraUrl = generateSauraEmitraUrl(tenantId);
        String baseMessage = "";
        
        if ("CENTRAL_OPERATIONS_LEAD".equals(recipientRole)) {
            // Central Operations Lead specific messages
            if ("LEVEL_TWO".equals(level)) {
                return "Kindly take immediate action to ensure these tickets are resolved before they appear in the weekly report shared with the leadership team.";
            } else {
                baseMessage = "Kindly coordinate with the respective state POC to mitigate further escalation.";
            }
        } else if ("CENTRAL_ONM_PROJECT_MANAGER".equals(recipientRole)) {
            // Central OnM Project Manager specific messages
            if ("LEVEL_TWO".equals(level)) {
                return "Kindly coordinate with state CRM team at the earliest to mitigate further escalation.";
            } else {
                baseMessage = "Kindly coordinate with state CRM team at the earliest to mitigate further escalation.";
            }
        } else if ("CENTRAL_POC".equals(recipientRole)) {
            // Central POC specific messages
            if ("LEVEL_ONE".equals(level)) {
                return "Kindly take immediate action to prevent escalation to L2 stage (Central OnM Project Manager or Central Ops Lead).";
            } else if ("LEVEL_TWO".equals(level)) {
                return "Kindly take immediate action to ensure these tickets are resolved before they appear in the weekly report shared with the leadership team.";
            } else {
                baseMessage = "Kindly take immediate action on these tickets.";
            }
        } else if ("STATE_POC".equals(recipientRole)) {
            // State POC specific messages
            if ("LEVEL_ZERO".equals(level)) {
                return "Kindly take immediate action on these tickets to prevent escalation to Central POC (L1) or Central OnM stage (L2).";
            } else if ("LEVEL_ONE".equals(level)) {
                return "Kindly take immediate action on these tickets to prevent escalation to Central OnM stage (L2).";
            } else {
                baseMessage = "Kindly coordinate with the respective teams to resolve these tickets promptly.";
            }
        } else if ("LEVEL_TWO".equals(level)) {
            baseMessage = "Kindly take immediate action to ensure these tickets are resolved before they appear in the weekly report shared with the leadership team.";
        } else if ("LEVEL_ONE".equals(level)) {
            // L1 Escalation should have a simple message without Saura eMitra link
            return "Kindly take immediate action to prevent escalation to L2 stage (Central O&M Project Manager or Central Ops Lead).";
        } else if ("LEVEL_ZERO".equals(level)) {
            // Special message for State POC (LEVEL_ZERO) - standalone message with only "Saura eMitra" as link
            return "Kindly go to <a href=\"" + sauraEmitraUrl + "\" target=\"_blank\" rel=\"noopener\" style=\"color: #f08400; text-decoration: underline;\">Saura eMitra</a> and take immediate action on these tickets before they escalate to the L1 (Central POC) stage";
        } else {
            baseMessage = "Kindly take immediate action on these tickets.";
        }
        
        return baseMessage + " Kindly go to <a href=\"" + sauraEmitraUrl + "\" target=\"_blank\" rel=\"noopener\" style=\"color: #f08400; text-decoration: underline;\">Saura eMitra</a> and take immediate action on these tickets before they escalate to the L1 (Central POC) stage";
    }
    
    /**
     * Get numeric order for escalation level sorting
     */
    private int getLevelOrder(String level) {
        if ("LEVEL_ZERO".equals(level)) return 0;
        if ("LEVEL_ONE".equals(level)) return 1;
        if ("LEVEL_TWO".equals(level)) return 2;
        return 99;
    }
    
    
    /**
     * Format workflow state for display based on role and escalation level
     * Direct formatting without MDMS - simpler and more maintainable
     */
    private String formatWorkflowStateForDisplay(String workflowState, String escalationLevel, String recipientRole) {
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
                return Arrays.stream(workflowState.split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
        }
    }
    
    
    /**
     * Get proper state display name from tenant ID
     */
    private String getStateDisplayName(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return "All States";
        }
        
        switch (tenantId.toLowerCase()) {
            case "pg":
                return "Karnataka";
            case "sk":
                return "Sikkim";
            case "mz":
                return "Mizoram";
            case "od":
                return "Odisha";
            case "as":
                return "Assam";
            case "mn":
                return "Manipur";
            case "nl":
                return "Nagaland";
            case "gj":
                return "Gujarat";
            case "mh":
                return "Maharashtra";
            case "ml":
                return "Meghalaya";
            case "in":
                return "India";
            default:
                // If tenant ID doesn't match known states, return as-is
                return tenantId.toUpperCase();
        }
    }
    
    /**
     * Generate state-specific dashboard URL
     */
    private String generateStateDashboardUrl(String tenantId) {
        // Use the E4H Kibana dashboard URL
        return "https://e4h-dev.selcofoundation.org/kibana/";
    }
    
    /**
     * Generate Saura eMitra URL for specific state
     */
    private String generateSauraEmitraUrl(String tenantId) {
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
            case "od":
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
     * Generate download URL using actual file store ID
     */
    private String generateDownloadUrl(String fileStoreId, String tenantId) {
        return consumerConfiguration.getFileStoreBaseUrl()+consumerConfiguration.getFileStoreDownloadEndpoint()+ "?tenantId=" + tenantId + "&fileStoreId=" + fileStoreId;
    }
    
    /**
     * Replace template variables with actual values
     */
    private String replaceTemplateVariables(String template, Map<String, String> variables) {
        String result = template;
        
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue());
        }
        
        return result;
    }
    
    /**
     * Escape HTML special characters
     */
    private String escapeHtml(String text) {
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
     * Generate fallback email if template loading fails
     */
    private String generateFallbackEmail(Map<String, List<EscalationTicket>> ticketsByLevel, 
                                       String recipientName, String boundaryLevel) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html><html><head><title>Escalation Alert</title></head><body>");
        html.append("<h1>SLA Escalation Alert</h1>");
        html.append("<p>Dear ").append(escapeHtml(recipientName)).append(",</p>");
        html.append("<p>This is an automated escalation alert for tickets that have breached their SLA.</p>");
        
        int totalTickets = ticketsByLevel.values().stream().mapToInt(List::size).sum();
        html.append("<p><strong>Total Tickets in Breach:</strong> ").append(totalTickets).append("</p>");
        
        for (Map.Entry<String, List<EscalationTicket>> entry : ticketsByLevel.entrySet()) {
            String level = entry.getKey();
            List<EscalationTicket> tickets = entry.getValue();
            
            String levelTitle = "LEVEL_ONE".equals(level) ? "L1 Escalation" : 
                              "LEVEL_TWO".equals(level) ? "L2 Escalation" : 
                              level + " Escalation";
            html.append("<h3>").append(levelTitle).append(": ").append(tickets.size()).append(" tickets</h3>");
        }
        
        html.append("<p>Please review the attached CSV files for detailed information.</p>");
        html.append("<p>This is an automated message. Please do not reply to this email.</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    /**
     * Generate role-based email subject
     */
    public String generateRoleBasedEmailSubject(String recipientRole, String tenantId, String asOfDate) {
        String stateName = getStateDisplayName(tenantId);
        
        switch (recipientRole) {
            case "STATE_POC":
                return String.format("Daily SLA Escalation — State POC — %s — %s", stateName, asOfDate);
            
            case "CENTRAL_POC":
                return String.format("Central POC – Daily Escalation Email – %s – %s", stateName, asOfDate);
            
            case "CENTRAL_ONM_PROJECT_MANAGER":
                return String.format("Central OnM Project – Daily Escalation Email – %s – %s", stateName, asOfDate);
            
            case "CENTRAL_OPERATIONS_LEAD":
                return String.format("Daily SLA Escalation – %s – %s", stateName, asOfDate);
            
            default:
                return String.format("Daily SLA Escalation Email – %s – %s", stateName, asOfDate);
        }
    }
    
}
