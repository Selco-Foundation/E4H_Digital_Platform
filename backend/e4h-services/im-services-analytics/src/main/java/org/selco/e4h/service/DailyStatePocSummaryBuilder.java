package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationActorUtil;
import org.selco.e4h.web.models.ActorCountRow;
import org.selco.e4h.web.models.DailyStatePocSummary;
import org.selco.e4h.web.models.EscalationRoleEscalationItem;
import org.selco.e4h.web.models.EscalationTicket;
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
public class DailyStatePocSummaryBuilder {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    private final SLABreachDetectionService slaBreachDetectionService;
    private final CommonUtility commonUtility;

    public DailyStatePocSummary buildSummary(String stateCode,
                                             String recipientName,
                                             EscalationRoleEscalationItem escalationItem,
                                             String escalationRecipientId,
                                             RequestInfo requestInfo) {
        List<String> workflowStates = escalationItem.getWorkflowStates();
        String escalationLevel = escalationItem.getEscalationLevel();

        List<EscalationTicket> newBreaches = slaBreachDetectionService.findSLABreachTickets(
                stateCode, workflowStates, escalationRecipientId, escalationLevel, requestInfo);
        List<EscalationTicket> previouslyOpen = slaBreachDetectionService.findPreviouslyEscalatedStillOpenTickets(
                stateCode, workflowStates, escalationRecipientId, escalationLevel, requestInfo);

        return DailyStatePocSummary.builder()
                .stateName(commonUtility.getStateDisplayName(stateCode))
                .recipientName(recipientName)
                .asOfDate(DATE_FORMAT.format(new Date()))
                .dashboardUrl(commonUtility.generateStateDashboardUrl())
                .newCrmBreaches(groupActorRows(newBreaches, EscalationActorUtil.ActorBucket.CRM, false))
                .newTechPocBreaches(groupActorRows(newBreaches, EscalationActorUtil.ActorBucket.TECH_POC, true))
                .previouslyOpenCrmBreaches(groupActorRows(previouslyOpen, EscalationActorUtil.ActorBucket.CRM, true))
                .previouslyOpenTechPocBreaches(groupActorRows(previouslyOpen, EscalationActorUtil.ActorBucket.TECH_POC, true))
                .build();
    }

    private List<ActorCountRow> groupActorRows(List<EscalationTicket> tickets,
                                               EscalationActorUtil.ActorBucket bucket,
                                               boolean includeStatus) {
        Map<String, ActorCountRow> grouped = new LinkedHashMap<>();

        for (EscalationTicket ticket : tickets) {
            if (EscalationActorUtil.classifyByWorkflowState(ticket.getApplicationStatus()) != bucket) {
                continue;
            }

            String actorName = EscalationActorUtil.resolveActorName(ticket);
            String status = EscalationActorUtil.resolveStatusLabel(ticket.getApplicationStatus());
            String key = includeStatus ? actorName + "|" + status : actorName;

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
