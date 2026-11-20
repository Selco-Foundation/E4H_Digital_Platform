package org.egov.im.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.web.models.Incident;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.egov.im.util.IMConstants.OUT_OF_WARRANTY_ON_HOLD;
import static org.egov.im.util.IMConstants.SYSTEM_FUNCTIONAL_NON_FUNCTIONAL;
import static org.egov.im.util.IMConstants.SYSTEM_FUNCTIONAL_ON_HOLD;

/**
 * Keeps a ticket's systemFunctional at {@code ON_HOLD} while it sits in {@code OUT_OF_WARRANTY_ON_HOLD}.
 *
 * <p>On entering hold the value the ticket had is saved in
 * {@code additionalDetail.systemFunctionalBeforeOnHold}; on leaving hold it is restored and the key removed.
 */
@Slf4j
public final class OnHoldSystemFunctional {

    public static final String BEFORE_ON_HOLD_KEY = "systemFunctionalBeforeOnHold";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OnHoldSystemFunctional() {
    }

    /**
     * Fills what an update request may leave out (systemFunctional and the saved pre-hold value) from
     * the stored ticket, so the hold transition works on the ticket's real state.
     */
    public static void carryOver(Incident incoming, Incident existing) {
        if (existing == null) {
            return;
        }
        if (incoming.getSystemFunctional() == null) {
            incoming.setSystemFunctional(existing.getSystemFunctional());
        }
        Object saved = toMap(existing.getAdditionalDetail()).get(BEFORE_ON_HOLD_KEY);
        if (saved != null) {
            Map<String, Object> detail = toMap(incoming.getAdditionalDetail());
            if (!detail.containsKey(BEFORE_ON_HOLD_KEY)) {
                detail.put(BEFORE_ON_HOLD_KEY, saved);
                incoming.setAdditionalDetail(detail);
            }
        }
    }

    /**
     * Applies the hold rule for the status the workflow has just moved the ticket to.
     */
    public static void apply(Incident incident, String newStatus) {
        boolean onHold = OUT_OF_WARRANTY_ON_HOLD.equals(newStatus);
        boolean flaggedOnHold = SYSTEM_FUNCTIONAL_ON_HOLD.equals(incident.getSystemFunctional());

        if (onHold && !flaggedOnHold) {
            Map<String, Object> detail = toMap(incident.getAdditionalDetail());
            detail.put(BEFORE_ON_HOLD_KEY, incident.getSystemFunctional());
            incident.setAdditionalDetail(detail);
            incident.setSystemFunctional(SYSTEM_FUNCTIONAL_ON_HOLD);
        } else if (!onHold && flaggedOnHold) {
            Map<String, Object> detail = toMap(incident.getAdditionalDetail());
            Object before = detail.remove(BEFORE_ON_HOLD_KEY);
            String restored = before != null ? before.toString() : SYSTEM_FUNCTIONAL_NON_FUNCTIONAL;
            if (before == null) {
                log.warn("No saved systemFunctional for incidentId={} leaving hold; defaulting to {}",
                        incident.getIncidentId(), restored);
            }
            incident.setAdditionalDetail(detail);
            incident.setSystemFunctional(restored);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toMap(Object additionalDetail) {
        Map<String, Object> map = additionalDetail == null
                ? null
                : MAPPER.convertValue(additionalDetail, LinkedHashMap.class);
        return map != null ? map : new LinkedHashMap<>();
    }
}
