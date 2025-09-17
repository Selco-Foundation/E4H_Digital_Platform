package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.web.models.EscalationTicket;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service to generate email templates for escalation notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateService {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    
    /**
     * Generate HTML email content for escalation notification
     */
    public String generateEscalationEmailHTML(List<EscalationTicket> tickets, String escalationRecipientName, String boundaryLevel) {
        try {
            log.info("Generating escalation email HTML for {} tickets, recipient: {}, boundary: {}", 
                tickets.size(), escalationRecipientName, boundaryLevel);
            
            StringBuilder html = new StringBuilder();
            
            // HTML header
            html.append("<!DOCTYPE html>");
            html.append("<html>");
            html.append("<head>");
            html.append("<meta charset='UTF-8'>");
            html.append("<title>SLA Escalation Alert</title>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
            html.append(".header { background-color: #f44336; color: white; padding: 20px; text-align: center; }");
            html.append(".content { margin: 20px 0; }");
            html.append(".ticket-table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
            html.append(".ticket-table th, .ticket-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            html.append(".ticket-table th { background-color: #f2f2f2; }");
            html.append(".footer { background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; }");
            html.append("</style>");
            html.append("</head>");
            html.append("<body>");
            
            // Header
            html.append("<div class='header'>");
            html.append("<h1>SLA Escalation Alert</h1>");
            html.append("</div>");
            
            // Content
            html.append("<div class='content'>");
            html.append("<p>Dear ").append(escalationRecipientName).append(",</p>");
            html.append("<p>This is an automated escalation alert for tickets that have breached their SLA.</p>");
            
            if ("country".equals(boundaryLevel)) {
                html.append("<p><strong>Scope:</strong> Country Level - All Tenants</p>");
            } else {
                html.append("<p><strong>Scope:</strong> State Level - Specific Tenants</p>");
            }
            
            html.append("<p><strong>Total Tickets in Breach:</strong> ").append(tickets.size()).append("</p>");
            html.append("<p><strong>Generated At:</strong> ").append(DATE_FORMAT.format(new Date())).append("</p>");
            
            // Tickets table
            if (!tickets.isEmpty()) {
                html.append("<h3>Tickets in SLA Breach:</h3>");
                html.append("<table class='ticket-table'>");
                html.append("<thead>");
                html.append("<tr>");
                html.append("<th>Ticket Number</th>");
                html.append("<th>District</th>");
                html.append("<th>Block</th>");
                html.append("<th>Health Facility</th>");
                html.append("<th>Issue Type</th>");
                html.append("<th>Issue Sub-Type</th>");
                html.append("<th>Priority</th>");
                html.append("<th>Mapped Vendor</th>");
                html.append("<th>Current Status</th>");
                html.append("<th>SLA Compliance</th>");
                html.append("<th>Solar System</th>");
                html.append("<th>Filed Date</th>");
                html.append("</tr>");
                html.append("</thead>");
                html.append("<tbody>");
                
                for (EscalationTicket ticket : tickets) {
                    html.append("<tr>");
                    html.append("<td>").append(escapeHtml(ticket.getTicketNumber() != null ? ticket.getTicketNumber() : ticket.getIncidentId())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getDistrict())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getBlock())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getHealthFacilityName())).append(" (").append(escapeHtml(ticket.getHealthFacilityType())).append(")").append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getIssueType() != null ? ticket.getIssueType() : ticket.getIncidentType())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getIssueSubType() != null ? ticket.getIssueSubType() : ticket.getIncidentSubType())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getPriority() != null ? ticket.getPriority() : "Not Defined")).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getMappedVendor() != null ? ticket.getMappedVendor() : "Not Assigned")).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getCurrentTicketStatus() != null ? ticket.getCurrentTicketStatus() : ticket.getApplicationStatus())).append("</td>");
                    html.append("<td>").append(ticket.getSlaComplianceCurrentStatus() != null && ticket.getSlaComplianceCurrentStatus() ? "Yes" : "No").append("</td>");
                    html.append("<td>").append(ticket.getIsSolarSystemWorking() != null && ticket.getIsSolarSystemWorking() ? "Working" : "Not Working").append("</td>");
                    html.append("<td>").append(formatDate(ticket.getTicketFiledDate() != null ? ticket.getTicketFiledDate() : ticket.getFiledDate())).append("</td>");
                    html.append("</tr>");
                }
                
                html.append("</tbody>");
                html.append("</table>");
            }
            
            html.append("<p>Please review the attached CSV file for detailed information about all tickets in breach.</p>");
            html.append("<p>This is an automated message. Please do not reply to this email.</p>");
            html.append("</div>");
            
            // Footer
            html.append("<div class='footer'>");
            html.append("<p>E4H Digital Platform - Incident Management System</p>");
            html.append("<p>Generated on: ").append(DATE_FORMAT.format(new Date())).append("</p>");
            html.append("</div>");
            
            html.append("</body>");
            html.append("</html>");
            
            return html.toString();
            
        } catch (Exception e) {
            log.error("Error generating escalation email HTML", e);
            return generateSimpleEmailText(tickets, escalationRecipientName, boundaryLevel);
        }
    }
    
    /**
     * Generate simple text email as fallback
     */
    private String generateSimpleEmailText(List<EscalationTicket> tickets, String escalationRecipientName, String boundaryLevel) {
        StringBuilder text = new StringBuilder();
        
        text.append("SLA Escalation Alert\n");
        text.append("===================\n\n");
        text.append("Dear ").append(escalationRecipientName).append(",\n\n");
        text.append("This is an automated escalation alert for tickets that have breached their SLA.\n\n");
        
        if ("country".equals(boundaryLevel)) {
            text.append("Scope: Country Level - All Tenants\n");
        } else {
            text.append("Scope: State Level - Specific Tenants\n");
        }
        
        text.append("Total Tickets in Breach: ").append(tickets.size()).append("\n");
        text.append("Generated At: ").append(DATE_FORMAT.format(new Date())).append("\n\n");
        
        if (!tickets.isEmpty()) {
            text.append("Tickets in SLA Breach:\n");
            text.append("====================\n");
            
            for (EscalationTicket ticket : tickets) {
                text.append("Incident ID: ").append(ticket.getIncidentId() != null ? ticket.getIncidentId() : ticket.getId()).append("\n");
                text.append("Tenant ID: ").append(ticket.getTenantId()).append("\n");
                text.append("Status: ").append(ticket.getApplicationStatus()).append("\n");
                text.append("Type: ").append(ticket.getIncidentType() != null ? ticket.getIncidentType() : ticket.getIssueType()).append("\n");
                text.append("Sub Type: ").append(ticket.getIncidentSubType() != null ? ticket.getIncidentSubType() : ticket.getIssueSubType()).append("\n");
                text.append("Filed Date: ").append(formatDate(ticket.getFiledDate() != null ? ticket.getFiledDate() : ticket.getTicketFiledDate())).append("\n");
                text.append("SLA Breach Time: ").append(formatDate(ticket.getSlaBreachTime())).append("\n");
                text.append("---\n");
            }
        }
        
        text.append("\nPlease review the attached CSV file for detailed information.\n");
        text.append("This is an automated message. Please do not reply to this email.\n\n");
        text.append("E4H Digital Platform - Incident Management System\n");
        text.append("Generated on: ").append(DATE_FORMAT.format(new Date())).append("\n");
        
        return text.toString();
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
     * Format timestamp to readable date string
     */
    private String formatDate(Long timestamp) {
        if (timestamp == null) {
            return "N/A";
        }
        
        try {
            return DATE_FORMAT.format(new Date(timestamp));
        } catch (Exception e) {
            log.warn("Error formatting date: {}", timestamp, e);
            return timestamp.toString();
        }
    }
    
    /**
     * Generate email subject for escalation
     */
    public String generateEmailSubject(String escalationRecipientName, int ticketCount, String boundaryLevel) {
        String scope = "country".equals(boundaryLevel) ? "Country" : "State";
        return String.format("SLA Escalation Alert - %s Level - %d Tickets in Breach", scope, ticketCount);
    }
    
    /**
     * Generate HTML email content for enhanced weekly summary notification (with both parts)
     */
    public String generateWeeklySummaryEmailHTML(List<EscalationTicket> previouslyEscalatedTickets,
                                               List<EscalationTicket> currentlyInBreachTickets,
                                               String escalationRecipientName, String boundaryLevel) {
        try {
            log.info("Generating enhanced weekly summary email HTML for {} previously escalated + {} currently in breach tickets, recipient: {}, boundary: {}", 
                previouslyEscalatedTickets.size(), currentlyInBreachTickets.size(), escalationRecipientName, boundaryLevel);
            
            StringBuilder html = new StringBuilder();
            
            // HTML header
            html.append("<!DOCTYPE html>");
            html.append("<html>");
            html.append("<head>");
            html.append("<meta charset='UTF-8'>");
            html.append("<title>Weekly SLA Summary - Escalation Status Report</title>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
            html.append(".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }");
            html.append(".content { margin: 20px 0; }");
            html.append(".metrics { background-color: #f8f9fa; padding: 15px; margin: 20px 0; border-left: 4px solid #4CAF50; }");
            html.append(".ticket-table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
            html.append(".ticket-table th, .ticket-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            html.append(".ticket-table th { background-color: #f2f2f2; }");
            html.append(".section-header { background-color: #e9ecef; padding: 10px; margin: 20px 0 10px 0; font-weight: bold; }");
            html.append(".footer { background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; }");
            html.append("</style>");
            html.append("</head>");
            html.append("<body>");
            
            // Header
            html.append("<div class='header'>");
            html.append("<h1>Weekly SLA Summary - Escalation Status Report</h1>");
            html.append("</div>");
            
            // Content
            html.append("<div class='content'>");
            html.append("<p>Dear ").append(escalationRecipientName).append(",</p>");
            html.append("<p>This is your weekly summary of escalation status for tickets. The report includes both tickets that were escalated last week and their current status, as well as tickets that have been in breach for more than one week.</p>");
            
            if ("country".equals(boundaryLevel)) {
                html.append("<p><strong>Scope:</strong> Country Level - All Tenants</p>");
            } else {
                html.append("<p><strong>Scope:</strong> State Level - Specific Tenants</p>");
            }
            
            // Metrics Summary
            html.append("<div class='metrics'>");
            html.append("<h3>📊 Weekly Escalation Metrics</h3>");
            html.append("<p><strong>Previously Escalated Tickets (Last Week):</strong> ").append(previouslyEscalatedTickets.size()).append("</p>");
            html.append("<p><strong>Currently in Breach Tickets (More than 1 week):</strong> ").append(currentlyInBreachTickets.size()).append("</p>");
            html.append("<p><strong>Total Tickets in Report:</strong> ").append(previouslyEscalatedTickets.size() + currentlyInBreachTickets.size()).append("</p>");
            html.append("<p><strong>Generated At:</strong> ").append(DATE_FORMAT.format(new Date())).append("</p>");
            html.append("</div>");
            
            // Part 1: Previously Escalated Tickets
            if (!previouslyEscalatedTickets.isEmpty()) {
                html.append("<div class='section-header'>");
                html.append("✅ PREVIOUS WEEK ESCALATIONS - CURRENT STATUS");
                html.append("</div>");
                html.append("<p>These tickets were escalated last week. Here's their current status:</p>");
                
                html.append("<table class='ticket-table'>");
                html.append("<tr>");
                html.append("<th>Ticket Number</th>");
                html.append("<th>District</th>");
                html.append("<th>Block</th>");
                html.append("<th>Health Facility Name</th>");
                html.append("<th>Health Facility Type</th>");
                html.append("<th>Issue Type</th>");
                html.append("<th>Issue Sub-Type</th>");
                html.append("<th>Priority</th>");
                html.append("<th>Mapped Vendor</th>");
                html.append("<th>Ticket Filed Date</th>");
                html.append("<th>SLA Breach</th>");
                html.append("<th>Status</th>");
                html.append("<th>Comments</th>");
                html.append("</tr>");
                
                for (EscalationTicket ticket : previouslyEscalatedTickets) {
                    html.append("<tr>");
                    html.append("<td>").append(escapeHtml(ticket.getTicketNumber())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getDistrict())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getBlock())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getHealthFacilityName())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getHealthFacilityType())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getIssueType() != null ? ticket.getIssueType() : ticket.getIncidentType())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getIssueSubType() != null ? ticket.getIssueSubType() : ticket.getIncidentSubType())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getPriority() != null ? ticket.getPriority() : "Not Defined")).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getMappedVendor() != null ? ticket.getMappedVendor() : "Not Assigned")).append("</td>");
                    html.append("<td>").append(formatDate(ticket.getTicketFiledDate())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getSlaBreachDetails() != null ? ticket.getSlaBreachDetails() : "N/A")).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getCurrentTicketStatus() != null ? ticket.getCurrentTicketStatus() : ticket.getApplicationStatus())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getComments() != null ? ticket.getComments() : "")).append("</td>");
                    html.append("</tr>");
                }
                
                html.append("</table>");
            }
            
            // Part 2: Currently in Breach Tickets
            if (!currentlyInBreachTickets.isEmpty()) {
                html.append("<div class='section-header'>");
                html.append("⚠️ CURRENTLY IN BREACH - ESCALATED MORE THAN ONE WEEK AGO");
                html.append("</div>");
                html.append("<p>These tickets have been escalated for more than one week and are still in breach. They may have slipped through the cracks:</p>");
                
                html.append("<table class='ticket-table'>");
                html.append("<tr>");
                html.append("<th>Ticket Number</th>");
                html.append("<th>District</th>");
                html.append("<th>Block</th>");
                html.append("<th>Health Facility Name</th>");
                html.append("<th>Health Facility Type</th>");
                html.append("<th>Issue Type</th>");
                html.append("<th>Issue Sub-Type</th>");
                html.append("<th>Priority</th>");
                html.append("<th>Mapped Vendor</th>");
                html.append("<th>Ticket Filed Date</th>");
                html.append("<th>SLA Breach</th>");
                html.append("<th>Status</th>");
                html.append("<th>Comments</th>");
                html.append("<th>Breach Duration</th>");
                html.append("</tr>");
                
                for (EscalationTicket ticket : currentlyInBreachTickets) {
                    html.append("<tr>");
                    html.append("<td>").append(escapeHtml(ticket.getTicketNumber())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getDistrict())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getBlock())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getHealthFacilityName())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getHealthFacilityType())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getIssueType() != null ? ticket.getIssueType() : ticket.getIncidentType())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getIssueSubType() != null ? ticket.getIssueSubType() : ticket.getIncidentSubType())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getPriority() != null ? ticket.getPriority() : "Not Defined")).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getMappedVendor() != null ? ticket.getMappedVendor() : "Not Assigned")).append("</td>");
                    html.append("<td>").append(formatDate(ticket.getTicketFiledDate())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getSlaBreachDetails() != null ? ticket.getSlaBreachDetails() : "N/A")).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getCurrentTicketStatus() != null ? ticket.getCurrentTicketStatus() : ticket.getApplicationStatus())).append("</td>");
                    html.append("<td>").append(escapeHtml(ticket.getComments() != null ? ticket.getComments() : "")).append("</td>");
                    html.append("<td>").append(calculateBreachDurationInWeeks(ticket)).append("</td>");
                    html.append("</tr>");
                }
                
                html.append("</table>");
            }
            
            html.append("<p><strong>Note:</strong> Please review the attached CSV file for detailed information about all tickets. The overlap between the two sections is expected and provides visibility into both metrics.</p>");
            html.append("</div>");
            
            // Footer
            html.append("<div class='footer'>");
            html.append("<p>This is an automated weekly summary message. Please do not reply to this email.</p>");
            html.append("<p>E4H Digital Platform - Incident Management System</p>");
            html.append("<p>Generated on: ").append(DATE_FORMAT.format(new Date())).append("</p>");
            html.append("</div>");
            
            html.append("</body>");
            html.append("</html>");
            
            return html.toString();
            
        } catch (Exception e) {
            log.error("Error generating enhanced weekly summary email HTML", e);
            return generateSimpleWeeklySummaryText(previouslyEscalatedTickets, currentlyInBreachTickets, escalationRecipientName, boundaryLevel);
        }
    }
    
    /**
     * Generate HTML email content for weekly summary notification (legacy method for backward compatibility)
     */
    public String generateWeeklySummaryEmailHTML(List<EscalationTicket> tickets, String escalationRecipientName, String boundaryLevel) {
        // For backward compatibility, create empty list for currently in breach
        return generateWeeklySummaryEmailHTML(tickets, new java.util.ArrayList<>(), escalationRecipientName, boundaryLevel);
    }
    
    /**
     * Calculate breach duration in weeks
     */
    private String calculateBreachDurationInWeeks(EscalationTicket ticket) {
        try {
            if (ticket.getSlaBreachTime() != null) {
                long currentTime = System.currentTimeMillis();
                long breachTime = ticket.getSlaBreachTime();
                long durationInMs = currentTime - breachTime;
                long weeks = durationInMs / (7 * 24 * 60 * 60 * 1000L);
                return weeks + " weeks";
            }
        } catch (Exception e) {
            log.warn("Error calculating breach duration for ticket: {}", ticket.getIncidentId() != null ? ticket.getIncidentId() : ticket.getId(), e);
        }
        return "Unknown";
    }
    
    /**
     * Generate email subject for weekly summary
     */
    public String generateWeeklySummaryEmailSubject(String escalationRecipientName, int ticketCount, String boundaryLevel) {
        String scope = "country".equals(boundaryLevel) ? "Country" : "State";
        return String.format("Weekly SLA Summary - %s Level - %d Previously Escalated Tickets Now Resolved", scope, ticketCount);
    }
    
    /**
     * Generate simple text version of enhanced weekly summary email (with both parts)
     */
    private String generateSimpleWeeklySummaryText(List<EscalationTicket> previouslyEscalatedTickets,
                                                 List<EscalationTicket> currentlyInBreachTickets,
                                                 String escalationRecipientName, String boundaryLevel) {
        StringBuilder text = new StringBuilder();
        
        text.append("Weekly SLA Summary - Escalation Status Report\n");
        text.append("===========================================\n\n");
        text.append("Dear ").append(escalationRecipientName).append(",\n\n");
        text.append("This is your weekly summary of escalation status for tickets. The report includes both tickets that were escalated last week and their current status, as well as tickets that have been in breach for more than one week.\n\n");
        
        if ("country".equals(boundaryLevel)) {
            text.append("Scope: Country Level - All Tenants\n");
        } else {
            text.append("Scope: State Level - Specific Tenants\n");
        }
        
        text.append("Weekly Escalation Metrics:\n");
        text.append("==========================\n");
        text.append("Previously Escalated Tickets (Last Week): ").append(previouslyEscalatedTickets.size()).append("\n");
        text.append("Currently in Breach Tickets (More than 1 week): ").append(currentlyInBreachTickets.size()).append("\n");
        text.append("Total Tickets in Report: ").append(previouslyEscalatedTickets.size() + currentlyInBreachTickets.size()).append("\n");
        text.append("Generated At: ").append(DATE_FORMAT.format(new Date())).append("\n\n");
        
        // Part 1: Previously Escalated Tickets
        if (!previouslyEscalatedTickets.isEmpty()) {
            text.append("PREVIOUS WEEK ESCALATIONS - CURRENT STATUS\n");
            text.append("==========================================\n");
            text.append("These tickets were escalated last week. Here's their current status:\n\n");
            
            for (EscalationTicket ticket : previouslyEscalatedTickets) {
                text.append("Ticket: ").append(ticket.getTicketNumber()).append("\n");
                text.append("District: ").append(ticket.getDistrict()).append("\n");
                text.append("Health Facility: ").append(ticket.getHealthFacilityName()).append("\n");
                text.append("Current Status: ").append(ticket.getCurrentTicketStatus()).append("\n");
                text.append("---\n");
            }
            text.append("\n");
        }
        
        // Part 2: Currently in Breach Tickets
        if (!currentlyInBreachTickets.isEmpty()) {
            text.append("CURRENTLY IN BREACH - ESCALATED MORE THAN ONE WEEK AGO\n");
            text.append("=====================================================\n");
            text.append("These tickets have been escalated for more than one week and are still in breach:\n\n");
            
            for (EscalationTicket ticket : currentlyInBreachTickets) {
                text.append("Ticket: ").append(ticket.getTicketNumber()).append("\n");
                text.append("District: ").append(ticket.getDistrict()).append("\n");
                text.append("Health Facility: ").append(ticket.getHealthFacilityName()).append("\n");
                text.append("Current Status: ").append(ticket.getCurrentTicketStatus()).append("\n");
                text.append("Breach Duration: ").append(calculateBreachDurationInWeeks(ticket)).append("\n");
                text.append("---\n");
            }
            text.append("\n");
        }
        
        text.append("Please review the attached CSV file for detailed information.\n");
        text.append("The overlap between the two sections is expected and provides visibility into both metrics.\n\n");
        text.append("This is an automated weekly summary message. Please do not reply to this email.\n\n");
        text.append("E4H Digital Platform - Incident Management System\n");
        text.append("Generated on: ").append(DATE_FORMAT.format(new Date())).append("\n");
        
        return text.toString();
    }
    
    /**
     * Generate simple text version of weekly summary email (legacy method for backward compatibility)
     */
    private String generateSimpleWeeklySummaryText(List<EscalationTicket> tickets, String escalationRecipientName, String boundaryLevel) {
        // For backward compatibility, create empty list for currently in breach
        return generateSimpleWeeklySummaryText(tickets, new java.util.ArrayList<>(), escalationRecipientName, boundaryLevel);
    }
}
