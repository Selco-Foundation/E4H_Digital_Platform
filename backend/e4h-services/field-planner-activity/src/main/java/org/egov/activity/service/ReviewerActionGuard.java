package org.egov.activity.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.activity.repository.ActivityAssignmentRepository;
import org.egov.activity.web.models.ActivityFacility;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.egov.activity.repository.ActivityAssignmentRepository.planActivityKey;
import static org.egov.activity.util.ActivityConstants.INSTALLATION_REPORT_APPROVER_QC_TEAM;
import static org.egov.activity.util.ActivityConstants.SUBMITTED_BY_SUPERVISOR;

/**
 * Who may take the reviewer's actions on an activity facility.
 *
 * Only the installation reviewer the plan-activity was assigned to. Holding the reviewer role is
 * not enough, and neither is being a project manager who can see the facility: workflow-v2 checks
 * the role and never the assignee, so without this a project manager who also holds the reviewer
 * role could approve or reject any facility they can see.
 *
 * The same rule feeds {@link ActivityFacility#getCanAct()}, so what the screen offers and what the
 * write path allows cannot drift apart.
 *
 * Which workflow actions count as the reviewer's is configuration
 * ({@code activity.reviewer.actions}): the workflow definition is not kept in this repository.
 */
@Component
@Slf4j
public class ReviewerActionGuard {

    private final ActivityAssignmentRepository assignmentRepository;
    private final Set<String> reviewerActions;

    public ReviewerActionGuard(ActivityAssignmentRepository assignmentRepository,
                               @Value("${activity.reviewer.actions:APPROVE,REJECT_AND_ASSIGN_FOR_FIELD_QC}") List<String> reviewerActions) {
        this.assignmentRepository = assignmentRepository;
        this.reviewerActions = (reviewerActions == null ? List.<String>of() : reviewerActions).stream()
                .filter(StringUtils::isNotBlank)
                .map(action -> action.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public boolean isReviewerAction(String action) {
        return action != null && reviewerActions.contains(action.trim().toUpperCase(Locale.ROOT));
    }

    static boolean holdsReviewerRole(RequestInfo requestInfo) {
        if (requestInfo == null || requestInfo.getUserInfo() == null || requestInfo.getUserInfo().getRoles() == null) {
            return false;
        }
        return requestInfo.getUserInfo().getRoles().stream()
                .filter(Objects::nonNull)
                .map(Role::getCode)
                .anyMatch(INSTALLATION_REPORT_APPROVER_QC_TEAM::equalsIgnoreCase);
    }

    /**
     * Sets {@code canAct} on each facility: true only when the caller is the installation reviewer
     * assigned to its plan-activity. It says nothing about the facility's state; the screen still
     * decides from the status which action applies.
     */
    public void markCanAct(RequestInfo requestInfo, List<ActivityFacility> facilities) {
        if (facilities == null || facilities.isEmpty()) {
            return;
        }
        Set<String> assigned = assignedKeys(requestInfo, facilities);
        for (ActivityFacility facility : facilities) {
            facility.setCanAct(assigned.contains(planActivityKey(facility.getFieldPlanId(), facility.getActivityId())));
        }
    }

    /**
     * Refuses a reviewer action on a facility the caller is not the assigned reviewer of.
     *
     * Applies to the reviewer's actions on a facility awaiting review, which is what the reviewer
     * screens offer; every other transition is left to workflow-v2 as before.
     */
    public void requireAssignedReviewer(RequestInfo requestInfo, ActivityFacility facility, String action) {
        if (!isReviewerAction(action) || !SUBMITTED_BY_SUPERVISOR.equals(facility.getStatus())) {
            return;
        }
        boolean assigned = assignedKeys(requestInfo, List.of(facility))
                .contains(planActivityKey(facility.getFieldPlanId(), facility.getActivityId()));
        if (!assigned) {
            log.error("Refusing {} on activity facility {}: caller is not its assigned installation reviewer",
                    action, facility.getId());
            throw new CustomException("NOT_ASSIGNED_REVIEWER",
                    "Only the installation reviewer assigned to this installation can " + action.toLowerCase(Locale.ROOT).replace('_', ' ') + " it");
        }
    }

    private Set<String> assignedKeys(RequestInfo requestInfo, List<ActivityFacility> facilities) {
        if (!holdsReviewerRole(requestInfo) || StringUtils.isBlank(requestInfo.getUserInfo().getUuid())) {
            return Set.of();
        }
        Set<String> planIds = facilities.stream()
                .map(ActivityFacility::getFieldPlanId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(HashSet::new));
        return assignmentRepository.findAssignedPlanActivityKeys(
                requestInfo.getUserInfo().getUuid(), INSTALLATION_REPORT_APPROVER_QC_TEAM, planIds);
    }
}
