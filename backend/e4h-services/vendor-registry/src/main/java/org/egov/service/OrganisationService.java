package org.egov.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.kafka.OrganizationProducer;
import org.egov.repository.OrganisationRepository;
import org.egov.config.Configuration;
import org.egov.tracer.model.CustomException;
import org.egov.validator.OrganisationServiceValidator;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.egov.util.OrganisationConstant.ORGANISATION_ENCRYPT_KEY;


@Service
@Slf4j
public class OrganisationService {

    private final OrganisationServiceValidator organisationServiceValidator;

    private final OrganisationRepository organisationRepository;

    private final OrganisationEnrichmentService organisationEnrichmentService;

    private final OrganizationProducer organizationProducer;

    private final Configuration configuration;


    private final IndividualService individualService;

    private final NotificationService notificationService;

    private final ObjectMapper mapper;

    @Autowired
    public OrganisationService(OrganisationServiceValidator organisationServiceValidator, OrganisationRepository organisationRepository, OrganisationEnrichmentService organisationEnrichmentService, OrganizationProducer organizationProducer, Configuration configuration, IndividualService individualService, NotificationService notificationService, ObjectMapper mapper) {
        this.organisationServiceValidator = organisationServiceValidator;
        this.organisationRepository = organisationRepository;
        this.organisationEnrichmentService = organisationEnrichmentService;
        this.organizationProducer = organizationProducer;
        this.configuration = configuration;
        this.individualService = individualService;
        this.notificationService = notificationService;
        this.mapper = mapper;
    }


    /**
     *
     * @param orgRequest
     * @return
     */
    public OrgRequest createOrganisationWithoutWorkFlow(OrgRequest orgRequest) {
        log.trace("OrganisationService::createOrganisationWithoutWorkFlow entry");
        String tenantId = orgRequest.getOrganisations() != null && !orgRequest.getOrganisations().isEmpty()
                ? orgRequest.getOrganisations().get(0).getTenantId() : "unknown";
        log.info("Starting organisation creation process for tenant: {}", tenantId);

        organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest);
        log.debug("Organisation validation completed");

        organisationEnrichmentService.enrichCreateOrgRegistryWithoutWorkFlow(orgRequest);
        log.debug("Organisation enrichment completed");

        OrgRequest clone;
        try {
            clone = mapper.readValue(mapper.writeValueAsString(orgRequest), OrgRequest.class);
        }catch (Exception e) {
            log.error("Error while cloning organisation request", e);
            throw new CustomException("CLONING_ERROR", "Error while cloning");
        }
//        encryptionService.encryptDetails(clone,ORGANISATION_ENCRYPT_KEY);

        organizationProducer.push(configuration.getOrgKafkaCreateTopic(), clone);
        log.info("Organisation creation message pushed to Kafka topic: {}", configuration.getOrgKafkaCreateTopic());

        try {
            notificationService.sendNotification(orgRequest, true);
            log.debug("Notification sent successfully");
        }catch (Exception e){
            log.warn("Failed to send notification for organisation creation, continuing without notification", e);
        }

        log.info("Organisation creation process completed successfully for tenant: {}", tenantId);
        return orgRequest;
    }

    /**
     *
     * @param orgRequest
     * @return
     */
    public OrgRequest updateOrganisationWithoutWorkFlow(OrgRequest orgRequest) {
        log.trace("OrganisationService::updateOrganisationWithoutWorkFlow entry");
        String tenantId = orgRequest.getOrganisations() != null && !orgRequest.getOrganisations().isEmpty()
                ? orgRequest.getOrganisations().get(0).getTenantId() : "unknown";
        String orgId = orgRequest.getOrganisations() != null && !orgRequest.getOrganisations().isEmpty()
                ? orgRequest.getOrganisations().get(0).getId() : "unknown";
        log.info("Starting organisation update process for organisation ID: {}, tenant: {}", orgId, tenantId);

        organisationServiceValidator.validateUpdateOrgRegistryWithoutWorkFlow(orgRequest);
        log.debug("Organisation validation completed");

        organisationEnrichmentService.enrichUpdateOrgRegistryWithoutWorkFlow(orgRequest);
        log.debug("Organisation enrichment completed");

        OrgRequest clone;
        try {
            clone = mapper.readValue(mapper.writeValueAsString(orgRequest), OrgRequest.class);
        }catch (Exception e) {
            log.error("Error while cloning organisation request", e);
            throw new CustomException("CLONING_ERROR", "Error while cloning");
        }

        try {
            notificationService.sendNotification(orgRequest,false);
            log.debug("Notification sent successfully");
        }catch (Exception e){
            log.warn("Failed to send notification for organisation update, continuing without notification", e);
        }
//        encryptionService.encryptDetails(clone,ORGANISATION_ENCRYPT_KEY);
        organizationProducer.push(configuration.getOrgKafkaUpdateTopic(), clone);
        log.info("Organisation update message pushed to Kafka topic: {}", configuration.getOrgKafkaUpdateTopic());

        log.info("Organisation update process completed successfully for organisation ID: {}", orgId);
        return orgRequest;
    }

    /**
     *
     * @param orgSearchRequest
     * @return
     */
    public List<Organisation> searchOrganisation(OrgSearchRequest orgSearchRequest) {
        log.trace("OrganisationService::searchOrganisation entry");
        String tenantId = orgSearchRequest.getSearchCriteria() != null
                ? orgSearchRequest.getSearchCriteria().getTenantId() : "unknown";
        log.info("Starting organisation search for tenant: {}", tenantId);

        organisationServiceValidator.validateSearchOrganisationRequest(orgSearchRequest);
        log.debug("Search criteria validation completed");

        List<Organisation> organisations = organisationRepository.getOrganisations(orgSearchRequest);
        log.info("Organisation search completed, found {} organisations", organisations != null ? organisations.size() : 0);
        return organisations;
    }

    /**
     *
     * @param orgSearchRequest
     * @return
     */
    public Integer countAllOrganisations(OrgSearchRequest orgSearchRequest) {
        log.trace("OrganisationService::countAllOrganisations entry");
        String tenantId = orgSearchRequest.getSearchCriteria() != null
                ? orgSearchRequest.getSearchCriteria().getTenantId() : "unknown";
        log.debug("Counting organisations for tenant: {}", tenantId);

        Integer count = organisationRepository.getOrganisationsCount(orgSearchRequest);
        log.debug("Organisation count: {}", count);
        return count;
    }
}
