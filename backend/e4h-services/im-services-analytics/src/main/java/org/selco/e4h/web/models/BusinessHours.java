package org.selco.e4h.web.models;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BusinessHours {

    private Map<String, Schedule> schedule = new HashMap<>();

    @Data
    public static class Schedule {
        private final String start;
        private final String end;

        public Schedule(String start, String end) {
            validateTimeFormat(start, "start");
            validateTimeFormat(end, "end");

            if (compareTime(start, end) >= 0) {
                throw new IllegalArgumentException("Start time must be before end time");
            }

            this.start = start;
            this.end = end;
        }

        private void validateTimeFormat(String time, String field) {
            if (time == null || !time.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
                throw new IllegalArgumentException("Invalid " + field +
                        " time format. Expected HH:mm, got: " + time);
            }
        }

        private int compareTime(String time1, String time2) {
            int hour1 = Integer.parseInt(time1.split(":")[0]);
            int minute1 = Integer.parseInt(time1.split(":")[1]);
            int hour2 = Integer.parseInt(time2.split(":")[0]);
            int minute2 = Integer.parseInt(time2.split(":")[1]);

            int totalMinutes1 = hour1 * 60 + minute1;
            int totalMinutes2 = hour2 * 60 + minute2;

            return Integer.compare(totalMinutes1, totalMinutes2);
        }

        public int getStartHour() {
            if (start == null || !start.matches("\\d{2}:\\d{2}")) {
                throw new IllegalArgumentException("Invalid time format. Expected HH:mm, got: " + start);
            }
            return Integer.parseInt(start.split(":")[0]);
        }

        public int getStartMinute() {
            if (start == null || !start.matches("\\d{2}:\\d{2}")) {
                throw new IllegalArgumentException("Invalid time format. Expected HH:mm, got: " + start);
            }
            return Integer.parseInt(start.split(":")[1]);
        }

        public int getEndHour() {
            if (end == null || !end.matches("\\d{2}:\\d{2}")) {
                throw new IllegalArgumentException("Invalid time format. Expected HH:mm, got: " + end);
            }
            return Integer.parseInt(end.split(":")[0]);
        }

        public int getEndMinute() {
            if (end == null || !end.matches("\\d{2}:\\d{2}")) {
                throw new IllegalArgumentException("Invalid time format. Expected HH:mm, got: " + end);
            }
            return Integer.parseInt(end.split(":")[1]);
        }
    }
}
