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
public class WeeklyBottleneckRow {
    private String statusLabel;
    private long count;
    private double pctOfTotalOpen;
    private double avgDaysOpen;
}
