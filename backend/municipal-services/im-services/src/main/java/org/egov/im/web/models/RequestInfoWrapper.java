package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import jakarta.validation.Valid;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestInfoWrapper {

	@NonNull
	@Valid
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;
}
