package org.egov.field_planner.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Project;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.web.models.FieldPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class FieldPlannerServiceUtil {
    @Autowired
    private ObjectMapper objectMapper;

    private final FieldPlannerConfiguration fieldPlannerConfiguration;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FieldPlannerServiceUtil(FieldPlannerConfiguration fieldPlannerConfiguration, KafkaTemplate<String, Object> kafkaTemplate) {
        this.fieldPlannerConfiguration = fieldPlannerConfiguration;
        this.kafkaTemplate = kafkaTemplate;
    }

    public AuditDetails getAuditDetails(String by, AuditDetails auditDetails, Boolean isCreate) {
        log.trace("Entering getAuditDetails method, isCreate: {}", isCreate);
        Long time = System.currentTimeMillis();
        AuditDetails result;
        if (isCreate) {
            result = AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
            log.debug("Created new audit details for user: {}", by);
        } else {
            result = AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).lastModifiedBy(by)
                    .createdTime(auditDetails.getCreatedTime()).lastModifiedTime(time).build();
            log.debug("Updated audit details, last modified by: {}", by);
        }
        log.trace("Exiting getAuditDetails method");
        return result;
    }

    public String extractStateName(String boundary) {
        log.trace("Entering extractStateName method");
        if (boundary == null || boundary.trim().isEmpty()) {
            log.debug("Boundary is null or empty, returning null");
            return null;
        }

        String[] boundaryParts = boundary.split("_");
        String stateName = null;

        if (boundaryParts.length >= 2 && "India".equalsIgnoreCase(boundaryParts[0])) {
            stateName = boundaryParts[1];
            log.debug("Extracted state name from India boundary: {}", stateName);
        } else if (boundaryParts.length >= 1) {
            stateName = boundaryParts[0];
            log.debug("Extracted state name from boundary: {}", stateName);
        }

        // Validate state name is not placeholder/invalid
        if (stateName != null && !stateName.equalsIgnoreCase("nan") &&
                !stateName.equalsIgnoreCase("XYZ") && stateName.trim().length() > 0) {
            log.trace("Exiting extractStateName method");
            return stateName.trim();
        }

        log.warn("Invalid state name found in boundary: {}, returning null", stateName);
        log.trace("Exiting extractStateName method");
        return null;
    }


    /**
     * Creates a map from a list of projects, using project IDs as keys.
     *
     * @param projects The list of projects to be converted into a map.
     * @return A map with project IDs as keys and project objects as values.
     */
    public Map<String, Project> createProjectMap(List<Project> projects) {
        log.trace("Entering createProjectMap method");
        log.debug("Creating project map from {} projects", projects.size());
        Map<String, Project> result = projects.stream()
                .collect(Collectors.toMap(p -> String.valueOf(p.getId()), Function.identity()));
        log.debug("Created project map with {} entries", result.size());
        log.trace("Exiting createProjectMap method");
        return result;
    }

    public void mergeAdditionalDetails(FieldPlan fieldPlan, FieldPlan fieldPlanFromDb) {
        log.trace("Entering mergeAdditionalDetails method for field plan ID: {}", fieldPlan.getId());
        log.debug("Merging additional details for field plan");
        JsonNode json = jsonMerge(objectMapper.valueToTree(fieldPlanFromDb.getAdditionalDetails()),
                objectMapper.valueToTree(fieldPlan.getAdditionalDetails()));
        fieldPlan.setAdditionalDetails(objectMapper.convertValue(json, Map.class));
        log.debug("Additional details merged successfully");
        log.trace("Exiting mergeAdditionalDetails method");
    }

    /**
     * Method to merge additional details during update
     *
     * @param mainNode
     * @param updateNode
     * @return
     */
    public JsonNode jsonMerge(JsonNode mainNode, JsonNode updateNode) {
        log.trace("Entering jsonMerge method");
        if (isNull(mainNode) || mainNode.isNull()) {
            log.debug("Main node is null, returning update node");
            return updateNode;
        }
        if (isNull(updateNode) || updateNode.isNull()) {
            log.debug("Update node is null, returning main node");
            return mainNode;
        }

        log.debug("Merging JSON nodes");
        Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode jsonNode = mainNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                jsonMerge(jsonNode, updateNode.get(fieldName));
            } else {
                if (mainNode instanceof ObjectNode) {
                    // Overwrite field
                    JsonNode value = updateNode.get(fieldName);
                    ((ObjectNode) mainNode).set(fieldName, value);
                }
            }

        }
        log.trace("Exiting jsonMerge method");
        return mainNode;
    }

    private String getDuration(FieldPlan fieldPlan) {
        log.trace("Entering getDuration method");
        if (fieldPlan.getStartDate() == null || fieldPlan.getEndDate() == null) {
            log.error("Start date or end date is null for field plan");
            throw new RuntimeException("Start date and end date are required for fieldPlan name generation");
        }

        LocalDateTime startDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(fieldPlan.getStartDate()),
                ZoneId.systemDefault()
        );

        LocalDateTime endDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(fieldPlan.getEndDate()),
                ZoneId.systemDefault()
        );

        int startYear = startDate.getYear();
        int endYear = endDate.getYear();
        log.debug("Calculated duration: startYear={}, endYear={}", startYear, endYear);

        // Format as YYYY-YY
        String duration = String.format("%d-%02d", startYear, endYear % 100);
        log.trace("Exiting getDuration method");
        return duration;
    }

    public static String replaceActivityAssignmentEmailBody(String role, String fieldPlanName, String username, String password, String contenue){
        log.trace("Entering replaceActivityAssignmentEmailBody method");
        log.debug("Replacing email body placeholders for role: {}, field plan: {}", role, fieldPlanName);
        String result = contenue.replace(":role",role )
                .replace(":fieldPlanName", fieldPlanName)
                .replace(":login_agent", username)
                .replace(":password_agent", password);
        log.trace("Exiting replaceActivityAssignmentEmailBody method");
        return result;
    }

    public void sendEmailViaKafka(String emailId, String subject, String body, String tenantId) {
        log.trace("Entering sendEmailViaKafka method");
        log.info("Sending email via Kafka for user: {}, tenant: {}", emailId, tenantId);
        try {
            // Create Email object following egov-notification-mail contract
            Map<String, Object> email = new HashMap<>();
            email.put("emailTo", new HashSet<>(Arrays.asList(emailId)));  // Set<String>
            email.put("subject", subject);
            email.put("body", body);
//            email.put("isHTML", true);
            email.put("tenantId", tenantId);

            // Note: CSV files are not attached as email attachments anymore
            // Download functionality is provided via download buttons in the email template

            // Create EmailRequest wrapper with RequestInfo
            Map<String, Object> emailRequest = new HashMap<>();
            emailRequest.put("requestInfo", new HashMap<>());  // Empty RequestInfo is acceptable
            emailRequest.put("email", email);

            // Publish to Kafka
            String topic = fieldPlannerConfiguration.getNotificationEmailTopic();
            kafkaTemplate.send(topic, emailRequest);

            log.info("Published email to Kafka topic: {} for user: {} (no attachments - download buttons used instead)",
                    topic, emailId);

        } catch (Exception e) {
            log.error("Error sending email via Kafka for user: {}", emailId, e);
            throw new RuntimeException("Failed to send email via Kafka", e);
        }
        log.trace("Exiting sendEmailViaKafka method");
    }
}
