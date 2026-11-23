package org.egov.hrms.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka payload consumed by the persister to update the employee code in
 * eg_hrms_employee and the (encrypted) username in eg_user for the same uuid.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUsernameEvent {

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("uuid")
	private String uuid;

	@JsonProperty("code")
	private String code;

	@JsonProperty("username")
	private String username;

}
