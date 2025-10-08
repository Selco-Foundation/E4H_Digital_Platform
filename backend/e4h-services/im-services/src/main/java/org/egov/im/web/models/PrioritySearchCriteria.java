package org.egov.im.web.models;

import lombok.Data;

@Data
public class PrioritySearchCriteria {
    private String incidentType;
    private String incidentSubType;
    private String systemFunctional;
}
