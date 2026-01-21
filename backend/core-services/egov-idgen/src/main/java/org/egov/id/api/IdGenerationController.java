package org.egov.id.api;

import lombok.extern.slf4j.Slf4j;
import org.egov.id.model.IdGenerationRequest;
import org.egov.id.model.IdGenerationResponse;
import org.egov.id.service.IdGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * api's related to the IdGeneration Controller
 * 
 * @author Pavan Kumar Kamma
 */
@RestController
@RequestMapping(path = "/id/")
@Slf4j
public class IdGenerationController {

	@Autowired
	IdGenerationService idGenerationService;

	/**
	 * description: generate unique ID for property
	 * 
	 * @param IdGenerationRequest
	 * @return IdGenerationResponse
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, path = "_generate")
	public IdGenerationResponse generateIdResponse(
			@RequestBody @Valid IdGenerationRequest idGenerationRequest)
			throws Exception {
		log.trace("generateIdResponse method invoked");
		
		try {
			log.info("Received ID generation request with {} id requests", 
					idGenerationRequest != null && idGenerationRequest.getIdRequests() != null 
						? idGenerationRequest.getIdRequests().size() : 0);
			
			IdGenerationResponse idGenerationResponse = idGenerationService
					.generateIdResponse(idGenerationRequest);

			log.info("Successfully generated {} IDs", 
					idGenerationResponse != null && idGenerationResponse.getIdResponses() != null 
						? idGenerationResponse.getIdResponses().size() : 0);
			
			return idGenerationResponse;
		} catch (Exception e) {
			log.error("Error occurred while generating ID response", e);
			throw e;
		}
	}

}
