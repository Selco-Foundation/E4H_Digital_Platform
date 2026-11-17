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
                    producer.push(activityConfiguration.getUpdateFacilityUserTopic(), request);
                    log.info("successfully updated bulk project staff");
                }
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
                    producer.push(activityConfiguration.getUpdateFacilityUserTopic(), request);
                    log.info("successfully updated bulk project staff");
                }
            }
        } catch (Exception exception) {
            log.error("error occurred while updating project staff", ExceptionUtils.getStackTrace(exception));
        }

        return validEntities;
    }


}
