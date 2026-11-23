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
     * % of the vendor's assigned tickets where their first action after entering a vendor-owned
     * workflow state happened within that step's SLA — computed from real egov-workflow-v2
     * process-instance history (see WeeklyEscalationAnalyticsService.resolveVendorResponseRates).
     * Falls back to a current-step-SLA-elapsed proxy only when workflow history is unavailable
     * for a ticket (e.g. the workflow-v2 call failed, or the ticket has no vendor-state entry).
     */
    private double responseRatePct;
    private double resolutionRatePct;
}
