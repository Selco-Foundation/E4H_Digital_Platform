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
        return "<p style=\"font-size:14px;line-height:20px;margin:0;\"><a href=\"" + downloadUrl
                + "\" target=\"_blank\" rel=\"noopener\" style=\"color:#f08400\">Download Ticket Details</a></p>";
    }

    /** Email tables show only the top rows by count - the rest is one click away in the download. */
    public static final int MAX_DISPLAY_ROWS = 3;

    // Styles below are inlined on every generated element rather than left as CSS classes, because
    // most webmail/desktop clients strip a <style> block on forward (they only carry the <body>
    // over, not <head>), silently dropping every banner/table-header color - while inline style=""
    // attributes survive forwarding, replying, and copy/paste. See the 6 template .html files for
    // the matching fix on their static markup.
    private static final String CELL = "padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:left;";
    private static final String CELL_RIGHT = "padding:10px 8px;border-bottom:1px solid #e5e7eb;font-size:13px;text-align:right;";
    private static final String MUTED_CENTER = "color:#6b7280;font-size:13px;text-align:center;";
    private static final String MUTED = "color:#6b7280;font-size:13px;";

    public static String renderActorRows(CommonUtility commonUtility, java.util.List<org.selco.e4h.web.models.ActorCountRow> rows,
                                         boolean includeStatus, String badgeBorderColor) {
        int colspan = includeStatus ? 3 : 2;
        if (rows == null || rows.isEmpty()) {
            return "<tr><td colspan=\"" + colspan + "\" style=\"" + CELL + MUTED_CENTER + "\">No breaches</td></tr>";
        }

        java.util.List<org.selco.e4h.web.models.ActorCountRow> sorted = new java.util.ArrayList<>(rows);
        sorted.sort(java.util.Comparator.comparingLong(org.selco.e4h.web.models.ActorCountRow::getCount).reversed());
        java.util.List<org.selco.e4h.web.models.ActorCountRow> display = sorted.size() > MAX_DISPLAY_ROWS
                ? sorted.subList(0, MAX_DISPLAY_ROWS) : sorted;

        StringBuilder html = new StringBuilder();
        for (org.selco.e4h.web.models.ActorCountRow row : display) {
            html.append("<tr>");
            html.append("<td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getActorName())).append("</td>");
            if (includeStatus) {
                html.append("<td style=\"").append(CELL).append("\">").append(commonUtility.escapeHtml(row.getCurrentStatus())).append("</td>");
            }
            html.append("<td style=\"").append(CELL_RIGHT).append("\"><span style=\"display:inline-block;min-width:32px;padding:6px 10px;border-radius:10px;background:#fee4e2;border:1px solid ")
                    .append(badgeBorderColor).append(";font-weight:700;text-align:center;\">").append(row.getCount()).append("</span></td>");
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
                                          boolean includeStatus, String headerBgColor, String badgeBorderColor, String... headers) {
        if (rows == null || rows.isEmpty()) {
            return "<p style=\"" + MUTED + "\">No Breaches</p>";
        }
        String thBase = CELL + "color:#fff;font-weight:700;background:" + headerBgColor + ";";
        String thRight = CELL_RIGHT + "color:#fff;font-weight:700;background:" + headerBgColor + ";";
        StringBuilder html = new StringBuilder();
        html.append("<table style=\"width:100%;border-collapse:collapse;margin-top:8px;\"><thead><tr>");
        for (int i = 0; i < headers.length; i++) {
            boolean isLast = i == headers.length - 1;
            html.append("<th style=\"").append(isLast ? thRight : thBase).append("\">").append(headers[i]).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        html.append(renderActorRows(commonUtility, rows, includeStatus, badgeBorderColor));
        html.append("</tbody></table>");
        return html.toString();
    }

    /** Appends a "+N more" note row when the full list exceeds MAX_DISPLAY_ROWS. */
    public static void appendMoreRow(StringBuilder html, int colspan, int totalCount) {
        if (totalCount > MAX_DISPLAY_ROWS) {
            html.append("<tr><td colspan=\"").append(colspan).append("\" style=\"").append(CELL).append(MUTED).append("\">+ ")
                    .append(totalCount - MAX_DISPLAY_ROWS)
                    .append(" more - see the downloaded ticket details for the full list</td></tr>");
        }
    }
}
