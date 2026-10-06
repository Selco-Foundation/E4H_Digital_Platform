package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.DailySeniorProgramManagerSummary;
import org.selco.e4h.web.models.StateDailyBreachSection;
import org.selco.e4h.web.models.StatePreviouslyOpenRow;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailySeniorProgramManagerEmailService {

    private static final String TEMPLATE_PATH = "templates/daily_senior_program_manager_email.html";

    private final CommonUtility commonUtility;

    public String generateEmailSubject(DailySeniorProgramManagerSummary summary) {
        return String.format("[Action Required] SLA Breach Escalation - %s | %s",
                summary.getRecipientName(), summary.getAsOfDate());
    }

    public String generateEmailHtml(DailySeniorProgramManagerSummary summary, String downloadUrl) {
        try {
            String template = EscalationEmailTemplateHelper.loadTemplate(TEMPLATE_PATH);
            Map<String, String> variables = EscalationEmailTemplateHelper.baseBrandingVariables(
                    commonUtility, summary.getRecipientName());
            variables.put("STATE_LIST", commonUtility.escapeHtml(summary.getStateListLabel()));
            variables.put("AS_OF_DATE", commonUtility.escapeHtml(summary.getAsOfDate()));
            variables.put("STATE_SECTIONS", renderStateSections(summary.getStateSections()));
            variables.put("PREVIOUSLY_OPEN_ROWS", renderPreviouslyOpenRows(summary.getPreviouslyOpenByState()));
            variables.put("DOWNLOAD_BUTTON", EscalationEmailTemplateHelper.renderDownloadButton(downloadUrl));
            variables.put("DASHBOARD_URL", summary.getDashboardUrl());
            return EscalationEmailTemplateHelper.render(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate daily SPM email HTML", e);
            return "<html><body><p>Daily SPM escalation email could not be generated.</p></body></html>";
        }
    }

    private String renderStateSections(java.util.List<StateDailyBreachSection> sections) {
        if (sections == null || sections.isEmpty()) {
            return "<p style=\"color:#6b7280;font-size:13px;\">No new breaches today across your states.</p>";
        }
        StringBuilder html = new StringBuilder();
        for (StateDailyBreachSection section : sections) {
            html.append("<p style=\"color:#fff;font-weight:700;padding:10px 14px;border-radius:2px;margin:20px 0 8px;font-size:14px;background:#55707f;\">")
                    .append(commonUtility.escapeHtml(section.getStateName())).append("</p>");
            html.append("<table style=\"width:100%;border-collapse:collapse;margin-top:8px;\"><thead><tr>")
                    .append("<th style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:left;color:#fff;font-weight:700;background:#55707f;\">Role Name</th>")
                    .append("<th style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:right;color:#fff;font-weight:700;background:#55707f;\">Count of Tickets</th>")
                    .append("</tr></thead><tbody>");
            html.append(EscalationEmailTemplateHelper.renderActorRows(commonUtility, section.getBreaches(), false, "#f97316"));
            html.append("</tbody></table>");
        }
        return html.toString();
    }

    private String renderPreviouslyOpenRows(java.util.List<StatePreviouslyOpenRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return "<tr><td colspan=\"4\" style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;color:#6b7280;text-align:center;\">No data</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        long totalStatePoc = 0;
        long totalVendor = 0;
        for (StatePreviouslyOpenRow row : rows) {
            long total = row.getStatePocCount() + row.getVendorCount();
            totalStatePoc += row.getStatePocCount();
            totalVendor += row.getVendorCount();
            html.append("<tr><td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:left;\">").append(commonUtility.escapeHtml(row.getStateName())).append("</td>")
                    .append("<td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:right;\">").append(row.getStatePocCount()).append("</td>")
                    .append("<td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:right;\">").append(row.getVendorCount()).append("</td>")
                    .append("<td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:right;\">").append(total).append("</td></tr>");
        }
        html.append("<tr><td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;font-weight:700;\">TOTAL</td>")
                .append("<td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:right;font-weight:700;\">").append(totalStatePoc).append("</td>")
                .append("<td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:right;font-weight:700;\">").append(totalVendor).append("</td>")
                .append("<td style=\"padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:right;font-weight:700;\">").append(totalStatePoc + totalVendor).append("</td></tr>");
        return html.toString();
    }
}
