package org.selco.e4h.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.CarbonEmissionProperties;
import org.selco.e4h.web.models.Co2FacilityContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RmsConsumptionClient {

    private final RestTemplate restTemplate;
    private final CarbonEmissionProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Key: facilityId|year|month → solar kWh from Elmeasure (rms-service → POST /selco/center_details/graph).
     */
    public Map<String, Double> fetchSolarKwhByFacilityMonth(List<Co2FacilityContext> facilities,
                                                            List<YearMonth> months) {
        Map<String, Double> result = new HashMap<>();
        if (facilities == null || facilities.isEmpty() || months == null || months.isEmpty()) {
            return result;
        }
        List<Map<String, Object>> requests = new ArrayList<>();
        for (Co2FacilityContext f : facilities) {
            for (YearMonth ym : months) {
                Map<String, Object> req = new HashMap<>();
                req.put("facilityId", f.getFacilityId());
                req.put("facilityName", f.getFacilityName());
                req.put("hfrId", f.getHfrId());
                req.put("month", ym.getMonthValue());
                req.put("year", ym.getYear());
                requests.add(req);
            }
        }
        String url = properties.getRmsHost() + properties.getRmsCo2ConsumptionBatchPath();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String response = restTemplate.postForObject(
                    url, new HttpEntity<>(Map.of("requests", requests), headers), String.class);
            JsonNode rows = objectMapper.readTree(response).path("consumption");
            if (!rows.isArray()) {
                return result;
            }
            for (JsonNode row : rows) {
                String facilityId = text(row, "facilityId");
                int year = row.path("year").asInt();
                int month = row.path("month").asInt();
                Double solar = number(row, "monthlySolarConsumptionKwh");
                if (facilityId != null && solar != null && solar > 0) {
                    result.put(key(facilityId, year, month), solar);
                }
            }
        } catch (Exception e) {
            log.error("RMS CO2 consumption batch call failed url={}", url, e);
        }
        return result;
    }

    public static String key(String facilityId, int year, int month) {
        return facilityId + "|" + year + "|" + month;
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return v != null && v.isTextual() ? v.asText() : null;
    }

    private static Double number(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return v != null && v.isNumber() ? v.asDouble() : null;
    }
}
