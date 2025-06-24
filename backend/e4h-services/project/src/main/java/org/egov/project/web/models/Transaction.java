package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("processInstanceId")
    private String processInstanceId;

    @JsonProperty("comments")
    private List<Comment> comments;

    @JsonProperty("projectId")
    private String projectId;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
