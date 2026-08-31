package org.egov.im.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.repository.IncidentIndexRepository;
import org.egov.im.repository.IncidentIndexRepository.CurrentOwnerPatch;
import org.egov.im.service.CurrentOwnerService.CurrentOwner;
import org.egov.im.web.models.CurrentOwnerBackfillRequest;
import org.egov.im.web.models.CurrentOwnerBackfillResponse;
import org.egov.im.web.models.workflow.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.egov.im.util.IMConstants.IM_BUSINESSSERVICE;

/**
 * One-off script that fills in {@code currentOwner} / {@code currentOwnerSystemRole} for incidents
 * that were indexed before those fields existed.
 * <p>
 * Live tickets get their owner from {@link CurrentOwnerService} on every workflow transition, so
 * only history needs fixing, and only these two fields. The script therefore walks the incident
 * index itself, reads each document's current workflow state, derives the owner from it and writes
 * the two fields back with a partial bulk update — no ticket is re-published and no other indexed
 * field is rewritten. A ticket that has since moved on is corrected by its next transition anyway.
 * <p>
 * The expensive lookups are done once, not per document: the reversed {@code USER_TYPE} master and
 * each incident BusinessService definition are fetched once and cached for the whole run, which is
 * what makes walking the whole index affordable. Terminal states are written as an explicit null so
 * a closed ticket does not linger in anyone's bucket.
 */
@Service
@Slf4j
public class CurrentOwnerBackfillService {

    private static final int DEFAULT_BATCH_SIZE = 500;
    private static final int MAX_BATCH_SIZE = 2000;
    private static final int PROGRESS_LOG_EVERY = 5000;

    /** Reported as the owner of a state nobody owns, to keep it distinguishable from "not seen". */
    private static final String NO_OWNER = "NONE";

    private final IncidentIndexRepository indexRepository;
    private final CurrentOwnerService currentOwnerService;
    private final WorkflowService workflowService;

    @Autowired
    public CurrentOwnerBackfillService(IncidentIndexRepository indexRepository,
                                       CurrentOwnerService currentOwnerService,
                                       WorkflowService workflowService) {
        this.indexRepository = indexRepository;
        this.currentOwnerService = currentOwnerService;
        this.workflowService = workflowService;
    }

    /**
     * Walks the incident index and brings every document's owner fields in line with its current
     * workflow state.
     *
     * @return the counts of the run plus the {@code businessService|state -> owner} mapping it applied
     */
    public CurrentOwnerBackfillResponse backfill(CurrentOwnerBackfillRequest request) {
        log.trace("CurrentOwnerBackfillService::backfill method invoked");
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getTenantId();
        boolean dryRun = Boolean.TRUE.equals(request.getDryRun());
        int batchSize = resolveBatchSize(request.getBatchSize());
        Integer maxDocuments = request.getMaxDocuments();
        log.info("Current owner backfill starting: tenantId={}, batchSize={}, maxDocuments={}, dryRun={}",
                tenantId, batchSize, maxDocuments, dryRun);

        // Fetched once for the whole run: the same master and the same workflow definitions apply to
        // every document, and per-document lookups would make walking the index untenable.
        Map<String, String> systemRoleToProgramRole =
                currentOwnerService.buildSystemRoleToProgramRoleMap(requestInfo, tenantId);
        log.info("Current owner backfill: {} system roles mapped to program roles", systemRoleToProgramRole.size());
        Map<String, List<State>> statesByBusinessService = new HashMap<>();
        Map<String, CurrentOwner> ownerByStateKey = new HashMap<>();
        Map<String, Integer> documentsByStateKey = new TreeMap<>();

        Counters counters = new Counters();
        List<CurrentOwnerPatch> pending = new ArrayList<>();
        JsonNode searchAfter = null;

        while (maxDocuments == null || counters.processed < maxDocuments) {
            int pageSize = (maxDocuments == null) ? batchSize
                    : Math.min(batchSize, maxDocuments - counters.processed);
            JsonNode hits = indexRepository.fetchOwnerBackfillPage(pageSize, searchAfter);
            if (hits == null || hits.isEmpty()) {
                break;
            }

            for (JsonNode hit : hits) {
                counters.processed++;
                JsonNode data = hit.path("_source").path("Data");
                String documentId = hit.path("_id").asText(null);
                String incidentId = text(data.path("incident").path("incidentId"));

                if (!StringUtils.hasText(documentId)) {
                    log.warn("Current owner backfill: hit without a document id (incidentId={}), skipping", incidentId);
                    counters.skipped++;
                    continue;
                }

                JsonNode processInstance = data.path("currentProcessInstance");
                String businessService = text(processInstance.path("businessService"));
                if (!StringUtils.hasText(businessService)) {
                    // Pre-priority tickets were all on the single "Incident" business service.
                    businessService = IM_BUSINESSSERVICE;
                }
                JsonNode state = processInstance.path("state");
                String stateUuid = text(state.path("uuid"));
                String stateName = text(state.path("state"));
                String stateKey = businessService + "|" + (stateName == null ? stateUuid : stateName);
                documentsByStateKey.merge(stateKey, 1, Integer::sum);

                // Derived once per distinct state, so both the workflow lookups and the derivation's
                // own logging happen once rather than once per ticket. Unowned states are cached as a
                // null value, which is why this is not computeIfAbsent.
                CurrentOwner owner;
                if (ownerByStateKey.containsKey(stateKey)) {
                    owner = ownerByStateKey.get(stateKey);
                } else {
                    owner = deriveOwner(requestInfo, tenantId, businessService, stateUuid, stateName,
                            statesByBusinessService, systemRoleToProgramRole, incidentId);
                    ownerByStateKey.put(stateKey, owner);
                }

                if (owner == null) {
                    counters.unresolved++;
                } else {
                    counters.resolved++;
                }

                String indexedOwner = text(data.path("currentOwner"));
                String indexedSystemRole = text(data.path("currentOwnerSystemRole"));
                String derivedOwner = (owner == null) ? null : owner.programRole();
                String derivedSystemRole = (owner == null) ? null : owner.systemRole();
                if (equalOrBothBlank(indexedOwner, derivedOwner)
                        && equalOrBothBlank(indexedSystemRole, derivedSystemRole)) {
                    counters.unchanged++;
                    continue;
                }

                pending.add(new CurrentOwnerPatch(documentId, derivedOwner, derivedSystemRole));
                if (pending.size() >= batchSize) {
                    flush(pending, counters, dryRun);
                }

                if (counters.processed % PROGRESS_LOG_EVERY == 0) {
                    log.info("Current owner backfill progress: {}", counters);
                }
            }

            // search_after resumes from the sort values of the page's last hit, so the walk neither
            // skips nor repeats documents even while the index is being written to.
            searchAfter = hits.get(hits.size() - 1).path("sort");
            if (searchAfter.isMissingNode() || !searchAfter.isArray() || searchAfter.isEmpty()) {
                log.warn("Current owner backfill: page returned no sort values, stopping after {} documents",
                        counters.processed);
                break;
            }
            if (hits.size() < pageSize) {
                break;
            }
        }

        flush(pending, counters, dryRun);
        log.info("Current owner backfill finished: {} (dryRun={})", counters, dryRun);

        return CurrentOwnerBackfillResponse.builder()
                .dryRun(dryRun)
                .processed(counters.processed)
                .updated(dryRun ? counters.wouldUpdate : counters.updated)
                .unchanged(counters.unchanged)
                .resolved(counters.resolved)
                .unresolved(counters.unresolved)
                .failed(counters.failed)
                .skipped(counters.skipped)
                .ownerByState(summarise(documentsByStateKey, ownerByStateKey))
                .build();
    }

