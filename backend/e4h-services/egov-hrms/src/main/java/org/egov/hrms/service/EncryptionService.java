package org.egov.hrms.service;

import java.util.Collections;
import java.util.List;

import org.egov.hrms.config.PropertiesManager;
import org.egov.hrms.repository.RestCallRepository;
import org.egov.hrms.utils.ErrorConstants;
import org.egov.hrms.web.contract.EncryptionRequest;
import org.egov.hrms.web.contract.EncryptionRequestData;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EncryptionService {

	private static final String ENCRYPTION_TYPE_NORMAL = "Normal";

	// egov-enc-service is always called with this tenantId, irrespective of the tenantId of the caller.
	private static final String ENCRYPTION_SERVICE_TENANT_ID = "pg";

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private RestCallRepository restCallRepository;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Encrypts the given plain text value by calling egov-enc-service and returns the encrypted value.
	 *
	 * @param value
	 * @return encrypted value
	 */
	public String encrypt(String value) {
		log.trace("EncryptionService.encrypt invoked");
		StringBuilder uri = new StringBuilder();
		uri.append(propertiesManager.getEncServiceHost()).append(propertiesManager.getEncServiceEncryptEndpoint());

		EncryptionRequestData requestData = EncryptionRequestData.builder()
				.tenantId(ENCRYPTION_SERVICE_TENANT_ID)
				.type(ENCRYPTION_TYPE_NORMAL)
				.value(value)
				.build();
		EncryptionRequest request = EncryptionRequest.builder()
				.encryptionRequests(Collections.singletonList(requestData))
				.build();

		try {
			log.debug("Calling encryption service, endpoint: {}", uri.toString());
			Object response = restCallRepository.fetchEncServiceResult(uri, request);
			List<String> encryptedValues = objectMapper.convertValue(response, new TypeReference<List<String>>() {});
			log.debug("Encryption service call completed successfully");
			return encryptedValues.get(0);
		} catch (Exception e) {
			log.error("Exception while encrypting value", e);
			throw new CustomException(ErrorConstants.HRMS_ENCRYPTION_FAILED_CODE, ErrorConstants.HRMS_ENCRYPTION_FAILED_MSG);
		}
	}

}
