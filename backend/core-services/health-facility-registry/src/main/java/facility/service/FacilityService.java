package facility.service;


import facility.exception.ServiceCallException;
import facility.repository.FacilityRepository;
import facility.web.models.*;
import facility.web.models.Idgen.IdGenerationRequest;
import facility.web.models.Idgen.IdGenerationResponse;
import facility.web.models.Idgen.IdRequest;
import facility.web.models.Idgen.IdResponse;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FacilityService {

    public static final String MDMS_SOURCE = "mdmsSource";
    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FacilityRowMapper facilityRowMapper;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.port}")
    private String mdmsPort;

    @Value("${egov.idgen.host}")
    private String idgenHost;

    @Value("${egov.idgen.path:/egov-idgen/id/_generate}")
    private String idgenPath;

    @Value("${egov.boundary.host}")
    private String boundaryHost;

    @Value("${egov.boundary.path:/boundary-service/boundary/_search}")
    private String boundaryPath;

    public Facility createFacility(FacilityCreateRequest request) {
        Facility facility = request.getFacility();
        String tenantId = facility.getTenantId();

        validateAgainstMDMS(facility, tenantId);
        validateBoundary(facility.getFacilityDetails().get("boundaryCode"), tenantId);

        // 3. Generate facility ID if not set
        if (facility.getFacilityId() == null) {
            facility.setFacilityId(generateFacilityId(tenantId));
        }

        if (facility.getWfStatus() == null) facility.setWfStatus("CREATED");
        if (facility.getIsActive() == null) facility.setIsActive(true);

        // 5. Push to Kafka via Persister
        facilityRepository.pushCreateFacility(request);

        return facility;
    }

    private void validateBoundary(Object boundaryCode, String tenantId) {
        if (boundaryCode == null || boundaryCode.toString().isBlank()) {
            throw new IllegalArgumentException("boundaryCode is required for facility");
        }

        String code = boundaryCode.toString();
        String url = String.format("%s%s?tenantId=%s&codes=%s", boundaryHost, boundaryPath, tenantId, code);

        Map<String, Object> requestInfo = Map.of(); // Add authToken if needed
        Map<String, Object> requestBody = Map.of("RequestInfo", requestInfo);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("Boundary service call failed with status: " + response.getStatusCode());
            }

            Map<String, Object> body = response.getBody();
            List<?> boundaries = (List<?>) body.get("Boundary");

            if (boundaries == null || boundaries.isEmpty()) {
                throw new IllegalArgumentException("Invalid boundaryCode: " + code);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Error validating boundaryCode: " + code, e);
        }
    }


    public Facility updateFacility(FacilityUpdateRequest request) {
        FacilityUpdateRequestFacilityUpdate update = request.getFacilityUpdate();

        if (update.getFacilityId() == null || update.getTenantId() == null) {
            throw new IllegalArgumentException("facilityId and tenantId must be provided for update");
        }

        String checkSql = "SELECT COUNT(*) FROM facility WHERE facility_id = ? AND tenant_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, update.getFacilityId(), update.getTenantId());
        if (count == null || count == 0) {
            return null;
        }

        // Convert update DTO to core Facility model
        Facility facility = new Facility();
        facility.setFacilityId(update.getFacilityId());
        facility.setTenantId(update.getTenantId());
        facility.setFacilityType(update.getFacilityType());
        facility.setFacilitySubtype(update.getFacilitySubtype());
        facility.setFacilityName(update.getFacilityName());
        facility.setAddress(update.getAddress());
        facility.setAdditionalDetails(update.getAdditionalDetails());

        validateAgainstMDMS(facility, update.getTenantId());

        if (facility.getWfStatus() == null) facility.setWfStatus("UPDATED");
        if (facility.getIsActive() == null) facility.setIsActive(true);

        FacilityUpdateRequest kafkaRequest = new FacilityUpdateRequest();
        kafkaRequest.setRequestInfo(request.getRequestInfo());
        kafkaRequest.setFacilityUpdate(update);
        facilityRepository.pushUpdateFacility(kafkaRequest);

        return facility;
    }

    public List<Facility> searchFacilities(String tenantId, String facilityId, String facilityName, String hfrId, String ninId, int limit, int offset) {
        StringBuilder query = new StringBuilder("SELECT * FROM facility WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (tenantId != null && !tenantId.isBlank()) {
            query.append(" AND tenant_id = ?");
            params.add(tenantId);
        }

        if (facilityId != null && !facilityId.isBlank()) {
            query.append(" AND facility_id::text = ?");
            params.add(facilityId);
        }

        if (facilityName != null && !facilityName.isBlank()) {
            query.append(" AND facility_name ILIKE ?");
            params.add("%" + facilityName + "%");
        }

        if (hfrId != null && !hfrId.isBlank()) {
            query.append(" AND facility_details->>'hfrId' = ?");
            params.add(hfrId);
        }

        if (ninId != null && !ninId.isBlank()) {
            query.append(" AND facility_details->>'ninId' = ?");
            params.add(ninId);
        }

        query.append(" ORDER BY created_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(query.toString(), params.toArray(), facilityRowMapper.rowMapper);
    }


    public FacilitySummary getFacilitySummary(String facilityId) {
        String sql = "SELECT facility_name, facility_type FROM facility WHERE facility_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{facilityId}, (rs, rowNum) -> {
                String name = rs.getString("facility_name");
                String type = rs.getString("facility_type");

                String summaryText = "Facility '" + name + "' is of type '" + type + "'.";

                FacilitySummary summary = new FacilitySummary();
                summary.setSummary(summaryText);
                return summary;
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }

    }


    private void validateAgainstMDMS(Facility facility, String tenantId) {
        List<Map<String, Object>> mdmsList = fetchMDMSData(tenantId);
        Map<String, Object> schema = extractFacilitySchema(mdmsList);

        List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
        Map<String, Object> input = buildInputMapWithOverrides(facility, columns);

        validateFields(columns, input, mdmsList);
        validateRowConstraints((List<Map<String, Object>>) schema.get("rowConstraints"), input);
    }

    private List<Map<String, Object>> fetchMDMSData(String tenantId) {
        String url = String.format("%s:%s/egov-mdms-service/v2/_search", mdmsHost, mdmsPort);
        Map<String, Object> requestInfo = Map.of("authToken", "");
        Map<String, Object> mdmsRequest = Map.of(
                "RequestInfo", requestInfo,
                "MdmsCriteria", Map.of(
                        "tenantId", tenantId,
                        "moduleDetails", List.of(
                                Map.of("moduleName", "data-ingestion", "masterDetails", List.of(Map.of("name", "FacilityIngestionSchema"))),
                                Map.of("moduleName", "facility", "masterDetails", List.of(
                                        Map.of("name", "FacilityType"),
                                        Map.of("name", "FacilityCategory"),
                                        Map.of("name", "FacilityOwnership")
                                ))
                        )
                )
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(url, mdmsRequest, Map.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            Map<String, String> errors = new HashMap<>();
            errors.put("MDMS_ERROR", "Failed to fetch MDMS data. Status: " + response.getStatusCode());
            throw new ServiceCallException(errors);
        }

        return (List<Map<String, Object>>) response.getBody().get("mdms");
    }

    private Map<String, Object> extractFacilitySchema(List<Map<String, Object>> mdmsList) {
        return mdmsList.stream()
                .filter(m -> "data-ingestion.FacilityIngestionSchema".equals(m.get("schemaCode")))
                .findFirst()
                .map(m -> (Map<String, Object>) m.get("data"))
                .orElseThrow(() -> new RuntimeException("FacilityIngestionSchema not found"));
    }

    private Map<String, Object> buildInputMapWithOverrides(Facility facility, List<Map<String, Object>> columns) {
        Map<String, Object> input = convertFacilityToMap(facility);

        Map<String, Function<Facility, Object>> staticMappers = Map.of(
                "Type of HC", Facility::getFacilityType,
                "Health Centre Name", Facility::getFacilityName,
                "facility_id", Facility::getFacilityId,
                "tenant_id", Facility::getTenantId
        );

        for (Map.Entry<String, Function<Facility, Object>> entry : staticMappers.entrySet()) {
            Object value = entry.getValue().apply(facility);
            if (value != null) {
                input.put(entry.getKey(), value);
            }
        }

        // ✅ Do NOT remove any columns — all should be validated
        return input;
    }

    private void validateFields(List<Map<String, Object>> columns, Map<String, Object> input, List<Map<String, Object>> mdmsList) {
        for (Map<String, Object> col : columns) {
            String name = (String) col.get("name");
            String key = deriveKeyFromColumn(col, name);

            Object value = input.getOrDefault(key, input.get(name));;

            if (Boolean.TRUE.equals(col.get("required")) && (value == null || value.toString().isBlank())) {
                throw new IllegalArgumentException("Missing required field: " + name);
            }

            if (value != null && col.containsKey("pattern")) {
                String pattern = (String) col.get("pattern");
                if (!value.toString().matches(pattern)) {
                    throw new IllegalArgumentException("Invalid format for " + name + ": " + value);
                }
            }

            validateColumns(mdmsList, col, value, name);
        }
    }

    private static void validateColumns(List<Map<String, Object>> mdmsList, Map<String, Object> col, Object value, String name) {
        if (value != null && col.containsKey(MDMS_SOURCE)) {
            Map<String, String> src = (Map<String, String>) col.get(MDMS_SOURCE);
            String schemaCode = src.get("module") + "." + src.get("master");
            String field = src.get("path") != null ? src.get("path").replace("$.", "") : null;

            if (field == null) {
                System.out.println("⚠️ Skipping MDMS validation for " + name + ": missing path");
                return;
            }

            Set<String> valid = mdmsList.stream()
                    .filter(m -> schemaCode.equals(m.get("schemaCode")))
                    .map(m -> (Map<String, Object>) m.get("data"))
                    .map(d -> (String) d.get(field))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            System.out.println("🔎 Valid values for " + name + " from " + schemaCode + ": " + valid);

            if (!valid.contains(value.toString())) {
                throw new IllegalArgumentException("❌ Invalid value for " + name + ": " + value + " — allowed: " + valid);
            }
        }
    }


    private String deriveKeyFromColumn(Map<String, Object> col, String defaultKey) {
        if (col.containsKey("svcSource")) {
            return ((Map<String, String>) col.get("svcSource")).get("key");
        } else if (col.containsKey(MDMS_SOURCE)) {
            return ((Map<String, String>) col.get(MDMS_SOURCE)).get("path").replace("$.", "");
        }
        return defaultKey;
    }

    private void validateRowConstraints(List<Map<String, Object>> constraints, Map<String, Object> input) {
        for (Map<String, Object> constraint : constraints) {
            List<String> fields = (List<String>) constraint.get("fields");
            long present = fields.stream().filter(f -> input.get(f) != null && !input.get(f).toString().isBlank()).count();

            switch ((String) constraint.get("type")) {
                case "atLeastOneRequired":
                    if (present < 1) throw new IllegalArgumentException((String) constraint.get("message"));
                    break;
                case "allOrNoneRequired":
                    if (present > 0 && present < fields.size()) {
                        throw new IllegalArgumentException((String) constraint.get("message"));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported constraint type: " + constraint.get("type"));
            }
        }
    }


    private Map<String, Object> convertFacilityToMap(Facility f) {
        Map<String, Object> map = new HashMap<>();

        FacilityAddress addr = f.getAddress();
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

        map.put("Health Centre Name", f.getFacilityName());
        map.put("Type of HC", f.getFacilityType());

        map.put("boundaryCode", get(f.getFacilityDetails(), "boundaryCode"));

        map.put("HFR ID", get(f.getFacilityDetails(), "hfrId"));
        map.put("NIN ID", get(f.getFacilityDetails(), "ninId"));
        map.put("Vendor Code", get(f.getFacilityDetails(), "vendorCode"));
        map.put("Solution Design Type", get(f.getFacilityDetails(), "solutionDesignType"));
        map.put("HC PoC Name", get(f.getFacilityDetails(), "pocName"));
        map.put("HC PoC Designation", get(f.getFacilityDetails(), "pocDesignation"));
        map.put("HC PoC Contact number", get(f.getFacilityDetails(), "pocContact"));

        return map;
    }

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


    private Object get(Map<String, Object> map, String key) {
        return map != null ? map.get(key) : null;
    }

    private String generateFacilityId(String tenantId) {

        RequestInfo requestInfo = RequestInfo.builder()
                .apiId("org.egov.facility")
                .ver("1.0")
                .ts(System.currentTimeMillis())
                .action("create")
                .did("1")
                .msgId(UUID.randomUUID().toString())
                .authToken("") // Optional or pass a real token if needed
                .build();

        // Build the ID request object
        IdRequest idRequest = IdRequest.builder()
                .idName("facility.id")
                .tenantId(tenantId)
                .format("")
                .build();

        IdGenerationRequest idGenRequest = IdGenerationRequest.builder()
                .requestInfo(requestInfo)
                .idRequests(List.of(idRequest))
                .build();

        // Call the IDGen service
        String url = idgenHost + idgenPath;

        ResponseEntity<IdGenerationResponse> response = restTemplate.postForEntity(
                url, idGenRequest, IdGenerationResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("IDGEN_ERROR", "Failed to fetch IDEN data. Status: " + response.getStatusCode());
            throw new ServiceCallException(errors);
        }

        List<IdResponse> idResponses = response.getBody().getIdResponses();

        if (idResponses == null || idResponses.isEmpty() || idResponses.getFirst().getId() == null) {
            throw new IllegalArgumentException("IDGen returned empty or invalid ID");
        }

        return idResponses.getFirst().getId();
    }

}

