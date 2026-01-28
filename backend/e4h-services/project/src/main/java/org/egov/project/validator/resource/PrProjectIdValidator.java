package org.egov.project.validator.resource;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.ProjectResource;
import org.egov.common.models.project.ProjectResourceBulkRequest;
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

@Component
@Order(value = 6)
@Slf4j
public class PrProjectIdValidator implements Validator<ProjectResourceBulkRequest, ProjectResource> {

    private final ProjectRepository projectRepository;

    @Autowired
    public PrProjectIdValidator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public Map<ProjectResource, List<Error>> validate(ProjectResourceBulkRequest request) {
        log.trace("Entering validate (PrProjectIdValidator)");
        log.info("Validating project ID for resources");
        log.debug("Validating {} resources", request.getProjectResource() != null ? request.getProjectResource().size() : 0);
        Map<ProjectResource, List<Error>> errorDetailsMap = new HashMap<>();
        List<ProjectResource> entities = request.getProjectResource();
        Class<?> objClass = getObjClass(entities);
        Method idMethod = getMethod("getProjectId", objClass);
        Map<String, ProjectResource> eMap = getIdToObjMap(entities
                .stream().filter(notHavingErrors()).toList(), idMethod);
        if (!eMap.isEmpty()) {
            List<String> entityIds = new ArrayList<>(eMap.keySet());
            log.debug("Validating {} project IDs against repository", entityIds.size());
            List<String> existingProjectIds = projectRepository.validateIds(entityIds,
                    getIdFieldName(idMethod));
            log.debug("Found {} existing project IDs", existingProjectIds != null ? existingProjectIds.size() : 0);
            List<ProjectResource> invalidEntities = entities.stream().filter(notHavingErrors()).filter(entity ->
                            !existingProjectIds.contains(entity.getProjectId()))
                    .toList();
            if (!invalidEntities.isEmpty()) {
                log.warn("Found {} resources with invalid project IDs", invalidEntities.size());
            }
            invalidEntities.forEach(projectResource -> {
                Error error = getErrorForNonExistentRelatedEntity(projectResource.getProjectId());
                populateErrorDetails(projectResource, error, errorDetailsMap);
            });
        }
        log.debug("Validation completed - {} errors found", errorDetailsMap.size());
        log.trace("Exiting validate (PrProjectIdValidator)");
        return errorDetailsMap;
    }

}
