package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitReportSubmissionRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("visitId")
    private String visitId;

    @JsonProperty("visitReport")
    private VisitReport visitReport;

    @JsonProperty("workflow")
    private Workflow workflow;
}
