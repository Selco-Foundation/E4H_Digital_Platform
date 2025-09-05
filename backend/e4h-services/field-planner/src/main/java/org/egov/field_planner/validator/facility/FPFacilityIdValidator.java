package org.egov.field_planner.validator.facility;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.Error;
import org.egov.common.models.facility.Facility;
import org.egov.common.models.facility.FacilityBulkResponse;
import org.egov.common.models.facility.FacilitySearch;
import org.egov.common.models.facility.FacilitySearchRequest;
import org.egov.common.validator.Validator;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.web.models.FieldPlanFacility;
import org.egov.field_planner.web.models.FieldPlanFacilityBulkRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.common.utils.ValidatorUtils.getErrorForEntityWithNetworkError;
import static org.egov.common.utils.ValidatorUtils.getErrorForNonExistentRelatedEntity;
import static org.egov.field_planner.Constants.GET_FACILITY_ID;

@Component
@Order(value = 7)
@Slf4j
public class FPFacilityIdValidator implements Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility> {

    private final ServiceRequestClient serviceRequestClient;

    private final FieldPlannerConfiguration fieldPlannerConfiguration;

    public FPFacilityIdValidator(ServiceRequestClient serviceRequestClient, FieldPlannerConfiguration fieldPlannerConfiguration) {
        this.serviceRequestClient = serviceRequestClient;
        this.fieldPlannerConfiguration = fieldPlannerConfiguration;
    }

    @Override
    public Map<FieldPlanFacility, List<Error>> validate(FieldPlanFacilityBulkRequest request) {
        log.info("validating for facility id");
        Map<FieldPlanFacility, List<Error>> errorDetailsMap = new HashMap<>();

        List<FieldPlanFacility> validEntities = request.getFieldPlanFacilities().stream()
                .filter(notHavingErrors())
                .toList();
        if (!validEntities.isEmpty()) {
            String tenantId = getTenantId(validEntities);
            Class<?> objClass = getObjClass(validEntities);
            Method idMethod = getMethod(GET_FACILITY_ID, objClass);
            Map<String, FieldPlanFacility> eMap = getIdToObjMap(validEntities, idMethod);

            if (!eMap.isEmpty()) {
                List<String> entityIds = new ArrayList<>(eMap.keySet());
                List<String> existingFacilityIds = validateFacilityIds(entityIds, validEntities,
                        tenantId, errorDetailsMap, request.getRequestInfo());
                List<FieldPlanFacility> invalidEntities = validEntities.stream().filter(notHavingErrors()).filter(entity ->
                                !existingFacilityIds.contains(entity.getFacilityId()))
                        .toList();
                invalidEntities.forEach(fieldPlanFacility -> {
                    Error error = getErrorForNonExistentRelatedEntity(fieldPlanFacility.getFacilityId());
                    populateErrorDetails(fieldPlanFacility, error, errorDetailsMap);
                });
            }
        }

        return errorDetailsMap;
    }

    private List<String> validateFacilityIds(List<String> entityIds,
                                             List<FieldPlanFacility> projectFacilities,
                                             String tenantId,
                                             Map<FieldPlanFacility, List<Error>> errorDetailsMap,
                                             RequestInfo requestInfo) {

        FacilitySearchRequest facilitySearchRequest = FacilitySearchRequest.builder()
                .facility(FacilitySearch.builder().id(entityIds).build())
                .requestInfo(requestInfo)
                .build();

        try {
            FacilityBulkResponse response = serviceRequestClient.fetchResult(
                    new StringBuilder(fieldPlannerConfiguration.getFacilityServiceHost()
                            + fieldPlannerConfiguration.getFacilityServiceSearchUrl()
                            + "?limit=" + fieldPlannerConfiguration.getSearchApiLimit()
                            + "&offset=0&tenantId=" + tenantId),
                    facilitySearchRequest,
                    FacilityBulkResponse.class);
            return response.getFacilities().stream().map(Facility::getId).toList();
        } catch (Exception e) {
            log.error("error while fetching facility list", ExceptionUtils.getStackTrace(e));
            projectFacilities.forEach(b -> {
                Error error = getErrorForEntityWithNetworkError();
                populateErrorDetails(b, error, errorDetailsMap);
            });
            return entityIds;
        }
    }
}
