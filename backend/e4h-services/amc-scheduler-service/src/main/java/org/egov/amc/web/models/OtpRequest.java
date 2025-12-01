package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpRequest {
    @Valid
    private Otp otp;
}


