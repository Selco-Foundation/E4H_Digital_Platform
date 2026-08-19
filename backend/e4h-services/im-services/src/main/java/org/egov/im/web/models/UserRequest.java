package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.util.IMConstants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Setter
@ToString
@Builder
public class UserRequest {

	@NotNull
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@NotNull
	@JsonProperty("User")
	private User user;

	/**
	 * Front-end the login was reported from. Optional — when the caller omits it the analytics
	 * event carries a null application rather than the call being rejected, so callers that have
	 * not been updated yet still get their logins counted. When present it must be one of
	 * {@link IMConstants#APPLICATION_PATTERN} ({@code @Pattern} does not apply to null).
	 */
	@Pattern(regexp = IMConstants.APPLICATION_PATTERN,
			message = "application must be one of SAURA_EMITRA, FIELD_ASSIST, MANAGEMENT_HUB")
	@JsonProperty("application")
	private String application;

}