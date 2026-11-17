package org.egov.hrms.web.contract;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncryptionRequest {

	@JsonProperty("encryptionRequests")
	private List<EncryptionRequestData> encryptionRequests;

}
