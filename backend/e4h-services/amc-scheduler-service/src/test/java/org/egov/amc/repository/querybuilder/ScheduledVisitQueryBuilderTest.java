package org.egov.amc.repository.querybuilder;

import org.egov.amc.config.AMCServiceConfiguration;
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
 * Everyone but a project manager sees the visits assigned to them. A project manager sees every
 * visit unless the search is made for a signed-in user, when they see only the visits of projects
 * they are staff on. Internal searches keep the old behaviour: several of them (reindexing, the
 * facility index sync) need every visit and would silently come back empty if scoped.
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

    private static ScheduledVisitSearchRequest request(String uuid, boolean scopeProjectManager, String... roleCodes) {
        User user = new User();
        user.setUuid(uuid);
        user.setRoles(Arrays.stream(roleCodes).map(c -> Role.builder().code(c).build()).collect(Collectors.toList()));
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setUserInfo(user);
        return ScheduledVisitSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(ScheduledVisitSearchCriteria.builder()
                        .tenantId("in")
                        .scopeProjectManagerToStaffProjects(scopeProjectManager)
                        .build())
                .build();
    }

    private String query(ScheduledVisitSearchRequest request, List<Object> binds) {
        URLParams params = URLParams.builder().limit(10).offset(0).tenantId("in").includeDeleted(false).build();
        return builder.getScheduledVisitSearchQuery(request, params, binds);
    }

    @Test
    @DisplayName("a reviewer sees only visits assigned to them")
    void reviewerIsScopedToTheirAssignments() {
        List<Object> binds = new ArrayList<>();
        String sql = query(request(UUID, true, "AMC_REVIEWER"), binds);

        assertTrue(sql.contains(ASSIGNEE_CLAUSE));
        assertFalse(sql.contains("project_staff"));
        assertTrue(binds.contains(UUID));
    }

    @Test
    @DisplayName("a project manager searching for themselves sees only visits of projects they are staff on")
    void projectManagerIsScopedToTheirProjects() {
        List<Object> binds = new ArrayList<>();
        String sql = query(request(UUID, true, "PROJECT_MANAGER"), binds);

        assertTrue(sql.contains(STAFF_CLAUSE), sql);
        assertTrue(sql.contains("(ps.isdeleted IS NULL OR ps.isdeleted = false)"));
        assertFalse(sql.contains(ASSIGNEE_CLAUSE));
        assertTrue(binds.contains(UUID));
    }

    @Test
    @DisplayName("a project manager who is also a reviewer is held to their projects, not to assignments")
    void projectManagerWinsWhenBothRolesAreHeld() {
        String sql = query(request(UUID, true, "PROJECT_MANAGER", "AMC_REVIEWER"), new ArrayList<>());

        assertTrue(sql.contains(STAFF_CLAUSE));
        assertFalse(sql.contains(ASSIGNEE_CLAUSE));
    }

    @Test
    @DisplayName("an internal search keeps seeing every visit for a project manager")
    void internalSearchIsNotScoped() {
        List<Object> binds = new ArrayList<>();
        String sql = query(request(UUID, false, "PROJECT_MANAGER"), binds);

        assertFalse(sql.contains("project_staff"));
        assertFalse(sql.contains(ASSIGNEE_CLAUSE));
        assertFalse(binds.contains(UUID));
    }

    @Test
    @DisplayName("the count query is scoped the same way as the list")
    void countQueryIsScopedToo() {
        ScheduledVisitSearchRequest pm = request(UUID, true, "PROJECT_MANAGER");
        List<Object> binds = new ArrayList<>();

        String sql = builder.getSearchCountQueryString(pm, "in", null, false, binds);

        assertTrue(sql.startsWith("SELECT COUNT(DISTINCT sv.id)"));
        assertTrue(sql.contains(STAFF_CLAUSE));
        assertTrue(binds.contains(UUID));
    }

    @Test
    @DisplayName("a caller we cannot identify gets an error, not every visit")
    void blankUuidIsRefused() {
        CustomException refused = assertThrows(CustomException.class,
                () -> query(request(" ", true, "PROJECT_MANAGER"), new ArrayList<>()));
        assertEquals("INVALID_USER", refused.getCode());
        assertThrows(CustomException.class,
                () -> query(request(" ", false, "AMC_REVIEWER"), new ArrayList<>()));
    }
}
