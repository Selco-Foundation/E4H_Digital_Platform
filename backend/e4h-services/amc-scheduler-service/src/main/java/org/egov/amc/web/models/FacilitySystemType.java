package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One facility's system type as field-planner reports it. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacilitySystemType {

    private String facilityId;

    /** Null when the facility's installation plan never captured a system type. */
    private String systemType;
}
