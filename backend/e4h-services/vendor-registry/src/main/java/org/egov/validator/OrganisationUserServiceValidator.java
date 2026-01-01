package org.egov.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.core.URLParams;
import org.egov.config.Configuration;
import org.egov.repository.OrganisationRepository;
import org.egov.repository.OrganisationUserRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.MDMSUtil;
import org.egov.util.OrganisationUtil;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.util.OrganisationConstant.*;

@Component
@Slf4j
public class OrganisationUserServiceValidator {

    private final MDMSUtil mdmsUtil;

    private final Configuration configuration;

    private final OrganisationRepository organisationRepository;

    private final OrganisationUtil organisationUtil;

    private final OrganisationUserRepository userRepository;

    private final ObjectMapper mapper;

    private static final String MDMS_RES = "$.MdmsRes.";
    private static final String NOT_PRESENT_IN_MDMS = " is not present in MDMS";
    private static final String VALID_FROM_PARAMETER_SHOULD_BE_LESS_THAN_VALID_TO = "Valid From in search parameters should be less than Valid To";
    private static final String INVALID_ORG_SEARCH_DATE ="INVALID_ORG_SEARCH_DATE";
    @Autowired
    public OrganisationUserServiceValidator(MDMSUtil mdmsUtil, Configuration configuration, OrganisationRepository organisationRepository,
                                            OrganisationUtil organisationUtil, OrganisationUserRepository userRepository, ObjectMapper mapper) {
        this.mdmsUtil = mdmsUtil;
        this.configuration = configuration;
        this.organisationRepository = organisationRepository;
        this.organisationUtil = organisationUtil;
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
        List<Employee> employee = organisationUtil.getUserByPhoneNumber(request, orgUser.getMobileNumber());
        if (employee == null || employee.isEmpty()) { //If user doesn't exist
            Organisation organisation = organisations.get(0);
            String orgType = organisation.getOrgType();
            Map<String, List<Role>> rolesMap =  getOrgRoles(request.getRequestInfo());
            if (rolesMap !=null && !rolesMap.isEmpty() && orgType !=null && !orgType.isBlank()){
                List<Role> roles = rolesMap.get(orgType);
                //Encrypt poc mobile number
                String encryptedPocMobileNumber = organisationUtil.encryptMobileNumber(orgUser.getMobileNumber());
                if(encryptedPocMobileNumber!=null && !encryptedPocMobileNumber.isBlank()){
                    orgUser.setMobileNumber(encryptedPocMobileNumber);
                }
                // Call HRMS service to create user

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
                request.getUser().setUuid(employee.get(0).getUser().getUuid());
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

    public void validateDeleteOrgUserRequest(OrgUserRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if org users request and mandatory fields are present
        validateDeleteUserOrgRequest(request);
    }

    private void validateDeleteUserOrgRequest(OrgUserRequest request) {
        String orgUserId = request.getId();
        if (orgUserId == null || orgUserId.isBlank()) {
            log.error("OrgUserId is mandatory in delete");
            throw new CustomException("Org User", "User is mandatory in delete");
        }

        OrgUserSearchCriteria searchUserCriteria = OrgUserSearchCriteria.builder().id(List.of()).tenantId(configuration.getGlobalTenantId()).build();
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

}
