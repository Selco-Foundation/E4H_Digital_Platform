package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment {

    @JsonProperty("commentId")
    UUID cmtId;

    @JsonProperty("commentMessage")
    String cmtMsg;

    @JsonProperty("assetType")
    String assetType;

    @JsonProperty("transactionId")
    String transactionId;

    @JsonProperty("auditDetails")
    AuditDetails auditDetails;
}
