package org.egov.user.domain.service.otplogin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtpLoginVerification {
    private final String loginTicket;
    private final int loginTicketValiditySeconds;
    private final List<OtpLoginAccount> accounts;
}
