package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.models.AuditDetails;

import javax.validation.Valid;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ICCReportUploadResponse {
    private String id;
    private String fileStoreId;
    private String systemType;
    private String totalSystemCapacity;
}
