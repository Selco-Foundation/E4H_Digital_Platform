package org.egov.field_planner.service.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.Project;
import org.egov.common.producer.Producer;
import org.egov.common.service.IdGenService;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.util.FieldPlannerServiceUtil;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.FieldPlanFacility;
import org.egov.field_planner.web.models.FieldPlanFacilityBulkRequest;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.egov.common.utils.CommonUtils.enrichForCreate;
import static org.egov.common.utils.CommonUtils.getTenantId;
import static org.egov.field_planner.util.FieldPlannerConstants.DRAFT_STATUS;

@Service
@Slf4j
@RequiredArgsConstructor
public class FieldPlannerEnrichment {

    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String FOR_PROJECT = " for project ";
    private final FieldPlannerServiceUtil fieldPlanServiceUtil;

    private final Producer producer;

    private final FieldPlannerConfiguration fieldPlannerConfiguration;

    private final IdGenService idGenService;

    private final FieldPlannerConfiguration config;

    /* Enrich Project on Create Request */
    public void enrichFieldPlanOnCreate(FieldPlan fieldPlan, RequestInfo requestInfo) {
        log.trace("Entering enrichFieldPlanOnCreate method");
        enrichFieldPlanRequestOnCreate(fieldPlan, requestInfo);
        log.info("Field plan enriched with ID and audit details, field plan ID: {}", fieldPlan.getId());
        log.trace("Exiting enrichFieldPlanOnCreate method");
    }

    /* Enrich FieldPlan with id and audit details */
    private void enrichFieldPlanRequestOnCreate(FieldPlan fieldPlan, RequestInfo requestInfo) {
        log.trace("Entering enrichFieldPlanRequestOnCreate method");
        fieldPlan.setId(UUID.randomUUID().toString());
        fieldPlan.setStatus(DRAFT_STATUS);
        log.debug("Field plan ID generated: {}, status set to: {}", fieldPlan.getId(), DRAFT_STATUS);
        
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        fieldPlan.setAuditDetails(auditDetails);
        log.debug("Audit details set for field plan ID: {}", fieldPlan.getId());
        log.trace("Exiting enrichFieldPlanRequestOnCreate method");
    }

    public void enrichFieldPlanFacilityOnCreate(List<FieldPlanFacility> entities, FieldPlanFacilityBulkRequest request) throws Exception {
        log.trace("Entering enrichFieldPlanFacilityOnCreate method");
        log.info("Starting enrichment for field plan facility creation, entity count: {}", entities.size());

        log.debug("Generating IDs using IdGenService for {} entities", entities.size());
        List<String> idList = idGenService.getIdList(request.getRequestInfo(),
                getTenantId(entities),
                fieldPlannerConfiguration.getFieldPlanFacilityIdFormat(), "", entities.size());
        log.debug("Generated {} IDs for field plan facilities", idList.size());

        enrichForCreate(entities, idList, request.getRequestInfo());
        log.info("Field plan facility enrichment completed successfully");
        log.trace("Exiting enrichFieldPlanFacilityOnCreate method");
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichFieldPlanRequestOnUpdate(FieldPlan fieldPlan, FieldPlan fieldPlanFromDB, RequestInfo requestInfo) {
        log.trace("Entering enrichFieldPlanRequestOnUpdate method for field plan ID: {}", fieldPlan.getId());
        fieldPlan.setAuditDetails(fieldPlanFromDB.getAuditDetails());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), fieldPlanFromDB.getAuditDetails(), false);
        fieldPlan.setAuditDetails(auditDetails);
        log.info("Field plan audit details enriched for field plan ID: {}", fieldPlan.getId());
        log.trace("Exiting enrichFieldPlanRequestOnUpdate method");
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichFieldPlanFacilityRequestOnDelete(FieldPlanFacility fieldPlan, RequestInfo requestInfo) {
        log.trace("Entering enrichFieldPlanFacilityRequestOnDelete method for field plan facility ID: {}", fieldPlan.getId());
        AuditDetails auditDetails = AuditDetails.builder().lastModifiedBy(requestInfo.getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis()).build();
        fieldPlan.setAuditDetails(auditDetails);
        log.info("Field plan facility audit details enriched for facility ID: {}", fieldPlan.getId());
        log.trace("Exiting enrichFieldPlanFacilityRequestOnDelete method");
    }


}
