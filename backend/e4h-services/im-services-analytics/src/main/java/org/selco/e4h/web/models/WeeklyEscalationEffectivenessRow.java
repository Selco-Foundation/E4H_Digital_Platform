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
public class WeeklyEscalationEffectivenessRow {
    private String levelLabel;
    private int escalatedCount;
    private int resolvedCount;
    private double resolutionRatePct;
}
