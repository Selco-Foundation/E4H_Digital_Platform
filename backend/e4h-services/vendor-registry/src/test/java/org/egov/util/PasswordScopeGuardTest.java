package org.egov.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.repository.OrganisationUserRepository;
import org.egov.repository.OrganisationUserRepository.UserOrgLink;
import org.egov.tracer.model.CustomException;
import org.egov.web.models.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Each admin type against an in-scope and an out-of-scope target (MH2-06). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PasswordScopeGuardTest {

    @Mock private OrgAdminScopeResolver orgAdminScopeResolver;
    @Mock private OrganisationUserRepository userRepository;

    @InjectMocks
    private PasswordScopeGuard guard;

    private static RequestInfo caller(String... roleCodes) {
        return RequestInfo.builder().userInfo(org.egov.common.contract.request.User.builder().uuid("caller")
                .roles(Arrays.stream(roleCodes)
                        .map(c -> org.egov.common.contract.request.Role.builder().code(c).build()).toList())
                .build()).build();
    }

    private static List<Role> roles(String... codes) {
        return Arrays.stream(codes).map(c -> Role.builder().code(c).build()).toList();
    }

    private void callerIn(String orgId, String orgType) {
        when(userRepository.getActiveOrgLinksForUser("caller")).thenReturn(List.of(new UserOrgLink(orgId, orgType)));
    }

    private void targetIn(String uuid, String orgId) {
        when(userRepository.getActiveOrgLinksForUser(uuid)).thenReturn(List.of(new UserOrgLink(orgId, "VENDOR")));
    }

    private String refusal(RequestInfo requestInfo, String orgId, String uuid, List<Role> targetRoles) {
        return assertThrows(CustomException.class, () -> guard.require(requestInfo, orgId, uuid, targetRoles)).getCode();
    }

    // ORG_PLATFORM_ADMIN

    @Test
    void platformAdminMaySetAnyonesPasswordIncludingAnotherPlatformAdmin() {
        assertDoesNotThrow(() -> guard.require(caller("ORG_PLATFORM_ADMIN"), "any-org", "u1", roles("ORG_PLATFORM_ADMIN")));
        verifyNoInteractions(userRepository, orgAdminScopeResolver);
    }

    @Test
    void platformAdminWinsOverAlsoHoldingOrgAdmin() {
        assertDoesNotThrow(() -> guard.require(caller("ORG_ADMIN", "ORG_PLATFORM_ADMIN"), "other-org", "u1", roles("EMPLOYEE")));
    }

    // ORG_ADMIN

    @Test
    void orgAdminMaySetPasswordsInTheirOwnOrg() {
        when(orgAdminScopeResolver.resolve(any())).thenReturn(new OrgAdminScopeResolver.OrgScope("org-A"));
        targetIn("u1", "org-A");
        assertDoesNotThrow(() -> guard.require(caller("ORG_ADMIN"), "org-A", "u1", roles("ORG_ADMIN")));
    }

    @Test
    void orgAdminIsRefusedOutsideTheirOrg() {
        when(orgAdminScopeResolver.resolve(any())).thenReturn(new OrgAdminScopeResolver.OrgScope("org-A"));
        assertEquals("PASSWORD_SET_OUT_OF_SCOPE", refusal(caller("ORG_ADMIN"), "org-B", "u1", roles("EMPLOYEE")));
    }

    @Test
    void orgAdminIsRefusedForAPlatformAdminEvenInTheirOwnOrg() {
        when(orgAdminScopeResolver.resolve(any())).thenReturn(new OrgAdminScopeResolver.OrgScope("org-A"));
        targetIn("u1", "org-A");
        assertEquals("PASSWORD_SET_OUT_OF_SCOPE", refusal(caller("ORG_ADMIN"), "org-A", "u1", roles("ORG_PLATFORM_ADMIN")));
    }

    @Test
    void orgAdminIsRefusedForAUserNotActuallyInTheirOrg() {
        when(orgAdminScopeResolver.resolve(any())).thenReturn(new OrgAdminScopeResolver.OrgScope("org-A"));
        targetIn("u1", "org-B");
        assertEquals("PASSWORD_SET_OUT_OF_SCOPE", refusal(caller("ORG_ADMIN"), "org-A", "u1", roles("EMPLOYEE")));
    }

    // VENDOR_ADMIN

    @Test
    void vendorAdminMaySetPasswordsInTheirOwnVendorOrg() {
        callerIn("vendor-A", "VENDOR");
        targetIn("u1", "vendor-A");
        assertDoesNotThrow(() -> guard.require(caller("VENDOR_ADMIN"), "vendor-A", "u1", roles("EMPLOYEE")));
    }

    @Test
    void vendorAdminMayCreateUsersInTheirOwnVendorOrg() {
        callerIn("vendor-A", "VENDOR");
        assertDoesNotThrow(() -> guard.require(caller("VENDOR_ADMIN"), "vendor-A", null, roles("EMPLOYEE")));
    }

    @Test
    void vendorAdminIsRefusedInAnotherVendorOrg() {
        callerIn("vendor-A", "VENDOR");
        assertEquals("PASSWORD_SET_OUT_OF_SCOPE", refusal(caller("VENDOR_ADMIN"), "vendor-B", "u1", roles("EMPLOYEE")));
    }

    @Test
    void vendorAdminIsRefusedForAPlatformAdmin() {
        callerIn("vendor-A", "VENDOR");
        targetIn("u1", "vendor-A");
        assertEquals("PASSWORD_SET_OUT_OF_SCOPE", refusal(caller("VENDOR_ADMIN"), "vendor-A", "u1", roles("ORG_PLATFORM_ADMIN")));
    }

    @Test
    void vendorAdminOutsideAVendorOrgIsRefused() {
        callerIn("platform-A", "PLATFORM");
        assertEquals("VENDOR_ADMIN_UNRESOLVED", refusal(caller("VENDOR_ADMIN"), "platform-A", "u1", roles("EMPLOYEE")));
    }

    @Test
    void vendorAdminWithNoOrMoreThanOneOrgIsRefused() {
        when(userRepository.getActiveOrgLinksForUser("caller")).thenReturn(List.of());
        assertEquals("VENDOR_ADMIN_UNRESOLVED", refusal(caller("VENDOR_ADMIN"), "vendor-A", null, roles()));
        when(userRepository.getActiveOrgLinksForUser("caller"))
                .thenReturn(List.of(new UserOrgLink("vendor-A", "VENDOR"), new UserOrgLink("vendor-B", "VENDOR")));
        assertEquals("VENDOR_ADMIN_UNRESOLVED", refusal(caller("VENDOR_ADMIN"), "vendor-A", null, roles()));
    }

    // Everyone else

    @Test
    void aCallerWithNoAdminRoleMaySetNoPasswords() {
        assertEquals("PASSWORD_SET_OUT_OF_SCOPE", refusal(caller("EMPLOYEE"), "vendor-A", null, roles()));
    }
}
