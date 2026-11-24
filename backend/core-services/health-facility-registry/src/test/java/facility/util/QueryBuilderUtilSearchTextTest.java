package facility.util;

import facility.web.models.FacilityBulkSearchCriteria;
import facility.web.models.FacilityScope;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The View Facilities search box: one text over name, HFR ID, NIN ID and PoC username. */
class QueryBuilderUtilSearchTextTest {

    private static final List<String> ONM_ROLES = Collections.singletonList("FACILITY_ADMIN");
    private static final String GROUP = "AND (fac.facility_name ILIKE ? OR fac.hfr_id ILIKE ?"
            + " OR fac.nin_id ILIKE ? OR fac.facility_poc_username ILIKE ?)";

    private static RequestInfo admin() {
        RequestInfo requestInfo = new RequestInfo();
        User user = new User();
        user.setRoles(List.of(Role.builder().code("FACILITY_ADMIN").build()));
        requestInfo.setUserInfo(user);
        return requestInfo;
    }

    private static QueryBuilderResult build(String searchText, FacilityScope scope) {
        FacilityBulkSearchCriteria criteria = FacilityBulkSearchCriteria.builder()
                .tenantIds(List.of("in")).searchText(searchText).build();
        return QueryBuilderUtil.buildBulkWhereClause(criteria, admin(), ONM_ROLES, scope, Collections.emptyMap());
    }

    @Test
    void textMatchesAllFourColumnsPartiallyAsOneBracketedCondition() {
        QueryBuilderResult result = build("  phc  ", null);

        assertTrue(result.getWhereClause().contains(GROUP), result.getWhereClause());
        assertEquals(Collections.nCopies(4, "%phc%"),
                result.getParams().subList(result.getParams().size() - 4, result.getParams().size()));
    }

    @Test
    void blankTextAddsNothing() {
        assertFalse(build("   ", null).getWhereClause().contains("ILIKE"));
        assertFalse(build(null, null).getWhereClause().contains("ILIKE"));
    }

    @Test
    void wildcardsInTheTextAreMatchedLiterally() {
        assertEquals("%50\\%%", build("50%", null).getParams().get(2));
        assertEquals("%HC\\_1%", build("HC_1", null).getParams().get(2));
        assertEquals("%a\\\\b%", build("a\\b", null).getParams().get(2));
    }

    @Test
    void theJurisdictionCeilingStillAppliesToASearch() {
        FacilityScope scope = FacilityScope.scoped("FACILITY_ADMIN", new LinkedHashSet<>(List.of("F1")), "test");

        String where = build("phc", scope).getWhereClause();

        assertTrue(where.indexOf(GROUP) < where.indexOf("AND fac.boundary_code = ANY (?::text[])"),
                "the search group and the ceiling are separate ANDed conditions: " + where);
    }
}
