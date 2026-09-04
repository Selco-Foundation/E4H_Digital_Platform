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
import org.selco.e4h.web.models.WeeklyNfAlert;
import org.selco.e4h.web.models.WeeklyReportData;
import org.selco.e4h.web.models.WeeklyTicketOverview;
import org.selco.e4h.web.models.WeeklyVendorPerformanceRow;
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

    static {
        TimeZone ist = TimeZone.getTimeZone("Asia/Kolkata");
        DATE_FORMAT.setTimeZone(ist);
        RANGE_FORMAT.setTimeZone(ist);
    }

    private final ElasticSearchClient elasticSearchClient;
    private final WeeklyReportService weeklyReportService;
    private final CommonUtility commonUtility;
    private final EscalationProperties escalationProperties;

    public WeeklyEscalationAnalytics buildAnalytics(Set<String> stateCodes, RequestInfo requestInfo) {
        Date[] weekDates = getPreviousWeekDates();
        long weekStartMs = weekDates[0].getTime();
        long weekEndMs = weekDates[1].getTime();

        String weekRangeLabel = RANGE_FORMAT.format(weekDates[0]) + " – "
                + RANGE_FORMAT.format(weekDates[1]) + " " + Calendar.getInstance().get(Calendar.YEAR);
        String stateListLabel = stateCodes.stream()
                .map(commonUtility::getStateDisplayName)
                .collect(Collectors.joining(", "));

        List<EscalationTicket> scopedTickets = loadScopedTickets(stateCodes);

        WeeklyTicketOverview nationalOverview = new WeeklyTicketOverview();
        Map<String, WeeklyTicketOverview> overviewByState = new HashMap<>();
        Map<String, Map<String, Integer>> vendorAssigned = new HashMap<>();
        Map<String, Map<String, Integer>> vendorResolved = new HashMap<>();
        Map<String, Long> bottleneckCounts = new HashMap<>();
        Set<String> facilities = new HashSet<>();
        Set<String> nfFacilities = new HashSet<>();

        int escalatedLastWeek = 0;
        int resolvedSinceEscalation = 0;
        int openTheftCases = 0;
        int newTheftThisWeek = 0;

        for (EscalationTicket ticket : scopedTickets) {
            String stateCode = EscalationTicketUtil.resolveStateCode(ticket);
            if (stateCode == null || !isStateInScope(stateCode, stateCodes)) {
                continue;
            }

            overviewByState.putIfAbsent(stateCode, new WeeklyTicketOverview());
            WeeklyTicketOverview stateOverview = overviewByState.get(stateCode);

            Long filedDate = EscalationTicketUtil.getFiledDate(ticket);
            Long resolvedTs = EscalationTicketUtil.getResolvedTimestamp(ticket);
            boolean closed = EscalationTicketUtil.isClosed(ticket);
            boolean breached = ticket.getSlaBreachTime() != null
                    || (ticket.getAdditionalDetails() != null
                    && ticket.getAdditionalDetails().get("slaRemaining") instanceof Number number
                    && number.doubleValue() < 0);

            if (filedDate != null && filedDate >= weekStartMs && filedDate <= weekEndMs) {
                nationalOverview.setRaisedThisWeek(nationalOverview.getRaisedThisWeek() + 1);
                stateOverview.setRaisedThisWeek(stateOverview.getRaisedThisWeek() + 1);
            }

            if (resolvedTs != null && resolvedTs >= weekStartMs && resolvedTs <= weekEndMs) {
                Object totalSlaRemaining = ticket.getAdditionalDetails() != null
                        ? ticket.getAdditionalDetails().get("totalSlaRemaining") : null;
                boolean withinSla = totalSlaRemaining instanceof Number number && number.doubleValue() >= 0;
                if (withinSla) {
                    nationalOverview.setResolvedWithinSla(nationalOverview.getResolvedWithinSla() + 1);
                    stateOverview.setResolvedWithinSla(stateOverview.getResolvedWithinSla() + 1);
                } else {
                    nationalOverview.setResolvedAfterBreach(nationalOverview.getResolvedAfterBreach() + 1);
                    stateOverview.setResolvedAfterBreach(stateOverview.getResolvedAfterBreach() + 1);
                }
            }

            if (!closed) {
                nationalOverview.setCarriedForward(nationalOverview.getCarriedForward() + 1);
                stateOverview.setCarriedForward(stateOverview.getCarriedForward() + 1);

                if (breached) {
                    String statusLabel = EscalationActorUtil.resolveStatusLabel(ticket.getApplicationStatus());
                    bottleneckCounts.merge(statusLabel, 1L, Long::sum);
                }
            }

            if (EscalationTicketUtil.isTheft(ticket)) {
                if (!closed) {
                    openTheftCases++;
                }
                if (filedDate != null && filedDate >= weekStartMs && filedDate <= weekEndMs) {
                    newTheftThisWeek++;
                }
            }

            String facilityKey = buildFacilityKey(ticket, stateCode);
            if (!closed) {
                facilities.add(facilityKey);
                if (EscalationTicketUtil.isNonFunctional(ticket)) {
                    nfFacilities.add(facilityKey);
                }
            }

            String vendor = ticket.getMappedVendor() != null ? ticket.getMappedVendor() : "Unknown Vendor";
            if (EscalationActorUtil.classifyByWorkflowState(ticket.getApplicationStatus())
                    == EscalationActorUtil.ActorBucket.VENDOR) {
                vendorAssigned.computeIfAbsent(vendor, k -> new HashMap<>())
                        .merge(stateCode, 1, Integer::sum);
                if (resolvedTs != null && resolvedTs >= weekStartMs && resolvedTs <= weekEndMs) {
                    vendorResolved.computeIfAbsent(vendor, k -> new HashMap<>())
                            .merge(stateCode, 1, Integer::sum);
                }
            }

            if (ticket.getEscalationInfo() != null) {
                for (EscalationInfo info : ticket.getEscalationInfo()) {
                    if (info.getEscalationTime() != null
                            && info.getEscalationTime() >= weekStartMs
                            && info.getEscalationTime() <= weekEndMs
                            && ("LEVEL_TWO".equals(info.getEscalationLevel())
                            || "LEVEL_THREE".equals(info.getEscalationLevel()))) {
                        escalatedLastWeek++;
                        if (closed && resolvedTs != null && resolvedTs > info.getEscalationTime()) {
                            resolvedSinceEscalation++;
                        }
                    }
                }
            }
        }

        List<WeeklyVendorPerformanceRow> vendorPerformance = buildVendorPerformance(vendorAssigned, vendorResolved);
        List<WeeklyBottleneckRow> bottlenecks = bottleneckCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> WeeklyBottleneckRow.builder().statusLabel(e.getKey()).count(e.getValue()).build())
                .collect(Collectors.toList());

        FunctionalMetrics startMetrics = aggregateFunctionalMetrics(stateCodes, requestInfo, true);
        FunctionalMetrics endMetrics = aggregateFunctionalMetrics(stateCodes, requestInfo, false);
        ArrowData funcArrow = computeArrow(startMetrics, endMetrics, true);
        ArrowData nonFuncArrow = computeArrow(startMetrics, endMetrics, false);

        List<WeeklyNfAlert> nfAlerts = buildNfAlerts(stateCodes, overviewByState, bottleneckCounts);

        return WeeklyEscalationAnalytics.builder()
                .weekRangeLabel(weekRangeLabel)
                .asOfDate(DATE_FORMAT.format(new Date()))
                .stateListLabel(stateListLabel)
                .nationalOverview(nationalOverview)
                .overviewByState(overviewByState)
                .vendorPerformance(vendorPerformance)
                .bottlenecks(bottlenecks)
                .facilitiesWithOpenTickets(facilities.size())
                .nfFacilitiesWithOpenTickets(nfFacilities.size())
                .escalatedLastWeek(escalatedLastWeek)
                .resolvedSinceEscalation(resolvedSinceEscalation)
                .openTheftCases(openTheftCases)
                .newTheftThisWeek(newTheftThisWeek)
                .nfAlerts(nfAlerts)
                .weekStartMetrics(startMetrics)
                .weekEndMetrics(endMetrics)
                .functionalArrow(funcArrow)
                .nonFunctionalArrow(nonFuncArrow)
                .build();
    }

    private List<EscalationTicket> loadScopedTickets(Set<String> stateCodes) {
        Map<String, Object> finalQuery = new HashMap<>();
        finalQuery.put("query", Map.of("match_all", Map.of()));
        finalQuery.put("size", 10000);
        finalQuery.put("track_total_hits", true);
        return elasticSearchClient.searchTickets(finalQuery);
    }

    private List<WeeklyVendorPerformanceRow> buildVendorPerformance(Map<String, Map<String, Integer>> assigned,
                                                                    Map<String, Map<String, Integer>> resolved) {
        List<WeeklyVendorPerformanceRow> rows = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : assigned.entrySet()) {
            int assignedCount = entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
            int resolvedCount = resolved.getOrDefault(entry.getKey(), Map.of()).values().stream()
                    .mapToInt(Integer::intValue).sum();
            double rate = assignedCount > 0 ? (resolvedCount * 100.0 / assignedCount) : 0;
            rows.add(WeeklyVendorPerformanceRow.builder()
                    .vendorName(entry.getKey())
                    .ticketsAssigned(assignedCount)
                    .resolvedCount(resolvedCount)
                    .resolutionRatePct(Math.round(rate * 10.0) / 10.0)
                    .build());
        }
        rows.sort(Comparator.comparingInt(WeeklyVendorPerformanceRow::getTicketsAssigned).reversed());
        return rows.stream().limit(10).collect(Collectors.toList());
    }

    private List<WeeklyNfAlert> buildNfAlerts(Set<String> stateCodes,
                                               Map<String, WeeklyTicketOverview> overviewByState,
                                               Map<String, Long> nationalBottlenecks) {
        double threshold = escalationProperties.getLeadership().getNfThresholdPct();
        List<WeeklyNfAlert> alerts = new ArrayList<>();

        for (String stateCode : stateCodes) {
            try {
                WeeklyReportData report = weeklyReportService.generateWeeklyReportData(stateCode, null);
                if (report.getWeekEndMetrics() == null) {
                    continue;
                }
                int func = report.getWeekEndMetrics().getFunctionalCount();
                int nonFunc = report.getWeekEndMetrics().getNonFunctionalCount();
                int total = func + nonFunc;
                if (total == 0) {
                    continue;
                }
                double nfPct = nonFunc * 100.0 / total;
                if (nfPct > threshold) {
                    String bottleneck = nationalBottlenecks.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("Pending Resolution");
                    alerts.add(WeeklyNfAlert.builder()
                            .stateName(commonUtility.getStateDisplayName(stateCode))
                            .nfPct(Math.round(nfPct * 10.0) / 10.0)
                            .primaryBottleneck(bottleneck)
                            .build());
                }
            } catch (Exception e) {
                log.warn("Could not compute NF alert for state {}", stateCode, e);
            }
        }
        return alerts;
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

    private Date[] getPreviousWeekDates() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_MONTH, -daysToSubtract - 7);
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
