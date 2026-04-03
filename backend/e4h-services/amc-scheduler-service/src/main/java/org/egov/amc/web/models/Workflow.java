package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workflow {
    @JsonProperty("action")
    private String action = null;
    @JsonProperty("comment")
    private String comment = null;
    @JsonProperty("documents")
    private @Valid List<Document> documents = null;
    @JsonProperty("assignes")
    private @Valid List<String> assignes = null;
    @JsonProperty("rating")
    private Integer rating = null;
    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;
}

