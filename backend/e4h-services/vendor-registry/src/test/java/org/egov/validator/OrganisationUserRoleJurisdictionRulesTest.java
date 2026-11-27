package org.egov.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.repository.OrganisationRepository;
import org.egov.repository.OrganisationUserRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.FacilityUtil;
import org.egov.util.HRMSUtils;
import org.egov.util.JurisdictionRoleRules;
import org.egov.util.MDMSUtil;
import org.egov.util.OrganisationUtil;
import org.egov.util.PasswordScopeGuard;
import org.egov.util.UserUtil;
import org.egov.util.JurisdictionScopeResolver;
import org.egov.web.models.Employee;
import org.egov.web.models.Jurisdiction;
import org.egov.web.models.OrgUser;
import org.egov.web.models.OrgUserRequest;
import org.egov.web.models.Organisation;
import org.egov.web.models.Role;
import org.egov.web.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * The OrgRoles jurisdiction rules on user create, for a caller that is not the org user form.
 *
 * The create flow is cut short at the HRMS phone lookup, which throws a sentinel: everything this
 * test cares about happens before it.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrganisationUserRoleJurisdictionRulesTest {

    @Mock private MDMSUtil mdmsUtil;
    @Mock private Configuration configuration;
    @Mock private OrganisationRepository organisationRepository;
    @Mock private OrganisationUtil organisationUtil;
    @Mock private HRMSUtils hrmsUtils;
    @Mock private UserUtil userUtil;
    @Mock private OrganisationUserRepository userRepository;
    @Mock private FacilityUtil facilityUtil;
    @Mock private PasswordScopeGuard passwordScopeGuard;
    @Mock private JurisdictionScopeResolver jurisdictionScopeResolver;
    @Spy private ObjectMapper mapper = new ObjectMapper().disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // as in MainConfiguration

    @InjectMocks
    private OrganisationUserServiceValidator validator;

    private Employee sentToHrms;

    private static final class ReachedHrms extends RuntimeException {
    }

    @BeforeEach
    void setUp() {
        when(configuration.getGlobalTenantId()).thenReturn("in");
        when(mdmsUtil.mDMSCall(any(), anyString())).thenReturn(Map.of("MdmsRes", Map.of("Organisation", Map.of("OrgRoles", List.of(
                Map.of("code", "VENDOR_ADMIN", "name", "Vendor Admin", "orgType", "VENDOR", "jurisdictionScoped", false),
                Map.of("code", "PROJECT_MANAGER", "name", "Project Manager", "orgType", "VENDOR",
                        "allowedJurisdictionLevels", List.of("Country", "State", "District", "Block")),
                Map.of("code", "COMPLAINT_RESOLVER", "name", "Resolver", "orgType", "VENDOR",
                        "allowedJurisdictionLevels", List.of("Facility")))))));
        when(hrmsUtils.getUserByPhoneNumber(any(), any())).thenThrow(new ReachedHrms());
        when(jurisdictionScopeResolver.resolve(any())).thenReturn(JurisdictionScopeResolver.Scope.everything());
        when(hrmsUtils.buildJurisdictions(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(organisationRepository.getOrganisations(any())).thenReturn(List.of(Organisation.builder().id("org-1").orgType("VENDOR").build()));
        when(hrmsUtils.updateHRMSUser(any())).thenAnswer(invocation -> {
            sentToHrms = ((org.egov.web.models.EmployeeRequest) invocation.getArgument(0)).getEmployees().get(0);
            throw new ReachedHrms();
        });
    }

    private Role role(String code) {
        return Role.builder().code(code).name(code).build();
    }

    private Jurisdiction grant(String type, String... roleCodes) {
        return Jurisdiction.builder().boundary("B").boundaryType(type).isActive(true)
                .roles(Arrays.stream(roleCodes).map(this::role).toList()).build();
    }

    private User user(List<Jurisdiction> jurisdictions, String... roleCodes) {
        return User.builder().name("n").userName("u").mobileNumber("9999999999").emailId("e@x.in").tenantId("in")
                .roles(new ArrayList<>(Arrays.stream(roleCodes).map(this::role).toList()))
                .jurisdictions(jurisdictions).build();
    }

    private OrgUserRequest create(User user) {
        return OrgUserRequest.builder().requestInfo(RequestInfo.builder().build()).user(user).organizationId("org-1").build();
    }

    private void run(OrgUserRequest request) {
        ReflectionTestUtils.invokeMethod(validator, "createOrLinkUser", request,
                Organisation.builder().id("org-1").orgType("VENDOR").build(), false);
    }

    private String refusalCode(OrgUserRequest request) {
        return assertThrows(CustomException.class, () -> run(request)).getCode();
    }

    @Test
    void createWithOnlyUnscopedRolesAndNoJurisdictionGetsACountryGrantAndReachesHrms() {
        OrgUserRequest request = create(user(null, "VENDOR_ADMIN"));

        assertThrows(ReachedHrms.class, () -> run(request));

        List<Jurisdiction> stored = request.getUser().getJurisdictions();
        assertEquals(1, stored.size());
        assertEquals("India", stored.get(0).getBoundary());
        assertEquals("Country", stored.get(0).getBoundaryType());
        assertEquals("SELCO", stored.get(0).getHierarchy());
        assertEquals(List.of("VENDOR_ADMIN"), stored.get(0).getRoles().stream().map(Role::getCode).toList());
    }

    @Test
    void createWithAScopedRoleAndNoJurisdictionIsStillRefused() {
        assertEquals(JurisdictionRoleRules.JURISDICTION_REQUIRED_FOR_ROLE, refusalCode(create(user(null, "VENDOR_ADMIN", "PROJECT_MANAGER"))));
    }

    @Test
    void createRefusesARoleGrantedAtAnotherLevel() {
        assertEquals(JurisdictionRoleRules.LEVEL_NOT_ALLOWED,
                refusalCode(create(user(List.of(grant("State", "COMPLAINT_RESOLVER")), "COMPLAINT_RESOLVER"))));
    }

    @Test
    void createRefusesAScopedRoleWithNoGrantOfItsOwn() {
        assertEquals(JurisdictionRoleRules.JURISDICTION_REQUIRED_FOR_ROLE,
                refusalCode(create(user(List.of(grant("Country", "VENDOR_ADMIN")), "VENDOR_ADMIN", "PROJECT_MANAGER"))));
    }

    @Test
    void createAcceptsAGrantCoveringEachScopedRole() {
        assertThrows(ReachedHrms.class,
                () -> run(create(user(List.of(grant("State", "PROJECT_MANAGER")), "VENDOR_ADMIN", "PROJECT_MANAGER"))));
    }

    // Update: the user as stored has `stored` roles and grants; the request changes some of it.

    private void stored(List<Jurisdiction> grants, String... roleCodes) {
        User storedUser = user(grants, roleCodes);
        storedUser.setUuid("uuid-1");
        when(userRepository.getOrgUsers(any(), any())).thenReturn(List.of(
                OrgUser.builder().id("ou-1").organizationId("org-1").userId("uuid-1").user(user(grants, roleCodes)).build()));
        User hrmsUser = user(grants, roleCodes);
        hrmsUser.setUuid("uuid-1");
        hrmsUser.getRoles().add(role("EMPLOYEE"));
        when(hrmsUtils.getUserByUsername(any(), any())).thenReturn(List.of(
                Employee.builder().user(hrmsUser).jurisdictions(new ArrayList<>(grants == null ? List.of() : grants)).build()));
    }

    private OrgUserRequest update(User user) {
        RequestInfo requestInfo = RequestInfo.builder()
                .userInfo(org.egov.common.contract.request.User.builder().uuid("caller").build()).build();
        return OrgUserRequest.builder().requestInfo(requestInfo).id("ou-1").user(user).organizationId("org-1").build();
    }

    private void runUpdate(OrgUserRequest request) {
        validator.validateUpdateOrgUserRequest(request);
    }

    @Test
    void updateAddingAScopedRoleWithoutTouchingJurisdictionsIsRefused() {
        List<Jurisdiction> auto = List.of(grant("Country", "VENDOR_ADMIN"));
        stored(auto, "VENDOR_ADMIN");

        CustomException refused = assertThrows(CustomException.class,
                () -> runUpdate(update(user(auto, "VENDOR_ADMIN", "PROJECT_MANAGER"))));

        assertEquals(JurisdictionRoleRules.JURISDICTION_REQUIRED_FOR_ROLE, refused.getCode());
        assertNull(sentToHrms, "nothing may reach HRMS");
    }

    @Test
    void updateThatOnlyRenamesALegacyUserIsNotHeldUpByTheRules() {
        // PROJECT_MANAGER with no grant of its own would fail the rules, but a rename changes neither.
        List<Jurisdiction> legacy = List.of(grant("Country", "VENDOR_ADMIN"));
        stored(legacy, "VENDOR_ADMIN", "PROJECT_MANAGER");
        User renamed = user(legacy, "VENDOR_ADMIN", "PROJECT_MANAGER");
        renamed.setName("renamed");

        assertThrows(ReachedHrms.class, () -> runUpdate(update(renamed)));
        assertNotNull(sentToHrms);
    }

    @Test
    void updateRemovingEveryGrantFromAnUnscopedOnlyUserGivesThemTheCountryGrant() {
        stored(List.of(grant("State", "VENDOR_ADMIN")), "VENDOR_ADMIN");

        assertThrows(ReachedHrms.class, () -> runUpdate(update(user(List.of(), "VENDOR_ADMIN"))));

        List<Jurisdiction> sent = sentToHrms.getJurisdictions();
        assertEquals(1, sent.size());
        assertEquals("India", sent.get(0).getBoundary());
        assertEquals("SELCO", sent.get(0).getHierarchy());
        assertEquals(List.of("VENDOR_ADMIN"), sent.get(0).getRoles().stream().map(Role::getCode).toList());
    }

    @Test
    void updateGrantingARoleAtADisallowedLevelIsRefused() {
        stored(List.of(grant("State", "PROJECT_MANAGER")), "PROJECT_MANAGER");

        CustomException refused = assertThrows(CustomException.class,
                () -> runUpdate(update(user(List.of(grant("State", "COMPLAINT_RESOLVER")), "COMPLAINT_RESOLVER"))));

        assertEquals(JurisdictionRoleRules.LEVEL_NOT_ALLOWED, refused.getCode());
    }
}
