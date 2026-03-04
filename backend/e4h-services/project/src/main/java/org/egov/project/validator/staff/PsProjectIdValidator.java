package org.egov.project.validator.staff;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.ProjectStaff;
import org.egov.common.models.project.ProjectStaffBulkRequest;
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
public class PsProjectIdValidator implements Validator<ProjectStaffBulkRequest, ProjectStaff> {

    private final ProjectRepository projectRepository;

    @Autowired
    public PsProjectIdValidator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }


    @Override
    public Map<ProjectStaff, List<Error>> validate(ProjectStaffBulkRequest request) {
        log.trace("Entering validate (PsProjectIdValidator)");
        log.info("Validating project ID for staff");
        log.debug("Validating {} staff", request.getProjectStaff() != null ? request.getProjectStaff().size() : 0);
        Map<ProjectStaff, List<Error>> errorDetailsMap = new HashMap<>();
        List<ProjectStaff> entities = request.getProjectStaff();
        Class<?> objClass = getObjClass(entities);
        Method idMethod = getMethod("getProjectId", objClass);
        Map<String, ProjectStaff> eMap = getIdToObjMap(entities
                .stream().filter(notHavingErrors()).toList(), idMethod);
        if (!eMap.isEmpty()) {
            List<String> entityIds = new ArrayList<>(eMap.keySet());
            log.debug("Validating {} project IDs against repository", entityIds.size());
            List<String> existingProjectIds = projectRepository.validateIds(entityIds,
                    getIdFieldName(idMethod));
            log.debug("Found {} existing project IDs", existingProjectIds != null ? existingProjectIds.size() : 0);
            List<ProjectStaff> invalidEntities = entities.stream().filter(notHavingErrors()).filter(entity ->
                            !existingProjectIds.contains(entity.getProjectId()))
                    .toList();
            if (!invalidEntities.isEmpty()) {
                log.warn("Found {} staff with invalid project IDs", invalidEntities.size());
            }
            invalidEntities.forEach(ProjectStaff -> {
                Error error = getErrorForNonExistentRelatedEntity(ProjectStaff.getProjectId());
                populateErrorDetails(ProjectStaff, error, errorDetailsMap);
            });
        }

        log.debug("Validation completed - {} errors found", errorDetailsMap.size());
        log.trace("Exiting validate (PsProjectIdValidator)");
        return errorDetailsMap;
    }
}
