package org.egov.project.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Document;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Component
@Slf4j
public class ProjectServiceUtil {
    @Autowired
    private ObjectMapper objectMapper;

    public AuditDetails getAuditDetails(String by, AuditDetails auditDetails, Boolean isCreate) {
        log.trace("Entering getAuditDetails for user: {}, isCreate: {}", by, isCreate);
        Long time = System.currentTimeMillis();
        AuditDetails result;
        if (isCreate) {
            log.debug("Creating new audit details");
            result = AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        } else {
            log.debug("Updating audit details with lastModifiedBy");
            result = AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).lastModifiedBy(by)
                    .createdTime(auditDetails.getCreatedTime()).lastModifiedTime(time).build();
        }
        log.trace("Exiting getAuditDetails");
        return result;
    }


    /**
     * Creates a map from a list of projects, using project IDs as keys.
     *
     * @param projects The list of projects to be converted into a map.
     * @return A map with project IDs as keys and project objects as values.
     */
    public Map<String, Project> createProjectMap(List<Project> projects) {
        log.trace("Entering createProjectMap for {} projects", projects != null ? projects.size() : 0);
        log.debug("Creating project map from project list");
        Map<String, Project> result = projects.stream()
                .collect(Collectors.toMap(p -> String.valueOf(p.getId()), Function.identity()));
        log.debug("Created map with {} entries", result.size());
        log.trace("Exiting createProjectMap");
        return result;
    }

    public void mergeAdditionalDetails(Project project, Project projectFromDb) {
        log.trace("Entering mergeAdditionalDetails for project: {}", project.getId());
        log.debug("Merging additional details from database project into request project");
        project.setAdditionalDetails(jsonMerge(objectMapper.valueToTree(projectFromDb.getAdditionalDetails()),
                objectMapper.valueToTree(project.getAdditionalDetails())));
        log.debug("Successfully merged additional details");
        log.trace("Exiting mergeAdditionalDetails");
    }

    /**
     * Method to merge additional details during update
     *
     * @param mainNode
     * @param updateNode
     * @return
     */
    public JsonNode jsonMerge(JsonNode mainNode, JsonNode updateNode) {
        log.trace("Entering jsonMerge");
        log.debug("Merging JSON nodes");

        if (isNull(mainNode) || mainNode.isNull()) {
            log.debug("Main node is null, returning update node");
            log.trace("Exiting jsonMerge");
            return updateNode;
        }
        if (isNull(updateNode) || updateNode.isNull()) {
            log.debug("Update node is null, returning main node");
            log.trace("Exiting jsonMerge");
            return mainNode;
        }

        Iterator<String> fieldNames = updateNode.fieldNames();
        int fieldCount = 0;
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            fieldCount++;
            JsonNode jsonNode = mainNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                log.debug("Merging nested object for field: {}", fieldName);
                jsonMerge(jsonNode, updateNode.get(fieldName));
            } else {
                if (mainNode instanceof ObjectNode) {
                    // Overwrite field
                    JsonNode value = updateNode.get(fieldName);
                    ((ObjectNode) mainNode).set(fieldName, value);
                    log.debug("Merged field: {}", fieldName);
                }
            }

        }
        log.debug("Merged {} fields", fieldCount);
        log.trace("Exiting jsonMerge");
        return mainNode;
    }
}
