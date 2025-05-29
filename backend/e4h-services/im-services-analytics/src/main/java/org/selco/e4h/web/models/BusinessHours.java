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
