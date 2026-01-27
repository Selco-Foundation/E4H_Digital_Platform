package org.egov.im.service;


import org.egov.common.contract.request.RequestInfo;

import org.egov.im.config.IMConfiguration;
import org.egov.im.producer.Producer;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.util.MDMSUtils;
import org.egov.im.util.UserUtils;
import org.egov.im.web.models.*;
import org.egov.im.web.models.user.CreateUserRequest;
import org.egov.im.web.models.user.UserDetailResponse;
import org.egov.im.web.models.user.UserSearchRequest;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.egov.im.util.IMConstants.USERTYPE_EMPLOYEE;

@org.springframework.stereotype.Service
@Slf4j
public class UserService {


    private UserUtils userUtils;

    private IMConfiguration config;

    private ServiceRequestRepository repository;

    private MDMSUtils mdmsUtils;

    private Producer producer;

    @Autowired
    public UserService(UserUtils userUtils, IMConfiguration config, ServiceRequestRepository repository, MDMSUtils mdmsUtils, Producer producer) {
        this.userUtils = userUtils;
        this.config = config;
        this.repository = repository;
        this.mdmsUtils = mdmsUtils;
        this.producer = producer;
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
        String tenantId = request.getIncident().getReporter().getTenantId();

        log.trace("Searching user by accountId");
        UserDetailResponse userDetailResponse = searchUser(tenantId,accountId,null);

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
        userSearchRequest.setTenantId(stateLevelTenant);

        if(StringUtils.isEmpty(accountId) && StringUtils.isEmpty(userName))
            return null;

        if(!StringUtils.isEmpty(accountId))
            userSearchRequest.setUuid(Collections.singletonList(accountId));

        if(!StringUtils.isEmpty(userName))
            userSearchRequest.setUserName(userName);

        log.debug("Searching user with stateLevelTenant={}, accountId={}, userName={}", stateLevelTenant, accountId, userName);
        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userUtils.userCall(userSearchRequest,uri);

    }

    /**
     * calls the user search API based on the given list of user uuids
     * @param uuids
     * @return
     */
    private Map<String,User> searchBulkUser(List<String> uuids){
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
            String roleCode = userInfo.getRoles().get(0).getCode();

            if (!isSupportedLoginReportRole(roleCode)) {
                return;
            }

            UserLoginReport userLoginReport = buildBaseLoginReport(userInfo, roleCode);

            if ("COMPLAINANT".equalsIgnoreCase(roleCode)) {
                if (hasComplaintAssessorRole(userInfo)) {
                    return;
                }
                populateComplainantLocationDetails(userRequest, userInfo, userLoginReport);
            } else {
                log.debug("User is COMPLAINT_RESOLVER. Setting default empty values for location.");
                userLoginReport.setHealthFacilityName("");
                userLoginReport.setBlock("");
                userLoginReport.setDistrict("");
                userLoginReport.setState("");
            }
            producer.push(userInfo.getTenantId(), config.getSaveTopicIndexer(), userLoginReport);
        } catch (Exception e) {
            log.error("Error while processing login report for user", e);
            throw new CustomException("LOGIN_REPORT_ERROR", "Unable to process login report: " + e.getMessage());
        }
    }

    private void setBlockAndDistrictFromMdms(Object mdmsResult, String tenantId, UserLoginReport userLoginReport) {
        if (!(mdmsResult instanceof Map)) {
            return;
        }
        Map<String, Object> resultMap = (Map<String, Object>) mdmsResult;
        Map<String, Object> mdmsRes = (Map<String, Object>) resultMap.get("MdmsRes");
        if (mdmsRes == null) {
            return;
        }
        Map<String, Object> tenantMap = (Map<String, Object>) mdmsRes.get("tenant");
        if (tenantMap == null) {
            return;
        }
        List<Map<String, Object>> tenants = (List<Map<String, Object>>) tenantMap.get("tenants");
        if (tenants == null) {
            return;
        }

        for (Map<String, Object> tenant : tenants) {
            String code = (String) tenant.get("code");
            if (!tenantId.equals(code)) {
                continue;
            }

            // City details
            Map<String, Object> city = (Map<String, Object>) tenant.get("city");
            String block = "";
            String district = "";
            if (city != null) {
                block = (String) city.get("blockCode");
                if (block != null && block.contains(".")) {
                    block = block.substring(block.indexOf('.') + 1);
                }
                district = (String) city.get("districtName");
            }
            userLoginReport.setBlock(block != null ? block : "");
            userLoginReport.setDistrict(district != null ? district : "");

            // Health facility and state
            String healthCenter = (String) tenant.get("name");
            String state = (String) tenant.get("address");
            userLoginReport.setHealthFacilityName(healthCenter != null ? healthCenter : "");
            userLoginReport.setState(state != null ? state : "");
            break; // Found the tenant, no need to continue
        }
    }

    private boolean isSupportedLoginReportRole(String roleCode) {
        return "COMPLAINANT".equalsIgnoreCase(roleCode) || "COMPLAINT_RESOLVER".equalsIgnoreCase(roleCode);
    }

    private UserLoginReport buildBaseLoginReport(User userInfo, String roleCode) {
        UserLoginReport userLoginReport = new UserLoginReport();
        userLoginReport.setId(UUID.randomUUID().toString());
        userLoginReport.setUserName(userInfo.getUserName());
        userLoginReport.setUserRole(roleCode);
        userLoginReport.setCurrentOwnerName(userInfo.getName());
        userLoginReport.setLastLoginDateTime(String.valueOf(System.currentTimeMillis()));
        return userLoginReport;
    }

    private boolean hasComplaintAssessorRole(User userInfo) {
        return userInfo.getRoles().stream()
                .anyMatch(role -> "COMPLAINT_ASSESSOR".equalsIgnoreCase(role.getCode()));
    }

    private void populateComplainantLocationDetails(UserRequest userRequest,
                                                    User userInfo,
                                                    UserLoginReport userLoginReport) {
        log.debug("Fetching tenant details from MDMS for tenant: {}", userInfo.getTenantId());
        String tenantId = userInfo.getTenantId();
        String stateLevelTenantId = tenantId.split("\\.")[0];

        MasterDetail masterDetail = MasterDetail.builder().name("tenants").build();
        List<MasterDetail> masterDetails = Collections.singletonList(masterDetail);

        ModuleDetail moduleDetail = ModuleDetail.builder()
                .moduleName("tenant")
                .masterDetails(masterDetails)
                .build();
        List<ModuleDetail> moduleDetails = Collections.singletonList(moduleDetail);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder()
                .tenantId(stateLevelTenantId)
                .moduleDetails(moduleDetails)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder()
                .requestInfo(userRequest.getRequestInfo())
                .mdmsCriteria(mdmsCriteria)
                .build();

        Object result = repository.fetchResult(mdmsUtils.getMdmsSearchUrl(), mdmsCriteriaReq);
        setBlockAndDistrictFromMdms(result, tenantId, userLoginReport);
    }
}