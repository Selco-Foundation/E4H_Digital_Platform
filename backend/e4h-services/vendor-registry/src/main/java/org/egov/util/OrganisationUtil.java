package org.egov.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.repository.ServiceRequestRepository;
import org.egov.web.models.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class OrganisationUtil {

    private final ServiceRequestRepository serviceRequestRepository;
    private final Configuration config;
    private final ObjectMapper mapper;
    private EncryptionDecryptionUtilV2 encryptionDecryptionUtil;

    public OrganisationUtil(ServiceRequestRepository serviceRequestRepository, Configuration config, ObjectMapper mapper, EncryptionDecryptionUtilV2 encryptionDecryptionUtil) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.config = config;
        this.mapper = mapper;
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
    }

    /**
     * Method to set auditDetails for create/update flows of organisations
     *
     * @param by
     * @param isCreate
     * @return
     */
    public void setAuditDetailsForOrganisation(String by, List<Organisation> organisationList, Boolean isCreate) {
        log.trace("OrganisationUtil::setAuditDetailsForOrganisation entry");
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
        log.debug("Set audit details for {} organisations, isCreate: {}", organisationList != null ? organisationList.size() : 0, isCreate);
    }

    /**
     * Method to set auditDetails for create/update flows of functions
     *
     * @param by
     * @param isCreate
     * @return
     */
    public void setAuditDetailsForFunction(String by, List<Function> functionList, Boolean isCreate) {
        log.trace("OrganisationUtil::setAuditDetailsForFunction entry");
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
        log.debug("Set audit details for {} functions, isCreate: {}", functionList != null ? functionList.size() : 0, isCreate);
    }

    public AuditDetails getAuditDetails(String by, AuditDetails auditDetails, Boolean isCreate) {
        log.trace("OrganisationUtil::getAuditDetails entry");
        Long time = System.currentTimeMillis();
        if (isCreate) {
            log.debug("Creating new audit details for user: {}", by);
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        } else {
            log.debug("Updating audit details for user: {}", by);
            return AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).lastModifiedBy(by)
                    .createdTime(auditDetails.getCreatedTime()).lastModifiedTime(time).build();
        }
    }

    /**
     * True when {@code value} carries egov-enc-service's {@code keyId|ciphertext} envelope, i.e. it
     * is safe to hand to the decrypt endpoint. A bare mobile number returns false — passing one to
     * {@code _decrypt} makes egov-enc-service throw {@code "<value>: Invalid Ciphertext"}.
     */
    public static boolean isEncrypted(String value) {
        if (value == null) {
            return false;
        }
        int separator = value.indexOf('|');
        if (separator <= 0 || separator == value.length() - 1) {
            return false;
        }
        return value.substring(0, separator).chars().allMatch(Character::isDigit);
    }

    /** Encrypts a plaintext mobile number; an already-encrypted value is returned unchanged. */
    public String encryptMobileNumber(String mobileNumber){
        if (isEncrypted(mobileNumber)) {
            return mobileNumber;
        }
        String encryptedMobileNumber = null;
        if(mobileNumber!=null && !mobileNumber.isBlank()){
            EncryptObject object = EncryptObject.builder()
                    .mobileNumber(mobileNumber)
                    .build();
            Map<String, EncryptObject> userMap = new HashMap<>();
            userMap.put("userObject", object);
            EncReqObject encReqObject = EncReqObject.builder()
                    .tenantId(config.getStateLevelTenantId())
                    .type("Normal")
                    .value(userMap)
                    .build();
            EncryptionRequest encryptionRequest = EncryptionRequest.builder()
                    .encryptionRequests(List.of(encReqObject))
                    .build();
            List<Map<String, EncryptObject>> response = encryptionDecryptionUtil.encryptObject(encryptionRequest);
            for (Map<String, EncryptObject> map : response) {
                EncryptObject user = map.get("userObject"); // clé du JSON
                if (user != null) {
                    log.info("Mobile crypté : {}", user.getMobileNumber());
                    encryptedMobileNumber = user.getMobileNumber();
                }
            }
        }
        return encryptedMobileNumber;
    }

    /**
     * Decrypts an encrypted mobile number. Legacy rows holding a plaintext number, and
     * egov-enc-service failures, both yield {@code mobileNumber} unchanged rather than an exception —
     * one bad row must not fail the whole organisation search.
     */
    public String decryptMobileNumber(String mobileNumber){
        if (!isEncrypted(mobileNumber)) {
            return mobileNumber;
        }
        try {
            EncryptObject object = EncryptObject.builder()
                    .mobileNumber(mobileNumber)
                    .build();
            Map<String, EncryptObject> userMap = new HashMap<>();
            userMap.put("userObject", object);
            DecryptionRequest request = DecryptionRequest.builder()
                    .decryptionRequests(List.of(userMap))
                    .build();
            List<Map<String, EncryptObject>> response = encryptionDecryptionUtil.decryptObject(request);
            String decryptedMobileNumber = null;
            if (response != null) {
                for (Map<String, EncryptObject> map : response) {
                    EncryptObject user = map.get("userObject");
                    if (user != null) {
                        decryptedMobileNumber = user.getMobileNumber();
                    }
                }
            }
            return decryptedMobileNumber != null && !decryptedMobileNumber.isBlank()
                    ? decryptedMobileNumber : mobileNumber;
        } catch (Exception e) {
            log.error("Could not decrypt POC mobile number, returning stored value as-is: {}", e.getMessage());
            return mobileNumber;
        }
    }

    public List<ActivityAssignment> getFieldPlanActivityAssignment(DeleteOrgUserRequest request) {
        String userId = request.getUserId();
        String tenantId = config.getGlobalTenantId();
        ActivityAssignmentSearchCriteria criteria = ActivityAssignmentSearchCriteria.builder().assignedTo(userId).isActive(true).tenantId(tenantId).build();
        RequestInfo requestInfoForAssignmentSearch = mapper.convertValue(request.getRequestInfo(), RequestInfo.class);
        if (requestInfoForAssignmentSearch != null && requestInfoForAssignmentSearch.getUserInfo() != null) {
            // field-planner-activity adds implicit "assigned_to = RequestInfo.userInfo.uuid" for non-PM users
            // so align caller uuid with the target user to fetch that user's assignments.
            requestInfoForAssignmentSearch.getUserInfo().setUuid(userId);
        }
        ActivityAssignmentSearchRequest assignmentSearchRequest = ActivityAssignmentSearchRequest.builder().criteria(criteria).requestInfo(requestInfoForAssignmentSearch).build();
        String url = config.getFieldPlanActivityServiceHost() + config.getFieldPlanActivitySearchUrl()+ "?tenantId="+tenantId+"&offset=0&limit=100";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), assignmentSearchRequest);
        ActivityAssignmentResponse activityAssignmentList = mapper.convertValue(response, ActivityAssignmentResponse.class);
        if(activityAssignmentList != null && activityAssignmentList.getActivityAssignment() !=null){
            return activityAssignmentList.getActivityAssignment();
        }
        return null;
    }

//    public List<ActivityFacility> getFieldPlanFacilityActivities(RequestInfo requestInfo, List<String> activityIds) {
//        String tenantId = config.getGlobalTenantId();
//        ActivityFacilitySearchCriteria criteria = ActivityFacilitySearchCriteria.builder().activityId(activityIds).isActive(true).tenantId(tenantId).build();
//        ActivityFacilitySearchRequest assignmentSearchRequest = ActivityFacilitySearchRequest.builder().criteria(criteria).requestInfo(requestInfo).build();
//        String url = config.getFieldPlanActivityServiceHost() + config.getFieldPlanActivityFacilitySearchUrl()+ "?tenantId="+tenantId+"&offset=0&limit=100";
//        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), assignmentSearchRequest);
//        ActivityFacilityResponse activityAssignmentList = mapper.convertValue(response, ActivityFacilityResponse.class);
//        if(activityAssignmentList != null && activityAssignmentList.getActivityFacilities() !=null){
//            return activityAssignmentList.getActivityFacilities();
//        }
//        return null;
//    }

}
