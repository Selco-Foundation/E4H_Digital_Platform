package org.egov.activity.service;

import org.egov.activity.repository.ActivityAssignmentRepository;
import org.egov.activity.repository.ActivityFacilityRepository;
import org.egov.activity.web.models.ActivityAssignment;
import org.egov.activity.web.models.ActivityFacilityUser;
import org.egov.activity.web.models.ActivityFacilityUserBulkRequest;
import org.egov.common.contract.request.RequestInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Keeping per-facility visibility in step with plan staffing.
 *
 * These rows are the only thing that lets a reviewer or field staffer see an activity facility,
 * so every case below is about not stranding someone or leaving access behind.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivityFacilityUserFanoutServiceTest {

    private static final String PLAN = "plan-1";
    private static final String ACTIVITY = "activity-1";
    private static final String TENANT = "in";
    private static final String REVIEWER = "INSTALLATION_REPORT_APPROVER_QC_TEAM";

    @Mock
    private ActivityFacilityRepository activityFacilityRepository;
    @Mock
    private ActivityFacilityUsersService activityFacilityUsersService;
    @Mock
    private ActivityAssignmentRepository activityAssignmentRepository;

    @InjectMocks
    private ActivityFacilityUserFanoutService fanout;

    private final RequestInfo requestInfo = new RequestInfo();

    private static ActivityAssignment assignment(String id, String user, String roleCode) {
        return ActivityAssignment.builder()
                .id(id).assignedTo(user).fieldPlanId(PLAN).activityId(ACTIVITY).tenantId(TENANT)
                .role(Map.of("code", roleCode))
                .build();
    }

    private void planHasFacilities(String... ids) {
        when(activityFacilityRepository.findActivityFacilityRefsForFanout(eq(PLAN), eq(ACTIVITY), anyString()))
                .thenReturn(java.util.Arrays.stream(ids)
                        .map(id -> new ActivityFacilityRepository.ActivityFacilityRef(id, TENANT))
                        .collect(Collectors.toList()));
    }

    private void userHoldsNoOtherRole() {
        when(activityAssignmentRepository.findActiveRoleCodesForPlanUser(anyString(), anyString(), anyString(), any()))
                .thenReturn(List.of());
    }

    private List<ActivityFacilityUser> capturedCreates() throws Exception {
        ArgumentCaptor<ActivityFacilityUserBulkRequest> captor =
                ArgumentCaptor.forClass(ActivityFacilityUserBulkRequest.class);
        verify(activityFacilityUsersService).createMissingActivityFacilityUsers(captor.capture());
        return captor.getValue().getActivityFacilityUsers();
    }

    @Test
    @DisplayName("staffing a reviewer gives them a row on every facility of that plan activity")
    void creatingAnAssignmentFansOut() throws Exception {
        planHasFacilities("fa-1", "fa-2");

        fanout.onAssignmentsCreated(requestInfo, List.of(assignment("a1", "u1", REVIEWER)));

        List<ActivityFacilityUser> rows = capturedCreates();
        assertEquals(2, rows.size());
        assertTrue(rows.stream().allMatch(r -> "u1".equals(r.getUserId())));
        assertEquals(Set.of("fa-1", "fa-2"),
                rows.stream().map(ActivityFacilityUser::getActivityFacilityId).collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("a role the search does not scope on gets no rows")
    void anUnscopedRoleIsIgnored() {
        fanout.onAssignmentsCreated(requestInfo, List.of(assignment("a1", "u1", "ENUMERATOR")));

        verifyNoInteractions(activityFacilityRepository, activityFacilityUsersService);
    }

    @Test
    @DisplayName("re-saving the staffing grid unchanged does no work at all")
    void anUnchangedUpdateIsANoOp() {
        ActivityAssignment before = assignment("a1", "u1", REVIEWER);
        ActivityAssignment after = assignment("a1", "u1", REVIEWER);

        fanout.onAssignmentsUpdated(requestInfo, List.of(
                new ActivityFacilityUserFanoutService.AssignmentChange(before, after)));

        // The staffing screen replays every saved row on each save; without this the common case
        // costs a facility query and a duplicate prefetch per plan activity for no writes.
        verifyNoInteractions(activityFacilityRepository, activityFacilityUsersService, activityAssignmentRepository);
    }

    @Test
    @DisplayName("swapping the reviewer moves the rows: the old one loses access, the new one gains it")
    void swappingTheAssigneeMovesTheRows() throws Exception {
        planHasFacilities("fa-1");
        userHoldsNoOtherRole();

        fanout.onAssignmentsUpdated(requestInfo, List.of(new ActivityFacilityUserFanoutService.AssignmentChange(
                assignment("a1", "old-user", REVIEWER), assignment("a1", "new-user", REVIEWER))));

        verify(activityFacilityUsersService).softDeleteByFacilityAndUser(
                any(), eq(List.of("fa-1")), eq(Set.of("old-user")), eq(TENANT));
        assertEquals(List.of("new-user"),
                capturedCreates().stream().map(ActivityFacilityUser::getUserId).collect(Collectors.toList()));
    }

    @Test
    @DisplayName("a user who still holds another staffing role on the plan keeps their rows")
    void aSecondRoleProtectsTheRows() throws Exception {
        planHasFacilities("fa-1");
        when(activityAssignmentRepository.findActiveRoleCodesForPlanUser(anyString(), eq("u1"), anyString(), any()))
                .thenReturn(List.of("INSTALLATION_REPORT_PART_B_EDITOR"));

        fanout.onAssignmentsUnassigned(requestInfo, List.of(assignment("a1", "u1", REVIEWER)));

        verify(activityFacilityUsersService, never())
                .softDeleteByFacilityAndUser(any(), any(), any(), anyString());
    }

    @Test
    @DisplayName("unassigning the last staffing role revokes the rows")
    void unassigningRevokes() throws Exception {
        planHasFacilities("fa-1", "fa-2");
        userHoldsNoOtherRole();

        fanout.onAssignmentsUnassigned(requestInfo, List.of(assignment("a1", "u1", REVIEWER)));

        verify(activityFacilityUsersService).softDeleteByFacilityAndUser(
                any(), eq(List.of("fa-1", "fa-2")), eq(Set.of("u1")), eq(TENANT));
    }

    @Test
    @DisplayName("an unscheduled plan has no facilities yet, which is not an error")
    void noFacilitiesYetIsFine() throws Exception {
        when(activityFacilityRepository.findActivityFacilityRefsForFanout(anyString(), anyString(), anyString()))
                .thenReturn(List.of());

        fanout.onAssignmentsCreated(requestInfo, List.of(assignment("a1", "u1", REVIEWER)));

        verify(activityFacilityUsersService, never()).createMissingActivityFacilityUsers(any());
    }

    @Test
    @DisplayName("a failure on one plan activity never reaches the caller")
    void failuresAreContainedNotPropagated() throws Exception {
        when(activityFacilityRepository.findActivityFacilityRefsForFanout(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("db down"));

        // A staffing save must not be rejected because a derived index could not be maintained.
        fanout.onAssignmentsCreated(requestInfo, List.of(assignment("a1", "u1", REVIEWER)));

        verify(activityFacilityUsersService, never()).createMissingActivityFacilityUsers(any());
    }

    @Test
    @DisplayName("an update that marks the assignment deleted revokes the previous user")
    void aDeletedFlagOnUpdateRevokes() throws Exception {
        planHasFacilities("fa-1");
        userHoldsNoOtherRole();
        ActivityAssignment after = assignment("a1", "u1", REVIEWER);
        after.setIsDeleted(true);

        fanout.onAssignmentsUpdated(requestInfo, List.of(
                new ActivityFacilityUserFanoutService.AssignmentChange(assignment("a1", "u1", REVIEWER), after)));

        verify(activityFacilityUsersService).softDeleteByFacilityAndUser(
                any(), eq(List.of("fa-1")), eq(Set.of("u1")), eq(TENANT));
        verify(activityFacilityUsersService, never()).createMissingActivityFacilityUsers(any());
    }
}
