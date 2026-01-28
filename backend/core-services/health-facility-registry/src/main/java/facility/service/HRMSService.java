package facility.service;

import facility.config.Configuration;
import facility.repository.ServiceRequestRepository;
import facility.web.models.Facility;
import facility.web.models.HealthFacilityDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class HRMSService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final Configuration configs;

    /**
     * Searches for an employee by mobile number (phone number) in HRMS.
     * 
     * @param mobileNumber The mobile number to search for
     * @param tenantId The tenant ID
     * @param requestInfo RequestInfo for the API call
     * @return true if an employee with the given mobile number exists, false otherwise
     */
    public boolean employeeExistsByMobileNumber(String mobileNumber, String tenantId, RequestInfo requestInfo) {
        log.trace("Entering employeeExistsByMobileNumber method");
        if (mobileNumber == null || mobileNumber.isBlank()) {
            log.debug("Mobile number is null or blank, returning false");
            return false;
        }

        log.info("Checking if employee exists by mobile number for tenant {}", tenantId);
        log.debug("Searching for employee with mobile number (last 4 digits only for privacy)");
        try {
            // Build HRMS search request
            String uri = UriComponentsBuilder
                    .fromUriString(configs.getHrmsHost())
                    .path(configs.getHrmsSearchEndPoint())
                    .queryParam("phone", mobileNumber)
                    .queryParam("tenantId", tenantId)
                    .queryParam("isActive", true)
                    .toUriString();
            log.debug("HRMS search URI constructed");

            // Request body should only contain RequestInfo (Criteria goes in query params)
            Map<String, Object> searchRequest = new HashMap<>();
            searchRequest.put("RequestInfo", requestInfo);

            // Call HRMS search API
            Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchResult(
                    new StringBuilder(uri), searchRequest
            );

            // Parse response to check if employee exists
            if (response != null && response.containsKey("Employees")) {
                List<Map<String, Object>> employees = (List<Map<String, Object>>) response.get("Employees");
                boolean exists = employees != null && !employees.isEmpty();
                log.info("Employee {} by mobile number for tenant {}", exists ? "exists" : "does not exist", tenantId);
                log.trace("Exiting employeeExistsByMobileNumber method");
                return exists;
            }

            log.debug("No employees found in HRMS response");
            return false;
        } catch (Exception e) {
            log.warn("Error checking if employee exists by mobile number for tenant {}: {}", tenantId, e.getMessage(), e);
            // If check fails, return false to allow creation (fail open approach)
            return false;
        }
    }

    /**
     * Creates an HRMS employee for the facility POC user with HCR role.
     * 
     * @param facility The facility for which to create the POC employee
     * @param requestInfo RequestInfo for the API call
     * @return true if employee was created successfully, false otherwise
     */
    public boolean createFacilityPOCEmployee(Facility facility, RequestInfo requestInfo) {
        log.trace("Entering createFacilityPOCEmployee method");
        HealthFacilityDetails facilityDetails = facility.getFacilityDetails();
        
        if (facilityDetails == null || facilityDetails.getHfrId() == null || 
            facilityDetails.getHfrId().isBlank() || facilityDetails.getPocContact() == null || 
            facilityDetails.getPocContact().isBlank() || facilityDetails.getPocName() == null) {
            log.warn("Cannot create POC employee for facility {}: missing HFR ID, POC contact, or name", 
                    sanitizeForLog(facility.getFacilityId()));
            return false;
        }

        log.info("Creating POC employee for facility {} with HFR ID {}", 
                sanitizeForLog(facility.getFacilityId()), sanitizeForLog(facilityDetails.getHfrId()));
        try {
            // Build employee object
            Map<String, Object> user = new HashMap<>();
            user.put("userName", facilityDetails.getHfrId()); // Use HFR ID as username
            user.put("name", facilityDetails.getPocName());
            user.put("mobileNumber", facilityDetails.getPocContact());
            user.put("tenantId", facility.getTenantId());
            user.put("type", "EMPLOYEE");
            user.put("active", true);

            // Add roles - COMPLAINANT and EMPLOYEE roles
            List<Map<String, Object>> roles = new ArrayList<>();
            
            // COMPLAINANT role
            Map<String, Object> complainantRole = new HashMap<>();
            complainantRole.put("code", "COMPLAINANT");
            complainantRole.put("name", "Complainant");
            complainantRole.put("tenantId", facility.getTenantId());
            roles.add(complainantRole);
            
            // EMPLOYEE role
            Map<String, Object> employeeRole = new HashMap<>();
            employeeRole.put("code", "EMPLOYEE");
            employeeRole.put("name", "Employee");
            employeeRole.put("tenantId", facility.getTenantId());
            roles.add(employeeRole);
            
            user.put("roles", roles);

            // Get current timestamp for dateOfAppointment
            long currentTimestamp = Instant.now().toEpochMilli();

            // Build employee object
            Map<String, Object> employee = new HashMap<>();
            employee.put("code", null); // HRMS will generate employee code
            employee.put("employeeStatus", "EMPLOYED");
            employee.put("employeeType", "PERMANENT");
            employee.put("dateOfAppointment", currentTimestamp);
            employee.put("tenantId", facility.getTenantId());
            employee.put("isActive", true);
            employee.put("user", user);

            // Add jurisdictions with facility boundary
            if (facility.getBoundaryCode() != null && !facility.getBoundaryCode().isBlank()) {
                List<Map<String, Object>> jurisdictions = new ArrayList<>();
                Map<String, Object> jurisdiction = new HashMap<>();
                jurisdiction.put("hierarchy", "ADMIN");
                jurisdiction.put("boundary", facility.getBoundaryCode());
                jurisdiction.put("boundaryType", "Facility");
                jurisdiction.put("tenantId", facility.getTenantId());
                jurisdiction.put("isActive", true);
                jurisdictions.add(jurisdiction);
                employee.put("jurisdictions", jurisdictions);
            }

            // Add assignments with designation and department
            List<Map<String, Object>> assignments = new ArrayList<>();
            Map<String, Object> assignment = new HashMap<>();
            
            // Add designation code if available
            if (facilityDetails.getPocDesignation() != null && !facilityDetails.getPocDesignation().isBlank()) {
                String designationCode = facilityDetails.getPocDesignation();
                assignment.put("designation", designationCode);
            }
            assignment.put("department", configs.getHrmsDefaultDepartmentCode());
            assignment.put("fromDate", currentTimestamp);
            assignment.put("toDate", null);
            assignment.put("tenantid", facility.getTenantId());
            assignment.put("isCurrentAssignment", true);
            assignments.add(assignment);
            employee.put("assignments", assignments);

            // Build create request
            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("RequestInfo", requestInfo);
            createRequest.put("Employees", Arrays.asList(employee));

            // Construct the URI
            String uri = UriComponentsBuilder
                    .fromUriString(configs.getHrmsHost())
                    .path(configs.getHrmsCreateEndPoint())
                    .toUriString();

            // Call HRMS create API
            Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchResult(
                    new StringBuilder(uri), createRequest
            );

            if (response != null) {
                log.info("Successfully created POC employee for facility {} with HFR ID {}", 
                        sanitizeForLog(facility.getFacilityId()), sanitizeForLog(facilityDetails.getHfrId()));
                log.trace("Exiting createFacilityPOCEmployee method");
                return true;
            }

            log.warn("HRMS create employee response was null for facility {}", sanitizeForLog(facility.getFacilityId()));
            return false;
        } catch (Exception e) {
            log.error("Error creating POC employee for facility {}: {}", 
                    sanitizeForLog(facility.getFacilityId()), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sanitizes a string value for safe logging by removing control characters
     * that could be used for log injection attacks (newlines, carriage returns).
     * 
     * @param value The string value to sanitize
     * @return null if input is null, otherwise the sanitized string with \r and \n replaced by spaces
     */
    private String sanitizeForLog(String value) {
        if (value == null) {
            return null;
        }
        return value.replace('\r', ' ').replace('\n', ' ');
    }
}

