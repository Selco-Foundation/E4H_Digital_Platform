package org.egov.amc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.repository.ScheduledVisitRepository;
import org.egov.amc.service.enrichment.ScheduledVisitEnrichment;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.util.MDMSUtils;
import org.egov.amc.validator.ScheduledVisitValidator;
import org.egov.amc.web.models.*;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.user.OtpValidationRequest;
import org.egov.common.producer.Producer;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.egov.amc.util.AmcConstants.*;

@Service
@Slf4j
public class ScheduledVisitService {

    private final ScheduledVisitValidator scheduledVisitsValidator;
    private final ScheduledVisitRepository scheduledVisitsRepository;
    private final ServiceRequestRepository requestRepository;
    private final Producer producer;
    private final ScheduledVisitEnrichment scheduledVisitsEnrichment;
    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;
    private final AMCServiceConfiguration amcServiceConfiguration;
    private final AmcConfigurationService amcConfigurationService;
    private final VisitWorkflowService workflowService;
    private final JdbcTemplate jdbcTemplate;
    private final MDMSUtils mdmsUtils;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public ScheduledVisitService(
            ScheduledVisitRepository scheduledVisitsRepository, ScheduledVisitValidator scheduledVisitsValidator, ServiceRequestRepository requestRepository, ScheduledVisitEnrichment scheduledVisitsEnrichment, AMCServiceConfiguration scheduledVisitsConfiguration,
            Producer producer, AmcConfigurationServiceUtil scheduledVisitsServiceUtil, AmcConfigurationService amcConfigurationService, VisitWorkflowService workflowService, JdbcTemplate jdbcTemplate, MDMSUtils mdmsUtils) {
            this.scheduledVisitsValidator = scheduledVisitsValidator;
        this.requestRepository = requestRepository;
        this.producer = producer;
            this.amcServiceConfiguration = scheduledVisitsConfiguration;
            this.scheduledVisitsRepository = scheduledVisitsRepository;
            this.scheduledVisitsEnrichment = scheduledVisitsEnrichment;
            this.amcConfigurationServiceUtil = scheduledVisitsServiceUtil;
        this.amcConfigurationService = amcConfigurationService;
        this.workflowService = workflowService;
        this.jdbcTemplate = jdbcTemplate;
        this.mdmsUtils = mdmsUtils;
    }

    public ScheduledVisitRequest createScheduledVisit(ScheduledVisitRequest request) {
        scheduledVisitsValidator.validateCreateScheduledVisitRequest(request);
        for (ScheduledVisit scheduledVisit : request.getScheduledVisits()) {
            ScheduledVisitSearchCriteria searchCriteria = ScheduledVisitSearchCriteria.builder()
                    .tenantId(scheduledVisit.getTenantId())
                    .amcConfigurationIds(List.of(scheduledVisit.getAmcConfigurationId()))
                    .visitNumbers(List.of(scheduledVisit.getVisitNumber()))
                    .build();
            ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder()
                    .RequestInfo(request.getRequestInfo())
                    .searchCriteria(searchCriteria)
                    .build();
            List<ScheduledVisit> scheduledVisits = searchScheduledVisit(searchRequest, 100, 0, scheduledVisit.getTenantId(), null, null);
            if (scheduledVisits !=null && !scheduledVisits.isEmpty()){
                throw new CustomException("CREATE_VISIT_ERROR", "A visit number: "+ scheduledVisit.getVisitNumber()+" already exist for configuration "+scheduledVisit.getAmcConfigurationId());
            }
            scheduledVisitsEnrichment.enrichScheduledVisitOnCreate(scheduledVisit, request.getRequestInfo());
            log.info("Enriched with AMC Ids and AuditDetails {}", scheduledVisit);
            log.info("Pushed to kafka");
        }
        producer.push(amcServiceConfiguration.getSaveScheduledVisitTopic(), request);
        return request;
    }

