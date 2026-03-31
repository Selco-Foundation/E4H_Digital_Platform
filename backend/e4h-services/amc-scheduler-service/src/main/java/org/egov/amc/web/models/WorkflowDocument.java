package org.egov.amc.web.models;

import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDocument {
    private String documentType;
    private String fileStoreId;
    private String fileName;
}

