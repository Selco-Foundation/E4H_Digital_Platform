package org.egov.im.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.util.IMUtils;
import org.egov.im.util.MDMSUtils;
import org.egov.im.util.VendorOrganisationUtil;
import org.egov.im.web.models.AuditDetails;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.Workflow;
import org.egov.im.web.models.workflow.ProcessInstance;
import org.egov.im.web.models.workflow.ProcessInstanceResponse;
import org.egov.im.web.models.workflow.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.egov.im.util.IMConstants.ASSIGN;
import static org.egov.im.util.IMConstants.ASSIGN_NEW_VENDOR;
import static org.egov.im.util.IMConstants.MARK_OUT_OF_SCOPE_ACTION;
import static org.egov.im.util.IMConstants.OUT_OF_SCOPE;
import static org.egov.im.util.IMConstants.PENDINGATVENDOR;
import static org.egov.im.util.IMConstants.RMS_DEVICE_PENDINGRESOLUTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the same-vendor / new-vendor decision taken on an ASSIGN out of OUT_OF_SCOPE.
 * <p>
 * U1 and U2 belong to vendor organisation ORG_A, U3 to ORG_B.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowServiceOutOfScopeAssignTest {

    private static final String TENANT_ID = "in";
    private static final String INCIDENT_ID = "IM-2026-000001";
    private static final String U1 = "uuid-user-1";
    private static final String U2 = "uuid-user-2";
    private static final String U3 = "uuid-user-3";
    private static final String ORG_A = "org-a";
    private static final String ORG_B = "org-b";

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
    @Mock
    private IMUtils imUtils;
    @Mock
    private VendorOrganisationUtil vendorOrganisationUtil;

    private final ObjectMapper mapper = new ObjectMapper();

    private WorkflowService workflowService;

    @BeforeEach
    void setUp() {
        when(imConfiguration.getWfHost()).thenReturn("http://workflow");
        when(imConfiguration.getWfProcessInstanceSearchPath()).thenReturn("/egov-wf/process/_search");
        workflowService = new WorkflowService(imConfiguration, repository, mapper, notificationService,
                mdmsUtils, slaService, null, imUtils, vendorOrganisationUtil);
    }

    @Test
    void keepsAssignWhenTheRequestCarriesNoAssignee() {
        IncidentRequest request = outOfScopeAssignRequest(Collections.emptyList());

        assertEquals(ASSIGN, workflowService.resolveOutOfScopeAssignAction(request));
        verify(vendorOrganisationUtil, never()).getOrganisationIdsByUserUuids(any(), anyString(), any());
    }

    @Test
    void keepsAssignWhenTheHistoryIsEmpty() {
        givenWorkflowHistory();

        assertEquals(ASSIGN, workflowService.resolveOutOfScopeAssignAction(outOfScopeAssignRequest(List.of(U3))));
    }

    @Test
    void keepsAssignWhenReassignedToTheSameUserWithoutAskingVendorRegistry() {
        givenWorkflowHistory(
                processInstance(ASSIGN, PENDINGATVENDOR, U1, null, 1_000L),
                processInstance(MARK_OUT_OF_SCOPE_ACTION, OUT_OF_SCOPE, null, U1, 2_000L));

        assertEquals(ASSIGN, workflowService.resolveOutOfScopeAssignAction(outOfScopeAssignRequest(List.of(U1))));
        verify(vendorOrganisationUtil, never()).getOrganisationIdsByUserUuids(any(), anyString(), any());
    }

    @Test
    void keepsAssignWhenReassignedToAnotherUserOfTheSameVendor() {
        givenWorkflowHistory(
                processInstance(ASSIGN, PENDINGATVENDOR, U1, null, 1_000L),
                processInstance(MARK_OUT_OF_SCOPE_ACTION, OUT_OF_SCOPE, null, U1, 2_000L));
        givenOrganisations(Map.of(U1, ORG_A, U2, ORG_A));

        assertEquals(ASSIGN, workflowService.resolveOutOfScopeAssignAction(outOfScopeAssignRequest(List.of(U2))));
    }

    @Test
    void usesAssignNewVendorWhenReassignedToAnotherVendor() {
        givenWorkflowHistory(
                processInstance(ASSIGN, PENDINGATVENDOR, U1, null, 1_000L),
                processInstance(MARK_OUT_OF_SCOPE_ACTION, OUT_OF_SCOPE, null, U1, 2_000L));
        givenOrganisations(Map.of(U1, ORG_A, U3, ORG_B));

        assertEquals(ASSIGN_NEW_VENDOR, workflowService.resolveOutOfScopeAssignAction(outOfScopeAssignRequest(List.of(U3))));
    }

    @Test
    void usesAssignNewVendorForAnRmsTicketToo() {
        givenWorkflowHistory(
                processInstance(ASSIGN, RMS_DEVICE_PENDINGRESOLUTION, U1, null, 1_000L),
                processInstance(MARK_OUT_OF_SCOPE_ACTION, OUT_OF_SCOPE, null, U1, 2_000L));
        givenOrganisations(Map.of(U1, ORG_A, U3, ORG_B));

        assertEquals(ASSIGN_NEW_VENDOR, workflowService.resolveOutOfScopeAssignAction(outOfScopeAssignRequest(List.of(U3))));
    }

    @Test
    void comparesAgainstTheMostRecentVendorNotTheFirstOne() {
        // U1 held it, then U3 did (out of scope once already); assigning back to U3 is the same vendor.
        givenWorkflowHistory(
                processInstance(ASSIGN, PENDINGATVENDOR, U1, null, 1_000L),
                processInstance(MARK_OUT_OF_SCOPE_ACTION, OUT_OF_SCOPE, null, U1, 2_000L),
                processInstance(ASSIGN_NEW_VENDOR, PENDINGATVENDOR, U3, null, 3_000L),
                processInstance(MARK_OUT_OF_SCOPE_ACTION, OUT_OF_SCOPE, null, U3, 4_000L));

        assertEquals(ASSIGN, workflowService.resolveOutOfScopeAssignAction(outOfScopeAssignRequest(List.of(U3))));
        verify(vendorOrganisationUtil, never()).getOrganisationIdsByUserUuids(any(), anyString(), any());
    }

    @Test
    void fallsBackOnTheResolverWhoMarkedItOutOfScopeWhenNoAssigneeWasRecorded() {
        givenWorkflowHistory(
                processInstance(ASSIGN, PENDINGATVENDOR, null, null, 1_000L),
                processInstance(MARK_OUT_OF_SCOPE_ACTION, OUT_OF_SCOPE, null, U1, 2_000L));
        givenOrganisations(Map.of(U1, ORG_A, U3, ORG_B));

        assertEquals(ASSIGN_NEW_VENDOR, workflowService.resolveOutOfScopeAssignAction(outOfScopeAssignRequest(List.of(U3))));
    }

    @Test
    void keepsAssignWhenVendorRegistryCannotResolveTheOrganisations() {
        givenWorkflowHistory(
                processInstance(ASSIGN, PENDINGATVENDOR, U1, null, 1_000L),
                processInstance(MARK_OUT_OF_SCOPE_ACTION, OUT_OF_SCOPE, null, U1, 2_000L));
        givenOrganisations(Collections.emptyMap());

        assertEquals(ASSIGN, workflowService.resolveOutOfScopeAssignAction(outOfScopeAssignRequest(List.of(U3))));
    }

    @Test
    void keepsAssignWhenTheWorkflowSearchBlowsUp() {
        when(repository.fetchResult(any(), any())).thenThrow(new RuntimeException("workflow unreachable"));

        assertEquals(ASSIGN, workflowService.resolveOutOfScopeAssignAction(outOfScopeAssignRequest(List.of(U3))));
    }

    /* helpers */

    private IncidentRequest outOfScopeAssignRequest(List<String> assignes) {
        return IncidentRequest.builder()
                .requestInfo(RequestInfo.builder().build())
                .incident(Incident.builder()
                        .incidentId(INCIDENT_ID)
                        .tenantId(TENANT_ID)
                        .applicationStatus(OUT_OF_SCOPE)
                        .build())
                .workflow(Workflow.builder()
                        .action(ASSIGN)
                        .assignes(assignes)
                        .build())
                .build();
    }

    private void givenWorkflowHistory(ProcessInstance... processInstances) {
        ProcessInstanceResponse response = ProcessInstanceResponse.builder()
                .processInstances(List.of(processInstances))
                .build();
        when(repository.fetchResult(any(), any())).thenReturn(mapper.convertValue(response, Map.class));
    }

    private void givenOrganisations(Map<String, String> organisationIdsByUserUuid) {
        when(vendorOrganisationUtil.getOrganisationIdsByUserUuids(any(), anyString(), any()))
                .thenReturn(organisationIdsByUserUuid);
    }

    private ProcessInstance processInstance(String action, String applicationStatus, String assigneeUuid,
                                            String assignerUuid, long lastModifiedTime) {
        ProcessInstance processInstance = ProcessInstance.builder()
                .businessId(INCIDENT_ID)
                .tenantId(TENANT_ID)
                .action(action)
                .state(State.builder().state(applicationStatus).applicationStatus(applicationStatus).build())
                .auditDetails(AuditDetails.builder().lastModifiedTime(lastModifiedTime).createdTime(lastModifiedTime).build())
                .build();
        if (assigneeUuid != null) {
            User assignee = new User();
            assignee.setUuid(assigneeUuid);
            processInstance.setAssignes(List.of(assignee));
        }
        if (assignerUuid != null) {
            User assigner = new User();
            assigner.setUuid(assignerUuid);
            processInstance.setAssigner(assigner);
        }
        return processInstance;
    }
}
