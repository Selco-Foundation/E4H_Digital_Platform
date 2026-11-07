package org.egov.inbox.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What each of the caller's roles may see, as boundary codes grouped by the level they sit at.
 *
 * This is the authority the inbox filters on. The browser still sends
 * {@code jurisdictionSearchCriteria}, but that is now read as a *narrowing request* - the user
 * choosing one state out of the several they hold - and is applied alongside this scope rather
 * than instead of it. A forged value can therefore only ever shrink the result, never widen it.
 *
 * Per role, not per user. A user who is State SPOC in one state and Tech POC in another must not
 * see State SPOC work in the second state: pairing each role with its own boundaries is the whole
 * point, and flattening them into one set is the bug this replaces.
 *
 * <h3>Why there is no descendant expansion</h3>
 * A grant is matched at its own level - a State grant becomes a clause on
 * {@code Data.incident.boundary.stateCode} - because every incident is indexed with its whole
 * ancestor chain: {@code BoundaryService.fetchBoundaryFromBoundaryCode} searches with
 * {@code includeParents=true} and {@code buildBoundaryHierarchy} sets country, state, district,
 * block and facility on each document. So a state-level clause already matches every ticket
 * beneath that state.
 *
 * Expanding a grant to its descendant codes instead would be worse, not merely redundant: that
 * list would have to be cached, and a facility created after the cache warmed would be missing
 * from it, making its tickets invisible to the state's holders until the cache expired. Matching
 * at the grant's own level has no such window, and it keeps boundary-service off this read path.
 */
@Slf4j
@Component
public class RoleJurisdictionScope {

    @Autowired
    private HRMSUtil hrmsUtil;

    /** Boundary type as HRMS stores it, to the criteria name the inbox config uses. */
    private static final Map<String, String> BOUNDARY_TYPE_TO_CRITERIA = new HashMap<>();

    static {
        BOUNDARY_TYPE_TO_CRITERIA.put("Country", "country");
        BOUNDARY_TYPE_TO_CRITERIA.put("State", "state");
        BOUNDARY_TYPE_TO_CRITERIA.put("District", "district");
        BOUNDARY_TYPE_TO_CRITERIA.put("Block", "block");
        BOUNDARY_TYPE_TO_CRITERIA.put("Facility", "facility");
    }

    /**
     * The scope as {@code roleCode -> level -> boundary codes}, or {@code null} when it could not
     * be resolved.
     *
     * {@code null} means "do not scope", not "scope to nothing". HRMS being unreachable must not
     * empty every inbox in the country; the same fail-open choice is already made for workflow
     * lookups in this service, and §3.2 of the design states it for the resolver. An empty map, by
     * contrast, is a real answer: the caller holds no jurisdictions.
     */
    public Map<String, Map<String, Set<String>>> resolve(RequestInfo requestInfo) {
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

        Map<String, Map<String, Set<String>>> scopeByRole = new LinkedHashMap<>();
        for (HRMSUtil.JurisdictionGrant grant : grants) {
            String level = BOUNDARY_TYPE_TO_CRITERIA.get(grant.getBoundaryType());
            if (level == null) {
                log.warn("Jurisdiction at {} has an unmapped boundaryType {}; ignoring it",
                        grant.getBoundary(), grant.getBoundaryType());
                continue;
            }
            // No roles on the grant means every role the holder has - what every jurisdiction
            // created before role scoping looks like.
            Set<String> appliesTo = grant.appliesToEveryRole() ? heldRoles : grant.getRoleCodes();
            for (String roleCode : appliesTo) {
                if (!heldRoles.contains(roleCode)) {
                    // Scoping stored against a role the user no longer holds grants nothing.
                    continue;
                }
                scopeByRole.computeIfAbsent(roleCode, k -> new LinkedHashMap<>())
                        .computeIfAbsent(level, k -> new HashSet<>())
                        .add(grant.getBoundary());
            }
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
}
