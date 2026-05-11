package org.egov.im.service;


import com.jayway.jsonpath.JsonPath;
import org.egov.common.contract.request.RequestInfo;

import org.egov.im.config.IMConfiguration;
import org.egov.im.producer.Producer;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.util.HRMSUtil;
import org.egov.im.util.UserUtils;
import org.egov.im.web.models.*;
import org.egov.im.web.models.user.CreateUserRequest;
import org.egov.im.web.models.user.UserDetailResponse;
import org.egov.im.web.models.user.UserSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.egov.im.util.IMConstants.*;

@org.springframework.stereotype.Service
@Slf4j
public class UserService {


    private UserUtils userUtils;

    private IMConfiguration config;

    private ServiceRequestRepository repository;

    private Producer producer;

    private HRMSUtil hrmsUtil;

    private RestTemplate restTemplate;

    @Autowired
    public UserService(UserUtils userUtils, IMConfiguration config, ServiceRequestRepository repository, Producer producer,
                       HRMSUtil hrmsUtil, RestTemplate restTemplate) {
        this.userUtils = userUtils;
        this.config = config;
        this.repository = repository;
        this.producer = producer;
        this.hrmsUtil = hrmsUtil;
        this.restTemplate = restTemplate;
    }

    /**
     * Calls user service to enrich user from search or upsert user
     * @param request
     */
    public void callUserService(IncidentRequest request){
        log.trace("UserService::callUserService method invoked");
        log.debug("Processing user service call for incidentId: {}", request.getIncident().getId());
        if(!StringUtils.isEmpty(request.getIncident().getReporter().getUuid())){
            log.info("Enriching user for existing uuid: {}", request.getIncident().getReporter().getUuid());
            enrichUser(request);
        }
        else{
            log.info("Upserting user for mobileNumber: {}", request.getIncident().getReporter().getMobileNumber());
            upsertUser(request);
        }

    }

    /**
     * Calls user search to fetch the list of user and enriches it in incidentWrappers
     * @param incidentWrappers
     */
    public void enrichUsers(List<IncidentWrapper> incidentWrappers){
        log.trace("UserService::enrichUsers method invoked");
        log.debug("Enriching users for {} incident wrappers", incidentWrappers.size());

        Set<String> uuids = new HashSet<>();

        incidentWrappers.forEach(incidentWrapper -> {
            uuids.add(incidentWrapper.getIncident().getAccountId());
        });

        log.trace("Searching bulk users for {} UUIDs", uuids.size());
        Map<String, User> idToUserMap = searchBulkUser(new LinkedList<>(uuids));

        incidentWrappers.forEach(incidentWrapper -> {
        	incidentWrapper.getIncident().setReporter(idToUserMap.get(incidentWrapper.getIncident().getAccountId()));
        });
        log.debug("Users enriched successfully");

    }


    /**
     * Creates or updates the user based on if the user exists. The user existance is searched based on userName = mobileNumber
     * If the there is already a user with that mobileNumber, the existing user is updated
     * @param request
     */
    private void upsertUser(IncidentRequest request){
        log.trace("UserService::upsertUser method invoked");
        User user = request.getIncident().getReporter();
        String tenantId = request.getIncident().getTenantId();
        User userServiceResponse = null;

        // Search on mobile number as user name
        log.trace("Searching user by mobile number");
        UserDetailResponse userDetailResponse = searchUser(tenantId,null, user.getMobileNumber());
        if (!userDetailResponse.getUser().isEmpty()) {
            User userFromSearch = userDetailResponse.getUser().get(0);
            log.info("User exists with mobile: {}", user.getMobileNumber());
            if(!user.getName().equalsIgnoreCase(userFromSearch.getName())){
                log.debug("User name differs, updating user");
                userServiceResponse = updateUser(request.getRequestInfo(),user,userFromSearch);
            }
            else {
                log.debug("User name matches, using existing user");
                userServiceResponse = userDetailResponse.getUser().get(0);
            }
        }
        else {
            log.info("Creating new user with mobile: {}", user.getMobileNumber());
            userServiceResponse = createUser(request.getRequestInfo(),tenantId,user);
        }

        // Enrich the accountId
        request.getIncident().setAccountId(userServiceResponse.getUuid());
        log.debug("User upsert completed, accountId={}", userServiceResponse.getUuid());
    }


