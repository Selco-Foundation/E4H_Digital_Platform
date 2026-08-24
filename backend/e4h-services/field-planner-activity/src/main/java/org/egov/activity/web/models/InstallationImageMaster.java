package org.egov.activity.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * One entry of the common-masters.InstallationImages MDMS master.
 * <p>
 * Each entry declares which system types require that image, and in which position it appears in
 * that system's installation report - hence {@link #orderBySystemType}, keyed by system type code
 * (DC, AC_ON_GRID_THREE_PHASE, ...). An entry absent from the map is not required for that system
 * and must not be rendered at all. Orders are fractional in the master (14.1, 14.2, 16.3), so they
 * are held as Double.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallationImageMaster {

    private String code;

    private String description;

    private Boolean active;

    private Map<String, Double> orderBySystemType;
}
