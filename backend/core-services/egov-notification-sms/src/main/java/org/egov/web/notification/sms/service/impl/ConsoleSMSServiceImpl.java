package org.egov.web.notification.sms.service.impl;

import lombok.extern.slf4j.*;
import org.egov.web.notification.sms.models.Sms;
import org.egov.web.notification.sms.service.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(value = "sms.provider.class", matchIfMissing = true, havingValue = "Console")
public class ConsoleSMSServiceImpl extends BaseSMSService {

    @Override
    protected void submitToExternalSmsService(Sms sms) {
        log.trace("submitToExternalSmsService method invoked for Console SMS provider");
        log.info("Sending SMS to {} with message length: {}",
                sms.getMobileNumber() != null ? sms.getMobileNumber().substring(0, Math.min(3, sms.getMobileNumber().length())) + "****" : "null",
                sms.getMessage() != null ? sms.getMessage().length() : 0);
        log.debug("Console SMS service - message preview: {}", 
                sms.getMessage() != null && sms.getMessage().length() > 50 
                    ? sms.getMessage().substring(0, 50) + "..." 
                    : sms.getMessage());
    }
}
