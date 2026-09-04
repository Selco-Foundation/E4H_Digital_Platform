package org.egov.im.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.egov.im.service.CurrentOwnerBackfillService;
import org.egov.im.util.ResponseInfoFactory;
import org.egov.im.web.models.CurrentOwnerBackfillRequest;
import org.egov.im.web.models.CurrentOwnerBackfillResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Script endpoint for the currentOwner backfill, kept out of {@link RequestsApiController} since it
 * is a one-off recovery tool rather than part of the incident API.
 */
@RestController
@RequestMapping("/v2")
@Slf4j
public class CurrentOwnerBackfillController {

    private final CurrentOwnerBackfillService currentOwnerBackfillService;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public CurrentOwnerBackfillController(CurrentOwnerBackfillService currentOwnerBackfillService,
                                          ResponseInfoFactory responseInfoFactory) {
        this.currentOwnerBackfillService = currentOwnerBackfillService;
        this.responseInfoFactory = responseInfoFactory;
    }

    /**
     * Reads every document in the incident index, derives the owner from the workflow state it is
     * currently in and writes {@code currentOwner} / {@code currentOwnerSystemRole} back. Safe to
     * re-run: documents already holding the derived owner are left alone.
     */
    @RequestMapping(value = "/request/_backfill-currentowner", method = RequestMethod.POST)
    public ResponseEntity<CurrentOwnerBackfillResponse> backfillCurrentOwner(
            @Valid @RequestBody CurrentOwnerBackfillRequest request) {
        log.trace("CurrentOwnerBackfillController::backfillCurrentOwner method invoked");
        log.info("Received current owner backfill request for tenantId={}, dryRun={}",
                request.getTenantId(), request.getDryRun());
        CurrentOwnerBackfillResponse response = currentOwnerBackfillService.backfill(request);
        response.setResponseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true));
        log.info("Current owner backfill request completed: processed={}, updated={}",
                response.getProcessed(), response.getUpdated());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
