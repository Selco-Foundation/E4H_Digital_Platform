package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One change of an organisation's PoC: who changed it, when, and from what to what.
 * Persisted to eg_org_poc_change_audit. Phone numbers are stored encrypted, as on eg_org.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PocChangeAudit {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("organisationId")
    private String organisationId;

    @JsonProperty("oldPocName")
    private String oldPocName;

    @JsonProperty("oldPocPhone")
    private String oldPocPhone;

    @JsonProperty("oldPocEmail")
    private String oldPocEmail;

    @JsonProperty("oldPocUsername")
    private String oldPocUsername;

    @JsonProperty("newPocName")
    private String newPocName;

    @JsonProperty("newPocPhone")
    private String newPocPhone;

    @JsonProperty("newPocEmail")
    private String newPocEmail;

    @JsonProperty("newPocUsername")
    private String newPocUsername;

    /** The user who held the PoC admin role before; null when there was none. */
    @JsonProperty("previousUserId")
    private String previousUserId;

    @JsonProperty("newUserId")
    private String newUserId;

    @JsonProperty("changedBy")
    private String changedBy;

    @JsonProperty("changedTime")
    private Long changedTime;
}
