package org.egov.im.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.service.MigrationService;
import org.egov.im.util.IMConstants;
import org.egov.im.web.models.imV1.ServiceResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@ConditionalOnProperty(
        value="migration.enabled",
        havingValue = "true",
        matchIfMissing = false)
@Slf4j
@Component
public class MigrationConsumer {


    @Autowired
    private MigrationService migrationService;

    @Autowired
    private ObjectMapper mapper;


    @KafkaListener(topics = { "${im.kafka.migration.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        try {
            log.info("Received migration request " + record);
            ServiceResponse serviceResponse = mapper.convertValue(record,ServiceResponse.class);
            
         // Adding in MDC so that tracer can add it in header
            MDC.put(IMConstants.TENANTID_MDC_STRING, serviceResponse.getServices().get(0).getTenantId());
            
            migrationService.migrate(serviceResponse);
        }
        catch (Exception e){
            log.error("Error occured while processing the record from topic : " + topic, e);
        }

    }

}
