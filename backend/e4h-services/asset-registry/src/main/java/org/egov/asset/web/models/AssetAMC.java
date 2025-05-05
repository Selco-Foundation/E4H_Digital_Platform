package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.models.coremodels.AuditDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

/**
 * AssetAMC
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.673231117+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetAMC {
    @JsonProperty("tenant_id")

    private Object tenantId = null;

    @JsonProperty("amcID")

    private Object amcID = null;

    @JsonProperty("assetID")

    private Object assetID = null;

    @JsonProperty("contractNumber")

    private Object contractNumber = null;

    @JsonProperty("vendorCode")

    private Object vendorCode = null;

    @JsonProperty("contractStartDate")

    private Object contractStartDate = null;

    @JsonProperty("contractEndDate")

    private Object contractEndDate = null;

    @JsonProperty("visitSchedule")

    private Object visitSchedule = null;

    @JsonProperty("visits")

    private Object visits = null;

    @JsonProperty("documents")

    private Object documents = null;

    @JsonProperty("auditDetails")

    @Valid
    private AuditDetails auditDetails = null;

    @JsonProperty("additionalDetails")

    private Object additionalDetails = null;


}
