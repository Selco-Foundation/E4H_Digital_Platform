package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.StorageUtil;
import org.selco.e4h.web.models.ProcessingContext;
import org.selco.e4h.web.models.User;
import org.selco.e4h.web.models.UserAnalyticsReport;
import org.selco.e4h.web.models.storage.StorageResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
 * The workbook is uploaded to filestore and then referenced from the
 * {@code egov.core.notification.email} payload, which is how egov-notification-mail attaches files —
 * it downloads them itself and has no base64 path. Unlike the escalation mails, which merely link
 * their CSVs from a download button, this one arrives with the sheet attached.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAnalyticsMailService {

    private final UserService userService;
    private final CommonUtility commonUtility;
    private final StorageUtil storageUtil;
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

            // egov-notification-mail fetches the attachment from filestore itself, so the workbook has
            // to be stored before the mail is raised. Without an id there is nothing to attach and the
            // mail's only purpose is the sheet, so bail rather than send an empty-handed one.
            String fileStoreId = uploadWorkbook(requestInfo, workbook, fileName);
            if (fileStoreId == null) {
                log.error("User analytics: workbook upload to filestore failed, report not mailed to {} recipients",
                        recipients.size());
                return;
            }

            String subject = buildSubject(report);

            int sent = 0;
            for (User recipient : recipients) {
                try {
                    String body = buildBody(report, recipient.getName());
                    publish(recipient.getEmailId(), subject, body, fileStoreId, fileName);
                    sent++;
                } catch (Exception e) {
                    log.error("User analytics: failed to publish report mail for {}", recipient.getEmailId(), e);
                }
            }

            log.info("User analytics: published report mail to {}/{} recipients, attachment {} ({} bytes, fileStoreId {})",
                    sent, recipients.size(), fileName, workbook.length, fileStoreId);

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

    /**
     * Store the workbook under the same tenant the mail is raised for — egov-notification-mail
     * downloads it with {@code ?tenantId=<email.tenantId>&fileStoreId=<key>}, so the two must agree
     * or the attachment 404s.
     *
     * @return the filestore id, or null if the upload failed
     */
    private String uploadWorkbook(RequestInfo requestInfo, byte[] workbook, String fileName) {
        try {
            ProcessingContext context = ProcessingContext.builder()
                    .tenantId(REPORT_MAIL_TENANT_ID)
                    .module("UserAnalytics")
                    .tag("user-analytics-report")
                    .requestInfo(commonUtility.convertRequestInfoToJson(requestInfo))
                    .build();

            StorageResponse response = storageUtil.uploadToFileStorage(
                    Arrays.asList(workbookMultipartFile(workbook, fileName)), context);

            if (response == null || response.getFiles() == null || response.getFiles().isEmpty()) {
                log.error("User analytics: filestore returned no file for {}", fileName);
                return null;
            }

            String fileStoreId = response.getFiles().get(0).getFileStoreId();
            log.info("User analytics: uploaded {} to filestore, fileStoreId {}", fileName, fileStoreId);
            return fileStoreId;

        } catch (Exception e) {
            log.error("User analytics: error uploading {} to filestore", fileName, e);
            return null;
        }
    }

    /** Minimal binary-safe {@link MultipartFile} over the in-memory workbook. */
    private MultipartFile workbookMultipartFile(byte[] workbook, String fileName) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return XLSX_CONTENT_TYPE;
            }

            @Override
            public boolean isEmpty() {
                return workbook.length == 0;
            }

            @Override
            public long getSize() {
                return workbook.length;
            }

            @Override
            public byte[] getBytes() {
                return workbook;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(workbook);
            }

            @Override
            public void transferTo(java.io.File dest) throws IOException {
                Files.write(dest.toPath(), workbook);
            }
        };
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
     * {@code EscalationController.sendEmailViaKafka} raises, plus the attachment.
     * <p>
     * Attachments go in {@code fileStoreId}, a map keyed by <em>filestore id</em> whose value is the
     * <em>name the attachment is presented under</em> — that ordering looks backwards but is what
     * egov-notification-mail's {@code ExternalEmailService} reads: it builds the download URL from
     * the key and calls {@code addAttachment(entry.getValue(), file)}. The service also rejects the
     * message outright if {@code tenantId} is null while this map is non-empty.
     */
    private void publish(String emailId, String subject, String body, String fileStoreId, String fileName) {
        Map<String, String> attachments = new HashMap<>();
        attachments.put(fileStoreId, fileName);

        Map<String, Object> email = new HashMap<>();
        email.put("emailTo", new HashSet<>(Arrays.asList(emailId)));
        email.put("subject", subject);
        email.put("body", body);
        email.put("isHTML", true);
        email.put("tenantId", REPORT_MAIL_TENANT_ID);
        email.put("fileStoreId", attachments);

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
