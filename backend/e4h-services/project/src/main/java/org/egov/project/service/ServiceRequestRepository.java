package org.egov.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
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
        log.trace("Entering fetchResult (POST) for URI: {}", uri);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            log.debug("Making POST request to external service: {}", uri);
            response = restTemplate.postForObject(uri.toString(), request, Map.class);
            log.debug("Successfully received response from external service");
        } catch (HttpClientErrorException e) {
            log.error("External service returned error for URI: {}. Status: {}, Response: {}", uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during service call: ", e);
            throw new ServiceCallException("Server error while fetching from service: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Network error during service call: ", e);
            throw new ServiceCallException("Network error while fetching from service: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Error during service call: ", e);
            throw new ServiceCallException("Error while fetching from service: " + e.getMessage());
        }
        log.trace("Exiting fetchResult (POST)");
        return response;
    }

    public <T> T fetchResult(StringBuilder uri, Object request, TypeReference<T> responseType) {
        log.trace("Entering fetchResult (POST with TypeReference) for URI: {}", uri);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            log.debug("Making POST request to external service with type reference: {}", uri);
            String jsonResponse = restTemplate.postForObject(uri.toString(), request, String.class);
            return mapper.readValue(jsonResponse, responseType);
        } catch (JsonProcessingException e) {
            log.error("JSON processing error during service call: ", e);
            throw new ServiceCallException("Error parsing response: " + e.getMessage());
        } catch (HttpClientErrorException e) {
            log.error("External service returned error for URI: {}. Status: {}, Response: {}", uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during service call: ", e);
            throw new ServiceCallException("Server error while fetching from service: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Network error during service call: ", e);
            throw new ServiceCallException("Network error while fetching from service: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Error during service call: ", e);
            throw new ServiceCallException("Error while fetching from service: " + e.getMessage());
        }
    }

    public Object fetchResult(StringBuilder uri) {
        log.trace("Entering fetchResult (GET) for URI: {}", uri);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            log.debug("Making GET request to external service: {}", uri);
            response = restTemplate.getForObject(uri.toString(), Map.class);
            log.debug("Successfully received response from external service");
        } catch (HttpClientErrorException e) {
            log.error("External service returned error for URI: {}. Status: {}, Response: {}", uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during service call: ", e);
            throw new ServiceCallException("Server error while fetching from service: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Network error during service call: ", e);
            throw new ServiceCallException("Network error while fetching from service: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Error during service call: ", e);
            throw new ServiceCallException("Error while fetching from service: " + e.getMessage());
        }
        log.trace("Exiting fetchResult (GET)");
        return response;
    }


}