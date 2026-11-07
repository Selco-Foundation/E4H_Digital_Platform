package facility.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import facility.config.Configuration;
import facility.repository.ServiceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Reading the caller's jurisdictions out of HRMS.
 *
 * What matters here is that the method is total - it must hand back a failure mode rather than
 * throw, because "HRMS says you hold nothing" and "HRMS did not answer" end in the same empty
 * result for the user but only one of them is an outage.
 */
@ExtendWith(MockitoExtension.class)
class HRMSUtilsTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    private HRMSUtils hrmsUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        Configuration configs = new Configuration();
        configs.setHrmsHost("http://hrms");
        configs.setHrmsSearchEndPoint("/egov-hrms/v2/employees/_search");
        hrmsUtils = new HRMSUtils(serviceRequestRepository, configs, objectMapper);
    }

    private static RequestInfo caller(String uuid) {
        RequestInfo requestInfo = new RequestInfo();
        User user = new User();
        user.setUuid(uuid);
        requestInfo.setUserInfo(user);
        return requestInfo;
    }

    private void hrmsRespondsWith(String json) throws Exception {
        when(serviceRequestRepository.fetchResult(any(), any()))
                .thenReturn(objectMapper.readValue(json, Map.class));
    }

    @Test
    @DisplayName("a grant with isActive null is live; only an explicit false deactivates")
    void onlyAnExplicitFalseDeactivatesAGrant() throws Exception {
        hrmsRespondsWith("{\"Employees\":[{\"jurisdictions\":["
                + "{\"boundary\":\"S1\",\"boundaryType\":\"State\",\"isActive\":false},"
                + "{\"boundary\":\"S2\",\"boundaryType\":\"State\",\"isActive\":true},"
                + "{\"boundary\":\"S3\",\"boundaryType\":\"State\"}"
                + "]}]}");

        JurisdictionGrants grants = hrmsUtils.getActiveJurisdictionsForCaller(caller("u1"));

        assertTrue(grants.isOk());
        assertEquals(2, grants.grants().size(),
                "rows written before the isActive column existed carry null and are live grants");
        assertEquals("S2", grants.grants().get(0).getBoundary());
        assertEquals("S3", grants.grants().get(1).getBoundary());
    }

    @Test
    @DisplayName("a grant with no boundary is dropped rather than producing a null in the ceiling")
    void aGrantWithoutABoundaryIsDropped() throws Exception {
        hrmsRespondsWith("{\"Employees\":[{\"jurisdictions\":[{\"boundaryType\":\"State\",\"isActive\":true}]}]}");

        assertTrue(hrmsUtils.getActiveJurisdictionsForCaller(caller("u1")).grants().isEmpty());
    }

    @Test
    @DisplayName("no caller identity is reported as such, not as an empty set of grants")
    void aMissingCallerUuidIsItsOwnOutcome() {
        JurisdictionGrants grants = hrmsUtils.getActiveJurisdictionsForCaller(new RequestInfo());

        assertEquals(JurisdictionGrants.Status.NO_CALLER_UUID, grants.status());
    }

    @Test
    @DisplayName("an unknown employee is distinguishable from HRMS being down")
    void anUnknownEmployeeIsNotAnOutage() throws Exception {
        hrmsRespondsWith("{\"Employees\":[]}");

        assertEquals(JurisdictionGrants.Status.EMPLOYEE_NOT_FOUND,
                hrmsUtils.getActiveJurisdictionsForCaller(caller("u1")).status());
    }

    @Test
    @DisplayName("HRMS throwing becomes a reported failure, never a propagated 500")
    void aTransportFailureIsReportedNotThrown() {
        when(serviceRequestRepository.fetchResult(any(), any()))
                .thenThrow(new RuntimeException("connection refused"));

        JurisdictionGrants grants = hrmsUtils.getActiveJurisdictionsForCaller(caller("u1"));

        assertEquals(JurisdictionGrants.Status.HRMS_UNAVAILABLE, grants.status(),
                "the resolver needs the failure mode to decide; an exception here would be a 500");
    }
}
