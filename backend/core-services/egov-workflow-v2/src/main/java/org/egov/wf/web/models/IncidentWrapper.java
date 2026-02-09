package org.egov.wf.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidentWrapper {


    @Valid
    @NonNull
    @JsonProperty("incident")
    private Incident incident = null;

//    @Valid
//    @JsonProperty("workflow")
//    private Workflow workflow = null;

}
