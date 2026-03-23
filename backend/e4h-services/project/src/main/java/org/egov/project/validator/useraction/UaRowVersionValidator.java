package org.egov.project.validator.useraction;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.useraction.UserAction;
import org.egov.common.models.project.useraction.UserActionBulkRequest;
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
import static org.egov.common.utils.ValidatorUtils.getErrorForRowVersionMismatch;

@Component
@Order(value = 5)
@Slf4j
public class UaRowVersionValidator implements Validator<UserActionBulkRequest, UserAction> {

    private final UserActionRepository userActionRepository;

    @Autowired
    public UaRowVersionValidator(UserActionRepository userActionRepository) {
        this.userActionRepository = userActionRepository;
    }


    @Override
    public Map<UserAction, List<Error>> validate(UserActionBulkRequest request) {
        log.info("validating row version");
        Map<UserAction, List<Error>> errorDetailsMap = new HashMap<>();
        try {

            Method idMethod = getIdMethod(request.getUserActions());
            Map<String, UserAction> eMap = getIdToObjMap(request.getUserActions().stream()
                    .filter(notHavingErrors())
                    .toList(), idMethod);
            if (!eMap.isEmpty()) {
                List<String> entityIds = new ArrayList<>(eMap.keySet());
                List<UserAction> existingEntities = userActionRepository.findById(entityIds,
                        getIdFieldName(idMethod)).getResponse();
                List<UserAction> entitiesWithMismatchedRowVersion =
                        getEntitiesWithMismatchedRowVersion(eMap, existingEntities, idMethod);
                entitiesWithMismatchedRowVersion.forEach(individual -> {
                    Error error = getErrorForRowVersionMismatch();
                    populateErrorDetails(individual, error, errorDetailsMap);
                });
            }
        } catch (DataAccessException e) {
            log.error("Data access exception during row version validation: {}", e.getMessage(), e);
            throw new CustomException("PROJECT_USER_ACTION_ROW_VERSION_VALIDATION_ERROR", "Database error while validating row versions: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected exception during row version validation: {}", e.getMessage(), e);
            throw new CustomException("PROJECT_USER_ACTION_ROW_VERSION_VALIDATION_ERROR", "Unexpected error occurred while validating row versions: " + e.getMessage());
        }
        return errorDetailsMap;
    }
}
