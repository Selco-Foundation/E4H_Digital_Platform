package org.egov.inbox.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What each of the caller's roles may see, as boundary codes.
 *
 * This is the authority the inbox filters on. The browser still sends
 * {@code jurisdictionSearchCriteria}, but that is now read as a *narrowing request* - the user
 * choosing one state out of the several they hold - and is intersected with what HRMS says they
 * actually have. A forged value can therefore only ever shrink the result, never widen it.
 *
 * Per role, not per user. A user who is State SPOC in one state and Tech POC in another must not
 * see State SPOC work in the second state: pairing each role with its own boundaries is the whole
 * point, and flattening them into one set is the bug this replaces.
 */
@Slf4j
@Component
public class RoleJurisdictionScope {

    @Autowired
    private HRMSUtil hrmsUtil;

    @Autowired
    private BoundaryUtil boundaryUtil;

    /**
     * The scope, or {@code null} when it could not be resolved.
     *
     * {@code null} means "do not scope", not "scope to nothing". HRMS or boundary-service being
     * unreachable must not empty every inbox in the country; the same fail-open choice is already
     * made for workflow lookups in this service, and §3.2 of the design states it for the resolver.
     * An empty map, by contrast, is a real answer: the caller holds no jurisdictions.
     */
    public Map<String, Set<String>> resolve(RequestInfo requestInfo, Map<String, Object> requestedNarrowing) {
        List<HRMSUtil.JurisdictionGrant> grants = hrmsUtil.getJurisdictionsForCaller(requestInfo);
        if (CollectionUtils.isEmpty(grants)) {
            log.warn("No jurisdictions resolved for the caller; leaving the inbox unscoped");
            return null;
        }

        Set<String> heldRoles = heldRoleCodes(requestInfo);
        if (heldRoles.isEmpty()) {
            log.warn("Caller holds no roles; leaving the inbox unscoped");
            return null;
        }

        Map<String, Set<String>> descendants = boundaryUtil.getDescendantCodes();
        if (CollectionUtils.isEmpty(descendants)) {
            log.warn("No boundary tree available; leaving the inbox unscoped");
            return null;
        }

        Map<String, Set<String>> scopeByRole = new LinkedHashMap<>();
        for (HRMSUtil.JurisdictionGrant grant : grants) {
            // Expanded to the codes documents actually carry. A boundary the tree does not know
            // contributes only itself rather than disappearing.
            Set<String> covered = descendants.getOrDefault(grant.getBoundary(),
                    Collections.singleton(grant.getBoundary()));
            // No roles on the grant means every role the holder has - what every jurisdiction
            // created before role scoping looks like.
            Set<String> appliesTo = grant.appliesToEveryRole() ? heldRoles : grant.getRoleCodes();
            for (String roleCode : appliesTo) {
                if (!heldRoles.contains(roleCode)) {
                    // Scoping stored against a role the user no longer holds grants nothing.
                    continue;
                }
                scopeByRole.computeIfAbsent(roleCode, k -> new HashSet<>()).addAll(covered);
            }
        }

        Set<String> narrowing = flattenRequestedBoundaries(requestedNarrowing);
        if (!narrowing.isEmpty()) {
            scopeByRole.replaceAll((roleCode, codes) -> {
                Set<String> intersected = new HashSet<>(codes);
                intersected.retainAll(narrowing);
                return intersected;
            });
            scopeByRole.values().removeIf(Set::isEmpty);
            log.debug("Narrowed the caller's scope to {} requested boundaries", narrowing.size());
        }

        log.info("Resolved inbox scope for {} role(s)", scopeByRole.size());
        return scopeByRole;
    }

    private Set<String> heldRoleCodes(RequestInfo requestInfo) {
        Set<String> codes = new HashSet<>();
        if (requestInfo == null || requestInfo.getUserInfo() == null
                || CollectionUtils.isEmpty(requestInfo.getUserInfo().getRoles())) {
            return codes;
        }
        for (Role role : requestInfo.getUserInfo().getRoles()) {
            if (role != null && role.getCode() != null) {
                codes.add(role.getCode());
            }
        }
        return codes;
    }

    /**
     * The boundary codes the client asked to be narrowed to, across every level it sent.
     *
     * Level is irrelevant once the scope is expanded to descendants: asking for a district and
     * asking for its block both mean "only these codes", and the intersection handles the rest.
     */
    @SuppressWarnings("unchecked")
    private Set<String> flattenRequestedBoundaries(Map<String, Object> requested) {
        Set<String> codes = new HashSet<>();
        if (CollectionUtils.isEmpty(requested)) {
            return codes;
        }
        for (Map.Entry<String, Object> entry : requested.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List) {
                for (Object item : (List<Object>) value) {
                    if (item != null) {
                        codes.add(String.valueOf(item));
                    }
                }
            } else if (value != null) {
                codes.add(String.valueOf(value));
            }
        }
        // The browser's placeholder for "no jurisdiction yet" - see useNewInbox.js, which defaults
        // to {country: ["-"]}. Treating it as a real boundary would intersect the scope to nothing.
        codes.remove("-");
        return codes;
    }

    /** The expanded scope of a single role, for callers that only need one. */
    public Set<String> boundariesFor(Map<String, Set<String>> scopeByRole, String roleCode) {
        if (scopeByRole == null) {
            return null;
        }
        return scopeByRole.getOrDefault(roleCode, new HashSet<>());
    }
}
