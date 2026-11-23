package org.egov.im.service;

import org.egov.im.service.CurrentOwnerService.CurrentOwner;
import org.egov.im.web.models.workflow.Action;
import org.egov.im.web.models.workflow.State;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.egov.im.util.IMConstants.OUT_OF_WARRANTY_ON_HOLD;
import static org.egov.im.util.IMConstants.PENDING_ASSIGNMENT_OUT_OF_WARRANTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Covers the current owner of a ticket on hold.
 */
class CurrentOwnerOnHoldTest {

    private final CurrentOwnerService service = new CurrentOwnerService(null);

    private static final Map<String, String> ROLE_MAP = Map.of("COMPLAINT_FACILITATOR_1", "State SPOC");

    @Test
    void onHoldIsNotApplicable() {
        CurrentOwner owner = service.resolveOwner(state(OUT_OF_WARRANTY_ON_HOLD), () -> ROLE_MAP, "IM-1");

        assertEquals("Not Applicable", owner.programRole());
        assertNull(owner.systemRole());
    }

    @Test
    void otherStatesStillResolveFromRoles() {
        CurrentOwner owner = service.resolveOwner(state(PENDING_ASSIGNMENT_OUT_OF_WARRANTY), () -> ROLE_MAP, "IM-1");

        assertEquals("State SPOC", owner.programRole());
        assertEquals("COMPLAINT_FACILITATOR_1", owner.systemRole());
    }

    private static State state(String status) {
        Action resume = Action.builder().action("RESUME").roles(List.of("COMPLAINT_FACILITATOR_1")).active(true).build();
        return State.builder().state(status).applicationStatus(status).isTerminateState(false)
                .actions(List.of(resume)).build();
    }
}
