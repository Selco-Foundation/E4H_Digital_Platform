package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationActorUtil;
import org.selco.e4h.web.models.ActorCountRow;
import org.selco.e4h.web.models.DailySeniorProgramManagerSummary;
import org.selco.e4h.web.models.EscalationTicket;
import org.selco.e4h.web.models.StateDailyBreachSection;
import org.selco.e4h.web.models.StatePreviouslyOpenRow;
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

/**
 * Builds the SPM daily summary for a single recipient across ALL states they own (per-state
 * sub-sections in one email), rather than one email per state.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailySeniorProgramManagerSummaryBuilder {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    private final CommonUtility commonUtility;

    public DailySeniorProgramManagerSummary buildSummary(List<String> stateCodesInOrder,
                                                          Map<String, List<EscalationTicket>> newBreachesByState,
                                                          Map<String, List<EscalationTicket>> previouslyOpenByState,
                                                          String recipientName) {
        List<StateDailyBreachSection> stateSections = new ArrayList<>();
        List<StatePreviouslyOpenRow> previouslyOpenRows = new ArrayList<>();

        for (String stateCode : stateCodesInOrder) {
            String stateName = commonUtility.getStateDisplayName(stateCode);

            List<EscalationTicket> newBreaches = newBreachesByState.getOrDefault(stateCode, List.of());
            List<ActorCountRow> statePocRows = groupActorRows(newBreaches, EscalationActorUtil.ActorBucket.STATE_SPOC);
            List<ActorCountRow> vendorRows = groupActorRows(newBreaches, EscalationActorUtil.ActorBucket.VENDOR);
            if (!statePocRows.isEmpty() || !vendorRows.isEmpty()) {
                List<ActorCountRow> combined = new ArrayList<>(statePocRows);
                combined.addAll(vendorRows);
                stateSections.add(StateDailyBreachSection.builder()
                        .stateName(stateName)
                        .breaches(combined)
                        .build());
            }

            List<EscalationTicket> previouslyOpen = previouslyOpenByState.getOrDefault(stateCode, List.of());
            long statePocCount = previouslyOpen.stream()
                    .filter(t -> EscalationActorUtil.classifyByWorkflowState(t.getApplicationStatus())
                            == EscalationActorUtil.ActorBucket.STATE_SPOC)
                    .count();
            long vendorCount = previouslyOpen.stream()
                    .filter(t -> EscalationActorUtil.classifyByWorkflowState(t.getApplicationStatus())
                            == EscalationActorUtil.ActorBucket.VENDOR)
                    .count();
            previouslyOpenRows.add(StatePreviouslyOpenRow.builder()
                    .stateName(stateName)
                    .statePocCount(statePocCount)
                    .vendorCount(vendorCount)
                    .build());
        }

        String stateListLabel = stateCodesInOrder.stream()
                .map(commonUtility::getStateDisplayName)
                .collect(Collectors.joining(", "));

        return DailySeniorProgramManagerSummary.builder()
                .recipientName(recipientName)
                .stateListLabel(stateListLabel)
                .asOfDate(DATE_FORMAT.format(new Date()))
                .dashboardUrl(commonUtility.generateStateDashboardUrl())
                .stateSections(stateSections)
                .previouslyOpenByState(previouslyOpenRows)
                .build();
    }

    private List<ActorCountRow> groupActorRows(List<EscalationTicket> tickets, EscalationActorUtil.ActorBucket bucket) {
        Map<String, ActorCountRow> grouped = new LinkedHashMap<>();

        for (EscalationTicket ticket : tickets) {
            if (EscalationActorUtil.classifyByWorkflowState(ticket.getApplicationStatus()) != bucket) {
                continue;
            }

            String actorName = EscalationActorUtil.resolveActorName(ticket);
            String status = EscalationActorUtil.resolveStatusLabel(ticket.getApplicationStatus());
            String key = actorName + "|" + status;

            ActorCountRow row = grouped.computeIfAbsent(key, k -> ActorCountRow.builder()
                    .actorName(actorName)
                    .currentStatus(status)
                    .count(0)
                    .build());
            row.setCount(row.getCount() + 1);
        }

        return grouped.values().stream()
                .sorted(Comparator.comparing(ActorCountRow::getActorName)
                        .thenComparing(ActorCountRow::getCurrentStatus))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
