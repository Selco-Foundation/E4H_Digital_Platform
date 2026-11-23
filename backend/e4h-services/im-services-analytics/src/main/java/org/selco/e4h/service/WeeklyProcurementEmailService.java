package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.WeeklyBottleneckRow;
import org.selco.e4h.web.models.WeeklyEscalationAnalytics;
import org.selco.e4h.web.models.WeeklyFacilityRestoredRow;
import org.selco.e4h.web.models.WeeklyTrendMetric;
import org.selco.e4h.web.models.WeeklyVendorPerformanceRow;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyProcurementEmailService {

    private static final String TEMPLATE_PATH = "templates/weekly_procurement_email.html";

    private final CommonUtility commonUtility;

    public String generateEmailSubject(WeeklyEscalationAnalytics analytics) {
        return String.format("[Weekly Summary] Vendor SLA Performance - Procurement Team | Week of %s", analytics.getWeekRangeLabel());
    }

    public String generateEmailHtml(WeeklyEscalationAnalytics analytics, String recipientName, String downloadUrl) {
        try {
            String template = EscalationEmailTemplateHelper.loadTemplate(TEMPLATE_PATH);
            Map<String, String> variables = EscalationEmailTemplateHelper.baseBrandingVariables(commonUtility, recipientName);
            variables.put("WEEK_RANGE", commonUtility.escapeHtml(analytics.getWeekRangeLabel()));
            variables.put("AS_OF_DATE", commonUtility.escapeHtml(analytics.getAsOfDate()));
            variables.put("VENDOR_ROWS", renderVendorRows(analytics));
            variables.put("OVERVIEW_TOTAL_SUMMARY", renderOverviewTotalSummary(analytics));
            variables.put("BOTTLENECK_ROWS", renderBottlenecks(analytics));
            variables.put("FACILITY_ROWS", renderFacilityRows(analytics));
            variables.put("DOWNLOAD_BUTTON", EscalationEmailTemplateHelper.renderDownloadButton(downloadUrl));
            variables.put("DASHBOARD_URL", commonUtility.generateStateDashboardUrl());
            return EscalationEmailTemplateHelper.render(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate weekly Procurement email HTML", e);
            return "<html><body><p>Weekly Procurement email could not be generated.</p></body></html>";
        }
    }

    private String renderOverviewTotalSummary(WeeklyEscalationAnalytics analytics) {
        WeeklyTrendMetric trend = analytics.getTotalOpenTrend();
        int totalOpen = analytics.getNationalOverview() != null ? analytics.getNationalOverview().getTotalOpen() : 0;
        if (trend == null) {
            return "<p>Overall this week: <strong>" + totalOpen + "</strong> vendor tickets still open</p>";
        }
        return "<p>Overall this week: <strong>" + totalOpen + "</strong> vendor tickets still open ("
                + trend.getArrow() + " " + Math.abs(trend.getChangePct()) + "% vs last week)</p>";
    }

    private static final String CELL = "padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:left;";
    private static final String CELL_RIGHT = "padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:right;";
    private static final String CELL_MUTED_CENTER = "padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;color:#6b7280;text-align:center;";

    private String renderVendorRows(WeeklyEscalationAnalytics analytics) {
        if (analytics.getVendorPerformance() == null || analytics.getVendorPerformance().isEmpty()) {
            return "<tr><td colspan=\"6\" style=\"" + CELL_MUTED_CENTER + "\">No vendor data</td></tr>";
        }
        java.util.List<WeeklyVendorPerformanceRow> rows = analytics.getVendorPerformance();
        StringBuilder html = new StringBuilder();
        for (WeeklyVendorPerformanceRow row : rows.subList(0, Math.min(rows.size(), EscalationEmailTemplateHelper.MAX_DISPLAY_ROWS))) {
            html.append("<tr>");
            html.append("<td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getVendorName())).append("</td>");
            html.append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getNewBreachesThisWeek()).append("</td>");
            html.append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getResolvedWithinSlaCount()).append("</td>");
            html.append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getResolvedAfterBreachCount()).append("</td>");
            html.append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getResponseRatePct()).append("%</td>");
            html.append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getResolutionRatePct()).append("%</td>");
            html.append("</tr>");
        }
        EscalationEmailTemplateHelper.appendMoreRow(html, 6, rows.size());
        return html.toString();
    }

    /** Vendor-scoped only - Procurement should not see CRM/Tech PoC/State SPOC-stage tickets. */
    private String renderBottlenecks(WeeklyEscalationAnalytics analytics) {
        if (analytics.getVendorBottlenecks() == null || analytics.getVendorBottlenecks().isEmpty()) {
            return "<tr><td colspan=\"3\" style=\"" + CELL_MUTED_CENTER + "\">No bottlenecks</td></tr>";
        }
        java.util.List<WeeklyBottleneckRow> rows = analytics.getVendorBottlenecks();
        StringBuilder html = new StringBuilder();
        for (WeeklyBottleneckRow row : rows.subList(0, Math.min(rows.size(), EscalationEmailTemplateHelper.MAX_DISPLAY_ROWS))) {
            html.append("<tr><td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getStatusLabel())).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getCount()).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getPctOfTotalOpen()).append("%</td></tr>");
        }
        EscalationEmailTemplateHelper.appendMoreRow(html, 3, rows.size());
        return html.toString();
    }

    private String renderFacilityRows(WeeklyEscalationAnalytics analytics) {
        if (analytics.getFacilitiesRestored() == null || analytics.getFacilitiesRestored().isEmpty()) {
            return "<tr><td colspan=\"3\" style=\"" + CELL_MUTED_CENTER + "\">No facilities restored this week</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        int total = 0;
        for (WeeklyFacilityRestoredRow row : analytics.getFacilitiesRestored()) {
            total += row.getRestoredCount();
            html.append("<tr><td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getStateName())).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getRestoredCount()).append("</td>")
                    .append("<td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getVendorNames())).append("</td></tr>");
        }
        html.append("<tr><td style=\"").append(CELL).append("font-weight:700\">TOTAL</td><td style=\"").append(CELL_RIGHT).append("font-weight:700\">")
                .append(total).append("</td><td style=\"").append(CELL).append("\"></td></tr>");
        return html.toString();
    }
}
