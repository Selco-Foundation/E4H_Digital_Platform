package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.selco.e4h.web.models.EscalationTicket;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates the "ticket details" workbook attached to / linked from escalation emails.
 * <p>
 * This is rendered as an {@code .xlsx} rather than a plain {@code .csv} so the header row can carry
 * real formatting (bold, shaded, frozen) - a plain CSV has no concept of cell styling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CSVGenerationService {

    private static final String[] HEADERS = {
            "Ticket Number", "District", "Block", "Health Facility Name", "Health Facility Type",
            "Is Solar System Working", "Issue Type", "Issue Sub-Type", "Priority", "Mapped Vendor",
            "Current Ticket Status", "SLA Compliance for Current Status", "Defined SLA Duration for Current Status",
            "SLA Compliance for Overall Ticket", "Defined Overall SLA Duration", "Comments", "Ticket Filed Date"
    };

    private static final int[] COLUMN_WIDTHS_CHARS = {
            22, 16, 16, 28, 20, 16, 18, 20, 10, 24, 22, 14, 16, 14, 16, 40, 18
    };

    /**
     * Generate the ticket-details workbook for escalation emails.
     *
     * @return the {@code .xlsx} bytes; the caller uploads them to FileStore as-is
     */
    public byte[] generateEscalationWorkbook(List<EscalationTicket> tickets) {
        log.info("Generating escalation ticket-details workbook for {} tickets", tickets != null ? tickets.size() : 0);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles styles = new Styles(workbook);
            Sheet sheet = workbook.createSheet("Ticket Details");

            writeHeaderRow(sheet, styles);

            if (tickets != null) {
                int rowIndex = 1;
                for (EscalationTicket ticket : tickets) {
                    writeTicketRow(sheet, rowIndex++, ticket, styles);
                }
            }

            setColumnWidths(sheet);
            workbook.write(out);
            byte[] bytes = out.toByteArray();
            log.info("Successfully generated ticket-details workbook of {} bytes for {} tickets",
                    bytes.length, tickets != null ? tickets.size() : 0);
            return bytes;
        } catch (IOException e) {
            log.error("Error generating escalation ticket-details workbook", e);
            return new byte[0];
        }
    }

    private void writeHeaderRow(Sheet sheet, Styles styles) {
        Row row = sheet.createRow(0);
        for (int column = 0; column < HEADERS.length; column++) {
            Cell cell = row.createCell(column);
            cell.setCellValue(HEADERS[column]);
            cell.setCellStyle(styles.header);
        }
        sheet.createFreezePane(0, 1);
    }

    private void writeTicketRow(Sheet sheet, int rowIndex, EscalationTicket ticket, Styles styles) {
        Row row = sheet.createRow(rowIndex);
        int column = 0;
        writeCell(row, column++, ticket.getTicketNumber(), styles);
        writeCell(row, column++, ticket.getDistrict(), styles);
        writeCell(row, column++, ticket.getBlock(), styles);
        writeCell(row, column++, ticket.getHealthFacilityName(), styles);
        writeCell(row, column++, ticket.getHealthFacilityType(), styles);
        writeCell(row, column++, ticket.getIsSolarSystemWorking() ? "Yes" : "No", styles);
        writeCell(row, column++, ticket.getIssueType(), styles);
        writeCell(row, column++, ticket.getIssueSubType(), styles);
        writeCell(row, column++, ticket.getPriority(), styles);
        writeCell(row, column++, ticket.getMappedVendor(), styles);
        writeCell(row, column++, ticket.getCurrentTicketStatus(), styles);
        writeCell(row, column++, ticket.getSlaComplianceCurrentStatus() ? "Yes" : "No", styles);
        writeCell(row, column++, ticket.getDefinedSlaDurationCurrentStatus(), styles);
        writeCell(row, column++, ticket.getSlaComplianceOverallTicket() ? "Yes" : "No", styles);
        writeCell(row, column++, ticket.getDefinedOverallSlaDuration(), styles);
        writeCell(row, column++, ticket.getComments(), styles);
        writeCell(row, column, formatDate(ticket.getTicketFiledDate()), styles);
    }

    private void writeCell(Row row, int column, String value, Styles styles) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(styles.data);
    }

    private void setColumnWidths(Sheet sheet) {
        for (int column = 0; column < COLUMN_WIDTHS_CHARS.length; column++) {
            sheet.setColumnWidth(column, COLUMN_WIDTHS_CHARS[column] * 256);
        }
    }

    /**
     * Generate the ticket-details workbook filename with timestamp
     */
    public String generateCsvFileName(String escalationType, String escalationLevel, String stateName) {
        log.trace("Generating workbook filename for escalationType: {}, level: {}, state: {}", escalationType, escalationLevel, stateName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        // Sanitize state name for filename (replace spaces and special characters)
        String sanitizedStateName = sanitizeForFileName(stateName);
        String fileName = String.format("escalation_%s_%s_%s_%s.xlsx",
                escalationType, escalationLevel, sanitizedStateName, timestamp);
        log.debug("Generated workbook filename: {}", fileName);
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
     * Format date for display
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

    /** Cell styles are workbook-scoped and capped, so they are created once and shared. */
    private static class Styles {

        private final CellStyle header;
        private final CellStyle data;

        Styles(Workbook workbook) {
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);

            header = workbook.createCellStyle();
            header.setFont(boldFont);
            header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setBorderBottom(BorderStyle.THIN);
            header.setWrapText(true);

            data = workbook.createCellStyle();
            data.setBorderBottom(BorderStyle.THIN);
        }
    }
}
