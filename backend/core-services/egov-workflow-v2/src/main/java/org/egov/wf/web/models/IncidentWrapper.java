package org.egov.wf.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncidentWrapper {


    @Valid
    @NonNull
    @JsonProperty("incident")
    private Incident incident = null;

//    @Valid
//    @JsonProperty("workflow")
//    private Workflow workflow = null;

}
