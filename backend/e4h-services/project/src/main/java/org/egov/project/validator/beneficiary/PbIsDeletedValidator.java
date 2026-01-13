package org.egov.project.validator.beneficiary;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.BeneficiaryBulkRequest;
import org.egov.common.models.project.ProjectBeneficiary;
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
public class PbIsDeletedValidator implements Validator<BeneficiaryBulkRequest, ProjectBeneficiary> {

    @Override
    public Map<ProjectBeneficiary, List<Error>> validate(BeneficiaryBulkRequest request) {
        log.trace("Entering validate (PbIsDeletedValidator)");
        log.info("Validating isDeleted field");
        log.debug("Validating {} beneficiaries for isDeleted field", request.getProjectBeneficiaries() != null ? request.getProjectBeneficiaries().size() : 0);
        HashMap<ProjectBeneficiary, List<Error>> errorDetailsMap = new HashMap<>();
        List<ProjectBeneficiary> validProjectBeneficiaries = request.getProjectBeneficiaries();
        validProjectBeneficiaries.stream().filter(ProjectBeneficiary::getIsDeleted).forEach(projectBeneficiary -> {
            Error error = getErrorForIsDelete();
            populateErrorDetails(projectBeneficiary, error, errorDetailsMap);
        });
        log.debug("IsDeleted validation completed - found {} errors", errorDetailsMap.size());
        log.trace("Exiting validate (PbIsDeletedValidator)");
        return errorDetailsMap;
    }
}
