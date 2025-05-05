package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.models.coremodels.AuditDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
    @JsonProperty("tenant_id")

    private Object tenantId = null;

    @JsonProperty("assetID")

    private Object assetID = null;

    @JsonProperty("facilityID")
    @NotNull

    private Object facilityID = null;

    @JsonProperty("assetTypeID")
    @NotNull

    private Object assetTypeID = null;

    @JsonProperty("serialNumber")
    @NotNull

    private Object serialNumber = null;

    @JsonProperty("modelNumber")
    @NotNull

    private Object modelNumber = null;

    @JsonProperty("brandID")
    @NotNull

    private Object brandID = null;

    @JsonProperty("assetDetails")

    private Object assetDetails = null;

    @JsonProperty("warrantyStartDate")
    @NotNull

    private Object warrantyStartDate = null;

    @JsonProperty("warrantyDuration")
    @NotNull

    private Object warrantyDuration = null;

    @JsonProperty("warrantyEndDate")
    @NotNull

    private Object warrantyEndDate = null;

    @JsonProperty("wfStatus")

    private Object wfStatus = null;

    @JsonProperty("isActive")

    private Object isActive = null;

    @JsonProperty("documents")

    private Object documents = null;

    @JsonProperty("auditDetails")

    @Valid
    private AuditDetails auditDetails = null;

    @JsonProperty("additionalDetails")

    private Object additionalDetails = null;


}
