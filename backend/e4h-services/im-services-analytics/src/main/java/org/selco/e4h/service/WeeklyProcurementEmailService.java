package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.WeeklyEscalationAnalytics;
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
        return String.format("Weekly Procurement Update | %s", analytics.getWeekRangeLabel());
    }

    public String generateEmailHtml(WeeklyEscalationAnalytics analytics, String recipientName, String downloadUrl) {
        try {
            String template = EscalationEmailTemplateHelper.loadTemplate(TEMPLATE_PATH);
            Map<String, String> variables = EscalationEmailTemplateHelper.baseBrandingVariables(commonUtility, recipientName);
            variables.put("WEEK_RANGE", commonUtility.escapeHtml(analytics.getWeekRangeLabel()));
            variables.put("AS_OF_DATE", commonUtility.escapeHtml(analytics.getAsOfDate()));
            variables.put("VENDOR_ROWS", renderVendorRows(analytics));
            variables.put("BOTTLENECK_ROWS", renderBottlenecks(analytics));
            variables.put("FACILITY_SUMMARY", renderFacilitySummary(analytics));
            variables.put("DOWNLOAD_BUTTON", EscalationEmailTemplateHelper.renderDownloadButton(commonUtility, downloadUrl));
            variables.put("DASHBOARD_URL", commonUtility.generateStateDashboardUrl());
            return EscalationEmailTemplateHelper.render(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate weekly Procurement email HTML", e);
            return "<html><body><p>Weekly Procurement email could not be generated.</p></body></html>";
        }
    }

    private String renderVendorRows(WeeklyEscalationAnalytics analytics) {
        if (analytics.getVendorPerformance() == null || analytics.getVendorPerformance().isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"muted center\">No vendor data</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        for (WeeklyVendorPerformanceRow row : analytics.getVendorPerformance()) {
            html.append("<tr>");
            html.append("<td>").append(commonUtility.escapeHtml(row.getVendorName())).append("</td>");
            html.append("<td class=\"right\">").append(row.getTicketsAssigned()).append("</td>");
            html.append("<td class=\"right\">").append(row.getResolvedCount()).append("</td>");
            html.append("<td class=\"right\">").append(row.getResolutionRatePct()).append("%</td>");
            html.append("</tr>");
        }
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

    private String renderFacilitySummary(WeeklyEscalationAnalytics analytics) {
        return "<p>Facilities with open vendor tickets: <strong>" + analytics.getFacilitiesWithOpenTickets()
                + "</strong> | NF facilities affected: <strong>"
                + analytics.getNfFacilitiesWithOpenTickets() + "</strong></p>";
    }
}
