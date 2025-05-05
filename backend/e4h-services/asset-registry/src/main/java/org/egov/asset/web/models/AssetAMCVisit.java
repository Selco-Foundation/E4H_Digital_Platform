package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * Description of an AMC visit
 */
@Schema(description = "Description of an AMC visit")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.673231117+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetAMCVisit {
    @JsonProperty("tenant_id")

    private Object tenantId = null;

    @JsonProperty("visitId")

    private Object visitId = null;

    @JsonProperty("assetId")

    private Object assetId = null;

    @JsonProperty("facilityId")

    private Object facilityId = null;

    @JsonProperty("scheduledDate")

    private Object scheduledDate = null;

    @JsonProperty("visitDate")

    private Object visitDate = null;

    @JsonProperty("engineerName")

    private Object engineerName = null;

    @JsonProperty("observations")

    private Object observations = null;

    @JsonProperty("nextDueDate")

    private Object nextDueDate = null;

    @JsonProperty("visitStatus")

    private Object visitStatus = null;

    @JsonProperty("additionalDetails")

    private Object additionalDetails = null;


}
