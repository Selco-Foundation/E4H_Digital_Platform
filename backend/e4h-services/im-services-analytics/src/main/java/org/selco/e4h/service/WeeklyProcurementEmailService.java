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

    private String renderVendorRows(WeeklyEscalationAnalytics analytics) {
        if (analytics.getVendorPerformance() == null || analytics.getVendorPerformance().isEmpty()) {
            return "<tr><td colspan=\"6\" class=\"muted center\">No vendor data</td></tr>";
        }
        java.util.List<WeeklyVendorPerformanceRow> rows = analytics.getVendorPerformance();
        StringBuilder html = new StringBuilder();
        for (WeeklyVendorPerformanceRow row : rows.subList(0, Math.min(rows.size(), EscalationEmailTemplateHelper.MAX_DISPLAY_ROWS))) {
            html.append("<tr>");
            html.append("<td>").append(commonUtility.escapeHtml(row.getVendorName())).append("</td>");
            html.append("<td class=\"right\">").append(row.getNewBreachesThisWeek()).append("</td>");
            html.append("<td class=\"right\">").append(row.getResolvedWithinSlaCount()).append("</td>");
            html.append("<td class=\"right\">").append(row.getResolvedAfterBreachCount()).append("</td>");
            html.append("<td class=\"right\">").append(row.getResponseRatePct()).append("%</td>");
            html.append("<td class=\"right\">").append(row.getResolutionRatePct()).append("%</td>");
            html.append("</tr>");
        }
        EscalationEmailTemplateHelper.appendMoreRow(html, 6, rows.size());
        return html.toString();
    }

    private String renderBottlenecks(WeeklyEscalationAnalytics analytics) {
        if (analytics.getBottlenecks() == null || analytics.getBottlenecks().isEmpty()) {
            return "<tr><td colspan=\"3\" class=\"muted center\">No bottlenecks</td></tr>";
        }
        java.util.List<WeeklyBottleneckRow> rows = analytics.getBottlenecks();
        StringBuilder html = new StringBuilder();
        for (WeeklyBottleneckRow row : rows.subList(0, Math.min(rows.size(), EscalationEmailTemplateHelper.MAX_DISPLAY_ROWS))) {
            html.append("<tr><td>").append(commonUtility.escapeHtml(row.getStatusLabel())).append("</td>")
                    .append("<td class=\"right\">").append(row.getCount()).append("</td>")
                    .append("<td class=\"right\">").append(row.getPctOfTotalOpen()).append("%</td></tr>");
        }
        EscalationEmailTemplateHelper.appendMoreRow(html, 3, rows.size());
        return html.toString();
    }

    private String renderFacilityRows(WeeklyEscalationAnalytics analytics) {
        if (analytics.getFacilitiesRestored() == null || analytics.getFacilitiesRestored().isEmpty()) {
            return "<tr><td colspan=\"3\" class=\"muted center\">No facilities restored this week</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        int total = 0;
        for (WeeklyFacilityRestoredRow row : analytics.getFacilitiesRestored()) {
            total += row.getRestoredCount();
            html.append("<tr><td>").append(commonUtility.escapeHtml(row.getStateName())).append("</td>")
                    .append("<td class=\"right\">").append(row.getRestoredCount()).append("</td>")
                    .append("<td>").append(commonUtility.escapeHtml(row.getVendorNames())).append("</td></tr>");
        }
        html.append("<tr><td style=\"font-weight:700\">TOTAL</td><td class=\"right\" style=\"font-weight:700\">")
                .append(total).append("</td><td></td></tr>");
        return html.toString();
    }
}
