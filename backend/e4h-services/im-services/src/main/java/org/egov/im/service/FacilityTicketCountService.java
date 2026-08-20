package org.egov.im.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.im.config.IMConfiguration;
import org.egov.im.producer.Producer;
import org.egov.im.repository.IMRepository;
import org.egov.im.web.models.FacilityTicketCountEvent;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.stereotype.Service;

/**
 * Publishes a facility's running ticket total onto {@code facility-ticket-count} whenever a ticket is
 * raised, so downstream modules can display the figure without querying the incident table.
 *
 * <p>Today the only consumer is amc-scheduler-service, which stamps the total onto the facility's
 * latest non-DRAFT AMC visit in the scheduled-visit search index.
 */
@Slf4j
@Service
public class FacilityTicketCountService {

    private final IMRepository repository;
    private final IMConfiguration config;
    private final Producer producer;

    public FacilityTicketCountService(IMRepository repository, IMConfiguration config, Producer producer) {
        this.repository = repository;
        this.config = config;
        this.producer = producer;
    }

    /**
     * Call after a ticket create: publishes the facility's ticket total including the new ticket.
     */
    public void onIncidentCreated(IncidentRequest request) {
        Incident incident = request.getIncident();
        // Set by EnrichmentService from the facility registry lookup on boundaryCode. No fallback to
        // IMUtils#extractFacilityCode here: that yields the "FAC/..." facility code, whereas both the
        // incident count and the AMC visit lookup key off facility(id).
        String facilityId = incident.getFacilityId();
        if (StringUtils.isBlank(facilityId)) {
            log.warn("Skipping facility ticket count event: no facilityId for incidentId={}", incident.getIncidentId());
            return;
        }

        // The incident just created is written by egov-persister off save-im-request, so it is not in
        // the table yet - count what is persisted and add the one in flight.
        int totalTickets = repository.countTicketsByFacility(incident.getTenantId(), facilityId) + 1;

        FacilityTicketCountEvent event = FacilityTicketCountEvent.builder()
                .tenantId(incident.getTenantId())
                .facilityId(facilityId)
                .incidentId(incident.getIncidentId())
                .totalTickets(totalTickets)
                .build();
        producer.push(incident.getTenantId(), config.getFacilityTicketCountTopic(), event);
        log.info("Published facility ticket count: facilityId={} totalTickets={} incidentId={}",
                facilityId, totalTickets, incident.getIncidentId());
    }
}
