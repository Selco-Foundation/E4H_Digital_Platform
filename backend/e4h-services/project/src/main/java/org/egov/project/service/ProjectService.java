package org.egov.project.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.core.ProjectSearchURLParams;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.models.project.*;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.ProjectRequest;
import org.egov.common.models.project.ProjectSearch;
import org.egov.common.models.project.ProjectSearchRequest;
import org.egov.common.producer.Producer;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.repository.ProjectRepository;
import org.egov.project.service.enrichment.ProjectEnrichment;
import org.egov.project.util.BoundaryV2Util;
import org.egov.project.util.ProjectServiceUtil;
import org.egov.project.validator.project.ProjectValidator;
import org.egov.project.web.models.*;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

import static org.egov.project.util.ProjectConstants.*;

@Service
@Slf4j
public class ProjectService {

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    private final ProjectRepository projectRepository;

    private final ProjectValidator projectValidator;

    private final ProjectEnrichment projectEnrichment;

    private final ProjectConfiguration projectConfiguration;

    private final ProjectFacilityService projectFacilityService;

    private final Producer producer;

    private final ProjectServiceUtil projectServiceUtil;

    private final ObjectMapper objectMapper;

    private final ProjectWorkflowService workflowService;

    private final JdbcTemplate jdbcTemplate;

    private final ServiceRequestRepository serviceRequestRepository;

    private final ProjectNameGenerationService projectNameGenerationService;

    @Autowired
    BoundaryV2Util boundaryV2Util;

    @Autowired
    public ProjectService(
            ProjectRepository projectRepository,
            ProjectValidator projectValidator, ProjectEnrichment projectEnrichment, ProjectConfiguration projectConfiguration, Producer producer, ProjectServiceUtil projectServiceUtil, ProjectWorkflowService workflowService, @Lazy ProjectFacilityService projectFacilityService, JdbcTemplate jdbcTemplate, ServiceRequestRepository serviceRequestRepository, @Qualifier("objectMapper") ObjectMapper mapper, ProjectNameGenerationService projectNameGenerationService) {
        this.projectRepository = projectRepository;
        this.projectValidator = projectValidator;
        this.projectEnrichment = projectEnrichment;
        this.projectConfiguration = projectConfiguration;
        this.producer = producer;
        this.projectServiceUtil = projectServiceUtil;
        this.workflowService = workflowService;
        this.jdbcTemplate = jdbcTemplate;
        this.serviceRequestRepository = serviceRequestRepository;
        this.mapper = mapper;
        this.objectMapper = new ObjectMapper();
        this.projectFacilityService = projectFacilityService;
        this.projectNameGenerationService = projectNameGenerationService;
    }

    public List<String> validateProjectIds(List<String> productIds) {
        log.trace("Entering validateProjectIds with {} project IDs", productIds != null ? productIds.size() : 0);
        List<String> result = projectRepository.validateIds(productIds, "id");
        log.debug("Validated {} project IDs", result != null ? result.size() : 0);
        log.trace("Exiting validateProjectIds");
        return result;
    }

    public List<Project> findByIds(List<String> projectIds) {
        log.trace("Entering findByIds with {} project IDs", projectIds != null ? projectIds.size() : 0);
        List<Project> result = projectRepository.findById(projectIds);
        log.debug("Found {} projects", result != null ? result.size() : 0);
        log.trace("Exiting findByIds");
        return result;
    }

    public ProjectRequest createProject(ProjectRequest projectRequest) {
        log.trace("Entering createProject");
        log.info("Starting project creation for {} projects", projectRequest.getProjects() != null ? projectRequest.getProjects().size() : 0);
        projectValidator.validateCreateProjectRequest(projectRequest);
        
        RequestInfo requestInfo = projectRequest.getRequestInfo();
        Set<String> generatedNamesInBatch = new HashSet<>();
        processProjectNames(projectRequest, requestInfo, generatedNamesInBatch);

        List<Project> parentProjects = getParentProjects(projectRequest);
        log.debug("Found {} parent projects", parentProjects != null ? parentProjects.size() : 0);
        
        if (parentProjects != null) {
            log.debug("Validating parent projects against database");
            projectValidator.validateParentAgainstDB(projectRequest.getProjects(), parentProjects);
        }
        
        log.info("Enriching projects with project number, IDs and audit details");
        projectEnrichment.enrichProjectOnCreate(projectRequest, parentProjects);
        log.info("Enriched with Project Number, Ids and AuditDetails");
        
        pushProjectsToKafka(projectRequest);
        log.info("Successfully completed project creation for {} projects", projectRequest.getProjects().size());
        log.trace("Exiting createProject");
        return projectRequest;
    }

    private void processProjectNames(ProjectRequest projectRequest, RequestInfo requestInfo, Set<String> generatedNamesInBatch) {
        for (Project project : projectRequest.getProjects()) {
            if (project.getName() == null || project.getName().trim().isEmpty()) {
                processEmptyProjectName(project, requestInfo, generatedNamesInBatch);
            } else {
                processExistingProjectName(project, generatedNamesInBatch);
            }
        }
    }

    private void processEmptyProjectName(Project project, RequestInfo requestInfo, Set<String> generatedNamesInBatch) {
        try {
            ProjectNameResult nameResult = projectNameGenerationService.generateNameAndCheckDuplicate(project, requestInfo);

            if (nameResult == null) {
                log.error("ProjectNameResult is null for project: {}", project.getId());
                throw new CustomException("PROJECT_NAME_GENERATION_FAILED", "Failed to generate project name for project: " + project.getId());
            }

            String generatedName = nameResult.getName();
            if (generatedName != null && !generatedName.trim().isEmpty()) {
                handleGeneratedName(project, generatedName, nameResult, generatedNamesInBatch);
            } else if (shouldSkipNameGeneration(project)) {
                handleSkippedNameGeneration(project);
            } else {
                log.warn("Generated name is null or empty for project: {} with project type: {}",
                        project.getId(), project.getProjectType());
                throw new CustomException("PROJECT_NAME_NULL_OR_EMPTY", "Generated project name is null or empty for project: " + project.getId());
            }
        } catch (Exception e) {
            log.error("Error generating name for project: {}", project.getId(), e);
            throw new CustomException("PROJECT_NAME_GENERATION_FAILED", "Failed to generate project name for project: " + project.getId());
        }
    }

    private void handleGeneratedName(Project project, String generatedName, ProjectNameResult nameResult, Set<String> generatedNamesInBatch) {
        boolean hasBatchDuplicateName = false;
        if (generatedNamesInBatch.contains(generatedName)) {
            log.warn("Duplicate name generated within batch for project: {}. Generated name: {}",
                    project.getId(), generatedName);
            generatedName = generateUniqueBatchName(generatedName, project.getTenantId(), generatedNamesInBatch);
            hasBatchDuplicateName = true;
        }

        project.setName(generatedName);
        generatedNamesInBatch.add(generatedName);

        boolean isDuplicateName = Boolean.TRUE.equals(nameResult.getIsDuplicateName()) || hasBatchDuplicateName;
        Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(
            project.getAdditionalDetails(),
            "isDuplicateName",
            isDuplicateName
        );
        project.setAdditionalDetails(enrichedAdditionalDetails);

        if (isDuplicateName) {
            log.info("Project {} has duplicate name", project.getId());
        } else {
            log.info("Project {} has unique name", project.getId());
        }
    }

    private boolean shouldSkipNameGeneration(Project project) {
        return project.getProjectType() != null &&
               (PROJECT_TYPE_FIELDPLAN.equals(project.getProjectType()) || PROJECT_TYPE_FACILITY.equals(project.getProjectType()));
    }

    private void handleSkippedNameGeneration(Project project) {
        log.info("Skipping name generation for project type: {} for project: {}",
                project.getProjectType(), project.getId());
        Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(
            project.getAdditionalDetails(),
            "isDuplicateName",
            false
        );
        project.setAdditionalDetails(enrichedAdditionalDetails);
    }

    private void processExistingProjectName(Project project, Set<String> generatedNamesInBatch) {
        String existingName = project.getName().trim();
        if (generatedNamesInBatch.contains(existingName)) {
            log.warn("Duplicate name found within batch for project: {}. Name: {}",
                    project.getId(), existingName);
            throw new CustomException("PROJECT_NAME_DUPLICATE_IN_BATCH", "Duplicate project name found within batch: " + existingName);
        }
        generatedNamesInBatch.add(existingName);

        Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(
            project.getAdditionalDetails(),
            "isDuplicateName",
            false
        );
        project.setAdditionalDetails(enrichedAdditionalDetails);
        log.info("Project {} has pre-existing name, marked isDuplicate=false", project.getId());
    }

    private void pushProjectsToKafka(ProjectRequest projectRequest) {
        log.debug("Pushing project request to Kafka topics");
        producer.push(projectConfiguration.getSaveProjectTopic(), projectRequest);
        producer.push(projectConfiguration.getSaveProjectTopicIndexer(), projectRequest);
        log.info("Pushed to kafka");
    }

