package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.ProjectStaff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrgUserRequest {
    @JsonProperty("RequestInfo")
    private @NotNull @Valid RequestInfo requestInfo = null;

    @JsonProperty("id")
    protected @Size(min = 2, max = 64) String id;

    @JsonProperty("userId")
    protected String userId;

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

    @JsonProperty("isDeleted")
    private Boolean isDeleted;
}
