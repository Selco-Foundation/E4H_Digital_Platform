package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Co2FacilityContext {
    private String facilityId;
    private String tenantId;
    private String facilityName;
    private String facilityType;
    private String facilityCategory;
    private String mappedVendorName;
    private String mappedVendorUserName;
    private String state;
    private String district;
    private String block;
    private String stateLocalized;
    private String districtLocalized;
    private String blockLocalized;
    private Co2Boundary boundary;
    private String geoPoint;
    private Boolean isLive;
    private String hfrId;
    private String ninId;
    private String projectName;
    private LocalDate solarInstallationDate;
    private LocalDate rmsInstallationDate;
    private Double solarSystemCapacity;
}
