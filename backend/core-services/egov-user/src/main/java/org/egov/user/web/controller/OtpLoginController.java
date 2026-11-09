package org.egov.user.web.controller;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.domain.service.otplogin.OtpLoginService;
import org.egov.user.domain.service.otplogin.OtpLoginVerification;
import org.egov.user.web.contract.otplogin.OtpLoginSendRequest;
import org.egov.user.web.contract.otplogin.OtpLoginSendResponse;
import org.egov.user.web.contract.otplogin.OtpLoginVerifyRequest;
import org.egov.user.web.contract.otplogin.OtpLoginVerifyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login with registered phone number + OTP (facility users and CRM). Both endpoints are called before
 * login, so they must be open endpoints in the gateway.
 */
@RestController
@RequestMapping("/otp-login/v1")
public class OtpLoginController {

    private final OtpLoginService otpLoginService;

    public OtpLoginController(OtpLoginService otpLoginService) {
        this.otpLoginService = otpLoginService;
    }

    @PostMapping("/_send")
    public OtpLoginSendResponse send(@RequestBody @Valid OtpLoginSendRequest request) {
        otpLoginService.sendOtp(request.getTenantId(), request.getMobileNumber(), request.getRequestInfo());
        return new OtpLoginSendResponse(ok(), otpLoginService.getResendCooldownSeconds(),
                otpLoginService.getOtpValiditySeconds());
    }

    @PostMapping("/_verify")
    public OtpLoginVerifyResponse verify(@RequestBody @Valid OtpLoginVerifyRequest request) {
        OtpLoginVerification verification = otpLoginService.verifyOtp(request.getTenantId(),
                request.getMobileNumber(), request.getOtp());
        return new OtpLoginVerifyResponse(ok(), verification.getLoginTicket(),
                verification.getLoginTicketValiditySeconds(), verification.getAccounts());
    }

    private static ResponseInfo ok() {
        return ResponseInfo.builder().status(String.valueOf(HttpStatus.OK.value())).build();
    }
}
