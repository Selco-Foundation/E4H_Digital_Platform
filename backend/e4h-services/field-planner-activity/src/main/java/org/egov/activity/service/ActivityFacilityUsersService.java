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
        log.trace("createActivityFacilityUsers method invoked");
        log.info("Received request to create bulk activity facility users");
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
            log.trace("Enriching activity facility user, userId: {}, activityFacilityId: {}", facilityUser.getUserId(), facilityUser.getActivityFacilityId());
            facilityUserEnrichment.enrichActivityFacilityUserOnCreate(facilityUser, request.getRequestInfo());
        }

        log.debug("Pushing activity facility users to topic: {}", activityConfiguration.getCreateFacilityUserTopic());
        producer.push(activityConfiguration.getCreateFacilityUserTopic(), request);
        log.info("Successfully created {} activity facility users", userCount);

        return activityFacilityUsers;
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
        log.trace("search method invoked with limit: {}, offset: {}, tenantId: {}", limit, offset, tenantId);
        log.info("Received request to search activity facility users");
        if (isSearchByIdOnly(searchRequest.getCriteria())) {
            List<String> ids = searchRequest.getCriteria().getId();
            int idCount = ids != null ? ids.size() : 0;
            log.debug("Searching activity facility users by ID, count: {}", idCount);
            List<ActivityFacilityUser> activityFacilityUsers = activityFacilityUserRepository.findById(ids, includeDeleted).stream()
                    .filter(lastChangedSince(lastChangedSince))
                    .filter(havingTenantId(tenantId))
                    .filter(includeDeleted(includeDeleted))
                    .toList();
            log.debug("Retrieved {} activity facility users by ID", activityFacilityUsers.size());
            return SearchResponse.<ActivityFacilityUser>builder().response(activityFacilityUsers).build();
        }
        log.debug("Searching activity facility users using criteria");
        SearchResponse<ActivityFacilityUser> result = activityFacilityUserRepository.findWithCount(searchRequest.getCriteria(),
                limit, offset, tenantId, lastChangedSince, includeDeleted);
        int resultCount = result.getResponse() != null ? result.getResponse().size() : 0;
        log.debug("Retrieved {} activity facility users using criteria", resultCount);
        return result;
    }

    public List<ActivityFacilityUser> update(ActivityFacilityUserBulkRequest request) {
        log.trace("update method invoked");
        log.info("Received request to update bulk activity facility users");
        facilityUserValidator.validateCreateActivityFacilityUsersRequest(request);
        List<ActivityFacilityUser> validEntities = request.getActivityFacilityUsers();
        int updateCount = validEntities != null ? validEntities.size() : 0;
        log.debug("Processing {} activity facility users for update", updateCount);
        try {
            if (!validEntities.isEmpty()) {
                for (ActivityFacilityUser facilityUser : validEntities) {
                    log.trace("Updating activity facility user, userId: {}, activityFacilityId: {}", facilityUser.getUserId(), facilityUser.getActivityFacilityId());
                    facilityUserEnrichment.enrichActivityFacilityUserRequestOnUpdate(facilityUser, request.getRequestInfo());
                    log.debug("Pushing update to topic: {}", activityConfiguration.getUpdateFacilityUserTopic());
                    producer.push(activityConfiguration.getUpdateFacilityUserTopic(), request);
                }
                log.info("Successfully updated {} activity facility users", updateCount);
            }
        } catch (Exception exception) {
            log.error("Error occurred while updating activity facility users, count: {}", updateCount, exception);
        }

        return validEntities;
    }

    public List<ActivityFacilityUser> delete(ActivityFacilityUserBulkRequest request) {
        log.trace("delete method invoked");
        log.info("Received request to delete bulk activity facility users");
        facilityUserValidator.validateCreateActivityFacilityUsersRequest(request);
        List<ActivityFacilityUser> validEntities = request.getActivityFacilityUsers();
        int deleteCount = validEntities != null ? validEntities.size() : 0;
        log.debug("Processing {} activity facility users for deletion", deleteCount);
        try {
            if (!validEntities.isEmpty()) {
                for (ActivityFacilityUser facilityUser : validEntities) {
                    log.trace("Deleting activity facility user, userId: {}, activityFacilityId: {}", facilityUser.getUserId(), facilityUser.getActivityFacilityId());
                    facilityUser.setIsDeleted(true);
                    facilityUserEnrichment.enrichActivityFacilityUserRequestOnUpdate(facilityUser, request.getRequestInfo());
                    log.debug("Pushing delete update to topic: {}", activityConfiguration.getUpdateFacilityUserTopic());
                    producer.push(activityConfiguration.getUpdateFacilityUserTopic(), request);
                }
                log.info("Successfully marked {} activity facility users as deleted", deleteCount);
            }
        } catch (Exception exception) {
            log.error("Error occurred while deleting activity facility users, count: {}", deleteCount, exception);
        }

        return validEntities;
    }


}
