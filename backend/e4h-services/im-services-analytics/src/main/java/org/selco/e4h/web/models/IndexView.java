package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndexView {

    @JsonProperty("incidentType_localized")
    private String incidentTypeLocalized;

    @JsonProperty("incidentSubType_localized")
    private String incidentSubTypeLocalized;

    @JsonProperty("applicationStatus_localized")
    private String applicationStatusLocalized;

    @JsonProperty("phcSubType_localized")
    private String phcSubTypeLocalized;

    @JsonProperty("tenantId_localized")
    private String tenantIdLocalized;

    @JsonProperty("state")
    private String state;

    @JsonProperty("nin_hfr_id")
    private String ninHfrId;

    @JsonProperty("mappedVendorUserName")
    private String mappedVendorUserName;

    @JsonProperty("mappedVendorName")
    private String mappedVendorName;

    @JsonProperty("lastActionTakenBy")
    private String lastActionTakenBy;

    @JsonProperty("startingStatus")
    private String startingStatus;

    @JsonProperty("endingStatus")
    private String endingStatus;

    @JsonProperty("startingStatus_localized")
    private String startingStatusLocalized;

    @JsonProperty("endingStatus_localized")
    private String endingStatusLocalized;

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("documentUrls")
    private String documentUrls ;

    @JsonProperty("definedTotalSla")
    private Long definedTotalSla;

    @JsonProperty("comments")
    private String comments;

    @JsonProperty("sendBackReason")
    private String sendBackReason;

    @JsonProperty("sendBackSubReason")
    private String sendBackSubReason;

    /**
     * Program role the ticket is currently waiting on, derived by im-services from the roles allowed
     * to act on its current workflow state. Null once the ticket reaches a terminal state.
     */
    @JsonProperty("currentOwner")
    private String currentOwner;

    /** Workflow system role {@link #currentOwner} was derived from. */
    @JsonProperty("currentOwnerSystemRole")
    private String currentOwnerSystemRole;

}
