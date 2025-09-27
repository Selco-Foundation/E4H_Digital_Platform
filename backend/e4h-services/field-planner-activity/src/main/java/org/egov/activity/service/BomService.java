package org.egov.activity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.repository.BomRepository;
import org.egov.activity.service.enrichment.BomEnrichment;
import org.egov.activity.util.ActivityServiceUtil;
import org.egov.activity.util.MDMSUtils;
import org.egov.activity.validator.BomValidator;
import org.egov.activity.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class BomService {

    private final BomRepository bomRepository;

    private final Producer producer;

    private final ActivityServiceUtil activityServiceUtil;
    private final BomEnrichment bomEnrichment;

    private final BomValidator bomValidator;

    private final ActivityConfiguration activityConfiguration;
    private final MDMSUtils mdmsUtils;

    private ServiceRequestRepository serviceRequest;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    @Autowired
    public BomService(
            BomRepository bomRepository, BomEnrichment bomEnrichment, ActivityConfiguration activityConfiguration, BomValidator bomValidator, ServiceRequestRepository serviceRequest,
            Producer producer, MDMSUtils mdmsUtils, ActivityServiceUtil activityServiceUtil, @Qualifier("objectMapper") ObjectMapper mapper) {
            this.producer = producer;
            this.activityConfiguration = activityConfiguration;
            this.bomRepository = bomRepository;
            this.bomEnrichment = bomEnrichment;
            this.mdmsUtils = mdmsUtils;
            this.activityServiceUtil = activityServiceUtil;
            this.mapper = mapper;
            this.bomValidator = bomValidator;
            this.serviceRequest = serviceRequest;
    }

    public List<BillOfMaterial> createBillOfMaterial(BomBulkRequest request) {
        log.info("received request to create bulk fieldplan facility");

        bomValidator.validateCreateBomRequest(request);
        List<BillOfMaterial> billOfMaterials = request.getBillOfMaterials();
        try {
            for (BillOfMaterial billOfMaterial : billOfMaterials) {
                log.info("processing {} valid entities", billOfMaterial);
                bomEnrichment.enrichBomOnCreate(billOfMaterial, request.getRequestInfo());
            }
            producer.push(activityConfiguration.getCreateBOMTopic(), request);
            log.info("successfully created activity facility");
        } catch (Exception exception) {
            log.error("error occurred while creating project facility: {}", ExceptionUtils.getStackTrace(exception));
        }

        return billOfMaterials;
    }

    public List<BillOfMaterial> searchBillOfMaterials(BomSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        bomValidator.validateSearchBOMRequest(request, limit, offset, tenantId);
        List<BillOfMaterial> activityFacilities = bomRepository.getBillOfMaterials(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        return activityFacilities;
    }

    public Integer countAllFieldPlans(BomSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return bomRepository.getBillOfMatrialsCount(request, tenantId, lastChangedSince, includeDeleted);
    }

    public BomBulkRequest updateBillOfMaterials(BomBulkRequest request) {
        /*
         * Validate the update activity request
         */
        bomValidator.validateCreateBomRequest(request);
        log.info("Update activity facility request validated");

        /*
         * Search for fieldplan based on fieldplan IDs provided in the request
         */
        List<BillOfMaterial> bomListFromDB = searchBillOfMaterials(
                getSearchBOMRequest(request.getBillOfMaterials(), request.getRequestInfo()),
                activityConfiguration.getMaxLimit(), activityConfiguration.getDefaultOffset(),
                request.getBillOfMaterials().get(0).getTenantId(), false, null);
        log.info("Fetched activities for update request");

        /*
         * Validate the update fieldplan request against the fieldplans fetched from the database
         */
        bomValidator.validateUpdateAgainstDB(request.getBillOfMaterials(), bomListFromDB);

        /*
         * Process each project in the update request
         */
        for (BillOfMaterial billOfMaterial : request.getBillOfMaterials()) {
            processBOMUpdate(request, billOfMaterial, bomListFromDB);
        }

        return request;
    }

    public void generateBOMPdf(GenerateBOMPdfRequest request, String key, String tenantId){
        getBOMPdfFile(key, tenantId, request);
    }

    private BomSearchRequest getSearchBOMRequest(List<BillOfMaterial> billOfMaterials, RequestInfo requestInfo) {
        List<String> activityFacilityIds = billOfMaterials.stream().map(BillOfMaterial::getId).toList();
        BomSearchCriteria criteria = BomSearchCriteria.builder().ids(activityFacilityIds).tenantId(billOfMaterials.get(0).getTenantId()).build();
        return BomSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(criteria)
                .build();
    }

    private void processBOMUpdate(BomBulkRequest request, BillOfMaterial billOfMaterial, List<BillOfMaterial> bomListFromDB) {
        /*
         * Convert activity facility ID to string for comparison
         */
        String bomId = String.valueOf(billOfMaterial.getId());

        /*
         * Find the activity from the database that matches the current project ID
         */
        BillOfMaterial bomFromDB = findBOMById(bomId, bomListFromDB);

        if (bomFromDB != null) {
            /*
             * Merge additional details of the project from the request and project from DB
             */
            activityServiceUtil.mergeBOMAdditionalDetails(billOfMaterial, bomFromDB);

            handleUpdateBOM(request, billOfMaterial, bomFromDB);

        }
    }

    private void handleUpdateBOM(BomBulkRequest request, BillOfMaterial billOfMaterial, BillOfMaterial bomFromDB) {

        /*
         * Ensure that no other properties are being updated besides the start and end dates
         */
        if (!isValidCascadingUpdate(bomFromDB, billOfMaterial)) {
            throw new CustomException(
                    "ACTIVITY_CASCADE_UPDATE_ERROR",
                    "Can only update Activity facility dates, geographyDetails and additional details if cascade FieldPlan date update true"
            );
        }

        /*
         * Update lastModifiedTime and lastModifiedBy for the activity
         */
        bomEnrichment.enrichFieldPlanRequestOnUpdate(billOfMaterial, bomFromDB, request.getRequestInfo());

        /*
         * Check and enrich cascading project dates and push the update to the message broker
         */
        producer.push(activityConfiguration.getUpdateBOMTopic(), request);
    }

    private boolean isValidCascadingUpdate(BillOfMaterial bomFromDB, BillOfMaterial billOfMaterial) {
        // Check if only allowed fields are being updated
        return Objects.equals(bomFromDB.getId(), billOfMaterial.getId()) &&
                Objects.equals(bomFromDB.getTenantId(), billOfMaterial.getTenantId()) &&
                Objects.equals(bomFromDB.getFacilityId(), billOfMaterial.getFacilityId());
        // Note: We allow assignedUser, data, active, additionalDetails to be different
    }

    private BillOfMaterial findBOMById(String bomId, List<BillOfMaterial> bomListFromDB) {
        /*
         * Find and return the activity with the matching ID from the list of activity fetched from the database
         */
        return bomListFromDB.stream()
                .filter(p -> bomId.equals(String.valueOf(p.getId())))
                .findFirst()
                .orElse(null);
    }

    public Facility getBOMPdfFile(String key, String tenantId, GenerateBOMPdfRequest request) {

        String url = activityConfiguration.getPdfServiceHost() + activityConfiguration.getPdfCreateNoSaveUrl()+ "?key="+key+"&tenantId="+tenantId;
        Object response = serviceRequest.fetchResult(new StringBuilder(url), request);

//        FacilitySearchResponse facilityList = mapper.convertValue(response, FacilitySearchResponse.class);
//        if(facilityList != null && facilityList.getFacilities() !=null && facilityList.getFacilities().size() > 0){
//            return facilityList.getFacilities().get(0);
//        }
        return null;
    }


}
