package org.egov.hrms.web.contract;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUsernameRequest {

	@NotNull
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@NotNull
	@Valid
	@JsonProperty("employee")
	private UpdateUsernameEmployee employee;

}
