package org.egov.user.web.contract.otplogin;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.user.domain.service.otplogin.OtpLoginAccount;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The client then calls /user/oauth/token with grant_type=password, loginMethod=OTP, userType=EMPLOYEE,
 * tenantId, username = one of accounts[].userName and password = loginTicket.
 * With a single account it can do so straight away; otherwise the user picks the account first.
 */
@Getter
@AllArgsConstructor
public class OtpLoginVerifyResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("loginTicket")
    private String loginTicket;

    @JsonProperty("loginTicketValiditySeconds")
    private int loginTicketValiditySeconds;

    @JsonProperty("accounts")
    private List<OtpLoginAccount> accounts;
}
