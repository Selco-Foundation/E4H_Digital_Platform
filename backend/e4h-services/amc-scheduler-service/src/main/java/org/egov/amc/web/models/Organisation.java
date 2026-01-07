package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.Address;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Organisation registry
 */
@ApiModel(description = "Organisation registry")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-02-15T14:49:42.141+05:30")

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Organisation {

    @JsonProperty("id")
    @Valid
    private String id = null;

    @JsonProperty("tenantId")
    @NotNull
    @Size(min = 2, max = 64)
    private String tenantId = null;

    @JsonProperty("name")
    @NotNull
    @Size(min = 2, max = 128)
    private String name = null;

    @JsonProperty("code")
    @Size(min = 2, max = 128)
    private String code = null;//idgen formatted code from start of the org creation request

    @JsonProperty("applicationNumber")
    private String applicationNumber = null;//idgen formatted number from start of the org creation request

    //Decided on 14th March : As of now, orgnumber will be generated from start of create org registry
    @JsonProperty("orgNumber")
    private String orgNumber = null;//idgen formatted number once workflow is 'APPROVED'

    @JsonProperty("orgStatus")
    private ApplicationStatus orgStatus = null;

    @JsonProperty("orgType")
    private String orgType = null;

    @JsonProperty("orgSubType")
    private String orgSubType = null;

    @JsonProperty("orgPocName")
    private String orgPocName = null;

    @JsonProperty("orgPocPhone")
    private String orgPocPhone = null;

    @JsonProperty("orgPocEmail")
    private String orgPocEmail = null;

    @JsonProperty("orgPocUsername")
    private String orgPocUsername = null;

    @JsonProperty("externalRefNumber")
    @Size(min = 2, max = 64)
    private String externalRefNumber = null;

    @JsonProperty("dateOfIncorporation")
    private BigDecimal dateOfIncorporation = null;

    @JsonProperty("orgAddress")
    @Valid
    private List<Address> orgAddress = null;//no

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonProperty("documents")
    @Valid
    private List<Document> documents = null;//upsert

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;

    public Organisation addOrgAddressItem(Address orgAddressItem) {
        if (this.orgAddress == null) {
            this.orgAddress = new ArrayList<>();
        }
        this.orgAddress.add(orgAddressItem);
        return this;
    }

    public Organisation addDocumentsItem(Document documentsItem) {
        if (this.documents == null) {
            this.documents = new ArrayList<>();
        }
        this.documents.add(documentsItem);
        return this;
    }

}

enum ApplicationStatus {
    DEBARRED("DEBARRED"),

    ACTIVE("ACTIVE"),

    INACTIVE("INACTIVE");

    private String value;

    ApplicationStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ApplicationStatus fromValue(String text) {
        for (ApplicationStatus b : ApplicationStatus.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

}
