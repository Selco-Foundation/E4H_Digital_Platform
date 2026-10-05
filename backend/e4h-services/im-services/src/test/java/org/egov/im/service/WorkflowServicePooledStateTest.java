package org.egov.im.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.util.MDMSUtils;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.Workflow;
import org.egov.im.web.models.workflow.Action;
import org.egov.im.web.models.workflow.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.egov.im.util.IMConstants.APPROVE_ACTION;
import static org.egov.im.util.IMConstants.MARK_OUT_OF_SCOPE_ACTION;
import static org.egov.im.util.IMConstants.ROLE_COMPLAINT_ASSESSOR;
import static org.egov.im.util.IMConstants.ROLE_COMPLAINT_FACILITATOR_1;
import static org.egov.im.util.IMConstants.ROLE_COMPLAINT_RESOLVER;
import static org.egov.im.util.IMConstants.ROLE_SYSTEM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Covers the decision to leave a ticket unassigned when it lands in a state owned by a pooled role.
 * <p>
 * The business service is stubbed out by hand rather than fetched: the decision is read off the state
 * definitions, so those definitions are the input under test.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowServicePooledStateTest {

    private static final String TENANT_ID = "in";
    private static final String INCIDENT_ID = "IM-2026-000001";
    private static final String SPOC_USER = "uuid-spoc-1";
    private static final String VENDOR_USER = "uuid-vendor-1";

    private static final String PENDINGRESOLUTION = "PENDINGRESOLUTION";
    private static final String OUT_OF_SCOPE = "OUT_OF_SCOPE";
    private static final String ASSIGN = "ASSIGN";

    private static final String UUID_PENDINGRESOLUTION = "state-pendingresolution";
    private static final String UUID_OUT_OF_SCOPE = "state-out-of-scope";
    private static final String UUID_PENDING_RESOLUTION_OUT_OF_SCOPE = "state-pending-resolution-out-of-scope";
    private static final String UUID_SHARED = "state-shared";

    @Mock
    private IMConfiguration imConfiguration;
    @Mock
    private ServiceRequestRepository repository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MDMSUtils mdmsUtils;
    @Mock
    private SLAService slaService;

    private WorkflowService workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new WorkflowService(imConfiguration, repository, new ObjectMapper(), notificationService,
                mdmsUtils, slaService);
    }

    @Test
    void leavesTheTicketUnassignedWhenItLandsOnTheStateSpoc() {
        IncidentRequest request = request(PENDINGRESOLUTION, MARK_OUT_OF_SCOPE_ACTION, List.of(SPOC_USER));

        workflowService.clearAssigneesForPooledState(request, MARK_OUT_OF_SCOPE_ACTION, states());

        assertNull(request.getWorkflow().getAssignes());
    }

    @Test
    void ignoresTheMachineRolesOnThePooledState() {
        // OUT_OF_SCOPE also carries an AUTO_ESCALATE action with the SYSTEM role; it must not make the
        // state look like it is waiting on someone other than the SPOC.
        State outOfScope = findByUuid(UUID_OUT_OF_SCOPE);
        assertEquals(2, outOfScope.getActions().size());

        IncidentRequest request = request(PENDINGRESOLUTION, MARK_OUT_OF_SCOPE_ACTION, List.of(SPOC_USER));
        workflowService.clearAssigneesForPooledState(request, MARK_OUT_OF_SCOPE_ACTION, states());

        assertNull(request.getWorkflow().getAssignes());
    }

    @Test
    void keepsTheAssigneeWhenTheTicketLandsWithAVendor() {
        IncidentRequest request = request(OUT_OF_SCOPE, ASSIGN, List.of(VENDOR_USER));

        workflowService.clearAssigneesForPooledState(request, ASSIGN, states());

        assertEquals(List.of(VENDOR_USER), request.getWorkflow().getAssignes());
    }

    @Test
    void keepsTheAssigneeWhenTheStateIsSharedWithANonPooledRole() {
        IncidentRequest request = request(PENDINGRESOLUTION, APPROVE_ACTION, List.of(SPOC_USER));

        workflowService.clearAssigneesForPooledState(request, APPROVE_ACTION, states());

        assertEquals(List.of(SPOC_USER), request.getWorkflow().getAssignes());
    }

    @Test
    void keepsTheAssigneeWhenTheResultantStateCannotBeResolved() {
        IncidentRequest request = request(PENDINGRESOLUTION, "UNKNOWN_ACTION", List.of(SPOC_USER));

        workflowService.clearAssigneesForPooledState(request, "UNKNOWN_ACTION", states());

        assertEquals(List.of(SPOC_USER), request.getWorkflow().getAssignes());
    }

    @Test
    void keepsTheAssigneeWhenThereIsNoBusinessServiceDefinition() {
        IncidentRequest request = request(PENDINGRESOLUTION, MARK_OUT_OF_SCOPE_ACTION, List.of(SPOC_USER));

        workflowService.clearAssigneesForPooledState(request, MARK_OUT_OF_SCOPE_ACTION, Collections.emptyList());

        assertEquals(List.of(SPOC_USER), request.getWorkflow().getAssignes());
    }

    /**
     * A cut of the Incident business service: PENDINGRESOLUTION hands the ticket to the SPOC on
     * MARK_OUT_OF_SCOPE and to a state shared by the SPOC and the CRM on APPROVE; OUT_OF_SCOPE hands it
     * back to a vendor on ASSIGN.
     */
    private List<State> states() {
        State pendingResolution = State.builder()
                .uuid(UUID_PENDINGRESOLUTION)
                .state(PENDINGRESOLUTION)
                .applicationStatus(PENDINGRESOLUTION)
                .actions(Arrays.asList(
                        action(MARK_OUT_OF_SCOPE_ACTION, UUID_PENDINGRESOLUTION, UUID_OUT_OF_SCOPE,
                                List.of(ROLE_COMPLAINT_RESOLVER)),
                        action(APPROVE_ACTION, UUID_PENDINGRESOLUTION, UUID_SHARED,
                                List.of(ROLE_COMPLAINT_RESOLVER))))
                .build();

        State outOfScope = State.builder()
                .uuid(UUID_OUT_OF_SCOPE)
                .state(OUT_OF_SCOPE)
                .applicationStatus(OUT_OF_SCOPE)
                .actions(Arrays.asList(
                        action(ASSIGN, UUID_OUT_OF_SCOPE, UUID_PENDING_RESOLUTION_OUT_OF_SCOPE,
                                List.of(ROLE_COMPLAINT_FACILITATOR_1)),
                        action("AUTO_ESCALATE", UUID_OUT_OF_SCOPE, UUID_PENDING_RESOLUTION_OUT_OF_SCOPE,
                                List.of(ROLE_SYSTEM))))
                .build();

        State pendingResolutionOutOfScope = State.builder()
                .uuid(UUID_PENDING_RESOLUTION_OUT_OF_SCOPE)
                .state("PENDING_RESOLUTION_OUT_OF_SCOPE")
                .applicationStatus("PENDING_RESOLUTION_OUT_OF_SCOPE")
                .actions(List.of(action("RESOLVE", UUID_PENDING_RESOLUTION_OUT_OF_SCOPE, UUID_PENDINGRESOLUTION,
                        List.of(ROLE_COMPLAINT_RESOLVER))))
                .build();

        State shared = State.builder()
                .uuid(UUID_SHARED)
                .state("SHARED_STATE")
                .applicationStatus("SHARED_STATE")
                .actions(List.of(action("SUBMIT", UUID_SHARED, UUID_PENDINGRESOLUTION,
                        Arrays.asList(ROLE_COMPLAINT_FACILITATOR_1, ROLE_COMPLAINT_ASSESSOR))))
                .build();

        return Arrays.asList(pendingResolution, outOfScope, pendingResolutionOutOfScope, shared);
    }

    private State findByUuid(String uuid) {
        return states().stream().filter(state -> uuid.equals(state.getUuid())).findFirst().orElseThrow();
    }

    private Action action(String name, String currentState, String nextState, List<String> roles) {
        return Action.builder()
                .action(name)
                .currentState(currentState)
                .nextState(nextState)
                .roles(roles)
                .build();
    }

    private IncidentRequest request(String applicationStatus, String action, List<String> assignes) {
        Incident incident = Incident.builder()
                .incidentId(INCIDENT_ID)
                .tenantId(TENANT_ID)
                .applicationStatus(applicationStatus)
                .build();
        Workflow workflow = Workflow.builder()
                .action(action)
                .assignes(assignes)
                .build();
        return IncidentRequest.builder().incident(incident).workflow(workflow).build();
    }
}
