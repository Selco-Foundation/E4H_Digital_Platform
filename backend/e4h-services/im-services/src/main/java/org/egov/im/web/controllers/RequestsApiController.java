package org.egov.im.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.im.service.IMService;
import org.egov.im.util.IMConstants;
import org.egov.im.util.ResponseInfoFactory;
import org.egov.im.web.models.CountResponse;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentResponse;
import org.egov.im.web.models.IncidentWrapper;
import org.egov.im.web.models.RequestInfoWrapper;
import org.egov.im.web.models.RequestSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

import jakarta.validation.Valid;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Controller
@RequestMapping("/v2")
@Slf4j
public class RequestsApiController{

    private final ObjectMapper objectMapper;

    private IMService imService;

    private ResponseInfoFactory responseInfoFactory;


    @Autowired
    public RequestsApiController(ObjectMapper objectMapper, IMService imService, ResponseInfoFactory responseInfoFactory) {
        this.objectMapper = objectMapper;
        this.imService = imService;
        this.responseInfoFactory = responseInfoFactory;
    }


    @RequestMapping(value="/request/_create", method = RequestMethod.POST)
    public ResponseEntity<IncidentResponse> requestsCreatePost(@Valid @RequestBody IncidentRequest request) throws IOException {
        IncidentRequest enrichedReq = imService.create(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        IncidentWrapper incidentWrapper = IncidentWrapper.builder().incident(enrichedReq.getIncident()).workflow(enrichedReq.getWorkflow()).build();
        IncidentResponse response = IncidentResponse.builder().responseInfo(responseInfo).IncidentWrappers(Collections.singletonList(incidentWrapper)).build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @RequestMapping(value="/request/_search", method = RequestMethod.POST)
    public ResponseEntity<IncidentResponse> requestsSearchPost(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                              @Valid @ModelAttribute RequestSearchCriteria criteria) {
    	
    	String tenantId = criteria.getTenantId();
        List<IncidentWrapper> incidentWrappers = imService.search(requestInfoWrapper.getRequestInfo(), criteria);
        //Map<String,Integer> dynamicData = imService.getDynamicData(tenantId);
        
        //int complaintsResolved = dynamicData.get(IMConstants.COMPLAINTS_RESOLVED);
	    //int averageResolutionTime = dynamicData.get(IMConstants.AVERAGE_RESOLUTION_TIME);
	    int complaintTypes = imService.getComplaintTypes();
        
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
        IncidentResponse response = IncidentResponse.builder().responseInfo(responseInfo).IncidentWrappers(incidentWrappers).build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @RequestMapping(value = "request/_plainsearch", method = RequestMethod.POST)
    public ResponseEntity<IncidentResponse> requestsPlainSearchPost(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper, @Valid @ModelAttribute RequestSearchCriteria requestSearchCriteria) {
        List<IncidentWrapper> incidentWrappers = imService.plainSearch(requestInfoWrapper.getRequestInfo(), requestSearchCriteria);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
        IncidentResponse response = IncidentResponse.builder().responseInfo(responseInfo).IncidentWrappers(incidentWrappers).build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @RequestMapping(value="/request/_update", method = RequestMethod.POST)
    public ResponseEntity<IncidentResponse> requestsUpdatePost(@Valid @RequestBody IncidentRequest request) throws IOException {
        IncidentRequest enrichedReq = imService.update(request);
        IncidentWrapper incidentWrapper = IncidentWrapper.builder().incident(enrichedReq.getIncident()).workflow(enrichedReq.getWorkflow()).build();
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        IncidentResponse response = IncidentResponse.builder().responseInfo(responseInfo).IncidentWrappers(Collections.singletonList(incidentWrapper)).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value="/request/_count", method = RequestMethod.POST)
    public ResponseEntity<CountResponse> requestsCountPost(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                           @Valid @ModelAttribute RequestSearchCriteria criteria) {
        Integer count = imService.count(requestInfoWrapper.getRequestInfo(), criteria);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
        CountResponse response = CountResponse.builder().responseInfo(responseInfo).count(count).build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

}
