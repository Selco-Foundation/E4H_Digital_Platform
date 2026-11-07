package facility.web.models;

import java.util.Collections;
import java.util.Set;

/**
 * What the caller is allowed to see, and how that was decided.
 *
 * <h3>Why this is a type and not a {@code Set<String>}</h3>
 * "no ceiling", "a ceiling of nothing" and "we could not work out a ceiling" are three different
 * answers, and two of them are opposites. Represented as a nullable set they are one null check
 * apart, and the first edit that gets that check backwards turns the feature off silently while
 * still looking correct in manual testing - a scoped user sees their own facilities either way.
 * Keeping the decision explicit is what stops that.
 */
public final class FacilityScope {

    public enum Decision {
        /** Feature flag is off. No clause, no lookups. */
        UNSCOPED_FLAG_OFF,
        /** A service, not a person. Identity-based, and evaluated before the header is read. */
        UNSCOPED_SYSTEM,
        /** Warn mode: the ceiling was computed and logged, but deliberately not applied. */
        UNSCOPED_WARN,
        /** A ceiling was resolved and must be applied. */
        SCOPED,
        /** Human caller, no acting role declared. */
        DENY_MISSING_ACTING_ROLE,
        /** Acting role declared, but the caller does not hold it. */
        DENY_INVALID_ACTING_ROLE,
        /** Resolved cleanly: this role grants the caller nothing. A real answer, so 200 with no rows. */
        DENY_NO_JURISDICTION,
        /** HRMS or boundary-service did not answer. Not an answer, so 503. */
        DENY_SCOPE_UNAVAILABLE
    }

    private final Decision decision;
    private final String actingRole;
    private final Set<String> ceiling;
    private final String summary;

    private FacilityScope(Decision decision, String actingRole, Set<String> ceiling, String summary) {
        this.decision = decision;
        this.actingRole = actingRole;
        this.ceiling = ceiling == null ? Collections.emptySet() : Collections.unmodifiableSet(ceiling);
        this.summary = summary;
    }

    public static FacilityScope unscoped(Decision decision) {
        return new FacilityScope(decision, null, Collections.emptySet(), null);
    }

    /** Warn mode: carries the ceiling that {@link Decision#SCOPED} would have applied, for the audit log. */
    public static FacilityScope warn(String actingRole, Set<String> wouldBeCeiling, String summary) {
        return new FacilityScope(Decision.UNSCOPED_WARN, actingRole, wouldBeCeiling, summary);
    }

    public static FacilityScope scoped(String actingRole, Set<String> ceiling, String summary) {
        return new FacilityScope(Decision.SCOPED, actingRole, ceiling, summary);
    }

    public static FacilityScope deny(Decision decision, String actingRole) {
        return new FacilityScope(decision, actingRole, Collections.emptySet(), null);
    }

    public Decision decision() {
        return decision;
    }

    public String actingRole() {
        return actingRole;
    }

    /** The resolved ceiling. In warn mode this is what <em>would</em> have been applied. */
    public Set<String> ceiling() {
        return ceiling;
    }

    public String summary() {
        return summary;
    }

    /** Whether the ceiling clause goes into the query. Only ever true for {@link Decision#SCOPED}. */
    public boolean isApplied() {
        return decision == Decision.SCOPED;
    }

    public boolean isDeny() {
        return decision == Decision.DENY_MISSING_ACTING_ROLE
                || decision == Decision.DENY_INVALID_ACTING_ROLE
                || decision == Decision.DENY_NO_JURISDICTION
                || decision == Decision.DENY_SCOPE_UNAVAILABLE;
    }

    /**
     * A caller who holds nothing has been given a real answer, so it is an empty result rather than
     * an error. Everything else that denies is a genuine failure the caller should see as one.
     */
    public boolean isEmptyResultDenial() {
        return decision == Decision.DENY_NO_JURISDICTION;
    }

    /** The value of the {@code x-jurisdiction-scope} response header. */
    public String headerValue() {
        if (decision == Decision.UNSCOPED_WARN) {
            return "warn";
        }
        return isApplied() || isEmptyResultDenial() ? "applied" : "unscoped";
    }

    public String errorCode() {
        switch (decision) {
            case DENY_MISSING_ACTING_ROLE:
                return "MISSING_ACTING_ROLE";
            case DENY_INVALID_ACTING_ROLE:
                return "INVALID_ACTING_ROLE";
            case DENY_SCOPE_UNAVAILABLE:
                return "JURISDICTION_SCOPE_UNAVAILABLE";
            default:
                return null;
        }
    }

    public String errorMessage() {
        switch (decision) {
            case DENY_MISSING_ACTING_ROLE:
                return "This request must declare which role it is acting as, in the x-acting-role header.";
            case DENY_INVALID_ACTING_ROLE:
                return "You do not hold the role '" + actingRole + "'.";
            case DENY_SCOPE_UNAVAILABLE:
                return "Could not determine your jurisdictions; the request was refused rather than "
                        + "answered with results that may be incomplete. Please retry.";
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "FacilityScope{" + decision + ", actingRole=" + actingRole + ", ceiling=" + ceiling.size() + "}";
    }
}
