package org.selco.e4h.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.config.UserAnalyticsProperties;
import org.selco.e4h.util.UpdateUtils;
import org.selco.e4h.web.models.UserAnalyticsAggregation;
import org.selco.e4h.web.models.UserAnalyticsMetrics;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.selco.e4h.util.UserAnalyticsConstants.AGG_ACTIVE_USERS;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_APPLICATION;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_ROLE;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_STATE;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_LOGINS;
import static org.selco.e4h.util.UserAnalyticsConstants.UNKNOWN;
import static org.selco.e4h.util.UserAnalyticsConstants.USER_LOGIN_EVENT_TYPE;

/**
 * Aggregates a single week out of the {@code user-analytics-report} index.
 * <p>
 * Everything the report needs for one week comes back in one {@code size: 0} search: the all-up
 * counts sit at the top level of {@code aggregations} and the same sub-aggregations are nested under
 * a state {@code terms} and a role {@code terms}, so state and role breakdowns cost no extra
 * round-trips. Active users are a {@code cardinality} over the user uuid rather than a document
 * count, and logins are a {@code filter} on {@code event_type = USER_LOGIN}.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserAnalyticsRepository {

    private static final String SEARCH_PATH = "_search";

    private final RestTemplate restTemplate;
    private final UpdateUtils indexerUtils;
    private final ConsumerConfiguration config;
    private final UserAnalyticsProperties properties;

    /**
     * Runs the aggregation over {@code [from, to)}.
     *
     * @param from inclusive start of the window, UTC
     * @param to   exclusive end of the window, UTC
     */
    @SuppressWarnings("unchecked")
    public UserAnalyticsAggregation aggregate(Instant from, Instant to) {
        String uri = config.getEsHostName() + ":" + config.getEsPortNo()
                + "/" + properties.getIndex() + "/" + SEARCH_PATH;
        Map<String, Object> query = buildQuery(from, to);
        log.info("User analytics: aggregating {} for window [{}, {})", properties.getIndex(), from, to);
        log.debug("User analytics: query {}", query);

        Map<String, Object> response;
        try {
            response = restTemplate.postForObject(uri, new HttpEntity<>(query, buildHeaders()), Map.class);
        } catch (Exception e) {
            log.error("User analytics: aggregation failed on index {} for window [{}, {})",
                    properties.getIndex(), from, to, e);
            throw new CustomException("USER_ANALYTICS_ES_ERROR",
                    "Failed to aggregate user analytics from Elasticsearch: " + e.getMessage());
        }

        Map<String, Object> aggregations = (response == null) ? null
                : (Map<String, Object>) response.get("aggregations");
        if (aggregations == null) {
            log.warn("User analytics: no aggregations in response for window [{}, {}), reporting zeroes", from, to);
            return UserAnalyticsAggregation.empty();
        }

        return UserAnalyticsAggregation.builder()
                .overall(parseMetrics(aggregations))
                .byState(parseDimension(aggregations, AGG_BY_STATE))
                .byRole(parseDimension(aggregations, AGG_BY_ROLE))
                .build();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", indexerUtils.getESEncodedCredentials());
        return headers;
    }

    private Map<String, Object> buildQuery(Instant from, Instant to) {
        Map<String, Object> aggregations = new LinkedHashMap<>(metricAggregations());
        aggregations.put(AGG_BY_STATE, termsAggregation(properties.getStateField()));
        aggregations.put(AGG_BY_ROLE, termsAggregation(properties.getRoleField()));

        Map<String, Object> query = new LinkedHashMap<>();
        query.put("size", 0);
        query.put("query", Map.of("range", Map.of(properties.getEventTimeField(),
                Map.of("gte", from.toString(), "lt", to.toString()))));
        query.put("aggs", aggregations);
        return query;
    }

    /** A state / role {@code terms} bucket carrying the same metrics as the top level. */
    private Map<String, Object> termsAggregation(String field) {
        return Map.of(
                "terms", Map.of(
                        "field", field,
                        "size", properties.getTermsSize(),
                        "missing", UNKNOWN),
                "aggs", metricAggregations());
    }

    /**
     * The metrics computed at every level: distinct active users, login count, and both again split
     * by application.
     */
    private Map<String, Object> metricAggregations() {
        Map<String, Object> aggregations = new LinkedHashMap<>();
        aggregations.put(AGG_ACTIVE_USERS, activeUsersAggregation());
        aggregations.put(AGG_LOGINS, loginsAggregation());
        aggregations.put(AGG_BY_APPLICATION, Map.of(
                "terms", Map.of(
                        "field", properties.getApplicationField(),
                        "size", properties.getTermsSize(),
                        "missing", UNKNOWN),
                "aggs", Map.of(
                        AGG_ACTIVE_USERS, activeUsersAggregation(),
                        AGG_LOGINS, loginsAggregation())));
        return aggregations;
    }

    private Map<String, Object> activeUsersAggregation() {
        return Map.of("cardinality", Map.of(
                "field", properties.getUserField(),
                "precision_threshold", properties.getCardinalityPrecisionThreshold()));
    }

    private Map<String, Object> loginsAggregation() {
        return Map.of("filter", Map.of("term",
                Map.of(properties.getEventTypeField(), USER_LOGIN_EVENT_TYPE)));
    }

    /** Turns a {@code terms} aggregation into bucket key -> metrics. */
    private Map<String, UserAnalyticsMetrics> parseDimension(Map<String, Object> aggregations, String name) {
        Map<String, UserAnalyticsMetrics> byKey = new LinkedHashMap<>();
        for (Map<String, Object> bucket : buckets(aggregations, name)) {
            String key = asString(bucket.get("key"));
            if (key == null) {
                continue;
            }
            byKey.put(key, parseMetrics(bucket));
        }
        return byKey;
    }

    /** Reads the four metrics out of any node that carries {@link #metricAggregations()}. */
    private UserAnalyticsMetrics parseMetrics(Map<String, Object> node) {
        Map<String, Long> activeUsersByApplication = new LinkedHashMap<>();
        Map<String, Long> loginsByApplication = new LinkedHashMap<>();
        for (Map<String, Object> bucket : buckets(node, AGG_BY_APPLICATION)) {
            String application = asString(bucket.get("key"));
            if (application == null) {
                continue;
            }
            activeUsersByApplication.put(application, cardinality(bucket));
            loginsByApplication.put(application, docCount(bucket));
        }
        return UserAnalyticsMetrics.builder()
                .activeUsersByApplication(activeUsersByApplication)
                .activeUsersTotal(cardinality(node))
                .loginsByApplication(loginsByApplication)
                .loginsTotal(docCount(node))
                .build();
    }

    private long cardinality(Map<String, Object> node) {
        return asLong(child(node, AGG_ACTIVE_USERS).get("value"));
    }

    private long docCount(Map<String, Object> node) {
        return asLong(child(node, AGG_LOGINS).get("doc_count"));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buckets(Map<String, Object> node, String aggregationName) {
        Object buckets = child(node, aggregationName).get("buckets");
        return (buckets instanceof List) ? (List<Map<String, Object>>) buckets : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> child(Map<String, Object> node, String aggregationName) {
        Object child = (node == null) ? null : node.get(aggregationName);
        return (child instanceof Map) ? (Map<String, Object>) child : Collections.emptyMap();
    }

    private long asLong(Object value) {
        return (value instanceof Number) ? ((Number) value).longValue() : 0L;
    }

    private String asString(Object value) {
        return (value == null) ? null : value.toString();
    }
}
