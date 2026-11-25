package org.egov.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.repository.OrganisationRepository;
import org.egov.repository.OrganisationUserRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.*;
import org.egov.web.models.Employee;
import org.egov.web.models.Role;
import org.egov.web.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Creating over an existing account sets that account's password. For an ORG_ADMIN - who is not
 * scoped by jurisdiction - only an account already in their own organisation, and never a
 * platform admin, may be reached this way.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrgAdminExistingAccountTest {

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

    @BeforeEach
    void setUp() {
        when(orgAdminScopeResolver.resolve(any())).thenReturn(new OrgAdminScopeResolver.OrgScope("org-1"));
    }

    private static RequestInfo orgAdmin() {
        return RequestInfo.builder().userInfo(org.egov.common.contract.request.User.builder().uuid("caller")
                .roles(List.of(org.egov.common.contract.request.Role.builder().code("ORG_ADMIN").build())).build()).build();
    }

    private static Employee existing(String... roleCodes) {
        List<Role> roles = Arrays.stream(roleCodes).map(c -> Role.builder().code(c).build()).toList();
        return Employee.builder().user(User.builder().uuid("existing").roles(roles).build()).build();
    }

    private void check(Employee employee) {
        ReflectionTestUtils.invokeMethod(validator, "requireCallerAdministers", orgAdmin(), employee, "9999999999");
    }

    @Test
    void accountFromAnotherOrgIsRefused() {
        when(userRepository.isUserLinkedToOrg("existing", "org-1")).thenReturn(false);
        CustomException e = assertThrows(CustomException.class, () -> check(existing("EMPLOYEE")));
        assertEquals("USER_OUT_OF_SCOPE", e.getCode());
    }

    @Test
    void platformAdminIsRefusedEvenInTheSameOrg() {
        when(userRepository.isUserLinkedToOrg("existing", "org-1")).thenReturn(true);
        assertThrows(CustomException.class, () -> check(existing("ORG_PLATFORM_ADMIN")));
    }

    @Test
    void accountAlreadyInTheOwnOrgIsAllowed() {
        when(userRepository.isUserLinkedToOrg("existing", "org-1")).thenReturn(true);
        assertDoesNotThrow(() -> check(existing("EMPLOYEE")));
        verifyNoInteractions(jurisdictionScopeResolver);
    }
}
