package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Address;
import org.egov.common.models.project.Document;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates all parameters for building a project search query.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectV2{

    @JsonProperty("id")
    private String id = null;
    @JsonProperty("tenantId")
    private String tenantId = null;
    @JsonProperty("projectNumber")
    private String projectNumber = null;
    @JsonProperty("name")
    private String name = null;
    @JsonProperty("projectType")
    private String projectType = null;
    @JsonProperty("projectSubType")
    private String projectSubType = null;
    @JsonProperty("department")
    private String department = null;
    @JsonProperty("description")
    private String description = null;
    @JsonProperty("referenceID")
    private String referenceID = null;
    @JsonProperty("projectTypeId")
    private String projectTypeId = null;
    @JsonProperty("documents")
    private @Valid List<Document> documents = null;
    @JsonProperty("address")
    private Address address = null;
    @JsonProperty("startDate")
    private Long startDate = null;
    @JsonProperty("endDate")
    private Long endDate = null;
    @JsonProperty("isTaskEnabled")
    private Boolean isTaskEnabled = false;
    @JsonProperty("parent")
    private String parent = null;
    @JsonProperty("projectHierarchy")
    private String projectHierarchy = null;
    @JsonProperty("natureOfWork")
    private String natureOfWork = null;
    @JsonProperty("ancestors")
    private List<Project> ancestors = null;
    @JsonProperty("descendants")
    private List<Project> descendants = null;
    @JsonProperty("targets")
    private @Valid List<Target> targets = null;
    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;
    @JsonProperty("isDeleted")
    private Boolean isDeleted = false;
    @JsonProperty("rowVersion")
    private Integer rowVersion = null;
    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;
    private long countFacilities;

    public ProjectV2 addDocumentsItem(Document documentsItem) {
        if (this.documents == null) {
            this.documents = new ArrayList();
        }

        this.documents.add(documentsItem);
        return this;
    }

    public ProjectV2 addTargetsItem(Target targetsItem) {
        if (this.targets == null) {
            this.targets = new ArrayList();
        }

        this.targets.add(targetsItem);
        return this;
    }

    public ProjectV2 addDescendant(Project project) {
        if (this.descendants == null) {
            this.descendants = new ArrayList();
        }

        this.descendants.add(project);
        return this;
    }
}
