package org.egov.field_planner.web.models;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IccTemplateSearchRequest {
    private RequestInfo RequestInfo;
    @NotBlank
    private String systemType;
    private String totalSystemCapacity;
}
