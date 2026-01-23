package org.egov.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.user.UserDetailResponse;
import org.egov.common.models.core.URLParams;
import org.egov.config.Configuration;
import org.egov.repository.OrganisationRepository;
import org.egov.repository.OrganisationUserRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.HRMSUtils;
import org.egov.util.MDMSUtil;
import org.egov.util.OrganisationUtil;
import org.egov.util.UserUtil;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.util.OrganisationConstant.MASTER_ORG_ROLES;
import static org.egov.util.OrganisationConstant.MDMS_ORGANIZATION_MODULE_NAME;

@Component
@Slf4j
public class OrganisationUserServiceValidator {

    private final MDMSUtil mdmsUtil;

    private final Configuration configuration;

    private final OrganisationRepository organisationRepository;

    private final OrganisationUtil organisationUtil;

    private final HRMSUtils hrmsUtils;
    private final UserUtil userUtil;

    private final OrganisationUserRepository userRepository;

    private final ObjectMapper mapper;

    private static final String MDMS_RES = "$.MdmsRes.";
    private static final String NOT_PRESENT_IN_MDMS = " is not present in MDMS";
    private static final String VALID_FROM_PARAMETER_SHOULD_BE_LESS_THAN_VALID_TO = "Valid From in search parameters should be less than Valid To";
    private static final String INVALID_ORG_SEARCH_DATE ="INVALID_ORG_SEARCH_DATE";
    @Autowired
    public OrganisationUserServiceValidator(MDMSUtil mdmsUtil, Configuration configuration, OrganisationRepository organisationRepository,
                                            OrganisationUtil organisationUtil, HRMSUtils hrmsUtils, UserUtil userUtil, OrganisationUserRepository userRepository, ObjectMapper mapper) {
        this.mdmsUtil = mdmsUtil;
        this.configuration = configuration;
        this.organisationRepository = organisationRepository;
        this.organisationUtil = organisationUtil;
        this.hrmsUtils = hrmsUtils;
        this.userUtil = userUtil;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public void validateCreateOrgUserRequest(OrgUserRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if org users request and mandatory fields are present
        validateUserOrgCreation(request);
    }

    private void validateRequestInfo(RequestInfo requestInfo) {
        if (requestInfo == null) {
            log.error("Request info is mandatory");
            throw new CustomException("REQUEST_INFO", "Request info is mandatory");
        }
        if (requestInfo.getUserInfo() == null) {
            log.error("UserInfo is mandatory in RequestInfo");
            throw new CustomException("USERINFO", "UserInfo is mandatory");
        }
        if (requestInfo.getUserInfo() != null && StringUtils.isBlank(requestInfo.getUserInfo().getUuid())) {
            log.error("UUID is mandatory in UserInfo");
            throw new CustomException("USERINFO_UUID", "UUID is mandatory");
        }
    }

    private void validateUserOrgCreation(OrgUserRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        User orgUser = request.getUser();
        if (orgUser == null) {
            log.error("User is mandatory in creation");
            throw new CustomException("Org User", "User is mandatory");
        }

        if (request.getOrganizationId() == null || request.getOrganizationId().isBlank()) {
            log.error("Organization is mandatory in org user request body");
            errorMap.put("ORGANIZATION", "Organization ID is mandatory");
        }

        // Check if organisationId already exist
        OrgSearchCriteria searchCriteria = OrgSearchCriteria.builder().ids(List.of(request.getOrganizationId())).tenantId(orgUser.getTenantId()).build();
        OrgSearchRequest orgSearchRequest = OrgSearchRequest.builder().requestInfo(request.getRequestInfo()).searchCriteria(searchCriteria).build();
        List<Organisation> organisations = organisationRepository.getOrganisations(orgSearchRequest);
        if(organisations == null || organisations.isEmpty()){
            log.error("Organization is mandatory in org user request body");
            throw new CustomException("Organization", "Organization ID do not exist");
        }

        // Validate user object fields
        validateUserRequest(orgUser);

        // Get existing user with mobile number from hrms service
        List<Employee> employee = hrmsUtils.getUserByPhoneNumber(request, orgUser.getMobileNumber());
        if (employee == null || employee.isEmpty()) { //If user doesn't exist
            Organisation organisation = organisations.get(0);
            String orgType = organisation.getOrgType();
            Map<String, List<Role>> rolesMap =  getOrgRoles(request.getRequestInfo());
            if (rolesMap !=null && !rolesMap.isEmpty() && orgType !=null && !orgType.isBlank()){
                List<Role> roles = rolesMap.get(orgType);
                List<String> roleCodesMDMS = roles.stream().map(Role::getCode).filter(Objects::nonNull).toList();
                List<String> requestRoleCodes = orgUser.getRoles().stream().map(Role::getCode).filter(Objects::nonNull).toList();
                Set<String> orgRolesReqSet = new HashSet<>();
                orgRolesReqSet.addAll(requestRoleCodes);
                // Check if Roles from request are valid
                validateOrgRoles(orgRolesReqSet, roleCodesMDMS);
                //Encrypt poc mobile number
                String mobileNumber = orgUser.getMobileNumber();
                String encryptedPocMobileNumber = organisationUtil.encryptMobileNumber(orgUser.getMobileNumber());
                if(encryptedPocMobileNumber!=null && !encryptedPocMobileNumber.isBlank()){
                    orgUser.setMobileNumber(encryptedPocMobileNumber);
                }
                // Call HRMS service to create user
                User user = User.builder()
                        .userName(orgUser.getUserName())
                        .name(orgUser.getName())
                        .gender(orgUser.getGender())
                        .mobileNumber(mobileNumber)
                        .emailId(orgUser.getEmailId())
                        .active(orgUser.getActive())
                        .dob(orgUser.getDob())
                        .locale(orgUser.getLocale())
                        .type(orgUser.getType())
                        .tenantId(orgUser.getTenantId())
                        .roles(orgUser.getRoles())
                        .jurisdictions(orgUser.getJurisdictions())
                        .build();

                Employee employee1 = hrmsUtils.buildEmployee(user, orgType);
                EmployeeRequest employeeRequest = EmployeeRequest.builder().requestInfo(request.getRequestInfo()).employees(List.of(employee1)).build();
                List<Employee> employees = hrmsUtils.createHRMSUser(employeeRequest);
                if (employees != null && !employees.isEmpty()) {
                    // User created successfully, get uuid and user infos
                    Employee employeeResp = employees.get(0);
                    request.setUser(employeeResp.getUser());
                    request.setUserId(employeeResp.getUser().getUuid());
                    employeeResp.getUser().setPassword(configuration.getDefaultUserPassword());
                    String url = configuration.getUserHost() + configuration.getUserUpdateEndpoint();
                    UserRequest userRequest = userUtil.mapToUserRequest(employeeResp.getUser());
                    CreateUserRequest createUserRequest = CreateUserRequest.builder()
                            .requestInfo(request.getRequestInfo())
                            .user(userRequest)
                            .build();
                    UserDetailResponse response = userUtil.updateUserPassword(createUserRequest, new StringBuilder(url));
                    log.info("New user created and updated");
                }
                else{
                    log.error("Error occured while creating the new user");
                    throw new CustomException("HRMS_CREATION", "Error occured while creating the new user");
                }
            }

        }
        else { // If user found, Check if user belong to another organisation record
            List<String> uuids = employee.stream().map(e -> e.getUser().getUuid()).filter(Objects::nonNull).toList();
            OrgUserSearchCriteria searchUserCriteria = OrgUserSearchCriteria.builder().userId(uuids).tenantId(orgUser.getTenantId()).build();
            OrgUserSearchRequest orgUserSearchRequest = OrgUserSearchRequest.builder().requestInfo(request.getRequestInfo()).criteria(searchUserCriteria).build();
            URLParams urlParams = URLParams.builder().limit(1).offset(0).build();
            List<OrgUser> users = userRepository.getOrgUsers(orgUserSearchRequest, urlParams);
            if(users != null && !users.isEmpty()){
                log.error("This user already belong to another org");
                throw new CustomException("Organization", "This user already belong to another org");
            }
            else{
//                request.getUser().setUuid(employee.get(0).getUser().getUuid());
                request.setUser(employee.get(0).getUser());
                request.setUserId(employee.get(0).getUser().getUuid());
            }
        }

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateUserRequest(User user) {
        if (StringUtils.isBlank(user.getTenantId())) {
            log.error("Tenant ID is mandatory in user request body");
            throw new CustomException("Organization", "Tenant ID is mandatory in user request body");
        }

        if (user.getName() == null) {
            log.error("Name is mandatory in User object");
            throw new CustomException("OrgUserCreation", "Name is mandatory in User object");
        }

        if (user.getMobileNumber() == null) {
            log.error("Mobile Number is mandatory in FieldPlans");
            throw new CustomException("OrgUserCreation", "Mobile Number is mandatory");
        }

        if (user.getEmailId() == null) {
            log.error("Email is mandatory in FieldPlans");
            throw new CustomException("OrgUserCreation", "Email is mandatory in User object");
        }

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            log.error("Roles are mandatory in User object");
            throw new CustomException("OrgUserCreation", "Roles are mandatory in User object");
        }
    }

    public void validateUpdateOrgUserRequest(OrgUserRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();
        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if org users request and mandatory fields are present
        validateUserOrgUpdate(request);
    }

    private void validateUserOrgUpdate(OrgUserRequest request) {

        Map<String, String> errorMap = new HashMap<>();
        List<Organisation> organisations = new ArrayList<>();
        User orgUser = request.getUser();
        if (orgUser == null) {
            log.error("User is mandatory in update");
            throw new CustomException("Org User", "User is mandatory");
        }

        if (request.getId() == null || request.getId().isBlank()) {
            log.error("Org User Id is mandatory in org user request body");
            throw new CustomException("UserOrg", "Org User Id is mandatory in org user request body");
        }

        if (request.getOrganizationId() == null || request.getOrganizationId().isBlank()) {
            log.error("Organization is mandatory in org user request body");
            throw new CustomException("ORGANIZATION", "Organization ID is mandatory");
        }

        // Check if user org id already exist in DB
        OrgUserSearchCriteria searchUserCriteria = OrgUserSearchCriteria.builder().id(List.of(request.getId())).tenantId(configuration.getGlobalTenantId()).build();
        OrgUserSearchRequest orgUserSearchRequest = OrgUserSearchRequest.builder().requestInfo(request.getRequestInfo()).criteria(searchUserCriteria).build();
        URLParams urlParams = URLParams.builder().limit(1).offset(0).build();
        List<OrgUser> users = userRepository.getOrgUsers(orgUserSearchRequest, urlParams);
        if(users == null || users.isEmpty()){
            log.error("This org user id do not exist");
            throw new CustomException("Organization", "This org user id do not exist");
        }

        OrgUser existingOrgUser = users.get(0);
        if (existingOrgUser ==null)
            throw new CustomException("Organization", "This org user id do not exist");

        // If new organizationId is used for update
        if(!Objects.equals(existingOrgUser.getOrganizationId(), request.getOrganizationId())){
            // Check if new organisationId already exist
            OrgSearchCriteria searchCriteria = OrgSearchCriteria.builder().ids(List.of(request.getOrganizationId())).tenantId(orgUser.getTenantId()).build();
            OrgSearchRequest orgSearchRequest = OrgSearchRequest.builder().requestInfo(request.getRequestInfo()).searchCriteria(searchCriteria).build();
            organisations = organisationRepository.getOrganisations(orgSearchRequest);
            if(organisations == null || organisations.isEmpty()){
                log.error("Organization is mandatory in org user request body");
                throw new CustomException("Organization", "Organization ID do not exist");
            }
        }

        // Get existing user with mobile number from hrms service
        List<Employee> employees = hrmsUtils.getUserByPhoneNumber(request, orgUser.getMobileNumber());
        if (employees == null || employees.isEmpty()) {
            //If user doesn't exist
            log.error("This user with this phone number do not exist: {}", orgUser.getMobileNumber());
            throw new CustomException("Organization", "This user with this phone number do not exist: "+orgUser.getMobileNumber());
        }

        // Get employee details fetched from HRMS
        Employee employee = employees.get(0);

        // -----------------------------------------
        // Detect Changes
        // -----------------------------------------
        UserChangeSet changes = detectUserChanges(existingOrgUser.getUser(), orgUser);

        // -----------------------------------------
        // HRMS Update
        // -----------------------------------------
        if (changes.shouldUpdateHRMS()) {
            // Mobile special handling
            if (changes.isMobileChanged()) {
                log.error("phone number is being updated. Old phoneNumber {} with new phoneNumber {}", existingOrgUser.getUser().getMobileNumber(), orgUser.getMobileNumber());
                // If user found, Check if user belong to another organisation record
                List<String> uuids = employees.stream().map(e -> e.getUser().getUuid()).filter(Objects::nonNull).toList();
                OrgUserSearchCriteria searchUserCriteria1 = OrgUserSearchCriteria.builder().userId(uuids).tenantId(orgUser.getTenantId()).build();
                OrgUserSearchRequest orgUserSearchRequest1 = OrgUserSearchRequest.builder().requestInfo(request.getRequestInfo()).criteria(searchUserCriteria1).build();
                URLParams urlParams1 = URLParams.builder().limit(1).offset(0).build();
                List<OrgUser> usersBis = userRepository.getOrgUsers(orgUserSearchRequest1, urlParams1);
                if(usersBis != null && !usersBis.isEmpty()){
                    log.error("This user already belong to another org");
                    throw new CustomException("Organization", "This user already belong to another org");
                }

                // This user not belong to another org

                //Encrypt new mobile number
                String encryptedPocMobileNumber = organisationUtil.encryptMobileNumber(orgUser.getMobileNumber());
                if(encryptedPocMobileNumber!=null && !encryptedPocMobileNumber.isBlank()){
                    orgUser.setMobileNumber(encryptedPocMobileNumber);
                }

                employee.getUser().setMobileNumber(orgUser.getMobileNumber());
            }

            if (changes.isNameChanged()) {
                employee.getUser().setName(orgUser.getName());
            }

            if (changes.isEmailChanged()) {
                employee.getUser().setEmailId(orgUser.getEmailId());
            }

            if (changes.isRolesChanged()) {
                // Roles are updated
                OrgSearchCriteria searchCriteria = OrgSearchCriteria.builder().ids(List.of(request.getOrganizationId())).tenantId(orgUser.getTenantId()).build();
                OrgSearchRequest orgSearchRequest = OrgSearchRequest.builder().requestInfo(request.getRequestInfo()).searchCriteria(searchCriteria).build();
                organisations = organisationRepository.getOrganisations(orgSearchRequest);
                if(organisations == null || organisations.isEmpty()){
                    log.error("Organization ID do not exist");
                    throw new CustomException("Organization", "Organization ID do not exist");
                }
                Organisation organisation = organisations.get(0);
                String orgType = organisation.getOrgType();
                Map<String, List<Role>> rolesMap =  getOrgRoles(request.getRequestInfo());
                if (rolesMap !=null && !rolesMap.isEmpty() && orgType !=null && !orgType.isBlank()){
                    List<Role> roles = rolesMap.get(orgType);
                    List<String> roleCodesMDMS = roles.stream().map(Role::getCode).filter(Objects::nonNull).toList();
                    List<String> requestRoleCodes = orgUser.getRoles().stream().map(Role::getCode).filter(Objects::nonNull).toList();
                    Set<String> orgRolesReqSet = new HashSet<>();
                    orgRolesReqSet.addAll(requestRoleCodes);
                    // Check if Roles from request are valid
                    validateOrgRoles(orgRolesReqSet, roleCodesMDMS);
                }
                employee.getUser().setRoles(orgUser.getRoles());
            }

            if (changes.isJurisdictionChanged()) {
                employee.setJurisdictions(
                        hrmsUtils.buildJurisdictions(orgUser.getJurisdictions())
                );
            }

            EmployeeRequest employeeRequest = EmployeeRequest.builder()
                    .requestInfo(request.getRequestInfo())
                    .employees(List.of(employee))
                    .build();

            List<Employee> updatedEmployees = hrmsUtils.updateHRMSUser(employeeRequest);

            if (updatedEmployees == null || updatedEmployees.isEmpty()) {
                throw new CustomException("HRMS_UPDATE", "Error occurred while updating the user");
            }

            Employee employeeResp = updatedEmployees.get(0);
            request.setUser(employeeResp.getUser());
            request.getUser().setJurisdictions(employeeResp.getJurisdictions());
            request.setUserId(employeeResp.getUser().getUuid());
        } else {
            log.info("No HRMS update required — no user changes detected");
        }

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    /* Validates search FieldPlan request body and parameters*/
    public void validateSearchOrgUsersRequest(OrgUserSearchRequest request, Integer limit, Integer offset, String tenantId) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if search fieldplan request is valid
        validateSearchOrgUsersCriteria(request.getCriteria(), tenantId);
        //Verify MDMS Data
        // TODO: Uncomment and fix as per HCM once we get clarity
        // validateRequestMDMSData(project, tenantId, errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateSearchOrgUsersCriteria(OrgUserSearchCriteria criteria, String tenantId) {
        if (criteria == null) {
            log.error("criteria is mandatory in Org search");
            throw new CustomException("OrgSearch", "criteria is mandatory");
        }
        if (StringUtils.isBlank(criteria.getTenantId())) {
            log.error("Tenant ID is mandatory");
            throw new CustomException("TENANT_ID", "Tenant ID is mandatory");
        }
        if ((criteria.getId()==null || criteria.getId().isEmpty()) && (criteria.getUserId()==null || criteria.getUserId().isEmpty())
                && (criteria.getOrganizationId()==null || criteria.getOrganizationId().isEmpty()))
        {
            log.error("Any one org user search field is required for users Search");
            throw new CustomException("USER_SEARCH_FIELDS", "Any one user search field is required");
        }

        if (!criteria.getTenantId().equals(tenantId)) {
            log.error("Tenant Id must be same in URL param as well as project request body");
            throw new CustomException("MULTIPLE_TENANTS", "Tenant Id must be same in URL param and project request");
        }
    }

    public void validateDeleteOrgUserRequest(DeleteOrgUserRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if org users request and mandatory fields are present
        validateDeleteUserOrgRequest(request);
    }

    private void validateDeleteUserOrgRequest(DeleteOrgUserRequest request) {
        String orgUserId = request.getId();
        if (orgUserId == null || orgUserId.isBlank()) {
            log.error("OrgUserId is mandatory in delete");
            throw new CustomException("Org User", "User is mandatory in delete");
        }

        OrgUserSearchCriteria searchUserCriteria = OrgUserSearchCriteria.builder().id(List.of(request.getId())).tenantId(configuration.getGlobalTenantId()).build();
        OrgUserSearchRequest orgUserSearchRequest = OrgUserSearchRequest.builder().requestInfo(request.getRequestInfo()).criteria(searchUserCriteria).build();
        URLParams urlParams = URLParams.builder().limit(1).offset(0).build();
        List<OrgUser> users = userRepository.getOrgUsers(orgUserSearchRequest, urlParams);
        if(users == null || users.isEmpty()){
            log.error("This org user id do not exist");
            throw new CustomException("Organization", "This org user id do not exist");
        }

        //Check if user has any activity assignments
        List<ActivityAssignment> activityAssignmentList = organisationUtil.getFieldPlanActivityAssignment(request);
        //user has active assignments exist
        if(activityAssignmentList != null && !activityAssignmentList.isEmpty()){
            try {
                OrgUserDeleteErrorResponse errorResponse = OrgUserDeleteErrorResponse.builder()
                        .message("User cannot be deleted because they have active or pending assignments.")
                        .blockingAssignments(activityAssignmentList)
                        .build();
                throw new ResponseStatusException(HttpStatus.CONFLICT, mapper.writeValueAsString((errorResponse))); // Ici on renvoie l'objet comme message JSON
            } catch (Exception e) {
                throw new CustomException("Organization", "User cannot be deleted because they have active or pending assignments.");
            }
        }
    }

    private void validateOrgRoles(Set<String> orgRolesReqSet, List<String> orgRolesCodesMDMS) {
        if (CollectionUtils.isEmpty(orgRolesCodesMDMS)) {
            log.error("Org Roles is not configured in MDMS");
            throw new CustomException("INVALID_ROLES", "Org Roles is not configured in MDMS");
        } else {
            if (!CollectionUtils.isEmpty(orgRolesReqSet)) {
                orgRolesReqSet.removeAll(orgRolesCodesMDMS);
                if (!CollectionUtils.isEmpty(orgRolesReqSet)) {
                    log.error("Invalid role assigned to the employee");
                    throw new CustomException("INVALID_ROLES", "Invalid role assigned to the employee "+orgRolesReqSet);
                }
            }
        }
    }

    public Map<String, List<Role>> getOrgRoles(RequestInfo requestInfo){
        Object mdmsData = mdmsUtil.mDMSCall(requestInfo, configuration.getGlobalTenantId());
        final String jsonPathForOrgRoles = MDMS_RES + MDMS_ORGANIZATION_MODULE_NAME + "." + MASTER_ORG_ROLES + "[*]";
        List<Map<String, Object>> orgRolesRes = null;
        try {
            orgRolesRes = JsonPath.read(mdmsData, jsonPathForOrgRoles);
            List<Role> orgRolesList = orgRolesRes.stream()
                    .map(item -> mapper.convertValue(item, Role.class))
                    .toList();
            Map<String, List<Role>> rolesByOrgType = orgRolesList.stream().collect(Collectors.groupingBy(Role::getOrgType));
            return rolesByOrgType;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new CustomException("JSONPATH_ERROR", "Failed to parse mdms response");
        }
    }

    private UserChangeSet detectUserChanges(User existing, User incoming) {
        UserChangeSet changes = new UserChangeSet();

        changes.setMobileChanged(
                !Objects.equals(existing.getMobileNumber(), incoming.getMobileNumber())
        );

        changes.setNameChanged(
                !Objects.equals(existing.getName(), incoming.getName())
        );

        changes.setEmailChanged(
                !Objects.equals(existing.getEmailId(), incoming.getEmailId())
        );

        changes.setRolesChanged(
                !safeRoleSet(existing.getRoles())
                        .equals(safeRoleSet(incoming.getRoles()))
        );

        changes.setJurisdictionChanged(
                !safeJurisSet(existing.getJurisdictions())
                        .equals(safeJurisSet(incoming.getJurisdictions()))
        );

        return changes;
    }

    private Set<String> safeRoleSet(List<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<String> safeJurisSet(List<Jurisdiction> jurisdictions) {
        if (jurisdictions == null) return Set.of();
        return jurisdictions.stream()
                .map(Jurisdiction::getBoundary)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }


}
