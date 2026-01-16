package org.egov.web.notification.sms.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.egov.tracer.kafka.*;
import org.egov.web.notification.sms.consumer.contract.SMSRequest;
import org.egov.web.notification.sms.models.Category;
import org.egov.web.notification.sms.models.RequestContext;
import org.egov.web.notification.sms.service.SMSService;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.kafka.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.*;
import org.springframework.kafka.config.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.*;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Service
public class SmsNotificationListener {

    private final ApplicationContext context;
    private SMSService smsService;
    private CustomKafkaTemplate<String, SMSRequest> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.topics.expiry.sms}")
    String expiredSmsTopic;

    @Value("${kafka.topics.backup.sms}")
    String backupSmsTopic;

    @Value("${kafka.topics.error.sms}")
    String errorSmsTopic;

    @Value("${sms.enabled}")
    Boolean smsEnable;


    @Autowired
    public SmsNotificationListener(
            ApplicationContext context,
            SMSService smsService,
                                   CustomKafkaTemplate<String, SMSRequest> kafkaTemplate) {
        this.smsService = smsService;
        this.context = context;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "${kafka.topics.notification.sms.name}"
    )
    public void process(HashMap<String, Object> consumerRecord) {
        log.trace("process method invoked - processing SMS notification from Kafka");
        String requestId = UUID.randomUUID().toString();
        RequestContext.setId(requestId);
        log.info("Processing SMS notification request with ID: {}", requestId);
        
        SMSRequest request = null;
        try {
            if(!smsEnable){
                log.warn("SMS service is disabled - to enable set sms.enabled flag to true");
                return;
            }
            
            log.debug("Converting consumer record to SMSRequest");
            request = objectMapper.convertValue(consumerRecord, SMSRequest.class);
            log.debug("SMSRequest converted successfully, category: {}", request.getCategory());
            
            if (request.getExpiryTime() != null && request.getCategory() == Category.OTP) {
                Long expiryTime = request.getExpiryTime();
                Long currentTime = System.currentTimeMillis();
                log.debug("OTP request detected - expiry time: {}, current time: {}", expiryTime, currentTime);
                
                if (expiryTime < currentTime) {
                    log.warn("OTP expired - expiry time {} is before current time {}", expiryTime, currentTime);
                    if (!StringUtils.isEmpty(expiredSmsTopic)) {
                        log.info("Sending expired OTP request to expired topic: {}", expiredSmsTopic);
                        kafkaTemplate.send(expiredSmsTopic, request);
                    }
                } else {
                    log.info("OTP request is valid, proceeding to send SMS");
                    smsService.sendSMS(request.toDomain());
                }
            } else {
                log.info("Processing non-OTP SMS request");
                smsService.sendSMS(request.toDomain());
            }
            log.info("SMS notification request processed successfully");

        } catch (RestClientException rx) {
            log.warn("RestClientException occurred while processing SMS - attempting backup service", rx);
            if (!StringUtils.isEmpty(backupSmsTopic)) {
                log.info("Sending request to backup SMS topic: {}", backupSmsTopic);
                kafkaTemplate.send(backupSmsTopic, request);
            } else if (!StringUtils.isEmpty(errorSmsTopic)) {
                log.info("Backup topic not configured, sending to error topic: {}", errorSmsTopic);
                kafkaTemplate.send(errorSmsTopic, request);
            } else {
                log.error("No backup or error topic configured, rethrowing exception");
                throw rx;
            }
        } catch (Exception ex) {
            log.error("Exception occurred while processing SMS notification", ex);
            if (!StringUtils.isEmpty(errorSmsTopic)) {
                log.info("Sending failed request to error topic: {}", errorSmsTopic);
                kafkaTemplate.send(errorSmsTopic, request);
            } else {
                log.error("No error topic configured, rethrowing exception");
                throw ex;
            }
        }
    }
}
