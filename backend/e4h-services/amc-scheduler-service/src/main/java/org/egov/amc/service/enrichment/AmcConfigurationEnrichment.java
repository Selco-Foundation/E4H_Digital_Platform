package org.egov.amc.service.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AmcConfigurationAssignment;
import org.egov.amc.web.models.ScheduledVisit;
import org.egov.amc.web.models.ScheduledVisitAssignment;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.egov.common.service.IdGenService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmcConfigurationEnrichment {

    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String FOR_PROJECT = " for project ";
    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;

    private final Producer producer;

    private final AMCServiceConfiguration amcConfigurationnerConfiguration;

    private final IdGenService idGenService;

    private final AMCServiceConfiguration config;

    /* Enrich Project on Create Request */
    public void enrichAmcConfigurationOnCreate(AmcConfiguration amcConfiguration, RequestInfo requestInfo) {
        log.trace("Entering enrichAmcConfigurationOnCreate method");
        //Enrich Project id and audit details
        enrichAmcConfigurationRequestOnCreate(amcConfiguration, requestInfo);
        log.info("AMC configuration enriched with ID and audit details, configurationId: {}", amcConfiguration.getId());
    }

    /* Enrich amcConfiguration with id and audit details */
    private void enrichAmcConfigurationRequestOnCreate(AmcConfiguration amcConfiguration, RequestInfo requestInfo) {
        log.trace("Entering enrichAmcConfigurationRequestOnCreate method");
        amcConfiguration.setId(UUID.randomUUID().toString());
        log.debug("Generated AMC configuration ID: {}", amcConfiguration.getId());
        enrichUserAssignmentOnCreate(amcConfiguration, requestInfo);
        AuditDetails auditDetails = amcConfigurationServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        amcConfiguration.setAuditDetails(auditDetails);
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichAmcConfigurationRequestOnUpdate(AmcConfiguration amcConfiguration, AmcConfiguration amcConfigurationFromDB, RequestInfo requestInfo) {
        log.trace("Entering enrichAmcConfigurationRequestOnUpdate method for configurationId: {}", amcConfiguration.getId());
        amcConfiguration.setAuditDetails(amcConfigurationFromDB.getAuditDetails());
        AuditDetails auditDetails = amcConfigurationServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), amcConfigurationFromDB.getAuditDetails(), false);
        amcConfiguration.setAuditDetails(auditDetails);
        log.info("AMC configuration audit details enriched for configurationId: {}", amcConfiguration.getId());
    }

    /* Enrich Document with id and audit details in create BOM request */
    private void enrichUserAssignmentOnCreate(AmcConfiguration amcConfiguration, RequestInfo requestInfo) {
        if (amcConfiguration.getAssignments() != null) {
            for (AmcConfigurationAssignment assignment : amcConfiguration.getAssignments()) {
                setUUIDAndAuditDetailsForAssignmentCreate(assignment, requestInfo);
            }
        }
    }

    private void setUUIDAndAuditDetailsForAssignmentCreate(AmcConfigurationAssignment assignment, RequestInfo requestInfo) {
        log.trace("Entering setUUIDAndAuditDetailsForAssignmentCreate method");
        assignment.setId(UUID.randomUUID().toString());
        assignment.setActive(true);
        AuditDetails auditDetailsForAdd = amcConfigurationServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        assignment.setAuditDetails(auditDetailsForAdd);
        log.debug("Created AMC configuration assignment with ID: {}", assignment.getId());
    }


}
