package org.egov.asset.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.asset.config.Configuration;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
public class MdmsUtil {

    private final RestTemplate restTemplate;

    private final Configuration configuration;

    @Autowired
    public MdmsUtil(RestTemplate restTemplate, Configuration configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }


    /**
     * Builds cache for MDMS data, this gets refreshed for every call.
     *
     * @param requestInfo
     * @param tenantId
     * @return
     */
    public Map<String, Object> getMDMSData(RequestInfo requestInfo, String tenantId) {
        log.info("MdmsUtil::getMDMSData called | tenantId={}", tenantId);
        MdmsResponse response = fetchMDMSData(requestInfo, tenantId);
        Map<String, Object> masterData = new HashMap<>();
        Map<String, List<String>> eachMasterMap = new HashMap<>();
        if (null != response) {
            if (!CollectionUtils.isEmpty(response.getMdmsRes().keySet())) {
                if (null != response.getMdmsRes().get(AssetConstants.ASSET_MODULE_NAME)) {
                    eachMasterMap = (Map) response.getMdmsRes().get(AssetConstants.ASSET_MODULE_NAME);
                    masterData.put(AssetConstants.ASSET_TYPE_CODE, eachMasterMap.get(AssetConstants.ASSET_TYPE_CODE));
                    masterData.put(AssetConstants.ASSET_COUNT_CODE, eachMasterMap.get(AssetConstants.ASSET_COUNT_CODE));
                    masterData.put(AssetConstants.BRAND_CODE, eachMasterMap.get(AssetConstants.BRAND_CODE));
                    masterData.put(AssetConstants.SYSTEM_CODE, eachMasterMap.get(AssetConstants.SYSTEM_CODE));
                    masterData.put(AssetConstants.WARRANTY_DURATION, eachMasterMap.get(AssetConstants.WARRANTY_DURATION));
                }
            }
        }

        return masterData;
    }


    /**
     * Makes call to the MDMS service to fetch the MDMS data.
     *
     * @param requestInfo
     * @param tenantId
     * @return
     */
    public MdmsResponse fetchMDMSData(RequestInfo requestInfo, String tenantId) {
        log.info("MdmsUtil::fetchMDMSData called | tenantId={}", tenantId);
        StringBuilder uri = new StringBuilder();
        MdmsCriteriaReq request = prepareMDMSRequest(uri, requestInfo, tenantId);
        MdmsResponse response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), request, MdmsResponse.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error while fetching from MDMS: {}", e.getResponseBodyAsString(), e);
            throw new CustomException(ErrorConstants.MDMS_SERVICE_ERROR_CODE, ErrorConstants.MDMS_SERVICE_ERROR_MSG + ": " + e.getMessage());
        } catch (ResourceAccessException e) {
            log.error("Connection error while fetching from MDMS", e);
            throw new CustomException(ErrorConstants.MDMS_SERVICE_ERROR_CODE, ErrorConstants.MDMS_SERVICE_ERROR_MSG + ": Connection failed");
        } catch (RestClientException e) {
            log.error("Error while fetching from MDMS", e);
            throw new CustomException(ErrorConstants.MDMS_SERVICE_ERROR_CODE, ErrorConstants.MDMS_SERVICE_ERROR_MSG + ": " + e.getMessage());
        }
        return response;
    }

    /**
     * Prepares request for MDMS in order to fetch all the required masters for asset.
     *
     * @param uri
     * @param requestInfo
     * @param tenantId
     * @return
     */
    public MdmsCriteriaReq prepareMDMSRequest(StringBuilder uri, RequestInfo requestInfo, String tenantId) {
        log.info("MdmsUtil::prepareMDMSRequest called | tenantId={}", tenantId);
        Map<String, List<String>> mapOfModulesAndMasters = new HashMap<>();
        String[] assetMasters = {AssetConstants.ASSET_TYPE_CODE, AssetConstants.ASSET_COUNT_CODE, AssetConstants.BRAND_CODE,
                AssetConstants.SYSTEM_CODE, AssetConstants.WARRANTY_DURATION};
        mapOfModulesAndMasters.put(AssetConstants.ASSET_MODULE_NAME, Arrays.asList(assetMasters));
        List<ModuleDetail> moduleDetails = new ArrayList<>(mapOfModulesAndMasters.entrySet().stream()
                .map(entry -> {
                    ModuleDetail moduleDetail = new ModuleDetail();
                    moduleDetail.setModuleName(entry.getKey());
                    List<MasterDetail> masterDetails = entry.getValue().stream()
                            .map(master -> MasterDetail.builder()
                                    .name(master)
                                    .build())
                            .toList();

                    moduleDetail.setMasterDetails(masterDetails);
                    return moduleDetail;
                })
                .toList());
        uri.append(configuration.getMdmsHost()).append(configuration.getMdmsSearchEndPoint());
        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
        return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();

    }


}