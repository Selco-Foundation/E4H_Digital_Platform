package org.egov.project.validator.staff;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.ProjectStaff;
import org.egov.common.models.project.ProjectStaffBulkRequest;
import org.egov.common.validator.Validator;
import org.egov.project.repository.ProjectStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
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
public class PsRowVersionValidator implements Validator<ProjectStaffBulkRequest, ProjectStaff> {

    private final ProjectStaffRepository repository;

    @Autowired
    public PsRowVersionValidator(ProjectStaffRepository repository) {
        this.repository = repository;
    }


    @Override
    public Map<ProjectStaff, List<Error>> validate(ProjectStaffBulkRequest request) {
        log.trace("Entering validate (PsRowVersionValidator)");
        log.info("Validating row version");
        log.debug("Validating {} staff for row version", request.getProjectStaff() != null ? request.getProjectStaff().size() : 0);
        Map<ProjectStaff, List<Error>> errorDetailsMap = new HashMap<>();
        Method idMethod = getIdMethod(request.getProjectStaff());
        Map<String, ProjectStaff> eMap = getIdToObjMap(request.getProjectStaff().stream()
                .filter(notHavingErrors())
                .toList(), idMethod);
        if (!eMap.isEmpty()) {
            List<String> entityIds = new ArrayList<>(eMap.keySet());
            log.debug("Checking row version for {} staff IDs", entityIds.size());
            List<ProjectStaff> existingEntities = repository.findById(entityIds, false,
                    getIdFieldName(idMethod));
            log.debug("Found {} existing staff entities", existingEntities != null ? existingEntities.size() : 0);
            List<ProjectStaff> entitiesWithMismatchedRowVersion =
                    getEntitiesWithMismatchedRowVersion(eMap, existingEntities, idMethod);
            if (!entitiesWithMismatchedRowVersion.isEmpty()) {
                log.warn("Found {} staff with mismatched row version", entitiesWithMismatchedRowVersion.size());
            }
            entitiesWithMismatchedRowVersion.forEach(individual -> {
                Error error = getErrorForRowVersionMismatch();
                populateErrorDetails(individual, error, errorDetailsMap);
            });
        }
        log.debug("Row version validation completed - found {} errors", errorDetailsMap.size());
        log.trace("Exiting validate (PsRowVersionValidator)");
        return errorDetailsMap;
    }
}
