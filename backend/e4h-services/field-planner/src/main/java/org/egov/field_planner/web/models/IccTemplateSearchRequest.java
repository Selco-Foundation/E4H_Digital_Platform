package org.egov.field_planner.web.models;

import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IccTemplateSearchRequest {
    private RequestInfo RequestInfo;
    private String systemType;
    private String totalSystemCapacity;
}
