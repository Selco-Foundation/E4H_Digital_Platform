package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.DailyStatePocSummary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyStatePocEmailService {

    private static final String TEMPLATE_PATH = "templates/daily_state_poc_email.html";

    private final CommonUtility commonUtility;

    public String generateEmailSubject(DailyStatePocSummary summary) {
        return String.format("[Action Required] SLA Breach Alert - %s | %s",
                summary.getStateName(), summary.getAsOfDate());
    }

    public String generateEmailHtml(DailyStatePocSummary summary, String downloadUrl) {
        try {
            String template = loadTemplate();
            Map<String, String> variables = new HashMap<>();
            variables.put("NAME", commonUtility.escapeHtml(summary.getRecipientName()));
            variables.put("STATE_NAME", commonUtility.escapeHtml(summary.getStateName()));
            variables.put("AS_OF_DATE", commonUtility.escapeHtml(summary.getAsOfDate()));
            variables.put("NEW_CRM_SECTION", EscalationEmailTemplateHelper.renderActorTable(commonUtility,
                    summary.getNewCrmBreaches(), false, "t-red", "CRM Name", "Count of Tickets Breached"));
            variables.put("NEW_TECH_POC_SECTION", EscalationEmailTemplateHelper.renderActorTable(commonUtility,
                    summary.getNewTechPocBreaches(), true, "t-magenta", "Tech PoC Name", "Current Status", "Count of Tickets Breached"));
            variables.put("PREVIOUS_CRM_SECTION", EscalationEmailTemplateHelper.renderActorTable(commonUtility,
                    summary.getPreviouslyOpenCrmBreaches(), true, "t-orange", "CRM", "Current Status", "Count of Tickets"));
            variables.put("PREVIOUS_TECH_POC_SECTION", EscalationEmailTemplateHelper.renderActorTable(commonUtility,
                    summary.getPreviouslyOpenTechPocBreaches(), true, "t-orange", "Tech PoC", "Current Status", "Count of Tickets"));
            variables.put("DOWNLOAD_BUTTON", EscalationEmailTemplateHelper.renderDownloadButton(downloadUrl));
            variables.put("DASHBOARD_URL", summary.getDashboardUrl());
            variables.put("SELCO_LOGO", commonUtility.getSelcoLogoUrl());
            variables.put("SAURA_LOGO", commonUtility.getSauraLogoUrl());
            return replaceTemplateVariables(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate daily State POC email HTML", e);
            return "<html><body><p>Daily State POC escalation email could not be generated.</p></body></html>";
        }
    }

    private String loadTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private String replaceTemplateVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
