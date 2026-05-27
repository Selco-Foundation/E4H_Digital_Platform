package org.egov.rms.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.model.co2.MonthlyConsumptionData;
import org.egov.rms.model.co2.MonthlyConsumptionRequest;
import org.egov.rms.repository.CenterIdMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Co2ElmeasureConsumptionService {

    private static final int PAGE_SIZE = 100;

    private final DashboardApiClient dashboardApiClient;
    private final CenterIdMappingRepository centerIdMappingRepository;

    public List<MonthlyConsumptionData> fetchMonthlyBatch(List<MonthlyConsumptionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        Map<String, MonthlyConsumptionData> cache = new HashMap<>();
        List<MonthlyConsumptionData> results = new ArrayList<>();

        for (MonthlyConsumptionRequest req : requests) {
            String centerId = resolveCenterId(req);
            if (!StringUtils.hasText(centerId)) {
                log.warn("No centerId for facilityId={} hfrId={}", req.getFacilityId(), req.getHfrId());
                results.add(empty(req, "CENTER_NOT_MAPPED"));
                continue;
            }
            String cacheKey = centerId + "|" + req.getYear() + "|" + req.getMonth();
            MonthlyConsumptionData data = cache.computeIfAbsent(cacheKey,
                    k -> fetchForCenter(centerId, req.getHfrId(), req.getMonth(), req.getYear()));
            results.add(MonthlyConsumptionData.builder()
                    .facilityId(req.getFacilityId())
                    .centerId(centerId)
                    .month(req.getMonth())
                    .year(req.getYear())
                    .monthlySolarConsumptionKwh(data.getMonthlySolarConsumptionKwh())
                    .monthlyGridConsumptionKwh(data.getMonthlyGridConsumptionKwh())
                    .monthlyTotalConsumptionKwh(data.getMonthlyTotalConsumptionKwh())
                    .source(data.getSource())
                    .build());
        }
        return results;
    }

    private MonthlyConsumptionData fetchForCenter(String centerId, String hfrId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        JsonNode root = dashboardApiClient.fetchMonthlyConsumptionTable(ym, centerId, hfrId, 1, PAGE_SIZE);
        if (hasApiError(root)) {
            log.warn("Selco table_data error for centerId={} period={}: {}",
                    centerId, ym, root.path("error").asText());
            return noData(centerId, month, year);
        }
        JsonNode rows = findTableRows(root);
        if (rows == null || !rows.isArray()) {
            log.warn("Selco table_data unrecognized shape for centerId={} period={}", centerId, ym);
            return MonthlyConsumptionData.builder()
                    .centerId(centerId)
                    .month(month)
                    .year(year)
                    .source("ELMEASURE_NOT_FOUND")
                    .build();
        }
        if (rows.isEmpty()) {
            logHeaderHint(root);
            return noData(centerId, month, year);
        }
        for (JsonNode row : rows) {
            if (matchesCenter(row, centerId, hfrId)) {
                return toConsumptionData(centerId, month, year, row);
            }
        }
        // When filtered by centerId, API may return a single row without id fields — use first row.
        if (rows.size() == 1) {
            return toConsumptionData(centerId, month, year, rows.get(0));
        }
        log.warn("Elmeasure rows returned but none matched centerId={} hfrId={} period={} count={}",
                centerId, hfrId, ym, rows.size());
        return MonthlyConsumptionData.builder()
                .centerId(centerId)
                .month(month)
                .year(year)
                .source("ELMEASURE_NOT_FOUND")
                .build();
    }

    private MonthlyConsumptionData toConsumptionData(String centerId, int month, int year, JsonNode row) {
        return MonthlyConsumptionData.builder()
                .centerId(centerId)
                .month(month)
                .year(year)
                .monthlySolarConsumptionKwh(readKwh(row,
                        "solarConsumption", "solar_consumption", "solar_consumption_kwh",
                        "solarConsumptionKwh", "solarDatas"))
                .monthlyGridConsumptionKwh(readKwh(row,
                        "gridConsumption", "grid_consumption", "grid_consumption_kwh",
                        "gridConsumptionKwh", "gridDatas"))
                .monthlyTotalConsumptionKwh(readKwh(row,
                        "totalConsumption", "total_consumption", "total_consumption_kwh",
                        "totalConsumptionKwh", "totalConsumptionDatas"))
                .source("ELMEASURE_SELCO_TABLE_DATA")
                .build();
    }

    private JsonNode findTableRows(JsonNode root) {
        if (root == null) {
            return null;
        }
        // Selco table_data success shape: { "data": { "body_content": [...], "header_content": [...] }, "status": "success" }
        JsonNode bodyContent = root.at("/data/body_content");
        if (bodyContent.isArray()) {
            return bodyContent;
        }
        for (String path : List.of(
                "response.table",
                "response.data.table",
                "response.data.HealthCenter",
                "response.HealthCenter",
                "data.table",
                "data.HealthCenter",
                "data.facilities",
                "table")) {
            JsonNode node = root.at("/" + path.replace('.', '/'));
            if (node.isArray()) {
                return node;
            }
        }
        return null;
    }

    private void logHeaderHint(JsonNode root) {
        JsonNode headers = root.at("/data/header_content");
        if (!headers.isArray()) {
            return;
        }
        List<String> fields = new ArrayList<>();
        for (JsonNode h : headers) {
            JsonNode field = h.get("field");
            if (field != null && field.isTextual()) {
                fields.add(field.asText());
            }
        }
        log.warn("Selco table_data body_content empty; header fields={} (expect solar/grid kWh columns for table_type=consumption)",
                fields);
    }

    private boolean hasApiError(JsonNode root) {
        return root != null && root.has("error")
                && root.get("error").isTextual()
                && !root.get("error").asText().isBlank();
    }

    private MonthlyConsumptionData noData(String centerId, int month, int year) {
        return MonthlyConsumptionData.builder()
                .centerId(centerId)
                .month(month)
                .year(year)
                .source("ELMEASURE_NO_DATA")
                .build();
    }

    private boolean matchesCenter(JsonNode row, String centerId, String hfrId) {
        if (rowMatchesIds(row, centerId, hfrId)) {
            return true;
        }
        JsonNode nested = row.get("centerData");
        if (nested != null && nested.isObject()) {
            return rowMatchesIds(nested, centerId, hfrId);
        }
        return false;
    }

    private boolean rowMatchesIds(JsonNode row, String centerId, String hfrId) {
        for (String field : List.of("center_id", "centerId", "deviceInstanceId", "selcoSensorId", "id", "centre_id")) {
            JsonNode v = row.get(field);
            if (v != null && centerId != null && centerId.equalsIgnoreCase(v.asText())) {
                return true;
            }
        }
        if (StringUtils.hasText(hfrId)) {
            for (String field : List.of("HFRID", "hfrId", "hfr_id")) {
                JsonNode v = row.get(field);
                if (v != null && hfrId.equalsIgnoreCase(v.asText().trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Double readKwh(JsonNode row, String... fields) {
        for (String field : fields) {
            JsonNode v = row.get(field);
            if (v == null || v.isNull()) {
                JsonNode consumption = row.get("consumption");
                if (consumption != null && consumption.isObject()) {
                    v = consumption.get(field);
                }
            }
            if (v != null && !v.isNull()) {
                if (v.isNumber()) {
                    return v.asDouble();
                }
                if (v.isArray() && !v.isEmpty()) {
                    JsonNode last = v.get(v.size() - 1);
                    if (last.isNumber()) {
                        return last.asDouble();
                    }
                }
            }
        }
        return null;
    }

    private String resolveCenterId(MonthlyConsumptionRequest req) {
        if (StringUtils.hasText(req.getCenterId())) {
            return req.getCenterId().trim();
        }
        if (StringUtils.hasText(req.getHfrId())) {
            Optional<String> center = centerIdMappingRepository.findCenterIdByHfrId(req.getHfrId().trim());
            if (center.isPresent()) {
                return center.get();
            }
        }
        return null;
    }

    private MonthlyConsumptionData empty(MonthlyConsumptionRequest req, String source) {
        return MonthlyConsumptionData.builder()
                .month(req.getMonth())
                .year(req.getYear())
                .source(source)
                .build();
    }
}
