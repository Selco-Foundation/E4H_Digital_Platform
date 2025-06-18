package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.models.coremodels.AuditDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Asset
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.673231117+05:30[Asia/Kolkata]")
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
    @NotNull
    private String modelNumber = null;

    @JsonProperty("brandID")
    @NotNull
    private String brandID = null;

    @JsonProperty("assetDetails")
    private Map<String, Object> assetDetails = null;

    @JsonProperty("warrantyStartDate")
    @NotNull
    private Date warrantyStartDate = null;

    @JsonProperty("warrantyDuration")
    @NotNull
    private Integer warrantyDuration = null;

    @JsonProperty("warrantyEndDate")
    @NotNull
    private Date warrantyEndDate = null;

    @JsonProperty("wfStatus")
    private String wfStatus = null;

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonProperty("documents")
    private List<Document> documents = null;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;

    @JsonProperty("additionalDetails")
    private Map<String, Object> additionalDetails = null;


}
