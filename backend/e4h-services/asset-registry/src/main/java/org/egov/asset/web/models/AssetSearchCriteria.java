package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Criteria to search for assets. tenantId is mandatory. Send one of the rest
 */
@Schema(description = "Criteria to search for assets. tenantId is mandatory. Send one of the rest")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.673231117+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetSearchCriteria {
    @JsonProperty("tenantId")
    @NotNull
    private String tenantId = null;

    @JsonProperty("assetID")
    private String assetID = null;

    @JsonProperty("assetType")
    private List<String> assetType = null;

    @JsonProperty("facilityID")
    private String facilityID = null;

    @JsonProperty("isOperational")
    private boolean isOperational;

    @JsonProperty("activityFacilityID")
    private String activityFacilityID = null;

    @JsonProperty("serialNumber")
    private List<String> serialNumber = null;

    @JsonProperty("modelNumber")
    private String modelNumber = null;

    @JsonProperty("brandID")
    private String brandID = null;

    @JsonProperty("wfStatus")
    private String wfStatus = null;

}
