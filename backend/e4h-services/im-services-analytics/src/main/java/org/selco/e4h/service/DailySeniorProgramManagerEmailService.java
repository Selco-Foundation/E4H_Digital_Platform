package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.DailySeniorProgramManagerSummary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailySeniorProgramManagerEmailService {

    private static final String TEMPLATE_PATH = "templates/daily_senior_program_manager_email.html";

    private final CommonUtility commonUtility;

    public String generateEmailSubject(DailySeniorProgramManagerSummary summary) {
        return String.format("[Action Required] SLA Breach Alert - %s | %s",
                summary.getStateName(), summary.getAsOfDate());
    }

    public String generateEmailHtml(DailySeniorProgramManagerSummary summary, String downloadUrl) {
        try {
            String template = EscalationEmailTemplateHelper.loadTemplate(TEMPLATE_PATH);
            Map<String, String> variables = EscalationEmailTemplateHelper.baseBrandingVariables(
                    commonUtility, summary.getRecipientName());
            variables.put("STATE_NAME", commonUtility.escapeHtml(summary.getStateName()));
            variables.put("AS_OF_DATE", commonUtility.escapeHtml(summary.getAsOfDate()));
            variables.put("NEW_STATE_POC_ROWS", EscalationEmailTemplateHelper.renderActorRows(
                    commonUtility, summary.getNewStatePocBreaches(), true));
            variables.put("NEW_VENDOR_ROWS", EscalationEmailTemplateHelper.renderActorRows(
                    commonUtility, summary.getNewVendorBreaches(), true));
            variables.put("PREVIOUS_STATE_POC_ROWS", EscalationEmailTemplateHelper.renderActorRows(
                    commonUtility, summary.getPreviouslyOpenStatePocBreaches(), true));
            variables.put("PREVIOUS_VENDOR_ROWS", EscalationEmailTemplateHelper.renderActorRows(
                    commonUtility, summary.getPreviouslyOpenVendorBreaches(), true));
            variables.put("DOWNLOAD_BUTTON", EscalationEmailTemplateHelper.renderDownloadButton(commonUtility, downloadUrl));
            variables.put("DASHBOARD_URL", summary.getDashboardUrl());
            return EscalationEmailTemplateHelper.render(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate daily SPM email HTML", e);
            return "<html><body><p>Daily SPM escalation email could not be generated.</p></body></html>";
        }
    }
}