    public ScheduledVisitResponse generateScheduledVisits(VisitGenerationRequest request) {
        if (request == null)
            throw new CustomException("GENERATE_VISIT_ERROR", "The request is empty");

        if (request.getConfigurationId() == null || request.getConfigurationId().isEmpty())
            throw new CustomException("GENERATE_VISIT_ERROR", "Configuration ID is mandatory");

        log.info("Generating scheduled visits for AMC configuration {}", request.getConfigurationId());
        // Check id configuration ID exist
        AmcConfigurationSearchCriteria criteria = AmcConfigurationSearchCriteria.builder().ids(List.of(request.getConfigurationId())).tenantId(request.getRequestInfo().getUserInfo().getTenantId()).build();
        AmcConfigurationSearchRequest searchRequest = AmcConfigurationSearchRequest.builder().RequestInfo(request.getRequestInfo()).searchCriteria(criteria).build();
        List<AmcConfiguration> amcConfigurationList = amcConfigurationService.searchAmcConfiguration(searchRequest, 10, 0, request.getRequestInfo().getUserInfo().getTenantId(), false, null);
        if(amcConfigurationList==null || amcConfigurationList.isEmpty())
            throw new CustomException("GENERATE_VISIT_ERROR", "The configuration ID: "+ request.getConfigurationId() +" do not exist");

        // Beginning of the scheduling horizon (defaults to configuration start date if not provided)
        // End of the scheduling horizon (defaults to configuration end date if not provided)
        Long startDate, endDate;
        AmcConfiguration amcConfiguration = amcConfigurationList.get(0);
        startDate = (amcConfiguration.getConfigurationStartDate() != null && amcConfiguration.getConfigurationStartDate() != 0) ? amcConfiguration.getConfigurationStartDate() : null ;
        endDate = (amcConfiguration.getConfigurationEndDate() != null && amcConfiguration.getConfigurationEndDate() != 0) ? amcConfiguration.getConfigurationEndDate() : null ;
        if(request.getGenerationStartDate() != null && request.getGenerationStartDate() != 0)
            startDate = request.getGenerationStartDate();
        if(request.getGenerationEndDate() != null && request.getGenerationEndDate() != 0)
            endDate = request.getGenerationStartDate();

        // Generate scheduled visit based on startDate and Frequency
        List<Long> generateAmcVisits = amcConfigurationServiceUtil.generateAmcVisits(startDate, endDate, amcConfiguration.getVisitFrequencyMonths());
        if (generateAmcVisits ==null || generateAmcVisits.isEmpty())
            throw new CustomException("GENERATE_VISIT_ERROR", "Cannot generate scheduled visit for this configuration");

        List<ScheduledVisit> scheduledVisitList = new ArrayList<>();
        Long previousVisitDate = null;
        int i =1;
        for (Long visitDate : generateAmcVisits){
            List<ScheduledVisitAssignment> assignments = amcConfiguration.getAssignments().stream()
                            .map(a -> ScheduledVisitAssignment.builder()
                                    .tenantId(amcConfiguration.getTenantId())
                                    .assignedUser(a.getAssignedUser())
                                    .build())
                            .toList();
            ScheduledVisit visit = ScheduledVisit.builder()
                    .tenantId(amcConfiguration.getTenantId())
                    .amcConfigurationId(amcConfiguration.getId())
                    .projectId(amcConfiguration.getProjectId())
                    .lastVisitDate(previousVisitDate)
                    .facilityId(amcConfiguration.getFacilityId())
                    .visitNumber(i)
                    .scheduledDate(visitDate)
                    .assignments(assignments)
                    .status("DRAFT")
                    .build();

            scheduledVisitList.add(visit);
            previousVisitDate = visitDate;
            i++;
        }

        ScheduledVisitRequest scheduledVisitRequest = ScheduledVisitRequest.builder().requestInfo(request.getRequestInfo()).scheduledVisits(scheduledVisitList).build();
        ScheduledVisitRequest response = createScheduledVisit(scheduledVisitRequest);

        return ScheduledVisitResponse.builder()
                .scheduledVisits(response.getScheduledVisits())
                .totalCount(response.getScheduledVisits().size())
                .build();
    }

    public OtpResponse resendOTP(ResendOTPRequest request) {
        if(request.getRequestInfo()==null || request.getRequestInfo().getUserInfo() ==null || request.getRequestInfo().getUserInfo().getUuid().isEmpty())
            throw new CustomException("GENERATE_TOKEN", "User ID is not found in requestInfo");

        Employee employee =  getUserById(request, request.getRequestInfo().getUserInfo().getUuid());
        if (employee !=null && employee.getUser() !=null && employee.getUser().getMobileNumber()!=null && !employee.getUser().getMobileNumber().isEmpty()){
            OtpResponse otpResponse = createOTP(employee.getUser().getMobileNumber(), request.getRequestInfo().getUserInfo().getTenantId());
            if (otpResponse !=null && otpResponse.getOtp()!=null){
                log.info("OTP {} generated for this mobile number {}", otpResponse.getOtp().getOtp(), employee.getUser().getMobileNumber());
                return  otpResponse;
            }
            else
                throw new CustomException("GENERATE_TOKEN", "Error occured while generating OTP");
        }
        else
            throw new CustomException("GENERATE_TOKEN", "User in requestInfo not found");
    }

