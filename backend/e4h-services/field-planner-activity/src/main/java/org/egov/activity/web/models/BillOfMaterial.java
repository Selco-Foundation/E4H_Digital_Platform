package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Document;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * Facility
 */
@Validated
@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillOfMaterial {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("facilityId")
    private String facilityId = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("assignUser")
    private String assignUser = null;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails = null;

    @JsonProperty("documents")
    private @Valid List<Document> documents = null;

    @JsonProperty("data")
    private Map<String, Object> data = null;

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonProperty("auditDetails")
    protected @Valid AuditDetails auditDetails;
}
