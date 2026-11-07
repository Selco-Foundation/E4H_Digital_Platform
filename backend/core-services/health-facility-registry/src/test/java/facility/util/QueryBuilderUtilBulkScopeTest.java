package facility.util;

import facility.web.models.FacilityBulkSearchCriteria;
import facility.web.models.FacilityScope;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * How the jurisdiction ceiling reaches the SQL.
 *
 * The method under test is static and pure, which makes this the cheapest place to pin the one
 * mistake that would quietly disable the whole feature: ORing the ceiling together with the
 * client's own boundary filter instead of ANDing it.
 */
class QueryBuilderUtilBulkScopeTest {

    private static final List<String> ONM_ROLES = Collections.singletonList("FACILITY_ADMIN");
    private static final String CEILING_CLAUSE = "AND fac.boundary_code = ANY (?::text[])";

    private static RequestInfo caller(String... roleCodes) {
        RequestInfo requestInfo = new RequestInfo();
        User user = new User();
        user.setUuid("caller-uuid");
        user.setRoles(Arrays.stream(roleCodes).map(c -> Role.builder().code(c).build()).collect(java.util.stream.Collectors.toList()));
        requestInfo.setUserInfo(user);
        return requestInfo;
    }

    private static FacilityScope ceilingOf(String... codes) {
        return FacilityScope.scoped("FACILITY_ADMIN", new LinkedHashSet<>(Arrays.asList(codes)), "test");
    }

    @Test
    @DisplayName("without a scope the query is exactly what it was before jurisdiction scoping")
    void anUnscopedQueryIsUnchanged() {
        FacilityBulkSearchCriteria criteria = FacilityBulkSearchCriteria.builder()
                .tenantIds(Collections.singletonList("in"))
                .build();

        QueryBuilderResult threeArg = QueryBuilderUtil.buildBulkWhereClause(criteria, caller("FACILITY_ADMIN"), ONM_ROLES);
        QueryBuilderResult withNullScope = QueryBuilderUtil.buildBulkWhereClause(criteria, caller("FACILITY_ADMIN"), ONM_ROLES, null, Collections.emptyMap());

        assertEquals(threeArg.getWhereClause(), withNullScope.getWhereClause(),
                "a null scope must behave exactly like the pre-scoping overload");
        assertFalse(threeArg.getWhereClause().contains(CEILING_CLAUSE), "no ceiling clause without a scope");
    }

    @Test
    @DisplayName("a scope adds exactly one clause and exactly one bind parameter")
    void aScopeAddsOneClauseAndOneParameter() {
        FacilityBulkSearchCriteria criteria = FacilityBulkSearchCriteria.builder()
                .tenantIds(Collections.singletonList("in"))
                .build();

        QueryBuilderResult unscoped = QueryBuilderUtil.buildBulkWhereClause(criteria, caller("FACILITY_ADMIN"), ONM_ROLES, null, Collections.emptyMap());
        QueryBuilderResult scoped = QueryBuilderUtil.buildBulkWhereClause(criteria, caller("FACILITY_ADMIN"), ONM_ROLES, ceilingOf("F1", "F2"), Collections.emptyMap());

        assertTrue(scoped.getWhereClause().contains(CEILING_CLAUSE), "the ceiling clause must be present");
        assertEquals(unscoped.getParams().size() + 1, scoped.getParams().size(),
                "the whole ceiling is one array parameter, however large it is");
        assertEquals("{\"F1\",\"F2\"}", scoped.getParams().get(scoped.getParams().size() - 1));
    }

    @Test
    @DisplayName("the client's own boundary filter and the ceiling are BOTH applied, never merged")
    void clientFilterAndCeilingIntersect() {
        FacilityBulkSearchCriteria criteria = FacilityBulkSearchCriteria.builder()
                .tenantIds(Collections.singletonList("in"))
                .boundaryCodes(Arrays.asList("F1", "F9"))
                .build();

        QueryBuilderResult result = QueryBuilderUtil.buildBulkWhereClause(
                criteria, caller("FACILITY_ADMIN"), ONM_ROLES, ceilingOf("F1", "F2"), Collections.emptyMap());

        assertTrue(result.getWhereClause().contains("AND boundary_code in ("),
                "the client's own filter must survive");
        assertTrue(result.getWhereClause().contains(CEILING_CLAUSE),
                "the ceiling must be a separate ANDed clause - merging the two would OR them, "
                        + "and a crafted request could then widen its own scope");
    }

