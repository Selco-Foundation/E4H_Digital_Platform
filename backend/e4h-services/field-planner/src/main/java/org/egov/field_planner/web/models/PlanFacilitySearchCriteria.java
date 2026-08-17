package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
<<<<<<<< HEAD:backend/e4h-services/field-planner/src/main/java/org/egov/field_planner/web/models/PlanFacilitySearchCriteria.java
========
import jakarta.validation.Valid;
>>>>>>>> 491986f1a (added backend flow for assessment module (#2868) (#2927)):backend/e4h-services/field-planner/src/main/java/org/egov/field_planner/web/models/PlanFacilitySearchRequest.java
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
<<<<<<<< HEAD:backend/e4h-services/field-planner/src/main/java/org/egov/field_planner/web/models/PlanFacilitySearchCriteria.java
========
import org.egov.common.contract.request.RequestInfo;
>>>>>>>> 491986f1a (added backend flow for assessment module (#2868) (#2927)):backend/e4h-services/field-planner/src/main/java/org/egov/field_planner/web/models/PlanFacilitySearchRequest.java

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFacilitySearchCriteria {

    @JsonProperty("criteria")
    @NotNull
    @Valid
    private PlanFacilitySearchCriteria criteria;
}
