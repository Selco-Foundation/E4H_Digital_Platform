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
        log.trace("Entering createScheduledVisit controller method");
        log.info("Received request to create {} scheduled visit(s)", request.getScheduledVisits().size());
        ScheduledVisitRequest enrichedScheduledVisitRequest = scheduledVisitService.createScheduledVisit(request);
        log.info("Successfully created {} scheduled visit(s)", enrichedScheduledVisitRequest.getScheduledVisits().size());
        ScheduledVisitResponse response = ScheduledVisitResponse.builder()
                .scheduledVisits(enrichedScheduledVisitRequest.getScheduledVisits())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(enrichedScheduledVisitRequest.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/configuration/_generate", method = RequestMethod.POST)
    public ResponseEntity<ScheduledVisitResponse> generateVisits(@Validated @RequestBody VisitGenerationRequest request
    ) {
        log.trace("Entering generateVisits controller method");
        log.info("Received request to generate visits for configuration: {}", request.getConfigurationId());
        ScheduledVisitResponse response = scheduledVisitService.generateScheduledVisits(request);
        log.info("Successfully generated {} visit(s) for configuration: {}", 
                response.getTotalCount(), request.getConfigurationId());
        return ResponseEntity.accepted().body(response);
    }

    @RequestMapping(value = "/workflow/_update", method = RequestMethod.POST)
    public ResponseEntity<ScheduledVisitResponse> updateScheduledVisit(@ApiParam(value = "Details for the updated workflow visit.", required = true) @Valid @RequestBody VisitReportSubmissionRequest request) throws Exception {
        log.trace("Entering updateScheduledVisit workflow controller method");
        log.info("Received workflow update request for visitId: {}", request.getVisitId());
        List<ScheduledVisit> enrichedScheduledVisit = scheduledVisitService.updateVisitWorkflow(request);
        log.info("Successfully updated workflow for visitId: {}", request.getVisitId());

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        ScheduledVisitResponse visitResponse = ScheduledVisitResponse.builder().responseInfo(responseInfo).scheduledVisits(enrichedScheduledVisit).build();
        return new ResponseEntity<ScheduledVisitResponse>(visitResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<ScheduledVisitResponse> searchScheduledVisit(
            @ApiParam(value = "Details for the visit.", required = true) @Valid @RequestBody ScheduledVisitSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        log.trace("Entering searchScheduledVisit controller method");
        log.info("Received search request for scheduled visits, tenantId: {}", urlParams.getTenantId());
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
        log.info("Scheduled visit search completed, found {} visit(s), total count: {}", scheduledVisits.size(), count);
        return new ResponseEntity<ScheduledVisitResponse>(visitResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_resend_otp", method = RequestMethod.POST)
    public ResponseEntity<OtpResponse> resendOTP(@ApiParam(value = "Capture details of scheduled visit.", required = true) @Valid @RequestBody ResendOTPRequest request) {
        OtpResponse otpResponse = scheduledVisitService.resendOTP(request);
        return new ResponseEntity<OtpResponse>(otpResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<ScheduledVisitResponse> updateScheduledVisits(
            @ApiParam(value = "Details for the updated scheduled visits.", required = true)
            @Valid @RequestBody ScheduledVisitRequest request) {
        log.trace("Entering updateScheduledVisits controller method");
        log.info("Received request to update {} scheduled visit(s)", request.getScheduledVisits().size());
        ScheduledVisitRequest enrichedScheduledVisitRequest = scheduledVisitService.updateScheduledVisit(request);
        log.info("Successfully updated {} scheduled visit(s)", enrichedScheduledVisitRequest.getScheduledVisits().size());
        ScheduledVisitResponse response = ScheduledVisitResponse.builder()
                .scheduledVisits(enrichedScheduledVisitRequest.getScheduledVisits())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(enrichedScheduledVisitRequest.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
