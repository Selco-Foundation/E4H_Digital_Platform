package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {
    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("tenantId")
    private String tenantId;
}
