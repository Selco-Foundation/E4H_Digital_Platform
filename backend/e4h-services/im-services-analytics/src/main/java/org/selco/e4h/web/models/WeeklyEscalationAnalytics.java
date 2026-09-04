package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyEscalationAnalytics {
    private String weekRangeLabel;
    private String asOfDate;
    private String stateListLabel;

    @Builder.Default
    private WeeklyTicketOverview nationalOverview = new WeeklyTicketOverview();

    @Builder.Default
    private Map<String, WeeklyTicketOverview> overviewByState = new HashMap<>();

    @Builder.Default
    private List<WeeklyVendorPerformanceRow> vendorPerformance = new ArrayList<>();

    @Builder.Default
    private List<WeeklyBottleneckRow> bottlenecks = new ArrayList<>();

    private int facilitiesWithOpenTickets;
    private int nfFacilitiesWithOpenTickets;

    private int escalatedLastWeek;
    private int resolvedSinceEscalation;

    private int openTheftCases;
    private int newTheftThisWeek;

    @Builder.Default
    private List<WeeklyNfAlert> nfAlerts = new ArrayList<>();

    private FunctionalMetrics weekStartMetrics;
    private FunctionalMetrics weekEndMetrics;
    private ArrowData functionalArrow;
    private ArrowData nonFunctionalArrow;
}
