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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final PocAdminService pocAdminService;

    @Autowired
    public OrganisationService(OrganisationServiceValidator organisationServiceValidator, OrganisationRepository organisationRepository, OrganisationEnrichmentService organisationEnrichmentService, OrganizationProducer organizationProducer, Configuration configuration, IndividualService individualService, NotificationService notificationService, ObjectMapper mapper,
                               PocAdminService pocAdminService) {
        this.organisationServiceValidator = organisationServiceValidator;
        this.organisationRepository = organisationRepository;
        this.organisationEnrichmentService = organisationEnrichmentService;
        this.organizationProducer = organizationProducer;
        this.configuration = configuration;
        this.individualService = individualService;
        this.notificationService = notificationService;
        this.mapper = mapper;
        this.pocAdminService = pocAdminService;
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

        // Taken before enrichment, which replaces the PoC phone with its encrypted form.
        List<PocAdminService.PocDetails> pocs = orgRequest.getOrganisations().stream()
                .map(PocAdminService.PocDetails::of).toList();
        pocAdminService.checkCreateConflicts(orgRequest.getRequestInfo(), pocs);

        organisationEnrichmentService.enrichCreateOrgRegistryWithoutWorkFlow(orgRequest);
        log.debug("Organisation enrichment completed");

        // Synchronous, and before the organisation is pushed: if HRMS refuses the admin, nothing
        // has been written. The reverse - an admin whose organisation push then fails - is the
        // one residual leak, and the smaller of the two.
        List<PocAdminService.Pending> pendingAdmins =
                pocAdminService.createAdmins(orgRequest.getRequestInfo(), orgRequest.getOrganisations(), pocs);

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

        orgRequest.setPocAdmins(pocAdminService.publish(orgRequest.getRequestInfo(), pendingAdmins));

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

        // Before enrichment, while the incoming PoC phone is still plaintext.
        List<PocAdminService.PocChangeResult> pocChanges = applyPocChanges(orgRequest);

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

        if (!pocChanges.isEmpty()) {
            orgRequest.setPocAdmins(pocChanges.stream()
                    .map(change -> pocAdminService.publishChange(orgRequest.getRequestInfo(), change))
                    .toList());
        }

        log.info("Organisation update process completed successfully for organisation ID: {}", orgId);
        return orgRequest;
    }

    /**
     * Moves the PoC admin for every organisation whose PoC details changed. Compared field by
     * field against the stored organisation, so an update that re-sends the same PoC does nothing.
     */
    private List<PocAdminService.PocChangeResult> applyPocChanges(OrgRequest orgRequest) {
        List<Organisation> incoming = orgRequest.getOrganisations();
        OrgSearchCriteria criteria = OrgSearchCriteria.builder()
                .ids(incoming.stream().map(Organisation::getId).toList())
                .tenantId(incoming.get(0).getTenantId())
                .build();
        List<Organisation> stored = organisationRepository.getOrganisations(
                OrgSearchRequest.builder().requestInfo(orgRequest.getRequestInfo()).searchCriteria(criteria).build());
        Map<String, Organisation> storedById = stored == null ? Map.of() : stored.stream()
                .collect(Collectors.toMap(Organisation::getId, o -> o, (a, b) -> a));

        List<PocAdminService.PocChangeResult> changes = new ArrayList<>();
        for (Organisation organisation : incoming) {
            Organisation existing = storedById.get(organisation.getId());
            if (existing == null || !PocAdminService.pocChanged(organisation, existing)) {
                continue;
            }
            log.info("PoC of organisation {} changed; moving its admin user", organisation.getId());
            changes.add(pocAdminService.applyChange(orgRequest.getRequestInfo(), organisation, existing));
        }
        return changes;
    }

    /**
     * Callers such as the UI send limit and offset as query parameters, while the search reads
     * them from the body's Pagination. A value in the body wins; a query parameter fills a value
     * the body does not set.
     */
    public void applyUrlPagination(OrgSearchRequest orgSearchRequest, Integer limit, Integer offset) {
        if (limit == null && offset == null) {
            return;
        }
        Pagination pagination = orgSearchRequest.getPagination();
        if (pagination == null) {
            pagination = new Pagination();
            pagination.setLimit(null);
            pagination.setOffset(null);
            orgSearchRequest.setPagination(pagination);
        }
        if (pagination.getLimit() == null && limit != null) {
            pagination.setLimit(limit.doubleValue());
        }
        if (pagination.getOffset() == null && offset != null) {
            pagination.setOffset(offset.doubleValue());
        }
        log.debug("Search pagination - limit: {}, offset: {}", pagination.getLimit(), pagination.getOffset());
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
