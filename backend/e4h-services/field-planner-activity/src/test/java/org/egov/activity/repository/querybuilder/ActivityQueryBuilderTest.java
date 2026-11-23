package org.egov.activity.repository.querybuilder;

import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.web.models.ActivityFacilitySearchCriteria;
import org.egov.activity.web.models.ActivityFacilitySearchRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.common.models.core.URLParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Who the activity-facility search is scoped to.
 *
 * The scope is the only thing standing between a reviewer and every activity facility in the
 * country, so each case below names the behaviour it protects rather than the branch it covers.
 */
class ActivityQueryBuilderTest {

    private static final String SCOPE_CLAUSE = "EXISTS (SELECT 1 FROM activity_facility_users fu";
    private static final String UUID = "caller-uuid";

    private ActivityConfiguration config;

    @BeforeEach
    void setUp() {
        config = new ActivityConfiguration();
        // Needed by the pagination wrapper, not by anything under test here.
        config.setDefaultLimit(10);
        config.setDefaultOffset(0);
        config.setMaxLimit(1000);
    }

    private ActivityQueryBuilder builder() {
        return new ActivityQueryBuilder(config);
    }

    private static ActivityFacilitySearchRequest request(String uuid, String... roleCodes) {
        User user = new User();
        user.setUuid(uuid);
        user.setRoles(Arrays.stream(roleCodes).map(c -> Role.builder().code(c).build()).collect(Collectors.toList()));
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserInfo(user);
        return ActivityFacilitySearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(ActivityFacilitySearchCriteria.builder().tenantId("in").build())
                .build();
    }

    private static URLParams params() {
        return URLParams.builder().limit(10).offset(0).tenantId("in").includeDeleted(false).build();
    }

    private String query(ActivityFacilitySearchRequest request, List<Object> binds) {
        return builder().getActivityFacilitySearchQuery(request, params(), binds);
    }

    @Test
    @DisplayName("a reviewer is scoped to the facilities they are assigned to")
    void reviewerIsScoped() {
        List<Object> binds = new ArrayList<>();
        String sql = query(request(UUID, "INSTALLATION_REPORT_APPROVER_QC_TEAM"), binds);

        assertTrue(sql.contains(SCOPE_CLAUSE), "the reviewer bypass is gone; they scope like field staff");
        assertTrue(sql.contains("fu.isdeleted = false"), "a revoked assignment must not still grant access");
        assertTrue(binds.contains(UUID), "the caller's uuid is the predicate");
    }

    @Test
    @DisplayName("field staff are scoped, as they always were")
    void fieldStaffAreScoped() {
        assertTrue(query(request(UUID, "INSTALLATION_REPORT_PART_A_EDITOR"), new ArrayList<>()).contains(SCOPE_CLAUSE));
    }

    @Test
    @DisplayName("project managers and facility admins keep their bypass - they are not assignment-based")
    void privilegedRolesAreNotScoped() {
        for (String role : new String[]{"PROJECT_MANAGER", "FACILITY_ADMIN"}) {
            List<Object> binds = new ArrayList<>();
            String sql = query(request(UUID, role), binds);
            assertFalse(sql.contains("activity_facility_users fu"),
                    role + " is scoped by jurisdiction later, not by assignment");
        }
    }

    @Test
    @DisplayName("holding a privileged role as well as the reviewer role still bypasses")
    void aPrivilegedReviewerStillBypasses() {
        String sql = query(request(UUID, "INSTALLATION_REPORT_APPROVER_QC_TEAM", "PROJECT_MANAGER"), new ArrayList<>());
        assertFalse(sql.contains("activity_facility_users fu"));
    }

    @Test
    @DisplayName("an unprivileged caller with no uuid is not scoped - documented, and the reason the join had to go")
    void aBlankUuidIsNotScoped() {
        String sql = query(request("", "INSTALLATION_REPORT_PART_B_EDITOR"), new ArrayList<>());

        // Preserves today's predicate semantics. The hazard the EXISTS rewrite removed is worse:
        // the old join was added without this guard, so such a caller got an unfiltered INNER
        // JOIN - every row, once per staffed user.
        assertFalse(sql.contains("activity_facility_users fu"));
    }

    @Test
    @DisplayName("a caller with no roles at all does not blow up")
    void nullRolesAreSurvivable() {
        User user = new User();
        user.setUuid(UUID);
        user.setRoles(null);
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserInfo(user);
        ActivityFacilitySearchRequest request = ActivityFacilitySearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(ActivityFacilitySearchCriteria.builder().tenantId("in").build())
                .build();

        assertTrue(query(request, new ArrayList<>()).contains(SCOPE_CLAUSE),
                "no roles means no privilege, so the caller is scoped");
    }

    @Test
    @DisplayName("the count query carries the same scope, so totals cannot disagree with rows")
    void theCountQueryIsScopedToo() {
        User user = new User();
        user.setUuid(UUID);
        user.setRoles(List.of(Role.builder().code("INSTALLATION_REPORT_APPROVER_QC_TEAM").build()));
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserInfo(user);
        ActivityFacilitySearchCriteria criteria = ActivityFacilitySearchCriteria.builder().tenantId("in").build();
        criteria.setCountQuery(true);
        ActivityFacilitySearchRequest request = ActivityFacilitySearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(criteria)
                .build();

        String sql = query(request, new ArrayList<>());

        assertTrue(sql.startsWith("SELECT COUNT(*)"), "still a plain count - EXISTS cannot multiply rows");
        assertTrue(sql.contains(SCOPE_CLAUSE));
    }

    @Test
    @DisplayName("the scope clause joins the WHERE correctly whether or not it is first")
    void clauseAssemblyIsWellFormed() {
        String sql = query(request(UUID, "INSTALLATION_REPORT_PART_B_EDITOR"), new ArrayList<>());

        int whereCount = sql.split(" WHERE ", -1).length - 1;
        assertTrue(whereCount >= 1, "there must be a WHERE");
        assertFalse(sql.contains("AND  AND"), "no doubled conjunction");
        assertFalse(sql.contains("WHERE AND"), "no WHERE immediately followed by AND");
    }

    @Test
    @DisplayName("an internal re-read can opt out of the scope entirely")
    void internalReReadsCanOptOut() {
        List<Object> binds = new ArrayList<>();
        String sql = builder().getActivityFacilitySearchQuery(
                request(UUID, "INSTALLATION_REPORT_APPROVER_QC_TEAM"), params(), binds, false);

        assertFalse(sql.contains("activity_facility_users fu"),
                "fetching a row before acting on it is not a permission check");
        assertFalse(binds.contains(UUID));
    }
}
