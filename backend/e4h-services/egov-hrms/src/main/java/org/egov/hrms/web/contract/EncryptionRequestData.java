package org.egov.hrms.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncryptionRequestData {

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("type")
	private String type;

	@JsonProperty("value")
	private Object value;

}
