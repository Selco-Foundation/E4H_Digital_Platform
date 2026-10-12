package facility.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.config.Configuration;
import facility.repository.ServiceRequestRepository;
import facility.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HRMSUtils {

    /**
     * The HRMS role a vendor user holds. A facility's mapped vendor is the holder of this role whose
     * jurisdictions carry that facility's boundary - the same role im-services resolves a ticket's
     * mapped vendor by ({@code IMConstants.ROLE_COMPLAINT_RESOLVER}).
     */
    /** Key wrapping the RequestInfo in every egov service-call body. */
    private static final String REQUEST_INFO_KEY = "RequestInfo";

    private static final String ROLE_COMPLAINT_RESOLVER = "COMPLAINT_RESOLVER";

    /** Jurisdiction boundary type carrying a facility mapping, as vendor-registry writes it. */
    private static final String FACILITY_BOUNDARY_TYPE = "Facility";

    private final ServiceRequestRepository serviceRequestRepository;

    private final Configuration config;

    private final ObjectMapper mapper;

    @Autowired
    public HRMSUtils(ServiceRequestRepository serviceRequestRepository, Configuration config, ObjectMapper mapper) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.config = config;
        this.mapper = mapper;
    }

    public Employee getUserById(Object request, String userId) {
        String url = config.getHrmsHost() + config.getHrmsSearchEndPoint()+ "?tenantId=in&uuids="+userId;
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            throw new CustomException("EMPLOYEE_NOT_FOUND", "Employee not found with ID: " + userId);
        }
        return employeeResponse.getEmployees().get(0);
    }

    public Employee getUserByUsername(Object request, String codes) {
        String url = config.getHrmsHost() + config.getHrmsSearchEndPoint()+ "?tenantId=in&codes="+codes;
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            throw new CustomException("EMPLOYEE_NOT_FOUND", "Employee not found with username: " + codes);
        }
        return employeeResponse.getEmployees().get(0);
    }

    /**
     * Searches for the first employee (active or inactive) assigned to the given boundary
     * (facility) code. Does not filter by active status so callers can detect and reactivate an
     * existing-but-inactive POC employee rather than mistakenly creating a duplicate.
     * Returns null (rather than throwing) when no employee is found, since that is a valid
     * outcome for callers reconciling a facility's HRMS-side username.
     */
    public Employee getEmployeeByBoundaryCode(Object requestInfo, String boundaryCode) {
        String url = config.getHrmsHost() + config.getHrmsSearchEndPoint()
                + "?tenantId=in&boundaryCodes=" + boundaryCode + "&roles=COMPLAINANT&searchOnlyInBoundary=true";
        Map<String, Object> searchRequest = new HashMap<>();
        searchRequest.put(REQUEST_INFO_KEY, requestInfo);
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), searchRequest);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            return null;
        }
        return employeeResponse.getEmployees().get(0);
    }

    /**
     * The vendor user HRMS currently maps to a facility: the active {@code COMPLAINT_RESOLVER}
     * holding an active {@code Facility} jurisdiction on {@code boundaryCode}.
     *
     * <p>HRMS is the upstream owner of this mapping - vendor-registry writes the jurisdiction onto
     * the vendor's employee record first and only then pushes the resulting name/username down onto
     * the facility row. Reading it back from here lets the index be built from the mapping itself
     * rather than from a copy that may never have been written.
     *
     * <p>The boundary carried by each returned employee is re-checked locally rather than trusting
     * {@code searchOnlyInBoundary} alone: an employee whose jurisdiction sits at block or district
     * level covers this facility without being mapped to it, and indexing that vendor would be worse
     * than indexing none. For the same reason a search that matches nothing verifiable returns null
     * instead of the first hit, leaving the caller on its own fallback.
     *
     * <p>Never throws: a facility index push must not fail because HRMS is unreachable.
     *
     * @return the mapped vendor's employee record, or {@code null} when HRMS holds no such mapping
     *         or could not be reached
     */
    public Employee getMappedVendorByBoundaryCode(Object requestInfo, String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return null;
        }
        // tenantId=in like the sibling lookups above: HRMS holds these employees at the national
        // tenant regardless of which sub-tenant the facility itself lives in.
        String url = config.getHrmsHost() + config.getHrmsSearchEndPoint()
                + "?tenantId=in&boundaryCodes=" + boundaryCode.trim()
                + "&roles=" + ROLE_COMPLAINT_RESOLVER
                + "&searchOnlyInBoundary=true&isActive=true";
        try {
            Map<String, Object> searchRequest = new HashMap<>();
            searchRequest.put(REQUEST_INFO_KEY, requestInfo);
            Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), searchRequest);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
            if (employeeResponse == null || employeeResponse.getEmployees() == null) {
                return null;
            }
            for (Employee employee : employeeResponse.getEmployees()) {
                if (employee != null && hasActiveFacilityJurisdiction(employee, boundaryCode)) {
                    return employee;
                }
            }
            log.info("HRMS holds no mapped vendor for boundaryCode {}", boundaryCode);
            return null;
        } catch (Exception e) {
            log.warn("Unable to resolve mapped vendor from HRMS for boundaryCode {}: {}",
                    boundaryCode, e.getMessage());
            return null;
        }
    }

    private static boolean hasActiveFacilityJurisdiction(Employee employee, String boundaryCode) {
        if (employee.getJurisdictions() == null) {
            return false;
        }
        return employee.getJurisdictions().stream()
                .filter(Objects::nonNull)
                .filter(j -> j.getIsActive() == null || Boolean.TRUE.equals(j.getIsActive()))
                .filter(j -> FACILITY_BOUNDARY_TYPE.equalsIgnoreCase(Objects.toString(j.getBoundaryType(), "")))
                .anyMatch(j -> boundaryCode.trim().equalsIgnoreCase(Objects.toString(j.getBoundary(), "")));
    }

    /**
     * Calls egov-hrms {@code /employees/_update_username} to force eg_user.username to match
     * eg_hrms_employee.code for the given employee uuid.
     */
    public boolean updateHrmsUsername(Object requestInfo, String uuid, String code, String tenantId) {
        String url = config.getHrmsHost() + config.getHrmsUpdateUsernameEndPoint();
        Map<String, Object> employee = new HashMap<>();
        employee.put("tenantId", tenantId);
        employee.put("uuid", uuid);
        employee.put("code", code);

        Map<String, Object> body = new HashMap<>();
        body.put(REQUEST_INFO_KEY, requestInfo);
        body.put("employee", employee);
        try {
            serviceRequestRepository.fetchResult(new StringBuilder(url), body);
            return true;
        } catch (Exception e) {
            log.error("Error calling HRMS update-username for uuid {}: {}", uuid, e.getMessage(), e);
            return false;
        }
    }

    public List<Employee> getUserByPhoneNumber(Object request, String phoneNumber) {
        String url = config.getHrmsHost() + config.getHrmsSearchEndPoint()+ "?tenantId=in&phone="+phoneNumber;
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            return null;
        }
        return employeeResponse.getEmployees();
    }

    public List<Employee> createHRMSUser(Object request) {
        String url = config.getHrmsHost() + config.getHrmsCreateEndPoint()+ "?tenantId=in";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            return null;
        }
        return employeeResponse.getEmployees();
    }

    public List<Employee> updateHRMSUser(Object request) {
        String url = config.getHrmsHost() + config.getHrmsUpdateEndPoint()+ "?tenantId=in";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            return null;
        }
        return employeeResponse.getEmployees();
    }

    public Employee buildEmployee(User user, String orgType) {
        Employee employee = Employee.builder()
//                .id(source.getId())
//                .uuid(source.getUuid())
                .code(user.getUserName())
                .employeeStatus("EMPLOYED")
                .employeeType("PERMANENT")
                .dateOfAppointment(1617215400000L)
                .tenantId("in")
                .IsActive(true)
                .reActivateEmployee(false)
                .assignments(buildAssignments())
                .user(user)
//                .auditDetails(source.getAuditDetails())
                .build();
        if (orgType != null && !orgType.isEmpty() && orgType.trim().equals("PLATFORM")){
            employee.setJurisdictions(buildJurisdictions(user.getJurisdiction()));
        }
        return employee;
    }

    public Jurisdiction buildFacilityJurisdiction(String boundaryCode, String tenantId) {
        return Jurisdiction.builder()
                .hierarchy("ADMIN")
                .boundary(boundaryCode)
                .boundaryType("Facility")
                .tenantId(tenantId)
                .isActive(true)
                .build();
    }

    /**
     * Adds or re-activates a facility boundary in the employee jurisdiction list (vendor user mapping).
     */
    public List<Jurisdiction> mergeFacilityJurisdiction(List<Jurisdiction> existing, Jurisdiction facilityJurisdiction) {
        List<Jurisdiction> merged = new ArrayList<>();
        if (existing != null) {
            merged.addAll(existing);
        }
        if (facilityJurisdiction == null || facilityJurisdiction.getBoundary() == null) {
            return merged;
        }

        int idx = indexOfJurisdictionByBoundary(merged, facilityJurisdiction.getBoundary());
        if (idx >= 0) {
            Jurisdiction target = merged.get(idx);
            target.setHierarchy(facilityJurisdiction.getHierarchy());
            target.setBoundaryType(facilityJurisdiction.getBoundaryType());
            target.setTenantId(facilityJurisdiction.getTenantId());
            target.setIsActive(true);
        } else {
            merged.add(facilityJurisdiction);
        }
        return merged;
    }

    private int indexOfJurisdictionByBoundary(List<Jurisdiction> jurisdictions, String boundary) {
        if (jurisdictions == null || boundary == null) {
            return -1;
        }
        for (int i = 0; i < jurisdictions.size(); i++) {
            Jurisdiction j = jurisdictions.get(i);
            if (j != null && boundary.equalsIgnoreCase(Objects.toString(j.getBoundary(), ""))) {
                return i;
            }
        }
        return -1;
    }

    public List<Jurisdiction> buildJurisdictions(List<String> boundaryCodes) {
        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
            Jurisdiction jurisdiction = Jurisdiction.builder()
                    .hierarchy("ADMIN")
                    .boundary("in")
                    .boundaryType("City")
                    .tenantId("in")
                    .isActive(true)
                    .build();
            return Collections.singletonList(jurisdiction);
        }

        return boundaryCodes.stream()
                .map(boundaryCode ->
                        Jurisdiction.builder()
                                .hierarchy("ADMIN")
                                .boundary(boundaryCode)
                                .boundaryType("Block")
                                .tenantId("in")
                                .isActive(true)
                                .build()
                )
                .collect(Collectors.toList());
    }

    public List<Assignment> buildAssignments() {
        Assignment assignment = Assignment.builder()
                .position(20809L)
                .designation("DESIG_01")
                .department("DEPT_1")
                .fromDate(1617215400000L)
                .tenantid("in")
                .isHOD(false)
                .isCurrentAssignment(true)
                .build();

        return Collections.singletonList(assignment);
    }

    public User buildUser(User source) {

        if (source == null) return null;

        return User.builder()
                .id(source.getId())
                .uuid(source.getUuid())
                .userName(source.getUserName())
                .name(source.getName())
                .gender(source.getGender())
                .mobileNumber(source.getMobileNumber())
                .emailId(source.getEmailId())
                .active(source.getActive())
                .dob(source.getDob())
                .locale(source.getLocale())
                .type(source.getType())
                .tenantId(source.getTenantId())
                .roles(source.getRoles())
                .build();
    }



}
