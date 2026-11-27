package org.egov.user.web.contract.otplogin;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtpLoginSendResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    /** Seconds before the client may enable "Resend OTP". */
    @JsonProperty("resendAfterSeconds")
    private int resendAfterSeconds;

    @JsonProperty("otpValiditySeconds")
    private int otpValiditySeconds;
}
