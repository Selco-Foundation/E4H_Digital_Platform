package org.egov.project.validator.useraction;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.Error;
import org.egov.common.models.core.Boundary;
import org.egov.common.models.project.useraction.UserAction;
import org.egov.common.models.project.useraction.UserActionBulkRequest;
import org.egov.common.validator.Validator;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.web.models.boundary.BoundaryResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.populateErrorDetails;

/**
 * Validator class for validating userAction boundaries.
 */
@Component
@Order(value = 4)
@Slf4j
public class UaBoundaryValidator implements Validator<UserActionBulkRequest, UserAction> {

    private final ServiceRequestClient serviceRequestClient;

    private final ProjectConfiguration projectConfiguration;

    /**
     * Constructor to initialize the HBoundaryValidator.
     *
     * @param serviceRequestClient Service request client for making HTTP requests
     * @param projectConfiguration Configuration properties for the userAction module
     */
    public UaBoundaryValidator(ServiceRequestClient serviceRequestClient, ProjectConfiguration projectConfiguration) {
        this.serviceRequestClient = serviceRequestClient;
        this.projectConfiguration = projectConfiguration;
    }

    /**
     * Validates the userActions' boundaries.
     *
     * @param request the bulk request containing userActions
     * @return a map containing userActions with their corresponding list of errors
     */
    @Override
    public Map<UserAction, List<Error>> validate(UserActionBulkRequest request) {
        log.debug("Validating userActions boundaries.");
        HashMap<UserAction, List<Error>> errorDetailsMap = new HashMap<>();

        List<UserAction> entitiesWithValidBoundaries = filterUserActionsWithBoundaries(request.getUserActions());
        Map<String, List<UserAction>> tenantIdUserActionMap = groupByTenantId(entitiesWithValidBoundaries);

        tenantIdUserActionMap.forEach((tenantId, userActions) -> {
            validateBoundariesForTenant(tenantId, userActions, request, errorDetailsMap);
        });

        return errorDetailsMap;
    }

    private List<UserAction> filterUserActionsWithBoundaries(List<UserAction> userActions) {
        return userActions.parallelStream()
                .filter(userAction -> Objects.nonNull(userAction.getBoundaryCode()))
                .toList();
    }

    private Map<String, List<UserAction>> groupByTenantId(List<UserAction> userActions) {
        return userActions.stream()
                .collect(Collectors.groupingBy(UserAction::getTenantId));
    }

    private void validateBoundariesForTenant(String tenantId, List<UserAction> userActions, 
                                             UserActionBulkRequest request, 
                                             Map<UserAction, List<Error>> errorDetailsMap) {
        Map<String, List<UserAction>> boundaryCodeUserActionsMap = groupByBoundaryCode(userActions);
        List<String> boundaries = new ArrayList<>(boundaryCodeUserActionsMap.keySet());
        
        if (CollectionUtils.isEmpty(boundaries)) {
            return;
        }

        try {
            BoundaryResponse boundarySearchResponse = fetchBoundaryDetails(tenantId, boundaries, request);
            List<String> invalidBoundaryCodes = findInvalidBoundaryCodes(boundaries, boundarySearchResponse);
            addErrorsForInvalidBoundaries(boundaryCodeUserActionsMap, invalidBoundaryCodes, errorDetailsMap);
        } catch (Exception e) {
            log.error("Exception while searching boundaries for tenantId: {}", tenantId, e);
            throw new CustomException("BOUNDARY_SERVICE_SEARCH_ERROR", 
                    "Error in while fetching boundaries from Boundary Service : " + e.getMessage());
        }
    }

    private Map<String, List<UserAction>> groupByBoundaryCode(List<UserAction> userActions) {
        return userActions.stream()
                .collect(Collectors.groupingBy(UserAction::getBoundaryCode));
    }

    private BoundaryResponse fetchBoundaryDetails(String tenantId, List<String> boundaries, UserActionBulkRequest request) {
        log.debug("Fetching boundary details for tenantId: {}, boundaries: {}", tenantId, boundaries);
        String url = buildBoundarySearchUrl(tenantId, boundaries);
        BoundaryResponse response = serviceRequestClient.fetchResult(
                new StringBuilder(url),
                request.getRequestInfo(),
                BoundaryResponse.class
        );
        log.debug("Boundary details fetched successfully for tenantId: {}", tenantId);
        return response;
    }

    private String buildBoundarySearchUrl(String tenantId, List<String> boundaries) {
        return projectConfiguration.getBoundaryServiceHost()
                + projectConfiguration.getBoundarySearchUrl()
                + "?limit=" + boundaries.size()
                + "&offset=0&tenantId=" + tenantId
                + "&codes=" + String.join(",", boundaries);
    }

    private List<String> findInvalidBoundaryCodes(List<String> boundaries, BoundaryResponse boundarySearchResponse) {
        List<String> validBoundaryCodes = boundarySearchResponse.getBoundary().stream()
                .map(Boundary::getCode)
                .toList();
        
        List<String> invalidBoundaryCodes = new ArrayList<>(boundaries);
        invalidBoundaryCodes.removeAll(validBoundaryCodes);
        return invalidBoundaryCodes;
    }

    private void addErrorsForInvalidBoundaries(Map<String, List<UserAction>> boundaryCodeUserActionsMap,
                                               List<String> invalidBoundaryCodes,
                                               Map<UserAction, List<Error>> errorDetailsMap) {
        List<UserAction> userActionsWithInvalidBoundaries = boundaryCodeUserActionsMap.entrySet().stream()
                .filter(entry -> invalidBoundaryCodes.contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .toList();

        Error error = Error.builder()
                .errorMessage("Boundary code does not exist in db")
                .errorCode("PROJECT_USER_ACTION_INVALID_BOUNDARY_ERROR")
                .type(Error.ErrorType.NON_RECOVERABLE)
                .exception(new CustomException("PROJECT_USER_ACTION_INVALID_BOUNDARY_ERROR", "Boundary code does not exist in db"))
                .build();
        
        userActionsWithInvalidBoundaries.forEach(userAction -> {
            populateErrorDetails(userAction, error, errorDetailsMap);
        });
    }
}
