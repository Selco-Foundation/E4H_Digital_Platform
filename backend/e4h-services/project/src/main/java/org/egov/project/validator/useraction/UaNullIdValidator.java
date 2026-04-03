package org.egov.project.validator.useraction;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.useraction.UserAction;
import org.egov.common.models.project.useraction.UserActionBulkRequest;
import org.egov.common.validator.Validator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.validateForNullId;
import static org.egov.project.Constants.GET_USER_ACTION;

@Component
@Order(value = 1)
@Slf4j
public class UaNullIdValidator implements Validator<UserActionBulkRequest, UserAction> {

    @Override
    public Map<UserAction, List<Error>> validate(UserActionBulkRequest request) {
        log.trace("Entering validate (UaNullIdValidator)");
        log.info("Validating for null ID");
        log.debug("Validating {} user actions for null ID", request.getUserActions() != null ? request.getUserActions().size() : 0);
        Map<UserAction, List<Error>> result = validateForNullId(request, GET_USER_ACTION);
        log.debug("Null ID validation completed - found {} errors", result != null ? result.size() : 0);
        log.trace("Exiting validate (UaNullIdValidator)");
        return result;
    }
}
