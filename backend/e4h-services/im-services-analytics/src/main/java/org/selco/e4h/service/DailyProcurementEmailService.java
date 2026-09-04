package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.DailyProcurementSummary;
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
        return String.format("[Action Required] Procurement SLA Breach Alert | %s", summary.getAsOfDate());
    }

    public String generateEmailHtml(DailyProcurementSummary summary, String downloadUrl) {
        try {
            String template = EscalationEmailTemplateHelper.loadTemplate(TEMPLATE_PATH);
            Map<String, String> variables = EscalationEmailTemplateHelper.baseBrandingVariables(
                    commonUtility, summary.getRecipientName());
            variables.put("AS_OF_DATE", commonUtility.escapeHtml(summary.getAsOfDate()));
            variables.put("NEW_VENDOR_ROWS", renderVendorRows(summary.getNewBreaches()));
            variables.put("PREVIOUS_VENDOR_ROWS", renderVendorRows(summary.getPreviouslyOpen()));
            variables.put("DOWNLOAD_BUTTON", EscalationEmailTemplateHelper.renderDownloadButton(commonUtility, downloadUrl));
            variables.put("DASHBOARD_URL", summary.getDashboardUrl());
            return EscalationEmailTemplateHelper.render(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate daily Procurement email HTML", e);
            return "<html><body><p>Daily Procurement escalation email could not be generated.</p></body></html>";
        }
    }

    private String renderVendorRows(List<VendorStateCountRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return "<tr><td colspan=\"4\" class=\"muted center\">No breaches</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        for (VendorStateCountRow row : rows) {
            html.append("<tr>");
            html.append("<td>").append(commonUtility.escapeHtml(row.getStateName())).append("</td>");
            html.append("<td>").append(commonUtility.escapeHtml(row.getVendorName())).append("</td>");
            html.append("<td>").append(commonUtility.escapeHtml(row.getCurrentStatus())).append("</td>");
            html.append("<td class=\"right\"><span class=\"badge\">").append(row.getCount()).append("</span></td>");
            html.append("</tr>");
        }
        return html.toString();
    }
}
