package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * The assembled weekly report: one all-up row plus a row per state and per role, each carrying the
 * reported week, the previous week and the growth between them.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAnalyticsReport {

    /** Monday the reported week starts on, in {@code user.analytics.report.zone}. */
    private LocalDate weekStartDate;

    /** Sunday the reported week ends on, inclusive. */
    private LocalDate weekEndDate;

    private LocalDate previousWeekStartDate;

    private LocalDate previousWeekEndDate;

    /** Zone the week boundaries were cut on, reported so the numbers are reproducible. */
    private String zone;

    /**
     * True when the reported week is the one still in progress, so its counts cover only the days
     * elapsed so far and are not comparable like-for-like against the full previous week.
     */
    private boolean partialWeek;

    /** Applications present across either week, in report column order. */
    private List<String> applications;

    /**
     * Event types seen in the reported week, busiest first — the column order of the By State
     * cross-tabs. Empty when the week carried no events at all.
     */
    private List<String> eventTypes;

    private UserAnalyticsBucket overall;

    /** Ordered by descending active users in the reported week. */
    private List<UserAnalyticsBucket> byState;

    /** Ordered by descending active users in the reported week. */
    private List<UserAnalyticsBucket> byRole;

    /** {@code primary_role} -> its top champion users for the reported week, most active first. */
    private Map<String, List<ChampionUser>> championsByRole;

    /** Application -> its top champion users for the reported week, most active first. */
    private Map<String, List<ChampionUser>> championsByApplication;

    /** Kibana login id -> sign-ins in the reported week, busiest first; the order is the ranking. */
    private Map<String, Long> kibanaLoginsByUser;
}
