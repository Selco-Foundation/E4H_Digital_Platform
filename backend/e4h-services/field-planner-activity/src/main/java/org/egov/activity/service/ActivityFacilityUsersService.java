package org.egov.activity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.repository.ActivityFacilityUserRepository;
import org.egov.activity.repository.BomRepository;
import org.egov.activity.service.enrichment.ActivityFacilityUserEnrichment;
import org.egov.activity.util.ActivityServiceUtil;
import org.egov.activity.validator.ActivityFacilityUserValidator;
import org.egov.activity.web.models.ActivityFacilityUser;
import org.egov.activity.web.models.ActivityFacilityUserBulkRequest;
import org.egov.activity.web.models.ActivityFacilityUserSearchCriteria;
import org.egov.activity.web.models.ActivityFacilityUserSearchRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.producer.Producer;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.egov.common.utils.CommonUtils.*;

@Service
@Slf4j
public class ActivityFacilityUsersService {

    private final BomRepository bomRepository;

    private final Producer producer;

    private final ActivityServiceUtil activityServiceUtil;
    private final ActivityFacilityUserEnrichment facilityUserEnrichment;

    private final ActivityFacilityUserValidator facilityUserValidator;

    private final ActivityConfiguration activityConfiguration;
    private final ActivityFacilityUserRepository activityFacilityUserRepository;

    private ServiceRequestRepository serviceRequest;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    @Autowired
    public ActivityFacilityUsersService(
            BomRepository bomRepository, ActivityFacilityUserEnrichment facilityUserEnrichment, ActivityConfiguration activityConfiguration, ActivityFacilityUserValidator facilityUserValidator, ActivityFacilityUserRepository activityFacilityUserRepository, ServiceRequestRepository serviceRequest,
            Producer producer, ActivityServiceUtil activityServiceUtil, @Qualifier("objectMapper") ObjectMapper mapper) {
        this.activityFacilityUserRepository = activityFacilityUserRepository;
        this.producer = producer;
            this.activityConfiguration = activityConfiguration;
            this.bomRepository = bomRepository;
            this.facilityUserEnrichment = facilityUserEnrichment;
            this.activityServiceUtil = activityServiceUtil;
            this.mapper = mapper;
            this.facilityUserValidator = facilityUserValidator;
            this.serviceRequest = serviceRequest;
    }

    private static final int FACILITY_USER_DUPLICATE_CHECK_CHUNK_SIZE = 500;

    public List<ActivityFacilityUser> createActivityFacilityUsers(ActivityFacilityUserBulkRequest request) throws Exception {
        log.info("received request to create bulk activity facility users");

        facilityUserValidator.validateCreateActivityFacilityUsersRequest(request);
        List<ActivityFacilityUser> activityFacilityUsers = request.getActivityFacilityUsers();
        log.info("received activityFacilityUsers to create bulk activity facility users size {}", activityFacilityUsers.size());

        // Prefetch existing (activityFacilityId, userId) assignments in one/few chunked queries instead of
        // one duplicate-check query per user - avoids O(n) DB round trips for large batches.
        Set<String> existingPairs = findExistingFacilityUserPairs(activityFacilityUsers, request.getRequestInfo());

        for (ActivityFacilityUser facilityUser : activityFacilityUsers) {
            String pairKey = facilityUser.getActivityFacilityId() + "|" + facilityUser.getUserId();
            if (existingPairs.contains(pairKey)) {
                log.error("User already assigned to this activity facility");
                throw new CustomException("FACILITY_ASSIGN_USER", "User "+facilityUser.getUserId() +" already assigned to this activity facility "+facilityUser.getActivityFacilityId());
            }
            log.info("processing {} valid entities", facilityUser);
            facilityUserEnrichment.enrichActivityFacilityUserOnCreate(facilityUser, request.getRequestInfo());
        }

        producer.push(activityConfiguration.getCreateFacilityUserTopic(), request);
        log.info("successfully created activity facility");

        return activityFacilityUsers;
    }

