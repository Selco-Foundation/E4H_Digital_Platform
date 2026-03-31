package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldPlanFacilitySearch {
    @JsonProperty("id")
    private @Valid List<String> id = null;

    @JsonProperty("facilityId")
    private List<String> facility_id = null;

    @JsonProperty("fieldPlanId")
    private List<String> field_plan_id = null;
}
