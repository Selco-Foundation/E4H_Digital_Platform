package facility.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.config.Configuration;
import facility.repository.ServiceRequestRepository;
import facility.util.FacilityMappedVendorHelper;
import facility.util.HRMSUtils;
import facility.web.models.Employee;
import facility.web.models.EmployeeRequest;
import facility.web.models.Facility;
import facility.web.models.Jurisdiction;
import facility.web.models.User;
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

    /** Key wrapping the RequestInfo in every egov service-call body. */
    private static final String REQUEST_INFO_KEY = "RequestInfo";

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

    /**
     * Stamps the mapped vendor onto facilities that are about to be created, resolved from the
     * {@code vendorCode} on their facility details.
     *
     * <p>Callers must invoke this <em>before</em> the create is pushed: the mapped vendor has no
     * column of its own and rides along inside {@code additional_details}, so a value set after the
     * push never reaches the row.
     *
     * <p>Without this a facility created with a vendor code carries no mapped vendor at all.
     * {@link #assignFacilityJurisdictionsBulk} makes the mapping itself, but it runs after the
     * create and writes in one direction only - onto the vendor's HRMS jurisdictions - and
     * vendor-registry pushes the names back down onto the facility only when that vendor's org user
     * is next edited.
     *
     * <p>Resolved once per distinct vendor code however many facilities share it, and a facility
     * whose create payload already carried a vendor is left as the payload had it. Best-effort
     * throughout: a vendor that cannot be resolved leaves its facilities untouched rather than
     * failing a create that has already been validated.
     */
    public void applyMappedVendorFromVendorCode(List<Facility> facilities, String tenantId, RequestInfo requestInfo) {
        if (facilities == null || facilities.isEmpty()) {
            return;
        }
        if (configs.getVendorHost() == null || configs.getVendorHost().isBlank()) {
            log.warn("egov.vendor.host is not configured; facilities will be created without a mapped vendor");
            return;
        }
        groupByVendorCodeNeedingMappedVendor(facilities)
                .forEach((vendorCode, pending) -> stampMappedVendor(vendorCode, pending, tenantId, requestInfo));
    }

    /** The facilities still awaiting a mapped vendor, keyed by the vendor code to resolve it from. */
    private Map<String, List<Facility>> groupByVendorCodeNeedingMappedVendor(List<Facility> facilities) {
        Map<String, List<Facility>> facilitiesByVendorCode = new LinkedHashMap<>();
        for (Facility facility : facilities) {
            String vendorCode = vendorCodeNeedingMappedVendor(facility);
            if (vendorCode != null) {
                facilitiesByVendorCode
                        .computeIfAbsent(vendorCode, ignored -> new ArrayList<>())
                        .add(facility);
            }
        }
        return facilitiesByVendorCode;
    }

    /**
     * The vendor code to resolve a mapped vendor from, or {@code null} when this facility needs no
     * resolution - it has no vendor code, or its create payload already carried a vendor.
     */
    private String vendorCodeNeedingMappedVendor(Facility facility) {
        if (facility == null) {
            return null;
        }
        FacilityMappedVendorHelper.hydrateFromAdditionalDetails(facility);
        if (FacilityMappedVendorHelper.hasMappedVendor(facility)) {
            return null;
        }
        String vendorCode = extractVendorCode(facility);
        return vendorCode == null || vendorCode.isBlank() ? null : vendorCode;
    }

    /** Resolves one vendor code and stamps the result onto every facility waiting on it. */
    private void stampMappedVendor(String vendorCode, List<Facility> pending, String tenantId,
                                   RequestInfo requestInfo) {
        try {
            User orgUser = findFirstOrgUser(vendorCode, tenantId, requestInfo);
            if (orgUser == null || orgUser.getUserName() == null || orgUser.getUserName().isBlank()) {
                log.warn("No resolvable org user for vendor code {}; leaving {} facilities without a "
                        + "mapped vendor", vendorCode, pending.size());
                return;
            }
            pending.forEach(facility -> FacilityMappedVendorHelper.applyMappedVendor(
                    facility, orgUser.getName(), orgUser.getUserName()));
            log.info("Stamped mapped vendor {} from vendor code {} onto {} facilities being created",
                    orgUser.getUserName(), vendorCode, pending.size());
        } catch (Exception e) {
            log.error("Failed to resolve mapped vendor for vendor code {} (non-blocking): {}",
                    vendorCode, e.getMessage(), e);
        }
    }

    /**
     * The HRMS user behind a vendor code - the same org user {@link
     * #assignFacilityJurisdictionsToFirstOrgUser} hands the facility boundary to, so the name
     * stamped on the facility and the jurisdiction holding the mapping always describe one person.
     */
    private User findFirstOrgUser(String vendorCode, String tenantId, RequestInfo requestInfo) {
        String organisationId = findOrganisationIdByCode(vendorCode, tenantId, requestInfo);
        if (organisationId == null) {
            log.warn("No organisation found for vendor code {}", vendorCode);
            return null;
        }
        String orgUserHrmsUuid = findFirstOrgUserHrmsUuid(organisationId, tenantId, requestInfo);
        if (orgUserHrmsUuid == null) {
            log.warn("No users found for organisation {} (vendor code {})", organisationId, vendorCode);
            return null;
        }
        Employee employee = hrmsUtils.getUserById(Map.of(REQUEST_INFO_KEY, requestInfo), orgUserHrmsUuid);
        return employee != null ? employee.getUser() : null;
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
        body.put(REQUEST_INFO_KEY, requestInfo);
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
        body.put(REQUEST_INFO_KEY, requestInfo);
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
        Map<String, Object> searchWrapper = Map.of(REQUEST_INFO_KEY, requestInfo);
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
