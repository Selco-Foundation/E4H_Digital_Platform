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
public class WeeklyVendorPerformanceRow {
    private String vendorName;
    private int newBreachesThisWeek;
    private int ticketsAssigned;
    private int resolvedWithinSlaCount;
    private int resolvedAfterBreachCount;
    /**
     * Approximation: % of the vendor's currently assigned tickets that are within their
     * current-step SLA ({@code slaRemaining >= 0}). Not a literal first-response metric —
     * ES has no first-response timestamp indexed (see ESCALATION_FLOW_WORK_DOC.md open decisions).
     */
    private double responseRatePct;
    private double resolutionRatePct;
}
