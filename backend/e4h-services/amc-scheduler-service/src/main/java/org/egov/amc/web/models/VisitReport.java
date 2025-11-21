package org.egov.amc.web.models;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitReport {
    private String schemaCode;
    private String version;
    private String submittedBy;
    private Long submittedAt;
    private String otpReference;
    private Long otpVerifiedAt;
    private Map<String, Object> responses;
    private List<VisitReportDocument> documents;
    private Map<String, Object> additionalDetails;
}
