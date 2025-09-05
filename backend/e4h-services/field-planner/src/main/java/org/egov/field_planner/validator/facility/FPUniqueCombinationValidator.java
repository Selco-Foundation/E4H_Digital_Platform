package org.egov.field_planner.validator.facility;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.ProjectFacility;
import org.egov.common.models.project.ProjectFacilityBulkRequest;
import org.egov.common.validator.Validator;
import org.egov.field_planner.repository.FieldPlanFacilityRepository;
import org.egov.field_planner.service.FieldPlannerFacilityService;
import org.egov.field_planner.web.models.FieldPlan;
import org.egov.field_planner.web.models.FieldPlanFacility;
import org.egov.field_planner.web.models.FieldPlanFacilityBulkRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.notHavingErrors;
import static org.egov.common.utils.CommonUtils.populateErrorDetails;
import static org.egov.common.utils.ValidatorUtils.getErrorForDuplicateMapping;
import static org.egov.field_planner.Constants.*;

@Component
@Order(value = 8)
@Slf4j
public class FPUniqueCombinationValidator implements Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility> {

    private final FieldPlanFacilityRepository fieldPlanFacilityRepository;

    public FPUniqueCombinationValidator(FieldPlanFacilityRepository fieldPlanFacilityRepository) {
        this.fieldPlanFacilityRepository = fieldPlanFacilityRepository;
    }

    @Override
    public Map<FieldPlanFacility, List<Error>> validate(FieldPlanFacilityBulkRequest request) {
        log.info("validating for project facility mapping uniqueness");
        Map<FieldPlanFacility, List<Error>> errorDetailsMap = new HashMap<>();

        List<FieldPlanFacility> validEntities = request.getFieldPlanFacilities().stream()
                .filter(notHavingErrors())
                .toList();
        if (!validEntities.isEmpty()) {
            validateFieldPlanFacilityMappingFromRequest(validEntities, errorDetailsMap);
            validEntities = request.getFieldPlanFacilities().stream()
                    .filter(notHavingErrors())
                    .toList();
            if (!validEntities.isEmpty()) {
                validateFieldPlanFacilityMappingFromDb(validEntities, errorDetailsMap);
            }
        }

        return errorDetailsMap;
    }

    private void validateFieldPlanFacilityMappingFromDb(List<FieldPlanFacility> validEntities,
                                                      Map<FieldPlanFacility, List<Error>> errorDetailsMap) {
        log.info("validating mapping from db");
        log.info("validating {} valid entities", validEntities.size());
        List<String> fieldPlanIds = validEntities.stream().map(FieldPlanFacility::getFieldPlanId)
                .toList();

        List<FieldPlanFacility> existingProjectFacilities = fieldPlanFacilityRepository.findById(fieldPlanIds,
                false, FIELDPLAN_ID);

        Map<String, FieldPlanFacility> existingIdMap = getMap(existingProjectFacilities);
        validEntities.stream().filter(entity -> {
            String combinationId = entity.getFacilityId() + PIPE + entity.getFieldPlanId();
            return existingIdMap.containsKey(combinationId)
                    && (entity.getId() == null || !entity.getId().equals(existingIdMap.get(combinationId).getId()));
        }).forEach(entity -> {
            Error error = getErrorForDuplicateMapping(entity.getFieldPlanId(),
                    entity.getFacilityId());
            populateErrorDetails(entity, error, errorDetailsMap);
        });

    }

    private void validateFieldPlanFacilityMappingFromRequest(List<FieldPlanFacility> validEntities,
                                                           Map<FieldPlanFacility, List<Error>> errorDetailsMap) {
        log.info("validating mapping from request");
        log.info("validating {} valid entities", validEntities.size());
        Map<String, FieldPlanFacility> map = getMap(validEntities);
        if (map.keySet().size() != validEntities.size()) {
            List<String> duplicates = map.keySet().stream().filter(id ->
                    validEntities.stream().filter(entity -> {
                        String combinationId = entity.getFacilityId() + PIPE + entity.getFieldPlanId();
                        return combinationId.equals(id);
                    }).count() > 1).toList();
            for (String key : duplicates) {
                FieldPlanFacility fieldPlanFacility = map.get(key);
                Error error = getErrorForDuplicateMapping(fieldPlanFacility.getFieldPlanId(),
                        fieldPlanFacility.getFacilityId());
                populateErrorDetails(fieldPlanFacility, error, errorDetailsMap);
            }
        }
    }

    private Map<String, FieldPlanFacility> getMap(List<FieldPlanFacility> validEntities) {
        Map<String, FieldPlanFacility> map = new HashMap<>();
        validEntities.forEach(entity -> map.put(entity.getFacilityId() + PIPE + entity.getFieldPlanId(), entity));
        return map;
    }
}
