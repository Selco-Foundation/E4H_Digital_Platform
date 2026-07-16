package org.egov.rms.model.co2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyConsumptionRequest {
    private String facilityId;
    private String facilityName;
    private String hfrId;
    private String ninId;
    private String centerId;
    private int month;
    private int year;
}
