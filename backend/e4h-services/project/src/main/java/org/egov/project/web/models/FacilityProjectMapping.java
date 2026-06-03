package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityProjectMapping {

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("projectId")
    private String projectId;

    @JsonProperty("projectName")
    private String projectName;
}
