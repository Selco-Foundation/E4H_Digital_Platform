package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.util.NotificationUtil;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.Notification.SMSRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.egov.im.util.IMConstants.*;

/**
 * Sends DLT-approved SMS on workflow transitions triggered by {@code /request/_update}.
 * Recipient roles: Tech POC = COMPLAINT_FACILITATOR_2, Vendor = COMPLAINT_RESOLVER,
 * State SPOC = COMPLAINT_FACILITATOR_1, CRM = COMPLAINT_ASSESSOR, Health Staff = COMPLAINANT.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowSmsNotificationService {

    private static final String TPL_OUT_OF_WARRANTY_QUOTATION_SUBMITTED =
            "Vendor marked ticket for {ticket_type} with ID {incidentId} submitted on {date} as Out of Warranty "
                    + "and submitted quotation for review. Please take necessary action or track ticket details on {url} - SELCO Foundation";

    private static final String TPL_OUT_OF_WARRANTY_STATE_SPOC_ACTION =
            "Out of Warranty ticket for {ticket_type} with ID {incidentId} submitted on {date} has been approved by Tech POC "
                    + "and is pending for your action. Please review or track ticket details on {url} - SELCO Foundation";

    private static final String TPL_OUT_OF_WARRANTY_REVISION_REQUIRED =
            "Quotation revision required for your ticket for {ticket_type} with ID {incidentId} submitted on {date}. "
                    + "Please review and resubmit quotation or track ticket details on {url} - SELCO Foundation";

    private static final String TPL_OUT_OF_WARRANTY_REVISED_QUOTATION_SUBMITTED =
            "Revised quotation submitted for ticket for {ticket_type} with ID {incidentId} submitted on {date}. "
                    + "Please review and take action or track ticket details on {url} - SELCO Foundation";

    private static final String TPL_OUT_OF_WARRANTY_QUOTATION_APPROVED_HEALTH_STAFF =
            "Your ticket for {ticket_type} with ID {incidentId} submitted on {date} is marked as Out of Warranty. "
                    + "Quotation submitted by vendor has been reviewed. Download quotation and track ticket details on {url} - SELCO Foundation";

    private static final String TPL_OUT_OF_WARRANTY_ASSIGNED_VENDOR =
            "Out of Warranty ticket for {ticket_type} with ID {incidentId} submitted on {date} has been assigned to you for resolution. "
                    + "Please take necessary action or track ticket details on {url} - SELCO Foundation";

    private static final String TPL_OUT_OF_SCOPE_STATE_SPOC =
            "Action required for ticket for {ticket_type} with ID {incidentId} submitted on {date}. "
                    + "Vendor marked this ticket as Out of Scope. Reason: {out_of_scope_reason}. "
                    + "Please review or track ticket details on {url} - SELCO Foundation";

    private static final String TPL_OUT_OF_SCOPE_HEALTH_STAFF =
            "Your ticket for {ticket_type} with ID {incidentId} submitted on {date} is marked as Out of Scope. "
                    + "Reason: {out_of_scope_reason}. Your support is required for resolution. "
                    + "You can track ticket details on {url} - SELCO Foundation";

    private static final String TPL_OUT_OF_SCOPE_RESOLVED_VENDOR =
            "Out of Scope issue for ticket for {ticket_type} with ID {incidentId} submitted on {date} has been resolved. "
                    + "Please take necessary action or track ticket details on {url} - SELCO Foundation";

    private static final String TPL_RMS_TECH_POC_ACTION =
            "RMS issue reported for ticket for {ticket_type} with ID {incidentId} submitted on {date}. "
                    + "Please take necessary action or track ticket details on {url} - SELCO Foundation";

    private final IMConfiguration config;
    private final NotificationUtil notificationUtil;
    private final NotificationService notificationService;

    private record SmsRule(String template, String recipientRole) {}

    private static final Map<String, List<SmsRule>> RULES_BY_TRANSITION = Map.ofEntries(
            Map.entry("OUT_OF_WARRANTY_" + OUT_OF_WARRANTY_PENDING_TECH_POC,
                    List.of(new SmsRule(TPL_OUT_OF_WARRANTY_QUOTATION_SUBMITTED, ROLE_COMPLAINT_FACILITATOR_2))),
            Map.entry("APPROVE_" + PENDING_ASSIGNMENT_OUT_OF_WARRANTY, List.of(
                    new SmsRule(TPL_OUT_OF_WARRANTY_STATE_SPOC_ACTION, ROLE_COMPLAINT_FACILITATOR_1),
                    new SmsRule(TPL_OUT_OF_WARRANTY_QUOTATION_APPROVED_HEALTH_STAFF, ROLE_COMPLAINANT))),
            Map.entry("REVISE_" + PENDING_REVISION,
                    List.of(new SmsRule(TPL_OUT_OF_WARRANTY_REVISION_REQUIRED, ROLE_COMPLAINT_RESOLVER))),
            Map.entry("SUBMIT_" + OUT_OF_WARRANTY_PENDING_TECH_POC_ROUND_2,
                    List.of(new SmsRule(TPL_OUT_OF_WARRANTY_REVISED_QUOTATION_SUBMITTED, ROLE_COMPLAINT_FACILITATOR_2))),
            Map.entry("ASSIGN_" + PENDING_RESOLUTION_PREFIX + "OUT_OF_WARRANTY",
                    List.of(new SmsRule(TPL_OUT_OF_WARRANTY_ASSIGNED_VENDOR, ROLE_COMPLAINT_RESOLVER))),
            Map.entry("MARK_OUT_OF_SCOPE_" + OUT_OF_SCOPE, List.of(
                    new SmsRule(TPL_OUT_OF_SCOPE_STATE_SPOC, ROLE_COMPLAINT_FACILITATOR_1),
                    new SmsRule(TPL_OUT_OF_SCOPE_HEALTH_STAFF, ROLE_COMPLAINANT))),
            Map.entry("ASSIGN_" + PENDING_RESOLUTION_OUT_OF_SCOPE,
                    List.of(new SmsRule(TPL_OUT_OF_SCOPE_RESOLVED_VENDOR, ROLE_COMPLAINT_RESOLVER))),
            Map.entry("ASSIGN_" + RMS_DEVICE_PENDING_TECH_POC,
                    List.of(new SmsRule(TPL_RMS_TECH_POC_ACTION, ROLE_COMPLAINT_FACILITATOR_2)))
    );

    /**
     * Sends workflow SMS when the action + new application status match a configured rule.
     */
    public void process(IncidentRequest request) {
        if (config.getIsSMSEnabled() == null || !config.getIsSMSEnabled()) {
            return;
        }
        if (request == null || request.getIncident() == null || request.getWorkflow() == null) {
            return;
        }
        String action = request.getWorkflow().getAction();
        String applicationStatus = request.getIncident().getApplicationStatus();
        if (!StringUtils.hasText(action) || !StringUtils.hasText(applicationStatus)) {
            return;
        }

        String transitionKey = action.trim().toUpperCase() + "_" + applicationStatus.trim().toUpperCase();
        List<SmsRule> rules = RULES_BY_TRANSITION.get(transitionKey);
        if (CollectionUtils.isEmpty(rules)) {
            log.debug("No workflow SMS rule for transition {}", transitionKey);
            return;
        }

        String tenantId = request.getIncident().getTenantId();
        String localizationMessage = notificationUtil.getLocalizationMessages(tenantId, request.getRequestInfo(), IM_MODULE);
        PlaceholderValues placeholders = buildPlaceholders(request, localizationMessage);

        for (SmsRule rule : rules) {
            try {
                sendForRole(request, tenantId, rule, placeholders);
            } catch (Exception ex) {
                log.error("Failed to send workflow SMS for transition {} to role {}", transitionKey, rule.recipientRole(), ex);
            }
        }
    }

    private void sendForRole(IncidentRequest request, String tenantId, SmsRule rule, PlaceholderValues placeholders) {
        String mobile = resolveMobileNumber(request, rule.recipientRole());
        if (!StringUtils.hasText(mobile)) {
            log.warn("Skipping workflow SMS: no mobile for role {} on incident {}", rule.recipientRole(),
                    request.getIncident().getIncidentId());
            return;
        }
        String message = applyPlaceholders(rule.template(), placeholders);
        List<SMSRequest> smsRequests = Collections.singletonList(
                SMSRequest.builder().mobileNumber(mobile).message(message).build());
        notificationUtil.sendSMS(tenantId, smsRequests);
        log.info("Workflow SMS sent for incident {} to role {}", request.getIncident().getIncidentId(), rule.recipientRole());
    }

    private String resolveMobileNumber(IncidentRequest request, String role) {
        if (ROLE_COMPLAINANT.equals(role)
                && request.getIncident().getReporter() != null
                && StringUtils.hasText(request.getIncident().getReporter().getMobileNumber())) {
            return request.getIncident().getReporter().getMobileNumber();
        }
        if (ROLE_COMPLAINT_RESOLVER.equals(role)
                && request.getWorkflow().getAssignes() != null
                && !request.getWorkflow().getAssignes().isEmpty()) {
            var user = notificationService.fetchUserByUUID(
                    request.getWorkflow().getAssignes().get(0),
                    request.getRequestInfo(),
                    request.getIncident().getTenantId());
            if (user != null && StringUtils.hasText(user.getMobileNumber())) {
                return user.getMobileNumber();
            }
        }
        Map<String, String> employee = notificationService.getHRMSEmployee(request, role);
        return employee != null ? employee.get("employeeMobile") : null;
    }

    private PlaceholderValues buildPlaceholders(IncidentRequest request, String localizationMessage) {
        Long createdTime = request.getIncident().getAuditDetails().getCreatedTime();
        LocalDate date = Instant.ofEpochMilli(createdTime > 10 ? createdTime : createdTime * 1000)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        String formattedDate = date.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        String url = notificationUtil.getUrlByTenantId(localizationMessage);
        if (!StringUtils.hasText(url)) {
            url = config.getMobileDownloadLink();
        }
        String outOfScopeReason = resolveOutOfScopeReason(request);
        return new PlaceholderValues(
                request.getIncident().getIncidentType(),
                request.getIncident().getIncidentId(),
                formattedDate,
                url,
                outOfScopeReason);
    }

    private String resolveOutOfScopeReason(IncidentRequest request) {
        if (request.getWorkflow().getSendBackReason() != null
                && StringUtils.hasText(request.getWorkflow().getSendBackReason().getReason())) {
            return request.getWorkflow().getSendBackReason().getReason();
        }
        if (StringUtils.hasText(request.getWorkflow().getComments())) {
            return request.getWorkflow().getComments();
        }
        if (StringUtils.hasText(request.getIncident().getComments())) {
            return request.getIncident().getComments();
        }
        return "Not specified";
    }

    private String applyPlaceholders(String template, PlaceholderValues values) {
        return template
                .replace("{ticket_type}", nullToEmpty(values.ticketType()))
                .replace("{incidentId}", nullToEmpty(values.incidentId()))
                .replace("{date}", nullToEmpty(values.date()))
                .replace("{url}", nullToEmpty(values.url()))
                .replace("{out_of_scope_reason}", nullToEmpty(values.outOfScopeReason()));
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private record PlaceholderValues(String ticketType, String incidentId, String date, String url, String outOfScopeReason) {}
}
