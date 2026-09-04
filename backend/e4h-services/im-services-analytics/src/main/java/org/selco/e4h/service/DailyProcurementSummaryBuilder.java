package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationActorUtil;
import org.selco.e4h.util.EscalationTicketUtil;
import org.selco.e4h.web.models.DailyProcurementSummary;
import org.selco.e4h.web.models.EscalationRoleEscalationItem;
import org.selco.e4h.web.models.EscalationTicket;
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
                .newBreaches(groupVendorRows(newBreaches))
                .previouslyOpen(groupVendorRows(previouslyOpen))
                .build();
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
