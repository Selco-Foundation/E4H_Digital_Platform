package org.egov.project.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.models.project.ProjectFacility;
import org.egov.common.models.project.ProjectFacilityBulkRequest;
import org.egov.project.service.ProjectFacilityService;
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
public class ProjectFacilityConsumer {

    private final ProjectFacilityService service;
    private final ObjectMapper objectMapper;

    public ProjectFacilityConsumer(ProjectFacilityService service, @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${project.facility.consumer.bulk.create.topic}")
    public List<ProjectFacility> bulkCreate(Map<String, Object> consumerRecord,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkCreate consumer for topic: {}", topic);
        log.info("Received bulk create request for project facilities from topic: {}", topic);
        try {
            log.debug("Converting consumer record to ProjectFacilityBulkRequest");
            ProjectFacilityBulkRequest request = objectMapper.convertValue(consumerRecord, ProjectFacilityBulkRequest.class);
            log.debug("Processing {} facilities for bulk create", request.getProjectFacilities() != null ? request.getProjectFacilities().size() : 0);
            List<ProjectFacility> result = service.create(request, true);
            log.info("Successfully processed bulk create for {} facilities", result != null ? result.size() : 0);
            log.trace("Exiting bulkCreate consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project facility consumer bulk create for topic: {}", topic, exception);
            log.trace("Exiting bulkCreate consumer with error");
            return Collections.emptyList();
        }
    }

    @KafkaListener(topics = "${project.facility.consumer.bulk.update.topic}")
    public List<ProjectFacility> bulkUpdate(Map<String, Object> consumerRecord,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkUpdate consumer for topic: {}", topic);
        log.info("Received bulk update request for project facilities from topic: {}", topic);
        try {
            log.debug("Converting consumer record to ProjectFacilityBulkRequest");
            ProjectFacilityBulkRequest request = objectMapper.convertValue(consumerRecord, ProjectFacilityBulkRequest.class);
            log.debug("Processing {} facilities for bulk update", request.getProjectFacilities() != null ? request.getProjectFacilities().size() : 0);
            List<ProjectFacility> result = service.update(request, true);
            log.info("Successfully processed bulk update for {} facilities", result != null ? result.size() : 0);
            log.trace("Exiting bulkUpdate consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project facility consumer bulk update for topic: {}", topic, exception);
            log.trace("Exiting bulkUpdate consumer with error");
            return Collections.emptyList();
        }
    }

    @KafkaListener(topics = "${project.facility.consumer.bulk.delete.topic}")
    public List<ProjectFacility> bulkDelete(Map<String, Object> consumerRecord,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkDelete consumer for topic: {}", topic);
        log.info("Received bulk delete request for project facilities from topic: {}", topic);
        try {
            log.debug("Converting consumer record to ProjectFacilityBulkRequest");
            ProjectFacilityBulkRequest request = objectMapper.convertValue(consumerRecord, ProjectFacilityBulkRequest.class);
            log.debug("Processing {} facilities for bulk delete", request.getProjectFacilities() != null ? request.getProjectFacilities().size() : 0);
            List<ProjectFacility> result = service.delete(request, true);
            log.info("Successfully processed bulk delete for {} facilities", result != null ? result.size() : 0);
            log.trace("Exiting bulkDelete consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project facility consumer bulk delete for topic: {}", topic, exception);
            log.trace("Exiting bulkDelete consumer with error");
            return Collections.emptyList();
        }
    }

}
