package org.egov.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import org.egov.project.repository.ProjectStaffRepository;
import org.egov.project.service.enrichment.ProjectStaffEnrichmentService;
import org.egov.project.validator.staff.*;
import org.egov.project.web.models.*;
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
import static org.egov.project.Constants.SET_STAFF;
import static org.egov.project.Constants.VALIDATION_ERROR;

@Service
@Slf4j
public class ProjectStaffService {

    public static final String CREATING_BULK_REQUEST = "creating bulk request";
    public static final String PROCESSING_VALID_ENTITIES = "processing {} valid entities";

    private final ProjectStaffRepository projectStaffRepository;

    private final ProjectConfiguration projectConfiguration;

    private final ProjectStaffEnrichmentService enrichmentService;

    private final List<Validator<ProjectStaffBulkRequest, ProjectStaff>> validators;

    private final ServiceRequestRepository serviceRequestRepository;

    private final Producer producer;

    private final ProjectService projectService;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    private final Predicate<Validator<ProjectStaffBulkRequest, ProjectStaff>> isApplicableForCreate = validator ->
            validator.getClass().equals(PsUserIdValidator.class)
                    || validator.getClass().equals(PsProjectIdValidator.class)
                    || validator.getClass().equals(PsUniqueCombinationValidator.class);

    private final Predicate<Validator<ProjectStaffBulkRequest, ProjectStaff>> isApplicableForUpdate = validator ->
            validator.getClass().equals(PsUserIdValidator.class)
                    || validator.getClass().equals(PsProjectIdValidator.class)
                    || validator.getClass().equals(PsNullIdValidator.class)
                    || validator.getClass().equals(PsIsDeletedValidator.class)
                    || validator.getClass().equals(PsRowVersionValidator.class)
                    || validator.getClass().equals(PsNonExistentEntityValidator.class)
                    || validator.getClass().equals(PsUniqueEntityValidator.class)
                    || validator.getClass().equals(PsUniqueCombinationValidator.class);

    private final Predicate<Validator<ProjectStaffBulkRequest, ProjectStaff>> isApplicableForDelete = validator ->
            validator.getClass().equals(PsNullIdValidator.class)
                    || validator.getClass().equals(PsNonExistentEntityValidator.class);

    @Autowired
    public ProjectStaffService(
            IdGenService idGenService,
            ProjectStaffRepository projectStaffRepository,
            ProjectService projectService,
            UserService userService,
            ProjectConfiguration projectConfiguration,
            ProjectStaffEnrichmentService enrichmentService,
            Producer producer, List<Validator<ProjectStaffBulkRequest, ProjectStaff>> validators, ServiceRequestRepository serviceRequestRepository, @Qualifier("objectMapper") ObjectMapper mapper) {
        this.projectStaffRepository = projectStaffRepository;
        this.projectConfiguration = projectConfiguration;
        this.enrichmentService = enrichmentService;
        this.validators = validators;
        this.producer = producer;
        this.serviceRequestRepository = serviceRequestRepository;
        this.mapper = mapper;
        this.projectService = projectService;
    }

    public ProjectStaff create(ProjectStaffRequest request) {
        log.info("received request to create project staff");
        ProjectStaffBulkRequest bulkRequest = ProjectStaffBulkRequest.builder().requestInfo(request.getRequestInfo())
                .projectStaff(Collections.singletonList(request.getProjectStaff())).build();
        log.info(CREATING_BULK_REQUEST);
        return create(bulkRequest, false).get(0);
    }


