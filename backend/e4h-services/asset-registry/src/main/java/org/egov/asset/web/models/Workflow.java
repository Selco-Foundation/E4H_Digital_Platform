package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.models.coremodels.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Workflow {
    private String action;
    private List<Document> verificationDocuments;
    private String comments;
    @JsonProperty("assignees")
    private List<String> assignees;
}
