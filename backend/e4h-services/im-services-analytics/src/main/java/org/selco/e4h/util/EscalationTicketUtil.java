package org.selco.e4h.util;

import org.selco.e4h.web.models.EscalationInfo;
import org.selco.e4h.web.models.EscalationTicket;

import java.util.List;
import java.util.Map;

public final class EscalationTicketUtil {

    private static final List<String> CLOSED_STATUSES = List.of(
            "RESOLVED", "CLOSED_AFTER_RESOLUTION", "CLOSED_AFTER_REJECTION", "REJECTED");

    private EscalationTicketUtil() {
    }

    @SuppressWarnings("unchecked")
    public static String resolveStateCode(EscalationTicket ticket) {
        if (ticket == null || ticket.getAdditionalDetails() == null) {
            return null;
        }
        Map<String, Object> incident = (Map<String, Object>) ticket.getAdditionalDetails().get("incident");
        if (incident == null) {
            return null;
        }
        Map<String, Object> boundary = (Map<String, Object>) incident.get("boundary");
        if (boundary == null) {
            return null;
        }
        Object stateCode = boundary.get("stateCode");
        return stateCode != null ? stateCode.toString() : null;
    }

    public static boolean isClosed(EscalationTicket ticket) {
        if (ticket == null || ticket.getApplicationStatus() == null) {
            return false;
        }
        return CLOSED_STATUSES.contains(ticket.getApplicationStatus().toUpperCase());
    }

    public static boolean isTheft(EscalationTicket ticket) {
        return ticket != null && "Theft".equalsIgnoreCase(ticket.getIncidentType());
    }

    public static Long getResolvedTimestamp(EscalationTicket ticket) {
        if (ticket == null || ticket.getAdditionalDetails() == null) {
            return null;
        }
        Object resolved = ticket.getAdditionalDetails().get("resolvedTimestamp");
        if (resolved instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public static Long getFiledDate(EscalationTicket ticket) {
        if (ticket == null) {
            return null;
        }
        if (ticket.getFiledDate() != null) {
            return ticket.getFiledDate();
        }
        return ticket.getTicketFiledDate();
    }

    public static boolean hasEscalationAtLevelSince(EscalationTicket ticket, String level, long beforeTimeMs) {
        if (ticket == null || ticket.getEscalationInfo() == null) {
            return false;
        }
        for (EscalationInfo info : ticket.getEscalationInfo()) {
            if (level.equals(info.getEscalationLevel())
                    && info.getEscalationTime() != null
                    && info.getEscalationTime() <= beforeTimeMs) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if the ticket's current applicationStatus matches the status recorded on its most
     * recent escalation entry at {@code level} (i.e. no workflow-state transition since escalation).
     * Tickets escalated before the applicationStatus field was captured (recorded status is null)
     * are treated as unchanged, since there is no prior status to compare against.
     */
    public static boolean statusUnchangedSinceEscalation(EscalationTicket ticket, String level) {
        if (ticket == null || ticket.getEscalationInfo() == null) {
            return false;
        }
        EscalationInfo latestAtLevel = null;
        for (EscalationInfo info : ticket.getEscalationInfo()) {
            if (level.equals(info.getEscalationLevel()) && info.getEscalationTime() != null
                    && (latestAtLevel == null || info.getEscalationTime() > latestAtLevel.getEscalationTime())) {
                latestAtLevel = info;
            }
        }
        if (latestAtLevel == null) {
            return false;
        }
        String recordedStatus = latestAtLevel.getApplicationStatus();
        return recordedStatus == null || recordedStatus.equals(ticket.getApplicationStatus());
    }

    public static boolean isNonFunctional(EscalationTicket ticket) {
        if (ticket == null || ticket.getAdditionalDetails() == null) {
            return false;
        }
        Object status = ticket.getAdditionalDetails().get("systemFunctional");
        return "NON_FUNCTIONAL".equalsIgnoreCase(String.valueOf(status));
    }
}
