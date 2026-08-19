package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * One row of the report: the reported week's counts, the previous week's counts, and the
 * week-on-week active-user growth between them.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAnalyticsBucket {

    /** {@code OVERALL}, or the state / role this row is for. */
    private String key;

    private UserAnalyticsMetrics current;

    private UserAnalyticsMetrics previous;

    /**
     * Active-user growth from the previous week to the reported week, in percent. Null when the
     * previous week had no active users but the reported week does — the growth is undefined rather
     * than infinite, and the report renders it as {@code N/A}.
     */
    private Double activeUserGrowthPercent;

    /** The same growth computed per application; entries may be null for the same reason. */
    private Map<String, Double> activeUserGrowthPercentByApplication;

    /**
     * Builds a bucket from the two weeks' metrics, deriving both growth figures.
     *
     * @param applications applications to compute per-application growth for
     */
    public static UserAnalyticsBucket of(String key, UserAnalyticsMetrics current,
                                         UserAnalyticsMetrics previous, Set<String> applications) {
        Map<String, Double> growthByApplication = new LinkedHashMap<>();
        for (String application : applications) {
            growthByApplication.put(application,
                    growthPercent(current.activeUsersFor(application), previous.activeUsersFor(application)));
        }
        return UserAnalyticsBucket.builder()
                .key(key)
                .current(current)
                .previous(previous)
                .activeUserGrowthPercent(growthPercent(current.getActiveUsersTotal(), previous.getActiveUsersTotal()))
                .activeUserGrowthPercentByApplication(growthByApplication)
                .build();
    }

    /**
     * Percent change from {@code previous} to {@code current}. Zero when both weeks are zero — no
     * users either week is flat, not undefined — and null when only the previous week is zero.
     */
    private static Double growthPercent(long current, long previous) {
        if (previous == 0L) {
            return (current == 0L) ? 0.0d : null;
        }
        return ((double) (current - previous) / previous) * 100.0d;
    }
}
