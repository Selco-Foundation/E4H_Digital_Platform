package org.egov.im.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.im.repository.IMRepository;
import org.egov.im.web.models.OnHoldBackfillRequest;
import org.egov.im.web.models.OnHoldBackfillResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.egov.im.util.IMConstants.SYSTEM_FUNCTIONAL_ON_HOLD;

/**
 * Brings tickets that were already in {@code OUT_OF_WARRANTY_ON_HOLD} before systemFunctional=ON_HOLD
 * existed in line with live ones.
 * <p>
 * Sets ON_HOLD (saving the previous value) in the database in one statement, then re-publishes every
 * on-hold ticket to the indexer topic. That refreshes the ticket index, and im-services-analytics
 * recomputes and re-publishes each ticket's facility document (solarPanelStatus) from the same message.
 * Safe to re-run: tickets already ON_HOLD are not rewritten, and re-publishing is idempotent.
 */
@Service
@Slf4j
public class OnHoldBackfillService {

    private final IMRepository repository;
    private final IMService imService;

    @Autowired
    public OnHoldBackfillService(IMRepository repository, IMService imService) {
        this.repository = repository;
        this.imService = imService;
    }

    public OnHoldBackfillResponse backfill(OnHoldBackfillRequest request) {
        log.trace("OnHoldBackfillService::backfill method invoked");
        String tenantId = request.getTenantId();
        boolean dryRun = Boolean.TRUE.equals(request.getDryRun());
        log.info("ON_HOLD backfill starting: tenantId={}, dryRun={}", tenantId, dryRun);

        List<Map<String, Object>> tickets = repository.fetchOnHoldTickets(tenantId);
        Map<String, Integer> previous = new LinkedHashMap<>();
        Set<String> boundaryCodes = new LinkedHashSet<>();
        int toUpdate = 0;
        for (Map<String, Object> ticket : tickets) {
            Object value = ticket.get("systemfunctional");
            previous.merge(value == null ? "NULL" : value.toString(), 1, Integer::sum);
            if (!SYSTEM_FUNCTIONAL_ON_HOLD.equals(value)) {
                toUpdate++;
            }
            if (ticket.get("boundarycode") != null) {
                boundaryCodes.add(ticket.get("boundarycode").toString());
            }
        }

        OnHoldBackfillResponse response = OnHoldBackfillResponse.builder()
                .dryRun(dryRun)
                .onHoldTickets(tickets.size())
                .previousSystemFunctional(previous)
                .facilities(boundaryCodes.size())
                .reindexed(0)
                .failedIncidentIds(new ArrayList<>())
                .build();

        if (dryRun) {
            response.setUpdated(toUpdate);
            log.info("ON_HOLD backfill dry run: onHoldTickets={}, wouldUpdate={}, previous={}",
                    tickets.size(), toUpdate, previous);
            return response;
        }

        response.setUpdated(repository.markOnHoldTicketsSystemFunctional(tenantId));

        int reindexed = 0;
        for (Map<String, Object> ticket : tickets) {
            String incidentId = (String) ticket.get("incidentid");
            try {
                imService.reindex(request.getRequestInfo(), (String) ticket.get("tenantid"), incidentId);
                reindexed++;
            } catch (Exception e) {
                log.error("ON_HOLD backfill: reindex failed for incidentId={}", incidentId, e);
                response.getFailedIncidentIds().add(incidentId);
            }
        }
        response.setReindexed(reindexed);
        log.info("ON_HOLD backfill finished: onHoldTickets={}, updated={}, reindexed={}, failed={}",
                tickets.size(), response.getUpdated(), reindexed, response.getFailedIncidentIds().size());
        return response;
    }
}
