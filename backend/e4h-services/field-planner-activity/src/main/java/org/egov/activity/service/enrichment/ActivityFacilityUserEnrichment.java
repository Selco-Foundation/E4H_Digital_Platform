package org.egov.activity.service.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.repository.ActivityFacilityRepository;
import org.egov.activity.util.ActivityServiceUtil;
import org.egov.activity.web.models.ActivityFacilityUser;
import org.egov.activity.web.models.BillOfMaterial;
import org.egov.activity.web.models.Document;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.service.IdGenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityFacilityUserEnrichment {

    private final ActivityServiceUtil fieldPlanServiceUtil;

    public static final String FOR_BOM = " for BOM ";

    @Autowired
    ActivityFacilityRepository activityFacilityRepository;

    private final IdGenService idGenService;

    private final ActivityConfiguration config;

    /* Enrich Activity Facility Users on Create Request */
    public void enrichActivityFacilityUserOnCreate(ActivityFacilityUser facilityUser, RequestInfo requestInfo) {
        //Enrich Project id and audit details
        enrichActivityFacilityUserRequestOnCreate(facilityUser, requestInfo);
        log.info("Enriched Activity Facility User request with id and Audit details");

    }

    /* Enrich FieldPlan with id and audit details */
    private void enrichActivityFacilityUserRequestOnCreate(ActivityFacilityUser facilityUser, RequestInfo requestInfo) {
        facilityUser.setId(UUID.randomUUID().toString());
        log.info("Facility user id set to " + facilityUser.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        facilityUser.setAuditDetails(auditDetails);
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichActivityFacilityUserRequestOnUpdate(ActivityFacilityUser facilityUser, RequestInfo requestInfo) {
        AuditDetails auditDetails = AuditDetails.builder().lastModifiedBy(requestInfo.getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis()).build();;
        facilityUser.setAuditDetails(auditDetails);
        log.info("Enriched activity audit details for project " + facilityUser.getId());
    }

    private void setUUIDAndAuditDetailsForDocumentCreate(Document document, RequestInfo requestInfo, BillOfMaterial billOfMaterial) {
        document.setId(UUID.randomUUID().toString());
        AuditDetails auditDetailsForAdd = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        document.setAuditDetails(auditDetailsForAdd);
        log.info("Added document with id " + document.getId() + FOR_BOM + billOfMaterial.getId());
    }


}
