package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.WeeklyBottleneckRow;
import org.selco.e4h.web.models.WeeklyEscalationAnalytics;
import org.selco.e4h.web.models.WeeklyFacilityRestoredRow;
import org.selco.e4h.web.models.WeeklyTicketOverview;
import org.selco.e4h.web.models.WeeklyTrendMetric;
import org.selco.e4h.web.models.WeeklyVendorPerformanceRow;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklySeniorProgramManagerEmailService {

    private static final String TEMPLATE_PATH = "templates/weekly_senior_program_manager_email.html";

    private final CommonUtility commonUtility;

    public String generateEmailSubject(WeeklyEscalationAnalytics analytics) {
        return String.format("Weekly Escalation Summary | %s | %s",
                analytics.getStateListLabel(), analytics.getWeekRangeLabel());
    }

    public String generateEmailHtml(WeeklyEscalationAnalytics analytics, String recipientName, String downloadUrl) {
        try {
            String template = EscalationEmailTemplateHelper.loadTemplate(TEMPLATE_PATH);
            Map<String, String> variables = EscalationEmailTemplateHelper.baseBrandingVariables(commonUtility, recipientName);
            variables.put("WEEK_RANGE", commonUtility.escapeHtml(analytics.getWeekRangeLabel()));
            variables.put("AS_OF_DATE", commonUtility.escapeHtml(analytics.getAsOfDate()));
            variables.put("STATE_LIST", commonUtility.escapeHtml(analytics.getStateListLabel()));
            variables.put("OVERVIEW_ROWS", renderOverviewRows(analytics));
            variables.put("OVERVIEW_TOTAL_SUMMARY", renderOverviewTotalSummary(analytics));
            variables.put("VENDOR_ROWS", renderVendorRows(analytics));
            variables.put("BOTTLENECK_ROWS", renderBottlenecks(analytics));
            variables.put("FACILITY_ROWS", renderFacilityRows(analytics));
            variables.put("DOWNLOAD_BUTTON", EscalationEmailTemplateHelper.renderDownloadButton(commonUtility, downloadUrl));
            variables.put("DASHBOARD_URL", commonUtility.generateStateDashboardUrl());
            return EscalationEmailTemplateHelper.render(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate weekly SPM email HTML", e);
            return "<html><body><p>Weekly SPM email could not be generated.</p></body></html>";
        }
    }

    private String renderOverviewRows(WeeklyEscalationAnalytics analytics) {
        StringBuilder html = new StringBuilder();
        if (analytics.getOverviewByState() != null) {
            analytics.getOverviewByState().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> appendOverviewRow(html, commonUtility.getStateDisplayName(e.getKey()), e.getValue(), false));
        }
        appendOverviewRow(html, "TOTAL", analytics.getNationalOverview(), true);
        return html.toString();
    }

    private void appendOverviewRow(StringBuilder html, String label, WeeklyTicketOverview o, boolean isTotal) {
        if (o == null) {
            return;
        }
        String style = isTotal ? " style=\"font-weight:700\"" : "";
        html.append("<tr><td").append(style).append(">").append(commonUtility.escapeHtml(label)).append("</td>")
                .append("<td class=\"right\">").append(o.getRaisedThisWeek()).append("</td>")
                .append("<td class=\"right\">").append(o.getNewBreaches()).append("</td>")
                .append("<td class=\"right\">").append(o.getResolvedWithinSla()).append("</td>")
                .append("<td class=\"right\">").append(o.getResolvedAfterBreach()).append("</td>")
                .append("<td class=\"right\">").append(o.getCarriedForward()).append("</td>")
                .append("<td class=\"right\">").append(o.getTotalOpen()).append("</td></tr>");
    }

    private String renderOverviewTotalSummary(WeeklyEscalationAnalytics analytics) {
        WeeklyTrendMetric trend = analytics.getTotalOpenTrend();
        int totalOpen = analytics.getNationalOverview() != null ? analytics.getNationalOverview().getTotalOpen() : 0;
        if (trend == null) {
            return "<p>Overall this week: <strong>" + totalOpen + "</strong> tickets open across your states</p>";
        }
        return "<p>Overall this week: <strong>" + totalOpen + "</strong> tickets open across your states ("
                + trend.getArrow() + " " + Math.abs(trend.getChangePct()) + "% vs last week)</p>";
    }

    private String renderVendorRows(WeeklyEscalationAnalytics analytics) {
        if (analytics.getVendorPerformance() == null || analytics.getVendorPerformance().isEmpty()) {
            return "<tr><td colspan=\"6\" class=\"muted center\">No vendor data</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        for (WeeklyVendorPerformanceRow row : analytics.getVendorPerformance()) {
            html.append("<tr>");
            html.append("<td>").append(commonUtility.escapeHtml(row.getVendorName())).append("</td>");
            html.append("<td class=\"right\">").append(row.getNewBreachesThisWeek()).append("</td>");
            html.append("<td class=\"right\">").append(row.getResolvedWithinSlaCount()).append("</td>");
            html.append("<td class=\"right\">").append(row.getResolvedAfterBreachCount()).append("</td>");
            html.append("<td class=\"right\">").append(row.getResponseRatePct()).append("%</td>");
            html.append("<td class=\"right\">").append(row.getResolutionRatePct()).append("%</td>");
            html.append("</tr>");
        }
        return html.toString();
    }

    private String renderBottlenecks(WeeklyEscalationAnalytics analytics) {
        if (analytics.getBottlenecks() == null || analytics.getBottlenecks().isEmpty()) {
            return "<tr><td colspan=\"3\" class=\"muted center\">No bottlenecks</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        for (WeeklyBottleneckRow row : analytics.getBottlenecks()) {
            html.append("<tr><td>").append(commonUtility.escapeHtml(row.getStatusLabel())).append("</td>")
                    .append("<td class=\"right\">").append(row.getCount()).append("</td>")
                    .append("<td class=\"right\">").append(row.getPctOfTotalOpen()).append("%</td></tr>");
        }
        return html.toString();
    }

    private String renderFacilityRows(WeeklyEscalationAnalytics analytics) {
        if (analytics.getFacilitiesRestored() == null || analytics.getFacilitiesRestored().isEmpty()) {
            return "<tr><td colspan=\"2\" class=\"muted center\">No facilities restored this week</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        int total = 0;
        for (WeeklyFacilityRestoredRow row : analytics.getFacilitiesRestored()) {
            total += row.getRestoredCount();
            html.append("<tr><td>").append(commonUtility.escapeHtml(row.getStateName())).append("</td>")
                    .append("<td class=\"right\">").append(row.getRestoredCount()).append("</td></tr>");
        }
        html.append("<tr><td style=\"font-weight:700\">TOTAL</td><td class=\"right\" style=\"font-weight:700\">")
                .append(total).append("</td></tr>");
        return html.toString();
    }
}
