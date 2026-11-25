package org.egov.activity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.repository.ActivityFacilityUserRepository;
import org.egov.activity.repository.BomRepository;
import org.egov.activity.service.enrichment.ActivityFacilityUserEnrichment;
import org.egov.activity.util.ActivityServiceUtil;
import org.egov.activity.validator.ActivityFacilityUserValidator;
import org.egov.activity.web.models.ActivityFacilityUser;
import org.egov.activity.web.models.ActivityFacilityUserBulkRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.producer.Producer;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Writing and revoking the rows that grant per-facility visibility.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivityFacilityUsersServiceTest {

    private static final String CREATE_TOPIC = "create-facility-user";
    private static final String UPDATE_TOPIC = "update-facility-user";

    @Mock private BomRepository bomRepository;
    @Mock private ActivityFacilityUserEnrichment enrichment;
    @Mock private ActivityFacilityUserValidator validator;
    @Mock private ActivityFacilityUserRepository repository;
    @Mock private ServiceRequestRepository serviceRequestRepository;
    @Mock private Producer producer;
    @Mock private ActivityServiceUtil activityServiceUtil;

    private ActivityFacilityUsersService service;

    @BeforeEach
    void setUp() {
        ActivityConfiguration config = new ActivityConfiguration();
        config.setCreateFacilityUserTopic(CREATE_TOPIC);
        config.setUpdateFacilityUserTopic(UPDATE_TOPIC);
        service = new ActivityFacilityUsersService(bomRepository, enrichment, config, validator, repository,
                serviceRequestRepository, producer, activityServiceUtil, new ObjectMapper());
    }

    private static ActivityFacilityUser row(String facilityId, String userId) {
        return ActivityFacilityUser.builder()
                .activityFacilityId(facilityId).userId(userId).tenantId("in").isDeleted(false).build();
    }

    private static ActivityFacilityUserBulkRequest bulk(ActivityFacilityUser... rows) {
        return ActivityFacilityUserBulkRequest.builder()
                .requestInfo(new RequestInfo()).activityFacilityUsers(List.of(rows)).build();
    }

    private void existingRows(ActivityFacilityUser... rows) throws Exception {
        when(repository.findWithCount(any(), anyInt(), anyInt(), anyString(), any(), anyBoolean()))
                .thenReturn(SearchResponse.<ActivityFacilityUser>builder().response(List.of(rows)).build());
    }

    @Test
    @DisplayName("the fan-out's create publishes only the rows that were missing")
    void createMissingSkipsWhatIsAlreadyThere() throws Exception {
        existingRows(row("fa-1", "u1"));

        List<ActivityFacilityUser> created = service.createMissingActivityFacilityUsers(
                bulk(row("fa-1", "u1"), row("fa-2", "u1")));

        assertEquals(1, created.size(), "fa-1 already had a row");
        assertEquals("fa-2", created.get(0).getActivityFacilityId());

        ArgumentCaptor<ActivityFacilityUserBulkRequest> published =
                ArgumentCaptor.forClass(ActivityFacilityUserBulkRequest.class);
        verify(producer).push(eq(CREATE_TOPIC), published.capture());
        // Republishing the original request would re-insert the row we just decided to skip.
        assertEquals(List.of("fa-2"), published.getValue().getActivityFacilityUsers().stream()
                .map(ActivityFacilityUser::getActivityFacilityId).collect(Collectors.toList()));
    }

    @Test
    @DisplayName("nothing missing means nothing published")
    void createMissingPublishesNothingWhenAllPresent() throws Exception {
        existingRows(row("fa-1", "u1"));

        assertEquals(List.of(), service.createMissingActivityFacilityUsers(bulk(row("fa-1", "u1"))));
        verify(producer, times(0)).push(eq(CREATE_TOPIC), any());
    }

    @Test
    @DisplayName("the public create still rejects a duplicate - its callers rely on that")
    void theThrowingVariantStillThrows() throws Exception {
        existingRows(row("fa-1", "u1"));

        assertThrows(CustomException.class,
                () -> service.createActivityFacilityUsers(bulk(row("fa-1", "u1"))));
    }

    @Test
    @DisplayName("a bulk soft-delete publishes once, not once per row")
    void deletePublishesOnce() {
        service.delete(bulk(row("fa-1", "u1"), row("fa-2", "u1"), row("fa-3", "u1")));

        // Pushing inside the loop republished the whole N-row request N times - quadratic once a
        // plan-wide unassign runs through it.
        verify(producer, times(1)).push(eq(UPDATE_TOPIC), any());
    }

    @Test
    @DisplayName("a bulk update publishes once too")
    void updatePublishesOnce() {
        service.update(bulk(row("fa-1", "u1"), row("fa-2", "u1")));

        verify(producer, times(1)).push(eq(UPDATE_TOPIC), any());
    }
}
