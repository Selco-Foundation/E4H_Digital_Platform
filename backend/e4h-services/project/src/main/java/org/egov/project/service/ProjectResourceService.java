package org.egov.project.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.data.query.exception.QueryBuilderException;
import org.egov.common.ds.Tuple;
import org.egov.common.models.ErrorDetails;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.models.project.ProjectResource;
import org.egov.common.models.project.ProjectResourceBulkRequest;
import org.egov.common.models.project.ProjectResourceRequest;
import org.egov.common.models.project.ProjectResourceSearchRequest;
import org.egov.common.validator.Validator;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.repository.ProjectResourceRepository;
import org.egov.project.service.enrichment.ProjectResourceEnrichmentService;
import org.egov.project.validator.resource.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.project.Constants.*;

@Service
@Slf4j
public class ProjectResourceService {

    public static final String PROCESSING_VALID_ENTITIES = "processing {} valid entities";
    private final List<Validator<ProjectResourceBulkRequest, ProjectResource>> validators;

    private final ProjectResourceRepository projectResourceRepository;

    private final ProjectConfiguration projectConfiguration;

    private final ProjectResourceEnrichmentService enrichmentService;

    private final Predicate<Validator<ProjectResourceBulkRequest, ProjectResource>> isApplicableForCreate = validator ->
            validator.getClass().equals(PrProductVariantIdValidator.class)
                    || validator.getClass().equals(PrProjectIdValidator.class)
                    || validator.getClass().equals(PrUniqueCombinationValidator.class);

    private final Predicate<Validator<ProjectResourceBulkRequest, ProjectResource>> isApplicableForUpdate = validator ->
            validator.getClass().equals(PrProductVariantIdValidator.class)
                    || validator.getClass().equals(PrProjectIdValidator.class)
                    || validator.getClass().equals(PrNonExistentEntityValidator.class)
                    || validator.getClass().equals(PrNullIdValidator.class)
                    || validator.getClass().equals(PrIsDeletedValidator.class)
                    || validator.getClass().equals(PrRowVersionValidator.class)
                    || validator.getClass().equals(PrUniqueEntityValidator.class)
                    || validator.getClass().equals(PrUniqueCombinationValidator.class);

    private final Predicate<Validator<ProjectResourceBulkRequest, ProjectResource>> isApplicableForDelete = validator ->
            validator.getClass().equals(PrNonExistentEntityValidator.class)
                    || validator.getClass().equals(PrNullIdValidator.class);

    public ProjectResourceService(List<Validator<ProjectResourceBulkRequest, ProjectResource>> validators, ProjectResourceRepository projectResourceRepository, ProjectConfiguration projectConfiguration, ProjectResourceEnrichmentService enrichmentService) {
        this.validators = validators;
        this.projectResourceRepository = projectResourceRepository;
        this.projectConfiguration = projectConfiguration;
        this.enrichmentService = enrichmentService;
    }

    public ProjectResource create(ProjectResourceRequest request) {
        log.trace("Entering create (single resource)");
        log.info("Received request to create project resource");
        ProjectResourceBulkRequest resourceBulkRequest = ProjectResourceBulkRequest.builder()
                .projectResource(Collections.singletonList(request.getProjectResource())).requestInfo(request.getRequestInfo())
                .build();

        ProjectResource result = create(resourceBulkRequest, false).get(0);
        log.trace("Exiting create (single resource)");
        return result;
    }

    public List<ProjectResource> create(ProjectResourceBulkRequest request, boolean isBulk) {
        log.trace("Entering create (bulk resources)");
        log.info("Received request to create bulk project resources");
        Tuple<List<ProjectResource>, Map<ProjectResource, ErrorDetails>> tuple = validate(validators,
                isApplicableForCreate, request, SET_PROJECT_RESOURCE, GET_PROJECT_RESOURCE, VALIDATION_ERROR,
                isBulk);

        Map<ProjectResource, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectResource> validEntities = tuple.getX();
        log.debug("Validation completed - {} valid resources, {} errors", validEntities.size(), errorDetailsMap.size());
        try {
            if (!validEntities.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validEntities.size());
                log.debug("Enriching resources before save");
                enrichmentService.create(validEntities, request);
                log.debug("Saving resources to repository");
                projectResourceRepository.save(validEntities, projectConfiguration.getCreateProjectResourceTopic());
                log.info("Successfully created {} project resources", validEntities.size());
            } else {
                log.warn("No valid resources to create after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while creating project resources", exception);
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_PROJECT_RESOURCE);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting create (bulk resources)");
        return validEntities;
    }

    public ProjectResource update(ProjectResourceRequest request) {
        log.trace("Entering update (single resource)");
        log.info("Received request to update project resource");
        ProjectResourceBulkRequest resourceBulkRequest = ProjectResourceBulkRequest.builder()
                .projectResource(Arrays.asList(request.getProjectResource())).requestInfo(request.getRequestInfo())
                .build();

        ProjectResource result = update(resourceBulkRequest, false).get(0);
        log.trace("Exiting update (single resource)");
        return result;
    }

