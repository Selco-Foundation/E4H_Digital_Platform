package facility.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import facility.kafka.Producer;
import facility.util.PocPhoneCipher;
import facility.web.models.FacilityUpdateRequest;
import facility.web.models.FacilityUpdateRequestFacilityUpdate;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** "Last changed by / at" is stamped by the service on every update push, never by the client. */
@ExtendWith(MockitoExtension.class)
class FacilityUpdateStampTest {

    @Mock private Producer producer;
    @Mock private ObjectMapper objectMapper;
    @Mock private PocPhoneCipher pocPhoneCipher;

    @InjectMocks private FacilityRepository facilityRepository;

    @Test
    void stampsTheCallerAndTheTime() {
        RequestInfo requestInfo = new RequestInfo();
        User user = new User();
        user.setUuid("admin-uuid");
        requestInfo.setUserInfo(user);
        FacilityUpdateRequest request = FacilityUpdateRequest.builder().requestInfo(requestInfo)
                .facilityUpdate(FacilityUpdateRequestFacilityUpdate.builder().facilityId("F1").build()).build();
        long before = System.currentTimeMillis();

        facilityRepository.pushUpdateFacility(request);

        assertEquals("admin-uuid", request.getFacilityUpdate().getUpdatedBy());
        assertNotNull(request.getFacilityUpdate().getUpdatedAt());
        assertTrue(request.getFacilityUpdate().getUpdatedAt() >= before);
    }

    @Test
    void anUpdateWithNoSignedInUserLeavesUpdatedByEmpty() {
        FacilityUpdateRequest request = FacilityUpdateRequest.builder()
                .facilityUpdate(FacilityUpdateRequestFacilityUpdate.builder().facilityId("F1").build()).build();

        facilityRepository.pushUpdateFacility(request);

        assertNull(request.getFacilityUpdate().getUpdatedBy());
        assertNotNull(request.getFacilityUpdate().getUpdatedAt());
    }

    @Test
    void clientCannotSetTheStampButThePersisterStillReceivesIt() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FacilityUpdateRequestFacilityUpdate parsed = mapper.readValue(
                "{\"facility_id\":\"F1\",\"updated_by\":\"someone\",\"updated_at\":1}",
                FacilityUpdateRequestFacilityUpdate.class);
        assertNull(parsed.getUpdatedBy());
        assertNull(parsed.getUpdatedAt());

        parsed.setUpdatedBy("admin-uuid");
        parsed.setUpdatedAt(5L);
        Map<?, ?> serialized = mapper.convertValue(parsed, Map.class);
        assertEquals("admin-uuid", serialized.get("updated_by"));
        assertEquals(5L, ((Number) serialized.get("updated_at")).longValue());
    }
}
