package org.egov.field_planner.web.models;

import lombok.AllArgsConstructor;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IccTemplateResponse {

    private ResponseInfo responseInfo;

    private List<ICCReportUploadResponse> iccTemplates;
}
