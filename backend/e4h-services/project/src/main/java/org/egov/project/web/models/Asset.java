package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Asset
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Asset {
    @JsonProperty("assetId")
    private String assetId = null;

    @JsonProperty("tenantId")
    @NotNull
    private String tenantId = null;

    @JsonProperty("system")
    @NotNull
    private String system = null;

    @JsonProperty("facilityID")
    @NotNull
    private String facilityID = null;

    @JsonProperty("assetTypeID")
    @NotNull
    private String assetTypeID = null;

    @JsonProperty("serialNumber")
    @NotNull
    private String serialNumber = null;

    @JsonProperty("modelNumber")
    private String modelNumber = null;

    @JsonProperty("brandID")
    @NotNull
    private String brandID = null;

    @JsonProperty("assetDetails")
    private Map<String, Object> assetDetails = null;

    @JsonProperty("warrantyStartDate")
    private Date warrantyStartDate = null;

    @JsonProperty("warrantyDuration")
    private Integer warrantyDuration = null;

    @JsonProperty("warrantyEndDate")
    @NotNull
    private Date warrantyEndDate = null;

    @JsonProperty("wfStatus")
    private String wfStatus = null;

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonProperty("isOperational")
    private Boolean isOperational = null;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails = null;


}
