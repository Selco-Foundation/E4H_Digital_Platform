package org.egov.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.config.Configuration;
import org.egov.repository.OrganisationRepository;
import org.egov.repository.OrganisationUserRepository;
import org.egov.util.FacilityUtil;
import org.egov.util.HRMSUtils;
import org.egov.util.MDMSUtil;
import org.egov.util.OrganisationUtil;
import org.egov.util.UserUtil;
import org.egov.web.models.Jurisdiction;
import org.egov.web.models.Role;
import org.egov.web.models.User;
import org.egov.web.models.UserChangeSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Role scoping on jurisdictions: change detection has to notice a roles-only edit, and roles
 * the user no longer holds have to be pruned before the employee reaches HRMS.
 */
@ExtendWith(MockitoExtension.class)
class OrganisationUserJurisdictionRoleTest {

    @Mock private MDMSUtil mdmsUtil;
    @Mock private Configuration configuration;
    @Mock private OrganisationRepository organisationRepository;
    @Mock private OrganisationUtil organisationUtil;
    @Mock private HRMSUtils hrmsUtils;
    @Mock private UserUtil userUtil;
    @Mock private OrganisationUserRepository userRepository;
    @Mock private FacilityUtil facilityUtil;
    @Mock private ObjectMapper mapper;

    @InjectMocks
    private OrganisationUserServiceValidator validator;

    private Role role(String code) {
        return Role.builder().code(code).name(code).build();
    }

    private Jurisdiction jurisdiction(String boundary, String... roleCodes) {
        List<Role> roles = new ArrayList<>();
        for (String code : roleCodes) {
            roles.add(role(code));
        }
        return Jurisdiction.builder().boundary(boundary).isActive(true).roles(roles).build();
    }

    private UserChangeSet detect(User existing, User incoming) {
        return (UserChangeSet) ReflectionTestUtils.invokeMethod(
                validator, "detectUserChanges", existing, incoming);
    }

    private void prune(List<Jurisdiction> jurisdictions, List<Role> heldRoles) {
        ReflectionTestUtils.invokeMethod(validator, "pruneJurisdictionRoles", jurisdictions, heldRoles);
    }

    private User user(List<Role> roles, List<Jurisdiction> jurisdictions) {
        return User.builder().name("n").userName("u").roles(roles).jurisdictions(jurisdictions).build();
    }

    @Test
    void narrowingAJurisdictionToOneRoleIsAChange() {
        User existing = user(List.of(role("PROJECT_MANAGER"), role("FACILITY_ADMIN")),
                List.of(jurisdiction("Kerala")));
        User incoming = user(List.of(role("PROJECT_MANAGER"), role("FACILITY_ADMIN")),
                List.of(jurisdiction("Kerala", "PROJECT_MANAGER")));

        assertTrue(detect(existing, incoming).isJurisdictionChanged(),
                "same boundary, same active flag, but the scoping narrowed");
    }

    @Test
    void swappingWhichRoleAJurisdictionAppliesToIsAChange() {
        User existing = user(List.of(role("PROJECT_MANAGER"), role("FACILITY_ADMIN")),
                List.of(jurisdiction("Kerala", "PROJECT_MANAGER")));
        User incoming = user(List.of(role("PROJECT_MANAGER"), role("FACILITY_ADMIN")),
                List.of(jurisdiction("Kerala", "FACILITY_ADMIN")));

        assertTrue(detect(existing, incoming).isJurisdictionChanged());
    }

    @Test
    void anUnchangedJurisdictionListIsNotAChange() {
        User existing = user(List.of(role("PROJECT_MANAGER")),
                List.of(jurisdiction("Kerala", "PROJECT_MANAGER")));
        User incoming = user(List.of(role("PROJECT_MANAGER")),
                List.of(jurisdiction("Kerala", "PROJECT_MANAGER")));

        assertFalse(detect(existing, incoming).isJurisdictionChanged());
    }

    @Test
    void removingARoleStripsTheJurisdictionsScopedToIt() {
        List<Jurisdiction> jurisdictions = new ArrayList<>(Arrays.asList(
                jurisdiction("Kerala", "PROJECT_MANAGER", "FACILITY_ADMIN"),
                jurisdiction("Bihar", "FACILITY_ADMIN")));

        prune(jurisdictions, List.of(role("PROJECT_MANAGER")));

        assertEquals(List.of("PROJECT_MANAGER"),
                jurisdictions.get(0).getRoles().stream().map(Role::getCode).toList());
        assertTrue(jurisdictions.get(1).getRoles().isEmpty(),
                "every role pruned leaves the jurisdiction unscoped, not deleted");
        assertEquals(2, jurisdictions.size(), "pruning must not drop the jurisdiction itself");
    }

    @Test
    void anUnscopedJurisdictionSurvivesPruning() {
        List<Jurisdiction> jurisdictions = new ArrayList<>(List.of(jurisdiction("Kerala")));

        prune(jurisdictions, List.of(role("PROJECT_MANAGER")));

        assertEquals(1, jurisdictions.size());
        assertTrue(jurisdictions.get(0).getRoles().isEmpty());
    }

    @Test
    void pruningToleratesAUserWithNoRolesAtAll() {
        List<Jurisdiction> jurisdictions = new ArrayList<>(List.of(jurisdiction("Kerala", "PROJECT_MANAGER")));

        prune(jurisdictions, null);

        assertTrue(jurisdictions.get(0).getRoles().isEmpty());
    }
}
