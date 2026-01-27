package org.egov.inbox.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.egov.inbox.util.BpaConstants;
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
import java.util.stream.Collectors;

import static org.egov.inbox.util.BpaConstants.BPAREG;
import static org.egov.inbox.util.BpaConstants.MOBILE_NUMBER_PARAM;
import static org.egov.inbox.util.TLConstants.*;

@Slf4j
@Service
public class TLInboxFilterService {

    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.searcher.host}")
    private String searcherHost;

    @Value("${egov.searcher.tl.search.path}")
    private String tlInboxSearcherEndpoint;

    @Value("${egov.searcher.tl.search.desc.path}")
    private String tlInboxSearcherDescEndpoint;

    @Value("${egov.searcher.tl.count.path}")
    private String tlInboxSearcherCountEndpoint;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    public List<String> fetchApplicationNumbersFromSearcher(InboxSearchCriteria criteria, HashMap<String, String> StatusIdNameMap, RequestInfo requestInfo){
        log.trace("Method invoked: fetchApplicationNumbersFromSearcher");
        HashMap moduleSearchCriteria = criteria.getModuleSearchCriteria();
        ProcessInstanceSearchCriteria processCriteria = criteria.getProcessSearchCriteria();
        
        moduleSearchCriteria = setUserWhenMobileNoIsEmptyForStakeholderRegOfCitizen(criteria, requestInfo, moduleSearchCriteria, processCriteria);
        List<String> userUUIDs = validateMobileNumberAndFetchUserUUIDs(criteria, moduleSearchCriteria, requestInfo);
        if (userUUIDs == null) {
            log.debug("Search result empty - returning empty list");
            return new ArrayList<>();
        }

        Map<String, Object> searchCriteria = buildTLSearchCriteria(criteria, moduleSearchCriteria, processCriteria, StatusIdNameMap, userUUIDs);
        Map<String, Object> searcherRequest = buildTLSearcherRequest(requestInfo, searchCriteria);
        String uri = buildTLSearcherUri(moduleSearchCriteria, false);
        
        Object result = restTemplate.postForObject(uri, searcherRequest, Map.class);
        List<String> acknowledgementNumbers = JsonPath.read(result, "$.Licenses.*.applicationnumber");
        log.info("Application numbers retrieved from searcher - count: {}", acknowledgementNumbers.size());
        
        return acknowledgementNumbers;
    }

    private HashMap setUserWhenMobileNoIsEmptyForStakeholderRegOfCitizen(InboxSearchCriteria criteria, RequestInfo requestInfo,
            HashMap moduleSearchCriteria, ProcessInstanceSearchCriteria processCriteria) {
        List<String> roles = requestInfo.getUserInfo().getRoles().stream().map(Role::getCode).collect(Collectors.toList());
        if(processCriteria.getModuleName().equals(BPAREG) && roles.contains(BpaConstants.CITIZEN)) {
            if (moduleSearchCriteria == null || moduleSearchCriteria.isEmpty() || !moduleSearchCriteria.containsKey(MOBILE_NUMBER_PARAM)) {
                moduleSearchCriteria = new HashMap<>();
                moduleSearchCriteria.put(MOBILE_NUMBER_PARAM, requestInfo.getUserInfo().getMobileNumber());
                criteria.setModuleSearchCriteria(moduleSearchCriteria);
            } 
        }
        return moduleSearchCriteria;
    }

    public Integer fetchApplicationCountFromSearcher(InboxSearchCriteria criteria, HashMap<String, String> StatusIdNameMap, RequestInfo requestInfo){
        log.trace("Method invoked: fetchApplicationCountFromSearcher");
        HashMap moduleSearchCriteria = criteria.getModuleSearchCriteria();
        ProcessInstanceSearchCriteria processCriteria = criteria.getProcessSearchCriteria();
        
        moduleSearchCriteria = setUserWhenMobileNoIsEmptyForStakeholderRegOfCitizen(criteria, requestInfo, moduleSearchCriteria, processCriteria);
        List<String> userUUIDs = validateMobileNumberAndFetchUserUUIDs(criteria, moduleSearchCriteria, requestInfo);
        if (userUUIDs == null) {
            log.debug("Search result empty - returning 0");
            return 0;
        }

        Map<String, Object> searchCriteria = buildTLSearchCriteria(criteria, moduleSearchCriteria, processCriteria, StatusIdNameMap, userUUIDs);
        Map<String, Object> searcherRequest = buildTLSearcherRequest(requestInfo, searchCriteria);
        String uri = buildTLSearcherUri(moduleSearchCriteria, true);
        
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

    private Map<String, Object> buildTLSearchCriteria(InboxSearchCriteria criteria, HashMap moduleSearchCriteria,
                                                      ProcessInstanceSearchCriteria processCriteria,
                                                      HashMap<String, String> StatusIdNameMap,
                                                      List<String> userUUIDs) {
        log.trace("Method invoked: buildTLSearchCriteria");
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put(TENANT_ID_PARAM, criteria.getTenantId());
        if(processCriteria.getBusinessService() != null) {
            searchCriteria.put(BUSINESS_SERVICE_PARAM, processCriteria.getBusinessService());
        }

        addTLModuleSearchCriteria(moduleSearchCriteria, searchCriteria, userUUIDs);
        addTLProcessSearchCriteria(processCriteria, StatusIdNameMap, searchCriteria);
        addTLPaginationCriteria(criteria, moduleSearchCriteria, searchCriteria);
        
        log.debug("TL search criteria built");
        return searchCriteria;
    }

    private void addTLModuleSearchCriteria(HashMap moduleSearchCriteria, Map<String, Object> searchCriteria, List<String> userUUIDs) {
        log.trace("Method invoked: addTLModuleSearchCriteria");
        if(moduleSearchCriteria.containsKey(MOBILE_NUMBER_PARAM) && !CollectionUtils.isEmpty(userUUIDs)){
            searchCriteria.put(USERID_PARAM, userUUIDs);
        }
        if(moduleSearchCriteria.containsKey(LOCALITY_PARAM)){
            searchCriteria.put(LOCALITY_PARAM, moduleSearchCriteria.get(LOCALITY_PARAM));
        }
        if(moduleSearchCriteria.containsKey(LICENSE_NUMBER_PARAM)){
            searchCriteria.put(LICENSE_NUMBER_PARAM, moduleSearchCriteria.get(LICENSE_NUMBER_PARAM));
        }
        if(moduleSearchCriteria.containsKey(APPLICATION_NUMBER_PARAM)) {
            searchCriteria.put(APPLICATION_NUMBER_PARAM, moduleSearchCriteria.get(APPLICATION_NUMBER_PARAM));
        }
        log.debug("TL module search criteria added");
    }

    private void addTLProcessSearchCriteria(ProcessInstanceSearchCriteria processCriteria,
                                           HashMap<String, String> StatusIdNameMap,
                                           Map<String, Object> searchCriteria) {
        log.trace("Method invoked: addTLProcessSearchCriteria");
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
        log.debug("TL process search criteria added");
    }

    private void addTLPaginationCriteria(InboxSearchCriteria criteria, HashMap moduleSearchCriteria, Map<String, Object> searchCriteria) {
        log.trace("Method invoked: addTLPaginationCriteria");
        searchCriteria.put(OFFSET_PARAM, criteria.getOffset());
        searchCriteria.put(NO_OF_RECORDS_PARAM, criteria.getLimit());
        moduleSearchCriteria.put(LIMIT_PARAM, criteria.getLimit());
        log.debug("TL pagination criteria added");
    }

    private Map<String, Object> buildTLSearcherRequest(RequestInfo requestInfo, Map<String, Object> searchCriteria) {
        log.trace("Method invoked: buildTLSearcherRequest");
        Map<String, Object> searcherRequest = new HashMap<>();
        searcherRequest.put(REQUESTINFO_PARAM, requestInfo);
        searcherRequest.put(SEARCH_CRITERIA_PARAM, searchCriteria);
        log.debug("TL searcher request built");
        return searcherRequest;
    }

    private String buildTLSearcherUri(HashMap moduleSearchCriteria, boolean isCount) {
        log.trace("Method invoked: buildTLSearcherUri - isCount: {}", isCount);
        StringBuilder uri = new StringBuilder();
        if(isCount) {
            uri.append(searcherHost).append(tlInboxSearcherCountEndpoint);
        } else if(moduleSearchCriteria.containsKey(SORT_ORDER_PARAM) && moduleSearchCriteria.get(SORT_ORDER_PARAM).equals(DESC_PARAM)){
            uri.append(searcherHost).append(tlInboxSearcherDescEndpoint);
        } else {
            uri.append(searcherHost).append(tlInboxSearcherEndpoint);
        }
        log.debug("TL searcher URI built: {}", uri.toString());
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
