package org.egov.project.consumer;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.models.project.ProjectResource;
import org.egov.common.models.project.ProjectResourceBulkRequest;
import org.egov.project.service.ProjectResourceService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ProjectResourceConsumer {

    private final ProjectResourceService service;
    private final ObjectMapper objectMapper;

    public ProjectResourceConsumer(ProjectResourceService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }


    @KafkaListener(topics = "${project.resource.consumer.bulk.create.topic}")
    public List<ProjectResource> bulkCreate(Map<String, Object> consumerRecord,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkCreate consumer for topic: {}", topic);
        log.info("Received bulk create request for project resources from topic: {}", topic);
        try {
            log.debug("Converting consumer record to ProjectResourceBulkRequest");
            ProjectResourceBulkRequest request = objectMapper.convertValue(consumerRecord, ProjectResourceBulkRequest.class);
            log.debug("Processing {} resources for bulk create", request.getProjectResource() != null ? request.getProjectResource().size() : 0);
            List<ProjectResource> result = service.create(request, true);
            log.info("Successfully processed bulk create for {} resources", result != null ? result.size() : 0);
            log.trace("Exiting bulkCreate consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project resource consumer bulk create for topic: {}", topic, exception);
            log.trace("Exiting bulkCreate consumer with error");
            return Collections.emptyList();
        }
    }

    @KafkaListener(topics = "${project.resource.consumer.bulk.update.topic}")
    public List<ProjectResource> bulkUpdate(Map<String, Object> consumerRecord,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkUpdate consumer for topic: {}", topic);
        log.info("Received bulk update request for project resources from topic: {}", topic);
        try {
            log.debug("Converting consumer record to ProjectResourceBulkRequest");
            ProjectResourceBulkRequest request = objectMapper.convertValue(consumerRecord, ProjectResourceBulkRequest.class);
            log.debug("Processing {} resources for bulk update", request.getProjectResource() != null ? request.getProjectResource().size() : 0);
            List<ProjectResource> result = service.update(request, true);
            log.info("Successfully processed bulk update for {} resources", result != null ? result.size() : 0);
            log.trace("Exiting bulkUpdate consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project resource consumer bulk update for topic: {}", topic, exception);
            log.trace("Exiting bulkUpdate consumer with error");
            return Collections.emptyList();
        }
    }

    @KafkaListener(topics = "${project.resource.consumer.bulk.delete.topic}")
    public List<ProjectResource> bulkDelete(Map<String, Object> consumerRecord,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkDelete consumer for topic: {}", topic);
        log.info("Received bulk delete request for project resources from topic: {}", topic);
        try {
            log.debug("Converting consumer record to ProjectResourceBulkRequest");
            ProjectResourceBulkRequest request = objectMapper.convertValue(consumerRecord, ProjectResourceBulkRequest.class);
            log.debug("Processing {} resources for bulk delete", request.getProjectResource() != null ? request.getProjectResource().size() : 0);
            List<ProjectResource> result = service.delete(request, true);
            log.info("Successfully processed bulk delete for {} resources", result != null ? result.size() : 0);
            log.trace("Exiting bulkDelete consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project resource consumer bulk delete for topic: {}", topic, exception);
            log.trace("Exiting bulkDelete consumer with error");
            return Collections.emptyList();
        }
    }
}
