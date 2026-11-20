package org.egov.user.domain.service.otplogin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * An account the verified phone number can log in as. The client sends {@code userName} back to
 * /oauth/token together with the login ticket.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpLoginAccount {
    private String userName;
    private String name;
    private String tenantId;
    private List<String> roleCodes;
}
