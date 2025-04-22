package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SendBackReason {

    @JsonProperty("reason")
    @NotNull
    private String reason;

    @JsonProperty("subReason")
    private String subReason;
}