    public List<ScheduledVisit> updateVisitWorkflow(VisitReportSubmissionRequest request) throws Exception {
        if(request.getRequestInfo()==null || request.getRequestInfo().getUserInfo() ==null || request.getRequestInfo().getUserInfo().getUuid().isEmpty())
            throw new CustomException("UPDATE_WORKFLOW", "User ID is not found in requestInfo");

        // 1. Fetch the existing visit
        ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder().ids(List.of(request.getVisitId())).tenantId(request.getRequestInfo().getUserInfo().getTenantId()).build();
        ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder().RequestInfo(request.getRequestInfo()).searchCriteria(criteria).build();
        List<ScheduledVisit> scheduledVisitsList = searchScheduledVisit(searchRequest, 10, 0, request.getRequestInfo().getUserInfo().getTenantId(), false, null);
        if(scheduledVisitsList==null || scheduledVisitsList.isEmpty())
            throw new CustomException("GENERATE_VISIT_ERROR", "The Visit ID: "+ request.getVisitId() +" is not found");

        ScheduledVisit existingVisit = scheduledVisitsList.get(0);

        // Step 2: if action is SUBMIT_VISIT_REPORT, check if send OTP is successful or not
        if ("SUBMIT_VISIT_REPORT".equalsIgnoreCase(request.getWorkflow().getAction())) {
            // We need to update visit report on existing visit
            existingVisit.setVisitReport(request.getVisitReport());
            // We need to send OTP to AMC_FIELD_STAFF
            Employee employee =  getUserById(request, request.getRequestInfo().getUserInfo().getUuid());
            if (employee !=null && employee.getUser() !=null && employee.getUser().getMobileNumber()!=null && !employee.getUser().getMobileNumber().isEmpty()){
                OtpResponse otpResponse = createOTP(employee.getUser().getMobileNumber(), existingVisit.getTenantId());
                if (otpResponse !=null && otpResponse.getOtp()!=null){
                    log.info("OTP {} generated for this mobile number {}", otpResponse.getOtp().getOtp(), employee.getUser().getMobileNumber());
                    existingVisit.getVisitReport().setOtpReference(otpResponse.getOtp().getOtp());
                }
                else {
                    log.warn("OTP generation returned null response for visit: {}", existingVisit.getId());
                }
            }
            else
                log.warn("Cannot send OTP - employee or mobile number not found for user ID: {}", request.getRequestInfo().getUserInfo().getUuid());
        }

        // if action is SUBMIT_OTP, check if OTP verification is working fine or not
        if ("SUBMIT_OTP".equalsIgnoreCase(request.getWorkflow().getAction())) {
            // We need to validate OTP to AMC_FIELD_STAFF
            if (request.getVisitReport() == null || request.getVisitReport().getOtpReference() == null) {
                throw new CustomException("INVALID_OTP_REQUEST", "Visit report with OTP reference is required for SUBMIT_OTP action");
            }
             if (existingVisit.getVisitReport() == null) {
                 throw new CustomException("INVALID_VISIT_STATE", "Visit report not found on existing visit");
             }
            Employee employee =  getUserById(request, request.getRequestInfo().getUserInfo().getUuid());
            if (employee !=null && employee.getUser() !=null && employee.getUser().getMobileNumber()!=null && !employee.getUser().getMobileNumber().isEmpty()){
                OtpResponse otpResponse = validateOTP(employee.getUser().getMobileNumber(), existingVisit.getTenantId(), request.getVisitReport().getOtpReference());
                if (otpResponse !=null && otpResponse.getOtp()!=null){
                    log.info("OTP {} validated for this mobile number {}", otpResponse.getOtp().getOtp(), employee.getUser().getMobileNumber());
                    // We need to update visit report on existing visit after validation
                    existingVisit.getVisitReport().setOtpVerifiedAt(new Timestamp(System.currentTimeMillis()).getTime());
                }
            }
        }

        // 3. Call workflow transition
        ProcessInstance updatedWorkflow;
        try {
            updatedWorkflow = workflowService.transitionWorkflow(
                    existingVisit,
                    request.getWorkflow().getAction(),
                    request.getWorkflow().getDocuments(),
                    request.getRequestInfo(),
                    request.getWorkflow().getComments()
            );
        } catch (Exception e) {
//            e.printStackTrace();
            log.error(e.getMessage());
            throw new CustomException("WORKFLOW_TRANSITION_FAILED",
                    "Failed to transition workflow for facility: " + request.getVisitId());
        }

        if(request.getVisitReport() != null) {
            handleTransactions(request, updatedWorkflow);
        }

        // 4. Inject workflow status into activity facility
        existingVisit.setStatus(updatedWorkflow.getState().getState());

        // 5. Create a new Visit Instance instance with enriched additionalDetails
        ScheduledVisit updatedScheduledVisit = ScheduledVisit.builder()
                .id(existingVisit.getId())
                .tenantId(existingVisit.getTenantId())
                .amcConfigurationId(existingVisit.getAmcConfigurationId())
                .facilityId(existingVisit.getFacilityId())
                .visitNumber(existingVisit.getVisitNumber())
                .status(existingVisit.getStatus())
                .scheduledDate(existingVisit.getScheduledDate())
                .actualVisitDate(existingVisit.getActualVisitDate())
                .visitReport(existingVisit.getVisitReport())
                .additionalDetails(existingVisit.getAdditionalDetails())
                .assignments(existingVisit.getAssignments())
                .build();

        // 6. Create Schedule visit request wrapper
        ScheduledVisitRequest enrichedRequest = ScheduledVisitRequest.builder()
                .requestInfo(request.getRequestInfo())
                .scheduledVisits(List.of(updatedScheduledVisit))
                .build();

        // 7. Perform enriched update using standard handler
        handleUpdateScheduledVisit(enrichedRequest, updatedScheduledVisit, existingVisit);

        return List.of(updatedScheduledVisit);
    }

