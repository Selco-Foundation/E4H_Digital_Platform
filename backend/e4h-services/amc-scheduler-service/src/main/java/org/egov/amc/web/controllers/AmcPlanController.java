package org.egov.amc.web.controllers;

import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.service.AmcPlanService;
import org.egov.amc.web.models.AmcPlan;
import org.egov.amc.web.models.AmcPlanRequest;
import org.egov.amc.web.models.AmcPlanResponse;
import org.egov.amc.web.models.AmcPlanSearchRequest;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.models.core.URLParams;
import org.egov.common.utils.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping("/v1/amc-plan")
@Validated
@Slf4j
public class AmcPlanController {

    private final AmcPlanService amcPlanService;

    @Autowired
    public AmcPlanController(AmcPlanService amcPlanService) {
        this.amcPlanService = amcPlanService;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<AmcPlanResponse> createAmcPlan(@ApiParam(value = "Capture details of AMC plan.", required = true) @Valid @RequestBody AmcPlanRequest request) {
        log.trace("Entering createAmcPlan controller method");
        log.info("Received request to create {} AMC plan(s)", request.getAmcPlans().size());
        AmcPlanRequest enrichedAmcPlanRequest = amcPlanService.createAmcPlan(request);
        AmcPlanResponse response = AmcPlanResponse.builder()
                .amcPlans(enrichedAmcPlanRequest.getAmcPlans())
                .responseInfo(ResponseInfoFactory.createResponseInfo(enrichedAmcPlanRequest.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<AmcPlanResponse> updateAmcPlan(@ApiParam(value = "Details for the updated AMC plan.", required = true) @Valid @RequestBody AmcPlanRequest request) {
        log.trace("Entering updateAmcPlan controller method");
        log.info("Received request to update {} AMC plan(s)", request.getAmcPlans().size());
        AmcPlanRequest enrichedAmcPlanRequest = amcPlanService.updateAmcPlan(request);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        AmcPlanResponse amcPlanResponse = AmcPlanResponse.builder().responseInfo(responseInfo).amcPlans(enrichedAmcPlanRequest.getAmcPlans()).build();
        return new ResponseEntity<>(amcPlanResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<AmcPlanResponse> searchAmcPlan(
            @ApiParam(value = "Details for the AmcPlan.", required = true) @Valid @RequestBody AmcPlanSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        log.trace("Entering searchAmcPlan controller method");
        log.info("Received search request for AMC plans, tenantId: {}", urlParams.getTenantId());
        List<AmcPlan> amcPlans = amcPlanService.searchAmcPlan(
                request,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getIncludeDeleted(),
                urlParams.getLastChangedSince()
        );
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        Integer count = amcPlanService.countAllAmcPlan(request, urlParams.getTenantId(), urlParams.getLastChangedSince(), urlParams.getIncludeDeleted());
        AmcPlanResponse amcPlanResponse = AmcPlanResponse.builder().responseInfo(responseInfo).amcPlans(amcPlans).totalCount(count).build();
        log.info("AMC plan search completed, found {} plan(s), total count: {}", amcPlans.size(), count);
        return new ResponseEntity<>(amcPlanResponse, HttpStatus.OK);
    }
}
