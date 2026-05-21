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
                    k -> fetchForCenter(centerId, req.getMonth(), req.getYear()));
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

    private MonthlyConsumptionData fetchForCenter(String centerId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        int page = 1;
        while (page <= 50) {
            JsonNode root = dashboardApiClient.fetchMonthlyConsumptionTable(ym, page, PAGE_SIZE);
            JsonNode rows = findTableRows(root);
            if (rows != null && rows.isArray()) {
                for (JsonNode row : rows) {
                    if (matchesCenter(row, centerId)) {
                        return MonthlyConsumptionData.builder()
                                .centerId(centerId)
                                .month(month)
                                .year(year)
                                .monthlySolarConsumptionKwh(readKwh(row, "solarConsumption", "solar_consumption"))
                                .monthlyGridConsumptionKwh(readKwh(row, "gridConsumption", "grid_consumption"))
                                .monthlyTotalConsumptionKwh(readKwh(row, "totalConsumption", "total_consumption"))
                                .source("ELMEASURE_SELCO_TABLE_DATA")
                                .build();
                    }
                }
                if (rows.size() < PAGE_SIZE) {
                    break;
                }
            } else {
                break;
            }
            page++;
        }
        return MonthlyConsumptionData.builder()
                .centerId(centerId)
                .month(month)
                .year(year)
                .source("ELMEASURE_NOT_FOUND")
                .build();
    }

    private JsonNode findTableRows(JsonNode root) {
        if (root == null) {
            return null;
        }
        for (String path : List.of("response.table", "response.data.table", "data.table", "table")) {
            JsonNode node = root.at("/" + path.replace('.', '/'));
            if (node.isArray() && !node.isEmpty()) {
                return node;
            }
        }
        JsonNode healthCenters = root.at("/response/HealthCenter");
        if (healthCenters.isArray()) {
            return healthCenters;
        }
        return root.at("/response/data/HealthCenter");
    }

    private boolean matchesCenter(JsonNode row, String centerId) {
        for (String field : List.of("center_id", "centerId", "selcoSensorId", "id")) {
            JsonNode v = row.get(field);
            if (v != null && centerId.equalsIgnoreCase(v.asText())) {
                return true;
            }
        }
        return false;
    }

    private Double readKwh(JsonNode row, String... fields) {
        for (String field : fields) {
            JsonNode v = row.get(field);
            if (v != null && !v.isNull()) {
                if (v.isNumber()) {
                    return v.asDouble();
                }
                if (v.isArray() && !v.isEmpty() && v.get(0).isNumber()) {
                    return v.get(0).asDouble();
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
