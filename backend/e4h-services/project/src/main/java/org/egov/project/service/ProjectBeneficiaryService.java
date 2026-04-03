package org.egov.project.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.ds.Tuple;
import org.egov.common.models.ErrorDetails;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.models.project.BeneficiaryBulkRequest;
import org.egov.common.models.project.BeneficiaryRequest;
import org.egov.common.models.project.BeneficiarySearchRequest;
import org.egov.common.models.project.ProjectBeneficiary;
import org.egov.common.service.IdGenService;
import org.egov.common.utils.CommonUtils;
import org.egov.common.validator.Validator;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.repository.ProjectBeneficiaryRepository;
import org.egov.project.service.enrichment.ProjectBeneficiaryEnrichmentService;
import org.egov.project.validator.beneficiary.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.project.Constants.SET_PROJECT_BENEFICIARIES;
import static org.egov.project.Constants.VALIDATION_ERROR;

@Service
@Slf4j
public class ProjectBeneficiaryService {

    public static final String CREATING_BULK_REQUEST = "creating bulk request";
    public static final String PROCESSING_VALID_ENTITIES = "processing {} valid entities";

    private final ProjectBeneficiaryRepository projectBeneficiaryRepository;

    private final ProjectConfiguration projectConfiguration;

    private final ProjectBeneficiaryEnrichmentService projectBeneficiaryEnrichmentService;

    private final List<Validator<BeneficiaryBulkRequest, ProjectBeneficiary>> validators;

    private final Predicate<Validator<BeneficiaryBulkRequest, ProjectBeneficiary>> isApplicableForUpdate = validator ->
            validator.getClass().equals(PbNullIdValidator.class)
                    || validator.getClass().equals(PbNonExistentEntityValidator.class)
                    || validator.getClass().equals(PbUniqueTagsValidator.class)
                    || validator.getClass().equals(PbVoucherTagUniqueForUpdateValidator.class)
                    || validator.getClass().equals(PbIsDeletedValidator.class)
                    || validator.getClass().equals(PbProjectIdValidator.class)
                    || validator.getClass().equals(BeneficiaryValidator.class)
                    || validator.getClass().equals(PbRowVersionValidator.class)
                    || validator.getClass().equals(PbUniqueEntityValidator.class);

    private final Predicate<Validator<BeneficiaryBulkRequest, ProjectBeneficiary>> isApplicableForCreate = validator ->
            validator.getClass().equals(PbProjectIdValidator.class)
                    || validator.getClass().equals(PbExistentEntityValidator.class)
                    || validator.getClass().equals(BeneficiaryValidator.class)
                    || validator.getClass().equals(PbUniqueTagsValidator.class)
                    || validator.getClass().equals(PbVoucherTagUniqueForCreateValidator.class);

    private final Predicate<Validator<BeneficiaryBulkRequest, ProjectBeneficiary>> isApplicableForDelete = validator ->
            validator.getClass().equals(PbNullIdValidator.class)
                    || validator.getClass().equals(PbNonExistentEntityValidator.class);

    @Autowired
    public ProjectBeneficiaryService(
            ProjectBeneficiaryRepository projectBeneficiaryRepository,
            ProjectConfiguration projectConfiguration,
            List<Validator<BeneficiaryBulkRequest, ProjectBeneficiary>> validators,
            ProjectBeneficiaryEnrichmentService projectBeneficiaryEnrichmentService
    ) {
        this.projectBeneficiaryRepository = projectBeneficiaryRepository;
        this.projectConfiguration = projectConfiguration;
        this.validators = validators;
        this.projectBeneficiaryEnrichmentService = projectBeneficiaryEnrichmentService;
    }

    public List<ProjectBeneficiary> create(BeneficiaryRequest request) {
        log.trace("Entering create (single beneficiary)");
        log.info("Received request to create project beneficiary");
        BeneficiaryBulkRequest bulkRequest = BeneficiaryBulkRequest.builder().requestInfo(request.getRequestInfo())
                .projectBeneficiaries(Collections.singletonList(request.getProjectBeneficiary())).build();
        log.debug(CREATING_BULK_REQUEST);
        List<ProjectBeneficiary> result = create(bulkRequest, false);
        log.trace("Exiting create (single beneficiary)");
        return result;
    }