    /**
     * Derives the owner of a {@code businessService|state} pair, loading that business service's
     * definition on first sight. Called once per distinct state, not once per document.
     */
    private CurrentOwner deriveOwner(RequestInfo requestInfo, String tenantId, String businessService,
                                     String stateUuid, String stateName,
                                     Map<String, List<State>> statesByBusinessService,
                                     Map<String, String> systemRoleToProgramRole, String incidentId) {
        List<State> states = statesByBusinessService.computeIfAbsent(businessService, name -> {
            try {
                List<State> fetched = workflowService.getBusinessServiceByName(requestInfo, tenantId, name)
                        .getStates();
                log.info("Current owner backfill: loaded {} states for business service {}",
                        (fetched == null) ? 0 : fetched.size(), name);
                return (fetched == null) ? List.of() : fetched;
            } catch (Exception e) {
                log.error("Current owner backfill: could not load business service {}, its tickets will be "
                        + "left without an owner", name, e);
                return List.of();
            }
        });

        State currentState = currentOwnerService.findState(stateUuid, stateName, states);
        if (currentState == null) {
            log.warn("Current owner backfill: state {} ({}) not found in business service {}, owner will be null",
                    stateName, stateUuid, businessService);
            return null;
        }
        return currentOwnerService.resolveOwner(currentState, () -> systemRoleToProgramRole, incidentId);
    }

    /** Sends the pending patches, unless this is a dry run, and folds the outcome into the counters. */
    private void flush(List<CurrentOwnerPatch> pending, Counters counters, boolean dryRun) {
        if (pending.isEmpty()) {
            return;
        }
        if (dryRun) {
            log.info("Current owner backfill (dryRun): would have updated {} documents", pending.size());
            counters.wouldUpdate += pending.size();
            pending.clear();
            return;
        }
        int succeeded = indexRepository.bulkUpdateCurrentOwner(pending);
        counters.updated += succeeded;
        counters.failed += pending.size() - succeeded;
        pending.clear();
    }

    /** {@code businessService|state -> owner (documents)}, ordered by state for readability. */
    private Map<String, String> summarise(Map<String, Integer> documentsByStateKey,
                                          Map<String, CurrentOwner> ownerByStateKey) {
        Map<String, String> summary = new LinkedHashMap<>();
        documentsByStateKey.forEach((stateKey, count) -> {
            CurrentOwner owner = ownerByStateKey.get(stateKey);
            String ownerLabel = (owner == null) ? NO_OWNER
                    : owner.programRole() + " (" + owner.systemRole() + ")";
            summary.put(stateKey, ownerLabel + " x" + count);
        });
        return summary;
    }

    private int resolveBatchSize(Integer requested) {
        if (requested == null || requested < 1) {
            return DEFAULT_BATCH_SIZE;
        }
        return Math.min(requested, MAX_BATCH_SIZE);
    }

    private boolean equalOrBothBlank(String left, String right) {
        if (!StringUtils.hasText(left) && !StringUtils.hasText(right)) {
            return true;
        }
        return left != null && left.equals(right);
    }

    private String text(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return StringUtils.hasText(value) ? value : null;
    }

    /** Mutable tally of a run, kept in one place so progress logging stays a single line. */
    private static class Counters {
        private int processed;
        private int resolved;
        private int unresolved;
        private int unchanged;
        private int updated;
        private int failed;
        private int skipped;
        private int wouldUpdate;

        @Override
        public String toString() {
            return String.format(
                    "processed=%d, resolved=%d, unresolved=%d, unchanged=%d, updated=%d, failed=%d, skipped=%d",
                    processed, resolved, unresolved, unchanged, updated, failed, skipped);
        }
    }
}
