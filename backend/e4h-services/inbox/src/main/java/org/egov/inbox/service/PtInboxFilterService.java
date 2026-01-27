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

import static org.egov.inbox.util.PTConstants.*;
import static org.egov.inbox.util.PTConstants.LIMIT_PARAM;

@Slf4j
@Service
public class PtInboxFilterService {

    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.searcher.host}")
    private String searcherHost;

    @Value("${egov.searcher.pt.search.path}")
    private String ptInboxSearcherEndpoint;

    @Value("${egov.searcher.pt.search.desc.path}")
    private String ptInboxSearcherDescEndpoint;

    @Value("${egov.searcher.pt.count.path}")
    private String ptInboxSearcherCountEndpoint;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    public List<String> fetchAcknowledgementIdsFromSearcher(InboxSearchCriteria criteria, HashMap<String, String> StatusIdNameMap, RequestInfo requestInfo){
        log.trace("Method invoked: fetchAcknowledgementIdsFromSearcher");
        String tenantId = criteria.getTenantId();
        log.info("Fetching acknowledgement IDs from searcher - tenantId: {}", tenantId);
        
        HashMap moduleSearchCriteria = criteria.getModuleSearchCriteria();
        ProcessInstanceSearchCriteria processCriteria = criteria.getProcessSearchCriteria();
        
        List<String> userUUIDs = validateMobileNumberAndFetchUserUUIDs(criteria, moduleSearchCriteria, requestInfo);
        if (userUUIDs == null) {
            log.debug("Search result empty - returning empty list");
            return new ArrayList<>();
        }

        Map<String, Object> searchCriteria = buildPTSearchCriteria(criteria, moduleSearchCriteria, processCriteria, StatusIdNameMap, userUUIDs);
        Map<String, Object> searcherRequest = buildPTSearcherRequest(requestInfo, searchCriteria);
        String uri = buildPTSearcherUri(moduleSearchCriteria, false);
        
        log.debug("Calling searcher service - URI: {}", uri);
        Object result = restTemplate.postForObject(uri, searcherRequest, Map.class);

        log.debug("Parsing acknowledgement numbers from searcher response");
        List<String> acknowledgementNumbers = JsonPath.read(result, "$.Properties.*.acknowldgementnumber");
        log.info("Acknowledgement IDs retrieved from searcher - count: {}", acknowledgementNumbers.size());

        return acknowledgementNumbers;
    }

    public Integer fetchAcknowledgementIdsCountFromSearcher(InboxSearchCriteria criteria, HashMap<String, String> StatusIdNameMap, RequestInfo requestInfo){
        log.trace("Method invoked: fetchAcknowledgementIdsCountFromSearcher");
        String tenantId = criteria.getTenantId();
        log.info("Fetching acknowledgement IDs count from searcher - tenantId: {}", tenantId);
        
        HashMap moduleSearchCriteria = criteria.getModuleSearchCriteria();
        ProcessInstanceSearchCriteria processCriteria = criteria.getProcessSearchCriteria();
        
        List<String> userUUIDs = validateMobileNumberAndFetchUserUUIDs(criteria, moduleSearchCriteria, requestInfo);
        if (userUUIDs == null) {
            log.debug("Search result empty - returning 0");
            return 0;
        }

        Map<String, Object> searchCriteria = buildPTSearchCriteriaForCount(criteria, moduleSearchCriteria, processCriteria, StatusIdNameMap, userUUIDs);
        Map<String, Object> searcherRequest = buildPTSearcherRequest(requestInfo, searchCriteria);
        String uri = buildPTSearcherUri(moduleSearchCriteria, true);
        
        log.debug("Calling searcher count service - URI: {}", uri);
        Object result = restTemplate.postForObject(uri, searcherRequest, Map.class);

        log.debug("Parsing count from searcher response");
        double count = JsonPath.read(result, "$.TotalCount[0].count");
        Integer totalCount = new Integer((int) count);
        log.info("Acknowledgement IDs count retrieved from searcher - count: {}", totalCount);

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
        log.debug("Fetching user UUIDs for mobile number");
        List<String> userUUIDs = fetchUserUUID(mobileNumber, requestInfo, tenantId);
        Boolean isUserPresentForGivenMobileNumber = CollectionUtils.isEmpty(userUUIDs) ? false : true;
        
        if(!isUserPresentForGivenMobileNumber){
            log.warn("No user found for mobile number");
            return null;
        }
        log.debug("User UUIDs retrieved - count: {}", userUUIDs.size());
        return userUUIDs;
    }

    private Map<String, Object> buildPTSearchCriteria(InboxSearchCriteria criteria, HashMap moduleSearchCriteria,
                                                      ProcessInstanceSearchCriteria processCriteria,
                                                      HashMap<String, String> StatusIdNameMap,
                                                      List<String> userUUIDs) {
        log.trace("Method invoked: buildPTSearchCriteria");
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put(TENANT_ID_PARAM, criteria.getTenantId());
        if(processCriteria.getBusinessService() != null) {
            searchCriteria.put(BUSINESS_SERVICE_PARAM, processCriteria.getBusinessService());
        }

        addPTModuleSearchCriteria(moduleSearchCriteria, searchCriteria, userUUIDs);
        addPTProcessSearchCriteria(processCriteria, StatusIdNameMap, searchCriteria);
        addPTPaginationCriteria(criteria, moduleSearchCriteria, searchCriteria);
        
        log.debug("PT search criteria built");
        return searchCriteria;
    }

    private Map<String, Object> buildPTSearchCriteriaForCount(InboxSearchCriteria criteria, HashMap moduleSearchCriteria,
                                                              ProcessInstanceSearchCriteria processCriteria,
                                                              HashMap<String, String> StatusIdNameMap,
                                                              List<String> userUUIDs) {
        log.trace("Method invoked: buildPTSearchCriteriaForCount");
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put(TENANT_ID_PARAM, criteria.getTenantId());

        addPTModuleSearchCriteria(moduleSearchCriteria, searchCriteria, userUUIDs);
        addPTProcessSearchCriteria(processCriteria, StatusIdNameMap, searchCriteria);
        
        log.debug("PT search criteria for count built");
        return searchCriteria;
    }

    private void addPTModuleSearchCriteria(HashMap moduleSearchCriteria, Map<String, Object> searchCriteria, List<String> userUUIDs) {
        log.trace("Method invoked: addPTModuleSearchCriteria");
        if(moduleSearchCriteria.containsKey(MOBILE_NUMBER_PARAM) && !CollectionUtils.isEmpty(userUUIDs)){
            searchCriteria.put(USERID_PARAM, userUUIDs);
        }
        if(moduleSearchCriteria.containsKey(LOCALITY_PARAM)){
            searchCriteria.put(LOCALITY_PARAM, moduleSearchCriteria.get(LOCALITY_PARAM));
        }
        if(moduleSearchCriteria.containsKey(PROPERTY_ID_PARAM)){
            searchCriteria.put(PROPERTY_ID_PARAM, moduleSearchCriteria.get(PROPERTY_ID_PARAM));
        }
        if(moduleSearchCriteria.containsKey(PT_APPLICATION_NUMBER_PARAM)) {
            searchCriteria.put(PT_APPLICATION_NUMBER_PARAM, moduleSearchCriteria.get(PT_APPLICATION_NUMBER_PARAM));
        }
        log.debug("PT module search criteria added");
    }

    private void addPTProcessSearchCriteria(ProcessInstanceSearchCriteria processCriteria,
                                           HashMap<String, String> StatusIdNameMap,
                                           Map<String, Object> searchCriteria) {
        log.trace("Method invoked: addPTProcessSearchCriteria");
        if(!ObjectUtils.isEmpty(processCriteria.getAssignee())){
            searchCriteria.put(ASSIGNEE_PARAM, processCriteria.getAssignee());
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
        log.debug("PT process search criteria added");
    }

    private void addPTPaginationCriteria(InboxSearchCriteria criteria, HashMap moduleSearchCriteria, Map<String, Object> searchCriteria) {
        log.trace("Method invoked: addPTPaginationCriteria");
        searchCriteria.put(OFFSET_PARAM, criteria.getOffset());
        searchCriteria.put(NO_OF_RECORDS_PARAM, criteria.getLimit());
        moduleSearchCriteria.put(LIMIT_PARAM, criteria.getLimit());
        log.debug("PT pagination criteria added");
    }

    private Map<String, Object> buildPTSearcherRequest(RequestInfo requestInfo, Map<String, Object> searchCriteria) {
        log.trace("Method invoked: buildPTSearcherRequest");
        Map<String, Object> searcherRequest = new HashMap<>();
        searcherRequest.put(REQUESTINFO_PARAM, requestInfo);
        searcherRequest.put(SEARCH_CRITERIA_PARAM, searchCriteria);
        log.debug("PT searcher request built");
        return searcherRequest;
    }

    private String buildPTSearcherUri(HashMap moduleSearchCriteria, boolean isCount) {
        log.trace("Method invoked: buildPTSearcherUri - isCount: {}", isCount);
        StringBuilder uri = new StringBuilder();
        if(isCount) {
            uri.append(searcherHost).append(ptInboxSearcherCountEndpoint);
        } else if(moduleSearchCriteria.containsKey(SORT_ORDER_PARAM) && moduleSearchCriteria.get(SORT_ORDER_PARAM).equals(DESC_PARAM)){
            uri.append(searcherHost).append(ptInboxSearcherDescEndpoint);
            log.debug("Using descending sort order endpoint");
        } else {
            uri.append(searcherHost).append(ptInboxSearcherEndpoint);
        }
        log.debug("PT searcher URI built: {}", uri.toString());
        return uri.toString();
    }


    private List<String> fetchUserUUID(String mobileNumber, RequestInfo requestInfo, String tenantId) {
        log.trace("Method invoked: fetchUserUUID - tenantId: {}", tenantId);
        log.debug("Fetching user UUID for mobile number - tenantId: {}", tenantId);
        StringBuilder uri = new StringBuilder();
        uri.append(userHost).append(userSearchEndpoint);
        Map<String, Object> userSearchRequest = new HashMap<>();
        userSearchRequest.put("RequestInfo", requestInfo);
        userSearchRequest.put("tenantId", tenantId);
        userSearchRequest.put("userType", "CITIZEN");
        userSearchRequest.put("mobileNumber", mobileNumber);
        List<String> userUuids = new ArrayList<>();
        try {
            log.debug("Calling user service - URI: {}", uri.toString());
            Object user = serviceRequestRepository.fetchResult(uri, userSearchRequest);
            if(null != user) {
                log.debug("User service response received");
                userUuids = JsonPath.read(user, "$.user.*.uuid");
                log.debug("User UUIDs extracted - count: {}", userUuids.size());
            }else {
                log.warn("User service returned null response for mobile number - tenantId: {}", tenantId);
            }
        }catch(Exception e) {
            log.error("Exception while fetching user for mobile number - tenantId: {}", tenantId, e);
        }
        return userUuids;
    }
}
