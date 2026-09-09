package org.selco.e4h.util;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class EscalationEmailTemplateHelper {

    private EscalationEmailTemplateHelper() {
    }

    public static String loadTemplate(String classpathPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static String render(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    public static Map<String, String> baseBrandingVariables(CommonUtility commonUtility, String recipientName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("NAME", commonUtility.escapeHtml(recipientName));
        variables.put("SELCO_LOGO", commonUtility.loadLogoAsBase64("selcofoundation.png"));
        variables.put("SAURA_LOGO", commonUtility.loadLogoAsBase64("SauraEmitra.png"));
        return variables;
    }

    public static String renderDownloadButton(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isBlank() || "#".equals(downloadUrl)) {
            return "";
        }
        return "<p class=\"center\"><a class=\"btn\" href=\"" + downloadUrl
                + "\" target=\"_blank\" rel=\"noopener\">Download Ticket Details</a></p>";
    }

    public static String renderActorRows(CommonUtility commonUtility, java.util.List<org.selco.e4h.web.models.ActorCountRow> rows,
                                         boolean includeStatus) {
        if (rows == null || rows.isEmpty()) {
            return includeStatus
                    ? "<tr><td colspan=\"3\" class=\"muted center\">No breaches</td></tr>"
                    : "<tr><td colspan=\"2\" class=\"muted center\">No breaches</td></tr>";
        }
        StringBuilder html = new StringBuilder();
        for (org.selco.e4h.web.models.ActorCountRow row : rows) {
            html.append("<tr>");
            html.append("<td>").append(commonUtility.escapeHtml(row.getActorName())).append("</td>");
            if (includeStatus) {
                html.append("<td>").append(commonUtility.escapeHtml(row.getCurrentStatus())).append("</td>");
            }
            html.append("<td class=\"right\"><span class=\"badge\">").append(row.getCount()).append("</span></td>");
            html.append("</tr>");
        }
        return html.toString();
    }
}
