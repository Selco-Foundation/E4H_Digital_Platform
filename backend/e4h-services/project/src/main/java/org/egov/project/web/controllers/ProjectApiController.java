package org.egov.project.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.models.core.ProjectSearchURLParams;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.project.*;
import org.egov.common.models.project.BeneficiarySearchRequest;
import org.egov.common.producer.Producer;
import org.egov.common.utils.ResponseInfoFactory;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.service.*;
import org.egov.project.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Collections;
import java.util.stream.Collectors;


@Controller
@RequestMapping("")
@Validated
@lombok.extern.slf4j.Slf4j
public class ProjectApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest httpServletRequest;

    private final ProjectStaffService projectStaffService;

    private final ProjectBeneficiaryService projectBeneficiaryService;

    private final ProjectTaskService projectTaskService;

    private final ProjectFacilityService projectFacilityService;

    private final Producer producer;

    private final ProjectConfiguration projectConfiguration;

    private final ProjectService projectService;

    private final ProjectWorkflowService projectWorkflowService;

    @Autowired
    public ProjectApiController(ObjectMapper objectMapper, HttpServletRequest httpServletRequest,
                                ProjectStaffService projectStaffService,
                                ProjectTaskService projectTaskService,
                                ProjectBeneficiaryService projectBeneficiaryService,
                                ProjectFacilityService projectFacilityService, Producer producer,
                                ProjectConfiguration projectConfiguration,
                                ProjectService projectService, ProjectWorkflowService projectWorkflowService) {
        this.objectMapper = objectMapper;
        this.httpServletRequest = httpServletRequest;
        this.projectStaffService = projectStaffService;
        this.projectTaskService = projectTaskService;
        this.projectBeneficiaryService = projectBeneficiaryService;
        this.projectFacilityService = projectFacilityService;
        this.producer = producer;
        this.projectConfiguration = projectConfiguration;
        this.projectService = projectService;
        this.projectWorkflowService = projectWorkflowService;
    }

    @RequestMapping(value = "/beneficiary/v1/bulk/_create", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectBeneficiaryV1BulkCreatePost(@ApiParam(value = "Capture details of benificiary type.", required = true) @Valid @RequestBody BeneficiaryBulkRequest beneficiaryRequest) {
        log.trace("Entering projectBeneficiaryV1BulkCreatePost");
        log.info("Received bulk create request for project beneficiaries");
        log.debug("Request URI: {}, Beneficiaries count: {}", httpServletRequest.getRequestURI(), beneficiaryRequest.getProjectBeneficiaries() != null ? beneficiaryRequest.getProjectBeneficiaries().size() : 0);
        beneficiaryRequest.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        beneficiaryRequest.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Putting beneficiaries in cache");
        projectBeneficiaryService.putInCache(beneficiaryRequest.getProjectBeneficiaries());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getBulkCreateProjectBeneficiaryTopic(), beneficiaryRequest);
        log.info("Successfully accepted bulk create request for project beneficiaries");
        log.trace("Exiting projectBeneficiaryV1BulkCreatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(beneficiaryRequest.getRequestInfo(), true));
    }

    @RequestMapping(value = "/beneficiary/v1/_create", method = RequestMethod.POST)
    public ResponseEntity<BeneficiaryResponse> projectBeneficiaryV1CreatePost(@ApiParam(value = "Capture details of benificiary type.", required = true) @Valid @RequestBody BeneficiaryRequest beneficiaryRequest) {
        log.trace("Entering projectBeneficiaryV1CreatePost");
        log.info("Received create request for project beneficiary");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());

        List<ProjectBeneficiary> projectBeneficiaries = projectBeneficiaryService.create(beneficiaryRequest);
        log.debug("Created {} beneficiaries", projectBeneficiaries != null ? projectBeneficiaries.size() : 0);
        BeneficiaryResponse response = BeneficiaryResponse.builder()
                .projectBeneficiary(projectBeneficiaries.get(0))
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(beneficiaryRequest.getRequestInfo(), true))
                .build();
        log.info("Successfully created project beneficiary");
        log.trace("Exiting projectBeneficiaryV1CreatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/beneficiary/v1/_search", method = RequestMethod.POST)
    public ResponseEntity<BeneficiaryBulkResponse> projectBeneficiaryV2SearchPost(
            @Valid @ModelAttribute URLParams urlParams,
            @ApiParam(value = "Project Beneficiary Search.", required = true) @Valid @RequestBody BeneficiarySearchRequest beneficiarySearchRequest
    ) throws Exception {
        log.trace("Entering projectBeneficiaryV2SearchPost");
        log.info("Received search request for project beneficiaries");
        log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}", urlParams.getLimit(), urlParams.getOffset(), urlParams.getTenantId());
        SearchResponse<ProjectBeneficiary> searchResponse = projectBeneficiaryService.search(
                beneficiarySearchRequest,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getLastChangedSince(),
                urlParams.getIncludeDeleted()
        );
        log.debug("Found {} beneficiaries", searchResponse.getResponse() != null ? searchResponse.getResponse().size() : 0);
        BeneficiaryBulkResponse beneficiaryResponse = BeneficiaryBulkResponse.builder()
                .projectBeneficiaries(searchResponse.getResponse())
                .totalCount(searchResponse.getTotalCount())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(beneficiarySearchRequest.getRequestInfo(), true))
                .build();
        log.info("Successfully completed beneficiary search");
        log.trace("Exiting projectBeneficiaryV2SearchPost");
        return ResponseEntity.status(HttpStatus.OK).body(beneficiaryResponse);
    }

    @RequestMapping(value = "/beneficiary/v1/_update", method = RequestMethod.POST)
    public ResponseEntity<BeneficiaryResponse> projectBeneficiaryV1UpdatePost(@ApiParam(value = "Project Beneficiary Registration.", required = true) @Valid @RequestBody BeneficiaryRequest beneficiaryRequest, @ApiParam(value = "Client can specify if the resource in request body needs to be sent back in the response. This is being used to limit amount of data that needs to flow back from the server to the client in low bandwidth scenarios. Server will always send the server generated id for validated requests.", defaultValue = "true") @Valid @RequestParam(value = "echoResource", required = false, defaultValue = "true") Boolean echoResource) {
        log.trace("Entering projectBeneficiaryV1UpdatePost");
        log.info("Received update request for project beneficiary");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        List<ProjectBeneficiary> projectBeneficiaries = projectBeneficiaryService.update(beneficiaryRequest);
        log.debug("Updated {} beneficiaries", projectBeneficiaries != null ? projectBeneficiaries.size() : 0);
        BeneficiaryResponse response = BeneficiaryResponse.builder()
                .projectBeneficiary(projectBeneficiaries.get(0))
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(beneficiaryRequest.getRequestInfo(), true))
                .build();
        log.info("Successfully updated project beneficiary");
        log.trace("Exiting projectBeneficiaryV1UpdatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/beneficiary/v1/bulk/_update", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectBeneficiaryV1BulkUpdatePost(@ApiParam(value = "Project Beneficiary Registration.", required = true) @Valid @RequestBody BeneficiaryBulkRequest beneficiaryRequest, @ApiParam(value = "Client can specify if the resource in request body needs to be sent back in the response. This is being used to limit amount of data that needs to flow back from the server to the client in low bandwidth scenarios. Server will always send the server generated id for validated requests.", defaultValue = "true") @Valid @RequestParam(value = "echoResource", required = false, defaultValue = "true") Boolean echoResource) {
        log.trace("Entering projectBeneficiaryV1BulkUpdatePost");
        log.info("Received bulk update request for project beneficiaries");
        log.debug("Request URI: {}, Beneficiaries count: {}", httpServletRequest.getRequestURI(), beneficiaryRequest.getProjectBeneficiaries() != null ? beneficiaryRequest.getProjectBeneficiaries().size() : 0);
        beneficiaryRequest.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getBulkUpdateProjectBeneficiaryTopic(), beneficiaryRequest);
        log.info("Successfully accepted bulk update request for project beneficiaries");
        log.trace("Exiting projectBeneficiaryV1BulkUpdatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(beneficiaryRequest.getRequestInfo(), true));
    }

    @RequestMapping(value = "/beneficiary/v1/bulk/_delete", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectBeneficiaryV1BulkDeletePost(@ApiParam(value = "Capture details of benificiary type.", required = true) @Valid @RequestBody BeneficiaryBulkRequest beneficiaryRequest) {
        log.trace("Entering projectBeneficiaryV1BulkDeletePost");
        log.info("Received bulk delete request for project beneficiaries");
        log.debug("Request URI: {}, Beneficiaries count: {}", httpServletRequest.getRequestURI(), beneficiaryRequest.getProjectBeneficiaries() != null ? beneficiaryRequest.getProjectBeneficiaries().size() : 0);
        beneficiaryRequest.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getBulkDeleteProjectBeneficiaryTopic(), beneficiaryRequest);
        log.info("Successfully accepted bulk delete request for project beneficiaries");
        log.trace("Exiting projectBeneficiaryV1BulkDeletePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(beneficiaryRequest.getRequestInfo(), true));
    }

    @RequestMapping(value = "/beneficiary/v1/_delete", method = RequestMethod.POST)
    public ResponseEntity<BeneficiaryResponse> projectBeneficiaryV1DeletePost(@ApiParam(value = "Capture details of benificiary type.", required = true) @Valid @RequestBody BeneficiaryRequest beneficiaryRequest) {
        log.trace("Entering projectBeneficiaryV1DeletePost");
        log.info("Received delete request for project beneficiary");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        List<ProjectBeneficiary> projectBeneficiaries = projectBeneficiaryService.delete(beneficiaryRequest);
        log.debug("Deleted {} beneficiaries", projectBeneficiaries != null ? projectBeneficiaries.size() : 0);
        BeneficiaryResponse response = BeneficiaryResponse.builder()
                .projectBeneficiary(projectBeneficiaries.get(0))
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(beneficiaryRequest.getRequestInfo(), true))
                .build();
        log.info("Successfully deleted project beneficiary");
        log.trace("Exiting projectBeneficiaryV1DeletePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/facility/v1/_create", method = RequestMethod.POST)
    public ResponseEntity<ProjectFacilityResponse> projectFacilityV1CreatePost(@ApiParam(value = "Capture linkage of Project and facility.", required = true) @Valid @RequestBody ProjectFacilityRequest request) {
        log.trace("Entering projectFacilityV1CreatePost");
        log.info("Received create request for project facility");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        ProjectFacility projectFacility = projectFacilityService.create(request);
        log.debug("Created project facility");
        ProjectFacilityResponse response = ProjectFacilityResponse.builder()
                .projectFacility(projectFacility)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.info("Successfully created project facility");
        log.trace("Exiting projectFacilityV1CreatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/facility/v1/bulk/_create", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectFacilityV1BulkCreatePost(@ApiParam(value = "Capture linkage of Project and facility.", required = true) @Valid @RequestBody ProjectFacilityBulkRequest request) {
        log.trace("Entering projectFacilityV1BulkCreatePost");
        log.info("Received bulk create request for project facilities");
        log.debug("Request URI: {}, Facilities count: {}", httpServletRequest.getRequestURI(), request.getProjectFacilities() != null ? request.getProjectFacilities().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getBulkCreateProjectFacilityTopic(), request);
        log.info("Successfully accepted bulk create request for project facilities");
        log.trace("Exiting projectFacilityV1BulkCreatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }


    @RequestMapping(value = "/facility/v1/_search", method = RequestMethod.POST)
    public ResponseEntity<ProjectFacilityBulkResponse> projectFacilityV2SearchPost(
            @Valid @ModelAttribute URLParams urlParams,
            @ApiParam(value = "Capture details of Project facility.", required = true) @Valid @RequestBody ProjectFacilitySearchRequest projectFacilitySearchRequest
    ) throws Exception {
        log.trace("Entering projectFacilityV2SearchPost");
        log.info("Received search request for project facilities");
        log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}", urlParams.getLimit(), urlParams.getOffset(), urlParams.getTenantId());
        SearchResponse<ProjectFacility> searchResponse = projectFacilityService.search(
                projectFacilitySearchRequest,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getLastChangedSince(),
                urlParams.getIncludeDeleted()
        );
        log.debug("Found {} facilities", searchResponse.getResponse() != null ? searchResponse.getResponse().size() : 0);
        ProjectFacilityBulkResponse response = ProjectFacilityBulkResponse.builder()
                .projectFacilities(searchResponse.getResponse())
                .totalCount(searchResponse.getTotalCount())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(projectFacilitySearchRequest.getRequestInfo(), true))
                .build();
        log.info("Successfully completed facility search");
        log.trace("Exiting projectFacilityV2SearchPost");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping(value = "/facility/v1/_update", method = RequestMethod.POST)
    public ResponseEntity<ProjectFacilityResponse> projectFacilityV1UpdatePost(@ApiParam(value = "Capture linkage of Project and facility.", required = true) @Valid @RequestBody ProjectFacilityRequest projectFacilityUpdateRequest) {
        log.trace("Entering projectFacilityV1UpdatePost");
        log.info("Received update request for project facility");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        ProjectFacility projectFacility = projectFacilityService.update(projectFacilityUpdateRequest);
        log.debug("Updated project facility");
        ProjectFacilityResponse response = ProjectFacilityResponse.builder()
                .projectFacility(projectFacility)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(projectFacilityUpdateRequest.getRequestInfo(), true))
                .build();
        log.info("Successfully updated project facility");
        log.trace("Exiting projectFacilityV1UpdatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/facility/v1/bulk/_update", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectFacilityV1BulkUpdatePost(@ApiParam(value = "Capture linkage of Project and facility.", required = true) @Valid @RequestBody ProjectFacilityBulkRequest request) {
        log.trace("Entering projectFacilityV1BulkUpdatePost");
        log.info("Received bulk update request for project facilities");
        log.debug("Request URI: {}, Facilities count: {}", httpServletRequest.getRequestURI(), request.getProjectFacilities() != null ? request.getProjectFacilities().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getBulkUpdateProjectFacilityTopic(), request);
        log.info("Successfully accepted bulk update request for project facilities");
        log.trace("Exiting projectFacilityV1BulkUpdatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }

    @RequestMapping(value = "/facility/v1/_delete", method = RequestMethod.POST)
    public ResponseEntity<ProjectFacilityResponse> projectFacilityV1DeletePost(@ApiParam(value = "Capture linkage of Project and facility.", required = true) @Valid @RequestBody ProjectFacilityRequest projectFacilityUpdateRequest) {
        log.trace("Entering projectFacilityV1DeletePost");
        log.info("Received delete request for project facility");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        ProjectFacility projectFacilities = projectFacilityService.delete(projectFacilityUpdateRequest);
        log.debug("Deleted project facility");
        ProjectFacilityResponse response = ProjectFacilityResponse.builder()
                .projectFacility(projectFacilities)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(projectFacilityUpdateRequest.getRequestInfo(), true))
                .build();
        log.info("Successfully deleted project facility");
        log.trace("Exiting projectFacilityV1DeletePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/facility/v1/bulk/_delete", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectFacilityV1BulkDeletePost(@ApiParam(value = "Capture linkage of Project and facility.", required = true) @Valid @RequestBody ProjectFacilityBulkRequest request) {
        log.trace("Entering projectFacilityV1BulkDeletePost");
        log.info("Received bulk delete request for project facilities");
        log.debug("Request URI: {}, Facilities count: {}", httpServletRequest.getRequestURI(), request.getProjectFacilities() != null ? request.getProjectFacilities().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getBulkDeleteProjectFacilityTopic(), request);
        log.info("Successfully accepted bulk delete request for project facilities");
        log.trace("Exiting projectFacilityV1BulkDeletePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }

    @RequestMapping(value = "/staff/v1/_create", method = RequestMethod.POST)
    public ResponseEntity<ProjectStaffResponse> projectStaffV1CreatePost(@ApiParam(value = "Capture linkage of Project and staff user.", required = true) @Valid @RequestBody ProjectStaffRequest request) {
        log.trace("Entering projectStaffV1CreatePost");
        log.info("Received create request for project staff");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        ProjectStaff staff = projectStaffService.create(request);
        log.debug("Created project staff");
        ProjectStaffResponse response = ProjectStaffResponse.builder()
                .projectStaff(staff)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.info("Successfully created project staff");
        log.trace("Exiting projectStaffV1CreatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/staff/v1/bulk/_create", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectStaffV1CreatePost(@ApiParam(value = "Capture linkage of Project and staff user.", required = true) @Valid @RequestBody ProjectStaffBulkRequest request) {
        log.trace("Entering projectStaffV1BulkCreatePost");
        log.info("Received bulk create request for project staff");
        log.debug("Request URI: {}, Staff count: {}", httpServletRequest.getRequestURI(), request.getProjectStaff() != null ? request.getProjectStaff().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getBulkCreateProjectStaffTopic(), request);
        log.info("Successfully accepted bulk create request for project staff");
        log.trace("Exiting projectStaffV1BulkCreatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }


    @RequestMapping(value = "/staff/v1/_search", method = RequestMethod.POST)
    public ResponseEntity<ProjectStaffBulkResponse> projectStaffV1SearchPost(
            @Valid @ModelAttribute URLParams urlParams,
            @ApiParam(value = "Capture details of Project staff.", required = true) @Valid @RequestBody ProjectStaffSearchRequest projectStaffSearchRequest
    ) throws Exception {
        log.trace("Entering projectStaffV1SearchPost");
        log.info("Received search request for project staff");
        log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}", urlParams.getLimit(), urlParams.getOffset(), urlParams.getTenantId());
        SearchResponse<ProjectStaff> searchResponse = projectStaffService.search(
                projectStaffSearchRequest,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getLastChangedSince(),
                urlParams.getIncludeDeleted()
        );
        log.debug("Found {} staff", searchResponse.getResponse() != null ? searchResponse.getResponse().size() : 0);
        ProjectStaffBulkResponse response = ProjectStaffBulkResponse.builder()
                .projectStaff(searchResponse.getResponse())
                .totalCount(searchResponse.getTotalCount())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(projectStaffSearchRequest.getRequestInfo(), true))
                .build();
        log.info("Successfully completed staff search");
        log.trace("Exiting projectStaffV1SearchPost");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping(value = "/staff/v1/_update", method = RequestMethod.POST)
    public ResponseEntity<ProjectStaffResponse> projectStaffV1UpdatePost(@ApiParam(value = "Capture linkage of Project and staff user.", required = true) @Valid @RequestBody ProjectStaffRequest projectStaffUpdateRequest) {
        log.trace("Entering projectStaffV1UpdatePost");
        log.info("Received update request for project staff");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        ProjectStaff staff = projectStaffService.update(projectStaffUpdateRequest);
        log.debug("Updated project staff");
        ProjectStaffResponse response = ProjectStaffResponse.builder()
                .projectStaff(staff)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(projectStaffUpdateRequest.getRequestInfo(), true))
                .build();
        log.info("Successfully updated project staff");
        log.trace("Exiting projectStaffV1UpdatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/staff/v1/bulk/_update", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectStaffV1BulkUpdatePost(@ApiParam(value = "Capture linkage of Project and staff user.", required = true) @Valid @RequestBody ProjectStaffBulkRequest request) {
        log.trace("Entering projectStaffV1BulkUpdatePost");
        log.info("Received bulk update request for project staff");
        log.debug("Request URI: {}, Staff count: {}", httpServletRequest.getRequestURI(), request.getProjectStaff() != null ? request.getProjectStaff().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getBulkUpdateProjectStaffTopic(), request);
        log.info("Successfully accepted bulk update request for project staff");
        log.trace("Exiting projectStaffV1BulkUpdatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }

    @RequestMapping(value = "/staff/v1/_delete", method = RequestMethod.POST)
    public ResponseEntity<ProjectStaffResponse> projectStaffV1DeletePost(@ApiParam(value = "Capture linkage of Project and staff user.", required = true) @Valid @RequestBody ProjectStaffRequest projectStaffUpdateRequest) {
        log.trace("Entering projectStaffV1DeletePost");
        log.info("Received delete request for project staff");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        ProjectStaff staff = projectStaffService.delete(projectStaffUpdateRequest);
        log.debug("Deleted project staff");
        ProjectStaffResponse response = ProjectStaffResponse.builder()
                .projectStaff(staff)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(projectStaffUpdateRequest.getRequestInfo(), true))
                .build();
        log.info("Successfully deleted project staff");
        log.trace("Exiting projectStaffV1DeletePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/staff/v1/bulk/_delete", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectStaffV1BulkDeletePost(@ApiParam(value = "Capture linkage of Project and staff user.", required = true) @Valid @RequestBody ProjectStaffBulkRequest request) {
        log.trace("Entering projectStaffV1BulkDeletePost");
        log.info("Received bulk delete request for project staff");
        log.debug("Request URI: {}, Staff count: {}", httpServletRequest.getRequestURI(), request.getProjectStaff() != null ? request.getProjectStaff().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getBulkDeleteProjectStaffTopic(), request);
        log.info("Successfully accepted bulk delete request for project staff");
        log.trace("Exiting projectStaffV1BulkDeletePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }

    @RequestMapping(value = "/task/v1/_create", method = RequestMethod.POST)
    public ResponseEntity<TaskResponse> projectTaskV1CreatePost(@ApiParam(value = "Capture details of Task", required = true) @Valid @RequestBody TaskRequest request) {
        log.trace("Entering projectTaskV1CreatePost");
        log.info("Received create request for project task");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        Task task = projectTaskService.create(request);
        log.debug("Created project task");
        TaskResponse response = TaskResponse.builder()
                .task(task)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.info("Successfully created project task");
        log.trace("Exiting projectTaskV1CreatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }


    @RequestMapping(value = "/task/v1/bulk/_create", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectTaskBulkV1CreatePost(@ApiParam(value = "Capture details of Task", required = true) @Valid @RequestBody TaskBulkRequest request) {
        log.trace("Entering projectTaskBulkV1CreatePost");
        log.info("Received bulk create request for project tasks");
        log.debug("Request URI: {}, Tasks count: {}", httpServletRequest.getRequestURI(), request.getTasks() != null ? request.getTasks().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Putting tasks in cache");
        projectTaskService.putInCache(request.getTasks());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getCreateProjectTaskBulkTopic(), request);
        log.info("Successfully accepted bulk create request for project tasks");
        log.trace("Exiting projectTaskBulkV1CreatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }


    @RequestMapping(value = "/task/v1/_search", method = RequestMethod.POST)
    public ResponseEntity<TaskBulkResponse> projectTaskV1SearchPost(
            @Valid @ModelAttribute URLParams urlParams,
            @ApiParam(value = "Project Task Search.", required = true) @Valid @RequestBody TaskSearchRequest taskSearchRequest
    ) {
        log.trace("Entering projectTaskV1SearchPost");
        log.info("Received search request for project tasks");
        log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}", urlParams.getLimit(), urlParams.getOffset(), urlParams.getTenantId());
        SearchResponse<Task> taskSearchResponse = projectTaskService.search(
                taskSearchRequest.getTask(),
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getLastChangedSince(),
                urlParams.getIncludeDeleted()
        );
        log.debug("Found {} tasks", taskSearchResponse.getResponse() != null ? taskSearchResponse.getResponse().size() : 0);
        TaskBulkResponse response = TaskBulkResponse.builder().responseInfo(ResponseInfoFactory
                .createResponseInfo(taskSearchRequest.getRequestInfo(), true)).tasks(taskSearchResponse.getResponse()).totalCount(taskSearchResponse.getTotalCount()).build();
        log.info("Successfully completed task search");
        log.trace("Exiting projectTaskV1SearchPost");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping(value = "/task/v1/_update", method = RequestMethod.POST)
    public ResponseEntity<TaskResponse> projectTaskV1UpdatePost(@ApiParam(value = "Capture details of Existing task", required = true) @Valid @RequestBody TaskRequest request) {
        log.trace("Entering projectTaskV1UpdatePost");
        log.info("Received update request for project task");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        Task task = projectTaskService.update(request);
        log.debug("Updated project task");
        TaskResponse response = TaskResponse.builder()
                .task(task)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.info("Successfully updated project task");
        log.trace("Exiting projectTaskV1UpdatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/task/v1/bulk/_update", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectTaskV1BulkUpdatePost(@ApiParam(value = "Capture details of Existing task", required = true) @Valid @RequestBody TaskBulkRequest request) {
        log.trace("Entering projectTaskV1BulkUpdatePost");
        log.info("Received bulk update request for project tasks");
        log.debug("Request URI: {}, Tasks count: {}", httpServletRequest.getRequestURI(), request.getTasks() != null ? request.getTasks().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getUpdateProjectTaskBulkTopic(), request);
        log.info("Successfully accepted bulk update request for project tasks");
        log.trace("Exiting projectTaskV1BulkUpdatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }

    @RequestMapping(value = "/task/v1/_delete", method = RequestMethod.POST)
    public ResponseEntity<TaskResponse> projectTaskV1DeletePost(@ApiParam(value = "Capture details of Existing task", required = true) @Valid @RequestBody TaskRequest request) {
        log.trace("Entering projectTaskV1DeletePost");
        log.info("Received delete request for project task");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        Task task = projectTaskService.delete(request);
        log.debug("Deleted project task");
        TaskResponse response = TaskResponse.builder()
                .task(task)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.info("Successfully deleted project task");
        log.trace("Exiting projectTaskV1DeletePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/task/v1/bulk/_delete", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> projectTaskV1BulkDeletePost(@ApiParam(value = "Capture details of Existing task", required = true) @Valid @RequestBody TaskBulkRequest request) {
        log.trace("Entering projectTaskV1BulkDeletePost");
        log.info("Received bulk delete request for project tasks");
        log.debug("Request URI: {}, Tasks count: {}", httpServletRequest.getRequestURI(), request.getTasks() != null ? request.getTasks().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getDeleteProjectTaskBulkTopic(), request);
        log.info("Successfully accepted bulk delete request for project tasks");
        log.trace("Exiting projectTaskV1BulkDeletePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }

    @RequestMapping(value = "/v1/_create", method = RequestMethod.POST)
    public ResponseEntity<ProjectResponse> createProject(@ApiParam(value = "Details for the new Project.", required = true) @Valid @RequestBody ProjectRequest project) {
        log.trace("Entering createProject");
        log.info("Received create project request");
        log.debug("Request URI: {}, Projects count: {}", httpServletRequest.getRequestURI(), project.getProjects() != null ? project.getProjects().size() : 0);
        ProjectRequest enrichedProjectRequest = projectService.createProject(project);
        log.debug("Project creation completed, building response");
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(project.getRequestInfo(), true);
        ProjectResponse projectResponse = ProjectResponse.builder().responseInfo(responseInfo).project(enrichedProjectRequest.getProjects()).build();
        log.info("Successfully created {} projects", enrichedProjectRequest.getProjects() != null ? enrichedProjectRequest.getProjects().size() : 0);
        log.trace("Exiting createProject");
        return new ResponseEntity<ProjectResponse>(projectResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/v1/_search", method = RequestMethod.POST)
    public ResponseEntity<ProjectResponse> searchProject(
            @ApiParam(value = "Details for the project.", required = true) @Valid @RequestBody ProjectRequest project,
            @NotNull @Min(0) @Max(1000) @ApiParam(value = "Pagination - limit records in response", required = true) @Valid @RequestParam(value = "limit", required = true) Integer limit,
            @NotNull @Min(0) @ApiParam(value = "Pagination - offset from which records should be returned in response", required = true) @Valid @RequestParam(value = "offset", required = true) Integer offset,
            @NotNull @ApiParam(value = "Unique id for a tenant.", required = true) @Valid @RequestParam(value = "tenantId", required = true) String tenantId,
            @ApiParam(value = "epoch of the time since when the changes on the object should be picked up. Search results from this parameter should include both newly created objects since this time as well as any modified objects since this time. This criterion is included to help polling clients to get the changes in system since a last time they synchronized with the platform. ") @Valid @RequestParam(value = "lastChangedSince", required = false) Long lastChangedSince,
            @ApiParam(value = "Used in search APIs to specify if (soft) deleted records should be included in search results.", defaultValue = "false") @Valid @RequestParam(value = "includeDeleted", required = false, defaultValue = "false") Boolean includeDeleted,
            @ApiParam(value = "Used in project search API to specify if response should include project elements that are in the preceding hierarchy of matched projects.", defaultValue = "false") @Valid @RequestParam(value = "includeAncestors", required = false, defaultValue = "false") Boolean includeAncestors,
            @ApiParam(value = "Used in project search API to specify if response should include project elements that are in the following hierarchy of matched projects.", defaultValue = "false") @Valid @RequestParam(value = "includeDescendants", required = false, defaultValue = "false") Boolean includeDescendants,
            @ApiParam(value = "Used in project search API to limit the search results to only those projects whose creation date is after the specified 'createdFrom' date", defaultValue = "false") @Valid @RequestParam(value = "createdFrom", required = false) Long createdFrom,
            @ApiParam(value = "Used in project search API to limit the search results to only those projects whose creation date is before the specified 'createdTo' date", defaultValue = "false") @Valid @RequestParam(value = "createdTo", required = false) Long createdTo,
            @ApiParam(value = "Used in project search API to specify if response should be one which is in the preceding hierarchy of matched projects.") @Valid @RequestParam(value = "isAncestorProjectId", required = false, defaultValue = "false") boolean isAncestorProjectId
    ) {
        log.trace("Entering searchProject");
        log.info("Received search request for projects");
        log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}", limit, offset, tenantId);
        List<Project> projects = projectService.searchProject(
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
        log.debug("Found {} projects", projects != null ? projects.size() : 0);
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(project.getRequestInfo(), true);
        log.debug("Getting total project count");
        Integer count = projectService.countAllProjects(project, tenantId, lastChangedSince, includeDeleted, createdFrom, createdTo, isAncestorProjectId);
        log.debug("Total project count: {}", count);
        ProjectResponse projectResponse = ProjectResponse.builder().responseInfo(responseInfo).project(projects).totalCount(count).build();
        log.info("Successfully completed project search - found {} projects", projects != null ? projects.size() : 0);
        log.trace("Exiting searchProject");
        return new ResponseEntity<ProjectResponse>(projectResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/v2/_search")
    public ResponseEntity<ProjectStatusResponse> searchV2Project(
            @Valid @ModelAttribute ProjectSearchURLParams urlParams,
            @ApiParam(value = "Details for the project.", required = true)
            @Valid @RequestBody ExtendedProjectSearchRequest projectSearchRequest,
            @Valid @ModelAttribute ProjectSortCriteria sortCriteria
    ) throws Exception {
        log.trace("Entering searchV2Project");
        log.info("Received v2 search request for projects");
        log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}", urlParams.getLimit(), urlParams.getOffset(), urlParams.getTenantId());
        List<String> workflowStatuses = projectSearchRequest.getWorkflowStatus();
        log.debug("Workflow statuses filter: {}", workflowStatuses);

        List<Project> projects = projectService.searchProject(projectSearchRequest, urlParams, workflowStatuses, sortCriteria);
        log.debug("Found {} projects", projects != null ? projects.size() : 0);
        Integer count = projectService.countAllProjects(projectSearchRequest, urlParams, workflowStatuses);
        log.debug("Total project count: {}", count);

        // Fetch all transactions by projectIds
        log.debug("Fetching transactions for {} projects", projects != null ? projects.size() : 0);
        List<String> projectIds = projects.stream().map(Project::getId).toList();
        List<Transaction> allTransactions = projectService.getTransactionsForProject(projectIds);
        log.debug("Found {} transactions", allTransactions != null ? allTransactions.size() : 0);

        // Fetch all comments by transactionIds
        log.debug("Fetching comments for {} transactions", allTransactions != null ? allTransactions.size() : 0);
        List<String> txnIds = allTransactions.stream().map(Transaction::getTransactionId).toList();
        List<Comment> allComments = projectService.getCommentsForTransaction(txnIds);
        log.debug("Found {} comments", allComments != null ? allComments.size() : 0);

        // Group transactions by projectId
        Map<String, List<Transaction>> txnsByProjectId = allTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getProjectId));

        // Group comments by transactionId
        Map<String, List<Comment>> commentsByTxnId = allComments.stream()
                .collect(Collectors.groupingBy(Comment::getTransactionId));

        log.debug("Building project status wrappers");
        ObjectMapper mapper = new ObjectMapper();
        List<ProjectStatusWrapper> projectStatusWrappers = new ArrayList<>();
        for (Project project : projects) {
            String status = null;
            ObjectNode additionalDetails = mapper.convertValue(project.getAdditionalDetails(), ObjectNode.class);
            if (additionalDetails != null && additionalDetails.has("status")) {
                status = additionalDetails.get("status").asText();
            }

            List<Transaction> txns = txnsByProjectId.getOrDefault(project.getId(), Collections.emptyList());
            for (Transaction txn : txns) {
                txn.setComments(commentsByTxnId.getOrDefault(txn.getTransactionId(), Collections.emptyList()));
            }

            List<ProcessInstance> processInstances = projectWorkflowService.getProcessInstanceById(
                    project.getId(),
                    project.getTenantId(),
                    projectSearchRequest.getRequestInfo()
            );
            ProjectStatusWrapper wrapper = ProjectStatusWrapper.builder()
                .project(project)
                .status(status)
                .transactions(txns)
                .processInstances(processInstances)
                .build();
            projectStatusWrappers.add(wrapper);
        }

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(projectSearchRequest.getRequestInfo(), true);
        ProjectStatusResponse projectResponse = ProjectStatusResponse.builder()
                .responseInfo(responseInfo)
                .project(projectStatusWrappers)
                .totalCount(count)
                .build();
        log.info("Successfully completed v2 project search - found {} projects", projectStatusWrappers.size());
        log.trace("Exiting searchV2Project");
        return ResponseEntity.ok(projectResponse);
    }

    @RequestMapping(value = "/v1/_update", method = RequestMethod.POST)
    public ResponseEntity<ProjectResponse> updateProject(@ApiParam(value = "Details for the updated Project.", required = true) @Valid @RequestBody ProjectRequest project) {
        log.trace("Entering updateProject");
        log.info("Received update project request");
        log.debug("Request URI: {}, Projects count: {}", httpServletRequest.getRequestURI(), project.getProjects() != null ? project.getProjects().size() : 0);
        ProjectRequest enrichedProjectRequest = projectService.updateProject(project);
        log.debug("Project update completed, building response");
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(project.getRequestInfo(), true);
        ProjectResponse projectResponse = ProjectResponse.builder().responseInfo(responseInfo).project(enrichedProjectRequest.getProjects()).build();
        log.info("Successfully updated {} projects", enrichedProjectRequest.getProjects() != null ? enrichedProjectRequest.getProjects().size() : 0);
        log.trace("Exiting updateProject");
        return new ResponseEntity<ProjectResponse>(projectResponse, HttpStatus.OK);
    }

    @PostMapping("/v1/project/workflow/update")
    public ResponseEntity<ProjectStatusResponse> updateProjectWorkflow(
            @Valid @RequestBody ProjectWorkflowRequest request) throws Exception {
        log.trace("Entering updateProjectWorkflow");
        log.info("Received workflow update request for project: {}", request.getProjectId());
        log.debug("Request URI: {}, Workflow action: {}", httpServletRequest.getRequestURI(), request.getWorkflow() != null ? request.getWorkflow().getAction() : null);
        ProjectStatusWrapper updatedProject = projectService.updateProjectWorkflow(request);
        log.debug("Workflow update completed successfully");
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        log.info("Successfully updated workflow for project: {}", request.getProjectId());
        log.trace("Exiting updateProjectWorkflow");
        return ResponseEntity.ok(ProjectStatusResponse.builder()
                .responseInfo(responseInfo)
                .project(List.of(updatedProject))
                .build());
    }

    @PostMapping("/v1/project/bulk/workflow/update")
    public ResponseEntity<BulkProjectUpdateResponse> updateBulkProjectWorkflow(
            @ApiParam(value = "Bulk project workflow update request", required = true)
            @Valid @RequestBody ProjectBulkApproveRequest projectBulkApproveRequest) throws Exception {
        log.trace("Entering updateBulkProjectWorkflow");
        log.info("Received bulk workflow update request");
        log.debug("Request URI: {}, IsAllSelected: {}", httpServletRequest.getRequestURI(), projectBulkApproveRequest.getIsAllSelected());
        Map<String, Object> result = projectService.updateBulkProjectWorkflow(projectBulkApproveRequest);
        List<String> failedProjectIDs = result.get("failedProjectIDs") instanceof List<?> list ?
                    list.stream().map(String::valueOf).collect(Collectors.toList()) : Collections.emptyList();
        List<String> succeededProjectIDs = result.get("succeededProjectIDs") instanceof List<?> list ?
                    list.stream().map(String::valueOf).collect(Collectors.toList()) : Collections.emptyList();
        int totalProjects = result.get("totalProjects") instanceof Integer count ? count : 0;
        log.debug("Bulk workflow update completed - succeeded: {}, failed: {}, total: {}", succeededProjectIDs.size(), failedProjectIDs.size(), totalProjects);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(projectBulkApproveRequest.getRequestInfo(), true);

        BulkProjectUpdateResponse response = BulkProjectUpdateResponse.builder()
                .responseInfo(responseInfo)
                .failedProjectIDs(failedProjectIDs)
                .succeededProjectIDs(succeededProjectIDs)
                .build();
        if (failedProjectIDs.isEmpty()) {
            // All succeeded
            log.info("All {} projects successfully updated", totalProjects);
            log.trace("Exiting updateBulkProjectWorkflow");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else if (failedProjectIDs.size() == totalProjects) {
            // All failed
            log.warn("All {} projects failed to update", totalProjects);
            log.trace("Exiting updateBulkProjectWorkflow");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            // Partial success/fail
            log.warn("Partial success - {} succeeded, {} failed out of {} total", succeededProjectIDs.size(), failedProjectIDs.size(), totalProjects);
            log.trace("Exiting updateBulkProjectWorkflow");
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }
    }
}
