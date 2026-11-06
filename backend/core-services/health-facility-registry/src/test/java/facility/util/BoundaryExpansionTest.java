package facility.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import facility.web.models.FacilityBulkSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * How the client's boundary filters become facility codes.
 *
 * Each test pins one behaviour that the previous implementation got wrong, so the names describe
 * the behaviour rather than the method.
 */
@ExtendWith(MockitoExtension.class)
class BoundaryExpansionTest {

    /** One state, two districts, two blocks each, two facilities per block. */
    private static final String HIERARCHY = "{\"TenantBoundary\":[{\"boundary\":["
            + node("S1", "State",
                node("D1", "District",
                    node("B1", "Block", facility("F1"), facility("F2")),
                    node("B2", "Block", facility("F3"), facility("F4"))),
                node("D2", "District",
                    node("B3", "Block", facility("F5"), facility("F6")),
                    node("B4", "Block", facility("F7"), facility("F8"))))
            + "]}]}";

    private static String node(String code, String type, String... children) {
        return "{\"code\":\"" + code + "\",\"boundaryType\":\"" + type + "\",\"children\":["
                + String.join(",", children) + "]}";
    }

    private static String facility(String code) {
        return node(code, "Facility");
    }

    @Mock
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private BoundaryUtil boundaryUtil;

    @BeforeEach
    void setUp() {
        boundaryUtil = new BoundaryUtil();
        ReflectionTestUtils.setField(boundaryUtil, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(boundaryUtil, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(boundaryUtil, "boundaryHost", "http://boundary");
        ReflectionTestUtils.setField(boundaryUtil, "boundaryV2Url", "/boundary-relationships/v2/_search");
        ReflectionTestUtils.setField(boundaryUtil, "boundaryHierarchyType", "SELCO");
    }

    @SuppressWarnings("unchecked")
    private void boundaryServiceReturns(String json) throws Exception {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(objectMapper.readValue(json, Map.class));
    }

    @Test
    @DisplayName("a state resolves to every facility beneath it, not just the first one found")
    void stateResolvesToEveryDescendantFacility() throws Exception {
        boundaryServiceReturns(HIERARCHY);

        Set<String> facilities = boundaryUtil.facilityCodesBeneath(Collections.singletonList("S1")).get("S1");

        assertEquals(setOf("F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8"), facilities,
                "the walk must visit every sibling; it used to stop at the first matching node");
    }

    @Test
    @DisplayName("several codes at the same level each resolve, rather than only the first")
    void everyRequestedCodeResolvesIndependently() throws Exception {
        boundaryServiceReturns(HIERARCHY);

        Map<String, Set<String>> beneath = boundaryUtil.facilityCodesBeneath(Arrays.asList("D1", "D2"));

        assertEquals(setOf("F1", "F2", "F3", "F4"), beneath.get("D1"), "D1 must resolve");
        assertEquals(setOf("F5", "F6", "F7", "F8"), beneath.get("D2"), "D2 must resolve too");
    }

    @Test
    @DisplayName("levels intersect: a state and a district together give the district's facilities")
    void levelsIntersectRatherThanTheLowestWinning() throws Exception {
        boundaryServiceReturns(HIERARCHY);

        FacilityBulkSearchCriteria criteria = FacilityBulkSearchCriteria.builder()
                .state(Collections.singletonList("S1"))
                .district(Collections.singletonList("D1"))
                .build();

        assertEquals(setOf("F1", "F2", "F3", "F4"), setOf(boundaryUtil.resolveFacilityCodes(criteria)),
                "the state must constrain the result, not be discarded in favour of the district");
    }

    @Test
    @DisplayName("levels that do not overlap resolve to nothing")
    void nonOverlappingLevelsResolveToNothing() throws Exception {
        boundaryServiceReturns(HIERARCHY);

        FacilityBulkSearchCriteria criteria = FacilityBulkSearchCriteria.builder()
                .state(Collections.singletonList("S1"))
                .district(Collections.singletonList("D_ELSEWHERE"))
                .build();

        assertTrue(boundaryUtil.resolveFacilityCodes(criteria).isEmpty(),
                "a district outside the state must not widen the result back to the state");
    }

    @Test
    @DisplayName("a requested code stays in its own level's set, for rows never migrated into the hierarchy")
    void aRequestedCodeSelectsItselfAsWellAsItsDescendants() throws Exception {
        boundaryServiceReturns(HIERARCHY);

        FacilityBulkSearchCriteria criteria = FacilityBulkSearchCriteria.builder()
                .boundaryCodes(Collections.singletonList("B1"))
                .build();

        assertEquals(setOf("B1", "F1", "F2"), setOf(boundaryUtil.resolveFacilityCodes(criteria)),
                "dropping the requested code would hide facilities whose boundary_code has no "
                        + "boundary-relationship row - which is what _backfill-boundary-relationships repairs");
    }

    @Test
    @DisplayName("a code absent from the hierarchy resolves to nothing rather than to everything")
    void anUnknownCodeResolvesToNothing() throws Exception {
        boundaryServiceReturns(HIERARCHY);

        assertTrue(boundaryUtil.facilityCodesBeneath(Collections.singletonList("NOPE")).get("NOPE").isEmpty(),
                "an unknown code must map to an empty set, and must still be present as a key");
    }

    @Test
    @DisplayName("an empty boundary response is survivable")
    void anEmptyBoundaryResponseDoesNotThrow() throws Exception {
        boundaryServiceReturns("{\"TenantBoundary\":[]}");

        assertTrue(boundaryUtil.facilityCodesBeneath(Collections.singletonList("S1")).get("S1").isEmpty(),
                "reading TenantBoundary[0] unconditionally used to throw here");
    }

    @Test
    @DisplayName("a facility-level grant resolves to itself without calling boundary-service")
    void aFacilityGrantNeedsNoBoundaryCall() throws Exception {
        assertEquals(Collections.singleton("F1"), boundaryUtil.expandToFacilityCodes("F1", "Facility"),
                "a facility is already a leaf; the call would be pure cost");
        verifyNoInteractions(restTemplate);
    }

    private static Set<String> setOf(String... values) {
        return new java.util.LinkedHashSet<>(Arrays.asList(values));
    }

    private static Set<String> setOf(List<String> values) {
        return new java.util.LinkedHashSet<>(values);
    }
}
