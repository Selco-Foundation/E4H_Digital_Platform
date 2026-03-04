package org.egov.wf.web.controllers;


import org.egov.common.contract.response.ResponseInfo;
import org.egov.wf.service.EscalationService;
import org.egov.wf.util.ResponseInfoFactory;
import org.egov.wf.web.models.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/egov-wf")
@Slf4j
public class EscalationController {

    private EscalationService escalationService;

    private ResponseInfoFactory responseInfoFactory;


    @Autowired
    public EscalationController(EscalationService escalationService, ResponseInfoFactory responseInfoFactory) {
        this.escalationService = escalationService;
        this.responseInfoFactory = responseInfoFactory;
    }

    /**
     * API to auto escalate applications for given businessService which have breached SLA
     * @param requestInfoWrapper
     * @param businessService
     * @return
     */
    @RequestMapping(value="/auto/{businessService}/_escalate", method = RequestMethod.POST)
    public ResponseEntity<ResponseInfo> processTransition(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                                     @PathVariable(required = true) String businessService) {
        log.info("Received escalation request for businessService: {}", businessService);
        try {
            escalationService.escalateApplications(requestInfoWrapper.getRequestInfo(), businessService);
            ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
            log.info("Successfully processed escalation request for businessService: {}", businessService);
            return new ResponseEntity<>(responseInfo, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing escalation request for businessService: {}", businessService, e);
            throw e;
        }
    }

    /**
     * Temporary for testing
     * @param requestInfoWrapper
     * @param businessService
     * @return
     */
    @RequestMapping(value="/auto/{businessService}/_test", method = RequestMethod.POST)
    public ResponseEntity<List> processTransitionTest(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                          @PathVariable(required = true) String businessService) {
        log.info("Received escalation test request for businessService: {}", businessService);
        try {
            List<String> ids = escalationService.escalateApplicationsTest(requestInfoWrapper.getRequestInfo(), businessService);
            ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
            log.info("Escalation test completed for businessService: {}, returning {} business ID(s)", businessService, ids != null ? ids.size() : 0);
            return new ResponseEntity<>(ids, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing escalation test request for businessService: {}", businessService, e);
            throw e;
        }
    }


}
