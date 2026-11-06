package org.egov.im.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.repository.IncidentIndexRepository;
import org.egov.im.service.WorkflowService.SlaSnapshot;
import org.egov.im.web.models.SlaBackfillRequest;
import org.egov.im.web.models.SlaBackfillResponse;
import org.egov.im.web.models.workflow.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.egov.im.util.IMConstants.IM_BUSINESSSERVICE;

/**
 * Writes the SLA anchor fields ({@code stateEnteredTime}, {@code totalSlaBase} and the deadlines) onto
 * open tickets indexed before im-services started producing them, along with fresh SLA values.
 * <p>
 * Live tickets get these on every workflow transition, so this is only needed once at rollout, and again
 * by hand whenever the SLA durations or the BusinessHours master change - the stored deadlines are
 * computed from both. Only the SLA fields are written, with a partial bulk update; nothing is
 * re-published. The BusinessHours master and each business service definition are fetched once per run;
 * the workflow history is fetched per ticket, which is what the SLA computation needs.
 */
@Service
@Slf4j
public class SlaBackfillService {

    private static final int DEFAULT_BATCH_SIZE = 200;
    private static final int MAX_BATCH_SIZE = 1000;
    private static final int PROGRESS_LOG_EVERY = 1000;

    private final IncidentIndexRepository indexRepository;
    private final WorkflowService workflowService;
    private final ObjectMapper mapper;

    @Autowired
    public SlaBackfillService(IncidentIndexRepository indexRepository, WorkflowService workflowService, ObjectMapper mapper) {
        this.indexRepository = indexRepository;
        this.workflowService = workflowService;
        this.mapper = mapper;
    }

    public SlaBackfillResponse backfill(SlaBackfillRequest request) {
        log.trace("SlaBackfillService::backfill method invoked");
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getTenantId();
        boolean dryRun = Boolean.TRUE.equals(request.getDryRun());
        int batchSize = resolveBatchSize(request.getBatchSize());
        Integer maxDocuments = request.getMaxDocuments();
        log.info("SLA backfill starting: tenantId={}, batchSize={}, maxDocuments={}, dryRun={}",
                tenantId, batchSize, maxDocuments, dryRun);

        List<Map<String, Object>> businessHours = workflowService.fetchBusinessHours(requestInfo, tenantId);
        Map<String, List<State>> statesByBusinessService = new HashMap<>();

        Counters counters = new Counters();
        Map<String, ObjectNode> pending = new LinkedHashMap<>();
        JsonNode searchAfter = null;

        while (maxDocuments == null || counters.processed < maxDocuments) {
            int pageSize = (maxDocuments == null) ? batchSize : Math.min(batchSize, maxDocuments - counters.processed);
            JsonNode hits = indexRepository.fetchSlaBackfillPage(pageSize, searchAfter);
            if (hits == null || hits.isEmpty()) {
                break;
            }

            for (JsonNode hit : hits) {
                counters.processed++;
                JsonNode data = hit.path("_source").path("Data");
                String documentId = text(hit.path("_id"));
                String incidentId = text(data.path("incident").path("incidentId"));
                String ticketTenantId = text(data.path("tenantId"));
                if (documentId == null || incidentId == null) {
                    log.warn("SLA backfill: hit without a document or incident id (documentId={}), skipping", documentId);
                    counters.skipped++;
                    continue;
                }

                String businessService = text(data.path("currentProcessInstance").path("businessService"));
                if (businessService == null) {
                    // Pre-priority tickets were all on the single "Incident" business service.
                    businessService = IM_BUSINESSSERVICE;
                }
                try {
                    List<State> states = statesByBusinessService.computeIfAbsent(businessService,
                            name -> workflowService.getBusinessServiceByName(requestInfo, tenantId, name).getStates());
                    SlaSnapshot sla = workflowService.computeSla(ticketTenantId != null ? ticketTenantId : tenantId,
                            incidentId, requestInfo, states, businessHours);
                    pending.put(documentId, toDataPatch(sla));
                } catch (Exception e) {
                    log.error("SLA backfill: could not compute SLA for incident {}", incidentId, e);
                    counters.failed++;
                }

                if (pending.size() >= batchSize) {
                    flush(pending, counters, dryRun);
                }
                if (counters.processed % PROGRESS_LOG_EVERY == 0) {
                    log.info("SLA backfill progress: {}", counters);
                }
            }

            searchAfter = hits.get(hits.size() - 1).path("sort");
            if (searchAfter.isMissingNode() || !searchAfter.isArray() || searchAfter.isEmpty()) {
                log.warn("SLA backfill: page returned no sort values, stopping after {} documents", counters.processed);
                break;
            }
            if (hits.size() < pageSize) {
                break;
            }
        }

        flush(pending, counters, dryRun);
        log.info("SLA backfill finished: {} (dryRun={})", counters, dryRun);

        return SlaBackfillResponse.builder()
                .dryRun(dryRun)
                .processed(counters.processed)
                .updated(counters.updated)
                .failed(counters.failed)
                .skipped(counters.skipped)
                .build();
    }

    /** The SLA fields under {@code Data}; anchors whose clock is not running are written as null to clear stale ones. */
    private ObjectNode toDataPatch(SlaSnapshot sla) {
        ObjectNode data = mapper.createObjectNode();
        data.put("definedTotalSla", sla.definedTotalSla());
        putOrNull(data, "stateSla", sla.stateSla());
        data.put("slaRemaining", sla.slaRemaining());
        data.put("totalSlaRemaining", sla.totalSlaRemaining());
        data.put("stateEnteredTime", sla.stateEnteredTime());
        putOrNull(data, "totalSlaBase", sla.totalSlaBase());
        putOrNull(data, "stateSlaDueAt", sla.stateSlaDueAt());
        putOrNull(data, "stateSlaWarnAt", sla.stateSlaWarnAt());
        putOrNull(data, "totalSlaDueAt", sla.totalSlaDueAt());
        return data;
    }

    private void flush(Map<String, ObjectNode> pending, Counters counters, boolean dryRun) {
        if (pending.isEmpty()) {
            return;
        }
        if (dryRun) {
            log.info("SLA backfill (dryRun): would have updated {} documents", pending.size());
            counters.updated += pending.size();
        } else {
            int succeeded = indexRepository.bulkUpdateData(pending);
            counters.updated += succeeded;
            counters.failed += pending.size() - succeeded;
        }
        pending.clear();
    }

    private void putOrNull(ObjectNode target, String field, Long value) {
        if (value != null) {
            target.put(field, value);
        } else {
            target.putNull(field);
        }
    }

    private int resolveBatchSize(Integer requested) {
        if (requested == null || requested < 1) {
            return DEFAULT_BATCH_SIZE;
        }
        return Math.min(requested, MAX_BATCH_SIZE);
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
        private int updated;
        private int failed;
        private int skipped;

        @Override
        public String toString() {
            return String.format("processed=%d, updated=%d, failed=%d, skipped=%d", processed, updated, failed, skipped);
        }
    }
}
