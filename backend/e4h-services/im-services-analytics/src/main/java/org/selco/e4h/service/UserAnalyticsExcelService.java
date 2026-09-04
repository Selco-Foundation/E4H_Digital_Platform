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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.selco.e4h.util.UserAnalyticsConstants.FIELD_ASSIST;
import static org.selco.e4h.util.UserAnalyticsConstants.MANAGEMENT_HUB;
import static org.selco.e4h.util.UserAnalyticsConstants.SAURA_EMITRA;
import static org.selco.e4h.util.UserAnalyticsConstants.SHEET_BY_STATE;
import static org.selco.e4h.util.UserAnalyticsConstants.SHEET_CHAMPIONS;
import static org.selco.e4h.util.UserAnalyticsConstants.SHEET_KIBANA_LOGINS;
import static org.selco.e4h.util.UserAnalyticsConstants.SHEET_SUMMARY;

/**
 * Renders a {@link UserAnalyticsReport} as a four-sheet workbook.
 * <p>
 * <b>Summary</b> is metric-per-row and application-per-column, so it carries the full detail
 * including the previous week and the growth for each application. <b>By State</b> leads with the
 * active-user table and then stacks one event table per application, each state down the rows
 * against the actions that application reports on. <b>Top Champions</b> ranks the busiest users per
 * application, and <b>Kibana Logins</b> leads with the week's dashboard-view total and then ranks the
 * Kibana accounts by sign-ins.
 * <p>
 * Every sheet opens with the report title in column D followed by the two week windows, so a saved
 * copy of any single sheet is still self-describing.
 */
@Slf4j
@Service
public class UserAnalyticsExcelService {

    private static final String TOTAL = "Total";
    private static final String NOT_APPLICABLE = "N/A";
    private static final String NO_ACTIVITY = "No activity in either week";
    private static final String REPORT_TITLE = "Weekly User Analytics Report";
    private static final String PERCENT_FORMAT = "0.00\"%\"";

    /** Column D, where the title sits on every sheet. */
    private static final int TITLE_COLUMN = 3;

    /** Counts every event the vendor role produced, whatever the action was. */
    private static final EventColumn VENDOR_ACTION =
            new EventColumn("Vendor Action", UserAnalyticsMetrics::vendorActionsFor);

    /**
     * The per-application event tables on the By State sheet, in the order they are stacked. The
     * event types are the {@code event_type} values the producing services publish; a column that
     * names several of them reports their sum.
     */
    private static final List<EventTable> EVENT_TABLES = List.of(
            new EventTable("Saura eMitra Events", SAURA_EMITRA, List.of(
                    events("Tickets Created", "TICKET_CREATE"),
                    events("Tickets Assigned", "TICKET_ASSIGN"),
                    VENDOR_ACTION)),
            new EventTable("Management Hub Events", MANAGEMENT_HUB, List.of(
                    events("Project Created", "PROJECT_CREATE"),
                    events("Field Plan Created", "FIELD_PLAN_CREATE"),
                    events("Installation Report Approved", "INSTALLATION_REPORT_APPROVED"),
                    events("AMC Scheduled", "AMC_SCHEDULED"),
                    events("AMC Approved", "AMC_VISIT_APPROVED"),
                    events("Facility Created", "FACILITY_CREATE"))),
            new EventTable("Field Assist Events", FIELD_ASSIST, List.of(
                    events("Installation Report Submitted",
                            "INSTALLATION_REPORT_PART_A_SUBMITTED", "INSTALLATION_REPORT_PART_B_SUBMITTED"),
                    events("AMC Submitted", "AMC_VISIT_SUBMITTED"))));

