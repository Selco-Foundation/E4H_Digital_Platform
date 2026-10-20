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

    /**
     * Some indexed tickets (~1,900 as of Sep 2026) have no {@code incident.boundary} object at all,
     * but do carry a plain state name directly on {@code Data.state} - falling back to that instead
     * of giving up avoids dumping otherwise well-formed tickets into an "Unknown" state bucket.
     */
    @SuppressWarnings("unchecked")
    public static String resolveStateCode(EscalationTicket ticket) {
        if (ticket == null || ticket.getAdditionalDetails() == null) {
            return null;
        }
        Map<String, Object> data = ticket.getAdditionalDetails();
        Map<String, Object> incident = (Map<String, Object>) data.get("incident");
        if (incident != null) {
            Map<String, Object> boundary = (Map<String, Object>) incident.get("boundary");
            if (boundary != null) {
                Object stateCode = boundary.get("stateCode");
                if (stateCode != null) {
                    return stateCode.toString();
                }
            }
        }
        Object plainState = data.get("state");
        return plainState != null ? plainState.toString() : null;
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

    /**
     * A real district/block name never starts with "India_" or contains "_" - when a ticket's
     * district/block field holds a raw boundary code instead of a resolved name, the actual name
     * is still recoverable: boundary codes follow the COUNTRY_STATE_DISTRICT_BLOCK_FACILITY
     * hierarchy, so the last "_"-separated segment is exactly the name this field represents
     * (e.g. "India_karnataka_raichur" as a district value -> "Raichur").
     */
    public static String resolveBoundaryNameOrFlag(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return rawValue;
        }
        if (rawValue.startsWith("India_") || rawValue.contains("_")) {
            String[] parts = rawValue.split("_");
            String lastPart = parts[parts.length - 1];
            if (lastPart.isEmpty()) {
                return rawValue;
            }
            return lastPart.substring(0, 1).toUpperCase() + lastPart.substring(1).toLowerCase();
        }
        return rawValue;
    }

    public static boolean isNonFunctional(EscalationTicket ticket) {
        if (ticket == null || ticket.getAdditionalDetails() == null) {
            return false;
        }
        Object status = ticket.getAdditionalDetails().get("systemFunctional");
        return "NON_FUNCTIONAL".equalsIgnoreCase(String.valueOf(status));
    }
}
