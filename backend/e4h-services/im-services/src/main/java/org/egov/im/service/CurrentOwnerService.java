package org.egov.im.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.util.MDMSUtils;
import org.egov.im.web.models.IncidentRequestWrapper;
import org.egov.im.web.models.IndexView;
import org.egov.im.web.models.workflow.Action;
import org.egov.im.web.models.workflow.ProcessInstance;
import org.egov.im.web.models.workflow.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static org.egov.im.util.IMConstants.USER_TYPE_JSONPATH;

/**
 * Resolves the program role that currently owns an incident, so it can be indexed alongside the
 * ticket and reported on.
 * <p>
 * The workflow engine only knows system roles: the {@code roles} on the actions available from the
 * ticket's current state are exactly the roles allowed to move it forward, i.e. the roles the
 * ticket is waiting on. Program roles ("CRM", "Vendor", "Tech POC", ...) live in the
 * {@code USER_ANALYTICS.USER_TYPE} MDMS master, which maps each program role to the system roles
 * that make it up. Reversing that master turns a workflow system role into the program role
 * reported on dashboards.
 * <p>
 * Purely additive and best-effort: any failure leaves the owner fields null rather than failing the
 * create/update the caller is in the middle of.
 *
 * @see CurrentOwnerBackfillService for the batch path that applies the same derivation to already
 * indexed tickets
 */
@Service
@Slf4j
public class CurrentOwnerService {

    private final MDMSUtils mdmsUtils;

    @Autowired
    public CurrentOwnerService(MDMSUtils mdmsUtils) {
        this.mdmsUtils = mdmsUtils;
    }

    /**
     * The owner of a ticket: the program role it is waiting on, and the workflow system role that
     * program role was derived from.
     */
    public record CurrentOwner(String programRole, String systemRole) {
    }

    /**
     * Sets {@code currentOwner} (program role) and {@code currentOwnerSystemRole} (the workflow role
     * it was derived from) on the wrapper's {@link IndexView}, so every push of that wrapper to an
     * indexer topic carries them.
     *
     * @param wrapper         the wrapper about to be published; its IndexView is enriched in place
     * @param processInstance the incident's current process instance, i.e. the state it now sits in
     * @param states          the states of the incident's BusinessService, which carry the actions
     *                        and their roles
     */
    public void enrichCurrentOwner(IncidentRequestWrapper wrapper, ProcessInstance processInstance,
                                   List<State> states) {
        log.trace("CurrentOwnerService::enrichCurrentOwner method invoked");
        if (wrapper == null || wrapper.getIncidentRequest() == null) {
            return;
        }
        IndexView indexView = wrapper.getIndexView();
        if (indexView == null) {
            indexView = new IndexView();
            wrapper.setIndexView(indexView);
        }

        String incidentId = wrapper.getIncidentRequest().getIncident() != null
                ? wrapper.getIncidentRequest().getIncident().getIncidentId() : null;
        try {
            State processState = (processInstance == null) ? null : processInstance.getState();
            String stateLabel = (processState == null) ? null : processState.getState();
            State currentState = (processState == null) ? null
                    : findState(processState.getUuid(), processState.getState(), states);
            if (currentState == null) {
                log.warn("Current owner: state {} not found in the business service definition for incidentId={}",
                        stateLabel, incidentId);
                return;
            }

            RequestInfo requestInfo = wrapper.getIncidentRequest().getRequestInfo();
            String tenantId = wrapper.getIncidentRequest().getIncident() == null ? null
                    : wrapper.getIncidentRequest().getIncident().getTenantId();
            // Supplied lazily so a terminal state costs no MDMS call.
            CurrentOwner owner = resolveOwner(currentState,
                    () -> buildSystemRoleToProgramRoleMap(requestInfo, tenantId), incidentId);
            if (owner == null) {
                return;
            }

            indexView.setCurrentOwner(owner.programRole());
            indexView.setCurrentOwnerSystemRole(owner.systemRole());
            log.debug("Current owner for incidentId={} state={} resolved to {} (system role {})",
                    incidentId, stateLabel, owner.programRole(), owner.systemRole());
        } catch (Exception e) {
            log.error("Current owner: failed to resolve owner for incidentId={}, owner will be null",
                    incidentId, e);
        }
    }

