package org.egov.amc.web.controllers;


import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/v1/visit")
@Validated
@Slf4j
public class ScheduledVisitController {
    private final ScheduledVisitService scheduledVisitService;

    @Autowired
    public ScheduledVisitController(ScheduledVisitService assetAmcService) {
        this.scheduledVisitService = assetAmcService;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<ScheduledVisitResponse> createScheduledVisit(@ApiParam(value = "Capture details of scheduled visit.", required = true) @Valid @RequestBody ScheduledVisitRequest request) {
        ScheduledVisitRequest enrichedScheduledVisitRequest = scheduledVisitService.createScheduledVisit(request);
        ScheduledVisitResponse response = ScheduledVisitResponse.builder()
                .scheduledVisits(enrichedScheduledVisitRequest.getScheduledVisits())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(enrichedScheduledVisitRequest.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/{configurationId}/visit/_generate", method = RequestMethod.POST)
    public ResponseEntity<ScheduledVisitResponse> generateVisits(@PathVariable("configurationId") String configurationId, @Validated @RequestBody VisitGenerationRequest request
    ) {
        log.info("Received request to generate visits for configuration: {}", configurationId);
        ScheduledVisitResponse response = scheduledVisitService.generateScheduledVisits(configurationId, request);
        return ResponseEntity.accepted().body(response);
    }

    @RequestMapping(value = "/workflow/_update", method = RequestMethod.POST)
    public ResponseEntity<ScheduledVisitResponse> updateScheduledVisit(@ApiParam(value = "Details for the updated workflow visit.", required = true) @Valid @RequestBody VisitReportSubmissionRequest request) throws Exception {
        List<ScheduledVisit> enrichedScheduledVisit = scheduledVisitService.updateVisitWorkflow(request);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        ScheduledVisitResponse visitResponse = ScheduledVisitResponse.builder().responseInfo(responseInfo).scheduledVisits(enrichedScheduledVisit).build();
        return new ResponseEntity<ScheduledVisitResponse>(visitResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<ScheduledVisitResponse> searchScheduledVisit(
            @ApiParam(value = "Details for the visit.", required = true) @Valid @RequestBody ScheduledVisitSearchRequest request,
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
        Integer count = scheduledVisitService.countAllScheduledVisits(request, urlParams.getTenantId(), urlParams.getLastChangedSince(), urlParams.getIncludeDeleted());
        // Fetch all transactions by visitIds
        List<String> visitIds = scheduledVisits.stream().map(ScheduledVisit::getId).toList();
        List<Transaction> allTransactions = scheduledVisitService.getTransactionsForVisit(visitIds);
        // Group transactions by visitId
        Map<String, List<Transaction>> txnsByVisitId = allTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getVisitId));

        for (ScheduledVisit scheduledVisit : scheduledVisits) {
            List<Transaction> txns = txnsByVisitId.getOrDefault(scheduledVisit.getId(), Collections.emptyList());

            List<ProcessInstance> processInstances = scheduledVisitService.getProcessInstanceById(
                    scheduledVisit.getId(),
                    scheduledVisit.getTenantId(),
                    request.getRequestInfo()
            );
            scheduledVisit.setProcessInstances(processInstances);
            scheduledVisit.setTransactions(txns);
        }
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        ScheduledVisitResponse visitResponse = ScheduledVisitResponse.builder().responseInfo(responseInfo).scheduledVisits(scheduledVisits).totalCount(count).build();
        return new ResponseEntity<ScheduledVisitResponse>(visitResponse, HttpStatus.OK);
    }
}
