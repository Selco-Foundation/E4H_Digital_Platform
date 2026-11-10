package org.egov.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.models.core.URLParams;
import org.egov.config.Configuration;
import org.egov.kafka.OrganizationProducer;
import org.egov.repository.OrganisationUserRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.HRMSUtils;
import org.egov.util.JurisdictionScopeResolver;
import org.egov.validator.OrganisationUserServiceValidator;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


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

    private final JurisdictionScopeResolver jurisdictionScopeResolver;

    @Autowired
    public OrganisationUserService(OrganisationUserServiceValidator validator, OrganisationUserRepository userRepository, OrganisationUserEnrichmentService organisationEnrichmentService, OrganizationProducer organizationProducer, Configuration configuration, NotificationService notificationService, HRMSUtils hrmsUtils, ObjectMapper mapper, JurisdictionScopeResolver jurisdictionScopeResolver) {
        this.validator = validator;
        this.userRepository = userRepository;
        this.organisationEnrichmentService = organisationEnrichmentService;
        this.organizationProducer = organizationProducer;
        this.configuration = configuration;
        this.notificationService = notificationService;
        this.hrmsUtils = hrmsUtils;
        this.mapper = mapper;
        this.jurisdictionScopeResolver = jurisdictionScopeResolver;
    }


    public OrgUserRequest createOrgUser(OrgUserRequest request) {
        log.info("received request to create org user {} ", request );

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

    public List<OrgUser> searchOrganisationUsers(OrgUserSearchRequest request, URLParams urlParams) {
        validator.validateSearchOrgUsersRequest(request, urlParams.getLimit(), urlParams.getOffset(), urlParams.getTenantId());
        log.debug("Search request validation completed");

        JurisdictionScopeResolver.Scope scope =
                jurisdictionScopeResolver.resolve(request.getRequestInfo(), urlParams.getTenantId());

        if (scope.unscoped()) {
            return userRepository.getOrgUsers(request, urlParams);
        }

        Map<String, Employee> inScope = resolveUsersInScope(request, urlParams, scope);
        if (inScope.isEmpty()) {
            return Collections.emptyList();
        }
        narrowToUsers(request, inScope.keySet());

        return userRepository.getOrgUsers(request, urlParams, inScope, scope);
    }

    public Integer countOrganisationUsers(OrgUserSearchRequest request) {
        URLParams urlParams = URLParams.builder().build();
        JurisdictionScopeResolver.Scope scope =
                jurisdictionScopeResolver.resolve(request.getRequestInfo(), request.getCriteria().getTenantId());

        if (scope.unscoped()) {
            return userRepository.getOrganisationsCount(request);
        }

        Map<String, Employee> inScope = resolveUsersInScope(request, urlParams, scope);
        if (inScope.isEmpty()) {
            return 0;
        }
        narrowToUsers(request, inScope.keySet());

        return userRepository.getOrganisationsCount(request);
    }

    /**
     * The users this search matches that the caller may actually administer, keyed by uuid.
     *
     * Asked of HRMS in one call for the whole matching set rather than per row, because the
     * answer has to be known *before* paging: filtering afterwards would return short pages
     * beneath a total that counted users the caller cannot see.
     */
    private Map<String, Employee> resolveUsersInScope(OrgUserSearchRequest request, URLParams urlParams,
                                                      JurisdictionScopeResolver.Scope scope) {
        List<String> candidateIds = userRepository.getOrgUserIds(request, urlParams);
        if (candidateIds.isEmpty() || scope.codes().isEmpty()) {
            return Collections.emptyMap();
        }

        /*
         * Fetch the candidates, then decide which of them the caller administers.
         *
         * The boundary filter is deliberately NOT pushed into the HRMS query. Doing that meant
         * listing every descendant of the caller's jurisdictions, and a state-level administrator
         * expands to thousands of facility codes - a request line far past what the servlet will
         * accept, rejected before HRMS ever sees it. Here the only boundaries resolved are the
         * ones these users actually hold, which is a small set however senior the caller is.
         */
        List<Employee> employees = hrmsUtils.getUsersByIds(request, candidateIds);
        String tenantId = request.getCriteria() != null ? request.getCriteria().getTenantId() : null;

        return employees.stream()
                .filter(employee -> employee != null && employee.getUser() != null
                        && employee.getUser().getUuid() != null)
                .filter(employee -> !jurisdictionScopeResolver.within(scope, employee.getJurisdictions(),
                        request.getRequestInfo(), tenantId).isEmpty())
                .collect(Collectors.toMap(employee -> employee.getUser().getUuid(),
                        Function.identity(), (first, second) -> first, LinkedHashMap::new));
    }

    /** Intersects any caller-supplied userIds filter with the set they are allowed to see. */
    private void narrowToUsers(OrgUserSearchRequest request, java.util.Set<String> allowed) {
        List<String> requested = request.getCriteria().getUserId();
        List<String> narrowed = (requested == null || requested.isEmpty())
                ? List.copyOf(allowed)
                : requested.stream().filter(allowed::contains).toList();
        request.getCriteria().setUserId(narrowed);
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
