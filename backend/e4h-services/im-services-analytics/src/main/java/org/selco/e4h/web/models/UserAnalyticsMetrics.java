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

    /**
     * {@code event_type} -> count of those events in the week, across all applications. Only
     * populated for the state dimension, which is the only one cross-tabbed against event type;
     * empty everywhere else.
     */
    private Map<String, Long> eventCountsByType;

    /** Application -> {@code event_type} -> count, the same breakdown split per application. */
    private Map<String, Map<String, Long>> eventCountsByApplicationAndType;

    /** An all-zero bucket, for a state or role present in one week but absent in the other. */
    public static UserAnalyticsMetrics empty() {
        return UserAnalyticsMetrics.builder()
                .activeUsersByApplication(Collections.emptyMap())
                .activeUsersTotal(0L)
                .loginsByApplication(Collections.emptyMap())
                .loginsTotal(0L)
                .eventCountsByType(Collections.emptyMap())
                .eventCountsByApplicationAndType(Collections.emptyMap())
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

    /** Count of one event type across all applications. */
    public long eventCountFor(String eventType) {
        return (eventCountsByType == null) ? 0L
                : eventCountsByType.getOrDefault(eventType, 0L);
    }

    /** Count of one event type within one application. */
    public long eventCountFor(String application, String eventType) {
        if (eventCountsByApplicationAndType == null) {
            return 0L;
        }
        return eventCountsByApplicationAndType
                .getOrDefault(application, Collections.emptyMap())
                .getOrDefault(eventType, 0L);
    }

    /** Never null, so callers can iterate the event types this bucket saw without guarding. */
    public Map<String, Long> eventTypeCounts() {
        return (eventCountsByType == null) ? Collections.emptyMap() : eventCountsByType;
    }
}
