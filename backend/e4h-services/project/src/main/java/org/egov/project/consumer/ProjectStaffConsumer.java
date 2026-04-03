package org.egov.project.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.models.project.ProjectStaff;
import org.egov.common.models.project.ProjectStaffBulkRequest;
import org.egov.project.service.ProjectStaffService;
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
public class ProjectStaffConsumer {

    private final ProjectStaffService service;
    private final ObjectMapper objectMapper;

    public ProjectStaffConsumer(ProjectStaffService service, @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${project.staff.consumer.bulk.create.topic}")
    public List<ProjectStaff> bulkCreate(Map<String, Object> consumerRecord,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkCreate consumer for topic: {}", topic);
        log.info("Received bulk create request for project staff from topic: {}", topic);
        try {
            log.debug("Converting consumer record to ProjectStaffBulkRequest");
            ProjectStaffBulkRequest request = objectMapper.convertValue(consumerRecord, ProjectStaffBulkRequest.class);
            log.debug("Processing {} staff for bulk create", request.getProjectStaff() != null ? request.getProjectStaff().size() : 0);
            List<ProjectStaff> result = service.create(request, true);
            log.info("Successfully processed bulk create for {} staff", result != null ? result.size() : 0);
            log.trace("Exiting bulkCreate consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project staff consumer bulk create for topic: {}", topic, exception);
            log.trace("Exiting bulkCreate consumer with error");
            return Collections.emptyList();
        }
    }

    @KafkaListener(topics = "${project.staff.consumer.bulk.update.topic}")
    public List<ProjectStaff> bulkUpdate(Map<String, Object> consumerRecord,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkUpdate consumer for topic: {}", topic);
        log.info("Received bulk update request for project staff from topic: {}", topic);
        try {
            log.debug("Converting consumer record to ProjectStaffBulkRequest");
            ProjectStaffBulkRequest request = objectMapper.convertValue(consumerRecord, ProjectStaffBulkRequest.class);
            log.debug("Processing {} staff for bulk update", request.getProjectStaff() != null ? request.getProjectStaff().size() : 0);
            List<ProjectStaff> result = service.update(request, true);
            log.info("Successfully processed bulk update for {} staff", result != null ? result.size() : 0);
            log.trace("Exiting bulkUpdate consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project staff consumer bulk update for topic: {}", topic, exception);
            log.trace("Exiting bulkUpdate consumer with error");
            return Collections.emptyList();
        }
    }

    @KafkaListener(topics = "${project.staff.consumer.bulk.delete.topic}")
    public List<ProjectStaff> bulkDelete(Map<String, Object> consumerRecord,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkDelete consumer for topic: {}", topic);
        log.info("Received bulk delete request for project staff from topic: {}", topic);
        try {
            log.debug("Converting consumer record to ProjectStaffBulkRequest");
            ProjectStaffBulkRequest request = objectMapper.convertValue(consumerRecord, ProjectStaffBulkRequest.class);
            log.debug("Processing {} staff for bulk delete", request.getProjectStaff() != null ? request.getProjectStaff().size() : 0);
            List<ProjectStaff> result = service.delete(request, true);
            log.info("Successfully processed bulk delete for {} staff", result != null ? result.size() : 0);
            log.trace("Exiting bulkDelete consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project staff consumer bulk delete for topic: {}", topic, exception);
            log.trace("Exiting bulkDelete consumer with error");
            return Collections.emptyList();
        }
    }

}
