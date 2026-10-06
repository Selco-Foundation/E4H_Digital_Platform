package org.egov.rms.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.model.co2.MonthlyConsumptionData;
import org.egov.rms.model.co2.MonthlyConsumptionRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class Co2ElmeasureConsumptionService {

    private final DashboardApiClient dashboardApiClient;
    private final CenterIdResolverService centerIdResolverService;

    public List<MonthlyConsumptionData> fetchMonthlyBatch(List<MonthlyConsumptionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        Map<String, List<ResolvedRequest>> byCenter = new HashMap<>();

        for (MonthlyConsumptionRequest req : requests) {
            String centerId = resolveCenterId(req);
            if (!StringUtils.hasText(centerId)) {
                continue;
            }
            byCenter.computeIfAbsent(centerId, k -> new ArrayList<>())
                    .add(new ResolvedRequest(req, centerId, YearMonth.of(req.getYear(), req.getMonth())));
        }

        Map<String, Map<YearMonth, MonthValues>> cacheByCenter = new HashMap<>();
        for (Map.Entry<String, List<ResolvedRequest>> entry : byCenter.entrySet()) {
            cacheByCenter.put(entry.getKey(), fetchRangeForCenter(entry.getKey(), entry.getValue()));
        }

        List<MonthlyConsumptionData> results = new ArrayList<>();
        for (MonthlyConsumptionRequest req : requests) {
            String centerId = resolveCenterId(req);
            if (!StringUtils.hasText(centerId)) {
                log.warn("No centerId for facilityId={} hfrId={} ninId={} facilityName={}",
                        req.getFacilityId(), req.getHfrId(), req.getNinId(), req.getFacilityName());
                results.add(empty(req, "CENTER_NOT_MAPPED"));
                continue;
            }
            YearMonth ym = YearMonth.of(req.getYear(), req.getMonth());
            MonthValues values = cacheByCenter
                    .getOrDefault(centerId, Map.of())
                    .getOrDefault(ym, MonthValues.empty());

            results.add(MonthlyConsumptionData.builder()
                    .facilityId(req.getFacilityId())
                    .centerId(centerId)
                    .month(req.getMonth())
                    .year(req.getYear())
                    .monthlySolarConsumptionKwh(values.solarKwh())
                    .monthlyGridConsumptionKwh(values.gridKwh())
                    .monthlyTotalConsumptionKwh(values.totalKwh())
                    .source(values.source())
                    .build());
        }
        return results;
    }

    private Map<YearMonth, MonthValues> fetchRangeForCenter(String centerId, List<ResolvedRequest> requests) {
        YearMonth from = requests.stream().map(ResolvedRequest::yearMonth).min(YearMonth::compareTo).orElseThrow();
        YearMonth to = requests.stream().map(ResolvedRequest::yearMonth).max(YearMonth::compareTo).orElseThrow();

        JsonNode root = dashboardApiClient.fetchMonthlyConsumptionGraph(from, to, centerId);
        if (hasApiError(root)) {
            log.warn("Selco graph error for centerId={} period={} to {}: {}",
                    centerId, from, to, root.path("error").asText(root.path("message").asText()));
            return noDataRange(requests, "ELMEASURE_NO_DATA");
        }

        JsonNode series = root.at("/data/series");
        if (!series.isArray() || series.isEmpty()) {
            log.warn("Selco graph missing series for centerId={} period={} to {}", centerId, from, to);
            return noDataRange(requests, "ELMEASURE_NOT_FOUND");
        }

        Map<YearMonth, Double> solarByMonth = parseSeries(series, "solarConsumption", "Solar Energy Consumed", from, to);
        Map<YearMonth, Double> gridByMonth = parseSeries(series, "gridConsumption", "Grid Energy Consumed", from, to);

        Map<YearMonth, MonthValues> out = new HashMap<>();
        for (ResolvedRequest req : requests) {
            YearMonth ym = req.yearMonth();
            Double solar = solarByMonth.get(ym);
            Double grid = gridByMonth.get(ym);
            String source = solar != null || grid != null ? "ELMEASURE_SELCO_GRAPH" : "ELMEASURE_NO_DATA";
            Double total = totalKwh(solar, grid);
            out.put(ym, new MonthValues(solar, grid, total, source));
        }
        return out;
    }

    private Map<YearMonth, Double> parseSeries(JsonNode series,
                                               String type,
                                               String nameContains,
                                               YearMonth from,
                                               YearMonth to) {
        Map<YearMonth, Double> out = new HashMap<>();
        for (JsonNode item : series) {
            if (!matchesSeries(item, type, nameContains)) {
                continue;
            }
            JsonNode data = item.get("data");
            if (data == null || !data.isArray()) {
                return out;
            }
            YearMonth cursor = from;
            for (JsonNode point : data) {
                if (!point.isArray() || point.size() < 2 || cursor.isAfter(to)) {
                    break;
                }
                if (point.get(1).isNumber()) {
                    out.put(cursor, point.get(1).asDouble());
                }
                cursor = cursor.plusMonths(1);
            }
            return out;
        }
        return out;
    }

    private boolean matchesSeries(JsonNode item, String type, String nameContains) {
        String seriesType = item.path("type").asText("");
        if (type.equalsIgnoreCase(seriesType)) {
            return true;
        }
        String name = item.path("name").asText("");
        return name.toLowerCase(Locale.ROOT).contains(nameContains.toLowerCase(Locale.ROOT));
    }

    private Double totalKwh(Double solar, Double grid) {
        if (solar == null && grid == null) {
            return null;
        }
        double s = solar != null ? solar : 0.0;
        double g = grid != null ? grid : 0.0;
        return s + g;
    }

    private Map<YearMonth, MonthValues> noDataRange(List<ResolvedRequest> requests, String source) {
        Map<YearMonth, MonthValues> out = new HashMap<>();
        for (ResolvedRequest req : requests) {
            out.put(req.yearMonth(), new MonthValues(null, null, null, source));
        }
        return out;
    }

    private boolean hasApiError(JsonNode root) {
        if (root == null || root.isEmpty()) {
            return true;
        }
        if (root.has("error") && root.get("error").isTextual() && !root.get("error").asText().isBlank()) {
            return true;
        }
        String status = root.path("status").asText("");
        return "error".equalsIgnoreCase(status) || "failed".equalsIgnoreCase(status);
    }

    private String resolveCenterId(MonthlyConsumptionRequest req) {
        return centerIdResolverService
                .resolveCenterId(req.getCenterId(), req.getHfrId(), req.getNinId(), req.getFacilityName())
                .orElse(null);
    }

    private MonthlyConsumptionData empty(MonthlyConsumptionRequest req, String source) {
        return MonthlyConsumptionData.builder()
                .facilityId(req.getFacilityId())
                .month(req.getMonth())
                .year(req.getYear())
                .source(source)
                .build();
    }

    private record ResolvedRequest(MonthlyConsumptionRequest request, String centerId, YearMonth yearMonth) {
    }

    private record MonthValues(Double solarKwh, Double gridKwh, Double totalKwh, String source) {
        static MonthValues empty() {
            return new MonthValues(null, null, null, "ELMEASURE_NO_DATA");
        }
    }
}
