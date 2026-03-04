package org.egov.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.ds.Tuple;
import org.egov.common.models.ErrorDetails;
import org.egov.common.models.core.ProjectSearchURLParams;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.models.project.*;
import org.egov.common.producer.Producer;
import org.egov.common.service.IdGenService;
import org.egov.common.service.UserService;
import org.egov.common.utils.CommonUtils;
import org.egov.common.validator.Validator;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.repository.ProjectFacilityRepository;
import org.egov.project.service.enrichment.ProjectFacilityEnrichmentService;
import org.egov.project.validator.facility.*;
import org.egov.project.web.models.*;
import org.egov.project.web.models.Document;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.project.Constants.SET_PROJECT_FACILITIES;
import static org.egov.project.Constants.VALIDATION_ERROR;

@Service
@Slf4j
public class ProjectFacilityService {

    private final IdGenService idGenService;

    private final ProjectFacilityRepository projectFacilityRepository;

    private final Producer producer;

    private final ProjectService projectService;

    private final UserService userService;

    private final ProjectConfiguration projectConfiguration;

    private final ProjectFacilityEnrichmentService enrichmentService;

    private final List<Validator<ProjectFacilityBulkRequest, ProjectFacility>> validators;

    private final ProjectConfiguration config;

    private final ServiceRequestRepository repository;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    private final Predicate<Validator<ProjectFacilityBulkRequest, ProjectFacility>> isApplicableForCreate = validator ->
            validator.getClass().equals(PfFacilityIdValidator.class)
                    || validator.getClass().equals(PfProjectIdValidator.class)
                    || validator.getClass().equals(PfUniqueCombinationValidator.class);

    private final Predicate<Validator<ProjectFacilityBulkRequest, ProjectFacility>> isApplicableForUpdate = validator ->
            validator.getClass().equals(PfFacilityIdValidator.class)
                    || validator.getClass().equals(PfProjectIdValidator.class)
                    || validator.getClass().equals(PfNullIdValidator.class)
                    || validator.getClass().equals(PfIsDeletedValidator.class)
                    || validator.getClass().equals(PfRowVersionValidator.class)
                    || validator.getClass().equals(PfNonExistentEntityValidator.class)
                    || validator.getClass().equals(PfUniqueEntityValidator.class)
                    || validator.getClass().equals(PfUniqueCombinationValidator.class);

    private final Predicate<Validator<ProjectFacilityBulkRequest, ProjectFacility>> isApplicableForDelete = validator ->
            validator.getClass().equals(PfNullIdValidator.class)
                    || validator.getClass().equals(PfNonExistentEntityValidator.class);

    @Autowired
    public ProjectFacilityService(
            IdGenService idGenService,
            ProjectFacilityRepository projectFacilityRepository,
            ProjectService projectService,
            UserService userService,
            ProjectConfiguration projectConfiguration,
            ProjectFacilityEnrichmentService enrichmentService, List<Validator<ProjectFacilityBulkRequest, ProjectFacility>> validators,
            Producer producer, ProjectConfiguration config, ServiceRequestRepository repository, @Qualifier("objectMapper") ObjectMapper mapper) {
        this.idGenService = idGenService;
        this.projectFacilityRepository = projectFacilityRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.projectConfiguration = projectConfiguration;
        this.enrichmentService = enrichmentService;
        this.validators = validators;
        this.producer = producer;
        this.config = config;
        this.repository = repository;
        this.mapper = mapper;
    }

    public ProjectFacility create(ProjectFacilityRequest request) {
        log.trace("Entering create (single facility)");
        log.info("Received request to create project facility");
        ProjectFacilityBulkRequest bulkRequest = ProjectFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .projectFacilities(Collections.singletonList(request.getProjectFacility())).build();
        log.debug("Creating bulk request");
        ProjectFacility result = create(bulkRequest, false).get(0);
        log.trace("Exiting create (single facility)");
        return result;
    }


