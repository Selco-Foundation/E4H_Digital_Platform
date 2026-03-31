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
import org.egov.common.models.core.SearchResponse;
import org.egov.common.producer.Producer;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public List<ActivityFacilityUser> createActivityFacilityUsers(ActivityFacilityUserBulkRequest request) throws Exception {
        log.info("received request to create bulk activity facility users");

        facilityUserValidator.validateCreateActivityFacilityUsersRequest(request);
        List<ActivityFacilityUser> activityFacilityUsers = request.getActivityFacilityUsers();
        log.info("received activityFacilityUsers to create bulk activity facility users size {}", activityFacilityUsers);
        for (ActivityFacilityUser facilityUser : activityFacilityUsers) {
            ActivityFacilityUserSearchCriteria searchCriteria = ActivityFacilityUserSearchCriteria.builder()
                    .activityFacilityId(new ArrayList<>(List.of(facilityUser.getActivityFacilityId())))
                    .userId(new ArrayList<>(List.of(facilityUser.getUserId())))
                    .build();
            ActivityFacilityUserSearchRequest searchRequest = ActivityFacilityUserSearchRequest.builder()
                    .criteria(searchCriteria)
                    .requestInfo(request.getRequestInfo())
                    .build();

            SearchResponse<ActivityFacilityUser> response = search(searchRequest, 10,0, "in", null, false);
            if (response!=null && response.getResponse() != null && !response.getResponse().isEmpty()){
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
