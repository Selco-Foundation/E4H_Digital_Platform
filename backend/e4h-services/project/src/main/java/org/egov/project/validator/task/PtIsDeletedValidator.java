package org.egov.project.validator.task;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.Task;
import org.egov.common.models.project.TaskBulkRequest;
import org.egov.common.validator.Validator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.populateErrorDetails;
import static org.egov.common.utils.ValidatorUtils.getErrorForIsDelete;

@Component
@Order(2)
@Slf4j
public class PtIsDeletedValidator implements Validator<TaskBulkRequest, Task> {

    @Override
    public Map<Task, List<Error>> validate(TaskBulkRequest request) {
        log.trace("Entering validate (PtIsDeletedValidator)");
        log.info("Validating isDeleted field");
        log.debug("Validating {} tasks for isDeleted field", request.getTasks() != null ? request.getTasks().size() : 0);
        HashMap<Task, List<Error>> errorDetailsMap = new HashMap<>();
        List<Task> validIndividuals = request.getTasks();
        validIndividuals.stream().filter(Task::getIsDeleted).forEach(individual -> {
            Error error = getErrorForIsDelete();
            populateErrorDetails(individual, error, errorDetailsMap);
        });
        log.debug("IsDeleted validation completed - found {} errors", errorDetailsMap.size());
        log.trace("Exiting validate (PtIsDeletedValidator)");
        return errorDetailsMap;
    }
}
