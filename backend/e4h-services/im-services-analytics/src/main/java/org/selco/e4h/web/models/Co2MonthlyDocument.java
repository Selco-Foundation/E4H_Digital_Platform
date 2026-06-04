package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Co2MonthlyDocument {
    private String state;
    private String district;
    private String block;
    private Co2Boundary boundary;
    private String geoPoint;
    private Boolean isLive;
    private String facilityId;
    private String hfrId;
    private String ninId;
    private String facilityType;
    private String facilityName;
    private String projectName;
    private String tenantId;
    private String solarInstallationDate;
    private String rmsInstallationDate;
    private Double solarSystemCapacity;
    private int month;
    private int year;
    private String financialYear;
    private int financialMonth;
    private double co2EmissionsAvoidedInTonnes;
    private double projectedCo2EmissionsAvoidedInTonnes;
    private boolean projection;
}
