package org.egov.inbox.repository;

import java.util.List;
import java.util.Map;

import org.egov.inbox.util.ESAuthUtil;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ServiceRequestRepository {
	private ObjectMapper mapper;

	@Autowired
	private RestTemplate restTemplate;

	private ESAuthUtil esAuthUtil;

	@Autowired
	public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate, ESAuthUtil esAuthUtil) {
		this.mapper = mapper;
		this.restTemplate = restTemplate;
		this.esAuthUtil = esAuthUtil;
	}
	/**
	 * fetchResult form the different services based on the url and request object
	 * @param uri
	 * @param request
	 * @return
	 */
	public Object fetchResult(StringBuilder uri, Object request) {
		Object response = null;
		//log.debug("URI: " + uri.toString());
		try {
			try {
				log.info("Request: " + mapper.writeValueAsString(request));
			} catch (JsonProcessingException e) {
				log.debug("Error serializing request for logging: ", e);
			}
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
	

	public Object fetchESResult(StringBuilder uri, Object request) {
		Object response = null;
		log.debug("URI: " + uri.toString());
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", esAuthUtil.getESEncodedCredentials());
			final HttpEntity<Object> entity = new HttpEntity<>(request, headers);
			try {
				log.info("Request: " + mapper.writeValueAsString(request));
				log.info("Entity: " + mapper.writeValueAsString(entity));
			} catch (JsonProcessingException e) {
				log.debug("Error serializing request/entity for logging: ", e);
			}
			response = restTemplate.postForObject(uri.toString(), entity, Map.class);
		} catch (HttpClientErrorException e) {
			log.error("HTTP client error during ES service call: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (HttpServerErrorException e) {
			log.error("HTTP server error during ES service call: ", e);
			throw new ServiceCallException("Server error while fetching from ES service: " + e.getResponseBodyAsString());
		} catch (ResourceAccessException e) {
			log.error("Network error during ES service call: ", e);
			throw new ServiceCallException("Network error while fetching from ES service: " + e.getMessage());
		} catch (RestClientException e) {
			log.error("Error during ES service call: ", e);
			throw new ServiceCallException("Error while fetching from ES service: " + e.getMessage());
		}

		return response;
	}
	/**
	 * fetchResult form the different services based on the url and request object
	 * @param uri
	 * @param request
	 * @return
	 */
	public List fetchListResult(StringBuilder uri, Object request) {
		List response = null;
		//log.debug("URI: " + uri.toString());
		try {
			//log.debug("Request: " + mapper.writeValueAsString(request));
			response = restTemplate.postForObject(uri.toString(), request, List.class);
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
	/**
	 * fetchResult form the different services based on the url and request object
	 * @param uri
	 * @param request
	 * @return
	 */
	public Integer fetchIntResult(StringBuilder uri, Object request) {
		Integer response = null;
		//log.debug("URI: " + uri.toString());
		try {
			//log.debug("Request: " + mapper.writeValueAsString(request));
			response = restTemplate.postForObject(uri.toString(), request, Integer.class);
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

}
