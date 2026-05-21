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

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.rms.service.RestTemplateSslUtils.restTemplateAcceptingAllCerts;

/**
 * Elmeasure dashboard APIs for monthly consumption (solar, grid, total kWh).
 * POST {{baseUrl}}/selco/dashboard/table_data
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardApiClient {

    private final RMSConfiguration config;
    private final ObjectMapper objectMapper;

    public JsonNode fetchMonthlyConsumptionTable(YearMonth yearMonth, int page, int pageSize) {
        Map<String, Object> body = buildTableDataRequest(yearMonth, page, pageSize);
        return post(config.getDashboardTableDataEndpoint(), body);
    }

    private Map<String, Object> buildTableDataRequest(YearMonth yearMonth, int page, int pageSize) {
        String start = yearMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String end = yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE);

        Map<String, Object> customRange = new HashMap<>();
        customRange.put("start_date", start);
        customRange.put("end_date", end);

        Map<String, Object> timeRange = new HashMap<>();
        timeRange.put("custom_range", customRange);
        timeRange.put("time_period", Map.of("label", "Custom", "value", "custom"));

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", pageSize);

        Map<String, Object> body = new HashMap<>();
        body.put("state", List.of());
        body.put("district", List.of());
        body.put("block", List.of());
        body.put("aggregation", "monthly");
        body.put("frequency", "monthly");
        body.put("table_type", "consumption");
        body.put("timeSet", true);
        body.put("time_range", timeRange);
        body.put("pagination", pagination);
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
