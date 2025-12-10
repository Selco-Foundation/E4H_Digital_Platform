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
    public String generateCsvFileName(String escalationType, String escalationLevel, String stateName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        // Sanitize state name for filename (replace spaces and special characters)
        String sanitizedStateName = sanitizeForFileName(stateName);
        return String.format("escalation_%s_%s_%s_%s.csv", 
                escalationType, escalationLevel, sanitizedStateName, timestamp);
    }
    
    /**
     * Sanitize string for use in filename (replace spaces and special characters with underscores)
     */
    private String sanitizeForFileName(String name) {
        if (name == null || name.isEmpty()) {
            return "Unknown";
        }
        // Replace spaces and special characters with underscores, convert to lowercase
        return name.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
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
}