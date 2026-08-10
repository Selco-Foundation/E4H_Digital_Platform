package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.core.AdditionalFields;
import org.egov.common.models.core.Field;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.models.project.ProjectFacility;
import org.egov.common.producer.Producer;
import org.egov.common.validator.Validator;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.repository.FieldPlanFacilityRepository;
import org.egov.field_planner.repository.FieldPlannerRepository;
import org.egov.field_planner.service.enrichment.FieldPlannerEnrichment;
import org.egov.field_planner.util.MDMSUtils;
import org.egov.field_planner.validator.FieldPlannerValidator;
import org.egov.field_planner.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.field_planner.Constants.GET_FIELDPLAN_ID;

@Service
@Slf4j
public class FieldPlannerFacilityService {

    private final FieldPlanFacilityRepository fieldPlanFacilityRepository;

    private final FieldPlannerRepository fieldPlannerRepository;
    private final Producer producer;

    private final ServiceRequestRepository serviceRequestClient;
    private final FieldPlannerEnrichment fieldPlannerEnrichment;

    private final List<Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility>> validators;
    private final FieldPlannerConfiguration fieldPlannerConfiguration;
    private final MDMSUtils mdmsUtils;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    @Autowired
    public FieldPlannerFacilityService(
            FieldPlanFacilityRepository fieldPlanFacilityRepository, List<Validator<FieldPlanFacilityBulkRequest, FieldPlanFacility>> validators,
            FieldPlannerValidator fieldPlannerValidator, FieldPlannerEnrichment fieldPlannerEnrichment, FieldPlannerConfiguration fieldPlannerConfiguration,
            Producer producer, FieldPlannerRepository fieldPlannerRepository, MDMSUtils mdmsUtils, ServiceRequestRepository serviceRequestClient, @Qualifier("objectMapper") ObjectMapper mapper) {
            this.producer = producer;
            this.fieldPlannerConfiguration = fieldPlannerConfiguration;
            this.fieldPlanFacilityRepository = fieldPlanFacilityRepository;
            this.fieldPlannerEnrichment = fieldPlannerEnrichment;
            this.mdmsUtils = mdmsUtils;
            this.validators = validators;
            this.serviceRequestClient = serviceRequestClient;
            this.mapper = mapper;
            this.fieldPlannerRepository = fieldPlannerRepository;
    }

    public FieldPlanFacility create(FieldPlanFacilityRequest request) {
        log.info("received request to create fieldplan facility");
        FieldPlanFacilityBulkRequest bulkRequest = FieldPlanFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .fieldPlanFacilities(Collections.singletonList(request.getFieldPlanFacility())).build();
        log.info("creating bulk request");
        return create(bulkRequest, false).get(0);
    }

    public List<FieldPlanFacility> create(FieldPlanFacilityBulkRequest request, boolean isBulk) {
        log.info("received request to create bulk fieldplan facility");
//
        validateCreateFieldPlanRequest(request);
        List<FieldPlanFacility> fieldPlanFacilities = request.getFieldPlanFacilities();
        try {
            if (!fieldPlanFacilities.isEmpty()) {
                log.info("processing {} valid entities", fieldPlanFacilities.size());
                fieldPlannerEnrichment.enrichFieldPlanFacilityOnCreate(fieldPlanFacilities, request);
                producer.push(fieldPlannerConfiguration.getCreateFieldPlanFacilityTopic(), fieldPlanFacilities);
                log.info("successfully created project facility");
            }
        } catch (Exception exception) {
            log.error("error occurred while creating project facility: {}", ExceptionUtils.getStackTrace(exception));
        }

        return fieldPlanFacilities;
    }

    public SearchResponse<FieldPlanFacility> search(FieldPlanFacilitySearchRequest request,
                                                  Integer limit,
                                                  Integer offset,
                                                  String tenantId,
                                                  Long lastChangedSince,
                                                  Boolean includeDeleted) throws Exception {
        log.info("received request to search project facility");

        if (isSearchByIdOnly(request.getCriteria())) {
            log.info("searching project facility by id");
            List<String> ids = request.getCriteria().getId();
            log.info("fetching fieldplan facility with ids: {}", ids);
            List<FieldPlanFacility> fieldPlanFacilities = fieldPlanFacilityRepository.findById(ids, includeDeleted).stream()
                    .filter(lastChangedSince(lastChangedSince))
                    .filter(havingTenantId(tenantId))
                    .filter(includeDeleted(includeDeleted))
                    .toList();
            return SearchResponse.<FieldPlanFacility>builder().response(fieldPlanFacilities).build();
        }
        log.info("searching project facility using criteria");
        return fieldPlanFacilityRepository.findWithCount(request.getCriteria(),
                limit, offset, tenantId, lastChangedSince, includeDeleted);
    }

