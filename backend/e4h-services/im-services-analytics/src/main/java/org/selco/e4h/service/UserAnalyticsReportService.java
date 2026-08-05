package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.selco.e4h.config.UserAnalyticsProperties;
import org.selco.e4h.repository.UserAnalyticsRepository;
import org.selco.e4h.web.models.UserAnalyticsAggregation;
import org.selco.e4h.web.models.UserAnalyticsBucket;
import org.selco.e4h.web.models.UserAnalyticsMetrics;
import org.selco.e4h.web.models.UserAnalyticsReport;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static org.selco.e4h.util.UserAnalyticsConstants.APPLICATIONS;
import static org.selco.e4h.util.UserAnalyticsConstants.OVERALL;

/**
 * Builds the weekly user-analytics report: resolves the week to report on, aggregates it and the
 * week before it, and pairs the two into rows carrying week-on-week growth.
 * <p>
 * Weeks are Monday-to-Sunday cut in {@code user.analytics.report.zone} rather than UTC — the program
 * runs on Indian local time, and cutting on UTC would push a Monday-morning login in India into the
 * previous week.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAnalyticsReportService {

    private final UserAnalyticsRepository repository;
    private final UserAnalyticsProperties properties;

    /**
     * @param weekStartDate ISO date of the Monday the week starts on; when null or blank the last
     *                      completed week is reported
     * @throws CustomException if the date is unparseable, is not a Monday, or names a week that has
     *                         not finished yet
     */
    public UserAnalyticsReport buildReport(String weekStartDate) {
        ZoneId zone = resolveZone();
        LocalDate weekStart = resolveWeekStart(weekStartDate, zone);
        LocalDate previousWeekStart = weekStart.minusWeeks(1);

        UserAnalyticsAggregation current = repository.aggregate(startOf(weekStart, zone), startOf(weekStart.plusWeeks(1), zone));
        UserAnalyticsAggregation previous = repository.aggregate(startOf(previousWeekStart, zone), startOf(weekStart, zone));

        Set<String> applications = resolveApplications(current, previous);
        log.info("User analytics: week {} had {} active users and {} logins across applications {}",
                weekStart, current.getOverall().getActiveUsersTotal(), current.getOverall().getLoginsTotal(),
                applications);

        return UserAnalyticsReport.builder()
                .weekStartDate(weekStart)
                .weekEndDate(weekStart.plusDays(6))
                .previousWeekStartDate(previousWeekStart)
                .previousWeekEndDate(weekStart.minusDays(1))
                .zone(zone.getId())
                .applications(new ArrayList<>(applications))
                .overall(UserAnalyticsBucket.of(OVERALL, current.getOverall(), previous.getOverall(), applications))
                .byState(buildDimension(current.getByState().keySet(), previous.getByState().keySet(),
                        current::stateMetrics, previous::stateMetrics, applications))
                .byRole(buildDimension(current.getByRole().keySet(), previous.getByRole().keySet(),
                        current::roleMetrics, previous::roleMetrics, applications))
                .build();
    }

    /**
     * Pairs each dimension value across both weeks. A value present in only one week still gets a
     * row, with {@link UserAnalyticsMetrics#empty()} standing in for the week it is missing from —
     * that is what makes a brand new state show as growth from zero rather than vanishing.
     */
    private List<UserAnalyticsBucket> buildDimension(Set<String> currentKeys, Set<String> previousKeys,
                                                     Function<String, UserAnalyticsMetrics> currentMetrics,
                                                     Function<String, UserAnalyticsMetrics> previousMetrics,
                                                     Set<String> applications) {
        Set<String> keys = new TreeSet<>(currentKeys);
        keys.addAll(previousKeys);

        List<UserAnalyticsBucket> buckets = new ArrayList<>(keys.size());
        for (String key : keys) {
            buckets.add(UserAnalyticsBucket.of(key, currentMetrics.apply(key), previousMetrics.apply(key), applications));
        }
        // Busiest first, so the states and roles that matter are at the top of the sheet.
        buckets.sort(Comparator
                .comparingLong((UserAnalyticsBucket bucket) -> bucket.getCurrent().getActiveUsersTotal()).reversed()
                .thenComparing(UserAnalyticsBucket::getKey));
        return buckets;
    }

    /**
     * The three known applications in column order, followed by anything else the index turned up so
     * a newly added front-end is still reported instead of being silently dropped.
     */
    private Set<String> resolveApplications(UserAnalyticsAggregation current, UserAnalyticsAggregation previous) {
        Set<String> applications = new LinkedHashSet<>(APPLICATIONS);
        Set<String> found = new TreeSet<>();
        found.addAll(current.getOverall().getActiveUsersByApplication().keySet());
        found.addAll(previous.getOverall().getActiveUsersByApplication().keySet());
        found.removeAll(APPLICATIONS);
        if (!found.isEmpty()) {
            log.info("User analytics: index carries applications outside the known set, appending {}", found);
            applications.addAll(found);
        }
        return applications;
    }

    private LocalDate resolveWeekStart(String weekStartDate, ZoneId zone) {
        LocalDate currentWeekStart = LocalDate.now(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (weekStartDate == null || weekStartDate.isBlank()) {
            LocalDate lastCompletedWeekStart = currentWeekStart.minusWeeks(1);
            log.info("User analytics: no weekStartDate given, reporting the last completed week starting {}",
                    lastCompletedWeekStart);
            return lastCompletedWeekStart;
        }

        LocalDate weekStart;
        try {
            weekStart = LocalDate.parse(weekStartDate.trim());
        } catch (DateTimeParseException e) {
            throw new CustomException("INVALID_WEEK_START_DATE",
                    "weekStartDate must be an ISO date such as 2026-07-27, got: " + weekStartDate);
        }
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new CustomException("INVALID_WEEK_START_DATE",
                    "weekStartDate must be a Monday, but " + weekStart + " is a "
                            + weekStart.getDayOfWeek() + ". The nearest Monday is "
                            + weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        }
        if (!weekStart.isBefore(currentWeekStart)) {
            throw new CustomException("INVALID_WEEK_START_DATE",
                    "weekStartDate " + weekStart + " is not a completed week; the latest reportable week starts "
                            + currentWeekStart.minusWeeks(1));
        }
        return weekStart;
    }

    private Instant startOf(LocalDate date, ZoneId zone) {
        return date.atStartOfDay(zone).toInstant();
    }

    private ZoneId resolveZone() {
        try {
            return ZoneId.of(properties.getReportZone());
        } catch (Exception e) {
            throw new CustomException("INVALID_REPORT_ZONE",
                    "user.analytics.report.zone is not a valid zone id: " + properties.getReportZone());
        }
    }
}
