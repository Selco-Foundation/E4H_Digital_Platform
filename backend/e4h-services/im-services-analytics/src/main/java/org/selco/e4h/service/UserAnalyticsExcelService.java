package org.selco.e4h.service;

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
import org.egov.tracer.model.CustomException;
import org.selco.e4h.web.models.UserAnalyticsBucket;
import org.selco.e4h.web.models.UserAnalyticsMetrics;
import org.selco.e4h.web.models.UserAnalyticsReport;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.selco.e4h.util.UserAnalyticsConstants.SHEET_BY_ROLE;
import static org.selco.e4h.util.UserAnalyticsConstants.SHEET_BY_STATE;
import static org.selco.e4h.util.UserAnalyticsConstants.SHEET_SUMMARY;

/**
 * Renders a {@link UserAnalyticsReport} as a three-sheet workbook.
 * <p>
 * <b>Summary</b> is metric-per-row and application-per-column, so it carries the full detail
 * including the previous week and the growth for each application. <b>By State</b> and <b>By Role</b>
 * are dimension-per-row, giving each state or role the active users per application, the previous
 * week's total and the growth.
 */
@Slf4j
@Service
public class UserAnalyticsExcelService {

    private static final String TOTAL = "Total";
    private static final String NOT_APPLICABLE = "N/A";
    private static final String PERCENT_FORMAT = "0.00\"%\"";

    /** POI column widths are in 1/256ths of a character, so these are 38 and 18 characters. */
    private static final int FIRST_COLUMN_WIDTH = 38 * 256;
    private static final int DATA_COLUMN_WIDTH = 18 * 256;

