package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.ApiOperation;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityRequest {
    @JsonProperty("RequestInfo")
    private @NotNull @Valid RequestInfo requestInfo = null;

    @JsonProperty("ActivityFacilities")
    private @NotNull @Valid @Size(min = 1)
    List<ActivityFacility> activityFacilities = new ArrayList();
    
    @JsonProperty("apiOperation")
    private @Valid ApiOperation apiOperation = null;
}
