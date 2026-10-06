package org.egov.im.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.IMRepository;
import org.egov.im.util.MDMSUtils;
import org.egov.im.util.NotificationUtil;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.Notification.SMSRequest;
import org.egov.im.web.models.RequestSearchCriteria;
import org.egov.im.web.models.TheftNotificationRequest;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.egov.im.util.IMConstants.*;

/**
 * Scans for theft tickets (PENDINGFORASSIGNMENT_THEFT) past the configured threshold
 * and sends SMS to CRM: "Theft ticket [Ticket No.] requires action".
 * Threshold is read from MDMS common-masters TheftNotificationThreshold (thresholdMs).
 */
@Service
@Slf4j
public class TheftNotificationService {

    private static final String THEFT_SLA_REMINDER_TEMPLATE =
            "Theft ticket for %s with ID %s submitted on %s is nearing SLA. Only 3 days are left for resolution. "
                    + "Please take necessary action or track ticket details on %s - SELCO Foundation";
    private static final long DEFAULT_THRESHOLD_MS = 29L * 24 * 60 * 60 * 1000; // 29 days

    private final IMConfiguration config;
    private final IMRepository repository;
    private final MDMSUtils mdmsUtils;
    private final NotificationUtil notificationUtil;

    private final NotificationService notificationService;

    @Autowired
    public TheftNotificationService(IMConfiguration config, IMRepository repository,
                                    MDMSUtils mdmsUtils, NotificationUtil notificationUtil,
                                    NotificationService notificationService) {
        this.config = config;
        this.repository = repository;
        this.mdmsUtils = mdmsUtils;
        this.notificationUtil = notificationUtil;
        this.notificationService = notificationService;
    }

    /**
     * Entry point for cron or REST: scan theft tickets past threshold and send SMS to CRM.
     *
     * @param request ; if tenant id null uses config im.theft.notification.tenantid
     * @return number of SMS notifications sent
     */
    public int runTheftNotification(TheftNotificationRequest request) {
        String effectiveTenantId = StringUtils.hasText(request.getRequestInfo().getUserInfo().getTenantId()) ? request.getRequestInfo().getUserInfo().getTenantId()
                : config.getTheftNotificationTenantId();
        log.info("Running theft notification for tenantId={}", effectiveTenantId);

//        String crmMobile = config.getTheftNotificationCrmMobile();
//        if (!StringUtils.hasText(crmMobile)) {
//            log.warn("Theft notification skipped: im.theft.notification.crm.mobile is not set");
//            return 0;
//        }

        // 29L * 24 * 60 * 60 * 1000; // 29 days en milliseconds
        long thresholdMs = fetchThresholdFromMdms(request.getRequestInfo(), effectiveTenantId);

        RequestSearchCriteria criteria = RequestSearchCriteria.builder()
                .tenantId(effectiveTenantId)
                .applicationStatus(Collections.singleton(PENDINGFORASSIGNMENT_THEFT))
                .filedDateThresholdMs(thresholdMs)
                .limit(config.getMaxLimit())
                .offset(0)
                .build();

        List<Incident> incidents = repository.getIncidents(criteria);
        if (CollectionUtils.isEmpty(incidents)) {
            log.debug("No theft tickets past threshold for tenantId={}", effectiveTenantId);
            return 0;
        }

        log.info("Found {} theft ticket(s) past threshold, sending SMS to CRM", incidents.size());
        String localizationMessage = notificationUtil.getLocalizationMessages(
                effectiveTenantId, request.getRequestInfo(), IM_MODULE);
        String trackUrl = notificationUtil.getUrlByTenantId(localizationMessage);
        if (!StringUtils.hasText(trackUrl)) {
            trackUrl = config.getMobileDownloadLink();
        }

        int sent = 0;
        for (Incident incident : incidents) {
            IncidentRequest incidentRequest = IncidentRequest.builder()
                    .incident(incident)
                    .requestInfo(request.getRequestInfo())
                    .build();
            Map<String, String> crmDetails = notificationService.getHRMSEmployee(incidentRequest, ROLE_COMPLAINT_ASSESSOR);
            String crmMobile = crmDetails != null ? crmDetails.get("employeeMobile") : null;
            if (!StringUtils.hasText(crmMobile)) {
                log.warn("Skipping theft SLA SMS for incident {}: no CRM mobile", incident.getIncidentId());
                continue;
            }
            String message = buildTheftSlaReminderMessage(incident, trackUrl);
            List<SMSRequest> smsRequests = Collections.singletonList(
                    SMSRequest.builder().mobileNumber(crmMobile).message(message).build());
            notificationUtil.sendSMS(incident.getTenantId(), smsRequests);
            sent++;
        }

        return sent;
    }

    private String buildTheftSlaReminderMessage(Incident incident, String trackUrl) {
        String ticketType = incident.getIncidentType() != null ? incident.getIncidentType() : "";
        String ticketNo = incident.getIncidentId() != null ? incident.getIncidentId() : incident.getId();
        String formattedDate = "";
        if (incident.getAuditDetails() != null && incident.getAuditDetails().getCreatedTime() != null) {
            Long createdTime = incident.getAuditDetails().getCreatedTime();
            LocalDate date = Instant.ofEpochMilli(createdTime > 10 ? createdTime : createdTime * 1000)
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            formattedDate = date.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        }
        return String.format(THEFT_SLA_REMINDER_TEMPLATE, ticketType, ticketNo, formattedDate, trackUrl);
    }

    private long fetchThresholdFromMdms(RequestInfo requestInfo, String tenantId) {
        try {
            Object mdmsData = mdmsUtils.fetchMDMSData(
                    requestInfo,
                    tenantId,
                    "common-masters",
                    Collections.singletonList("incidentSubTypeThreshold"),
                    null
            );
            Number threshold = JsonPath.read(mdmsData, "$.MdmsRes['common-masters'].incidentSubTypeThreshold[0].thresholdMs");
            if (threshold != null) {
                return threshold.longValue();
            }
        } catch (Exception e) {
            log.warn("Could not read TheftNotificationThreshold from MDMS, using default {} ms: {}", DEFAULT_THRESHOLD_MS, e.getMessage());
        }
        return DEFAULT_THRESHOLD_MS;
    }

}
