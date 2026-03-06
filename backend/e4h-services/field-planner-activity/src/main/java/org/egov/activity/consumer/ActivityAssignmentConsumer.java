package org.egov.activity.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
        try {
            ActivityAssignmentBulkRequest request = objectMapper.convertValue(consumerRecord, ActivityAssignmentBulkRequest.class);
            return activityService.createActivityAssignment(request);
        } catch (Exception exception) {
            log.error("error in fieldplan facility consumer bulk create", ExceptionUtils.getStackTrace(exception));
            return Collections.emptyList();
        }
    }

    // Update activity facility status workflow to SCHEDULED first, and Update to ASSIGNED_TO_FIELD_STAFF. So staff can see facility from APK
    @KafkaListener(topics = "${persister.kafka.create.topic}")
    public void updateActivityFacilityWorkflowStatus(Map<String, Object> producerRecord) throws Exception {
        log.info("Received topic from process-audit-records");
        String createActivityFacilityTopic = activityConfiguration.getCreateActivityFacilityTopic();
        if(producerRecord !=null && !producerRecord.isEmpty()){
            String topic = (String)producerRecord.get("topic");
            if(topic !=null && !topic.isEmpty() && topic.trim().equals(createActivityFacilityTopic)){
                log.info("Received topic from {}", createActivityFacilityTopic);
                Object value = producerRecord.get("value");
                ActivityFacilityBulkRequest request = objectMapper.convertValue(value, ActivityFacilityBulkRequest.class);
                    if (request!=null && request.getActivityFacilities()!=null){
                        List<ActivityFacility> activityFacilities = request.getActivityFacilities();
                        // Update status workflow to SCHEDULED first, and Update to ASSIGNED_TO_FIELD_STAFF. So staff can see facility from APK
                        List<String> ids = activityFacilities.stream().map(ActivityFacility::getId).collect(Collectors.toList());
                        FacilityBulkApproveRequest bulkRequest = FacilityBulkApproveRequest.builder()
                                .requestInfo(request.getRequestInfo())
                                .isAllSelected(false)
                                .activityFacilityIds(ids)
                                .filters(null)
                                .build();
                        processBulkWorkflow(bulkRequest);
                    }
            }
        }
    }

    public void processBulkWorkflow(FacilityBulkApproveRequest bulkRequest) throws Exception {
        //Update Status to SCHEDULED
        Workflow workflow = Workflow.builder()
                .action("SCHEDULED")
                .comments("Scheduled activity facility")
                .build();
        bulkRequest.setWorkflow(workflow);
        Map<String, Object> result = activityService.updateBulkActivityFacilityWorkflow(bulkRequest);
        log.info("Updating workflow for SCHEDULED action {} ", result);

        //Update Status to ASSIGNED_TO_FIELD_STAFF
        workflow.setAction("ASSIGN_FIELD_STAFF");
        workflow.setComments("Assign to Field Staff");
        bulkRequest.setWorkflow(workflow);
        Map<String, Object> result1 = activityService.updateBulkActivityFacilityWorkflow(bulkRequest);
        log.info("Updating workflow for ASSIGN_FIELD_STAFF action {} ", result1);
    }
}
