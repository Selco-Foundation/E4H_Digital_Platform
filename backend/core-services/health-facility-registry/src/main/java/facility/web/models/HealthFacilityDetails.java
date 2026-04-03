package facility.web.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

/**
 * These are health facility specific attributes. This needs to be converted into a JSON schema and added to MDMS. All facilities of type \&quot;Health\&quot; to be verified against this schema.
 */
@Schema(description = "These are health facility specific attributes. This needs to be converted into a JSON schema and added to MDMS. All facilities of type \"Health\" to be verified against this schema.")
@Validated
@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthFacilityDetails {

    @Size(min = 10, max = 10)
    @JsonProperty("hfr_id")
    private String hfrId;

    @Size(min = 12, max = 12)
    @JsonProperty("nin_id")
    private String ninId;

    @JsonProperty("solar_solution_design_type")
    @NotNull(message = "solar_solution_design_type is required")
    private SolarSolutionDesignType solarSolutionDesignType;

    @JsonProperty("pocContact")
    @Pattern(
            regexp = "^\\d{10}$",
            message = "PoC contact number must be a valid 10-digit Indian mobile number"
    )
    private String pocContact;

    @JsonProperty("pocName")
    @Pattern(
            regexp = "^[a-zA-Z\\s]+$",
            message = "POC Name pattern is incorrect"
    )
    private String pocName;

    @JsonProperty("pocEmail")
    private String pocEmail;

    @JsonProperty("pocDesignation")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "POC Designation pattern is incorrect"
    )
    private String pocDesignation;
}
