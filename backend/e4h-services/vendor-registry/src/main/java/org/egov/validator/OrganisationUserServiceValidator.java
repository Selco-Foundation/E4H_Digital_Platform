package org.egov.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.repository.OrganisationRepository;
import org.egov.service.OrganisationService;
import org.egov.tracer.model.CustomException;
import org.egov.util.BoundaryUtil;
import org.egov.util.MDMSUtil;
import org.egov.util.OrganisationUtil;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class OrganisationUserServiceValidator {

    private final MDMSUtil mdmsUtil;

    private final OrganisationRepository organisationRepository;

    private final OrganisationUtil organisationUtil;

    private final OrganisationService organisationService;

    private static final String MDMS_RES = "$.MdmsRes.";
    private static final String NOT_PRESENT_IN_MDMS = " is not present in MDMS";
    private static final String VALID_FROM_PARAMETER_SHOULD_BE_LESS_THAN_VALID_TO = "Valid From in search parameters should be less than Valid To";
    private static final String INVALID_ORG_SEARCH_DATE ="INVALID_ORG_SEARCH_DATE";
    @Autowired
    public OrganisationUserServiceValidator(MDMSUtil mdmsUtil, OrganisationRepository organisationRepository,
                                            @Qualifier("objectMapper") ObjectMapper mapper, OrganisationUtil organisationUtil, OrganisationService organisationService) {
        this.mdmsUtil = mdmsUtil;
        this.organisationRepository = organisationRepository;
        this.organisationUtil = organisationUtil;
        this.organisationService = organisationService;
    }

    public void validateCreateOrgUserRequest(OrgUserRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        RequestInfo requestInfo = request.getRequestInfo();

        //Verify if RequestInfo and UserInfo is present
        validateRequestInfo(requestInfo);
        //Verify if ActivityAssignment request and mandatory fields are present
        validateUserOrgRequest(request);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
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

    private void validateUserOrgRequest(OrgUserRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        if (request.getOrgUsers() == null || request.getOrgUsers().size() == 0) {
            log.error("Field Plans list is empty. Field Plans is mandatory");
            throw new CustomException("FIELDPLAN", "Field Plans are mandatory");
        }

        for (OrgUser orgUser : request.getOrgUsers()) {
            if (orgUser == null) {
                log.error("Org User is mandatory in Activities");
                throw new CustomException("Activity", "Activity is mandatory");
            }

            if (orgUser.getUserId() == null) {
                log.error("User ID is mandatory in FieldPlans");
                throw new CustomException("USERID", "User ID is mandatory");
            }
            // Get existing user with userId from hrms service
            Employee employee = organisationUtil.getUserById(request, orgUser.getUserId());
            if (employee == null) {
                log.error("user ID do not exist");
                throw new CustomException("HRMS", "User ID do not exist");
            }

            if (StringUtils.isBlank(orgUser.getTenantId())) {
                log.error("Tenant ID is mandatory in Activity request body");
                errorMap.put("TENANT_ID", "Tenant ID is mandatory");
            }
            if (orgUser.getOrganizationId() == null) {
                log.error("Organization is mandatory in Activity request body");
                errorMap.put("ORGANIZATION", "Organization ID is mandatory");
            }
            OrgSearchCriteria searchCriteria = OrgSearchCriteria.builder().id(List.of(orgUser.getOrganizationId())).tenantId(orgUser.getTenantId()).build();
            OrgSearchRequest orgSearchRequest = OrgSearchRequest.builder().requestInfo(request.getRequestInfo()).searchCriteria(searchCriteria).build();
            List<Organisation> organisations = organisationService.searchOrganisation(orgSearchRequest);
            if(organisations == null || organisations.isEmpty()){
                log.error("Organization is mandatory in Activity request body");
                throw new CustomException("Organization", "Organization ID do not exist");
            }

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
        validateActivityAssignmentSearchRequest(request.getCriteria(), tenantId);
        //Verify MDMS Data
        // TODO: Uncomment and fix as per HCM once we get clarity
        // validateRequestMDMSData(project, tenantId, errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateActivityAssignmentSearchRequest(OrgUserSearchCriteria criteria, String tenantId) {
        if (criteria == null) {
            log.error("fieldPlan is mandatory in FieldPlans");
            throw new CustomException("FIELDPLAN", "FieldPlan is mandatory");
        }
        if (StringUtils.isBlank(criteria.getTenantId())) {
            log.error("Tenant ID is mandatory");
            throw new CustomException("TENANT_ID", "Tenant ID is mandatory");
        }
        if ((criteria.getId()==null || criteria.getId().isEmpty()) && (criteria.getUserId()==null || criteria.getUserId().isEmpty())
                && (criteria.getOrganizationId()==null || criteria.getOrganizationId().isEmpty()))
        {
            log.error("Any one Activity search field is required for FieldPlan Search");
            throw new CustomException("ACTIVITY_SEARCH_FIELDS", "Any one activity search field is required");
        }

        if (!criteria.getTenantId().equals(tenantId)) {
            log.error("Tenant Id must be same in URL param as well as project request body");
            throw new CustomException("MULTIPLE_TENANTS", "Tenant Id must be same in URL param and project request");
        }
    }

}
