package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.User;
import org.egov.common.contract.workflow.Action;
import org.egov.common.contract.workflow.State;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessInstance {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("businessService")
    private String businessService;

    @JsonProperty("businessId")
    private String businessId;

    @JsonProperty("action")
    private String action;

    @JsonProperty("moduleName")
    private String moduleName;

    @JsonProperty("state")
    private State state;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("assigner")
    private User assigner;

    @JsonProperty("assignes")
    private List<User> assignes;

    @JsonProperty("nextActions")
    private List<Action> nextActions;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
