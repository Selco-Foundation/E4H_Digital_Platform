package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstallationReportDocumentResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("InstallationReportDocuments")
    private List<InstallationReportDocument> installationReportDocuments;

    @JsonProperty("TotalCount")
    private Integer totalCount;
}
