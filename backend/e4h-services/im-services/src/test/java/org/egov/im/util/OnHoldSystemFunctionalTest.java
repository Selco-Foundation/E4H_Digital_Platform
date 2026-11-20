package org.egov.im.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.im.web.models.Incident;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.egov.im.util.IMConstants.OUT_OF_WARRANTY_ON_HOLD;
import static org.egov.im.util.IMConstants.PENDING_ASSIGNMENT_OUT_OF_WARRANTY;
import static org.egov.im.util.OnHoldSystemFunctional.BEFORE_ON_HOLD_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Covers systemFunctional switching to ON_HOLD around OUT_OF_WARRANTY_ON_HOLD.
 */
class OnHoldSystemFunctionalTest {

    @Test
    void enteringHoldSavesPreviousValue() {
        Incident incident = incident("NON_FUNCTIONAL", Map.of("rejectReason", "x"));
        OnHoldSystemFunctional.apply(incident, OUT_OF_WARRANTY_ON_HOLD);

        assertEquals("ON_HOLD", incident.getSystemFunctional());
        assertEquals("NON_FUNCTIONAL", detail(incident).get(BEFORE_ON_HOLD_KEY));
        assertEquals("x", detail(incident).get("rejectReason"));
    }

    @Test
    void leavingHoldRestoresPreviousValue() {
        Incident incident = incident("FUNCTIONAL", null);
        OnHoldSystemFunctional.apply(incident, OUT_OF_WARRANTY_ON_HOLD);
        OnHoldSystemFunctional.apply(incident, PENDING_ASSIGNMENT_OUT_OF_WARRANTY);

        assertEquals("FUNCTIONAL", incident.getSystemFunctional());
        assertFalse(detail(incident).containsKey(BEFORE_ON_HOLD_KEY));
    }

    @Test
    void stayingOnHoldKeepsSavedValue() {
        Incident incident = incident("NON_FUNCTIONAL", null);
        OnHoldSystemFunctional.apply(incident, OUT_OF_WARRANTY_ON_HOLD);
        OnHoldSystemFunctional.apply(incident, OUT_OF_WARRANTY_ON_HOLD);

        assertEquals("ON_HOLD", incident.getSystemFunctional());
        assertEquals("NON_FUNCTIONAL", detail(incident).get(BEFORE_ON_HOLD_KEY));
    }

    @Test
    void leavingHoldWithoutSavedValueDefaultsToNonFunctional() {
        Incident incident = incident("ON_HOLD", null);
        OnHoldSystemFunctional.apply(incident, PENDING_ASSIGNMENT_OUT_OF_WARRANTY);

        assertEquals("NON_FUNCTIONAL", incident.getSystemFunctional());
    }

    @Test
    void otherTransitionsAreUntouched() {
        Incident incident = incident("NON_FUNCTIONAL", null);
        OnHoldSystemFunctional.apply(incident, PENDING_ASSIGNMENT_OUT_OF_WARRANTY);

        assertEquals("NON_FUNCTIONAL", incident.getSystemFunctional());
    }

    @Test
    void carryOverFillsWhatTheRequestLeftOut() {
        Incident stored = incident("ON_HOLD",
                new ObjectMapper().valueToTree(Map.of(BEFORE_ON_HOLD_KEY, "FUNCTIONAL")));
        Incident incoming = incident(null, new HashMap<>(Map.of("other", 1)));

        OnHoldSystemFunctional.carryOver(incoming, stored);

        assertEquals("ON_HOLD", incoming.getSystemFunctional());
        assertEquals("FUNCTIONAL", detail(incoming).get(BEFORE_ON_HOLD_KEY));
        assertEquals(1, detail(incoming).get("other"));
    }

    private static Incident incident(String systemFunctional, Object additionalDetail) {
        Incident incident = new Incident();
        incident.setSystemFunctional(systemFunctional);
        incident.setAdditionalDetail(additionalDetail);
        return incident;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> detail(Incident incident) {
        return (Map<String, Object>) incident.getAdditionalDetail();
    }
}
