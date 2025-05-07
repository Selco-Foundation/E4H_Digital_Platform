package facility.service;


import facility.repository.FacilityRepository;
import facility.web.models.Facility;
import facility.web.models.FacilityAddress;
import facility.web.models.FacilityCreateRequest;
import facility.web.models.Idgen.IdGenerationRequest;
import facility.web.models.Idgen.IdGenerationResponse;
import facility.web.models.Idgen.IdRequest;
import facility.web.models.Idgen.IdResponse;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FacilityService {

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private RestTemplate restTemplate;

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

    @Value("${egov.boundary.path:/egov-location/boundarys/_search}")
    private String boundaryPath;

    public Facility createFacility(FacilityCreateRequest request) {
        Facility facility = request.getFacility();
        String tenantId = facility.getTenantId();

        //TODO 1. MDMS validation
        validateAgainstMDMS(facility, tenantId);

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

    private void validateAgainstMDMS(Facility facility, String tenantId) {
        String url = String.format("%s:%s/egov-mdms-service/v2/_search", mdmsHost, mdmsPort);
        Map<String, Object> requestInfo = Map.of("authToken", ""); // Set token if required
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
        if (!response.getStatusCode().is2xxSuccessful()) throw new RuntimeException("MDMS call failed");

        List<Map<String, Object>> mdmsList = (List<Map<String, Object>>) response.getBody().get("mdms");
        Map<String, Object> schema = mdmsList.stream()
                .filter(m -> "data-ingestion.FacilityIngestionSchema".equals(m.get("schemaCode")))
                .findFirst()
                .map(m -> (Map<String, Object>) m.get("data"))
                .orElseThrow(() -> new RuntimeException("FacilityIngestionSchema not found"));

        List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
        Map<String, Object> input = convertFacilityToMap(facility);

        for (Map<String, Object> col : columns) {
            String name = (String) col.get("name");

            // Determine the key used in input map (defaults to column name)
            String key = name;
            if (col.containsKey("svcSource")) {
                key = ((Map<String, String>) col.get("svcSource")).get("key");
            } else if (col.containsKey("mdmsSource") && !key.equals("Type of HC")) {
                key = ((Map<String, String>) col.get("mdmsSource")).get("path").replace("$.", "");
            }

            Object value = input.get(key);

            if (Boolean.TRUE.equals(col.get("required")) && (value == null || value.toString().isBlank())) {
                throw new IllegalArgumentException("Missing required field: " + name);
            }

            if (value != null && col.containsKey("pattern")) {
                String pattern = (String) col.get("pattern");
                if (!value.toString().matches(pattern)) {
                    throw new IllegalArgumentException("Invalid format for " + name + ": " + value);
                }
            }

            if (value != null && col.containsKey("mdmsSource")) {
                Map<String, String> src = (Map<String, String>) col.get("mdmsSource");
                String schemaCode = src.get("module") + "." + src.get("master");

                Set<String> valid = mdmsList.stream()
                        .filter(m -> schemaCode.equals(m.get("schemaCode")))
                        .map(m -> (Map<String, Object>) m.get("data"))
                        .map(d -> (String) d.get(src.get("path").replace("$.", "")))
                        .collect(Collectors.toSet());

                if (!valid.contains(value.toString())) {
                    throw new IllegalArgumentException("Invalid value for " + name + ": " + value);
                }
            }
        }


        List<Map<String, Object>> rowConstraints = (List<Map<String, Object>>) schema.get("rowConstraints");
        for (Map<String, Object> c : rowConstraints) {
            List<String> fields = (List<String>) c.get("fields");
            long present = fields.stream().filter(f -> input.get(f) != null && !input.get(f).toString().isBlank()).count();
            switch ((String) c.get("type")) {
                case "atLeastOneRequired":
                    if (present < 1) throw new IllegalArgumentException((String) c.get("message"));
                    break;
                case "allOrNoneRequired":
                    if (present > 0 && present < fields.size())
                        throw new IllegalArgumentException((String) c.get("message"));
                    break;
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
            map.put("State", addr.getDetail());     // Adjust if you later map "State" specifically
            map.put("District", addr.getDetail());  // Adjust if schema defines them separately
            map.put("Block", addr.getDetail());     // Optional fallback
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
            throw new RuntimeException("Failed to generate Facility ID from IDGen service");
        }

        List<IdResponse> idResponses = response.getBody().getIdResponses();

        if (idResponses == null || idResponses.isEmpty() || idResponses.getFirst().getId() == null) {
            throw new IllegalArgumentException("IDGen returned empty or invalid ID");
        }

        return idResponses.getFirst().getId();
    }

}

