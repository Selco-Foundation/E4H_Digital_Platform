package org.egov.infra.mdms.service.enrichment;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.egov.infra.mdms.model.SchemaDefinition;
import org.egov.infra.mdms.model.SchemaDefinitionRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Component
@Slf4j
public class SchemaDefinitionEnricher {

    /**
     * This method enriches schemaDefinitionRequest
     * @param schemaDefinitionRequest
     */
    public void enrichCreateRequest(SchemaDefinitionRequest schemaDefinitionRequest) {
        log.trace("SchemaDefinitionEnricher.enrichCreateRequest: method invoked");
        SchemaDefinition schemaDefinition = schemaDefinitionRequest.getSchemaDefinition();
        String tenantId = schemaDefinition != null ? schemaDefinition.getTenantId() : "null";
        String code = schemaDefinition != null ? schemaDefinition.getCode() : "null";
        log.info("Enriching schema definition create request for tenant: {}, code: {}", tenantId, code);

        // Invoke enrichment of uuid on id field of schemaDefinition
        log.debug("Enriching UUID for schema definition");
        UUIDEnrichmentUtil.enrichRandomUuid(schemaDefinition, "id");

        // Populate auditDetails
        log.debug("Enriching audit details for schema definition");
        schemaDefinition.setAuditDetails(getAuditDetail(schemaDefinitionRequest.getRequestInfo(),schemaDefinition.getAuditDetails(), true));
        log.info("Schema definition create request enrichment completed successfully");
    }

    public AuditDetails getAuditDetail(RequestInfo requestInfo, AuditDetails auditDetails, Boolean isCreateRequest) {
        log.trace("SchemaDefinitionEnricher.getAuditDetail: method invoked, isCreateRequest: {}", isCreateRequest);
        if(isCreateRequest) {
            log.debug("Creating audit details for create request");
            auditDetails = AuditDetails.builder().createdBy(requestInfo.getUserInfo().getUuid()).
                    createdTime(System.currentTimeMillis()).lastModifiedBy(requestInfo.getUserInfo().getUuid()).
                    lastModifiedTime(System.currentTimeMillis()).build();
        } else {
            if(auditDetails != null ) {
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

}
