package org.egov.im.web.models.vendor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Search criteria of vendor-registry's organisation-user search. Field names are deliberately
 * singular while the JSON names are plural - that is the contract vendor-registry exposes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgUserSearchCriteria {

    @JsonProperty("userIds")
    private List<String> userId = null;

    @JsonProperty("organizationIds")
    private List<String> organizationId = null;

    @JsonProperty("tenantId")
    private String tenantId = null;
}
