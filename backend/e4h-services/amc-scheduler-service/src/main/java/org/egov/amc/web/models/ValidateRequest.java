package org.egov.amc.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Getter
public class ValidateRequest {
    private String tenantId;
    private String otp;
    private String identity;
}
