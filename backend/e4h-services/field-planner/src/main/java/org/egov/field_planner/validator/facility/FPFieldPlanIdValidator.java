package org.egov.field_planner.validator.facility;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.validator.Validator;
import org.egov.field_planner.repository.FieldPlanFacilityRepository;
import org.egov.field_planner.web.models.FieldPlanFacility;
import org.egov.field_planner.web.models.FieldPlanFacilityBulkRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.common.utils.ValidatorUtils.getErrorForNonExistentRelatedEntity;
import static org.egov.field_planner.Constants.GET_FIELDPLAN_ID;

@Component
@Order(value = 6)
@Slf4j
public class FPFieldPlanIdValidator implements Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility> {

    private final FieldPlanFacilityRepository fieldPlanFacilityRepository;

    @Autowired
    public FPFieldPlanIdValidator(FieldPlanFacilityRepository fieldPlanFacilityRepository) {
        this.fieldPlanFacilityRepository = fieldPlanFacilityRepository;
    }


    @Override
    public Map<FieldPlanFacility, List<Error>> validate(FieldPlanFacilityBulkRequest request) {
        log.info("validating field plan id");
        Map<FieldPlanFacility, List<Error>> errorDetailsMap = new HashMap<>();
        List<FieldPlanFacility> validEntities = request.getFieldPlanFacilities().stream()
                .filter(notHavingErrors())
                .toList();
        if (!validEntities.isEmpty()) {
            Class<?> objClass = getObjClass(validEntities);
            Method idMethod = getMethod(GET_FIELDPLAN_ID, objClass);
            Map<String, FieldPlanFacility> eMap = getIdToObjMap(validEntities, idMethod);
            if (!eMap.isEmpty()) {
                List<String> entityIds = new ArrayList<>(eMap.keySet());
                List<String> existingFieldPlansIds = fieldPlanFacilityRepository.validateIds(entityIds,
                        getIdFieldName(idMethod));
                List<FieldPlanFacility> invalidEntities = validEntities.stream().filter(notHavingErrors()).filter(entity ->
                                !existingFieldPlansIds.contains(entity.getFieldPlanId()))
                        .toList();
                invalidEntities.forEach(fieldPlanFacility -> {
                    Error error = getErrorForNonExistentRelatedEntity(fieldPlanFacility.getFieldPlanId());
                    populateErrorDetails(fieldPlanFacility, error, errorDetailsMap);
                });
            }
        }
        return errorDetailsMap;
    }
}
