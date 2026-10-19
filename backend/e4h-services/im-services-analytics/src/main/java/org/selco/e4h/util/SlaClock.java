package org.selco.e4h.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.selco.e4h.util.IMConstants.BUSINESS_HOUR_MASTER;
import static org.selco.e4h.util.IMConstants.MODULE_NAME_COMMON_MASTERS;

/**
 * Live SLA values from the anchors im-services writes on every state change ({@code stateEnteredTime},
 * {@code totalSlaBase}, {@code stateSlaDueAt} ...), counted in the {@code common-masters.BusinessHours}
 * schedule in IST - the same rules im-services applies, so the values match the inbox.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlaClock {

    public static final String SLA_REMAINING = "slaRemaining";
    public static final String TOTAL_SLA_REMAINING = "totalSlaRemaining";
    public static final String STATE_SLA = "stateSla";
    public static final String STATE_ENTERED_TIME = "stateEnteredTime";
    public static final String TOTAL_SLA_BASE = "totalSlaBase";
    public static final String STATE_SLA_DUE_AT = "stateSlaDueAt";

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    // Business hours are a country-level master, read for the same tenant as the escalation levels
    private static final String BUSINESS_HOURS_TENANT = "in";
    private static final long CACHE_TTL_MS = 3_600_000L;
    private static final int MAX_DAYS_TO_WALK = 3660;

    private final MdmsUtil mdmsUtil;

    private volatile Map<DayOfWeek, LocalTime[]> workingHours;
    private volatile long workingHoursLoadedAt;

    /**
     * Overwrites {@code slaRemaining} / {@code totalSlaRemaining} on the given {@code Data} map with their
     * values at this instant. The anchors only exist while the respective clock runs, so closed tickets,
     * states without an SLA and documents indexed before the anchors existed keep their stored values.
     */
    public void applyLiveSlaRemaining(Map<String, Object> data) {
        Long stateEnteredTime = asLong(data.get(STATE_ENTERED_TIME));
        if (stateEnteredTime == null) {
            return;
        }
        long elapsed = businessMillisBetween(stateEnteredTime, System.currentTimeMillis());

        Long stateSla = asLong(data.get(STATE_SLA));
        if (stateSla != null && data.get(STATE_SLA_DUE_AT) != null) {
            data.put(SLA_REMAINING, stateSla - elapsed);
        }
        Long totalSlaBase = asLong(data.get(TOTAL_SLA_BASE));
        if (totalSlaBase != null) {
            data.put(TOTAL_SLA_REMAINING, totalSlaBase - elapsed);
        }
    }

    public long businessMillisBetween(long startMillis, long endMillis) {
        if (startMillis >= endMillis) {
            return 0;
        }
        Map<DayOfWeek, LocalTime[]> hours = getWorkingHours();
        if (hours.isEmpty()) {
            return endMillis - startMillis;
        }
        ZonedDateTime start = Instant.ofEpochMilli(startMillis).atZone(IST);
        ZonedDateTime end = Instant.ofEpochMilli(endMillis).atZone(IST);
        long total = 0;
        for (ZonedDateTime day = start.toLocalDate().atStartOfDay(IST);
             !day.toLocalDate().isAfter(end.toLocalDate());
             day = day.plusDays(1)) {
            LocalTime[] window = hours.get(day.getDayOfWeek());
            if (window == null) {
                continue;
            }
            ZonedDateTime workStart = ZonedDateTime.of(day.toLocalDate(), window[0], IST);
            ZonedDateTime workEnd = ZonedDateTime.of(day.toLocalDate(), window[1], IST);
            ZonedDateTime from = start.isAfter(workStart) ? start : workStart;
            ZonedDateTime to = end.isBefore(workEnd) ? end : workEnd;
            if (from.isBefore(to)) {
                total += Duration.between(from, to).toMillis();
            }
        }
        return total;
    }

    /**
     * The instant reached after {@code durationMillis} of business time from {@code startMillis}; a
     * negative duration walks backwards. Since stored deadlines always fall inside a working window,
     * "SLA remaining <= T" is exactly "stateSlaDueAt <= addBusinessDuration(now, T)".
     */
    public long addBusinessDuration(long startMillis, long durationMillis) {
        Map<DayOfWeek, LocalTime[]> hours = getWorkingHours();
        if (durationMillis == 0 || hours.isEmpty()) {
            return startMillis + durationMillis;
        }
        ZonedDateTime current = Instant.ofEpochMilli(startMillis).atZone(IST);
        long remaining = Math.abs(durationMillis);
        boolean forward = durationMillis > 0;

        for (int day = 0; day < MAX_DAYS_TO_WALK; day++) {
            LocalTime[] window = hours.get(current.getDayOfWeek());
            if (window != null) {
                ZonedDateTime workStart = ZonedDateTime.of(current.toLocalDate(), window[0], IST);
                ZonedDateTime workEnd = ZonedDateTime.of(current.toLocalDate(), window[1], IST);
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
                    ? current.toLocalDate().plusDays(1).atStartOfDay(IST)
                    : current.toLocalDate().atStartOfDay(IST).minusNanos(1);
        }
        return startMillis + durationMillis;
    }

    /**
     * The schedule, reloaded at most hourly. A failed load keeps the previous schedule; with none loaded
     * yet, time is counted on the wall clock until the next attempt.
     */
    private Map<DayOfWeek, LocalTime[]> getWorkingHours() {
        long now = System.currentTimeMillis();
        if (workingHours == null || now - workingHoursLoadedAt > CACHE_TTL_MS) {
            synchronized (this) {
                if (workingHours == null || now - workingHoursLoadedAt > CACHE_TTL_MS) {
                    Map<DayOfWeek, LocalTime[]> loaded = loadWorkingHours();
                    if (loaded != null) {
                        workingHours = loaded;
                    } else if (workingHours == null) {
                        workingHours = Collections.emptyMap();
                    }
                    workingHoursLoadedAt = now;
                }
            }
        }
        return workingHours;
    }

    @SuppressWarnings("unchecked")
    private Map<DayOfWeek, LocalTime[]> loadWorkingHours() {
        try {
            Map<String, Map<String, JSONArray>> mdmsData = mdmsUtil.fetchMdmsData(new RequestInfo(),
                    BUSINESS_HOURS_TENANT, MODULE_NAME_COMMON_MASTERS, List.of(BUSINESS_HOUR_MASTER));
            JSONArray records = mdmsData.get(MODULE_NAME_COMMON_MASTERS).get(BUSINESS_HOUR_MASTER);
            List<Map<String, Object>> entries = (List<Map<String, Object>>) ((Map<String, Object>) records.get(0)).get(BUSINESS_HOUR_MASTER);

            Map<DayOfWeek, LocalTime[]> loaded = new EnumMap<>(DayOfWeek.class);
            for (Map<String, Object> entry : entries) {
                loaded.put(DayOfWeek.valueOf(String.valueOf(entry.get("day")).toUpperCase()), new LocalTime[]{
                        LocalTime.parse(String.valueOf(entry.get("start"))),
                        LocalTime.parse(String.valueOf(entry.get("end")))});
            }
            log.info("Loaded business hours for {} days", loaded.size());
            return loaded;
        } catch (Exception e) {
            log.error("Failed to load BusinessHours from MDMS", e);
            return null;
        }
    }

    private Long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }
}
