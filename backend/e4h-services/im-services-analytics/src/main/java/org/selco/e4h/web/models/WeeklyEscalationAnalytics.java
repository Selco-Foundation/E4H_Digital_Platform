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

    private int openTheftCases;

    @Builder.Default
    private List<WeeklyNfAlert> nfAlerts = new ArrayList<>();

    @Builder.Default
    private List<WeeklyFacilityRestoredRow> facilitiesRestored = new ArrayList<>();

    @Builder.Default
    private List<WeeklyTheftCaseRow> theftCases = new ArrayList<>();

    @Builder.Default
    private List<WeeklyEscalationEffectivenessRow> escalationEffectiveness = new ArrayList<>();

    @Builder.Default
    private List<WeeklyStateNfTrendRow> stateNfTrend = new ArrayList<>();

    @Builder.Default
    private List<WeeklyTrendMetric> overallTrend = new ArrayList<>();

    private WeeklyTrendMetric totalOpenTrend;

    private String vendorLowestTatName;
    private double vendorLowestTatDays;

    private FunctionalMetrics weekStartMetrics;
    private FunctionalMetrics weekEndMetrics;
    private ArrowData functionalArrow;
    private ArrowData nonFunctionalArrow;

    @Override
    public String toString() {
        return "WeeklyEscalationAnalytics{" +
                "weekRangeLabel='" + weekRangeLabel + '\'' +
                ", asOfDate='" + asOfDate + '\'' +
                ", stateListLabel='" + stateListLabel + '\'' +
                ", facilitiesWithOpenTickets=" + facilitiesWithOpenTickets +
                ", nfFacilitiesWithOpenTickets=" + nfFacilitiesWithOpenTickets +
                ", openTheftCases=" + openTheftCases +
                ", vendorLowestTatName='" + vendorLowestTatName + '\'' +
                ", vendorLowestTatDays=" + vendorLowestTatDays +
                ", overviewByStateSize=" + (overviewByState == null ? 0 : overviewByState.size()) +
                ", vendorPerformanceSize=" + (vendorPerformance == null ? 0 : vendorPerformance.size()) +
                ", bottlenecksSize=" + (bottlenecks == null ? 0 : bottlenecks.size()) +
                ", nfAlertsSize=" + (nfAlerts == null ? 0 : nfAlerts.size()) +
                ", facilitiesRestoredSize=" + (facilitiesRestored == null ? 0 : facilitiesRestored.size()) +
                ", theftCasesSize=" + (theftCases == null ? 0 : theftCases.size()) +
                ", escalationEffectivenessSize=" + (escalationEffectiveness == null ? 0 : escalationEffectiveness.size()) +
                ", stateNfTrendSize=" + (stateNfTrend == null ? 0 : stateNfTrend.size()) +
                ", overallTrendSize=" + (overallTrend == null ? 0 : overallTrend.size()) +
                '}';
    }
}
