package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.DailyProcurementSummary;
import org.selco.e4h.web.models.StateDailyBreachSection;
import org.selco.e4h.web.models.VendorStateCountRow;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyProcurementEmailService {

    private static final String TEMPLATE_PATH = "templates/daily_procurement_email.html";

    private final CommonUtility commonUtility;

    public String generateEmailSubject(DailyProcurementSummary summary) {
        return String.format("[Urgent] Vendor SLA Escalation - Procurement Action Required | %s", summary.getAsOfDate());
    }

    public String generateEmailHtml(DailyProcurementSummary summary, String downloadUrl) {
        try {
            String template = EscalationEmailTemplateHelper.loadTemplate(TEMPLATE_PATH);
            Map<String, String> variables = EscalationEmailTemplateHelper.baseBrandingVariables(
                    commonUtility, summary.getRecipientName());
            variables.put("AS_OF_DATE", commonUtility.escapeHtml(summary.getAsOfDate()));
            variables.put("STATE_SECTIONS", renderStateSections(summary.getNewBreachesByState()));
            variables.put("PREVIOUS_VENDOR_ROWS", renderPreviousRows(summary.getPreviouslyOpen()));
            variables.put("DOWNLOAD_BUTTON", EscalationEmailTemplateHelper.renderDownloadButton(downloadUrl));
            variables.put("DASHBOARD_URL", summary.getDashboardUrl());
            return EscalationEmailTemplateHelper.render(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate daily Procurement email HTML", e);
            return "<html><body><p>Daily Procurement escalation email could not be generated.</p></body></html>";
        }
    }

    private String renderStateSections(List<StateDailyBreachSection> sections) {
        if (sections == null || sections.isEmpty()) {
            return "<p class=\"muted\">No new escalations today.</p>";
        }
        StringBuilder html = new StringBuilder();
        for (StateDailyBreachSection section : sections) {
            html.append("<p class=\"banner b-slate\">&gt;")
                    .append(commonUtility.escapeHtml(section.getStateName())).append("</p>");
            html.append("<table class=\"t-slate\"><thead><tr><th>Vendor</th><th class=\"right\">Count of Tickets</th></tr></thead><tbody>");
            html.append(EscalationEmailTemplateHelper.renderActorRows(commonUtility, section.getBreaches(), false));
            html.append("</tbody></table>");
        }
        return html.toString();
    }

    private String renderPreviousRows(List<VendorStateCountRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return "<tr><td colspan=\"3\" class=\"muted center\">No breaches</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        for (VendorStateCountRow row : rows) {
            html.append("<tr>");
            html.append("<td>").append(commonUtility.escapeHtml(row.getStateName())).append("</td>");
            html.append("<td>").append(commonUtility.escapeHtml(row.getVendorName())).append("</td>");
            html.append("<td class=\"right\"><span class=\"badge\">").append(row.getCount()).append("</span></td>");
            html.append("</tr>");
        }
        return html.toString();
    }
}
