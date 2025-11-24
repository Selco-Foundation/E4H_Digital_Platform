package org.egov.amc.web.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class TokenSearchCriteria {
    private String uuid;
    private String tenantId;
}
