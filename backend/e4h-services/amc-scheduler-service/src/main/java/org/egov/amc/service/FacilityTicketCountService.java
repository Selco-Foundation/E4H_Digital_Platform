package org.egov.amc.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.amc.repository.ScheduledVisitIndexRepository;
import org.egov.amc.repository.ScheduledVisitRepository;
import org.egov.amc.web.models.FacilityTicketCountEvent;
import org.egov.amc.web.models.ScheduledVisit;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Keeps a facility's ticket total visible alongside its AMC visit: on every {@code facility-ticket-count}
 * event from im-services, the total is stamped onto the facility's latest non-DRAFT visit - both on the
 * row and on its scheduled-visit index document.
 *
 * <p>Only the latest visit carries the figure. It is a live property of the facility rather than of any
 * one visit, so back-filling the whole history would misrepresent what was true at the time of each
 * earlier visit.
 */
@Slf4j
@Service
public class FacilityTicketCountService {

    private final ScheduledVisitRepository scheduledVisitRepository;
    private final ScheduledVisitIndexRepository scheduledVisitIndexRepository;

    public FacilityTicketCountService(ScheduledVisitRepository scheduledVisitRepository,
                                      ScheduledVisitIndexRepository scheduledVisitIndexRepository) {
        this.scheduledVisitRepository = scheduledVisitRepository;
        this.scheduledVisitIndexRepository = scheduledVisitIndexRepository;
    }

    public void applyTicketCount(FacilityTicketCountEvent event) {
        if (event == null || StringUtils.isBlank(event.getFacilityId()) || event.getTotalTickets() == null) {
            log.warn("Ignoring facility ticket count event with missing facilityId or totalTickets: {}", event);
            return;
        }

        Optional<ScheduledVisit> latestVisit =
                scheduledVisitRepository.getLatestNonDraftVisitByFacility(event.getTenantId(), event.getFacilityId());
        if (latestVisit.isEmpty()) {
            // Expected for facilities with no AMC configured, or whose visits are all still drafts.
            log.info("No non-DRAFT AMC visit for facilityId={}; skipping ticket count update", event.getFacilityId());
            return;
        }

        // The row is the source of truth and is written first, so the count survives a re-index even if
        // the Elasticsearch update below fails.
        ScheduledVisit visit = latestVisit.get();
        int rowsUpdated = scheduledVisitRepository.updateTotalTicketsOnVisit(visit.getId(), event.getTotalTickets());
        if (rowsUpdated == 0) {
            log.warn("Scheduled visit {} vanished before totalTickets could be stamped", visit.getId());
            return;
        }

        scheduledVisitIndexRepository.updateTotalTickets(visit.getId(), event.getTotalTickets());
        log.info("Stamped totalTickets={} on visitId={} for facilityId={} (incidentId={})",
                event.getTotalTickets(), visit.getId(), event.getFacilityId(), event.getIncidentId());
    }
}