    public List<SystemTypeCapacity> searchSystemTypeCapacity(FieldPlanFacilitySearchRequest request,
                                                               Integer limit,
                                                               Integer offset,
                                                               String tenantId,
                                                               Long lastChangedSince,
                                                               Boolean includeDeleted) throws Exception {
        log.trace("Entering searchSystemTypeCapacity method for field plan facility");
        log.info("Received request to search systemType/totalSystemCapacity for field plan facility, tenant: {}", tenantId);

        SearchResponse<FieldPlanFacility> searchResponse = search(request, limit, offset, tenantId, lastChangedSince, includeDeleted);
        Map<String, SystemTypeCapacity> uniqueCombinations = new LinkedHashMap<>();
        for (FieldPlanFacility fieldPlanFacility : searchResponse.getResponse()) {
            SystemTypeCapacity systemTypeCapacity = toSystemTypeCapacity(fieldPlanFacility);
            if (systemTypeCapacity == null) {
                continue;
            }
            String key = systemTypeCapacity.getSystemType() + "|" + systemTypeCapacity.getTotalSystemCapacity();
            uniqueCombinations.putIfAbsent(key, systemTypeCapacity);
        }
        List<SystemTypeCapacity> result = new ArrayList<>(uniqueCombinations.values());

        log.info("SystemType/totalSystemCapacity search completed, found {} unique combinations", result.size());
        log.trace("Exiting searchSystemTypeCapacity method");
        return result;
    }

    private SystemTypeCapacity toSystemTypeCapacity(FieldPlanFacility fieldPlanFacility) {
        AdditionalFields additionalFields = fieldPlanFacility.getAdditionalFields();
        if (additionalFields == null || additionalFields.getFields() == null) {
            return null;
        }
        String systemType = null;
        String totalSystemCapacity = null;
        String customTotalSystemCapacity = null;
        for (Field field : additionalFields.getFields()) {
            if ("systemType".equals(field.getKey())) {
                systemType = field.getValue();
            } else if ("totalSystemCapacity".equals(field.getKey())) {
                totalSystemCapacity = field.getValue();
            } else if ("customTotalSystemCapacity".equals(field.getKey())) {
                customTotalSystemCapacity = field.getValue();
            }
        }
        if ("CUSTOM".equalsIgnoreCase(totalSystemCapacity)) {
            totalSystemCapacity = customTotalSystemCapacity;
        }
        if (systemType == null && totalSystemCapacity == null) {
            return null;
        }
        return SystemTypeCapacity.builder()
                .systemType(systemType)
                .totalSystemCapacity(totalSystemCapacity)
                .build();
    }

    public FieldPlanFacility unassign(FieldPlanFacilityRequest request) {
        log.info("received request to create fieldplan facility");
        FieldPlanFacilityBulkRequest bulkRequest = FieldPlanFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .fieldPlanFacilities(Collections.singletonList(request.getFieldPlanFacility())).build();
        log.info("creating bulk request");
        return unassignBulk(bulkRequest, false).get(0);
    }

    public List<FieldPlanFacility> unassignBulk(FieldPlanFacilityBulkRequest request, boolean isBulk) {
        log.info("received request to unassign bulk fieldplan facility");
//
        validateCreateFieldPlanRequest(request);
        List<FieldPlanFacility> fieldPlanFacilities = request.getFieldPlanFacilities();
        try {
            if (!fieldPlanFacilities.isEmpty()) {
                for (FieldPlanFacility fieldPlanFacility : fieldPlanFacilities){
                    log.info("processing {} valid entities", fieldPlanFacilities.size());
                    fieldPlannerEnrichment.enrichFieldPlanFacilityRequestOnDelete(fieldPlanFacility, request.getRequestInfo());
                }
                producer.push(fieldPlannerConfiguration.getDeleteFieldPlanFacilityTopic(), fieldPlanFacilities);
                log.info("successfully created project facility");
            }
        } catch (Exception exception) {
            log.error("error occurred while creating project facility: {}", ExceptionUtils.getStackTrace(exception));
        }

        return fieldPlanFacilities;
    }

    // Fields a user may edit on an already-linked FieldPlanFacility - facilityType and the
    // facilityId/fieldPlanId link itself stay immutable via this path.
    private static final Set<String> UPDATABLE_ADDITIONAL_FIELD_KEYS = Set.of(
            "systemType", "solarSolutionDesignType", "totalSystemCapacity",
            "customSolarSolutionDesignType", "customTotalSystemCapacity"
    );

