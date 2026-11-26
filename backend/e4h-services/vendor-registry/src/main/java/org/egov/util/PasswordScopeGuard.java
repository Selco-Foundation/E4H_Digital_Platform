package org.egov.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.repository.OrganisationUserRepository;
import org.egov.tracer.model.CustomException;
import org.egov.web.models.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static org.egov.util.OrganisationConstant.ORG_PLATFORM_ADMIN_ROLE_CODE;
import static org.egov.util.OrganisationConstant.VENDOR_ADMIN;

/**
 * Who may set whose password.
 *
 * | Caller               | May set the password of                                      |
 * |----------------------|--------------------------------------------------------------|
 * | ORG_PLATFORM_ADMIN   | anyone                                                       |
 * | ORG_ADMIN            | users in their own org, never an ORG_PLATFORM_ADMIN          |
 * | VENDOR_ADMIN         | users in their own Vendor Org, never an ORG_PLATFORM_ADMIN   |
 * | anyone else          | no one                                                       |
 *
 * A password rides on the ordinary create and update calls, and every create sets one, so this
 * is checked on each of them rather than trusted to the UI. The current password is never
 * required: the scope is what makes a reset safe.
 *
 * VENDOR_ADMIN is otherwise scoped by jurisdiction ({@link JurisdictionScopeResolver}). That
 * still applies; this adds the organisation on top for anything that sets a password, because a
 * jurisdiction can span many vendor orgs - a vendor PoC admin's spans the whole country.
 *
 * **This fails closed.** A restricted caller whose own organisation cannot be resolved to exactly
 * one active org is refused.
 */
@Component
@Slf4j
public class PasswordScopeGuard {

    static final String VENDOR_ORG_TYPE = "VENDOR";

    private final OrgAdminScopeResolver orgAdminScopeResolver;
    private final OrganisationUserRepository userRepository;

    @Autowired
    public PasswordScopeGuard(OrgAdminScopeResolver orgAdminScopeResolver, OrganisationUserRepository userRepository) {
        this.orgAdminScopeResolver = orgAdminScopeResolver;
        this.userRepository = userRepository;
    }

    /**
     * Refuses unless the caller may set the password of a user in `targetOrgId`.
     *
     * @param targetUserUuid the existing user whose password is set, or null for a user being
     *                       created. An existing user must already be an active member of the
     *                       caller's org - naming them in a request for that org is not enough.
     * @param targetRoles    the roles the user holds as stored (or is being given, on create)
     */
    public void require(RequestInfo requestInfo, String targetOrgId, String targetUserUuid, List<Role> targetRoles) {
        if (OrgAdminScopeResolver.holdsRole(requestInfo, ORG_PLATFORM_ADMIN_ROLE_CODE)) {
            return;
        }
        if (holds(targetRoles, ORG_PLATFORM_ADMIN_ROLE_CODE)) {
            refuse("Only a platform admin can set the password of a platform admin", targetUserUuid);
        }
        String ownOrg;
        if (OrgAdminScopeResolver.isOrgAdmin(requestInfo)) {
            ownOrg = orgAdminScopeResolver.resolve(requestInfo).orgId();
        } else if (OrgAdminScopeResolver.holdsRole(requestInfo, VENDOR_ADMIN)) {
            ownOrg = resolveVendorOrg(requestInfo);
        } else {
            refuse("You are not allowed to set passwords", targetUserUuid);
            return;
        }
        if (!Objects.equals(ownOrg, targetOrgId)) {
            refuse("You can only set passwords for users in your own organisation", targetUserUuid);
        }
        if (targetUserUuid != null && userRepository.getActiveOrgLinksForUser(targetUserUuid).stream()
                .noneMatch(link -> ownOrg.equals(link.organizationId()))) {
            refuse("You can only set passwords for users in your own organisation", targetUserUuid);
        }
    }

    private String resolveVendorOrg(RequestInfo requestInfo) {
        String callerUuid = requestInfo.getUserInfo() == null ? null : requestInfo.getUserInfo().getUuid();
        if (StringUtils.isBlank(callerUuid)) {
            throw new CustomException("VENDOR_ADMIN_UNRESOLVED", "User UUID is required to resolve the caller's organisation");
        }
        List<OrganisationUserRepository.UserOrgLink> links = userRepository.getActiveOrgLinksForUser(callerUuid);
        List<String> orgIds = links.stream().map(OrganisationUserRepository.UserOrgLink::organizationId).distinct().toList();
        if (orgIds.size() != 1) {
            log.error("VENDOR_ADMIN {} is linked to {} active organisations; refusing", callerUuid, orgIds.size());
            throw new CustomException("VENDOR_ADMIN_UNRESOLVED", "Your account is not linked to exactly one organisation");
        }
        OrganisationUserRepository.UserOrgLink link = links.get(0);
        if (!VENDOR_ORG_TYPE.equals(StringUtils.trim(link.orgType()))) {
            log.error("VENDOR_ADMIN {} belongs to organisation {} of type {}, not a Vendor Org; refusing",
                    callerUuid, link.organizationId(), link.orgType());
            throw new CustomException("VENDOR_ADMIN_UNRESOLVED", "The vendor admin role is only valid in a Vendor organisation");
        }
        return link.organizationId();
    }

    private static boolean holds(List<Role> roles, String code) {
        return roles != null && roles.stream().filter(Objects::nonNull).map(Role::getCode).anyMatch(code::equalsIgnoreCase);
    }

    private static void refuse(String message, String targetUserUuid) {
        log.error("Password set refused for user {}: {}", targetUserUuid, message);
        throw new CustomException("PASSWORD_SET_OUT_OF_SCOPE", message);
    }
}
