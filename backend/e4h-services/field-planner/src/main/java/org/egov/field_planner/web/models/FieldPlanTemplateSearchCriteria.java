package org.egov.field_planner.web.models;

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
public class FieldPlanTemplateSearchCriteria {

    @JsonProperty("id")
    private @Valid List<String> ids;

    @JsonProperty("fieldPlanId")
    private List<String> fieldPlanId;

    @JsonProperty("systemType")
    private List<String> systemType;

    @JsonProperty("totalCapacity")
    private List<String> totalCapacity;

    private String tenantId;

    private boolean countQuery;
}
