package org.egov.project.web.controllers;

import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.data.query.exception.QueryBuilderException;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.project.*;
import org.egov.common.producer.Producer;
import org.egov.common.utils.ResponseInfoFactory;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.service.ProjectResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import lombok.extern.slf4j.Slf4j;


@Controller
@RequestMapping("")
@Validated
@Slf4j
public class ProjectResourceApiController {

    private final HttpServletRequest httpServletRequest;

    private final Producer producer;

    private final ProjectConfiguration projectConfiguration;

    @Autowired
    ProjectResourceService projectResourceService;

    public ProjectResourceApiController(HttpServletRequest httpServletRequest, Producer producer, ProjectConfiguration projectConfiguration) {
        this.httpServletRequest = httpServletRequest;
        this.producer = producer;
        this.projectConfiguration = projectConfiguration;
    }

    @RequestMapping(value = "/resource/v1/_create", method = RequestMethod.POST)
    public ResponseEntity<ProjectResourceResponse> resourceV1CreatePost(@ApiParam(value = "Capture linkage of Project and resources.", required = true) @Valid @RequestBody ProjectResourceRequest request) {
        log.trace("Entering resourceV1CreatePost");
        log.info("Received create request for project resource");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        ProjectResource projectResourceResponse = projectResourceService.create(request);
        log.debug("Created project resource");
        ProjectResourceResponse response = ProjectResourceResponse.builder()
                .projectResource(projectResourceResponse)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.info("Successfully created project resource");
        log.trace("Exiting resourceV1CreatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/resource/v1/bulk/_create", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> resourceV1BulkCreatePost(@ApiParam(value = "Capture linkage of Project and resources.", required = true) @Valid @RequestBody ProjectResourceBulkRequest request) {
        log.trace("Entering resourceV1BulkCreatePost");
        log.info("Received bulk create request for project resources");
        log.debug("Request URI: {}, Resources count: {}", httpServletRequest.getRequestURI(), request.getProjectResource() != null ? request.getProjectResource().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getCreateProjectResourceBulkTopic(), request);
        log.info("Successfully accepted bulk create request for project resources");
        log.trace("Exiting resourceV1BulkCreatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }

    @RequestMapping(value = "/resource/v1/_search", method = RequestMethod.POST)
    public ResponseEntity<ProjectResourceBulkResponse> resourceV1SearchPost(
            @Valid @ModelAttribute URLParams urlParams,
            @ApiParam(value = "Search linkage of Project and resource.", required = true) @Valid @RequestBody ProjectResourceSearchRequest projectResourceSearchRequest
    ) throws QueryBuilderException {
        log.trace("Entering resourceV1SearchPost");
        log.info("Received search request for project resources");
        log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}", urlParams.getLimit(), urlParams.getOffset(), urlParams.getTenantId());

        SearchResponse<ProjectResource> searchResponse = projectResourceService.search(
                projectResourceSearchRequest,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getLastChangedSince(),
                urlParams.getIncludeDeleted()
        );
        log.debug("Found {} resources", searchResponse.getResponse() != null ? searchResponse.getResponse().size() : 0);
        ProjectResourceBulkResponse response = ProjectResourceBulkResponse.builder().responseInfo(ResponseInfoFactory
                        .createResponseInfo(projectResourceSearchRequest.getRequestInfo(), true))
                .projectResource(searchResponse.getResponse())
                .totalCount(searchResponse.getTotalCount())
                .build();
        log.info("Successfully completed resource search");
        log.trace("Exiting resourceV1SearchPost");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping(value = "/resource/v1/_update", method = RequestMethod.POST)
    public ResponseEntity<ProjectResourceResponse> resourceV1UpdatePost(@ApiParam(value = "Capture linkage of Project and Resource.", required = true) @Valid @RequestBody ProjectResourceRequest request) {
        log.trace("Entering resourceV1UpdatePost");
        log.info("Received update request for project resource");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        ProjectResource projectResourceResponse = projectResourceService.update(request);
        log.debug("Updated project resource");
        ProjectResourceResponse response = ProjectResourceResponse.builder()
                .projectResource(projectResourceResponse)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.info("Successfully updated project resource");
        log.trace("Exiting resourceV1UpdatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/resource/v1/bulk/_update", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> resourceV1BulkUpdatePost(@ApiParam(value = "Capture linkage of Project and Resource.", required = true) @Valid @RequestBody ProjectResourceBulkRequest request) {
        log.trace("Entering resourceV1BulkUpdatePost");
        log.info("Received bulk update request for project resources");
        log.debug("Request URI: {}, Resources count: {}", httpServletRequest.getRequestURI(), request.getProjectResource() != null ? request.getProjectResource().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getUpdateProjectResourceBulkTopic(), request);
        log.info("Successfully accepted bulk update request for project resources");
        log.trace("Exiting resourceV1BulkUpdatePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }

    @RequestMapping(value = "/resource/v1/_delete", method = RequestMethod.POST)
    public ResponseEntity<ProjectResourceResponse> resourceV1DeletePost(@ApiParam(value = "Capture linkage of Project and Resource.", required = true) @Valid @RequestBody ProjectResourceRequest request) {
        log.trace("Entering resourceV1DeletePost");
        log.info("Received delete request for project resource");
        log.debug("Request URI: {}", httpServletRequest.getRequestURI());
        ProjectResource projectResourceResponse = projectResourceService.delete(request);
        log.debug("Deleted project resource");
        ProjectResourceResponse response = ProjectResourceResponse.builder()
                .projectResource(projectResourceResponse)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.info("Successfully deleted project resource");
        log.trace("Exiting resourceV1DeletePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/resource/v1/bulk/_delete", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> resourceV1BulkDeletePost(@ApiParam(value = "Capture linkage of Project and Resource.", required = true) @Valid @RequestBody ProjectResourceBulkRequest request) {
        log.trace("Entering resourceV1BulkDeletePost");
        log.info("Received bulk delete request for project resources");
        log.debug("Request URI: {}, Resources count: {}", httpServletRequest.getRequestURI(), request.getProjectResource() != null ? request.getProjectResource().size() : 0);
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing request to Kafka topic");
        producer.push(projectConfiguration.getDeleteProjectResourceBulkTopic(), request);
        log.info("Successfully accepted bulk delete request for project resources");
        log.trace("Exiting resourceV1BulkDeletePost");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }
}
