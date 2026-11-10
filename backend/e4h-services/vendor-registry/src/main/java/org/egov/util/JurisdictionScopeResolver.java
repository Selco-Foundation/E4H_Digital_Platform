package org.egov.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.config.Configuration;
import org.egov.tracer.model.CustomException;
import org.egov.web.models.Employee;
import org.egov.web.models.Jurisdiction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.egov.util.OrganisationConstant.VENDOR_ADMIN;

/**
 * The area of the organisation tree one caller may administer users in.
 *
 * A vendor administrator manages only the users inside their own jurisdictions. "Inside" means at
 * or beneath a boundary they hold VENDOR_ADMIN at, so a district administrator reaches their
 * district and its blocks, and not the state above it.
 *
 * The same resolved scope answers every question the org user screens ask - which users are
 * listed, which jurisdictions are shown for each of them, which ones may be granted, and which
 * must be preserved untouched on a save. One definition, so the table and the save cannot
 * disagree about who is in reach.
 *
 * **This fails closed.** A caller whose scope cannot be resolved administers nobody. That is a
 * harder failure than it is elsewhere in this feature - a hidden row is an inconvenience, a
 * wrongly granted one is privilege escalation - and the whole point here is that a vendor
 * administrator in one state cannot mint a user in another.
 */
@Component
@Slf4j
public class JurisdictionScopeResolver {

    private final HRMSUtils hrmsUtils;
    private final BoundaryUtil boundaryUtil;
    private final Configuration config;

    @Autowired
    public JurisdictionScopeResolver(HRMSUtils hrmsUtils, BoundaryUtil boundaryUtil, Configuration config) {
        this.hrmsUtils = hrmsUtils;
        this.boundaryUtil = boundaryUtil;
        this.config = config;
    }

    /**
     * What one caller may reach.
     *
     * `unscoped` is not "an empty scope" - the two are opposites, and keeping them apart is why
     * this is a type rather than a bare set. A platform administrator is unscoped and sees
     * everything; a vendor administrator whose grants came back empty is scoped to nothing and
     * sees nobody.
     */
    public record Scope(boolean unscoped, Set<String> codes) {

        public static Scope everything() {
            return new Scope(true, Collections.emptySet());
        }

        public static Scope nothing() {
            return new Scope(false, Collections.emptySet());
        }

        /**
         * Whether this scope reaches `boundaryCode`, given that boundary's own ancestor chain.
         *
         * The chain has to be supplied rather than looked up here, because resolving it is a
         * network call and the callers batch it across every boundary they are testing.
         */
        public boolean covers(String boundaryCode, Set<String> ancestorsIncludingSelf) {
            if (unscoped) {
                return true;
            }
            if (StringUtils.isBlank(boundaryCode) || ancestorsIncludingSelf == null) {
                return false;
            }
            // In scope when the boundary is one of the grants, or sits anywhere beneath one.
            return ancestorsIncludingSelf.stream().anyMatch(codes::contains);
        }
    }

    public Scope resolve(RequestInfo requestInfo, String tenantId) {
        if (requestInfo == null || requestInfo.getUserInfo() == null) {
            throw new CustomException("INVALID_USER", "A caller is required to administer organisation users");
        }

        if (isUnscoped(requestInfo)) {
            log.debug("Caller administers organisation users without a jurisdiction restriction");
            return Scope.everything();
        }

        String callerUuid = requestInfo.getUserInfo().getUuid();
        if (StringUtils.isBlank(callerUuid)) {
            throw new CustomException("INVALID_USER", "User UUID is required to administer organisation users");
        }

        Set<String> grants = fetchVendorAdminBoundaries(requestInfo, callerUuid);

        if (grants.isEmpty()) {
            // Every vendor administrator is expected to hold at least one jurisdiction, so this is
            // a data or availability problem rather than a legitimate state. Refusing makes it show
            // up as an empty screen to investigate instead of as silent over-permission.
            log.warn("Caller {} holds no {} jurisdictions; administering no organisation users", callerUuid, VENDOR_ADMIN);
            return Scope.nothing();
        }

        log.debug("Caller {} administers {} jurisdictions", callerUuid, grants.size());
        return new Scope(false, grants);
    }

