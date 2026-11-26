package org.egov.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.repository.OrganisationUserRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.egov.util.OrganisationConstant.ORG_ADMIN_ROLE_CODE;
import static org.egov.util.OrganisationConstant.ORG_PLATFORM_ADMIN_ROLE_CODE;
import static org.egov.util.OrganisationConstant.PLATFORM_ORG_TYPE;

/**
 * The one organisation an ORG_ADMIN caller may act on.
 *
 * ORG_ADMIN is tied to exactly one Platform Org: it sees that org and its users, and nothing else -
 * not other Platform Orgs, not any Vendor Org. ORG_PLATFORM_ADMIN is unrestricted, and wins when a
 * caller holds both. Every other caller is not restricted on this axis.
 *
 * This is deliberately organisation based, not jurisdiction based. It is a different axis from the
 * VENDOR_ADMIN scoping in {@link JurisdictionScopeResolver}; the two are not combined.
 *
 * **This fails closed.** An ORG_ADMIN whose organisation cannot be resolved to exactly one active
 * Platform Org is refused, never shown everything.
 */
@Component
@Slf4j
public class OrgAdminScopeResolver {

    private static final String REQUEST_CACHE_KEY = OrgAdminScopeResolver.class.getName() + ".scope.";

    private final OrganisationUserRepository userRepository;

    @Autowired
    public OrgAdminScopeResolver(OrganisationUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * What one caller may reach on the organisation axis.
     *
     * `orgId == null` means unrestricted. It is never "no organisation": an ORG_ADMIN with no
     * organisation is refused in {@link #resolve} and never gets a scope at all.
     */
    public record OrgScope(String orgId) {

        public static OrgScope unrestricted() {
            return new OrgScope(null);
        }

        public boolean restricted() {
            return orgId != null;
        }

        public boolean allows(String organisationId) {
            return !restricted() || orgId.equals(organisationId);
        }

        /**
         * Narrows a requested list of organisation ids to what this scope allows.
         *
         * For a restricted scope, an absent list becomes the caller's own org, and a list that does
         * not contain it becomes empty. Callers must answer "nothing" on an empty result rather
         * than pass it on: the query builders read an empty list as "no filter".
         */
        public List<String> narrow(List<String> requested) {
            if (!restricted()) {
                return requested;
            }
            if (requested == null || requested.isEmpty() || requested.contains(orgId)) {
                return List.of(orgId);
            }
            return Collections.emptyList();
        }
    }

    public static boolean holdsRole(RequestInfo requestInfo, String roleCode) {
        if (requestInfo == null || requestInfo.getUserInfo() == null || requestInfo.getUserInfo().getRoles() == null) {
            return false;
        }
        return requestInfo.getUserInfo().getRoles().stream()
                .filter(Objects::nonNull)
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .anyMatch(roleCode::equalsIgnoreCase);
    }

    /** True for a caller restricted to one organisation: ORG_ADMIN without ORG_PLATFORM_ADMIN. */
    public static boolean isOrgAdmin(RequestInfo requestInfo) {
        return holdsRole(requestInfo, ORG_ADMIN_ROLE_CODE) && !holdsRole(requestInfo, ORG_PLATFORM_ADMIN_ROLE_CODE);
    }

    /** Resolved once per HTTP request, then reused by the service and validator calls behind it. */
    public OrgScope resolve(RequestInfo requestInfo) {
        if (!isOrgAdmin(requestInfo)) {
            return OrgScope.unrestricted();
        }

        String callerUuid = requestInfo.getUserInfo().getUuid();
        if (StringUtils.isBlank(callerUuid)) {
            throw new CustomException("ORG_ADMIN_UNRESOLVED", "User UUID is required to resolve the caller's organisation");
        }

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        String cacheKey = REQUEST_CACHE_KEY + callerUuid;
        if (attributes != null && attributes.getAttribute(cacheKey, RequestAttributes.SCOPE_REQUEST) instanceof OrgScope cached) {
            return cached;
        }

        OrgScope scope = new OrgScope(resolveCallerOrg(callerUuid));
        if (attributes != null) {
            attributes.setAttribute(cacheKey, scope, RequestAttributes.SCOPE_REQUEST);
        }
        return scope;
    }

    private String resolveCallerOrg(String callerUuid) {
        List<OrganisationUserRepository.UserOrgLink> links = userRepository.getActiveOrgLinksForUser(callerUuid);
        List<String> orgIds = links.stream().map(OrganisationUserRepository.UserOrgLink::organizationId).distinct().toList();

        if (orgIds.size() != 1) {
            log.error("ORG_ADMIN {} is linked to {} active organisations; refusing", callerUuid, orgIds.size());
            throw new CustomException("ORG_ADMIN_UNRESOLVED",
                    "Your account is not linked to exactly one organisation");
        }

        OrganisationUserRepository.UserOrgLink link = links.get(0);
        if (!PLATFORM_ORG_TYPE.equals(StringUtils.trim(link.orgType()))) {
            log.error("ORG_ADMIN {} belongs to organisation {} of type {}, not a Platform Org; refusing",
                    callerUuid, link.organizationId(), link.orgType());
            throw new CustomException("ORG_ADMIN_UNRESOLVED",
                    "The organisation admin role is only valid in a Platform organisation");
        }
        return link.organizationId();
    }
}