    /**
     * @return the {@code .xlsx} bytes; the caller streams them straight back to the client
     */
    public byte[] generate(UserAnalyticsReport report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles styles = new Styles(workbook);
            writeSummarySheet(workbook.createSheet(SHEET_SUMMARY), report, styles);
            writeDimensionSheet(workbook.createSheet(SHEET_BY_STATE), "State", report.getByState(), report, styles);
            writeDimensionSheet(workbook.createSheet(SHEET_BY_ROLE), "Role", report.getByRole(), report, styles);
            workbook.write(out);
            byte[] bytes = out.toByteArray();
            log.info("User analytics: generated workbook of {} bytes for week {}", bytes.length, report.getWeekStartDate());
            return bytes;
        } catch (IOException e) {
            log.error("User analytics: failed to write workbook for week {}", report.getWeekStartDate(), e);
            throw new CustomException("USER_ANALYTICS_EXCEL_ERROR",
                    "Failed to generate the user analytics workbook: " + e.getMessage());
        }
    }

    /** Report metadata, then one row per metric with a column per application plus a total. */
    private void writeSummarySheet(Sheet sheet, UserAnalyticsReport report, Styles styles) {
        List<String> applications = report.getApplications();
        int rowIndex = 0;

        rowIndex = writeMetadata(sheet, report, styles, rowIndex);
        rowIndex++;

        List<String> headers = new ArrayList<>();
        headers.add("Metric");
        headers.addAll(applications);
        headers.add(TOTAL);
        writeHeaderRow(sheet, rowIndex++, headers, styles);

        UserAnalyticsBucket overall = report.getOverall();
        rowIndex = writeCountRow(sheet, rowIndex, "Active Users (reported week)",
                overall.getCurrent(), applications, styles, true);
        rowIndex = writeCountRow(sheet, rowIndex, "Active Users (previous week)",
                overall.getPrevious(), applications, styles, true);
        rowIndex = writeGrowthRow(sheet, rowIndex, overall, applications, styles);
        rowIndex = writeCountRow(sheet, rowIndex, "Logins (reported week)",
                overall.getCurrent(), applications, styles, false);
        writeCountRow(sheet, rowIndex, "Logins (previous week)",
                overall.getPrevious(), applications, styles, false);

        setColumnWidths(sheet, headers.size());
    }

    /** The window the numbers cover, so a saved copy of the sheet is self-describing. */
    private int writeMetadata(Sheet sheet, UserAnalyticsReport report, Styles styles, int rowIndex) {
        writeLabelledValue(sheet, rowIndex++, "Weekly User Analytics Report", null, styles);
        writeLabelledValue(sheet, rowIndex++, "Reported week",
                report.getWeekStartDate() + " to " + report.getWeekEndDate(), styles);
        writeLabelledValue(sheet, rowIndex++, "Previous week",
                report.getPreviousWeekStartDate() + " to " + report.getPreviousWeekEndDate(), styles);
        writeLabelledValue(sheet, rowIndex++, "Week boundaries in", report.getZone(), styles);
        return rowIndex;
    }

    private void writeLabelledValue(Sheet sheet, int rowIndex, String label, String value, Styles styles) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.label);
        if (value != null) {
            row.createCell(1).setCellValue(value);
        }
    }

    /**
     * One metric across the applications and the total. Active-user totals are distinct counts, so
     * the total column is deliberately not the sum of the application columns.
     */
    private int writeCountRow(Sheet sheet, int rowIndex, String label, UserAnalyticsMetrics metrics,
                              List<String> applications, Styles styles, boolean activeUsers) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.label);

        int column = 1;
        for (String application : applications) {
            long value = activeUsers ? metrics.activeUsersFor(application) : metrics.loginsFor(application);
            writeNumber(row, column++, value, styles);
        }
        writeNumber(row, column, activeUsers ? metrics.getActiveUsersTotal() : metrics.getLoginsTotal(), styles);
        return rowIndex + 1;
    }

    private int writeGrowthRow(Sheet sheet, int rowIndex, UserAnalyticsBucket bucket,
                               List<String> applications, Styles styles) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue("Active User Growth % (week on week)");
        labelCell.setCellStyle(styles.label);

        int column = 1;
        Map<String, Double> byApplication = bucket.getActiveUserGrowthPercentByApplication();
        for (String application : applications) {
            writePercent(row, column++, byApplication == null ? null : byApplication.get(application), styles);
        }
        writePercent(row, column, bucket.getActiveUserGrowthPercent(), styles);
        return rowIndex + 1;
    }

    /** One row per state or role: active users per application, then the total, previous week and growth. */
    private void writeDimensionSheet(Sheet sheet, String dimension, List<UserAnalyticsBucket> buckets,
                                     UserAnalyticsReport report, Styles styles) {
        List<String> applications = report.getApplications();
        int rowIndex = 0;

        rowIndex = writeMetadata(sheet, report, styles, rowIndex);
        rowIndex++;

        List<String> headers = new ArrayList<>();
        headers.add(dimension);
        for (String application : applications) {
            headers.add("Active Users - " + application);
        }
        headers.add("Active Users - Total");
        headers.add("Active Users - Previous Week");
        headers.add("Active User Growth %");
        for (String application : applications) {
            headers.add("Logins - " + application);
        }
        headers.add("Logins - Total");
        writeHeaderRow(sheet, rowIndex++, headers, styles);

        if (buckets == null || buckets.isEmpty()) {
            Row row = sheet.createRow(rowIndex);
            row.createCell(0).setCellValue("No activity in either week");
            setColumnWidths(sheet, headers.size());
            return;
        }

        for (UserAnalyticsBucket bucket : buckets) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(bucket.getKey());
            int column = 1;
            for (String application : applications) {
                writeNumber(row, column++, bucket.getCurrent().activeUsersFor(application), styles);
            }
            writeNumber(row, column++, bucket.getCurrent().getActiveUsersTotal(), styles);
            writeNumber(row, column++, bucket.getPrevious().getActiveUsersTotal(), styles);
            writePercent(row, column++, bucket.getActiveUserGrowthPercent(), styles);
            for (String application : applications) {
                writeNumber(row, column++, bucket.getCurrent().loginsFor(application), styles);
            }
            writeNumber(row, column, bucket.getCurrent().getLoginsTotal(), styles);
        }

        setColumnWidths(sheet, headers.size());
    }

    private void writeHeaderRow(Sheet sheet, int rowIndex, List<String> headers, Styles styles) {
        Row row = sheet.createRow(rowIndex);
        for (int column = 0; column < headers.size(); column++) {
            Cell cell = row.createCell(column);
            cell.setCellValue(headers.get(column));
            cell.setCellStyle(styles.header);
        }
        // rowIndex is the header's own row, so rowIndex + 1 is the number of rows above the data —
        // the metadata block, the spacer and the header itself all stay pinned while the rows scroll.
        sheet.createFreezePane(1, rowIndex + 1);
    }

    private void writeNumber(Row row, int column, long value, Styles styles) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(styles.number);
    }

    /** Renders an undefined growth (no active users the previous week) as {@code N/A}, not as zero. */
    private void writePercent(Row row, int column, Double value, Styles styles) {
        Cell cell = row.createCell(column);
        if (value == null) {
            cell.setCellValue(NOT_APPLICABLE);
            cell.setCellStyle(styles.number);
            return;
        }
        cell.setCellValue(value);
        cell.setCellStyle(styles.percent);
    }

    /**
     * Widths are set explicitly rather than through {@code autoSizeColumn}, which measures glyphs
     * through AWT and needs fonts installed — not something a slim service container guarantees.
     * The first column holds the metric / state / role name and gets the wider allowance.
     */
    private void setColumnWidths(Sheet sheet, int columns) {
        sheet.setColumnWidth(0, FIRST_COLUMN_WIDTH);
        for (int column = 1; column < columns; column++) {
            sheet.setColumnWidth(column, DATA_COLUMN_WIDTH);
        }
    }

    /** Cell styles are workbook-scoped and capped, so they are created once and shared. */
    private static class Styles {

        private final CellStyle header;
        private final CellStyle label;
        private final CellStyle number;
        private final CellStyle percent;

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

            label = workbook.createCellStyle();
            label.setFont(boldFont);

            number = workbook.createCellStyle();
            number.setAlignment(HorizontalAlignment.RIGHT);

            percent = workbook.createCellStyle();
            percent.setAlignment(HorizontalAlignment.RIGHT);
            percent.setDataFormat(workbook.createDataFormat().getFormat(PERCENT_FORMAT));
        }
    }
}
