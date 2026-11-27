package org.egov.im.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.repository.IMRepository;
import org.egov.im.web.models.DuplicateTicket;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.RequestSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.*;

/**
 * Duplicate check by component (SEM 4.0 Ticket Reporting): a ticket is a duplicate of an open
 * ticket of the same facility (boundaryCode) and the same component, whoever raised it, and its
 * creation is refused. Support request components are never checked.
 */
@Slf4j
@Service
public class DuplicateCheckService {

    // Upper bound on the matches read back - only the newest one is linked, the rest are shown
    private static final int MAX_DUPLICATES = 20;

    private final IMRepository repository;

    @Value("#{'${workflow.ticket.open.statuses}'.split(',')}")
    private Set<String> openTicketStatuses;

    @Autowired
    public DuplicateCheckService(IMRepository repository) {
        this.repository = repository;
    }

    /**
     * Whether the component is checked for duplicates: every component except a support request.
     */
    public boolean isApplicable(String component, Object mdmsData) {
        if (!StringUtils.hasText(component)) {
            return false;
        }
        return !COMPONENT_CATEGORY_SUPPORT_REQUEST.equals(getComponentCategory(component, mdmsData));
    }

    /**
     * Open tickets of the facility for the component, newest first.
     */
    public List<Incident> findOpenTickets(String tenantId, String boundaryCode, String component) {
        RequestSearchCriteria criteria = RequestSearchCriteria.builder()
                .tenantId(tenantId)
                .boundaryCode(boundaryCode)
                .component(Collections.singleton(component))
                .applicationStatus(openTicketStatuses)
                .sortOrder(RequestSearchCriteria.SortOrder.DESC)
                .limit(MAX_DUPLICATES)
                .offset(0)
                .build();
        List<Incident> incidents = repository.getIncidents(criteria);
        log.debug("Found {} open tickets for boundaryCode={} component={}", incidents.size(), boundaryCode, component);
        return incidents;
    }

    public List<DuplicateTicket> toDuplicateTickets(List<Incident> incidents) {
        return incidents.stream()
                .map(incident -> DuplicateTicket.builder()
                        .incidentId(incident.getIncidentId())
                        .component(incident.getComponent())
                        .applicationStatus(incident.getApplicationStatus())
                        .createdTime(incident.getAuditDetails() != null ? incident.getAuditDetails().getCreatedTime() : null)
                        .reporterType(incident.getReporterType())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Applies the duplicate check to a ticket being created: refused for everyone while an open
     * ticket exists for the same facility and component.
     */
    public void checkOnCreate(IncidentRequest request, Object mdmsData) {
        Incident incident = request.getIncident();
        incident.setPotentialDuplicate(false);
        if (!isApplicable(incident.getComponent(), mdmsData)) {
            return;
        }
        List<Incident> matches = findOpenTickets(incident.getTenantId(), incident.getBoundaryCode(), incident.getComponent());
        if (CollectionUtils.isEmpty(matches)) {
            return;
        }
        List<String> matchIds = matches.stream().map(Incident::getIncidentId).collect(Collectors.toList());
        log.info("Ticket for boundaryCode={} component={} refused, open tickets: {}",
                incident.getBoundaryCode(), incident.getComponent(), matchIds);
        throw new CustomException("DUPLICATE_TICKET",
                "An open ticket already exists for this facility and component " + incident.getComponent() + ": " + matchIds);
    }

    private Object getComponentCategory(String component, Object mdmsData) {
        if (mdmsData == null) {
            return null;
        }
        try {
            List<Object> components = JsonPath.read(mdmsData, MDMS_COMPONENT_SEARCH.replace("{CODE}", component));
            if (CollectionUtils.isEmpty(components) || !(components.get(0) instanceof Map)) {
                return null;
            }
            return ((Map<?, ?>) components.get(0)).get("category");
        } catch (Exception e) {
            throw new CustomException("JSONPATH_ERROR", "Failed to parse mdms response");
        }
    }
}
