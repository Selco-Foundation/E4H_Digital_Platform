package org.egov.im.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.util.DateParserUtil;
import org.egov.im.util.HRMSUtil;
import org.egov.im.util.UserUtils;
import org.egov.im.web.models.*;
import org.egov.im.web.models.user.CreateUserRequest;
import org.egov.im.web.models.user.UserDetailResponse;
import org.egov.im.web.models.user.UserSearchRequest;
import org.egov.im.web.models.workflow.ProcessInstance;
import org.egov.im.web.models.workflow.ProcessInstanceResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {

    private final UserUtils userUtils;
    private final IMConfiguration config;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper mapper;
    private final HRMSUtil hrmsUtils;
    private final DateParserUtil dateParserUtil;

    /**
     * Calls user service to enrich user from search or upsert user
     *
     * @param request
     */
    public void callUserService(IncidentRequest request) {

        if (!StringUtils.isEmpty(request.getIncident().getReporter().getUuid()))
            enrichUser(request);
        else
            upsertUser(request);

    }

    /**
     * Calls user search to fetch the list of user and enriches it in serviceWrappers
     *
     * @param incidentWrappers
     */
    public void enrichUsers(List<IncidentWrapper> incidentWrappers) {

        Set<String> uuids = new HashSet<>();

        incidentWrappers.forEach(incidentWrapper -> {
            uuids.add(incidentWrapper.getIncident().getAccountId());
        });

        Map<String, User> idToUserMap = searchBulkUser(new LinkedList<>(uuids));

        incidentWrappers.forEach(incidentWrapper -> {
            incidentWrapper.getIncident()
                    .setReporter(idToUserMap.get(incidentWrapper.getIncident().getAccountId()));
        });

    }


    /**
     * Creates or updates the user based on if the user exists. The user existance is searched based on userName = mobileNumber
     * If the there is already a user with that mobileNumber, the existing user is updated
     *
     * @param request
     */
    private void upsertUser(IncidentRequest request) {

        User user = request.getIncident().getReporter();
        String tenantId = request.getIncident().getTenantId();
        User userServiceResponse = null;

        // Search on mobile number as user name
        UserDetailResponse userDetailResponse = searchUser(tenantId, null, user.getMobileNumber());
        if (!userDetailResponse.getUser().isEmpty()) {
            User userFromSearch = userDetailResponse.getUser().get(0);
            if (!user.getName().equalsIgnoreCase(userFromSearch.getName())) {
                userServiceResponse = updateUser(request.getRequestInfo(), user, userFromSearch);
            } else userServiceResponse = userDetailResponse.getUser().get(0);
        } else {
            userServiceResponse = createUser(request.getRequestInfo(), tenantId, user);
        }

        // Enrich the accountId
        request.getIncident().setAccountId(userServiceResponse.getUuid());
    }


    /**
     * Calls user search to fetch a user and enriches it in request
     *
     * @param request
     */
    private void enrichUser(IncidentRequest request) {

        RequestInfo requestInfo = request.getRequestInfo();
        String accountId = request.getIncident().getReporter().getUuid();
        String tenantId = request.getIncident().getReporter().getTenantId();

        UserDetailResponse userDetailResponse = searchUser(tenantId, accountId, null);

        if (userDetailResponse.getUser().isEmpty())
            throw new CustomException("INVALID_ACCOUNTID", "No user exist for the given accountId");

        else request.getIncident().setReporter(userDetailResponse.getUser().get(0));

    }

    /**
     * Creates the user from the given userInfo by calling user service
     *
     * @param requestInfo
     * @param tenantId
     * @param userInfo
     * @return
     */
    private User createUser(RequestInfo requestInfo, String tenantId, User userInfo) {

        userUtils.addUserDefaultFields(userInfo.getMobileNumber(), tenantId, userInfo);
        StringBuilder uri = new StringBuilder(config.getUserHost())
                .append(config.getUserContextPath())
                .append(config.getUserCreateEndpoint());


        UserDetailResponse userDetailResponse = userUtils.userCall(new CreateUserRequest(requestInfo, userInfo), uri);

        return userDetailResponse.getUser().get(0);

    }

    /**
     * Updates the given user by calling user service
     *
     * @param requestInfo
     * @param user
     * @param userFromSearch
     * @return
     */
    private User updateUser(RequestInfo requestInfo, User user, User userFromSearch) {

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
     *
     * @param stateLevelTenant
     * @param accountId
     * @param userName
     * @return
     */
    private UserDetailResponse searchUser(String stateLevelTenant, String accountId, String userName) {

        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType(USERTYPE_EMPLOYEE);
        userSearchRequest.setTenantId(stateLevelTenant);

        if (StringUtils.isEmpty(accountId) && StringUtils.isEmpty(userName))
            return null;

        if (!StringUtils.isEmpty(accountId))
            userSearchRequest.setUuid(Collections.singletonList(accountId));

        if (!StringUtils.isEmpty(userName))
            userSearchRequest.setUserName(userName);

        log.info(stateLevelTenant + "," + accountId);
        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userUtils.userCall(userSearchRequest, uri);

    }

    /**
     * calls the user search API based on the given list of user uuids
     *
     * @param uuids
     * @return
     */
    private Map<String, User> searchBulkUser(List<String> uuids) {

        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType(USERTYPE_EMPLOYEE);


        if (!CollectionUtils.isEmpty(uuids))
            userSearchRequest.setUuid(uuids);


        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        UserDetailResponse userDetailResponse = userUtils.userCall(userSearchRequest, uri);
        List<User> users = userDetailResponse.getUser();

        if (CollectionUtils.isEmpty(users))
            throw new CustomException("USER_NOT_FOUND", "No user found for the uuids");

        return users.stream().collect(Collectors.toMap(User::getUuid, Function.identity()));
    }

    /**
     * Enriches the list of userUuids associated with the mobileNumber in the search criteria
     *
     * @param tenantId
     * @param criteria
     */
    public void enrichUserIds(String tenantId, RequestSearchCriteria criteria) {

        String mobileNumber = criteria.getMobileNumber();

        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType(USERTYPE_EMPLOYEE);
        userSearchRequest.setTenantId(tenantId);
        userSearchRequest.setMobileNumber(mobileNumber);

        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        UserDetailResponse userDetailResponse = userUtils.userCall(userSearchRequest, uri);
        List<User> users = userDetailResponse.getUser();

        Set<String> userIds = users.stream().map(User::getUuid).collect(Collectors.toSet());
        criteria.setUserIds(userIds);
    }

    public ProcessInstance findProcessInstanceByAction(IncidentRequest request,
                                                       String action,
                                                       StringBuilder url) {
        String tenantId = request.getIncident().getTenantId();
        RequestInfo requestInfo = request.getRequestInfo();

        User userInfoCopy = requestInfo.getUserInfo();
        User userInfo = getInternalMicroserviceUser(tenantId);
        requestInfo.setUserInfo(userInfo);

        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);

        ProcessInstanceResponse processInstanceResponse;
        try {
            processInstanceResponse = mapper.convertValue(result, ProcessInstanceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException("PARSING_ERROR", "Failed to parse response of workflow processInstance search");
        }

        if (CollectionUtils.isEmpty(processInstanceResponse.getProcessInstances())) {
            throw new CustomException("WORKFLOW_NOT_FOUND", "The workflow object is not found");
        }

        ProcessInstance processInstanceToReturn = processInstanceResponse.getProcessInstances()
                .stream()
                .filter(processInstance -> processInstance.getAction().equalsIgnoreCase(action))
                .findFirst()
                .orElse(null);

        requestInfo.setUserInfo(userInfoCopy);
        return processInstanceToReturn;
    }

    public String getAssignerMobileNumber(IncidentRequest request, String action, StringBuilder url) {
        ProcessInstance processInstance = findProcessInstanceByAction(request, action, url);

        User assigner = processInstance.getAssigner();
        return assigner.getMobileNumber();
    }

    public String getAssigneeMobileNumber(IncidentRequest request, String action, StringBuilder url) {
        ProcessInstance processInstance = findProcessInstanceByAction(request, action, url);

        List<User> assignees = processInstance.getAssignes();
        if (assignees == null || assignees.size() < 2) {
            throw new CustomException("INVALID_ASSIGNEE", "No assignee found at index 1");
        }
        return assignees.get(1).getMobileNumber();
    }

    public User getInternalMicroserviceUser(String tenantId) {
        //Creating role with INTERNAL_MICROSERVICE_ROLE
        Role role = Role.builder()
                .name("Internal Microservice Role").code("INTERNAL_MICROSERVICE_ROLE")
                .tenantId(tenantId).build();

        //Creating userinfo with uuid and role of internal micro service role
        return User.builder()
                .uuid(config.getEgovInternalMicroserviceUserUuid())
                .type("SYSTEM")
                .roles(Collections.singletonList(role)).id(0L).build();
    }

    public Map<String, String> getHRMSEmployee(IncidentRequest request, String role) {
        Map<String, String> reassigneeDetails = new HashMap<>();

        List<String> employeeName;
        List<String> employeeMobile;
        List<String> employeeUUID;

        StringBuilder url = null;
        if (request.getWorkflow().getAssignes() != null)
            url = hrmsUtils.getHRMSURI(request.getWorkflow().getAssignes(), request.getIncident().getTenantId(), role);
        else
            url = hrmsUtils.getHRMSURI(null, request.getIncident().getTenantId(), role);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(request.getRequestInfo()).build();
        Object response = serviceRequestRepository.fetchResult(url, requestInfoWrapper);

        employeeName = JsonPath.read(response, HRMS_EMP_NAME_JSONPATH);
        employeeMobile = JsonPath.read(response, HRMS_EMP_MOBILE_JSONPATH);
        employeeUUID = JsonPath.read(response, HRMS_EMP_UUID_JSONPATH);
        reassigneeDetails.put("employeeName", employeeName.get(0));
        reassigneeDetails.put("employeeMobile", employeeMobile.get(0));
        reassigneeDetails.put("employeeUUID", employeeUUID.get(0));
        return reassigneeDetails;
    }

    /**
     * Fetches User Object based on the UUID.
     *
     * @param request - UUID of User
     * @return - Returns User object with given UUID
     */
    public User fetchUserByUUID(IncidentRequest request, String uuidString) {
        String tenantId = request.getIncident().getTenantId();
        RequestInfo requestInfo = request.getRequestInfo();
        User userInfo = getInternalMicroserviceUser(tenantId);
        requestInfo.setUserInfo(userInfo);

        StringBuilder uri = new StringBuilder();
        uri.append(config.getUserHost()).append(config.getUserSearchEndpoint());
        Map<String, Object> userSearchRequest = new HashMap<>();
        userSearchRequest.put("RequestInfo", requestInfo);
        userSearchRequest.put("tenantId", tenantId);
        userSearchRequest.put("userType", "EMPLOYEE");
        Set<String> uuid = new HashSet<>();
        uuid.add(uuidString);
        userSearchRequest.put("uuid", uuid);
        User user = null;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap
                    = (Map<String, Object>) serviceRequestRepository.fetchResult(uri, userSearchRequest);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> users = (List<Map<String, Object>>) responseMap.get("user");
            if (CollectionUtils.isEmpty(users)) {
                throw new CustomException("USER_NOT_FOUND", "No user found with UUID: " + uuidString);
            }
            String dobFormat = "yyyy-MM-dd";
            dateParserUtil.parseResponse(responseMap, dobFormat);
            return mapper.convertValue(users.get(0), User.class);

        } catch (IllegalArgumentException e) {
            log.error("Error parsing user object for UUID {}: {}", uuidString, e.getMessage());
            throw new CustomException("PARSING_ERROR", "Failed to parse user object");
        } catch (RuntimeException e) {
            log.error("Exception while trying parse user object: ", e);
            throw new CustomException("USER_FETCH_ERROR", "Error fetching user with UUID: " + uuidString);
        }
    }

}
