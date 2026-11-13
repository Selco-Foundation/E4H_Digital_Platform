package org.egov.im.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.im.service.BoundaryService;
import org.egov.im.web.models.Boundary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Whether the caller may act on a ticket, given where the ticket is.
 *
 * <h3>The question</h3>
 * Workflow decides which <em>roles</em> may take an action. HRMS decides <em>where</em> a person
 * holds a role. Neither knows about the other, so a Tech POC in State 2 is offered Tech POC actions
 * on a State 1 ticket and the transition succeeds. This class is the intersection: does the caller
 * hold one of the action's roles at or above this ticket's boundary?
 *
 * <h3>At or above, resolved properly</h3>
 * A ticket sits at a facility; a grant may be held at facility, block, district, state or country.
 * The test is therefore containment, and the ancestor chain comes from boundary-service through
 * {@link BoundaryService#fetchBoundaryFromBoundaryCode} - which already asks with
 * {@code includeParents=true} and returns exactly those five codes.
 *
 * <p>It is deliberately <em>not</em> derived by splitting the boundary code on underscores. That
 * trick appears five times in this service already and is wrong for any boundary whose name
 * contains an underscore; extending it into an authorisation decision would be worse than leaving
 * it where it is.
 *
 * <h3>Fail closed</h3>
 * If HRMS cannot be asked, or the ticket's ancestors cannot be resolved, this refuses. That is the
 * opposite of the inbox, which fails open - and deliberately so. An inbox that shows too much is a
 * nuisance; an action gate that opens under failure is not a gate.
 */
@Slf4j
@Component
public class JurisdictionAuthorizer {

    @Autowired
    private HrmsJurisdictionUtil hrmsJurisdictionUtil;

    @Autowired
    private BoundaryService boundaryService;

    /**
     * Whether the caller holds any of {@code allowedRoles} at or above {@code boundaryCode}.
     *
     * @param allowedRoles the roles the workflow action permits; only those the caller actually
     *                     holds are considered, so a role they were never granted cannot be
     *                     satisfied by a jurisdiction
     */
    public boolean mayActAt(RequestInfo requestInfo, Collection<String> allowedRoles,
                            String boundaryCode, String tenantId) {
        Set<String> heldRoles = heldRoleCodes(requestInfo);
        Set<String> effectiveRoles = new LinkedHashSet<>();
        for (String allowed : safe(allowedRoles)) {
            for (String held : heldRoles) {
                if (held.equalsIgnoreCase(allowed)) {
                    effectiveRoles.add(held);
                }
            }
        }
        if (effectiveRoles.isEmpty()) {
            // Workflow would refuse this anyway; say so here rather than reporting it as a
            // jurisdiction problem, which would send the reader looking in the wrong place.
            log.debug("Caller holds none of the roles this action allows: {}", allowedRoles);
            return false;
        }

        Set<String> ancestors = ancestorsOf(requestInfo, boundaryCode, tenantId);
        if (ancestors.isEmpty()) {
            log.error("Could not resolve the boundary chain for {}; refusing the action", boundaryCode);
            return false;
        }

        HrmsJurisdictionUtil.Outcome outcome = hrmsJurisdictionUtil.getJurisdictionsForCaller(requestInfo);
        if (!outcome.isResolved()) {
            log.error("Could not resolve the caller's jurisdictions; refusing the action");
            return false;
        }

        for (HrmsJurisdictionUtil.Grant grant : outcome.getGrants()) {
            if (!ancestors.contains(grant.getBoundary())) {
                continue;
            }
            for (String role : effectiveRoles) {
                if (grant.appliesTo(role, heldRoles)) {
                    log.debug("Caller may act as {} at {} (granted {})", role, boundaryCode, grant.getBoundary());
                    return true;
                }
            }
        }
        log.info("Caller holds {} but none of them at or above {}", effectiveRoles, boundaryCode);
        return false;
    }

    /**
     * The ticket's boundary and every boundary above it.
     *
     * Empty means "could not resolve" - {@link BoundaryService} swallows its exceptions and returns
     * null, so an empty result here covers both a failure and a boundary that is not in the
     * hierarchy. Both must refuse, so they are not separated.
     */
    public Set<String> ancestorsOf(RequestInfo requestInfo, String boundaryCode, String tenantId) {
        if (!StringUtils.hasText(boundaryCode)) {
            return Collections.emptySet();
        }
        Boundary boundary = boundaryService.fetchBoundaryFromBoundaryCode(requestInfo, boundaryCode, tenantId);
        if (boundary == null) {
            return Collections.emptySet();
        }
        Set<String> chain = new LinkedHashSet<>();
        addIfPresent(chain, boundary.getCountryCode());
        addIfPresent(chain, boundary.getStateCode());
        addIfPresent(chain, boundary.getDistrictCode());
        addIfPresent(chain, boundary.getBlockCode());
        addIfPresent(chain, boundary.getFacilityCode());
        // The code on the ticket counts even if the hierarchy spells its leaf differently.
        addIfPresent(chain, boundaryCode);
        return chain;
    }

    /**
     * The ticket's boundary and its ancestors as a CSV, for HRMS's {@code boundaryCodes} param.
     *
     * Empty when the chain cannot be resolved. Callers searching for role-holders must treat that
     * as "do not widen" rather than "search everywhere" - searching the whole tenant is the hack
     * this replaces.
     */
    public String ancestorChainCsv(RequestInfo requestInfo, String boundaryCode, String tenantId) {
        return String.join(",", ancestorsOf(requestInfo, boundaryCode, tenantId));
    }

    private static void addIfPresent(Set<String> target, String value) {
        if (StringUtils.hasText(value)) {
            target.add(value);
        }
    }

    private static Set<String> heldRoleCodes(RequestInfo requestInfo) {
        Set<String> codes = new HashSet<>();
        if (requestInfo == null || requestInfo.getUserInfo() == null
                || CollectionUtils.isEmpty(requestInfo.getUserInfo().getRoles())) {
            return codes;
        }
        for (Role role : requestInfo.getUserInfo().getRoles()) {
            if (role != null && StringUtils.hasText(role.getCode())) {
                codes.add(role.getCode().trim());
            }
        }
        return codes;
    }

    private static Collection<String> safe(Collection<String> values) {
        return values == null ? Collections.emptyList() : values;
    }
}
