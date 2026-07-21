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
        searchRequest.put("RequestInfo", requestInfo);
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), searchRequest);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            return null;
        }
        return employeeResponse.getEmployees().get(0);
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
        body.put("RequestInfo", requestInfo);
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
