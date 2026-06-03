package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Matches egov-indexer fieldMapping for {@code $.boundary.*} (configs PR #145).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Co2Boundary {
    private String countryCode;
    private String stateCode;
    private String districtCode;
    private String blockCode;
    private String facilityCode;
}
