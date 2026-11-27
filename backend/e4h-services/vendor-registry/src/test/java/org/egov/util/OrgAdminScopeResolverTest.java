package org.egov.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.repository.OrganisationUserRepository;
import org.egov.repository.OrganisationUserRepository.UserOrgLink;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ORG_ADMIN is bound to one Platform Org. The failures that matter here are the silent ones:
 * an unresolvable caller treated as unrestricted, or ORG_PLATFORM_ADMIN read as ORG_ADMIN.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrgAdminScopeResolverTest {

    private static final String CALLER = "caller-uuid";

    @Mock
    private OrganisationUserRepository userRepository;

    private OrgAdminScopeResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new OrgAdminScopeResolver(userRepository);
    }

    private RequestInfo caller(String... roleCodes) {
        List<Role> roles = Arrays.stream(roleCodes).map(code -> Role.builder().code(code).build()).toList();
        return RequestInfo.builder().userInfo(User.builder().uuid(CALLER).roles(roles).build()).build();
    }

    @Test
    void platformAdminIsUnrestrictedAndNeverLooksUpAnOrg() {
        assertFalse(resolver.resolve(caller("ORG_PLATFORM_ADMIN")).restricted());
        verifyNoInteractions(userRepository);
    }

    @Test
    void platformAdminWinsWhenBothRolesAreHeld() {
        assertFalse(resolver.resolve(caller("ORG_ADMIN", "ORG_PLATFORM_ADMIN")).restricted());
    }

    @Test
    void otherCallersAreNotRestrictedOnThisAxis() {
        assertFalse(resolver.resolve(caller("PROJECT_MANAGER")).restricted());
        verifyNoInteractions(userRepository);
    }

    @Test
    void vendorAdminIsBoundToTheirVendorOrg() {
        when(userRepository.getActiveOrgLinksForUser(CALLER)).thenReturn(List.of(new UserOrgLink("vendor-1", "VENDOR")));
        OrgAdminScopeResolver.OrgScope scope = resolver.resolve(caller("VENDOR_ADMIN"));
        assertTrue(scope.restricted());
        assertTrue(scope.allows("vendor-1"));
        assertFalse(scope.allows("vendor-2"));
    }

    @Test
    void vendorAdminInAPlatformOrgIsRefused() {
        when(userRepository.getActiveOrgLinksForUser(CALLER)).thenReturn(List.of(new UserOrgLink("org-1", "PLATFORM")));
        CustomException e = assertThrows(CustomException.class, () -> resolver.resolve(caller("VENDOR_ADMIN")));
        assertEquals("VENDOR_ADMIN_UNRESOLVED", e.getCode());
    }

    @Test
    void vendorAdminWithNoOrSeveralOrgsIsRefused() {
        when(userRepository.getActiveOrgLinksForUser(CALLER)).thenReturn(List.of());
        assertThrows(CustomException.class, () -> resolver.resolve(caller("VENDOR_ADMIN")));
        when(userRepository.getActiveOrgLinksForUser(CALLER)).thenReturn(List.of(
                new UserOrgLink("vendor-1", "VENDOR"), new UserOrgLink("vendor-2", "VENDOR")));
        assertThrows(CustomException.class, () -> resolver.resolve(caller("VENDOR_ADMIN")));
    }

    @Test
    void platformAdminWinsOverVendorAdmin() {
        assertFalse(resolver.resolve(caller("VENDOR_ADMIN", "ORG_PLATFORM_ADMIN")).restricted());
        verifyNoInteractions(userRepository);
    }

    @Test
    void orgAdminWinsOverVendorAdmin() {
        when(userRepository.getActiveOrgLinksForUser(CALLER)).thenReturn(List.of(new UserOrgLink("org-1", "PLATFORM")));
        assertEquals("org-1", resolver.resolve(caller("VENDOR_ADMIN", "ORG_ADMIN")).orgId());
    }

    @Test
    void orgAdminIsBoundToTheirPlatformOrg() {
        when(userRepository.getActiveOrgLinksForUser(CALLER)).thenReturn(List.of(new UserOrgLink("org-1", "PLATFORM")));
        OrgAdminScopeResolver.OrgScope scope = resolver.resolve(caller("ORG_ADMIN"));
        assertTrue(scope.restricted());
        assertEquals("org-1", scope.orgId());
        assertTrue(scope.allows("org-1"));
        assertFalse(scope.allows("org-2"));
    }

    @Test
    void orgAdminWithNoOrgIsRefused() {
        when(userRepository.getActiveOrgLinksForUser(CALLER)).thenReturn(List.of());
        assertThrows(CustomException.class, () -> resolver.resolve(caller("ORG_ADMIN")));
    }

    @Test
    void orgAdminInSeveralOrgsIsRefused() {
        when(userRepository.getActiveOrgLinksForUser(CALLER)).thenReturn(List.of(
                new UserOrgLink("org-1", "PLATFORM"), new UserOrgLink("org-2", "PLATFORM")));
        assertThrows(CustomException.class, () -> resolver.resolve(caller("ORG_ADMIN")));
    }

    @Test
    void orgAdminInAVendorOrgIsRefused() {
        when(userRepository.getActiveOrgLinksForUser(CALLER)).thenReturn(List.of(new UserOrgLink("org-1", "VENDOR")));
        assertThrows(CustomException.class, () -> resolver.resolve(caller("ORG_ADMIN")));
    }

    @Test
    void narrowKeepsOnlyTheCallersOrg() {
        OrgAdminScopeResolver.OrgScope scope = new OrgAdminScopeResolver.OrgScope("org-1");
        assertEquals(List.of("org-1"), scope.narrow(null));
        assertEquals(List.of("org-1"), scope.narrow(List.of()));
        assertEquals(List.of("org-1"), scope.narrow(List.of("org-1", "org-2")));
        assertTrue(scope.narrow(List.of("org-2")).isEmpty());
    }

    @Test
    void unrestrictedNarrowLeavesTheRequestAlone() {
        List<String> requested = List.of("org-2");
        assertSame(requested, OrgAdminScopeResolver.OrgScope.unrestricted().narrow(requested));
    }
}