    private void handleTransactions(VisitReportSubmissionRequest request, ProcessInstance updatedWorkflow) {
        Transaction transaction = new Transaction();
        transaction.setProcessInstanceId(updatedWorkflow.getId());
        transaction.setVisitReport(request.getVisitReport());
        String userUUID = request.getRequestInfo().getUserInfo().getUuid();
        transaction.setVisitId(request.getVisitId());
        transaction.setAuditDetails(amcConfigurationServiceUtil.getAuditDetails(userUUID, null, true));
        if(transaction.getTransactionId() == null || transaction.getTransactionId().isEmpty()) {
            transaction.setTransactionId(UUID.randomUUID().toString());
        }

        producer.push(amcServiceConfiguration.getTransactionPersistTopic(), new TransactionRequest(List.of(transaction)));
    }

    public ScheduledVisitRequest updateScheduledVisit(ScheduledVisitRequest request) {
        /*
         * Validate the update scheduledVisits request
         */
        scheduledVisitsValidator.validateUpdateScheduledVisitRequest(request);
        log.info("Update asset_amc request validated");

        /*
         * Search for asset_amc based on asset_amc IDs provided in the request
         */
        List<ScheduledVisit> scheduleVisitFromDB = searchScheduledVisit(
                getSearchScheduledVisitRequest(request.getScheduledVisits(), request.getRequestInfo()),
                amcServiceConfiguration.getMaxLimit(), amcServiceConfiguration.getDefaultOffset(),
                request.getScheduledVisits().get(0).getTenantId(), false, null);
        log.info("Fetched scheduledVisits for update request");

        /*
         * Validate the update asset_amc request against the asset_amcs fetched from the database
         */
        scheduledVisitsValidator.validateUpdateAgainstDB(request.getScheduledVisits(), scheduleVisitFromDB);

        /*
         * Process each scheduledVisits in the update request
         */
        for (ScheduledVisit scheduledVisit : request.getScheduledVisits()) {
            processScheduledVisitUpdate(request, scheduledVisit, scheduleVisitFromDB);
        }

        return request;
    }

    public List<Transaction> getTransactionsForVisit(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) return Collections.emptyList();

        String sql = "SELECT id, visit_id, process_instance_id, visit_report, created_by, last_modified_by, created_time, last_modified_time " +
                "FROM visit_transaction WHERE visit_id = ANY(?)";

