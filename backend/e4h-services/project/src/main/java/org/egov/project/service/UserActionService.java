package org.egov.project.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.data.query.exception.QueryBuilderException;
import org.egov.common.ds.Tuple;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.ErrorDetails;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.project.useraction.UserAction;
import org.egov.common.models.project.useraction.UserActionBulkRequest;
import org.egov.common.models.project.useraction.UserActionSearch;
import org.egov.common.models.project.useraction.UserActionSearchRequest;
import org.egov.common.service.IdGenService;
import org.egov.common.utils.CommonUtils;
import org.egov.common.validator.Validator;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.repository.UserActionRepository;
import org.egov.project.service.enrichment.UserActionEnrichmentService;
import org.egov.project.validator.useraction.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.project.Constants.SET_USER_ACTION;
import static org.egov.project.Constants.VALIDATION_ERROR;

@Service
@Slf4j
public class UserActionService {
    private final IdGenService idGenService; // Service for generating unique IDs
    private final UserActionRepository userActionTaskRepository; // Repository for user actions
    private final ServiceRequestClient serviceRequestClient; // Client for external service requests
    private final ProjectConfiguration projectConfiguration; // Configuration properties for the project
    private final UserActionEnrichmentService userActionEnrichmentService; // Service for enriching user actions
    private final List<Validator<UserActionBulkRequest, UserAction>> validators; // List of validators for user actions

    // Predicate to filter validators applicable for creation
    private final Predicate<Validator<UserActionBulkRequest, UserAction>> isApplicableForCreate = validator ->
            validator.getClass().equals(UaProjectIdValidator.class)
                    || validator.getClass().equals(UaExistentEntityValidator.class)
                    || validator.getClass().equals(UaBoundaryValidator.class);

    // Predicate to filter validators applicable for updates
    private final Predicate<Validator<UserActionBulkRequest, UserAction>> isApplicableForUpdate = validator ->
            validator.getClass().equals(UaProjectIdValidator.class)
                    || validator.getClass().equals(UaNullIdValidator.class)
                    || validator.getClass().equals(UaNonExistentEntityValidator.class)
                    || validator.getClass().equals(UaRowVersionValidator.class)
                    || validator.getClass().equals(UaBoundaryValidator.class);

    // Constructor for dependency injection
    @Autowired
    public UserActionService(
            IdGenService idGenService,
            UserActionRepository userActionTaskRepository,
            ServiceRequestClient serviceRequestClient,
            ProjectConfiguration projectConfiguration,
            UserActionEnrichmentService userActionEnrichmentService,
            List<Validator<UserActionBulkRequest, UserAction>> validators
    ) {
        this.idGenService = idGenService;
        this.userActionTaskRepository = userActionTaskRepository;
        this.serviceRequestClient = serviceRequestClient;
        this.projectConfiguration = projectConfiguration;
        this.userActionEnrichmentService = userActionEnrichmentService;
        this.validators = validators;
    }

    // Method to handle the creation of user actions
    public List<UserAction> create(UserActionBulkRequest request, boolean isBulk) {
        log.trace("Entering create (bulk user actions)");
        log.info("Received request to create bulk user actions");

        // Validate the request and get valid user actions along with error details
        Tuple<List<UserAction>, Map<UserAction, ErrorDetails>> tuple = validate(validators, isApplicableForCreate, request, isBulk);
        Map<UserAction, ErrorDetails> errorDetailsMap = tuple.getY();
        List<UserAction> validUserActions = tuple.getX();
        log.debug("Validation completed - {} valid user actions, {} errors", validUserActions.size(), errorDetailsMap.size());

        try {
            // If there are valid user actions, enrich and save them
            if (!validUserActions.isEmpty()) {
                log.info("Processing {} valid entities", validUserActions.size());
                log.debug("Enriching user actions before save");
                userActionEnrichmentService.create(validUserActions, request);
                log.debug("Saving user actions to repository");
                userActionTaskRepository.save(validUserActions, projectConfiguration.getCreateUserActionTopic());
                log.info("Successfully created {} user actions", validUserActions.size());
            } else {
                log.warn("No valid user actions to create after validation");
            }
        } catch (Exception exception) {
            // Handle and log any exceptions that occur
            log.error("Error occurred while creating user actions", exception);
            populateErrorDetails(request, errorDetailsMap, validUserActions, exception, SET_USER_ACTION);
        }

        // Handle any validation errors
        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting create (bulk user actions)");
        return validUserActions;
    }