    /**
     * Like {@link #createActivityFacilityUsers}, but skips pairs that already exist rather than
     * throwing.
     *
     * The assignment fan-out is re-entrant by design - the project manager's screen replays the
     * whole staffing grid on every save - so a duplicate is the normal case, not an error. The
     * throwing variant stays for {@code /facility-users/_create}, whose callers rely on it.
     *
     * @return only the rows actually published
     */
    public List<ActivityFacilityUser> createMissingActivityFacilityUsers(ActivityFacilityUserBulkRequest request) throws Exception {
        List<ActivityFacilityUser> requested = request.getActivityFacilityUsers();
        if (requested == null || requested.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> existingPairs = findExistingFacilityUserPairs(requested, request.getRequestInfo());
        List<ActivityFacilityUser> missing = new ArrayList<>();
        for (ActivityFacilityUser facilityUser : requested) {
            if (existingPairs.contains(facilityUser.getActivityFacilityId() + "|" + facilityUser.getUserId())) {
                continue;
            }
            facilityUserEnrichment.enrichActivityFacilityUserOnCreate(facilityUser, request.getRequestInfo());
            missing.add(facilityUser);
        }
        if (missing.isEmpty()) {
            return missing;
        }

        // Publish only the new rows. Reusing the incoming request would re-insert the ones we
        // just decided to skip.
        ActivityFacilityUserBulkRequest toPublish = ActivityFacilityUserBulkRequest.builder()
                .requestInfo(request.getRequestInfo())
                .activityFacilityUsers(missing)
                .build();
        producer.push(activityConfiguration.getCreateFacilityUserTopic(), toPublish);
        log.info("Created {} activity facility user row(s), skipped {} already present",
                missing.size(), requested.size() - missing.size());
        return missing;
    }

    /**
     * Soft-deletes the live rows matching any of the given (activityFacilityId, userId) pairs.
     *
     * Resolves ids by search first, because {@link #delete} needs the row id and the caller -
     * the assignment fan-out - only knows the pair.
     */
    public List<ActivityFacilityUser> softDeleteByFacilityAndUser(RequestInfo requestInfo,
                                                                  Collection<String> activityFacilityIds,
                                                                  Collection<String> userIds,
                                                                  String tenantId) throws Exception {
        if (activityFacilityIds == null || activityFacilityIds.isEmpty() || userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> facilityIdList = new ArrayList<>(new LinkedHashSet<>(activityFacilityIds));
        List<String> userIdList = new ArrayList<>(new LinkedHashSet<>(userIds));
        List<ActivityFacilityUser> matched = new ArrayList<>();

        for (int i = 0; i < facilityIdList.size(); i += FACILITY_USER_DUPLICATE_CHECK_CHUNK_SIZE) {
            int end = Math.min(i + FACILITY_USER_DUPLICATE_CHECK_CHUNK_SIZE, facilityIdList.size());
            ActivityFacilityUserSearchCriteria criteria = ActivityFacilityUserSearchCriteria.builder()
                    .activityFacilityId(new ArrayList<>(facilityIdList.subList(i, end)))
                    .userId(userIdList)
                    .build();
            ActivityFacilityUserSearchRequest searchRequest = ActivityFacilityUserSearchRequest.builder()
                    .criteria(criteria)
                    .requestInfo(requestInfo)
                    .build();
            int chunkLimit = Math.max((end - i) * userIdList.size(), 1000);
            SearchResponse<ActivityFacilityUser> response = search(searchRequest, chunkLimit, 0, tenantId, null, false);
            if (response != null && response.getResponse() != null) {
                matched.addAll(response.getResponse());
            }
        }

        if (matched.isEmpty()) {
            return matched;
        }
        delete(ActivityFacilityUserBulkRequest.builder()
                .requestInfo(requestInfo)
                .activityFacilityUsers(matched)
                .build());
        return matched;
    }

    private Set<String> findExistingFacilityUserPairs(List<ActivityFacilityUser> activityFacilityUsers, RequestInfo requestInfo) throws Exception {
        List<String> activityFacilityIds = activityFacilityUsers.stream()
                .map(ActivityFacilityUser::getActivityFacilityId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<String> userIds = activityFacilityUsers.stream()
                .map(ActivityFacilityUser::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (activityFacilityIds.isEmpty() || userIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> existingPairs = new HashSet<>();
        for (int i = 0; i < activityFacilityIds.size(); i += FACILITY_USER_DUPLICATE_CHECK_CHUNK_SIZE) {
            int end = Math.min(i + FACILITY_USER_DUPLICATE_CHECK_CHUNK_SIZE, activityFacilityIds.size());
            List<String> facilityIdChunk = new ArrayList<>(activityFacilityIds.subList(i, end));

            ActivityFacilityUserSearchCriteria searchCriteria = ActivityFacilityUserSearchCriteria.builder()
                    .activityFacilityId(facilityIdChunk)
                    .userId(new ArrayList<>(userIds))
                    .build();
            ActivityFacilityUserSearchRequest searchRequest = ActivityFacilityUserSearchRequest.builder()
                    .criteria(searchCriteria)
                    .requestInfo(requestInfo)
                    .build();

            int chunkLimit = Math.max(facilityIdChunk.size() * userIds.size(), 1000);
            SearchResponse<ActivityFacilityUser> response = search(searchRequest, chunkLimit, 0, "in", null, false);
            if (response != null && response.getResponse() != null) {
                for (ActivityFacilityUser existing : response.getResponse()) {
                    existingPairs.add(existing.getActivityFacilityId() + "|" + existing.getUserId());
                }
            }
        }
        return existingPairs;
    }

    public SearchResponse<ActivityFacilityUser> search(ActivityFacilityUserSearchRequest searchRequest,
                                               Integer limit,
                                               Integer offset,
                                               String tenantId,
                                               Long lastChangedSince,
                                               Boolean includeDeleted) throws Exception {
        log.info("received request to search project staff");

        if (isSearchByIdOnly(searchRequest.getCriteria())) {
            log.info("searching activity facility staff by id");
            List<String> ids = searchRequest.getCriteria().getId();
            log.info("fetching activity facility staff with ids: {}", ids);
            List<ActivityFacilityUser> activityFacilityUsers = activityFacilityUserRepository.findById(ids, includeDeleted).stream()
                    .filter(lastChangedSince(lastChangedSince))
                    .filter(havingTenantId(tenantId))
                    .filter(includeDeleted(includeDeleted))
                    .toList();
            return SearchResponse.<ActivityFacilityUser>builder().response(activityFacilityUsers).build();
        }
        log.info("searching project staff using criteria");
        return activityFacilityUserRepository.findWithCount(searchRequest.getCriteria(),
                limit, offset, tenantId, lastChangedSince, includeDeleted);
    }

    public List<ActivityFacilityUser> update(ActivityFacilityUserBulkRequest request) {
        log.info("received request to update bulk activity facility staff");
        facilityUserValidator.validateCreateActivityFacilityUsersRequest(request);
        List<ActivityFacilityUser> validEntities = request.getActivityFacilityUsers();
        try {
            if (!validEntities.isEmpty()) {
                for (ActivityFacilityUser facilityUser : validEntities) {
                    facilityUserEnrichment.enrichActivityFacilityUserRequestOnUpdate(facilityUser, request.getRequestInfo());
                }
                // Once, outside the loop. Pushing inside it published the whole N-row request N
                // times - harmless for a single row, quadratic for a large plan.
                log.debug("Pushing update to topic: {}", activityConfiguration.getUpdateFacilityUserTopic());
                producer.push(activityConfiguration.getUpdateFacilityUserTopic(), request);
                log.info("Successfully updated {} activity facility users", validEntities.size());
            }
        } catch (Exception exception) {
            log.error("error occurred while updating project staff", ExceptionUtils.getStackTrace(exception));
        }

        return validEntities;
    }

    public List<ActivityFacilityUser> delete(ActivityFacilityUserBulkRequest request) {
        log.info("received request to delete bulk activity facility staff");
        facilityUserValidator.validateCreateActivityFacilityUsersRequest(request);
        List<ActivityFacilityUser> validEntities = request.getActivityFacilityUsers();
        try {
            if (!validEntities.isEmpty()) {
                for (ActivityFacilityUser facilityUser : validEntities) {
                    facilityUser.setIsDeleted(true);
                    facilityUserEnrichment.enrichActivityFacilityUserRequestOnUpdate(facilityUser, request.getRequestInfo());
                }
                // Once, outside the loop - see update().
                log.debug("Pushing delete update to topic: {}", activityConfiguration.getUpdateFacilityUserTopic());
                producer.push(activityConfiguration.getUpdateFacilityUserTopic(), request);
                log.info("Successfully marked {} activity facility users as deleted", validEntities.size());
            }
        } catch (Exception exception) {
            log.error("error occurred while updating project staff", ExceptionUtils.getStackTrace(exception));
        }

        return validEntities;
    }


}
