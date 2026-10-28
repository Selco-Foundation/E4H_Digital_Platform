package org.selco.e4h.util;

import org.selco.e4h.web.models.workflow.ProcessInstance;
import org.selco.e4h.web.models.workflow.State;

import java.util.Comparator;
import java.util.List;

/**
 * Computes whether a vendor responded to a ticket within the SLA allotted to the workflow step
 * they were assigned in, using the ticket's real workflow transition history (from
 * egov-workflow-v2's process-instance search, {@code history=true}) rather than a proxy.
 */
public final class VendorResponseUtil {

    private VendorResponseUtil() {
    }

    /**
     * @param history the ticket's full process-instance history (any order), or null/empty if
     *                unavailable.
     * @return {@code true}/{@code false} when a definitive answer can be computed; {@code null}
     *         when the history doesn't contain a vendor-state entry at all (no data to judge —
     *         caller should fall back to another signal rather than treat this as a failure).
     */
    public static Boolean respondedWithinSla(List<ProcessInstance> history) {
        if (history == null || history.isEmpty()) {
            return null;
        }

        List<ProcessInstance> sorted = history.stream()
                .filter(pi -> pi.getAuditDetails() != null && pi.getAuditDetails().getCreatedTime() != null)
                .sorted(Comparator.comparing(pi -> pi.getAuditDetails().getCreatedTime()))
                .toList();

        Long assignedTime = null;
        Long slaMs = null;

        for (ProcessInstance pi : sorted) {
            State state = pi.getState();
            String applicationStatus = state != null ? state.getApplicationStatus() : null;
            long transitionTime = pi.getAuditDetails().getCreatedTime();

            if (assignedTime == null) {
                if (applicationStatus != null && EscalationWorkflowStates.VENDOR_STATES.contains(applicationStatus)) {
                    assignedTime = transitionTime;
                    slaMs = state.getSla();
                }
                continue;
            }

            if (transitionTime > assignedTime
                    && (applicationStatus == null || !EscalationWorkflowStates.VENDOR_STATES.contains(applicationStatus))) {
                long responseMs = transitionTime - assignedTime;
                return slaMs != null && responseMs <= slaMs;
            }
        }

        if (assignedTime == null) {
            // Ticket's history never shows it entering a vendor-owned state — no basis to judge.
            return null;
        }

        // Still awaiting the vendor's first action — check if the step SLA has elapsed yet.
        long elapsed = System.currentTimeMillis() - assignedTime;
        return slaMs != null && elapsed <= slaMs;
    }

    private static final String VENDOR_ROLE = "COMPLAINT_RESOLVER";

    /**
     * @return {@code true} if the ticket's first RESOLVED step was done by a vendor user (role
     *         COMPLAINT_RESOLVER) or came straight out of a vendor-owned state, {@code false} if it
     *         was resolved by someone else, {@code null} if the history is unavailable or has no
     *         RESOLVED step. The role check matters: tickets migrated from the old system only carry
     *         the final RESOLVE step in their history, so there is no previous state to look at.
     */
    public static Boolean resolvedFromVendorState(List<ProcessInstance> history) {
        if (history == null || history.isEmpty()) {
            return null;
        }
        List<ProcessInstance> sorted = history.stream()
                .filter(pi -> pi.getAuditDetails() != null && pi.getAuditDetails().getCreatedTime() != null)
                .sorted(Comparator.comparing(pi -> pi.getAuditDetails().getCreatedTime()))
                .toList();

        String previousStatus = null;
        for (ProcessInstance pi : sorted) {
            String applicationStatus = pi.getState() != null ? pi.getState().getApplicationStatus() : null;
            if (IMConstants.RESOLVED.equals(applicationStatus)) {
                boolean resolvedByVendorUser = pi.getAssigner() != null && pi.getAssigner().getRoles() != null
                        && pi.getAssigner().getRoles().stream().anyMatch(r -> VENDOR_ROLE.equals(r.getCode()));
                return resolvedByVendorUser
                        || (previousStatus != null && EscalationWorkflowStates.VENDOR_STATES.contains(previousStatus));
            }
            previousStatus = applicationStatus;
        }
        return null;
    }
}
