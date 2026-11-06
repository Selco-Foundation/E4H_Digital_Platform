package org.selco.e4h.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.selco.e4h.web.models.Co2Boundary;
import org.selco.e4h.web.models.Co2FacilityContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityRegistryClient {

    private final RestTemplate restTemplate;
    private final CarbonEmissionProperties properties;
    private final ObjectMapper objectMapper;

    public List<Co2FacilityContext> bulkSearchByFacilityIds(RequestInfo requestInfo,
                                                            String tenantId,
                                                            List<String> facilityIds) {
        if (facilityIds == null || facilityIds.isEmpty()) {
            return List.of();
        }
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("tenantIds", List.of(tenantId));
        criteria.put("facilityIds", facilityIds);
        criteria.put("limit", Math.max(facilityIds.size(), 50));
        criteria.put("offset", 0);
        criteria.put("sendNonPaginatedResponse", true);
        return search(requestInfo, tenantId, criteria).facilities();
    }

    /**
     * Paginated active-facility search for CO2 batch (no facilityIds allowlist).
     * Ordered by created_at ASC per LLD.
     */
    public FacilityPage searchFacilitiesPage(RequestInfo requestInfo,
                                             String tenantId,
                                             int offset,
                                             int limit) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("tenantIds", List.of(tenantId));
        criteria.put("limit", Math.max(limit, 1));
        criteria.put("offset", Math.max(offset, 0));
        criteria.put("sendNonPaginatedResponse", false);
        criteria.put("sortBy", "created_at");
        criteria.put("sortOrder", "asc");
        return search(requestInfo, tenantId, criteria);
    }

    /**
     * Number of facilities registered under a state boundary code (e.g. "India_Karnataka"), read from
     * the bulk-search {@code totalCount}. Returns null when the lookup fails or the response carries no
     * totalCount, so callers can tell "unknown" apart from a genuine zero.
     */
    public Integer countFacilitiesByState(RequestInfo requestInfo, String stateCode) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("tenantIds", List.of("in"));
        criteria.put("state", List.of(stateCode));
        // Only facilities under O&M can raise maintenance tickets. Set explicitly: without it,
        // facility-service decides by caller - no user gets O&M-ready only, a system user also gets
        // not-yet-onboarded facilities (e.g. Gujarat 203 vs 308).
        criteria.put("isOnmReady", true);
        criteria.put("limit", 1);
        criteria.put("offset", 0);
        criteria.put("sendNonPaginatedResponse", false);

        Map<String, Object> body = new HashMap<>();
        body.put("RequestInfo", requestInfo);
        body.put("Facility", criteria);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String response = restTemplate.postForObject(
                    properties.getFacilityHost() + properties.getFacilityBulkSearchPath(),
                    new HttpEntity<>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode totalCount = root.path("totalCount");
            if (!totalCount.isNumber()) {
                log.warn("Facility bulk search returned no totalCount for state={}", stateCode);
                return null;
            }
            // If the state code isn't one facility-service recognises, the filter can be silently
            // dropped and the count becomes the whole registry - make sure the sample row is really
            // from the requested state before trusting the count.
            JsonNode facilities = root.path("facilities");
            if (totalCount.asInt() > 0 && (!facilities.isArray() || facilities.isEmpty())) {
                // An unrecognised state code returns no rows but the whole registry's totalCount.
                log.error("Facility count for state={} returned totalCount={} but no facilities - state code not recognised, treating as unknown",
                        stateCode, totalCount.asInt());
                return null;
            }
            if (facilities.isArray() && !facilities.isEmpty()) {
                String sampleState = facilities.get(0).path("boundary").path("state").asText(null);
                if (!stateCode.equals(sampleState)) {
                    log.error("Facility count for state={} ignored the state filter (sample facility is in {}) - treating as unknown",
                            stateCode, sampleState);
                    return null;
                }
            }
            return totalCount.asInt();
        } catch (Exception e) {
            log.error("Facility count lookup failed for state={}", stateCode, e);
            return null;
        }
    }

    private FacilityPage search(RequestInfo requestInfo, String tenantId, Map<String, Object> criteria) {
        String url = properties.getFacilityHost() + properties.getFacilityBulkSearchPath();
        Map<String, Object> body = new HashMap<>();
        body.put("RequestInfo", requestInfo);
        body.put("Facility", criteria);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
            return parseFacilityPage(response, tenantId);
        } catch (Exception e) {
            log.error("Facility bulk search failed tenantId={} offset={} limit={}",
                    tenantId, criteria.get("offset"), criteria.get("limit"), e);
            return FacilityPage.empty();
        }
    }

    public record FacilityPage(List<Co2FacilityContext> facilities, int totalCount) {
        static FacilityPage empty() {
            return new FacilityPage(List.of(), 0);
        }
    }

    private FacilityPage parseFacilityPage(String response, String tenantId) {
        List<Co2FacilityContext> result = new ArrayList<>();
        int totalCount = 0;
        try {
            JsonNode root = objectMapper.readTree(response);
            totalCount = root.path("totalCount").asInt(0);
            JsonNode facilities = root.path("facilities");
            if (!facilities.isArray()) {
                facilities = root.path("Facility");
            }
            if (!facilities.isArray()) {
                return new FacilityPage(result, totalCount);
            }
            for (JsonNode f : facilities) {
                Co2FacilityContext ctx = Co2FacilityContext.builder()
                        .facilityId(text(f, "facility_id", "facilityId", "id"))
                        .tenantId(tenantId)
                        .facilityName(text(f, "facility_name", "facilityName"))
                        .facilityType(text(f, "facility_type", "facilityType"))
                        .facilityCategory(text(f, "facility_category", "facilityCategory"))
                        .mappedVendorName(text(f, "mappedVendorName", "mapped_vendor_name"))
                        .mappedVendorUserName(text(f, "mappedVendorUserName", "mapped_vendor_user_name"))
                        .hfrId(text(f, "hfr_id", "hfrId"))
                        .ninId(text(f, "nin_id", "ninId"))
                        .solarInstallationDate(parseDate(f, "solar_installation_date", "solarInstallationDate"))
                        .rmsInstallationDate(parseDate(f, "rms_installation_date", "rmsInstallationDate"))
                        .solarSystemCapacity(parseDouble(f, "solar_system_capacity_kwp", "solarSystemCapacityKwp"))
                        .isLive(parseBoolean(f, "isActive", "is_active"))
                        .build();
                JsonNode boundary = f.path("boundary");
                if (!boundary.isMissingNode()) {
                    String state = text(boundary, "state");
                    String district = text(boundary, "district");
                    String block = text(boundary, "block");
                    ctx.setState(state);
                    ctx.setDistrict(district);
                    ctx.setBlock(block);
                    // Bulk search Boundary model exposes state/district/block as codes, not *Code fields.
                    ctx.setBoundary(Co2Boundary.builder()
                            .countryCode(firstNonBlank(text(boundary, "countryCode", "country_code")))
                            .stateCode(firstNonBlank(text(boundary, "stateCode", "state_code"), state))
                            .districtCode(firstNonBlank(text(boundary, "districtCode", "district_code"), district))
                            .blockCode(firstNonBlank(text(boundary, "blockCode", "block_code"), block))
                            .facilityCode(firstNonBlank(
                                    text(boundary, "facilityCode", "facility_code", "code")))
                            .build());
                }
                String boundaryCode = text(f, "boundaryCode", "boundary_code");
                if (boundaryCode != null && ctx.getBoundary() == null) {
                    ctx.setBoundary(Co2Boundary.builder().facilityCode(boundaryCode).build());
                } else if (boundaryCode != null && ctx.getBoundary() != null
                        && ctx.getBoundary().getFacilityCode() == null) {
                    ctx.getBoundary().setFacilityCode(boundaryCode);
                }
                JsonNode address = f.path("address");
                if (!address.isMissingNode()) {
                    ctx.setGeoPoint(parseGeoPoint(address));
                }
                result.add(ctx);
            }
        } catch (Exception e) {
            log.error("Failed to parse facility bulk search response", e);
            return FacilityPage.empty();
        }
        return new FacilityPage(result, totalCount > 0 ? totalCount : result.size());
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private static String text(JsonNode node, String... fields) {
        for (String f : fields) {
            JsonNode v = node.get(f);
            if (v != null && !v.isNull() && v.isTextual()) {
                return v.asText();
            }
        }
        return null;
    }

    private static LocalDate parseDate(JsonNode node, String... fields) {
        for (String f : fields) {
            JsonNode v = node.get(f);
            if (v != null && !v.isNull() && v.isTextual()) {
                return LocalDate.parse(v.asText().substring(0, 10));
            }
        }
        return null;
    }

    private static Double parseDouble(JsonNode node, String... fields) {
        for (String f : fields) {
            JsonNode v = node.get(f);
            if (v != null && !v.isNull() && v.isNumber()) {
                return v.doubleValue();
            }
            if (v != null && !v.isNull() && v.isTextual()) {
                try {
                    return Double.parseDouble(v.asText().trim());
                } catch (NumberFormatException ignored) {
                    // try next field alias
                }
            }
        }
        return null;
    }

    private static Boolean parseBoolean(JsonNode node, String... fields) {
        for (String f : fields) {
            JsonNode v = node.get(f);
            if (v != null && !v.isNull() && v.isBoolean()) {
                return v.asBoolean();
            }
        }
        return null;
    }

    private static String parseGeoPoint(JsonNode address) {
        JsonNode lat = address.get("latitude");
        JsonNode lon = address.get("longitude");
        if (lat != null && lon != null && !lat.isNull() && !lon.isNull()) {
            return lat.asText() + "," + lon.asText();
        }
        return null;
    }
}
