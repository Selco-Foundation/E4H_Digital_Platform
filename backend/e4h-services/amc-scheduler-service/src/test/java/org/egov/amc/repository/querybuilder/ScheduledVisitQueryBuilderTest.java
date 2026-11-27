package org.egov.amc.repository.querybuilder;

import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.util.SystemRequestInfo;
import org.egov.amc.web.models.ScheduledVisitSearchCriteria;
import org.egov.amc.web.models.ScheduledVisitSearchRequest;
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
 * Whose AMC visits the visit search returns.
 *
 * This service acting for itself (the index sync, reindexing, the lookups behind visit generation)
 * sees every visit. A project manager sees the visits of projects they are staff on, and everyone
 * else the visits assigned to them. Nobody gets an unfiltered list by being unidentified.
 */
class ScheduledVisitQueryBuilderTest {

    private static final String STAFF_CLAUSE = "EXISTS (SELECT 1 FROM project_staff ps WHERE ps.projectid = sv.project_id";
    private static final String ASSIGNEE_CLAUSE = "sva.assigned_user = ?";
    private static final String UUID = "caller-uuid";

    private ScheduledVisitQueryBuilder builder;

    @BeforeEach
    void setUp() {
        AMCServiceConfiguration config = new AMCServiceConfiguration();
        config.setDefaultLimit(10);
        config.setDefaultOffset(0);
        config.setMaxLimit(1000);
        builder = new ScheduledVisitQueryBuilder(config);
    }

    private static ScheduledVisitSearchRequest request(RequestInfo requestInfo) {
        return ScheduledVisitSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(ScheduledVisitSearchCriteria.builder().tenantId("in").build())
                .build();
    }

    private static RequestInfo caller(String uuid, String type, String... roleCodes) {
        User user = new User();
        user.setUuid(uuid);
        user.setType(type);
        user.setRoles(Arrays.stream(roleCodes).map(c -> Role.builder().code(c).build()).collect(Collectors.toList()));
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserInfo(user);
        return requestInfo;
    }

    private String query(RequestInfo requestInfo, List<Object> binds) {
        URLParams params = URLParams.builder().limit(10).offset(0).tenantId("in").includeDeleted(false).build();
        return builder.getScheduledVisitSearchQuery(request(requestInfo), params, binds);
    }

    @Test
    @DisplayName("a reviewer sees only visits assigned to them")
    void reviewerIsScopedToTheirAssignments() {
        List<Object> binds = new ArrayList<>();
        String sql = query(caller(UUID, "EMPLOYEE", "AMC_REVIEWER"), binds);

        assertTrue(sql.contains(ASSIGNEE_CLAUSE));
        assertFalse(sql.contains("project_staff"));
        assertTrue(binds.contains(UUID));
    }

    @Test
    @DisplayName("a project manager sees only visits of projects they are staff on")
    void projectManagerIsScopedToTheirProjects() {
        List<Object> binds = new ArrayList<>();
        String sql = query(caller(UUID, "EMPLOYEE", "PROJECT_MANAGER"), binds);

        assertTrue(sql.contains(STAFF_CLAUSE), sql);
        assertTrue(sql.contains("(ps.isdeleted IS NULL OR ps.isdeleted = false)"));
        assertFalse(sql.contains(ASSIGNEE_CLAUSE));
        assertTrue(binds.contains(UUID));
    }

    @Test
    @DisplayName("a project manager who is also a reviewer is held to their projects, not to assignments")
    void projectManagerWinsWhenBothRolesAreHeld() {
        String sql = query(caller(UUID, "EMPLOYEE", "PROJECT_MANAGER", "AMC_REVIEWER"), new ArrayList<>());

        assertTrue(sql.contains(STAFF_CLAUSE));
        assertFalse(sql.contains(ASSIGNEE_CLAUSE));
    }

    @Test
    @DisplayName("this service acting for itself sees every visit")
    void systemCallerIsNotScoped() {
        List<Object> binds = new ArrayList<>();
        RequestInfo asSystem = SystemRequestInfo.of(caller(UUID, "EMPLOYEE", "AMC_REVIEWER"));

        String sql = query(asSystem, binds);

        assertFalse(sql.contains("project_staff"));
        assertFalse(sql.contains(ASSIGNEE_CLAUSE));
        assertFalse(binds.contains(UUID));
    }

    @Test
    @DisplayName("a project manager passed through SystemRequestInfo is no longer scoped, and the original is untouched")
    void systemIdentityReplacesTheProjectManagerRole() {
        RequestInfo pm = caller(UUID, "EMPLOYEE", "PROJECT_MANAGER");

        assertFalse(query(SystemRequestInfo.of(pm), new ArrayList<>()).contains("project_staff"));
        assertTrue(query(pm, new ArrayList<>()).contains(STAFF_CLAUSE));
    }

    @Test
    @DisplayName("the system identity needs both the SYSTEM type and the SYSTEM_USER role")
    void systemCallerNeedsBothTypeAndRole() {
        String typeOnly = query(caller(UUID, "SYSTEM", "AMC_REVIEWER"), new ArrayList<>());
        String roleOnly = query(caller(UUID, "EMPLOYEE", "SYSTEM_USER"), new ArrayList<>());
        String typeOnlyPm = query(caller(UUID, "SYSTEM", "PROJECT_MANAGER"), new ArrayList<>());

        assertTrue(typeOnly.contains(ASSIGNEE_CLAUSE));
        assertTrue(roleOnly.contains(ASSIGNEE_CLAUSE));
        assertTrue(typeOnlyPm.contains(STAFF_CLAUSE));
    }

    @Test
    @DisplayName("the count query is scoped the same way as the list")
    void countQueryIsScopedToo() {
        List<Object> binds = new ArrayList<>();
        String pm = builder.getSearchCountQueryString(
                request(caller(UUID, "EMPLOYEE", "PROJECT_MANAGER")), "in", null, false, binds);
        String system = builder.getSearchCountQueryString(
                request(SystemRequestInfo.of(caller(UUID, "EMPLOYEE", "PROJECT_MANAGER"))), "in", null, false, new ArrayList<>());

        assertTrue(pm.startsWith("SELECT COUNT(DISTINCT sv.id)"));
        assertTrue(pm.contains(STAFF_CLAUSE));
        assertTrue(binds.contains(UUID));
        assertFalse(system.contains("project_staff"));
    }

    @Test
    @DisplayName("a caller we cannot identify gets an error, not every visit")
    void blankUuidIsRefused() {
        for (String role : new String[]{"PROJECT_MANAGER", "AMC_REVIEWER"}) {
            CustomException refused = assertThrows(CustomException.class,
                    () -> query(caller(" ", "EMPLOYEE", role), new ArrayList<>()));
            assertEquals("INVALID_USER", refused.getCode());
        }
    }
}
