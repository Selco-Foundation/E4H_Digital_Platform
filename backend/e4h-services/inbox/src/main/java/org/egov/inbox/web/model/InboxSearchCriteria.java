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
