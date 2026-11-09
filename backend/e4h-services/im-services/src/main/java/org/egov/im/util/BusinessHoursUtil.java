package org.egov.im.util;

import org.egov.im.web.models.workflow.ProcessInstance;

import java.time.*;
import java.util.*;

import static org.egov.im.util.IMConstants.*;
import static org.egov.im.util.IMConstants.PENDING_RESOLUTION_PREFIX;

public class BusinessHoursUtil {

    private static final int MAX_DAYS_TO_WALK = 3660;

    private final Map<DayOfWeek, Pair<LocalTime, LocalTime>> workingHoursMap;

    @SuppressWarnings("unchecked")
    public BusinessHoursUtil(List<Map<String, Object>> businessHoursConfig) {
        this.workingHoursMap = new HashMap<>();

        for (Map<String, Object> entry : businessHoursConfig) {
            String day = (String) entry.get("day");
            String start = (String) entry.get("start");
            String end = (String) entry.get("end");

            DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
            LocalTime startTime = LocalTime.parse(start);
            LocalTime endTime = LocalTime.parse(end);

            workingHoursMap.put(dayOfWeek, Pair.of(startTime, endTime));
        }
    }

    public long calculateBusinessDuration(ZonedDateTime start, ZonedDateTime end) {
        if (start.isAfter(end)) return 0;

        long duration = 0;

        ZonedDateTime current = start.withZoneSameInstant(ZoneId.of(ASIA_KOLKATA));
        ZonedDateTime endZdt = end.withZoneSameInstant(ZoneId.of(ASIA_KOLKATA));

        while (!current.toLocalDate().isAfter(endZdt.toLocalDate())) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (workingHoursMap.containsKey(dayOfWeek)) {
                LocalDate date = current.toLocalDate();
                Pair<LocalTime, LocalTime> hours = workingHoursMap.get(dayOfWeek);

                ZonedDateTime workStart = ZonedDateTime.of(date, hours.getFirst(), current.getZone());
                ZonedDateTime workEnd = ZonedDateTime.of(date, hours.getSecond(), current.getZone());

                ZonedDateTime from = current.isBefore(workStart) ? workStart : current;
                ZonedDateTime to = endZdt.isBefore(workEnd) ? endZdt : workEnd;

                if (!from.isAfter(to)) {
                    duration += Duration.between(from, to).toMillis();
                }
            }

            // Move to next day's start
            current = current.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }

        return duration;
    }

    public long calculateBusinessDuration(long startMillis, long endMillis) {
        ZoneId zone = ZoneId.of(ASIA_KOLKATA);
        return calculateBusinessDuration(ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis), zone),
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(endMillis), zone));
    }

    /**
     * Returns the instant reached after {@code durationMillis} of business time has elapsed from
     * {@code startMillis} - the inverse of {@link #calculateBusinessDuration(long, long)}. A negative
     * duration walks backwards, so an SLA that is already overrun yields a deadline in the past.
     * Falls back to plain wall-clock arithmetic when no business hours are configured.
     */
    public long addBusinessDuration(long startMillis, long durationMillis) {
        if (durationMillis == 0 || workingHoursMap.isEmpty()) {
            return startMillis + durationMillis;
        }
        ZoneId zone = ZoneId.of(ASIA_KOLKATA);
        ZonedDateTime current = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis), zone);
        long remaining = Math.abs(durationMillis);
        boolean forward = durationMillis > 0;

        // Every week holds at least one working window, so this bound only guards against bad config.
        for (int day = 0; day < MAX_DAYS_TO_WALK; day++) {
            Pair<LocalTime, LocalTime> hours = workingHoursMap.get(current.getDayOfWeek());
            if (hours != null) {
                ZonedDateTime workStart = ZonedDateTime.of(current.toLocalDate(), hours.getFirst(), zone);
                ZonedDateTime workEnd = ZonedDateTime.of(current.toLocalDate(), hours.getSecond(), zone);
                ZonedDateTime from = forward ? (current.isBefore(workStart) ? workStart : current) : workStart;
                ZonedDateTime to = forward ? workEnd : (current.isBefore(workEnd) ? current : workEnd);
                if (from.isBefore(to)) {
                    long available = Duration.between(from, to).toMillis();
                    if (remaining <= available) {
                        return forward ? from.toInstant().toEpochMilli() + remaining
                                : to.toInstant().toEpochMilli() - remaining;
                    }
                    remaining -= available;
                }
            }
            current = forward
                    ? current.toLocalDate().plusDays(1).atStartOfDay(zone)
                    : current.toLocalDate().atStartOfDay(zone).minusNanos(1);
        }
        return startMillis + durationMillis;
    }

    public long calculateBusinessDurationForAllStates(List<ProcessInstance> processInstances) {
        if (processInstances == null || processInstances.isEmpty()) {
            return 0;
        }
        long totalBusinessDuration = 0;

        for (int i = 0; i < processInstances.size(); i++) {
            ProcessInstance current = processInstances.get(i);
            String state = current.getState().getApplicationStatus();

            if (PENDINGFORASSIGNMENT.equals(state) || PENDINGATVENDOR.equals(state)
                    || state.startsWith(PENDING_ASSIGNMENT_PREFIX) || state.startsWith(PENDING_RESOLUTION_PREFIX)) {

                long prevStateTime = current.getAuditDetails().getCreatedTime();
                ZonedDateTime zonedPrevStateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(prevStateTime), ZoneId.of(ASIA_KOLKATA));
                ZonedDateTime zonedNextStateTime;
                if (i + 1 < processInstances.size()) {
                    long nextStateTime = processInstances.get(i + 1).getAuditDetails().getCreatedTime();
                    zonedNextStateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nextStateTime), ZoneId.of(ASIA_KOLKATA));
                } else {
                    zonedNextStateTime = ZonedDateTime.now(ZoneId.of(ASIA_KOLKATA));
                }
                totalBusinessDuration += calculateBusinessDuration(zonedPrevStateTime, zonedNextStateTime);
            }
        }

        return totalBusinessDuration;
    }

    // Utility class to represent a pair
    public static class Pair<F, S> {
        private final F first;
        private final S second;

        private Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public static <F, S> Pair<F, S> of(F first, S second) {
            return new Pair<>(first, second);
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }
    }
}
