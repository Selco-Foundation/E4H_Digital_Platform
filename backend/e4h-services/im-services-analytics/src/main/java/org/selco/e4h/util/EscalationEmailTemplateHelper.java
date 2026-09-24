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

    /**
     * Gmail and most webmail clients strip inline {@code data:} URI images from HTML email
     * bodies, so the logos are served from a real public filestore URL instead of embedded as
     * base64 - see {@link CommonUtility#getSelcoLogoUrl()} / {@link CommonUtility#getSauraLogoUrl()}.
     */
    public static Map<String, String> baseBrandingVariables(CommonUtility commonUtility, String recipientName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("NAME", commonUtility.escapeHtml(recipientName));
        variables.put("SELCO_LOGO", commonUtility.getSelcoLogoUrl());
        variables.put("SAURA_LOGO", commonUtility.getSauraLogoUrl());
        return variables;
    }

    /** Styled to match the "Visit Dashboard" link in the Resources section - plain text link, not a button. */
    public static String renderDownloadButton(String downloadUrl) {
        if (downloadUrl == null || downloadUrl.isBlank() || "#".equals(downloadUrl)) {
            return "";
        }
        return "<p class=\"text\"><a href=\"" + downloadUrl
                + "\" target=\"_blank\" rel=\"noopener\" style=\"color:#f08400\">Download Ticket Details</a></p>";
    }

    /** Email tables show only the top rows by count - the rest is one click away in the download. */
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

    /**
     * Renders a full actor-count table (colored header + rows), or - when there are no rows -
     * just a plain "No Breaches" message with no header at all. Showing an empty table with only
     * a colored header bar and nothing underneath reads as broken, so the header is suppressed
     * along with the rows rather than left dangling above an empty body.
     */
    public static String renderActorTable(CommonUtility commonUtility, java.util.List<org.selco.e4h.web.models.ActorCountRow> rows,
                                          boolean includeStatus, String tableClass, String... headers) {
        if (rows == null || rows.isEmpty()) {
            return "<p class=\"muted\">No Breaches</p>";
        }
        StringBuilder html = new StringBuilder();
        html.append("<table class=\"").append(tableClass).append("\"><thead><tr>");
        for (int i = 0; i < headers.length; i++) {
            boolean isLast = i == headers.length - 1;
            html.append(isLast ? "<th class=\"right\">" : "<th>").append(headers[i]).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        html.append(renderActorRows(commonUtility, rows, includeStatus));
        html.append("</tbody></table>");
        return html.toString();
    }

    /** Appends a "+N more" note row when the full list exceeds MAX_DISPLAY_ROWS. */
    public static void appendMoreRow(StringBuilder html, int colspan, int totalCount) {
        if (totalCount > MAX_DISPLAY_ROWS) {
            html.append("<tr><td colspan=\"").append(colspan).append("\" class=\"muted\">+ ")
                    .append(totalCount - MAX_DISPLAY_ROWS)
                    .append(" more - see the downloaded ticket details for the full list</td></tr>");
        }
    }
}
