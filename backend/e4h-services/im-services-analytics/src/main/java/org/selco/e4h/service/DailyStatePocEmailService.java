package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.ActorCountRow;
import org.selco.e4h.web.models.DailyStatePocSummary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
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
            variables.put("NEW_CRM_ROWS", renderRows(summary.getNewCrmBreaches(), false));
            variables.put("NEW_TECH_POC_ROWS", renderRows(summary.getNewTechPocBreaches(), true));
            variables.put("PREVIOUS_CRM_ROWS", renderRows(summary.getPreviouslyOpenCrmBreaches(), true));
            variables.put("PREVIOUS_TECH_POC_ROWS", renderRows(summary.getPreviouslyOpenTechPocBreaches(), true));
            variables.put("DOWNLOAD_BUTTON", renderDownloadButton(downloadUrl));
            variables.put("DASHBOARD_URL", summary.getDashboardUrl());
            variables.put("SELCO_LOGO", commonUtility.loadLogoAsBase64("selcofoundation.png"));
            variables.put("SAURA_LOGO", commonUtility.loadLogoAsBase64("SauraEmitra.png"));
            return replaceTemplateVariables(template, variables);
        } catch (Exception e) {
            log.error("Failed to generate daily State POC email HTML", e);
            return "<html><body><p>Daily State POC escalation email could not be generated.</p></body></html>";
        }
    }

    private String renderRows(List<ActorCountRow> rows, boolean includeStatus) {
        return EscalationEmailTemplateHelper.renderActorRows(commonUtility, rows, includeStatus);
    }

    private String renderDownloadButton(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isBlank() || "#".equals(downloadUrl)) {
            return "";
        }
        return "<p class=\"center\"><a class=\"btn\" href=\"" + downloadUrl + "\" target=\"_blank\" rel=\"noopener\">Download Ticket Details</a></p>";
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
