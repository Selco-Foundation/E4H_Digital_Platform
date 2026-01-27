package org.egov.project.validator.staff;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.contract.request.User;
import org.egov.common.contract.user.UserSearchRequest;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.Error;
import org.egov.common.models.individual.Individual;
import org.egov.common.models.individual.IndividualBulkResponse;
import org.egov.common.models.individual.IndividualSearch;
import org.egov.common.models.individual.IndividualSearchRequest;
import org.egov.common.models.project.ProjectStaff;
import org.egov.common.models.project.ProjectStaffBulkRequest;
import org.egov.common.service.UserService;
import org.egov.common.validator.Validator;
import org.egov.project.config.ProjectConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.common.utils.ValidatorUtils.getErrorForEntityWithNetworkError;
import static org.egov.common.utils.ValidatorUtils.getErrorForNonExistentRelatedEntity;
import static org.egov.project.Constants.GET_USER_ID;

@Component
@Order(value = 7)
@Slf4j
public class PsUserIdValidator implements Validator<ProjectStaffBulkRequest, ProjectStaff> {

    private final UserService userService;

    private final ProjectConfiguration projectConfiguration;

    private final ServiceRequestClient serviceRequestClient;

    public PsUserIdValidator(UserService userService,
                             ProjectConfiguration projectConfiguration,
                             ServiceRequestClient serviceRequestClient) {
        this.userService = userService;
        this.projectConfiguration = projectConfiguration;
        this.serviceRequestClient = serviceRequestClient;
    }

    @Override
    public Map<ProjectStaff, List<Error>> validate(ProjectStaffBulkRequest request) {
        log.info("validating for user id");
        List<ProjectStaff> entities = request.getProjectStaff();
        Map<ProjectStaff, List<Error>> errorDetailsMap = new HashMap<>();

        List<String> userIds = extractUserIds(entities);
        if (userIds.isEmpty()) {
            return errorDetailsMap;
        }

        final String tenantId = getTenantId(entities);
        Map<String, ProjectStaff> uMap = getIdToObjMap(entities, getMethod(GET_USER_ID, getObjClass(entities)));

        try {
            List<String> validUserIds = validateUserIds(userIds, tenantId);
            addErrorsForInvalidUsers(uMap, validUserIds, errorDetailsMap);
        } catch (Exception exception) {
            log.error("error while validating users", ExceptionUtils.getStackTrace(exception));
            addNetworkErrors(entities, errorDetailsMap);
        }

        return errorDetailsMap;
    }

    private List<String> extractUserIds(List<ProjectStaff> entities) {
        return entities.stream()
                .filter(notHavingErrors())
                .map(ProjectStaff::getUserId)
                .distinct()
                .toList();
    }

    private List<String> validateUserIds(List<String> userIds, String tenantId) {
        String validatorType = projectConfiguration.getEgovUserIdValidator();
        
        if ("egov-user".equalsIgnoreCase(validatorType)) {
            return validateUsingEgovUser(userIds, tenantId);
        } else if ("individual".equalsIgnoreCase(validatorType)) {
            return validateUsingIndividual(userIds, tenantId);
        }
        
        return new ArrayList<>();
    }

    private List<String> validateUsingEgovUser(List<String> userIds, String tenantId) {
        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setTenantId(tenantId);
        userSearchRequest.setUuid(userIds);
        return userService.search(userSearchRequest)
                .stream()
                .map(User::getUuid)
                .toList();
    }

    private List<String> validateUsingIndividual(List<String> userIds, String tenantId) {
        IndividualSearchRequest individualSearchRequest = IndividualSearchRequest.builder()
                .individual(IndividualSearch.builder()
                        .id(userIds)
                        .build())
                .build();
        
        String url = buildIndividualSearchUrl(tenantId);
        IndividualBulkResponse response = serviceRequestClient.fetchResult(
                new StringBuilder(url),
                individualSearchRequest,
                IndividualBulkResponse.class);
        
        return response.getIndividual().stream()
                .map(Individual::getId)
                .toList();
    }

    private String buildIndividualSearchUrl(String tenantId) {
        return projectConfiguration.getIndividualServiceHost()
                + projectConfiguration.getIndividualServiceSearchUrl()
                + "?limit=" + projectConfiguration.getSearchApiLimit()
                + "&offset=0&tenantId=" + tenantId;
    }

    private void addErrorsForInvalidUsers(Map<String, ProjectStaff> uMap, List<String> validUserIds, 
                                         Map<ProjectStaff, List<Error>> errorDetailsMap) {
        for (Map.Entry<String, ProjectStaff> entry : uMap.entrySet()) {
            if (!validUserIds.contains(entry.getKey())) {
                ProjectStaff staff = entry.getValue();
                Error error = getErrorForNonExistentRelatedEntity(staff.getUserId());
                populateErrorDetails(staff, error, errorDetailsMap);
            }
        }
    }

    private void addNetworkErrors(List<ProjectStaff> entities, Map<ProjectStaff, List<Error>> errorDetailsMap) {
        entities.stream()
                .filter(notHavingErrors())
                .forEach(b -> {
                    Error error = getErrorForEntityWithNetworkError();
                    populateErrorDetails(b, error, errorDetailsMap);
                });
    }
}