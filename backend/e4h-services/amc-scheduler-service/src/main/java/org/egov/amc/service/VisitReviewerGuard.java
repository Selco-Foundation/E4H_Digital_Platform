package org.egov.amc.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.amc.web.models.ScheduledVisit;
import org.egov.amc.web.models.ScheduledVisitAssignment;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.egov.amc.util.AmcConstants.AMC_REVIEWER;
import static org.egov.amc.util.AmcConstants.PENDING_APPROVAL;

/**
 * Who may take the reviewer's actions on an AMC visit.
 *
 * Only an AMC reviewer the visit is assigned to. A project manager sees every visit, and
 * workflow-v2 checks the role and never the assignee, so without this a project manager who also
 * holds the reviewer role could approve or reject any visit they can see.
 *
 * The same rule feeds {@link ScheduledVisit#getCanAct()}, so what the screen offers and what the
 * write path allows cannot drift apart.
 *
 * Visit assignments record the assignee but not the role they were given, so "reviewer" here means
 * assigned to the visit and holding {@code AMC_REVIEWER}. Which workflow actions count as the
 * reviewer's is configuration ({@code amc.reviewer.actions}): the workflow definition is not kept
 * in this repository.
 */
@Component
@Slf4j
public class VisitReviewerGuard {

    private final Set<String> reviewerActions;

    public VisitReviewerGuard(@Value("${amc.reviewer.actions:APPROVE,REJECT}") List<String> reviewerActions) {
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
                .anyMatch(AMC_REVIEWER::equalsIgnoreCase);
    }

    /** True when the caller holds the reviewer role and has an active assignment on the visit. */
    public boolean isAssignedReviewer(RequestInfo requestInfo, ScheduledVisit visit) {
        if (!holdsReviewerRole(requestInfo) || visit == null || visit.getAssignments() == null) {
            return false;
        }
        String uuid = requestInfo.getUserInfo().getUuid();
        if (StringUtils.isBlank(uuid)) {
            return false;
        }
        // Matched on the uuid explicitly: a project manager's search returns every assignee of the
        // visit, not only their own row.
        return visit.getAssignments().stream()
                .filter(Objects::nonNull)
                .anyMatch(assignment -> assignment.isActive() && uuid.equals(assignment.getAssignedUser()));
    }

    /** Sets {@code canAct} for a caller holding the reviewer role; leaves it unset for anyone else. */
    public void markCanAct(RequestInfo requestInfo, ScheduledVisit visit) {
        if (holdsReviewerRole(requestInfo)) {
            visit.setCanAct(isAssignedReviewer(requestInfo, visit));
        }
    }

    /**
     * Refuses a reviewer action on a visit the caller is not assigned to review.
     *
     * Applies to the reviewer's actions on a visit awaiting approval, which is what the reviewer
     * screen offers; every other transition is left to workflow-v2 as before.
     */
    public void requireAssignedReviewer(RequestInfo requestInfo, ScheduledVisit visit, String action) {
        if (!isReviewerAction(action) || !PENDING_APPROVAL.equals(visit.getStatus())) {
            return;
        }
        if (!isAssignedReviewer(requestInfo, visit)) {
            log.error("Refusing {} on visit {}: caller is not its assigned AMC reviewer", forLog(action), forLog(visit.getId()));
            throw new CustomException("NOT_ASSIGNED_REVIEWER",
                    "Only the AMC reviewer assigned to this visit can " + action.toLowerCase(Locale.ROOT).replace('_', ' ') + " it");
        }
    }

    /** Request values are logged without line breaks, so one cannot forge a log line. */
    private static String forLog(String value) {
        return value == null ? null : value.replaceAll("[\\r\\n]", "_");
    }
}