    public List<ProjectResource> update(ProjectResourceBulkRequest request, boolean isBulk) {
        log.trace("Entering update (bulk resources)");
        log.info("Received request to update bulk project resources");
        Tuple<List<ProjectResource>, Map<ProjectResource, ErrorDetails>> tuple = validate(validators,
                isApplicableForUpdate, request, SET_PROJECT_RESOURCE, GET_PROJECT_RESOURCE, VALIDATION_ERROR,
                isBulk);

        Map<ProjectResource, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectResource> validEntities = tuple.getX();
        log.debug("Validation completed - {} valid resources, {} errors", validEntities.size(), errorDetailsMap.size());
        try {
            if (!validEntities.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validEntities.size());
                log.debug("Enriching resources before update");
                enrichmentService.update(validEntities, request);
                log.debug("Saving updated resources to repository");
                projectResourceRepository.save(validEntities, projectConfiguration.getUpdateProjectResourceTopic());
                log.info("Successfully updated {} project resources", validEntities.size());
            } else {
                log.warn("No valid resources to update after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while updating project resources", exception);
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_PROJECT_RESOURCE);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting update (bulk resources)");
        return validEntities;
    }

    public ProjectResource delete(ProjectResourceRequest request) {
        log.trace("Entering delete (single resource)");
        log.info("Received request to delete project resource");
        ProjectResourceBulkRequest resourceBulkRequest = ProjectResourceBulkRequest.builder()
                .projectResource(Arrays.asList(request.getProjectResource())).requestInfo(request.getRequestInfo())
                .build();

        ProjectResource result = delete(resourceBulkRequest, false).get(0);
        log.trace("Exiting delete (single resource)");
        return result;
    }

    public List<ProjectResource> delete(ProjectResourceBulkRequest request, boolean isBulk) {
        log.trace("Entering delete (bulk resources)");
        log.info("Received request to delete bulk project resources");
        Tuple<List<ProjectResource>, Map<ProjectResource, ErrorDetails>> tuple = validate(validators,
                isApplicableForDelete, request, SET_PROJECT_RESOURCE, GET_PROJECT_RESOURCE, VALIDATION_ERROR,
                isBulk);

        Map<ProjectResource, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectResource> validEntities = tuple.getX();
        log.debug("Validation completed - {} valid resources, {} errors", validEntities.size(), errorDetailsMap.size());
        try {
            if (!validEntities.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validEntities.size());
                log.debug("Enriching resources before delete");
                enrichmentService.delete(validEntities, request);
                log.debug("Saving deleted resources to repository");
                projectResourceRepository.save(validEntities, projectConfiguration.getDeleteProjectResourceTopic());
                log.info("Successfully deleted {} project resources", validEntities.size());
            } else {
                log.warn("No valid resources to delete after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while deleting project resources", exception);
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_PROJECT_RESOURCE);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting delete (bulk resources)");
        return validEntities;
    }


    public SearchResponse<ProjectResource> search(ProjectResourceSearchRequest request,
                                                  Integer limit,
                                                  Integer offset,
                                                  String tenantId,
                                                  Long lastChangedSince,
                                                  Boolean includeDeleted) throws QueryBuilderException {
        log.trace("Entering search");
        log.info("Received request to search project resources");
        String idFieldName = getIdFieldName(request.getProjectResource());

        if (isSearchByIdOnly(request.getProjectResource(), idFieldName)) {
            log.info("Searching project resources by ID");
            List<String> ids = (List<String>) ReflectionUtils.invokeMethod(getIdMethod((Collections
                            .singletonList(request.getProjectResource()))),
                    request.getProjectResource());
            log.debug("Fetching project resources with {} IDs", ids != null ? ids.size() : 0);
            List<ProjectResource> projectResources = projectResourceRepository.findById(ids, includeDeleted, idFieldName).stream()
                    .filter(lastChangedSince(lastChangedSince))
                    .filter(havingTenantId(tenantId))
                    .filter(includeDeleted(includeDeleted))
                    .toList();
            log.info("Search by ID completed - found {} resources", projectResources.size());
            log.trace("Exiting search");
            return SearchResponse.<ProjectResource>builder().response(projectResources).build();
        }

        log.info("Searching project resources using criteria");
        log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}", limit, offset, tenantId);
        SearchResponse<ProjectResource> result = projectResourceRepository.findWithCount(request.getProjectResource(),
                limit, offset, tenantId, lastChangedSince, includeDeleted);
        log.info("Search by criteria completed - found {} resources", result.getResponse() != null ? result.getResponse().size() : 0);
        log.trace("Exiting search");
        return result;
    }
}