    public List<ProjectStaff> create(ProjectStaffBulkRequest request, boolean isBulk) {
        log.info("received request to create bulk project staff");
        Tuple<List<ProjectStaff>, Map<ProjectStaff, ErrorDetails>> tuple = validate(validators,
                isApplicableForCreate, request,
                isBulk);

        Map<ProjectStaff, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectStaff> validEntities = tuple.getX();
        try {
            if (!validEntities.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validEntities.size());
                enrichmentService.create(validEntities, request);
                // Pushing the data as ProjectStaffBulkRequest for Attendance Service Consumer
                producer.push(projectConfiguration.getProjectStaffAttendanceTopic(), new ProjectStaffBulkRequest(request.getRequestInfo(), validEntities));
                // Pushing the data as list for persister consumer
                projectStaffRepository.save(validEntities, projectConfiguration.getCreateProjectStaffTopic());
                Project existingProject = searchProject(request);
                Employee employee = getUserById(request);
                Object enrichedAdditionalDetails = mergeListIntoAdditionalDetails(existingProject.getAdditionalDetails(), "assignedTo", employee);
                existingProject.setAdditionalDetails(enrichedAdditionalDetails);
                ProjectRequest projectRequest = ProjectRequest.builder().requestInfo(request.getRequestInfo()).projects(List.of(existingProject)).build();
                producer.push(projectConfiguration.getUpdateProjectTopicIndexerAssignTo(), projectRequest);
                log.info("successfully created project staff");
            }
        } catch (Exception exception) {
            log.error("error occurred while creating project staff: {}", ExceptionUtils.getStackTrace(exception));
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_STAFF);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);

        return validEntities;
    }


    public ProjectStaff update(ProjectStaffRequest request) {
        log.debug("received request to update project staff");
        ProjectStaffBulkRequest bulkRequest = ProjectStaffBulkRequest.builder().requestInfo(request.getRequestInfo())
                .projectStaff(Collections.singletonList(request.getProjectStaff())).build();
        log.info(CREATING_BULK_REQUEST);
        return update(bulkRequest, false).get(0);
    }

    public List<ProjectStaff> update(ProjectStaffBulkRequest request, boolean isBulk) {
        log.info("received request to update bulk project staff");
        Tuple<List<ProjectStaff>, Map<ProjectStaff, ErrorDetails>> tuple = validate(validators,
                isApplicableForUpdate, request,
                isBulk);

        Map<ProjectStaff, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectStaff> validEntities = tuple.getX();
        try {
            if (!validEntities.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validEntities.size());
                enrichmentService.update(validEntities, request);
                projectStaffRepository.save(validEntities, projectConfiguration.getUpdateProjectStaffTopic());
                log.info("successfully updated bulk project staff");
            }
        } catch (Exception exception) {
            log.error("error occurred while updating project staff", ExceptionUtils.getStackTrace(exception));
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_STAFF);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);

        return validEntities;
    }

    public ProjectStaff delete(ProjectStaffRequest request) {
        log.info("received request to delete a project staff");
        ProjectStaffBulkRequest bulkRequest = ProjectStaffBulkRequest.builder().requestInfo(request.getRequestInfo())
                .projectStaff(Collections.singletonList(request.getProjectStaff())).build();
        log.info(CREATING_BULK_REQUEST);
        return delete(bulkRequest, false).get(0);
    }

    public List<ProjectStaff> delete(ProjectStaffBulkRequest request, boolean isBulk) {
        Tuple<List<ProjectStaff>, Map<ProjectStaff, ErrorDetails>> tuple = validate(validators,
                isApplicableForDelete, request,
                isBulk);

        Map<ProjectStaff, ErrorDetails> errorDetailsMap = tuple.getY();
        List<ProjectStaff> validEntities = tuple.getX();
        try {
            if (!validEntities.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validEntities.size());
                enrichmentService.delete(validEntities, request);
                projectStaffRepository.save(validEntities, projectConfiguration.getDeleteProjectStaffTopic());
                log.info("successfully deleted entities");
            }
        } catch (Exception exception) {
            log.error("error occurred while deleting entities: {}", ExceptionUtils.getStackTrace(exception));
            populateErrorDetails(request, errorDetailsMap, validEntities, exception, SET_STAFF);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);

        return validEntities;
    }

    private Tuple<List<ProjectStaff>, Map<ProjectStaff, ErrorDetails>> validate(List<Validator<ProjectStaffBulkRequest, ProjectStaff>> validators,
                                                                                Predicate<Validator<ProjectStaffBulkRequest, ProjectStaff>> applicableValidators,
                                                                                ProjectStaffBulkRequest request, boolean isBulk) {
        log.info("validating request");
        Map<ProjectStaff, ErrorDetails> errorDetailsMap = new HashMap<>();
        if (!errorDetailsMap.isEmpty() && !isBulk) {
            log.error("validation error occurred. error details: {}", errorDetailsMap.values().toString());
            throw new CustomException(VALIDATION_ERROR, errorDetailsMap.values().toString());
        }
        List<ProjectStaff> validEntities = request.getProjectStaff().stream()
                .filter(notHavingErrors()).toList();
        log.info("validation successful, found valid project staff");
        return new Tuple<>(validEntities, errorDetailsMap);
    }

    public SearchResponse<ProjectStaff> search(ProjectStaffSearchRequest projectStaffSearchRequest,
                                               Integer limit,
                                               Integer offset,
                                               String tenantId,
                                               Long lastChangedSince,
                                               Boolean includeDeleted) throws Exception {
        log.info("received request to search project staff");

        if (isSearchByIdOnly(projectStaffSearchRequest.getProjectStaff())) {
            log.info("searching project staff by id");
            List<String> ids = projectStaffSearchRequest.getProjectStaff().getId();
            log.info("fetching project staff with ids: {}", ids);
            List<ProjectStaff> projectStaffs = projectStaffRepository.findById(ids, includeDeleted).stream()
                    .filter(lastChangedSince(lastChangedSince))
                    .filter(havingTenantId(tenantId))
                    .filter(includeDeleted(includeDeleted))
                    .toList();
            return SearchResponse.<ProjectStaff>builder().response(projectStaffs).build();
        }
        log.info("searching project staff using criteria");
        return projectStaffRepository.findWithCount(projectStaffSearchRequest.getProjectStaff(),
                limit, offset, tenantId, lastChangedSince, includeDeleted);
    }

    public Employee getUserById(ProjectStaffBulkRequest request) {
        String userId = request.getProjectStaff().get(0).getUserId();

        String url = projectConfiguration.getHrmsHost() + projectConfiguration.getHrmsSearchUrl()+ "?tenantId=in&uuids="+userId;
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);

        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        return employeeResponse.getEmployees().get(0);
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

    public Project searchProject(ProjectStaffBulkRequest request) throws Exception {
        String projectId = request.getProjectStaff().get(0).getProjectId();
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

}
