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
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.inbox.util.InboxConstants.*;


@Component
@Slf4j
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
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error while fetching inbox configuration from MDMS for module: {} status={} body={}", 
                    moduleName, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException("CONFIG_ERROR", "Error in fetching inbox query configuration from MDMS for: " + moduleName);
        } catch (ResourceAccessException e) {
            log.error("Network error while fetching inbox configuration from MDMS for module: {}", moduleName, e);
            throw new CustomException("CONFIG_ERROR", "Network error while fetching inbox query configuration from MDMS for: " + moduleName);
        } catch (RestClientException e) {
            log.error("Error while fetching inbox configuration from MDMS for module: {}", moduleName, e);
            throw new CustomException("CONFIG_ERROR", "Error in fetching inbox query configuration from MDMS for: " + moduleName);
        } catch (com.jayway.jsonpath.PathNotFoundException e) {
            log.error("Path not found while parsing MDMS response for module: {}", moduleName, e);
            throw new CustomException("CONFIG_ERROR", "Error parsing inbox query configuration from MDMS for: " + moduleName);
        } catch (RuntimeException e) {
            log.error("Error while fetching inbox configuration from MDMS for module: {}", moduleName, e);
            throw new CustomException("CONFIG_ERROR", "Error in fetching inbox query configuration from MDMS for: " + moduleName);
        }

        if (CollectionUtils.isEmpty(configs))
            throw new CustomException("CONFIG_ERROR","Inbox Query Configuration not found in MDMS response for: " + moduleName);

        InboxQueryConfiguration configuration = objectMapper.convertValue(configs.get(0), InboxQueryConfiguration.class);

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

