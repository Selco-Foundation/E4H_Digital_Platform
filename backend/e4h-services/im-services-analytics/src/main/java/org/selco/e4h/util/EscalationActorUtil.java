package org.selco.e4h.util;

import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.web.models.EscalationTicket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public final class EscalationActorUtil {

    public enum ActorBucket {
        CRM,
        TECH_POC,
        STATE_SPOC,
        VENDOR,
        UNKNOWN
    }

    private EscalationActorUtil() {
    }

    public static ActorBucket classifyByWorkflowState(String applicationStatus) {
        if (applicationStatus == null) {
            return ActorBucket.UNKNOWN;
        }
        if (EscalationWorkflowStates.CRM_STATES.contains(applicationStatus)) {
            return ActorBucket.CRM;
        }
        if (EscalationWorkflowStates.TECH_POC_STATES.contains(applicationStatus)) {
            return ActorBucket.TECH_POC;
        }
        if (EscalationWorkflowStates.STATE_SPOC_STATES.contains(applicationStatus)) {
            return ActorBucket.STATE_SPOC;
        }
        if (EscalationWorkflowStates.VENDOR_STATES.contains(applicationStatus)) {
            return ActorBucket.VENDOR;
        }
        return ActorBucket.UNKNOWN;
    }

    public static String resolveActorName(EscalationTicket ticket) {
        if (ticket == null) {
            return "Unknown";
        }

        ActorBucket bucket = classifyByWorkflowState(ticket.getApplicationStatus());
        if (bucket == ActorBucket.VENDOR) {
            if (ticket.getMappedVendor() != null && !ticket.getMappedVendor().isBlank()) {
                return ticket.getMappedVendor();
            }
        }

        String assigneeName = extractAssigneeName(ticket.getAdditionalDetails());
        if (assigneeName != null && !assigneeName.isBlank()) {
            return assigneeName;
        }

        return switch (bucket) {
            case CRM -> "CRM";
            case TECH_POC -> "Tech PoC";
            case STATE_SPOC -> "State POC";
            case VENDOR -> "Vendor";
            default -> "Unknown";
        };
    }

    public static String resolveStatusLabel(String applicationStatus) {
        if (applicationStatus == null) {
            return "Unknown";
        }
        return switch (applicationStatus) {
            case EscalationWorkflowStates.PENDING_FOR_ASSIGNMENT -> "Pending For Assignment";
            case EscalationWorkflowStates.RMS_DEVICE_PENDING_TECH_POC -> "RMS Device - Pending with Tech POC";
            case EscalationWorkflowStates.OUT_OF_WARRANTY_PENDING_TECH_POC -> "Out of Warranty - Pending with Tech POC";
            case EscalationWorkflowStates.OUT_OF_SCOPE -> "Out of Scope - Pending with State SPOC";
            case EscalationWorkflowStates.PENDING_ASSIGNMENT_OUT_OF_WARRANTY -> "Out of Warranty - Pending with State SPOC";
            case EscalationWorkflowStates.PENDING_RESOLUTION -> "Pending Resolution";
            case EscalationWorkflowStates.PENDING_RESOLUTION_SPARE_PART_NEEDED ->
                    "Spare Part Change - Pending with Vendor for Resolution";
            case EscalationWorkflowStates.PENDING_RESOLUTION_OUT_OF_WARRANTY ->
                    "Out of Warranty - Pending with Vendor for Resolution";
            case EscalationWorkflowStates.PENDING_RESOLUTION_OUT_OF_SCOPE ->
                    "Out of Scope - Pending Resolution";
            default -> applicationStatus;
        };
    }

    @SuppressWarnings("unchecked")
    private static String extractAssigneeName(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        Map<String, Object> currentProcessInstance = (Map<String, Object>) data.get("currentProcessInstance");
        if (currentProcessInstance == null) {
            return null;
        }

        Object assignesObj = currentProcessInstance.get("assignes");
        if (assignesObj instanceof List<?> assignees && !assignees.isEmpty()) {
            Object first = assignees.get(0);
            if (first instanceof Map<?, ?> assigneeMap) {
                Object name = assigneeMap.get("name");
                return name != null ? name.toString() : null;
            }
        }

        Object assignerObj = currentProcessInstance.get("assigner");
        if (assignerObj instanceof Map<?, ?> assignerMap) {
            Object name = assignerMap.get("name");
            return name != null ? name.toString() : null;
        }
        return null;
    }
}
