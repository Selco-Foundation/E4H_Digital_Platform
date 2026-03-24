package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.im.config.IMConfiguration;
import org.egov.im.producer.Producer;
import org.egov.im.util.IMConstants;
import org.egov.im.util.IMUtils;
import org.egov.im.web.models.AuditDetails;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.stereotype.Service;

/**
 * Maintains facility_rms_inactive_incident: one record per RMS/Theft incident when the ticket is
 * in an "inactive" (open) state. Insert on create/re-open; delete on resolve/decline/close.
 */
@Slf4j
@Service
public class RmsInactiveIncidentService {

    private final IMUtils imUtils;
    private IMConfiguration config;
    private Producer producer;

    public RmsInactiveIncidentService(IMUtils imUtils, IMConfiguration config, Producer producer) {
        this.imUtils = imUtils;
        this.config = config;
        this.producer = producer;
    }

    /**
     * Call after ticket create: insert one record for RMS or Theft tickets.
     */
    public void onIncidentCreated(IncidentRequest request) {
        Incident incident = request.getIncident();
        if (!isRmsOrTheftTicket(incident)) {
            return;
        }
        insertRecord(request);
    }

    /**
     * Call after ticket update: insert on re-open, delete when resolved/declined/closed after resolution/declined after resolution.
     */
    public void onIncidentUpdated(IncidentRequest request, String workflowAction) {
        Incident incident = request.getIncident();
        if (!isRmsOrTheftTicket(incident)) {
            return;
        }
        String newStatus = incident.getApplicationStatus();
        if (newStatus == null) {
            return;
        }
        String upperStatus = newStatus.toUpperCase();

        if (isClosedStatus(upperStatus)) {
            producer.push(incident.getTenantId(),config.getDeleteRmsInactiveIncident(),request);
            log.debug("Deleted facility_rms_inactive_incident for RMS/Theft incidentId={} status={}", incident.getIncidentId(), upperStatus);
        } else if (isReopenAction(workflowAction)) {
            insertRecord(request);
            log.debug("Inserted facility_rms_inactive_incident for re-opened RMS/Theft incidentId={}", incident.getIncidentId());
        }
    }

    private boolean isRmsOrTheftTicket(Incident incident) {
        String type = incident.getIncidentType();
        if (StringUtils.isBlank(type)) {
            return false;
        }
        return type.trim().equalsIgnoreCase(IMConstants.TICKET_TYPE_RMS)
                || type.trim().equalsIgnoreCase(IMConstants.TICKET_TYPE_THEFT);
    }

    private boolean isClosedStatus(String status) {
        if (status == null) return false;
        return IMConstants.RESOLVED.equals(status)
                || IMConstants.CLOSED_AFTER_RESOLUTION.equals(status)
                || IMConstants.CLOSED_AFTER_REJECTION.equals(status)
                || IMConstants.REJECTED.equals(status);
    }

    private boolean isReopenAction(String action) {
        if (StringUtils.isBlank(action)) {
            return false;
        }
        return action.toUpperCase().startsWith("REOPEN");
    }

    private void insertRecord(IncidentRequest request) {
        Incident incident = request.getIncident();
        String incidentId = incident.getIncidentId();
        String boundaryCode = incident.getBoundaryCode();
        String facilityId = incident.getFacilityId();
        if (StringUtils.isBlank(facilityId) && StringUtils.isNotBlank(boundaryCode)) {
            facilityId = imUtils.extractFacilityCode(boundaryCode);
        }
        if (StringUtils.isBlank(facilityId)) {
            log.warn("Cannot insert facility_rms_inactive_incident: no facilityId for incidentId={}", incidentId);
            return;
        }
        incident.setFacilityId(facilityId);
        producer.push(incident.getTenantId(),config.getSaveRmsInactiveIncident(),request);
    }
}
