package facility.util;

import facility.web.models.Jurisdiction;

import java.util.Collections;
import java.util.List;

/**
 * The outcome of asking HRMS for a caller's jurisdictions.
 *
 * The failure modes are kept apart on purpose. "HRMS says you hold nothing" and "HRMS did not
 * answer" both end in the caller seeing no rows, but one is a correct answer and the other is an
 * outage - and only one of them should page somebody. The inbox's equivalent flattens every failure
 * to an empty list, which is safe for an inbox that fails open and is exactly wrong here.
 */
public final class JurisdictionGrants {

    public enum Status {
        OK,
        NO_CALLER_UUID,
        EMPLOYEE_NOT_FOUND,
        HRMS_UNAVAILABLE
    }

    private final Status status;
    private final List<Jurisdiction> grants;

    private JurisdictionGrants(Status status, List<Jurisdiction> grants) {
        this.status = status;
        this.grants = grants == null ? Collections.emptyList() : Collections.unmodifiableList(grants);
    }

    public static JurisdictionGrants ok(List<Jurisdiction> grants) {
        return new JurisdictionGrants(Status.OK, grants);
    }

    public static JurisdictionGrants failed(Status status) {
        return new JurisdictionGrants(status, Collections.emptyList());
    }

    public Status status() {
        return status;
    }

    public boolean isOk() {
        return status == Status.OK;
    }

    /** Active grants carrying a boundary. Empty when {@link #isOk()} is false. */
    public List<Jurisdiction> grants() {
        return grants;
    }
}
