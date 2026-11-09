package org.egov.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

import java.time.Duration;
import java.util.Collection;
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

    /** Boundaries a caller holds VENDOR_ADMIN at, before expansion. Keyed by caller uuid. */
    private final Cache<String, Set<String>> grantsByCaller;

    /** One boundary's descendants. Boundaries change far more slowly than assignments do. */
    private final Cache<String, Set<String>> codesBeneath;

    @Autowired
    public JurisdictionScopeResolver(HRMSUtils hrmsUtils, BoundaryUtil boundaryUtil, Configuration config) {
        this.hrmsUtils = hrmsUtils;
        this.boundaryUtil = boundaryUtil;
        this.config = config;
        this.grantsByCaller = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(config.getJurisdictionScopeCacheSeconds()))
                .maximumSize(10_000)
                .build();
        this.codesBeneath = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(config.getJurisdictionBoundaryCacheSeconds()))
                .maximumSize(10_000)
                .build();
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

        public boolean covers(String boundaryCode) {
            return unscoped || (StringUtils.isNotBlank(boundaryCode) && codes.contains(boundaryCode));
        }

        /** The jurisdictions of this list that the holder of this scope may see or change. */
        public List<Jurisdiction> within(List<Jurisdiction> jurisdictions) {
            if (jurisdictions == null) {
                return Collections.emptyList();
            }
            if (unscoped) {
                return jurisdictions;
            }
            return jurisdictions.stream()
                    .filter(Objects::nonNull)
                    .filter(jurisdiction -> covers(jurisdiction.getBoundary()))
                    .toList();
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

        Set<String> grants = grantsByCaller.get(callerUuid,
                uuid -> fetchVendorAdminBoundaries(requestInfo, uuid));

        if (grants.isEmpty()) {
            // Every vendor administrator is expected to hold at least one jurisdiction, so this is
            // a data or availability problem rather than a legitimate state. Refusing makes it show
            // up as an empty screen to investigate instead of as silent over-permission.
            log.warn("Caller {} holds no {} jurisdictions; administering no organisation users", callerUuid, VENDOR_ADMIN);
            return Scope.nothing();
        }

        Set<String> scope = new LinkedHashSet<>();
        for (String boundary : grants) {
            scope.addAll(codesBeneath.get(cacheKey(tenantId, boundary),
                    key -> boundaryUtil.codesBeneath(requestInfo, tenantId, List.of(boundary))));
        }

        log.debug("Caller {} administers {} boundaries, expanded from {} jurisdictions",
                callerUuid, scope.size(), grants.size());
        return new Scope(false, scope);
    }

    /** True when every jurisdiction in the list is one this caller may act on. */
    public boolean coversAll(Scope scope, Collection<Jurisdiction> jurisdictions) {
        if (scope.unscoped()) {
            return true;
        }
        if (jurisdictions == null || jurisdictions.isEmpty()) {
            return true;
        }
        return jurisdictions.stream()
                .filter(Objects::nonNull)
                .allMatch(jurisdiction -> scope.covers(jurisdiction.getBoundary()));
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

    private String cacheKey(String tenantId, String boundary) {
        return tenantId + "|" + boundary;
    }
}
