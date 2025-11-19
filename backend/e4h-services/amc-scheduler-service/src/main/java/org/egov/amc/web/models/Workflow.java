package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workflow {
    private String action;
    private String comment;
    private List<String> assignees;
    private @Valid List<Document> documents = null;
    private Map<String, Object> additionalDetails;
}