    /**
     * Calls user search to fetch a user and enriches it in request
     * @param request
     */
    private void enrichUser(IncidentRequest request){
        log.trace("UserService::enrichUser method invoked");
        RequestInfo requestInfo = request.getRequestInfo();
        String accountId = request.getIncident().getReporter().getUuid();
//        String tenantId = request.getIncident().getPhcType();

        log.trace("Searching user by accountId");
        UserDetailResponse userDetailResponse = searchUser(null,accountId,null);

        if(userDetailResponse.getUser().isEmpty()) {
            log.error("No user found for accountId: {}", accountId);
            throw new CustomException("INVALID_ACCOUNTID","No user exist for the given accountId");
        }

        request.getIncident().setReporter(userDetailResponse.getUser().get(0));
        log.debug("User enriched successfully for accountId: {}", accountId);
    }

    /**
     * Creates the user from the given userInfo by calling user service
     * @param requestInfo
     * @param tenantId
     * @param userInfo
     * @return
     */
    private User createUser(RequestInfo requestInfo,String tenantId, User userInfo) {
        log.debug("Creating user in tenantId: {} with mobile: {}", tenantId, userInfo.getMobileNumber());
        userUtils.addUserDefaultFields(userInfo.getMobileNumber(),tenantId, userInfo);
        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserCreateEndpoint());


        UserDetailResponse userDetailResponse = userUtils.userCall(new CreateUserRequest(requestInfo, userInfo), uri);

