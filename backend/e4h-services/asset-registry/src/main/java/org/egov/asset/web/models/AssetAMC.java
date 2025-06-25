package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.models.coremodels.AuditDetails;
import digit.models.coremodels.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    @JsonProperty("tenantId")

    private String tenantId = null;

    @JsonProperty("amcID")

    private String amcID = null;

    @JsonProperty("assetID")

    private String assetID = null;

    @JsonProperty("contractNumber")

    private String contractNumber = null;

    @JsonProperty("vendorCode")

    private String vendorCode = null;

    @JsonProperty("contractStartDate")

    private Date contractStartDate = null;

    @JsonProperty("contractEndDate")

    private Date contractEndDate = null;

    @JsonProperty("visitSchedule")

    private Object visitSchedule = null;

    @JsonProperty("visits")
    private List<AssetAMCVisit> visits = null;

    @JsonProperty("documents")

    private List<Document> documents = null;

    @JsonProperty("auditDetails")

    @Valid
    private AuditDetails auditDetails = null;

    @JsonProperty("additionalDetails")

    private Map<String, Object> additionalDetails = null;


}
