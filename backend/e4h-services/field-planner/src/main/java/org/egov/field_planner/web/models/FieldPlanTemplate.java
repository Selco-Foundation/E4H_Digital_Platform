package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldPlanTemplate {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    @NotNull
    @Size(min = 2, max = 1000)
    private String tenantId;

    @JsonProperty("fieldPlanId")
    @NotNull
    @Size(min = 2, max = 64)
    private String fieldPlanId;

    @JsonProperty("systemType")
    @NotNull
    @Size(min = 1, max = 255)
    private String systemType;

    @JsonProperty("totalCapacity")
    @NotNull
    @Size(min = 1, max = 255)
    private String totalCapacity;

    @JsonProperty("templateData")
    private Map<String, Object> templateData;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails;
}
