package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.models.project.ProjectSearch;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilityBulkFilter {

    @JsonProperty("searchCriteria")
    private ActivityFacilitySearchCriteria searchCriteria;
}
