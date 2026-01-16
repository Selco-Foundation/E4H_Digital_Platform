package org.egov.infra.mdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.service.MDMSServiceV2;
import org.egov.infra.mdms.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/v2")
public class MDMSControllerV2 {

    private MDMSServiceV2 mdmsServiceV2;

    @Autowired
    public MDMSControllerV2(MDMSServiceV2 mdmsServiceV2) {
        this.mdmsServiceV2 = mdmsServiceV2;
    }

    /**
     * Request handler for serving create requests
     * @param mdmsRequest
     * @return
     */
    @PostMapping(value="/_create")
    public ResponseEntity<MdmsResponseV2> create(@Valid @RequestBody MdmsRequest mdmsRequest) {
        log.trace("MDMSControllerV2.create: method invoked");
        String tenantId = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getTenantId() : "null";
        String schemaCode = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getSchemaCode() : "null";
        log.info("Processing MDMS v2 create request for tenant: {}, schemaCode: {}", tenantId, schemaCode);
        
        try {
            List<Mdms> masterDataList = mdmsServiceV2.create(mdmsRequest);
            log.debug("MDMS create request processed, records created: {}", masterDataList != null ? masterDataList.size() : 0);
            log.info("MDMS v2 create request processed successfully");
            return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(mdmsRequest.getRequestInfo(), masterDataList), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            log.error("Error processing MDMS v2 create request for tenant: {}, schemaCode: {}", tenantId, schemaCode, e);
            throw e;
        }
    }

    /**
     * Request handler for serving search requests
     * @param masterDataSearchCriteria
     * @return
     */
    @PostMapping(value="_search", produces = { "application/json; charset=utf-8" })
    public ResponseEntity<MdmsResponseV2> search(@Valid @RequestBody MdmsCriteriaReqV2 masterDataSearchCriteria) {
        log.trace("MDMSControllerV2.search: method invoked");
        String tenantId = masterDataSearchCriteria.getMdmsCriteria() != null ? masterDataSearchCriteria.getMdmsCriteria().getTenantId() : "null";
        log.info("Processing MDMS v2 search request for tenant: {}", tenantId);
        
        try {
            List<Mdms> masterDataList = mdmsServiceV2.search(masterDataSearchCriteria);
            log.debug("MDMS search completed, records found: {}", masterDataList != null ? masterDataList.size() : 0);
            log.info("MDMS v2 search request processed successfully");
            return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(RequestInfo.builder().build(), masterDataList), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing MDMS v2 search request for tenant: {}", tenantId, e);
            throw e;
        }
    }

    /**
     * Request handler for serving update requests
     * @param mdmsRequest
     * @param schemaCode
     * @return
     */
    @PostMapping(value="_update/{schemaCode}")
    public ResponseEntity<MdmsResponseV2> update(@Valid @RequestBody MdmsRequest mdmsRequest, @PathVariable("schemaCode") String schemaCode) {
        log.trace("MDMSControllerV2.update: method invoked");
        String tenantId = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getTenantId() : "null";
        String id = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getId() : "null";
        log.info("Processing MDMS v2 update request for tenant: {}, schemaCode: {}, id: {}", tenantId, schemaCode, id);
        
        try {
            List<Mdms> masterDataList = mdmsServiceV2.update(mdmsRequest);
            log.debug("MDMS update request processed, records updated: {}", masterDataList != null ? masterDataList.size() : 0);
            log.info("MDMS v2 update request processed successfully");
            return new ResponseEntity<>(ResponseUtil.getMasterDataV2Response(mdmsRequest.getRequestInfo(), masterDataList), HttpStatus.ACCEPTED);
        } catch (Exception e) {
            log.error("Error processing MDMS v2 update request for tenant: {}, schemaCode: {}, id: {}", tenantId, schemaCode, id, e);
            throw e;
        }
    }
}