    /**
     * The jurisdictions of this list that the scope reaches.
     *
     * Resolves the ancestors of every boundary in the list in **one** call, then filters. Batching
     * is the point: this is called per user on a page of results, and a lookup each would be a
     * network round trip per row.
     */
    public List<Jurisdiction> within(Scope scope, List<Jurisdiction> jurisdictions,
                                     RequestInfo requestInfo, String tenantId) {
        if (jurisdictions == null || jurisdictions.isEmpty()) {
            return Collections.emptyList();
        }
        if (scope.unscoped()) {
            return jurisdictions;
        }
        Map<String, Set<String>> chains = chainsFor(jurisdictions, requestInfo, tenantId);
        return jurisdictions.stream()
                .filter(Objects::nonNull)
                .filter(jurisdiction -> scope.covers(jurisdiction.getBoundary(),
                        chains.get(jurisdiction.getBoundary())))
                .toList();
    }

    /** True when every jurisdiction in the list is one this caller may act on. */
    public boolean coversAll(Scope scope, Collection<Jurisdiction> jurisdictions,
                             RequestInfo requestInfo, String tenantId) {
        if (scope.unscoped()) {
            return true;
        }
        if (jurisdictions == null || jurisdictions.isEmpty()) {
            return true;
        }
        Map<String, Set<String>> chains = chainsFor(jurisdictions, requestInfo, tenantId);
        return jurisdictions.stream()
                .filter(Objects::nonNull)
                .allMatch(jurisdiction -> scope.covers(jurisdiction.getBoundary(),
                        chains.get(jurisdiction.getBoundary())));
    }

    /** The jurisdictions of this list the scope does NOT reach - what an edit must preserve. */
    public List<Jurisdiction> outside(Scope scope, List<Jurisdiction> jurisdictions,
                                      RequestInfo requestInfo, String tenantId) {
        if (scope.unscoped() || jurisdictions == null || jurisdictions.isEmpty()) {
            return new ArrayList<>();
        }
        List<Jurisdiction> inScope = within(scope, jurisdictions, requestInfo, tenantId);
        return jurisdictions.stream()
                .filter(Objects::nonNull)
                .filter(jurisdiction -> !inScope.contains(jurisdiction))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Map<String, Set<String>> chainsFor(Collection<Jurisdiction> jurisdictions,
                                               RequestInfo requestInfo, String tenantId) {
        Set<String> boundaries = jurisdictions.stream()
                .filter(Objects::nonNull)
                .map(Jurisdiction::getBoundary)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return boundaryUtil.ancestorsOf(requestInfo, tenantId, boundaries);
    }

    private boolean isUnscoped(RequestInfo requestInfo) {
        List<Role> roles = requestInfo.getUserInfo().getRoles();
        if (roles == null) {
            return false;
        }
        List<String> unscopedRoles = config.getJurisdictionUnscopedRoles();
        return roles.stream()
                .filter(Objects::nonNull)
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .anyMatch(code -> unscopedRoles.stream().anyMatch(code::equalsIgnoreCase));
    }

    /**
     * The boundaries this caller holds VENDOR_ADMIN at.
     *
     * Only the jurisdictions carrying that role count, not every jurisdiction the person has: an
     * administrator who is also a field user somewhere else does not administer users there.
     * A jurisdiction with no role rows predates role scoping and applies to every role its holder
     * has, which is the same rule HRMS search uses, so it counts here too.
     */
    private Set<String> fetchVendorAdminBoundaries(RequestInfo requestInfo, String callerUuid) {
        Employee employee;
        try {
            employee = hrmsUtils.getUserById(RequestInfoWrapper.builder().requestInfo(requestInfo).build(), callerUuid);
        } catch (Exception exception) {
            log.error("HRMS lookup failed for caller {}", callerUuid, exception);
            throw new CustomException("JURISDICTION_LOOKUP_FAILED",
                    "Could not determine the jurisdictions of the signed-in user");
        }

        if (employee == null || employee.getJurisdictions() == null) {
            return Collections.emptySet();
        }

        Set<String> boundaries = new LinkedHashSet<>();
        for (Jurisdiction jurisdiction : employee.getJurisdictions()) {
            if (jurisdiction == null || StringUtils.isBlank(jurisdiction.getBoundary())) {
                continue;
            }
            if (Boolean.FALSE.equals(jurisdiction.getIsActive())) {
                continue;
            }
            if (appliesToVendorAdmin(jurisdiction)) {
                boundaries.add(jurisdiction.getBoundary());
            }
        }
        return boundaries;
    }

    private boolean appliesToVendorAdmin(Jurisdiction jurisdiction) {
        List<org.egov.web.models.Role> roles = jurisdiction.getRoles();
        if (roles == null || roles.isEmpty()) {
            return true;
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .map(org.egov.web.models.Role::getCode)
                .filter(Objects::nonNull)
                .anyMatch(VENDOR_ADMIN::equalsIgnoreCase);
    }
}
