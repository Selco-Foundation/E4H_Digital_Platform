package org.egov.project.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.models.project.Task;
import org.egov.common.models.project.TaskBulkRequest;
import org.egov.project.service.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ProjectTaskConsumer {

    private final ProjectTaskService projectTaskService;

    private final ObjectMapper objectMapper;

    @Autowired
    public ProjectTaskConsumer(ProjectTaskService projectTaskService,
                               @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.projectTaskService = projectTaskService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${project.task.consumer.bulk.create.topic}")
    public List<Task> bulkCreate(Map<String, Object> consumerRecord,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkCreate consumer for topic: {}", topic);
        log.info("Received bulk create request for project tasks from topic: {}", topic);
        try {
            log.debug("Converting consumer record to TaskBulkRequest");
            TaskBulkRequest request = objectMapper.convertValue(consumerRecord, TaskBulkRequest.class);
            log.debug("Processing {} tasks for bulk create", request.getTasks() != null ? request.getTasks().size() : 0);
            List<Task> result = projectTaskService.create(request, true);
            log.info("Successfully processed bulk create for {} tasks", result != null ? result.size() : 0);
            log.trace("Exiting bulkCreate consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project task consumer bulk create for topic: {}", topic, exception);
            log.trace("Exiting bulkCreate consumer with error");
            return Collections.emptyList();
        }
    }

    @KafkaListener(topics = "${project.task.consumer.bulk.update.topic}")
    public List<Task> bulkUpdate(Map<String, Object> consumerRecord,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkUpdate consumer for topic: {}", topic);
        log.info("Received bulk update request for project tasks from topic: {}", topic);
        try {
            log.debug("Converting consumer record to TaskBulkRequest");
            TaskBulkRequest request = objectMapper.convertValue(consumerRecord, TaskBulkRequest.class);
            log.debug("Processing {} tasks for bulk update", request.getTasks() != null ? request.getTasks().size() : 0);
            List<Task> result = projectTaskService.update(request, true);
            log.info("Successfully processed bulk update for {} tasks", result != null ? result.size() : 0);
            log.trace("Exiting bulkUpdate consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project task consumer bulk update for topic: {}", topic, exception);
            log.trace("Exiting bulkUpdate consumer with error");
            return Collections.emptyList();
        }
    }

    @KafkaListener(topics = "${project.task.consumer.bulk.delete.topic}")
    public List<Task> bulkDelete(Map<String, Object> consumerRecord,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkDelete consumer for topic: {}", topic);
        log.info("Received bulk delete request for project tasks from topic: {}", topic);
        try {
            log.debug("Converting consumer record to TaskBulkRequest");
            TaskBulkRequest request = objectMapper.convertValue(consumerRecord, TaskBulkRequest.class);
            log.debug("Processing {} tasks for bulk delete", request.getTasks() != null ? request.getTasks().size() : 0);
            List<Task> result = projectTaskService.delete(request, true);
            log.info("Successfully processed bulk delete for {} tasks", result != null ? result.size() : 0);
            log.trace("Exiting bulkDelete consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project task consumer bulk delete for topic: {}", topic, exception);
            log.trace("Exiting bulkDelete consumer with error");
            return Collections.emptyList();
        }
    }
}
