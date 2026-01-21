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


    public List<OrgUser> createOrgUser(OrgUserRequest request) {
        log.trace("OrganisationUserService::createOrgUser entry");
        log.info("Received request to create organisation users");
        
        validator.validateCreateOrgUserRequest(request);
        log.debug("Organisation user validation completed");
        
        List<OrgUser> orgUserList = request.getOrgUsers();
        log.debug("Processing {} organisation users", orgUserList != null ? orgUserList.size() : 0);
        
        try {
            for (OrgUser orgUser : orgUserList) {
                log.trace("Enriching organisation user: {}", orgUser.getId());
                organisationEnrichmentService.enrichOrgUserRequestOnCreate(orgUser, request.getRequestInfo());
            }
            log.debug("Organisation user enrichment completed");
            
            organizationProducer.push(configuration.getCreateOrgUserTopic(), request);
            log.info("Organisation user creation message pushed to Kafka topic: {}", configuration.getCreateOrgUserTopic());
        } catch (Exception exception) {
            log.error("Error occurred while creating organisation user", exception);
            throw exception;
        }

        log.info("Organisation user creation completed successfully");
        return orgUserList;
    }

    public List<OrgUserEnriched> searchOrganisationUsers(OrgUserSearchRequest request, URLParams urlParams) {
        log.trace("OrganisationUserService::searchOrganisationUsers entry");
        String tenantId = urlParams != null ? urlParams.getTenantId() : "unknown";
        log.info("Starting organisation user search for tenant: {}", tenantId);
        
        validator.validateSearchOrgUsersRequest(request, urlParams.getLimit(), urlParams.getOffset(), urlParams.getTenantId());
        log.debug("Search request validation completed");
        
        List<OrgUser> orgUserList = userRepository.getOrgUsers(request, urlParams);
        log.debug("Retrieved {} organisation users from repository", orgUserList != null ? orgUserList.size() : 0);
        
        List<OrgUserEnriched> orgUserEnricheds = new ArrayList<>();
        for (OrgUser orgUser: orgUserList){
            log.trace("Enriching organisation user with user details: {}", orgUser.getId());
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
        log.info("Organisation user search completed, returning {} enriched users", orgUserEnricheds.size());
        return orgUserEnricheds;
    }
}
