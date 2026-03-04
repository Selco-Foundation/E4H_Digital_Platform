package org.egov.project.validator.facility;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.ProjectFacility;
import org.egov.common.models.project.ProjectFacilityBulkRequest;
import org.egov.common.validator.Validator;
import org.egov.project.repository.ProjectRepository;
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
import static org.egov.common.utils.ValidatorUtils.getErrorForNonExistentRelatedEntity;
import static org.egov.project.Constants.GET_PROJECT_ID;

@Component
@Order(value = 6)
@Slf4j
public class PfProjectIdValidator implements Validator<ProjectFacilityBulkRequest, ProjectFacility> {

    private final ProjectRepository projectRepository;

    @Autowired
    public PfProjectIdValidator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }


    @Override
    public Map<ProjectFacility, List<Error>> validate(ProjectFacilityBulkRequest request) {
        log.trace("Entering validate (PfProjectIdValidator)");
        log.info("Validating project ID for facilities");
        log.debug("Validating {} facilities", request.getProjectFacilities() != null ? request.getProjectFacilities().size() : 0);
        Map<ProjectFacility, List<Error>> errorDetailsMap = new HashMap<>();
        List<ProjectFacility> validEntities = request.getProjectFacilities().stream()
                .filter(notHavingErrors())
                .toList();
        log.debug("Found {} valid facilities to validate", validEntities.size());
        if (!validEntities.isEmpty()) {
            Class<?> objClass = getObjClass(validEntities);
            Method idMethod = getMethod(GET_PROJECT_ID, objClass);
            Map<String, ProjectFacility> eMap = getIdToObjMap(validEntities, idMethod);
            if (!eMap.isEmpty()) {
                List<String> entityIds = new ArrayList<>(eMap.keySet());
                log.debug("Validating {} project IDs against repository", entityIds.size());
                List<String> existingProjectIds = projectRepository.validateIds(entityIds,
                        getIdFieldName(idMethod));
                log.debug("Found {} existing project IDs", existingProjectIds.size());
                List<ProjectFacility> invalidEntities = validEntities.stream().filter(notHavingErrors()).filter(entity ->
                                !existingProjectIds.contains(entity.getProjectId()))
                        .toList();
                if (!invalidEntities.isEmpty()) {
                    log.warn("Found {} facilities with invalid project IDs", invalidEntities.size());
                }
                invalidEntities.forEach(projectFacility -> {
                    Error error = getErrorForNonExistentRelatedEntity(projectFacility.getProjectId());
                    populateErrorDetails(projectFacility, error, errorDetailsMap);
                });
            }
        }
        log.debug("Validation completed - {} errors found", errorDetailsMap.size());
        log.trace("Exiting validate (PfProjectIdValidator)");
        return errorDetailsMap;
    }
}
