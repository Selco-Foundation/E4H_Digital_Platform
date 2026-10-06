package org.egov.rms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.config.RMSConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.rms.service.RestTemplateSslUtils.restTemplateAcceptingAllCerts;

/**
 * Elmeasure dashboard APIs for monthly consumption (solar, grid kWh).
 * CO2 consumption uses POST {{baseUrl}}/selco/center_details/graph
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardApiClient {

    private static final String GRAPH_TYPE_SOLAR_VS_GRID = "solarVsGrid_Eb";

    private final RMSConfiguration config;
    private final ObjectMapper objectMapper;

    /**
     * Monthly solar/grid series for one center over a date range (dashboard graph API).
     */
    public JsonNode fetchMonthlyConsumptionGraph(YearMonth from,
                                                 YearMonth to,
                                                 String centerId) {
        Map<String, Object> body = buildGraphRequest(from, to, centerId);
        return post(config.getCenterDetailsEndpoint(), body);
    }

    /**
     * @deprecated table_data returns zero consumption for many centres; use {@link #fetchMonthlyConsumptionGraph}.
     */
    @Deprecated
    public JsonNode fetchMonthlyConsumptionTable(YearMonth yearMonth,
                                                String centerId,
                                                String hfrId,
                                                int page,
                                                int pageSize) {
        Map<String, Object> body = buildTableDataRequest(yearMonth, centerId, hfrId, page, pageSize);
        return post(config.getDashboardTableDataEndpoint(), body);
    }

    private Map<String, Object> buildGraphRequest(YearMonth from, YearMonth to, String centerId) {
        Map<String, Object> customRange = new HashMap<>();
        customRange.put("from", from.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        customRange.put("to", to.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE));

        Map<String, Object> timeRange = new HashMap<>();
        timeRange.put("custom_range", customRange);
        timeRange.put("time_period", Map.of("label", "Custom", "value", "custom"));

        Map<String, Object> body = new HashMap<>();
        body.put("centerId", centerId.trim());
        body.put("graphType", GRAPH_TYPE_SOLAR_VS_GRID);
        body.put("time_range", timeRange);
        body.put("frequency", "monthly");
        body.put("aggregation", "deltaSum");
        return body;
    }

    private Map<String, Object> buildTableDataRequest(YearMonth yearMonth,
                                                      String centerId,
                                                      String hfrId,
                                                      int page,
                                                      int pageSize) {
        String from = yearMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String to = yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE);

        Map<String, Object> customRange = new HashMap<>();
        customRange.put("from", from);
        customRange.put("to", to);

        Map<String, Object> timeRange = new HashMap<>();
        timeRange.put("custom_range", customRange);
        timeRange.put("time_period", Map.of("label", "Custom", "value", "custom"));

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", pageSize);

        Map<String, Object> body = new HashMap<>();
        body.put("aggregation", "monthly");
        body.put("frequency", "monthly");
        body.put("table_type", "consumption");
        body.put("timeSet", true);
        body.put("time_range", timeRange);
        body.put("pagination", pagination);
        body.put("state", List.of());
        body.put("district", List.of());
        body.put("block", List.of());
        body.put("filters", List.of());

        if (StringUtils.hasText(centerId)) {
            body.put("centerId", centerId.trim());
        }
        if (StringUtils.hasText(centerId) || StringUtils.hasText(hfrId)) {
            Map<String, Object> healthCenter = new HashMap<>();
            if (StringUtils.hasText(centerId)) {
                healthCenter.put("centerId", centerId.trim());
            }
            if (StringUtils.hasText(hfrId)) {
                healthCenter.put("HFRID", hfrId.trim());
            }
            body.put("health_center", List.of(healthCenter));
        }
        return body;
    }

    private JsonNode post(String endpoint, Map<String, Object> body) {
        try {
            var rt = restTemplateAcceptingAllCerts();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Access-Token", config.getRmsApiAccessToken());

            String url = config.getRmsApiBaseUrl() + endpoint;
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = rt.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getBody() == null || response.getBody().isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Selco dashboard API call failed endpoint={}", endpoint, e);
            return objectMapper.createObjectNode();
        }
    }
}

