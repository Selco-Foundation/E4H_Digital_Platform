package org.egov.project.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.models.project.useraction.UserActionBulkRequest;
import org.egov.project.service.LocationCaptureService;
import org.egov.project.service.UserActionService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class UserActionConsumer {

    private final UserActionService userActionService;
    private final LocationCaptureService locationCaptureService;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserActionConsumer(UserActionService userActionService, LocationCaptureService locationCaptureService, ObjectMapper objectMapper) {
        // Constructor injection for services and object mapper
        this.userActionService = userActionService;
        this.locationCaptureService = locationCaptureService;
        this.objectMapper = objectMapper;
    }

    /**
     * Kafka listener for bulk creating user actions.
     *
     * @param consumerRecord The Kafka consumer record as a map.
     * @param topic          The topic from which the message was received.
     * @return List of created UserAction objects.
     */
    @KafkaListener(topics = "${project.user.action.consumer.bulk.create.topic}")
    public void bulkCreateUserAction(Map<String, Object> consumerRecord,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkCreateUserAction consumer for topic: {}", topic);
        log.info("Received bulk create request for user actions from topic: {}", topic);
        try {
            log.debug("Converting consumer record to UserActionBulkRequest");
            UserActionBulkRequest request = objectMapper.convertValue(consumerRecord, UserActionBulkRequest.class);
            log.debug("Processing {} user actions for bulk create", request.getUserActions() != null ? request.getUserActions().size() : 0);
            userActionService.create(request, true);
            log.info("Successfully processed bulk create for user actions");
            log.trace("Exiting bulkCreateUserAction consumer");
        } catch (Exception exception) {
            log.error("Error processing bulk create for user actions from topic: {}", topic, exception);
            log.trace("Exiting bulkCreateUserAction consumer with error");
            throw new CustomException("PROJECT_USER_ACTION_BULK_CREATE", exception.getMessage());
        }
    }

    /**
     * Kafka listener for bulk updating user actions.
     *
     * @param consumerRecord The Kafka consumer record as a map.
     * @param topic          The topic from which the message was received.
     * @return List of updated UserAction objects.
     */
    @KafkaListener(topics = "${project.user.action.consumer.bulk.update.topic}")
    public void bulkUpdateUserAction(Map<String, Object> consumerRecord,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkUpdateUserAction consumer for topic: {}", topic);
        log.info("Received bulk update request for user actions from topic: {}", topic);
        try {
            log.debug("Converting consumer record to UserActionBulkRequest");
            UserActionBulkRequest request = objectMapper.convertValue(consumerRecord, UserActionBulkRequest.class);
            log.debug("Processing {} user actions for bulk update", request.getUserActions() != null ? request.getUserActions().size() : 0);
            userActionService.update(request, true);
            log.info("Successfully processed bulk update for user actions");
            log.trace("Exiting bulkUpdateUserAction consumer");
        } catch (Exception exception) {
            log.error("Error processing bulk update for user actions from topic: {}", topic, exception);
            log.trace("Exiting bulkUpdateUserAction consumer with error");
            throw new CustomException("PROJECT_USER_ACTION_BULK_UPDATE", exception.getMessage());
        }
    }

    /**
     * Kafka listener for bulk creating location captures.
     *
     * @param consumerRecord The Kafka consumer record as a map.
     * @param topic          The topic from which the message was received.
     * @return List of created UserAction objects.
     */
    @KafkaListener(topics = "${project.location.capture.consumer.bulk.create.topic}")
    public void bulkCreateLocationCapture(Map<String, Object> consumerRecord,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkCreateLocationCapture consumer for topic: {}", topic);
        log.info("Received bulk create request for location captures from topic: {}", topic);
        try {
            log.debug("Converting consumer record to UserActionBulkRequest");
            UserActionBulkRequest request = objectMapper.convertValue(consumerRecord, UserActionBulkRequest.class);
            log.debug("Processing {} location captures for bulk create", request.getUserActions() != null ? request.getUserActions().size() : 0);
            locationCaptureService.create(request, true);
            log.info("Successfully processed bulk create for location captures");
            log.trace("Exiting bulkCreateLocationCapture consumer");
        } catch (Exception exception) {
            log.error("Error processing bulk create for location captures from topic: {}", topic, exception);
            log.trace("Exiting bulkCreateLocationCapture consumer with error");
            throw new CustomException("PROJECT_USER_ACTION_LOCATION_CAPTURE_BULK_CREATE", exception.getMessage());
        }
    }

}
