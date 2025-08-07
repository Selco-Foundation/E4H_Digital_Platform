package org.egov.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        log.info("received request to create project facility");
        ProjectFacilityBulkRequest bulkRequest = ProjectFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .projectFacilities(Collections.singletonList(request.getProjectFacility())).build();
        log.info("creating bulk request");
        return create(bulkRequest, false).get(0);
    }


    public List<ProjectFacility> create(ProjectFacilityBulkRequest request, boolean isBulk) {
        log.info("received request to create bulk project facility");
        Tuple<List<ProjectFacility>, Map<ProjectFacility, ErrorDetails>> tuple = validate(validators,
                isApplicableForCreate, request,
                isBulk);

        Map<ProjectFacility, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectFacility> validEntities = tuple.getX();
        try {
            if (!validEntities.isEmpty()) {
                log.info("processing {} valid entities", validEntities.size());
                enrichmentService.create(validEntities, request);
                projectFacilityRepository.save(validEntities, projectConfiguration.getCreateProjectFacilityTopic());
                Project existingProject = searchProject(request);
                Facility facility = getFacilityById(request);
                Object enrichedAdditionalDetails = mergeListIntoAdditionalDetails(existingProject.getAdditionalDetails(), "facility", facility);
                existingProject.setAdditionalDetails(enrichedAdditionalDetails);
                ProjectRequest projectRequest = ProjectRequest.builder().requestInfo(request.getRequestInfo()).projects(List.of(existingProject)).build();
                producer.push(projectConfiguration.getUpdateProjectTopicIndexer(), projectRequest);
                log.info("successfully created project facility");
            }
        } catch (Exception exception) {
            log.error("error occurred while creating project facility: {}", ExceptionUtils.getStackTrace(exception));
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_PROJECT_FACILITIES);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);

        return validEntities;
    }


    public ProjectFacility update(ProjectFacilityRequest request) {
        log.debug("received request to update project facility");
        ProjectFacilityBulkRequest bulkRequest = ProjectFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .projectFacilities(Collections.singletonList(request.getProjectFacility())).build();
        log.info("creating bulk request");
        return update(bulkRequest, false).get(0);
    }

    public List<ProjectFacility> update(ProjectFacilityBulkRequest request, boolean isBulk) {
        log.info("received request to update bulk project facility");
        Tuple<List<ProjectFacility>, Map<ProjectFacility, ErrorDetails>> tuple = validate(validators,
                isApplicableForUpdate, request,
                isBulk);

        Map<ProjectFacility, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectFacility> validEntities = tuple.getX();
        try {
            if (!validEntities.isEmpty()) {
                log.info("processing {} valid entities", validEntities.size());
                enrichmentService.update(validEntities, request);
                projectFacilityRepository.save(validEntities, projectConfiguration.getUpdateProjectFacilityTopic());
                log.info("successfully updated bulk project facility");
            }
        } catch (Exception exception) {
            log.error("error occurred while updating project facility", ExceptionUtils.getStackTrace(exception));
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_PROJECT_FACILITIES);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);

        return validEntities;
    }

    public ProjectFacility delete(ProjectFacilityRequest request) {
        log.info("received request to delete a project facility");
        ProjectFacilityBulkRequest bulkRequest = ProjectFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .projectFacilities(Collections.singletonList(request.getProjectFacility())).build();
        log.info("creating bulk request");
        return delete(bulkRequest, false).get(0);
    }

    public List<ProjectFacility> delete(ProjectFacilityBulkRequest request, boolean isBulk) {
        Tuple<List<ProjectFacility>, Map<ProjectFacility, ErrorDetails>> tuple = validate(validators,
                isApplicableForDelete, request,
                isBulk);

        Map<ProjectFacility, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectFacility> validEntities = tuple.getX();
        try {
            if (!validEntities.isEmpty()) {
                log.info("processing {} valid entities", validEntities.size());
                enrichmentService.delete(validEntities, request);
                projectFacilityRepository.save(validEntities, projectConfiguration.getDeleteProjectFacilityTopic());
                log.info("successfully deleted entities");
            }
        } catch (Exception exception) {
            log.error("error occurred while deleting entities: {}", ExceptionUtils.getStackTrace(exception));
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_PROJECT_FACILITIES);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);

        return validEntities;
    }

    private Tuple<List<ProjectFacility>, Map<ProjectFacility, ErrorDetails>> validate(List<Validator<ProjectFacilityBulkRequest, ProjectFacility>> validators,
                                                                                      Predicate<Validator<ProjectFacilityBulkRequest, ProjectFacility>> applicableValidators,
                                                                                      ProjectFacilityBulkRequest request, boolean isBulk) {
        log.info("validating request");
        Map<ProjectFacility, ErrorDetails> errorDetailsMap = new HashMap<>();
        List<ProjectFacility> validEntities = request.getProjectFacilities().stream()
                .filter(notHavingErrors()).toList();
        log.info("validation successful, found valid project facility");
        return new Tuple<>(validEntities, errorDetailsMap);
    }

    public SearchResponse<ProjectFacility> search(ProjectFacilitySearchRequest projectFacilitySearchRequest,
                                                  Integer limit,
                                                  Integer offset,
                                                  String tenantId,
                                                  Long lastChangedSince,
                                                  Boolean includeDeleted) throws Exception {
        log.info("received request to search project facility");

        if (isSearchByIdOnly(projectFacilitySearchRequest.getProjectFacility())) {
            log.info("searching project facility by id");
            List<String> ids = projectFacilitySearchRequest.getProjectFacility().getId();
            log.info("fetching project facility with ids: {}", ids);
            List<ProjectFacility> projectfacilities = projectFacilityRepository.findById(ids, includeDeleted).stream()
                    .filter(lastChangedSince(lastChangedSince))
                    .filter(havingTenantId(tenantId))
                    .filter(includeDeleted(includeDeleted))
                    .toList();
            return SearchResponse.<ProjectFacility>builder().response(projectfacilities).build();
        }
        log.info("searching project facility using criteria");
        return projectFacilityRepository.findWithCount(projectFacilitySearchRequest.getProjectFacility(),
                limit, offset, tenantId, lastChangedSince, includeDeleted);
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
        return facilityList.getFacilities().get(0);
    }

    private Object mergeListIntoAdditionalDetails(Object additionalDetails, String key, Object value) {
        if (additionalDetails instanceof Map) {
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
