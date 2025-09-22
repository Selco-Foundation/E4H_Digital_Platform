package org.egov.field_planner.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.models.project.ProjectFacility;
import org.egov.common.models.project.ProjectFacilityBulkRequest;
import org.egov.field_planner.service.FieldPlannerFacilityService;
import org.egov.field_planner.web.models.FieldPlanFacility;
import org.egov.field_planner.web.models.FieldPlanFacilityBulkRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class FieldPlanFacilityConsumer {

    private final FieldPlannerFacilityService service;
    private final ObjectMapper objectMapper;

    public FieldPlanFacilityConsumer(FieldPlannerFacilityService service, @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${fieldPlan.facility.consumer.bulk.create.topic}")
    public List<FieldPlanFacility> bulkCreate(Map<String, Object> consumerRecord,
                                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            FieldPlanFacilityBulkRequest request = objectMapper.convertValue(consumerRecord, FieldPlanFacilityBulkRequest.class);
            return service.create(request, true);
        } catch (Exception exception) {
            log.error("error in fieldplan facility consumer bulk create", ExceptionUtils.getStackTrace(exception));
            return Collections.emptyList();
        }
    }

    @KafkaListener(topics = "${fieldPlan.facility.consumer.bulk.unassign.topic}")
    public List<FieldPlanFacility> bulkUnassign(Map<String, Object> consumerRecord,
                                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            FieldPlanFacilityBulkRequest request = objectMapper.convertValue(consumerRecord, FieldPlanFacilityBulkRequest.class);
            return service.unassignBulk(request, true);
        } catch (Exception exception) {
            log.error("error in fieldplan facility consumer bulk delete", ExceptionUtils.getStackTrace(exception));
            return Collections.emptyList();
        }
    }

}
