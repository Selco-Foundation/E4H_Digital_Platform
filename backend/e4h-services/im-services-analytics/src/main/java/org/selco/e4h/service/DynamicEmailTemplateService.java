package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.web.models.EscalationTicket;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.TimeZone;

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
    private final CommonUtility commonUtility;
    
    static {
        // Set timezone to IST for date formatting
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    /**
     * Generate role-based dynamic email HTML with file store IDs for download functionality
     */
    public String generateRoleBasedEscalationEmailHTML(Map<String, List<EscalationTicket>> ticketsByLevel, 
                                                      String recipientName, String recipientRole, 
                                                      String boundaryLevel, String tenantId, 
                                                      RequestInfo requestInfo, Map<String, String> fileStoreIdsByLevel,
                                                      Map<String, List<String>> workflowStatesByLevel) {
        try {
            log.info("Generating role-based escalation email for role: {}, levels: {}, recipient: {} with file store IDs", 
                recipientRole, ticketsByLevel.keySet(), recipientName);
            
            // Load base template
            String template = loadTemplate();
            
            // Prepare template variables with file store IDs and MDMS workflow states
            Map<String, String> templateVariables = prepareRoleBasedTemplateVariables(
                ticketsByLevel, recipientName, recipientRole, boundaryLevel, tenantId, requestInfo, fileStoreIdsByLevel, workflowStatesByLevel);
            
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
     * Prepare role-based template variables with dynamic sections and file store IDs
     */
    private Map<String, String> prepareRoleBasedTemplateVariables(Map<String, List<EscalationTicket>> ticketsByLevel, 
                                                                 String recipientName, String recipientRole,
                                                                 String boundaryLevel, String tenantId,
                                                                 RequestInfo requestInfo, Map<String, String> fileStoreIdsByLevel,
                                                                 Map<String, List<String>> workflowStatesByLevel) {
        Map<String, String> variables = new HashMap<>();
        
        // Basic variables
        variables.put("NAME", commonUtility.escapeHtml(recipientName));
        variables.put("STATE_NAME", commonUtility.escapeHtml(commonUtility.getStateDisplayName(tenantId)));
        variables.put("AS_OF_DATE", DATE_FORMAT.format(new Date()));
        variables.put("BOUNDARY_LEVEL", boundaryLevel);
        
        // Calculate total tickets
        int totalTickets = ticketsByLevel.values().stream().mapToInt(List::size).sum();
        variables.put("TOTAL_TICKETS", String.valueOf(totalTickets));
        
        // Load and embed logos as base64 data URIs
        variables.put("SELCO_LOGO", commonUtility.loadLogoAsBase64("selcofoundation.png"));
        variables.put("SAURA_LOGO", commonUtility.loadLogoAsBase64("SauraEmitra.png"));
        
        // Generate dynamic escalation sections based on role and available levels with MDMS workflow states
        String escalationSections = generateRoleBasedEscalationSections(
            ticketsByLevel, recipientRole, tenantId, requestInfo, fileStoreIdsByLevel, workflowStatesByLevel);
        variables.put("ESCALATION_SECTIONS", escalationSections);
        
        // Generate state-specific dashboard URL
        variables.put("DASHBOARD_URL", commonUtility.generateStateDashboardUrl(tenantId));

        // Role-specific intro line with resolved placeholders
        String introLine = generateIntroLine(
            recipientRole,
            variables.get("STATE_NAME"),
            variables.get("AS_OF_DATE")
        );
        variables.put("INTRO_LINE", introLine);
        
        return variables;
    }
    
    
    private String generateRoleBasedEscalationSections(Map<String, List<EscalationTicket>> ticketsByLevel,
                                                      String recipientRole, String tenantId,
                                                      RequestInfo requestInfo, Map<String, String> fileStoreIdsByLevel,
                                                      Map<String, List<String>> workflowStatesByLevel) {
        StringBuilder sections = new StringBuilder();
        
        // Generate sections for all escalation levels
        List<String> expectedLevels = getExpectedLevelsForRole(recipientRole);
        
        for (String level : expectedLevels) {
            List<EscalationTicket> tickets = ticketsByLevel.get(level);
            if (tickets == null) {
                tickets = new ArrayList<>();
            }
            // Generate section for this level (always show, even with 0 tickets)
            String fileStoreId = fileStoreIdsByLevel != null ? fileStoreIdsByLevel.get(level) : null;
            List<String> mdmsWorkflowStates = workflowStatesByLevel != null ? workflowStatesByLevel.get(level) : null;
            String section = generateEscalationSection(level, tickets, recipientRole, tenantId, requestInfo, fileStoreId, mdmsWorkflowStates);
            if (section != null && !section.isEmpty()) {
                sections.append(section);
                sections.append("<div class=\"sp-20\"></div>\n"); // Spacing between sections
            }
        }
        
        return sections.toString();
    }
    
    /**
     * Generate HTML section for a specific escalation level with file store ID
     */
    private String generateEscalationSection(String level, List<EscalationTicket> tickets,
                                           String recipientRole, String tenantId,
                                           RequestInfo requestInfo, String fileStoreId, List<String> mdmsWorkflowStates) {
        StringBuilder section = new StringBuilder();
        
        // Determine section title and subtext based on level and role
        String sectionTitle = getSectionTitle(level, recipientRole);
        
        // Handle empty tickets by showing sections with count 0
        if (tickets == null) {
            tickets = new ArrayList<>();
        }
        
        String sectionSubtext = getSectionSubtext(level, recipientRole);
        String callToAction = getCallToAction(level, recipientRole, tenantId);
        
        section.append("<table role=\"presentation\" width=\"100%\" class=\"bubble p-20\">\n");
        section.append("  <tr><td align=\"center\"><p class=\"h2\">").append(sectionTitle).append("</p></td></tr>\n");
        section.append("  <tr><td class=\"sp-8\"></td></tr>\n");
        section.append("  <tr><td class=\"center small\">").append(sectionSubtext).append("</td></tr>\n");
        section.append("  <tr><td class=\"sp-12\"></td></tr>\n");
        
        // Generate ticket rows based on workflow states from MDMS
        String ticketRows = generateTicketRows(level, tickets, recipientRole, requestInfo, mdmsWorkflowStates);
        section.append(ticketRows);
        
        section.append("  <tr><td class=\"sp-16\"></td></tr>\n");
        
        // Download button - always show if file store ID is available (even for zero counts)
        if (fileStoreId != null && !fileStoreId.isEmpty()) {
            String downloadUrl = commonUtility.generateDownloadUrl(fileStoreId, tenantId, 
                consumerConfiguration.getFileStoreBaseUrl(), consumerConfiguration.getFileStoreDownloadEndpoint());
            section.append("  <tr>\n");
            section.append("    <td align=\"center\">\n");
            section.append("      <a href=\"").append(downloadUrl).append("\" target=\"_blank\" rel=\"noopener\" style=\"display:inline-block;background:#FFFFFF;color:#f08400;border:1.5px solid #f07400;border-radius:12px;padding:12px 18px;font:600 14px/20px Arial,Helvetica,sans-serif;text-decoration:none;\">Download Ticket Details</a>\n");
            section.append("    </td>\n");
            section.append("  </tr>\n");
        }
        // Download button always shown when CSV file is available (headers only for zero counts)
        
        section.append("  <tr><td class=\"sp-12\"></td></tr>\n");
        section.append("  <tr>\n");
        section.append("    <td><p class=\"text small\">").append(callToAction).append("</p></td>\n");
        section.append("  </tr>\n");
        section.append("</table>\n");
        
        return section.toString();
    }

    private String generateIntroLine(String recipientRole, String stateName, String asOfDate) {
        if ("STATE_POC".equals(recipientRole)) {
            return "Please find below the daily summary of the issues reported in <strong>" + stateName + "</strong> on Saura-eMitra as of <strong>" + asOfDate + "</strong>.";
        }
        // Default/other roles
        return "Please find below the daily summary of the issues escalated to you in <strong>" + stateName + "</strong> on Saura-eMitra as of <strong>" + asOfDate + "</strong>.";
    }

    /**
     * Generate ticket rows grouped by workflow state
     */
    private String generateTicketRows(String level, List<EscalationTicket> tickets,
                                     String recipientRole, RequestInfo requestInfo, List<String> mdmsWorkflowStates) {
        StringBuilder rows = new StringBuilder();
        
        // Group tickets by workflow state
        Map<String, Long> stateCounts = tickets.stream()
            .collect(Collectors.groupingBy(
                ticket -> ticket.getApplicationStatus() != null ? ticket.getApplicationStatus() : "Unknown",
                Collectors.counting()
            ));
        
        // Use MDMS workflow states if provided, otherwise fall back to hardcoded values
        List<String> workflowStatesToShow = mdmsWorkflowStates != null ? mdmsWorkflowStates : getCommonWorkflowStates(level, recipientRole);
        
        // Always show all configured workflow states, with actual counts or 0
        for (String workflowState : workflowStatesToShow) {
            String displayName = commonUtility.formatWorkflowStateForDisplay(workflowState, level, recipientRole);
            
            if (displayName != null) {
                // Get actual count for this workflow state, or 0 if not present
                Long count = stateCounts.getOrDefault(workflowState, 0L);
                
                rows.append("  <tr>\n");
                rows.append("    <td>\n");
                rows.append("      <table role=\"presentation\" width=\"100%\">\n");
                rows.append("        <tr>\n");
                rows.append("          <td class=\"row label\" style=\"width:70%;\">").append(displayName).append("</td>\n");
                
                // Use orange color for STATE_POC LEVEL_ZERO only, default color for others
                String badgeClass = ("STATE_POC".equals(recipientRole) && "LEVEL_ZERO".equals(level)) ? "badge-orange" : "badge";
                rows.append("          <td class=\"row right\" style=\"width:30%;\"><span class=\"").append(badgeClass).append("\">").append(count).append("</span></td>\n");
                
                rows.append("        </tr>\n");
                rows.append("      </table>\n");
                rows.append("    </td>\n");
                rows.append("  </tr>\n");
            }
        }
        
        return rows.toString();
    }
    
    /**
     * Get common workflow states for each escalation level when no tickets are found
     * Role-specific workflow states based on escalation matrix
     */
    private List<String> getCommonWorkflowStates(String level, String recipientRole) {
        List<String> commonStates = new ArrayList<>();
        
        // Role-specific workflow states based on escalation matrix
        switch (recipientRole) {
            case "CENTRAL_OPERATIONS_LEAD":
                // Central Operations Lead should only see "Out of Warranty - Pending with State Manager"
                if ("LEVEL_TWO".equals(level)) {
                    commonStates.add("PENDING_ASSIGNMENT_OUT_OF_WARRANTY");
                }
                break;
                
            case "CENTRAL_ONM_PROJECT_MANAGER":
                // Central OnM Project Manager should only see spare part change states
                if ("LEVEL_TWO".equals(level)) {
                    commonStates.add("PENDINGFORASSIGNMENT");
                    commonStates.add("PENDING_ASSIGNMENT_SPARE_PART_NEEDED");
                }
                break;
                
            case "STATE_POC":
                // State POC sees all workflow states
                if ("LEVEL_ZERO".equals(level)) {
                    commonStates.add("PENDINGFORASSIGNMENT");
                    commonStates.add("PENDING_ASSIGNMENT_SPARE_PART_NEEDED");
                    commonStates.add("PENDING_ASSIGNMENT_OUT_OF_WARRANTY");
                } else if ("LEVEL_ONE".equals(level)) {
                    commonStates.add("PENDINGFORASSIGNMENT");
                    commonStates.add("PENDINGRESOLUTION");
                    commonStates.add("PENDING_ASSIGNMENT_SPARE_PART_NEEDED");
                    commonStates.add("PENDING_RESOLUTION_SPARE_PART_NEEDED");
                    commonStates.add("PENDING_RESOLUTION_OUT_OF_WARRANTY");
                }
                break;
                
            case "CENTRAL_POC":
                // Central POC sees resolution and out of warranty states
                if ("LEVEL_ONE".equals(level)) {
                    commonStates.add("PENDING_ASSIGNMENT_OUT_OF_WARRANTY");
                } else if ("LEVEL_TWO".equals(level)) {
                    commonStates.add("PENDINGRESOLUTION");
                    commonStates.add("PENDING_RESOLUTION_SPARE_PART_NEEDED");
                    commonStates.add("PENDING_RESOLUTION_OUT_OF_WARRANTY");
                }
                break;
        }
        
        return commonStates;
    }
    
    /**
     * Get section title based on escalation level and role
     */
    private String getSectionTitle(String level, String recipientRole) {
        if ("LEVEL_ZERO".equals(level)) {
            return "Your Tickets";
        } else if ("LEVEL_ONE".equals(level)) {
            return "Escalated to You (First Level)";
        } else if ("LEVEL_TWO".equals(level)) {
            return "Escalated to You (Second Level)";
        }
        return level + " Escalation";
    }
    
    /**
     * Get expected escalation levels for each role
     */
    private List<String> getExpectedLevelsForRole(String recipientRole) {
        List<String> expectedLevels = new ArrayList<>();
        
        switch (recipientRole) {
            case "STATE_POC":
                expectedLevels.add("LEVEL_ZERO"); // My Tickets
                expectedLevels.add("LEVEL_ONE");  // L1 Escalation
                break;
            case "CENTRAL_OPERATIONS_LEAD":
            case "CENTRAL_ONM_PROJECT_MANAGER":
                expectedLevels.add("LEVEL_TWO");  // L2 Escalation only
                break;
            case "CENTRAL_POC":
                expectedLevels.add("LEVEL_ONE");  // L1 Escalation
                expectedLevels.add("LEVEL_TWO");  // L2 Escalation
                break;
        }
        
        return expectedLevels;
    }
    
    /**
     * Get section subtext based on escalation level and role
     */
    private String getSectionSubtext(String level, String recipientRole) {
        if ("LEVEL_ZERO".equals(level)) {
            return "Please find the tickets assigned to you that are nearing their SLA and awaiting your action.";
        } else if ("LEVEL_ONE".equals(level)) {
            return "Please find the tickets escalated to you as you are the first level after SLA breaching.";
        } else if ("LEVEL_TWO".equals(level)) {
            return "Please find the tickets escalated to you as you are the second level after SLA breaching.";
        }
        return "Number of tickets requiring attention:";
    }
    
    /**
     * Get call to action text based on escalation level and role
     */
    private String getCallToAction(String level, String recipientRole, String tenantId) {
        String sauraEmitraUrl = commonUtility.generateSauraEmitraUrl(tenantId);

        // Central Operations Lead (SPM)
        if ("CENTRAL_OPERATIONS_LEAD".equals(recipientRole)) {
            if ("LEVEL_TWO".equals(level)) {
                return "Please resolve these tickets promptly, as they have been unresolved for too long. Thank you!";
            }
            return "Please coordinate with the respective state POC to mitigate further escalation.";
        }

        // Central OnM Project Manager
        if ("CENTRAL_ONM_PROJECT_MANAGER".equals(recipientRole)) {
            if ("LEVEL_TWO".equals(level)) {
                return "Please coordinate with the State CRM team to ensure these tickets are assigned ASAP. Thank you!";
            }
            return "Please coordinate with the State CRM team to ensure timely assignment.";
        }

        // Central POC
        if ("CENTRAL_POC".equals(recipientRole)) {
            if ("LEVEL_ONE".equals(level)) {
                return "Kindly take immediate action on these tickets to resolve the issues in the health centers. Thank you!";
            }
            if ("LEVEL_TWO".equals(level)) {
                return "Please resolve these tickets promptly, as they have been unresolved for too long. Thank you!";
            }
            return "Kindly take immediate action on these tickets.";
        }

        // State POC
        if ("STATE_POC".equals(recipientRole)) {
            if ("LEVEL_ZERO".equals(level)) {
                return "Kindly go to <a href=\"" + sauraEmitraUrl + "\" target=\"_blank\" rel=\"noopener\" style=\"color: #f08400; text-decoration: underline;\">Saura eMitra</a> take immediate action on these tickets to help the health centers. Thank you.";
            }
            if ("LEVEL_ONE".equals(level)) {
                return "Kindly take immediate action on these tickets to resolve the issues in the health centers. Thank you!";
            }
            return "Kindly coordinate with the respective teams to resolve these tickets promptly.";
        }

        // Default fallback
        return "Kindly take immediate action on these tickets.";
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
     * Generate fallback email if template loading fails
     */
    private String generateFallbackEmail(Map<String, List<EscalationTicket>> ticketsByLevel, 
                                       String recipientName, String boundaryLevel) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html><html><head><title>Escalation Alert</title></head><body>");
        html.append("<h1>SLA Escalation Alert</h1>");
        html.append("<p>Dear ").append(commonUtility.escapeHtml(recipientName)).append(",</p>");
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
        String stateName = commonUtility.getStateDisplayName(tenantId);
        
        switch (recipientRole) {
            case "STATE_POC":
                return String.format("Saura-eMitra Daily Escalation - State POC — %s — %s", stateName, asOfDate);
            
            case "CENTRAL_POC":
                return String.format("Saura-eMitra Daily Escalation - Central POC — %s — %s", stateName, asOfDate);
            
            case "CENTRAL_ONM_PROJECT_MANAGER":
                return String.format("Saura-eMitra Daily Escalation - Central O&M Project Manager — %s — %s", stateName, asOfDate);
            
            case "CENTRAL_OPERATIONS_LEAD":
                return String.format("Saura-eMitra Daily Escalation - SPM — %s — %s", stateName, asOfDate);
            
            default:
                return String.format("Daily SLA Escalation Email – %s – %s", stateName, asOfDate);
        }
    }
    
}
