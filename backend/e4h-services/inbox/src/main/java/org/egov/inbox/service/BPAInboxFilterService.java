package org.egov.inbox.service;

import static org.egov.inbox.util.BpaConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.egov.inbox.web.model.InboxSearchCriteria;
import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BPAInboxFilterService {

    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.searcher.host}")
    private String searcherHost;

    @Value("${egov.searcher.bpa.search.path}")
    private String bpaInboxSearcherEndpoint;

    @Value("${egov.searcher.bpa.search.desc.path}")
    private String bpaInboxSearcherDescEndpoint;

    @Value("${egov.searcher.bpa.count.path}")
    private String bpaInboxSearcherCountEndpoint;

    @Value("${egov.searcher.bpa.citizen.search.path}")
    private String bpaCitizenInboxSearcherEndpoint;

    @Value("${egov.searcher.bpa.citizen.search.desc.path}")
    private String bpaCitizenInboxSearcherDescEndpoint;

    @Value("${egov.searcher.bpa.citizen.count.path}")
    private String bpaCitizenInboxSearcherCountEndpoint;

    @Value("${egov.searcher.bpa.tenant.wise.applnno.path}")
    private String bpaStakeholderInboxTenantWiseApplnNosEndpoint;

    @Value("${egov.searcher.bpa.citizen.tenant.wise.applnno.path}")
    private String bpaCitizenInboxTenantWiseApplnNosEndpoint;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    public List<String> fetchApplicationNumbersFromSearcher(InboxSearchCriteria criteria,
            HashMap<String, String> StatusIdNameMap, RequestInfo requestInfo) {
        log.trace("Method invoked: fetchApplicationNumbersFromSearcher");
        HashMap<String, Object> moduleSearchCriteria = criteria.getModuleSearchCriteria();
        ProcessInstanceSearchCriteria processCriteria = criteria.getProcessSearchCriteria();
        
        UserDetails userDetails = validateAndFetchUserDetails(criteria, moduleSearchCriteria, requestInfo);
        if (userDetails == null || userDetails.userUUIDs.isEmpty()) {
            log.debug("Search result empty - returning empty list");
            return new ArrayList<>();
        }

        Map<String, Object> searchCriteria = buildBPASearchCriteria(criteria, StatusIdNameMap, requestInfo,
                moduleSearchCriteria, processCriteria, userDetails.userUUIDs, userDetails.citizenRoles);
        Map<String, Object> searcherRequest = buildBPASearcherRequest(requestInfo, searchCriteria);
        
        List<String> applicationNumbers = fetchBPAApplicationNumbers(moduleSearchCriteria, searcherRequest, 
                requestInfo, userDetails.citizenRoles);
        log.info("Application numbers retrieved from searcher - count: {}", applicationNumbers.size());
        
        return applicationNumbers;
    }

    private UserDetails validateAndFetchUserDetails(InboxSearchCriteria criteria, HashMap<String, Object> moduleSearchCriteria, RequestInfo requestInfo) {
        log.trace("Method invoked: validateAndFetchUserDetails");
        List<String> userUUIDs = new ArrayList<>();
        List<String> citizenRoles = Collections.emptyList();
        
        if (moduleSearchCriteria.containsKey(MOBILE_NUMBER_PARAM)) {
            String tenantId = criteria.getTenantId();
            String mobileNumber = String.valueOf(moduleSearchCriteria.get(MOBILE_NUMBER_PARAM));
            Map<String, List<String>> userDetailsMap = fetchUserUUID(mobileNumber, requestInfo, tenantId);
            userUUIDs = userDetailsMap.get(USER_UUID);
            citizenRoles = userDetailsMap.get(USER_ROLES);
            Boolean isUserPresentForGivenMobileNumber = CollectionUtils.isEmpty(userUUIDs) ? false : true;
            
            if (!isUserPresentForGivenMobileNumber) {
                log.warn("No user found for mobile number");
                return null;
            }
            log.debug("User UUIDs retrieved from mobile number - count: {}", userUUIDs.size());
        } else {
            List<String> roles = requestInfo.getUserInfo().getRoles().stream().map(Role::getCode).collect(Collectors.toList());
            if(roles.contains(CITIZEN)) {
                userUUIDs.add(requestInfo.getUserInfo().getUuid());
                citizenRoles = roles;
                log.debug("Using current user UUID for citizen role");
            }
        }
        
        UserDetails details = new UserDetails();
        details.userUUIDs = userUUIDs;
        details.citizenRoles = citizenRoles;
        return details;
    }

    private Map<String, Object> buildBPASearchCriteria(InboxSearchCriteria criteria, HashMap<String, String> StatusIdNameMap,
                                                       RequestInfo requestInfo, HashMap<String, Object> moduleSearchCriteria,
                                                       ProcessInstanceSearchCriteria processCriteria,
                                                       List<String> userUUIDs, List<String> citizenRoles) {
        log.trace("Method invoked: buildBPASearchCriteria");
        Map<String, Object> searchCriteria = getSearchCriteria(criteria, StatusIdNameMap, requestInfo,
                moduleSearchCriteria, processCriteria, userUUIDs, citizenRoles);
        searchCriteria.put(OFFSET_PARAM, criteria.getOffset());
        searchCriteria.put(NO_OF_RECORDS_PARAM, criteria.getLimit());
        moduleSearchCriteria.put(LIMIT_PARAM, criteria.getLimit());
        log.debug("BPA search criteria built");
        return searchCriteria;
    }

    private Map<String, Object> buildBPASearcherRequest(RequestInfo requestInfo, Map<String, Object> searchCriteria) {
        log.trace("Method invoked: buildBPASearcherRequest");
        Map<String, Object> searcherRequest = new HashMap<>();
        searcherRequest.put(REQUESTINFO_PARAM, requestInfo);
        searcherRequest.put(SEARCH_CRITERIA_PARAM, searchCriteria);
        log.debug("BPA searcher request built");
        return searcherRequest;
    }

    private List<String> fetchBPAApplicationNumbers(HashMap<String, Object> moduleSearchCriteria,
                                                  Map<String, Object> searcherRequest,
                                                  RequestInfo requestInfo, List<String> citizenRoles) {
        log.trace("Method invoked: fetchBPAApplicationNumbers");
        List<String> applicationNumbers = new ArrayList<>();
        
        if (citizenHasStakeholderRoles(requestInfo, citizenRoles)) {
            String uri = buildBPAStakeholderUri(moduleSearchCriteria);
            Object result = restTemplate.postForObject(uri, searcherRequest, Map.class);
            applicationNumbers = JsonPath.read(result, "$.BPAs.*.applicationno");
            log.debug("Fetched application numbers from stakeholder endpoint - count: {}", applicationNumbers.size());
        } else {
            String citizenUri = buildBPACitizenUri(moduleSearchCriteria);
            Object result = restTemplate.postForObject(citizenUri, searcherRequest, Map.class);
            List<String> citizenApplicationsNumbers = JsonPath.read(result, "$.BPAs.*.applicationno");
            applicationNumbers.addAll(citizenApplicationsNumbers);
            log.debug("Fetched application numbers from citizen endpoint - count: {}", citizenApplicationsNumbers.size());
        }
        
        return applicationNumbers;
    }

    private String buildBPAStakeholderUri(HashMap<String, Object> moduleSearchCriteria) {
        log.trace("Method invoked: buildBPAStakeholderUri");
        StringBuilder uri = new StringBuilder();
        if (moduleSearchCriteria.containsKey(SORT_ORDER_PARAM)
                && moduleSearchCriteria.get(SORT_ORDER_PARAM).equals(DESC_PARAM))
            uri.append(searcherHost).append(bpaInboxSearcherDescEndpoint);
        else
            uri.append(searcherHost).append(bpaInboxSearcherEndpoint);
        log.debug("BPA stakeholder URI built: {}", uri.toString());
        return uri.toString();
    }

    private String buildBPACitizenUri(HashMap<String, Object> moduleSearchCriteria) {
        log.trace("Method invoked: buildBPACitizenUri");
        StringBuilder citizenUri = new StringBuilder();
        if (moduleSearchCriteria.containsKey(SORT_ORDER_PARAM)
                && moduleSearchCriteria.get(SORT_ORDER_PARAM).equals(DESC_PARAM))
            citizenUri.append(searcherHost).append(bpaCitizenInboxSearcherDescEndpoint);
        else
            citizenUri.append(searcherHost).append(bpaCitizenInboxSearcherEndpoint);
        log.debug("BPA citizen URI built: {}", citizenUri.toString());
        return citizenUri.toString();
    }

    private static class UserDetails {
        List<String> userUUIDs;
        List<String> citizenRoles;
    }

    private Map<String, Object> getSearchCriteria(InboxSearchCriteria criteria, Map<String, String> statusIdNameMap,
            RequestInfo requestInfo, Map<String, Object> moduleSearchCriteria,
            ProcessInstanceSearchCriteria processCriteria, List<String> userUUIDs, List<String> userRoles) {
        Map<String, Object> searchCriteria = new HashMap<>();

        searchCriteria.put(TENANT_ID_PARAM, criteria.getTenantId());
        searchCriteria.put(BUSINESS_SERVICE_PARAM, processCriteria.getBusinessService());

        // Accommodating module search criteria in searcher request
        if (moduleSearchCriteria != null && (moduleSearchCriteria.containsKey(MOBILE_NUMBER_PARAM) || userRoles.contains(CITIZEN))
                && !CollectionUtils.isEmpty(userUUIDs)) {
            searchCriteria.put(USERID_PARAM, userUUIDs);
        }
        if (moduleSearchCriteria != null && moduleSearchCriteria.containsKey(LOCALITY_PARAM)) {
            List<String> localities = Arrays.asList(String.valueOf(moduleSearchCriteria.get(LOCALITY_PARAM)).split(","));
            searchCriteria.put(LOCALITY_PARAM, localities);
        }
        if (moduleSearchCriteria != null && moduleSearchCriteria.containsKey(APPROVAL_NUMBER_PARAM)) {
            searchCriteria.put(APPROVAL_NUMBER_PARAM, moduleSearchCriteria.get(APPROVAL_NUMBER_PARAM));
        }
        if (moduleSearchCriteria != null && moduleSearchCriteria.containsKey(BPA_APPLICATION_NUMBER_PARAM)) {
            searchCriteria.put(BPA_APPLICATION_NUMBER_PARAM, moduleSearchCriteria.get(BPA_APPLICATION_NUMBER_PARAM));
        }
        if (moduleSearchCriteria != null && moduleSearchCriteria.containsKey(APPLICATION_TYPE)) {
            searchCriteria.put(APPLICATION_TYPE, moduleSearchCriteria.get(APPLICATION_TYPE));
        }
        if (moduleSearchCriteria != null && moduleSearchCriteria.containsKey(SERVICE_TYPE)) {
            searchCriteria.put(SERVICE_TYPE, moduleSearchCriteria.get(SERVICE_TYPE));
        }

        // Accommodating process search criteria in searcher request
        if (!ObjectUtils.isEmpty(processCriteria.getAssignee())) {
            searchCriteria.put(ASSIGNEE_PARAM, processCriteria.getAssignee());
        }
        if (!ObjectUtils.isEmpty(processCriteria.getStatus())) {
            searchCriteria.put(STATUS_PARAM, processCriteria.getStatus());
        } else {
            if (statusIdNameMap != null && statusIdNameMap.values().size() > 0) {
                if (CollectionUtils.isEmpty(processCriteria.getStatus())) {
                    searchCriteria.put(STATUS_PARAM, statusIdNameMap.keySet());
                }
            }
        }
        return searchCriteria;
    }

    public Integer fetchApplicationCountFromSearcher(InboxSearchCriteria criteria,
            HashMap<String, String> StatusIdNameMap, RequestInfo requestInfo) {
        log.trace("Method invoked: fetchApplicationCountFromSearcher");
        HashMap<String, Object> moduleSearchCriteria = criteria.getModuleSearchCriteria();
        ProcessInstanceSearchCriteria processCriteria = criteria.getProcessSearchCriteria();
        
        UserDetails userDetails = validateAndFetchUserDetails(criteria, moduleSearchCriteria, requestInfo);
        if (userDetails == null || userDetails.userUUIDs.isEmpty()) {
            log.debug("Search result empty - returning 0");
            return 0;
        }

        Map<String, Object> searchCriteria = getSearchCriteria(criteria, StatusIdNameMap, requestInfo,
                moduleSearchCriteria, processCriteria, userDetails.userUUIDs, userDetails.citizenRoles);
        Map<String, Object> searcherRequest = buildBPASearcherRequest(requestInfo, searchCriteria);
        
        Integer totalCount = fetchBPACount(searcherRequest, requestInfo, userDetails.citizenRoles);
        log.info("Application count retrieved from searcher - count: {}", totalCount);
        
        return totalCount;
    }

    private Integer fetchBPACount(Map<String, Object> searcherRequest, RequestInfo requestInfo, List<String> citizenRoles) {
        log.trace("Method invoked: fetchBPACount");
        Integer totalCount = 0;
        
        if (citizenHasStakeholderRoles(requestInfo, citizenRoles)) {
            String uri = searcherHost + bpaInboxSearcherCountEndpoint;
            Object result = restTemplate.postForObject(uri, searcherRequest, Map.class);
            double count = JsonPath.read(result, "$.TotalCount[0].count");
            totalCount = (int) count;
            log.debug("Fetched count from stakeholder endpoint - count: {}", totalCount);
        } else {
            String citizenUri = searcherHost + bpaCitizenInboxSearcherCountEndpoint;
            Object citizenResult = restTemplate.postForObject(citizenUri, searcherRequest, Map.class);
            double citizenCount = JsonPath.read(citizenResult, "$.TotalCount[0].count");
            totalCount = totalCount + (int) citizenCount;
            log.debug("Fetched count from citizen endpoint - count: {}", (int) citizenCount);
        }
        
        return totalCount;
    }

    private Map<String, List<String>> fetchUserUUID(String mobileNumber, RequestInfo requestInfo, String tenantId) {
        Map<String, List<String>> userDetails = new ConcurrentHashMap<>();
        StringBuilder uri = new StringBuilder();
        uri.append(userHost).append(userSearchEndpoint);
        Map<String, Object> userSearchRequest = new HashMap<>();
        userSearchRequest.put("RequestInfo", requestInfo);
        userSearchRequest.put("tenantId", tenantId);
        userSearchRequest.put("userType", CITIZEN);
        userSearchRequest.put("mobileNumber", mobileNumber);
        try {
            Object user = serviceRequestRepository.fetchResult(uri, userSearchRequest);
            if (null != user) {
                // log.info(user.toString());
                userDetails.put(USER_UUID, JsonPath.read(user, "$.user.*.uuid"));
                userDetails.put(USER_ROLES, new ArrayList<>(new HashSet<>(JsonPath.read(user, "$.user.*.roles.*.code"))));
            } else {
                log.error("Service returned null while fetching user for mobile number - " + mobileNumber);
            }
        } catch (Exception e) {
            log.error("Exception while fetching user for mobile number - " + mobileNumber);
            log.error("Exception trace: ", e);
        }
        return userDetails;
    }

    private boolean citizenHasStakeholderRoles(RequestInfo requestInfo, List<String> citizenRoles) {
        if (citizenRoles.isEmpty())
            citizenRoles = requestInfo.getUserInfo().getRoles().stream().map(Role::getCode)
                    .collect(Collectors.toList());
        if (!citizenRoles.isEmpty() && citizenRoles.size() > 1 && citizenRoles.contains(CITIZEN))
            return true;
        return false;
    }

    public List<Map<String, String>> fetchTenantWiseApplicationNumbersForCitizenInboxFromSearcher(InboxSearchCriteria criteria,
            Map<String, String> statusIdNameMap, RequestInfo requestInfo) {
        List<Map<String, String>> tenantWiseApplns = new ArrayList<>();
        HashMap<String, Object> moduleSearchCriteria = criteria.getModuleSearchCriteria();
        ProcessInstanceSearchCriteria processCriteria = criteria.getProcessSearchCriteria();
        Boolean isSearchResultEmpty = false;
        Boolean isMobileNumberPresent = true;
        List<String> userUUIDs = new ArrayList<>();
        List<String> citizenRoles = new ArrayList<>();
        if ((moduleSearchCriteria == null || moduleSearchCriteria.isEmpty()) || (moduleSearchCriteria != null && !moduleSearchCriteria.containsKey(MOBILE_NUMBER_PARAM))) {
            moduleSearchCriteria = new HashMap<>();
            moduleSearchCriteria.put(MOBILE_NUMBER_PARAM, requestInfo.getUserInfo().getMobileNumber());
        } 
        if (Boolean.TRUE.equals(isMobileNumberPresent)) {
            String tenantId = criteria.getTenantId();
            String mobileNumber = String.valueOf(moduleSearchCriteria.get(MOBILE_NUMBER_PARAM));
            Map<String, List<String>> userDetails = fetchUserUUID(mobileNumber, requestInfo, tenantId);
            userUUIDs = userDetails.get(USER_UUID);
            citizenRoles = userDetails.get(USER_ROLES);
            Boolean isUserPresentForGivenMobileNumber = !CollectionUtils.isEmpty(userUUIDs);
            isSearchResultEmpty = !isUserPresentForGivenMobileNumber;
            if (Boolean.TRUE.equals(isSearchResultEmpty)) {
                userUUIDs.add(requestInfo.getUserInfo().getUuid());
                List<String> roles = requestInfo.getUserInfo().getRoles().stream().map(Role::getCode).collect(Collectors.toList());
                citizenRoles.addAll(roles);
            }
        } /*
           * else { userUUIDs.add(requestInfo.getUserInfo().getUuid());
           * citizenRoles.addAll(requestInfo.getUserInfo().getRoles().stream().map(Role::getCode).collect(Collectors.toList())); }
           */

        if (Boolean.FALSE.equals(isSearchResultEmpty)) {
            Object result = null;

            Map<String, Object> searcherRequest = new HashMap<>();
            Map<String, Object> searchCriteria = getSearchCriteria(criteria, statusIdNameMap, requestInfo,
                    moduleSearchCriteria, processCriteria, userUUIDs, citizenRoles);

            searcherRequest.put(REQUESTINFO_PARAM, requestInfo);
            searcherRequest.put(SEARCH_CRITERIA_PARAM, searchCriteria);
            if (citizenHasStakeholderRoles(requestInfo, citizenRoles)) {
                StringBuilder uri = new StringBuilder();
                uri.append(searcherHost).append(bpaStakeholderInboxTenantWiseApplnNosEndpoint);
                result = restTemplate.postForObject(uri.toString(), searcherRequest, Map.class);
                tenantWiseApplns = JsonPath.read(result, "$.BPA.*");
            } else {
                StringBuilder citizenUri = new StringBuilder();
                citizenUri.append(searcherHost).append(bpaCitizenInboxTenantWiseApplnNosEndpoint);
                result = restTemplate.postForObject(citizenUri.toString(), searcherRequest, Map.class);
                tenantWiseApplns = JsonPath.read(result, "$.BPA.*");
            }
        }
        return tenantWiseApplns;
    }

}