    /**
     * Generates a unique name within the current batch by appending a numeric suffix
     * Derives the base root, looks up the highest suffix in DB, then chooses the next unused number
     * considering both database and batch names for consistency with global scheme
     * @param baseName The base name to make unique
     * @param tenantId The tenant ID for database lookup
     * @param existingNamesInBatch Set of names already used in this batch
     * @return A unique name that doesn't conflict with existing names in the batch or database
     */
    private String generateUniqueBatchName(String baseName, String tenantId, Set<String> existingNamesInBatch) {
        log.trace("Entering generateUniqueBatchName with baseName: {}, tenantId: {}", baseName, tenantId);
        // Normalize to root: strip a trailing -digits if present
        String baseRoot = baseName.replaceFirst("-\\d+$", "");
        log.debug("Normalized base root: {}", baseRoot);
        int next = 0;

        try {
            String highestExisting = projectRepository.findHighestExistingProjectName(baseRoot, tenantId);
            if (highestExisting != null) {
                if (highestExisting.equals(baseRoot)) {
                    next = 1;
                } else if (highestExisting.startsWith(baseRoot + "-")) {
                    String suffix = highestExisting.substring((baseRoot + "-").length());
                    try {
                        next = Integer.parseInt(suffix) + 1;
                    } catch (NumberFormatException ignored) {
                        next = 1;
                    }
                }
            } else {
                next = 1;
            }
        } catch (Exception e) {
            log.warn("Falling back to batch-only uniquing for base '{}': {}", baseRoot, e.getMessage());
            next = 1;
        }

        String candidate = (next <= 0) ? baseRoot + "-1" : baseRoot + "-" + next;
        int guard = 0;

        while (existingNamesInBatch.contains(candidate)) {
            next++;
            candidate = baseRoot + "-" + next;
            if (++guard > 1000) {
                throw new CustomException("PROJECT_NAME_GENERATION_FAILED",
                        "Unable to generate unique batch name after 1000 attempts for: " + baseRoot);
            }
        }

        log.info("Generated unique batch name: {} from base: {}", candidate, baseRoot);
        log.trace("Exiting generateUniqueBatchName");
        return candidate;
    }

    /**
     * Search for projects based on various criteria
     *
     * @param isAncestorProjectId When true, treats the project IDs in the search criteria as ancestor project IDs
     *                            and returns all projects (including children) under these ancestors
     */
    public List<Project> searchProject(
            ProjectRequest project,
            Integer limit,
            Integer offset,
            String tenantId,
            Long lastChangedSince,
            Boolean includeDeleted,
            Boolean includeAncestors,
            Boolean includeDescendants,
            Long createdFrom,
            Long createdTo,
            boolean isAncestorProjectId
    ) {
        log.trace("Entering searchProject with limit: {}, offset: {}, tenantId: {}", limit, offset, tenantId);
        log.info("Searching projects with criteria");
        projectValidator.validateSearchProjectRequest(project, limit, offset, tenantId, createdFrom, createdTo);
        log.debug("Search criteria validated, fetching projects from repository");
        List<Project> projects = projectRepository.getProjects(
                project,
                limit,
                offset,
                tenantId,
                lastChangedSince,
                includeDeleted,
                includeAncestors,
                includeDescendants,
                createdFrom,
                createdTo,
                isAncestorProjectId
        );
        log.debug("Found {} projects matching search criteria", projects != null ? projects.size() : 0);
        log.info("Project search completed successfully");
        log.trace("Exiting searchProject");
        return projects;
    }

    public List<Project> searchProject(ProjectSearchRequest projectSearchRequest, @Valid ProjectSearchURLParams urlParams, List<String> workflowStatuses, @Valid ProjectSortCriteria sortCriteria) throws Exception {
        log.trace("Entering searchProject (v2)");
        log.info("Starting project search with v2 API");
        projectValidator.validateSearchV2ProjectRequest(projectSearchRequest, urlParams, sortCriteria);
        log.debug("Search request validated, fetching projects");
        List<Project> projects = projectRepository.getProjects(projectSearchRequest, urlParams, workflowStatuses, sortCriteria);
        log.debug("Retrieved {} projects from repository", projects != null ? projects.size() : 0);
        
        // Get count of project type = Facility for each project type FieldPlan
        if(projectSearchRequest.getProject() !=null && projectSearchRequest.getProject().getProjectTypeId() !=null
                && projectSearchRequest.getProject().getProjectTypeId().equals(PROJECT_TYPE_FIELDPLAN)) {
            log.debug("Enriching FieldPlan projects with facility counts");
            projects = getCountProjectTypeFacilities(projects, projectSearchRequest, urlParams, workflowStatuses, sortCriteria);
        }

        // Get facility of project type = Facility for each project type Facility
        if(projectSearchRequest.getProject() !=null && projectSearchRequest.getProject().getProjectTypeId() !=null
                && projectSearchRequest.getProject().getProjectTypeId().equals(PROJECT_TYPE_FACILITY)) {
            log.debug("Enriching Facility projects with facility details");
            getFacilityProject(projects, projectSearchRequest.getRequestInfo());
        }

        // Enrich all projects with HLS (Health Center) count
        if (projectSearchRequest.getProject() != null
                && projectSearchRequest.getProject().getSubProjectTypeId() != null
                && PROJECT_SUB_TYPE.equalsIgnoreCase(projectSearchRequest.getProject().getSubProjectTypeId())) {
            log.debug("Enriching projects with HLS count");
            projects = enrichProjectsWithHlsCount(projects, projectSearchRequest.getRequestInfo());
        }

        log.info("Project search v2 completed successfully with {} projects", projects != null ? projects.size() : 0);
        log.trace("Exiting searchProject (v2)");
        return projects;
    }

    public List<Project> getFacilityProject(List<Project> listProjects, RequestInfo requestInfo) throws Exception {
        log.trace("Entering getFacilityProject for {} projects", listProjects != null ? listProjects.size() : 0);
        log.info("Enriching {} projects with facility details", listProjects != null ? listProjects.size() : 0);
        log.debug("Fetching boundary data");
        Map<String, BoundaryV2> listBlock = boundaryV2Util.getBoundaryByCode();
        log.debug("Found {} boundary entries", listBlock != null ? listBlock.size() : 0);
        for (Project project : listProjects) {
            log.debug("Processing facility enrichment for project: {}", project.getId());
            List<String> listProjectId = new ArrayList<>();
            listProjectId.add(project.getId());
            ProjectFacilitySearch projectFacilitySearch = ProjectFacilitySearch.builder().projectId(listProjectId).facilityId(null).build();
            ProjectFacilitySearchRequest projectFacilitySearchRequest = ProjectFacilitySearchRequest.builder().projectFacility(projectFacilitySearch).requestInfo(requestInfo).build();
            SearchResponse<ProjectFacility> searchResponse = projectFacilityService.search(
                    projectFacilitySearchRequest,
                    100,
                    0,
                    project.getTenantId(),
                    null,
                    false);

            if (searchResponse != null && searchResponse.getResponse() != null && !searchResponse.getResponse().isEmpty()) {
                log.debug("Found facility for project: {}, enriching with systemCode", project.getId());
                ProjectFacility projectFacility = searchResponse.getResponse().get(0);
                Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(project.getAdditionalDetails(), "systemCode", "AC_OFF_GRID");
                project.setAdditionalDetails(enrichedAdditionalDetails);
            } else {
                log.debug("No facility found for project: {}", project.getId());
            }

            // Get district and state for project type facility
            if(listBlock != null){
                log.debug("Enriching project: {} with district and state from boundary", project.getId());
                Object additionalDetails = project.getAdditionalDetails();
                Address address = project.getAddress();
                if(address !=null){
                    String boundaryCode = address.getBoundary();
                    if(boundaryCode != null){
                        BoundaryV2 boundary = listBlock.get(boundaryCode);
                        if(boundary != null){
                            Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(additionalDetails, "state", boundary.getState());
                            project.setAdditionalDetails(enrichedAdditionalDetails);
                            additionalDetails = project.getAdditionalDetails();
                            enrichedAdditionalDetails = mergeIntoAdditionalDetails(additionalDetails, "district", boundary.getDistrict());
                            project.setAdditionalDetails(enrichedAdditionalDetails);
                        }
                    }
                }
            }
        }
        log.info("Successfully enriched {} projects with facility details", listProjects.size());
        log.trace("Exiting getFacilityProject");
        return listProjects;
    }

