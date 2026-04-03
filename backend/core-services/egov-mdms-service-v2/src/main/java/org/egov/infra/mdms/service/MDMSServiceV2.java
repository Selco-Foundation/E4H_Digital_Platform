package org.egov.infra.mdms.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.infra.mdms.model.*;
import org.egov.infra.mdms.repository.MdmsDataRepository;
import org.egov.infra.mdms.service.enrichment.MdmsDataEnricher;
import org.egov.infra.mdms.service.validator.MdmsDataValidator;
import org.egov.infra.mdms.utils.FallbackUtil;
import org.egov.infra.mdms.utils.SchemaUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class MDMSServiceV2 {

    private MdmsDataValidator mdmsDataValidator;

    private MdmsDataEnricher mdmsDataEnricher;

    private MdmsDataRepository mdmsDataRepository;

    private SchemaUtil schemaUtil;

    private MultiStateInstanceUtil multiStateInstanceUtil;

    @Autowired
    public MDMSServiceV2(MdmsDataValidator mdmsDataValidator, MdmsDataEnricher mdmsDataEnricher,
                         MdmsDataRepository mdmsDataRepository, SchemaUtil schemaUtil, MultiStateInstanceUtil multiStateInstanceUtil) {
        this.mdmsDataValidator = mdmsDataValidator;
        this.mdmsDataEnricher = mdmsDataEnricher;
        this.mdmsDataRepository = mdmsDataRepository;
        this.schemaUtil = schemaUtil;
        this.multiStateInstanceUtil = multiStateInstanceUtil;
    }

    /**
     * This method processes the requests that come for master data creation.
     * @param mdmsRequest
     * @return
     */
    public List<Mdms> create(MdmsRequest mdmsRequest) {
        log.trace("MDMSServiceV2.create: method invoked");
        String tenantId = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getTenantId() : "null";
        String schemaCode = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getSchemaCode() : "null";
        log.info("Processing MDMS v2 create request for tenant: {}, schemaCode: {}", tenantId, schemaCode);

        // Fetch schema against which data is getting created
        log.debug("Fetching schema definition for schemaCode: {}", schemaCode);
        JSONObject schemaObject = schemaUtil.getSchema(mdmsRequest);

        // Perform validations on incoming request
        log.debug("Validating MDMS v2 create request");
        mdmsDataValidator.validateCreateRequest(mdmsRequest, schemaObject);

        // Enrich incoming master data
        log.debug("Enriching MDMS v2 create request");
        mdmsDataEnricher.enrichCreateRequest(mdmsRequest, schemaObject);

        // Emit MDMS create event to be listened by persister
        log.debug("Publishing MDMS v2 create request to Kafka");
        mdmsDataRepository.create(mdmsRequest);
        log.info("MDMS v2 create request processed successfully for tenant: {}, schemaCode: {}", tenantId, schemaCode);

        return Arrays.asList(mdmsRequest.getMdms());
    }

    /**
     * This method processes the requests that come for master data search.
     * @param mdmsCriteriaReqV2
     * @return
     */
    public List<Mdms> search(MdmsCriteriaReqV2 mdmsCriteriaReqV2) {
        log.trace("MDMSServiceV2.search: method invoked");
        /*
         * Set incoming tenantId as state level tenantId for fallback in case master data for
         * concrete tenantId does not exist.
         */
        String tenantId = mdmsCriteriaReqV2.getMdmsCriteria().getTenantId();
        log.info("Processing MDMS v2 search request for tenant: {}", tenantId);

        List<Mdms> masterDataList = new ArrayList<>();
        List<String> subTenantListForFallback = FallbackUtil.getSubTenantListForFallBack(tenantId);
        log.debug("Generated fallback tenant list with count: {}", subTenantListForFallback != null ? subTenantListForFallback.size() : 0);

        // Make a call to repository and get list of master data
        for(String subTenantId : subTenantListForFallback) {
            log.debug("Searching for master data in tenant: {}", subTenantId);
            mdmsCriteriaReqV2.getMdmsCriteria().setTenantId(subTenantId);
            masterDataList = mdmsDataRepository.searchV2(mdmsCriteriaReqV2.getMdmsCriteria());

            if(!CollectionUtils.isEmpty(masterDataList)) {
                log.debug("Found master data in tenant: {}, count: {}", subTenantId, masterDataList.size());
                break;
            }
        }

        log.info("MDMS v2 search request processed successfully, records found: {}", masterDataList != null ? masterDataList.size() : 0);
        return masterDataList;
    }

    /**
     * This method processes the requests that come for master data update.
     * @param mdmsRequest
     * @return
     */
    public List<Mdms> update(MdmsRequest mdmsRequest) {
        log.trace("MDMSServiceV2.update: method invoked");
        String tenantId = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getTenantId() : "null";
        String schemaCode = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getSchemaCode() : "null";
        String id = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getId() : "null";
        log.info("Processing MDMS v2 update request for tenant: {}, schemaCode: {}, id: {}", tenantId, schemaCode, id);

        // Fetch schema against which data is getting created
        log.debug("Fetching schema definition for schemaCode: {}", schemaCode);
        JSONObject schemaObject = schemaUtil.getSchema(mdmsRequest);

        // Validate master data update request
        log.debug("Validating MDMS v2 update request");
        mdmsDataValidator.validateUpdateRequest(mdmsRequest, schemaObject);

        // Enrich master data update request
        log.debug("Enriching MDMS v2 update request");
        mdmsDataEnricher.enrichUpdateRequest(mdmsRequest);

        // Emit MDMS update event to be listened by persister
        log.debug("Publishing MDMS v2 update request to Kafka");
        mdmsDataRepository.update(mdmsRequest);
        log.info("MDMS v2 update request processed successfully for tenant: {}, schemaCode: {}, id: {}", tenantId, schemaCode, id);

        return Arrays.asList(mdmsRequest.getMdms());
    }

}