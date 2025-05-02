package org.egov.inbox.web.model.dss;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseDto {

    @Valid
    @JsonProperty("totalAmountPaid")
    private BigDecimal totalAmountPaid;

    @Valid
    @JsonProperty("activeConnections")
    private BigDecimal activeConnections;
}
