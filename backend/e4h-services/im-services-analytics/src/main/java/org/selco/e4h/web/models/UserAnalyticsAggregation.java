package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * One week's worth of aggregation output, as returned by a single Elasticsearch round-trip.
 * The report pairs two of these — the reported week and the one before it — into
 * {@link UserAnalyticsBucket} rows.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAnalyticsAggregation {

    /** Counts across every document in the week, ignoring state and role. */
    private UserAnalyticsMetrics overall;

    /** Localized state name -> counts. Documents with a null state are left out entirely. */
    private Map<String, UserAnalyticsMetrics> byState;

    /** {@code primary_role} -> counts. Documents with a null role are left out entirely. */
    private Map<String, UserAnalyticsMetrics> byRole;

    /**
     * {@code primary_role} -> its most active users, highest activity first. Only populated for the
     * reported week; the previous week is aggregated purely to compute growth.
     */
    private Map<String, List<ChampionUser>> championsByRole;

    /** Application -> its most active users, highest activity first. */
    private Map<String, List<ChampionUser>> championsByApplication;

    /**
     * Kibana login id -> how many times it signed in, busiest first. Iteration order is the ranking,
     * so this is a {@code LinkedHashMap}. Reported week only, like the champions.
     */
    private Map<String, Long> kibanaLoginsByUser;

    public static UserAnalyticsAggregation empty() {
        return UserAnalyticsAggregation.builder()
                .overall(UserAnalyticsMetrics.empty())
                .byState(Collections.emptyMap())
                .byRole(Collections.emptyMap())
                .championsByRole(Collections.emptyMap())
                .championsByApplication(Collections.emptyMap())
                .kibanaLoginsByUser(Collections.emptyMap())
                .build();
    }

    public UserAnalyticsMetrics stateMetrics(String state) {
        return (byState == null) ? UserAnalyticsMetrics.empty()
                : byState.getOrDefault(state, UserAnalyticsMetrics.empty());
    }

    public UserAnalyticsMetrics roleMetrics(String role) {
        return (byRole == null) ? UserAnalyticsMetrics.empty()
                : byRole.getOrDefault(role, UserAnalyticsMetrics.empty());
    }
}
