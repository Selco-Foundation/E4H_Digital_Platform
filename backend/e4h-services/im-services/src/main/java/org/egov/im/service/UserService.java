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

import static org.egov.im.util.IMConstants.*;

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

        if(!StringUtils.isEmpty(request.getIncident().getReporter().getUuid()))
        		enrichUser(request);
        else
            upsertUser(request);

    }

    /**
     * Calls user search to fetch the list of user and enriches it in serviceWrappers
     * @param serviceWrappers
     */
    public void enrichUsers(List<IncidentWrapper> incidentWrappers){

        Set<String> uuids = new HashSet<>();

        incidentWrappers.forEach(incidentWrapper -> {
            uuids.add(incidentWrapper.getIncident().getAccountId());
        });

        Map<String, User> idToUserMap = searchBulkUser(new LinkedList<>(uuids));

        incidentWrappers.forEach(incidentWrapper -> {
        	incidentWrapper.getIncident().setReporter(idToUserMap.get(incidentWrapper.getIncident().getAccountId()));
        });

    }


    /**
     * Creates or updates the user based on if the user exists. The user existance is searched based on userName = mobileNumber
     * If the there is already a user with that mobileNumber, the existing user is updated
     * @param request
     */
    private void upsertUser(IncidentRequest request){

        User user = request.getIncident().getReporter();
        String tenantId = request.getIncident().getTenantId();
        User userServiceResponse = null;

        // Search on mobile number as user name
        UserDetailResponse userDetailResponse = searchUser(tenantId,null, user.getMobileNumber());
        if (!userDetailResponse.getUser().isEmpty()) {
            User userFromSearch = userDetailResponse.getUser().get(0);
            if(!user.getName().equalsIgnoreCase(userFromSearch.getName())){
                userServiceResponse = updateUser(request.getRequestInfo(),user,userFromSearch);
            }
            else userServiceResponse = userDetailResponse.getUser().get(0);
        }
        else {
            userServiceResponse = createUser(request.getRequestInfo(),tenantId,user);
        }

        // Enrich the accountId
        request.getIncident().setAccountId(userServiceResponse.getUuid());
    }


    /**
     * Calls user search to fetch a user and enriches it in request
     * @param request
     */
    private void enrichUser(IncidentRequest request){

        RequestInfo requestInfo = request.getRequestInfo();
        String accountId = request.getIncident().getReporter().getUuid();
        String tenantId = request.getIncident().getReporter().getTenantId();

        UserDetailResponse userDetailResponse = searchUser(tenantId,accountId,null);

        if(userDetailResponse.getUser().isEmpty())
            throw new CustomException("INVALID_ACCOUNTID","No user exist for the given accountId");

        else request.getIncident().setReporter(userDetailResponse.getUser().get(0));

    }

    /**
     * Creates the user from the given userInfo by calling user service
     * @param requestInfo
     * @param tenantId
     * @param userInfo
     * @return
     */
    private User createUser(RequestInfo requestInfo,String tenantId, User userInfo) {

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

        log.info(stateLevelTenant+","+accountId);        
        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userUtils.userCall(userSearchRequest,uri);

    }

    /**
     * calls the user search API based on the given list of user uuids
     * @param uuids
     * @return
     */
    private Map<String,User> searchBulkUser(List<String> uuids){

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

        String mobileNumber = criteria.getMobileNumber();

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
    }


    /**
     * Handles the login reporting for a user.
     * Only processes users with COMPLAINANT or COMPLAINT_RESOLVER roles.
     * For COMPLAINANT, enriches the report with location details from MDMS.
     * Skips users who also have the COMPLAINT_ASSESSOR role.
     *
     * @param userRequest The user request containing user info and request details.
     */
    public void loginReport(UserRequest userRequest) {
        try {
            User userInfo = userRequest.getUser();
            if (userInfo.getRoles() == null || userInfo.getRoles().isEmpty()) {
                log.info("No roles found for user");
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

                } else {
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

    private void setBlockAndDistrictFromMdms(Object mdmsResult, String tenantId, UserLoginReport userLoginReport) {
        if (!(mdmsResult instanceof Map)) {
            log.error("mdmsResult is not a Map. Actual type: {}", mdmsResult != null ? mdmsResult.getClass().getName() : "null");
            return;
        }
        Map<String, Object> resultMap;
        try {
            resultMap = (Map<String, Object>) mdmsResult;
        } catch (ClassCastException e) {
            log.error("Failed to cast mdmsResult to Map: {}", e.getMessage(), e);
            return;
        }
        Object mdmsResObj = resultMap.get("MdmsRes");
        if (!(mdmsResObj instanceof Map)) {
            log.error("MdmsRes is not a Map. Actual type: {}", mdmsResObj != null ? mdmsResObj.getClass().getName() : "null");
            return;
        }
        Map<String, Object> mdmsRes = (Map<String, Object>) mdmsResObj;
        Object tenantMapObj = mdmsRes.get("tenant");
        if (!(tenantMapObj instanceof Map)) {
            log.error("tenant is not a Map. Actual type: {}", tenantMapObj != null ? tenantMapObj.getClass().getName() : "null");
            return;
        }
        Map<String, Object> tenantMap = (Map<String, Object>) tenantMapObj;
        Object tenantsObj = tenantMap.get("tenants");
        if (!(tenantsObj instanceof List)) {
            log.error("tenants is not a List. Actual type: {}", tenantsObj != null ? tenantsObj.getClass().getName() : "null");
            return;
        }
        List<Map<String, Object>> tenants;
        try {
            tenants = (List<Map<String, Object>>) tenantsObj;
        } catch (ClassCastException e) {
            log.error("Failed to cast tenants to List<Map<String, Object>>: {}", e.getMessage(), e);
            return;
        }
        for (Map<String, Object> tenant : tenants) {
            String code = (String) tenant.get("code");
            if (!tenantId.equals(code)) {
                continue;
            }
            Object cityObj = tenant.get("city");
            String block = "";
            String district = "";
            if (cityObj instanceof Map) {
                Map<String, Object> city = (Map<String, Object>) cityObj;
                block = (String) city.get("blockCode");
                if (block != null && block.contains(".")) {
                    block = block.substring(block.indexOf('.') + 1);
                }
                district = (String) city.get("districtName");
            } else if (cityObj != null) {
                log.error("city is not a Map. Actual type: {}", cityObj.getClass().getName());
            }
            userLoginReport.setBlock(block == null ? "" : block);
            userLoginReport.setDistrict(district == null ? "" : district);
            String healthCenter = (String) tenant.get("name");
            String state = (String) tenant.get("address");
            userLoginReport.setHealthFacilityName(healthCenter == null ? "" : healthCenter);
            userLoginReport.setState(state == null ? "" : state);
            break;
        }
    }
}
