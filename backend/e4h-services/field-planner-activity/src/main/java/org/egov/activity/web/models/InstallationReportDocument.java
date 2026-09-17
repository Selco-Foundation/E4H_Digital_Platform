package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * One generated installation report (INSTALLATION_REPORT_BOM) of an approved activity facility,
 * with just enough context to tell the reports apart and fetch them from filestore.
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstallationReportDocument {

    @JsonProperty("filestoreId")
    private String filestoreId = null;

    @JsonProperty("facilityName")
    private String facilityName = null;

    @JsonProperty("projectId")
    private String projectId = null;

    @JsonProperty("fieldPlanId")
    private String fieldPlanId = null;
}
