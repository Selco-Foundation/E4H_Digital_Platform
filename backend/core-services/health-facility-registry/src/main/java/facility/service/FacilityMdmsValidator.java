package facility.service;

import facility.util.MdmsUtil;
import facility.web.models.Facility;
import facility.web.models.FacilityAddress;
import facility.web.models.HealthFacilityDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class FacilityMdmsValidator {

    private static final String MDMS_SOURCE = "mdmsSource";

    /** MDMS code for optional column that becomes mandatory when category is ANGANWADI (ingestion-aligned). */
    private static final String MDMS_CODE_FACILITY_POC_USERNAME = "facility_poc_username";

    private static final String ERR_POC_USERNAME_REQUIRED_WHEN_ANGANWADI =
            "PoC Username is required when Facility Category is ANGANWADI.";

    /** Same semantics as API / ingestion when MDMS row constraint message is absent. */
    private static final String ERR_HFR_OR_NIN_REQUIRED_WHEN_HEALTH =
            "When Facility Category is HEALTH, at least one of HFR ID or NIN ID is required.";

    private final MdmsUtil mdmsUtil;

    /**
     * Validates a list of facilities against MDMS master data.
     * It checks both schema-based column validations and row-level constraints.
     *
     * @param facilities List of Facility objects to validate
     * @param tenantId   Tenant ID to scope the MDMS lookup
     * @param requestInfo Request metadata
     */
    public void validateAgainstMDMS(List<Facility> facilities, String tenantId, RequestInfo requestInfo) {
        log.trace("Entering validateAgainstMDMS method");
        Objects.requireNonNull(facilities, "Facility list cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(requestInfo, "RequestInfo cannot be null");

        if (facilities.isEmpty()) {
            log.debug("Facility list is empty, skipping validation");
            return;
        }

        log.info("Starting MDMS validation for {} facilities in tenant {}", facilities.size(), tenantId);

        // Fetch MDMS data from relevant modules
        log.debug("Fetching MDMS data for data-ingestion module");
        Map<String, Map<String, JSONArray>> mdmsData = new HashMap<>();
        mdmsData.putAll(mdmsUtil.fetchMdmsData(requestInfo, tenantId, "data-ingestion", List.of("FacilityIngestionSchema")));
        log.debug("Fetching MDMS data for facility module");
        mdmsData.putAll(mdmsUtil.fetchMdmsData(requestInfo, tenantId, "facility", List.of("FacilityType", "FacilityCategory", "FacilityOwnership", "SolarSolutionDesignType")));

        // Extract ingestion schema definition
        JSONArray ingestionSchemas = mdmsData.getOrDefault("data-ingestion", Map.of()).get("FacilityIngestionSchema");
        if (ingestionSchemas == null || ingestionSchemas.isEmpty()) {
            log.error("FacilityIngestionSchema not found in MDMS response for tenant {}", tenantId);
            throw new IllegalArgumentException("FacilityIngestionSchema not found in MDMS response");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) ingestionSchemas.get(0);

        List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
        List<Map<String, Object>> rowConstraints = (List<Map<String, Object>>) schema.get("rowConstraints");
        log.debug("Extracted {} columns and {} row constraints from schema",
                columns != null ? columns.size() : 0,
                rowConstraints != null ? rowConstraints.size() : 0);

        // Flatten MDMS data to enable quick lookups during validation
        List<Map<String, Object>> flattenedMdmsData = flattenMdmsData(mdmsData);
        log.debug("Flattened MDMS data into {} records", flattenedMdmsData.size());

        int validatedCount = 0;
        for (Facility facility : facilities) {
            log.trace("Validating facility: {}", facility.getFacilityId());
            Map<String, Object> input = convertFacilityToMap(facility);

            // Validate individual fields (required, pattern, allowed values)
            validateFields(columns, input, flattenedMdmsData);

            // Validate cross-field constraints (e.g. atLeastOne, allOrNone)
            validateRowConstraints(rowConstraints, input);
            validatedCount++;
        }

        log.info("Completed MDMS validation for {} facilities in tenant {}", validatedCount, tenantId);
        log.trace("Exiting validateAgainstMDMS method");
    }

    /**
     * Validates each column's value against required, pattern, and allowed MDMS values.
     */
    private void validateFields(List<Map<String, Object>> columns, Map<String, Object> input, List<Map<String, Object>> mdmsList) {
        log.trace("Entering validateFields method");
        for (Map<String, Object> col : columns) {
            String name = (String) col.get("name");
            String key = deriveKeyFromColumn(col, name);
            log.trace("Validating field: {} with key: {}", name, key);

            Object value = input.getOrDefault(key, input.get(name));

            if (Boolean.TRUE.equals(col.get("required")) && (value == null || value.toString().isBlank())) {
                log.error("Validation failed: Missing required field: {}", name);
                throw new IllegalArgumentException("Missing required field: " + name);
            }

            validateAnganwadiRequiresPocUsername(col, input, value);

            if (value != null && col.containsKey("pattern")) {
                String pattern = (String) col.get("pattern");
                if (!value.toString().matches(pattern)) {
                    log.warn("Validation failed: Invalid format for field {} with value (pattern mismatch)", name);
                    throw new IllegalArgumentException("Invalid format for " + name + ": " + value);
                }
                log.debug("Pattern validation passed for field: {}", name);
            }

            // Check if value is allowed per MDMS source
            validateColumns(mdmsList, col, value, name, input);
        }
        log.trace("Exiting validateFields method");
    }

    /**
     * MDMS marks {@code facility_poc_username} as optional; it is required when facility category is ANGANWADI.
     */
    private void validateAnganwadiRequiresPocUsername(Map<String, Object> col, Map<String, Object> input, Object value) {
        if (!MDMS_CODE_FACILITY_POC_USERNAME.equals(col.get("code"))) {
            return;
        }
        if (!"ANGANWADI".equals(normalizeFacilityCategoryForValidation(input))) {
            return;
        }
        if (value == null || value.toString().isBlank()) {
            log.error("Validation failed: {}", ERR_POC_USERNAME_REQUIRED_WHEN_ANGANWADI);
            throw new IllegalArgumentException(ERR_POC_USERNAME_REQUIRED_WHEN_ANGANWADI);
        }
    }

    /**
     * Validates row-level constraints like "atLeastOneRequired" or "allOrNoneRequired"
     */
    private void validateRowConstraints(List<Map<String, Object>> constraints, Map<String, Object> input) {
        log.trace("Entering validateRowConstraints method");
        if (constraints == null) {
            log.debug("No row constraints to validate");
            return;
        }

        log.debug("Validating {} row constraints", constraints.size());
        for (Map<String, Object> constraint : constraints) {
            List<String> fields = (List<String>) constraint.get("fields");
            if (fields == null) {
                log.debug("Skipping row constraint with null fields");
                continue;
            }

            long present = fields.stream()
                    .filter(f -> input.get(f) != null && !input.get(f).toString().isBlank())
                    .count();

            String type = (String) constraint.get("type");
            String message = (String) constraint.get("message");
            log.trace("Validating constraint type: {} with {} fields, {} present", type, fields.size(), present);

            // MDMS atLeastOneRequired on HFR ID + NIN ID applies only when category is HEALTH (ANGANWADI: optional).
            if ("atLeastOneRequired".equals(type) && isHfrNinAtLeastOneConstraint(fields)) {
                if (!"HEALTH".equals(normalizeFacilityCategoryForValidation(input))) {
                    continue;
                }
                if (present < 1) {
                    String err = (message != null && !message.isBlank()) ? message : ERR_HFR_OR_NIN_REQUIRED_WHEN_HEALTH;
                    log.error("Validation failed: atLeastOneRequired (HFR/NIN, HEALTH only) - {}", err);
                    throw new IllegalArgumentException(err);
                }
                continue;
            }

            switch (type) {
                case "atLeastOneRequired":
                    if (present < 1) {
                        log.error("Validation failed: atLeastOneRequired constraint violated - {}", message);
                        throw new IllegalArgumentException(message);
                    }
                    break;
                case "allOrNoneRequired":
                    if (present > 0 && present < fields.size()) {
                        log.error("Validation failed: allOrNoneRequired constraint violated - {}", message);
                        throw new IllegalArgumentException(message);
                    }
                    break;
                default:
                    log.error("Unsupported constraint type: {}", type);
                    throw new IllegalArgumentException("Unsupported constraint type: " + type);
            }
        }
        log.trace("Exiting validateRowConstraints method");
    }

    private static boolean isHfrNinAtLeastOneConstraint(List<String> fields) {
        if (fields == null || fields.size() != 2) {
            return false;
        }
        Set<String> normalized = fields.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());
        return normalized.contains("HFR ID") && normalized.contains("NIN ID");
    }

    /**
     * Validates values against the list of allowed MDMS values.
     * For facility type ({@code facility_type} / {@code facility.FacilityType}), when facility category is
     * {@code HEALTH} or {@code ANGANWADI}, only MDMS rows whose {@code facilityCategory} matches are considered.
     */
    private void validateColumns(List<Map<String, Object>> mdmsList, Map<String, Object> col, Object value, String name,
                                 Map<String, Object> input) {
        log.trace("Entering validateColumns method for field: {}", name);
        if (value != null && col.containsKey(MDMS_SOURCE)) {
            Map<String, String> src = (Map<String, String>) col.get(MDMS_SOURCE);
            String schemaCode = src.get("module") + "." + src.get("master");
            String field = src.get("path") != null ? src.get("path").replace("$.", "") : null;
            log.debug("Validating field {} against MDMS schema: {} with field path: {}", name, schemaCode, field);

            if (field == null) {
                log.debug("No field path specified for MDMS validation, skipping");
                return;
            }

            Stream<Map<String, Object>> dataStream = mdmsList.stream()
                    .filter(m -> schemaCode.equals(m.get("schemaCode")))
                    .map(m -> (Map<String, Object>) m.get("data"));

            String categoryForType = "";
            if (isFacilityTypeMdmsColumn(col, schemaCode)) {
                categoryForType = normalizeFacilityCategoryForValidation(input);
                if ("HEALTH".equals(categoryForType) || "ANGANWADI".equals(categoryForType)) {
                    final String expectedCategory = categoryForType;
                    dataStream = dataStream.filter(d -> expectedCategory.equals(mdmsFacilityCategoryUpper(d)));
                }
            }

            Set<String> valid = dataStream
                    .map(d -> (String) d.get(field))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            log.debug("Found {} valid values for field {} from MDMS", valid.size(), name);
            if (!valid.contains(value.toString())) {
                log.error("Validation failed: Invalid value for field {} - value not found in allowed MDMS values", name);
                if (isFacilityTypeMdmsColumn(col, schemaCode)
                        && ("HEALTH".equals(categoryForType) || "ANGANWADI".equals(categoryForType))) {
                    throw new IllegalArgumentException(
                            name + " must be a facility type for Facility Category '" + categoryForType
                                    + "' (MDMS facilityCategory); '" + value + "' is not valid for this category. Allowed: "
                                    + valid);
                }
                throw new IllegalArgumentException("Invalid value for " + name + ": " + value + " — allowed: " + valid);
            }
            log.trace("MDMS validation passed for field: {}", name);
        }
        log.trace("Exiting validateColumns method");
    }

    private static boolean isFacilityTypeMdmsColumn(Map<String, Object> col, String schemaCode) {
        Object code = col.get("code");
        return "facility_type".equals(code) || "facility.FacilityType".equals(schemaCode);
    }

    /**
     * Upper-case category from payload (same keys as ingestion template / {@link #convertFacilityToMap}).
     */
    private static String normalizeFacilityCategoryForValidation(Map<String, Object> input) {
        for (String key : List.of("Category of Facility", "Facility Category")) {
            Object v = input.get(key);
            if (v == null) {
                continue;
            }
            String s = v.toString().trim();
            if (!s.isEmpty()) {
                // Values are typically MDMS codes (e.g. ANGANWADI, HEALTH).
                return s.toUpperCase(Locale.ROOT);
            }
        }
        return "";
    }

    private static String mdmsFacilityCategoryUpper(Map<String, Object> data) {
        Object fc = data.get("facilityCategory");
        if (fc == null) {
            return "";
        }
        return fc.toString().trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Determines the key name to use from column definition,
     * using svcSource, mdmsSource, or falling back to column name.
     */
    private String deriveKeyFromColumn(Map<String, Object> col, String defaultKey) {
        log.trace("Entering deriveKeyFromColumn method");
        String result;
        if (col.containsKey("svcSource")) {
            result = ((Map<String, String>) col.get("svcSource")).get("key");
        } else if (col.containsKey(MDMS_SOURCE)) {
            result = ((Map<String, String>) col.get(MDMS_SOURCE)).get("path").replace("$.", "");
        } else {
            result = defaultKey;
        }
        log.trace("Exiting deriveKeyFromColumn method, derived key: {}", result);
        return result;
    }

    /**
     * Flattens MDMS module/master records into a schemaCode + data structure.
     * This allows simple lookup during value validation.
     */
    private List<Map<String, Object>> flattenMdmsData(Map<String, Map<String, JSONArray>> mdmsData) {
        log.trace("Entering flattenMdmsData method");
        List<Map<String, Object>> flat = new ArrayList<>();

        for (Map.Entry<String, Map<String, JSONArray>> module : mdmsData.entrySet()) {
            String moduleName = module.getKey();
            log.debug("Flattening MDMS module: {}", moduleName);
            for (Map.Entry<String, JSONArray> master : module.getValue().entrySet()) {
                JSONArray records = master.getValue();
                log.debug("Processing {} records from master: {}", records.size(), master.getKey());
                for (Object obj : records) {
                    if (obj instanceof Map) {
                        Map<String, Object> schemaCodeMap = new HashMap<>();
                        schemaCodeMap.put("schemaCode", moduleName + "." + master.getKey());
                        schemaCodeMap.put("data", obj);
                        flat.add(schemaCodeMap);
                    }
                }
            }
        }

        log.debug("Flattened MDMS data into {} records", flat.size());
        log.trace("Exiting flattenMdmsData method");
        return flat;
    }

    /**
     * Converts a Facility object into a flat map suitable for validation.
     * Includes data from nested FacilityAddress and HealthFacilityDetails.
     */
    private Map<String, Object> convertFacilityToMap(Facility facility) {
        log.trace("Entering convertFacilityToMap method for facility: {}", facility.getFacilityId());
        Map<String, Object> map = new HashMap<>();

        FacilityAddress addr = facility.getAddress();
        if (addr != null) {
            map.put("Latitude", addr.getLatitude());
            map.put("Longitude", addr.getLongitude());
            map.put("Address", buildFullAddress(addr));
            map.put("City", addr.getCity());
            map.put("Pincode", addr.getPincode());
            map.put("State", addr.getState());
            map.put("District", addr.getDistrict());
            map.put("Block", addr.getBlock());
            log.debug("Converted address data for facility: {}", facility.getFacilityId());
        }

        map.put("Health Centre Name", facility.getFacilityName());
        map.put("Type of HC", facility.getFacilityType());
        map.put("Category of Facility", facility.getFacilityCategory());
        map.put("facility_id", facility.getFacilityId());
        map.put("tenant_id", facility.getTenantId());
        map.put("boundaryCode", facility.getBoundaryCode());
        map.put("HFR ID", facility.getHfrId());
        map.put("NIN ID", facility.getNinId());
        map.put("HC PoC Name", facility.getFacilityPocName());
        map.put("HC PoC Contact number", facility.getFacilityPocPhone());
        map.put("PoC Username", facility.getFacilityPocUsername());

        HealthFacilityDetails details = facility.getFacilityDetails();
        if (details != null) {
            map.put("Solution Design Type", details.getSolarSolutionDesignType());
            map.put("HC PoC Designation", details.getPocDesignation());
            // Don't log POC contact in debug as it's PI data
            log.debug("Converted facility details data for facility: {}", facility.getFacilityId());
        }

        log.debug("Converted facility to map with {} fields", map.size());
        log.trace("Exiting convertFacilityToMap method");
        return map;
    }

    /**
     * Combines all address fields into a single human-readable address string.
     */
    private String buildFullAddress(FacilityAddress addr) {
        log.trace("Entering buildFullAddress method");
        String result = Stream.of(
                        addr.getAddressNumber(),
                        addr.getAddressLine1(),
                        addr.getAddressLine2(),
                        addr.getLandmark(),
                        addr.getCity(),
                        addr.getPincode()
                ).filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
        log.trace("Exiting buildFullAddress method");
        return result;
    }
}
