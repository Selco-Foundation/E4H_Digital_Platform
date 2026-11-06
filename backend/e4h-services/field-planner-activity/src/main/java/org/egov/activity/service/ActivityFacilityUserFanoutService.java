package org.egov.activity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.repository.ActivityAssignmentRepository;
import org.egov.activity.repository.ActivityFacilityRepository;
import org.egov.activity.web.models.ActivityAssignment;
import org.egov.activity.web.models.ActivityFacilityUser;
import org.egov.activity.web.models.ActivityFacilityUserBulkRequest;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.egov.activity.util.ActivityConstants.INSTALLATION_REPORT_APPROVER_QC_TEAM;
import static org.egov.activity.util.ActivityConstants.INSTALLATION_REPORT_PART_A_EDITOR;
import static org.egov.activity.util.ActivityConstants.INSTALLATION_REPORT_PART_B_EDITOR;

/**
 * Keeps {@code activity_facility_users} in step with plan staffing.
 *
 * <h3>Why this exists</h3>
 * Those rows are what let a field staffer, supervisor or reviewer see an activity facility at
 * all. Until now they were written at exactly one moment - when a field plan first reached
 * SCHEDULED and fanned out - and never again. Staffing a plan afterwards wrote nothing, and
 * unstaffing it removed nothing. That was invisible while the three roles bypassed the search
 * scope; the moment reviewers are scoped, a reviewer swapped in after scheduling sees nothing
 * at all.
 *
 * <h3>Assignment is plan-level, visibility is facility-level</h3>
 * {@code activity_assignments} has no facility column, so every change here has to be expanded
 * across the plan activity's facilities. That is the whole job.
 *
 * <h3>It never fails the caller</h3>
 * A staffing save must not be rejected because a derived index could not be maintained - the
 * assignment write is itself a fire-and-forget Kafka push whose failure is already swallowed.
 * But it must not fail <em>silently</em> either, which is how the original gap survived: every
 * failure logs one greppable line, and every run logs what it did, so a zero-write run is
 * visible rather than inferred.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityFacilityUserFanoutService {

    /** The roles whose uuid grants facility visibility through {@code activity_facility_users}. */
    private static final Set<String> SCOPED_ROLES = Set.of(
            INSTALLATION_REPORT_APPROVER_QC_TEAM,
            INSTALLATION_REPORT_PART_A_EDITOR,
            INSTALLATION_REPORT_PART_B_EDITOR);

    private final ActivityFacilityRepository activityFacilityRepository;
    private final ActivityFacilityUsersService activityFacilityUsersService;
    private final ActivityAssignmentRepository activityAssignmentRepository;

    /** One assignment as it was and as it now is. Either side may be null on create or unassign. */
    public record AssignmentChange(ActivityAssignment before, ActivityAssignment after) {
    }

    private record PlanActivityKey(String fieldPlanId, String activityId, String tenantId) {
    }

    public void onAssignmentsCreated(RequestInfo requestInfo, List<ActivityAssignment> created) {
        Map<PlanActivityKey, Set<String>> add = new LinkedHashMap<>();
        for (ActivityAssignment assignment : safe(created)) {
            if (isScoped(assignment)) {
                collect(add, assignment, assignment.getAssignedTo());
            }
        }
        apply(requestInfo, add, new LinkedHashMap<>(), List.of());
    }

    public void onAssignmentsUnassigned(RequestInfo requestInfo, List<ActivityAssignment> removed) {
        Map<PlanActivityKey, Set<String>> remove = new LinkedHashMap<>();
        List<String> excluded = new ArrayList<>();
        for (ActivityAssignment assignment : safe(removed)) {
            if (isScoped(assignment)) {
                collect(remove, assignment, assignment.getAssignedTo());
                excluded.add(assignment.getId());
            }
        }
        apply(requestInfo, new LinkedHashMap<>(), remove, excluded);
    }

    /**
     * Only genuine changes do any work.
     *
     * The project manager's screen replays the entire staffing grid to {@code /assignment/_update}
     * on every save, so the overwhelming majority of these are no-ops. Without this guard each one
     * would cost a facility query and a duplicate prefetch per plan activity, for no writes.
     */
    public void onAssignmentsUpdated(RequestInfo requestInfo, List<AssignmentChange> changes) {
        Map<PlanActivityKey, Set<String>> add = new LinkedHashMap<>();
        Map<PlanActivityKey, Set<String>> remove = new LinkedHashMap<>();
        List<String> excluded = new ArrayList<>();

        for (AssignmentChange change : safe(changes)) {
            ActivityAssignment before = change.before();
            ActivityAssignment after = change.after();
            if (after == null) {
                continue;
            }
            boolean deleted = Boolean.TRUE.equals(after.getIsDeleted());
            String previousUser = before == null ? null : before.getAssignedTo();
            String currentUser = deleted ? null : after.getAssignedTo();
            boolean previousScoped = before != null && isScoped(before);
            boolean currentScoped = !deleted && isScoped(after);

            if (previousScoped == currentScoped && Objects.equals(previousUser, currentUser)) {
                continue;
            }

            if (previousScoped && previousUser != null && !previousUser.equals(currentUser)) {
                collect(remove, before, previousUser);
                excluded.add(after.getId());
            }
            if (currentScoped && currentUser != null) {
                collect(add, after, currentUser);
            }
        }
        apply(requestInfo, add, remove, excluded);
    }

    /**
     * Removals run before additions, so that a user moved between two in-scope roles on the same
     * plan activity ends up with the row still present rather than removed by their own change.
     */
    private void apply(RequestInfo requestInfo,
                       Map<PlanActivityKey, Set<String>> add,
                       Map<PlanActivityKey, Set<String>> remove,
                       Collection<String> excludedAssignmentIds) {
        if (add.isEmpty() && remove.isEmpty()) {
            return;
        }

        int inserted = 0;
        int softDeleted = 0;
        Set<PlanActivityKey> keys = new LinkedHashSet<>(remove.keySet());
        keys.addAll(add.keySet());

        for (PlanActivityKey key : keys) {
            try {
                List<ActivityFacilityRepository.ActivityFacilityRef> facilities = activityFacilityRepository
                        .findActivityFacilityRefsForFanout(key.fieldPlanId(), key.activityId(), key.tenantId());
                if (facilities.isEmpty()) {
                    // Normal before a plan is scheduled: assignments exist, facilities do not yet.
                    // The scheduling fan-out writes the rows when they appear.
                    log.debug("No activity facilities yet for plan {} activity {}; nothing to fan out",
                            key.fieldPlanId(), key.activityId());
                    continue;
                }
                List<String> facilityIds = facilities.stream()
                        .map(ActivityFacilityRepository.ActivityFacilityRef::id).toList();

                Set<String> toRemove = keepOnlyFullyRevoked(key, remove.get(key), excludedAssignmentIds);
                if (!toRemove.isEmpty()) {
                    softDeleted += activityFacilityUsersService
                            .softDeleteByFacilityAndUser(requestInfo, facilityIds, toRemove, key.tenantId()).size();
                }

                Set<String> toAdd = add.get(key);
                if (toAdd != null && !toAdd.isEmpty()) {
                    List<ActivityFacilityUser> rows = new ArrayList<>();
                    for (ActivityFacilityRepository.ActivityFacilityRef facility : facilities) {
                        for (String userId : toAdd) {
                            rows.add(ActivityFacilityUser.builder()
                                    .activityFacilityId(facility.id())
                                    .userId(userId)
                                    .tenantId(facility.tenantId())
                                    .isDeleted(false)
                                    .build());
                        }
                    }
                    inserted += activityFacilityUsersService.createMissingActivityFacilityUsers(
                            ActivityFacilityUserBulkRequest.builder()
                                    .requestInfo(requestInfo)
                                    .activityFacilityUsers(rows)
                                    .build()).size();
                }
            } catch (Exception exception) {
                // One bad plan activity must not cost the others their fan-out.
                log.error("FACILITY_USER_FANOUT_FAILED planId={} activityId={} add={} remove={}",
                        key.fieldPlanId(), key.activityId(),
                        add.getOrDefault(key, Set.of()), remove.getOrDefault(key, Set.of()), exception);
            }
        }

        log.info("Activity facility user fan-out: inserted={} softDeleted={} planActivities={}",
                inserted, softDeleted, keys.size());
    }

    /**
     * Drops any user who still holds another in-scope role on the plan - revoking one role must
     * not take away the visibility the other still needs.
     */
    private Set<String> keepOnlyFullyRevoked(PlanActivityKey key, Set<String> candidates,
                                             Collection<String> excludedAssignmentIds) {
        if (candidates == null || candidates.isEmpty()) {
            return Set.of();
        }
        Set<String> revoked = new LinkedHashSet<>();
        for (String userId : candidates) {
            List<String> remaining = activityAssignmentRepository.findActiveRoleCodesForPlanUser(
                    key.fieldPlanId(), userId, key.tenantId(), excludedAssignmentIds);
            boolean stillStaffed = remaining.stream().anyMatch(ActivityFacilityUserFanoutService::isScopedRoleCode);
            if (stillStaffed) {
                log.debug("Keeping facility rows for {} on plan {}: still holds another staffing role",
                        userId, key.fieldPlanId());
            } else {
                revoked.add(userId);
            }
        }
        return revoked;
    }

    private static void collect(Map<PlanActivityKey, Set<String>> target, ActivityAssignment assignment, String userId) {
        if (assignment.getFieldPlanId() == null || assignment.getActivityId() == null || userId == null) {
            return;
        }
        target.computeIfAbsent(
                        new PlanActivityKey(assignment.getFieldPlanId(), assignment.getActivityId(), assignment.getTenantId()),
                        key -> new LinkedHashSet<>())
                .add(userId);
    }

    private static boolean isScoped(ActivityAssignment assignment) {
        return isScopedRoleCode(roleCode(assignment));
    }

    private static boolean isScopedRoleCode(String code) {
        return code != null && SCOPED_ROLES.stream().anyMatch(scoped -> scoped.equalsIgnoreCase(code));
    }

    private static String roleCode(ActivityAssignment assignment) {
        Map<String, Object> role = assignment == null ? null : assignment.getRole();
        Object code = role == null ? null : role.get("code");
        return code == null ? null : String.valueOf(code);
    }

    private static <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
