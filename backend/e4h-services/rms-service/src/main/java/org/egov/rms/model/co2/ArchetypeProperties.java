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
public class ArchetypeProperties {
    private String archetype;
    /** Year-one total facility energy consumption in kWh/year. */
    private BigDecimal yearOneAnnualConsumptionKwh;
    private BigDecimal alpha;
}
