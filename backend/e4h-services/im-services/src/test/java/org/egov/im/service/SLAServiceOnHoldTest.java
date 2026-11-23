package org.egov.im.service;

import org.egov.im.web.models.workflow.ProcessInstance;
import org.egov.im.web.models.workflow.State;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.egov.im.util.IMConstants.OUT_OF_WARRANTY_ON_HOLD;
import static org.egov.im.util.IMConstants.OUT_OF_WARRANTY_PENDING_TECH_POC;
import static org.egov.im.util.IMConstants.PENDINGATVENDOR;
import static org.egov.im.util.IMConstants.PENDINGFORASSIGNMENT;
import static org.egov.im.util.IMConstants.PENDING_ASSIGNMENT_OUT_OF_WARRANTY;
import static org.egov.im.util.IMConstants.PENDING_RESOLUTION_OUT_OF_WARRANTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the SLA pause and reset around OUT_OF_WARRANTY_ON_HOLD.
 */
class SLAServiceOnHoldTest {

    private static final long CRM_SLA = 10L;
    private static final long VENDOR_SLA = 20L;
    private static final long TECH_POC_SLA = 30L;
    private static final long SPOC_SLA = 40L;
    private static final long OOW_VENDOR_SLA = 50L;

    private final SLAService slaService = new SLAService(null);

    private final List<State> states = List.of(
            state(PENDINGFORASSIGNMENT, CRM_SLA),
            state(PENDINGATVENDOR, VENDOR_SLA),
            state(OUT_OF_WARRANTY_PENDING_TECH_POC, TECH_POC_SLA),
            state(PENDING_ASSIGNMENT_OUT_OF_WARRANTY, SPOC_SLA),
            state(OUT_OF_WARRANTY_ON_HOLD, null),
            state(PENDING_RESOLUTION_OUT_OF_WARRANTY, OOW_VENDOR_SLA));

    @Test
    void historyWithoutOnHoldIsUnchanged() {
        List<ProcessInstance> history = history(PENDINGFORASSIGNMENT, PENDINGATVENDOR, OUT_OF_WARRANTY_PENDING_TECH_POC);

        assertEquals(history, slaService.getCurrentSlaCycle(history));
    }

    @Test
    void onHoldHasNoSla() {
        List<ProcessInstance> cycle = slaService.getCurrentSlaCycle(history(PENDINGFORASSIGNMENT, PENDINGATVENDOR,
                OUT_OF_WARRANTY_PENDING_TECH_POC, PENDING_ASSIGNMENT_OUT_OF_WARRANTY, OUT_OF_WARRANTY_ON_HOLD));

        assertTrue(cycle.isEmpty());
        assertEquals(0L, slaService.computeTotalSla(OUT_OF_WARRANTY_ON_HOLD, states, cycle));
    }

    @Test
    void resumeResetsSlaToStateSpoc() {
        List<ProcessInstance> cycle = slaService.getCurrentSlaCycle(history(PENDINGFORASSIGNMENT, PENDINGATVENDOR,
                OUT_OF_WARRANTY_PENDING_TECH_POC, PENDING_ASSIGNMENT_OUT_OF_WARRANTY, OUT_OF_WARRANTY_ON_HOLD,
                PENDING_ASSIGNMENT_OUT_OF_WARRANTY));

        assertEquals(1, cycle.size());
        assertEquals(PENDING_ASSIGNMENT_OUT_OF_WARRANTY, cycle.get(0).getState().getApplicationStatus());
        // Fresh SPOC SLA plus the follow-up OOW vendor SLA; CRM, vendor, Tech POC and SPOC time before the hold is dropped
        assertEquals(SPOC_SLA + OOW_VENDOR_SLA, slaService.computeTotalSla(PENDING_ASSIGNMENT_OUT_OF_WARRANTY, states, cycle));
    }

    @Test
    void assignAfterResumeAddsVendorSla() {
        List<ProcessInstance> cycle = slaService.getCurrentSlaCycle(history(PENDING_ASSIGNMENT_OUT_OF_WARRANTY,
                OUT_OF_WARRANTY_ON_HOLD, PENDING_ASSIGNMENT_OUT_OF_WARRANTY, PENDING_RESOLUTION_OUT_OF_WARRANTY));

        assertEquals(2, cycle.size());
        assertEquals(SPOC_SLA + OOW_VENDOR_SLA, slaService.computeTotalSla(PENDING_RESOLUTION_OUT_OF_WARRANTY, states, cycle));
    }

    @Test
    void onlyTheMostRecentHoldCounts() {
        List<ProcessInstance> cycle = slaService.getCurrentSlaCycle(history(PENDING_ASSIGNMENT_OUT_OF_WARRANTY,
                OUT_OF_WARRANTY_ON_HOLD, PENDING_ASSIGNMENT_OUT_OF_WARRANTY, OUT_OF_WARRANTY_ON_HOLD,
                PENDING_ASSIGNMENT_OUT_OF_WARRANTY));

        assertEquals(1, cycle.size());
    }

    private static List<ProcessInstance> history(String... applicationStatuses) {
        return java.util.Arrays.stream(applicationStatuses)
                .map(status -> ProcessInstance.builder().state(state(status, null)).build())
                .toList();
    }

    private static State state(String applicationStatus, Long sla) {
        return State.builder().state(applicationStatus).applicationStatus(applicationStatus).sla(sla).build();
    }
}
