package org.egov.im.web.models.vendor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One membership of a user in a vendor organisation, as returned by vendor-registry. Only the fields
 * this module needs are mapped; the rest of the payload (user, auditDetails, additionalDetails, ...)
 * is ignored.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgUser {

    @JsonProperty("userId")
    private String userId = null;

    @JsonProperty("organizationId")
    private String organizationId = null;

    @JsonProperty("isDeleted")
    private Boolean isDeleted = null;
}
