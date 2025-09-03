package org.egov.field_planner.web.controllers;


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
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.service.*;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.FieldPlanRequest;
import org.egov.field_planner.web.models.FieldPlanResponse;
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
@RequestMapping("/v1/field-plans")
@Validated
public class FieldPlannerApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest httpServletRequest;

    private final Producer producer;

    private final FieldPlannerConfiguration fieldPlannerConfiguration;

    private final FieldPlannerService fieldPlannerService;

    @Autowired
    public FieldPlannerApiController(ObjectMapper objectMapper, HttpServletRequest httpServletRequest,
                                     Producer producer,
                                     FieldPlannerConfiguration fieldPlannerConfiguration,
                                     FieldPlannerService fieldPlannerService) {
        this.objectMapper = objectMapper;
        this.httpServletRequest = httpServletRequest;
        this.producer = producer;
        this.fieldPlannerConfiguration = fieldPlannerConfiguration;
        this.fieldPlannerService = fieldPlannerService;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanResponse> projectBeneficiaryV1CreatePost(@ApiParam(value = "Capture details of benificiary type.", required = true) @Valid @RequestBody FieldPlanRequest fieldPlanRequest) {
        FieldPlanRequest enrichedFieldPlanRequest = fieldPlannerService.createFieldPlan(fieldPlanRequest);
        FieldPlanResponse response = FieldPlanResponse.builder()
                .fieldPlans(enrichedFieldPlanRequest.getFieldPlans())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(fieldPlanRequest.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
