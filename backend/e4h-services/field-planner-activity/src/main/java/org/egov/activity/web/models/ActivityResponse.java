package org.egov.activity.web.models;

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
public class ActivityResponse {
    @JsonProperty("ResponseInfo")
    private @NotNull @Valid ResponseInfo responseInfo = null;

    @JsonProperty("Activities")
    private @NotNull @Valid List<Activity> activities = null;

    @JsonProperty("TotalCount")
    private Integer totalCount = 0;
}
