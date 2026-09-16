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
public class WeeklyStateNfTrendRow {
    private String stateName;
    private int totalNfLastWeek;
    private int totalNfThisWeek;
    private double nfPctThisWeek;
}
