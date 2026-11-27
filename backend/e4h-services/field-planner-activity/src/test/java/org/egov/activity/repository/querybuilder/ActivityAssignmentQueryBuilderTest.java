package org.egov.activity.repository.querybuilder;

import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.web.models.ActivityAssignmentSearchCriteria;
import org.egov.activity.web.models.ActivityAssignmentSearchRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.common.models.core.URLParams;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Whose plan assignments the assignment search returns.
 *
 * A project manager is held to the projects they are staff on, everyone else to what is assigned
 * to them, and nobody gets an unfiltered list.
 */
class ActivityAssignmentQueryBuilderTest {

    private static final String STAFF_CLAUSE = "EXISTS (SELECT 1 FROM project_staff ps WHERE ps.projectid = fp.project_id";
    private static final String ASSIGNEE_CLAUSE = "aa.assigned_to = ?";
    private static final String UUID = "caller-uuid";

    private ActivityAssignmentQueryBuilder builder;

    @BeforeEach
    void setUp() {
        ActivityConfiguration config = new ActivityConfiguration();
        config.setDefaultLimit(10);
        config.setDefaultOffset(0);
        config.setMaxLimit(1000);
        builder = new ActivityAssignmentQueryBuilder(config);
    }

    private static ActivityAssignmentSearchRequest request(String uuid, String... roleCodes) {
        User user = new User();
        user.setUuid(uuid);
        user.setRoles(Arrays.stream(roleCodes).map(c -> Role.builder().code(c).build()).collect(Collectors.toList()));
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserInfo(user);
        return ActivityAssignmentSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(ActivityAssignmentSearchCriteria.builder().tenantId("in").build())
                .build();
    }

    private String query(ActivityAssignmentSearchRequest request, List<Object> binds) {
        URLParams params = URLParams.builder().limit(10).offset(0).tenantId("in").includeDeleted(false).build();
        return builder.getActivityAssignmentSearchQuery(request, params, binds);
    }

    @Test
    @DisplayName("a reviewer sees only assignments made to them")
    void reviewerIsScopedToTheirAssignments() {
        List<Object> binds = new ArrayList<>();
        String sql = query(request(UUID, "INSTALLATION_REPORT_APPROVER_QC_TEAM"), binds);

        assertTrue(sql.contains(ASSIGNEE_CLAUSE));
        assertFalse(sql.contains("project_staff"));
        assertTrue(binds.contains(UUID));
    }

    @Test
    @DisplayName("a project manager sees only plans of projects they are staff on")
    void projectManagerIsScopedToTheirProjects() {
        List<Object> binds = new ArrayList<>();
        String sql = query(request(UUID, "PROJECT_MANAGER"), binds);

        assertTrue(sql.contains(STAFF_CLAUSE), sql);
        assertTrue(sql.contains("ps.staffid = ?"));
        assertFalse(sql.contains(ASSIGNEE_CLAUSE));
        assertTrue(binds.contains(UUID));
    }

    @Test
    @DisplayName("a deleted staff row no longer grants the plan")
    void deletedProjectStaffRowsAreIgnored() {
        String sql = query(request(UUID, "PROJECT_MANAGER"), new ArrayList<>());

        assertTrue(sql.contains("(ps.isdeleted IS NULL OR ps.isdeleted = false)"));
    }

    @Test
    @DisplayName("a project manager who is also a reviewer is still held to their projects")
    void projectManagerWinsWhenBothRolesAreHeld() {
        String sql = query(request(UUID, "PROJECT_MANAGER", "INSTALLATION_REPORT_APPROVER_QC_TEAM"), new ArrayList<>());

        assertTrue(sql.contains(STAFF_CLAUSE));
    }

    @Test
    @DisplayName("the count query is scoped the same way as the list")
    void countQueryIsScopedToo() {
        ActivityAssignmentSearchRequest pm = request(UUID, "PROJECT_MANAGER");
        List<Object> binds = new ArrayList<>();

        String sql = builder.getSearchCountQueryString(pm, "in", null, false, binds);

        assertTrue(sql.startsWith("SELECT COUNT(*)"));
        assertTrue(sql.contains(STAFF_CLAUSE));
        assertTrue(binds.contains(UUID));
    }

    @Test
    @DisplayName("a caller we cannot identify gets an error, not every assignment")
    void blankUuidIsRefused() {
        for (String role : new String[]{"PROJECT_MANAGER", "INSTALLATION_REPORT_APPROVER_QC_TEAM"}) {
            CustomException refused = assertThrows(CustomException.class,
                    () -> query(request(" ", role), new ArrayList<>()));
            assertEquals("INVALID_USER", refused.getCode());
        }
    }
}
