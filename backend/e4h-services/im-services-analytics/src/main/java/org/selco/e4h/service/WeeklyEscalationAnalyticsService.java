package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.EscalationProperties;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationActorUtil;
import org.selco.e4h.util.EscalationTicketUtil;
import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.web.models.ArrowData;
import org.selco.e4h.web.models.EscalationInfo;
import org.selco.e4h.web.models.EscalationTicket;
import org.selco.e4h.web.models.FunctionalMetrics;
import org.selco.e4h.web.models.WeeklyBottleneckRow;
import org.selco.e4h.web.models.WeeklyEscalationAnalytics;
import org.selco.e4h.web.models.WeeklyEscalationEffectivenessRow;
import org.selco.e4h.web.models.WeeklyFacilityRestoredRow;
import org.selco.e4h.web.models.WeeklyNfAlert;
import org.selco.e4h.web.models.WeeklyReportData;
import org.selco.e4h.web.models.WeeklyStateNfTrendRow;
import org.selco.e4h.web.models.WeeklyTheftCaseRow;
import org.selco.e4h.web.models.WeeklyTicketOverview;
import org.selco.e4h.web.models.WeeklyTrendMetric;
import org.selco.e4h.web.models.WeeklyVendorPerformanceRow;
import org.selco.e4h.web.models.workflow.ProcessInstance;
import org.selco.e4h.util.VendorResponseUtil;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyEscalationAnalyticsService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
    private static final SimpleDateFormat RANGE_FORMAT = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    static {
        TimeZone ist = TimeZone.getTimeZone("Asia/Kolkata");
        DATE_FORMAT.setTimeZone(ist);
        RANGE_FORMAT.setTimeZone(ist);
    }

    private final ElasticSearchClient elasticSearchClient;
    private final WeeklyReportService weeklyReportService;
    private final CommonUtility commonUtility;
    private final EscalationProperties escalationProperties;
    private final WorkflowService workflowService;

    public WeeklyEscalationAnalytics buildAnalytics(Set<String> stateCodes, RequestInfo requestInfo) {
        Date[] reportWeek = getPreviousWeekDates(0);
        Date[] priorWeek = getPreviousWeekDates(1);
        long weekStartMs = reportWeek[0].getTime();
        long weekEndMs = reportWeek[1].getTime();

        String weekRangeLabel = RANGE_FORMAT.format(reportWeek[0]) + " – "
                + RANGE_FORMAT.format(reportWeek[1]) + " " + Calendar.getInstance().get(Calendar.YEAR);
        String stateListLabel = stateCodes.stream()
                .map(commonUtility::getStateDisplayName)
                .collect(Collectors.joining(", "));

        List<EscalationTicket> scopedTickets = loadScopedTickets();

        long priorWeekEndMs = priorWeek[1].getTime();
        WeekAccumulator acc = new WeekAccumulator();
        for (EscalationTicket ticket : scopedTickets) {
            String stateCode = EscalationTicketUtil.resolveStateCode(ticket);
            if (stateCode == null || !isStateInScope(stateCode, stateCodes)) {
                continue;
            }
            accumulateTicket(acc, ticket, stateCode, weekStartMs, weekEndMs, priorWeekEndMs);
        }

        resolveVendorResponseRates(acc, requestInfo);

        TrendCounts priorWeekCounts = computeTrendCounts(scopedTickets, stateCodes,
                priorWeek[0].getTime(), priorWeek[1].getTime());
        TrendCounts thisWeekCounts = new TrendCounts(
                acc.nationalOverview.getNewBreaches(),
                acc.nationalOverview.getResolvedWithinSla(),
                acc.nationalOverview.getResolvedAfterBreach(),
                acc.theftCases.size());

        List<WeeklyVendorPerformanceRow> vendorPerformance = buildVendorPerformance(acc.vendorAgg);
        List<WeeklyBottleneckRow> bottlenecks = buildBottlenecks(acc);
        List<WeeklyFacilityRestoredRow> facilitiesRestored = buildFacilitiesRestored(acc);
        List<WeeklyEscalationEffectivenessRow> effectiveness = buildEffectiveness(acc);
        List<WeeklyTrendMetric> overallTrend = buildOverallTrend(priorWeekCounts, thisWeekCounts);
        WeeklyTrendMetric totalOpenTrend = trendMetric("Total Open Tickets",
                acc.openAsOfPriorWeekEnd, acc.nationalOverview.getTotalOpen());

        FunctionalMetrics startMetrics = aggregateFunctionalMetrics(stateCodes, requestInfo, true);
        FunctionalMetrics endMetrics = aggregateFunctionalMetrics(stateCodes, requestInfo, false);
        ArrowData funcArrow = computeArrow(startMetrics, endMetrics, true);
        ArrowData nonFuncArrow = computeArrow(startMetrics, endMetrics, false);

        List<WeeklyNfAlert> nfAlerts = new ArrayList<>();
        List<WeeklyStateNfTrendRow> stateNfTrend = new ArrayList<>();
        buildPerStateNfData(stateCodes, acc.bottleneckCountsByState, nfAlerts, stateNfTrend);

        return WeeklyEscalationAnalytics.builder()
                .weekRangeLabel(weekRangeLabel)
                .asOfDate(DATE_FORMAT.format(new Date()))
                .stateListLabel(stateListLabel)
                .nationalOverview(acc.nationalOverview)
                .overviewByState(acc.overviewByState)
                .vendorPerformance(vendorPerformance)
                .bottlenecks(bottlenecks)
                .facilitiesWithOpenTickets(acc.facilities.size())
                .nfFacilitiesWithOpenTickets(acc.nfFacilities.size())
                .openTheftCases(acc.openTheftCases)
                .nfAlerts(nfAlerts)
                .facilitiesRestored(facilitiesRestored)
                .theftCases(acc.theftCases)
                .escalationEffectiveness(effectiveness)
                .stateNfTrend(stateNfTrend)
                .overallTrend(overallTrend)
                .totalOpenTrend(totalOpenTrend)
                .vendorLowestTatName(acc.lowestTatVendorName())
                .vendorLowestTatDays(acc.lowestTatVendorDays())
                .weekStartMetrics(startMetrics)
                .weekEndMetrics(endMetrics)
                .functionalArrow(funcArrow)
                .nonFunctionalArrow(nonFuncArrow)
                .build();
    }

    /**
     * Internal accumulator for a single pass over the report week's scoped tickets.
     */
    private static class WeekAccumulator {
        WeeklyTicketOverview nationalOverview = new WeeklyTicketOverview();
        Map<String, WeeklyTicketOverview> overviewByState = new HashMap<>();
        Map<String, VendorAgg> vendorAgg = new LinkedHashMap<>();
        Map<String, Long> bottleneckCounts = new HashMap<>();
        Map<String, Double> bottleneckDaysOpenSum = new HashMap<>();
        Map<String, Map<String, Long>> bottleneckCountsByState = new HashMap<>();
        int totalOpenBreached = 0;
        Set<String> facilities = new HashSet<>();
        Set<String> nfFacilities = new HashSet<>();
        Map<String, Set<String>> restoredFacilitiesByState = new HashMap<>();
        Map<String, Set<String>> restoredVendorNamesByState = new HashMap<>();
        List<WeeklyTheftCaseRow> theftCases = new ArrayList<>();
        int openTheftCases = 0;
        Map<String, int[]> effectivenessByLevel = new LinkedHashMap<>(); // [escalated, resolved]
        Map<String, Double> vendorTatDaysSum = new HashMap<>();
        Map<String, Integer> vendorTatCount = new HashMap<>();
        Map<String, Boolean> vendorFallbackWithinSla = new HashMap<>();
        int openAsOfPriorWeekEnd = 0;

        String lowestTatVendorName() {
            return vendorTatCount.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .min(Comparator.comparingDouble(e -> vendorTatDaysSum.get(e.getKey()) / e.getValue()))
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }

        double lowestTatVendorDays() {
            String vendor = lowestTatVendorName();
            if (vendor == null) {
                return 0;
            }
            return Math.round((vendorTatDaysSum.get(vendor) / vendorTatCount.get(vendor)) * 10.0) / 10.0;
        }
    }

    private static class VendorAgg {
        int newBreachesThisWeek;
        int resolvedWithinSlaCount;
        int resolvedAfterBreachCount;
        int ticketsAssigned;
        int respondedWithinStepSla;
        List<String> incidentIds = new ArrayList<>();
    }

    private void accumulateTicket(WeekAccumulator acc, EscalationTicket ticket, String stateCode,
                                   long weekStartMs, long weekEndMs, long priorWeekEndMs) {
        acc.overviewByState.putIfAbsent(stateCode, new WeeklyTicketOverview());
        WeeklyTicketOverview stateOverview = acc.overviewByState.get(stateCode);

        Long filedDate = EscalationTicketUtil.getFiledDate(ticket);
        Long resolvedTs = EscalationTicketUtil.getResolvedTimestamp(ticket);
        boolean closed = EscalationTicketUtil.isClosed(ticket);

        if (filedDate != null && filedDate <= priorWeekEndMs && (resolvedTs == null || resolvedTs > priorWeekEndMs)) {
            acc.openAsOfPriorWeekEnd++;
        }
        boolean stepBreached = isStepBreached(ticket);
        boolean breached = ticket.getSlaBreachTime() != null || stepBreached;

        if (filedDate != null && filedDate >= weekStartMs && filedDate <= weekEndMs) {
            acc.nationalOverview.setRaisedThisWeek(acc.nationalOverview.getRaisedThisWeek() + 1);
            stateOverview.setRaisedThisWeek(stateOverview.getRaisedThisWeek() + 1);
        }

        if (ticket.getSlaBreachTime() != null && ticket.getSlaBreachTime() >= weekStartMs
                && ticket.getSlaBreachTime() <= weekEndMs) {
            acc.nationalOverview.setNewBreaches(acc.nationalOverview.getNewBreaches() + 1);
            stateOverview.setNewBreaches(stateOverview.getNewBreaches() + 1);
        }

        boolean resolvedThisWeek = resolvedTs != null && resolvedTs >= weekStartMs && resolvedTs <= weekEndMs;
        boolean withinSla = false;
        if (resolvedThisWeek) {
            Object totalSlaRemaining = ticket.getAdditionalDetails() != null
                    ? ticket.getAdditionalDetails().get("totalSlaRemaining") : null;
            withinSla = totalSlaRemaining instanceof Number number && number.doubleValue() >= 0;
            if (withinSla) {
                acc.nationalOverview.setResolvedWithinSla(acc.nationalOverview.getResolvedWithinSla() + 1);
                stateOverview.setResolvedWithinSla(stateOverview.getResolvedWithinSla() + 1);
            } else {
                acc.nationalOverview.setResolvedAfterBreach(acc.nationalOverview.getResolvedAfterBreach() + 1);
                stateOverview.setResolvedAfterBreach(stateOverview.getResolvedAfterBreach() + 1);
            }
        }

        if (!closed) {
            acc.nationalOverview.setTotalOpen(acc.nationalOverview.getTotalOpen() + 1);
            stateOverview.setTotalOpen(stateOverview.getTotalOpen() + 1);
            if (filedDate == null || filedDate < weekStartMs) {
                acc.nationalOverview.setCarriedForward(acc.nationalOverview.getCarriedForward() + 1);
                stateOverview.setCarriedForward(stateOverview.getCarriedForward() + 1);
            }

            if (breached) {
                String statusLabel = EscalationActorUtil.resolveStatusLabel(ticket.getApplicationStatus());
                acc.bottleneckCounts.merge(statusLabel, 1L, Long::sum);
                acc.totalOpenBreached++;
                if (filedDate != null) {
                    double daysOpen = (System.currentTimeMillis() - filedDate) / (double) DAY_MS;
                    acc.bottleneckDaysOpenSum.merge(statusLabel, daysOpen, Double::sum);
                }
                acc.bottleneckCountsByState.computeIfAbsent(stateCode, k -> new HashMap<>())
                        .merge(statusLabel, 1L, Long::sum);
            }
        }

        if (EscalationTicketUtil.isTheft(ticket)) {
            if (!closed) {
                acc.openTheftCases++;
            }
            if (filedDate != null && filedDate >= weekStartMs && filedDate <= weekEndMs) {
                acc.theftCases.add(WeeklyTheftCaseRow.builder()
                        .stateName(commonUtility.getStateDisplayName(stateCode))
                        .healthFacilityName(ticket.getHealthFacilityName())
                        .district(ticket.getDistrict())
                        .filedDateFormatted(DATE_FORMAT.format(new Date(filedDate)))
                        .statusLabel(EscalationActorUtil.resolveStatusLabel(ticket.getApplicationStatus()))
                        .build());
            }
        }

        String facilityKey = buildFacilityKey(ticket, stateCode);
        boolean currentlyFunctional = !EscalationTicketUtil.isNonFunctional(ticket);
        if (!closed) {
            acc.facilities.add(facilityKey);
            if (EscalationTicketUtil.isNonFunctional(ticket)) {
                acc.nfFacilities.add(facilityKey);
            }
        }
        if (resolvedThisWeek && currentlyFunctional) {
            acc.restoredFacilitiesByState.computeIfAbsent(stateCode, k -> new HashSet<>()).add(facilityKey);
            String vendorForFacility = ticket.getMappedVendor();
            if (vendorForFacility != null && !vendorForFacility.isBlank()) {
                acc.restoredVendorNamesByState.computeIfAbsent(stateCode, k -> new HashSet<>()).add(vendorForFacility);
            }
        }

        if (EscalationActorUtil.classifyByWorkflowState(ticket.getApplicationStatus())
                == EscalationActorUtil.ActorBucket.VENDOR) {
            String vendor = ticket.getMappedVendor() != null ? ticket.getMappedVendor() : "Unknown Vendor";
            VendorAgg vendorAgg = acc.vendorAgg.computeIfAbsent(vendor, k -> new VendorAgg());
            vendorAgg.ticketsAssigned++;
            if (ticket.getIncidentId() != null) {
                vendorAgg.incidentIds.add(ticket.getIncidentId());
                // Fallback used only if real workflow history is unavailable for this ticket.
                acc.vendorFallbackWithinSla.put(ticket.getIncidentId(), !stepBreached);
            }
            if (ticket.getSlaBreachTime() != null && ticket.getSlaBreachTime() >= weekStartMs
                    && ticket.getSlaBreachTime() <= weekEndMs) {
                vendorAgg.newBreachesThisWeek++;
            }
            if (resolvedThisWeek) {
                if (withinSla) {
                    vendorAgg.resolvedWithinSlaCount++;
                } else {
                    vendorAgg.resolvedAfterBreachCount++;
                }
                if (filedDate != null) {
                    double tatDays = (resolvedTs - filedDate) / (double) DAY_MS;
                    acc.vendorTatDaysSum.merge(vendor, tatDays, Double::sum);
                    acc.vendorTatCount.merge(vendor, 1, Integer::sum);
                }
            }
        }

        if (ticket.getEscalationInfo() != null) {
            for (EscalationInfo info : ticket.getEscalationInfo()) {
                if (info.getEscalationTime() == null || info.getEscalationTime() < weekStartMs
                        || info.getEscalationTime() > weekEndMs) {
                    continue;
                }
                if ("LEVEL_TWO".equals(info.getEscalationLevel()) || "LEVEL_THREE".equals(info.getEscalationLevel())) {
                    int[] counts = acc.effectivenessByLevel.computeIfAbsent(info.getEscalationLevel(), k -> new int[2]);
                    counts[0]++;
                    if (closed && resolvedTs != null && resolvedTs > info.getEscalationTime()) {
                        counts[1]++;
                    }
                }
            }
        }
    }

    /**
     * Computes each vendor's real Response Rate — % of assigned tickets where the vendor's first
     * action after entering a vendor-owned workflow state landed within that step's SLA — using
     * egov-workflow-v2's process-instance history (batched, one HTTP call per ~100 tickets).
     * Falls back to the current-step-SLA proxy per ticket when history is unavailable (e.g. the
     * workflow service call fails, or a ticket has no vendor-state entry in its history).
     */
    private void resolveVendorResponseRates(WeekAccumulator acc, RequestInfo requestInfo) {
        List<String> allIncidentIds = acc.vendorAgg.values().stream()
                .flatMap(v -> v.incidentIds.stream())
                .distinct()
                .collect(Collectors.toList());
        if (allIncidentIds.isEmpty()) {
            return;
        }

        List<ProcessInstance> history = workflowService.getProcessInstancesByIncidentIds(
                "in", allIncidentIds, requestInfo);
        Map<String, List<ProcessInstance>> historyByBusinessId = history.stream()
                .filter(pi -> pi.getBusinessId() != null)
                .collect(Collectors.groupingBy(ProcessInstance::getBusinessId));

        for (VendorAgg vendorAgg : acc.vendorAgg.values()) {
            for (String incidentId : vendorAgg.incidentIds) {
                Boolean respondedWithinSla = VendorResponseUtil.respondedWithinSla(historyByBusinessId.get(incidentId));
                boolean result = respondedWithinSla != null
                        ? respondedWithinSla
                        : acc.vendorFallbackWithinSla.getOrDefault(incidentId, false);
                if (result) {
                    vendorAgg.respondedWithinStepSla++;
                }
            }
        }
    }

    /**
     * Current-step SLA breach check (as opposed to overall-ticket breach) — used for the
     * national/state bottleneck breach flag, and as the fallback basis for vendor Response Rate
     * when real workflow history isn't available (see resolveVendorResponseRates).
     */
    private boolean isStepBreached(EscalationTicket ticket) {
        return ticket.getAdditionalDetails() != null
                && ticket.getAdditionalDetails().get("slaRemaining") instanceof Number number
                && number.doubleValue() < 0;
    }

    private record TrendCounts(int breaches, int resolvedWithinSla, int resolvedAfterBreach, int theftCases) {
    }

    private TrendCounts computeTrendCounts(List<EscalationTicket> scopedTickets, Set<String> stateCodes,
                                            long weekStartMs, long weekEndMs) {
        int breaches = 0;
        int resolvedWithinSla = 0;
        int resolvedAfterBreach = 0;
        int theftCases = 0;

        for (EscalationTicket ticket : scopedTickets) {
            String stateCode = EscalationTicketUtil.resolveStateCode(ticket);
            if (stateCode == null || !isStateInScope(stateCode, stateCodes)) {
                continue;
            }

            if (ticket.getSlaBreachTime() != null && ticket.getSlaBreachTime() >= weekStartMs
                    && ticket.getSlaBreachTime() <= weekEndMs) {
                breaches++;
            }

            Long resolvedTs = EscalationTicketUtil.getResolvedTimestamp(ticket);
            if (resolvedTs != null && resolvedTs >= weekStartMs && resolvedTs <= weekEndMs) {
                Object totalSlaRemaining = ticket.getAdditionalDetails() != null
                        ? ticket.getAdditionalDetails().get("totalSlaRemaining") : null;
                boolean withinSla = totalSlaRemaining instanceof Number number && number.doubleValue() >= 0;
                if (withinSla) {
                    resolvedWithinSla++;
                } else {
                    resolvedAfterBreach++;
                }
            }

            Long filedDate = EscalationTicketUtil.getFiledDate(ticket);
            if (EscalationTicketUtil.isTheft(ticket) && filedDate != null
                    && filedDate >= weekStartMs && filedDate <= weekEndMs) {
                theftCases++;
            }
        }

        return new TrendCounts(breaches, resolvedWithinSla, resolvedAfterBreach, theftCases);
    }

    private List<WeeklyTrendMetric> buildOverallTrend(TrendCounts lastWeek, TrendCounts thisWeek) {
        List<WeeklyTrendMetric> rows = new ArrayList<>();
        rows.add(trendMetric("Total SLA Breaches", lastWeek.breaches(), thisWeek.breaches()));
        rows.add(trendMetric("Tickets Resolved (within SLA)", lastWeek.resolvedWithinSla(), thisWeek.resolvedWithinSla()));
        rows.add(trendMetric("Tickets Resolved (breached SLA)", lastWeek.resolvedAfterBreach(), thisWeek.resolvedAfterBreach()));
        rows.add(trendMetric("New Theft Cases", lastWeek.theftCases(), thisWeek.theftCases()));
        return rows;
    }

    private WeeklyTrendMetric trendMetric(String label, long lastWeekValue, long thisWeekValue) {
        double changePct = lastWeekValue > 0
                ? Math.round(((thisWeekValue - lastWeekValue) * 100.0 / lastWeekValue) * 10.0) / 10.0
                : (thisWeekValue > 0 ? 100.0 : 0.0);
        String arrow = thisWeekValue > lastWeekValue ? "▲" : (thisWeekValue < lastWeekValue ? "▼" : "");
        return WeeklyTrendMetric.builder()
                .label(label)
                .lastWeekValue(lastWeekValue)
                .thisWeekValue(thisWeekValue)
                .changePct(changePct)
                .arrow(arrow)
                .build();
    }

    private List<WeeklyVendorPerformanceRow> buildVendorPerformance(Map<String, VendorAgg> vendorAgg) {
        List<WeeklyVendorPerformanceRow> rows = new ArrayList<>();
        for (Map.Entry<String, VendorAgg> entry : vendorAgg.entrySet()) {
            VendorAgg v = entry.getValue();
            int resolvedTotal = v.resolvedWithinSlaCount + v.resolvedAfterBreachCount;
            double resolutionRate = v.ticketsAssigned > 0 ? (resolvedTotal * 100.0 / v.ticketsAssigned) : 0;
            double responseRate = v.ticketsAssigned > 0 ? (v.respondedWithinStepSla * 100.0 / v.ticketsAssigned) : 0;
            rows.add(WeeklyVendorPerformanceRow.builder()
                    .vendorName(entry.getKey())
                    .newBreachesThisWeek(v.newBreachesThisWeek)
                    .ticketsAssigned(v.ticketsAssigned)
                    .resolvedWithinSlaCount(v.resolvedWithinSlaCount)
                    .resolvedAfterBreachCount(v.resolvedAfterBreachCount)
                    .responseRatePct(Math.round(responseRate * 10.0) / 10.0)
                    .resolutionRatePct(Math.round(resolutionRate * 10.0) / 10.0)
                    .build());
        }
        rows.sort(Comparator.comparingDouble(WeeklyVendorPerformanceRow::getResolutionRatePct));
        return rows;
    }

    private List<WeeklyBottleneckRow> buildBottlenecks(WeekAccumulator acc) {
        return acc.bottleneckCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    double pct = acc.totalOpenBreached > 0 ? (e.getValue() * 100.0 / acc.totalOpenBreached) : 0;
                    double avgDays = acc.bottleneckDaysOpenSum.getOrDefault(e.getKey(), 0.0) / e.getValue();
                    return WeeklyBottleneckRow.builder()
                            .statusLabel(e.getKey())
                            .count(e.getValue())
                            .pctOfTotalOpen(Math.round(pct * 10.0) / 10.0)
                            .avgDaysOpen(Math.round(avgDays * 10.0) / 10.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<WeeklyFacilityRestoredRow> buildFacilitiesRestored(WeekAccumulator acc) {
        List<WeeklyFacilityRestoredRow> rows = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : acc.restoredFacilitiesByState.entrySet()) {
            Set<String> vendors = acc.restoredVendorNamesByState.getOrDefault(entry.getKey(), Set.of());
            rows.add(WeeklyFacilityRestoredRow.builder()
                    .stateName(commonUtility.getStateDisplayName(entry.getKey()))
                    .restoredCount(entry.getValue().size())
                    .vendorNames(String.join(", ", vendors))
                    .build());
        }
        rows.sort(Comparator.comparing(WeeklyFacilityRestoredRow::getStateName));
        return rows;
    }

    private List<WeeklyEscalationEffectivenessRow> buildEffectiveness(WeekAccumulator acc) {
        List<WeeklyEscalationEffectivenessRow> rows = new ArrayList<>();
        for (String level : List.of("LEVEL_TWO", "LEVEL_THREE")) {
            int[] counts = acc.effectivenessByLevel.getOrDefault(level, new int[2]);
            double rate = counts[0] > 0 ? (counts[1] * 100.0 / counts[0]) : 0;
            rows.add(WeeklyEscalationEffectivenessRow.builder()
                    .levelLabel("LEVEL_TWO".equals(level) ? "Level 2 — SPM" : "Level 3 — Procurement")
                    .escalatedCount(counts[0])
                    .resolvedCount(counts[1])
                    .resolutionRatePct(Math.round(rate * 10.0) / 10.0)
                    .build());
        }
        return rows;
    }

    private void buildPerStateNfData(Set<String> stateCodes,
                                      Map<String, Map<String, Long>> bottleneckCountsByState,
                                      List<WeeklyNfAlert> nfAlerts,
                                      List<WeeklyStateNfTrendRow> stateNfTrend) {
        double threshold = escalationProperties.getLeadership().getNfThresholdPct();

        for (String stateCode : stateCodes) {
            try {
                WeeklyReportData report = weeklyReportService.generateWeeklyReportData(stateCode, null);
                if (report.getWeekStartMetrics() == null || report.getWeekEndMetrics() == null) {
                    continue;
                }
                int startNf = report.getWeekStartMetrics().getNonFunctionalCount();
                int endFunc = report.getWeekEndMetrics().getFunctionalCount();
                int endNf = report.getWeekEndMetrics().getNonFunctionalCount();
                int endTotal = endFunc + endNf;
                double nfPct = endTotal > 0 ? endNf * 100.0 / endTotal : 0;
                String stateName = commonUtility.getStateDisplayName(stateCode);

                stateNfTrend.add(WeeklyStateNfTrendRow.builder()
                        .stateName(stateName)
                        .totalNfLastWeek(startNf)
                        .totalNfThisWeek(endNf)
                        .nfPctThisWeek(Math.round(nfPct * 10.0) / 10.0)
                        .build());

                if (nfPct > threshold) {
                    Map<String, Long> stateBottlenecks = bottleneckCountsByState.getOrDefault(stateCode, Map.of());
                    String bottleneck = stateBottlenecks.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("Pending Resolution");
                    nfAlerts.add(WeeklyNfAlert.builder()
                            .stateName(stateName)
                            .nfPct(Math.round(nfPct * 10.0) / 10.0)
                            .primaryBottleneck(bottleneck)
                            .build());
                }
            } catch (Exception e) {
                log.warn("Could not compute NF data for state {}", stateCode, e);
            }
        }

        nfAlerts.sort(Comparator.comparingDouble(WeeklyNfAlert::getNfPct).reversed());
    }

    private List<EscalationTicket> loadScopedTickets() {
        Map<String, Object> finalQuery = new HashMap<>();
        finalQuery.put("query", Map.of("match_all", Map.of()));
        finalQuery.put("size", 10000);
        finalQuery.put("track_total_hits", true);
        return elasticSearchClient.searchTickets(finalQuery);
    }

    private FunctionalMetrics aggregateFunctionalMetrics(Set<String> stateCodes, RequestInfo requestInfo, boolean start) {
        int func = 0;
        int nonFunc = 0;
        for (String stateCode : stateCodes) {
            try {
                WeeklyReportData report = weeklyReportService.generateWeeklyReportData(stateCode, requestInfo);
                FunctionalMetrics metrics = start ? report.getWeekStartMetrics() : report.getWeekEndMetrics();
                if (metrics != null) {
                    func += metrics.getFunctionalCount();
                    nonFunc += metrics.getNonFunctionalCount();
                }
            } catch (Exception e) {
                log.warn("Could not load functional metrics for state {}", stateCode, e);
            }
        }
        return FunctionalMetrics.builder().functionalCount(func).nonFunctionalCount(nonFunc).build();
    }

    private ArrowData computeArrow(FunctionalMetrics start, FunctionalMetrics end, boolean functional) {
        int startTotal = start.getFunctionalCount() + start.getNonFunctionalCount();
        int endTotal = end.getFunctionalCount() + end.getNonFunctionalCount();
        double startPct = startTotal > 0
                ? (functional ? start.getFunctionalCount() : start.getNonFunctionalCount()) * 100.0 / startTotal : 0;
        double endPct = endTotal > 0
                ? (functional ? end.getFunctionalCount() : end.getNonFunctionalCount()) * 100.0 / endTotal : 0;
        return commonUtility.calculateArrow(startPct, endPct, functional);
    }

    private String buildFacilityKey(EscalationTicket ticket, String stateCode) {
        return stateCode + "|" + ticket.getDistrict() + "|" + ticket.getBlock() + "|" + ticket.getHealthFacilityName();
    }

    private boolean isStateInScope(String ticketStateCode, Set<String> targetStateCodes) {
        for (String target : targetStateCodes) {
            if (ticketStateCode.equalsIgnoreCase(target)
                    || ticketStateCode.startsWith(target + ".")
                    || target.startsWith(ticketStateCode + ".")) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param weeksAgo 0 = the most recently completed Mon–Sun week ("report week" / "This Week"
     *                 in the Overall Trend table), 1 = the week before that ("Last Week").
     */
    private Date[] getPreviousWeekDates(int weeksAgo) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_MONTH, -daysToSubtract - 7 - (7 * weeksAgo));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date weekStart = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return new Date[]{weekStart, cal.getTime()};
    }
}
