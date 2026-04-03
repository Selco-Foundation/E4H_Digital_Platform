package org.egov.project.validator.task;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.Task;
import org.egov.common.models.project.TaskBulkRequest;
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
public class PtProjectIdValidator implements Validator<TaskBulkRequest, Task> {

    private final ProjectRepository projectRepository;

    @Autowired
    public PtProjectIdValidator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }


    @Override
    public Map<Task, List<Error>> validate(TaskBulkRequest request) {
        log.trace("Entering validate (PtProjectIdValidator)");
        log.info("Validating project ID for tasks");
        log.debug("Validating {} tasks", request.getTasks() != null ? request.getTasks().size() : 0);
        Map<Task, List<Error>> errorDetailsMap = new HashMap<>();
        List<Task> entities = request.getTasks();
        Class<?> objClass = getObjClass(entities);
        Method idMethod = getMethod("getProjectId", objClass);
        Map<String, Task> eMap = getIdToObjMap(entities
                .stream().filter(notHavingErrors()).toList(), idMethod);
        log.debug("Found {} valid tasks to validate", eMap.size());
        if (!eMap.isEmpty()) {
            List<String> entityIds = new ArrayList<>(eMap.keySet());
            log.debug("Validating {} project IDs against repository", entityIds.size());
            List<String> existingProjectIds = projectRepository.validateIds(entityIds,
                    getIdFieldName(idMethod));
            log.debug("Found {} existing project IDs", existingProjectIds.size());
            List<Task> invalidEntities = entities.stream().filter(notHavingErrors()).filter(entity ->
                            !existingProjectIds.contains(entity.getProjectId()))
                    .toList();
            if (!invalidEntities.isEmpty()) {
                log.warn("Found {} tasks with invalid project IDs", invalidEntities.size());
            }
            invalidEntities.forEach(task -> {
                Error error = getErrorForNonExistentRelatedEntity(task.getProjectId());
                populateErrorDetails(task, error, errorDetailsMap);
            });
        }
        log.debug("Validation completed - {} errors found", errorDetailsMap.size());
        log.trace("Exiting validate (PtProjectIdValidator)");
        return errorDetailsMap;
    }
}
