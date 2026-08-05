package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

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

    /** Applications present across either week, in report column order. */
    private List<String> applications;

    private UserAnalyticsBucket overall;

    /** Ordered by descending active users in the reported week. */
    private List<UserAnalyticsBucket> byState;

    /** Ordered by descending active users in the reported week. */
    private List<UserAnalyticsBucket> byRole;
}
