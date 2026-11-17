package org.egov.im.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.egov.im.service.SlaBackfillService;
import org.egov.im.util.ResponseInfoFactory;
import org.egov.im.web.models.SlaBackfillRequest;
import org.egov.im.web.models.SlaBackfillResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Script endpoint for the SLA anchor backfill, kept out of {@link RequestsApiController} since it is a
 * maintenance tool rather than part of the incident API.
 */
@RestController
@RequestMapping("/v2")
@Slf4j
public class SlaBackfillController {

    private final SlaBackfillService slaBackfillService;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public SlaBackfillController(SlaBackfillService slaBackfillService, ResponseInfoFactory responseInfoFactory) {
        this.slaBackfillService = slaBackfillService;
        this.responseInfoFactory = responseInfoFactory;
    }

    /**
     * Recomputes the SLA fields of every open ticket from its workflow history and writes them back.
     * Run once at rollout, and again whenever the SLA durations or the BusinessHours master change.
     * Safe to re-run.
     */
    @RequestMapping(value = "/request/_backfill-sla", method = RequestMethod.POST)
    public ResponseEntity<SlaBackfillResponse> backfillSla(@Valid @RequestBody SlaBackfillRequest request) {
        log.trace("SlaBackfillController::backfillSla method invoked");
        log.info("Received SLA backfill request for tenantId={}, dryRun={}", request.getTenantId(), request.getDryRun());
        SlaBackfillResponse response = slaBackfillService.backfill(request);
        response.setResponseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true));
        log.info("SLA backfill request completed: processed={}, updated={}, failed={}",
                response.getProcessed(), response.getUpdated(), response.getFailed());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
