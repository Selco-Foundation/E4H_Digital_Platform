package org.egov.amc.service.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.service.AmcConfigurationService;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.web.models.ScheduledVisit;
import org.egov.amc.web.models.ScheduledVisitAssignment;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledVisitEnrichment {

    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String FOR_PROJECT = " for project ";
    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;

    @Autowired
    private final AmcConfigurationService amcConfigurationService;

    /* Enrich Project on Create Request */
    public void enrichScheduledVisitOnCreate(ScheduledVisit scheduledVisit, RequestInfo requestInfo) {
        log.trace("Entering enrichScheduledVisitOnCreate method");
        //Enrich Project id and audit details
        enrichScheduledVisitRequestOnCreate(scheduledVisit, requestInfo);
        //Enrich document id and audit details
        enrichUserAssignmentOnCreate(scheduledVisit, requestInfo);
        log.info("Scheduled visit enriched with ID and audit details, visitId: {}", scheduledVisit.getId());
    }

    /* Enrich ScheduledVisit with id and audit details */
    private void enrichScheduledVisitRequestOnCreate(ScheduledVisit scheduledVisit, RequestInfo requestInfo) {
        log.trace("Entering enrichScheduledVisitRequestOnCreate method");
        scheduledVisit.setId(UUID.randomUUID().toString());
        log.debug("Generated scheduled visit ID: {}", scheduledVisit.getId());
        if (scheduledVisit.getStatus()==null || scheduledVisit.getStatus().isEmpty()) {
            scheduledVisit.setStatus("DRAFT");
            log.debug("Set default status DRAFT for scheduled visit ID: {}", scheduledVisit.getId());
        }
        AuditDetails auditDetails = amcConfigurationServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        scheduledVisit.setAuditDetails(auditDetails);
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichScheduledVisitRequestOnUpdate(ScheduledVisit scheduledVisit, ScheduledVisit scheduledVisitFromDB, RequestInfo requestInfo) {
        log.trace("Entering enrichScheduledVisitRequestOnUpdate method for visitId: {}", scheduledVisit.getId());
        scheduledVisit.setAuditDetails(scheduledVisitFromDB.getAuditDetails());
        AuditDetails auditDetails = amcConfigurationServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), scheduledVisitFromDB.getAuditDetails(), false);
        scheduledVisit.setAuditDetails(auditDetails);
        log.info("Scheduled visit audit details enriched for visitId: {}", scheduledVisit.getId());
    }

    /* Enrich Document with id and audit details in create BOM request */
    private void enrichUserAssignmentOnCreate(ScheduledVisit scheduledVisit, RequestInfo requestInfo) {
        if (scheduledVisit.getAssignments() != null) {
            for (ScheduledVisitAssignment document : scheduledVisit.getAssignments()) {
                setUUIDAndAuditDetailsForAssignmentCreate(document, requestInfo, scheduledVisit);
            }
        }
    }

    private void setUUIDAndAuditDetailsForAssignmentCreate(ScheduledVisitAssignment document, RequestInfo requestInfo, ScheduledVisit scheduledVisit) {
        log.trace("Entering setUUIDAndAuditDetailsForAssignmentCreate method");
        document.setId(UUID.randomUUID().toString());
        document.setActive(true);
        AuditDetails auditDetailsForAdd = amcConfigurationServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        document.setAuditDetails(auditDetailsForAdd);
        log.debug("Created assignment with ID: {} for scheduled visit ID: {}", document.getId(), scheduledVisit.getId());
    }


}