    @Test
    @DisplayName("an explicit facilityIds filter still intersects with the ceiling")
    void facilityIdsIntersectWithTheCeiling() {
        FacilityBulkSearchCriteria criteria = FacilityBulkSearchCriteria.builder()
                .facilityIds(Arrays.asList("FAC-1", "FAC-2"))
                .build();

        QueryBuilderResult result = QueryBuilderUtil.buildBulkWhereClause(
                criteria, caller("FACILITY_ADMIN"), ONM_ROLES, ceilingOf("F1"), Collections.emptyMap());

        assertTrue(result.getWhereClause().contains("fac.id in ("), "the id filter must survive");
        assertTrue(result.getWhereClause().contains(CEILING_CLAUSE), "and the ceiling must still apply");
    }

    @Test
    @DisplayName("a ceiling of nothing matches nothing, rather than falling through to no filter")
    void anEmptyCeilingMatchesNothing() {
        FacilityBulkSearchCriteria criteria = FacilityBulkSearchCriteria.builder().build();

        QueryBuilderResult result = QueryBuilderUtil.buildBulkWhereClause(
                criteria, caller("FACILITY_ADMIN"), ONM_ROLES, ceilingOf(), Collections.emptyMap());

        assertTrue(result.getWhereClause().contains(CEILING_CLAUSE),
                "an empty ceiling must still emit the clause; omitting it would widen to everything");
        assertEquals("{}", result.getParams().get(result.getParams().size() - 1));
    }

    @Test
    @DisplayName("codes carrying quotes or backslashes are escaped into the array literal")
    void arrayLiteralEscapesAwkwardCodes() {
        assertEquals("{}", QueryBuilderUtil.toPgTextArrayLiteral(Collections.emptyList()));
        assertEquals("{\"India_Assam_Kamrup\"}",
                QueryBuilderUtil.toPgTextArrayLiteral(Collections.singletonList("India_Assam_Kamrup")));
        assertEquals("{\"FAC/2025/0045\"}",
                QueryBuilderUtil.toPgTextArrayLiteral(Collections.singletonList("FAC/2025/0045")));
        assertEquals("{\"a\\\"b\"}", QueryBuilderUtil.toPgTextArrayLiteral(Collections.singletonList("a\"b")));
        assertEquals("{\"a\\\\b\"}", QueryBuilderUtil.toPgTextArrayLiteral(Collections.singletonList("a\\b")));
    }

    @Test
    @DisplayName("each resolved level becomes its own ANDed clause, alongside the ceiling")
    void everyLevelGetsItsOwnClause() {
        FacilityBulkSearchCriteria criteria = FacilityBulkSearchCriteria.builder()
                .boundaryCodes(Collections.singletonList("F1"))
                .build();

        Map<String, Set<String>> levels = new LinkedHashMap<>();
        levels.put("state", new LinkedHashSet<>(Arrays.asList("F1", "F2", "F3")));
        levels.put("district", new LinkedHashSet<>(Arrays.asList("F1", "F2")));
        levels.put("block", new LinkedHashSet<>(Collections.singletonList("F1")));

        QueryBuilderResult result = QueryBuilderUtil.buildBulkWhereClause(
                criteria, caller("FACILITY_ADMIN"), ONM_ROLES, ceilingOf("F1", "F9"), levels);

        int ceilingClauses = result.getWhereClause().split(java.util.regex.Pattern.quote(CEILING_CLAUSE), -1).length - 1;
        assertEquals(4, ceilingClauses,
                "three levels plus the jurisdiction ceiling, each ANDed - the database does the intersecting");
        assertTrue(result.getWhereClause().contains("AND boundary_code in ("),
                "boundaryCodes are facility codes already and go straight to the column");
    }

    @Test
    @DisplayName("a level that resolved to nothing matches nothing")
    void anEmptyLevelMatchesNothing() {
        Map<String, Set<String>> levels = new LinkedHashMap<>();
        levels.put("state", new LinkedHashSet<>());

        QueryBuilderResult result = QueryBuilderUtil.buildBulkWhereClause(
                FacilityBulkSearchCriteria.builder().build(), caller("FACILITY_ADMIN"), ONM_ROLES, null, levels);

        assertTrue(result.getWhereClause().contains(CEILING_CLAUSE));
        assertEquals("{}", result.getParams().get(result.getParams().size() - 1),
                "an empty array is the whole special case - no early return needed anywhere");
    }
}
