package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetSearchCriteria {

    @JsonProperty("tenantId")
    @NotNull
    private String tenantId = null;

    @JsonProperty("facilityID")
    private String facilityID = null;

    @JsonProperty("activityFacilityID")
    private String activityFacilityID = null;

}
