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

import static org.egov.project.util.ProjectConstants.SUBMITTED_BY_SUPERVISOR;

import static org.egov.project.util.ProjectConstants.PROJECT_TYPE_FACILITY;
import static org.egov.project.util.ProjectConstants.PROJECT_TYPE_FIELDPLAN;

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

    @Autowired
    BoundaryV2Util boundaryV2Util;

    @Autowired
    public ProjectService(
            ProjectRepository projectRepository,
            ProjectValidator projectValidator, ProjectEnrichment projectEnrichment, ProjectConfiguration projectConfiguration, Producer producer, ProjectServiceUtil projectServiceUtil, ProjectWorkflowService workflowService, @Lazy ProjectFacilityService projectFacilityService, JdbcTemplate jdbcTemplate, ServiceRequestRepository serviceRequestRepository, @Qualifier("objectMapper") ObjectMapper mapper) {
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
    }

    public List<String> validateProjectIds(List<String> productIds) {
        return projectRepository.validateIds(productIds, "id");
    }

    public List<Project> findByIds(List<String> projectIds) {
        return projectRepository.findById(projectIds);
    }

    public ProjectRequest createProject(ProjectRequest projectRequest) {
        projectValidator.validateCreateProjectRequest(projectRequest);
        //Get parent projects if "parent" is present (For enrichment of projectHierarchy)
        List<Project> parentProjects = getParentProjects(projectRequest);
        //Validate Parent in request against projects fetched form database
        if (parentProjects != null)
            projectValidator.validateParentAgainstDB(projectRequest.getProjects(), parentProjects);
        projectEnrichment.enrichProjectOnCreate(projectRequest, parentProjects);
        log.info("Enriched with Project Number, Ids and AuditDetails");
        producer.push(projectConfiguration.getSaveProjectTopic(), projectRequest);
        producer.push(projectConfiguration.getSaveProjectTopicIndexer(), projectRequest);
        log.info("Pushed to kafka");
        return projectRequest;
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
        projectValidator.validateSearchProjectRequest(project, limit, offset, tenantId, createdFrom, createdTo);
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
        return projects;
    }

    public List<Project> searchProject(ProjectSearchRequest projectSearchRequest, @Valid ProjectSearchURLParams urlParams, List<String> workflowStatuses, @Valid ProjectSortCriteria sortCriteria) throws Exception {
        projectValidator.validateSearchV2ProjectRequest(projectSearchRequest, urlParams, sortCriteria);
        List<Project> projects = projectRepository.getProjects(projectSearchRequest, urlParams, workflowStatuses, sortCriteria);
        // Get count of project type = Facility for each project type FieldPlan
        if(projectSearchRequest.getProject() !=null && projectSearchRequest.getProject().getProjectTypeId() !=null
                && projectSearchRequest.getProject().getProjectTypeId().equals(PROJECT_TYPE_FIELDPLAN))
            projects = getCountProjectTypeFacilities(projects, projectSearchRequest, urlParams, workflowStatuses, sortCriteria);

        // Get facility of project type = Facility for each project type Facility
        if(projectSearchRequest.getProject() !=null && projectSearchRequest.getProject().getProjectTypeId() !=null
                && projectSearchRequest.getProject().getProjectTypeId().equals(PROJECT_TYPE_FACILITY))
            getFacilityProject(projects, projectSearchRequest.getRequestInfo());

        return projects;
    }

    public List<Project> getFacilityProject(List<Project> listProjects, RequestInfo requestInfo) throws Exception {
        Map<String, BoundaryV2> listBlock = boundaryV2Util.getBoundaryByCode();
        for (Project project : listProjects) {
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
                ProjectFacility projectFacility = searchResponse.getResponse().get(0);
                Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(project.getAdditionalDetails(), "systemCode", "AC_OFF_GRID");
                project.setAdditionalDetails(enrichedAdditionalDetails);
            }

            // Get district and state for project type facility
            if(listBlock != null){
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

        return listProjects;
    }

    public List<Project> getCountProjectTypeFacilities(List<Project> listProjects, ProjectSearchRequest projectSearchRequest, @Valid ProjectSearchURLParams urlParams, List<String> workflowStatuses, @Valid ProjectSortCriteria sortCriteria) throws Exception {
        for (Project project : listProjects) {
            ProjectSearch copyProject = ProjectSearch.builder()
                    .parent(projectSearchRequest.getProject().getParent())
                    .projectTypeId("Facility")
                    .build();

            ProjectSearchRequest projectSearchRequest1 = ProjectSearchRequest.builder().project(copyProject).requestInfo(projectSearchRequest.getRequestInfo()).build();
            projectSearchRequest1.getProject().setProjectTypeId("Facility");
            projectSearchRequest1.getProject().setParent(project.getId());
            Integer count = countAllProjects(projectSearchRequest1, urlParams, workflowStatuses);
            Object enrichedAdditionalDetails = mergeIntoAdditionalDetails(project.getAdditionalDetails(), "countProjectFacilities", count);
            project.setAdditionalDetails(enrichedAdditionalDetails);
            List<ProjectStatusAgregation> statusAgregations = getStatusProjectsAgregation(project.getId());
            enrichedAdditionalDetails = mergeListIntoAdditionalDetails(project.getAdditionalDetails(), "statusAgregation", statusAgregations);
            project.setAdditionalDetails(enrichedAdditionalDetails);
        }

        return listProjects;
    }

    public ProjectRequest updateProject(ProjectRequest request) {
        /*
         * Validate the update project request
         */
        projectValidator.validateUpdateProjectRequest(request);
        log.info("Update project request validated");

        /*
         * Search for projects based on project IDs provided in the request
         */
        List<Project> projectsFromDB = searchProject(
                getSearchProjectRequest(request.getProjects(), request.getRequestInfo(), false),
                projectConfiguration.getMaxLimit(), projectConfiguration.getDefaultOffset(),
                request.getProjects().get(0).getTenantId(), null, false, false, false, null, null, false
        );
        log.info("Fetched projects for update request");

        /*
         * Validate the update project request against the projects fetched from the database
         */
        projectValidator.validateUpdateAgainstDB(request.getProjects(), projectsFromDB);

        /*
         * Process each project in the update request
         */
        for (Project project : request.getProjects()) {
            processProjectUpdate(request, project, projectsFromDB);
        }

        return request;
    }

    private void processProjectUpdate(ProjectRequest request, Project project, List<Project> projectsFromDB) {
        /*
         * Convert project ID to string for comparison
         */
        String projectId = String.valueOf(project.getId());

        /*
         * Find the project from the database that matches the current project ID
         */
        Project projectFromDB = findProjectById(projectId, projectsFromDB);
        boolean isCascadingProjectDateUpdate = request.isCascadingProjectDateUpdate();

        if (projectFromDB != null) {
            /*
             * Merge additional details of the project from the request and project from DB
             */
            projectServiceUtil.mergeAdditionalDetails(project, projectFromDB);

            /*
             * Handle cases where cascading project date update is true
             */
            if (isCascadingProjectDateUpdate) {
                handleUpdateProjectDates(request, project, projectFromDB);
            }
            /*
             * Handle cases for normal update flow
             */
            else {
                handleNormalUpdate(request, project, projectFromDB);
            }
        }
    }

    private Project findProjectById(String projectId, List<Project> projectsFromDB) {
        /*
         * Find and return the project with the matching ID from the list of projects fetched from the database
         */
        return projectsFromDB.stream()
                .filter(p -> projectId.equals(String.valueOf(p.getId())))
                .findFirst()
                .orElse(null);
    }


    private void handleNormalUpdate(ProjectRequest request, Project project, Project projectFromDB) {
        /*
         * Ensure that start and end dates are not being updated when flag is false
         */
        if (!project.getStartDate().equals(projectFromDB.getStartDate()) ||
                !project.getEndDate().equals(projectFromDB.getEndDate())) {
            throw new CustomException("PROJECT_CASCADE_UPDATE_DATE_ERROR",
                    "Can't Update Date Range if Cascade Project Date Update  false");
        }

        /*
         * Enrich the project with values other than the start, end dates, and AdditionalDetails,
         * and push the update to the message broker
         */
        projectEnrichment.enrichProjectOnUpdate(request, project, projectFromDB);
        producer.push(projectConfiguration.getUpdateProjectTopic(), request);
        producer.push(projectConfiguration.getUpdateProjectTopicIndexer(), request);
    }

    private void handleUpdateProjectDates(ProjectRequest request, Project project, Project projectFromDB) {
        /*
         * Save original values of start date, end date, and additional details
         */
        Long originalStartDate = projectFromDB.getStartDate();
        Long originalEndDate = projectFromDB.getEndDate();
        Object originalAdditionalDetails = projectFromDB.getAdditionalDetails();
        AuditDetails originalAuditDetails = projectFromDB.getAuditDetails();


        /*
         * Update the project with new start date, end date, and additional details
         */
        projectFromDB.setStartDate(project.getStartDate());
        projectFromDB.setEndDate(project.getEndDate());
        projectFromDB.setAdditionalDetails(project.getAdditionalDetails());
        projectFromDB.setAuditDetails(project.getAuditDetails());

        /*
         * Ensure that no other properties are being updated besides the start and end dates
         */
        if (!objectMapper.valueToTree(projectFromDB).equals(objectMapper.valueToTree(project))) {
            throw new CustomException(
                    "PROJECT_CASCADE_UPDATE_ERROR",
                    "Can only update Project dates and additional details if cascade Project date update true"
            );
        }

        /*
         * Restore original values of start date, end date, and additional details
         */
        projectFromDB.setStartDate(originalStartDate);
        projectFromDB.setEndDate(originalEndDate);
        projectFromDB.setAdditionalDetails(originalAdditionalDetails);
        projectFromDB.setAuditDetails(originalAuditDetails);

        /*
         * Update lastModifiedTime and lastModifiedBy for the project
         */
        projectEnrichment.enrichProjectRequestOnUpdate(project, projectFromDB, request.getRequestInfo());

        /*
         * Check and enrich cascading project dates and push the update to the message broker
         */
        checkAndEnrichCascadingProjectDates(request, project);
        producer.push(projectConfiguration.getUpdateProjectDateTopic(), request);
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
        return projectRepository.getProjectCount(project, tenantId, lastChangedSince, includeDeleted, createdFrom, createdTo, isAncestorProjectId);
    }


    public Integer countAllProjects(ProjectSearchRequest request,
                                    ProjectSearchURLParams urlParams,
                                    List<String> workflowStatuses) {
        return projectRepository.getProjectCount(request, urlParams, workflowStatuses);
    }

    public List<ProjectStatusAgregation> getStatusProjectsAgregation(String parentId) {
        return projectRepository.getStatusProjectsAgregation(parentId);
    }

    public ProjectStatusWrapper updateProjectWorkflow(ProjectWorkflowRequest request) throws Exception {
        // 1. Fetch the existing project
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
        List<String> workflowStatuses = null;
        ProjectSortCriteria sortCriteria = null;

        List<Project> projects = searchProject(searchRequest, urlParams, workflowStatuses, sortCriteria);

        if (projects == null || projects.isEmpty()) {
            throw new CustomException("PROJECT_NOT_FOUND", "Project not found with ID: " + request.getProjectId());
        }

        Project existingProject = projects.get(0);

        // 2. Call workflow transition
        ProcessInstance updatedWorkflow;
        try {
            updatedWorkflow = workflowService.transitionWorkflow(
                    existingProject,
                    request.getWorkflow().getAction(),
                    request.getWorkflow().getDocuments(),
                    request.getRequestInfo(),
                    request.getWorkflow().getComments()
            );
        } catch (Exception e) {
            throw new CustomException("WORKFLOW_TRANSITION_FAILED",
                    "Failed to transition workflow for project: " + request.getProjectId());
        }

        if(request.getTransactions() != null && !request.getTransactions().isEmpty()) {
            handleTransactionsAndComment(request, updatedWorkflow);
        }

        // 3. Inject workflow status into additionalDetails map
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

        // 4. Create a new Project instance with enriched additionalDetails
        Project updatedProject = Project.builder()
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

        // 5. Create project request wrapper
        ProjectRequest enrichedRequest = ProjectRequest.builder()
                .requestInfo(request.getRequestInfo())
                .projects(List.of(updatedProject))
                .build();

        // 6. Perform enriched update using standard handler
        handleNormalUpdate(enrichedRequest, updatedProject, existingProject);

        // Step 7: After successful workflow transition, if action is APPROVED_BY_QC_SPOC
        if ("APPROVE".equalsIgnoreCase(request.getWorkflow().getAction())) {
            // fetch facility for associated projectId -> facility search api to get associtaed facility
            ProjectFacilitySearch projectFacilitySearch = ProjectFacilitySearch.builder()
                    .projectId(new ArrayList<>(Arrays.asList(existingProject.getId())))
                    .facilityId(null)
                    .build();

            ProjectFacilitySearchRequest projectFacilitySearchRequest = ProjectFacilitySearchRequest.builder()
                    .projectFacility(projectFacilitySearch)
                    .requestInfo(request.getRequestInfo())
                    .build();

            SearchResponse<ProjectFacility> facilitySearchResponse;
            try {
                facilitySearchResponse = projectFacilityService.search(
                        projectFacilitySearchRequest,
                        100, 0,
                        existingProject.getTenantId(),
                        null,
                        false
                );
            } catch (Exception e) {
                throw new CustomException("FACILITY_FETCH_FAILED",
                        "Failed to fetch facilities for project: " + existingProject.getId());
            }

            // once facility is fetched we need to fetch assets for that facility
            ProjectFacility facility = null;
            if (facilitySearchResponse != null && facilitySearchResponse.getResponse() != null && !facilitySearchResponse.getResponse().isEmpty()) {
                facility = facilitySearchResponse.getResponse().get(0);
            }
            if (facility != null) {
                updateAssetsForFacility(existingProject, request.getRequestInfo(), facility.getFacilityId());
            }
        }

        return new ProjectStatusWrapper(updatedProject, updatedWorkflow.getState().getState(), null, null);
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

        List<String> projectIds = new ArrayList<>();
        int totalProjects = 0;
        int finalProjects = 0;
        
        if (projectBulkApproveRequest.getIsAllSelected()) {
            // Case 1: Search all projects using filters
            ExtendedProjectSearchRequest projectSearchRequest = getProjectSearchRequest(projectBulkApproveRequest);

            ProjectSearchURLParams urlParams = ProjectSearchURLParams.builder()
                    .includeDescendants(false)
                    .includeAncestors(false)
                    .tenantId(projectBulkApproveRequest.getRequestInfo().getUserInfo().getTenantId())
                    .limit(projectConfiguration.getMaxLimit()) 
                    .offset(projectConfiguration.getDefaultOffset())
                    .build();

            List<String> workflowStatuses = projectSearchRequest.getWorkflowStatus();

            List<Project> allProjects = searchProject(projectSearchRequest, urlParams, workflowStatuses, null);
            totalProjects = countAllProjects(projectSearchRequest, urlParams, workflowStatuses);

            // only those projects whose status is SUBMITTED_BY_SUPERVISOR
            List<Project> projects = allProjects.stream().filter(this::hasSubmittedBySupervisorStatus).toList();

            finalProjects = projects.size();
            projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
        } else {
            // Case 2: Use provided project IDs
            if (projectBulkApproveRequest.getProjectIDs() != null && !projectBulkApproveRequest.getProjectIDs().isEmpty()) {
                projectIds = projectBulkApproveRequest.getProjectIDs();
                totalProjects = projectIds.size();
            } else {
                throw new CustomException("INVALID_REQUEST", "Project IDs are required when isAllSelected is false");
            }
        }
        Map<String, Object> result = new HashMap<>();
        // Validate that we have projects to process
        if (projectIds.isEmpty()) {
            result.put("failedProjectIDs", new ArrayList<>());
            result.put("succeededProjectIDs", new ArrayList<>());
            result.put("totalProjects", 0);
            return result;
        }

        // Update workflow for all project IDs
        log.info("Starting bulk workflow update for {} projects", projectIds.size());
        List<String> failedProjectIDs = new ArrayList<>();
        List<String> succeededProjectIDs = new ArrayList<>();
        for (String projectId : projectIds) {
            try {
                ProjectWorkflowRequest workflowRequest = ProjectWorkflowRequest.builder()
                        .requestInfo(projectBulkApproveRequest.getRequestInfo())
                        .projectId(projectId)
                        .workflow(projectBulkApproveRequest.getWorkflow())
                        .build();

                ProjectStatusWrapper updatedProject = updateProjectWorkflow(workflowRequest);
                log.info("Successfully updated workflow for project: {}", projectId);
                succeededProjectIDs.add(projectId);
            } catch (Exception e) {
                log.error("Failed to update workflow for project {}: {}", projectId, e.getMessage());
                failedProjectIDs.add(projectId);
            }
        }
        
        result.put("failedProjectIDs", failedProjectIDs);
        result.put("succeededProjectIDs", succeededProjectIDs);
        if(projectBulkApproveRequest.getIsAllSelected() && finalProjects > 0) {
            result.put("totalProjects", finalProjects);
        } else {
            result.put("totalProjects", totalProjects);
        }
        return result;
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
