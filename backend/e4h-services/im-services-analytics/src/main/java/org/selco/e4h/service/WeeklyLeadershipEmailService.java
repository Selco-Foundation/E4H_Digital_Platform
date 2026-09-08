package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.EscalationProperties;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.WeeklyBottleneckRow;
import org.selco.e4h.web.models.WeeklyEscalationAnalytics;
import org.selco.e4h.web.models.WeeklyEscalationEffectivenessRow;
import org.selco.e4h.web.models.WeeklyNfAlert;
import org.selco.e4h.web.models.WeeklyStateNfTrendRow;
import org.selco.e4h.web.models.WeeklyTheftCaseRow;
import org.selco.e4h.web.models.WeeklyTrendMetric;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyLeadershipEmailService {

    private static final String TEMPLATE_PATH = "templates/weekly_leadership_email.html";

    private final CommonUtility commonUtility;
    private final EscalationProperties escalationProperties;

    public String generateEmailSubject(WeeklyEscalationAnalytics analytics) {
        return String.format("Weekly Programme Summary | %s", analytics.getWeekRangeLabel());
    }

    public String generateEmailHtml(WeeklyEscalationAnalytics analytics, String recipientName, String downloadUrl) {
        try {
            String template = EscalationEmailTemplateHelper.loadTemplate(TEMPLATE_PATH);
            Map<String, String> variables = EscalationEmailTemplateHelper.baseBrandingVariables(commonUtility, recipientName);
            variables.put("WEEK_RANGE", commonUtility.escapeHtml(analytics.getWeekRangeLabel()));
            variables.put("AS_OF_DATE", commonUtility.escapeHtml(analytics.getAsOfDate()));
            variables.put("KEY_HIGHLIGHTS", renderKeyHighlights(analytics));
            variables.put("EFFECTIVENESS_ROWS", renderEffectiveness(analytics));
            variables.put("TREND_ROWS", renderTrend(analytics));
            variables.put("STATE_NF_TREND_ROWS", renderStateNfTrend(analytics));
            variables.put("THEFT_SUMMARY", renderTheft(analytics));
            variables.put("BOTTLENECK_ROWS", renderBottlenecks(analytics));
            variables.put("NF_ALERTS", renderNfAlerts(analytics));
            variables.put("DOWNLOAD_BUTTON", EscalationEmailTemplateHelper.renderDownloadButton(commonUtility, downloadUrl));
            variables.put("DASHBOARD_URL", commonUtility.generateStateDashboardUrl());
            return EscalationEmailTemplateHelper.render(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate weekly Leadership email HTML", e);
            return "<html><body><p>Weekly Leadership email could not be generated.</p></body></html>";
        }
    }

    private String renderKeyHighlights(WeeklyEscalationAnalytics analytics) {
        int facilitiesRestored = analytics.getFacilitiesRestored() == null ? 0
                : analytics.getFacilitiesRestored().stream().mapToInt(r -> r.getRestoredCount()).sum();
        int ticketsResolved = (analytics.getNationalOverview() != null
                ? analytics.getNationalOverview().getResolvedWithinSla() + analytics.getNationalOverview().getResolvedAfterBreach()
                : 0);
        String vendorLine = analytics.getVendorLowestTatName() != null
                ? commonUtility.escapeHtml(analytics.getVendorLowestTatName()) + " (" + analytics.getVendorLowestTatDays() + "d)"
                : "No resolved vendor tickets this week";

        return "<ul>"
                + "<li>Facilities moved Non-Functional → Functional: <strong>" + facilitiesRestored + "</strong></li>"
                + "<li>Tickets Resolved This Week: <strong>" + ticketsResolved + "</strong></li>"
                + "<li>Vendor with Lowest Average TAT: <strong>" + vendorLine + "</strong></li>"
                + "</ul>";
    }

    private String renderEffectiveness(WeeklyEscalationAnalytics analytics) {
        if (analytics.getEscalationEffectiveness() == null || analytics.getEscalationEffectiveness().isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"muted center\">No escalations last week</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        int totalEscalated = 0;
        int totalResolved = 0;
        for (WeeklyEscalationEffectivenessRow row : analytics.getEscalationEffectiveness()) {
            totalEscalated += row.getEscalatedCount();
            totalResolved += row.getResolvedCount();
            html.append("<tr><td>").append(commonUtility.escapeHtml(row.getLevelLabel())).append("</td>")
                    .append("<td class=\"right\">").append(row.getEscalatedCount()).append("</td>")
                    .append("<td class=\"right\">").append(row.getResolvedCount()).append("</td>")
                    .append("<td class=\"right\">").append(row.getResolutionRatePct()).append("%</td></tr>");
        }
        double totalRate = totalEscalated > 0 ? Math.round((totalResolved * 100.0 / totalEscalated) * 10.0) / 10.0 : 0;
        html.append("<tr><td style=\"font-weight:700\">TOTAL</td>")
                .append("<td class=\"right\" style=\"font-weight:700\">").append(totalEscalated).append("</td>")
                .append("<td class=\"right\" style=\"font-weight:700\">").append(totalResolved).append("</td>")
                .append("<td class=\"right\" style=\"font-weight:700\">").append(totalRate).append("%</td></tr>");
        return html.toString();
    }

    private String renderTrend(WeeklyEscalationAnalytics analytics) {
        if (analytics.getOverallTrend() == null || analytics.getOverallTrend().isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"muted center\">Trend data unavailable</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        for (WeeklyTrendMetric row : analytics.getOverallTrend()) {
            html.append("<tr><td>").append(commonUtility.escapeHtml(row.getLabel())).append("</td>")
                    .append("<td class=\"right\">").append(row.getLastWeekValue()).append("</td>")
                    .append("<td class=\"right\">").append(row.getThisWeekValue()).append("</td>")
                    .append("<td class=\"right\">").append(row.getArrow()).append(" ")
                    .append(Math.abs(row.getChangePct())).append("%</td></tr>");
        }
        return html.toString();
    }

    private String renderStateNfTrend(WeeklyEscalationAnalytics analytics) {
        if (analytics.getStateNfTrend() == null || analytics.getStateNfTrend().isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"muted center\">No data</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        for (WeeklyStateNfTrendRow row : analytics.getStateNfTrend()) {
            html.append("<tr><td>").append(commonUtility.escapeHtml(row.getStateName())).append("</td>")
                    .append("<td class=\"right\">").append(row.getTotalNfLastWeek()).append("</td>")
                    .append("<td class=\"right\">").append(row.getTotalNfThisWeek()).append("</td>")
                    .append("<td class=\"right\">").append(row.getNfPctThisWeek()).append("%</td></tr>");
        }
        return html.toString();
    }

    private String renderTheft(WeeklyEscalationAnalytics analytics) {
        StringBuilder html = new StringBuilder();
        html.append("<p>Open theft cases: <strong>").append(analytics.getOpenTheftCases()).append("</strong></p>");
        if (analytics.getTheftCases() == null || analytics.getTheftCases().isEmpty()) {
            html.append("<p class=\"muted\">No new theft cases this week.</p>");
            return html.toString();
        }
        html.append("<table><thead><tr><th>State</th><th>Health Facility</th><th>District</th><th>Date</th><th>Status</th></tr></thead><tbody>");
        for (WeeklyTheftCaseRow row : analytics.getTheftCases()) {
            html.append("<tr><td>").append(commonUtility.escapeHtml(row.getStateName())).append("</td>")
                    .append("<td>").append(commonUtility.escapeHtml(row.getHealthFacilityName())).append("</td>")
                    .append("<td>").append(commonUtility.escapeHtml(row.getDistrict())).append("</td>")
                    .append("<td>").append(commonUtility.escapeHtml(row.getFiledDateFormatted())).append("</td>")
                    .append("<td>").append(commonUtility.escapeHtml(row.getStatusLabel())).append("</td></tr>");
        }
        html.append("</tbody></table>");
        return html.toString();
    }

    private String renderBottlenecks(WeeklyEscalationAnalytics analytics) {
        if (analytics.getBottlenecks() == null || analytics.getBottlenecks().isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"muted center\">No bottlenecks</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        for (WeeklyBottleneckRow row : analytics.getBottlenecks()) {
            html.append("<tr><td>").append(commonUtility.escapeHtml(row.getStatusLabel())).append("</td>")
                    .append("<td class=\"right\">").append(row.getCount()).append("</td>")
                    .append("<td class=\"right\">").append(row.getAvgDaysOpen()).append("</td>")
                    .append("<td class=\"right\">").append(row.getPctOfTotalOpen()).append("%</td></tr>");
        }
        return html.toString();
    }

    private String renderNfAlerts(WeeklyEscalationAnalytics analytics) {
        double threshold = escalationProperties.getLeadership().getNfThresholdPct();
        if (analytics.getNfAlerts() == null || analytics.getNfAlerts().isEmpty()) {
            return "<p>✅ No states currently exceed the " + threshold + "% Non-Functional threshold.</p>";
        }
        StringBuilder html = new StringBuilder();
        for (WeeklyNfAlert alert : analytics.getNfAlerts()) {
            html.append("<div class=\"alert\">🔴 <strong>").append(commonUtility.escapeHtml(alert.getStateName()))
                    .append("</strong> — Non-Functional rate is ").append(alert.getNfPct())
                    .append("% (exceeds ").append(threshold).append("% threshold)<br>")
                    .append("Primary bottleneck: ").append(commonUtility.escapeHtml(alert.getPrimaryBottleneck()))
                    .append("</div>");
        }
        return html.toString();
    }
}
