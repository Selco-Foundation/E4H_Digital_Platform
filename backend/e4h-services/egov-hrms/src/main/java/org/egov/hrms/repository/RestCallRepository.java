package org.egov.hrms.repository;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class RestCallRepository {

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Fetches results from the given API and request and handles errors.
	 * 
	 * @param uri
	 * @param request
	 * @return Object
	 * @author vishal
	 */
	public Object fetchResult(StringBuilder uri, Object request) {
		log.trace("RestCallRepository.fetchResult invoked for URI: {}", uri.toString());
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Object response = null;
		try {
			log.debug("Calling external service endpoint: {}", uri.toString());
			response = restTemplate.postForObject(uri.toString(), request, Map.class);
			log.debug("External service call completed successfully for endpoint: {}", uri.toString());
		} catch (HttpClientErrorException e) {
			log.error("External service returned HTTP error for endpoint: {}, status: {}", 
					uri.toString(), e.getStatusCode(), e);
			if (!StringUtils.isEmpty(e.getResponseBodyAsString())) {
				throw new CustomException("EXTERNAL_SERVICE_EXCEPTION", e.getResponseBodyAsString());
			}
		} catch (Exception e) {
			log.error("Exception while calling external service endpoint: {}", uri.toString(), e);
		}

		return response;

	}

}