        return jdbcTemplate.query(sql, ps -> {
            Array sqlArray = ps.getConnection().createArrayOf("text", projectIds.toArray(new String[0]));
            ps.setArray(1, sqlArray);
        }, (rs, rowNum) -> {
            Transaction transaction = new Transaction();
            transaction.setTransactionId(rs.getString("id"));
            transaction.setVisitId(rs.getString("visit_id"));
            transaction.setProcessInstanceId(rs.getString("process_instance_id"));
            try {
                transaction.setVisitReport(mapper.readValue(rs.getString("visit_report"), VisitReport.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            AuditDetails auditDetails = new AuditDetails();
            auditDetails.setCreatedBy(rs.getString("created_by"));
            auditDetails.setLastModifiedBy(rs.getString("last_modified_by"));
            auditDetails.setCreatedTime(rs.getLong("created_time"));
            auditDetails.setLastModifiedTime(rs.getLong("last_modified_time"));
            transaction.setAuditDetails(auditDetails);
            return transaction;
        });
    }

    public Integer countAllScheduledVisits(ScheduledVisitSearchRequest request, String tenantId, Long lastChangedSince, Boolean includeDeleted) {
        return scheduledVisitsRepository.getScheduledVisitCount(request, tenantId, lastChangedSince, includeDeleted);
    }

    /* Construct ScheduledVisit Request object for search which contains asset_amc id and tenantId */
    private ScheduledVisitSearchRequest getSearchScheduledVisitRequest(List<ScheduledVisit> scheduleVisit, RequestInfo requestInfo) {
        List<String> scheduledVisitsIds = scheduleVisit.stream().map(ScheduledVisit::getId).toList();
        ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder().ids(scheduledVisitsIds).tenantId(scheduleVisit.get(0).getTenantId()).build();
        return ScheduledVisitSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();
    }

    public List<ScheduledVisit> searchScheduledVisit(ScheduledVisitSearchRequest request, Integer limit, Integer offset, String tenantId, Boolean includeDeleted, Long lastChangedSince) {
        scheduledVisitsValidator.validateSearchScheduledVisitRequest(request, limit, offset, tenantId);
        List<ScheduledVisit> amcConfigurationList = scheduledVisitsRepository.getScheduledVisit(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        return amcConfigurationList;
    }

    private void processScheduledVisitUpdate(ScheduledVisitRequest request, ScheduledVisit visit, List<ScheduledVisit> scheduleVisitFromDB) {
        /*
         * Convert asset_amc ID to string for comparison
         */
        String scheduledVisitsId = String.valueOf(visit.getId());

        /*
         * Find the scheduledVisits from the database that matches the current scheduledVisits ID
         */
        ScheduledVisit scheduledVisit = findScheduledVisitById(scheduledVisitsId, scheduleVisitFromDB);

        if (scheduledVisit != null) {
            /*
             * Merge additional details of the scheduledVisits from the request and scheduledVisits from DB
             */
            amcConfigurationServiceUtil.mergeScheduledVisitAdditionalDetails(visit, scheduledVisit);

            // Check if visit needs to be scheduled based on notice period
            checkAndScheduleVisitIfNeeded(visit, request.getRequestInfo());

            handleUpdateScheduledVisit(request, visit, scheduledVisit);
        }
    }


    private void handleUpdateScheduledVisit(ScheduledVisitRequest request, ScheduledVisit scheduledVisits, ScheduledVisit scheduledVisitsFromDB) {
        /*
         * Save original values of start date, end date, and additional details
         */
        AuditDetails originalAuditDetails = scheduledVisitsFromDB.getAuditDetails();


        /*
         * Update the scheduledVisits with new start date, end date, and additional details
         */
        scheduledVisitsFromDB.setAuditDetails(scheduledVisits.getAuditDetails());

        /*
         * Ensure that no other properties are being updated besides the start and end dates
         */
        if (!isValidCascadingUpdate(scheduledVisitsFromDB, scheduledVisits)) {
            throw new CustomException(
                    "AMC_UPDATE_ERROR",
                    "Can only update scheduled visit dates, status, assignment, visit report and additional details"
            );
        }

        /*
         * Restore original values of start date, end date, and additional details
         */
        scheduledVisitsFromDB.setAuditDetails(originalAuditDetails);

        /*
         * Update lastModifiedTime and lastModifiedBy for the scheduledVisits
         */
        scheduledVisitsEnrichment.enrichScheduledVisitRequestOnUpdate(scheduledVisits, scheduledVisitsFromDB, request.getRequestInfo());

        /*
         * Check and enrich cascading scheduledVisits dates and push the update to the message broker
         */
        producer.push(amcServiceConfiguration.getUpdateScheduledVisitTopic(), request);
    }

    private boolean isValidCascadingUpdate(ScheduledVisit scheduledVisitsFromDB, ScheduledVisit scheduledVisits) {
        // Check if only allowed fields are being updated
        return Objects.equals(scheduledVisitsFromDB.getId(), scheduledVisits.getId()) &&
                Objects.equals(scheduledVisitsFromDB.getTenantId(), scheduledVisits.getTenantId()) &&
                Objects.equals(scheduledVisitsFromDB.getAmcConfigurationId(), scheduledVisits.getAmcConfigurationId()) &&
                Objects.equals(scheduledVisitsFromDB.getFacilityId(), scheduledVisits.getFacilityId());
        // Note: We allow startDate, endDate, vendorId, geographyDetails, activities and auditDetails to be different
    }

    private ScheduledVisit findScheduledVisitById(String scheduledVisitsId, List<ScheduledVisit> scheduleVisitFromDB) {
        /*
         * Find and return the scheduledVisits with the matching ID from the list of asset_amc fetched from the database
         */
        return scheduleVisitFromDB.stream()
                .filter(p -> scheduledVisitsId.equals(String.valueOf(p.getId())))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if visit is nearing scheduled date and apply SCHEDULE action if needed
     * This checks MDMS for notice period (amc.AMCThresholds.amc_visit_notice_period_in_days)
     * and applies workflow action if scheduled_date < current_date + notice_period
     */
    private void checkAndScheduleVisitIfNeeded(ScheduledVisit visit, RequestInfo requestInfo) {
        // Only process DRAFT visits
        if (visit.getStatus() == null || !visit.getStatus().equals("DRAFT")) {
            return;
        }

        try {
            // Fetch notice period from MDMS
            AmcConfigurationRequest mdmsRequest = AmcConfigurationRequest.builder()
                    .requestInfo(requestInfo)
                    .amcConfigurations(new ArrayList<>())
                    .build();
            Object mdmsData = mdmsUtils.mDMSCall(mdmsRequest, visit.getTenantId());
            Integer noticePeriod = parseNoticePeriodFromMDMS(mdmsData, visit.getTenantId());
            
            if (noticePeriod == null) {
                log.warn("Could not fetch notice period from MDMS for tenant: {}. Skipping auto-schedule check.", visit.getTenantId());
                return;
            }

            // Calculate threshold date: current_date + notice_period
            long currentTimeMillis = System.currentTimeMillis();
            LocalDate currentDate = Instant.ofEpochMilli(currentTimeMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate thresholdDate = currentDate.plusDays(noticePeriod);
            long thresholdDateMillis = thresholdDate.atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            // Check if scheduled_date < threshold_date
            if (visit.getScheduledDate() != null && visit.getScheduledDate() < thresholdDateMillis) {
                log.info("Visit {} is nearing scheduled date (scheduled: {}, threshold: {}). Applying SCHEDULE action.", 
                        visit.getId(), visit.getScheduledDate(), thresholdDateMillis);
                
                try {
                    // This updates the workflow state and returns the new ProcessInstance
                    ProcessInstance updatedWorkflow = workflowService.transitionWorkflow(
                            visit,
                            "SCHEDULE",
                            null,
                            requestInfo,
                            "Automatically scheduled by daily cron job - visit nearing scheduled date"
                    );
                    
                    // Update the visit status directly from the workflow response
                    if (updatedWorkflow != null && updatedWorkflow.getState() != null) {
                        visit.setStatus(updatedWorkflow.getState().getState());
                        log.info("Successfully applied SCHEDULE action on visit: {}. New status: {}", 
                                visit.getId(), visit.getStatus());
                    } else {
                        log.warn("Workflow transition succeeded but returned null state for visit: {}", visit.getId());
                    }
                } catch (Exception e) {
                    log.error("Error applying SCHEDULE workflow action on visit: {}", visit.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error checking if visit needs scheduling: {}", visit.getId(), e);
        }
    }

    /**
     * Parse MDMS response to extract notice period from AMCThresholds
     */
    private Integer parseNoticePeriodFromMDMS(Object mdmsData, String tenantId) {
        if (mdmsData == null) {
            log.warn("MDMS response is null for tenant: {}", tenantId);
            return null;
        }

        try {
            LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) mdmsData;
            LinkedHashMap<String, Object> mdmsRes = (LinkedHashMap<String, Object>) responseMap.get("MdmsRes");

            if (mdmsRes == null) {
                log.warn("MdmsRes not found in MDMS response for tenant: {}", tenantId);
                return null;
            }

            LinkedHashMap<String, Object> amcModule = (LinkedHashMap<String, Object>) mdmsRes.get(MDMS_AMC_MODULE_NAME);

            if (amcModule == null) {
                log.warn("AMC module not found in MDMS response for tenant: {}", tenantId);
                return null;
            }
            List<LinkedHashMap<String, Object>> thresholds = (List<LinkedHashMap<String, Object>>) amcModule.get(MDMS_AMC_THRESHOLD_MODULE_NAME);

            if (thresholds == null || thresholds.isEmpty()) {
                log.warn("AMCThresholds not found in MDMS response for tenant: {}", tenantId);
                return null;
            }

            // Find the first active threshold (status == "active")
            LinkedHashMap<String, Object> activeThreshold = null;
            for (LinkedHashMap<String, Object> threshold : thresholds) {
                Object status = threshold.get("status");
                if ("active".equals(status)) {
                    activeThreshold = threshold;
                    break;
                }
            }

            if (activeThreshold == null) {
                log.warn("No active AMCThresholds found in MDMS response for tenant: {}", tenantId);
                return null;
            }

            Object noticePeriodObj = activeThreshold.get("amc_visit_notice_period_in_days");

            if (noticePeriodObj == null) {
                log.warn("amc_visit_notice_period_in_days not found in AMCThresholds for tenant: {}", tenantId);
                return null;
            }

            Integer noticePeriod = null;
            if (noticePeriodObj instanceof Integer) {
                noticePeriod = (Integer) noticePeriodObj;
            } else if (noticePeriodObj instanceof Number) {
                noticePeriod = ((Number) noticePeriodObj).intValue();
            }

            log.info("Fetched notice period from MDMS for tenant {}: {} days", tenantId, noticePeriod);
            return noticePeriod;

        } catch (Exception e) {
            log.error("Error parsing AMCThresholds from MDMS response for tenant: {}", tenantId, e);
            return null;
        }
    }

    public List<ProcessInstance> getProcessInstanceById(String businessId, String tenantId, RequestInfo requestInfo) {
        return workflowService.getProcessInstanceById(businessId, tenantId, requestInfo);
    }

    public OtpResponse createOTP(String identity, String tenantId) {
        String url = amcServiceConfiguration.getOtpServiceHost() + amcServiceConfiguration.getOtpServiceCreateUrl();
        Otp otp = Otp.builder()
                .tenantId(tenantId)
                .identity(identity)
                .build();
        OtpRequest request = OtpRequest.builder()
                .otp(otp)
                .build();
        Object response = requestRepository.fetchResult(new StringBuilder(url), request);
        OtpResponse otpResponse = mapper.convertValue(response,OtpResponse.class);
        if(otpResponse == null){
            throw new CustomException(
                    "ERROR_OTP_GENERATION",
                    "Error occured while creating OTP"
            );
        }
        return otpResponse;
    }

    public OtpResponse validateOTP(String identity, String tenantId, String otpCode) {
        String url = amcServiceConfiguration.getOtpServiceHost() + amcServiceConfiguration.getOtpServiceValidateUrl();
        Otp otp = Otp.builder()
                .tenantId(tenantId)
                .identity(identity)
                .otp(otpCode)
                .build();
        OtpValidateRequest request = OtpValidateRequest.builder()
                .otp(otp)
                .build();
        Object response = requestRepository.fetchResult(new StringBuilder(url), request);
        OtpResponse otpResponse = mapper.convertValue(response,OtpResponse.class);
        if(otpResponse == null){
            throw new CustomException(
                    "ERROR_OTP_GENERATION",
                    "OTP validation unsuccessful"
            );
        }
        return otpResponse;
    }

    public Employee getUserById(Object request, String userId) {

        String url = amcServiceConfiguration.getHrmsHost() + amcServiceConfiguration.getHrmsSearchUrl()+ "?tenantId=in&uuids="+userId;
        Object response = requestRepository.fetchResult(new StringBuilder(url), request);

        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            throw new CustomException("EMPLOYEE_NOT_FOUND", "Employee not found with ID: " + userId);
        }
        return employeeResponse.getEmployees().get(0);
    }

}
