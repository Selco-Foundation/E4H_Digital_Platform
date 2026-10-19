package org.egov.inbox.util;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Business time between two instants under the {@code common-masters.BusinessHours} schedule, read in
 * IST. Same rules im-services uses when it writes the SLA anchors: one working window per configured
 * day, days without an entry count as non-working.
 */
public class BusinessHoursCalculator {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final Map<DayOfWeek, LocalTime[]> workingHours = new EnumMap<>(DayOfWeek.class);

    public BusinessHoursCalculator(List<Map<String, Object>> businessHoursConfig) {
        for (Map<String, Object> entry : businessHoursConfig) {
            DayOfWeek day = DayOfWeek.valueOf(String.valueOf(entry.get("day")).toUpperCase());
            workingHours.put(day, new LocalTime[]{
                    LocalTime.parse(String.valueOf(entry.get("start"))),
                    LocalTime.parse(String.valueOf(entry.get("end")))});
        }
    }

    public long businessMillisBetween(long startMillis, long endMillis) {
        if (startMillis >= endMillis) {
            return 0;
        }
        if (workingHours.isEmpty()) {
            return endMillis - startMillis;
        }
        ZonedDateTime start = Instant.ofEpochMilli(startMillis).atZone(IST);
        ZonedDateTime end = Instant.ofEpochMilli(endMillis).atZone(IST);
        long total = 0;
        for (ZonedDateTime day = start.toLocalDate().atStartOfDay(IST);
             !day.toLocalDate().isAfter(end.toLocalDate());
             day = day.plusDays(1)) {
            LocalTime[] hours = workingHours.get(day.getDayOfWeek());
            if (hours == null) {
                continue;
            }
            ZonedDateTime workStart = ZonedDateTime.of(day.toLocalDate(), hours[0], IST);
            ZonedDateTime workEnd = ZonedDateTime.of(day.toLocalDate(), hours[1], IST);
            ZonedDateTime from = start.isAfter(workStart) ? start : workStart;
            ZonedDateTime to = end.isBefore(workEnd) ? end : workEnd;
            if (from.isBefore(to)) {
                total += Duration.between(from, to).toMillis();
            }
        }
        return total;
    }
}