    public List<ProjectFacility> create(ProjectFacilityBulkRequest request, boolean isBulk) {
        log.trace("Entering create (bulk facilities)");
        log.info("Received request to create bulk project facilities");
        Tuple<List<ProjectFacility>, Map<ProjectFacility, ErrorDetails>> tuple = validate(validators,
                isApplicableForCreate, request,
                isBulk);

        Map<ProjectFacility, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectFacility> validEntities = tuple.getX();
        log.debug("Validation completed - {} valid facilities, {} errors", validEntities.size(), errorDetailsMap.size());
        try {
            if (!validEntities.isEmpty()) {
                log.info("Processing {} valid entities", validEntities.size());
                log.debug("Enriching facilities before save");
                enrichmentService.create(validEntities, request);
                log.debug("Saving facilities to repository");
                projectFacilityRepository.save(validEntities, projectConfiguration.getCreateProjectFacilityTopic());
                log.debug("Fetching associated project and facility details");
                Project existingProject = searchProject(request);
                Facility facility = getFacilityById(request);
                log.debug("Enriching project with facility details");
                Object enrichedAdditionalDetails = mergeListIntoAdditionalDetails(existingProject.getAdditionalDetails(), "facility", facility);
                existingProject.setAdditionalDetails(enrichedAdditionalDetails);
                ProjectRequest projectRequest = ProjectRequest.builder().requestInfo(request.getRequestInfo()).projects(List.of(existingProject)).build();
                log.debug("Pushing project update to Kafka");
                producer.push(projectConfiguration.getUpdateProjectTopic(), projectRequest);
                producer.push(projectConfiguration.getUpdateProjectTopicIndexer(), projectRequest);
                log.info("Successfully created {} project facilities", validEntities.size());
            } else {
                log.warn("No valid facilities to create after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while creating project facilities", exception);
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_PROJECT_FACILITIES);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting create (bulk facilities)");
        return validEntities;
    }


    public ProjectFacility update(ProjectFacilityRequest request) {
        log.trace("Entering update (single facility)");
        log.info("Received request to update project facility");
        ProjectFacilityBulkRequest bulkRequest = ProjectFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .projectFacilities(Collections.singletonList(request.getProjectFacility())).build();
        log.debug("Creating bulk request");
        ProjectFacility result = update(bulkRequest, false).get(0);
        log.trace("Exiting update (single facility)");
        return result;
    }

    public List<ProjectFacility> update(ProjectFacilityBulkRequest request, boolean isBulk) {
        log.trace("Entering update (bulk facilities)");
        log.info("Received request to update bulk project facilities");
        Tuple<List<ProjectFacility>, Map<ProjectFacility, ErrorDetails>> tuple = validate(validators,
                isApplicableForUpdate, request,
                isBulk);

        Map<ProjectFacility, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectFacility> validEntities = tuple.getX();
        log.debug("Validation completed - {} valid facilities, {} errors", validEntities.size(), errorDetailsMap.size());
        try {
            if (!validEntities.isEmpty()) {
                log.info("Processing {} valid entities", validEntities.size());
                log.debug("Enriching facilities before update");
                enrichmentService.update(validEntities, request);
                log.debug("Saving updated facilities to repository");
                projectFacilityRepository.save(validEntities, projectConfiguration.getUpdateProjectFacilityTopic());
                log.info("Successfully updated {} project facilities", validEntities.size());
            } else {
                log.warn("No valid facilities to update after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while updating project facilities", exception);
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_PROJECT_FACILITIES);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting update (bulk facilities)");
        return validEntities;
    }

    public ProjectFacility delete(ProjectFacilityRequest request) {
        log.trace("Entering delete (single facility)");
        log.info("Received request to delete a project facility");
        ProjectFacilityBulkRequest bulkRequest = ProjectFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .projectFacilities(Collections.singletonList(request.getProjectFacility())).build();
        log.debug("Creating bulk request");
        ProjectFacility result = delete(bulkRequest, false).get(0);
        log.trace("Exiting delete (single facility)");
        return result;
    }

    public List<ProjectFacility> delete(ProjectFacilityBulkRequest request, boolean isBulk) {
        log.trace("Entering delete (bulk facilities)");
        log.info("Received request to delete bulk project facilities");
        Tuple<List<ProjectFacility>, Map<ProjectFacility, ErrorDetails>> tuple = validate(validators,
                isApplicableForDelete, request,
                isBulk);

        Map<ProjectFacility, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectFacility> validEntities = tuple.getX();
        log.debug("Validation completed - {} valid facilities, {} errors", validEntities.size(), errorDetailsMap.size());
        try {
            if (!validEntities.isEmpty()) {
                log.info("Processing {} valid entities", validEntities.size());
                log.debug("Enriching facilities before delete");
                enrichmentService.delete(validEntities, request);
                log.debug("Saving deleted facilities to repository");
                projectFacilityRepository.save(validEntities, projectConfiguration.getDeleteProjectFacilityTopic());
                log.info("Successfully deleted {} project facilities", validEntities.size());
            } else {
                log.warn("No valid facilities to delete after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while deleting project facilities", exception);
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_PROJECT_FACILITIES);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting delete (bulk facilities)");
        return validEntities;
    }

    private Tuple<List<ProjectFacility>, Map<ProjectFacility, ErrorDetails>> validate(List<Validator<ProjectFacilityBulkRequest, ProjectFacility>> validators,
                                                                                      Predicate<Validator<ProjectFacilityBulkRequest, ProjectFacility>> applicableValidators,
                                                                                      ProjectFacilityBulkRequest request, boolean isBulk) {
        log.trace("Entering validate for {} facilities", request.getProjectFacilities() != null ? request.getProjectFacilities().size() : 0);
        log.debug("Validating request with {} validators", validators.size());
        Map<ProjectFacility, ErrorDetails> errorDetailsMap = new HashMap<>();
        List<ProjectFacility> validEntities = request.getProjectFacilities().stream()
                .filter(notHavingErrors()).toList();
        log.debug("Validation completed - {} valid facilities out of {}", validEntities.size(), request.getProjectFacilities().size());
        log.trace("Exiting validate");
        return new Tuple<>(validEntities, errorDetailsMap);
    }

    public SearchResponse<ProjectFacility> search(ProjectFacilitySearchRequest projectFacilitySearchRequest,
                                                  Integer limit,
                                                  Integer offset,
                                                  String tenantId,
                                                  Long lastChangedSince,
                                                  Boolean includeDeleted) throws Exception {
        log.trace("Entering search");
        log.info("Received request to search project facilities");

        if (isSearchByIdOnly(projectFacilitySearchRequest.getProjectFacility())) {
            log.info("Searching project facilities by ID");
            List<String> ids = projectFacilitySearchRequest.getProjectFacility().getId();
            log.debug("Fetching project facilities with {} IDs", ids != null ? ids.size() : 0);
            List<ProjectFacility> projectfacilities = projectFacilityRepository.findById(ids, includeDeleted).stream()
                    .filter(lastChangedSince(lastChangedSince))
                    .filter(havingTenantId(tenantId))
                    .filter(includeDeleted(includeDeleted))
                    .toList();
            log.info("Search by ID completed - found {} facilities", projectfacilities.size());
            log.trace("Exiting search");
            return SearchResponse.<ProjectFacility>builder().response(projectfacilities).build();
        }
        log.info("Searching project facilities using criteria");
        log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}", limit, offset, tenantId);
        SearchResponse<ProjectFacility> result = projectFacilityRepository.findWithCount(projectFacilitySearchRequest.getProjectFacility(),
                limit, offset, tenantId, lastChangedSince, includeDeleted);
        log.info("Search by criteria completed - found {} facilities", result.getResponse() != null ? result.getResponse().size() : 0);
        log.trace("Exiting search");
        return result;
    }

    public Project searchProject(ProjectFacilityBulkRequest request) throws Exception {
        String projectId = request.getProjectFacilities().get(0).getProjectId();
        ProjectSearch searchCriteria = ProjectSearch.builder()
                .id(List.of(projectId))
                .build();

        ProjectSearchRequest searchRequest = ProjectSearchRequest.builder()
                .project(searchCriteria)
                .requestInfo(request.getRequestInfo())
                .build();


        ProjectSearchURLParams urlParams = ProjectSearchURLParams.builder()
                .limit(1)
                .offset(0)
                .tenantId("in")
                .includeAncestors(false)
                .includeDescendants(false)
                .build();
        List<String> workflowStatuses = null;
        ProjectSortCriteria sortCriteria = null;

        List<Project> projects = projectService.searchProject(searchRequest, urlParams, workflowStatuses, sortCriteria);

        if (projects == null || projects.isEmpty()) {
            throw new CustomException("PROJECT_NOT_FOUND", "Project not found with ID: " + projectId);
        }

        Project existingProject = projects.get(0);

        return existingProject;
    }

    public Facility getFacilityById(ProjectFacilityBulkRequest request) {
        String facilityId = request.getProjectFacilities().get(0).getFacilityId();

        String url = config.getFacilityServiceHost() + config.getFacilityServiceSearchUrlV2()+ "?facilityId="+facilityId;
        Object response = repository.fetchResult(new StringBuilder(url));

        FacilitySearchResponse facilityList = mapper.convertValue(response, FacilitySearchResponse.class);
        if(facilityList != null && facilityList.getFacilities() !=null && facilityList.getFacilities().size() > 0){
            return facilityList.getFacilities().get(0);
        }
        return null;
    }

    private Object mergeListIntoAdditionalDetails(Object additionalDetails, String key, Object value) {
        if (additionalDetails instanceof ObjectNode) {
            ((ObjectNode) additionalDetails).put(key, mapper.valueToTree(value));
            return additionalDetails;
        } else if (additionalDetails instanceof Map) {
            ((Map<String, Object>) additionalDetails).put(key, value);
            return additionalDetails;
        } else {
            // default to HashMap if null or unknown type
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            return map;
        }
    }
}