    /**
     * Derives the owner of a ticket sitting in {@code currentState}, or null when the ticket is owned
     * by nobody (a terminal state) or by a role outside {@code USER_TYPE} (SYSTEM, AUTO_ESCALATE).
     *
     * @param currentState             the state as defined on the BusinessService, i.e. carrying its
     *                                 actions and their roles
     * @param systemRoleToProgramRole  supplier of the reversed {@code USER_TYPE} lookup, invoked only
     *                                 once the state is known to be owned by someone; batch callers
     *                                 pass a cached map, the live path builds one per call
     * @param contextId                incident id, for logging only
     */
    public CurrentOwner resolveOwner(State currentState, Supplier<Map<String, String>> systemRoleToProgramRole,
                                     String contextId) {
        Set<String> systemRoles = resolveOwningSystemRoles(currentState);
        if (systemRoles.isEmpty()) {
            // A closed ticket is owned by nobody: terminal states are either action-less
            // (CLOSEDAFTERREJECTION) or only carry after-the-fact actions such as RATE
            // (CLOSEDAFTERRESOLUTION). Either way, leave the fields empty rather than inventing
            // an owner.
            log.info("Current owner: no actionable roles on state {} for incidentId={}, owner will be null",
                    currentState.getState(), contextId);
            return null;
        }

        // Distinct program roles the state is waiting on, in the order the workflow lists them.
        Map<String, String> roleMap = systemRoleToProgramRole.get();
        Map<String, String> programRoleToSystemRole = new LinkedHashMap<>();
        for (String systemRole : systemRoles) {
            String programRole = roleMap.get(systemRole);
            if (programRole != null) {
                programRoleToSystemRole.putIfAbsent(programRole, systemRole);
            }
        }

        if (programRoleToSystemRole.isEmpty()) {
            // Roles such as SYSTEM / AUTO_ESCALATE / COMPLAINT_CLOSER are not part of USER_TYPE.
            log.info("Current owner: no USER_TYPE program role for roles {} on state {} for incidentId={}",
                    systemRoles, currentState.getState(), contextId);
            return null;
        }
        if (programRoleToSystemRole.size() > 1) {
            log.warn("Current owner: state {} maps to multiple program roles {} for incidentId={}, "
                            + "taking the first", currentState.getState(),
                    programRoleToSystemRole.keySet(), contextId);
        }

        Map.Entry<String, String> owner = programRoleToSystemRole.entrySet().iterator().next();
        return new CurrentOwner(owner.getKey(), owner.getValue());
    }

    /**
     * The system roles allowed to act on the given state, in the order the workflow lists them. These
     * are the roles the ticket is waiting on, so they identify its owner.
     * <p>
     * Terminal states are deliberately excluded: the ticket has finished its lifecycle, so it is not
     * waiting on anyone. Any actions they still define are after-the-fact ones — CLOSEDAFTERRESOLUTION
     * keeps RATE for the complainant — and treating those as ownership would leave closed tickets
     * sitting in someone's bucket.
     */
    private Set<String> resolveOwningSystemRoles(State currentState) {
        if (Boolean.TRUE.equals(currentState.getIsTerminateState())
                || CollectionUtils.isEmpty(currentState.getActions())) {
            return Collections.emptySet();
        }
        Set<String> systemRoles = new LinkedHashSet<>();
        for (Action action : currentState.getActions()) {
            if (action == null || Boolean.FALSE.equals(action.getActive())
                    || CollectionUtils.isEmpty(action.getRoles())) {
                continue;
            }
            action.getRoles().stream().filter(Objects::nonNull).forEach(systemRoles::add);
        }
        return systemRoles;
    }

    /**
     * Locates a state in the business service definition, which is the only place actions and their
     * roles are available. Matches on uuid first since it is unambiguous, falling back to the state
     * name.
     */
    public State findState(String stateUuid, String stateName, List<State> states) {
        if (CollectionUtils.isEmpty(states)) {
            return null;
        }
        if (stateUuid != null) {
            for (State state : states) {
                if (stateUuid.equals(state.getUuid())) {
                    return state;
                }
            }
        }
        if (stateName != null) {
            for (State state : states) {
                if (stateName.equals(state.getState())) {
                    return state;
                }
            }
        }
        return null;
    }

    /**
     * Reverses {@code USER_ANALYTICS.USER_TYPE} into a system role -> program role lookup. A system
     * role can appear on several records (COMPLAINANT is listed both on its own for HCR and
     * alongside COMPLAINT_ASSESSOR for CRM); the record with the fewest system roles is the most
     * specific definition of that role and wins, so COMPLAINANT resolves to HCR while
     * COMPLAINT_ASSESSOR resolves to CRM.
     */
    public Map<String, String> buildSystemRoleToProgramRoleMap(RequestInfo requestInfo, String tenantId) {
        Object mdmsData = mdmsUtils.getUserAnalyticsMDMSData(requestInfo, tenantId);
        List<Map<String, Object>> userTypeRecords = readList(mdmsData, USER_TYPE_JSONPATH);

        Map<String, String> systemRoleToProgramRole = new LinkedHashMap<>();
        userTypeRecords.stream()
                .filter(this::isActive)
                .filter(record -> asString(record.get("program_role")) != null)
                .filter(record -> !asStringList(record.get("system_roles")).isEmpty())
                .sorted(Comparator.comparingInt(
                        record -> asStringList(record.get("system_roles")).size()))
                .forEach(record -> {
                    String programRole = asString(record.get("program_role"));
                    asStringList(record.get("system_roles"))
                            .forEach(systemRole -> systemRoleToProgramRole.putIfAbsent(systemRole, programRole));
                });
        return systemRoleToProgramRole;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readList(Object mdmsData, String jsonPath) {
        try {
            Object result = JsonPath.read(mdmsData, jsonPath);
            if (result instanceof List) {
                return (List<Map<String, Object>>) result;
            }
        } catch (Exception e) {
            log.warn("Current owner: unable to read MDMS path {}", jsonPath);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<String> asStringList(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        return ((List<Object>) value).stream().filter(Objects::nonNull).map(Object::toString).toList();
    }

    private String asString(Object value) {
        return (value == null) ? null : value.toString();
    }

    private boolean isActive(Map<String, Object> record) {
        Object active = record.get("active");
        return active == null || Boolean.parseBoolean(active.toString());
    }
}
