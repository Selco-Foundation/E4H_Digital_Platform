package org.egov.wf.web.controllers;


import java.util.List;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.egov.wf.service.WorkflowService;
import org.egov.wf.service.PauseStateService;
import org.egov.wf.util.ResponseInfoFactory;
import org.egov.wf.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.wf.service.ProjectService;


@RestController
@RequestMapping("/egov-wf")
public class WorkflowController {


    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final WorkflowService workflowService;

    private final PauseStateService pauseStateService;

    private final ResponseInfoFactory responseInfoFactory;

    private final ProjectService projectService;

    @Autowired
    public WorkflowController(ObjectMapper objectMapper, HttpServletRequest request,
                              WorkflowService workflowService, PauseStateService pauseStateService, 
                              ResponseInfoFactory responseInfoFactory, ProjectService projectService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.workflowService = workflowService;
        this.pauseStateService = pauseStateService;
        this.responseInfoFactory = responseInfoFactory;
        this.projectService = projectService;
    }



        @RequestMapping(value="/process/_transition", method = RequestMethod.POST)
        public ResponseEntity<ProcessInstanceResponse> processTransition(@Valid @RequestBody ProcessInstanceRequest processInstanceRequest) {
                List<ProcessInstance> processInstances =  workflowService.transition(processInstanceRequest);
                ProcessInstanceResponse response = ProcessInstanceResponse.builder().processInstances(processInstances)
                        .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(processInstanceRequest.getRequestInfo(), true))
                        .build();
                return new ResponseEntity<>(response,HttpStatus.OK);
        }




        @RequestMapping(value="/process/_search", method = RequestMethod.POST)
        public ResponseEntity<ProcessInstanceResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                              @Valid @ModelAttribute ProcessInstanceSearchCriteria criteria) {
        List<ProcessInstance> processInstances = workflowService.search(requestInfoWrapper.getRequestInfo(),criteria);
        Integer count = workflowService.getUserBasedProcessInstancesCount(requestInfoWrapper.getRequestInfo(),criteria);
            ProcessInstanceResponse response  = ProcessInstanceResponse.builder().processInstances(processInstances).totalCount(count).build();
                return new ResponseEntity<>(response,HttpStatus.OK);
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
    		criteria.setIsNearingSlaCount(Boolean.FALSE);
            Integer count = workflowService.count(requestInfoWrapper.getRequestInfo(),criteria);
            return new ResponseEntity<>(count,HttpStatus.OK);
        }

    @RequestMapping(value="/escalate/_search", method = RequestMethod.POST)
    public ResponseEntity<ProcessInstanceResponse> searchEscalatedApplications(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                          @Valid @ModelAttribute ProcessInstanceSearchCriteria criteria) {
        List<ProcessInstance> processInstances = workflowService.escalatedApplicationsSearch(requestInfoWrapper.getRequestInfo(),criteria);
        Integer count = workflowService.countEscalatedApplications(requestInfoWrapper.getRequestInfo(),criteria);
        ProcessInstanceResponse response  = ProcessInstanceResponse.builder().processInstances(processInstances).totalCount(count)
                .build();
        return new ResponseEntity<>(response,HttpStatus.OK);
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
        ProcessInstanceSearchCriteria statusCriteria = statusCountRequest.getProcessInstanceSearchCriteria();
        if (statusCriteria == null) {
            statusCriteria = criteria;
        }
        List result = workflowService.statusCount(statusCountRequest.getRequestInfo(), statusCriteria);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping(value="/process/_nearingslacount", method = RequestMethod.POST)
    public ResponseEntity<Integer> nearingSlaCount(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                         @Valid @ModelAttribute ProcessInstanceSearchCriteria criteria) {
        criteria.setIsNearingSlaCount(Boolean.TRUE);
        Integer count = workflowService.count(requestInfoWrapper.getRequestInfo(),criteria);
        return new ResponseEntity<>(count,HttpStatus.OK);
    }

    /**
     * Pause or unpause a workflow
     * @param pauseStateRequest The request containing pause state information
     * @return The updated pause state
     */
    @RequestMapping(value="/process/_pause", method = RequestMethod.POST)
    public ResponseEntity<PauseStateResponse> pauseWorkflow(@Valid @RequestBody PauseStateRequest pauseStateRequest) {
        List<PauseState> pauseStates = pauseStateService.saveOrUpdate(pauseStateRequest);

        // Update project indexer with pause state for each pause state
        for (PauseState pauseState : pauseStates) {
            projectService.updateProjectIndexerWithPauseState(
                pauseState.getBusinessId(),
                pauseState.getBusinessService(),
                pauseState.getIsPaused(),
                pauseStateRequest.getRequestInfo()
            );
        }

        PauseStateResponse response = PauseStateResponse.builder()
                .pauseStates(pauseStates)
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(pauseStateRequest.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
