package org.egov.field_planner.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.models.core.URLParams;
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

import java.util.List;


@Controller
@RequestMapping("/v1/field-plans")
@Validated
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
        FieldPlanRequest enrichedFieldPlanRequest = fieldPlannerService.createFieldPlan(fieldPlanRequest);
        FieldPlanResponse response = FieldPlanResponse.builder()
                .fieldPlans(enrichedFieldPlanRequest.getFieldPlans())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(fieldPlanRequest.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanResponse> updateFieldPlan(@ApiParam(value = "Details for the updated Field Plan.", required = true) @Valid @RequestBody FieldPlanRequest fieldPlanRequest) {
        FieldPlanRequest enrichedFieldPlanRequest = fieldPlannerService.updateFieldPlan(fieldPlanRequest);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(fieldPlanRequest.getRequestInfo(), true);
        FieldPlanResponse fieldPlanResponse = FieldPlanResponse.builder().responseInfo(responseInfo).fieldPlans(enrichedFieldPlanRequest.getFieldPlans()).build();
        return new ResponseEntity<FieldPlanResponse>(fieldPlanResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanResponse> searchfieldPlanV2(
            @ApiParam(value = "Details for the fieldPlan.", required = true) @Valid @RequestBody FieldPlanSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
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
        return new ResponseEntity<FieldPlanResponse>(fieldPlanResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/facility/_create", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanFacilityResponse> fieldPlanFacilityV1CreatePost(@ApiParam(value = "Capture linkage of Field Plan and facility.", required = true) @Valid @RequestBody FieldPlanFacilityRequest request) {

        FieldPlanFacility fieldPlanFacility = fieldPlannerFacilityService.create(request);
        FieldPlanFacilityResponse response = FieldPlanFacilityResponse.builder()
                .fieldPlanFacility(fieldPlanFacility)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/facility/bulk/_create", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> fieldPlanFacilityV1BulkCreatePost(@ApiParam(value = "Capture linkage of Field Plan and facility.", required = true) @Valid @RequestBody FieldPlanFacilityBulkRequest request) {
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        producer.push(fieldPlannerConfiguration.getBulkCreateFieldPlanFacilityTopic(), request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }

    @RequestMapping(value = "/facility/_unassign", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanFacilityResponse> fieldPlanFacilityUnassign(@ApiParam(value = "Capture linkage of Field Plan and facility.", required = true) @Valid @RequestBody FieldPlanFacilityRequest request) {

        FieldPlanFacility fieldPlanFacility = fieldPlannerFacilityService.unassign(request);
        FieldPlanFacilityResponse response = FieldPlanFacilityResponse.builder()
                .fieldPlanFacility(fieldPlanFacility)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/facility/bulk/_unassign", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> fieldPlanFacilityUnassignBulk(@ApiParam(value = "Capture linkage of Field Plan and facility.", required = true) @Valid @RequestBody FieldPlanFacilityBulkRequest request) {
        request.getRequestInfo().setApiId(httpServletRequest.getRequestURI());
        producer.push(fieldPlannerConfiguration.getBulkUnassignFieldPlanFacilityTopic(), request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseInfoFactory
                .createResponseInfo(request.getRequestInfo(), true));
    }
}
