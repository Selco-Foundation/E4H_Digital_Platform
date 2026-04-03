package org.egov.project.validator.facility;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.ProjectFacility;
import org.egov.common.models.project.ProjectFacilityBulkRequest;
import org.egov.common.validator.Validator;
import org.egov.project.repository.ProjectFacilityRepository;
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
public class PfRowVersionValidator implements Validator<ProjectFacilityBulkRequest, ProjectFacility> {

    private final ProjectFacilityRepository repository;

    @Autowired
    public PfRowVersionValidator(ProjectFacilityRepository repository) {
        this.repository = repository;
    }


    @Override
    public Map<ProjectFacility, List<Error>> validate(ProjectFacilityBulkRequest request) {
        log.trace("Entering validate (PfRowVersionValidator)");
        log.info("Validating row version");
        log.debug("Validating {} facilities for row version", request.getProjectFacilities() != null ? request.getProjectFacilities().size() : 0);
        Map<ProjectFacility, List<Error>> errorDetailsMap = new HashMap<>();
        List<ProjectFacility> validEntities = request.getProjectFacilities().stream()
                .filter(notHavingErrors())
                .toList();
        if (!validEntities.isEmpty()) {
            Method idMethod = getIdMethod(validEntities);
            Map<String, ProjectFacility> eMap = getIdToObjMap(validEntities, idMethod);
            if (!eMap.isEmpty()) {
                List<String> entityIds = new ArrayList<>(eMap.keySet());
                log.debug("Checking row version for {} facility IDs", entityIds.size());
                List<ProjectFacility> existingEntities = repository.findById(entityIds, false,
                        getIdFieldName(idMethod));
                log.debug("Found {} existing facility entities", existingEntities != null ? existingEntities.size() : 0);
                List<ProjectFacility> entitiesWithMismatchedRowVersion =
                        getEntitiesWithMismatchedRowVersion(eMap, existingEntities, idMethod);
                if (!entitiesWithMismatchedRowVersion.isEmpty()) {
                    log.warn("Found {} facilities with mismatched row version", entitiesWithMismatchedRowVersion.size());
                }
                entitiesWithMismatchedRowVersion.forEach(projectFacility -> {
                    Error error = getErrorForRowVersionMismatch();
                    populateErrorDetails(projectFacility, error, errorDetailsMap);
                });
            }
        }
        log.debug("Row version validation completed - found {} errors", errorDetailsMap.size());
        log.trace("Exiting validate (PfRowVersionValidator)");
        return errorDetailsMap;
    }
}
