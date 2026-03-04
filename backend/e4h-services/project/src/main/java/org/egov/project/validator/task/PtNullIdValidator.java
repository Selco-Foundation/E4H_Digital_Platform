package org.egov.project.validator.task;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.Task;
import org.egov.common.models.project.TaskBulkRequest;
import org.egov.common.validator.Validator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.validateForNullId;
import static org.egov.project.Constants.GET_TASKS;

@Component
@Order(value = 1)
@Slf4j
public class PtNullIdValidator implements Validator<TaskBulkRequest, Task> {

    @Override
    public Map<Task, List<Error>> validate(TaskBulkRequest request) {
        log.trace("Entering validate (PtNullIdValidator)");
        log.info("Validating for null ID");
        log.debug("Validating {} tasks for null ID", request.getTasks() != null ? request.getTasks().size() : 0);
        Map<Task, List<Error>> result = validateForNullId(request, GET_TASKS);
        log.debug("Null ID validation completed - found {} errors", result != null ? result.size() : 0);
        log.trace("Exiting validate (PtNullIdValidator)");
        return result;
    }
}
