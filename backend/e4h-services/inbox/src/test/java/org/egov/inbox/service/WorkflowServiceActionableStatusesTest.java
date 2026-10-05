package org.egov.inbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.inbox.config.InboxConfiguration;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.egov.inbox.web.model.workflow.Action;
import org.egov.inbox.web.model.workflow.BusinessService;
import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;
import org.egov.inbox.web.model.workflow.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers which statuses the inbox lets a user act on, and in particular that a pooled role registered
 * at the state tenant still owns tickets of the narrower tenant the inbox is queried at.
 * <p>
 * The business service is stubbed by hand: the answer is read off the state definitions, so those
 * definitions are the input under test.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowServiceActionableStatusesTest {

    private static final String STATE_TENANT = "in";
    private static final String FACILITY_TENANT = "in.karnataka.bagalkote";

    private static final String UUID_PENDINGFORASSIGNMENT = "state-pendingforassignment";
    private static final String UUID_OUT_OF_SCOPE = "state-out-of-scope";
    private static final String UUID_PENDINGRESOLUTION = "state-pendingresolution";

    private static final String SPOC = "COMPLAINT_FACILITATOR_1";
    private static final String TECH_POC = "COMPLAINT_FACILITATOR_2";
    private static final String RESOLVER = "COMPLAINT_RESOLVER";
    private static final String ASSESSOR = "COMPLAINT_ASSESSOR";

    @Mock
    private InboxConfiguration config;
    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    private WorkflowService workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new WorkflowService(config, serviceRequestRepository, new ObjectMapper());
    }

    @Test
    void givesTheStateSpocTheStatesItCanActOnAlthoughItsRoleSitsAtTheStateTenant() {
        // The SPOC's role is registered at "in" while the inbox is queried at a facility tenant. Before
        // the tickets were pooled this combination resolved to nothing and the inbox came back empty.
        HashMap<String, String> statuses = workflowService.getActionableStatusesForRole(
                requestInfo(STATE_TENANT, SPOC), businessServices(FACILITY_TENANT), criteria(FACILITY_TENANT));

        assertEquals(Collections.singletonMap(UUID_OUT_OF_SCOPE, "OUT_OF_SCOPE"), statuses);
    }

    @Test
    void givesTheTechPocItsOwnStates() {
        HashMap<String, String> statuses = workflowService.getActionableStatusesForRole(
                requestInfo(STATE_TENANT, TECH_POC), businessServices(FACILITY_TENANT), criteria(FACILITY_TENANT));

        assertEquals(Collections.singletonMap(UUID_PENDINGRESOLUTION, "PENDINGRESOLUTION"), statuses);
    }

    @Test
    void doesNotWidenANonPooledRoleHeldAtTheStateTenant() {
        // A CRM is not pooled, so its state-level role must not reach into a facility tenant's inbox;
        // only the pooled roles get that.
        HashMap<String, String> statuses = workflowService.getActionableStatusesForRole(
                requestInfo(STATE_TENANT, ASSESSOR), businessServices(FACILITY_TENANT), criteria(FACILITY_TENANT));

        assertTrue(statuses.isEmpty());
    }

    @Test
    void stillMatchesARoleHeldAtExactlyTheRequestedTenant() {
        HashMap<String, String> statuses = workflowService.getActionableStatusesForRole(
                requestInfo(FACILITY_TENANT, RESOLVER), businessServices(FACILITY_TENANT), criteria(FACILITY_TENANT));

        assertEquals(Collections.singletonMap(UUID_PENDINGFORASSIGNMENT, "PENDINGFORASSIGNMENT"), statuses);
    }

    @Test
    void doesNotTreatATenantSharingAPrefixAsAnAncestor() {
        // "in.kar" is not an ancestor of "in.karnataka.bagalkote" - ancestry is per dot separated segment.
        HashMap<String, String> statuses = workflowService.getActionableStatusesForRole(
                requestInfo("in.kar", SPOC), businessServices(FACILITY_TENANT), criteria(FACILITY_TENANT));

        assertTrue(statuses.isEmpty());
    }

    @Test
    void doesNotWidenANonPooledRoleHeldAlongsideAPooledOneAtTheSameTenant() {
        // The SPOC also holds COMPLAINT_RESOLVER at the state tenant. Only the facilitator reaches down
        // from there, so the vendor state must stay out of the inbox.
        HashMap<String, String> statuses = workflowService.getActionableStatusesForRole(
                requestInfo(STATE_TENANT, SPOC, RESOLVER), businessServices(FACILITY_TENANT),
                criteria(FACILITY_TENANT));

        assertEquals(Collections.singletonMap(UUID_OUT_OF_SCOPE, "OUT_OF_SCOPE"), statuses);
    }

    /**
     * One business service whose three states are each waiting on a different role.
     */
    private List<BusinessService> businessServices(String tenantId) {
        State pendingForAssignment = State.builder()
                .uuid(UUID_PENDINGFORASSIGNMENT)
                .state("PENDINGFORASSIGNMENT")
                .applicationStatus("PENDINGFORASSIGNMENT")
                .actions(Collections.singletonList(action("ASSIGN", List.of(RESOLVER))))
                .build();

        State outOfScope = State.builder()
                .uuid(UUID_OUT_OF_SCOPE)
                .state("OUT_OF_SCOPE")
                .applicationStatus("OUT_OF_SCOPE")
                .actions(Collections.singletonList(action("ASSIGN", List.of(SPOC))))
                .build();

        State pendingResolution = State.builder()
                .uuid(UUID_PENDINGRESOLUTION)
                .state("PENDINGRESOLUTION")
                .applicationStatus("PENDINGRESOLUTION")
                .actions(Collections.singletonList(action("APPROVE", List.of(TECH_POC))))
                .build();

        return Collections.singletonList(BusinessService.builder()
                .tenantId(tenantId)
                .businessService("Incident_Medium")
                .states(Arrays.asList(pendingForAssignment, outOfScope, pendingResolution))
                .build());
    }

    private Action action(String name, List<String> roles) {
        Action action = new Action();
        action.setAction(name);
        action.setRoles(roles);
        action.setActive(true);
        return action;
    }

    private RequestInfo requestInfo(String roleTenantId, String... roleCodes) {
        List<Role> roles = Arrays.stream(roleCodes)
                .map(code -> Role.builder().code(code).tenantId(roleTenantId).build())
                .collect(java.util.stream.Collectors.toList());
        User user = User.builder().uuid("uuid-user").roles(roles).build();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserInfo(user);
        return requestInfo;
    }

    private ProcessInstanceSearchCriteria criteria(String tenantId) {
        ProcessInstanceSearchCriteria criteria = new ProcessInstanceSearchCriteria();
        criteria.setTenantId(tenantId);
        return criteria;
    }
}
