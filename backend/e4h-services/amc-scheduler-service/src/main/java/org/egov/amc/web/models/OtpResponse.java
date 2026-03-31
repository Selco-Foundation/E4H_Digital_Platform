package org.egov.amc.web.models;

import lombok.*;
import org.egov.common.contract.response.ResponseInfo;

@Builder
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Setter
@ToString
public class OtpResponse {
    private ResponseInfo responseInfo;
    private Otp otp;
}


