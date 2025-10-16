package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.web.models.WeeklyReportData;
import org.selco.e4h.util.CommonUtility;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service for generating weekly report emails
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportEmailService {
    
    private final CommonUtility commonUtility;
    
    private static final String WEEKLY_TEMPLATE_PATH = "templates/weekly_report_email.html";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");
    
    
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }
    
    /**
     * Generate weekly report email HTML
     */
    public String generateWeeklyReportEmailHTML(WeeklyReportData reportData, String recipientName, 
                                              String tenantId, RequestInfo requestInfo, String downloadUrl) {
        try {
            log.info("Generating weekly report email for tenant: {}, recipient: {}", tenantId, recipientName);
            
            // Load base template
            String template = loadTemplate();
            
            // Prepare template variables
            Map<String, String> templateVariables = prepareTemplateVariables(
                reportData, recipientName, tenantId, requestInfo, downloadUrl);
            
            // Replace template variables
            String html = replaceTemplateVariables(template, templateVariables);
            
            log.info("Successfully generated weekly report email HTML for tenant: {}", tenantId);
            return html;
            
        } catch (Exception e) {
            log.error("Error generating weekly report email HTML", e);
            return generateFallbackEmail(reportData, recipientName);
        }
    }
    
    /**
     * Generate weekly report email subject
     */
    public String generateWeeklyReportEmailSubject(String recipientName, WeeklyReportData reportData) {
        return String.format("Weekly Report - %s - %s", reportData.getDateRange(), DATE_FORMAT.format(new Date()));
    }
    
    /**
     * Load HTML template from classpath
     */
    private String loadTemplate() throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(WEEKLY_TEMPLATE_PATH);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load template from: {}", WEEKLY_TEMPLATE_PATH, e);
            throw e;
        }
    }
    
    
    /**
     * Prepare template variables
     */
    private Map<String, String> prepareTemplateVariables(WeeklyReportData reportData, String recipientName,
                                                        String tenantId, RequestInfo requestInfo, String downloadUrl) {
        Map<String, String> variables = new HashMap<>();
        
        // Basic variables
        variables.put("NAME", commonUtility.escapeHtml(recipientName));
        variables.put("DATE_RANGE", reportData.getDateRange());
        variables.put("WEEK_START_DATE", reportData.getWeekStartDate());
        variables.put("WEEK_END_DATE", reportData.getWeekEndDate());
        variables.put("TODAY_DDMMMYYYY", reportData.getTodayFormatted());
        
        // Functional metrics - using new model structure
        if (reportData.getWeekStartMetrics() != null) {
            variables.put("FUNC_START_COUNT", String.valueOf(reportData.getWeekStartMetrics().getFunctionalCount()));
            variables.put("NONFUNC_START_COUNT", String.valueOf(reportData.getWeekStartMetrics().getNonFunctionalCount()));
            int startTotal = reportData.getWeekStartMetrics().getFunctionalCount() + reportData.getWeekStartMetrics().getNonFunctionalCount();
            if (startTotal > 0) {
                variables.put("FUNC_START_PCT", String.format("%.1f", (reportData.getWeekStartMetrics().getFunctionalCount() * 100.0 / startTotal)));
                variables.put("NONFUNC_START_PCT", String.format("%.1f", (reportData.getWeekStartMetrics().getNonFunctionalCount() * 100.0 / startTotal)));
            } else {
                variables.put("FUNC_START_PCT", "0.0");
                variables.put("NONFUNC_START_PCT", "0.0");
            }
        } else {
            variables.put("FUNC_START_COUNT", "0");
            variables.put("FUNC_START_PCT", "0.0");
            variables.put("NONFUNC_START_COUNT", "0");
            variables.put("NONFUNC_START_PCT", "0.0");
        }
        
        if (reportData.getWeekEndMetrics() != null) {
            variables.put("FUNC_END_COUNT", String.valueOf(reportData.getWeekEndMetrics().getFunctionalCount()));
            variables.put("NONFUNC_END_COUNT", String.valueOf(reportData.getWeekEndMetrics().getNonFunctionalCount()));
            int endTotal = reportData.getWeekEndMetrics().getFunctionalCount() + reportData.getWeekEndMetrics().getNonFunctionalCount();
            if (endTotal > 0) {
                variables.put("FUNC_END_PCT", String.format("%.1f", (reportData.getWeekEndMetrics().getFunctionalCount() * 100.0 / endTotal)));
                variables.put("NONFUNC_END_PCT", String.format("%.1f", (reportData.getWeekEndMetrics().getNonFunctionalCount() * 100.0 / endTotal)));
            } else {
                variables.put("FUNC_END_PCT", "0.0");
                variables.put("NONFUNC_END_PCT", "0.0");
            }
        } else {
            variables.put("FUNC_END_COUNT", "0");
            variables.put("FUNC_END_PCT", "0.0");
            variables.put("NONFUNC_END_COUNT", "0");
            variables.put("NONFUNC_END_PCT", "0.0");
        }
        
        // Arrow indicators
        if (reportData.getFunctionalArrow() != null) {
            variables.put("FUNC_ARROW", reportData.getFunctionalArrow().getArrow());
            variables.put("FUNC_ARROW_CLASS", reportData.getFunctionalArrow().getArrowClass());
        } else {
            variables.put("FUNC_ARROW", "");
            variables.put("FUNC_ARROW_CLASS", "");
        }
        
        if (reportData.getNonFunctionalArrow() != null) {
            variables.put("NONFUNC_ARROW", reportData.getNonFunctionalArrow().getArrow());
            variables.put("NONFUNC_ARROW_CLASS", reportData.getNonFunctionalArrow().getArrowClass());
        } else {
            variables.put("NONFUNC_ARROW", "");
            variables.put("NONFUNC_ARROW_CLASS", "");
        }
        
        // Age bucket totals
        if (reportData.getTotalAgeBuckets() != null) {
            variables.put("TOTAL_LT1WK", String.valueOf(reportData.getTotalAgeBuckets().getTotalLt1Wk()));
            variables.put("TOTAL_LT1MO", String.valueOf(reportData.getTotalAgeBuckets().getTotalLt1Mo()));
            variables.put("TOTAL_LT3MO", String.valueOf(reportData.getTotalAgeBuckets().getTotalLt3Mo()));
        } else {
            variables.put("TOTAL_LT1WK", "0");
            variables.put("TOTAL_LT1MO", "0");
            variables.put("TOTAL_LT3MO", "0");
        }
        
        // State-wise data (limit to 3 states for template)
        Map<String, WeeklyReportData.StateAgeBucketData> stateData = reportData.getStateData();
        List<String> stateKeys = new ArrayList<>(stateData.keySet());
        
        // State 1
        if (stateKeys.size() > 0) {
            String state1Key = stateKeys.get(0);
            WeeklyReportData.StateAgeBucketData state1 = stateData.get(state1Key);
            variables.put("STATE_1", state1.getStateName());
            variables.put("S1_LT1WK", String.valueOf(state1.getLt1Wk()));
            variables.put("S1_LT1MO", String.valueOf(state1.getLt1Mo()));
            variables.put("S1_LT3MO", String.valueOf(state1.getLt3Mo()));
        } else {
            variables.put("STATE_1", "");
            variables.put("S1_LT1WK", "0");
            variables.put("S1_LT1MO", "0");
            variables.put("S1_LT3MO", "0");
        }
        
        // State 2
        if (stateKeys.size() > 1) {
            String state2Key = stateKeys.get(1);
            WeeklyReportData.StateAgeBucketData state2 = stateData.get(state2Key);
            variables.put("STATE_2", state2.getStateName());
            variables.put("S2_LT1WK", String.valueOf(state2.getLt1Wk()));
            variables.put("S2_LT1MO", String.valueOf(state2.getLt1Mo()));
            variables.put("S2_LT3MO", String.valueOf(state2.getLt3Mo()));
        } else {
            variables.put("STATE_2", "");
            variables.put("S2_LT1WK", "0");
            variables.put("S2_LT1MO", "0");
            variables.put("S2_LT3MO", "0");
        }
        
        // State 3
        if (stateKeys.size() > 2) {
            String state3Key = stateKeys.get(2);
            WeeklyReportData.StateAgeBucketData state3 = stateData.get(state3Key);
            variables.put("STATE_3", state3.getStateName());
            variables.put("S3_LT1WK", String.valueOf(state3.getLt1Wk()));
            variables.put("S3_LT1MO", String.valueOf(state3.getLt1Mo()));
            variables.put("S3_LT3MO", String.valueOf(state3.getLt3Mo()));
        } else {
            variables.put("STATE_3", "");
            variables.put("S3_LT1WK", "0");
            variables.put("S3_LT1MO", "0");
            variables.put("S3_LT3MO", "0");
        }
        
        // State list
        variables.put("STATE_LIST", reportData.getStateList());
        
        // Load and embed logos as base64 data URIs
        variables.put("SELCO_LOGO", commonUtility.loadLogoAsBase64("selcofoundation.png"));
        variables.put("SAURA_LOGO", commonUtility.loadLogoAsBase64("SauraEmitra.png"));
        
        // URLs
        variables.put("DOWNLOAD_URL", downloadUrl != null ? downloadUrl : "#");
        variables.put("DASHBOARD_URL", commonUtility.generateStateDashboardUrl(tenantId));
        
        return variables;
    }
    
    
    /**
     * Replace template variables with actual values
     */
    private String replaceTemplateVariables(String template, Map<String, String> variables) {
        String result = template;
        
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue());
        }
        
        return result;
    }
    
    
    /**
     * Generate fallback email if template loading fails
     */
    private String generateFallbackEmail(WeeklyReportData reportData, String recipientName) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html><html><head><title>Weekly Report</title></head><body>");
        html.append("<h1>Weekly DRE System Report</h1>");
        html.append("<p>Dear ").append(commonUtility.escapeHtml(recipientName)).append(",</p>");
        html.append("<p>Please find below the weekly report for the week of <strong>").append(reportData.getDateRange()).append("</strong>.</p>");
        
        html.append("<h2>Functional vs Non-Functional Status</h2>");
        html.append("<p>Week Start (").append(reportData.getWeekStartDate()).append("):</p>");
        html.append("<ul>");
        if (reportData.getWeekStartMetrics() != null) {
            int startTotal = reportData.getWeekStartMetrics().getFunctionalCount() + reportData.getWeekStartMetrics().getNonFunctionalCount();
            double funcStartPct = startTotal > 0 ? (reportData.getWeekStartMetrics().getFunctionalCount() * 100.0 / startTotal) : 0;
            double nonFuncStartPct = startTotal > 0 ? (reportData.getWeekStartMetrics().getNonFunctionalCount() * 100.0 / startTotal) : 0;
            html.append("<li>Functional: ").append(reportData.getWeekStartMetrics().getFunctionalCount()).append(" (").append(String.format("%.1f", funcStartPct)).append("%)</li>");
            html.append("<li>Non-Functional: ").append(reportData.getWeekStartMetrics().getNonFunctionalCount()).append(" (").append(String.format("%.1f", nonFuncStartPct)).append("%)</li>");
        } else {
            html.append("<li>Functional: 0 (0.0%)</li>");
            html.append("<li>Non-Functional: 0 (0.0%)</li>");
        }
        html.append("</ul>");
        
        html.append("<p>Week End (").append(reportData.getWeekEndDate()).append("):</p>");
        html.append("<ul>");
        if (reportData.getWeekEndMetrics() != null) {
            int endTotal = reportData.getWeekEndMetrics().getFunctionalCount() + reportData.getWeekEndMetrics().getNonFunctionalCount();
            double funcEndPct = endTotal > 0 ? (reportData.getWeekEndMetrics().getFunctionalCount() * 100.0 / endTotal) : 0;
            double nonFuncEndPct = endTotal > 0 ? (reportData.getWeekEndMetrics().getNonFunctionalCount() * 100.0 / endTotal) : 0;
            String funcArrow = reportData.getFunctionalArrow() != null ? reportData.getFunctionalArrow().getArrow() : "";
            String nonFuncArrow = reportData.getNonFunctionalArrow() != null ? reportData.getNonFunctionalArrow().getArrow() : "";
            html.append("<li>Functional: ").append(reportData.getWeekEndMetrics().getFunctionalCount()).append(" (").append(String.format("%.1f", funcEndPct)).append("%) ").append(funcArrow).append("</li>");
            html.append("<li>Non-Functional: ").append(reportData.getWeekEndMetrics().getNonFunctionalCount()).append(" (").append(String.format("%.1f", nonFuncEndPct)).append("%) ").append(nonFuncArrow).append("</li>");
        } else {
            html.append("<li>Functional: 0 (0.0%)</li>");
            html.append("<li>Non-Functional: 0 (0.0%)</li>");
        }
        html.append("</ul>");
        
        html.append("<h2>Age Bucket Summary</h2>");
        html.append("<ul>");
        if (reportData.getTotalAgeBuckets() != null) {
            html.append("<li>&lt; 1 Week: ").append(reportData.getTotalAgeBuckets().getTotalLt1Wk()).append("</li>");
            html.append("<li>&lt; 1 Month: ").append(reportData.getTotalAgeBuckets().getTotalLt1Mo()).append("</li>");
            html.append("<li>&lt; 3 Month: ").append(reportData.getTotalAgeBuckets().getTotalLt3Mo()).append("</li>");
        } else {
            html.append("<li>&lt; 1 Week: 0</li>");
            html.append("<li>&lt; 1 Month: 0</li>");
            html.append("<li>&lt; 3 Month: 0</li>");
        }
        html.append("</ul>");
        
        html.append("<p>This is an automated weekly summary from Saura eMitra.</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
}
