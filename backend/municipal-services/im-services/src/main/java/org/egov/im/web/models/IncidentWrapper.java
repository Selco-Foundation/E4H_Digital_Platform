package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import jakarta.validation.Valid;
import org.egov.im.web.models.workflow.Workflow;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncidentWrapper {


    @Valid
    @NonNull
    @JsonProperty("incident")
    private Incident incident = null;

    @Valid
    @JsonProperty("workflow")
    private Workflow workflow = null;

}
