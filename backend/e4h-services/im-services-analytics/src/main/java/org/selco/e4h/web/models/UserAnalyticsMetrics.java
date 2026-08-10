package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.Map;

/**
 * The counts for one week within one dimension bucket (overall, or a single state / role).
 * <p>
 * {@code activeUsersTotal} is a distinct count over the whole bucket, so it is not the sum of
 * {@code activeUsersByApplication} — a user active in both Saura eMitra and Field Assist is counted
 * once in the total and once under each application.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAnalyticsMetrics {

    /** Application -> distinct users with at least one event in the week. */
    private Map<String, Long> activeUsersByApplication;

    /** Distinct users with at least one event in the week, across all applications. */
    private long activeUsersTotal;

    /** Application -> count of {@code USER_LOGIN} events in the week. */
    private Map<String, Long> loginsByApplication;

    /** Count of {@code USER_LOGIN} events in the week, across all applications. */
    private long loginsTotal;

    /** An all-zero bucket, for a state or role present in one week but absent in the other. */
    public static UserAnalyticsMetrics empty() {
        return UserAnalyticsMetrics.builder()
                .activeUsersByApplication(Collections.emptyMap())
                .activeUsersTotal(0L)
                .loginsByApplication(Collections.emptyMap())
                .loginsTotal(0L)
                .build();
    }

    public long activeUsersFor(String application) {
        return (activeUsersByApplication == null) ? 0L
                : activeUsersByApplication.getOrDefault(application, 0L);
    }

    public long loginsFor(String application) {
        return (loginsByApplication == null) ? 0L
                : loginsByApplication.getOrDefault(application, 0L);
    }
}
