package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * FacilityAssessment
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilityAssessment {
    @JsonProperty("tenant_id")

    private Object tenantId = null;

    @JsonProperty("assessment_id")

    private Object assessmentId = null;

    @JsonProperty("facility_id")

    private Object facilityId = null;

    @JsonProperty("rowVersion")

    private Object rowVersion = null;

    @JsonProperty("assessment_type")

    private Object assessmentType = null;

    @JsonProperty("date_of_assessment")

    private Object dateOfAssessment = null;

    @JsonProperty("assessed_by")

    private Object assessedBy = null;

    @JsonProperty("final_result")

    private Object finalResult = null;

    @JsonProperty("isActive")

    private Object isActive = null;


}
