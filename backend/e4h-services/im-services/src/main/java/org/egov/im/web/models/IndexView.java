package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @JsonProperty("warrantyStatus")
    private String warrantyStatus;

    @JsonProperty("warrantyStatus_localized")
    private String warrantyStatusLocalized;

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

    /*
     * SLA anchors written on every state change. Readers (inbox, escalation, Kibana) derive the live
     * remaining values from these and the current time, so nothing has to refresh the index hourly.
     * The *DueAt / base fields are only set while the corresponding SLA clock is running.
     */

    /** When the ticket entered its current workflow state (epoch millis). */
    @JsonProperty("stateEnteredTime")
    private Long stateEnteredTime;

    /** Total SLA remaining at {@link #stateEnteredTime}; live value = this - business time elapsed since. */
    @JsonProperty("totalSlaBase")
    private Long totalSlaBase;

    /** Business-hours deadline for the current state's SLA. */
    @JsonProperty("stateSlaDueAt")
    private Long stateSlaDueAt;

    /** Point after which the ticket counts as nearing its state SLA. */
    @JsonProperty("stateSlaWarnAt")
    private Long stateSlaWarnAt;

    /** Business-hours deadline for the ticket's total SLA. */
    @JsonProperty("totalSlaDueAt")
    private Long totalSlaDueAt;

    @JsonProperty("resolvedTimestamp")
    private Long resolvedTimestamp;

    @JsonProperty("declinedTimestamp")
    private Long declinedTimestamp;

    @JsonProperty("comments")
    private String comments;

    @JsonProperty("sendBackReason")
    private String sendBackReason;

    @JsonProperty("sendBackSubReason")
    private String sendBackSubReason;

    @JsonProperty("latestRejectReason")
    private String latestRejectReason;

    @JsonProperty("boundary")
    private Boundary boundary;

    /**
     * Program role the ticket is currently waiting on, resolved from the roles allowed to act on its
     * current workflow state. Null once the ticket reaches a terminal state.
     * @see org.egov.im.service.CurrentOwnerService
     */
    @JsonProperty("currentOwner")
    private String currentOwner;

    /** Workflow system role {@link #currentOwner} was derived from. */
    @JsonProperty("currentOwnerSystemRole")
    private String currentOwnerSystemRole;
}
