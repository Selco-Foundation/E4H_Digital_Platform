package org.egov.project.validator.resource;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.ProjectResource;
import org.egov.common.models.project.ProjectResourceBulkRequest;
import org.egov.common.validator.Validator;
import org.egov.project.repository.ProjectResourceRepository;
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
public class PrRowVersionValidator implements Validator<ProjectResourceBulkRequest, ProjectResource> {
    private final ProjectResourceRepository projectResourceRepository;

    @Autowired
    public PrRowVersionValidator(ProjectResourceRepository projectResourceRepository) {
        this.projectResourceRepository = projectResourceRepository;
    }

    @Override
    public Map<ProjectResource, List<Error>> validate(ProjectResourceBulkRequest request) {
        log.trace("Entering validate (PrRowVersionValidator)");
        log.info("Validating row version");
        log.debug("Validating {} resources for row version", request.getProjectResource() != null ? request.getProjectResource().size() : 0);
        Map<ProjectResource, List<Error>> errorDetailsMap = new HashMap<>();
        Method idMethod = getIdMethod(request.getProjectResource());
        Map<String, ProjectResource> eMap = getIdToObjMap(request.getProjectResource().stream()
                .filter(notHavingErrors())
                .toList(), idMethod);
        if (!eMap.isEmpty()) {
            List<String> entityIds = new ArrayList<>(eMap.keySet());
            log.debug("Checking row version for {} resource IDs", entityIds.size());
            List<ProjectResource> existingEntities = projectResourceRepository
                    .findById(entityIds, false, getIdFieldName(idMethod));
            log.debug("Found {} existing resource entities", existingEntities != null ? existingEntities.size() : 0);
            List<ProjectResource> entitiesWithMismatchedRowVersion =
                    getEntitiesWithMismatchedRowVersion(eMap, existingEntities, idMethod);
            if (!entitiesWithMismatchedRowVersion.isEmpty()) {
                log.warn("Found {} resources with mismatched row version", entitiesWithMismatchedRowVersion.size());
            }
            entitiesWithMismatchedRowVersion.forEach(individual -> {
                Error error = getErrorForRowVersionMismatch();
                populateErrorDetails(individual, error, errorDetailsMap);
            });
        }
        log.debug("Row version validation completed - found {} errors", errorDetailsMap.size());
        log.trace("Exiting validate (PrRowVersionValidator)");
        return errorDetailsMap;
    }
}
