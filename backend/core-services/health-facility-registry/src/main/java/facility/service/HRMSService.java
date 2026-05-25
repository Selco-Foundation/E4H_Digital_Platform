package facility.service;

import facility.config.Configuration;
import facility.repository.ServiceRequestRepository;
import facility.util.MdmsUtil;
import facility.web.models.Facility;
import facility.web.models.HealthFacilityDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class HRMSService {

    private static final String CATEGORY_ANGANWADI = "ANGANWADI";
    private static final String MDMS_COMMON_MASTERS_MODULE = "common-masters";
    private static final String MDMS_DESIGNATION_MASTER = "Designation";

    private final ServiceRequestRepository serviceRequestRepository;
    private final Configuration configs;
    private final MdmsUtil mdmsUtil;

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

    public boolean employeeExistsByUsername(String username, String tenantId, RequestInfo requestInfo) {
        log.trace("Entering employeeExistsByMobileNumber method");
        if (username == null || username.isBlank()) {
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
                    .queryParam("codes", username)
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

        String normalizedCategory = facility.getFacilityCategory() == null
                ? ""
                : facility.getFacilityCategory().trim().toUpperCase(Locale.ROOT);
        boolean isAnganwadi = CATEGORY_ANGANWADI.equals(normalizedCategory);

        String employeeCode;
        if (isAnganwadi) {
            if (facilityDetails == null || facilityDetails.getPocContact() == null
                    || facilityDetails.getPocContact().isBlank()
                    || facilityDetails.getPocName() == null || facilityDetails.getPocName().isBlank()) {
                log.warn("Cannot create POC employee for ANGANWADI facility {}: missing POC contact or name",
                        sanitizeForLog(facility.getFacilityId()));
                return false;
            }
            String pocUsername = facility.getFacilityPocUsername();
            if (pocUsername == null || pocUsername.isBlank()) {
                log.warn("Cannot create POC employee for ANGANWADI facility {}: missing facility POC username",
                        sanitizeForLog(facility.getFacilityId()));
                return false;
            }
            employeeCode = pocUsername.trim();
        } else {
            if (facilityDetails == null || facilityDetails.getPocContact() == null
                    || facilityDetails.getPocContact().isBlank() || facilityDetails.getPocName() == null
                    || facilityDetails.getPocName().isBlank()) {
                log.warn("Cannot create POC employee for facility {}: missing POC contact or name",
                        sanitizeForLog(facility.getFacilityId()));
                return false;
            }
            employeeCode = resolveFacilityEmployeeCode(facility);
            if (employeeCode == null || employeeCode.isBlank()) {
                log.warn("Cannot create POC employee for facility {}: missing HFR or NIN ID",
                        sanitizeForLog(facility.getFacilityId()));
                return false;
            }
        }

        log.info("Creating POC employee for facility {} with employee code {}",
                sanitizeForLog(facility.getFacilityId()), sanitizeForLog(employeeCode));
        try {
            // Build employee object
            Map<String, Object> user = new HashMap<>();
            user.put("userName", employeeCode);
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
            employee.put("code", employeeCode);
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
            String designationCode = null;
            if (facilityDetails.getPocDesignation() != null && !facilityDetails.getPocDesignation().isBlank()) {
                designationCode = resolveDesignationCode(
                        facilityDetails.getPocDesignation(), facility.getTenantId(), requestInfo);
                if (designationCode == null) {
                    log.warn("Could not resolve designation code for POC designation '{}' for facility {}, using default if configured",
                            sanitizeForLog(facilityDetails.getPocDesignation()), sanitizeForLog(facility.getFacilityId()));
                }
            }
            if (designationCode == null
                    && configs.getHrmsDefaultDesignationCode() != null
                    && !configs.getHrmsDefaultDesignationCode().isBlank()) {
                designationCode = configs.getHrmsDefaultDesignationCode();
            }
            if (designationCode != null) {
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
                log.info("Successfully created POC employee for facility {} with employee code {}",
                        sanitizeForLog(facility.getFacilityId()), sanitizeForLog(employeeCode));
                
                // Update user password after successful creation
                updateUserPassword(response, requestInfo);
                
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
     * Updates the password for a newly created user from HRMS response.
     * Extracts user details from HRMS employee response and calls user service to update password.
     * 
     * @param hrmsResponse The HRMS create employee response containing employee and user details
     * @param requestInfo RequestInfo for the API call
     */
    private void updateUserPassword(Map<String, Object> hrmsResponse, RequestInfo requestInfo) {
        log.trace("Entering updateUserPassword method");
        try {
            // Extract employees from HRMS response
            if (!hrmsResponse.containsKey("Employees")) {
                log.warn("HRMS response does not contain Employees, cannot update password");
                return;
            }

            List<Map<String, Object>> employees = (List<Map<String, Object>>) hrmsResponse.get("Employees");
            if (employees == null || employees.isEmpty()) {
                log.warn("No employees found in HRMS response, cannot update password");
                return;
            }

            // Get the first employee (should be the one we just created)
            Map<String, Object> employee = employees.get(0);
            if (!employee.containsKey("user")) {
                log.warn("Employee does not contain user information, cannot update password");
                return;
            }

            Map<String, Object> user = (Map<String, Object>) employee.get("user");
            if (user == null) {
                log.warn("User object is null, cannot update password");
                return;
            }

            // Verify user has required fields (uuid or id) for update
            if (!user.containsKey("uuid") && !user.containsKey("id")) {
                log.warn("User object does not contain uuid or id, cannot update password. User: {}", 
                        sanitizeForLog((String) user.get("userName")));
                return;
            }

            // Set default password
            user.put("password", configs.getDefaultUserPassword());
            
            // Build user update request
            Map<String, Object> userUpdateRequest = new HashMap<>();
            userUpdateRequest.put("RequestInfo", requestInfo);
            userUpdateRequest.put("user", user);

            // Build user update URI
            String updateUri = configs.getUserHost() + configs.getUserContextPath() + configs.getUserUpdateEndpoint();
            
            log.debug("Updating password for user: {}", sanitizeForLog((String) user.get("userName")));
            
            // Call user service to update password
            serviceRequestRepository.fetchResult(new StringBuilder(updateUri), userUpdateRequest);
            
            log.info("Successfully updated password for user: {}", sanitizeForLog((String) user.get("userName")));
            log.trace("Exiting updateUserPassword method");
        } catch (Exception e) {
            log.error("Error updating user password: {}", e.getMessage(), e);
        }
    }

    /**
     * Resolves HRMS designation code from a POC designation value by looking up
     * {@code common-masters.Designation} in MDMS. Matches by designation name (e.g. "Medical officer")
     * or returns the value as-is when it already matches a designation code.
     */
    private String resolveDesignationCode(String designationValue, String tenantId, RequestInfo requestInfo) {
        if (designationValue == null || designationValue.isBlank()) {
            return null;
        }

        String normalizedValue = designationValue.trim();
        try {
            Map<String, Map<String, JSONArray>> mdmsData = mdmsUtil.fetchMdmsData(
                    requestInfo, tenantId, MDMS_COMMON_MASTERS_MODULE, List.of(MDMS_DESIGNATION_MASTER));

            JSONArray designations = mdmsData
                    .getOrDefault(MDMS_COMMON_MASTERS_MODULE, Map.of())
                    .get(MDMS_DESIGNATION_MASTER);
            if (designations == null || designations.isEmpty()) {
                log.warn("No Designation records found in MDMS for tenant {}", tenantId);
                return null;
            }

            for (Object obj : designations) {
                if (!(obj instanceof Map)) {
                    continue;
                }
                Map<String, Object> designation = (Map<String, Object>) obj;
                Object codeObj = designation.get("code");
                Object nameObj = designation.get("name");
                if (codeObj != null && normalizedValue.equalsIgnoreCase(codeObj.toString().trim())) {
                    return codeObj.toString().trim();
                }
                if (nameObj != null && normalizedValue.equalsIgnoreCase(nameObj.toString().trim())
                        && codeObj != null && !codeObj.toString().isBlank()) {
                    return codeObj.toString().trim();
                }
            }

            log.warn("Designation '{}' not found in MDMS common-masters.Designation for tenant {}",
                    sanitizeForLog(normalizedValue), tenantId);
        } catch (Exception e) {
            log.warn("Error resolving designation code for '{}' in tenant {}: {}",
                    sanitizeForLog(normalizedValue), tenantId, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Resolves the facility identifier used as HRMS username/employee code.
     * Prefers HFR ID over NIN ID; checks both top-level facility fields and nested facilityDetails.
     */
    private String resolveFacilityEmployeeCode(Facility facility) {
        HealthFacilityDetails facilityDetails = facility.getFacilityDetails();
        if (facility.getHfrId() != null && !facility.getHfrId().trim().isBlank()) {
            return facility.getHfrId().trim();
        }
        if (facilityDetails != null && facilityDetails.getHfrId() != null && !facilityDetails.getHfrId().isBlank()) {
            return facilityDetails.getHfrId().trim();
        }
        if (facility.getNinId() != null && !facility.getNinId().trim().isBlank()) {
            return facility.getNinId().trim();
        }
        if (facilityDetails != null && facilityDetails.getNinId() != null && !facilityDetails.getNinId().isBlank()) {
            return facilityDetails.getNinId().trim();
        }
        return null;
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

