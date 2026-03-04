package org.egov.project.validator.staff;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.ProjectStaff;
import org.egov.common.models.project.ProjectStaffBulkRequest;
import org.egov.common.validator.Validator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.validateForNullId;
import static org.egov.project.Constants.GET_STAFF;

@Component
@Order(value = 1)
@Slf4j
public class PsNullIdValidator implements Validator<ProjectStaffBulkRequest, ProjectStaff> {

    @Override
    public Map<ProjectStaff, List<Error>> validate(ProjectStaffBulkRequest request) {
        log.trace("Entering validate (PsNullIdValidator)");
        log.info("Validating for null ID");
        log.debug("Validating {} staff for null ID", request.getProjectStaff() != null ? request.getProjectStaff().size() : 0);
        Map<ProjectStaff, List<Error>> result = validateForNullId(request, GET_STAFF);
        log.debug("Null ID validation completed - found {} errors", result != null ? result.size() : 0);
        log.trace("Exiting validate (PsNullIdValidator)");
        return result;
    }
}
