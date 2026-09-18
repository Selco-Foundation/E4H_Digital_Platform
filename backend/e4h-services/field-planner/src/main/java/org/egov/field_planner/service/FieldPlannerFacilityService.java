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
        log.trace("Entering create method for installation plan facility");
        log.info("Received request to create installation plan facility");
        FieldPlanFacilityBulkRequest bulkRequest = FieldPlanFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .fieldPlanFacilities(Collections.singletonList(request.getFieldPlanFacility())).build();
        log.debug("Created bulk request for installation plan facility");
        FieldPlanFacility result = create(bulkRequest, false).get(0);
        log.info("Installation plan facility created successfully with ID: {}", result.getId());
        log.trace("Exiting create method");
        return result;
    }

    public List<FieldPlanFacility> create(FieldPlanFacilityBulkRequest request, boolean isBulk) {
        log.trace("Entering create method for bulk installation plan facility, isBulk: {}", isBulk);
        log.info("Received request to create bulk installation plan facility, count: {}", request.getFieldPlanFacilities().size());

        validateCreateFieldPlanRequest(request);
        log.debug("Installation plan facility creation request validated");

        List<FieldPlanFacility> fieldPlanFacilities = request.getFieldPlanFacilities();
        try {
            if (!fieldPlanFacilities.isEmpty()) {
                log.debug("Processing {} installation plan facilities", fieldPlanFacilities.size());
                fieldPlannerEnrichment.enrichFieldPlanFacilityOnCreate(fieldPlanFacilities, request);
                log.debug("Installation plan facilities enriched, pushing to Kafka");
                producer.push(fieldPlannerConfiguration.getCreateFieldPlanFacilityTopic(), fieldPlanFacilities);
                log.info("Successfully created {} installation plan facilities", fieldPlanFacilities.size());
            } else {
                log.warn("Empty installation plan facility list in create request");
            }
        } catch (Exception exception) {
            log.error("Error occurred while creating installation plan facilities", exception);
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
        log.trace("Entering search method for installation plan facility");
        log.info("Received request to search installation plan facility for tenant: {}", tenantId);

        if (isSearchByIdOnly(request.getCriteria())) {
            log.debug("Searching installation plan facility by ID");
            List<String> ids = request.getCriteria().getId();
            log.debug("Fetching installation plan facilities with IDs: {}", ids);
            List<FieldPlanFacility> fieldPlanFacilities = fieldPlanFacilityRepository.findById(ids, includeDeleted).stream()
                    .filter(lastChangedSince(lastChangedSince))
                    .filter(havingTenantId(tenantId))
                    .filter(includeDeleted(includeDeleted))
                    .toList();
            log.info("Installation plan facility search by ID completed, found {} results", fieldPlanFacilities.size());
            log.trace("Exiting search method");
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
        log.trace("Entering searchSystemTypeCapacity method for installation plan facility");
        log.info("Received request to search systemType/totalSystemCapacity for installation plan facility, tenant: {}", tenantId);

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

    /**
     * Resolves the system type of each requested facility, for callers that need the value keyed by
     * facility rather than the plan-wide combinations {@link #searchSystemTypeCapacity} returns.
     *
     * <p>Walks every matching row rather than honouring a caller-supplied page: this answers "what is
     * this facility's system type", and a truncated page would report no system type for facilities
     * that have one - which, for the AMC index sync that consumes this, means overwriting a good
     * indexed value with "Not Applicable". Facility ids are therefore mandatory, so the walk can never
     * degrade into a scan of every installation plan facility in the tenant.
     *
     * @return one entry per requested facility that is linked to an installation plan. Facilities with
     *         no linked plan are absent rather than present-with-null, so a caller can tell "nothing
     *         recorded" apart from "nothing to record against".
     */
    public List<FacilitySystemType> searchSystemTypeByFacilityIds(FieldPlanFacilitySearchRequest request,
                                                                  String tenantId,
                                                                  Long lastChangedSince,
                                                                  Boolean includeDeleted) throws Exception {
        log.trace("Entering searchSystemTypeByFacilityIds method for installation plan facility");
        List<String> facilityIds = request.getCriteria() == null ? null : request.getCriteria().getFacility_id();
        if (facilityIds == null || facilityIds.isEmpty()) {
            throw new CustomException("FACILITY_ID_REQUIRED",
                    "At least one facilityId is required to resolve a facility's system type");
        }
        log.info("Received request to resolve systemType for {} facility/facilities, tenant: {}",
                facilityIds.size(), tenantId);

        Map<String, FieldPlanFacility> winnerByFacilityId = new LinkedHashMap<>();
        int pageSize = systemTypePageSize();
        int offset = 0;
        while (true) {
            SearchResponse<FieldPlanFacility> page =
                    search(request, pageSize, offset, tenantId, lastChangedSince, includeDeleted);
            List<FieldPlanFacility> rows = page == null ? null : page.getResponse();
            if (rows == null || rows.isEmpty()) {
                break;
            }
            for (FieldPlanFacility row : rows) {
                String facilityId = row.getFacilityId();
                if (facilityId == null || facilityId.isBlank()) {
                    continue;
                }
                winnerByFacilityId.merge(facilityId, row,
                        FieldPlannerFacilityService::preferredSystemTypeRow);
            }
            if (rows.size() < pageSize) {
                break;
            }
            offset += pageSize;
        }

        List<FacilitySystemType> result = winnerByFacilityId.entrySet().stream()
                .map(entry -> FacilitySystemType.builder()
                        .facilityId(entry.getKey())
                        .systemType(additionalFieldValue(entry.getValue(), "systemType"))
                        .build())
                .toList();
        log.info("SystemType resolution completed, resolved {} of {} requested facility/facilities",
                result.size(), facilityIds.size());
        log.trace("Exiting searchSystemTypeByFacilityIds method");
        return result;
    }

    /**
     * Rows per page for the systemType walk, read from the service's own search cap rather than
     * hardcoded. Asking for more than {@code project.search.max.limit} would come back clamped to
     * exactly the cap, and the "a short page means the last page" check would read that as the end of
     * the data and silently drop every row after it.
     */
    private int systemTypePageSize() {
        Integer maxLimit = fieldPlannerConfiguration.getMaxLimit();
        return maxLimit == null || maxLimit < 1 ? SYSTEM_TYPE_PAGE_SIZE_FALLBACK : maxLimit;
    }

    /** Fallback page size for the systemType walk when no search cap is configured. */
    private static final int SYSTEM_TYPE_PAGE_SIZE_FALLBACK = 100;

    /**
     * Which of two installation-plan links a facility's system type should come from.
     *
     * <p>A row carrying no system type never wins, even when it is the more recent: a facility newly
     * linked to a plan that has not captured a system type yet must keep reporting the value that was
     * actually recorded, instead of suddenly reporting nothing. Among rows that do carry one, the most
     * recently modified wins, with created time then id breaking ties so repeated calls resolve to the
     * same plan rather than flipping between two equally recent ones - which would otherwise make the
     * indexed system type change for no real reason.
     */
    private static FieldPlanFacility preferredSystemTypeRow(FieldPlanFacility current, FieldPlanFacility candidate) {
        boolean currentHasValue = additionalFieldValue(current, "systemType") != null;
        boolean candidateHasValue = additionalFieldValue(candidate, "systemType") != null;
        if (currentHasValue != candidateHasValue) {
            return currentHasValue ? current : candidate;
        }
        return SYSTEM_TYPE_ROW_RECENCY.compare(candidate, current) > 0 ? candidate : current;
    }

    private static final Comparator<FieldPlanFacility> SYSTEM_TYPE_ROW_RECENCY = Comparator
            .comparing(FieldPlannerFacilityService::lastModifiedTimeOf, Comparator.nullsFirst(Long::compareTo))
            .thenComparing(FieldPlannerFacilityService::createdTimeOf, Comparator.nullsFirst(Long::compareTo))
            .thenComparing(FieldPlanFacility::getId, Comparator.nullsFirst(String::compareTo));

    private static Long lastModifiedTimeOf(FieldPlanFacility fieldPlanFacility) {
        return fieldPlanFacility.getAuditDetails() == null
                ? null : fieldPlanFacility.getAuditDetails().getLastModifiedTime();
    }

    private static Long createdTimeOf(FieldPlanFacility fieldPlanFacility) {
        return fieldPlanFacility.getAuditDetails() == null
                ? null : fieldPlanFacility.getAuditDetails().getCreatedTime();
    }

    /**
     * The value of one {@code additionalFields} key, or null when the row does not carry it. Blank
     * counts as absent: a plan saved with an empty system type must read as "nothing recorded" rather
     * than reaching the caller as an empty string.
     */
    private static String additionalFieldValue(FieldPlanFacility fieldPlanFacility, String key) {
        AdditionalFields additionalFields = fieldPlanFacility.getAdditionalFields();
        if (additionalFields == null || additionalFields.getFields() == null) {
            return null;
        }
        for (Field field : additionalFields.getFields()) {
            if (key.equals(field.getKey())) {
                String value = field.getValue();
                return value == null || value.isBlank() ? null : value;
            }
        }
        return null;
    }

    public FieldPlanFacility unassign(FieldPlanFacilityRequest request) {
        log.trace("Entering unassign method for installation plan facility");
        log.info("Received request to unassign installation plan facility");
        FieldPlanFacilityBulkRequest bulkRequest = FieldPlanFacilityBulkRequest.builder().requestInfo(request.getRequestInfo())
                .fieldPlanFacilities(Collections.singletonList(request.getFieldPlanFacility())).build();
        log.debug("Created bulk request for installation plan facility unassign");
        FieldPlanFacility result = unassignBulk(bulkRequest, false).get(0);
        log.info("Installation plan facility unassigned successfully with ID: {}", result.getId());
        log.trace("Exiting unassign method");
        return result;
    }

    public List<FieldPlanFacility> unassignBulk(FieldPlanFacilityBulkRequest request, boolean isBulk) {
        log.trace("Entering unassignBulk method for installation plan facility, isBulk: {}", isBulk);
        log.info("Received request to unassign bulk installation plan facility, count: {}", request.getFieldPlanFacilities().size());

        validateCreateFieldPlanRequest(request);
        log.debug("Installation plan facility unassign request validated");

        List<FieldPlanFacility> fieldPlanFacilities = request.getFieldPlanFacilities();
        try {
            if (!fieldPlanFacilities.isEmpty()) {
                log.debug("Processing {} installation plan facilities for unassign", fieldPlanFacilities.size());
                for (FieldPlanFacility fieldPlanFacility : fieldPlanFacilities){
                    log.info("processing {} valid entities", fieldPlanFacilities.size());
                    fieldPlannerEnrichment.enrichFieldPlanFacilityRequestOnDelete(fieldPlanFacility, request.getRequestInfo());
                }
                log.debug("Installation plan facilities enriched, pushing to Kafka");
                producer.push(fieldPlannerConfiguration.getDeleteFieldPlanFacilityTopic(), fieldPlanFacilities);
                log.info("successfully created project facility");
            }
        } catch (Exception exception) {
            log.error("error occurred while updating installation plan facility: {}", ExceptionUtils.getStackTrace(exception));
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
        log.info("received request to update bulk installation plan facility");
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
                log.info("successfully pushed installation plan facility update");
            }
        } catch (Exception exception) {
            log.error("error occurred while updating installation plan facility: {}", ExceptionUtils.getStackTrace(exception));
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
        log.trace("Entering validateCreateFieldPlanRequest method");
        log.debug("Validating installation plan facility request with {} facilities", request.getFieldPlanFacilities().size());
        Map<String, String> errorMap = new HashMap<>();

        //Verify if facilityId is valid
        validateFacilityIds(request, errorMap);
        log.debug("Facility IDs validation completed, error count: {}", errorMap.size());
        //Verify if FieldPlanId is valid
        validateFieldPlanIds(request, errorMap);
        log.debug("Installation plan IDs validation completed, total error count: {}", errorMap.size());

        if (!errorMap.isEmpty()) {
            log.error("Installation plan facility request validation failed with {} errors", errorMap.size());
            throw new CustomException(errorMap);
        }
        log.debug("Installation plan facility request validation successful");
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
                    log.error("error while fetching facility list", ExceptionUtils.getStackTrace(e));
                    throw new CustomException("FACILITY_ERROR", "error while calling facility service");
                }
            }
        }
        log.debug("Facility IDs validation completed");
        log.trace("Exiting validateFacilityIds method");
    }

    private void validateFieldPlanIds(FieldPlanFacilityBulkRequest request, Map<String, String> errorMap) {
        log.trace("Entering validateFieldPlanIds method");
        log.debug("Validating {} installation plan IDs", request.getFieldPlanFacilities().size());
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
                log.error("Error while validating installation plan IDs", e);
                throw new CustomException("FIELDPLAN_ERROR", "error while calling installation plan");
            }
        }
        log.debug("Installation plan IDs validation completed");
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
