package org.egov.activity.service.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.repository.ActivityFacilityRepository;
import org.egov.activity.util.ActivityServiceUtil;
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
public class BomEnrichment {

    private final ActivityServiceUtil fieldPlanServiceUtil;

    public static final String FOR_BOM = " for BOM ";

    @Autowired
    ActivityFacilityRepository activityFacilityRepository;

    private final IdGenService idGenService;

    private final ActivityConfiguration config;

    /* Enrich Project on Create Request */
    public void enrichBomOnCreate(BillOfMaterial billOfMaterial, RequestInfo requestInfo) {
        log.trace("enrichBomOnCreate method invoked");
        //Enrich Project id and audit details
        enrichBomRequestOnCreate(billOfMaterial, requestInfo);
        //Enrich document id and audit details
        enrichBOMDocumentOnCreate(billOfMaterial, requestInfo);
        int documentCount = billOfMaterial.getDocuments() != null ? billOfMaterial.getDocuments().size() : 0;
        log.debug("Enriched BOM with id: {}, documents count: {}", billOfMaterial.getId(), documentCount);
    }

    /* Enrich FieldPlan with id and audit details */
    private void enrichBomRequestOnCreate(BillOfMaterial billOfMaterial, RequestInfo requestInfo) {
        log.trace("enrichBomRequestOnCreate method invoked");
        billOfMaterial.setId(UUID.randomUUID().toString());
        log.debug("BOM id set to: {}", billOfMaterial.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        billOfMaterial.setAuditDetails(auditDetails);
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichFieldPlanRequestOnUpdate(BillOfMaterial billOfMaterial, BillOfMaterial bomFromDB, RequestInfo requestInfo) {
        log.trace("enrichFieldPlanRequestOnUpdate method invoked for bomId: {}", billOfMaterial.getId());
        billOfMaterial.setAuditDetails(bomFromDB.getAuditDetails());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), bomFromDB.getAuditDetails(), false);
        billOfMaterial.setAuditDetails(auditDetails);
        log.debug("Enriched BOM audit details for bomId: {}", billOfMaterial.getId());
    }

    /* Enrich Document with id and audit details in create BOM request */
    private void enrichBOMDocumentOnCreate(BillOfMaterial billOfMaterial, RequestInfo requestInfo) {
        log.trace("enrichBOMDocumentOnCreate method invoked for bomId: {}", billOfMaterial.getId());
        if (billOfMaterial.getDocuments() != null) {
            int documentCount = billOfMaterial.getDocuments().size();
            log.debug("Enriching {} documents for BOM, bomId: {}", documentCount, billOfMaterial.getId());
            for (Document document : billOfMaterial.getDocuments()) {
                setUUIDAndAuditDetailsForDocumentCreate(document, requestInfo, billOfMaterial);
            }
        }
    }

    private void setUUIDAndAuditDetailsForDocumentCreate(Document document, RequestInfo requestInfo, BillOfMaterial billOfMaterial) {
        log.trace("setUUIDAndAuditDetailsForDocumentCreate method invoked for bomId: {}", billOfMaterial.getId());
        document.setId(UUID.randomUUID().toString());
        AuditDetails auditDetailsForAdd = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        document.setAuditDetails(auditDetailsForAdd);
        log.debug("Added document with id: {} for BOM, bomId: {}", document.getId(), billOfMaterial.getId());
    }


}
