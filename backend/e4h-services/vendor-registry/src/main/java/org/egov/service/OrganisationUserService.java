package org.egov.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.models.core.URLParams;
import org.egov.config.Configuration;
import org.egov.kafka.OrganizationProducer;
import org.egov.repository.OrganisationUserRepository;
import org.egov.util.OrganisationUtil;
import org.egov.validator.OrganisationUserServiceValidator;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class OrganisationUserService {

    private final OrganisationUserServiceValidator validator;

    private final OrganisationUserRepository userRepository;

    private final OrganisationUserEnrichmentService organisationEnrichmentService;

    private final OrganizationProducer organizationProducer;

    private final Configuration configuration;


    private final OrganisationUtil organisationUtil;

    private final NotificationService notificationService;

    private final ObjectMapper mapper;

    @Autowired
    public OrganisationUserService(OrganisationUserServiceValidator validator, OrganisationUserRepository userRepository, OrganisationUserEnrichmentService organisationEnrichmentService, OrganizationProducer organizationProducer, Configuration configuration, OrganisationUtil organisationUtil, NotificationService notificationService, ObjectMapper mapper) {
        this.validator = validator;
        this.userRepository = userRepository;
        this.organisationEnrichmentService = organisationEnrichmentService;
        this.organizationProducer = organizationProducer;
        this.configuration = configuration;
        this.organisationUtil = organisationUtil;
        this.notificationService = notificationService;
        this.mapper = mapper;
    }


    public OrgUserRequest createOrgUser(OrgUserRequest request) {
        log.info("received request to create org user {} ", request );

        validator.validateCreateOrgUserRequest(request);
//        List<OrgUser> orgUserList = request.getOrgUsers();
        try {
            log.info("processing  {} valid entities", request.getUser());
            organisationEnrichmentService.enrichOrgUserRequestOnCreate(request, request.getRequestInfo());
            log.info("successfully created org user");
            organizationProducer.push(configuration.getCreateOrgUserTopic(), request);
        } catch (Exception exception) {
            log.error("error occurred while creating project facility: {}", ExceptionUtils.getStackTrace(exception));
        }

        return request;
    }

    public List<OrgUserEnriched> searchOrganisationUsers(OrgUserSearchRequest request, URLParams urlParams) {
        validator.validateSearchOrgUsersRequest(request, urlParams.getLimit(), urlParams.getOffset(), urlParams.getTenantId());
        List<OrgUser> orgUserList = userRepository.getOrgUsers(request, urlParams);
        List<OrgUserEnriched> orgUserEnricheds = new ArrayList<>();
        for (OrgUser orgUser: orgUserList){
            Employee employee = organisationUtil.getUserById(request, orgUser.getUserId());
            OrgUserEnriched enriched = OrgUserEnriched.builder()
                    .user(employee.getUser())
                    .userId(orgUser.getUserId())
                    .tenantId(orgUser.getTenantId())
                    .organizationId(orgUser.getOrganizationId())
                    .id(orgUser.getId())
                    .auditDetails(orgUser.getAuditDetails())
                    .additionalDetails(orgUser.getAdditionalDetails())
                    .isDeleted(orgUser.getIsDeleted())
                    .build();
            orgUserEnricheds.add(enriched);
        }
        return orgUserEnricheds;
    }

    public OrgUserRequest deleteUserOrg(OrgUserRequest request) {
        log.info("received request to delete bulk activity facility staff");
        validator.validateDeleteOrgUserRequest(request);
        try {
            request.setIsDeleted(true);
            organisationEnrichmentService.enrichOrgUserRequestOnUpdate(request);
            organizationProducer.push(configuration.getDeleteOrgUserTopic(), request);
            log.info("successfully deleted org user");
        } catch (Exception exception) {
            log.error("error occurred while deleting org user", ExceptionUtils.getStackTrace(exception));
        }

        return request;
    }
}
