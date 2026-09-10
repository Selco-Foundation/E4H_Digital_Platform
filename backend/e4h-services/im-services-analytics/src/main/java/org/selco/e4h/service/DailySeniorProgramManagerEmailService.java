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
        return String.format("[Action Required] SLA Breach Escalation — %s | %s",
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
            return "<p class=\"muted\">No new breaches today across your states.</p>";
        }
        StringBuilder html = new StringBuilder();
        for (StateDailyBreachSection section : sections) {
            html.append("<p class=\"banner b-slate\">▸ ")
                    .append(commonUtility.escapeHtml(section.getStateName())).append("</p>");
            html.append("<table class=\"t-slate\"><thead><tr><th>Role Name</th><th class=\"right\">Count of Tickets</th></tr></thead><tbody>");
            html.append(EscalationEmailTemplateHelper.renderActorRows(commonUtility, section.getBreaches(), false));
            html.append("</tbody></table>");
        }
        return html.toString();
    }

    private String renderPreviouslyOpenRows(java.util.List<StatePreviouslyOpenRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"muted center\">No data</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        long totalStatePoc = 0;
        long totalVendor = 0;
        for (StatePreviouslyOpenRow row : rows) {
            long total = row.getStatePocCount() + row.getVendorCount();
            totalStatePoc += row.getStatePocCount();
            totalVendor += row.getVendorCount();
            html.append("<tr><td>").append(commonUtility.escapeHtml(row.getStateName())).append("</td>")
                    .append("<td class=\"right\">").append(row.getStatePocCount()).append("</td>")
                    .append("<td class=\"right\">").append(row.getVendorCount()).append("</td>")
                    .append("<td class=\"right\">").append(total).append("</td></tr>");
        }
        html.append("<tr><td style=\"font-weight:700\">TOTAL</td>")
                .append("<td class=\"right\" style=\"font-weight:700\">").append(totalStatePoc).append("</td>")
                .append("<td class=\"right\" style=\"font-weight:700\">").append(totalVendor).append("</td>")
                .append("<td class=\"right\" style=\"font-weight:700\">").append(totalStatePoc + totalVendor).append("</td></tr>");
        return html.toString();
    }
}
