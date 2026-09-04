package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.EscalationProperties;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.WeeklyEscalationAnalytics;
import org.selco.e4h.web.models.WeeklyNfAlert;
import org.selco.e4h.web.models.WeeklyTicketOverview;
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
        return String.format("Weekly Leadership Escalation Report | %s", analytics.getWeekRangeLabel());
    }

    public String generateEmailHtml(WeeklyEscalationAnalytics analytics, String recipientName, String downloadUrl) {
        try {
            String template = EscalationEmailTemplateHelper.loadTemplate(TEMPLATE_PATH);
            Map<String, String> variables = EscalationEmailTemplateHelper.baseBrandingVariables(commonUtility, recipientName);
            variables.put("WEEK_RANGE", commonUtility.escapeHtml(analytics.getWeekRangeLabel()));
            variables.put("AS_OF_DATE", commonUtility.escapeHtml(analytics.getAsOfDate()));
            variables.put("TICKET_OVERVIEW", renderOverview(analytics.getNationalOverview()));
            variables.put("EFFECTIVENESS", renderEffectiveness(analytics));
            variables.put("TREND", renderTrend(analytics));
            variables.put("THEFT_SUMMARY", renderTheft(analytics));
            variables.put("NF_ALERTS", renderNfAlerts(analytics));
            variables.put("BOTTLENECK_ROWS", renderBottlenecks(analytics));
            variables.put("DOWNLOAD_BUTTON", EscalationEmailTemplateHelper.renderDownloadButton(commonUtility, downloadUrl));
            variables.put("DASHBOARD_URL", commonUtility.generateStateDashboardUrl());
            return EscalationEmailTemplateHelper.render(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate weekly Leadership email HTML", e);
            return "<html><body><p>Weekly Leadership email could not be generated.</p></body></html>";
        }
    }

    private String renderOverview(WeeklyTicketOverview overview) {
        if (overview == null) {
            return "<p class=\"muted\">No ticket data available.</p>";
        }
        return "<ul>"
                + "<li>Raised this week: <strong>" + overview.getRaisedThisWeek() + "</strong></li>"
                + "<li>Resolved within SLA: <strong>" + overview.getResolvedWithinSla() + "</strong></li>"
                + "<li>Resolved after breach: <strong>" + overview.getResolvedAfterBreach() + "</strong></li>"
                + "<li>Carried forward: <strong>" + overview.getCarriedForward() + "</strong></li>"
                + "</ul>";
    }

    private String renderEffectiveness(WeeklyEscalationAnalytics analytics) {
        return "<p>Escalated to L2/L3 last week: <strong>" + analytics.getEscalatedLastWeek()
                + "</strong> | Resolved since escalation: <strong>"
                + analytics.getResolvedSinceEscalation() + "</strong></p>";
    }

    private String renderTrend(WeeklyEscalationAnalytics analytics) {
        if (analytics.getWeekStartMetrics() == null || analytics.getWeekEndMetrics() == null) {
            return "<p class=\"muted\">Trend data unavailable.</p>";
        }
        int startTotal = analytics.getWeekStartMetrics().getFunctionalCount()
                + analytics.getWeekStartMetrics().getNonFunctionalCount();
        int endTotal = analytics.getWeekEndMetrics().getFunctionalCount()
                + analytics.getWeekEndMetrics().getNonFunctionalCount();
        double startFuncPct = startTotal > 0
                ? analytics.getWeekStartMetrics().getFunctionalCount() * 100.0 / startTotal : 0;
        double endFuncPct = endTotal > 0
                ? analytics.getWeekEndMetrics().getFunctionalCount() * 100.0 / endTotal : 0;
        return "<p>Functional facilities: <strong>" + String.format("%.1f", startFuncPct) + "%</strong>"
                + " → <strong>" + String.format("%.1f", endFuncPct) + "%</strong>"
                + " (threshold for NF alerts: " + escalationProperties.getLeadership().getNfThresholdPct() + "%)</p>";
    }

    private String renderTheft(WeeklyEscalationAnalytics analytics) {
        return "<p>Open theft cases: <strong>" + analytics.getOpenTheftCases()
                + "</strong> | New theft cases this week: <strong>"
                + analytics.getNewTheftThisWeek() + "</strong></p>";
    }

    private String renderNfAlerts(WeeklyEscalationAnalytics analytics) {
        if (analytics.getNfAlerts() == null || analytics.getNfAlerts().isEmpty()) {
            return "<p class=\"muted\">No states above NF threshold.</p>";
        }
        StringBuilder html = new StringBuilder("<table><thead><tr><th>State</th><th>NF %</th><th>Primary Bottleneck</th></tr></thead><tbody>");
        for (WeeklyNfAlert alert : analytics.getNfAlerts()) {
            html.append("<tr><td>").append(commonUtility.escapeHtml(alert.getStateName())).append("</td>");
            html.append("<td>").append(alert.getNfPct()).append("%</td>");
            html.append("<td>").append(commonUtility.escapeHtml(alert.getPrimaryBottleneck())).append("</td></tr>");
        }
        html.append("</tbody></table>");
        return html.toString();
    }

    private String renderBottlenecks(WeeklyEscalationAnalytics analytics) {
        if (analytics.getBottlenecks() == null || analytics.getBottlenecks().isEmpty()) {
            return "<tr><td colspan=\"2\" class=\"muted center\">No bottlenecks</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        analytics.getBottlenecks().forEach(row -> html.append("<tr><td>")
                .append(commonUtility.escapeHtml(row.getStatusLabel())).append("</td><td class=\"right\">")
                .append(row.getCount()).append("</td></tr>"));
        return html.toString();
    }
}
