package org.egov.activity.web.models;

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
    @JsonProperty("isdeleted")
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
    @JsonProperty("additionalFields")
    protected @Valid AdditionalFields additionalFields;
    @JsonProperty("auditDetails")
    protected @Valid AuditDetails auditDetails;
    @JsonProperty("rowVersion")
    protected Integer rowVersion;
    @JsonProperty("hasErrors")
    protected Boolean hasErrors;

}
