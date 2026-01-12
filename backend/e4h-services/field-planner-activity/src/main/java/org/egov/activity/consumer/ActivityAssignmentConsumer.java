package org.egov.activity.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.service.ActivityService;
import org.egov.activity.web.models.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ActivityAssignmentConsumer {

    private final ObjectMapper objectMapper;
    private final ActivityConfiguration activityConfiguration;
    private ActivityService activityService;

    public ActivityAssignmentConsumer(@Qualifier("objectMapper") ObjectMapper objectMapper, ActivityConfiguration activityConfiguration, ActivityService activityService) {
        this.objectMapper = objectMapper;
        this.activityConfiguration = activityConfiguration;
        this.activityService = activityService;
    }

    @KafkaListener(topics = "${activity.assignment.consumer.bulk.create.topic}")
    public List<ActivityAssignment> bulkCreate(Map<String, Object> consumerRecord,
                                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("bulkCreate consumer method invoked, topic: {}", topic);
        log.info("Received Kafka message for bulk activity assignment creation, topic: {}", topic);
        try {
            ActivityAssignmentBulkRequest request = objectMapper.convertValue(consumerRecord, ActivityAssignmentBulkRequest.class);
            int assignmentCount = request.getActivityAssignments() != null ? request.getActivityAssignments().size() : 0;
            log.debug("Processing {} activity assignments from Kafka message", assignmentCount);
            List<ActivityAssignment> result = activityService.createActivityAssignment(request);
            log.info("Successfully processed Kafka message for activity assignment creation, count: {}", assignmentCount);
            return result;
        } catch (Exception exception) {
            log.error("Error processing Kafka message for bulk activity assignment creation, topic: {}", topic, exception);
            return Collections.emptyList();
        }
    }

    // Update activity facility status workflow to SCHEDULED first, and Update to ASSIGNED_TO_FIELD_STAFF. So staff can see facility from APK
    @KafkaListener(topics = "${persister.kafka.create.topic}")
    public void updateActivityFacilityWorkflowStatus(Map<String, Object> producerRecord) throws Exception {
        log.trace("updateActivityFacilityWorkflowStatus consumer method invoked");
        log.info("Received Kafka message from process-audit-records topic");
        String createActivityFacilityTopic = activityConfiguration.getCreateActivityFacilityTopic();
        if(producerRecord !=null && !producerRecord.isEmpty()){
            String topic = (String)producerRecord.get("topic");
            if(topic !=null && !topic.isEmpty() && topic.trim().equals(createActivityFacilityTopic)){
                log.info("Processing workflow status update for topic: {}", createActivityFacilityTopic);
                Object value = producerRecord.get("value");
                ActivityFacilityBulkRequest request = objectMapper.convertValue(value, ActivityFacilityBulkRequest.class);
                    if (request!=null && request.getActivityFacilities()!=null){
                        List<ActivityFacility> activityFacilities = request.getActivityFacilities();
                        int facilityCount = activityFacilities.size();
                        log.debug("Processing workflow status update for {} activity facilities", facilityCount);
                        // Update status workflow to SCHEDULED first, and Update to ASSIGNED_TO_FIELD_STAFF. So staff can see facility from APK
                        List<String> ids = activityFacilities.stream().map(ActivityFacility::getId).collect(Collectors.toList());
                        FacilityBulkApproveRequest bulkRequest = FacilityBulkApproveRequest.builder()
                                .requestInfo(request.getRequestInfo())
                                .isAllSelected(false)
                                .activityFacilityIds(ids)
                                .filters(null)
                                .build();
                        processBulkWorkflow(bulkRequest);
                        log.info("Successfully processed workflow status update for {} activity facilities", facilityCount);
                    }
            }
        }
    }

    public void processBulkWorkflow(FacilityBulkApproveRequest bulkRequest) throws Exception {
        log.trace("processBulkWorkflow method invoked");
        int facilityCount = bulkRequest.getActivityFacilityIds() != null ? bulkRequest.getActivityFacilityIds().size() : 0;
        log.info("Processing bulk workflow update for {} activity facilities", facilityCount);
        //Update Status to SCHEDULED
        Workflow workflow = Workflow.builder()
                .action("SCHEDULED")
                .comments("Scheduled activity facility")
                .build();
        bulkRequest.setWorkflow(workflow);
        log.debug("Updating workflow status to SCHEDULED");
        Map<String, Object> result = activityService.updateBulkActivityFacilityWorkflow(bulkRequest);
        log.info("Workflow status updated to SCHEDULED, succeeded: {}, failed: {}", 
                result.get("succeededActivityFacilitiesIDs") != null ? ((List<?>) result.get("succeededActivityFacilitiesIDs")).size() : 0,
                result.get("failedActivityFacilityIDs") != null ? ((List<?>) result.get("failedActivityFacilityIDs")).size() : 0);

        //Update Status to ASSIGN_FIELD_STAFF
        workflow.setAction("ASSIGN_FIELD_STAFF");
        workflow.setComments("Assign to Field Staff");
        bulkRequest.setWorkflow(workflow);
        log.debug("Updating workflow status to ASSIGN_FIELD_STAFF");
        Map<String, Object> result1 = activityService.updateBulkActivityFacilityWorkflow(bulkRequest);
        log.info("Workflow status updated to ASSIGN_FIELD_STAFF, succeeded: {}, failed: {}", 
                result1.get("succeededActivityFacilitiesIDs") != null ? ((List<?>) result1.get("succeededActivityFacilitiesIDs")).size() : 0,
                result1.get("failedActivityFacilityIDs") != null ? ((List<?>) result1.get("failedActivityFacilityIDs")).size() : 0);
    }
}
