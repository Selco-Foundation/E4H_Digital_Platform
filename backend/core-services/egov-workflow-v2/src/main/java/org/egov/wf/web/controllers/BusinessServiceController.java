package org.egov.wf.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.wf.service.BusinessMasterService;
import org.egov.wf.util.ResponseInfoFactory;
import org.egov.wf.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/egov-wf")
@Slf4j
public class BusinessServiceController {

    private BusinessMasterService businessMasterService;

    private final ResponseInfoFactory responseInfoFactory;

    private ObjectMapper mapper;

    @Autowired
    public BusinessServiceController(BusinessMasterService businessMasterService, ResponseInfoFactory responseInfoFactory,
                                     ObjectMapper mapper) {
        this.businessMasterService = businessMasterService;
        this.responseInfoFactory = responseInfoFactory;
        this.mapper = mapper;
    }


    /**
     * Controller for creating BusinessService
     * @param businessServiceRequest The BusinessService request for create
     * @return The created object
     */
    @RequestMapping(value="/businessservice/_create", method = RequestMethod.POST)
    public ResponseEntity<BusinessServiceResponse> create(@Valid @RequestBody BusinessServiceRequest businessServiceRequest) {
        log.info("Received create business service request for {} business service(s)", 
                businessServiceRequest.getBusinessServices() != null ? businessServiceRequest.getBusinessServices().size() : 0);
        try {
            List<BusinessService> businessServices = businessMasterService.create(businessServiceRequest);
            BusinessServiceResponse response = BusinessServiceResponse.builder().businessServices(businessServices)
                    .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(businessServiceRequest.getRequestInfo(),true))
                    .build();
            log.info("Successfully created business service(s)");
            return new ResponseEntity<>(response,HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error creating business service", e);
            throw e;
        }
    }


    /**
     * Controller for searching BusinessService api
     * @param searchCriteria Object containing the search params
     * @param requestInfoWrapper The requestInfoWrapper object containing requestInfo
     * @return List of businessServices from db based on search params
     */
    @RequestMapping(value="/businessservice/_search", method = RequestMethod.POST)
    public ResponseEntity<BusinessServiceResponse> search(@Valid @ModelAttribute BusinessServiceSearchCriteria searchCriteria,
                                                          @Valid @RequestBody RequestInfoWrapper requestInfoWrapper) {
        log.info("Received business service search request - tenantId: {}", searchCriteria.getTenantId());
        try {
            List<BusinessService> businessServices = businessMasterService.search(searchCriteria);
            BusinessServiceResponse response = BusinessServiceResponse.builder().businessServices(businessServices)
                    .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(),true))
                    .build();
            log.info("Business service search completed successfully, returning {} result(s)", 
                    businessServices != null ? businessServices.size() : 0);
            return new ResponseEntity<>(response,HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing business service search request", e);
            throw e;
        }
    }

    @RequestMapping(value="/businessservice/_update", method = RequestMethod.POST)
    public ResponseEntity<BusinessServiceResponse> update(@Valid @RequestBody BusinessServiceRequest businessServiceRequest) {
        log.info("Received update business service request for {} business service(s)", 
                businessServiceRequest.getBusinessServices() != null ? businessServiceRequest.getBusinessServices().size() : 0);
        try {
            List<BusinessService> businessServices = businessMasterService.update(businessServiceRequest);
            BusinessServiceResponse response = BusinessServiceResponse.builder().businessServices(businessServices)
                    .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(businessServiceRequest.getRequestInfo(),true))
                    .build();
            log.info("Successfully updated business service(s)");
            return new ResponseEntity<>(response,HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error updating business service", e);
            throw e;
        }
    }




}
