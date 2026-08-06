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
import org.selco.e4h.web.models.ChampionUser;
import org.selco.e4h.web.models.UserAnalyticsBucket;
import org.selco.e4h.web.models.UserAnalyticsMetrics;
import org.selco.e4h.web.models.UserAnalyticsReport;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.selco.e4h.util.UserAnalyticsConstants.SHEET_BY_ROLE;
import static org.selco.e4h.util.UserAnalyticsConstants.SHEET_BY_STATE;
import static org.selco.e4h.util.UserAnalyticsConstants.SHEET_CHAMPIONS;
import static org.selco.e4h.util.UserAnalyticsConstants.SHEET_SUMMARY;

/**
 * Renders a {@link UserAnalyticsReport} as a four-sheet workbook.
 * <p>
 * <b>Summary</b> is metric-per-row and application-per-column, so it carries the full detail
 * including the previous week and the growth for each application. <b>By State</b> and <b>By Role</b>
 * stack two blocks per sheet — an Active Users table, then a Logins table below it — so each metric
 * reads as its own table rather than as one very wide row. <b>Top Champions</b> ranks the busiest
 * users per role and per application.
 */
@Slf4j
@Service
public class UserAnalyticsExcelService {

    private static final String TOTAL = "Total";
    private static final String NOT_APPLICABLE = "N/A";
    private static final String PERCENT_FORMAT = "0.00\"%\"";
    private static final String NO_ACTIVITY = "No activity in either week";

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
            writeChampionsSheet(workbook.createSheet(SHEET_CHAMPIONS), report, styles);
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
        int rowIndex = writeMetadata(sheet, report, styles, 0) + 1;

        List<String> headers = new ArrayList<>();
        headers.add("Metric");
        headers.addAll(applications);
        headers.add(TOTAL);
        writeHeaderRow(sheet, rowIndex++, headers, styles, true);

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

    /**
     * One row per state or role, in two stacked blocks: Active Users first, then Logins beneath it.
     * Each block repeats the dimension column so either table can be read or copied on its own.
     */
    private void writeDimensionSheet(Sheet sheet, String dimension, List<UserAnalyticsBucket> buckets,
                                     UserAnalyticsReport report, Styles styles) {
        List<String> applications = report.getApplications();
        int rowIndex = writeMetadata(sheet, report, styles, 0) + 1;

        rowIndex = writeSectionTitle(sheet, rowIndex, "Active Users", styles);
        List<String> activeUserHeaders = new ArrayList<>();
        activeUserHeaders.add(dimension);
        activeUserHeaders.addAll(applications);
        activeUserHeaders.add(TOTAL);
        activeUserHeaders.add("Previous Week");
        activeUserHeaders.add("Growth %");
        writeHeaderRow(sheet, rowIndex++, activeUserHeaders, styles, true);

        if (buckets == null || buckets.isEmpty()) {
            sheet.createRow(rowIndex++).createCell(0).setCellValue(NO_ACTIVITY);
        } else {
            for (UserAnalyticsBucket bucket : buckets) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(bucket.getKey());
                int column = 1;
                for (String application : applications) {
                    writeNumber(row, column++, bucket.getCurrent().activeUsersFor(application), styles);
                }
                writeNumber(row, column++, bucket.getCurrent().getActiveUsersTotal(), styles);
                writeNumber(row, column++, bucket.getPrevious().getActiveUsersTotal(), styles);
                writePercent(row, column, bucket.getActiveUserGrowthPercent(), styles);
            }
        }

        rowIndex++;
        rowIndex = writeSectionTitle(sheet, rowIndex, "Logins", styles);
        List<String> loginHeaders = new ArrayList<>();
        loginHeaders.add(dimension);
        loginHeaders.addAll(applications);
        loginHeaders.add(TOTAL);
        loginHeaders.add("Previous Week");
        writeHeaderRow(sheet, rowIndex++, loginHeaders, styles, false);

