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
import facility.web.models.FacilityVendorMappingResponse.VendorOrganisation;
import facility.web.models.Jurisdiction;
import facility.web.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps facilities to vendor organisations.
 *
 * <p>The Facility Admin picks organisations, never people. An organisation is mapped to a facility
 * by giving the facility's boundary to the organisation's first active {@code COMPLAINT_RESOLVER}
 * in HRMS; the mapping lives only in those HRMS jurisdictions. An organisation with no active
 * resolver can't be mapped, and that is refused before anything is written.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VendorOrganisationService {

    /** Key wrapping the RequestInfo in every egov service-call body. */
    private static final String REQUEST_INFO_KEY = "RequestInfo";
    /** Enough to cover every user of one organisation in a single org-user search. */
    private static final int ORG_USER_PAGE = 100;

    static final String ERR_NOT_FOUND = "VENDOR_ORGANISATION_NOT_FOUND";
    static final String ERR_NO_RESOLVER = "VENDOR_ORGANISATION_HAS_NO_COMPLAINT_RESOLVER";
    static final String ERR_LAST_JURISDICTION = "VENDOR_USER_LAST_JURISDICTION";
    static final String ERR_HRMS_FAILED = "VENDOR_MAPPING_HRMS_FAILED";

    private final ServiceRequestRepository serviceRequestRepository;
    private final Configuration configs;
    private final HRMSUtils hrmsUtils;
    private final ObjectMapper objectMapper;

    /** An organisation and the resolver its facilities are mapped to. */
    public record ResolvedVendor(VendorOrganisation organisation, Employee resolver) {
    }

    /** An organisation currently holding a facility, and every one of its users who holds it. */
    record Holding(VendorOrganisation organisation, List<Employee> holders) {
        boolean hasActiveHolder() {
            return holders.stream().anyMatch(employee -> !Boolean.FALSE.equals(employee.getIsActive()));
        }
    }

    // ---------------------------------------------------------------- resolution

    /**
     * Resolves each organisation code to its first active complaint resolver. Throws, naming every
     * problem at once, if any code names no organisation or one without a resolver. "First" is
     * HRMS's order, which it doesn't guarantee; an accepted known issue.
     */
    public Map<String, ResolvedVendor> resolveVendors(Collection<String> codes, String tenantId, RequestInfo requestInfo) {
        Map<String, ResolvedVendor> resolved = new LinkedHashMap<>();
        List<String> notFound = new ArrayList<>();
        List<String> withoutResolver = new ArrayList<>();
        for (String code : normalize(codes)) {
            VendorOrganisation organisation = findOrganisationByCode(code, tenantId, requestInfo);
            if (organisation == null) {
                notFound.add(code);
                continue;
            }
            List<String> userIds = findOrgUserIds(organisation.id(), tenantId, requestInfo);
            List<Employee> resolvers = hrmsUtils.findActiveComplaintResolvers(requestInfo, userIds);
            if (resolvers.isEmpty()) {
                withoutResolver.add(label(organisation));
                continue;
            }
            resolved.put(code, new ResolvedVendor(organisation, resolvers.get(0)));
        }
        Map<String, String> errors = new LinkedHashMap<>();
        if (!notFound.isEmpty()) {
            errors.put(ERR_NOT_FOUND, "No vendor organisation with code " + String.join(", ", notFound));
        }
        if (!withoutResolver.isEmpty()) {
            errors.put(ERR_NO_RESOLVER, "Vendor organisation " + String.join(", ", withoutResolver)
                    + " has no active Complaint Resolver; add one before mapping it to a facility");
        }
        if (!errors.isEmpty()) {
            throw new CustomException(errors);
        }
        return resolved;
    }

    // ---------------------------------------------------------------- create

    /**
     * Maps newly created facilities to the organisations already resolved for them, and stamps each
     * facility's mapped-vendor label from its resolver.
     *
     * <p>Called after the facility boundaries exist and before the create is pushed, so the label is
     * persisted with the row. The jurisdiction assignment is best-effort: the vendors were validated
     * before anything was written, and a transient HRMS failure here mustn't fail a create whose
     * boundaries already exist. Facilities without a vendor fall back to whatever HRMS already maps.
     */
    public void mapCreatedFacilities(List<Facility> facilities, Map<String, String> vendorCodeByFacilityId,
                                     Map<String, ResolvedVendor> resolved, String tenantId, RequestInfo requestInfo) {
        if (facilities == null || facilities.isEmpty()) {
            return;
        }
        Map<String, List<Facility>> facilitiesByVendor = new LinkedHashMap<>();
        for (Facility facility : facilities) {
            String code = vendorCodeByFacilityId.get(facility.getFacilityId());
            if (code != null && resolved.containsKey(code)) {
                facilitiesByVendor.computeIfAbsent(code, ignored -> new ArrayList<>()).add(facility);
            }
        }
        facilitiesByVendor.forEach((code, mapped) -> {
            Employee resolver = resolved.get(code).resolver();
            List<String> boundaries = mapped.stream().map(Facility::getBoundaryCode)
                    .filter(Objects::nonNull).distinct().toList();
            try {
                if (!addFacilityJurisdictions(resolver.getUuid(), boundaries, tenantId, requestInfo)) {
                    log.warn("HRMS did not confirm mapping {} facilities to vendor {}", boundaries.size(), code);
                }
            } catch (Exception e) {
                log.error("Failed to map {} facilities to vendor {} (non-blocking): {}",
                        boundaries.size(), code, e.getMessage(), e);
            }
            User user = resolver.getUser();
            String userName = firstNonBlank(user != null ? user.getUserName() : null, resolver.getCode());
            mapped.forEach(facility -> FacilityMappedVendorHelper.applyMappedVendor(
                    facility, user != null ? user.getName() : null, userName));
        });
        applyMappedVendorFromHrms(facilities, requestInfo);
    }

    // ---------------------------------------------------------------- read and write

    /** The organisations mapped to a facility: those with an active user holding its boundary. */
    public List<VendorOrganisation> mappedOrganisations(String boundaryCode, String tenantId, RequestInfo requestInfo) {
        return holdings(boundaryCode, tenantId, requestInfo).values().stream()
                .filter(Holding::hasActiveHolder)
                .map(Holding::organisation)
                .sorted(BY_NAME)
                .toList();
    }

    /**
     * Sets the organisations mapped to a facility to exactly {@code desiredCodes}.
     *
     * <ul>
     *   <li>An added organisation's first active resolver is given the facility boundary.</li>
     *   <li>A removed organisation loses it from <em>every</em> one of its users who holds it, active
     *       or not - not just "the first resolver", which could be someone else, leaving the real
     *       holder with access to the facility's tickets.</li>
     * </ul>
     *
     * <p>Every check runs before the first write: unknown organisations, organisations without a
     * resolver, and a removal that would leave a user with no active jurisdiction (HRMS refuses
     * that). Writes nothing on the facility row.
     *
     * @return the organisations now mapped, sorted by name
     */
    public List<VendorOrganisation> setMappedOrganisations(String boundaryCode, Collection<String> desiredCodes,
                                                           String tenantId, RequestInfo requestInfo) {
        Set<String> desired = normalize(desiredCodes);
        Map<String, Holding> current = holdings(boundaryCode, tenantId, requestInfo);

        Set<String> kept = current.values().stream()
                .filter(Holding::hasActiveHolder)
                .map(holding -> holding.organisation().code())
                .filter(desired::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> added = new LinkedHashSet<>(desired);
        added.removeAll(kept);
        Set<String> removed = new LinkedHashSet<>(current.keySet());
        removed.removeAll(desired);

        Map<String, ResolvedVendor> resolvedAdded = resolveVendors(added, tenantId, requestInfo);
        List<Employee> toUnmap = removed.stream()
                .flatMap(code -> current.get(code).holders().stream())
                .toList();
        checkUnmappable(toUnmap, boundaryCode);

        for (Employee holder : toUnmap) {
            unmap(holder, boundaryCode, requestInfo);
        }
        for (ResolvedVendor vendor : resolvedAdded.values()) {
            if (!addFacilityJurisdictions(vendor.resolver().getUuid(), List.of(boundaryCode), tenantId, requestInfo)) {
                throw new CustomException(ERR_HRMS_FAILED, "Could not map vendor organisation "
                        + label(vendor.organisation()) + " to the facility; retry the change");
            }
        }
        log.info("Vendor mapping for boundary {}: kept {}, added {}, removed {}", boundaryCode, kept, added, removed);

        List<VendorOrganisation> now = new ArrayList<>();
        kept.forEach(code -> now.add(current.get(code).organisation()));
        resolvedAdded.values().forEach(vendor -> now.add(vendor.organisation()));
        now.sort(BY_NAME);
        return now;
    }

    /**
     * Every vendor organisation with a user holding an active jurisdiction on the boundary, keyed by
     * organisation code, with those users. Users of any role count: earlier code mapped facilities
     * to an organisation's first user whatever their role, and those mappings must be visible and
     * removable too. Employees that are no organisation's users (the facility PoC) are ignored.
     */
    Map<String, Holding> holdings(String boundaryCode, String tenantId, RequestInfo requestInfo) {
        List<Employee> employees = hrmsUtils.getEmployeesByBoundaryCode(requestInfo, boundaryCode);
        Map<String, Employee> byUuid = new LinkedHashMap<>();
        employees.stream().filter(employee -> employee.getUuid() != null)
                .forEach(employee -> byUuid.putIfAbsent(employee.getUuid(), employee));
        if (byUuid.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, String> organisationIdByUserId = findOrganisationIdsByUserIds(byUuid.keySet(), tenantId, requestInfo);
        Map<String, VendorOrganisation> organisationsById = findOrganisationsByIds(
                new LinkedHashSet<>(organisationIdByUserId.values()), tenantId, requestInfo);

        // In HRMS's order, so the writes that follow happen in a predictable order.
        Map<String, Holding> holdings = new LinkedHashMap<>();
        byUuid.forEach((userId, holder) -> {
            VendorOrganisation organisation = organisationsById.get(organisationIdByUserId.get(userId));
            if (organisation == null) {
                return;
            }
            holdings.computeIfAbsent(organisation.code(), ignored -> new Holding(organisation, new ArrayList<>()))
                    .holders().add(holder);
        });
        return holdings;
    }

    /** Refuses, before any write, a removal that would leave a user with no active jurisdiction. */
    private static void checkUnmappable(List<Employee> holders, String boundaryCode) {
        List<String> stranded = holders.stream()
                .filter(holder -> holder.getJurisdictions() == null || holder.getJurisdictions().stream()
                        .filter(Objects::nonNull)
                        .filter(j -> !Boolean.FALSE.equals(j.getIsActive()))
                        .noneMatch(j -> !boundaryCode.equals(j.getBoundary())))
                .map(Employee::getCode)
                .toList();
        if (!stranded.isEmpty()) {
            throw new CustomException(ERR_LAST_JURISDICTION, "Vendor user " + String.join(", ", stranded)
                    + " holds no other active jurisdiction, and HRMS requires at least one. Give them "
                    + "another jurisdiction before removing this facility.");
        }
    }

    /** Deactivates the user's jurisdiction on the boundary. HRMS never deletes a jurisdiction. */
    private void unmap(Employee holder, String boundaryCode, RequestInfo requestInfo) {
        holder.getJurisdictions().stream()
                .filter(j -> boundaryCode.equals(j.getBoundary()))
                .forEach(j -> j.setIsActive(false));
        // HRMS reads this flag unboxed on every active employee's update; search leaves it null.
        if (holder.getReActivateEmployee() == null) {
            holder.setReActivateEmployee(false);
        }
        List<Employee> updated = hrmsUtils.updateHRMSUser(EmployeeRequest.builder()
                .requestInfo(requestInfo).employees(List.of(holder)).build());
        if (updated == null || updated.isEmpty()) {
            throw new CustomException(ERR_HRMS_FAILED, "Could not remove the facility from vendor user "
                    + holder.getCode() + "; retry the change");
        }
    }

    // ---------------------------------------------------------------- HRMS

    /**
     * Adds the facility boundaries to the employee's jurisdictions, re-activating any it already
     * had, in one HRMS update. True when HRMS confirmed it.
     */
    private boolean addFacilityJurisdictions(String uuid, List<String> boundaryCodes, String tenantId,
                                             RequestInfo requestInfo) {
        if (boundaryCodes.isEmpty()) {
            return true;
        }
        Employee employee = hrmsUtils.getUserById(Map.of(REQUEST_INFO_KEY, requestInfo), uuid);
        List<Jurisdiction> merged = employee.getJurisdictions();
        for (String boundaryCode : boundaryCodes) {
            merged = hrmsUtils.mergeFacilityJurisdiction(merged, hrmsUtils.buildFacilityJurisdiction(boundaryCode, tenantId));
        }
        employee.setJurisdictions(merged);
        if (employee.getReActivateEmployee() == null) {
            employee.setReActivateEmployee(false);
        }
        List<Employee> updated = hrmsUtils.updateHRMSUser(EmployeeRequest.builder()
                .requestInfo(requestInfo).employees(List.of(employee)).build());
        boolean confirmed = updated != null && !updated.isEmpty();
        if (confirmed) {
            log.info("Mapped {} facility boundaries to vendor user {}", boundaryCodes.size(), uuid);
        }
        return confirmed;
    }

    /**
     * Settles the mapped-vendor label for created facilities that got none from a vendor code, from
     * whatever HRMS already maps to their boundary. Never throws.
     */
    private void applyMappedVendorFromHrms(List<Facility> facilities, RequestInfo requestInfo) {
        for (Facility facility : facilities) {
            if (facility == null) {
                continue;
            }
            FacilityMappedVendorHelper.hydrateFromAdditionalDetails(facility);
            if (FacilityMappedVendorHelper.hasMappedVendor(facility)) {
                continue;
            }
            String boundaryCode = facility.getBoundaryCode();
            if (boundaryCode == null || boundaryCode.isBlank()) {
                continue;
            }
            try {
                Employee mappedVendor = hrmsUtils.getMappedVendorByBoundaryCode(requestInfo, boundaryCode);
                if (mappedVendor == null) {
                    continue;
                }
                User vendorUser = mappedVendor.getUser();
                String userName = firstNonBlank(
                        vendorUser != null ? vendorUser.getUserName() : null,
                        mappedVendor.getCode());
                if (userName == null) {
                    continue;
                }
                FacilityMappedVendorHelper.applyMappedVendor(
                        facility, vendorUser != null ? vendorUser.getName() : null, userName);
                log.info("Resolved mapped vendor {} from HRMS for facility {} (boundaryCode {})",
                        userName, facility.getFacilityId(), boundaryCode);
            } catch (Exception e) {
                log.error("Failed to resolve mapped vendor from HRMS for boundaryCode {} (non-blocking): {}",
                        boundaryCode, e.getMessage(), e);
            }
        }
    }

    // ---------------------------------------------------------------- vendor-registry

    private VendorOrganisation findOrganisationByCode(String code, String tenantId, RequestInfo requestInfo) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("tenantId", tenantId);
        criteria.put("code", code);
        List<VendorOrganisation> found = searchOrganisations(criteria, 1, requestInfo);
        return found.isEmpty() ? null : found.get(0);
    }

    private Map<String, VendorOrganisation> findOrganisationsByIds(Collection<String> ids, String tenantId,
                                                                   RequestInfo requestInfo) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("tenantId", tenantId);
        criteria.put("ids", new ArrayList<>(ids));
        return searchOrganisations(criteria, ids.size(), requestInfo).stream()
                .collect(Collectors.toMap(VendorOrganisation::id, organisation -> organisation, (a, b) -> a));
    }

    private List<VendorOrganisation> searchOrganisations(Map<String, Object> criteria, int limit, RequestInfo requestInfo) {
        Map<String, Object> body = new HashMap<>();
        body.put(REQUEST_INFO_KEY, requestInfo);
        body.put("SearchCriteria", criteria);
        body.put("Pagination", Map.of("limit", limit, "offset", 0));
        Map<String, Object> response = castToMap(serviceRequestRepository.fetchResult(
                new StringBuilder(vendorHost() + configs.getVendorOrganisationSearchPath()), body));
        return castToListOfMaps(response.get("organisations")).stream()
                .filter(organisation -> organisation.get("id") != null)
                .map(organisation -> new VendorOrganisation(
                        organisation.get("id").toString(),
                        Objects.toString(organisation.get("code"), null),
                        Objects.toString(organisation.get("name"), null)))
                .toList();
    }

    /** The user ids of an organisation's users, deleted ones excluded. */
    private List<String> findOrgUserIds(String organisationId, String tenantId, RequestInfo requestInfo) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("tenantId", tenantId);
        criteria.put("organizationIds", List.of(organisationId));
        return searchOrgUsers(criteria, tenantId, requestInfo).stream()
                .map(orgUser -> Objects.toString(orgUser.get("userId"), ""))
                .filter(userId -> !userId.isBlank())
                .distinct()
                .toList();
    }

    /** The organisation of each given user that belongs to one. */
    private Map<String, String> findOrganisationIdsByUserIds(Collection<String> userIds, String tenantId,
                                                             RequestInfo requestInfo) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("tenantId", tenantId);
        criteria.put("userIds", new ArrayList<>(userIds));
        Map<String, String> organisationIdByUserId = new LinkedHashMap<>();
        for (Map<String, Object> orgUser : searchOrgUsers(criteria, tenantId, requestInfo)) {
            Object userId = orgUser.get("userId");
            Object organisationId = orgUser.get("organizationId");
            if (userId != null && organisationId != null) {
                organisationIdByUserId.putIfAbsent(userId.toString(), organisationId.toString());
            }
        }
        return organisationIdByUserId;
    }

    private List<Map<String, Object>> searchOrgUsers(Map<String, Object> criteria, String tenantId,
                                                     RequestInfo requestInfo) {
        String uri = UriComponentsBuilder
                .fromUriString(vendorHost() + configs.getVendorOrganisationUserSearchPath())
                .queryParam("limit", ORG_USER_PAGE)
                .queryParam("offset", 0)
                .queryParam("tenantId", tenantId)
                .toUriString();
        Map<String, Object> body = new HashMap<>();
        body.put(REQUEST_INFO_KEY, requestInfo);
        body.put("OrgUser", criteria);
        Map<String, Object> response = castToMap(serviceRequestRepository.fetchResult(new StringBuilder(uri), body));
        return castToListOfMaps(response.get("OrgUsers")).stream()
                .filter(orgUser -> !Boolean.TRUE.equals(orgUser.get("isDeleted")))
                .toList();
    }

    private String vendorHost() {
        if (configs.getVendorHost() == null || configs.getVendorHost().isBlank()) {
            throw new CustomException("CONFIG_ERROR", "egov.vendor.host is not configured");
        }
        return configs.getVendorHost();
    }

    // ---------------------------------------------------------------- helpers

    private static final Comparator<VendorOrganisation> BY_NAME = Comparator
            .comparing((VendorOrganisation organisation) -> Objects.toString(organisation.name(), ""),
                    String.CASE_INSENSITIVE_ORDER)
            .thenComparing(organisation -> Objects.toString(organisation.code(), ""));

    /** Trimmed, distinct, non-blank codes, in the order given. */
    static Set<String> normalize(Collection<String> codes) {
        Set<String> normalized = new LinkedHashSet<>();
        if (codes != null) {
            codes.stream().filter(Objects::nonNull).map(String::trim).filter(code -> !code.isEmpty())
                    .forEach(normalized::add);
        }
        return normalized;
    }

    private static String label(VendorOrganisation organisation) {
        return organisation.name() == null ? organisation.code() : organisation.name() + " (" + organisation.code() + ")";
    }

    /** The first value that is neither null nor blank, trimmed; null when there is none. */
    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
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
