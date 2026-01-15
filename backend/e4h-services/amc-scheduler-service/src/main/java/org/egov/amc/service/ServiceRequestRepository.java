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
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), request, Map.class);
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during service call: ", e);
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
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            String jsonResponse = restTemplate.postForObject(uri.toString(), request, String.class);
            return mapper.readValue(jsonResponse, responseType);
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during service call: ", e);
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
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            response = restTemplate.getForObject(uri.toString(), Map.class);
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during service call: ", e);
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