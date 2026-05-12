package org.egov.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.models.core.URLParams;
import org.egov.config.Configuration;
import org.egov.kafka.OrganizationProducer;
import org.egov.repository.OrganisationUserRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.HRMSUtils;
import org.egov.util.UserUtil;
import org.egov.validator.OrganisationUserServiceValidator;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@Slf4j
public class OrganisationUserService {

    private final OrganisationUserServiceValidator validator;

    private final OrganisationUserRepository userRepository;

    private final OrganisationUserEnrichmentService organisationEnrichmentService;

    private final OrganizationProducer organizationProducer;

    private final Configuration configuration;

    private final NotificationService notificationService;

    private final HRMSUtils hrmsUtils;

    private final ObjectMapper mapper;

    private final UserUtil userUtil;

    private static final String EMPLOYEE_ROLE_CODE = "EMPLOYEE";
    private static final String EMPLOYEE_ROLE_NAME = "Employee";
    private static final String DEFAULT_ROLE_TENANT = "in";

    @Autowired
    public OrganisationUserService(OrganisationUserServiceValidator validator, OrganisationUserRepository userRepository, OrganisationUserEnrichmentService organisationEnrichmentService, OrganizationProducer organizationProducer, Configuration configuration, NotificationService notificationService, HRMSUtils hrmsUtils, ObjectMapper mapper, UserUtil userUtil) {
        this.validator = validator;
        this.userRepository = userRepository;
        this.organisationEnrichmentService = organisationEnrichmentService;
        this.organizationProducer = organizationProducer;
        this.configuration = configuration;
        this.notificationService = notificationService;
        this.hrmsUtils = hrmsUtils;
        this.mapper = mapper;
        this.userUtil = userUtil;
    }


    public OrgUserRequest createOrgUser(OrgUserRequest request) {
        log.info("received request to create org user {} ", request );

        ensureDefaultEmployeeRoleForOrgUser(request);

        validator.validateCreateOrgUserRequest(request);

        if (request.getId() != null && !request.getId().isBlank()) {
            log.info("User with same phone already exists in org {}, HRMS employee updated, pushing to update topic",
                    request.getOrganizationId());
            organisationEnrichmentService.enrichOrgUserRequestOnUpdate(request);
            organizationProducer.push(configuration.getUpdateOrgUserTopic(), request);
            return request;
        }

        try {
            log.info("processing  {} valid entities", request.getUser());
            organisationEnrichmentService.enrichOrgUserRequestOnCreate(request, request.getRequestInfo());
            log.info("successfully created org user");
            organizationProducer.push(configuration.getCreateOrgUserTopic(), request);
            log.info("Organisation user creation message pushed to Kafka topic: {}", configuration.getCreateOrgUserTopic());
        } catch (Exception exception) {
            log.error("Error occurred while creating organisation user", exception);
            throw exception;
        }

        return request;
    }

    /**
     * Ensures vendor org users created via /organisation/v1/user/_create get the EMPLOYEE role (and user type)
     * when not already provided, so HRMS / egov-user behave like other internal staff users.
     */
    private void ensureDefaultEmployeeRoleForOrgUser(OrgUserRequest request) {
        User user = request.getUser();
        if (user == null) {
            return;
        }
        if (StringUtils.isBlank(user.getType())) {
            user.setType(EMPLOYEE_ROLE_CODE);
        }
        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }
        boolean hasEmployeeRole = user.getRoles().stream()
                .filter(Objects::nonNull)
                .anyMatch(r -> r.getCode() != null && EMPLOYEE_ROLE_CODE.equalsIgnoreCase(r.getCode()));
        if (!hasEmployeeRole) {
            String roleTenantId = StringUtils.isNotBlank(user.getTenantId())
                    ? userUtil.getStateLevelTenant(user.getTenantId())
                    : DEFAULT_ROLE_TENANT;
            user.getRoles().add(Role.builder()
                    .code(EMPLOYEE_ROLE_CODE)
                    .name(EMPLOYEE_ROLE_NAME)
                    .tenantId(roleTenantId)
                    .build());
        }
    }

    public List<OrgUser> searchOrganisationUsers(OrgUserSearchRequest request, URLParams urlParams) {
        validator.validateSearchOrgUsersRequest(request, urlParams.getLimit(), urlParams.getOffset(), urlParams.getTenantId());
        log.debug("Search request validation completed");

        List<OrgUser> orgUserList = userRepository.getOrgUsers(request, urlParams);
        return orgUserList;
    }

    public Integer countOrganisationUsers(OrgUserSearchRequest request) {
        return userRepository.getOrganisationsCount(request);
    }

    public OrgUserRequest updateOrgUser(OrgUserRequest request) {
        log.info("received request to create org user {} ", request );

        validator.validateUpdateOrgUserRequest(request);
//        List<OrgUser> orgUserList = request.getOrgUsers();
        try {
            log.info("processing  {} valid entities", request.getUser());
            organisationEnrichmentService.enrichOrgUserRequestOnUpdate(request);
            log.info("successfully created org user");
            organizationProducer.push(configuration.getUpdateOrgUserTopic(), request);
        } catch (Exception exception) {
            log.error("error occurred while creating project facility: {}", ExceptionUtils.getStackTrace(exception));
        }

        return request;
    }

    public DeleteOrgUserRequest deleteUserOrg(DeleteOrgUserRequest request) {
        log.info("received request to delete bulk activity facility staff");
        validator.validateDeleteOrgUserRequest(request);
        try {
            deactivateHrmsUser(request);
            request.setIsDeleted(true);
            organisationEnrichmentService.enrichOrgUserRequestOnDelete(request);
            organizationProducer.push(configuration.getDeleteOrgUserTopic(), request);
            log.info("successfully deleted org user");
        } catch (Exception exception) {
            log.error("error occurred while deleting org user", ExceptionUtils.getStackTrace(exception));
            throw exception;
        }

        return request;
    }

    private void deactivateHrmsUser(DeleteOrgUserRequest request) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new CustomException("HRMS_DEACTIVATION", "Cannot deactivate HRMS user: userId is missing");
        }

        Employee employee = hrmsUtils.getUserById(request, request.getUserId());
        if (employee == null || employee.getUser() == null) {
            throw new CustomException("HRMS_DEACTIVATION", "Cannot deactivate HRMS user: user not found");
        }

        employee.getUser().setActive(false);
        employee.setIsActive(false);
        employee.setEmployeeStatus("INACTIVE");
        employee.setReActivateEmployee(false);

        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .requestInfo(request.getRequestInfo())
                .employees(List.of(employee))
                .build();

        List<Employee> updatedEmployees = hrmsUtils.updateHRMSUser(employeeRequest);
        if (updatedEmployees == null || updatedEmployees.isEmpty()) {
            throw new CustomException("HRMS_DEACTIVATION", "Failed to deactivate HRMS user");
        }

        request.setUser(updatedEmployees.get(0).getUser());
    }
}