    public List<Project> getCountProjectTypeFacilities(List<Project> listProjects, ProjectSearchRequest projectSearchRequest, @Valid ProjectSearchURLParams urlParams, List<String> workflowStatuses, @Valid ProjectSortCriteria sortCriteria) throws Exception {
        log.trace("Entering getCountProjectTypeFacilities for {} projects", listProjects != null ? listProjects.size() : 0);
        log.info("Enriching {} projects with facility counts", listProjects != null ? listProjects.size() : 0);
        for (Project project : listProjects) {
            log.debug("Processing facility count enrichment for project: {}", project.getId());
            ProjectSearch copyProject = ProjectSearch.builder()
                    .parent(projectSearchRequest.getProject().getParent())
                    .projectTypeId("Facility")
                    .build();

            ProjectSearchRequest projectSearchRequest1 = ProjectSearchRequest.builder().project(copyProject).requestInfo(projectSearchRequest.getRequestInfo()).build();
            projectSearchRequest1.getProject().setProjectTypeId("Facility");
            projectSearchRequest1.getProject().setParent(project.getId());
            log.debug("Counting facilities for project: {}", project.getId());
            Integer count = countAllProjects(projectSearchRequest1, urlParams, workflowStatuses);
            log.debug("Found {} facilities for project: {}", count, project.getId());
            Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(project.getAdditionalDetails(), "countProjectFacilities", count);
            project.setAdditionalDetails(enrichedAdditionalDetails);
            log.debug("Fetching status aggregations for project: {}", project.getId());
            List<ProjectStatusAgregation> statusAgregations = getStatusProjectsAgregation(project.getId());
            log.debug("Found {} status aggregations for project: {}", statusAgregations != null ? statusAgregations.size() : 0, project.getId());
            enrichedAdditionalDetails = mergeListIntoAdditionalDetails(project.getAdditionalDetails(), "statusAgregation", statusAgregations);
            project.setAdditionalDetails(enrichedAdditionalDetails);
        }
        log.info("Successfully enriched {} projects with facility counts", listProjects.size());
        log.trace("Exiting getCountProjectTypeFacilities");
        return listProjects;
    }

    /**
     * Enriches all projects with HLS (Health Center) count by searching for linked ProjectFacility entities
     * and adding the count to each project's additionalDetails
     *
     * @param projects List of projects to enrich
     * @param requestInfo Request information for the search
     * @return List of projects with HLS count added to additionalDetails
     * @throws Exception if there's an error during the enrichment process
     */
    public List<Project> enrichProjectsWithHlsCount(List<Project> projects, RequestInfo requestInfo) throws Exception {
        if (projects == null || projects.isEmpty()) {
            return projects;
        }

        log.info("Enriching {} projects with HLS count", projects.size());

        for (Project project : projects) {
            enrichProjectWithHlsCount(project, requestInfo);
        }

        log.info("Successfully enriched all projects with HLS count");
        return projects;
    }

    private void enrichProjectWithHlsCount(Project project, RequestInfo requestInfo) {
        try {
            int hlsCount = getHlsCountForProject(project, requestInfo);
            addHlsCountToProject(project, hlsCount);
            log.debug("Project {} enriched with HLS count: {}", project.getId(), hlsCount);
        } catch (Exception e) {
            log.error("Error enriching project {} with HLS count: {}", project.getId(), e.getMessage(), e);
            addHlsCountToProject(project, 0);
        }
    }

    private int getHlsCountForProject(Project project, RequestInfo requestInfo) throws Exception {
        ProjectFacilitySearchRequest searchRequest = buildProjectFacilitySearchRequest(project, requestInfo);
        
        SearchResponse<ProjectFacility> searchResponse = projectFacilityService.search(
                searchRequest,
                1000,
                0,
                project.getTenantId(),
                null,
                false
        );

        if (searchResponse != null && searchResponse.getResponse() != null) {
            return searchResponse.getResponse().size();
        }
        return 0;
    }

    private ProjectFacilitySearchRequest buildProjectFacilitySearchRequest(Project project, RequestInfo requestInfo) {
        List<String> projectIds = new ArrayList<>();
        projectIds.add(project.getId());

        ProjectFacilitySearch projectFacilitySearch = ProjectFacilitySearch.builder()
                .projectId(projectIds)
                .facilityId(null)
                .build();

        return ProjectFacilitySearchRequest.builder()
                .projectFacility(projectFacilitySearch)
                .requestInfo(requestInfo)
                .build();
    }

    private void addHlsCountToProject(Project project, int hlsCount) {
        Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(
                project.getAdditionalDetails(),
                "hlsCount",
                hlsCount
        );
        project.setAdditionalDetails(enrichedAdditionalDetails);
    }

    public ProjectRequest updateProject(ProjectRequest request) {
        log.trace("Entering updateProject");
        log.info("Starting project update for {} projects", request.getProjects() != null ? request.getProjects().size() : 0);
        /*
         * Validate the update project request
         */
        projectValidator.validateUpdateProjectRequest(request);
        log.info("Update project request validated");

        /*
         * Search for projects based on project IDs provided in the request
         */
        log.debug("Fetching existing projects from database for update");
        List<Project> projectsFromDB = searchProject(
                getSearchProjectRequest(request.getProjects(), request.getRequestInfo(), false),
                projectConfiguration.getMaxLimit(), projectConfiguration.getDefaultOffset(),
                request.getProjects().get(0).getTenantId(), null, false, false, false, null, null, false
        );
        log.info("Fetched projects for update request");
        log.debug("Found {} existing projects in database", projectsFromDB != null ? projectsFromDB.size() : 0);

        /*
         * Validate the update project request against the projects fetched from the database
         */
        log.debug("Validating update request against database state");
        projectValidator.validateUpdateAgainstDB(request.getProjects(), projectsFromDB);

        /*
         * Process each project in the update request
         */
        log.info("Processing {} projects for update", request.getProjects().size());
        for (Project project : request.getProjects()) {
            processProjectUpdate(request, project, projectsFromDB);
        }

        log.info("Successfully completed project update for {} projects", request.getProjects().size());
        log.trace("Exiting updateProject");
        return request;
    }

    private void processProjectUpdate(ProjectRequest request, Project project, List<Project> projectsFromDB) {
        log.trace("Entering processProjectUpdate for project: {}", project.getId());
        /*
         * Convert project ID to string for comparison
         */
        String projectId = String.valueOf(project.getId());
        log.debug("Processing update for project ID: {}", projectId);

        /*
         * Find the project from the database that matches the current project ID
         */
        Project projectFromDB = findProjectById(projectId, projectsFromDB);
        boolean isCascadingProjectDateUpdate = request.isCascadingProjectDateUpdate();
        log.debug("Cascading project date update flag: {}", isCascadingProjectDateUpdate);

        if (projectFromDB != null) {
            log.debug("Found existing project in database, proceeding with update");
            /*
             * Check if geography details (boundary codes) have changed and unlink facilities if needed
             */
            handleFacilityUnlinkingOnGeographyChange(request, project, projectFromDB);

            /*
             * Merge additional details of the project from the request and project from DB
             */
            log.debug("Merging additional details from request and database");
            projectServiceUtil.mergeAdditionalDetails(project, projectFromDB);

            /*
             * Handle cases where cascading project date update is true
             */
            if (isCascadingProjectDateUpdate) {
                log.info("Handling cascading project date update");
                handleUpdateProjectDates(request, project, projectFromDB);
            }
            /*
             * Handle cases for normal update flow
             */
            else {
                log.info("Handling normal project update");
                handleNormalUpdate(request, project, projectFromDB);
            }
        } else {
            log.warn("Project not found in database for ID: {}", projectId);
        }
        log.trace("Exiting processProjectUpdate for project: {}", projectId);
    }

    private Project findProjectById(String projectId, List<Project> projectsFromDB) {
        log.trace("Entering findProjectById for project ID: {}", projectId);
        /*
         * Find and return the project with the matching ID from the list of projects fetched from the database
         */
        Project result = projectsFromDB.stream()
                .filter(p -> projectId.equals(String.valueOf(p.getId())))
                .findFirst()
                .orElse(null);
        log.debug("Project lookup result: {}", result != null ? "found" : "not found");
        log.trace("Exiting findProjectById");
        return result;
    }


    private void handleNormalUpdate(ProjectRequest request, Project project, Project projectFromDB) {
        log.trace("Entering handleNormalUpdate for project: {}", project.getId());
        /*
         * Ensure that start and end dates are not being updated when flag is false
         */
        if (!project.getStartDate().equals(projectFromDB.getStartDate()) ||
                !project.getEndDate().equals(projectFromDB.getEndDate())) {
            log.error("Attempted to update date range without cascading flag for project: {}", project.getId());
            throw new CustomException("PROJECT_CASCADE_UPDATE_DATE_ERROR",
                    "Can't Update Date Range if Cascade Project Date Update  false");
        }

        /*
         * Handle project name regeneration if needed
         */
        log.debug("Checking if project name update is needed");
        handleProjectNameUpdate(request, project, projectFromDB);

        /*
         * Enrich the project with values other than the start, end dates, and AdditionalDetails,
         * and push the update to the message broker
         */
        log.info("Enriching project for update");
        projectEnrichment.enrichProjectOnUpdate(request, project, projectFromDB);
        log.debug("Pushing project update to Kafka topics");
        producer.push(projectConfiguration.getUpdateProjectTopic(), request);
        producer.push(projectConfiguration.getUpdateProjectTopicIndexer(), request);
        log.info("Successfully completed normal update for project: {}", project.getId());
        log.trace("Exiting handleNormalUpdate");
    }

    private void handleUpdateProjectDates(ProjectRequest request, Project project, Project projectFromDB) {
        log.trace("Entering handleUpdateProjectDates for project: {}", project.getId());
        
        ProjectDateUpdateState originalState = saveOriginalProjectState(projectFromDB);
        updateProjectDatesTemporarily(project, projectFromDB);
        validateCascadingUpdate(projectFromDB, project);
        restoreOriginalProjectState(projectFromDB, originalState);

        handleProjectNameUpdate(request, project, projectFromDB);
        projectEnrichment.enrichProjectRequestOnUpdate(project, projectFromDB, request.getRequestInfo());
        
        processCascadingDateUpdate(request, project);
        log.info("Successfully completed cascading date update for project: {}", project.getId());
        log.trace("Exiting handleUpdateProjectDates");
    }

