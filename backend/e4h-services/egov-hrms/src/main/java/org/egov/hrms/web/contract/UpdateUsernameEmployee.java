package org.egov.hrms.web.contract;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUsernameEmployee {

	@NotEmpty
	@JsonProperty("tenantId")
	private String tenantId;

	@NotEmpty
	@JsonProperty("uuid")
	private String uuid;

	@NotEmpty
	@JsonProperty("code")
	private String code;

}
