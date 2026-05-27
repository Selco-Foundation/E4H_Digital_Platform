package org.egov.rms.model.co2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyConsumptionData {
    private String facilityId;
    private String centerId;
    private int month;
    private int year;
    private Double monthlySolarConsumptionKwh;
    private Double monthlyGridConsumptionKwh;
    private Double monthlyTotalConsumptionKwh;
    private String source;
}
