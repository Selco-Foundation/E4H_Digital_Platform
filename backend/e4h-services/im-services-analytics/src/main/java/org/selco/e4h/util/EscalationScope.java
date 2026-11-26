package org.selco.e4h.util;

import org.selco.e4h.web.models.Employee;
import org.selco.e4h.web.models.EscalationTicket;
import org.selco.e4h.web.models.Jurisdiction;
import org.selco.e4h.web.models.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The part of the country one escalation recipient answers for: a level and a boundary code, taken
 * from one of their HRMS jurisdictions.
 *
 * <p>A State SPOC used to be found by searching HRMS for the state, so only state-level holders were
 * ever emailed and each got the whole state. A holder may now be posted at district or block level,
 * so the recipient's own jurisdictions decide both whether they are emailed for a state and which of
 * that state's tickets they see.
 *
 * <p>Tickets are matched on the codes indexed with them ({@code Data.incident.boundary.*}). A ticket
 * indexed without a boundary object can still be matched at state level through the {@code Data.state}
 * fallback, but never at district or block level - it reaches the state-level recipients only.
 */
public final class EscalationScope {

    public enum Level { COUNTRY, STATE, DISTRICT, BLOCK }

    private final Level level;
    private final String code;

    private EscalationScope(Level level, String code) {
        this.level = level;
        this.code = code;
    }

    public Level getLevel() {
        return level;
    }

    public String getCode() {
        return code;
    }

    /**
     * The scopes in which {@code employee} holds {@code roleCode}: every active jurisdiction that lists
     * the role, or lists no roles at all (which applies to every role the employee holds). Facility
     * grants are ignored - an escalation recipient covers an area, not one health centre.
     */
    public static List<EscalationScope> forRole(Employee employee, String roleCode) {
        Set<EscalationScope> scopes = new LinkedHashSet<>();
        if (employee == null || employee.getJurisdictions() == null) {
            return new ArrayList<>(scopes);
        }
        for (Jurisdiction jurisdiction : employee.getJurisdictions()) {
            if (jurisdiction == null || Boolean.FALSE.equals(jurisdiction.getIsActive())
                    || jurisdiction.getBoundary() == null || jurisdiction.getBoundary().isBlank()
                    || !appliesTo(jurisdiction, roleCode)) {
                continue;
            }
            Level level = levelOf(jurisdiction.getBoundaryType());
            if (level != null) {
                scopes.add(new EscalationScope(level, jurisdiction.getBoundary().trim()));
            }
        }
        return new ArrayList<>(scopes);
    }

    /**
     * Whether this scope falls in {@code stateCode}: the whole country does, the state itself does,
     * and so does any district or block whose code sits under it.
     *
     * <p>The containment test is a code prefix, the same one vendor-registry and rms-service already
     * use: boundary codes are built as paths ({@code India_Karnataka_Bagalkote}), and matching against
     * the full state code plus the separator cannot confuse two states.
     */
    public boolean liesIn(String stateCode) {
        switch (level) {
            case COUNTRY:
                return true;
            case STATE:
                return code.equals(stateCode);
            default:
                return stateCode != null && code.startsWith(stateCode + "_");
        }
    }

    /** Whether the ticket is inside this scope. */
    public boolean covers(EscalationTicket ticket) {
        switch (level) {
            case COUNTRY:
                return true;
            case STATE:
                return code.equals(EscalationTicketUtil.resolveStateCode(ticket));
            case DISTRICT:
                return code.equals(boundaryCode(ticket, "districtCode"));
            case BLOCK:
                return code.equals(boundaryCode(ticket, "blockCode"));
            default:
                return false;
        }
    }

    public static boolean anyCovers(Collection<EscalationScope> scopes, EscalationTicket ticket) {
        return scopes.stream().anyMatch(scope -> scope.covers(ticket));
    }

    private static boolean appliesTo(Jurisdiction jurisdiction, String roleCode) {
        List<Role> roles = jurisdiction.getRoles();
        if (roles == null || roles.isEmpty()) {
            return true;
        }
        return roles.stream().anyMatch(role -> role != null && roleCode.equalsIgnoreCase(role.getCode()));
    }

    private static Level levelOf(String boundaryType) {
        if (boundaryType == null) {
            return null;
        }
        switch (boundaryType.trim().toLowerCase(Locale.ROOT)) {
            case "country":
                return Level.COUNTRY;
            case "state":
                return Level.STATE;
            case "district":
                return Level.DISTRICT;
            case "block":
                return Level.BLOCK;
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static String boundaryCode(EscalationTicket ticket, String key) {
        if (ticket == null || ticket.getAdditionalDetails() == null) {
            return null;
        }
        Object incident = ticket.getAdditionalDetails().get("incident");
        if (!(incident instanceof Map)) {
            return null;
        }
        Object boundary = ((Map<String, Object>) incident).get("boundary");
        if (!(boundary instanceof Map)) {
            return null;
        }
        Object value = ((Map<String, Object>) boundary).get(key);
        return value == null ? null : value.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EscalationScope)) {
            return false;
        }
        EscalationScope that = (EscalationScope) other;
        return level == that.level && code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, code);
    }

    @Override
    public String toString() {
        return level + ":" + code;
    }
}
