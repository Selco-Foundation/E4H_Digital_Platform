package org.egov.im.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.web.models.RequestInfoWrapper;
import org.egov.im.web.models.User;
import org.egov.im.web.models.user.UserDetailResponse;
import org.egov.im.web.models.user.UserSearchRequest;
import org.egov.im.web.models.workflow.BusinessServiceResponse;
import org.egov.im.web.models.workflow.State;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.*;

@Slf4j
@Component
public class MigrationUtils {


    private UserUtils userUtils;

    private IMConfiguration config;

    private ObjectMapper mapper;

    private ServiceRequestRepository repository;

    private MDMSUtils mdmsUtils;

    @Autowired
    public MigrationUtils(UserUtils userUtils, IMConfiguration config, ObjectMapper mapper, ServiceRequestRepository repository, MDMSUtils mdmsUtils) {
        this.userUtils = userUtils;
        this.config = config;
        this.mapper = mapper;
        this.repository = repository;
        this.mdmsUtils = mdmsUtils;
    }





    public Map<Long, String> getIdtoUUIDMap(List<String> ids) {
        log.trace("MigrationUtils::getIdtoUUIDMap method invoked");
        log.debug("Fetching UUID mapping for {} IDs", ids != null ? ids.size() : 0);

        /**
         * calls the user search API based on the given list of user uuids
         * @param uuids
         * @return
         */

        ids.removeAll(Collections.singleton(null));

        UserSearchRequest userSearchRequest = new UserSearchRequest();

        if (!CollectionUtils.isEmpty(ids))
            userSearchRequest.setId(ids);

        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        UserDetailResponse userDetailResponse = userUtils.userCall(userSearchRequest, uri);
        List<User> users = userDetailResponse.getUser();

        if (CollectionUtils.isEmpty(users)) {
            log.error("No users found for the provided IDs");
            throw new CustomException("USER_NOT_FOUND", "No user found for the uuids");
        }

        Map<Long, String> idToUuidMap = users.stream().collect(Collectors.toMap(User::getId, User::getUuid));

        if (idToUuidMap.keySet().size() != ids.size()) {
            log.warn("UUID mapping incomplete: searched {} IDs, got {} UUIDs", ids.size(), idToUuidMap.keySet().size());
            throw new CustomException("UUID_NOT_FOUND", "Number of ids searched: " + ids.size() + " uuids returned: " + idToUuidMap.keySet().size());
        }

        log.debug("Successfully mapped {} IDs to UUIDs", idToUuidMap.size());
        return idToUuidMap;

    }




    public Map<String,String> getStatusToUUIDMap(String tenantId) {
        log.trace("MigrationUtils::getStatusToUUIDMap method invoked");
        log.debug("Fetching status to UUID mapping for tenantId: {}", tenantId);
        StringBuilder url = getSearchURLWithParams(tenantId, IM_BUSINESSSERVICE);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(new RequestInfo()).build();
        Object result = repository.fetchResult(url, requestInfoWrapper);
        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result, BusinessServiceResponse.class);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse business service response", e);
            throw new CustomException("PARSING ERROR", "Failed to parse response of workflow business service search");
        }

        if (CollectionUtils.isEmpty(response.getBusinessServices())) {
            log.error("Business service not found for tenantId: {}, businessService: {}", tenantId, IM_BUSINESSSERVICE);
            throw new CustomException("BUSINESSSERVICE_NOT_FOUND", "The businessService " + IM_BUSINESSSERVICE + " is not found");
        }

        Map<String,String> statusToUUIDMap = response.getBusinessServices().get(0).getStates().stream()
                .collect(Collectors.toMap(State::getState,State::getUuid));

        log.debug("Successfully mapped {} statuses to UUIDs", statusToUUIDMap.size());
        return statusToUUIDMap;
    }


    public Map<String,Long> getServiceCodeToSLAMap(String tenantId) {
        log.trace("MigrationUtils::getServiceCodeToSLAMap method invoked");
        log.debug("Fetching service code to SLA mapping for tenantId: {}", tenantId);

        Map<String, Long> serviceCodeToSLA = new HashMap<>();

        MdmsCriteriaReq mdmsCriteriaReq = mdmsUtils.getMDMSRequest(new RequestInfo(),tenantId);
        Object result = repository.fetchResult(mdmsUtils.getMdmsSearchUrl(), mdmsCriteriaReq);
        List<Map<String, Object>> res = new LinkedList<>();


        try{
            res = JsonPath.read(result,MDMS_DATA_JSONPATH);
        }
        catch (Exception e){
            log.error("Failed to parse MDMS response for SLA mapping", e);
            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response");
        }

        for(Map<String, Object> map : res){
            Long SLA = TimeUnit.HOURS.toMillis((Integer)map.get(MDMS_DATA_SLA_KEYWORD));
            serviceCodeToSLA.put((String)map.get(MDMS_DATA_SERVICE_CODE_KEYWORD), SLA);
        }

        log.debug("Successfully mapped {} service codes to SLA", serviceCodeToSLA.size());
        return serviceCodeToSLA;
    }



    private StringBuilder getSearchURLWithParams(String tenantId, String businessService) {

        StringBuilder url = new StringBuilder(config.getWfHost());
        url.append(config.getWfBusinessServiceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessServices=");
        url.append(businessService);
        return url;
    }


}
