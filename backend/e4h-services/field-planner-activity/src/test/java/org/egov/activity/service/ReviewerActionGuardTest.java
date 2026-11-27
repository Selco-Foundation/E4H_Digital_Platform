package org.egov.activity.service;

import org.egov.activity.repository.ActivityAssignmentRepository;
import org.egov.activity.web.models.ActivityFacility;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Who may approve or reject an installation: the reviewer it is assigned to, and nobody else,
 * including a project manager who also holds the reviewer role.
 */
class ReviewerActionGuardTest {

    private static final String QC = "INSTALLATION_REPORT_APPROVER_QC_TEAM";
    private static final String CALLER = "caller-uuid";

    private ActivityAssignmentRepository repository;
    private ReviewerActionGuard guard;

    @BeforeEach
    void setUp() {
        repository = mock(ActivityAssignmentRepository.class);
        guard = new ReviewerActionGuard(repository, List.of("APPROVE", "REJECT_AND_ASSIGN_FOR_FIELD_QC"));
    }

    private static RequestInfo caller(String uuid, String... roleCodes) {
        User user = new User();
        user.setUuid(uuid);
        user.setRoles(Arrays.stream(roleCodes).map(c -> Role.builder().code(c).build()).collect(Collectors.toList()));
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserInfo(user);
        return requestInfo;
    }

    private static ActivityFacility facility(String plan, String activity, String status) {
        ActivityFacility facility = new ActivityFacility();
        facility.setId("af-" + plan + "-" + activity);
        facility.setFieldPlanId(plan);
        facility.setActivityId(activity);
        facility.setStatus(status);
        return facility;
    }

    private void assignedTo(String... planActivityKeys) {
        when(repository.findAssignedPlanActivityKeys(eq(CALLER), eq(QC), any())).thenReturn(Set.of(planActivityKeys));
    }

    @Test
    @DisplayName("the reviewer a plan-activity is assigned to can act on its facilities")
    void assignedReviewerCanAct() {
        assignedTo("plan-1|act-1");
        ActivityFacility mine = facility("plan-1", "act-1", "SUBMITTED_BY_SUPERVISOR");
        ActivityFacility other = facility("plan-1", "act-2", "SUBMITTED_BY_SUPERVISOR");

        guard.markCanAct(caller(CALLER, QC), new ArrayList<>(List.of(mine, other)));

        assertTrue(mine.getCanAct());
        assertFalse(other.getCanAct(), "assigned to another activity of the same plan");
        assertDoesNotThrow(() -> guard.requireAssignedReviewer(caller(CALLER, QC), mine, "APPROVE"));
    }

    @Test
    @DisplayName("a reviewer cannot act on a facility they are not assigned to")
    void unassignedReviewerIsRefused() {
        assignedTo("plan-9|act-9");
        ActivityFacility facility = facility("plan-1", "act-1", "SUBMITTED_BY_SUPERVISOR");

        CustomException refused = assertThrows(CustomException.class,
                () -> guard.requireAssignedReviewer(caller(CALLER, QC), facility, "APPROVE"));

        assertEquals("NOT_ASSIGNED_REVIEWER", refused.getCode());
    }

    @Test
    @DisplayName("a project manager who also holds the reviewer role is refused on an unassigned facility")
    void projectManagerWithReviewerRoleIsRefused() {
        assignedTo();
        ActivityFacility facility = facility("plan-1", "act-1", "SUBMITTED_BY_SUPERVISOR");

        assertThrows(CustomException.class,
                () -> guard.requireAssignedReviewer(caller(CALLER, "PROJECT_MANAGER", QC), facility, "REJECT_AND_ASSIGN_FOR_FIELD_QC"));
        guard.markCanAct(caller(CALLER, "PROJECT_MANAGER", QC), List.of(facility));
        assertFalse(facility.getCanAct());
    }

    @Test
    @DisplayName("without the reviewer role nothing is assigned, and the database is not asked")
    void callerWithoutTheRoleCannotAct() {
        ActivityFacility facility = facility("plan-1", "act-1", "SUBMITTED_BY_SUPERVISOR");

        guard.markCanAct(caller(CALLER, "PROJECT_MANAGER"), List.of(facility));

        assertFalse(facility.getCanAct());
        assertThrows(CustomException.class,
                () -> guard.requireAssignedReviewer(caller(CALLER, "PROJECT_MANAGER"), facility, "APPROVE"));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("a caller we cannot identify cannot act")
    void blankUuidCannotAct() {
        ActivityFacility facility = facility("plan-1", "act-1", "SUBMITTED_BY_SUPERVISOR");

        guard.markCanAct(caller(" ", QC), List.of(facility));

        assertFalse(facility.getCanAct());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("transitions other than the reviewer's are left to workflow-v2")
    void otherActionsAreNotChecked() {
        ActivityFacility facility = facility("plan-1", "act-1", "SUBMITTED_BY_SUPERVISOR");

        assertDoesNotThrow(() -> guard.requireAssignedReviewer(caller(CALLER, "SOMEONE_ELSE"), facility, "SUBMIT_REPORT_A"));
        assertDoesNotThrow(() -> guard.requireAssignedReviewer(caller(CALLER, "SOMEONE_ELSE"), facility, null));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("the reviewer's actions are only checked on a facility awaiting review")
    void onlyFacilitiesAwaitingReviewAreChecked() {
        ActivityFacility facility = facility("plan-1", "act-1", "APPROVED_BY_QC_SPOC");

        assertDoesNotThrow(() -> guard.requireAssignedReviewer(caller(CALLER, "SOMEONE_ELSE"), facility, "APPROVE"));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("action names are matched ignoring case and the list is configurable")
    void actionNamesAreConfiguration() {
        assertTrue(guard.isReviewerAction("approve"));
        assertFalse(guard.isReviewerAction("FLAG_FOR_QC"));

        ReviewerActionGuard custom = new ReviewerActionGuard(repository, List.of(" flag_for_qc ", ""));
        assertTrue(custom.isReviewerAction("FLAG_FOR_QC"));
        assertFalse(custom.isReviewerAction("APPROVE"));
    }

    @Test
    @DisplayName("a facility with no plan or activity is never the caller's")
    void facilityWithoutPlanOrActivityIsNotAssigned() {
        assignedTo("plan-1|act-1");
        ActivityFacility noPlan = facility(null, "act-1", "SUBMITTED_BY_SUPERVISOR");

        guard.markCanAct(caller(CALLER, QC), List.of(noPlan));

        assertFalse(noPlan.getCanAct());
    }
}
