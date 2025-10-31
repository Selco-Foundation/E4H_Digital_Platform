package org.egov.activity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.repository.BomRepository;
import org.egov.activity.service.enrichment.ActivityFacilityUserEnrichment;
import org.egov.activity.util.ActivityServiceUtil;
import org.egov.activity.validator.ActivityFacilityUserValidator;
import org.egov.activity.web.models.*;
import org.egov.common.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.egov.common.utils.CommonUtils.populateErrorDetails;

@Service
@Slf4j
public class ActivityFacilityUsersService {

    private final BomRepository bomRepository;

    private final Producer producer;

    private final ActivityServiceUtil activityServiceUtil;
    private final ActivityFacilityUserEnrichment facilityUserEnrichment;

    private final ActivityFacilityUserValidator facilityUserValidator;

    private final ActivityConfiguration activityConfiguration;

    private ServiceRequestRepository serviceRequest;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    @Autowired
    public ActivityFacilityUsersService(
            BomRepository bomRepository, ActivityFacilityUserEnrichment facilityUserEnrichment, ActivityConfiguration activityConfiguration, ActivityFacilityUserValidator facilityUserValidator, ServiceRequestRepository serviceRequest,
            Producer producer, ActivityServiceUtil activityServiceUtil, @Qualifier("objectMapper") ObjectMapper mapper) {
            this.producer = producer;
            this.activityConfiguration = activityConfiguration;
            this.bomRepository = bomRepository;
            this.facilityUserEnrichment = facilityUserEnrichment;
            this.activityServiceUtil = activityServiceUtil;
            this.mapper = mapper;
            this.facilityUserValidator = facilityUserValidator;
            this.serviceRequest = serviceRequest;
    }

    public List<ActivityFacilityUser> createActivityFacilityUsers(ActivityFacilityUserBulkRequest request) {
        log.info("received request to create bulk activity facility users");

        facilityUserValidator.validateCreateActivityFacilityUsersRequest(request);
        List<ActivityFacilityUser> activityFacilityUsers = request.getActivityFacilityUsers();
        try {
            for (ActivityFacilityUser facilityUser : activityFacilityUsers) {
                log.info("processing {} valid entities", facilityUser);
                facilityUserEnrichment.enrichActivityFacilityUserOnCreate(facilityUser, request.getRequestInfo());
            }
            producer.push(activityConfiguration.getCreateFacilityUserTopic(), request);
            log.info("successfully created activity facility");
        } catch (Exception exception) {
            log.error("error occurred while creating project facility: {}", ExceptionUtils.getStackTrace(exception));
        }

        return activityFacilityUsers;
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
