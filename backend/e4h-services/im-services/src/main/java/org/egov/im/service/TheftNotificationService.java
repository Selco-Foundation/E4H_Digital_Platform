package org.egov.im.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.IMRepository;
import org.egov.im.util.MDMSUtils;
import org.egov.im.util.NotificationUtil;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.Notification.SMSRequest;
import org.egov.im.web.models.RequestSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

import static org.egov.im.util.IMConstants.PENDINGFORASSIGNMENT_THEFT;

/**
 * Scans for theft tickets (PENDINGFORASSIGNMENT_THEFT) past the configured threshold
 * and sends SMS to CRM: "Theft ticket [Ticket No.] requires action".
 * Threshold is read from MDMS common-masters TheftNotificationThreshold (thresholdMs).
 */
@Service
@Slf4j
public class TheftNotificationService {

    private static final String THEFT_NOTIFICATION_MESSAGE = "Theft ticket %s requires action";
    private static final long DEFAULT_THRESHOLD_MS = 29L * 24 * 60 * 60 * 1000; // 29 days

    private final IMConfiguration config;
    private final IMRepository repository;
    private final MDMSUtils mdmsUtils;
    private final NotificationUtil notificationUtil;

    @Autowired
    public TheftNotificationService(IMConfiguration config, IMRepository repository,
                                    MDMSUtils mdmsUtils, NotificationUtil notificationUtil) {
        this.config = config;
        this.repository = repository;
        this.mdmsUtils = mdmsUtils;
        this.notificationUtil = notificationUtil;
    }

    /**
     * Entry point for cron or REST: scan theft tickets past threshold and send SMS to CRM.
     *
     * @param tenantId optional; if null uses config im.theft.notification.tenantid
     * @return number of SMS notifications sent
     */
    public int runTheftNotification(String tenantId) {
        String effectiveTenantId = StringUtils.hasText(tenantId) ? tenantId : config.getTheftNotificationTenantId();
        log.info("Running theft notification for tenantId={}", effectiveTenantId);

        String crmMobile = config.getTheftNotificationCrmMobile();
        if (!StringUtils.hasText(crmMobile)) {
            log.warn("Theft notification skipped: im.theft.notification.crm.mobile is not set");
            return 0;
        }

        RequestInfo requestInfo = getSystemRequestInfo(effectiveTenantId);
        long thresholdMs = fetchThresholdFromMdms(requestInfo, effectiveTenantId);

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
        for (Incident incident : incidents) {
            String ticketNo = incident.getIncidentId() != null ? incident.getIncidentId() : incident.getId();
            String message = String.format(THEFT_NOTIFICATION_MESSAGE, ticketNo);
            List<SMSRequest> smsRequests = Collections.singletonList(
                    SMSRequest.builder().mobileNumber(crmMobile).message(message).build()
            );
            notificationUtil.sendSMS(incident.getTenantId(), smsRequests);
        }
        return incidents.size();
    }

    private RequestInfo getSystemRequestInfo(String tenantId) {
        User user = User.builder()
                .uuid(config.getEgovInternalMicroserviceUserUuid())
                .tenantId(tenantId.split("\\.")[0])
                .build();
        return RequestInfo.builder().userInfo(user).build();
    }

    private long fetchThresholdFromMdms(RequestInfo requestInfo, String tenantId) {
        try {
            Object mdmsData = mdmsUtils.fetchMDMSData(
                    requestInfo,
                    tenantId,
                    "common-masters",
                    Collections.singletonList("TheftNotificationThreshold"),
                    null
            );
            Number threshold = JsonPath.read(mdmsData, "$.MdmsRes['common-masters'].TheftNotificationThreshold[0].thresholdMs");
            if (threshold != null) {
                return threshold.longValue();
            }
        } catch (Exception e) {
            log.warn("Could not read TheftNotificationThreshold from MDMS, using default {} ms: {}", DEFAULT_THRESHOLD_MS, e.getMessage());
        }
        return DEFAULT_THRESHOLD_MS;
    }
}
