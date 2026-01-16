package org.egov.wf.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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


	public Object fetchResult(StringBuilder uri, Object request) {
		log.trace("Entering fetchResult method");
		log.debug("Calling external service: {}", uri.toString());
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Object response = null;
		try {
			response = restTemplate.postForObject(uri.toString(), request, Map.class);
			log.debug("Successfully received response from external service: {}", uri.toString());
		}catch(HttpClientErrorException e) {
			log.error("External service threw an exception for URI: {}", uri.toString(), e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		}catch(Exception e) {
			log.error("Exception while fetching from external service: {}", uri.toString(), e);
		}
		log.trace("Exiting fetchResult method");
		return response;
	}
}