    public List<ProjectBeneficiary> create(BeneficiaryBulkRequest beneficiaryRequest, boolean isBulk) {
        log.trace("Entering create (bulk beneficiaries)");
        log.info("Received request to create bulk project beneficiaries");
        Tuple<List<ProjectBeneficiary>, Map<ProjectBeneficiary, ErrorDetails>> tuple = validate(validators,
                isApplicableForCreate, beneficiaryRequest, isBulk);
        Map<ProjectBeneficiary, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectBeneficiary> validProjectBeneficiaries = tuple.getX();
        log.debug("Validation completed - {} valid beneficiaries, {} errors", validProjectBeneficiaries.size(), errorDetailsMap.size());

        try {
            if (!validProjectBeneficiaries.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validProjectBeneficiaries.size());
                log.debug("Enriching beneficiaries before save");
                projectBeneficiaryEnrichmentService.create(validProjectBeneficiaries, beneficiaryRequest);
                log.debug("Saving beneficiaries to repository");
                projectBeneficiaryRepository.save(validProjectBeneficiaries,
                        projectConfiguration.getCreateProjectBeneficiaryTopic());
                log.info("Successfully created {} project beneficiaries", validProjectBeneficiaries.size());
            } else {
                log.warn("No valid beneficiaries to create after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while creating project beneficiaries", exception);
            populateErrorDetails(beneficiaryRequest, errorDetailsMap, validProjectBeneficiaries,
                    exception, SET_PROJECT_BENEFICIARIES);
        }
        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting create (bulk beneficiaries)");
        return validProjectBeneficiaries;
    }

    public List<ProjectBeneficiary> update(BeneficiaryRequest request) {
        log.trace("Entering update (single beneficiary)");
        log.info("Received request to update project beneficiary");
        BeneficiaryBulkRequest bulkRequest = BeneficiaryBulkRequest.builder().requestInfo(request.getRequestInfo())
                .projectBeneficiaries(Collections.singletonList(request.getProjectBeneficiary())).build();
        log.debug(CREATING_BULK_REQUEST);
        List<ProjectBeneficiary> result = update(bulkRequest, false);
        log.trace("Exiting update (single beneficiary)");
        return result;
    }

    public List<ProjectBeneficiary> update(BeneficiaryBulkRequest beneficiaryRequest, boolean isBulk) {
        log.trace("Entering update (bulk beneficiaries)");
        log.info("Received request to update bulk project beneficiaries");
        Tuple<List<ProjectBeneficiary>, Map<ProjectBeneficiary, ErrorDetails>> tuple = validate(validators,
                isApplicableForUpdate, beneficiaryRequest, isBulk);
        Map<ProjectBeneficiary, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectBeneficiary> validProjectBeneficiaries = tuple.getX();
        log.debug("Validation completed - {} valid beneficiaries, {} errors", validProjectBeneficiaries.size(), errorDetailsMap.size());

        try {
            if (!validProjectBeneficiaries.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validProjectBeneficiaries.size());
                log.debug("Enriching beneficiaries before update");
                projectBeneficiaryEnrichmentService.update(validProjectBeneficiaries, beneficiaryRequest);
                log.debug("Saving updated beneficiaries to repository");
                projectBeneficiaryRepository.save(validProjectBeneficiaries,
                        projectConfiguration.getUpdateProjectBeneficiaryTopic());
                log.info("Successfully updated {} project beneficiaries", validProjectBeneficiaries.size());
            } else {
                log.warn("No valid beneficiaries to update after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while updating project beneficiaries", exception);
            populateErrorDetails(beneficiaryRequest, errorDetailsMap, validProjectBeneficiaries,
                    exception, SET_PROJECT_BENEFICIARIES);
        }
        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting update (bulk beneficiaries)");
        return validProjectBeneficiaries;
    }

