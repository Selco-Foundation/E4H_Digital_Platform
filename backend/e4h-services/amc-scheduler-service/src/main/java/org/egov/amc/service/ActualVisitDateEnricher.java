package org.egov.amc.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.amc.web.models.ProcessInstance;
import org.egov.amc.web.models.ScheduledVisit;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.amc.util.AmcConstants.DRAFT_STATUS;
import static org.egov.amc.util.AmcConstants.SUBMIT_VISIT_REPORT_ACTION;

/**
 * Recovers a visit's {@code actualVisitDate} from workflow history when the visits table does not
 * hold one.
 *
 * <p>{@code actual_visit_date} only started being stamped when SUBMIT_VISIT_REPORT is handled, so
 * visits submitted before that change hold NULL in the table and would be indexed without a date.
 * egov-workflow-v2 still has the transition history, and the SUBMIT_VISIT_REPORT transition's
 * createdTime is exactly the instant the runtime stamp records - so it is used as the authoritative
 * value here, taking the latest one when a visit was re-submitted out of REJECTED (mirroring the
 * overwrite the runtime does).
 *
 * <p>Enrichment is in memory only; nothing is written back to the visits table. Every index path
 * that reads {@code actualVisitDate} therefore has to run this first, or it will index the NULL and
 * undo what a previous path recovered. That shared requirement is why this lives in its own
 * component rather than staying private to the visit reindex: the visit index and the health
 * facility index both depend on it, and a second private copy would be free to drift.
 *
 * <p>When workflow has no such transition - never submitted, or history pruned - the visit keeps the
 * date already in the visits table (possibly none). Nothing is guessed from scheduled_date, and a
 * workflow failure degrades to that same stored value rather than aborting the caller.
 *
 * <p>DRAFT visits are skipped: they are never indexed and by definition have not been submitted.
 */
@Component
@Slf4j
public class ActualVisitDateEnricher {

    /** Visits per workflow-history lookup. */
    private static final int WORKFLOW_HISTORY_VISIT_BATCH_SIZE = 20;

    private final VisitWorkflowService workflowService;

    public ActualVisitDateEnricher(VisitWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /** Fills in {@code actualVisitDate} on {@code visits} in place. Never throws. */
    public void enrichActualVisitDateFromWorkflow(RequestInfo requestInfo, List<ScheduledVisit> visits) {
        if (visits == null || visits.isEmpty()) {
            return;
        }
        // Grouped by the visit's own tenantId, not any caller-supplied one, because the workflow search
        // filters on an exact tenantid match and a batch may span sub-tenants.
        Map<String, List<ScheduledVisit>> visitsByTenant = visits.stream()
                .filter(visit -> visit.getId() != null && visit.getTenantId() != null)
                .filter(visit -> visit.getStatus() != null && !DRAFT_STATUS.equalsIgnoreCase(visit.getStatus()))
                .collect(Collectors.groupingBy(ScheduledVisit::getTenantId));

        visitsByTenant.forEach((tenantId, tenantVisits) -> {
            for (int from = 0; from < tenantVisits.size(); from += WORKFLOW_HISTORY_VISIT_BATCH_SIZE) {
                List<ScheduledVisit> batch = tenantVisits.subList(from,
                        Math.min(from + WORKFLOW_HISTORY_VISIT_BATCH_SIZE, tenantVisits.size()));
                Map<String, Long> submittedAtByVisitId;
                try {
                    submittedAtByVisitId = getVisitReportSubmissionTimes(requestInfo, tenantId, batch);
                } catch (Exception e) {
                    log.error("Workflow history lookup failed for {} visits in tenantId={}. Falling back to the "
                            + "actual_visit_date already stored on those visits.", batch.size(), tenantId, e);
                    continue;
                }

                for (ScheduledVisit visit : batch) {
                    Long submittedAt = submittedAtByVisitId.get(visit.getId());
                    if (submittedAt != null) {
                        visit.setActualVisitDate(submittedAt);
                    } else if (visit.getActualVisitDate() == null) {
                        log.info("No {} transition in workflow and no stored actual_visit_date for visitId={}; "
                                + "indexing it without an actual visit date.", SUBMIT_VISIT_REPORT_ACTION, visit.getId());
                    }
                }
            }
        });
    }

    /**
     * Map visitId -> createdTime of its latest SUBMIT_VISIT_REPORT transition. Visits with no such
     * transition are simply absent from the map.
     */
    private Map<String, Long> getVisitReportSubmissionTimes(RequestInfo requestInfo, String tenantId,
                                                            List<ScheduledVisit> visits) {
        List<String> visitIds = visits.stream().map(ScheduledVisit::getId).toList();
        List<ProcessInstance> history = workflowService.getProcessInstanceHistory(visitIds, tenantId, requestInfo);

        Map<String, Long> submittedAtByVisitId = new HashMap<>();
        for (ProcessInstance instance : history) {
            if (!SUBMIT_VISIT_REPORT_ACTION.equalsIgnoreCase(instance.getAction())
                    || instance.getBusinessId() == null
                    || instance.getAuditDetails() == null
                    || instance.getAuditDetails().getCreatedTime() == null) {
                continue;
            }
            submittedAtByVisitId.merge(instance.getBusinessId(), instance.getAuditDetails().getCreatedTime(), Math::max);
        }
        return submittedAtByVisitId;
    }
}
