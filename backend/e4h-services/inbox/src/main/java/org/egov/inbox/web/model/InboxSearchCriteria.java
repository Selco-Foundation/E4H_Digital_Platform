package org.egov.inbox.web.model;

import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class InboxSearchCriteria {


    @NotNull
    @JsonProperty("tenantId")
    private String tenantId;

    @Valid
    @JsonProperty("processSearchCriteria")
    private ProcessInstanceSearchCriteria processSearchCriteria;
    
    @JsonProperty("moduleSearchCriteria")
    private HashMap<String,Object> moduleSearchCriteria;

    @JsonProperty("jurisdictionSearchCriteria")
    private HashMap<String,Object> jurisdictionSearchCriteria;

    /**
     * Per role, the statuses that role can act on. Derived by the server, never read from the wire.
     * <p>
     * {@code @JsonIgnore} is the point of it: paired with that role's own jurisdictions it decides
     * what the caller may see, so a client able to set it could award itself another role's work.
     * The field exists because {@link org.egov.inbox.service.V2.InboxServiceV2} already fetches the
     * business services needed to compute it, and the query builder runs three times per inbox load
     * - items, total count and status counts - which would otherwise mean three more workflow calls.
     * <p>
     * Null means "do not pair by status": the caller asked for everything in their jurisdiction
     * rather than the tickets waiting on them.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.Map<String, java.util.Set<String>> actionableStatusesByRole;

    /**
     * Statuses a pooled role can act on but which are kept out of its default "mine" bucket.
     *
     * On Hold tickets are parked rather than waiting on anyone, so they do not belong in the
     * default list - but they must still be counted, or the On Hold filter shows nothing until it
     * is selected. The count query clears this for the duration of its build, exactly as it does
     * the client's own status filter.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.Set<String> mineExcludedStatuses;

    /**
     * Asks for the tickets waiting on the caller rather than everything in their jurisdiction.
     * <p>
     * State SPOC and Tech POC tickets carry no assignee - im-services pools those states so that any
     * holder of the role can pick the ticket up - so "mine" cannot mean "assigned to my uuid" for them.
     * It means the ticket is in a state my roles can act on, within my jurisdiction. The backend
     * resolves that from the workflow config; the client only says which of the two views it wants.
     * <p>
     * Absent or false, the inbox returns everything the jurisdiction allows, in whatever states the
     * client asked for.
     */
    @JsonProperty("assignedToMe")
    private Boolean assignedToMe;
    
    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    @Max(value = 300)
    private Integer limit;
}
