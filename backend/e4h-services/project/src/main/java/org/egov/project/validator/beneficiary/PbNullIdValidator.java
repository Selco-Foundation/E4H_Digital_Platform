package org.egov.project.validator.beneficiary;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.BeneficiaryBulkRequest;
import org.egov.common.models.project.ProjectBeneficiary;
import org.egov.common.validator.Validator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.validateForNullId;
import static org.egov.project.Constants.GET_PROJECT_BENEFICIARIES;

@Component
@Order(value = 1)
@Slf4j
public class PbNullIdValidator implements Validator<BeneficiaryBulkRequest, ProjectBeneficiary> {

    @Override
    public Map<ProjectBeneficiary, List<Error>> validate(BeneficiaryBulkRequest request) {
        log.trace("Entering validate (PbNullIdValidator)");
        log.info("Validating for null ID");
        log.debug("Validating {} beneficiaries for null ID", request.getProjectBeneficiaries() != null ? request.getProjectBeneficiaries().size() : 0);
        Map<ProjectBeneficiary, List<Error>> result = validateForNullId(request, GET_PROJECT_BENEFICIARIES);
        log.debug("Null ID validation completed - found {} errors", result != null ? result.size() : 0);
        log.trace("Exiting validate (PbNullIdValidator)");
        return result;
    }
}
