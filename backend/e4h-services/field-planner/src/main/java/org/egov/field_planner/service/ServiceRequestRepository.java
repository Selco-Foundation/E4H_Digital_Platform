package org.egov.field_planner.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Repository
@Slf4j
public class ServiceRequestRepository {

    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;

    public ServiceRequestRepository(@Qualifier("objectMapper") ObjectMapper mapper, RestTemplate restTemplate) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

    public Object fetchResult(StringBuilder uri, Object request) {
        log.trace("Entering fetchResult method with POST request");
        log.debug("Calling external service at URL: {}", uri);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), request, Map.class);
            log.debug("External service call completed successfully");
        } catch (HttpClientErrorException e) {
            log.error("External service returned error for URL: {}, status: {}", uri, e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error during external service call to URL: {}", uri, e);
            throw new ServiceCallException();
        }
        log.trace("Exiting fetchResult method");
        return response;
    }

    public <T> T fetchResult(StringBuilder uri, Object request, TypeReference<T> responseType) {
        log.trace("Entering fetchResult method with POST request and type reference");
        log.debug("Calling external service at URL: {} with type: {}", uri, responseType.getType());
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            String jsonResponse = restTemplate.postForObject(uri.toString(), request, String.class);
            log.debug("External service call completed successfully");
            return mapper.readValue(jsonResponse, responseType);
        } catch (HttpClientErrorException e) {
            log.error("External service returned error for URL: {}, status: {}", uri, e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error during external service call to URL: {}", uri, e);
            throw new ServiceCallException();
        }
    }

    public Object fetchResult(StringBuilder uri) {
        log.trace("Entering fetchResult method with GET request");
        log.debug("Calling external service at URL: {}", uri);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            response = restTemplate.getForObject(uri.toString(), Map.class);
            log.debug("External service call completed successfully");
        } catch (HttpClientErrorException e) {
            log.error("External service returned error for URL: {}, status: {}", uri, e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error during external service call to URL: {}", uri, e);
            throw new ServiceCallException();
        }
        log.trace("Exiting fetchResult method");
        return response;
    }


}