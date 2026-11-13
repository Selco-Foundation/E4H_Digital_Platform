package org.egov.user.web.contract.otplogin;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpLoginVerifyRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @NotNull
    @JsonProperty("tenantId")
    private String tenantId;

    @NotNull
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @NotNull
    @JsonProperty("otp")
    private String otp;
}
