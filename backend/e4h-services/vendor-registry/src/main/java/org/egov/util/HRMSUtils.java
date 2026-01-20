package org.egov.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.egov.util.OrganisationConstant.HRMS_USER_MOBILE_NO;
import static org.egov.util.OrganisationConstant.HRMS_USER_USERNAME_CODE;

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


    public Map<String, String> getEmployeeDetailsByUuid(RequestInfo requestInfo, String tenantId, String uuid) {
        StringBuilder url = getHRMSURIWithUUid(tenantId, uuid);

        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();

        Object res = serviceRequestRepository.fetchResult(url, requestInfoWrapper);

        Map<String, String> userDetailsForSMS = new HashMap<>();
        List<String> userNames = null;
        List<String> mobileNumbers = null;

        try {
            userNames = JsonPath.read(res, HRMS_USER_USERNAME_CODE);
            mobileNumbers = JsonPath.read(res, HRMS_USER_MOBILE_NO);

        } catch (Exception e) {
            throw new CustomException("PARSING_ERROR", "Failed to parse HRMS response");
        }

        userDetailsForSMS.put("userName", userNames.get(0));
        userDetailsForSMS.put("mobileNumber", mobileNumbers.get(0));

        return userDetailsForSMS;
    }

    private StringBuilder getHRMSURIWithUUid(String tenantId, String employeeUuid) {

        StringBuilder builder = new StringBuilder(config.getHrmsHost());
        builder.append(config.getHrmsSearchEndPoint());
        builder.append("?tenantId=");
        builder.append(tenantId);
        builder.append("&uuids=");
        builder.append(employeeUuid);

        return builder;
    }

    public Employee getUserById(Object request, String userId) {
        String url = config.getHrmsHost() + config.getHrmsSearchEndPoint()+ "?tenantId=in&uuids="+userId;
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            log.info("EMPLOYEE_NOT_FOUND", "Employee not found with ID: " + userId);
            return null;
        }
        return employeeResponse.getEmployees().get(0);
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
                .jurisdictions(buildJurisdictions(user.getJurisdictions()))
                .user(user)
//                .auditDetails(source.getAuditDetails())
                .build();
        if (orgType != null && !orgType.isEmpty() && orgType.trim().equals("PLATFORM")){
            employee.setJurisdictions(buildJurisdictions(user.getJurisdictions()));
        }
        return employee;
    }

//    public List<Jurisdiction> buildJurisdictions(List<String> boundaryCodes) {
//        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
//            Jurisdiction jurisdiction = Jurisdiction.builder()
//                    .hierarchy("ADMIN")
//                    .boundary("in")
//                    .boundaryType("Country")
//                    .tenantId("in")
//                    .isActive(true)
//                    .build();
//            return Collections.singletonList(jurisdiction);
//        }
//
//        return boundaryCodes.stream()
//                .map(boundaryCode ->
//                        Jurisdiction.builder()
//                                .hierarchy("ADMIN")
//                                .boundary(boundaryCode)
//                                .boundaryType("Block")
//                                .tenantId("in")
//                                .isActive(true)
//                                .build()
//                )
//                .collect(Collectors.toList());
//    }

    public List<Jurisdiction> buildJurisdictions(List<Jurisdiction> jurisdiction) {
        if (jurisdiction == null || jurisdiction.isEmpty()) {
            Jurisdiction jurisdiction1 = Jurisdiction.builder()
                    .hierarchy("ADMIN")
                    .boundary("in")
                    .boundaryType("Country")
                    .tenantId("in")
                    .isActive(true)
                    .build();
            return Collections.singletonList(jurisdiction1);
        }

        return jurisdiction;
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
