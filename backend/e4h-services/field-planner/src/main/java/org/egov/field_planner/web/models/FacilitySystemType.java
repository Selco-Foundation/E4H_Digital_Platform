package org.egov.field_planner.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One facility's system type, as captured on the installation plan the facility is linked to.
 *
 * <p>Distinct from {@link SystemTypeCapacity}, which answers "which systemType/capacity combinations
 * does this installation plan contain" and is deliberately deduplicated across facilities. This
 * carries the facilityId so a caller resolving many facilities at once can tell whose value is whose.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacilitySystemType {

    private String facilityId;

    /**
     * Null when the facility is linked to an installation plan that never captured a system type.
     * A facility with no installation plan at all is absent from the response entirely, so callers
     * can distinguish "no plan" from "plan with nothing recorded".
     */
    private String systemType;
}
