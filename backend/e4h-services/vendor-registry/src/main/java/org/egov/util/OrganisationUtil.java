package org.egov.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.egov.web.models.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class OrganisationUtil {

    private final ServiceRequestRepository serviceRequestRepository;
    private final Configuration config;
    private final ObjectMapper mapper;

    public OrganisationUtil(ServiceRequestRepository serviceRequestRepository, Configuration config, ObjectMapper mapper) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.config = config;
        this.mapper = mapper;
    }

    /**
     * Method to set auditDetails for create/update flows of organisations
     *
     * @param by
     * @param isCreate
     * @return
     */
    public void setAuditDetailsForOrganisation(String by, List<Organisation> organisationList, Boolean isCreate) {
        Long time = System.currentTimeMillis();
        for (Organisation organisation : organisationList) {
            if (Boolean.TRUE.equals(isCreate)) {
                AuditDetails auditDetailsForCreate = AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
                organisation.setAuditDetails(auditDetailsForCreate);
            } else {
                AuditDetails auditDetailsForUpdate = AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
                organisation.setAuditDetails(auditDetailsForUpdate);
            }
        }
    }

    /**
     * Method to set auditDetails for create/update flows of functions
     *
     * @param by
     * @param isCreate
     * @return
     */
    public void setAuditDetailsForFunction(String by, List<Function> functionList, Boolean isCreate) {
        Long time = System.currentTimeMillis();
        for (Function function : functionList) {
            if (Boolean.TRUE.equals(isCreate)) {
                AuditDetails auditDetailsForCreate = AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
                function.setAuditDetails(auditDetailsForCreate);
            } else {
                AuditDetails auditDetailsForUpdate = AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
                function.setAuditDetails(auditDetailsForUpdate);
            }
        }
    }

    public AuditDetails getAuditDetails(String by, AuditDetails auditDetails, Boolean isCreate) {
        Long time = System.currentTimeMillis();
        if (isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).lastModifiedBy(by)
                    .createdTime(auditDetails.getCreatedTime()).lastModifiedTime(time).build();
    }

    public Employee getUserById(Object request, String userId) {

        String url = config.getHrmsHost() + config.getHrmsEndPoint()+ "?tenantId=in&uuids="+userId;
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            throw new CustomException("EMPLOYEE_NOT_FOUND", "Employee not found with ID: " + userId);
        }
        return employeeResponse.getEmployees().get(0);
    }

}
