package facility.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundaryAncestryTest {

    private static final String RESPONSE = """
            {"TenantBoundary":[{"tenantId":"in","hierarchyType":"ADMIN","boundary":[
              {"code":"India","boundaryType":"Country","children":[
                {"code":"India_Karnataka","boundaryType":"State","children":[
                  {"code":"India_Karnataka_Dharwad","boundaryType":"District","children":[
                    {"code":"India_Karnataka_Dharwad_Hubli","boundaryType":"Block","children":[]}
                  ]}
                ]}
              ]}
            ]}]}
            """;

    @Test
    void chainRunsFromTheRootDownToTheCode() throws Exception {
        Map<String, String> chain = BoundaryUtil.chainTo(new ObjectMapper().readTree(RESPONSE),
                "India_Karnataka_Dharwad_Hubli");

        assertEquals(List.of("country", "state", "district", "block"), List.copyOf(chain.keySet()));
        assertEquals("India_Karnataka", chain.get("state"));
        assertEquals("India_Karnataka_Dharwad_Hubli", chain.get("block"));
    }

    @Test
    void aDistrictCodeEndsAtDistrict() throws Exception {
        Map<String, String> chain = BoundaryUtil.chainTo(new ObjectMapper().readTree(RESPONSE),
                "India_Karnataka_Dharwad");

        assertEquals("India_Karnataka_Dharwad", chain.get("district"));
        assertTrue(!chain.containsKey("block"));
    }

    @Test
    void anUnknownCodeGivesAnEmptyChain() throws Exception {
        assertTrue(BoundaryUtil.chainTo(new ObjectMapper().readTree(RESPONSE), "Nowhere").isEmpty());
    }
}
