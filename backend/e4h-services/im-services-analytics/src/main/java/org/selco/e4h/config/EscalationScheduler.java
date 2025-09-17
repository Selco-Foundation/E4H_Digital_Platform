package org.selco.e4h.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.selco.e4h.web.controller.EscalationController;
import org.selco.e4h.web.models.EscalationEmailRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Scheduler for SLA escalation processing
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EscalationScheduler {
    
    private final EscalationController escalationController;
    private final ConsumerConfiguration consumerConfiguration;
    
    /**
     * Scheduled method to process SLA escalations daily at end of workday
     * Default: Monday to Friday at 6:00 PM (18:00)
     * Based on LLD: Cronjob 1 calls /im-services-analytics/v1/escalation-emails/daily
     */
    @Scheduled(cron = "${escalation.daily.cron.expression:0 0 18 * * MON-FRI}")
    public void processDailyEscalationsScheduled() {
        try {
            log.info("Starting scheduled daily SLA escalation processing");
            
            // Create RequestInfo for the scheduled job
            RequestInfo requestInfo = createScheduledRequestInfo();
            
            // Create EscalationEmailRequest
            EscalationEmailRequest request = EscalationEmailRequest.builder()
                .requestInfo(requestInfo)
                .build();
            
            // Process daily escalations using the correct method name
            escalationController.sendDailyEscalationEmail(request);
            
            log.info("Completed scheduled daily SLA escalation processing");
            
        } catch (Exception e) {
            log.error("Error during scheduled daily SLA escalation processing", e);
        }
    }
    
    /**
     * Scheduled method to process SLA escalations weekly on Monday morning
     * Default: Monday at 9:00 AM (09:00)
     * Based on LLD: Cronjob 2 calls /im-services-analytics/v1/escalation-emails/weekly
     */
    @Scheduled(cron = "${escalation.weekly.cron.expression:0 0 9 * * MON}")
    public void processWeeklyEscalationsScheduled() {
        try {
            log.info("Starting scheduled weekly SLA escalation processing");
            
            // Create RequestInfo for the scheduled job
            RequestInfo requestInfo = createScheduledRequestInfo();
            
            // Create EscalationEmailRequest
            EscalationEmailRequest request = EscalationEmailRequest.builder()
                .requestInfo(requestInfo)
                .build();
            
            // Process weekly escalations using the correct method name
            escalationController.sendWeeklyEscalationEmail(request);
            
            log.info("Completed scheduled weekly SLA escalation processing");
            
        } catch (Exception e) {
            log.error("Error during scheduled weekly SLA escalation processing", e);
        }
    }
    
    /**
     * Create RequestInfo for scheduled job execution
     */
    private RequestInfo createScheduledRequestInfo() {
        // Create system user for scheduled job
        User systemUser = new User();
        systemUser.setUuid(UUID.randomUUID().toString());
        systemUser.setUserName("system");
        systemUser.setName("System User");
        systemUser.setMobileNumber("0000000000");
        systemUser.setEmailId("system@e4h.com");
        systemUser.setTenantId("in");
        
        // Create RequestInfo
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setApiId("im-services-analytics");
        requestInfo.setVer("1.0");
        requestInfo.setTs(System.currentTimeMillis());
        requestInfo.setAction("_create");
        requestInfo.setDid("scheduler");
        requestInfo.setKey("scheduler-key");
        requestInfo.setMsgId(UUID.randomUUID().toString());
        requestInfo.setAuthToken("scheduler-token");
        requestInfo.setUserInfo(systemUser);

        return requestInfo;
    }
}
