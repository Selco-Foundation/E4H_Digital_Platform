package org.egov.activity.service.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.repository.ActivityRepository;
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
    ActivityRepository activityRepository;

    private final IdGenService idGenService;

    private final ActivityConfiguration config;

    /* Enrich Project on Create Request */
    public void enrichBomOnCreate(BillOfMaterial billOfMaterial, RequestInfo requestInfo) {
        //Enrich Project id and audit details
        enrichBomRequestOnCreate(billOfMaterial, requestInfo);
        //Enrich document id and audit details
        enrichBOMDocumentOnCreate(billOfMaterial, requestInfo);
        log.info("Enriched documents with id and Audit details");
        log.info("Enriched FieldPlan request with id and Audit details");

    }

    /* Enrich FieldPlan with id and audit details */
    private void enrichBomRequestOnCreate(BillOfMaterial billOfMaterial, RequestInfo requestInfo) {
        billOfMaterial.setId(UUID.randomUUID().toString());
        log.info("Bom id set to " + billOfMaterial.getId());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        billOfMaterial.setAuditDetails(auditDetails);
    }

    /* Enrich Project update request with last modified by and last modified time */
    public void enrichFieldPlanRequestOnUpdate(BillOfMaterial billOfMaterial, BillOfMaterial bomFromDB, RequestInfo requestInfo) {
        billOfMaterial.setAuditDetails(bomFromDB.getAuditDetails());
        AuditDetails auditDetails = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), bomFromDB.getAuditDetails(), false);
        billOfMaterial.setAuditDetails(auditDetails);
        log.info("Enriched activity audit details for project " + billOfMaterial.getId());
    }

    /* Enrich Document with id and audit details in create BOM request */
    private void enrichBOMDocumentOnCreate(BillOfMaterial billOfMaterial, RequestInfo requestInfo) {
        if (billOfMaterial.getDocuments() != null) {
            for (Document document : billOfMaterial.getDocuments()) {
                setUUIDAndAuditDetailsForDocumentCreate(document, requestInfo, billOfMaterial);
            }
        }
    }

    private void setUUIDAndAuditDetailsForDocumentCreate(Document document, RequestInfo requestInfo, BillOfMaterial billOfMaterial) {
        document.setId(UUID.randomUUID().toString());
        AuditDetails auditDetailsForAdd = fieldPlanServiceUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), null, true);
        document.setAuditDetails(auditDetailsForAdd);
        log.info("Added document with id " + document.getId() + FOR_BOM + billOfMaterial.getId());
    }


}
