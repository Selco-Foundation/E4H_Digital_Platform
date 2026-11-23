package org.egov.im.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.egov.im.service.OnHoldBackfillService;
import org.egov.im.util.ResponseInfoFactory;
import org.egov.im.web.models.OnHoldBackfillRequest;
import org.egov.im.web.models.OnHoldBackfillResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Script endpoint for the ON_HOLD systemFunctional backfill, kept out of {@link RequestsApiController}
 * since it is a one-off recovery tool rather than part of the incident API.
 */
@RestController
@RequestMapping("/v2")
@Slf4j
public class OnHoldBackfillController {

    private final OnHoldBackfillService onHoldBackfillService;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public OnHoldBackfillController(OnHoldBackfillService onHoldBackfillService,
                                    ResponseInfoFactory responseInfoFactory) {
        this.onHoldBackfillService = onHoldBackfillService;
        this.responseInfoFactory = responseInfoFactory;
    }

    /**
     * Sets systemFunctional=ON_HOLD on tickets already in OUT_OF_WARRANTY_ON_HOLD, then re-publishes
     * them so the ticket index and their facilities' solarPanelStatus are refreshed.
     */
    @RequestMapping(value = "/request/_backfill-onhold-systemfunctional", method = RequestMethod.POST)
    public ResponseEntity<OnHoldBackfillResponse> backfillOnHold(@Valid @RequestBody OnHoldBackfillRequest request) {
        log.trace("OnHoldBackfillController::backfillOnHold method invoked");
        log.info("Received ON_HOLD backfill request for tenantId={}, dryRun={}",
                request.getTenantId(), request.getDryRun());
        OnHoldBackfillResponse response = onHoldBackfillService.backfill(request);
        response.setResponseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true));
        log.info("ON_HOLD backfill request completed: updated={}, reindexed={}",
                response.getUpdated(), response.getReindexed());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
