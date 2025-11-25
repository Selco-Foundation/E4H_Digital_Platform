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
        Long time = System.currentTimeMillis();
        if (isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).lastModifiedBy(by)
                    .createdTime(auditDetails.getCreatedTime()).lastModifiedTime(time).build();
    }

    public String extractStateName(String boundary) {
        if (boundary == null || boundary.trim().isEmpty()) {
            return null;
        }

        String[] boundaryParts = boundary.split("_");
        String stateName = null;

        if (boundaryParts.length >= 2 && "India".equalsIgnoreCase(boundaryParts[0])) {
            stateName = boundaryParts[1];
        } else if (boundaryParts.length >= 1) {
            stateName = boundaryParts[0];
        }

        // Validate state name is not placeholder/invalid
        if (stateName != null && !stateName.equalsIgnoreCase("nan") &&
                !stateName.equalsIgnoreCase("XYZ") && stateName.trim().length() > 0) {
            return stateName.trim();
        }

        log.warn("Invalid state name found in boundary: {}, returning null", stateName);
        return null;
    }


    /**
     * Creates a map from a list of projects, using project IDs as keys.
     *
     * @param projects The list of projects to be converted into a map.
     * @return A map with project IDs as keys and project objects as values.
     */
    public Map<String, Project> createProjectMap(List<Project> projects) {
        return projects.stream()
                .collect(Collectors.toMap(p -> String.valueOf(p.getId()), Function.identity()));
    }

    public void mergeAdditionalDetails(FieldPlan fieldPlan, FieldPlan fieldPlanFromDb) {
        JsonNode json = jsonMerge(objectMapper.valueToTree(fieldPlanFromDb.getAdditionalDetails()),
                objectMapper.valueToTree(fieldPlan.getAdditionalDetails()));
        fieldPlan.setAdditionalDetails(objectMapper.convertValue(json, Map.class));
    }

    /**
     * Method to merge additional details during update
     *
     * @param mainNode
     * @param updateNode
     * @return
     */
    public JsonNode jsonMerge(JsonNode mainNode, JsonNode updateNode) {

        if (isNull(mainNode) || mainNode.isNull())
            return updateNode;
        if (isNull(updateNode) || updateNode.isNull())
            return mainNode;

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
        return mainNode;
    }

    private String getDuration(FieldPlan fieldPlan) {
        if (fieldPlan.getStartDate() == null || fieldPlan.getEndDate() == null) {
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

        // Format as YYYY-YY
        return String.format("%d-%02d", startYear, endYear % 100);
    }

    public static String replaceActivityAssignmentEmailBody(String role, String fieldPlanName, String username, String password, String contenue){
        return contenue.replace(":role",role )
                .replace(":fieldPlanName", fieldPlanName)
                .replace(":login_agent", username)
                .replace(":password_agent", password);
    }

    public void sendEmailViaKafka(String emailId, String subject, String body, String tenantId) {
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
    }
}
