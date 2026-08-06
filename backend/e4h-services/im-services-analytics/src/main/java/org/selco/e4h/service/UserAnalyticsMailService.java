package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.web.models.Attachment;
import org.selco.e4h.web.models.User;
import org.selco.e4h.web.models.UserAnalyticsReport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.selco.e4h.util.UserAnalyticsConstants.REPORT_MAIL_TEMPLATE_PATH;
import static org.selco.e4h.util.UserAnalyticsConstants.REPORT_MAIL_TENANT_ID;
import static org.selco.e4h.util.UserAnalyticsConstants.REPORT_RECIPIENT_ROLE;
import static org.selco.e4h.util.UserAnalyticsConstants.XLSX_CONTENT_TYPE;

/**
 * Mails the generated user-analytics workbook to every holder of the
 * {@link org.selco.e4h.util.UserAnalyticsConstants#REPORT_RECIPIENT_ROLE} role in HRMS.
 * <p>
 * The workbook rides along as a base64 attachment on the {@code egov.core.notification.email}
 * payload rather than as a filestore download button, which is how the escalation mails link their
 * CSVs. Recipients of this report are expected to open the sheet, not follow a link.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAnalyticsMailService {

    private final UserService userService;
    private final CommonUtility commonUtility;
    private final ConsumerConfiguration consumerConfiguration;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    /**
     * Look up the recipients and publish one mail per recipient. Never throws — a report that was
     * successfully generated must still reach the caller even if HRMS or Kafka is unavailable, so
     * every failure here is logged and swallowed.
     *
     * @param workbook the xlsx bytes as returned by {@link UserAnalyticsExcelService#generate}
     * @param fileName the name the attachment is presented under, e.g.
     *                 {@code user-analytics-report-2026-07-27.xlsx}
     */
    public void sendReport(RequestInfo requestInfo, UserAnalyticsReport report, byte[] workbook, String fileName) {
        try {
            List<User> recipients = resolveRecipients(requestInfo);
            if (recipients.isEmpty()) {
                log.warn("User analytics: no {} user with an email address found, report not mailed",
                        REPORT_RECIPIENT_ROLE);
                return;
            }

            Attachment attachment = buildAttachment(workbook, fileName);
            String subject = buildSubject(report);

            int sent = 0;
            for (User recipient : recipients) {
                try {
                    String body = buildBody(report, recipient.getName());
                    publish(recipient.getEmailId(), subject, body, attachment);
                    sent++;
                } catch (Exception e) {
                    log.error("User analytics: failed to publish report mail for {}", recipient.getEmailId(), e);
                }
            }

            log.info("User analytics: published report mail to {}/{} recipients, attachment {} ({} bytes)",
                    sent, recipients.size(), fileName, workbook.length);

        } catch (Exception e) {
            log.error("User analytics: error mailing the weekly report", e);
        }
    }

    /**
     * Role holders that have an email address, one entry per address so a person posted to several
     * boundaries — HRMS returns an employee per posting — is not mailed the same sheet twice.
     */
    private List<User> resolveRecipients(RequestInfo requestInfo) {
        List<User> users = userService.searchUsersByRole(requestInfo, Arrays.asList(REPORT_RECIPIENT_ROLE));

        Map<String, User> byEmail = new LinkedHashMap<>();
        for (User user : users) {
            String email = user.getEmailId();
            if (email == null || email.trim().isEmpty()) {
                log.warn("User analytics: {} user {} has no email address, skipping",
                        REPORT_RECIPIENT_ROLE, user.getUserName());
                continue;
            }
            byEmail.putIfAbsent(email.trim(), user);
        }

        return new ArrayList<>(byEmail.values());
    }

    private Attachment buildAttachment(byte[] workbook, String fileName) {
        return Attachment.builder()
                .fileName(fileName)
                .contentType(XLSX_CONTENT_TYPE)
                .base64Content(Base64.getEncoder().encodeToString(workbook))
                .fileSize((long) workbook.length)
                .build();
    }

    private String buildSubject(UserAnalyticsReport report) {
        return String.format("Saura-eMitra User Analytics Report — %s to %s",
                format(report.getWeekStartDate()), format(report.getWeekEndDate()));
    }

    private String buildBody(UserAnalyticsReport report, String recipientName) {
        Map<String, String> variables = new HashMap<>();
        variables.put("NAME", commonUtility.escapeHtml(recipientName));
        variables.put("WEEK_START_DATE", format(report.getWeekStartDate()));
        variables.put("WEEK_END_DATE", format(report.getWeekEndDate()));
        variables.put("ACTIVE_USERS", activeUsers(report));
        variables.put("PARTIAL_WEEK_NOTE", report.isPartialWeek()
                ? "This week is still in progress, so the figures below cover it only up to the time the report was generated."
                : "");
        variables.put("DASHBOARD_URL", commonUtility.generateStateDashboardUrl());
        variables.put("SELCO_LOGO", commonUtility.loadLogoAsBase64("selcofoundation.png"));
        variables.put("SAURA_LOGO", commonUtility.loadLogoAsBase64("SauraEmitra.png"));

        try {
            String template = new String(
                    new ClassPathResource(REPORT_MAIL_TEMPLATE_PATH).getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
            return replaceTemplateVariables(template, variables);
        } catch (IOException e) {
            log.error("User analytics: failed to load mail template {}, falling back to plain body",
                    REPORT_MAIL_TEMPLATE_PATH, e);
            return fallbackBody(variables);
        }
    }

    private String activeUsers(UserAnalyticsReport report) {
        if (report.getOverall() == null || report.getOverall().getCurrent() == null) {
            return "0";
        }
        return String.valueOf(report.getOverall().getCurrent().getActiveUsersTotal());
    }

    private String replaceTemplateVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace("${" + entry.getKey() + "}", value);
        }
        return result;
    }

    private String fallbackBody(Map<String, String> variables) {
        return "<html><body>"
                + "<p>Dear " + variables.get("NAME") + ",</p>"
                + "<p>Please find attached the user analytics report for "
                + variables.get("WEEK_START_DATE") + " to " + variables.get("WEEK_END_DATE") + ".</p>"
                + "<p>Active users this week: <strong>" + variables.get("ACTIVE_USERS") + "</strong></p>"
                + "<p>This is an automated report from Saura eMitra.</p>"
                + "</body></html>";
    }

    /**
     * Publish on the shared egov-notification-mail contract, matching the payload
     * {@code EscalationController.sendEmailViaKafka} raises, plus an {@code attachments} list.
     */
    private void publish(String emailId, String subject, String body, Attachment attachment) {
        Map<String, Object> email = new HashMap<>();
        email.put("emailTo", new HashSet<>(Arrays.asList(emailId)));
        email.put("subject", subject);
        email.put("body", body);
        email.put("isHTML", true);
        email.put("tenantId", REPORT_MAIL_TENANT_ID);
        email.put("attachments", Arrays.asList(attachment));

        Map<String, Object> emailRequest = new HashMap<>();
        emailRequest.put("requestInfo", new HashMap<>());
        emailRequest.put("email", email);

        kafkaTemplate.send(consumerConfiguration.getNotificationEmailTopic(), emailRequest);
        log.info("User analytics: published report mail to {} on topic {}",
                emailId, consumerConfiguration.getNotificationEmailTopic());
    }

    private String format(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }
}
