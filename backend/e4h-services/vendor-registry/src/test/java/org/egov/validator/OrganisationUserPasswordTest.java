package org.egov.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.repository.OrganisationRepository;
import org.egov.repository.OrganisationUserRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.FacilityUtil;
import org.egov.util.HRMSUtils;
import org.egov.util.JurisdictionScopeResolver;
import org.egov.util.MDMSUtil;
import org.egov.util.OrganisationUtil;
import org.egov.util.UserUtil;
import org.egov.web.models.CreateUserRequest;
import org.egov.web.models.Employee;
import org.egov.web.models.OrgUser;
import org.egov.web.models.Organisation;
import org.egov.web.models.Role;
import org.egov.web.models.OrgUserRequest;
import org.egov.web.models.User;
import org.egov.web.models.UserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * A supplied password must meet the policy, and the first-login marker is set with every password.
 */
@ExtendWith(MockitoExtension.class)
class OrganisationUserPasswordTest {

    @Mock private MDMSUtil mdmsUtil;
    @Mock private Configuration configuration;
    @Mock private OrganisationRepository organisationRepository;
    @Mock private OrganisationUtil organisationUtil;
    @Mock private HRMSUtils hrmsUtils;
    @Mock private UserUtil userUtil;
    @Mock private OrganisationUserRepository userRepository;
    @Mock private FacilityUtil facilityUtil;
    @Mock private ObjectMapper mapper;
    @Mock private JurisdictionScopeResolver jurisdictionScopeResolver;

    @Mock private org.egov.util.PasswordScopeGuard passwordScopeGuard;


    @InjectMocks
    private OrganisationUserServiceValidator validator;

    private User createPayload(String password) {
        return User.builder().tenantId("in").name("n").userName("u").mobileNumber("9999999999")
                .emailId("a@b.c").password(password).roles(List.of(Role.builder().code("R").build())).build();
    }

    private OrgUserRequest createRequest(String password) {
        when(organisationRepository.getOrganisations(any())).thenReturn(List.of(Organisation.builder().build()));
        return OrgUserRequest.builder().requestInfo(new RequestInfo()).organizationId("org-1")
                .user(createPayload(password)).build();
    }

    @Test
    void createWithAPasswordBreakingThePolicyIsRefusedBeforeHrmsIsCalled() {
        OrgUserRequest request = createRequest("password");
        CustomException e = assertThrows(CustomException.class,
                () -> ReflectionTestUtils.invokeMethod(validator, "validateUserOrgCreation", request));
        assertEquals("INVALID_PWD_PATTERN", e.getCode());
        verifyNoInteractions(hrmsUtils, userUtil);
    }

    @Test
    void settingAPasswordMarksItForChangeAndLeavesTheReturnedUserClean() {
        User settled = User.builder().uuid("uuid-1").userName("u").build();

        long before = System.currentTimeMillis();
        ReflectionTestUtils.invokeMethod(validator, "setPassword", new RequestInfo(), settled, "abc1@de");

        ArgumentCaptor<java.util.Date> expiry = ArgumentCaptor.forClass(java.util.Date.class);
        verify(userUtil).setPasswordPreservingUser(any(), eq("uuid-1"), eq("abc1@de"), expiry.capture());
        assertTrue(expiry.getValue().getTime() < before, "pwdExpiryDate must be in the past");
        assertNull(settled.getPassword(), "the password must not ride back on the response or Kafka payload");
    }

    @Test
    void anOutOfScopeResetIsRefusedBeforeAnythingIsWritten() {
        Employee stored = Employee.builder().user(User.builder().uuid("u1").userName("ramesh")
                .roles(List.of(Role.builder().code("EMPLOYEE").build())).build()).build();
        when(configuration.getGlobalTenantId()).thenReturn("in");
        when(userRepository.getOrgUsers(any(), any())).thenReturn(List.of(
                OrgUser.builder().id("row-1").organizationId("vendor-B").userId("u1").build()));
        when(hrmsUtils.getUserByUsername(any(), eq("ramesh"))).thenReturn(List.of(stored));
        doThrow(new CustomException("PASSWORD_SET_OUT_OF_SCOPE", "no"))
                .when(passwordScopeGuard).require(any(), eq("vendor-B"), eq("u1"), any());

        OrgUserRequest request = OrgUserRequest.builder().requestInfo(new RequestInfo()).id("row-1")
                .organizationId("vendor-B")
                .user(User.builder().tenantId("in").userName("ramesh").name("Ramesh").password("newpass1!").build())
                .build();

        CustomException e = assertThrows(CustomException.class,
                () -> ReflectionTestUtils.invokeMethod(validator, "validateUserOrgUpdate", request));
        assertEquals("PASSWORD_SET_OUT_OF_SCOPE", e.getCode());
        verify(hrmsUtils, never()).updateHRMSUser(any());
        verify(userUtil, never()).setPasswordPreservingUser(any(), any(), any(), any());
    }

    @Test
    void anUpdateWithoutAPasswordIsNotAPasswordSet() {
        Employee stored = Employee.builder().user(User.builder().uuid("u1").userName("ramesh").build()).build();
        when(configuration.getGlobalTenantId()).thenReturn("in");
        when(userRepository.getOrgUsers(any(), any())).thenReturn(List.of(
                OrgUser.builder().id("row-1").organizationId("vendor-B").userId("u1").build()));
        when(hrmsUtils.getUserByUsername(any(), eq("ramesh"))).thenReturn(List.of(stored));
        when(jurisdictionScopeResolver.resolve(any())).thenThrow(new CustomException("STOP", "past the password check"));

        OrgUserRequest request = OrgUserRequest.builder().requestInfo(new RequestInfo()).id("row-1")
                .organizationId("vendor-B")
                .user(User.builder().tenantId("in").userName("ramesh").name("Ramesh").build())
                .build();

        CustomException e = assertThrows(CustomException.class,
                () -> ReflectionTestUtils.invokeMethod(validator, "validateUserOrgUpdate", request));
        assertEquals("STOP", e.getCode());
        verifyNoInteractions(passwordScopeGuard);
    }

    @Test
    void passwordIsNeverLoggedOrSerialised() throws Exception {
        User user = createPayload("abc1@de");
        assertFalse(user.toString().contains("abc1@de"));
        assertFalse(new ObjectMapper().writeValueAsString(user).contains("abc1@de"));
        User read = new ObjectMapper().readValue("{\"password\":\"abc1@de\"}", User.class);
        assertEquals("abc1@de", read.getPassword());
    }
}
