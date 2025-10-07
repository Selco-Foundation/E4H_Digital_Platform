package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.core.AdditionalFields;

import javax.validation.Valid;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrgUser {
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
    @JsonProperty("userId")
    private @NotNull @Size(
            min = 2,
            max = 64
    ) String userId = null;
    @JsonProperty("organizationId")
    private @NotNull @Size(
            min = 2,
            max = 64
    ) String organizationId = null;
    @JsonProperty("isDeleted")
    private Boolean isDeleted;
    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails = null;
}