    private ProjectDateUpdateState saveOriginalProjectState(Project projectFromDB) {
        log.debug("Saved original project dates - start: {}, end: {}", 
                projectFromDB.getStartDate(), projectFromDB.getEndDate());
        return new ProjectDateUpdateState(
                projectFromDB.getStartDate(),
                projectFromDB.getEndDate(),
                projectFromDB.getAdditionalDetails(),
                projectFromDB.getAuditDetails()
        );
    }

    private void updateProjectDatesTemporarily(Project project, Project projectFromDB) {
        projectFromDB.setStartDate(project.getStartDate());
        projectFromDB.setEndDate(project.getEndDate());
        projectFromDB.setAdditionalDetails(project.getAdditionalDetails());
        projectFromDB.setAuditDetails(project.getAuditDetails());
    }

    private void validateCascadingUpdate(Project projectFromDB, Project project) {
        if (!isValidCascadingUpdate(projectFromDB, project)) {
            throw new CustomException(
                    "PROJECT_CASCADE_UPDATE_ERROR",
                    "Can only update Project dates, name, and additional details if cascade Project date update true"
            );
        }
    }

    private void restoreOriginalProjectState(Project projectFromDB, ProjectDateUpdateState originalState) {
        projectFromDB.setStartDate(originalState.getStartDate());
        projectFromDB.setEndDate(originalState.getEndDate());
        projectFromDB.setAdditionalDetails(originalState.getAdditionalDetails());
        projectFromDB.setAuditDetails(originalState.getAuditDetails());
    }

    private void processCascadingDateUpdate(ProjectRequest request, Project project) {
        log.debug("Checking and enriching cascading project dates");
        checkAndEnrichCascadingProjectDates(request, project);
        log.debug("Pushing cascading date update to Kafka topics");
        producer.push(projectConfiguration.getUpdateProjectDateTopic(), request);
        producer.push(projectConfiguration.getUpdateProjectTopicIndexer(), request);
    }

    // Helper class for project date update state
    private static class ProjectDateUpdateState {
        private final Long startDate;
        private final Long endDate;
        private final Object additionalDetails;
        private final AuditDetails auditDetails;

        public ProjectDateUpdateState(Long startDate, Long endDate, Object additionalDetails, AuditDetails auditDetails) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.additionalDetails = additionalDetails;
            this.auditDetails = auditDetails;
        }

