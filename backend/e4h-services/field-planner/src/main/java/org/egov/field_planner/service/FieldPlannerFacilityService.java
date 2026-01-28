package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.contract.request.RequestInfo;
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
        log.trace("Entering create method for field plan facility");
        log.info("Received request to create field plan facility");
        FieldPlanFacilityBulkRequest bulkRequest = FieldPlanFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .fieldPlanFacilities(Collections.singletonList(request.getFieldPlanFacility())).build();
        log.debug("Created bulk request for field plan facility");
        FieldPlanFacility result = create(bulkRequest, false).get(0);
        log.info("Field plan facility created successfully with ID: {}", result.getId());
        log.trace("Exiting create method");
        return result;
    }

    public List<FieldPlanFacility> create(FieldPlanFacilityBulkRequest request, boolean isBulk) {
        log.trace("Entering create method for bulk field plan facility, isBulk: {}", isBulk);
        log.info("Received request to create bulk field plan facility, count: {}", request.getFieldPlanFacilities().size());
        
        validateCreateFieldPlanRequest(request);
        log.debug("Field plan facility creation request validated");
        
        List<FieldPlanFacility> fieldPlanFacilities = request.getFieldPlanFacilities();
        try {
            if (!fieldPlanFacilities.isEmpty()) {
                log.debug("Processing {} field plan facilities", fieldPlanFacilities.size());
                fieldPlannerEnrichment.enrichFieldPlanFacilityOnCreate(fieldPlanFacilities, request);
                log.debug("Field plan facilities enriched, pushing to Kafka");
                producer.push(fieldPlannerConfiguration.getCreateFieldPlanFacilityTopic(), fieldPlanFacilities);
                log.info("Successfully created {} field plan facilities", fieldPlanFacilities.size());
            } else {
                log.warn("Empty field plan facility list in create request");
            }
        } catch (Exception exception) {
            log.error("Error occurred while creating field plan facilities", exception);
        }

        log.trace("Exiting create method");
        return fieldPlanFacilities;
    }

    public SearchResponse<FieldPlanFacility> search(FieldPlanFacilitySearchRequest request,
                                                  Integer limit,
                                                  Integer offset,
                                                  String tenantId,
                                                  Long lastChangedSince,
                                                  Boolean includeDeleted) throws Exception {
        log.trace("Entering search method for field plan facility");
        log.info("Received request to search field plan facility for tenant: {}", tenantId);

        if (isSearchByIdOnly(request.getCriteria())) {
            log.debug("Searching field plan facility by ID");
            List<String> ids = request.getCriteria().getId();
            log.debug("Fetching field plan facilities with IDs: {}", ids);
            List<FieldPlanFacility> fieldPlanFacilities = fieldPlanFacilityRepository.findById(ids, includeDeleted).stream()
                    .filter(lastChangedSince(lastChangedSince))
                    .filter(havingTenantId(tenantId))
                    .filter(includeDeleted(includeDeleted))
                    .toList();
            log.info("Field plan facility search by ID completed, found {} results", fieldPlanFacilities.size());
            log.trace("Exiting search method");
            return SearchResponse.<FieldPlanFacility>builder().response(fieldPlanFacilities).build();
        }
        log.debug("Searching field plan facility using criteria, limit: {}, offset: {}", limit, offset);
        SearchResponse<FieldPlanFacility> result = fieldPlanFacilityRepository.findWithCount(request.getCriteria(),
                limit, offset, tenantId, lastChangedSince, includeDeleted);
        log.info("Field plan facility search completed, found {} results", result.getTotalCount());
        log.trace("Exiting search method");
        return result;
    }

    public FieldPlanFacility unassign(FieldPlanFacilityRequest request) {
        log.trace("Entering unassign method for field plan facility");
        log.info("Received request to unassign field plan facility");
        FieldPlanFacilityBulkRequest bulkRequest = FieldPlanFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .fieldPlanFacilities(Collections.singletonList(request.getFieldPlanFacility())).build();
        log.debug("Created bulk request for field plan facility unassign");
        FieldPlanFacility result = unassignBulk(bulkRequest, false).get(0);
        log.info("Field plan facility unassigned successfully with ID: {}", result.getId());
        log.trace("Exiting unassign method");
        return result;
    }

    public List<FieldPlanFacility> unassignBulk(FieldPlanFacilityBulkRequest request, boolean isBulk) {
        log.trace("Entering unassignBulk method for field plan facility, isBulk: {}", isBulk);
        log.info("Received request to unassign bulk field plan facility, count: {}", request.getFieldPlanFacilities().size());
        
        validateCreateFieldPlanRequest(request);
        log.debug("Field plan facility unassign request validated");
        
        List<FieldPlanFacility> fieldPlanFacilities = request.getFieldPlanFacilities();
        try {
            if (!fieldPlanFacilities.isEmpty()) {
                log.debug("Processing {} field plan facilities for unassign", fieldPlanFacilities.size());
                for (FieldPlanFacility fieldPlanFacility : fieldPlanFacilities){
                    fieldPlannerEnrichment.enrichFieldPlanFacilityRequestOnDelete(fieldPlanFacility, request.getRequestInfo());
                }
                log.debug("Field plan facilities enriched, pushing to Kafka");
                producer.push(fieldPlannerConfiguration.getDeleteFieldPlanFacilityTopic(), fieldPlanFacilities);
                log.info("Successfully unassigned {} field plan facilities", fieldPlanFacilities.size());
            } else {
                log.warn("Empty field plan facility list in unassign request");
            }
        } catch (Exception exception) {
            log.error("Error occurred while unassigning field plan facilities", exception);
        }

        log.trace("Exiting unassignBulk method");
        return fieldPlanFacilities;
    }

    public void validateCreateFieldPlanRequest(FieldPlanFacilityBulkRequest request) {
        log.trace("Entering validateCreateFieldPlanRequest method");
        log.debug("Validating field plan facility request with {} facilities", request.getFieldPlanFacilities().size());
        Map<String, String> errorMap = new HashMap<>();

        //Verify if facilityId is valid
        validateFacilityIds(request, errorMap);
        log.debug("Facility IDs validation completed, error count: {}", errorMap.size());
        //Verify if FieldPlanId is valid
        validateFieldPlanIds(request, errorMap);
        log.debug("Field plan IDs validation completed, total error count: {}", errorMap.size());

        if (!errorMap.isEmpty()) {
            log.error("Field plan facility request validation failed with {} errors", errorMap.size());
            throw new CustomException(errorMap);
        }
        log.debug("Field plan facility request validation successful");
        log.trace("Exiting validateCreateFieldPlanRequest method");
    }

    private void validateFacilityIds(FieldPlanFacilityBulkRequest request, Map<String, String> errorMap) {
        log.trace("Entering validateFacilityIds method");
        log.debug("Validating {} facility IDs", request.getFieldPlanFacilities().size());
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
                    log.error("Error while fetching facility list for facility ID: {}", facility.getFacilityId(), e);
                    throw new CustomException("FACILITY_ERROR", "error while calling facility service");
                }
            }
        }
        log.debug("Facility IDs validation completed");
        log.trace("Exiting validateFacilityIds method");
    }

    private void validateFieldPlanIds(FieldPlanFacilityBulkRequest request, Map<String, String> errorMap) {
        log.trace("Entering validateFieldPlanIds method");
        log.debug("Validating {} field plan IDs", request.getFieldPlanFacilities().size());
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
                log.error("Error while validating field plan IDs", e);
                throw new CustomException("FIELDPLAN_ERROR", "error while calling fieldplan");
            }
        }
        log.debug("Field plan IDs validation completed");
        log.trace("Exiting validateFieldPlanIds method");
    }

    public Facility getFacilityById(String facilityId) {
        log.trace("Entering getFacilityById method for facility ID: {}", facilityId);
        log.debug("Fetching facility details from facility service");
        String url = fieldPlannerConfiguration.getFacilityServiceHost() + fieldPlannerConfiguration.getFacilityServiceSearchUrlV2()+ "?facilityId="+facilityId;
        Object response = serviceRequestClient.fetchResult(new StringBuilder(url));

        FacilitySearchResponse facilityList = mapper.convertValue(response, FacilitySearchResponse.class);
        if(facilityList != null && facilityList.getFacilities() !=null && facilityList.getFacilities().size() > 0){
            log.debug("Successfully retrieved facility with ID: {}", facilityId);
            log.trace("Exiting getFacilityById method");
            return facilityList.getFacilities().get(0);
        }
        log.warn("Facility not found with ID: {}", facilityId);
        log.trace("Exiting getFacilityById method");
        return null;
    }

    /**
     * Searches facilities by a specific boundary code
     */
    public Set<String> searchFacilitiesByBoundaryCode(String boundaryCode, String tenantId, RequestInfo requestInfo) {
        log.trace("Entering searchFacilitiesByBoundaryCode method for boundary code: {}", boundaryCode);
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

        log.trace("Exiting searchFacilitiesByBoundaryCode method");
        return facilityIds;
    }


}
