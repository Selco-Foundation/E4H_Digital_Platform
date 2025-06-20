package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {

    @JsonProperty("commentId")
    String cmtId;

    @JsonProperty("commentMessage")
    String cmtMsg;

    @JsonProperty("assetType")
    String assetType;

    @JsonProperty("transactionId")
    String transactionId;

    @JsonProperty("auditDetails")
    AuditDetails auditDetails;
}