    // Method to handle the update of user actions
    public List<UserAction> update(UserActionBulkRequest request, boolean isBulk) {
        log.trace("Entering update (bulk user actions)");
        log.info("Received request to update bulk user actions");

        // Validate the request and get valid user actions along with error details
        Tuple<List<UserAction>, Map<UserAction, ErrorDetails>> tuple = validate(validators, isApplicableForUpdate, request, isBulk);
        Map<UserAction, ErrorDetails> errorDetailsMap = tuple.getY();
        List<UserAction> validUserActions = tuple.getX();
        log.debug("Validation completed - {} valid user actions, {} errors", validUserActions.size(), errorDetailsMap.size());

        try {
            // If there are valid user actions, enrich and update them
            if (!validUserActions.isEmpty()) {
                log.info("Processing {} valid entities", validUserActions.size());
                log.debug("Enriching user actions before update");
                userActionEnrichmentService.update(validUserActions, request);
                log.debug("Saving updated user actions to repository");
                userActionTaskRepository.save(validUserActions, projectConfiguration.getUpdateUserActionTopic());
                log.info("Successfully updated {} user actions", validUserActions.size());
            } else {
                log.warn("No valid user actions to update after validation");
            }
        } catch (Exception exception) {
            // Handle and log any exceptions that occur
            log.error("Error occurred while updating user actions", exception);
            populateErrorDetails(request, errorDetailsMap, validUserActions, exception, SET_USER_ACTION);
        }

        // Handle any validation errors
        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting update (bulk user actions)");
        return validUserActions;
    }

    // Method to validate user action requests
    private Tuple<List<UserAction>, Map<UserAction, ErrorDetails>> validate(List<Validator<UserActionBulkRequest, UserAction>> validators,
                                                                            Predicate<Validator<UserActionBulkRequest, UserAction>> applicableValidators,
                                                                            UserActionBulkRequest request, boolean isBulk) {
        log.trace("Entering validate for {} user actions", request.getUserActions() != null ? request.getUserActions().size() : 0);
        log.debug("Validating request with {} validators", validators.size());

        // Validate the request using the applicable validators
        Map<UserAction, ErrorDetails> errorDetailsMap = new HashMap<>();

        // Throw exception if there are validation errors and it's not a bulk request
        if (!errorDetailsMap.isEmpty() && !isBulk) {
            log.error("Validation error occurred. Error details: {}", errorDetailsMap.values());
            throw new CustomException(VALIDATION_ERROR, errorDetailsMap.values().toString());
        }

        // Filter and return valid user actions
        List<UserAction> validUserActions = request.getUserActions().stream()
                .filter(notHavingErrors()).toList();
        log.debug("Validation completed - {} valid user actions out of {}", validUserActions.size(), request.getUserActions().size());
        log.trace("Exiting validate");
        return new Tuple<>(validUserActions, errorDetailsMap);
    }

    // Method to search for user actions based on the request and URL parameters
    public SearchResponse<UserAction> search(UserActionSearchRequest request, URLParams urlParams) {
        log.trace("Entering search");
        log.info("Received request to search user actions");

        UserActionSearch userActionSearch = request.getUserAction();

        // Determine the ID field name for search
        String idFieldName = getIdFieldName(userActionSearch);
        if (isSearchByIdOnly(userActionSearch, idFieldName)) {
            log.info("Searching user actions by ID");

            // Retrieve IDs and search for user actions by ID
            List<String> ids = (List<String>) ReflectionUtils.invokeMethod(getIdMethod(Collections
                            .singletonList(userActionSearch)),
                    userActionSearch);
            log.debug("Fetching user actions with {} IDs", ids != null ? ids.size() : 0);
            SearchResponse<UserAction> searchResponse = userActionTaskRepository.findById(ids, idFieldName);
            log.debug("Found {} user actions before filtering", searchResponse.getResponse() != null ? searchResponse.getResponse().size() : 0);
            SearchResponse<UserAction> result = SearchResponse.<UserAction>builder().response(searchResponse.getResponse().stream()
                    .filter(lastChangedSince(urlParams.getLastChangedSince()))
                    .filter(havingTenantId(urlParams.getTenantId()))
                    .filter(includeDeleted(urlParams.getIncludeDeleted()))
                    .toList()).totalCount(searchResponse.getTotalCount()).build();
            log.info("Search by ID completed - found {} user actions", result.getResponse() != null ? result.getResponse().size() : 0);
            log.trace("Exiting search");
            return result;
        }

        try {
            // Search using the criteria specified in the request
            log.info("Searching user actions using criteria");
            log.debug("Search parameters - tenantId: {}, includeDeleted: {}", urlParams.getTenantId(), urlParams.getIncludeDeleted());
            SearchResponse<UserAction> result = userActionTaskRepository.find(userActionSearch, urlParams);
            log.info("Search by criteria completed - found {} user actions", result.getResponse() != null ? result.getResponse().size() : 0);
            log.trace("Exiting search");
            return result;
        } catch (QueryBuilderException e) {
            // Handle and log query building exceptions
            log.error("Error in building query", e);
            throw new CustomException("ERROR_IN_QUERY", e.getMessage());
        }
    }

    // Method to put user actions into cache
    public void putInCache(List<UserAction> userActions) {
        log.trace("Entering putInCache for {} user actions", userActions != null ? userActions.size() : 0);
        log.info("Putting {} user actions in cache", userActions != null ? userActions.size() : 0);
        userActionTaskRepository.putInCache(userActions);
        log.info("Successfully put user actions in cache");
        log.trace("Exiting putInCache");
    }
}
