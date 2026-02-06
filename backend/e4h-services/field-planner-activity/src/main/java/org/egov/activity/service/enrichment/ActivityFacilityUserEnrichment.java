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
        log.trace("enrichActivityFacilityUserOnCreate method invoked");
        //Enrich Project id and audit details
        enrichActivityFacilityUserRequestOnCreate(facilityUser, requestInfo);
        log.debug("Enriched activity facility user with id: {}, userId: {}, activityFacilityId: {}", 
                facilityUser.getId(), facilityUser.getUserId(), facilityUser.getActivityFacilityId());
    }

    /* Enrich FieldPlan with id and audit details */
    private void enrichActivityFacilityUserRequestOnCreate(ActivityFacilityUser facilityUser, RequestInfo requestInfo) {
        log.trace("enrichActivityFacilityUserRequestOnCreate method invoked");
        facilityUser.setId(UUID.randomUUID().toString());
        log.debug("Activity facility user id set to: {}", facilityUser.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        facilityUser.setAuditDetails(auditDetails);
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichActivityFacilityUserRequestOnUpdate(ActivityFacilityUser facilityUser, RequestInfo requestInfo) {
        log.trace("enrichActivityFacilityUserRequestOnUpdate method invoked for activityFacilityUserId: {}", facilityUser.getId());
        AuditDetails auditDetails = AuditDetails.builder().lastModifiedBy(requestInfo.getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis()).build();;
        facilityUser.setAuditDetails(auditDetails);
        log.debug("Enriched activity facility user audit details for activityFacilityUserId: {}", facilityUser.getId());
    }

    private void setUUIDAndAuditDetailsForDocumentCreate(Document document, RequestInfo requestInfo, BillOfMaterial billOfMaterial) {
        log.trace("setUUIDAndAuditDetailsForDocumentCreate method invoked for bomId: {}", billOfMaterial.getId());
        document.setId(UUID.randomUUID().toString());
        AuditDetails auditDetailsForAdd = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        document.setAuditDetails(auditDetailsForAdd);
        log.debug("Added document with id: {} for BOM, bomId: {}", document.getId(), billOfMaterial.getId());
    }


}
