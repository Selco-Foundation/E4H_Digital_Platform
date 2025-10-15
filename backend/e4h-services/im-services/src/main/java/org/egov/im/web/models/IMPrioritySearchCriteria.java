package org.egov.im.web.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IMPrioritySearchCriteria {
    private String tenantId;
    private String incidentType;
    private String incidentSubType;
    private String systemFunctional;

}
