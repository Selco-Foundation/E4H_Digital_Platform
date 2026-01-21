package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.web.models.EscalationTicket;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        log.trace("Generating escalation CSV for {} tickets", tickets != null ? tickets.size() : 0);
        log.info("Generating CSV for {} escalation tickets", tickets != null ? tickets.size() : 0);
        try {
            StringBuilder csvContent = new StringBuilder();
            csvContent.append(CSV_HEADER);
            log.debug("CSV header appended, starting ticket processing");
            
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
        log.trace("Generating CSV filename for escalationType: {}, level: {}, state: {}", escalationType, escalationLevel, stateName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        // Sanitize state name for filename (replace spaces and special characters)
        String sanitizedStateName = sanitizeForFileName(stateName);
        String fileName = String.format("escalation_%s_%s_%s_%s.csv", 
                escalationType, escalationLevel, sanitizedStateName, timestamp);
        log.debug("Generated CSV filename: {}", fileName);
        return fileName;
    }

    /**
     * Sanitize string for use in filename (replace spaces and special characters with underscores)
     */
    private String sanitizeForFileName(String name) {
        log.trace("Sanitizing filename: {}", name);
        if (name == null || name.isEmpty()) {
            return "Unknown";
        }
        // Replace spaces and special characters with underscores, convert to lowercase
        String sanitized = name.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        log.debug("Sanitized filename: {} -> {}", name, sanitized);
        return sanitized;
    }

    /**
     * Escape CSV values to handle commas, quotes, and newlines
     */
    private String escapeCsvValue(String value) {
        log.trace("Escaping CSV value, length: {}", value != null ? value.length() : 0);
        if (value == null) {
            log.debug("Value is null, returning empty string");
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        log.debug("Value does not require escaping");
        return value;
    }

    /**
     * Format date for CSV display
     */
    private String formatDate(Long timestamp) {
        log.trace("Formatting date timestamp: {}", timestamp);
        if (timestamp == null) {
            return "";
        }
        
        try {
            String formatted = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, 
                    java.time.ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            log.debug("Formatted date: {} -> {}", timestamp, formatted);
            return formatted;
        } catch (Exception e) {
            log.warn("Error formatting date: {}", timestamp, e);
            return String.valueOf(timestamp);
        }
    }
}