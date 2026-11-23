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
        when(userUtil.mapToUserRequest(settled)).thenReturn(UserRequest.builder().uuid("uuid-1").build());
        when(configuration.getUserHost()).thenReturn("http://user");
        when(configuration.getUserUpdateEndpoint()).thenReturn("/user/users/_updatenovalidate");

        long before = System.currentTimeMillis();
        ReflectionTestUtils.invokeMethod(validator, "setPassword", new RequestInfo(), settled, "abc1@de");

        ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
        verify(userUtil).updateUserPassword(sent.capture(), any());
        UserRequest outbound = ((CreateUserRequest) sent.getValue()).getUser();
        assertEquals("abc1@de", outbound.getPassword());
        assertTrue(outbound.getPwdExpiryDate().getTime() < before, "pwdExpiryDate must be in the past");
        assertNull(settled.getPassword(), "the password must not ride back on the response or Kafka payload");
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