    /**
     * @return the {@code .xlsx} bytes; the caller streams them straight back to the client
     */
    public byte[] generate(UserAnalyticsReport report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles styles = new Styles(workbook);
            writeSummarySheet(new SheetWriter(workbook.createSheet(SHEET_SUMMARY), styles), report);
            writeStateSheet(new SheetWriter(workbook.createSheet(SHEET_BY_STATE), styles), report);
            writeChampionsSheet(new SheetWriter(workbook.createSheet(SHEET_CHAMPIONS), styles), report);
            writeKibanaLoginsSheet(new SheetWriter(workbook.createSheet(SHEET_KIBANA_LOGINS), styles), report);
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
    private void writeSummarySheet(SheetWriter writer, UserAnalyticsReport report) {
        List<String> applications = report.getApplications();
        writeMetadata(writer, report);
        writer.blankRow();

        List<String> headers = new ArrayList<>();
        headers.add("Metric");
        headers.addAll(applications);
        headers.add(TOTAL);
        writer.headerRow(headers);

        UserAnalyticsBucket overall = report.getOverall();
        writeCountRow(writer, "Active Users (reported week)", overall.getCurrent(), applications, true);
        writeCountRow(writer, "Active Users (previous week)", overall.getPrevious(), applications, true);
        writeGrowthRow(writer, overall, applications);
        writeCountRow(writer, "Logins (reported week)", overall.getCurrent(), applications, false);
        writeCountRow(writer, "Logins (previous week)", overall.getPrevious(), applications, false);

        writer.finish();
    }

    /**
     * Active users per state, then one event table per application below it. Every table repeats the
     * state column so any single block can be read, filtered or copied on its own.
     */
    private void writeStateSheet(SheetWriter writer, UserAnalyticsReport report) {
        List<String> applications = report.getApplications();
        List<UserAnalyticsBucket> buckets = report.getByState();
        writeMetadata(writer, report);
        writer.blankRow();

        writer.sectionTitle("Active Users");
        List<String> headers = new ArrayList<>();
        headers.add("State");
        headers.addAll(applications);
        headers.add(TOTAL);
        headers.add("Previous Week");
        headers.add("Growth %");
        writer.headerRow(headers);

        if (buckets == null || buckets.isEmpty()) {
            writer.text(writer.row(), 0, NO_ACTIVITY);
        } else {
            for (UserAnalyticsBucket bucket : buckets) {
                Row row = writer.row();
                writer.text(row, 0, bucket.getKey());
                int column = 1;
                for (String application : applications) {
                    writer.number(row, column++, bucket.getCurrent().activeUsersFor(application));
                }
                writer.number(row, column++, bucket.getCurrent().getActiveUsersTotal());
                writer.number(row, column++, bucket.getPrevious().getActiveUsersTotal());
                writer.percent(row, column, bucket.getActiveUserGrowthPercent());
            }
        }

        for (EventTable table : EVENT_TABLES) {
            writer.blankRow();
            writeEventTable(writer, table, buckets);
        }

        writer.finish();
    }

    /**
     * One application's event table: a state per row, the application's reported actions across the
     * columns. These are event counts rather than distinct users, so the total column genuinely is
     * the sum of the columns to its left.
     * <p>
     * Every state that saw any activity gets a row, including the ones whose counts are all zero, so
     * the tables carry the same states in the same order and can be read against each other.
     */
    private void writeEventTable(SheetWriter writer, EventTable table, List<UserAnalyticsBucket> buckets) {
        writer.sectionTitle(table.title());

        List<String> headers = new ArrayList<>();
        headers.add("State");
        table.columns().forEach(column -> headers.add(column.label()));
        headers.add(TOTAL);
        writer.headerRow(headers);

        if (buckets == null || buckets.isEmpty()) {
            writer.text(writer.row(), 0, NO_ACTIVITY);
            return;
        }

        for (UserAnalyticsBucket bucket : buckets) {
            Row row = writer.row();
            writer.text(row, 0, bucket.getKey());
            int column = 1;
            long total = 0L;
            for (EventColumn eventColumn : table.columns()) {
                long count = eventColumn.count().apply(bucket.getCurrent(), table.application());
                total += count;
                writer.number(row, column++, count);
            }
            writer.number(row, column, total);
        }
    }

    /**
     * The busiest users per application, ranked on non-login activity. A user is listed once per role
     * they worked under — the role and the user together are what gets ranked.
     */
    private void writeChampionsSheet(SheetWriter writer, UserAnalyticsReport report) {
        writeMetadata(writer, report);
        writer.blankRow();

        writer.sectionTitle("Top Champion Users by Application");
        writer.headerRow(List.of("Application", "Rank", "Name", "Role", "Activity Count"));

        Map<String, List<ChampionUser>> championsByGroup = report.getChampionsByApplication();
        if (championsByGroup == null || championsByGroup.isEmpty()) {
            writer.text(writer.row(), 0, "No non-login activity in the reported week");
            writer.finish();
            return;
        }

        // Applications are ordered by their strongest champion, so the busiest one is at the top.
        List<String> groups = new ArrayList<>(championsByGroup.keySet());
        groups.sort((left, right) -> Long.compare(topActivity(championsByGroup.get(right)),
                topActivity(championsByGroup.get(left))));

        for (String group : groups) {
            List<ChampionUser> champions = championsByGroup.getOrDefault(group, Collections.emptyList());
            int rank = 1;
            for (ChampionUser champion : champions) {
                Row row = writer.row();
                // The application repeats on every row rather than only the first, so the block stays
                // filterable and pivotable in Excel.
                writer.text(row, 0, group);
                writer.number(row, 1, rank++);
                writer.text(row, 2, blankIfNull(champion.getName()));
                writer.text(row, 3, blankIfNull(champion.getRole()));
                writer.number(row, 4, champion.getActivityCount());
            }
        }
        writer.finish();
    }

    /**
     * The week's dashboard-view total, then Kibana sign-ins per account, busiest first. These records
     * carry no state, role or egov user — the accounts are Elasticsearch-native — so the login id is
     * all there is to group the sign-ins by, and the views, which carry no username at all, are only
     * a total.
     */
    private void writeKibanaLoginsSheet(SheetWriter writer, UserAnalyticsReport report) {
        writeMetadata(writer, report);
        writer.blankRow();

        // Counted out of the separate kibana-dashboard-report index, so this is the reported week's
        // total with no application or user breakdown to sit beside it.
        writer.sectionTitle("Kibana Dashboard Views");
        Row viewsRow = writer.row();
        writer.label(viewsRow, 0, "Views");
        writer.number(viewsRow, 1, report.getKibanaDashboardViews());
        writer.blankRow();

        writer.sectionTitle("Kibana Logins");
        writer.headerRow(List.of("Username", "Logins"));

        Map<String, Long> loginsByUser = report.getKibanaLoginsByUser();
        if (loginsByUser == null || loginsByUser.isEmpty()) {
            writer.text(writer.row(), 0, "No Kibana logins in the reported week");
            writer.finish();
            return;
        }

        // The map is a LinkedHashMap in the order Elasticsearch ranked it, so it is already descending.
        for (Map.Entry<String, Long> entry : loginsByUser.entrySet()) {
            Row row = writer.row();
            writer.text(row, 0, entry.getKey());
            writer.number(row, 1, entry.getValue());
        }
        writer.finish();
    }

    private long topActivity(List<ChampionUser> champions) {
        return (champions == null || champions.isEmpty()) ? 0L : champions.get(0).getActivityCount();
    }

    /** A missing name or login id is left blank rather than filled in with a placeholder. */
    private String blankIfNull(String value) {
        return (value == null) ? "" : value;
    }

    /** The title in column D, then the window the numbers cover. */
    private void writeMetadata(SheetWriter writer, UserAnalyticsReport report) {
        writer.title(REPORT_TITLE);
        writer.labelled("Reported week",
                report.getWeekStartDate() + " to " + report.getWeekEndDate());
        writer.labelled("Previous week",
                report.getPreviousWeekStartDate() + " to " + report.getPreviousWeekEndDate());
    }

    /**
     * One metric across the applications and the total. Active-user totals are distinct counts, so
     * the total column is deliberately not the sum of the application columns.
     */
    private void writeCountRow(SheetWriter writer, String label, UserAnalyticsMetrics metrics,
                               List<String> applications, boolean activeUsers) {
        Row row = writer.row();
        writer.label(row, 0, label);

        int column = 1;
        for (String application : applications) {
            long value = activeUsers ? metrics.activeUsersFor(application) : metrics.loginsFor(application);
            writer.number(row, column++, value);
        }
        writer.number(row, column, activeUsers ? metrics.getActiveUsersTotal() : metrics.getLoginsTotal());
    }

    private void writeGrowthRow(SheetWriter writer, UserAnalyticsBucket bucket, List<String> applications) {
        Row row = writer.row();
        writer.label(row, 0, "Active User Growth % (week on week)");

        int column = 1;
        Map<String, Double> byApplication = bucket.getActiveUserGrowthPercentByApplication();
        for (String application : applications) {
            writer.percent(row, column++, byApplication == null ? null : byApplication.get(application));
        }
        writer.percent(row, column, bucket.getActiveUserGrowthPercent());
    }

    /** A column whose count is the sum of one or more {@code event_type} values. */
    private static EventColumn events(String label, String... eventTypes) {
        List<String> types = List.of(eventTypes);
        return new EventColumn(label, (metrics, application) -> types.stream()
                .mapToLong(eventType -> metrics.eventCountFor(application, eventType))
                .sum());
    }

    /** How a column reads its count out of one state's metrics, for the table's application. */
    @FunctionalInterface
    private interface EventCount {
        long apply(UserAnalyticsMetrics metrics, String application);
    }

    /** One column of an event table: the header shown, and the count behind it. */
    private record EventColumn(String label, EventCount count) {}

    /** One event table on the By State sheet: the application it covers and its columns. */
    private record EventTable(String title, String application, List<EventColumn> columns) {}

    /**
     * Writes one sheet top to bottom, tracking the row it is on and the widest text in each column
     * so {@link #finish()} can size the columns to fit rather than to a fixed guess.
     */
    private static class SheetWriter {

        /** Padding, and the bounds the fitted width is clamped into — all in characters. */
        private static final int WIDTH_PADDING = 3;
        private static final int MIN_WIDTH = 10;
        private static final int MAX_WIDTH = 40;

        /** POI column widths are in 1/256ths of a character. */
        private static final int WIDTH_UNIT = 256;

        private final Sheet sheet;
        private final Styles styles;
        private final Map<Integer, Integer> widestByColumn = new HashMap<>();
        private int rowIndex;

        SheetWriter(Sheet sheet, Styles styles) {
            this.sheet = sheet;
            this.styles = styles;
        }

        Row row() {
            return sheet.createRow(rowIndex++);
        }

        void blankRow() {
            rowIndex++;
        }

        /**
         * The report title. Deliberately not measured — it is far wider than the column it sits in
         * and spills over the ones beside it, which is what the header of a sheet should do.
         */
        void title(String text) {
            Cell cell = row().createCell(TITLE_COLUMN);
            cell.setCellValue(text);
            cell.setCellStyle(styles.reportTitle);
        }

        /** A section heading, also unmeasured — it spills over the columns to its right. */
        void sectionTitle(String text) {
            Cell cell = row().createCell(0);
            cell.setCellValue(text);
            cell.setCellStyle(styles.sectionTitle);
        }

        /**
         * A metadata line above the tables. The value is not measured — nothing sits beneath it in
         * that column, so Excel spills it over the empty cells beside it, and widening the column to
         * fit a date range would only stretch whichever table column happens to land underneath.
         */
        void labelled(String label, String value) {
            Row row = row();
            label(row, 0, label);
            row.createCell(1).setCellValue(value);
        }

        void headerRow(List<String> headers) {
            Row row = row();
            for (int column = 0; column < headers.size(); column++) {
                Cell cell = row.createCell(column);
                cell.setCellValue(headers.get(column));
                cell.setCellStyle(styles.header);
                measure(column, headers.get(column));
            }
        }

        void label(Row row, int column, String value) {
            Cell cell = row.createCell(column);
            cell.setCellValue(value);
            cell.setCellStyle(styles.label);
            measure(column, value);
        }

        void text(Row row, int column, String value) {
            row.createCell(column).setCellValue(value);
            measure(column, value);
        }

        void number(Row row, int column, long value) {
            Cell cell = row.createCell(column);
            cell.setCellValue(value);
            cell.setCellStyle(styles.number);
            measure(column, Long.toString(value));
        }

        /** Renders an undefined growth (no active users the previous week) as {@code N/A}, not zero. */
        void percent(Row row, int column, Double value) {
            Cell cell = row.createCell(column);
            if (value == null) {
                cell.setCellValue(NOT_APPLICABLE);
                cell.setCellStyle(styles.number);
                measure(column, NOT_APPLICABLE);
                return;
            }
            cell.setCellValue(value);
            cell.setCellStyle(styles.percent);
            measure(column, String.format("%.2f%%", value));
        }

        private void measure(int column, String text) {
            if (text == null) {
                return;
            }
            widestByColumn.merge(column, text.length(), Math::max);
        }

        /**
         * Sizes every column to the widest text written into it. Widths are fitted here rather than
         * through {@code autoSizeColumn}, which measures glyphs through AWT and needs fonts
         * installed — not something a slim service container guarantees.
         */
        void finish() {
            widestByColumn.forEach((column, widest) -> {
                int characters = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, widest + WIDTH_PADDING));
                sheet.setColumnWidth(column, characters * WIDTH_UNIT);
            });
        }
    }

    /** Cell styles are workbook-scoped and capped, so they are created once and shared. */
    private static class Styles {

        private final CellStyle header;
        private final CellStyle label;
        private final CellStyle reportTitle;
        private final CellStyle sectionTitle;
        private final CellStyle number;
        private final CellStyle percent;

        Styles(Workbook workbook) {
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);

            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            sectionFont.setFontHeightInPoints((short) 12);

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            header = workbook.createCellStyle();
            header.setFont(boldFont);
            header.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setBorderBottom(BorderStyle.THIN);

            label = workbook.createCellStyle();
            label.setFont(boldFont);

            reportTitle = workbook.createCellStyle();
            reportTitle.setFont(titleFont);

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
