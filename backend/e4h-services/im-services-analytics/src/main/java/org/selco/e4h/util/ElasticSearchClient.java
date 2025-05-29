package org.selco.e4h.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticSearchClient {

    private final RestTemplate restTemplate;

    @Value("${egov.indexer.es.host.name}")
    private String esHost;

    @Value("${egov.indexer.es.port.no}")
    private int esPort;

    private static final String SEARCH_PATH = "_search";
    private static final String INDEX_NAME = "computed-sla-im-services";

    public List<Map<String, Object>> fetchOpenTickets(int from, int size) {
        String uri = getBaseUrl() + "/" + INDEX_NAME + "/" + SEARCH_PATH;
        Map<String, Object> query = buildOpenTicketQuery(from, size);

        try {
            Map<String, Object> response = restTemplate.postForObject(uri, query, Map.class);
            return parseESHits(response);  // ✅ use your existing parser
        } catch (Exception e) {
            log.error("Failed to fetch open tickets from Elasticsearch", e);
            return Collections.emptyList();
        }
    }

    private Map<String, Object> buildOpenTicketQuery(int from, int size) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        List<Map<String, Object>> must = new ArrayList<>();

        must.add(Map.of("match", Map.of("Data.currentProcessInstance.state.applicationStatus.keyword", "OPEN")));

        bool.put("must", must);
        query.put("query", Map.of("bool", bool));
        query.put("_source", true);
        query.put("from", from);
        query.put("size", size);

        return query;
    }


    private List<Map<String, Object>> parseESHits(Map<String, Object> response) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (response == null) return resultList;

        Map<String, Object> hits = (Map<String, Object>) response.get("hits");
        if (hits == null || !hits.containsKey("hits")) return resultList;

        List<Map<String, Object>> rawHits = (List<Map<String, Object>>) hits.get("hits");
        for (Map<String, Object> hit : rawHits) {
            Map<String, Object> source = (Map<String, Object>) hit.get("_source");
            resultList.add(source);
        }

        return resultList;
    }

    private String getBaseUrl() {
        return esHost + ":" + esPort;
    }
}

