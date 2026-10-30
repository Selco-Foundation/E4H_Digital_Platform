package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.SafeHtml;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OnHoldReason {

    @SafeHtml
    @JsonProperty("reason")
    private String reason;
}
