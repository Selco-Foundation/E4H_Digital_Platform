package org.egov.inbox.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.egov.inbox.web.model.InboxSearchCriteria;
import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.inbox.util.WSConstants.*;

@Slf4j
@Service
public class WSInboxFilterService {

    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.searcher.host}")
    private String searcherHost;

    @Value("${egov.searcher.ws.search.path}")
    private String wsInboxSearcherEndpoint;

    @Value("${egov.searcher.ws.search.desc.path}")
    private String wsInboxSearcherDescEndpoint;

    @Value("${egov.searcher.ws.count.path}")
    private String wsInboxSearcherCountEndpoint;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    public List<String> fetchApplicationNumbersFromSearcher(InboxSearchCriteria criteria, HashMap<String, String> StatusIdNameMap, RequestInfo requestInfo){
        log.trace("Method invoked: fetchApplicationNumbersFromSearcher");
        HashMap moduleSearchCriteria = criteria.getModuleSearchCriteria();
        ProcessInstanceSearchCriteria processCriteria = criteria.getProcessSearchCriteria();
        
        List<String> userUUIDs = validateMobileNumberAndFetchUserUUIDs(criteria, moduleSearchCriteria, requestInfo);
        if (userUUIDs == null) {
            log.debug("Search result empty - returning empty list");
            return new ArrayList<>();
        }

        Map<String, Object> searchCriteria = buildWSSearchCriteria(criteria, moduleSearchCriteria, processCriteria, StatusIdNameMap, userUUIDs);
        Map<String, Object> searcherRequest = buildWSSearcherRequest(requestInfo, searchCriteria);
        String uri = buildWSSearcherUri(moduleSearchCriteria, false);
        
        Object result = restTemplate.postForObject(uri, searcherRequest, Map.class);
        List<String> applicationNumbers = JsonPath.read(result, "$.WaterConnections.*.applicationno");
        log.info("Application numbers retrieved from searcher - count: {}", applicationNumbers.size());
        
        return applicationNumbers;
    }

    public Integer fetchApplicationCountFromSearcher(InboxSearchCriteria criteria, HashMap<String, String> StatusIdNameMap, RequestInfo requestInfo){
        log.trace("Method invoked: fetchApplicationCountFromSearcher");
        HashMap moduleSearchCriteria = criteria.getModuleSearchCriteria();
        ProcessInstanceSearchCriteria processCriteria = criteria.getProcessSearchCriteria();
        
        List<String> userUUIDs = validateMobileNumberAndFetchUserUUIDs(criteria, moduleSearchCriteria, requestInfo);
        if (userUUIDs == null) {
            log.debug("Search result empty - returning 0");
            return 0;
        }

        Map<String, Object> searchCriteria = buildWSSearchCriteria(criteria, moduleSearchCriteria, processCriteria, StatusIdNameMap, userUUIDs);
        Map<String, Object> searcherRequest = buildWSSearcherRequest(requestInfo, searchCriteria);
        String uri = buildWSSearcherUri(moduleSearchCriteria, true);
        
        Object result = restTemplate.postForObject(uri, searcherRequest, Map.class);
        double count = JsonPath.read(result, "$.TotalCount[0].count");
        Integer totalCount = new Integer((int) count);
        log.info("Application count retrieved from searcher - count: {}", totalCount);
        
        return totalCount;
    }

    private List<String> validateMobileNumberAndFetchUserUUIDs(InboxSearchCriteria criteria, HashMap moduleSearchCriteria, RequestInfo requestInfo) {
        log.trace("Method invoked: validateMobileNumberAndFetchUserUUIDs");
        if(!moduleSearchCriteria.containsKey(MOBILE_NUMBER_PARAM)){
            log.debug("Mobile number not present in search criteria");
            return new ArrayList<>();
        }
        
        String tenantId = criteria.getTenantId();
        String mobileNumber = String.valueOf(moduleSearchCriteria.get(MOBILE_NUMBER_PARAM));
        List<String> userUUIDs = fetchUserUUID(mobileNumber, requestInfo, tenantId);
        Boolean isUserPresentForGivenMobileNumber = CollectionUtils.isEmpty(userUUIDs) ? false : true;
        
        if(!isUserPresentForGivenMobileNumber){
            log.warn("No user found for mobile number");
            return null;
        }
        log.debug("User UUIDs retrieved - count: {}", userUUIDs.size());
        return userUUIDs;
    }

    private Map<String, Object> buildWSSearchCriteria(InboxSearchCriteria criteria, HashMap moduleSearchCriteria,
                                                      ProcessInstanceSearchCriteria processCriteria,
                                                      HashMap<String, String> StatusIdNameMap,
                                                      List<String> userUUIDs) {
        log.trace("Method invoked: buildWSSearchCriteria");
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put(TENANT_ID_PARAM, criteria.getTenantId());
        searchCriteria.put(WS_BUSINESS_SERVICE_PARAM, processCriteria.getBusinessService());

        addWSModuleSearchCriteria(moduleSearchCriteria, searchCriteria, userUUIDs);
        addWSProcessSearchCriteria(processCriteria, StatusIdNameMap, searchCriteria);
        addWSPaginationCriteria(criteria, searchCriteria);
        
        log.debug("WS search criteria built");
        return searchCriteria;
    }

    private void addWSModuleSearchCriteria(HashMap moduleSearchCriteria, Map<String, Object> searchCriteria, List<String> userUUIDs) {
        log.trace("Method invoked: addWSModuleSearchCriteria");
        if(moduleSearchCriteria.containsKey(MOBILE_NUMBER_PARAM) && !CollectionUtils.isEmpty(userUUIDs)){
            searchCriteria.put(USERID_PARAM, userUUIDs);
        }
        if(moduleSearchCriteria.containsKey(LOCALITY_PARAM)){
            searchCriteria.put(LOCALITY_PARAM, moduleSearchCriteria.get(LOCALITY_PARAM));
        }
        if(moduleSearchCriteria.containsKey(PROPERTY_ID_PARAM)){
            searchCriteria.put(PROPERTY_ID_PARAM, moduleSearchCriteria.get(PROPERTY_ID_PARAM));
        }
        if(moduleSearchCriteria.containsKey(WS_APPLICATION_NUMBER_PARAM)) {
            searchCriteria.put(WS_APPLICATION_NUMBER_PARAM, moduleSearchCriteria.get(WS_APPLICATION_NUMBER_PARAM));
        }
        if(moduleSearchCriteria.containsKey(WS_APPLICATION_TYPE_PARAM)) {
            searchCriteria.put(WS_APPLICATION_TYPE_PARAM, moduleSearchCriteria.get(WS_APPLICATION_TYPE_PARAM));
        }
        if(moduleSearchCriteria.containsKey(WS_CONNECTION_NO_PARAM)){
            searchCriteria.put(WS_CONNECTION_NO_PARAM, moduleSearchCriteria.get(WS_CONNECTION_NO_PARAM));
        }
        if(moduleSearchCriteria.containsKey("appStatus")){
            searchCriteria.put(WS_APPLICATION_STATUS_PARAM, moduleSearchCriteria.get("appStatus"));
        }
        log.debug("WS module search criteria added");
    }

    private void addWSProcessSearchCriteria(ProcessInstanceSearchCriteria processCriteria,
                                           HashMap<String, String> StatusIdNameMap,
                                           Map<String, Object> searchCriteria) {
        log.trace("Method invoked: addWSProcessSearchCriteria");
        if(!ObjectUtils.isEmpty(processCriteria.getAssignee())){
            searchCriteria.put(WS_ASSIGNEE_PARAM, processCriteria.getAssignee());
        }
        if(!ObjectUtils.isEmpty(processCriteria.getStatus())){
            searchCriteria.put(STATUS_PARAM, processCriteria.getStatus());
        }else{
            if(StatusIdNameMap.values().size() > 0) {
                if(CollectionUtils.isEmpty(processCriteria.getStatus())) {
                    searchCriteria.put(STATUS_PARAM, StatusIdNameMap.keySet());
                }
            }
        }
        log.debug("WS process search criteria added");
    }

    private void addWSPaginationCriteria(InboxSearchCriteria criteria, Map<String, Object> searchCriteria) {
        log.trace("Method invoked: addWSPaginationCriteria");
        searchCriteria.put(OFFSET_PARAM, criteria.getOffset());
        searchCriteria.put(NO_OF_RECORDS_PARAM, criteria.getLimit());
        log.debug("WS pagination criteria added");
    }

    private Map<String, Object> buildWSSearcherRequest(RequestInfo requestInfo, Map<String, Object> searchCriteria) {
        log.trace("Method invoked: buildWSSearcherRequest");
        Map<String, Object> searcherRequest = new HashMap<>();
        searcherRequest.put(WS_REQUESTINFO_PARAM, requestInfo);
        searcherRequest.put(WS_SEARCH_CRITERIA_PARAM, searchCriteria);
        log.debug("WS searcher request built");
        return searcherRequest;
    }

    private String buildWSSearcherUri(HashMap moduleSearchCriteria, boolean isCount) {
        log.trace("Method invoked: buildWSSearcherUri - isCount: {}", isCount);
        StringBuilder uri = new StringBuilder();
        if(isCount) {
            uri.append(searcherHost).append(wsInboxSearcherCountEndpoint);
        } else if(moduleSearchCriteria.containsKey(SORT_ORDER_PARAM) && moduleSearchCriteria.get(SORT_ORDER_PARAM).equals(DESC_PARAM)){
            uri.append(searcherHost).append(wsInboxSearcherDescEndpoint);
        } else {
            uri.append(searcherHost).append(wsInboxSearcherEndpoint);
        }
        log.debug("WS searcher URI built: {}", uri.toString());
        return uri.toString();
    }


    private List<String> fetchUserUUID(String mobileNumber, RequestInfo requestInfo, String tenantId) {
        StringBuilder uri = new StringBuilder();
        uri.append(userHost).append(userSearchEndpoint);
        Map<String, Object> userSearchRequest = new HashMap<>();
        userSearchRequest.put("RequestInfo", requestInfo);
        userSearchRequest.put("tenantId", tenantId);
        userSearchRequest.put("userType", "CITIZEN");
        userSearchRequest.put("mobileNumber", mobileNumber);
        List<String> userUuids = new ArrayList<>();
        try {
            Object user = serviceRequestRepository.fetchResult(uri, userSearchRequest);
            if(null != user) {
                //log.info(user.toString());
                userUuids = JsonPath.read(user, "$.user.*.uuid");
            }else {
                log.error("Service returned null while fetching user for mobile number - " + mobileNumber);
            }
        }catch(Exception e) {
            log.error("Exception while fetching user for mobile number - " + mobileNumber);
            log.error("Exception trace: ", e);
        }
        return userUuids;
    }
}
