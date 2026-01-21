package facility.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import facility.config.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static facility.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_MDMS;

@Slf4j
@Component
@RequiredArgsConstructor
public class MdmsUtil {

    private final RestTemplate restTemplate;

    private final ObjectMapper mapper;

    private final Configuration configs;


    public Map<String, Map<String, JSONArray>> fetchMdmsData(RequestInfo requestInfo, String tenantId, String moduleName,
                                                             List<String> masterNameList) {
        log.trace("Entering fetchMdmsData method");
        log.info("Fetching MDMS data for module {} with {} masters for tenant {}", moduleName, masterNameList.size(), tenantId);
        log.debug("Master names: {}", masterNameList);
        
        StringBuilder uri = new StringBuilder();
        uri.append(configs.getMdmsHost()).append(configs.getMdmsEndPoint());
        log.debug("MDMS URI: {}", uri);
        
        MdmsCriteriaReq mdmsCriteriaReq = getMdmsRequest(requestInfo, tenantId, moduleName, masterNameList);
        Object response = new HashMap<>();

        MdmsResponse mdmsResponse = new MdmsResponse();
        try {
            response = restTemplate.postForObject(uri.toString(), mdmsCriteriaReq, Map.class);
            mdmsResponse = mapper.convertValue(response, MdmsResponse.class);
            log.debug("Successfully fetched MDMS data for module {}", moduleName);
        } catch (Exception e) {
            log.error("Error while fetching MDMS data for module {} and tenant {}: {}", moduleName, tenantId, e.getMessage(), e);
        }

        Map<String, Map<String, JSONArray>> result = mdmsResponse.getMdmsRes();
        log.trace("Exiting fetchMdmsData method");
        return result;
    }

    private MdmsCriteriaReq getMdmsRequest(RequestInfo requestInfo, String tenantId,
                                           String moduleName, List<String> masterNameList) {
        log.trace("Entering getMdmsRequest method");
        log.debug("Building MDMS request for module: {}, tenant: {}", moduleName, tenantId);
        List<MasterDetail> masterDetailList = new ArrayList<>();
        for (String masterName : masterNameList) {
            MasterDetail masterDetail = new MasterDetail();
            masterDetail.setName(masterName);
            masterDetailList.add(masterDetail);
        }

        ModuleDetail moduleDetail = new ModuleDetail();
        moduleDetail.setMasterDetails(masterDetailList);
        moduleDetail.setModuleName(moduleName);
        List<ModuleDetail> moduleDetailList = new ArrayList<>();
        moduleDetailList.add(moduleDetail);

        MdmsCriteria mdmsCriteria = new MdmsCriteria();
        String baseTenantId = tenantId.split("\\.")[0];
        mdmsCriteria.setTenantId(baseTenantId);
        mdmsCriteria.setModuleDetails(moduleDetailList);
        log.debug("Using base tenant ID: {} for MDMS request", baseTenantId);

        MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
        mdmsCriteriaReq.setMdmsCriteria(mdmsCriteria);
        mdmsCriteriaReq.setRequestInfo(requestInfo);

        log.trace("Exiting getMdmsRequest method");
        return mdmsCriteriaReq;
    }
}