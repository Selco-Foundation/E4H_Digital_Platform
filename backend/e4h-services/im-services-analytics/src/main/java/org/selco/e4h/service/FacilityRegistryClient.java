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
        String url = properties.getFacilityHost() + properties.getFacilityBulkSearchPath();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("tenantIds", List.of(tenantId));
        criteria.put("facilityIds", facilityIds);
        criteria.put("limit", facilityIds.size());
        criteria.put("offset", 0);
        criteria.put("sendNonPaginatedResponse", true);

        Map<String, Object> body = new HashMap<>();
        body.put("RequestInfo", requestInfo);
        body.put("Facility", criteria);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
            return parseFacilities(response, tenantId);
        } catch (Exception e) {
            log.error("Facility bulk search failed for {} ids", facilityIds.size(), e);
            return List.of();
        }
    }

    private List<Co2FacilityContext> parseFacilities(String response, String tenantId) {
        List<Co2FacilityContext> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode facilities = root.path("facilities");
            if (!facilities.isArray()) {
                facilities = root.path("Facility");
            }
            if (!facilities.isArray()) {
                return result;
            }
            for (JsonNode f : facilities) {
                Co2FacilityContext ctx = Co2FacilityContext.builder()
                        .facilityId(text(f, "facility_id", "facilityId", "id"))
                        .tenantId(tenantId)
                        .facilityName(text(f, "facility_name", "facilityName"))
                        .facilityType(text(f, "facility_type", "facilityType"))
                        .hfrId(text(f, "hfr_id", "hfrId"))
                        .ninId(text(f, "nin_id", "ninId"))
                        .solarInstallationDate(parseDate(f, "solar_installation_date", "solarInstallationDate"))
                        .rmsInstallationDate(parseDate(f, "rms_installation_date", "rmsInstallationDate"))
                        .solarSystemCapacity(parseDouble(f, "solar_system_capacity_kwp", "solarSystemCapacityKwp"))
                        .isLive(parseBoolean(f, "isActive", "is_active"))
                        .build();
                JsonNode boundary = f.path("boundary");
                if (!boundary.isMissingNode()) {
                    ctx.setState(text(boundary, "state"));
                    ctx.setDistrict(text(boundary, "district"));
                    ctx.setBlock(text(boundary, "block"));
                    ctx.setBoundary(Co2Boundary.builder()
                            .countryCode(text(boundary, "countryCode", "country_code"))
                            .stateCode(text(boundary, "stateCode", "state_code"))
                            .districtCode(text(boundary, "districtCode", "district_code"))
                            .blockCode(text(boundary, "blockCode", "block_code"))
                            .facilityCode(text(boundary, "facilityCode", "facility_code", "code"))
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
        }
        return result;
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
