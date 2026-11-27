package org.egov.amc.service;

import org.egov.amc.web.models.ScheduledVisit;
import org.egov.amc.web.models.ScheduledVisitAssignment;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Who may approve or reject an AMC visit: the reviewer it is assigned to, and nobody else,
 * including a project manager who also holds the reviewer role.
 */
class VisitReviewerGuardTest {

    private static final String CALLER = "caller-uuid";
    private final VisitReviewerGuard guard = new VisitReviewerGuard(List.of("APPROVE", "REJECT"));

    private static RequestInfo caller(String uuid, String... roleCodes) {
        User user = new User();
        user.setUuid(uuid);
        user.setRoles(Arrays.stream(roleCodes).map(c -> Role.builder().code(c).build()).collect(Collectors.toList()));
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserInfo(user);
        return requestInfo;
    }

    private static ScheduledVisitAssignment assignment(String user, boolean active) {
        return ScheduledVisitAssignment.builder().assignedUser(user).isActive(active).build();
    }

    private static ScheduledVisit visit(String status, ScheduledVisitAssignment... assignments) {
        return ScheduledVisit.builder().id("visit-1").status(status).assignments(List.of(assignments)).build();
    }

    @Test
    @DisplayName("the reviewer a visit is assigned to can act on it")
    void assignedReviewerCanAct() {
        ScheduledVisit visit = visit("PENDING_APPROVAL", assignment("someone-else", true), assignment(CALLER, true));

        guard.markCanAct(caller(CALLER, "AMC_REVIEWER"), visit);

        assertTrue(visit.getCanAct());
        assertDoesNotThrow(() -> guard.requireAssignedReviewer(caller(CALLER, "AMC_REVIEWER"), visit, "APPROVE"));
    }

    @Test
    @DisplayName("a reviewer cannot act on a visit assigned to somebody else")
    void unassignedReviewerIsRefused() {
        ScheduledVisit visit = visit("PENDING_APPROVAL", assignment("someone-else", true));

        guard.markCanAct(caller(CALLER, "AMC_REVIEWER"), visit);

        assertFalse(visit.getCanAct());
        CustomException refused = assertThrows(CustomException.class,
                () -> guard.requireAssignedReviewer(caller(CALLER, "AMC_REVIEWER"), visit, "REJECT"));
        assertEquals("NOT_ASSIGNED_REVIEWER", refused.getCode());
    }

    @Test
    @DisplayName("a project manager who also holds the reviewer role is refused on an unassigned visit")
    void projectManagerWithReviewerRoleIsRefused() {
        // A project manager's search returns every assignee of the visit, not only their own row.
        ScheduledVisit visit = visit("PENDING_APPROVAL", assignment("a", true), assignment("b", true));

        assertThrows(CustomException.class,
                () -> guard.requireAssignedReviewer(caller(CALLER, "PROJECT_MANAGER", "AMC_REVIEWER"), visit, "APPROVE"));
    }

    @Test
    @DisplayName("holding the assignment without the reviewer role is not enough")
    void assignedWithoutTheRoleCannotAct() {
        ScheduledVisit visit = visit("PENDING_APPROVAL", assignment(CALLER, true));

        assertThrows(CustomException.class,
                () -> guard.requireAssignedReviewer(caller(CALLER, "AMC_FIELD_STAFF"), visit, "APPROVE"));
    }

    @Test
    @DisplayName("a deactivated assignment no longer lets the user act")
    void inactiveAssignmentDoesNotCount() {
        ScheduledVisit visit = visit("PENDING_APPROVAL", assignment(CALLER, false));

        guard.markCanAct(caller(CALLER, "AMC_REVIEWER"), visit);

        assertFalse(visit.getCanAct());
    }

    @Test
    @DisplayName("canAct is left unset for a caller who is not a reviewer, and for a blank uuid")
    void canActIsOnlySetForReviewers() {
        ScheduledVisit visit = visit("PENDING_APPROVAL", assignment(CALLER, true));

        guard.markCanAct(caller(CALLER, "PROJECT_MANAGER"), visit);
        assertNull(visit.getCanAct());

        guard.markCanAct(caller(" ", "AMC_REVIEWER"), visit);
        assertFalse(visit.getCanAct());
    }

    @Test
    @DisplayName("field staff actions and other states are left to workflow-v2")
    void otherActionsAndStatesAreNotChecked() {
        ScheduledVisit awaitingApproval = visit("PENDING_APPROVAL", assignment("someone-else", true));
        ScheduledVisit scheduled = visit("SCHEDULED", assignment("someone-else", true));

        assertDoesNotThrow(() -> guard.requireAssignedReviewer(caller(CALLER), awaitingApproval, "SUBMIT_VISIT_REPORT"));
        assertDoesNotThrow(() -> guard.requireAssignedReviewer(caller(CALLER), awaitingApproval, null));
        assertDoesNotThrow(() -> guard.requireAssignedReviewer(caller(CALLER), scheduled, "APPROVE"));
    }

    @Test
    @DisplayName("action names are matched ignoring case and the list is configurable")
    void actionNamesAreConfiguration() {
        assertTrue(guard.isReviewerAction("approve"));
        assertFalse(guard.isReviewerAction("SCHEDULE"));

        VisitReviewerGuard custom = new VisitReviewerGuard(List.of(" schedule ", ""));
        assertTrue(custom.isReviewerAction("SCHEDULE"));
        assertFalse(custom.isReviewerAction("APPROVE"));
    }
}
