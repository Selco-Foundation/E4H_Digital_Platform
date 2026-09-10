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

    /** Email tables show only the top rows by count — the rest is one click away in the download. */
    public static final int MAX_DISPLAY_ROWS = 3;

    public static String renderActorRows(CommonUtility commonUtility, java.util.List<org.selco.e4h.web.models.ActorCountRow> rows,
                                         boolean includeStatus) {
        int colspan = includeStatus ? 3 : 2;
        if (rows == null || rows.isEmpty()) {
            return "<tr><td colspan=\"" + colspan + "\" class=\"muted center\">No breaches</td></tr>";
        }

        java.util.List<org.selco.e4h.web.models.ActorCountRow> sorted = new java.util.ArrayList<>(rows);
        sorted.sort(java.util.Comparator.comparingLong(org.selco.e4h.web.models.ActorCountRow::getCount).reversed());
        java.util.List<org.selco.e4h.web.models.ActorCountRow> display = sorted.size() > MAX_DISPLAY_ROWS
                ? sorted.subList(0, MAX_DISPLAY_ROWS) : sorted;

        StringBuilder html = new StringBuilder();
        for (org.selco.e4h.web.models.ActorCountRow row : display) {
            html.append("<tr>");
            html.append("<td>").append(commonUtility.escapeHtml(row.getActorName())).append("</td>");
            if (includeStatus) {
                html.append("<td>").append(commonUtility.escapeHtml(row.getCurrentStatus())).append("</td>");
            }
            html.append("<td class=\"right\"><span class=\"badge\">").append(row.getCount()).append("</span></td>");
            html.append("</tr>");
        }
        appendMoreRow(html, colspan, sorted.size());
        return html.toString();
    }

    /** Appends a "+N more" note row when the full list exceeds MAX_DISPLAY_ROWS. */
    public static void appendMoreRow(StringBuilder html, int colspan, int totalCount) {
        if (totalCount > MAX_DISPLAY_ROWS) {
            html.append("<tr><td colspan=\"").append(colspan).append("\" class=\"muted\">+ ")
                    .append(totalCount - MAX_DISPLAY_ROWS)
                    .append(" more — see the downloaded ticket details for the full list</td></tr>");
        }
    }
}
