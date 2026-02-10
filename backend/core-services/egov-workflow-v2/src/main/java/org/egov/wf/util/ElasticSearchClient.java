package org.egov.wf.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.wf.web.models.ProcessInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticSearchClient {

    @Value("${es.index.computed.sla.im.services}")
    private String computedSlaImServicesIndex;

    private final RestTemplate restTemplate;

    @Value("${egov.infra.indexer.host}")
    private String esHostUrl;

    @Value("${egov.indexer.es.host.name}")
    private String esHost;

    @Value("${egov.indexer.es.port.no}")
    private int esPort;

    @Value("${egov.indexer.es.username}")
    private String esUsername;

    @Value("${egov.indexer.es.password}")
    private String esPassword;

    private String INDEX_NAME;

    @PostConstruct
    public void init() {
        log.trace("Initializing ElasticSearchClient");
        this.INDEX_NAME = computedSlaImServicesIndex;
        log.debug("Index name set to: {}", INDEX_NAME);
        log.info("ElasticSearchClient initialized");
    }

//    private static final String DOC_PATH = "_doc";

//    public Map<String, Object> getTicketByIncidentId(String incidentId) {
//        log.trace("Getting Ticket by incident Id: {}", incidentId);
//        return fetchTicketByIncidentId(computedSlaImServicesIndex, incidentId);
//    }
//
//    private Map<String, Object> fetchTicketByIncidentId(String indexName, String incidentId) {
//        log.trace("Fetching ticket by boundary code: {} from index: {}", incidentId, indexName);
//        String uri = getBaseUrl() + "/{index}/" + DOC_PATH + "/{id}";
//        log.debug("Elasticsearch URI: {}", uri);
//        HttpEntity<String> entity = new HttpEntity<>(buildHeaders());
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    uri,
//                    HttpMethod.GET,
//                    entity,
//                    Map.class,
//                    indexName,
//                    incidentId
//            );
//
//            log.info("Fetched ticket audit for incidentId={} from index={}", incidentId, indexName);
//            Map<String, Object> body = response.getBody() != null ? response.getBody() : Collections.emptyMap();
//            log.debug("Retrieved ticket data, keys: {}", body.keySet());
//            Map<String, Object> source = (Map<String, Object>) body.get("_source");
//            Map<String, Object> indexData = (Map<String, Object>) source.get("Data");
//            return indexData;
//
//        } catch (Exception e) {
//            log.error("Failed to fetch ticket audit from index '{}' with incidentId '{}'", indexName, incidentId, e);
//            return Collections.emptyMap();
//        }
//    }

    private String getBaseUrl() {
        log.trace("Getting Elasticsearch base URL");
        String url = esHost + ":" + esPort;
        log.debug("Elasticsearch base URL: {}", url);
        return url;
    }

    public HttpHeaders buildHeaders() {
        log.trace("Building HTTP headers for Elasticsearch request");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getESEncodedCredentials());
        log.debug("HTTP headers built with content type: APPLICATION_JSON");
        return headers;
    }

    public String getESEncodedCredentials() {
        log.trace("Encoding Elasticsearch credentials");
        String credentials = esUsername + ":" + esPassword;
        byte[] credentialsBytes = credentials.getBytes();
        byte[] base64CredentialsBytes = Base64.getEncoder().encode(credentialsBytes);
        log.debug("Elasticsearch credentials encoded successfully");
        return "Basic " + new String(base64CredentialsBytes);
    }

    public void updateProcessInstanceFields(ProcessInstance updatedProcessInstance) {
        String incidentId = updatedProcessInstance.getBusinessId();
        log.trace("Updating process instance for incident ID: {}", incidentId);
        log.info("Updating process instance for incident: {}, businessService: {}", incidentId, updatedProcessInstance.getId());
        Map<String, Object> dataMap = new HashMap<>();

        if (updatedProcessInstance != null) {
            dataMap.put("currentProcessInstance", updatedProcessInstance);
        }
        log.debug("Current Process Instance data prepared:");

        Map<String, Object> doc = new HashMap<>();
        doc.put("Data", dataMap);

        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("doc", doc);

        String url = esHostUrl + computedSlaImServicesIndex+ "/_update/" + incidentId;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateBody, buildHeaders());

        try {
            restTemplate.postForEntity(url, entity, String.class);
            log.info("Successfully updated process instance for incident: {}", incidentId);
        } catch (Exception e) {
            log.error("Failed to update process instance for incident: {}", incidentId, e);
        }
    }
}
