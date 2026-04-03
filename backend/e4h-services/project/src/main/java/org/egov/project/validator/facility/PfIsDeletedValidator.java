package org.egov.project.validator.facility;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.ProjectFacility;
import org.egov.common.models.project.ProjectFacilityBulkRequest;
import org.egov.common.validator.Validator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.notHavingErrors;
import static org.egov.common.utils.CommonUtils.populateErrorDetails;
import static org.egov.common.utils.ValidatorUtils.getErrorForIsDelete;

@Component
@Order(2)
@Slf4j
public class PfIsDeletedValidator implements Validator<ProjectFacilityBulkRequest, ProjectFacility> {

    @Override
    public Map<ProjectFacility, List<Error>> validate(ProjectFacilityBulkRequest request) {
        log.trace("Entering validate (PfIsDeletedValidator)");
        log.info("Validating isDeleted field");
        log.debug("Validating {} facilities for isDeleted field", request.getProjectFacilities() != null ? request.getProjectFacilities().size() : 0);
        HashMap<ProjectFacility, List<Error>> errorDetailsMap = new HashMap<>();
        List<ProjectFacility> validEntities = request.getProjectFacilities().stream()
                .filter(notHavingErrors())
                .toList();
        if (!validEntities.isEmpty()) {
            validEntities.stream().filter(ProjectFacility::getIsDeleted).forEach(individual -> {
                Error error = getErrorForIsDelete();
                populateErrorDetails(individual, error, errorDetailsMap);
            });
        }
        log.debug("IsDeleted validation completed - found {} errors", errorDetailsMap.size());
        log.trace("Exiting validate (PfIsDeletedValidator)");
        return errorDetailsMap;
    }
}
