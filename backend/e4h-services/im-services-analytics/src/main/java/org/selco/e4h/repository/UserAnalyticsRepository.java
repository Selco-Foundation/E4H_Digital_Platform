package org.selco.e4h.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.config.UserAnalyticsProperties;
import org.selco.e4h.util.UpdateUtils;
import org.selco.e4h.web.models.ChampionUser;
import org.selco.e4h.web.models.UserAnalyticsAggregation;
import org.selco.e4h.web.models.UserAnalyticsMetrics;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.selco.e4h.util.UserAnalyticsConstants.AGG_ACTIVE_USERS;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_APPLICATION;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_GROUP;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_ROLE;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_STATE;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_CHAMPIONS_BY_APPLICATION;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_CHAMPIONS_BY_ROLE;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_LOGINS;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_TOP_USERS;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_USER_DETAILS;
import static org.selco.e4h.util.UserAnalyticsConstants.UNKNOWN;
import static org.selco.e4h.util.UserAnalyticsConstants.USER_LOGIN_EVENT_TYPE;
import static org.selco.e4h.util.UserAnalyticsConstants.USER_NAME_SOURCE_PATH;
import static org.selco.e4h.util.UserAnalyticsConstants.USER_USERNAME_SOURCE_PATH;

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
     * @param from              inclusive start of the window, UTC
     * @param to                exclusive end of the window, UTC
     * @param includeChampions  whether to also rank champion users; false for the previous week,
     *                          which is only aggregated to compute growth and whose champions would
     *                          never be read
     */
    @SuppressWarnings("unchecked")
    public UserAnalyticsAggregation aggregate(Instant from, Instant to, boolean includeChampions) {
        String uri = config.getEsHostName() + ":" + config.getEsPortNo()
                + "/" + properties.getIndex() + "/" + SEARCH_PATH;
        Map<String, Object> query = buildQuery(from, to, includeChampions);
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
                .championsByRole(parseChampions(aggregations, AGG_CHAMPIONS_BY_ROLE))
                .championsByApplication(parseChampions(aggregations, AGG_CHAMPIONS_BY_APPLICATION))
                .build();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", indexerUtils.getESEncodedCredentials());
        return headers;
    }

    private Map<String, Object> buildQuery(Instant from, Instant to, boolean includeChampions) {
        Map<String, Object> aggregations = new LinkedHashMap<>(metricAggregations());
        aggregations.put(AGG_BY_STATE, termsAggregation(properties.getStateField()));
        aggregations.put(AGG_BY_ROLE, termsAggregation(properties.getRoleField()));
        if (includeChampions) {
            aggregations.put(AGG_CHAMPIONS_BY_ROLE, championsAggregation(properties.getRoleField()));
            aggregations.put(AGG_CHAMPIONS_BY_APPLICATION, championsAggregation(properties.getApplicationField()));
        }

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

    /**
     * Ranks the top users within each value of {@code groupField}, counting every event except
     * {@code USER_LOGIN} — signing in is not activity worth crowning someone for.
     * <p>
     * The users {@code terms} orders on {@code doc_count} by default, which is exactly the ranking
     * wanted, and a one-hit {@code top_hits} carries the name and login id back so the report does
     * not have to resolve uuids against the user service afterwards.
     */
    private Map<String, Object> championsAggregation(String groupField) {
        Map<String, Object> topUsers = Map.of(
                "terms", Map.of(
                        "field", properties.getUserField(),
                        "size", properties.getChampionCount()),
                "aggs", Map.of(AGG_USER_DETAILS, Map.of(
                        "top_hits", Map.of(
                                "size", 1,
                                "_source", Map.of("includes",
                                        List.of(USER_NAME_SOURCE_PATH, USER_USERNAME_SOURCE_PATH))))));

        return Map.of(
                "filter", Map.of("bool", Map.of("must_not",
                        List.of(Map.of("term", Map.of(properties.getEventTypeField(), USER_LOGIN_EVENT_TYPE))))),
                "aggs", Map.of(AGG_BY_GROUP, Map.of(
                        "terms", Map.of(
                                "field", groupField,
                                "size", properties.getTermsSize(),
                                "missing", UNKNOWN),
                        "aggs", topUsers)));
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

    /**
     * Turns a champions filter into group key -> ranked users. Absent when the caller asked for no
     * champions, in which case there is nothing to read and the map comes back empty.
     */
    private Map<String, List<ChampionUser>> parseChampions(Map<String, Object> aggregations, String name) {
        Map<String, List<ChampionUser>> byKey = new LinkedHashMap<>();
        for (Map<String, Object> group : buckets(child(aggregations, name), AGG_BY_GROUP)) {
            String key = asString(group.get("key"));
            if (key == null) {
                continue;
            }
            byKey.put(key, parseChampionUsers(group));
        }
        return byKey;
    }

    private List<ChampionUser> parseChampionUsers(Map<String, Object> group) {
        List<ChampionUser> champions = new ArrayList<>();
        for (Map<String, Object> userBucket : buckets(group, AGG_TOP_USERS)) {
            Map<String, Object> user = championUserSource(userBucket);
            champions.add(ChampionUser.builder()
                    .uuid(asString(userBucket.get("key")))
                    .userName(asString(user.get("userName")))
                    .name(asString(user.get("name")))
                    .activityCount(asLong(userBucket.get("doc_count")))
                    .build());
        }
        return champions;
    }

    /**
     * Digs {@code Data.user} out of the single {@code top_hits} document. Returns an empty map when
     * the hit is missing, so a champion still lists with a blank name rather than failing the report.
     */
    private Map<String, Object> championUserSource(Map<String, Object> userBucket) {
        List<Map<String, Object>> hits = listOf(child(child(userBucket, AGG_USER_DETAILS), "hits"), "hits");
        if (hits.isEmpty()) {
            return Collections.emptyMap();
        }
        return child(child(child(hits.get(0), "_source"), "Data"), "user");
    }

    private long cardinality(Map<String, Object> node) {
        return asLong(child(node, AGG_ACTIVE_USERS).get("value"));
    }

    private long docCount(Map<String, Object> node) {
        return asLong(child(node, AGG_LOGINS).get("doc_count"));
    }

    private List<Map<String, Object>> buckets(Map<String, Object> node, String aggregationName) {
        return listOf(child(node, aggregationName), "buckets");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOf(Map<String, Object> node, String key) {
        Object value = (node == null) ? null : node.get(key);
        return (value instanceof List) ? (List<Map<String, Object>>) value : Collections.emptyList();
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
