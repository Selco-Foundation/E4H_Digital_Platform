package org.egov.project.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.models.project.BeneficiaryBulkRequest;
import org.egov.common.models.project.ProjectBeneficiary;
import org.egov.project.service.ProjectBeneficiaryService;
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
public class ProjectBeneficiaryConsumer {

    private final ProjectBeneficiaryService projectBeneficiaryService;

    private final ObjectMapper objectMapper;

    @Autowired
    public ProjectBeneficiaryConsumer(ProjectBeneficiaryService projectBeneficiaryService,
                                      @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.projectBeneficiaryService = projectBeneficiaryService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${project.beneficiary.consumer.bulk.create.topic}")
    public List<ProjectBeneficiary> bulkCreate(Map<String, Object> consumerRecord,
                                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkCreate consumer for topic: {}", topic);
        log.info("Received bulk create request for project beneficiaries from topic: {}", topic);
        try {
            log.debug("Converting consumer record to BeneficiaryBulkRequest");
            BeneficiaryBulkRequest request = objectMapper.convertValue(consumerRecord, BeneficiaryBulkRequest.class);
            log.debug("Processing {} beneficiaries for bulk create", request.getProjectBeneficiaries() != null ? request.getProjectBeneficiaries().size() : 0);
            List<ProjectBeneficiary> result = projectBeneficiaryService.create(request, true);
            log.info("Successfully processed bulk create for {} beneficiaries", result != null ? result.size() : 0);
            log.trace("Exiting bulkCreate consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project beneficiary consumer bulk create for topic: {}", topic, exception);
            log.trace("Exiting bulkCreate consumer with error");
            return Collections.emptyList();
        }
    }

    @KafkaListener(topics = "${project.beneficiary.consumer.bulk.update.topic}")
    public List<ProjectBeneficiary> bulkUpdate(Map<String, Object> consumerRecord,
                                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkUpdate consumer for topic: {}", topic);
        log.info("Received bulk update request for project beneficiaries from topic: {}", topic);
        try {
            log.debug("Converting consumer record to BeneficiaryBulkRequest");
            BeneficiaryBulkRequest request = objectMapper.convertValue(consumerRecord, BeneficiaryBulkRequest.class);
            log.debug("Processing {} beneficiaries for bulk update", request.getProjectBeneficiaries() != null ? request.getProjectBeneficiaries().size() : 0);
            List<ProjectBeneficiary> result = projectBeneficiaryService.update(request, true);
            log.info("Successfully processed bulk update for {} beneficiaries", result != null ? result.size() : 0);
            log.trace("Exiting bulkUpdate consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project beneficiary consumer bulk update for topic: {}", topic, exception);
            log.trace("Exiting bulkUpdate consumer with error");
            return Collections.emptyList();
        }
    }

    @KafkaListener(topics = "${project.beneficiary.consumer.bulk.delete.topic}")
    public List<ProjectBeneficiary> bulkDelete(Map<String, Object> consumerRecord,
                                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("Entering bulkDelete consumer for topic: {}", topic);
        log.info("Received bulk delete request for project beneficiaries from topic: {}", topic);
        try {
            log.debug("Converting consumer record to BeneficiaryBulkRequest");
            BeneficiaryBulkRequest request = objectMapper.convertValue(consumerRecord, BeneficiaryBulkRequest.class);
            log.debug("Processing {} beneficiaries for bulk delete", request.getProjectBeneficiaries() != null ? request.getProjectBeneficiaries().size() : 0);
            List<ProjectBeneficiary> result = projectBeneficiaryService.delete(request, true);
            log.info("Successfully processed bulk delete for {} beneficiaries", result != null ? result.size() : 0);
            log.trace("Exiting bulkDelete consumer");
            return result;
        } catch (Exception exception) {
            log.error("Error in project beneficiary consumer bulk delete for topic: {}", topic, exception);
            log.trace("Exiting bulkDelete consumer with error");
            return Collections.emptyList();
        }
    }
}
