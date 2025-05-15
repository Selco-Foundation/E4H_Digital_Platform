package facility_v2.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;

/**
 * FacilityAssessmentCreateRequest
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilityAssessmentCreateRequest {
    @JsonProperty("RequestInfo")
    @NotNull

    private Object requestInfo = null;

    @JsonProperty("assessments")
    @NotNull

    private Object assessments = null;


}
