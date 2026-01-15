package org.egov.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;


@Repository
@Slf4j
public class ServiceRequestRepository {

    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;

	@Autowired
	public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate) {
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
			log.error("Network error during service call: ", e);
			throw new ServiceCallException("Network error while fetching from service: " + e.getMessage());
		} catch (RestClientException e) {
			log.error("Error during service call: ", e);
			throw new ServiceCallException("Error while fetching from service: " + e.getMessage());
		}

		return response;
	}

	public Object fetchEncServiceResult(StringBuilder uri, Object request) {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Object response = null;
		try {
			response = restTemplate.postForObject(uri.toString(), request, Object.class);
		} catch (HttpClientErrorException e) {
			log.error("HTTP client error during encryption service call: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (HttpServerErrorException e) {
			log.error("HTTP server error during encryption service call: ", e);
			throw new ServiceCallException("Server error while fetching from encryption service: " + e.getResponseBodyAsString());
		} catch (ResourceAccessException e) {
			log.error("Network error during encryption service call: ", e);
			throw new ServiceCallException("Network error while fetching from encryption service: " + e.getMessage());
		} catch (RestClientException e) {
			log.error("Error during encryption service call: ", e);
			throw new ServiceCallException("Error while fetching from encryption service: " + e.getMessage());
		}

		return Objects.requireNonNull(response,
				() -> "External service returned empty response for URI: " + uri);
	}
}
