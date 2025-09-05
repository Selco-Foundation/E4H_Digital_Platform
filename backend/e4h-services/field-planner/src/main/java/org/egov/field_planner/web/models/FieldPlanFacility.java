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
import org.egov.common.models.core.AdditionalFields;
import org.egov.common.models.core.EgovModel;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldPlanFacility {

    @JsonProperty("facilityId")
    private @NotNull @Size(
            min = 2,
            max = 64
    ) String facilityId = null;
    @JsonProperty("fieldPlanId")
    private @NotNull @Size(
            min = 2,
            max = 64
    ) String fieldPlanId = null;
    @JsonProperty("isDeleted")
    private Boolean isDeleted;
    @JsonProperty("id")
    protected @Size(
            min = 2,
            max = 64
    ) String id;
    @JsonProperty("tenantId")
    protected @NotNull @Size(
            min = 2,
            max = 1000
    ) String tenantId;
    @JsonProperty("source")
    protected String source;
    @JsonProperty("rowVersion")
    protected Integer rowVersion;
    @JsonProperty("applicationId")
    protected String applicationId;
    @JsonProperty("hasErrors")
    protected Boolean hasErrors = Boolean.FALSE;
    @JsonProperty("additionalFields")
    protected @Valid AdditionalFields additionalFields;
    @JsonProperty("auditDetails")
    protected @Valid AuditDetails auditDetails;

    private static Boolean $default$hasErrors() {
        return Boolean.FALSE;
    }
}
