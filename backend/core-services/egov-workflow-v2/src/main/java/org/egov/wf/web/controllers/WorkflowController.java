package org.egov.wf.web.controllers;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.egov.wf.service.WorkflowService;
import org.egov.wf.util.ResponseInfoFactory;
import org.egov.wf.web.models.ProcessInstance;
import org.egov.wf.web.models.ProcessInstanceRequest;
import org.egov.wf.web.models.ProcessInstanceResponse;
import org.egov.wf.web.models.ProcessInstanceSearchCriteria;
import org.egov.wf.web.models.RequestInfoWrapper;
import org.egov.wf.web.models.StatusCountRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("/egov-wf")
@Slf4j
public class WorkflowController {


    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final WorkflowService workflowService;

    private final ResponseInfoFactory responseInfoFactory;


    @Autowired
    public WorkflowController(ObjectMapper objectMapper, HttpServletRequest request,
                              WorkflowService workflowService, ResponseInfoFactory responseInfoFactory) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.workflowService = workflowService;
        this.responseInfoFactory = responseInfoFactory;
    }



        @RequestMapping(value="/process/_transition", method = RequestMethod.POST)
        public ResponseEntity<ProcessInstanceResponse> processTransition(@Valid @RequestBody ProcessInstanceRequest processInstanceRequest) {
                log.info("Received workflow transition request for {} process instance(s)", 
                        processInstanceRequest.getProcessInstances() != null ? processInstanceRequest.getProcessInstances().size() : 0);
                try {
                    List<ProcessInstance> processInstances =  workflowService.transition(processInstanceRequest);
                    ProcessInstanceResponse response = ProcessInstanceResponse.builder().processInstances(processInstances)
                            .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(processInstanceRequest.getRequestInfo(), true))
                            .build();
                    log.info("Successfully processed workflow transition request");
                    return new ResponseEntity<>(response,HttpStatus.OK);
                } catch (Exception e) {
                    log.error("Error processing workflow transition request", e);
                    throw e;
                }
        }




        @RequestMapping(value="/process/_search", method = RequestMethod.POST)
        public ResponseEntity<ProcessInstanceResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                              @Valid @ModelAttribute ProcessInstanceSearchCriteria criteria) {
        log.info("Received process instance search request - businessService: {}, tenantId: {}", 
                criteria.getBusinessService(), criteria.getTenantId());
        try {
            List<ProcessInstance> processInstances = workflowService.search(requestInfoWrapper.getRequestInfo(),criteria);
            Integer count = workflowService.getUserBasedProcessInstancesCount(requestInfoWrapper.getRequestInfo(),criteria);
            ProcessInstanceResponse response  = ProcessInstanceResponse.builder().processInstances(processInstances).totalCount(count).build();
            log.info("Process instance search completed successfully, returning {} result(s)", 
                    processInstances != null ? processInstances.size() : 0);
            return new ResponseEntity<>(response,HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing process instance search request", e);
            throw e;
        }
        }

    /**
     * Returns the count of records matching the given criteria
     * @param requestInfoWrapper
     * @param criteria
     * @return
     */
    @RequestMapping(value="/process/_count", method = RequestMethod.POST)
        public ResponseEntity<Integer> count(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                              @Valid @ModelAttribute ProcessInstanceSearchCriteria criteria) {
    		log.info("Received process instance count request - businessService: {}, tenantId: {}", 
                    criteria.getBusinessService(), criteria.getTenantId());
            criteria.setIsNearingSlaCount(Boolean.FALSE);
            try {
                Integer count = workflowService.count(requestInfoWrapper.getRequestInfo(),criteria);
                log.info("Process instance count request completed, count: {}", count);
                return new ResponseEntity<>(count,HttpStatus.OK);
            } catch (Exception e) {
                log.error("Error processing process instance count request", e);
                throw e;
            }
        }

    @RequestMapping(value="/escalate/_search", method = RequestMethod.POST)
    public ResponseEntity<ProcessInstanceResponse> searchEscalatedApplications(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                          @Valid @ModelAttribute ProcessInstanceSearchCriteria criteria) {
        log.info("Received escalated applications search request - businessService: {}, tenantId: {}", 
                criteria.getBusinessService(), criteria.getTenantId());
        try {
            List<ProcessInstance> processInstances = workflowService.escalatedApplicationsSearch(requestInfoWrapper.getRequestInfo(),criteria);
            Integer count = workflowService.countEscalatedApplications(requestInfoWrapper.getRequestInfo(),criteria);
            ProcessInstanceResponse response  = ProcessInstanceResponse.builder().processInstances(processInstances).totalCount(count)
                    .build();
            log.info("Escalated applications search completed successfully, returning {} result(s)", 
                    processInstances != null ? processInstances.size() : 0);
            return new ResponseEntity<>(response,HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing escalated applications search request", e);
            throw e;
        }
    }

    /**
     * Returns the count of each status of records matching the given criteria
     * @param requestInfoWrapper
     * @param criteria
     * @return
     */
    @RequestMapping(value = "/process/_statuscount", method = RequestMethod.POST)
    public ResponseEntity<List> StatusCount(@Valid @RequestBody StatusCountRequest statusCountRequest,
            @Valid @ModelAttribute ProcessInstanceSearchCriteria criteria) {
        log.info("Received status count request - businessService: {}", criteria.getBusinessService());
        try {
            ProcessInstanceSearchCriteria statusCriteria = statusCountRequest.getProcessInstanceSearchCriteria();
            if (statusCriteria == null) {
                statusCriteria = criteria;
            }
            List result = workflowService.statusCount(statusCountRequest.getRequestInfo(), statusCriteria);
            log.info("Status count request completed successfully, returning {} status count(s)", 
                    result != null ? result.size() : 0);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing status count request", e);
            throw e;
        }
    }
    
    @RequestMapping(value="/process/_nearingslacount", method = RequestMethod.POST)
    public ResponseEntity<Integer> nearingSlaCount(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                         @Valid @ModelAttribute ProcessInstanceSearchCriteria criteria) {
        log.info("Received nearing SLA count request - businessService: {}", criteria.getBusinessService());
        criteria.setIsNearingSlaCount(Boolean.TRUE);
        try {
            Integer count = workflowService.count(requestInfoWrapper.getRequestInfo(),criteria);
            log.info("Nearing SLA count request completed, count: {}", count);
            return new ResponseEntity<>(count,HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing nearing SLA count request", e);
            throw e;
        }
    }

}
