package org.selco.e4h.service;

import org.selco.e4h.web.models.Co2Boundary;
import org.selco.e4h.web.models.Co2MonthlyIndexPayload;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds the final Elasticsearch document for the CO2 monthly indexes.
 *
 * <p>This replaces egov-indexer's {@code customJsonMapping} for the CO2 topics: the bulk-indexer
 * writes documents verbatim, so the field layout is owned here. The layout below reproduces the
 * {@code save-co2-monthly-facility-indexer} / {@code save-co2-monthly-projection-facility-indexer}
 * mappings from egov-indexer's {@code im-services.yml} exactly, so documents land identically and
 * the existing Kibana dashboards keep working.
 *
 * <p>Absent fields are omitted rather than written as null, matching the {@code NON_NULL}
 * serialisation of the payloads the indexer used to receive. {@code projectName} and
 * {@code rmsInstallationDate} are always written, because the payload marks them {@code ALWAYS}.
 *
 * <p>Note: the indexer mapping also mapped {@code facilityCategory}, {@code mappedVendorName} and
 * {@code mappedVendorUserName}, but {@link Co2MonthlyIndexPayload} has never carried those fields —
 * they were no-ops and are intentionally not reproduced here.
 */
@Component
public class Co2EsDocumentFactory {

    public Map<String, Object> toEsDocument(Co2MonthlyIndexPayload payload) {
        Map<String, Object> document = new LinkedHashMap<>();

        Map<String, Object> data = new LinkedHashMap<>();
        putIfPresent(data, "state", payload.getState());
        putIfPresent(data, "district", payload.getDistrict());
        putIfPresent(data, "block", payload.getBlock());
        putIfPresent(data, "facilityId", payload.getFacilityId());
        putIfPresent(data, "type", payload.getFacilityType());
        putIfPresent(data, "tenantId_localized", payload.getFacilityName());
        data.put("projectName", payload.getProjectName() != null ? payload.getProjectName() : "");
        document.put("Data", data);

        document.put("boundary", boundary(payload.getBoundary()));

        putIfPresent(document, "hfrId", payload.getHfrId());
        putIfPresent(document, "ninId", payload.getNinId());
        putIfPresent(document, "tenantId", payload.getTenantId());
        putIfPresent(document, "geo-point", payload.getGeoPoint());
        putIfPresent(document, "isLive", payload.getIsLive());
        putIfPresent(document, "solarInstallationDate", payload.getSolarInstallationDate());
        document.put("rmsInstallationDate", payload.getRmsInstallationDate());
        putIfPresent(document, "solarSystemCapacity", payload.getSolarSystemCapacity());
        putIfPresent(document, "totalSolarEnergyGeneratedInKwh", payload.getTotalSolarEnergyGeneratedInKwh());
        document.put("month", payload.getMonth());
        document.put("year", payload.getYear());
        putIfPresent(document, "financialYear", payload.getFinancialYear());
        document.put("financialMonth", payload.getFinancialMonth());

        // Only one of these is set, by fromActual() / fromProjection().
        putIfPresent(document, "co2EmissionsAvoidedInTonnes", payload.getCo2EmissionsAvoidedInTonnes());
        putIfPresent(document, "projectedCo2EmissionsAvoidedInTonnes",
                payload.getProjectedCo2EmissionsAvoidedInTonnes());

        // The indexer mapped solarInstallationDate to @timestamp as well.
        putIfPresent(document, "@timestamp", payload.getSolarInstallationDate());

        return document;
    }

    private static Map<String, Object> boundary(Co2Boundary source) {
        Map<String, Object> boundary = new LinkedHashMap<>();
        if (source == null) {
            return boundary;
        }
        putIfPresent(boundary, "countryCode", source.getCountryCode());
        putIfPresent(boundary, "stateCode", source.getStateCode());
        putIfPresent(boundary, "districtCode", source.getDistrictCode());
        putIfPresent(boundary, "blockCode", source.getBlockCode());
        putIfPresent(boundary, "facilityCode", source.getFacilityCode());
        return boundary;
    }

    private static void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }
}
