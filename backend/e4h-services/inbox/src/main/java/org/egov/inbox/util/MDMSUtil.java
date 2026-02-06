package org.egov.inbox.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.inbox.web.model.V2.InboxQueryConfiguration;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.inbox.util.InboxConstants.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MDMSUtil {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MultiStateInstanceUtil multiStateInstanceUtil;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsUrl;

    @Autowired
    private ObjectMapper objectMapper;

    @Cacheable(value="inboxConfiguration")
    public InboxQueryConfiguration getConfigFromMDMS(String tenantId, String moduleName) {
        log.trace("Method invoked: getConfigFromMDMS - tenantId: {}, module: {}", tenantId, moduleName);
        log.info("Fetching inbox query configuration from MDMS - tenantId: {}, module: {}", tenantId, moduleName);

        StringBuilder uri = new StringBuilder();
        uri.append(mdmsHost).append(mdmsUrl);
        log.debug("Building MDMS request for inbox query configuration");
        MdmsCriteriaReq mdmsCriteriaReq = getMdmsRequestForInboxQueryConfiguration(tenantId);
        Object response = new HashMap<>();
        List<Map> configs;
        try {
            log.debug("Calling MDMS service - URI: {}", uri.toString());
            response = restTemplate.postForObject(uri.toString(), mdmsCriteriaReq, Map.class);
            String jsonpath = MDMS_RESPONSE_JSONPATH.replace(MODULE_PLACEHOLDER, moduleName);
            configs = JsonPath.read(response, jsonpath);
            log.debug("MDMS response parsed - configCount: {}", configs != null ? configs.size() : 0);
        }catch(Exception e) {
            log.error("Error in fetching inbox query configuration from MDMS - tenantId: {}, module: {}", tenantId, moduleName, e);
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query configuration from MDMS for: " + moduleName);
        }

        if (CollectionUtils.isEmpty(configs)) {
            log.error("Inbox Query Configuration not found in MDMS response - tenantId: {}, module: {}", tenantId, moduleName);
            throw new CustomException("CONFIG_ERROR","Inbox Query Configuration not found in MDMS response for: " + moduleName);
        }

        log.debug("Converting MDMS response to InboxQueryConfiguration");
        InboxQueryConfiguration configuration = objectMapper.convertValue(configs.get(0), InboxQueryConfiguration.class);
        log.info("Inbox query configuration retrieved successfully - tenantId: {}, module: {}", tenantId, moduleName);

        return configuration;
    }

    private MdmsCriteriaReq getMdmsRequestForInboxQueryConfiguration(String tenantId) {
        MasterDetail masterDetail = new MasterDetail();
        masterDetail.setName(INBOX_QUERY_CONFIG_NAME);
        List<MasterDetail> masterDetailList = new ArrayList<>();
        masterDetailList.add(masterDetail);

        ModuleDetail moduleDetail = new ModuleDetail();
        moduleDetail.setMasterDetails(masterDetailList);
        moduleDetail.setModuleName(INBOX_MODULE_CODE);
        List<ModuleDetail> moduleDetailList = new ArrayList<>();
        moduleDetailList.add(moduleDetail);

        MdmsCriteria mdmsCriteria = new MdmsCriteria();
        mdmsCriteria.setTenantId(multiStateInstanceUtil.getStateLevelTenant(tenantId));
        mdmsCriteria.setModuleDetails(moduleDetailList);

        MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
        mdmsCriteriaReq.setMdmsCriteria(mdmsCriteria);
        mdmsCriteriaReq.setRequestInfo(new RequestInfo());

        return mdmsCriteriaReq;
    }
}