        public Long getStartDate() { return startDate; }
        public Long getEndDate() { return endDate; }
        public Object getAdditionalDetails() { return additionalDetails; }
        public AuditDetails getAuditDetails() { return auditDetails; }
    }


    /**
     * Handles project name regeneration during updates
     * Only regenerates name if the underlying data that affects the name has changed
     */
    private void handleProjectNameUpdate(ProjectRequest request, Project project, Project projectFromDB) {
        try {
            if (shouldSkipNameRegeneration(project)) {
                return;
            }

            if (!hasNameAffectingDataChanged(project, projectFromDB)) {
                log.info("No name-affecting data changed for project: {}, keeping existing name: {}", 
                        project.getId(), projectFromDB.getName());
                return;
            }

            updateProjectNameIfNeeded(request, project, projectFromDB);
            
        } catch (Exception e) {
            log.error("Error handling project name update for project: {}", project.getId(), e);
            // Don't throw exception - continue with update even if name generation fails
        }
    }

    private boolean shouldSkipNameRegeneration(Project project) {
        String projectType = project.getProjectType();
        if (PROJECT_TYPE_FIELDPLAN.equals(projectType) || PROJECT_TYPE_FACILITY.equals(projectType)) {
            log.info("Skipping name regeneration for project type: {} during update", projectType);
            return true;
        }
        return false;
    }

    private void updateProjectNameIfNeeded(ProjectRequest request, Project project, Project projectFromDB) {
        ProjectNameResult nameResult = projectNameGenerationService.generateNameAndCheckDuplicate(
                project, request.getRequestInfo(), project.getId());
        
        if (nameResult == null || nameResult.getName() == null) {
            log.warn("Could not generate new name for project: {} during update", project.getId());
            return;
        }

        String newBaseName = nameResult.getName();
        String existingName = projectFromDB.getName();
        String existingBaseName = extractBaseNameFromExistingName(existingName);

        if (!newBaseName.equals(existingBaseName)) {
            applyNameUpdate(project, nameResult, existingName, newBaseName);
        } else {
            log.info("Project name unchanged. Existing: {}, New base: {}", existingName, newBaseName);
        }
    }

    private void applyNameUpdate(Project project, ProjectNameResult nameResult, String existingName, String newBaseName) {
        log.info("Project name needs update. Existing: {}, New: {}", existingName, newBaseName);
        project.setName(nameResult.getName());
        log.info("Updated project name to: {}", nameResult.getName());
        
        Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(
            project.getAdditionalDetails(),
            "isDuplicateName",
            nameResult.getIsDuplicateName()
        );
        project.setAdditionalDetails(enrichedAdditionalDetails);
    }

    /**
     * Checks if any data that affects project name generation has changed
     * Name is affected by: startDate, endDate, projectType, address.boundary (state)
     */
    private boolean hasNameAffectingDataChanged(Project project, Project projectFromDB) {
        // Check if start date changed
        if (!Objects.equals(project.getStartDate(), projectFromDB.getStartDate())) {
            log.info("Start date changed for project: {} - name regeneration needed", project.getId());
            return true;
        }
        
        // Check if end date changed
        if (!Objects.equals(project.getEndDate(), projectFromDB.getEndDate())) {
            log.info("End date changed for project: {} - name regeneration needed", project.getId());
            return true;
        }
        
        // Check if project type changed
        if (!Objects.equals(project.getProjectType(), projectFromDB.getProjectType())) {
            log.info("Project type changed for project: {} - name regeneration needed", project.getId());
            return true;
        }
        
        // Check if address boundary (state) changed
        String currentBoundary = project.getAddress() != null ? project.getAddress().getBoundary() : null;
        String existingBoundary = projectFromDB.getAddress() != null ? projectFromDB.getAddress().getBoundary() : null;
        if (!Objects.equals(currentBoundary, existingBoundary)) {
            log.info("Address boundary changed for project: {} - name regeneration needed", project.getId());
            return true;
        }
        
        log.info("No name-affecting data changed for project: {}", project.getId());
        return false;
    }

    /**
     * Validates if the cascading update only modifies allowed fields
     * Allowed fields: startDate, endDate, name, additionalDetails.geographyDetails, auditDetails
     * Read-only fields: projectType, state, justificationCode
     */
    private boolean isValidCascadingUpdate(Project projectFromDB, Project project) {
        // Check if only allowed fields are being updated
        return Objects.equals(projectFromDB.getId(), project.getId()) &&
               Objects.equals(projectFromDB.getTenantId(), project.getTenantId()) &&
               Objects.equals(projectFromDB.getProjectType(), project.getProjectType()) &&
               isValidAddressUpdate(projectFromDB.getAddress(), project.getAddress()) &&
               isValidAdditionalDetailsUpdate(projectFromDB.getAdditionalDetails(), project.getAdditionalDetails());
        // Note: We allow startDate, endDate, name, additionalDetails.geographyDetails, and auditDetails to be different
    }

    /**
     * Validates if only allowed fields in address are being updated
     * Read-only: boundary (state cannot be changed)
     * Other fields can be different (id, clientReferenceId, etc.)
     */
    private boolean isValidAddressUpdate(Address originalAddress, Address newAddress) {
        if (originalAddress == null && newAddress == null) {
            return true;
        }
        if (originalAddress == null || newAddress == null) {
            return false;
        }
        
        // Only validate that the boundary (state) hasn't changed
        return Objects.equals(originalAddress.getBoundary(), newAddress.getBoundary());
    }

    /**
     * Validates if only allowed fields in additionalDetails are being updated
     * Allowed: geographyDetails (districts, blocks)
     * Read-only: justificationCode field
     */
    private boolean isValidAdditionalDetailsUpdate(Object originalAdditionalDetails, Object newAdditionalDetails) {
        if (originalAdditionalDetails == null && newAdditionalDetails == null) {
            return true;
        }
        if (originalAdditionalDetails == null || newAdditionalDetails == null) {
            return false;
        }

        try {
            // Convert to JsonNode for easier comparison
            JsonNode originalNode = mapper.valueToTree(originalAdditionalDetails);
            JsonNode newNode = mapper.valueToTree(newAdditionalDetails);

            // Check if justificationCode is unchanged (read-only)
            JsonNode originalJustification = originalNode.get("justificationCode");
            JsonNode newJustification = newNode.get("justificationCode");
            if (!Objects.equals(originalJustification, newJustification)) {
                log.warn("justificationCode cannot be changed during cascading update");
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating additionalDetails update", e);
            return false;
        }
    }

    /**
     * Extracts base name from existing name by removing numeric suffixes
     * Example: "E4H-TS-2023-25-5" -> "E4H-TS-2023-25"
     */
    private String extractBaseNameFromExistingName(String existingName) {
        if (existingName == null || existingName.trim().isEmpty()) {
            return existingName;
        }
        
        // Remove trailing numeric suffix pattern: -digits
        return existingName.replaceFirst("-\\d+$", "");
    }

    /**
     * Handles facility unlinking when geography details (boundary codes) are changed
     * Only processes unlinking when geographyDetails is explicitly present in the request
     * Only allows unlinking for Draft projects (status = null)
     */
    private void handleFacilityUnlinkingOnGeographyChange(ProjectRequest request, Project project, Project projectFromDB) {
        try {
            // Guard: Only process unlinking if geographyDetails is explicitly present in the request
            if (!hasGeographyDetailsInRequest(project.getAdditionalDetails())) {
                log.debug("No geographyDetails in request for project: {} - skipping facility unlinking", project.getId());
                return;
            }

            // STATUS CHECK: Only allow facility unlinking for Draft projects (status = null or missing)
            String projectStatus = getProjectStatus(project);
            if (!isDraftProject(projectStatus)) {
                log.info("Project {} has status '{}' - facility unlinking not allowed. Only Draft projects (status=null or missing) can unlink facilities.", 
                        project.getId(), projectStatus);
                return;
            }

            // Extract boundary codes from old and new geography details
            Set<String> oldBoundaryCodes = extractBoundaryCodesFromGeographyDetails(projectFromDB.getAdditionalDetails());
            Set<String> newBoundaryCodes = extractBoundaryCodesFromGeographyDetails(project.getAdditionalDetails());

            // Check if boundary codes have changed
            if (!oldBoundaryCodes.equals(newBoundaryCodes)) {
                log.info("Geography details changed for project: {}. Old boundaries: {}, New boundaries: {}",
                        project.getId(), oldBoundaryCodes, newBoundaryCodes);

                // Unlink facilities that are no longer associated with the new boundary codes
                unlinkProjectFacilities(project.getId(), project.getTenantId(), request.getRequestInfo(), newBoundaryCodes);
            } else {
                log.debug("Geography details unchanged for project: {} - no facility unlinking needed", project.getId());
            }
        } catch (Exception e) {
            log.error("Error handling facility unlinking for project: {}", project.getId(), e);
            // Don't throw exception - continue with update even if facility unlinking fails
        }
    }

    /**
     * Checks if geographyDetails is explicitly present in the request
     * This prevents unlinking facilities when geography wasn't actually modified
     */
    private boolean hasGeographyDetailsInRequest(Object additionalDetails) {
        if (additionalDetails == null) {
            return false;
        }

        try {
            JsonNode additionalDetailsNode = mapper.valueToTree(additionalDetails);
            // Return true if the key is explicitly present in payload (even if null)
            return additionalDetailsNode != null && !additionalDetailsNode.isNull()
                    && additionalDetailsNode.has("geographyDetails");
        } catch (Exception e) {
            log.error("Error checking for geographyDetails in request", e);
            return false;
        }
    }

    /**
     * Gets the project status from additionalDetails
     * Returns null if status is not present, null, or if additionalDetails is empty
     */
    private String getProjectStatus(Project project) {
        try {
            Object additionalDetails = project.getAdditionalDetails();
            if (additionalDetails == null) {
                return null; // No additionalDetails = Draft status
            }
            
            JsonNode additionalDetailsNode = mapper.valueToTree(additionalDetails);
            
            // If additionalDetails is empty or doesn't have status field, it's Draft
            if (additionalDetailsNode == null || additionalDetailsNode.isNull() || !additionalDetailsNode.has("status")) {
                return null; // No status field = Draft status
            }
            
            JsonNode statusNode = additionalDetailsNode.get("status");
            return (statusNode != null && !statusNode.isNull()) ? statusNode.asText() : null;
        } catch (Exception e) {
            log.error("Error getting project status for project: {}", project.getId(), e);
            return null; // Default to Draft on error
        }
    }

    /**
     * Checks if the project is in Draft status
     * Draft status is indicated by status = null
     */
    private boolean isDraftProject(String projectStatus) {
        return projectStatus == null;
    }

    /**
     * Extracts boundary codes from geography details in additional details
     */
    private Set<String> extractBoundaryCodesFromGeographyDetails(Object additionalDetails) {
        Set<String> boundaryCodes = new HashSet<>();

        if (additionalDetails == null) {
            return boundaryCodes;
        }

        try {
            JsonNode additionalDetailsNode = mapper.valueToTree(additionalDetails);
            JsonNode geographyDetails = additionalDetailsNode.get("geographyDetails");

            if (geographyDetails != null) {
                // Extract boundary codes from blocks
                JsonNode blocks = geographyDetails.get("blocks");
                if (blocks != null && blocks.isArray()) {
                    for (JsonNode block : blocks) {
                        JsonNode code = block.get("code");
                        if (code != null && !code.isNull()) {
                            boundaryCodes.add(code.asText());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting boundary codes from geography details", e);
        }

        return boundaryCodes;
    }

    /**
     * Unlinks facilities that are no longer associated with the project's new boundary codes
     */
    private void unlinkProjectFacilities(String projectId, String tenantId, RequestInfo requestInfo, Set<String> newBoundaryCodes) {
        try {
            log.info("Starting selective facility unlinking for project: {} with new boundary codes: {}", projectId, newBoundaryCodes);
            
            List<ProjectFacility> linkedProjectFacilities = getFacilitiesLinkedToProject(projectId, tenantId, requestInfo);
            if (linkedProjectFacilities.isEmpty()) {
                log.info("No facilities currently linked to project: {}", projectId);
                return;
            }
            
            Set<String> facilitiesInNewBoundaries = getFacilitiesByBoundaryCodes(newBoundaryCodes, tenantId, requestInfo);
            if (shouldSkipUnlinking(newBoundaryCodes, facilitiesInNewBoundaries, projectId)) {
                return;
            }

            List<ProjectFacility> facilitiesToUnlink = findFacilitiesToUnlink(linkedProjectFacilities, facilitiesInNewBoundaries);
            if (facilitiesToUnlink.isEmpty()) {
                log.info("No facilities need to be unlinked for project: {}", projectId);
                return;
            }
            
            log.info("Found {} facilities to unlink out of {} linked facilities for project: {}", 
                    facilitiesToUnlink.size(), linkedProjectFacilities.size(), projectId);
            
            performFacilityUnlinking(facilitiesToUnlink, requestInfo, projectId);
            
        } catch (Exception e) {
            log.error("Error unlinking facilities for project: {}", projectId, e);
            throw new CustomException("FACILITY_UNLINKING_FAILED", 
                    "Failed to unlink facilities for project: " + projectId + ". Error: " + e.getMessage());
        }
    }

    private boolean shouldSkipUnlinking(Set<String> newBoundaryCodes, Set<String> facilitiesInNewBoundaries, String projectId) {
        if (!newBoundaryCodes.isEmpty() && facilitiesInNewBoundaries.isEmpty()) {
            log.warn("Facility lookup returned 0 results for non-empty boundaries {}. Skipping unlink to avoid accidental data loss for project: {}",
                    newBoundaryCodes, projectId);
            return true;
        }
        return false;
    }

    private List<ProjectFacility> findFacilitiesToUnlink(List<ProjectFacility> linkedFacilities, Set<String> facilitiesInNewBoundaries) {
        return linkedFacilities.stream()
                .filter(projectFacility -> !facilitiesInNewBoundaries.contains(projectFacility.getFacilityId()))
                .collect(Collectors.toList());
    }

    private void performFacilityUnlinking(List<ProjectFacility> facilitiesToUnlink, RequestInfo requestInfo, String projectId) {
        List<ProjectFacility> facilitiesToUpdate = facilitiesToUnlink.stream()
                .map(this::createDeletedFacility)
                .collect(Collectors.toList());
        
        ProjectFacilityBulkRequest updateRequest = ProjectFacilityBulkRequest.builder()
                .requestInfo(requestInfo)
                .projectFacilities(facilitiesToUpdate)
                .build();
        
        projectFacilityService.update(updateRequest, true);
        log.info("Successfully unlinked {} facilities for project: {} by setting isDeleted=true", facilitiesToUpdate.size(), projectId);
    }

    private ProjectFacility createDeletedFacility(ProjectFacility projectFacility) {
        return ProjectFacility.builder()
                .id(projectFacility.getId())
                .projectId(projectFacility.getProjectId())
                .facilityId(projectFacility.getFacilityId())
                .tenantId(projectFacility.getTenantId())
                .isDeleted(true)
                .rowVersion(projectFacility.getRowVersion())
                .auditDetails(projectFacility.getAuditDetails())
                .build();
    }

    /**
     * Gets all facilities currently linked to a project
     */
    private List<ProjectFacility> getFacilitiesLinkedToProject(String projectId, String tenantId, RequestInfo requestInfo) {
        try {
            List<String> projectIds = new ArrayList<>();
            projectIds.add(projectId);

            ProjectFacilitySearch projectFacilitySearch = ProjectFacilitySearch.builder()
                    .projectId(projectIds)
                    .facilityId(null)
                    .build();

            ProjectFacilitySearchRequest projectFacilitySearchRequest = ProjectFacilitySearchRequest.builder()
                    .projectFacility(projectFacilitySearch)
                    .requestInfo(requestInfo)
                    .build();

            SearchResponse<ProjectFacility> searchResponse = projectFacilityService.search(
                    projectFacilitySearchRequest,
                    1000, // Large limit to get all facilities
                    0,
                    tenantId,
                    null,
                    false
            );

            return (searchResponse != null && searchResponse.getResponse() != null)
                    ? searchResponse.getResponse()
                    : new ArrayList<>();

        } catch (Exception e) {
            log.error("Error getting facilities linked to project: {}", projectId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets all facility IDs associated with the given boundary codes
     */
    private Set<String> getFacilitiesByBoundaryCodes(Set<String> boundaryCodes, String tenantId, RequestInfo requestInfo) {
        Set<String> facilityIds = new HashSet<>();

        if (boundaryCodes.isEmpty()) {
            return facilityIds;
        }

        try {
            // Search facilities by boundary codes
            for (String boundaryCode : boundaryCodes) {
                Set<String> facilitiesForBoundary = searchFacilitiesByBoundaryCode(boundaryCode, tenantId, requestInfo);
                facilityIds.addAll(facilitiesForBoundary);
            }

            log.info("Found {} unique facilities across {} boundary codes", facilityIds.size(), boundaryCodes.size());

        } catch (Exception e) {
            log.error("Error getting facilities by boundary codes: {}", boundaryCodes, e);
        }

        return facilityIds;
    }

    /**
     * Searches facilities by a specific boundary code
     */
    private Set<String> searchFacilitiesByBoundaryCode(String boundaryCode, String tenantId, RequestInfo requestInfo) {
        Set<String> facilityIds = new HashSet<>();

        try {
            // Build facility search URL with boundary code filter
            StringBuilder facilitySearchUrl = new StringBuilder();
            facilitySearchUrl.append(projectConfiguration.getFacilityServiceHost())
                    .append(projectConfiguration.getFacilityServiceSearchUrlV2())
                    .append("?tenantId=")
                    .append(tenantId)
                    .append("&boundaryCode=")
                    .append(boundaryCode);

            log.debug("Searching facilities for boundary code: {} with URL: {}", boundaryCode, facilitySearchUrl);

            // Call facility service
            Object response = serviceRequestRepository.fetchResult(facilitySearchUrl);

            if (response != null) {
                FacilitySearchResponse facilitySearchResponse = mapper.convertValue(response, FacilitySearchResponse.class);

                if (facilitySearchResponse != null && facilitySearchResponse.getFacilities() != null) {
                    facilityIds = facilitySearchResponse.getFacilities().stream()
                            .map(Facility::getFacilityId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    log.debug("Found {} facilities for boundary code: {}", facilityIds.size(), boundaryCode);
                }
            }

        } catch (Exception e) {
            log.error("Error searching facilities for boundary code: {}", boundaryCode, e);
        }

        return facilityIds;
    }

    /**
     * Checks and enriches cascading project dates.
     *
     * @param request The project request containing projects and request information.
     */
    private void checkAndEnrichCascadingProjectDates(ProjectRequest request, Project project) {
        /*
         * Retrieve tenant ID from the first project in the request
         */
        String tenantId = request.getProjects().get(0).getTenantId();
        String projectId = String.valueOf(project.getId());

        /*
         * Fetch projects from the database with ancestors and descendants
         */
        List<Project> projectsFromDbWithAncestorsAndDescendants = searchProject(
                getSearchProjectRequest(request.getProjects(), request.getRequestInfo(), false),
                projectConfiguration.getMaxLimit(),
                projectConfiguration.getDefaultOffset(),
                tenantId,
                null,
                false,
                true,
                true,
                null,
                null,
                false
        );

        /*
         * Create a map of projects from the database with ancestors and descendants
         */
        Map<String, Project> projectFromDbWithAncestorsAndDescendantsMap = projectServiceUtil.createProjectMap(projectsFromDbWithAncestorsAndDescendants);
        Project projectFromDbWithAncestorsAndDescendants = projectFromDbWithAncestorsAndDescendantsMap.get(projectId);

        /*
         * Enrich project cascading dates based on the retrieved data
         */
        projectEnrichment.enrichProjectCascadingDatesOnUpdate(project, projectFromDbWithAncestorsAndDescendants);
    }


    /* Search for parent projects based on "parent" field and returns parent projects  */
    private List<Project> getParentProjects(ProjectRequest projectRequest) {
        List<Project> parentProjects = null;
        List<Project> projectsForSearchRequest = projectRequest.getProjects().stream().filter(p -> StringUtils.isNotBlank(p.getParent())).toList();
        if (projectsForSearchRequest.size() > 0) {
            parentProjects = searchProject(getSearchProjectRequest(projectsForSearchRequest, projectRequest.getRequestInfo(), true), projectConfiguration.getMaxLimit(), projectConfiguration.getDefaultOffset(), projectRequest.getProjects().get(0).getTenantId(), null, false, false, false, null, null, false);
        }
        log.info("Fetched parent projects from DB");
        return parentProjects;
    }

    /* Construct Project Request object for search which contains project id and tenantId */
    private ProjectRequest getSearchProjectRequest(List<Project> projects, RequestInfo requestInfo, Boolean isParentProjectSearch) {
        List<Project> projectList = new ArrayList<>();

        for (Project project : projects) {
            String projectId = isParentProjectSearch ? project.getParent() : project.getId();
            Project newProject = Project.builder()
                    .id(projectId)
                    .tenantId(project.getTenantId())
                    .build();

            projectList.add(newProject);
        }
        return ProjectRequest.builder()
                .requestInfo(requestInfo)
                .projects(projectList)
                .build();
    }

    /**
     * @return Count of List of matching projects
     */
    public Integer countAllProjects(ProjectRequest project, String tenantId, Long lastChangedSince, Boolean includeDeleted, Long createdFrom, Long createdTo, boolean isAncestorProjectId) {
        log.trace("Entering countAllProjects (v1) for tenantId: {}", tenantId);
        log.debug("Counting projects with criteria");
        Integer count = projectRepository.getProjectCount(project, tenantId, lastChangedSince, includeDeleted, createdFrom, createdTo, isAncestorProjectId);
        log.debug("Found {} projects matching criteria", count);
        log.trace("Exiting countAllProjects (v1)");
        return count;
    }


    public Integer countAllProjects(ProjectSearchRequest request,
                                    ProjectSearchURLParams urlParams,
                                    List<String> workflowStatuses) {
        log.trace("Entering countAllProjects (v2)");
        log.debug("Counting projects with search request criteria");
        Integer count = projectRepository.getProjectCount(request, urlParams, workflowStatuses);
        log.debug("Found {} projects matching search criteria", count);
        log.trace("Exiting countAllProjects (v2)");
        return count;
    }

    public List<ProjectStatusAgregation> getStatusProjectsAgregation(String parentId) {
        log.trace("Entering getStatusProjectsAgregation for parentId: {}", parentId);
        log.debug("Fetching status aggregations from repository");
        List<ProjectStatusAgregation> result = projectRepository.getStatusProjectsAgregation(parentId);
        log.debug("Found {} status aggregations", result != null ? result.size() : 0);
        log.trace("Exiting getStatusProjectsAgregation");
        return result;
    }

    public ProjectStatusWrapper updateProjectWorkflow(ProjectWorkflowRequest request) throws Exception {
        log.trace("Entering updateProjectWorkflow for project: {}", request.getProjectId());
        log.info("Starting workflow update for project: {}", request.getProjectId());
        
        Project existingProject = fetchProjectForWorkflow(request);
        ProcessInstance updatedWorkflow = transitionWorkflow(request, existingProject);
        
        if(request.getTransactions() != null && !request.getTransactions().isEmpty()) {
            handleTransactionsAndComment(request, updatedWorkflow);
        }

        Object enrichedAdditionalDetails = enrichProjectWithWorkflowStatus(existingProject, updatedWorkflow, request);
        Project updatedProject = buildUpdatedProject(existingProject, enrichedAdditionalDetails);
        
        ProjectRequest enrichedRequest = ProjectRequest.builder()
                .requestInfo(request.getRequestInfo())
                .projects(List.of(updatedProject))
                .build();

        handleNormalUpdate(enrichedRequest, updatedProject, existingProject);
        processAssetUpdatesIfApproved(request, existingProject);

        log.info("Successfully completed workflow update for project: {}", request.getProjectId());
        log.trace("Exiting updateProjectWorkflow");
        return new ProjectStatusWrapper(updatedProject, updatedWorkflow.getState().getState(), null, null);
    }

    private Project fetchProjectForWorkflow(ProjectWorkflowRequest request) throws Exception {
        log.debug("Fetching existing project from database");
        ProjectSearch searchCriteria = ProjectSearch.builder()
                .id(List.of(request.getProjectId()))
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

        List<Project> projects = searchProject(searchRequest, urlParams, null, null);

        if (projects == null || projects.isEmpty()) {
            log.error("Project not found for workflow update: {}", request.getProjectId());
            throw new CustomException("PROJECT_NOT_FOUND", "Project not found with ID: " + request.getProjectId());
        }

        log.debug("Found existing project, proceeding with workflow transition");
        return projects.get(0);
    }

    private ProcessInstance transitionWorkflow(ProjectWorkflowRequest request, Project existingProject) {
        try {
            log.info("Transitioning workflow with action: {}", request.getWorkflow().getAction());
            ProcessInstance updatedWorkflow = workflowService.transitionWorkflow(
                    existingProject,
                    request.getWorkflow().getAction(),
                    request.getWorkflow().getDocuments(),
                    request.getRequestInfo(),
                    request.getWorkflow().getComments()
            );
            log.debug("Workflow transition completed successfully");
            return updatedWorkflow;
        } catch (Exception e) {
            log.error("Workflow transition failed for project: {}", request.getProjectId(), e);
            throw new CustomException("WORKFLOW_TRANSITION_FAILED",
                    "Failed to transition workflow for project: " + request.getProjectId());
        }
    }

    private Object enrichProjectWithWorkflowStatus(Project existingProject, ProcessInstance updatedWorkflow, ProjectWorkflowRequest request) {
        Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(
                existingProject.getAdditionalDetails(),
                "status",
                updatedWorkflow.getState().getState()
        );

        existingProject.setAdditionalDetails(enrichedAdditionalDetails);
        Object bom = request.getWorkflow().getAdditionalDetails();
        if(bom != null) {
            enrichedAdditionalDetails = mergeIntoAdditionalDetails(
                    existingProject.getAdditionalDetails(),
                    "bom",
                    bom
            );
        }
        return enrichedAdditionalDetails;
    }

    private Project buildUpdatedProject(Project existingProject, Object enrichedAdditionalDetails) {
        return Project.builder()
                .id(existingProject.getId())
                .tenantId(existingProject.getTenantId())
                .projectNumber(existingProject.getProjectNumber())
                .startDate(existingProject.getStartDate())
                .endDate(existingProject.getEndDate())
                .projectType(existingProject.getProjectType())
                .projectSubType(existingProject.getProjectSubType())
                .department(existingProject.getDepartment())
                .description(existingProject.getDescription())
                .referenceID(existingProject.getReferenceID())
                .projectTypeId(existingProject.getProjectTypeId())
                .address(existingProject.getAddress())
                .isTaskEnabled(existingProject.getIsTaskEnabled())
                .parent(existingProject.getParent())
                .projectHierarchy(existingProject.getProjectHierarchy())
                .natureOfWork(existingProject.getNatureOfWork())
                .additionalDetails(enrichedAdditionalDetails)
                .rowVersion(existingProject.getRowVersion())
                .isDeleted(existingProject.getIsDeleted())
                .name(existingProject.getName())
                .build();
    }

    private void processAssetUpdatesIfApproved(ProjectWorkflowRequest request, Project existingProject) throws Exception {
        if (!"APPROVE".equalsIgnoreCase(request.getWorkflow().getAction())) {
            return;
        }

        log.info("Workflow action is APPROVE, processing asset updates for facilities");
        log.debug("Searching for facilities associated with project: {}", existingProject.getId());
        
        ProjectFacility facility = fetchFirstFacilityForProject(existingProject, request.getRequestInfo());
        if (facility != null) {
            log.info("Updating assets for facility: {}", facility.getFacilityId());
            updateAssetsForFacility(existingProject, request.getRequestInfo(), facility.getFacilityId());
        } else {
            log.warn("No facility found for project: {}, skipping asset update", existingProject.getId());
        }
    }

    private ProjectFacility fetchFirstFacilityForProject(Project existingProject, RequestInfo requestInfo) {
        ProjectFacilitySearch projectFacilitySearch = ProjectFacilitySearch.builder()
                .projectId(new ArrayList<>(Arrays.asList(existingProject.getId())))
                .facilityId(null)
                .build();

        ProjectFacilitySearchRequest projectFacilitySearchRequest = ProjectFacilitySearchRequest.builder()
                .projectFacility(projectFacilitySearch)
                .requestInfo(requestInfo)
                .build();

        try {
            SearchResponse<ProjectFacility> facilitySearchResponse = projectFacilityService.search(
                    projectFacilitySearchRequest,
                    100, 0,
                    existingProject.getTenantId(),
                    null,
                    false
            );
            log.debug("Found {} facilities for project", 
                    facilitySearchResponse != null && facilitySearchResponse.getResponse() != null 
                            ? facilitySearchResponse.getResponse().size() : 0);
            
            if (facilitySearchResponse != null && facilitySearchResponse.getResponse() != null 
                    && !facilitySearchResponse.getResponse().isEmpty()) {
                return facilitySearchResponse.getResponse().get(0);
            }
        } catch (Exception e) {
            log.error("Failed to fetch facilities for project: {}", existingProject.getId(), e);
            throw new CustomException("FACILITY_FETCH_FAILED",
                    "Failed to fetch facilities for project: " + existingProject.getId());
        }
        return null;
    }

    private void handleTransactionsAndComment(ProjectWorkflowRequest request, ProcessInstance updatedWorkflow) {
        for(Transaction transaction: request.getTransactions()) {
            transaction.setProcessInstanceId(updatedWorkflow.getId());
            String userUUID = request.getRequestInfo().getUserInfo().getUuid();
            transaction.setProjectId(request.getProjectId());
            transaction.setAuditDetails(projectServiceUtil.getAuditDetails(userUUID, null, true));
            if(transaction.getTransactionId() == null || transaction.getTransactionId().isEmpty()) {
                transaction.setTransactionId(UUID.randomUUID().toString());
            }
            if(transaction.getComments() != null) handleCommentUpdate(transaction.getComments(), transaction.getTransactionId(), userUUID);
        }
        handleTransactionUpdate(request.getTransactions());
    }

    private void updateAssetsForFacility(Project existingProject, RequestInfo requestInfo, String facilityId) throws CustomException {
        AssetSearchCriteria assetSearchCriteria = AssetSearchCriteria.builder()
                .facilityID(facilityId)
                .tenantId(existingProject.getTenantId())
                .build();

        AssetSearchRequest assetSearchRequest = AssetSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(assetSearchCriteria)
                .build();

        StringBuilder assetSearchUri = new StringBuilder(projectConfiguration.getAssetHost())
                .append(projectConfiguration.getAssetSearchUrl());

        try {
            List<Asset> assets = serviceRequestRepository.fetchResult(assetSearchUri, assetSearchRequest, new TypeReference<List<Asset>>() {});
            if (assets != null && !assets.isEmpty()) {
                for (Asset asset : assets) {
                    updateAssetOperationalStatus(asset, requestInfo);
                }
            }
        } catch (ServiceCallException e) {
            log.error("Service call failed while processing assets for project {}: {}", existingProject.getId(), e.getMessage());
            throw new CustomException("ASSET_UPDATE_FAILED", "Failed to update asset operational status");
        } catch (Exception e) {
            log.error("Unexpected error while processing assets for project {}: {}", existingProject.getId(), e.getMessage(), e);
            throw new CustomException("ASSET_PROCESSING_ERROR", "An error occurred while processing assets");
        }
    }

    private void updateAssetOperationalStatus(Asset asset, RequestInfo requestInfo) {
        try {
            asset.setIsOperational(true);

            String assetUpdateEndpoint = projectConfiguration.getAssetHost() +
                    projectConfiguration.getAssetUpdateUrl();

            StringBuilder assetUpdateUri = new StringBuilder(assetUpdateEndpoint);
            assetUpdateUri.append("?assetID=").append(asset.getAssetId());

            AssetCreate assetCreate = AssetCreate.builder()
                    .asset(asset)
                    .build();

            AssetCreateRequest createRequest = AssetCreateRequest.builder()
                    .requestInfo(requestInfo)
                    .assetDetail(assetCreate)
                    .build();

            serviceRequestRepository.fetchResult(assetUpdateUri, createRequest);
        } catch (Exception e) {
            log.error("Failed to update asset {}: {}", asset.getAssetId(), e.getMessage());
        }
    }

    private Object mergeIntoAdditionalDetails(Object additionalDetails, String key, Object value) {
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

    private void handleTransactionUpdate(List<Transaction> transactions) {
        producer.push(projectConfiguration.getTransactionPersistTopic(), new TransactionRequest(transactions));
    }

    public void handleCommentUpdate(List<Comment> comments, String txId, String uuid) {
        comments.forEach(comment -> {
            comment.setAuditDetails(projectServiceUtil.getAuditDetails(uuid, null, true));
            if (comment.getCmtId() == null) {
                comment.setCmtId(UUID.randomUUID());
            }
            comment.setTransactionId(txId);
        });

        producer.push(projectConfiguration.getCommentPersistTopic(), new CommentRequest(comments));
    }

    public List<Transaction> getTransactionsForProject(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) return Collections.emptyList();

        String sql = "SELECT id, project_id, process_instance_id, created_by, last_modified_by, created_time, last_modified_time " +
                "FROM project_transaction WHERE project_id = ANY(?)";

        return jdbcTemplate.query(sql, ps -> {
            Array sqlArray = ps.getConnection().createArrayOf("text", projectIds.toArray(new String[0]));
            ps.setArray(1, sqlArray);
        }, (rs, rowNum) -> {
            Transaction transaction = new Transaction();
            transaction.setTransactionId(rs.getString("id"));
            transaction.setProjectId(rs.getString("project_id"));
            transaction.setProcessInstanceId(rs.getString("process_instance_id"));
            AuditDetails auditDetails = new AuditDetails();
            auditDetails.setCreatedBy(rs.getString("created_by"));
            auditDetails.setLastModifiedBy(rs.getString("last_modified_by"));
            auditDetails.setCreatedTime(rs.getLong("created_time"));
            auditDetails.setLastModifiedTime(rs.getLong("last_modified_time"));
            transaction.setAuditDetails(auditDetails);
            return transaction;
        });
    }

    public List<Comment> getCommentsForTransaction(List<String> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty()) return Collections.emptyList();

        String inSql = String.join(",", Collections.nCopies(transactionIds.size(), "?"));
        String sql = "SELECT id, transaction_id, comment_message, asset_type, created_by, last_modified_by, created_time, last_modified_time " +
                "FROM project_transaction_comment WHERE transaction_id IN (" + inSql + ")";

        return jdbcTemplate.query(sql, transactionIds.toArray(), (rs, rowNum) -> {
            Comment comment = new Comment();
            comment.setCmtId(UUID.fromString(rs.getString("id")));
            comment.setTransactionId(rs.getString("transaction_id"));
            comment.setCmtMsg(rs.getString("comment_message"));
            comment.setAssetType(rs.getString("asset_type"));
            AuditDetails auditDetails = new AuditDetails();
            auditDetails.setCreatedBy(rs.getString("created_by"));
            auditDetails.setLastModifiedBy(rs.getString("last_modified_by"));
            auditDetails.setCreatedTime(rs.getLong("created_time"));
            auditDetails.setLastModifiedTime(rs.getLong("last_modified_time"));
            comment.setAuditDetails(auditDetails);
            return comment;
        });
    }

    public Map<String, Object> updateBulkProjectWorkflow(ProjectBulkApproveRequest projectBulkApproveRequest) throws Exception {
        log.trace("Entering updateBulkProjectWorkflow");
        log.info("Starting bulk workflow update");

        BulkProjectWorkflowResult workflowResult = collectProjectIdsForBulkUpdate(projectBulkApproveRequest);
        
        if (workflowResult.getProjectIds().isEmpty()) {
            log.warn("No projects to process for bulk workflow update");
            return createEmptyBulkUpdateResult();
        }

        BulkUpdateExecutionResult executionResult = executeBulkWorkflowUpdates(
                workflowResult.getProjectIds(), 
                projectBulkApproveRequest
        );
        
        return buildBulkUpdateResult(executionResult, workflowResult, projectBulkApproveRequest);
    }

    private BulkProjectWorkflowResult collectProjectIdsForBulkUpdate(ProjectBulkApproveRequest request) throws Exception {
        if (request.getIsAllSelected()) {
            return collectProjectIdsFromFilters(request);
        } else {
            return collectProjectIdsFromRequest(request);
        }
    }

    private BulkProjectWorkflowResult collectProjectIdsFromFilters(ProjectBulkApproveRequest request) throws Exception {
        log.debug("Processing all selected projects based on filters");
        ExtendedProjectSearchRequest projectSearchRequest = getProjectSearchRequest(request);

        ProjectSearchURLParams urlParams = ProjectSearchURLParams.builder()
                .includeDescendants(false)
                .includeAncestors(false)
                .tenantId(request.getRequestInfo().getUserInfo().getTenantId())
                .limit(projectConfiguration.getMaxLimit()) 
                .offset(projectConfiguration.getDefaultOffset())
                .build();

        List<String> workflowStatuses = projectSearchRequest.getWorkflowStatus();
        List<Project> allProjects = searchProject(projectSearchRequest, urlParams, workflowStatuses, null);
        int totalProjects = countAllProjects(projectSearchRequest, urlParams, workflowStatuses);

        log.debug("Filtering projects with SUBMITTED_BY_SUPERVISOR status");
        List<Project> filteredProjects = allProjects.stream()
                .filter(this::hasSubmittedBySupervisorStatus)
                .toList();

        int finalProjects = filteredProjects.size();
        List<String> projectIds = filteredProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());
        
        log.debug("Found {} projects with SUBMITTED_BY_SUPERVISOR status out of {} total", finalProjects, totalProjects);
        return new BulkProjectWorkflowResult(projectIds, totalProjects, finalProjects);
    }

    private BulkProjectWorkflowResult collectProjectIdsFromRequest(ProjectBulkApproveRequest request) {
        if (request.getProjectIDs() != null && !request.getProjectIDs().isEmpty()) {
            int totalProjects = request.getProjectIDs().size();
            log.debug("Using {} provided project IDs", totalProjects);
            return new BulkProjectWorkflowResult(request.getProjectIDs(), totalProjects, 0);
        } else {
            log.error("Project IDs are required when isAllSelected is false");
            throw new CustomException("INVALID_REQUEST", "Project IDs are required when isAllSelected is false");
        }
    }

    private BulkUpdateExecutionResult executeBulkWorkflowUpdates(List<String> projectIds, ProjectBulkApproveRequest request) {
        log.info("Starting bulk workflow update for {} projects", projectIds.size());
        List<String> failedProjectIDs = new ArrayList<>();
        List<String> succeededProjectIDs = new ArrayList<>();
        
        for (String projectId : projectIds) {
            try {
                ProjectWorkflowRequest workflowRequest = ProjectWorkflowRequest.builder()
                        .requestInfo(request.getRequestInfo())
                        .projectId(projectId)
                        .workflow(request.getWorkflow())
                        .build();

                updateProjectWorkflow(workflowRequest);
                log.info("Successfully updated workflow for project: {}", projectId);
                succeededProjectIDs.add(projectId);
            } catch (Exception e) {
                log.error("Failed to update workflow for project {}: {}", projectId, e.getMessage());
                failedProjectIDs.add(projectId);
            }
        }
        
        return new BulkUpdateExecutionResult(succeededProjectIDs, failedProjectIDs);
    }

    private Map<String, Object> buildBulkUpdateResult(BulkUpdateExecutionResult executionResult, 
                                                      BulkProjectWorkflowResult workflowResult,
                                                      ProjectBulkApproveRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("failedProjectIDs", executionResult.getFailedProjectIDs());
        result.put("succeededProjectIDs", executionResult.getSucceededProjectIDs());
        
        int totalProjects = (request.getIsAllSelected() && workflowResult.getFinalProjects() > 0) 
                ? workflowResult.getFinalProjects() 
                : workflowResult.getTotalProjects();
        result.put("totalProjects", totalProjects);
        
        log.info("Bulk workflow update completed - succeeded: {}, failed: {}", 
                executionResult.getSucceededProjectIDs().size(), 
                executionResult.getFailedProjectIDs().size());
        log.trace("Exiting updateBulkProjectWorkflow");
        return result;
    }

    private Map<String, Object> createEmptyBulkUpdateResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("failedProjectIDs", new ArrayList<>());
        result.put("succeededProjectIDs", new ArrayList<>());
        result.put("totalProjects", 0);
        return result;
    }

    // Helper classes for bulk workflow update
    private static class BulkProjectWorkflowResult {
        private final List<String> projectIds;
        private final int totalProjects;
        private final int finalProjects;

        public BulkProjectWorkflowResult(List<String> projectIds, int totalProjects, int finalProjects) {
            this.projectIds = projectIds;
            this.totalProjects = totalProjects;
            this.finalProjects = finalProjects;
        }

        public List<String> getProjectIds() { return projectIds; }
        public int getTotalProjects() { return totalProjects; }
        public int getFinalProjects() { return finalProjects; }
    }

    private static class BulkUpdateExecutionResult {
        private final List<String> succeededProjectIDs;
        private final List<String> failedProjectIDs;

        public BulkUpdateExecutionResult(List<String> succeededProjectIDs, List<String> failedProjectIDs) {
            this.succeededProjectIDs = succeededProjectIDs;
            this.failedProjectIDs = failedProjectIDs;
        }

        public List<String> getSucceededProjectIDs() { return succeededProjectIDs; }
        public List<String> getFailedProjectIDs() { return failedProjectIDs; }
    }

    private static ExtendedProjectSearchRequest getProjectSearchRequest(ProjectBulkApproveRequest projectBulkApproveRequest) {
        ExtendedProjectSearchRequest projectSearchRequest = new ExtendedProjectSearchRequest();

        if( projectBulkApproveRequest.getFilters() != null ) {
            projectSearchRequest.setRequestInfo(projectBulkApproveRequest.getRequestInfo());
            projectSearchRequest.setProject(projectBulkApproveRequest.getFilters().getProjectSearch());
            projectSearchRequest.setWorkflowStatus(projectBulkApproveRequest.getFilters().getStatus());
        }  else {
            throw new CustomException("INVALID_REQUEST", "Filters are required when isAllSelected is true");
        }
        return projectSearchRequest;
    }

    private boolean hasSubmittedBySupervisorStatus(Project project) {
        Object additionalDetails = project.getAdditionalDetails();
        if (!(additionalDetails instanceof ObjectNode detailsNode)) return false;

        JsonNode statusNode = detailsNode.get("status");
        return statusNode != null && SUBMITTED_BY_SUPERVISOR.equals(statusNode.asText());
    }

}
