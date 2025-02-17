package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.service.handler.notification.NotificationContext;
import org.egov.im.util.NotificationUtil;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentWrapper;
import org.egov.im.web.models.Notification.SMSRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.egov.im.util.IMConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceV2 {

    private final NotificationUtil notificationUtil;
    private final WorkflowActionProcessor workflowActionProcessor;
    private  final WorkflowMessageProcessor workflowMessageProcessor;
    private final IMConfiguration config;

    public void process(IncidentRequest request, String topic) {
        try {
            log.info("Processing request for notification: {}", request);

            String tenantId = request.getIncident().getTenantId();
            String applicationStatus = request.getIncident().getApplicationStatus();

            if (config.getIsSMSEnabled() == null || Boolean.FALSE.equals(config.getIsSMSEnabled())) {
                log.error("Notification Disabled For State: {}", applicationStatus);
                throw new CustomException("SMS_DISABLED", "SMS is disabled");
            }

            //get notification context
            NotificationContext notificationContext = workflowActionProcessor.processWorkflow(request);

            //get localization
            String localizationMessage
                    = notificationUtil.getLocalizationMessages(tenantId, request.getRequestInfo(), IM_MODULE);
            log.info("localized message: {}", localizationMessage);

            //get final message
            Map<String, List<String>> finalMessage
                    = workflowMessageProcessor.process(request, localizationMessage, topic);

            if(finalMessage.isEmpty()) {
                log.error("FINAL MESSAGE: unable to construct final message");
                throw new CustomException("FINAL_MESSAGE_ERROR", "Unable to construct final message");
            }

            finalMessage.forEach((key, value) -> value.forEach(msg -> {
                if (Objects.equals(key, CITIZEN)) {
                    value.forEach(v -> {
                        final List<SMSRequest> smsRequests
                                = enrichSmsRequest(notificationContext.getCitizenMobileNumber(), msg);
                        notificationUtil.sendSMS(tenantId, smsRequests);
                    });
                } else if (Objects.equals(key, EMPLOYEE)) {
                    value.forEach(v -> {
                        final List<SMSRequest> smsRequests
                                = enrichSmsRequest(notificationContext.getEmployeeMobileNumber(), msg);
                        notificationUtil.sendSMS(tenantId, smsRequests);
                    });
                } else {
                    value.forEach(v -> {
                        final List<SMSRequest> smsRequests
                                = enrichSmsRequest(notificationContext.getCrmMobileNumber(), msg);
                        notificationUtil.sendSMS(tenantId, smsRequests);
                    });
                }
            }));
        } catch (Exception ex) {
            log.error("Error occurred while processing the record from topic: {}", topic, ex);
        }
    }

    private List<SMSRequest> enrichSmsRequest(String mobileNumber, String finalMessage) {
        List<SMSRequest> smsRequest = new ArrayList<>();
        SMSRequest req = SMSRequest.builder()
                .mobileNumber(mobileNumber)
                .message(finalMessage)
                .build();
        smsRequest.add(req);
        return smsRequest;
    }
}

