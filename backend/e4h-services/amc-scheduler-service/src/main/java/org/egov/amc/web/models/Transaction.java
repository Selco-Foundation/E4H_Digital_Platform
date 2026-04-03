package org.egov.amc.web.models;

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
public class Transaction {

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("processInstanceId")
    private String processInstanceId;

    @JsonProperty("visitReport")
    private VisitReport visitReport;

    @JsonProperty("visitId")
    private String visitId;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
