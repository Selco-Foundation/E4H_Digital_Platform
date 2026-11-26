package org.egov.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.user.UserDetailResponse;
import org.egov.config.Configuration;
import org.egov.kafka.OrganizationProducer;
import org.egov.repository.OrganisationUserRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.HRMSUtils;
import org.egov.util.OrganisationUtil;
import org.egov.util.UserUtil;
import org.egov.validator.OrganisationUserServiceValidator;
import org.egov.web.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PocAdminServiceTest {

    @Mock private OrganisationUserServiceValidator userValidator;
    @Mock private OrganisationUserEnrichmentService userEnrichment;
    @Mock private OrganisationUserRepository orgUserRepository;
    @Mock private OrganizationProducer producer;
    @Mock private Configuration config;
    @Mock private HRMSUtils hrmsUtils;
    @Mock private UserUtil userUtil;
    @Mock private OrganisationUtil organisationUtil;
    @Mock private CredentialsNotificationService credentialsNotificationService;
    @Mock private org.egov.util.PasswordScopeGuard passwordScopeGuard;

    @InjectMocks
    private PocAdminService service;

    private final RequestInfo requestInfo = RequestInfo.builder().userInfo(User.builder().uuid("caller").build()).build();

    @BeforeEach
    void setUp() {
        lenient().when(config.getUserHost()).thenReturn("http://user");
        lenient().when(config.getUserSearchEndpoint()).thenReturn("/user/_search");
        lenient().when(config.getCreateOrgUserTopic()).thenReturn("save-org-user");
        lenient().when(config.getPocChangeAuditTopic()).thenReturn("save-org-poc-change-audit");
        lenient().when(organisationUtil.encryptMobileNumber(anyString())).thenAnswer(i -> "enc:" + i.getArgument(0));
        lenient().when(userUtil.userCall(any(), any())).thenReturn(new UserDetailResponse());
    }

    private Organisation org(String type, String name, String phone, String email, String userName) {
        return Organisation.builder().id("org-1").tenantId("in").name("Sunrise").orgType(type)
                .orgPocName(name).orgPocPhone(phone).orgPocEmail(email).orgPocUsername(userName).build();
    }

    private Employee employee(String uuid, String userName, Role... roles) {
        org.egov.web.models.User user = org.egov.web.models.User.builder().uuid(uuid).userName(userName)
                .name(userName).emailId(userName + "@x.org").roles(new ArrayList<>(List.of(roles))).build();
        return Employee.builder().user(user).jurisdictions(new ArrayList<>()).build();
    }

    private Role role(String code) {
        return Role.builder().code(code).name(code).build();
    }

    @Test
    void adminRoleFollowsOrgType() {
        assertEquals("VENDOR_ADMIN", PocAdminService.adminRoleFor("VENDOR").getCode());
        assertEquals("ORG_ADMIN", PocAdminService.adminRoleFor("PLATFORM").getCode());
    }

    @Test
    void resendingTheSamePocIsNotAChange() {
        Organisation stored = org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh");
        Organisation incoming = org("VENDOR", " Ramesh ", "9876543210", "R@X.org", "RAMESH");
        incoming.setOrgPocPassword("abc1@de");
        assertFalse(PocAdminService.pocChanged(incoming, stored));
        incoming.setOrgPocEmail("other@x.org");
        assertTrue(PocAdminService.pocChanged(incoming, stored));
    }

    @Test
    void createRefusesAPocWhoseEmailBelongsToAnotherUser() {
        UserDetailResponse found = mock(UserDetailResponse.class);
        when(found.getUser()).thenReturn(List.of(User.builder().uuid("someone").build()));
        when(userUtil.userCall(any(), any())).thenReturn(found);

        PocAdminService.PocDetails poc = PocAdminService.PocDetails.of(org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh"));
        CustomException e = assertThrows(CustomException.class,
                () -> service.checkCreateConflicts(requestInfo, List.of(poc)));
        assertTrue(e.getErrors().containsKey("ORG_POC_EMAIL_EXISTS"));
    }

    @Test
    void createRefusesAnExistingUsernameAndContactNamingBoth() {
        when(hrmsUtils.getUserByUsername(any(), eq("ramesh"))).thenReturn(List.of(employee("u1", "ramesh")));
        when(hrmsUtils.getUserByPhoneNumber(any(), eq("9876543210"))).thenReturn(List.of(employee("u2", "other")));

        PocAdminService.PocDetails poc = PocAdminService.PocDetails.of(org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh"));
        CustomException e = assertThrows(CustomException.class,
                () -> service.checkCreateConflicts(requestInfo, List.of(poc)));
        assertTrue(e.getErrors().containsKey("ORG_POC_USERNAME_EXISTS"));
        assertTrue(e.getErrors().containsKey("ORG_POC_PHONE_EXISTS"));
    }

    @Test
    void platformOrgPocBecomesACountryWideOrgAdmin() {
        Organisation organisation = org("PLATFORM", "Ramesh", "9876543210", "r@x.org", "ramesh");
        when(userValidator.createPocAdmin(any(), eq(organisation))).thenAnswer(i -> {
            OrgUserRequest r = i.getArgument(0);
            r.setUser(org.egov.web.models.User.builder().uuid("new-uuid").userName("ramesh")
                    .name("Ramesh").emailId("r@x.org").build());
            return "Rame@3210";
        });

        List<PocAdminService.Pending> pending = service.createAdmins(requestInfo, List.of(organisation),
                List.of(PocAdminService.PocDetails.of(organisation)));

        ArgumentCaptor<OrgUserRequest> sent = ArgumentCaptor.forClass(OrgUserRequest.class);
        verify(userValidator).createPocAdmin(sent.capture(), eq(organisation));
        // Captured after the call replaced the user, so check what the result reports instead.
        assertEquals("org-1", sent.getValue().getOrganizationId());
        assertEquals("ORG_ADMIN", pending.get(0).result().getRole());
        assertEquals("new-uuid", pending.get(0).result().getUserId());
        verifyNoInteractions(producer, credentialsNotificationService);
    }

    @Test
    void theAdminIsGrantedTheRoleAtCountryLevel() {
        Organisation organisation = org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh");
        List<org.egov.web.models.User> seen = new ArrayList<>();
        when(userValidator.createPocAdmin(any(), any())).thenAnswer(i -> {
            OrgUserRequest r = i.getArgument(0);
            seen.add(r.getUser());
            r.setUser(org.egov.web.models.User.builder().uuid("new-uuid").userName("ramesh").build());
            return "Rame@3210";
        });

        service.createAdmins(requestInfo, List.of(organisation), List.of(PocAdminService.PocDetails.of(organisation)));

        org.egov.web.models.User requested = seen.get(0);
        assertEquals(List.of("VENDOR_ADMIN"), requested.getRoles().stream().map(Role::getCode).toList());
        Jurisdiction jurisdiction = requested.getJurisdictions().get(0);
        assertEquals("India", jurisdiction.getBoundary());
        assertEquals(List.of("VENDOR_ADMIN"), jurisdiction.getRoles().stream().map(Role::getCode).toList());
    }

    @Test
    void publishLinksTheUserThenEmailsThem() {
        Organisation organisation = org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh");
        when(userValidator.createPocAdmin(any(), any())).thenAnswer(i -> {
            OrgUserRequest r = i.getArgument(0);
            r.setUser(org.egov.web.models.User.builder().uuid("new-uuid").userName("ramesh").emailId("r@x.org").build());
            return "Rame@3210";
        });
        when(credentialsNotificationService.send(any())).thenReturn(false);
        List<PocAdminService.Pending> pending = service.createAdmins(requestInfo, List.of(organisation),
                List.of(PocAdminService.PocDetails.of(organisation)));

        List<PocAdmin> results = service.publish(requestInfo, pending);

        verify(producer).push(eq("save-org-user"), any(OrgUserRequest.class));
        ArgumentCaptor<CredentialsNotificationService.Credentials> mail = ArgumentCaptor.forClass(CredentialsNotificationService.Credentials.class);
        verify(credentialsNotificationService).send(mail.capture());
        assertEquals("Rame@3210", mail.getValue().password());
        assertFalse(mail.getValue().reset());
        assertEquals(Boolean.FALSE, results.get(0).getCredentialsEmailSent(), "a failed email is reported, not thrown");
    }

    @Test
    void changingThePocToANewPersonCreatesThemAndRevokesThePreviousAdmin() {
        Organisation stored = org("VENDOR", "Old", "9000000000", "old@x.org", "old");
        Organisation incoming = org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh");
        Employee previous = employee("old-uuid", "old", role("VENDOR_ADMIN"), role("EMPLOYEE"));
        previous.getJurisdictions().add(Jurisdiction.builder().boundary("India").isActive(true)
                .roles(new ArrayList<>(List.of(role("VENDOR_ADMIN")))).build());
        when(hrmsUtils.getUserByUsername(any(), eq("old"))).thenReturn(List.of(previous));
        when(orgUserRepository.getOrgUsers(any(), any()))
                .thenReturn(List.of(OrgUser.builder().organizationId("org-1").isDeleted(false).build()));
        when(hrmsUtils.updateHRMSUser(any())).thenReturn(List.of(previous));
        when(userValidator.createPocAdmin(any(), any())).thenAnswer(i -> {
            OrgUserRequest r = i.getArgument(0);
            r.setUser(org.egov.web.models.User.builder().uuid("new-uuid").userName("ramesh").build());
            return "Rame@3210";
        });

        PocAdminService.PocChangeResult change = service.applyChange(requestInfo, incoming, stored);

        assertEquals(List.of("EMPLOYEE"), previous.getUser().getRoles().stream().map(Role::getCode).toList());
        Jurisdiction country = previous.getJurisdictions().get(0);
        assertFalse(country.getIsActive(), "an admin-only jurisdiction is deactivated, not widened to every role");
        assertEquals("old-uuid", change.audit().getPreviousUserId());
        assertEquals("new-uuid", change.audit().getNewUserId());
        assertEquals("caller", change.audit().getChangedBy());
        assertEquals("enc:9000000000", change.audit().getOldPocPhone());
        assertEquals("enc:9876543210", change.audit().getNewPocPhone());
    }

    @Test
    void editingTheSamePocsDetailsKeepsTheirRoleAndPassword() {
        Organisation stored = org("VENDOR", "Ramesh", "9876543210", "old@x.org", "ramesh");
        Organisation incoming = org("VENDOR", "Ramesh K", "9876543210", "new@x.org", "ramesh");
        Employee same = employee("u1", "ramesh", role("VENDOR_ADMIN"));
        when(hrmsUtils.getUserByUsername(any(), eq("ramesh"))).thenReturn(List.of(same));
        when(hrmsUtils.getUserByPhoneNumber(any(), eq("9876543210"))).thenReturn(List.of(same));
        when(hrmsUtils.updateHRMSUser(any())).thenAnswer(i -> ((EmployeeRequest) i.getArgument(0)).getEmployees());
        when(orgUserRepository.getOrgUsers(any(), any())).thenReturn(List.of());
        when(orgUserRepository.ensureActiveOrgUserLinkOrReactivateDeleted("u1", "org-1")).thenReturn(Optional.of("link"));

        PocAdminService.PocChangeResult change = service.applyChange(requestInfo, incoming, stored);

        assertEquals("Ramesh K", same.getUser().getName());
        assertEquals("new@x.org", same.getUser().getEmailId());
        assertEquals(List.of("VENDOR_ADMIN"), same.getUser().getRoles().stream().map(Role::getCode).toList());
        verify(userValidator, never()).setPassword(any(), any(), any());
        verify(hrmsUtils, times(1)).updateHRMSUser(any());
        assertEquals("u1", change.audit().getPreviousUserId());
        assertEquals("u1", change.audit().getNewUserId());
    }

    @Test
    void pointingToSomeoneInAnotherOrgIsRefused() {
        Organisation stored = org("VENDOR", "Old", "9000000000", "old@x.org", "old");
        Organisation incoming = org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh");
        lenient().when(hrmsUtils.getUserByUsername(any(), eq("ramesh"))).thenReturn(List.of(employee("u2", "ramesh")));
        when(orgUserRepository.getOrgUsers(any(), any()))
                .thenReturn(List.of(OrgUser.builder().organizationId("org-2").isDeleted(false).build()));

        CustomException e = assertThrows(CustomException.class, () -> service.applyChange(requestInfo, incoming, stored));
        assertEquals("ORG_POC_IN_OTHER_ORG", e.getCode());
        verify(hrmsUtils, never()).updateHRMSUser(any());
    }

    @Test
    void aPreviousPocNoLongerInTheOrgKeepsTheirRole() {
        Organisation stored = org("VENDOR", "Old", "9000000000", "old@x.org", "old");
        Organisation incoming = org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh");
        Employee previous = employee("old-uuid", "old", role("VENDOR_ADMIN"));
        when(hrmsUtils.getUserByUsername(any(), eq("old"))).thenReturn(List.of(previous));
        when(orgUserRepository.getOrgUsers(any(), any()))
                .thenReturn(List.of(OrgUser.builder().organizationId("org-2").isDeleted(false).build()));
        when(userValidator.createPocAdmin(any(), any())).thenAnswer(i -> {
            OrgUserRequest r = i.getArgument(0);
            r.setUser(org.egov.web.models.User.builder().uuid("new-uuid").userName("ramesh").build());
            return "Rame@3210";
        });

        service.applyChange(requestInfo, incoming, stored);

        assertEquals(List.of("VENDOR_ADMIN"), previous.getUser().getRoles().stream().map(Role::getCode).toList());
        verify(hrmsUtils, never()).updateHRMSUser(any());
    }

    @Test
    void anExistingAdminKeepsTheirOwnScopeWhenMadePoc() {
        Employee e = employee("u1", "ramesh", role("VENDOR_ADMIN"));
        e.getJurisdictions().add(Jurisdiction.builder().boundary("KA").isActive(true).build());

        PocAdminService.grantAdminRole(e, PocAdminService.adminRoleFor("VENDOR"));

        assertEquals(List.of("KA"), e.getJurisdictions().stream().map(Jurisdiction::getBoundary).toList());
    }

    @Test
    void samePocWithAPasswordResetsThePocsPassword() {
        Organisation stored = org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh");
        Organisation incoming = org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh");
        incoming.setOrgPocPassword("newpass1!");
        Employee poc = employee("u1", "ramesh", role("VENDOR_ADMIN"));
        when(hrmsUtils.getUserByUsername(any(), eq("ramesh"))).thenReturn(List.of(poc));

        PocAdminService.PocChangeResult reset = service.resetPassword(requestInfo, incoming, stored);

        verify(passwordScopeGuard).require(requestInfo, "org-1", "u1", poc.getUser().getRoles());
        verify(userValidator).setPassword(requestInfo, poc.getUser(), "newpass1!");
        assertNull(reset.audit(), "a reset does not change the PoC, so nothing is audited");
        when(credentialsNotificationService.send(any())).thenReturn(true);
        PocAdmin result = service.publishChange(requestInfo, reset);
        ArgumentCaptor<CredentialsNotificationService.Credentials> mail = ArgumentCaptor.forClass(CredentialsNotificationService.Credentials.class);
        verify(credentialsNotificationService).send(mail.capture());
        assertTrue(mail.getValue().reset());
        assertEquals(Boolean.TRUE, result.getCredentialsEmailSent());
        verify(producer, never()).push(eq("save-org-poc-change-audit"), any());
    }

    @Test
    void aPocResetOutOfScopeSetsNothing() {
        Organisation stored = org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh");
        Organisation incoming = org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh");
        incoming.setOrgPocPassword("newpass1!");
        when(hrmsUtils.getUserByUsername(any(), eq("ramesh"))).thenReturn(List.of(employee("u1", "ramesh")));
        doThrow(new CustomException("PASSWORD_SET_OUT_OF_SCOPE", "no")).when(passwordScopeGuard).require(any(), any(), any(), any());

        assertThrows(CustomException.class, () -> service.resetPassword(requestInfo, incoming, stored));
        verify(userValidator, never()).setPassword(any(), any(), any());
    }

    @Test
    void aPocResetChecksThePasswordPolicyFirst() {
        Organisation incoming = org("VENDOR", "Ramesh", "9876543210", "r@x.org", "ramesh");
        incoming.setOrgPocPassword("password");
        CustomException e = assertThrows(CustomException.class, () -> service.resetPassword(requestInfo, incoming, incoming));
        assertEquals("INVALID_PWD_PATTERN", e.getCode());
        verifyNoInteractions(hrmsUtils);
    }

    @Test
    void grantingAddsToAnExistingScopedCountryJurisdiction() {
        Employee e = employee("u1", "ramesh", role("PROJECT_MANAGER"));
        e.getJurisdictions().add(Jurisdiction.builder().boundary("India").isActive(true)
                .roles(new ArrayList<>(List.of(role("PROJECT_MANAGER")))).build());

        PocAdminService.grantAdminRole(e, PocAdminService.adminRoleFor("VENDOR"));

        assertEquals(1, e.getJurisdictions().size());
        assertEquals(List.of("PROJECT_MANAGER", "VENDOR_ADMIN"),
                e.getJurisdictions().get(0).getRoles().stream().map(Role::getCode).toList());
    }
}
