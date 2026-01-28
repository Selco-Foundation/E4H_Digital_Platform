package org.egov.asset.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import digit.models.coremodels.IdGenerationRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.egov.asset.config.ServiceConstants.EXTERNAL_SERVICE_EXCEPTION;
import static org.egov.asset.config.ServiceConstants.SEARCHER_SERVICE_EXCEPTION;

@Repository
@Slf4j
public class ServiceRequestRepository {

    private ObjectMapper mapper;

    private RestTemplate restTemplate;


    @Autowired
    public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }


    public <T> T fetchResult(StringBuilder uri, Object request, Class<T> responseType) {
        log.trace("ServiceRequestRepository::fetchResult called");
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        String uriString = uri.toString();
        log.debug("Fetching result from service | uri={} responseType={}", uriString, responseType.getSimpleName());
        try {
            T result = restTemplate.postForObject(uriString, request, responseType);
            log.debug("Successfully fetched result from service | uri={}", uriString);
            return result;
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error while fetching from service | uri={} statusCode={} error={}", 
                    uriString, e.getStatusCode(), e.getMessage(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error while fetching from service | uri={} error={}", uriString, e.getMessage(), e);
            throw new ServiceCallException("Error while fetching from service: " + e.getMessage());
        }
    }
}