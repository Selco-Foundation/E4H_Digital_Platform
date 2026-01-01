package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgUserResponse {
    @JsonProperty("ResponseInfo")
    private @NotNull @Valid ResponseInfo responseInfo = null;

    @JsonProperty("id")
    protected @Size(min = 2, max = 64) String id;

    @JsonProperty("user")
    @NotNull
    private User user = null;

    @JsonProperty("organizationId")
    @NotNull
    private @NotNull @Size(
            min = 2,
            max = 64
    ) String organizationId = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails = null;
}
