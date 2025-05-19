package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * FacilityCreateResponse
 */
@Validated
@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilityCreateResponse {
    @JsonProperty("ResponseInfo")
    @NotNull

    private Object responseInfo = null;

    @JsonProperty("facilities")

    private Object facilities = null;


}
