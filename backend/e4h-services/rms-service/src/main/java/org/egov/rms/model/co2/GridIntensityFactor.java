package org.egov.rms.model.co2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GridIntensityFactor {
    private String id;
    private String tenantId;
    private String financialYear;
    private BigDecimal gridIntensityFactor;
    private BigDecimal projectedGridIntensityFactor;

    /** LLD: use published factor, else projected. */
    public BigDecimal resolveFactor() {
        if (gridIntensityFactor != null) {
            return gridIntensityFactor;
        }
        return projectedGridIntensityFactor;
    }
}
