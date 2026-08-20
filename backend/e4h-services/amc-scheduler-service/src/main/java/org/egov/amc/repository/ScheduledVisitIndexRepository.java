package org.egov.amc.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.egov.amc.util.AmcConstants.TOTAL_TICKETS;

/**
 * Write-side access to the scheduled-visit Elasticsearch index, for the one field that cannot go
 * through the normal Kafka indexer pipeline.
 *
 * <p>Pushing a visit onto {@code update-scheduled-visit-index} is a full-document replace, so every
 * field the payload omits is lost. A visit loaded straight from the database has no workflow history
 * and no HRMS-hydrated assignment users - those are only enriched on the request path, where an auth
 * token is available - so re-publishing one from a Kafka consumer would silently strip them from the
 * indexed document. A partial {@code _update} touches only {@code Data.totalTickets} and leaves the
 * rest of the document exactly as the last full index left it.
 */
@Repository
@Slf4j
public class ScheduledVisitIndexRepository {

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 5000;

    private final AMCServiceConfiguration config;

    /**
     * Deliberately not the shared (tracer-provided) RestTemplate, which carries no timeouts - this
     * must fail fast rather than hang the consumer on a degraded Elasticsearch.
     */
    private final RestTemplate restTemplate;

    public ScheduledVisitIndexRepository(AMCServiceConfiguration config) {
        this.config = config;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MS);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    /**
     * Merges {@code Data.totalTickets} into the indexed visit document.
     *
     * <p>Never throws: the count is already committed to {@code scheduled_visits.additional_details},
     * so a document that is missing (not indexed yet) or an unreachable Elasticsearch only means the
     * index lags until the next full index push of that visit carries the value across.
     *
     * @return true when Elasticsearch accepted the update
     */
    public boolean updateTotalTickets(String visitId, Integer totalTickets) {
        if (!StringUtils.hasText(visitId) || totalTickets == null) {
            return false;
        }
        String url = config.getEsHost() + "/" + config.getEsScheduledVisitIndex() + "/_update/" + visitId;
        // "doc" merges recursively for object fields, so Data's other keys survive untouched.
        Map<String, Object> body = Map.of("doc", Map.of("Data", Map.of(TOTAL_TICKETS, totalTickets)));
        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, buildHeaders()), Void.class);
            log.debug("Indexed totalTickets={} for visitId={}", totalTickets, visitId);
            return true;
        } catch (Exception e) {
            log.warn("Could not index totalTickets={} for visitId={}: {}. The value is persisted and will " +
                    "reach the index on the next full push of this visit.", totalTickets, visitId, e.getMessage());
            return false;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(config.getEsUsername())) {
            String credentials = config.getEsUsername() + ":" + config.getEsPassword();
            headers.set(HttpHeaders.AUTHORIZATION,
                    "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)));
        }
        return headers;
    }
}
