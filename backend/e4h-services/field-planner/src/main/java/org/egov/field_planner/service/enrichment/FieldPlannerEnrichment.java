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
        //Enrich Project id and audit details
        enrichFieldPlanRequestOnCreate(fieldPlan, requestInfo);
        log.info("Enriched FieldPlan request with id and Audit details");

    }

    /* Enrich FieldPlan with id and audit details */
    private void enrichFieldPlanRequestOnCreate(FieldPlan fieldPlan, RequestInfo requestInfo) {
        fieldPlan.setId(UUID.randomUUID().toString());
        fieldPlan.setStatus(DRAFT_STATUS);
        log.info("fieldPlan id set to " + fieldPlan.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        fieldPlan.setAuditDetails(auditDetails);
    }

    public void enrichFieldPlanFacilityOnCreate(List<FieldPlanFacility> entities, FieldPlanFacilityBulkRequest request) throws Exception {
        log.info("starting the enrichment for create project facility");

        log.info("generating IDs using IdGenService");
        List<String> idList = idGenService.getIdList(request.getRequestInfo(),
                getTenantId(entities),
                fieldPlannerConfiguration.getFieldPlanFacilityIdFormat(), "", entities.size());

        enrichForCreate(entities, idList, request.getRequestInfo());
        log.info("enrichment done");
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichFieldPlanRequestOnUpdate(FieldPlan fieldPlan, FieldPlan fieldPlanFromDB, RequestInfo requestInfo) {
        fieldPlan.setAuditDetails(fieldPlanFromDB.getAuditDetails());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), fieldPlanFromDB.getAuditDetails(), false);
        fieldPlan.setAuditDetails(auditDetails);
        log.info("Enriched project audit details for project " + fieldPlan.getId());
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichFieldPlanFacilityRequestOnDelete(FieldPlanFacility fieldPlan, RequestInfo requestInfo) {
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), fieldPlan.getAuditDetails(), false);
        fieldPlan.setAuditDetails(auditDetails);
        log.info("Enriched project audit details for project " + fieldPlan.getId());
    }


}
