package com.example.hfr.service;


import com.example.hfr.repository.FacilityRepository;
import com.example.hfr.web.models.Facility;
import com.example.hfr.web.models.FacilityAddress;
import com.example.hfr.web.models.FacilityCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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

        // 1. MDMS validation
        validateAgainstMDMS(facility, tenantId);

        // 2. Boundary validation
        validateBoundary(facility.getAddress(), tenantId);

        // 3. Generate facility ID if not set
        if (facility.getFacilityId() == null) {
            UUID generatedId = generateFacilityId(tenantId);
            facility.setFacilityId(generatedId);
        }

        // 4. Enrich data
        if (facility.getWfStatus() == null) {
            facility.setWfStatus("CREATED");
        }
        if (facility.getIsActive() == null) {
            facility.setIsActive(true);
        }

        // 5. Push to Kafka via Persister
        facilityRepository.pushCreateFacility(request);

        return facility;
    }

    private void validateAgainstMDMS(Facility facility, String tenantId) {
        String url = String.format("%s:%s/egov-mdms-service/v1/_search", mdmsHost, mdmsPort);

        Map<String, Object> mdmsRequest = new HashMap<>();
        Map<String, Object> requestInfo = new HashMap<>(); // Replace with actual RequestInfo if available
        mdmsRequest.put("RequestInfo", requestInfo);

        Map<String, Object> moduleDetail = new HashMap<>();
        moduleDetail.put("moduleName", "common-masters");

        List<Map<String, String>> masterDetails = new ArrayList<>();
        masterDetails.add(Map.of("name", "FacilityType"));
        masterDetails.add(Map.of("name", "FacilityCategory"));
        masterDetails.add(Map.of("name", "FacilityOwnership"));

        moduleDetail.put("masterDetails", masterDetails);

        Map<String, Object> mdmsCriteria = new HashMap<>();
        mdmsCriteria.put("tenantId", tenantId);
        mdmsCriteria.put("moduleDetails", List.of(moduleDetail));

        mdmsRequest.put("MdmsCriteria", mdmsCriteria);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, mdmsRequest, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to fetch MDMS data for validation");
        }

        Map<String, Object> mdmsRes = response.getBody();
        Map<String, Object> mdms = (Map<String, Object>) mdmsRes.get("MdmsRes");
        Map<String, Object> commonMasters = (Map<String, Object>) mdms.get("common-masters");

        Set<String> validFacilityTypes = extractCodeSet(commonMasters, "FacilityType");
        Set<String> validFacilityCategories = extractCodeSet(commonMasters, "FacilityCategory");
        Set<String> validFacilityOwnerships = extractCodeSet(commonMasters, "FacilityOwnership");

        if (!validFacilityTypes.contains(facility.getFacilityType())) {
            throw new IllegalArgumentException("Invalid Facility Type: " + facility.getFacilityType());
        }

        if (!validFacilityCategories.contains(facility.getFacilityCategory())) {
            throw new IllegalArgumentException("Invalid Facility Category: " + facility.getFacilityCategory());
        }

        if (!validFacilityOwnerships.contains(facility.getFacilityOwnership())) {
            throw new IllegalArgumentException("Invalid Facility Ownership: " + facility.getFacilityOwnership());
        }
    }

    private Set<String> extractCodeSet(Map<String, Object> module, String masterName) {
        List<Map<String, Object>> masterData = (List<Map<String, Object>>) module.get(masterName);
        Set<String> codes = new HashSet<>();
        for (Map<String, Object> item : masterData) {
            codes.add((String) item.get("code"));
        }
        return codes;
    }


    private void validateBoundary(FacilityAddress address, String tenantId) {
        if (address == null || address.getCity() == null) return;

        String url = boundaryHost + boundaryPath + "?tenantId=" + tenantId + "&hierarchyTypeCode=ADMIN";

        ResponseEntity<Object> response = restTemplate.postForEntity(url, new HashMap<>(), Object.class);
        // TODO: Extract list of valid boundaries and check if address.getCity() is valid
    }

    private UUID generateFacilityId(String tenantId) {
        Map<String, Object> idGenRequest = Map.of(
                "RequestInfo", new HashMap<>(),  // Add actual RequestInfo if needed
                "idRequests", List.of(Map.of(
                        "tenantId", tenantId,
                        "idName", "facility.id",
                        "format", "[tenantId]/FAC/[SEQ_FAC]",
                        "count", 1
                ))
        );

        String url = idgenHost + idgenPath;

        ResponseEntity<Map> response = restTemplate.postForEntity(url, idGenRequest, Map.class);
        List<Map<String, String>> ids = (List<Map<String, String>>) ((Map) response.getBody().get("idResponses")).get("id");
        return UUID.fromString(ids.get(0).get("id")); // Adjust based on actual structure
    }
}