        return userDetailResponse.getUser().get(0);

    }

    /**
     * Updates the given user by calling user service
     * @param requestInfo
     * @param user
     * @param userFromSearch
     * @return
     */
    private User updateUser(RequestInfo requestInfo,User user,User userFromSearch) {
        log.debug("Updating user uuid: {} with new name: {}", userFromSearch.getUuid(), user.getName());
        userFromSearch.setName(user.getName());
        userFromSearch.setActive(true);

        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserUpdateEndpoint());


        UserDetailResponse userDetailResponse = userUtils.userCall(new CreateUserRequest(requestInfo, userFromSearch), uri);

        return userDetailResponse.getUser().get(0);

    }

    /**
     * calls the user search API based on the given accountId and userName
     * @param stateLevelTenant
     * @param accountId
     * @param userName
     * @return
     */
    private UserDetailResponse searchUser(String stateLevelTenant, String accountId, String userName){

        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType(USERTYPE_EMPLOYEE);
//        userSearchRequest.setTenantId(stateLevelTenant);

        if(StringUtils.isEmpty(accountId) && StringUtils.isEmpty(userName))
            return null;

        if(!StringUtils.isEmpty(accountId))
            userSearchRequest.setUuid(Collections.singletonList(accountId));

        if(!StringUtils.isEmpty(userName))
            userSearchRequest.setUserName(userName);

        if(!StringUtils.isEmpty(stateLevelTenant))
            userSearchRequest.setTenantId(stateLevelTenant);

        log.debug("Searching user with stateLevelTenant={}, accountId={}, userName={}", stateLevelTenant, accountId, userName);
        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userUtils.userCall(userSearchRequest,uri);

    }

    /**
     * calls the user search API based on the given list of user uuids
     * @param uuids
     * @return
     */
    public Map<String,User> searchBulkUser(List<String> uuids){
        log.debug("Searching bulk users for uuids: {}", uuids);
        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType(USERTYPE_EMPLOYEE);


        if(!CollectionUtils.isEmpty(uuids))
            userSearchRequest.setUuid(uuids);


        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        UserDetailResponse userDetailResponse = userUtils.userCall(userSearchRequest,uri);
        List<User> users = userDetailResponse.getUser();

        if(CollectionUtils.isEmpty(users))
            throw new CustomException("USER_NOT_FOUND","No user found for the uuids");

        Map<String,User> idToUserMap = users.stream().collect(Collectors.toMap(User::getUuid, Function.identity()));

        return idToUserMap;
    }

    /**
     * Enriches the list of userUuids associated with the mobileNumber in the search criteria
     * @param tenantId
     * @param criteria
     */
    public void enrichUserIds(String tenantId, RequestSearchCriteria criteria){
        log.trace("UserService::enrichUserIds method invoked");
        String mobileNumber = criteria.getMobileNumber();
        log.debug("Enriching user IDs for mobileNumber: {}, tenantId: {}", mobileNumber, tenantId);

        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType(USERTYPE_EMPLOYEE);
        userSearchRequest.setTenantId(tenantId);
        userSearchRequest.setMobileNumber(mobileNumber);

        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        UserDetailResponse userDetailResponse = userUtils.userCall(userSearchRequest,uri);
        List<User> users = userDetailResponse.getUser();

        Set<String> userIds = users.stream().map(User::getUuid).collect(Collectors.toSet());
        criteria.setUserIds(userIds);
        log.debug("Enriched {} user IDs for mobileNumber", userIds.size());
    }



    public void loginReport(UserRequest userRequest) {
        log.trace("UserService::loginReport method invoked");
        try {
            log.debug("Processing login report for user: {}", userRequest.getUser().getUserName());
            User userInfo = userRequest.getUser();
            if (userInfo.getRoles() == null || userInfo.getRoles().isEmpty()) {
                log.info("No roles found for user, skipping login report");
                return;
            }
            boolean hasAllowedRole = userInfo.getRoles().stream()
                .anyMatch(role -> ROLE_COMPLAINANT.equalsIgnoreCase(role.getCode()) || ROLE_COMPLAINT_RESOLVER.equalsIgnoreCase(role.getCode()));

            if (hasAllowedRole) {
                UserLoginReport userLoginReport = new UserLoginReport();
                userLoginReport.setId(UUID.randomUUID().toString());
                userLoginReport.setUserName(userInfo.getUserName());
                userLoginReport.setCurrentOwnerName(userInfo.getName());
                // Set the first matching allowed role as userRole
                String allowedRole = userInfo.getRoles().stream()
                    .map(role -> role.getCode())
                    .filter(code -> ROLE_COMPLAINANT.equalsIgnoreCase(code) || ROLE_COMPLAINT_RESOLVER.equalsIgnoreCase(code))
                    .findFirst().orElse("");
                userLoginReport.setUserRole(allowedRole);
                userLoginReport.setLastLoginDateTime(String.valueOf(System.currentTimeMillis()));

                if (ROLE_COMPLAINANT.equalsIgnoreCase(allowedRole)) {
                    // Check if user also has COMPLAINT_ASSESSOR role (CRM)
                    boolean hasComplaintAssessorRole = userInfo.getRoles().stream()
                        .anyMatch(role -> ROLE_COMPLAINT_ASSESSOR.equalsIgnoreCase(role.getCode()));
                    if (hasComplaintAssessorRole) {
                        return;
                    }
                    populateComplainantLocationDetails(userRequest, userLoginReport);

                } else {
                    log.debug("User is COMPLAINT_RESOLVER. Setting default empty values for location.");
                    userLoginReport.setHealthFacilityName("");
                    userLoginReport.setBlock("");
                    userLoginReport.setDistrict("");
                    userLoginReport.setState("");
                }
                producer.push(userInfo.getTenantId(), config.getSaveTopicIndexer(), userLoginReport);
            }
        } catch (Exception e) {
            log.error("Error while processing login report for user", e);
            throw new CustomException("LOGIN_REPORT_ERROR", "Unable to process login report: " + e.getMessage());
        }
    }

    private void populateComplainantLocationDetails(UserRequest userRequest, UserLoginReport userLoginReport) {
        User userInfo = userRequest.getUser();
        String tenantId = userInfo.getTenantId();
        String boundaryCode = fetchBoundaryCodeFromHrms(userRequest);

        if (boundaryCode == null || boundaryCode.isBlank()) {
            log.warn("Boundary code not found in HRMS for user: {}", userInfo.getUserName());
            userLoginReport.setHealthFacilityName("");
            userLoginReport.setDistrict("");
            userLoginReport.setBlock("");
            userLoginReport.setState("");
            return;
        }

        Map<String, String> facilityDetails = fetchFacilityDetails(boundaryCode, tenantId);
        String[] districtAndBlock = extractDistrictAndBlockFromBoundaryCode(boundaryCode);
        userLoginReport.setDistrict(firstNonBlank(facilityDetails.get("district"), districtAndBlock[0]));
        userLoginReport.setBlock(firstNonBlank(facilityDetails.get("block"), districtAndBlock[1]));
        userLoginReport.setHealthFacilityName(firstNonBlank(facilityDetails.get("healthFacilityName"), ""));
        userLoginReport.setState(firstNonBlank(facilityDetails.get("state"), extractStateFromBoundaryCode(boundaryCode)));
    }

    private String fetchBoundaryCodeFromHrms(UserRequest userRequest) {
        User userInfo = userRequest.getUser();
        if (userInfo == null || userInfo.getUuid() == null || userInfo.getUuid().isBlank() ||
                userInfo.getTenantId() == null || userInfo.getTenantId().isBlank()) {
            return null;
        }

        StringBuilder hrmsSearchUrl = hrmsUtil.getHRMSURI(Collections.singletonList(userInfo.getUuid()),
                userInfo.getTenantId(), null, null);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder()
                .requestInfo(userRequest.getRequestInfo())
                .build();

        Object hrmsResponse = repository.fetchResult(hrmsSearchUrl, requestInfoWrapper);
        if (hrmsResponse == null) {
            return null;
        }

        try {
            String boundaryCode = JsonPath.read(hrmsResponse, "$.Employees[0].jurisdictions[0].boundary");
            if (boundaryCode != null && !boundaryCode.isBlank()) {
                return boundaryCode;
            }
        } catch (Exception ignored) {
            // HRMS response does not have expected jurisdiction boundary shape.
        }

        return null;
    }

    private String[] extractDistrictAndBlockFromBoundaryCode(String boundaryCode) {
        String district = "";
        String block = "";

        if (boundaryCode == null || boundaryCode.isBlank()) {
            return new String[]{district, block};
        }

        String normalizedBoundaryCode = boundaryCode.replace('.', '_');
        List<String> segments = Arrays.stream(normalizedBoundaryCode.split("_"))
                .filter(segment -> segment != null && !segment.isBlank())
                .toList();

        if (segments.isEmpty()) {
            return new String[]{district, block};
        }

        int facilityIndex = -1;
        for (int i = 0; i < segments.size(); i++) {
            if (segments.get(i).toUpperCase(Locale.ROOT).contains("FAC/")) {
                facilityIndex = i;
                break;
            }
        }

        if (facilityIndex > 1) {
            district = segments.get(facilityIndex - 2);
            block = segments.get(facilityIndex - 1);
        } else if (segments.size() >= 4) {
            // Expected fallback shape: Country_State_District_Block
            district = segments.get(2);
            block = segments.get(3);
        } else if (segments.size() >= 3) {
            district = segments.get(segments.size() - 2);
            block = segments.get(segments.size() - 1);
        } else if (segments.size() == 2) {
            district = segments.get(0);
            block = segments.get(1);
        }

        return new String[]{district, block};
    }

    /**
     * Second segment of underscore-separated boundary codes (e.g. India_Karnataka_... -> Karnataka).
     */
    private String extractStateFromBoundaryCode(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return "";
        }
        String normalized = boundaryCode.replace('.', '_');
        List<String> segments = Arrays.stream(normalized.split("_"))
                .filter(s -> s != null && !s.isBlank())
                .toList();
        if (segments.size() < 2) {
            return "";
        }
        return segments.get(1);
    }

    private Map<String, String> fetchFacilityDetails(String boundaryCode, String tenantId) {
        Map<String, String> details = new HashMap<>();
        details.put("healthFacilityName", "");
        details.put("district", "");
        details.put("block", "");
        details.put("state", "");

        if (boundaryCode == null || boundaryCode.isBlank()) {
            return details;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(config.getFacilityHost() + config.getFacilitySearchPath())
                    .queryParam("tenantId", tenantId != null ? tenantId : "")
                    .queryParam("boundaryCode", boundaryCode)
                    .toUriString();

            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseMap = responseEntity.getBody();
            if (responseMap == null) {
                return details;
            }

            Object facilitiesObj = responseMap.get("facilities");
            if (!(facilitiesObj instanceof List<?> facilities) || facilities.isEmpty()) {
                return details;
            }

            Object firstFacility = facilities.get(0);
            if (!(firstFacility instanceof Map<?, ?> firstFacilityMap)) {
                return details;
            }

            Object facilityName = firstFacilityMap.get("facility_name");
            if (facilityName == null || facilityName.toString().isBlank()) {
                facilityName = firstFacilityMap.get("name");
            }
            if (facilityName == null || facilityName.toString().isBlank()) {
                facilityName = firstFacilityMap.get("facilityName");
            }
            details.put("healthFacilityName", facilityName == null ? "" : facilityName.toString());

            Object boundaryObj = firstFacilityMap.get("boundary");
            if (boundaryObj instanceof Map<?, ?> boundaryMap) {
                details.put("district", normalizeBoundaryName(boundaryMap.get("district")));
                details.put("block", normalizeBoundaryName(boundaryMap.get("block")));
                details.put("state", normalizeBoundaryName(boundaryMap.get("state")));
            }

            return details;
        } catch (Exception e) {
            log.error("Error fetching facility details for boundaryCode: {}", boundaryCode, e);
            return details;
        }
    }

    private String normalizeBoundaryName(Object value) {
        if (value == null) {
            return "";
        }

        String text = value.toString().trim();
        if (text.isBlank()) {
            return "";
        }

        String normalized = text.replace('.', '_');
        String[] parts = normalized.split("_");
        if (parts.length == 0) {
            return text;
        }

        String lastPart = parts[parts.length - 1];
        return lastPart == null ? "" : lastPart.trim();
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback == null ? "" : fallback;
    }

}
