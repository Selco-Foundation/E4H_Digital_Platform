package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.web.models.EscalationTicket;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to generate CSV content for escalation tickets
 * LLD Compliant: CSV generation for escalation email attachments
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CSVGenerationService {
    
    private static final String CSV_HEADER = "Ticket Number,District,Block,Health Facility Name,Health Facility Type," +
            "Is Solar System Working,Issue Type,Issue Sub-Type,Priority,Mapped Vendor,Current Ticket Status," +
            "SLA Compliance for Current Status,Defined SLA Duration for Current Status,SLA Compliance for Overall Ticket," +
            "Defined Overall SLA Duration,Comments,Ticket Filed Date\n";
    
    /**
     * Generate CSV content for escalation tickets
     * LLD Compliant: CSV format as specified in LLD
     */
    public String generateEscalationCsv(List<EscalationTicket> tickets) {
        try {
            log.info("Generating CSV for {} escalation tickets", tickets.size());
            
            StringBuilder csvContent = new StringBuilder();
            csvContent.append(CSV_HEADER);
            
            for (EscalationTicket ticket : tickets) {
                csvContent.append(escapeCsvValue(ticket.getTicketNumber())).append(",");
                csvContent.append(escapeCsvValue(ticket.getDistrict())).append(",");
                csvContent.append(escapeCsvValue(ticket.getBlock())).append(",");
                csvContent.append(escapeCsvValue(ticket.getHealthFacilityName())).append(",");
                csvContent.append(escapeCsvValue(ticket.getHealthFacilityType())).append(",");
                csvContent.append(escapeCsvValue(ticket.getIsSolarSystemWorking() ? "Yes" : "No")).append(",");
                csvContent.append(escapeCsvValue(ticket.getIssueType())).append(",");
                csvContent.append(escapeCsvValue(ticket.getIssueSubType())).append(",");
                csvContent.append(escapeCsvValue(ticket.getPriority())).append(",");
                csvContent.append(escapeCsvValue(ticket.getMappedVendor())).append(",");
                csvContent.append(escapeCsvValue(ticket.getCurrentTicketStatus())).append(",");
                csvContent.append(escapeCsvValue(ticket.getSlaComplianceCurrentStatus() ? "Yes" : "No")).append(",");
                csvContent.append(escapeCsvValue(ticket.getDefinedSlaDurationCurrentStatus())).append(",");
                csvContent.append(escapeCsvValue(ticket.getSlaComplianceOverallTicket() ? "Yes" : "No")).append(",");
                csvContent.append(escapeCsvValue(ticket.getDefinedOverallSlaDuration())).append(",");
                csvContent.append(escapeCsvValue(ticket.getComments())).append(",");
                csvContent.append(escapeCsvValue(formatDate(ticket.getTicketFiledDate()))).append("\n");
            }
            
            log.info("Successfully generated CSV content with {} tickets", tickets.size());
            return csvContent.toString();
            
        } catch (Exception e) {
            log.error("Error generating CSV for escalation tickets", e);
            return CSV_HEADER; // Return just header if error
        }
    }
    
    /**
     * Generate CSV filename with timestamp
     */
    public String generateCsvFileName(String escalationType, String escalationLevel, String tenantId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("escalation_%s_%s_%s_%s.csv", 
                escalationType, escalationLevel, tenantId, timestamp);
    }
    
    /**
     * Generate CSV filename for weekly summary
     */
    public String generateWeeklySummaryCsvFileName(String tenantId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("weekly_summary_%s_%s.csv", tenantId, timestamp);
    }
    
    /**
     * Escape CSV values to handle commas, quotes, and newlines
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Format date for CSV display
     */
    private String formatDate(Long timestamp) {
        if (timestamp == null) {
            return "";
        }
        
        try {
            return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, 
                    java.time.ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("Error formatting date: {}", timestamp, e);
            return String.valueOf(timestamp);
        }
    }
    
    /**
     * Generate CSV content for weekly summary (with both parts)
     */
    public String generateWeeklySummaryCsv(List<EscalationTicket> previouslyEscalatedTickets, 
                                         List<EscalationTicket> currentlyInBreachTickets) {
        try {
            log.info("Generating weekly summary CSV for {} previously escalated + {} currently in breach tickets", 
                    previouslyEscalatedTickets.size(), currentlyInBreachTickets.size());
            
            StringBuilder csvContent = new StringBuilder();
            
            // Part 1: Previously Escalated Tickets Now Resolved
            csvContent.append("PREVIOUS WEEK ESCALATIONS - CURRENT STATUS\n");
            csvContent.append("=========================================\n");
            csvContent.append("Ticket Number,District,Block,Health Facility Name,Health Facility Type,");
            csvContent.append("Issue Type,Issue Sub-Type,Priority,Mapped Vendor,Ticket Filed Date,");
            csvContent.append("SLA Breach,Current Status,Comments\n");
            
            for (EscalationTicket ticket : previouslyEscalatedTickets) {
                csvContent.append(escapeCsvValue(ticket.getTicketNumber())).append(",");
                csvContent.append(escapeCsvValue(ticket.getDistrict())).append(",");
                csvContent.append(escapeCsvValue(ticket.getBlock())).append(",");
                csvContent.append(escapeCsvValue(ticket.getHealthFacilityName())).append(",");
                csvContent.append(escapeCsvValue(ticket.getHealthFacilityType())).append(",");
                csvContent.append(escapeCsvValue(ticket.getIssueType())).append(",");
                csvContent.append(escapeCsvValue(ticket.getIssueSubType())).append(",");
                csvContent.append(escapeCsvValue(ticket.getPriority())).append(",");
                csvContent.append(escapeCsvValue(ticket.getMappedVendor())).append(",");
                csvContent.append(escapeCsvValue(formatDate(ticket.getTicketFiledDate()))).append(",");
                csvContent.append(escapeCsvValue(ticket.getSlaBreachDetails())).append(",");
                csvContent.append(escapeCsvValue(ticket.getCurrentTicketStatus())).append(",");
                csvContent.append(escapeCsvValue(ticket.getComments())).append("\n");
            }
            
            csvContent.append("\n\n");
            
            // Part 2: Currently in Breach Tickets (escalated more than one week ago)
            csvContent.append("CURRENTLY IN BREACH - ESCALATED MORE THAN ONE WEEK AGO\n");
            csvContent.append("=====================================================\n");
            csvContent.append("Ticket Number,District,Block,Health Facility Name,Health Facility Type,");
            csvContent.append("Issue Type,Issue Sub-Type,Priority,Mapped Vendor,Ticket Filed Date,");
            csvContent.append("SLA Breach,Current Status,Comments,Breach Duration (Weeks)\n");
            
            for (EscalationTicket ticket : currentlyInBreachTickets) {
                csvContent.append(escapeCsvValue(ticket.getTicketNumber())).append(",");
                csvContent.append(escapeCsvValue(ticket.getDistrict())).append(",");
                csvContent.append(escapeCsvValue(ticket.getBlock())).append(",");
                csvContent.append(escapeCsvValue(ticket.getHealthFacilityName())).append(",");
                csvContent.append(escapeCsvValue(ticket.getHealthFacilityType())).append(",");
                csvContent.append(escapeCsvValue(ticket.getIssueType())).append(",");
                csvContent.append(escapeCsvValue(ticket.getIssueSubType())).append(",");
                csvContent.append(escapeCsvValue(ticket.getPriority())).append(",");
                csvContent.append(escapeCsvValue(ticket.getMappedVendor())).append(",");
                csvContent.append(escapeCsvValue(formatDate(ticket.getTicketFiledDate()))).append(",");
                csvContent.append(escapeCsvValue(ticket.getSlaBreachDetails())).append(",");
                csvContent.append(escapeCsvValue(ticket.getCurrentTicketStatus())).append(",");
                csvContent.append(escapeCsvValue(ticket.getComments())).append(",");
                csvContent.append(escapeCsvValue(calculateBreachDurationInWeeksForCSV(ticket))).append("\n");
            }
            
            csvContent.append("\n\nSUMMARY\n");
            csvContent.append("=======\n");
            csvContent.append("Previously Escalated Tickets (Last Week): ").append(previouslyEscalatedTickets.size()).append("\n");
            csvContent.append("Currently in Breach Tickets: ").append(currentlyInBreachTickets.size()).append("\n");
            csvContent.append("Total Unique Tickets: ").append(previouslyEscalatedTickets.size() + currentlyInBreachTickets.size()).append("\n");
            
            log.info("Successfully generated weekly summary CSV with {} previously escalated + {} currently in breach tickets", 
                    previouslyEscalatedTickets.size(), currentlyInBreachTickets.size());
            return csvContent.toString();
            
        } catch (Exception e) {
            log.error("Error generating weekly summary CSV", e);
            return "Ticket Number,District,Block,Health Facility Name,Health Facility Type," +
                    "Issue Type,Issue Sub-Type,Priority,Mapped Vendor,Ticket Filed Date,SLA Breach,Status,Comments\n";
        }
    }
    
    /**
     * Generate CSV content for weekly summary (legacy method for backward compatibility)
     */
    public String generateWeeklySummaryCsv(List<EscalationTicket> tickets) {
        // For backward compatibility, create empty list for currently in breach
        return generateWeeklySummaryCsv(tickets, new ArrayList<>());
    }
    
    /**
     * Calculate breach duration in weeks (CSV version)
     */
    private String calculateBreachDurationInWeeksForCSV(EscalationTicket ticket) {
        try {
            if (ticket.getSlaBreachTime() != null) {
                long currentTime = System.currentTimeMillis();
                long breachTime = ticket.getSlaBreachTime();
                long durationInMs = currentTime - breachTime;
                long weeks = durationInMs / (7 * 24 * 60 * 60 * 1000L);
                return weeks + " weeks";
            }
        } catch (Exception e) {
            log.warn("Error calculating breach duration for ticket: {}", ticket.getIncidentId(), e);
        }
        return "Unknown";
    }
}