    public List<FieldPlanFacility> updateBulk(FieldPlanFacilityBulkRequest request, boolean isBulk) {
        log.info("received request to update bulk fieldplan facility");
        List<FieldPlanFacility> fieldPlanFacilities = request.getFieldPlanFacilities();

        List<String> ids = fieldPlanFacilities.stream().map(FieldPlanFacility::getId).toList();
        List<FieldPlanFacility> existingFacilities = fieldPlanFacilityRepository.findById(ids, false);
        Map<String, FieldPlanFacility> existingById = existingFacilities.stream()
                .collect(Collectors.toMap(FieldPlanFacility::getId, f -> f));

        Map<String, String> errorMap = new HashMap<>();
        AtomicInteger counter = new AtomicInteger(1);
        for (FieldPlanFacility fieldPlanFacility : fieldPlanFacilities) {
            if (fieldPlanFacility.getId() == null || !existingById.containsKey(fieldPlanFacility.getId())) {
                errorMap.put("INVALID_FIELDPLAN_FACILITY_ID" + counter.getAndIncrement(),
                        "FieldPlanFacility does not exist: " + fieldPlanFacility.getId());
            }
        }
        if (!errorMap.isEmpty()) {
            throw new CustomException(errorMap);
        }

        try {
            if (!fieldPlanFacilities.isEmpty()) {
                for (FieldPlanFacility fieldPlanFacility : fieldPlanFacilities) {
                    FieldPlanFacility fieldPlanFacilityFromDb = existingById.get(fieldPlanFacility.getId());
                    mergeUpdatableAdditionalFields(fieldPlanFacility, fieldPlanFacilityFromDb);
                    fieldPlanFacility.setFacilityId(fieldPlanFacilityFromDb.getFacilityId());
                    fieldPlanFacility.setFieldPlanId(fieldPlanFacilityFromDb.getFieldPlanId());
                    fieldPlanFacility.setTenantId(fieldPlanFacilityFromDb.getTenantId());
                    fieldPlanFacility.setIsDeleted(fieldPlanFacilityFromDb.getIsDeleted());
                    fieldPlannerEnrichment.enrichFieldPlanFacilityRequestOnUpdate(fieldPlanFacility, fieldPlanFacilityFromDb, request.getRequestInfo());
                }
                producer.push(fieldPlannerConfiguration.getUpdateFieldPlanFacilityTopic(), fieldPlanFacilities);
                log.info("successfully pushed fieldplan facility update");
            }
        } catch (Exception exception) {
            log.error("error occurred while updating fieldplan facility: {}", ExceptionUtils.getStackTrace(exception));
        }

        return fieldPlanFacilities;
    }

    /**
     * Overlays only the user-editable keys (systemType, solarSolutionDesignType,
     * totalSystemCapacity, customSolarSolutionDesignType, customTotalSystemCapacity) from the
     * incoming request onto the existing DB record's additionalFields - every other existing key
     * (e.g. facilityType) is preserved unchanged, same merge-not-replace approach as
     * FieldPlanTemplateService.mergeTemplateData.
     */
    private void mergeUpdatableAdditionalFields(FieldPlanFacility fieldPlanFacility, FieldPlanFacility fieldPlanFacilityFromDb) {
        Map<String, String> merged = new LinkedHashMap<>();
        AdditionalFields existingAdditionalFields = fieldPlanFacilityFromDb.getAdditionalFields();
        if (existingAdditionalFields != null && existingAdditionalFields.getFields() != null) {
            for (Field field : existingAdditionalFields.getFields()) {
                if (field.getKey() != null) {
                    merged.put(field.getKey(), field.getValue());
                }
            }
        }

        AdditionalFields incomingAdditionalFields = fieldPlanFacility.getAdditionalFields();
        if (incomingAdditionalFields != null && incomingAdditionalFields.getFields() != null) {
            for (Field field : incomingAdditionalFields.getFields()) {
                if (field.getKey() != null && UPDATABLE_ADDITIONAL_FIELD_KEYS.contains(field.getKey())) {
                    merged.put(field.getKey(), field.getValue());
                }
            }
        }

        List<Field> mergedFields = merged.entrySet().stream()
                .map(entry -> Field.builder().key(entry.getKey()).value(entry.getValue()).build())
                .toList();

        fieldPlanFacility.setAdditionalFields(AdditionalFields.builder()
                .schema(existingAdditionalFields != null ? existingAdditionalFields.getSchema() : "FieldPlanFacility")
                .version(existingAdditionalFields != null ? existingAdditionalFields.getVersion() : 1)
                .fields(mergedFields)
                .build());
    }

