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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.inbox.util.InboxConstants.*;


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

        StringBuilder uri = new StringBuilder();
        uri.append(mdmsHost).append(mdmsUrl);
        MdmsCriteriaReq mdmsCriteriaReq = getMdmsRequestForInboxQueryConfiguration(tenantId);
        Object response = new HashMap<>();
        List<Map> configs;
        try {
            response = restTemplate.postForObject(uri.toString(), mdmsCriteriaReq, Map.class);
            String jsonpath = MDMS_RESPONSE_JSONPATH.replace(MODULE_PLACEHOLDER, moduleName);
            configs = JsonPath.read(response, jsonpath);
        }catch(Exception e) {
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query configuration from MDMS for: " + moduleName);
        }

        if (CollectionUtils.isEmpty(configs))
            throw new CustomException("CONFIG_ERROR","Inbox Query Configuration not found in MDMS response for: " + moduleName);

        InboxQueryConfiguration configuration = objectMapper.convertValue(configs.get(0), InboxQueryConfiguration.class);

        return configuration;
    }

    /**
     * The {@code common-masters.BusinessHours} schedule SLA time is counted in. Cached like the inbox
     * configuration; a failed lookup throws, so it is never cached and the next search retries.
     */
    @Cacheable(value="businessHours")
    public List<Map<String, Object>> getBusinessHours(String tenantId) {
        MasterDetail masterDetail = new MasterDetail();
        masterDetail.setName(BUSINESS_HOURS_MASTER);
        ModuleDetail moduleDetail = new ModuleDetail();
        moduleDetail.setMasterDetails(Collections.singletonList(masterDetail));
        moduleDetail.setModuleName(COMMON_MASTERS_MODULE);

        MdmsCriteria mdmsCriteria = new MdmsCriteria();
        mdmsCriteria.setTenantId(multiStateInstanceUtil.getStateLevelTenant(tenantId));
        mdmsCriteria.setModuleDetails(Collections.singletonList(moduleDetail));
        MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
        mdmsCriteriaReq.setMdmsCriteria(mdmsCriteria);
        mdmsCriteriaReq.setRequestInfo(new RequestInfo());

        List<Map<String, Object>> businessHours;
        try {
            Object response = restTemplate.postForObject(mdmsHost + mdmsUrl, mdmsCriteriaReq, Map.class);
            businessHours = JsonPath.read(response, BUSINESS_HOURS_JSONPATH);
        } catch (Exception e) {
            throw new CustomException("CONFIG_ERROR", "Error in fetching BusinessHours from MDMS: " + e.getMessage());
        }
        if (CollectionUtils.isEmpty(businessHours))
            throw new CustomException("CONFIG_ERROR", "BusinessHours not found in MDMS response");
        return businessHours;
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

