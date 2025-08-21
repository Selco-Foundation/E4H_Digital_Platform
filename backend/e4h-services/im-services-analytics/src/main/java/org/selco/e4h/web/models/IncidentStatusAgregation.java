package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidentStatusAgregation {

    @JsonProperty("tenantId")
    String tenantId;

    @JsonProperty("totalOccurences")
    int totalOccurences;

    @JsonProperty("totalOpenOccurrences")
    int totalOpenOccurrences;

    @JsonProperty("totalOpenOccurrences")
    int totalCloseOccurrences;

    @JsonProperty("systemFunctional")
    String systemFunctional;
}
