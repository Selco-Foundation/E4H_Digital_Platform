package org.egov.infra.mdms.service.enrichment;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.egov.infra.mdms.model.Mdms;
import org.egov.infra.mdms.model.MdmsRequest;
import org.egov.infra.mdms.utils.CompositeUniqueIdentifierGenerationUtil;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MdmsDataEnricher {

    public void enrichCreateRequest(MdmsRequest mdmsRequest, JSONObject schemaObject) {
        log.trace("MdmsDataEnricher.enrichCreateRequest: method invoked");
        Mdms mdms = mdmsRequest.getMdms();
        String tenantId = mdms != null ? mdms.getTenantId() : "null";
        log.info("Enriching MDMS create request for tenant: {}", tenantId);
        
        log.debug("Enriching UUID for master data");
        UUIDEnrichmentUtil.enrichRandomUuid(mdms, "id");
        
        log.debug("Enriching audit details");
        mdms.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(mdms.getAuditDetails(), mdmsRequest.getRequestInfo(), Boolean.TRUE));
        
        log.debug("Generating unique identifier");
        mdms.setUniqueIdentifier(CompositeUniqueIdentifierGenerationUtil.getUniqueIdentifier(schemaObject, mdmsRequest));
        log.info("MDMS create request enrichment completed successfully");
    }

    public AuditDetails getAuditDetails(RequestInfo requestInfo, AuditDetails auditDetails, Boolean isCreateRequest) {
        log.trace("MdmsDataEnricher.getAuditDetails: method invoked, isCreateRequest: {}", isCreateRequest);
        if(isCreateRequest) {
            log.debug("Creating audit details for create request");
            auditDetails = AuditDetails.builder().createdBy(requestInfo.getUserInfo().getUuid()).
                    createdTime(System.currentTimeMillis()).lastModifiedBy(requestInfo.getUserInfo().getUuid()).
                    lastModifiedTime(System.currentTimeMillis()).build();
        } else {
            if(auditDetails != null) {
                log.debug("Updating audit details for update request");
                auditDetails = AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).
                        createdTime(auditDetails.getCreatedTime()).lastModifiedBy(requestInfo.getUserInfo().getUuid()).
                        lastModifiedTime(System.currentTimeMillis()).build();
            } else {
                log.error("Audit details are null for update request");
                throw new CustomException("AUDIT_DETAILS_NULL_FOR_UPDATE_REQ","Audit details can not be null for update request");
            }
        }
        return auditDetails;
    }

    public void enrichUpdateRequest(MdmsRequest mdmsRequest) {
        log.trace("MdmsDataEnricher.enrichUpdateRequest: method invoked");
        Mdms mdms = mdmsRequest.getMdms();
        String tenantId = mdms != null ? mdms.getTenantId() : "null";
        String id = mdms != null ? mdms.getId() : "null";
        log.info("Enriching MDMS update request for tenant: {}, id: {}", tenantId, id);

        if(ObjectUtils.isEmpty(mdms.getAuditDetails())) {
            log.error("Audit details are absent for update request");
            throw new CustomException("AUDIT_DETAILS_ABSENT_ERR", "Audit details cannot be absent for update request");
        }

        log.debug("Updating audit details for update request");
        mdms.setAuditDetails(getAuditDetails(mdmsRequest.getRequestInfo(), mdms.getAuditDetails(), Boolean.FALSE));
        log.info("MDMS update request enrichment completed successfully");
    }
}
