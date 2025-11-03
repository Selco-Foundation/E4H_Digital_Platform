package org.egov.im.util;

import java.time.*;
import java.util.*;

public class BusinessHoursUtil {

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

        ZonedDateTime current = start.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime endZdt = end.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

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
