package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgUserResponse {
    @JsonProperty("ResponseInfo")
    private @NotNull @Valid ResponseInfo responseInfo = null;

    @JsonProperty("OrgUsers")
    private @NotNull @Valid List<OrgUser> orgUsers = null;
}
