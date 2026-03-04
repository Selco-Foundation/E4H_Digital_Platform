package org.egov.amc.service;

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

import com.fasterxml.jackson.core.JsonProcessingException;

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
        log.trace("Entering fetchResult method, URI: {}", uri);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            log.debug("Calling external service at URI: {}", uri);
            response = restTemplate.postForObject(uri.toString(), request, Map.class);
            log.debug("External service call successful for URI: {}", uri);
        } catch (HttpClientErrorException e) {
            log.error("External service call failed with HTTP error, URI: {}, status: {}", uri, e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during service call: ", e);
            throw new ServiceCallException("Server error while fetching from service: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Connection error during service call: ", e);
            throw new ServiceCallException("Connection error while fetching from service: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Error during service call: ", e);
            throw new ServiceCallException("Error while fetching from service: " + e.getMessage());
        }
        return response;
    }

    public <T> T fetchResult(StringBuilder uri, Object request, TypeReference<T> responseType) {
        log.trace("Entering fetchResult method with type reference, URI: {}", uri);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            log.debug("Calling external service at URI: {} with type reference", uri);
            String jsonResponse = restTemplate.postForObject(uri.toString(), request, String.class);
            log.debug("External service call successful for URI: {}", uri);
            return mapper.readValue(jsonResponse, responseType);
        } catch (HttpClientErrorException e) {
            log.error("External service call failed with HTTP error, URI: {}, status: {}", uri, e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during service call: ", e);
            throw new ServiceCallException("Server error while fetching from service: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Connection error during service call: ", e);
            throw new ServiceCallException("Connection error while fetching from service: " + e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("JSON processing error during service call: ", e);
            throw new ServiceCallException("Error parsing response: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Error during service call: ", e);
            throw new ServiceCallException("Error while fetching from service: " + e.getMessage());
        }
    }

    public Object fetchResult(StringBuilder uri) {
        log.trace("Entering fetchResult method (GET), URI: {}", uri);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            log.debug("Calling external service (GET) at URI: {}", uri);
            response = restTemplate.getForObject(uri.toString(), Map.class);
            log.debug("External service call (GET) successful for URI: {}", uri);
        } catch (HttpClientErrorException e) {
            log.error("External service call (GET) failed with HTTP error, URI: {}, status: {}", uri, e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during service call: ", e);
            throw new ServiceCallException("Server error while fetching from service: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Connection error during service call: ", e);
            throw new ServiceCallException("Connection error while fetching from service: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Error during service call: ", e);
            throw new ServiceCallException("Error while fetching from service: " + e.getMessage());
        }
        return response;
    }


}