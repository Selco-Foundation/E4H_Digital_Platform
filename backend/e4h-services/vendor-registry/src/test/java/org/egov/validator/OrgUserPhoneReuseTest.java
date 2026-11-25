package org.egov.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.repository.OrganisationRepository;
import org.egov.repository.OrganisationUserRepository;
import org.egov.util.*;
import org.egov.web.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * A mobile number freed by a deleted user can be given to someone new. Only a match on both the
 * number and the username is the same person; anything else is a new user.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrgUserPhoneReuseTest {

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
    @Mock private OrgAdminScopeResolver orgAdminScopeResolver;

    @Mock private org.egov.util.PasswordScopeGuard passwordScopeGuard;


    @InjectMocks
    private OrganisationUserServiceValidator validator;

    private static final String PHONE = "9999999999";

    @BeforeEach
    void setUp() {
        when(jurisdictionScopeResolver.resolve(any())).thenReturn(JurisdictionScopeResolver.Scope.everything());
        when(userUtil.mapToUserRequest(any())).thenReturn(UserRequest.builder().build());
        when(configuration.getUserHost()).thenReturn("http://user");
        when(configuration.getUserUpdateEndpoint()).thenReturn("/user/users/_updatenovalidate");
        when(hrmsUtils.buildEmployee(any(), any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return Employee.builder().code(u.getUserName()).user(u)
                    .jurisdictions(new ArrayList<>(u.getJurisdictions())).build();
        });
        when(hrmsUtils.createHRMSUser(any())).thenReturn(List.of(employee("new-uuid", "new.user")));
    }

    private static Employee employee(String uuid, String userName) {
        return Employee.builder().user(User.builder().uuid(uuid).userName(userName).mobileNumber(PHONE)
                .roles(new ArrayList<>(List.of(Role.builder().code("EMPLOYEE").build()))).build())
                .jurisdictions(new ArrayList<>()).build();
    }

    private OrgUserRequest create(String userName) {
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setBoundary("India_Karnataka");
        jurisdiction.setIsActive(true);
        User user = User.builder().tenantId("in").name("Same Name").userName(userName).mobileNumber(PHONE)
                .emailId("same@mail.com")
                .roles(new ArrayList<>(List.of(Role.builder().code("VENDOR_STAFF").build())))
                .jurisdictions(new ArrayList<>(List.of(jurisdiction))).build();
        return OrgUserRequest.builder().requestInfo(new RequestInfo()).organizationId("org-1").user(user).build();
    }

    private void run(OrgUserRequest request) {
        ReflectionTestUtils.invokeMethod(validator, "createOrLinkUser", request,
                Organisation.builder().orgType("VENDOR").build(), true);
    }

    @Test
    void aDeletedUsersNumberWithADifferentUsernameCreatesANewUser() {
        when(hrmsUtils.getUserByPhoneNumber(any(), eq(PHONE))).thenReturn(List.of(employee("old-uuid", "old.user")));
        OrgUserRequest request = create("new.user");

        run(request);

        ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
        verify(hrmsUtils).createHRMSUser(sent.capture());
        Employee created = ((EmployeeRequest) sent.getValue()).getEmployees().get(0);
        assertEquals("new.user", created.getUser().getUserName());
        assertEquals("India_Karnataka", created.getJurisdictions().get(0).getBoundary());
        assertEquals("new-uuid", request.getUserId());
        // The old account is neither revived in this org nor edited.
        verify(userRepository, never()).ensureActiveOrgUserLinkOrReactivateDeleted(any(), any());
        verify(hrmsUtils, never()).updateHRMSUser(any());
    }

    @Test
    void theSameUsernameStillReusesTheExistingAccount() {
        when(hrmsUtils.getUserByPhoneNumber(any(), eq(PHONE))).thenReturn(List.of(employee("old-uuid", "old.user")));
        OrgUserRequest request = create("OLD.user");

        run(request);

        verify(hrmsUtils, never()).createHRMSUser(any());
        verify(userRepository).ensureActiveOrgUserLinkOrReactivateDeleted("old-uuid", "org-1");
        assertEquals("old-uuid", request.getUserId());
    }

    @Test
    void amongSeveralHoldersOfTheNumberTheOneWithTheUsernameIsPicked() {
        List<Employee> matches = List.of(employee("a", "first.user"), employee("b", "second.user"));
        assertEquals("b", OrganisationUserServiceValidator.findReusableEmployee(matches, " second.user ").getUser().getUuid());
        assertNull(OrganisationUserServiceValidator.findReusableEmployee(matches, "third.user"));
        assertNull(OrganisationUserServiceValidator.findReusableEmployee(matches, null));
        assertNull(OrganisationUserServiceValidator.findReusableEmployee(null, "first.user"));
    }
}
