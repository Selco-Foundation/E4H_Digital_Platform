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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.selco.e4h.util.UserAnalyticsConstants.AGG_ACTIVE_USERS;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_APPLICATION;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_EVENT_TYPE;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_GROUP;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_ROLE;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_STATE;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_BY_USER_NAME;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_CHAMPIONS_BY_APPLICATION;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_CHAMPIONS_BY_ROLE;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_KIBANA_LOGINS;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_LOGINS;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_TOP_USERS;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_USER_DETAILS;
import static org.selco.e4h.util.UserAnalyticsConstants.AGG_VENDOR_ACTIONS;
import static org.selco.e4h.util.UserAnalyticsConstants.KIBANA_LOGIN_EVENT_TYPE;
import static org.selco.e4h.util.UserAnalyticsConstants.USER_LOGIN_EVENT_TYPE;
import static org.selco.e4h.util.UserAnalyticsConstants.USER_NAME_SOURCE_PATH;
import static org.selco.e4h.util.UserAnalyticsConstants.USER_USERNAME_SOURCE_PATH;
import static org.selco.e4h.util.UserAnalyticsConstants.VENDOR_SYSTEM_ROLE;

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
     * @param from          inclusive start of the window, UTC
     * @param to            exclusive end of the window, UTC
     * @param reportedWeek  whether this is the week being reported on rather than the one before it.
     *                      The champion ranking and the Kibana login list are only ever read off the
     *                      reported week, so they are not asked for on the previous week, which
     *                      exists solely to compute growth
     */
    @SuppressWarnings("unchecked")
    public UserAnalyticsAggregation aggregate(Instant from, Instant to, boolean reportedWeek) {
        String uri = config.getEsHostName() + ":" + config.getEsPortNo()
                + "/" + properties.getIndex() + "/" + SEARCH_PATH;
        Map<String, Object> query = buildQuery(from, to, reportedWeek);
        log.info("User analytics: aggregating {} for window [{}, {})", properties.getIndex(), from, to);
        log.debug("User analytics: query {}", query);

        Map<String, Object> response;
        try {
            response = restTemplate.postForObject(uri, new HttpEntity<>(query, buildHeaders()), Map.class);
        } catch (Exception e) {
            // The query goes in the error log, not just at debug — an Elasticsearch 400 names a line
            // and column of the request body, which is unreadable without the body itself.
            log.error("User analytics: aggregation failed on index {} for window [{}, {}), query was {}",
                    properties.getIndex(), from, to, query, e);
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
                .kibanaLoginsByUser(parseKibanaLogins(aggregations))
                .build();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", indexerUtils.getESEncodedCredentials());
        return headers;
    }

    private Map<String, Object> buildQuery(Instant from, Instant to, boolean reportedWeek) {
        Map<String, Object> aggregations = new LinkedHashMap<>(metricAggregations(false));
        aggregations.put(AGG_BY_STATE, termsAggregation(properties.getStateField(), true));
        aggregations.put(AGG_BY_ROLE, termsAggregation(properties.getRoleField(), false));
        if (reportedWeek) {
            aggregations.put(AGG_CHAMPIONS_BY_ROLE, championsAggregation(properties.getRoleField()));
            aggregations.put(AGG_CHAMPIONS_BY_APPLICATION, championsAggregation(properties.getApplicationField()));
            aggregations.put(AGG_KIBANA_LOGINS, kibanaLoginsAggregation());
        }

        Map<String, Object> query = new LinkedHashMap<>();
        query.put("size", 0);
        query.put("query", Map.of("range", Map.of(properties.getEventTimeField(),
                Map.of("gte", from.toString(), "lt", to.toString()))));
        query.put("aggs", aggregations);
        return query;
    }

    /**
     * A state / role {@code terms} bucket carrying the same metrics as the top level.
     * <p>
     * No {@code missing} bucket is requested: a document whose field is null is left out of the
     * breakdown altogether rather than being reported under a placeholder key.
     *
     * @param includeEventTypes whether to also break the bucket down by event type
     */
    private Map<String, Object> termsAggregation(String field, boolean includeEventTypes) {
        return Map.of(
                "terms", Map.of(
                        "field", field,
                        "size", properties.getTermsSize()),
                "aggs", metricAggregations(includeEventTypes));
    }

    /**
     * The metrics computed at every level: distinct active users, login count, and both again split
     * by application.
     *
     * @param includeEventTypes adds an event-type {@code terms} beside the metrics and again inside
     *                          the per-application buckets, plus the vendor-action count within each
     *                          application — between them the numbers the By State event tables read
     */
    private Map<String, Object> metricAggregations(boolean includeEventTypes) {
        Map<String, Object> applicationAggregations = new LinkedHashMap<>();
        applicationAggregations.put(AGG_ACTIVE_USERS, activeUsersAggregation());
        applicationAggregations.put(AGG_LOGINS, loginsAggregation());
        if (includeEventTypes) {
            applicationAggregations.put(AGG_BY_EVENT_TYPE, eventTypesAggregation());
            applicationAggregations.put(AGG_VENDOR_ACTIONS, vendorActionsAggregation());
        }

        Map<String, Object> aggregations = new LinkedHashMap<>();
        aggregations.put(AGG_ACTIVE_USERS, activeUsersAggregation());
        aggregations.put(AGG_LOGINS, loginsAggregation());
        aggregations.put(AGG_BY_APPLICATION, Map.of(
                "terms", Map.of(
                        "field", properties.getApplicationField(),
                        "size", properties.getTermsSize()),
                "aggs", applicationAggregations));
        if (includeEventTypes) {
            aggregations.put(AGG_BY_EVENT_TYPE, eventTypesAggregation());
        }
        return aggregations;
    }

    /**
     * Every event type seen in the bucket with its document count. {@code USER_LOGIN} is included
     * here — unlike the champions ranking, the cross-tab is a plain census of what happened.
     */
    private Map<String, Object> eventTypesAggregation() {
        return Map.of("terms", Map.of(
                "field", properties.getEventTypeField(),
                "size", properties.getEventTypeTermsSize()));
    }

    /** Every event the vendor role produced in the bucket, regardless of what the action was. */
    private Map<String, Object> vendorActionsAggregation() {
        return Map.of("filter", Map.of("term",
                Map.of(properties.getSystemRoleField(), VENDOR_SYSTEM_ROLE)));
    }

    /**
     * Kibana sign-ins per login id, busiest first — {@code terms} orders on {@code doc_count} by
     * default, which is the ranking the sheet wants. Grouped on the login id rather than the uuid
     * because Kibana accounts are Elasticsearch-native and carry no egov uuid.
     */
    private Map<String, Object> kibanaLoginsAggregation() {
        return Map.of(
                "filter", Map.of("term",
                        Map.of(properties.getEventTypeField(), KIBANA_LOGIN_EVENT_TYPE)),
                "aggs", Map.of(AGG_BY_USER_NAME, Map.of(
                        "terms", Map.of(
                                "field", properties.getUserNameField(),
                                "size", properties.getTermsSize()))));
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
     * Ranks the top user-and-role pairs within each value of {@code groupField}, counting every event
     * except {@code USER_LOGIN} — signing in is not activity worth crowning someone for.
     * <p>
     * The ranked entity is a role together with a user, so the users {@code terms} sits underneath a
     * role {@code terms} rather than beside it. Asking for the top {@link
     * UserAnalyticsProperties#getChampionCount() championCount} users of <em>every</em> role and
     * ranking the pairs in {@link #parseChampionUsers(Map)} gives the same answer as ranking the
     * pairs in Elasticsearch would: a pair that belongs in the group's top N is necessarily in its
     * own role's top N as well.
     * <p>
     * Both {@code terms} order on {@code doc_count} by default, and a one-hit {@code top_hits}
     * carries the name and login id back so the report does not have to resolve uuids against the
     * user service afterwards.
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

        Map<String, Object> byRole = Map.of(
                "terms", Map.of(
                        "field", properties.getRoleField(),
                        "size", properties.getTermsSize()),
                // topUsers is an aggregation body, so it has to be keyed by its name here — handing
                // it to "aggs" bare makes Elasticsearch read "terms" as the aggregation's name
                // rather than its type.
                "aggs", Map.of(AGG_TOP_USERS, topUsers));

        return Map.of(
                "filter", Map.of("bool", Map.of("must_not",
                        List.of(Map.of("term", Map.of(properties.getEventTypeField(), USER_LOGIN_EVENT_TYPE))))),
                "aggs", Map.of(AGG_BY_GROUP, Map.of(
                        "terms", Map.of(
                                "field", groupField,
                                "size", properties.getTermsSize()),
                        "aggs", Map.of(AGG_BY_ROLE, byRole))));
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
        Map<String, Map<String, Long>> eventCountsByApplicationAndType = new LinkedHashMap<>();
        Map<String, Long> vendorActionsByApplication = new LinkedHashMap<>();
        for (Map<String, Object> bucket : buckets(node, AGG_BY_APPLICATION)) {
            String application = asString(bucket.get("key"));
            if (application == null) {
                continue;
            }
            activeUsersByApplication.put(application, cardinality(bucket));
            loginsByApplication.put(application, docCount(bucket));
            eventCountsByApplicationAndType.put(application, parseEventCounts(bucket));
            // Absent on the levels that asked for no event breakdown, where it reads as zero.
            vendorActionsByApplication.put(application,
                    asLong(child(bucket, AGG_VENDOR_ACTIONS).get("doc_count")));
        }
        return UserAnalyticsMetrics.builder()
                .activeUsersByApplication(activeUsersByApplication)
                .activeUsersTotal(cardinality(node))
                .loginsByApplication(loginsByApplication)
                .loginsTotal(docCount(node))
                .eventCountsByType(parseEventCounts(node))
                .eventCountsByApplicationAndType(eventCountsByApplicationAndType)
                .vendorActionsByApplication(vendorActionsByApplication)
                .build();
    }

    /**
     * Kibana login id -> sign-in count, in the order Elasticsearch ranked them. Comes back empty for
     * the previous week, which never asks for the aggregation.
     */
    private Map<String, Long> parseKibanaLogins(Map<String, Object> aggregations) {
        Map<String, Long> byUserName = new LinkedHashMap<>();
        for (Map<String, Object> bucket : buckets(child(aggregations, AGG_KIBANA_LOGINS), AGG_BY_USER_NAME)) {
            String userName = asString(bucket.get("key"));
            if (userName == null) {
                continue;
            }
            byUserName.put(userName, asLong(bucket.get("doc_count")));
        }
        return byUserName;
    }

    /**
     * Event type -> document count. Comes back empty for the levels that did not ask for the
     * breakdown — the overall and by-role nodes simply carry no {@code by_event_type} to read.
     */
    private Map<String, Long> parseEventCounts(Map<String, Object> node) {
        Map<String, Long> byEventType = new LinkedHashMap<>();
        for (Map<String, Object> bucket : buckets(node, AGG_BY_EVENT_TYPE)) {
            String eventType = asString(bucket.get("key"));
            if (eventType == null) {
                continue;
            }
            byEventType.put(eventType, asLong(bucket.get("doc_count")));
        }
        return byEventType;
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

    /**
     * Flattens the role and user buckets into one ranking of user-and-role pairs, busiest first, cut
     * back to the champion count the group is meant to list.
     */
    private List<ChampionUser> parseChampionUsers(Map<String, Object> group) {
        List<ChampionUser> champions = new ArrayList<>();
        for (Map<String, Object> roleBucket : buckets(group, AGG_BY_ROLE)) {
            String role = asString(roleBucket.get("key"));
            for (Map<String, Object> userBucket : buckets(roleBucket, AGG_TOP_USERS)) {
                Map<String, Object> user = championUserSource(userBucket);
                champions.add(ChampionUser.builder()
                        .uuid(asString(userBucket.get("key")))
                        .role(role)
                        .userName(asString(user.get("userName")))
                        .name(asString(user.get("name")))
                        .activityCount(asLong(userBucket.get("doc_count")))
                        .build());
            }
        }
        champions.sort(Comparator.comparingLong(ChampionUser::getActivityCount).reversed());
        int limit = Math.min(champions.size(), properties.getChampionCount());
        return new ArrayList<>(champions.subList(0, limit));
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
