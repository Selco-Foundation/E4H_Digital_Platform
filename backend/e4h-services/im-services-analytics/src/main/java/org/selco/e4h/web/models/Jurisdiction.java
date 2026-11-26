package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * One HRMS jurisdiction, as the v2 employee search returns it: a boundary, its level, and the roles
 * it applies to. No roles means every role the employee holds.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Jurisdiction {
    @JsonProperty("boundary")
    private String boundary;
    @JsonProperty("boundaryType")
    private String boundaryType;
    @JsonProperty("isActive")
    private Boolean isActive;
    @JsonProperty("roles")
    private List<Role> roles;
}
