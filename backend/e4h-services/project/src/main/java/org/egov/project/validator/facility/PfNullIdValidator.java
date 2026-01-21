package org.egov.project.validator.facility;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.ProjectFacility;
import org.egov.common.models.project.ProjectFacilityBulkRequest;
import org.egov.common.validator.Validator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.validateForNullId;
import static org.egov.project.Constants.GET_PROJECT_FACILITIES;


@Component
@Order(value = 1)
@Slf4j
public class PfNullIdValidator implements Validator<ProjectFacilityBulkRequest, ProjectFacility> {

    @Override
    public Map<ProjectFacility, List<Error>> validate(ProjectFacilityBulkRequest request) {
        log.trace("Entering validate (PfNullIdValidator)");
        log.info("Validating for null ID");
        log.debug("Validating {} facilities for null ID", request.getProjectFacilities() != null ? request.getProjectFacilities().size() : 0);
        Map<ProjectFacility, List<Error>> result = validateForNullId(request, GET_PROJECT_FACILITIES);
        log.debug("Null ID validation completed - found {} errors", result != null ? result.size() : 0);
        log.trace("Exiting validate (PfNullIdValidator)");
        return result;
    }
}
