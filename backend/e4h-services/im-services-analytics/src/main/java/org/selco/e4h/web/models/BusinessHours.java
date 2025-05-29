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
            return Integer.parseInt(start.split(":")[0]);
        }

        public int getStartMinute() {
            return Integer.parseInt(start.split(":")[1]);
        }

        public int getEndHour() {
            return Integer.parseInt(end.split(":")[0]);
        }

        public int getEndMinute() {
            return Integer.parseInt(end.split(":")[1]);
        }
    }
}
