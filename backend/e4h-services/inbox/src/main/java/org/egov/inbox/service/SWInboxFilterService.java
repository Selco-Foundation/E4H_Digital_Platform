package org.egov.inbox.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.egov.inbox.web.model.InboxSearchCriteria;
import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;
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

import static org.egov.inbox.util.SWConstants.*;
import static org.egov.inbox.util.WSConstants.WS_ASSIGNEE_PARAM;
import static org.egov.inbox.util.WSConstants.WS_APPLICATION_STATUS_PARAM;

@Slf4j
@Service
public class SWInboxFilterService {

    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.searcher.host}")
    private String searcherHost;

    @Value("${egov.searcher.sw.search.path}")
    private String swInboxSearcherEndpoint;

    @Value("${egov.searcher.sw.search.desc.path}")
    private String swInboxSearcherDescEndpoint;

    @Value("${egov.searcher.sw.count.path}")
    private String swInboxSearcherCountEndpoint;

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

        Map<String, Object> searchCriteria = buildSWSearchCriteria(criteria, moduleSearchCriteria, processCriteria, StatusIdNameMap, userUUIDs);
        Map<String, Object> searcherRequest = buildSWSearcherRequest(requestInfo, searchCriteria);
        String uri = buildSWSearcherUri(moduleSearchCriteria, false);
        
        Object result = restTemplate.postForObject(uri, searcherRequest, Map.class);
        List<String> applicationNumbers = JsonPath.read(result, "$.SewerageConnections.*.applicationno");
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

        Map<String, Object> searchCriteria = buildSWSearchCriteria(criteria, moduleSearchCriteria, processCriteria, StatusIdNameMap, userUUIDs);
        Map<String, Object> searcherRequest = buildSWSearcherRequest(requestInfo, searchCriteria);
        String uri = buildSWSearcherUri(moduleSearchCriteria, true);
        
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

    private Map<String, Object> buildSWSearchCriteria(InboxSearchCriteria criteria, HashMap moduleSearchCriteria,
                                                      ProcessInstanceSearchCriteria processCriteria,
                                                      HashMap<String, String> StatusIdNameMap,
                                                      List<String> userUUIDs) {
        log.trace("Method invoked: buildSWSearchCriteria");
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put(TENANT_ID_PARAM, criteria.getTenantId());
        if(processCriteria.getBusinessService() != null) {
            searchCriteria.put(SW_BUSINESS_SERVICE_PARAM, processCriteria.getBusinessService());
        }

        addSWModuleSearchCriteria(moduleSearchCriteria, searchCriteria, userUUIDs);
        addSWProcessSearchCriteria(processCriteria, StatusIdNameMap, searchCriteria);
        addSWPaginationCriteria(criteria, searchCriteria);
        
        log.debug("SW search criteria built");
        return searchCriteria;
    }

    private void addSWModuleSearchCriteria(HashMap moduleSearchCriteria, Map<String, Object> searchCriteria, List<String> userUUIDs) {
        log.trace("Method invoked: addSWModuleSearchCriteria");
        if(moduleSearchCriteria.containsKey(MOBILE_NUMBER_PARAM) && !CollectionUtils.isEmpty(userUUIDs)){
            searchCriteria.put(USERID_PARAM, userUUIDs);
        }
        if(moduleSearchCriteria.containsKey(LOCALITY_PARAM)){
            searchCriteria.put(LOCALITY_PARAM, moduleSearchCriteria.get(LOCALITY_PARAM));
        }
        if(moduleSearchCriteria.containsKey(PROPERTY_ID_PARAM)){
            searchCriteria.put(PROPERTY_ID_PARAM, moduleSearchCriteria.get(PROPERTY_ID_PARAM));
        }
        if(moduleSearchCriteria.containsKey(SW_APPLICATION_NUMBER_PARAM)) {
            searchCriteria.put(SW_APPLICATION_NUMBER_PARAM, moduleSearchCriteria.get(SW_APPLICATION_NUMBER_PARAM));
        }
        if(moduleSearchCriteria.containsKey(SW_APPLICATION_SEARCH_TYPE_PARAM)) {
            searchCriteria.put(SW_APPLICATION_SEARCH_TYPE_PARAM, moduleSearchCriteria.get(SW_APPLICATION_SEARCH_TYPE_PARAM));
        }
        if(moduleSearchCriteria.containsKey(SW_CONNECTION_NO_PARAM)){
            searchCriteria.put(SW_CONNECTION_NO_PARAM, moduleSearchCriteria.get(SW_CONNECTION_NO_PARAM));
        }
        if(moduleSearchCriteria.containsKey("appStatus")){
            searchCriteria.put(SW_APPLICATION_STATUS_PARAM, moduleSearchCriteria.get("appStatus"));
        }
        log.debug("SW module search criteria added");
    }

    private void addSWProcessSearchCriteria(ProcessInstanceSearchCriteria processCriteria,
                                           HashMap<String, String> StatusIdNameMap,
                                           Map<String, Object> searchCriteria) {
        log.trace("Method invoked: addSWProcessSearchCriteria");
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
        log.debug("SW process search criteria added");
    }

    private void addSWPaginationCriteria(InboxSearchCriteria criteria, Map<String, Object> searchCriteria) {
        log.trace("Method invoked: addSWPaginationCriteria");
        searchCriteria.put(OFFSET_PARAM, criteria.getOffset());
        searchCriteria.put(NO_OF_RECORDS_PARAM, criteria.getLimit());
        log.debug("SW pagination criteria added");
    }

    private Map<String, Object> buildSWSearcherRequest(RequestInfo requestInfo, Map<String, Object> searchCriteria) {
        log.trace("Method invoked: buildSWSearcherRequest");
        Map<String, Object> searcherRequest = new HashMap<>();
        searcherRequest.put(SW_REQUESTINFO_PARAM, requestInfo);
        searcherRequest.put(SW_SEARCH_CRITERIA_PARAM, searchCriteria);
        log.debug("SW searcher request built");
        return searcherRequest;
    }

    private String buildSWSearcherUri(HashMap moduleSearchCriteria, boolean isCount) {
        log.trace("Method invoked: buildSWSearcherUri - isCount: {}", isCount);
        StringBuilder uri = new StringBuilder();
        if(isCount) {
            uri.append(searcherHost).append(swInboxSearcherCountEndpoint);
        } else if(moduleSearchCriteria.containsKey(SORT_ORDER_PARAM) && moduleSearchCriteria.get(SORT_ORDER_PARAM).equals(DESC_PARAM)){
            uri.append(searcherHost).append(swInboxSearcherDescEndpoint);
        } else {
            uri.append(searcherHost).append(swInboxSearcherEndpoint);
        }
        log.debug("SW searcher URI built: {}", uri.toString());
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