    public SearchResponse<ProjectBeneficiary> search(BeneficiarySearchRequest beneficiarySearchRequest,
                                                     Integer limit,
                                                     Integer offset,
                                                     String tenantId,
                                                     Long lastChangedSince,
                                                     Boolean includeDeleted) throws Exception {
        log.trace("Entering search");
        log.info("Received request to search project beneficiaries");
        String idFieldName = getIdFieldName(beneficiarySearchRequest.getProjectBeneficiary());
        if (isSearchByIdOnly(beneficiarySearchRequest.getProjectBeneficiary(), idFieldName)) {
            log.info("Searching project beneficiaries by ID");
            List<String> ids = (List<String>) ReflectionUtils.invokeMethod(getIdMethod(Collections
                            .singletonList(beneficiarySearchRequest.getProjectBeneficiary())),
                    beneficiarySearchRequest.getProjectBeneficiary());
            log.debug("Fetching project beneficiaries with {} IDs", ids != null ? ids.size() : 0);

            SearchResponse<ProjectBeneficiary> searchResponse = projectBeneficiaryRepository.findById(ids, idFieldName, includeDeleted);
            log.debug("Found {} beneficiaries before filtering", searchResponse.getResponse() != null ? searchResponse.getResponse().size() : 0);

            List<ProjectBeneficiary> projectBeneficiaries = searchResponse.getResponse().stream()
                    .filter(lastChangedSince(lastChangedSince))
                    .filter(havingTenantId(tenantId))
                    .filter(includeDeleted(includeDeleted))
                    .toList();
            searchResponse.setResponse(projectBeneficiaries);
            log.info("Search by ID completed - found {} beneficiaries", projectBeneficiaries.size());
            log.trace("Exiting search");
            return searchResponse;
        }
        log.info("Searching project beneficiaries using criteria");
        log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}", limit, offset, tenantId);
        SearchResponse<ProjectBeneficiary> result = projectBeneficiaryRepository.find(beneficiarySearchRequest.getProjectBeneficiary(),
                limit, offset, tenantId, lastChangedSince, includeDeleted);
        log.info("Search by criteria completed - found {} beneficiaries", result.getResponse() != null ? result.getResponse().size() : 0);
        log.trace("Exiting search");
        return result;
    }

    public List<ProjectBeneficiary> delete(BeneficiaryRequest beneficiaryRequest) {
        log.trace("Entering delete (single beneficiary)");
        log.info("Received request to delete a project beneficiary");
        BeneficiaryBulkRequest bulkRequest = BeneficiaryBulkRequest.builder().requestInfo(beneficiaryRequest.getRequestInfo())
                .projectBeneficiaries(Collections.singletonList(beneficiaryRequest.getProjectBeneficiary())).build();
        log.debug(CREATING_BULK_REQUEST);
        List<ProjectBeneficiary> result = delete(bulkRequest, false);
        log.trace("Exiting delete (single beneficiary)");
        return result;
    }

    public List<ProjectBeneficiary> delete(BeneficiaryBulkRequest beneficiaryRequest, boolean isBulk) {
        log.trace("Entering delete (bulk beneficiaries)");
        log.info("Received request to delete bulk project beneficiaries");
        Tuple<List<ProjectBeneficiary>, Map<ProjectBeneficiary, ErrorDetails>> tuple = validate(validators,
                isApplicableForDelete, beneficiaryRequest, isBulk);
        Map<ProjectBeneficiary, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectBeneficiary> validProjectBeneficiaries = tuple.getX();
        log.debug("Validation completed - {} valid beneficiaries, {} errors", validProjectBeneficiaries.size(), errorDetailsMap.size());

        try {
            if (!validProjectBeneficiaries.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validProjectBeneficiaries.size());
                log.debug("Enriching beneficiaries before delete");
                projectBeneficiaryEnrichmentService.delete(validProjectBeneficiaries, beneficiaryRequest);
                log.debug("Saving deleted beneficiaries to repository");
                projectBeneficiaryRepository.save(validProjectBeneficiaries,
                        projectConfiguration.getDeleteProjectBeneficiaryTopic());
                log.info("Successfully deleted {} project beneficiaries", validProjectBeneficiaries.size());
            } else {
                log.warn("No valid beneficiaries to delete after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while deleting project beneficiaries", exception);
            populateErrorDetails(beneficiaryRequest, errorDetailsMap, validProjectBeneficiaries,
                    exception, SET_PROJECT_BENEFICIARIES);
        }
        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting delete (bulk beneficiaries)");
        return validProjectBeneficiaries;
    }

    public void putInCache(List<ProjectBeneficiary> projectBeneficiaries) {
        log.trace("Entering putInCache for {} beneficiaries", projectBeneficiaries != null ? projectBeneficiaries.size() : 0);
        log.info("Putting {} project beneficiaries in cache", projectBeneficiaries != null ? projectBeneficiaries.size() : 0);
        projectBeneficiaryRepository.putInCache(projectBeneficiaries);
        log.info("Successfully put project beneficiaries in cache");
        log.trace("Exiting putInCache");
    }

    private Tuple<List<ProjectBeneficiary>, Map<ProjectBeneficiary, ErrorDetails>> validate(List<Validator<BeneficiaryBulkRequest,
                                                                                                    ProjectBeneficiary>> validators,
                                                                                            Predicate<Validator<BeneficiaryBulkRequest,
                                                                                                    ProjectBeneficiary>> isApplicable, BeneficiaryBulkRequest request, boolean isBulk) {
        log.trace("Entering validate for {} beneficiaries", request.getProjectBeneficiaries() != null ? request.getProjectBeneficiaries().size() : 0);
        log.debug("Validating request with {} validators", validators.size());
        Map<ProjectBeneficiary, ErrorDetails> errorDetailsMap = new HashMap<>();
        if (!errorDetailsMap.isEmpty() && !isBulk) {
            log.error("Validation error occurred. Error details: {}", errorDetailsMap.values());
            throw new CustomException(VALIDATION_ERROR, errorDetailsMap.values().toString());
        }
        List<ProjectBeneficiary> validProjectBeneficiaries = request.getProjectBeneficiaries().stream()
                .filter(notHavingErrors()).toList();
        log.debug("Validation completed - {} valid beneficiaries out of {}", validProjectBeneficiaries.size(), request.getProjectBeneficiaries().size());
        log.trace("Exiting validate");
        return new Tuple<>(validProjectBeneficiaries, errorDetailsMap);
    }
}
