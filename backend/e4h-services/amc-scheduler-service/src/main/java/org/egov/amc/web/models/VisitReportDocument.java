package org.egov.amc.web.models;

import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitReportDocument {
    private String documentType;
    private String fileStoreId;
    private String fileName;
    private Map<String, Object> additionalDetails;
}

