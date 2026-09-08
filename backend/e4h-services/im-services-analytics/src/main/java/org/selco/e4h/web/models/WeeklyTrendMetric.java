package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyTrendMetric {
    private String label;
    private long lastWeekValue;
    private long thisWeekValue;
    private double changePct;
    private String arrow;
}
