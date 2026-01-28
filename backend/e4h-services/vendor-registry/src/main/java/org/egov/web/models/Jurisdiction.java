package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@ApiModel(description = "Organisation Jurisdiction")
@Validated

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Jurisdiction {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("orgId")
    private String orgId = null;

    @JsonProperty("code")
    private String code = null;

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;

    @Size(min=2, max=100)
    private String hierarchy;

    @Size(min=2, max=100)
    private String boundary;

    @Size(max=256)
    private String boundaryType;

    private String tenantId;

    private AuditDetails auditDetails;

    private Boolean isActive;
}
