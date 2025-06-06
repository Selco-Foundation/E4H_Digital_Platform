package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.Date;
import java.util.Map;

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
    @JsonProperty("tenantId")

    private String tenantId = null;

    @JsonProperty("visitId")

    private String visitId = null;

    @JsonProperty("assetId")

    private String assetId = null;

    @JsonProperty("facilityId")

    private String facilityId = null;

    @JsonProperty("scheduledDate")
    private Date scheduledDate = null;

    @JsonProperty("visitDate")

    private Date visitDate = null;

    @JsonProperty("engineerName")

    private String engineerName = null;

    @JsonProperty("observations")

    private String observations = null;

    @JsonProperty("nextDueDate")

    private Date nextDueDate = null;

    @JsonProperty("visitStatus")

    private String visitStatus = null;

    @JsonProperty("additionalDetails")

    private Map<String,Object> additionalDetails = null;


}
