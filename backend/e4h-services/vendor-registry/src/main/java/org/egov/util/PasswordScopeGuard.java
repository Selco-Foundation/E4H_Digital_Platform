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
 * The caller's own organisation comes from {@link OrgAdminScopeResolver}, which restricts both
 * ORG_ADMIN and VENDOR_ADMIN to one org for every org-user operation, not only passwords.
 *
 * **This fails closed.** A restricted caller whose own organisation cannot be resolved to exactly
 * one active org is refused.
 */
@Component
@Slf4j
public class PasswordScopeGuard {

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
        if (!OrgAdminScopeResolver.isOrgRestricted(requestInfo)) {
            refuse("You are not allowed to set passwords", targetUserUuid);
            return;
        }
        String ownOrg = orgAdminScopeResolver.resolve(requestInfo).orgId();
        if (!Objects.equals(ownOrg, targetOrgId)) {
            refuse("You can only set passwords for users in your own organisation", targetUserUuid);
        }
        if (targetUserUuid != null && userRepository.getActiveOrgLinksForUser(targetUserUuid).stream()
                .noneMatch(link -> ownOrg.equals(link.organizationId()))) {
            refuse("You can only set passwords for users in your own organisation", targetUserUuid);
        }
    }

    private static boolean holds(List<Role> roles, String code) {
        return roles != null && roles.stream().filter(Objects::nonNull).map(Role::getCode).anyMatch(code::equalsIgnoreCase);
    }

    private static void refuse(String message, String targetUserUuid) {
        log.error("Password set refused for user {}: {}", targetUserUuid, message);
        throw new CustomException("PASSWORD_SET_OUT_OF_SCOPE", message);
    }
}
