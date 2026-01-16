package org.egov.field_planner.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.project.ProjectFacility;
import org.egov.common.models.project.ProjectFacilityBulkResponse;
import org.egov.common.producer.Producer;
import org.egov.common.utils.ResponseInfoFactory;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.service.FieldPlannerFacilityService;
import org.egov.field_planner.service.FieldPlannerService;
import org.egov.field_planner.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Controller
@RequestMapping("/v1/field-plans")
@Validated
@Slf4j
public class FieldPlannerApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest httpServletRequest;

    private final Producer producer;

    private final FieldPlannerConfiguration fieldPlannerConfiguration;

    private final FieldPlannerService fieldPlannerService;

    private final FieldPlannerFacilityService fieldPlannerFacilityService;

    @Autowired
    public FieldPlannerApiController(ObjectMapper objectMapper, HttpServletRequest httpServletRequest,
                                     Producer producer,
                                     FieldPlannerConfiguration fieldPlannerConfiguration,
                                     FieldPlannerService fieldPlannerService, FieldPlannerFacilityService fieldPlannerFacilityService) {
        this.objectMapper = objectMapper;
        this.httpServletRequest = httpServletRequest;
        this.producer = producer;
        this.fieldPlannerConfiguration = fieldPlannerConfiguration;
        this.fieldPlannerService = fieldPlannerService;
        this.fieldPlannerFacilityService = fieldPlannerFacilityService;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanResponse> fieldPlanBeneficiaryV1CreatePost(@ApiParam(value = "Capture details of benificiary type.", required = true) @Valid @RequestBody FieldPlanRequest fieldPlanRequest) {
        log.trace("Entering fieldPlanBeneficiaryV1CreatePost endpoint");
        log.info("Received field plan creation request, field plan count: {}", fieldPlanRequest.getFieldPlans().size());
        
        FieldPlanRequest enrichedFieldPlanRequest = fieldPlannerService.createFieldPlan(fieldPlanRequest);
        FieldPlanResponse response = FieldPlanResponse.builder()
                .fieldPlans(enrichedFieldPlanRequest.getFieldPlans())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(fieldPlanRequest.getRequestInfo(), true))
                .build();
        log.info("Field plan creation request processed successfully");
        log.trace("Exiting fieldPlanBeneficiaryV1CreatePost endpoint");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanResponse> updateFieldPlan(@ApiParam(value = "Details for the updated Field Plan.", required = true) @Valid @RequestBody FieldPlanRequest fieldPlanRequest) {
        log.trace("Entering updateFieldPlan endpoint");
        log.info("Received field plan update request, field plan count: {}", fieldPlanRequest.getFieldPlans().size());
        
        FieldPlanRequest enrichedFieldPlanRequest = fieldPlannerService.updateFieldPlan(fieldPlanRequest);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(fieldPlanRequest.getRequestInfo(), true);
        FieldPlanResponse fieldPlanResponse = FieldPlanResponse.builder().responseInfo(responseInfo).fieldPlans(enrichedFieldPlanRequest.getFieldPlans()).build();
        log.info("Field plan update request processed successfully");
        log.trace("Exiting updateFieldPlan endpoint");
        return new ResponseEntity<FieldPlanResponse>(fieldPlanResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanResponse> searchfieldPlanV2(
            @ApiParam(value = "Details for the fieldPlan.", required = true) @Valid @RequestBody FieldPlanSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        log.trace("Entering searchfieldPlanV2 endpoint");
        log.info("Received field plan search request for tenant: {}, limit: {}, offset: {}", 
                urlParams.getTenantId(), urlParams.getLimit(), urlParams.getOffset());
        
        List<FieldPlan> fieldPlans = fieldPlannerService.searchFieldPlan(
                request,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getIncludeDeleted(),
                urlParams.getLastChangedSince(),
                request.getFieldPlan().getFromDate(),
                request.getFieldPlan().getToDate()
        );
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        Integer count = fieldPlannerService.countAllFieldPlans(request, urlParams.getTenantId(), urlParams.getLastChangedSince(), urlParams.getIncludeDeleted());
        FieldPlanResponse fieldPlanResponse = FieldPlanResponse.builder().responseInfo(responseInfo).fieldPlans(fieldPlans).totalCount(count).build();
        log.info("Field plan search completed, found {} results out of {} total", fieldPlans.size(), count);
        log.trace("Exiting searchfieldPlanV2 endpoint");
        return new ResponseEntity<FieldPlanResponse>(fieldPlanResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/facility/_create", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanFacilityResponse> fieldPlanFacilityV1CreatePost(@ApiParam(value = "Capture linkage of Field Plan and facility.", required = true) @Valid @RequestBody FieldPlanFacilityRequest request) {
        log.trace("Entering fieldPlanFacilityV1CreatePost endpoint");
        log.info("Received field plan facility creation request");
        
        FieldPlanFacility fieldPlanFacility = fieldPlannerFacilityService.create(request);
        FieldPlanFacilityResponse response = FieldPlanFacilityResponse.builder()
                .fieldPlanFacility(fieldPlanFacility)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.info("Field plan facility creation request processed successfully");
        log.trace("Exiting fieldPlanFacilityV1CreatePost endpoint");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/facility/bulk/_create", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> fieldPlanFacilityV1BulkCreatePost(@ApiParam(value = "Capture linkage of Field Plan and facility.", required = true) @Valid @RequestBody FieldPlanFacilityBulkRequest request) {
        log.trace("Entering fieldPlanFacilityV1BulkCreatePost endpoint");
        log.info("Received bulk field plan facility creation request, count: {}", request.getFieldPlanFacilities().size());
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing bulk create request to Kafka topic: {}", fieldPlannerConfiguration.getBulkCreateFieldPlanFacilityTopic());
        producer.push(fieldPlannerConfiguration.getBulkCreateFieldPlanFacilityTopic(), request);
        log.info("Bulk field plan facility creation request pushed to Kafka successfully");
        log.trace("Exiting fieldPlanFacilityV1BulkCreatePost endpoint");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }

    @RequestMapping(value = "/facility/_search", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanFacilityBulkResponse> fieldPlanFacilityV2SearchPost(
            @Valid @ModelAttribute URLParams urlParams,
            @ApiParam(value = "Capture details of Project facility.", required = true) @Valid @RequestBody FieldPlanFacilitySearchRequest request
    ) throws Exception {
        log.trace("Entering fieldPlanFacilityV2SearchPost endpoint");
        log.info("Received field plan facility search request for tenant: {}, limit: {}, offset: {}", 
                urlParams.getTenantId(), urlParams.getLimit(), urlParams.getOffset());
        
        SearchResponse<FieldPlanFacility> searchResponse = fieldPlannerFacilityService.search(
                request,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getLastChangedSince(),
                urlParams.getIncludeDeleted()
        );
        FieldPlanFacilityBulkResponse response = FieldPlanFacilityBulkResponse.builder()
                .fieldPlanFacilities(searchResponse.getResponse())
                .totalCount(searchResponse.getTotalCount())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.info("Field plan facility search completed, found {} results out of {} total", 
                searchResponse.getResponse().size(), searchResponse.getTotalCount());
        log.trace("Exiting fieldPlanFacilityV2SearchPost endpoint");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping(value = "/facility/_unassign", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanFacilityResponse> fieldPlanFacilityUnassign(@ApiParam(value = "Capture linkage of Field Plan and facility.", required = true) @Valid @RequestBody FieldPlanFacilityRequest request) {
        log.trace("Entering fieldPlanFacilityUnassign endpoint");
        log.info("Received field plan facility unassign request");
        
        FieldPlanFacility fieldPlanFacility = fieldPlannerFacilityService.unassign(request);
        FieldPlanFacilityResponse response = FieldPlanFacilityResponse.builder()
                .fieldPlanFacility(fieldPlanFacility)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.info("Field plan facility unassign request processed successfully");
        log.trace("Exiting fieldPlanFacilityUnassign endpoint");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/facility/bulk/_unassign", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> fieldPlanFacilityUnassignBulk(@ApiParam(value = "Capture linkage of Field Plan and facility.", required = true) @Valid @RequestBody FieldPlanFacilityBulkRequest request) {
        log.trace("Entering fieldPlanFacilityUnassignBulk endpoint");
        log.info("Received bulk field plan facility unassign request, count: {}", request.getFieldPlanFacilities().size());
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        log.debug("Pushing bulk unassign request to Kafka topic: {}", fieldPlannerConfiguration.getBulkUnassignFieldPlanFacilityTopic());
        producer.push(fieldPlannerConfiguration.getBulkUnassignFieldPlanFacilityTopic(), request);
        log.info("Bulk field plan facility unassign request pushed to Kafka successfully");
        log.trace("Exiting fieldPlanFacilityUnassignBulk endpoint");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }
}