    public void validateCreateFieldPlanRequest(FieldPlanFacilityBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();

        //Verify if facilityId is valid
        validateFacilityIds(request, errorMap);
        //Verify if FieldPlanId is valid
        validateFieldPlanIds(request, errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    private void validateFacilityIds(FieldPlanFacilityBulkRequest request, Map<String, String> errorMap) {

        List<FieldPlanFacility> validEntities = request.getFieldPlanFacilities();
        if (!validEntities.isEmpty()) {
            AtomicInteger counter = new AtomicInteger(1);
            for (FieldPlanFacility facility : validEntities){
                try {
                    Facility response = getFacilityById(facility.getFacilityId());
                    if(response==null)
                        throw new CustomException("FACILITY_ERROR", "Facility ID do not exist");

                    if (!response.getFacilityId().equals(facility.getFacilityId())) {
                        int i = counter.getAndIncrement();
                        errorMap.put("INVALID_FACILITY"+i, "FacilityId does not exist: " + facility.getFacilityId());
                    }

                } catch (Exception e) {
                    log.error("error while fetching facility list", ExceptionUtils.getStackTrace(e));
                    throw new CustomException("FACILITY_ERROR", "error while calling facility service");
                }
            }
        }
    }

    private void validateFieldPlanIds(FieldPlanFacilityBulkRequest request, Map<String, String> errorMap) {
        List<FieldPlanFacility> validEntities = request.getFieldPlanFacilities();
        if (!validEntities.isEmpty()) {
            Class<?> objClass = getObjClass(validEntities);
            Method idMethod = getMethod(GET_FIELDPLAN_ID, objClass);
            List<String> entityIds = validEntities.stream().map(FieldPlanFacility::getFieldPlanId).toList();
            try {
                AtomicInteger counter = new AtomicInteger(1);
                List<String> existingFieldPlansIds = fieldPlannerRepository.validateIds(entityIds, getIdFieldName(idMethod));
                validEntities.stream().filter(entity -> {
                            boolean invalid = !existingFieldPlansIds.contains(entity.getFieldPlanId());
                            if (invalid) {
                                int i = counter.getAndIncrement();
                                errorMap.put("INVALID_FIELDPLAN"+i, "FIELDPLAN_ID does not exist: " + entity.getFieldPlanId());
                            }
                            return invalid;
                        })
                        .toList();

            } catch (Exception e) {
                log.error("error while fetching facility list", ExceptionUtils.getStackTrace(e));
                throw new CustomException("FIELDPLAN_ERROR", "error while calling fieldplan");
            }
        }
    }

    public Facility getFacilityById(String facilityId) {

        String url = fieldPlannerConfiguration.getFacilityServiceHost() + fieldPlannerConfiguration.getFacilityServiceSearchUrlV2()+ "?facilityId="+facilityId;
        Object response = serviceRequestClient.fetchResult(new StringBuilder(url));

        FacilitySearchResponse facilityList = mapper.convertValue(response, FacilitySearchResponse.class);
        if(facilityList != null && facilityList.getFacilities() !=null && facilityList.getFacilities().size() > 0){
            return facilityList.getFacilities().get(0);
        }
        return null;
    }

    /**
     * Searches facilities by a specific boundary code
     */
    public Set<String> searchFacilitiesByBoundaryCode(String boundaryCode, String tenantId, RequestInfo requestInfo) {
        Set<String> facilityIds = new HashSet<>();

        try {
            // Build facility search URL with boundary code filter
            StringBuilder facilitySearchUrl = new StringBuilder();
            facilitySearchUrl.append(fieldPlannerConfiguration.getFacilityServiceHost())
                    .append(fieldPlannerConfiguration.getFacilityServiceSearchUrlV2())
                    .append("?tenantId=")
                    .append(tenantId)
                    .append("&boundaryCode=")
                    .append(boundaryCode);

            log.debug("Searching facilities for boundary code: {} with URL: {}", boundaryCode, facilitySearchUrl);

            // Call facility service
            Object response = serviceRequestClient.fetchResult(facilitySearchUrl);

            if (response != null) {
                FacilitySearchResponse facilitySearchResponse = mapper.convertValue(response, FacilitySearchResponse.class);

                if (facilitySearchResponse != null && facilitySearchResponse.getFacilities() != null) {
                    facilityIds = facilitySearchResponse.getFacilities().stream()
                            .map(Facility::getFacilityId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    log.debug("Found {} facilities for boundary code: {}", facilityIds.size(), boundaryCode);
                }
            }

        } catch (Exception e) {
            log.error("Error searching facilities for boundary code: {}", boundaryCode, e);
        }

        return facilityIds;
    }


}
