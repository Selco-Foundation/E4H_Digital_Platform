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
        return String.format("[Weekly Programme Summary] E4H SLA & Facility Status | Week of %s", analytics.getWeekRangeLabel());
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
            variables.put("DOWNLOAD_BUTTON", EscalationEmailTemplateHelper.renderDownloadButton(downloadUrl));
            variables.put("DASHBOARD_URL", commonUtility.generateStateDashboardUrl());
            return EscalationEmailTemplateHelper.render(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate weekly Leadership email HTML", e);
            return "<html><body><p>Weekly Leadership email could not be generated.</p></body></html>";
        }
    }

    private static final String CELL = "padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:left;";
    private static final String CELL_RIGHT = "padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:right;";
    private static final String CELL_MUTED_CENTER = "padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;color:#6b7280;text-align:center;";

    private String renderKeyHighlights(WeeklyEscalationAnalytics analytics) {
        int facilitiesRestored = analytics.getFacilitiesRestored() == null ? 0
                : analytics.getFacilitiesRestored().stream().mapToInt(r -> r.getRestoredCount()).sum();
        int ticketsResolved = (analytics.getNationalOverview() != null
                ? analytics.getNationalOverview().getResolvedWithinSla() + analytics.getNationalOverview().getResolvedAfterBreach()
                : 0);
        String vendorLine = analytics.getVendorLowestTatName() != null
                ? commonUtility.escapeHtml(analytics.getVendorLowestTatName()) + " (" + analytics.getVendorLowestTatDays() + "d)"
                : "No resolved vendor tickets this week";

        return "<ul style=\"font-size:14px;line-height:20px;margin:0;\">"
                + "<li>Facilities moved Non-Functional -&gt; Functional: <strong>" + facilitiesRestored + "</strong></li>"
                + "<li>Tickets Resolved This Week: <strong>" + ticketsResolved + "</strong></li>"
                + "<li>Vendor with Lowest Average TAT: <strong>" + vendorLine + "</strong></li>"
                + "</ul>";
    }

    private String renderEffectiveness(WeeklyEscalationAnalytics analytics) {
        if (analytics.getEscalationEffectiveness() == null || analytics.getEscalationEffectiveness().isEmpty()) {
            return "<tr><td colspan=\"4\" style=\"" + CELL_MUTED_CENTER + "\">No escalations last week</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        int totalEscalated = 0;
        int totalResolved = 0;
        for (WeeklyEscalationEffectivenessRow row : analytics.getEscalationEffectiveness()) {
            totalEscalated += row.getEscalatedCount();
            totalResolved += row.getResolvedCount();
            html.append("<tr><td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getLevelLabel())).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getEscalatedCount()).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getResolvedCount()).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getResolutionRatePct()).append("%</td></tr>");
        }
        double totalRate = totalEscalated > 0 ? Math.round((totalResolved * 100.0 / totalEscalated) * 10.0) / 10.0 : 0;
        html.append("<tr><td style=\"").append(CELL).append("font-weight:700\">TOTAL</td>")
                .append("<td style=\"").append(CELL_RIGHT).append("font-weight:700\">").append(totalEscalated).append("</td>")
                .append("<td style=\"").append(CELL_RIGHT).append("font-weight:700\">").append(totalResolved).append("</td>")
                .append("<td style=\"").append(CELL_RIGHT).append("font-weight:700\">").append(totalRate).append("%</td></tr>");
        return html.toString();
    }

    private String renderTrend(WeeklyEscalationAnalytics analytics) {
        if (analytics.getOverallTrend() == null || analytics.getOverallTrend().isEmpty()) {
            return "<tr><td colspan=\"4\" style=\"" + CELL_MUTED_CENTER + "\">Trend data unavailable</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        for (WeeklyTrendMetric row : analytics.getOverallTrend()) {
            html.append("<tr><td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getLabel())).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getLastWeekValue()).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getThisWeekValue()).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getArrow()).append(" ")
                    .append(Math.abs(row.getChangePct())).append("%</td></tr>");
        }
        return html.toString();
    }

    private String renderStateNfTrend(WeeklyEscalationAnalytics analytics) {
        if (analytics.getStateNfTrend() == null || analytics.getStateNfTrend().isEmpty()) {
            return "<tr><td colspan=\"6\" style=\"" + CELL_MUTED_CENTER + "\">No data</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        int sumLastWeek = 0;
        int sumMoved = 0;
        int sumNew = 0;
        int sumThisWeek = 0;
        int sumTotalFacilities = 0;
        for (WeeklyStateNfTrendRow row : analytics.getStateNfTrend()) {
            sumLastWeek += row.getTotalNfLastWeek();
            sumMoved += row.getFacilitiesMovedToFunctional();
            sumNew += row.getNewNonFunctionalThisWeek();
            sumThisWeek += row.getTotalNfThisWeek();
            sumTotalFacilities += row.getTotalFacilitiesThisWeek();
            html.append("<tr><td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getStateName())).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getTotalNfLastWeek()).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getFacilitiesMovedToFunctional()).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getNewNonFunctionalThisWeek()).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getTotalNfThisWeek()).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getNfPctThisWeek()).append("%</td></tr>");
        }
        // Weighted by facility count, not an average of the per-state percentages, so a large
        // state doesn't get diluted to the same weight as a small one.
        double overallPct = sumTotalFacilities > 0 ? (sumThisWeek * 100.0 / sumTotalFacilities) : 0;
        html.append("<tr><td style=\"").append(CELL).append("font-weight:700\">TOTAL</td>")
                .append("<td style=\"").append(CELL_RIGHT).append("font-weight:700\">").append(sumLastWeek).append("</td>")
                .append("<td style=\"").append(CELL_RIGHT).append("font-weight:700\">").append(sumMoved).append("</td>")
                .append("<td style=\"").append(CELL_RIGHT).append("font-weight:700\">").append(sumNew).append("</td>")
                .append("<td style=\"").append(CELL_RIGHT).append("font-weight:700\">").append(sumThisWeek).append("</td>")
                .append("<td style=\"").append(CELL_RIGHT).append("font-weight:700\">")
                .append(Math.round(overallPct * 10.0) / 10.0).append("%</td></tr>");
        return html.toString();
    }

    private String renderTheft(WeeklyEscalationAnalytics analytics) {
        StringBuilder html = new StringBuilder();
        html.append("<p style=\"font-size:14px;line-height:20px;margin:0;\">Open theft cases: <strong>").append(analytics.getOpenTheftCases()).append("</strong></p>");
        if (analytics.getTheftCases() == null || analytics.getTheftCases().isEmpty()) {
            html.append("<p style=\"color:#6b7280;font-size:13px;\">No new theft cases this week.</p>");
            return html.toString();
        }
        html.append("<table style=\"width:100%;border-collapse:collapse;margin-top:8px;\"><thead><tr>")
                .append("<th style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:left;color:#fff;font-weight:700;background:#b83227;\">State</th>")
                .append("<th style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:left;color:#fff;font-weight:700;background:#b83227;\">Health Facility</th>")
                .append("<th style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:left;color:#fff;font-weight:700;background:#b83227;\">District</th>")
                .append("<th style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:left;color:#fff;font-weight:700;background:#b83227;\">Date Reported</th>")
                .append("<th style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:left;color:#fff;font-weight:700;background:#b83227;\">Current Status</th>")
                .append("</tr></thead><tbody>");
        java.util.List<WeeklyTheftCaseRow> theftRows = analytics.getTheftCases();
        for (WeeklyTheftCaseRow row : theftRows.subList(0, Math.min(theftRows.size(), EscalationEmailTemplateHelper.MAX_DISPLAY_ROWS))) {
            html.append("<tr><td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getStateName())).append("</td>")
                    .append("<td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getHealthFacilityName())).append("</td>")
                    .append("<td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getDistrict())).append("</td>")
                    .append("<td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getFiledDateFormatted())).append("</td>")
                    .append("<td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getStatusLabel())).append("</td></tr>");
        }
        EscalationEmailTemplateHelper.appendMoreRow(html, 5, theftRows.size());
        html.append("</tbody></table>");
        return html.toString();
    }

    private String renderBottlenecks(WeeklyEscalationAnalytics analytics) {
        if (analytics.getBottlenecks() == null || analytics.getBottlenecks().isEmpty()) {
            return "<tr><td colspan=\"4\" style=\"" + CELL_MUTED_CENTER + "\">No bottlenecks</td></tr>";
        }
        java.util.List<WeeklyBottleneckRow> rows = analytics.getBottlenecks();
        StringBuilder html = new StringBuilder();
        for (WeeklyBottleneckRow row : rows.subList(0, Math.min(rows.size(), EscalationEmailTemplateHelper.MAX_DISPLAY_ROWS))) {
            html.append("<tr><td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getStatusLabel())).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getCount()).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getAvgDaysOpen()).append("</td>")
                    .append("<td style=\"").append(CELL_RIGHT).append("\">").append(row.getPctOfTotalOpen()).append("%</td></tr>");
        }
        EscalationEmailTemplateHelper.appendMoreRow(html, 4, rows.size());
        return html.toString();
    }

    private String renderNfAlerts(WeeklyEscalationAnalytics analytics) {
        double threshold = escalationProperties.getLeadership().getNfThresholdPct();
        if (analytics.getNfAlerts() == null || analytics.getNfAlerts().isEmpty()) {
            return "<p style=\"font-size:14px;line-height:20px;margin:0;\">No states currently exceed the " + threshold + "% Non-Functional threshold.</p>";
        }
        StringBuilder html = new StringBuilder();
        for (WeeklyNfAlert alert : analytics.getNfAlerts()) {
            html.append("<div style=\"background:#fef3c7;border:1px solid #f59e0b;border-radius:8px;padding:12px;margin:8px 0;font-size:13px;\"><strong>").append(commonUtility.escapeHtml(alert.getStateName()))
                    .append("</strong> - Non-Functional rate is ").append(alert.getNfPct())
                    .append("% (exceeds ").append(threshold).append("% threshold)<br>")
                    .append("Primary bottleneck: ").append(commonUtility.escapeHtml(alert.getPrimaryBottleneck()))
                    .append("</div>");
        }
        return html.toString();
    }
}
