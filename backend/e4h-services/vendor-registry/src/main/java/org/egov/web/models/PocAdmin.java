package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The admin user built from an organisation's PoC details, as reported back to the caller.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PocAdmin {

    @JsonProperty("organisationId")
    private String organisationId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userName")
    private String userName;

    /** VENDOR_ADMIN for a vendor org, ORG_ADMIN for a platform org. */
    @JsonProperty("role")
    private String role;

    /** True when the user was newly created for this PoC; false when an existing user was pointed to. */
    @JsonProperty("created")
    private Boolean created;

    /**
     * True when the login-details email was queued, false when it could not be sent. Absent when
     * no password was set, so there was nothing to email.
     */
    @JsonProperty("credentialsEmailSent")
    private Boolean credentialsEmailSent;
}
