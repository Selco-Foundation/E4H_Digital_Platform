package org.selco.e4h.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.config.KibanaDashboardProperties;
import org.selco.e4h.util.UpdateUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

/**
 * Counts the Kibana dashboard views in one week out of the {@code kibana-dashboard-report} index.
 * <p>
 * A plain {@code _count} rather than a {@code size: 0} search with aggregations: the index is
 * dedicated to dashboard views — {@code KibanaDashboardEventListener} is its only producer and drops
 * everything that is not one — so every document in the window is a view, and there is no dimension
 * to break the number down by. The report shows the total and nothing else.
 * <p>
 * Unlike {@link UserAnalyticsRepository}, a failure here does not fail the report: the count is one
 * line on one sheet, and losing it is not worth losing the week's whole workbook over.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class KibanaDashboardRepository {

    private static final String COUNT_PATH = "_count";

    private final RestTemplate restTemplate;
    private final UpdateUtils indexerUtils;
    private final ConsumerConfiguration config;
    private final KibanaDashboardProperties properties;

    /**
     * @param from inclusive start of the window, UTC
     * @param to   exclusive end of the window, UTC
     * @return views in {@code [from, to)}, or 0 if the index is missing or unreachable
     */
    public long countViews(Instant from, Instant to) {
        String uri = config.getEsHostName() + ":" + config.getEsPortNo()
                + "/" + properties.getIndex() + "/" + COUNT_PATH;
        Map<String, Object> query = Map.of("query", Map.of("range",
                Map.of(properties.getEventTimeField(),
                        Map.of("gte", from.toString(), "lt", to.toString()))));

        try {
            Map<?, ?> response = restTemplate.postForObject(uri, new HttpEntity<>(query, buildHeaders()), Map.class);
            Object count = (response == null) ? null : response.get("count");
            if (!(count instanceof Number)) {
                log.warn("Kibana dashboard views: no count in response for window [{}, {}), reporting zero",
                        from, to);
                return 0L;
            }
            long views = ((Number) count).longValue();
            log.info("Kibana dashboard views: {} in window [{}, {})", views, from, to);
            return views;
        } catch (Exception e) {
            // Reported as zero rather than rethrown so the rest of the workbook still goes out. The
            // query goes in the log because an Elasticsearch 400 names a line and column of the
            // request body, which is unreadable without the body itself.
            log.error("Kibana dashboard views: count failed on index {} for window [{}, {}), reporting zero, "
                    + "query was {}", properties.getIndex(), from, to, query, e);
            return 0L;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", indexerUtils.getESEncodedCredentials());
        return headers;
    }
}
