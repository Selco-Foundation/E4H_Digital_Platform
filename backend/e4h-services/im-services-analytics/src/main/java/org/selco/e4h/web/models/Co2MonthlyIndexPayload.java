package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka payload for {@code save-co2-monthly-*-facility-indexer} topics.
 * Aligned with deployed egov-indexer topics {@code save-co2-monthly-facility-indexer} /
 * {@code save-co2-monthly-projection-facility-indexer}: id = {@code $.uuid},
 * capacity field = {@code solarSystemCapacity}, geo = {@code geoPoint} → ES {@code geo-point}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Co2MonthlyIndexPayload {

    /**
     * Elasticsearch document id for egov-indexer configs using {@code id: $.uuid}
     * (composite: {@code tenantId_facilityId_year_month}).
     */
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
    private String facilityName;
    private String projectName;
    private String tenantId;
    /** Indexer maps to ES {@code geo-point} via {@code $.geoPoint}. */
    private String geoPoint;
    private Boolean isLive;
    private String solarInstallationDate;
    private String rmsInstallationDate;
    private Double solarSystemCapacity;
    private int month;
    private int year;
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
                .facilityName(doc.getFacilityName())
                .projectName(doc.getProjectName())
                .tenantId(doc.getTenantId())
                .geoPoint(doc.getGeoPoint())
                .isLive(doc.getIsLive())
                .solarInstallationDate(doc.getSolarInstallationDate())
                .rmsInstallationDate(doc.getRmsInstallationDate())
                .solarSystemCapacity(doc.getSolarSystemCapacity())
                .month(doc.getMonth())
                .year(doc.getYear());
    }
}
