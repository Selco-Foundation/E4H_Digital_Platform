package org.egov.project.validator.useraction;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.project.useraction.UserAction;
import org.egov.common.models.project.useraction.UserActionBulkRequest;
import org.egov.common.models.project.useraction.UserActionSearch;
import org.egov.common.validator.Validator;
import org.egov.project.repository.UserActionRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.common.utils.ValidatorUtils.getErrorForNonExistentEntity;
import static org.egov.project.Constants.GET_ID;

@Component
@Order(value = 4)
@Slf4j
public class UaNonExistentEntityValidator implements Validator<UserActionBulkRequest, UserAction> {

    private final UserActionRepository userActionRepository;

    @Autowired
    public UaNonExistentEntityValidator(UserActionRepository userActionRepository) {
        this.userActionRepository = userActionRepository;
    }


    @Override
    public Map<UserAction, List<Error>> validate(UserActionBulkRequest request) {
        log.info("Validating existence of entities in UserActionBulkRequest with {} user actions", request.getUserActions().size());
        Map<UserAction, List<Error>> errorDetailsMap = new HashMap<>();
        List<UserAction> entities = request.getUserActions();
        Class<?> objClass = getObjClass(entities);
        Method idMethod = getMethod(GET_ID, objClass);
        Map<String, UserAction> eMap = getIdToObjMap(entities
                .stream().filter(notHavingErrors()).toList(), idMethod);
        // Lists to store IDs and client reference IDs
        List<String> idList = new ArrayList<>();
        List<String> clientReferenceIdList = new ArrayList<>();
        // Extract IDs and client reference IDs from Project UserAction entities
        entities.forEach(entity -> {
            idList.add(entity.getId());
            clientReferenceIdList.add(entity.getClientReferenceId());
        });
        if (!eMap.isEmpty()) {
            UserActionSearch taskSearch = UserActionSearch.builder()
                    .clientReferenceId(clientReferenceIdList)
                    .id(idList)
                    .build();

            URLParams urlParams = URLParams.builder()
                    .tenantId(entities.get(0).getTenantId())
                    .limit(entities.size())
                    .offset(0)
                    .includeDeleted(false)
                    .lastChangedSince(null)
                    .build();

            List<UserAction> existingEntities;
            try {
                // Query the repository to find existing entities
                existingEntities = userActionRepository.find(taskSearch, urlParams).getResponse();
            } catch (DataAccessException e) {
                log.error("Data access exception while searching for ProjectUserAction: {}", e.getMessage(), e);
                throw new CustomException("PROJECT_USER_ACTION_SEARCH_FAILED", "Search failed for ProjectUserAction with clientReferenceId(s): "
                        + clientReferenceIdList + " and id(s): " + idList + ". Database error: " + e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected exception while searching for ProjectUserAction: {}", e.getMessage(), e);
                throw new CustomException("PROJECT_USER_ACTION_SEARCH_FAILED", "Search failed for ProjectUserAction with clientReferenceId(s): "
                        + clientReferenceIdList + " and id(s): " + idList + ". Error: " + e.getMessage());
            }
            List<UserAction> nonExistentEntities = checkNonExistentEntities(eMap,
                    existingEntities, idMethod);
            nonExistentEntities.forEach(task -> {
                Error error = getErrorForNonExistentEntity();
                populateErrorDetails(task, error, errorDetailsMap);
            });
        }

        return errorDetailsMap;
    }
}
