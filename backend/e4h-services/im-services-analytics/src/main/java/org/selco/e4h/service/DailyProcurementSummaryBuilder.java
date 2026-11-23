package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationActorUtil;
import org.selco.e4h.util.EscalationTicketUtil;
import org.selco.e4h.web.models.ActorCountRow;
import org.selco.e4h.web.models.DailyProcurementSummary;
import org.selco.e4h.web.models.EscalationRoleEscalationItem;
import org.selco.e4h.web.models.EscalationTicket;
import org.selco.e4h.web.models.StateDailyBreachSection;
import org.selco.e4h.web.models.VendorStateCountRow;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyProcurementSummaryBuilder {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    private final SLABreachDetectionService slaBreachDetectionService;
    private final CommonUtility commonUtility;

    public DailyProcurementSummary buildSummary(String recipientName,
                                                EscalationRoleEscalationItem escalationItem,
                                                String escalationRecipientId,
                                                int triggerDelayHours,
                                                RequestInfo requestInfo) {
        List<String> workflowStates = escalationItem.getWorkflowStates();
        String escalationLevel = escalationItem.getEscalationLevel();

        List<EscalationTicket> newBreaches = slaBreachDetectionService.findProcurementEligibleTickets(
                workflowStates, escalationRecipientId, escalationLevel, triggerDelayHours, requestInfo);
        List<EscalationTicket> previouslyOpen = slaBreachDetectionService.findProcurementPreviouslyOpenTickets(
                workflowStates, escalationRecipientId, escalationLevel, triggerDelayHours, requestInfo);

        return DailyProcurementSummary.builder()
                .recipientName(recipientName)
                .asOfDate(DATE_FORMAT.format(new Date()))
                .dashboardUrl(commonUtility.generateStateDashboardUrl())
                .newBreachesByState(groupByState(newBreaches))
                .previouslyOpen(capTopVendorsPerState(groupVendorRows(previouslyOpen), 2))
                .build();
    }

    /**
     * Section 1 per the spec: a "▸ StateName" banner + a 2-column (Vendor, Count) table for
     * each state — not a single flat table with State as a column.
     */
    private List<StateDailyBreachSection> groupByState(List<EscalationTicket> tickets) {
        Map<String, List<EscalationTicket>> byState = new LinkedHashMap<>();
        for (EscalationTicket ticket : tickets) {
            if (EscalationActorUtil.classifyByWorkflowState(ticket.getApplicationStatus())
                    != EscalationActorUtil.ActorBucket.VENDOR) {
                continue;
            }
            String stateCode = EscalationTicketUtil.resolveStateCode(ticket);
            String stateName = commonUtility.getStateDisplayName(stateCode != null ? stateCode : "Unknown");
            byState.computeIfAbsent(stateName, k -> new ArrayList<>()).add(ticket);
        }

        List<StateDailyBreachSection> sections = new ArrayList<>();
        byState.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sections.add(StateDailyBreachSection.builder()
                        .stateName(entry.getKey())
                        .breaches(groupVendorActorRows(entry.getValue()))
                        .build()));
        return sections;
    }

    private List<ActorCountRow> groupVendorActorRows(List<EscalationTicket> tickets) {
        Map<String, ActorCountRow> grouped = new LinkedHashMap<>();
        for (EscalationTicket ticket : tickets) {
            String vendorName = EscalationActorUtil.resolveActorName(ticket);
            String status = EscalationActorUtil.resolveStatusLabel(ticket.getApplicationStatus());
            String key = vendorName + "|" + status;

            ActorCountRow row = grouped.computeIfAbsent(key, k -> ActorCountRow.builder()
                    .actorName(vendorName)
                    .currentStatus(status)
                    .count(0)
                    .build());
            row.setCount(row.getCount() + 1);
        }
        return grouped.values().stream()
                .sorted(Comparator.comparing(ActorCountRow::getActorName))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Section 2 note in the spec: "Top 2 vendors may be shown for each state" — keep only the
     * highest-count rows per state, ranked by count desc, ties broken by vendor name.
     */
    private List<VendorStateCountRow> capTopVendorsPerState(List<VendorStateCountRow> rows, int maxPerState) {
        Map<String, List<VendorStateCountRow>> byState = new LinkedHashMap<>();
        for (VendorStateCountRow row : rows) {
            byState.computeIfAbsent(row.getStateName(), k -> new ArrayList<>()).add(row);
        }

        List<VendorStateCountRow> result = new ArrayList<>();
        for (List<VendorStateCountRow> stateRows : byState.values()) {
            stateRows.sort(Comparator.comparingLong(VendorStateCountRow::getCount).reversed()
                    .thenComparing(VendorStateCountRow::getVendorName));
            result.addAll(stateRows.stream().limit(maxPerState).collect(Collectors.toList()));
        }
        return result;
    }

    private List<VendorStateCountRow> groupVendorRows(List<EscalationTicket> tickets) {
        Map<String, VendorStateCountRow> grouped = new LinkedHashMap<>();

        for (EscalationTicket ticket : tickets) {
            if (EscalationActorUtil.classifyByWorkflowState(ticket.getApplicationStatus())
                    != EscalationActorUtil.ActorBucket.VENDOR) {
                continue;
            }

            String stateCode = EscalationTicketUtil.resolveStateCode(ticket);
            String stateName = commonUtility.getStateDisplayName(stateCode != null ? stateCode : "Unknown");
            String vendorName = EscalationActorUtil.resolveActorName(ticket);
            String status = EscalationActorUtil.resolveStatusLabel(ticket.getApplicationStatus());
            String key = stateName + "|" + vendorName + "|" + status;

            VendorStateCountRow row = grouped.computeIfAbsent(key, k -> VendorStateCountRow.builder()
                    .stateName(stateName)
                    .vendorName(vendorName)
                    .currentStatus(status)
                    .count(0)
                    .build());
            row.setCount(row.getCount() + 1);
        }

        return grouped.values().stream()
                .sorted(Comparator.comparing(VendorStateCountRow::getStateName)
                        .thenComparing(VendorStateCountRow::getVendorName)
                        .thenComparing(VendorStateCountRow::getCurrentStatus))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
