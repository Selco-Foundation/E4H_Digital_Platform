package org.egov.activity.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.egov.activity.web.models.ActivityFacility;
import org.egov.activity.web.models.BillOfMaterial;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Project;
import org.egov.activity.web.models.FieldPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Component
public class ActivityServiceUtil {
    @Autowired
    private ObjectMapper objectMapper;

    public AuditDetails getAuditDetails(String by, AuditDetails auditDetails, Boolean isCreate) {
        Long time = System.currentTimeMillis();
        if (isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).lastModifiedBy(by)
                    .createdTime(auditDetails.getCreatedTime()).lastModifiedTime(time).build();
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

    public void mergeAdditionalDetails(ActivityFacility activityFacility, ActivityFacility activityFacilityFromDb) {
        JsonNode json = jsonMerge(objectMapper.valueToTree(activityFacilityFromDb.getAdditionalDetails()),
                objectMapper.valueToTree(activityFacility.getAdditionalDetails()));
        activityFacility.setAdditionalDetails(objectMapper.convertValue(json, Map.class));
    }

    public void mergeBOMAdditionalDetails(BillOfMaterial billOfMaterial, BillOfMaterial billOfMaterialFromDb) {
        JsonNode json = jsonMerge(objectMapper.valueToTree(billOfMaterialFromDb.getAdditionalDetails()),
                objectMapper.valueToTree(billOfMaterial.getAdditionalDetails()));
        billOfMaterial.setAdditionalDetails(objectMapper.convertValue(json, Map.class));
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
}
