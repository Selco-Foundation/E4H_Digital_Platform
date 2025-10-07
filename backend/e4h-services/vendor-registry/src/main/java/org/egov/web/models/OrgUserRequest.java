package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.ProjectStaff;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrgUserRequest {
    @JsonProperty("RequestInfo")
    private @NotNull @Valid RequestInfo requestInfo = null;

    @JsonProperty("OrgUser")
    private @NotNull @Valid @Size(min = 1)
    List<OrgUser> orgUsers = new ArrayList();
}
