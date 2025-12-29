package facility.service;

import facility.util.MdmsUtil;
import facility.web.models.Facility;
import facility.web.models.FacilityAddress;
import facility.web.models.HealthFacilityDetails;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class FacilityMdmsValidator {

    private static final String MDMS_SOURCE = "mdmsSource";

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
        Objects.requireNonNull(facilities, "Facility list cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(requestInfo, "RequestInfo cannot be null");

        if (facilities.isEmpty()) return;

        // Fetch MDMS data from relevant modules
        Map<String, Map<String, JSONArray>> mdmsData = new HashMap<>();
        mdmsData.putAll(mdmsUtil.fetchMdmsData(requestInfo, tenantId, "data-ingestion", List.of("FacilityIngestionSchema")));
        mdmsData.putAll(mdmsUtil.fetchMdmsData(requestInfo, tenantId, "facility", List.of("FacilityType", "FacilityCategory", "FacilityOwnership", "SolarSolutionDesignType")));

        // Extract ingestion schema definition
        JSONArray ingestionSchemas = mdmsData.getOrDefault("data-ingestion", Map.of()).get("FacilityIngestionSchema");
        if (ingestionSchemas == null || ingestionSchemas.isEmpty()) {
            throw new IllegalArgumentException("FacilityIngestionSchema not found in MDMS response");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) ingestionSchemas.get(0);

        List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
        List<Map<String, Object>> rowConstraints = (List<Map<String, Object>>) schema.get("rowConstraints");

        // Flatten MDMS data to enable quick lookups during validation
        List<Map<String, Object>> flattenedMdmsData = flattenMdmsData(mdmsData);

        for (Facility facility : facilities) {
            Map<String, Object> input = convertFacilityToMap(facility);

            // Validate individual fields (required, pattern, allowed values)
            validateFields(columns, input, flattenedMdmsData);

            // Validate cross-field constraints (e.g. atLeastOne, allOrNone)
            validateRowConstraints(rowConstraints, input);
        }
    }

    /**
     * Validates each column's value against required, pattern, and allowed MDMS values.
     */
    private void validateFields(List<Map<String, Object>> columns, Map<String, Object> input, List<Map<String, Object>> mdmsList) {
        for (Map<String, Object> col : columns) {
            String name = (String) col.get("name");
            String key = deriveKeyFromColumn(col, name);

            Object value = input.getOrDefault(key, input.get(name));

            if (Boolean.TRUE.equals(col.get("required")) && (value == null || value.toString().isBlank())) {
                throw new IllegalArgumentException("Missing required field: " + name);
            }

            if (value != null && col.containsKey("pattern")) {
                String pattern = (String) col.get("pattern");
                if (!value.toString().matches(pattern)) {
                    throw new IllegalArgumentException("Invalid format for " + name + ": " + value);
                }
            }

            // Check if value is allowed per MDMS source
            validateColumns(mdmsList, col, value, name);
        }
    }

    /**
     * Validates row-level constraints like "atLeastOneRequired" or "allOrNoneRequired"
     */
    private void validateRowConstraints(List<Map<String, Object>> constraints, Map<String, Object> input) {
        if (constraints == null) return;

        for (Map<String, Object> constraint : constraints) {
            List<String> fields = (List<String>) constraint.get("fields");
            long present = fields.stream().filter(f -> input.get(f) != null && !input.get(f).toString().isBlank()).count();

            String type = (String) constraint.get("type");
            String message = (String) constraint.get("message");

            switch (type) {
                case "atLeastOneRequired":
                    if (present < 1) throw new IllegalArgumentException(message);
                    break;
                case "allOrNoneRequired":
                    if (present > 0 && present < fields.size()) {
                        throw new IllegalArgumentException(message);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported constraint type: " + type);
            }
        }
    }

    /**
     * Validates values against the list of allowed MDMS values.
     */
    private void validateColumns(List<Map<String, Object>> mdmsList, Map<String, Object> col, Object value, String name) {
        if (value != null && col.containsKey(MDMS_SOURCE)) {
            Map<String, String> src = (Map<String, String>) col.get(MDMS_SOURCE);
            String schemaCode = src.get("module") + "." + src.get("master");
            String field = src.get("path") != null ? src.get("path").replace("$.", "") : null;

            if (field == null) return;

            Set<String> valid = mdmsList.stream()
                    .filter(m -> schemaCode.equals(m.get("schemaCode")))
                    .map(m -> (Map<String, Object>) m.get("data"))
                    .map(d -> (String) d.get(field))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (!valid.contains(value.toString())) {
                throw new IllegalArgumentException("Invalid value for " + name + ": " + value + " — allowed: " + valid);
            }
        }
    }

    /**
     * Determines the key name to use from column definition,
     * using svcSource, mdmsSource, or falling back to column name.
     */
    private String deriveKeyFromColumn(Map<String, Object> col, String defaultKey) {
        if (col.containsKey("svcSource")) {
            return ((Map<String, String>) col.get("svcSource")).get("key");
        } else if (col.containsKey(MDMS_SOURCE)) {
            return ((Map<String, String>) col.get(MDMS_SOURCE)).get("path").replace("$.", "");
        }
        return defaultKey;
    }

    /**
     * Flattens MDMS module/master records into a schemaCode + data structure.
     * This allows simple lookup during value validation.
     */
    private List<Map<String, Object>> flattenMdmsData(Map<String, Map<String, JSONArray>> mdmsData) {
        List<Map<String, Object>> flat = new ArrayList<>();

        for (Map.Entry<String, Map<String, JSONArray>> module : mdmsData.entrySet()) {
            String moduleName = module.getKey();
            for (Map.Entry<String, JSONArray> master : module.getValue().entrySet()) {
                JSONArray records = master.getValue();
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

        return flat;
    }

    /**
     * Converts a Facility object into a flat map suitable for validation.
     * Includes data from nested FacilityAddress and HealthFacilityDetails.
     */
    private Map<String, Object> convertFacilityToMap(Facility facility) {
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
        }

        map.put("Health Centre Name", facility.getFacilityName());
        map.put("Type of HC", facility.getFacilityType());
        map.put("facility_id", facility.getFacilityId());
        map.put("tenant_id", facility.getTenantId());
        map.put("boundaryCode", facility.getBoundaryCode());
        map.put("HFR ID", facility.getHfrId());
        map.put("NIN ID", facility.getNinId());
        map.put("HC PoC Name", facility.getFacilityPocName());
        map.put("HC PoC Contact number", facility.getFacilityPocPhone());

        HealthFacilityDetails details = facility.getFacilityDetails();
        if (details != null) {
            map.put("Solution Design Type", details.getSolarSolutionDesignType());
            map.put("HC PoC Designation", details.getPocDesignation());
        }

        return map;
    }

    /**
     * Combines all address fields into a single human-readable address string.
     */
    private String buildFullAddress(FacilityAddress addr) {
        return Stream.of(
                        addr.getAddressNumber(),
                        addr.getAddressLine1(),
                        addr.getAddressLine2(),
                        addr.getLandmark(),
                        addr.getCity(),
                        addr.getPincode()
                ).filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }
}
