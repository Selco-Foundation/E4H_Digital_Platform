package org.egov.project.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Document;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.Target;
import org.egov.project.web.models.ProjectV2;
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
public class ProjectServiceUtil {
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

    public static List<ProjectV2> convertProjectToV2(List<Project> projects) {
        List<ProjectV2> projectV2List = new ArrayList<>();
        for (Project project : projects){
            ProjectV2 v2 = new ProjectV2();

            v2.setId(project.getId());
            v2.setTenantId(project.getTenantId());
            v2.setProjectNumber(project.getProjectNumber());
            v2.setName(project.getName());
            v2.setProjectType(project.getProjectType());
            v2.setProjectSubType(project.getProjectSubType());
            v2.setDepartment(project.getDepartment());
            v2.setDescription(project.getDescription());
            v2.setReferenceID(project.getReferenceID());
            v2.setProjectTypeId(project.getProjectTypeId());
            v2.setAddress(project.getAddress());
            v2.setStartDate(project.getStartDate());
            v2.setEndDate(project.getEndDate());
            v2.setIsTaskEnabled(project.getIsTaskEnabled());
            v2.setParent(project.getParent());
            v2.setProjectHierarchy(project.getProjectHierarchy());
            v2.setNatureOfWork(project.getNatureOfWork());
            v2.setAncestors(project.getAncestors());
            v2.setDescendants(project.getDescendants());
            v2.setAdditionalDetails(project.getAdditionalDetails());
            v2.setIsDeleted(project.getIsDeleted());
            v2.setRowVersion(project.getRowVersion());
            v2.setAuditDetails(project.getAuditDetails());

            // Handle lists (documents, targets)
            if (project.getDocuments() != null) {
                for (Document doc : project.getDocuments()) {
                    v2.addDocumentsItem(doc);
                }
            }

            if (project.getTargets() != null) {
                for (Target target : project.getTargets()) {
                    v2.addTargetsItem(target);
                }
            }

            projectV2List.add(v2);
        }
        return projectV2List;
    }

    public void mergeAdditionalDetails(Project project, Project projectFromDb) {
        project.setAdditionalDetails(jsonMerge(objectMapper.valueToTree(projectFromDb.getAdditionalDetails()),
                objectMapper.valueToTree(project.getAdditionalDetails())));
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
}
