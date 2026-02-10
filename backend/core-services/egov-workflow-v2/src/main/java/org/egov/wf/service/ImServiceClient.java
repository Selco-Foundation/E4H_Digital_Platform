package org.egov.wf.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.ServiceCallException;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Client to call IM (Incident Management) service APIs from workflow.
 * Calls /request/_search (POST) with RequestInfo in body and RequestSearchCriteria as query params.
 */
@Service
@Slf4j
public class ImServiceClient {

    private final WorkflowConfig config;
    private final ServiceRequestRepository serviceRequestRepository;

    @Autowired
    public ImServiceClient(WorkflowConfig config, ServiceRequestRepository serviceRequestRepository) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    /**
     * Calls IM service request search API: POST /v2/request/_search
     *
     * @param requestInfo request info for the call
     * @param criteria    search criteria as key-value (e.g. tenantId, offset, limit, applicationStatus).
     *                    These are sent as query parameters.
     * @return response as Map (contains responseInfo and IncidentWrappers); null if call fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> requestSearch(RequestInfo requestInfo, Map<String, Object> criteria) {
        String baseUrl = config.getImHost() + config.getImRequestSearchEndpoint();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (criteria != null) {
            criteria.forEach((key, value) -> {
                if (value != null) {
                    params.add(key, value.toString());
                }
            });
        }
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl).queryParams(params).build().toUriString();

        Map<String, Object> body = new HashMap<>();
        body.put("RequestInfo", requestInfo);

        try {
            Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), body);
            return response instanceof Map ? (Map<String, Object>) response : null;
        } catch (ServiceCallException e) {
            log.warn("IM request search call failed: {}", e.getMessage());
            throw e;
        }
    }
}
