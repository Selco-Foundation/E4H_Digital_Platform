package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * organisation search attributes
 */
@ApiModel(description = "organisation search attributes")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-02-15T14:49:42.141+05:30")

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgSearchCriteria {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("ids")
    private List<String> ids = null;

    @JsonProperty("tenantId")
    @Size(min = 2, max = 64)
    @NotNull
    private String tenantId = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("code")
    private String code = null;

    @JsonProperty("applicationNumber")
    private String applicationNumber = null;

    @JsonProperty("orgNumber")
    private String orgNumber = null;

    @JsonProperty("orgStatus")
    private String orgStatus = null;

    @JsonProperty("orgType")
    private String orgType = null;

    @JsonProperty("orgSubType")
    private String orgSubType = null;

    @JsonProperty("orgPocPhone")
    private String orgPocPhone = null;

    @JsonProperty("orgPocName")
    private String orgPocName = null;

    @JsonProperty("functions")
    private Function functions = null;

    @JsonProperty("createdFrom")
    private Long createdFrom = null;

    @JsonProperty("createdTo")
    private Long createdTo = null;

    @JsonProperty("boundaryCode")
    private String boundaryCode = null;

    @JsonProperty("identifierType")
    private String identifierType = null;

    @JsonProperty("identifierValue")
    private String identifierValue = null;

    @JsonProperty("includeDeleted")
    private Boolean includeDeleted = false;

    public OrgSearchCriteria addIdItem(String idItem) {
        if (this.ids == null) {
            this.ids = new ArrayList<>();
        }
        this.ids.add(idItem);
        return this;
    }

}
