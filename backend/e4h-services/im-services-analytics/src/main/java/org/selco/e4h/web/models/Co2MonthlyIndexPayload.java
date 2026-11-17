package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Kafka payload for CO2 monthly indexer topics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Co2MonthlyIndexPayload {

    @JsonProperty("uuid")
    private String uuid;

    private String state;
    private String district;
    private String block;
    private Co2Boundary boundary;
    private String facilityId;
    private String hfrId;
    private String ninId;
    private String facilityType;
    private String facilityCategory;
    private String mappedVendorName;
    private String mappedVendorUserName;
    private String facilityName;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String projectName;
    private String tenantId;
    private String geoPoint;
    private Boolean isLive;
    private Long solarInstallationDate;
    
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Long rmsInstallationDate;
    private Double solarSystemCapacity;
    private Double totalSolarEnergyGeneratedInKwh;
    private int month;
    private int year;
    private String financialYear;
    private int financialMonth;
    private Double co2EmissionsAvoidedInTonnes;
    private Double projectedCo2EmissionsAvoidedInTonnes;

    public static String documentId(String tenantId, String facilityId, int year, int month) {
        return tenantId + "_" + facilityId + "_" + year + "_" + month;
    }

    public static Co2MonthlyIndexPayload fromActual(Co2MonthlyDocument doc) {
        return baseFrom(doc)
                .co2EmissionsAvoidedInTonnes(doc.getCo2EmissionsAvoidedInTonnes())
                .build();
    }

    public static Co2MonthlyIndexPayload fromProjection(Co2MonthlyDocument doc) {
        return baseFrom(doc)
                .projectedCo2EmissionsAvoidedInTonnes(doc.getProjectedCo2EmissionsAvoidedInTonnes())
                .build();
    }

    private static Co2MonthlyIndexPayload.Co2MonthlyIndexPayloadBuilder baseFrom(Co2MonthlyDocument doc) {
        return Co2MonthlyIndexPayload.builder()
                .uuid(documentId(doc.getTenantId(), doc.getFacilityId(), doc.getYear(), doc.getMonth()))
                .state(doc.getState())
                .district(doc.getDistrict())
                .block(doc.getBlock())
                .boundary(doc.getBoundary())
                .facilityId(doc.getFacilityId())
                .hfrId(doc.getHfrId())
                .ninId(doc.getNinId())
                .facilityType(doc.getFacilityType())
                .facilityCategory(doc.getFacilityCategory())
                .mappedVendorName(doc.getMappedVendorName())
                .mappedVendorUserName(doc.getMappedVendorUserName())
                .facilityName(doc.getFacilityName())
                .projectName(doc.getProjectName() != null ? doc.getProjectName() : "")
                .tenantId(doc.getTenantId())
                .geoPoint(doc.getGeoPoint())
                .isLive(doc.getIsLive())
                .solarInstallationDate(isoDateToEpochMillis(doc.getSolarInstallationDate()))
                .rmsInstallationDate(isoDateToEpochMillis(doc.getRmsInstallationDate()))
                .solarSystemCapacity(doc.getSolarSystemCapacity())
                .totalSolarEnergyGeneratedInKwh(doc.getTotalSolarEnergyGeneratedInKwh())
                .month(doc.getMonth())
                .year(doc.getYear())
                .financialYear(doc.getFinancialYear())
                .financialMonth(doc.getFinancialMonth());
    }

    static Long isoDateToEpochMillis(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) {
            return null;
        }
        return LocalDate.parse(isoDate).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }
}
