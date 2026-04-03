package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Otp {
    @Size(max = 128)
    private String otp;
    @JsonProperty("UUID")
    @Size(max = 36)
    private String uuid;
    @Size(max = 100)
    private String identity;
    @Size(max = 256)
    private String tenantId;
    @JsonProperty("isValidationSuccessful")
    private boolean validationSuccessful;
}