        // Every branch leaves rowIndex past the last row it wrote, even though this is currently the
        // final block on the sheet — a block appended below would otherwise overwrite it.
        if (buckets == null || buckets.isEmpty()) {
            sheet.createRow(rowIndex++).createCell(0).setCellValue(NO_ACTIVITY);
        } else {
            for (UserAnalyticsBucket bucket : buckets) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(bucket.getKey());
                int column = 1;
                for (String application : applications) {
                    writeNumber(row, column++, bucket.getCurrent().loginsFor(application), styles);
                }
                writeNumber(row, column++, bucket.getCurrent().getLoginsTotal(), styles);
                writeNumber(row, column, bucket.getPrevious().getLoginsTotal(), styles);
            }
        }

        setColumnWidths(sheet, Math.max(activeUserHeaders.size(), loginHeaders.size()));
    }

    /** The busiest users per role, then per application, each ranked on non-login activity. */
    private void writeChampionsSheet(Sheet sheet, UserAnalyticsReport report, Styles styles) {
        int rowIndex = writeMetadata(sheet, report, styles, 0);
        writeLabelledValue(sheet, rowIndex++, "Ranked on", "events in the reported week, logins excluded", styles);
        rowIndex++;

        rowIndex = writeChampionsSection(sheet, rowIndex, "Top Champion Users by Role", "Role",
                report.getChampionsByRole(), styles, true);
        rowIndex++;
        writeChampionsSection(sheet, rowIndex, "Top Champion Users by Application", "Application",
                report.getChampionsByApplication(), styles, false);

        setColumnWidths(sheet, 5);
    }

    /**
     * One block of champions. Groups are ordered by their strongest champion's activity so the most
     * engaged role or application is at the top, and users within a group keep the ranking
     * Elasticsearch returned.
     */
    private int writeChampionsSection(Sheet sheet, int rowIndex, String title, String groupHeader,
                                      Map<String, List<ChampionUser>> championsByGroup, Styles styles,
                                      boolean freeze) {
        rowIndex = writeSectionTitle(sheet, rowIndex, title, styles);
        writeHeaderRow(sheet, rowIndex++, List.of(groupHeader, "Rank", "Username", "Name", "Activity Count"),
                styles, freeze);

        if (championsByGroup == null || championsByGroup.isEmpty()) {
            sheet.createRow(rowIndex++).createCell(0).setCellValue("No non-login activity in the reported week");
            return rowIndex;
        }

        List<String> groups = new ArrayList<>(championsByGroup.keySet());
        groups.sort((left, right) -> Long.compare(topActivity(championsByGroup.get(right)),
                topActivity(championsByGroup.get(left))));

        for (String group : groups) {
            List<ChampionUser> champions = championsByGroup.getOrDefault(group, Collections.emptyList());
            if (champions.isEmpty()) {
                continue;
            }
            int rank = 1;
            for (ChampionUser champion : champions) {
                Row row = sheet.createRow(rowIndex++);
                // The group name repeats on every row rather than only the first, so the block stays
                // filterable and pivotable in Excel.
                row.createCell(0).setCellValue(group);
                writeNumber(row, 1, rank++, styles);
                row.createCell(2).setCellValue(blankIfNull(champion.getUserName()));
                row.createCell(3).setCellValue(blankIfNull(champion.getName()));
                writeNumber(row, 4, champion.getActivityCount(), styles);
            }
        }
        return rowIndex;
    }

    private long topActivity(List<ChampionUser> champions) {
        return (champions == null || champions.isEmpty()) ? 0L : champions.get(0).getActivityCount();
    }

    private String blankIfNull(String value) {
        return (value == null) ? "" : value;
    }

    /** The window the numbers cover, so a saved copy of the sheet is self-describing. */
    private int writeMetadata(Sheet sheet, UserAnalyticsReport report, Styles styles, int rowIndex) {
        writeLabelledValue(sheet, rowIndex++, "Weekly User Analytics Report", null, styles);
        writeLabelledValue(sheet, rowIndex++, "Reported week",
                report.getWeekStartDate() + " to " + report.getWeekEndDate()
                        + (report.isPartialWeek() ? " (in progress — partial week)" : ""), styles);
        writeLabelledValue(sheet, rowIndex++, "Previous week",
                report.getPreviousWeekStartDate() + " to " + report.getPreviousWeekEndDate(), styles);
        writeLabelledValue(sheet, rowIndex++, "Week boundaries in", report.getZone(), styles);
        if (report.isPartialWeek()) {
            writeLabelledValue(sheet, rowIndex++, "Note",
                    "The reported week has not finished, so growth against the full previous week reads low",
                    styles);
        }
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

    private int writeSectionTitle(Sheet sheet, int rowIndex, String title, Styles styles) {
        Cell cell = sheet.createRow(rowIndex).createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(styles.sectionTitle);
        return rowIndex + 1;
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

    /**
     * @param freeze pins everything above and including this header. Only the first header on a sheet
     *               may freeze — a sheet has one freeze pane, and a later call would move it down
     *               past the rows of the block above.
     */
    private void writeHeaderRow(Sheet sheet, int rowIndex, List<String> headers, Styles styles, boolean freeze) {
        Row row = sheet.createRow(rowIndex);
        for (int column = 0; column < headers.size(); column++) {
            Cell cell = row.createCell(column);
            cell.setCellValue(headers.get(column));
            cell.setCellStyle(styles.header);
        }
        if (freeze) {
            // rowIndex is the header's own row, so rowIndex + 1 is the count of rows above the data.
            sheet.createFreezePane(1, rowIndex + 1);
        }
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
        private final CellStyle sectionTitle;
        private final CellStyle number;
        private final CellStyle percent;

        Styles(Workbook workbook) {
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);

            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            sectionFont.setFontHeightInPoints((short) 12);

            header = workbook.createCellStyle();
            header.setFont(boldFont);
            header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setBorderBottom(BorderStyle.THIN);
            header.setWrapText(true);

            label = workbook.createCellStyle();
            label.setFont(boldFont);

            sectionTitle = workbook.createCellStyle();
            sectionTitle.setFont(sectionFont);

            number = workbook.createCellStyle();
            number.setAlignment(HorizontalAlignment.RIGHT);

            percent = workbook.createCellStyle();
            percent.setAlignment(HorizontalAlignment.RIGHT);
            percent.setDataFormat(workbook.createDataFormat().getFormat(PERCENT_FORMAT));
        }
    }
}
