package facility.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.config.Configuration;
import facility.repository.ServiceRequestRepository;
import facility.util.HRMSUtils;
import facility.web.models.Employee;
import facility.web.models.EmployeeRequest;
import facility.web.models.Facility;
import facility.web.models.Jurisdiction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Links newly created facilities to vendor organisations: resolves org by vendor code,
 * picks the first org user, and updates that user's HRMS jurisdiction with the facility boundary.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VendorOrganisationService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final Configuration configs;
    private final HRMSUtils hrmsUtils;
    private final ObjectMapper objectMapper;

    /**
     * When a facility is created with a vendor code, assign its boundary to the first user of that vendor organisation.
     */
    public void assignFacilityJurisdictionToFirstOrgUser(
            String vendorCode, Facility facility, String tenantId, RequestInfo requestInfo) {
        if (facility == null || facility.getBoundaryCode() == null || facility.getBoundaryCode().isBlank()) {
            if (facility != null) {
                log.warn("Facility {} has no boundary code; cannot assign vendor jurisdiction", facility.getFacilityId());
            }
            return;
        }
        assignFacilityJurisdictionsToFirstOrgUser(
                vendorCode, List.of(facility.getBoundaryCode()), tenantId, requestInfo);
    }

    /**
     * Groups facilities by vendor code and performs one HRMS jurisdiction update per vendor
     * (all facility boundaries merged in a single read-modify-write).
     */
    public void assignFacilityJurisdictionsBulk(
            List<Facility> facilities, String tenantId, RequestInfo requestInfo) {
        if (facilities == null || facilities.isEmpty()) {
            return;
        }

        Map<String, Set<String>> boundariesByVendor = new LinkedHashMap<>();
        for (Facility facility : facilities) {
            if (facility == null) {
                continue;
            }
            String vendorCode = extractVendorCode(facility);
            String boundaryCode = facility.getBoundaryCode();
            if (vendorCode == null || vendorCode.isBlank() || boundaryCode == null || boundaryCode.isBlank()) {
                continue;
            }
            boundariesByVendor
                    .computeIfAbsent(vendorCode.trim(), ignored -> new LinkedHashSet<>())
                    .add(boundaryCode.trim());
        }

        for (Map.Entry<String, Set<String>> entry : boundariesByVendor.entrySet()) {
            assignFacilityJurisdictionsToFirstOrgUser(
                    entry.getKey(), new ArrayList<>(entry.getValue()), tenantId, requestInfo);
        }
    }

    /**
     * Merges multiple facility boundaries into the first org user's HRMS jurisdictions with a single update.
     */
    public void assignFacilityJurisdictionsToFirstOrgUser(
            String vendorCode, List<String> boundaryCodes, String tenantId, RequestInfo requestInfo) {
        if (vendorCode == null || vendorCode.isBlank()) {
            return;
        }
        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
            return;
        }
        if (configs.getVendorHost() == null || configs.getVendorHost().isBlank()) {
            log.warn("egov.vendor.host is not configured; skipping vendor jurisdiction assignment for vendor code {}",
                    vendorCode);
            return;
        }

        String normalizedVendorCode = vendorCode.trim();
        List<String> normalizedBoundaries = boundaryCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (normalizedBoundaries.isEmpty()) {
            return;
        }

        log.info("Assigning {} facility boundaries to first user of vendor organisation with code {}",
                normalizedBoundaries.size(), normalizedVendorCode);

        try {
            String organisationId = findOrganisationIdByCode(normalizedVendorCode, tenantId, requestInfo);
            if (organisationId == null) {
                log.warn("No organisation found for vendor code {}", normalizedVendorCode);
                return;
            }

            String orgUserHrmsUuid = findFirstOrgUserHrmsUuid(organisationId, tenantId, requestInfo);
            if (orgUserHrmsUuid == null) {
                log.warn("No users found for organisation {} (vendor code {})", organisationId, normalizedVendorCode);
                return;
            }

            updateEmployeeJurisdictionsWithFacilityBoundaries(
                    orgUserHrmsUuid, normalizedBoundaries, tenantId, requestInfo);
        } catch (Exception e) {
            log.error("Failed to assign {} boundaries to vendor {} (non-blocking): {}",
                    normalizedBoundaries.size(), normalizedVendorCode, e.getMessage(), e);
        }
    }

    private String findOrganisationIdByCode(String vendorCode, String tenantId, RequestInfo requestInfo) {
        String uri = configs.getVendorHost() + configs.getVendorOrganisationSearchPath();

        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("tenantId", tenantId);
        searchCriteria.put("code", vendorCode);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("limit", 1);
        pagination.put("offset", 0);

        Map<String, Object> body = new HashMap<>();
        body.put("RequestInfo", requestInfo);
        body.put("SearchCriteria", searchCriteria);
        body.put("Pagination", pagination);

        Map<String, Object> response = castToMap(serviceRequestRepository.fetchResult(new StringBuilder(uri), body));
        List<Map<String, Object>> organisations = castToListOfMaps(response.get("organisations"));
        if (organisations == null || organisations.isEmpty()) {
            return null;
        }
        Object id = organisations.get(0).get("id");
        return id != null ? id.toString() : null;
    }

    private String findFirstOrgUserHrmsUuid(String organisationId, String tenantId, RequestInfo requestInfo) {
        String uri = UriComponentsBuilder
                .fromUriString(configs.getVendorHost() + configs.getVendorOrganisationUserSearchPath())
                .queryParam("limit", 10)
                .queryParam("offset", 0)
                .queryParam("tenantId", tenantId)
                .toUriString();

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("tenantId", tenantId);
        criteria.put("organizationIds", List.of(organisationId));

        Map<String, Object> body = new HashMap<>();
        body.put("RequestInfo", requestInfo);
        body.put("OrgUser", criteria);

        Map<String, Object> response = castToMap(serviceRequestRepository.fetchResult(new StringBuilder(uri), body));
        List<Map<String, Object>> orgUsers = castToListOfMaps(response.get("OrgUsers"));
        if (orgUsers == null || orgUsers.isEmpty()) {
            return null;
        }
        for (Map<String, Object> orgUser : orgUsers) {
            if (Boolean.TRUE.equals(orgUser.get("isDeleted"))) {
                continue;
            }
            Object userId = orgUser.get("userId");
            if (userId != null && !userId.toString().isBlank()) {
                return userId.toString();
            }
        }
        return null;
    }

    private void updateEmployeeJurisdictionsWithFacilityBoundaries(
            String hrmsUserUuid, List<String> boundaryCodes, String tenantId, RequestInfo requestInfo) {
        Map<String, Object> searchWrapper = Map.of("RequestInfo", requestInfo);
        Employee employee = hrmsUtils.getUserById(searchWrapper, hrmsUserUuid);
        if (employee == null) {
            log.warn("HRMS employee not found for uuid {}", hrmsUserUuid);
            return;
        }

        List<Jurisdiction> merged = employee.getJurisdictions();
        for (String boundaryCode : boundaryCodes) {
            Jurisdiction facilityJurisdiction = hrmsUtils.buildFacilityJurisdiction(boundaryCode, tenantId);
            merged = hrmsUtils.mergeFacilityJurisdiction(merged, facilityJurisdiction);
        }
        employee.setJurisdictions(merged);

        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .requestInfo(requestInfo)
                .employees(List.of(employee))
                .build();
        List<Employee> updated = hrmsUtils.updateHRMSUser(employeeRequest);
        if (updated != null && !updated.isEmpty()) {
            log.info("Updated HRMS jurisdictions for vendor user {} with {} facility boundaries",
                    hrmsUserUuid, boundaryCodes.size());
        } else {
            log.warn("HRMS update returned no employees for vendor user {}", hrmsUserUuid);
        }
    }

    private String extractVendorCode(Facility facility) {
        if (facility.getFacilityDetails() == null) {
            return null;
        }
        String vendorCode = facility.getFacilityDetails().getVendorCode();
        return vendorCode != null ? vendorCode.trim() : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.convertValue(value, Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castToListOfMaps(Object value) {
        if (value == null) {
            return new ArrayList<>();
        }
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.convertValue(value, List.class);
    }
}
