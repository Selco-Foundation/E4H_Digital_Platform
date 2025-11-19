package org.egov.amc.web.controllers;


import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import org.egov.amc.service.ScheduledVisitService;
import org.egov.amc.web.models.*;
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

import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/v1/visit")
@Validated
public class ScheduledVisitController {
    private final ScheduledVisitService scheduledVisitService;

    @Autowired
    public ScheduledVisitController(ScheduledVisitService assetAmcService) {
        this.scheduledVisitService = assetAmcService;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<ScheduledVisitResponse> createScheduledVisit(@ApiParam(value = "Capture details of benificiary type.", required = true) @Valid @RequestBody ScheduledVisitRequest request) {
        ScheduledVisitRequest enrichedScheduledVisitRequest = scheduledVisitService.createScheduledVisit(request);
        ScheduledVisitResponse response = ScheduledVisitResponse.builder()
                .scheduledVisits(enrichedScheduledVisitRequest.getScheduledVisits())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(enrichedScheduledVisitRequest.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/workflow/_update", method = RequestMethod.POST)
    public ResponseEntity<ScheduledVisitResponse> updateScheduledVisit(@ApiParam(value = "Details for the updated Field Plan.", required = true) @Valid @RequestBody ScheduledVisitRequest request) {
        ScheduledVisitRequest enrichedScheduledVisitRequest = scheduledVisitService.updateScheduledVisit(request);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        ScheduledVisitResponse fieldPlanResponse = ScheduledVisitResponse.builder().responseInfo(responseInfo).scheduledVisits(enrichedScheduledVisitRequest.getScheduledVisits()).build();
        return new ResponseEntity<ScheduledVisitResponse>(fieldPlanResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<ScheduledVisitResponse> searchScheduledVisit(
            @ApiParam(value = "Details for the fieldPlan.", required = true) @Valid @RequestBody ScheduledVisitSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        List<ScheduledVisit> scheduledVisits = scheduledVisitService.searchScheduledVisit(
                request,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getIncludeDeleted(),
                urlParams.getLastChangedSince()
        );
        for (ScheduledVisit scheduledVisit : scheduledVisits) {
            List<ProcessInstance> processInstances = scheduledVisitService.getProcessInstanceById(
                    scheduledVisit.getId(),
                    scheduledVisit.getTenantId(),
                    request.getRequestInfo()
            );
            scheduledVisit.setProcessInstances(processInstances);
        }
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        Integer count = scheduledVisitService.countAllScheduledVisits(request, urlParams.getTenantId(), urlParams.getLastChangedSince(), urlParams.getIncludeDeleted());
        ScheduledVisitResponse fieldPlanResponse = ScheduledVisitResponse.builder().responseInfo(responseInfo).scheduledVisits(scheduledVisits).totalCount(count).build();
        return new ResponseEntity<ScheduledVisitResponse>(fieldPlanResponse, HttpStatus.OK);
    }
}
