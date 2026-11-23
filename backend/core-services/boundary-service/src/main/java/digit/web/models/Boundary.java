package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Boundary
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-10-16T17:02:11.361704+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Boundary {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("code")
    @NotNull
    private String code = null;

    @JsonProperty("geometry")
    @Valid
    private JsonNode geometry = null;

    @JsonProperty("stateCode")
    private String stateCode = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;

    @JsonProperty("additionalDetails")
    private JsonNode additionalDetails = null;

    /*
     * Local Government Directory identity, denormalised down the hierarchy: a State fills
     * only lgdStateCode, a District fills state + district, a Block fills all three.
     *
     * These are what reconciliation matches on. A boundary code is derived from the name
     * (and some were hand-entered), so it cannot be compared against LGD; an LGD code
     * survives a rename, which lets a later import update the label rather than create a
     * second boundary.
     */

    @JsonProperty("lgdStateCode")
    private String lgdStateCode = null;

    @JsonProperty("lgdDistrictCode")
    private String lgdDistrictCode = null;

    /** Development Block or Sub-district code - lgdBlockTier says which. */
    @JsonProperty("lgdBlockCode")
    private String lgdBlockCode = null;

    /**
     * "Block", "Sub-district", or "Both" when the name exists at both tiers; "Panchayat" or
     * "Village" when the boundary sits below block level and lgdBlockCode is its container.
     */
    @JsonProperty("lgdBlockTier")
    private String lgdBlockTier = null;
}
