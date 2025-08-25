package org.selco.e4h.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.service.UpdateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticSearchClient {

    private final RestTemplate restTemplate;
    private final UpdateService updateService;

    @Value("${egov.indexer.es.host.name}")
    private String esHost;

    @Value("${egov.indexer.es.port.no}")
    private int esPort;

    private static final String SEARCH_PATH = "_search";
    private static final String INDEX_NAME = "computed-sla-im-services";
    private static final String OLD_INDEX_NAME = "im-services";

    private static final String INDEX_NAME_PHC = "phc-master-list-new-2";

    private static final String DOC_PATH = "_doc";

    public List<Map<String, Object>> fetchOpenTickets(int from, int size) {
        return fetchTickets(INDEX_NAME, from, size);
    }

    public List<Map<String, Object>> fetchOldOpenTicketsFromImServices(int from, int size) {
        return fetchTickets(OLD_INDEX_NAME, from, size);
    }

    public Map<String, Object> getHFByTenantId(String tenantId) {
        return fetchTicketById(INDEX_NAME_PHC, tenantId);
    }

    public List<Map<String, Object>> getAllPHC(int from, int size) {
        return fetchAllPHCs(INDEX_NAME_PHC, from, size);
    }

    private List<Map<String, Object>> fetchTickets(String indexName, int from, int size) {
        String uri = getBaseUrl() + "/" + indexName + "/" + SEARCH_PATH;
        Map<String, Object> query = buildOpenTicketQuery(from, size);
        HttpEntity<Object> entity = new HttpEntity<>(query, updateService.buildHeaders());

        try {
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);
            return parseESHits(response);
        } catch (Exception e) {
            log.error("Failed to fetch open tickets from index '{}'", indexName, e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> fetchAllPHCs(String indexName, int from, int size) {
        String uri = getBaseUrl() + "/" + indexName + "/" + SEARCH_PATH;
        Map<String, Object> query = buildHFQuery(from, size);
        HttpEntity<Object> entity = new HttpEntity<>(query, updateService.buildHeaders());
        try {
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);
            return parseESHits(response);
        } catch (Exception e) {
            log.error("Failed to fetch open tickets from index '{}'", indexName, e);
            return Collections.emptyList();
        }
    }

    private Map<String, Object> fetchTicketById(String indexName, String tenantId) {
        String uri = getBaseUrl() + "/" + indexName + "/" + DOC_PATH + "/" + tenantId;

        HttpEntity<String> entity = new HttpEntity<>(updateService.buildHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            log.info("Fetched ticket audit for tenantId={} from index={}", tenantId, indexName);
            return response.getBody() != null ? response.getBody() : Collections.emptyMap();

        } catch (Exception e) {
            log.error("Failed to fetch ticket audit from index '{}' with tenantId '{}'", indexName, tenantId, e);
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> buildOpenTicketQuery(int from, int size) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();

        List<Map<String, Object>> mustNot = new ArrayList<>();
        mustNot.add(Map.of("term", Map.of("Data.currentProcessInstance.state.isTerminateState", true)));

        mustNot.add(Map.of("terms", Map.of(
                "Data.currentProcessInstance.state.applicationStatus.keyword",
                List.of("RESOLVED", "CLOSED_AFTER_RESOLUTION", "REJECTED")
        )));

        bool.put("must_not", mustNot);
        query.put("query", Map.of("bool", bool));
        query.put("_source", true);
        query.put("from", from);
        query.put("size", size);

        return query;
    }

    private Map<String, Object> buildHFQuery(int from, int size) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();

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
