package org.egov.amc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.repository.ScheduledVisitRepository;
import org.egov.amc.service.enrichment.ScheduledVisitEnrichment;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.util.BoundaryUtil;
import org.egov.amc.util.FacilityPocPhoneUtil;
import org.egov.amc.util.LocalizationUtil;
import org.egov.amc.util.MDMSUtils;
import org.egov.amc.validator.ScheduledVisitValidator;
import org.egov.amc.web.models.*;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.sql.Array;
import java.sql.Timestamp;
import java.nio.charset.StandardCharsets;
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
    private BoundaryUtil boundaryUtil;
    private final FacilityPocPhoneUtil facilityPocPhoneUtil;
    private final LocalizationUtil localizationUtil;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public ScheduledVisitService(
            ScheduledVisitRepository scheduledVisitsRepository, ScheduledVisitValidator scheduledVisitsValidator, ServiceRequestRepository requestRepository, ScheduledVisitEnrichment scheduledVisitsEnrichment, AMCServiceConfiguration scheduledVisitsConfiguration,
            Producer producer, AmcConfigurationServiceUtil scheduledVisitsServiceUtil, AmcConfigurationService amcConfigurationService, VisitWorkflowService workflowService, JdbcTemplate jdbcTemplate, MDMSUtils mdmsUtils, BoundaryUtil boundaryUtil,
            FacilityPocPhoneUtil facilityPocPhoneUtil, LocalizationUtil localizationUtil) {
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
        this.boundaryUtil = boundaryUtil;
        this.facilityPocPhoneUtil = facilityPocPhoneUtil;
        this.localizationUtil = localizationUtil;
    }

    public ScheduledVisitRequest createScheduledVisit(ScheduledVisitRequest request) {
        scheduledVisitsValidator.validateCreateScheduledVisitRequest(request);
        for (ScheduledVisit scheduledVisit : request.getScheduledVisits()) {
            // Avoid creating two visits with same visit number
            ScheduledVisitSearchCriteria searchCriteria = ScheduledVisitSearchCriteria.builder()
                    .tenantId(scheduledVisit.getTenantId())
                    .amcConfigurationIds(List.of(scheduledVisit.getAmcConfigurationId()))
                    .visitNumbers(List.of(scheduledVisit.getVisitNumber()))
                    .build();
            ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder()
                    .RequestInfo(request.getRequestInfo())
                    .searchCriteria(searchCriteria)
                    .build();
            List<ScheduledVisit> scheduledVisits = searchScheduledVisit(searchRequest, 1, 0, scheduledVisit.getTenantId(), null, null);
            if (scheduledVisits !=null && !scheduledVisits.isEmpty()){
                throw new CustomException("CREATE_VISIT_ERROR", "A visit number: "+ scheduledVisit.getVisitNumber()+" already exist for configuration "+scheduledVisit.getAmcConfigurationId());
            }

            Facility facility = getFacilityById(scheduledVisit.getFacilityId());
            if (facility == null) {
                throw new CustomException("CREATE_VISIT_ERROR", "Facility not found for facilityId: " + scheduledVisit.getFacilityId());
            }
            scheduledVisit.setFacilityName(facility.getFacilityName());
            scheduledVisit.setFacility(facility);

            // remove Duplicate Assignments
            Set<String> seenUsers = new HashSet<>();
            List<ScheduledVisitAssignment> assignments = scheduledVisit.getAssignments().stream().filter(a -> seenUsers.add(a.getAssignedUser()))
                    .toList();
            scheduledVisit.setAssignments(assignments);
            scheduledVisitsEnrichment.enrichScheduledVisitOnCreate(scheduledVisit, request.getRequestInfo());
            log.info("Enriched with AMC Ids and AuditDetails {}", scheduledVisit);
            log.info("Pushed to kafka");
        }
        producer.push(amcServiceConfiguration.getSaveScheduledVisitTopic(), request);
        pushNonDraftVisitsToIndex(request.getRequestInfo(), request.getScheduledVisits(), amcServiceConfiguration.getSaveScheduledVisitIndexTopic());
        return request;
    }

    /**
     * Index only non-DRAFT visits - the search index should never surface visits that are still being drafted.
     * Returns the number of visits actually pushed (DRAFT visits in the input are silently skipped).
     */
    private int pushNonDraftVisitsToIndex(RequestInfo requestInfo, List<ScheduledVisit> visits, String indexTopic) {
        List<ScheduledVisit> nonDraftVisits = visits.stream()
                .filter(visit -> visit.getStatus() != null && !DRAFT_STATUS.equalsIgnoreCase(visit.getStatus()))
                .toList();
        if (nonDraftVisits.isEmpty()) {
            return 0;
        }
        enrichBoundaryLocalization(requestInfo, nonDraftVisits);
        ScheduledVisitRequest indexRequest = ScheduledVisitRequest.builder()
                .requestInfo(requestInfo)
                .scheduledVisits(nonDraftVisits)
                .build();
        producer.push(indexTopic, indexRequest);
        return nonDraftVisits.size();
    }

    /**
     * Backfills the search index from the DB: pages through every visit for the tenant and pushes the
     * non-DRAFT ones onto the same index topic/pipeline used by create/update/expire. Existing documents
     * with the same id are overwritten (upsert), so this is safe to re-run.
     */
    public int reindexNonDraftVisits(RequestInfo requestInfo, String tenantId) {
        int limit = amcServiceConfiguration.getMaxLimit();
        int offset = 0;
        int totalIndexed = 0;

        while (true) {
            ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder()
                    .tenantId(tenantId)
                    .build();
            ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder()
                    .RequestInfo(requestInfo)
                    .searchCriteria(criteria)
                    .build();
            List<ScheduledVisit> visits = searchScheduledVisit(searchRequest, limit, offset, tenantId, false, null);
            if (visits == null || visits.isEmpty()) {
                break;
            }

            int indexedInBatch = pushNonDraftVisitsToIndex(requestInfo, visits, amcServiceConfiguration.getSaveScheduledVisitIndexTopic());
            totalIndexed += indexedInBatch;
            log.info("Reindex batch offset={} fetched={} indexed={} totalIndexed={}",
                    offset, visits.size(), indexedInBatch, totalIndexed);

            if (visits.size() < limit) {
                break;
            }
            offset += limit;
        }

        log.info("Reindex complete for tenantId={}. Total non-DRAFT visits pushed to index: {}", tenantId, totalIndexed);
        return totalIndexed;
    }

    /**
     * Resolves each visit's facility boundary (state/district/block codes) and merges it into
     * facility.additionalDetails.boundary - matching what searchScheduledVisit already does for search
     * responses - then localizes those codes via egov-localization and sets visit.state/district/block
     * so the index carries the exact "state"/"district"/"block" field names used by other indices.
     */
    private void enrichBoundaryLocalization(RequestInfo requestInfo, List<ScheduledVisit> visits) {
        List<ScheduledVisit> visitsWithBoundaryCode = visits.stream()
                .filter(v -> v.getFacility() != null && v.getFacility().getBoundaryCode() != null)
                .toList();
        if (visitsWithBoundaryCode.isEmpty()) {
            return;
        }

        Map<String, Boundary> boundaryByFacilityCode = boundaryUtil.getBoundaryByCode();
        if (boundaryByFacilityCode == null || boundaryByFacilityCode.isEmpty()) {
            return;
        }

        Set<String> rawBoundaryCodes = new HashSet<>();
        for (ScheduledVisit visit : visitsWithBoundaryCode) {
            Boundary boundary = boundaryByFacilityCode.get(visit.getFacility().getBoundaryCode());
            if (boundary == null) continue;
            Object additionalDetails = visit.getFacility().getAdditionalDetails();
            visit.getFacility().setAdditionalDetails(
                    (Map<String, Object>) mergeListIntoAdditionalDetails(additionalDetails, "boundary", boundary));
            if (boundary.getState() != null) rawBoundaryCodes.add(boundary.getState());
            if (boundary.getDistrict() != null) rawBoundaryCodes.add(boundary.getDistrict());
            if (boundary.getBlock() != null) rawBoundaryCodes.add(boundary.getBlock());
        }
        if (rawBoundaryCodes.isEmpty()) {
            return;
        }

        String tenantId = requestInfo != null && requestInfo.getUserInfo() != null
                ? requestInfo.getUserInfo().getTenantId() : null;
        Map<String, String> labels = localizationUtil.fetchBoundaryLabels(requestInfo, tenantId, rawBoundaryCodes);

        for (ScheduledVisit visit : visitsWithBoundaryCode) {
            Boundary boundary = boundaryByFacilityCode.get(visit.getFacility().getBoundaryCode());
            if (boundary == null) continue;
            visit.setState(labels.getOrDefault(boundary.getState(), boundary.getState()));
            visit.setDistrict(labels.getOrDefault(boundary.getDistrict(), boundary.getDistrict()));
            visit.setBlock(labels.getOrDefault(boundary.getBlock(), boundary.getBlock()));
        }
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
        if(request == null || request.getRequestInfo() == null || request.getRequestInfo().getUserInfo() == null
                || request.getRequestInfo().getUserInfo().getTenantId() == null
                || request.getRequestInfo().getUserInfo().getTenantId().trim().isEmpty())
            throw new CustomException("GENERATE_TOKEN", "Tenant ID is not found in requestInfo");

        if (request.getVisitId() == null || request.getVisitId().trim().isEmpty()) {
            throw new CustomException("GENERATE_TOKEN", "Visit ID is mandatory for resend OTP");
        }

        String tenantId = request.getRequestInfo().getUserInfo().getTenantId();
        log.info("Resend OTP requested for visitId={} tenantId={}", request.getVisitId(), tenantId);

        ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder()
                .ids(List.of(request.getVisitId()))
                .tenantId(tenantId)
                .build();
        ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder()
                .RequestInfo(request.getRequestInfo())
                .searchCriteria(criteria)
                .build();
        List<ScheduledVisit> scheduledVisitsList = searchScheduledVisit(searchRequest, 10, 0, tenantId, false, null);
        if (scheduledVisitsList == null || scheduledVisitsList.isEmpty()) {
            throw new CustomException("GENERATE_TOKEN", "Visit not found with ID: " + request.getVisitId());
        }

        ScheduledVisit existingVisit = scheduledVisitsList.get(0);
        if (existingVisit.getFacilityId() == null || existingVisit.getFacilityId().trim().isEmpty()) {
            log.error("RESEND_OTP failed: facilityId missing for visitId={}", existingVisit.getId());
            throw new CustomException("GENERATE_TOKEN", "Facility ID is missing for visit: " + existingVisit.getId());
        }

        log.debug("Fetching facility for resend OTP visitId={} facilityId={}", existingVisit.getId(), existingVisit.getFacilityId());
        Facility facility = getFacilityById(existingVisit.getFacilityId());
        if (facility == null) {
            log.error("RESEND_OTP failed: facility not found for facilityId={} visitId={}", existingVisit.getFacilityId(), existingVisit.getId());
            throw new CustomException("GENERATE_TOKEN", "Facility not found for facilityId: " + existingVisit.getFacilityId());
        }

        log.info("Resolved HRMS lookup for resend OTP visitId={} facilityId={} boundaryCode={}",
                existingVisit.getId(), existingVisit.getFacilityId(), facility.getBoundaryCode());

        Employee employee = getEmployeeByBoundaryCode(request, facility.getBoundaryCode());
        if (employee != null && employee.getUser() != null && employee.getUser().getMobileNumber() != null && !employee.getUser().getMobileNumber().isEmpty()) {
            OtpResponse otpResponse = createOTP(employee.getUser().getMobileNumber(), existingVisit.getTenantId());
            if (otpResponse != null && otpResponse.getOtp() != null) {
                log.info("OTP {} generated for this mobile number {}", otpResponse.getOtp().getOtp(), employee.getUser().getMobileNumber());
                sendOtpSms(employee.getUser().getMobileNumber(), otpResponse.getOtp().getOtp(), existingVisit.getTenantId());
                return otpResponse;
            } else {
                throw new CustomException("GENERATE_TOKEN", "Error occured while generating OTP");
            }
        } else {
            throw new CustomException("GENERATE_TOKEN", "Employee or mobile number not found for boundaryCode: " + facility.getBoundaryCode());
        }
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
            log.info("SUBMIT_VISIT_REPORT started for visitId={} tenantId={}", existingVisit.getId(), existingVisit.getTenantId());
            // We need to send OTP to facility POC resolved by facility boundary (HCR user)
            if (existingVisit.getFacilityId() == null || existingVisit.getFacilityId().trim().isEmpty()) {
                log.error("SUBMIT_VISIT_REPORT failed: facilityId missing for visitId={}", existingVisit.getId());
                throw new CustomException("UPDATE_WORKFLOW", "Facility ID is missing for visit: " + existingVisit.getId());
            }

            log.debug("Fetching facility for visitId={} facilityId={}", existingVisit.getId(), existingVisit.getFacilityId());
            Facility facility = getFacilityById(existingVisit.getFacilityId());
            if (facility == null) {
                log.error("SUBMIT_VISIT_REPORT failed: facility not found for facilityId={} visitId={}", existingVisit.getFacilityId(), existingVisit.getId());
                throw new CustomException("UPDATE_WORKFLOW", "Facility not found for facilityId: " + existingVisit.getFacilityId());
            }

            log.info("Resolved HRMS lookup for visitId={} facilityId={} boundaryCode={}",
                    existingVisit.getId(), existingVisit.getFacilityId(), facility.getBoundaryCode());

            Employee employee = getEmployeeByBoundaryCode(request, facility.getBoundaryCode());
            if (employee !=null && employee.getUser() !=null && employee.getUser().getMobileNumber()!=null && !employee.getUser().getMobileNumber().isEmpty()){
                OtpResponse otpResponse = createOTP(employee.getUser().getMobileNumber(), existingVisit.getTenantId());
                if (otpResponse !=null && otpResponse.getOtp()!=null){
                    log.info("OTP {} generated for this mobile number {}", otpResponse.getOtp().getOtp(), employee.getUser().getMobileNumber());
                    existingVisit.getVisitReport().setOtpReference(otpResponse.getOtp().getOtp());
                    sendOtpSms(employee.getUser().getMobileNumber(), otpResponse.getOtp().getOtp(), existingVisit.getTenantId());
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
            if (existingVisit.getVisitReport() == null) {
                throw new CustomException("INVALID_VISIT_STATE", "Visit report not found on existing visit");
            }
            if (request.getVisitReport().getOtpReference() == null) {
                throw new CustomException("INVALID_OTP_REQUEST", "Visit report with OTP reference is required for SUBMIT_OTP action");
            }
            // Check if we should bypass OTP validation and use default OTP
            if (amcServiceConfiguration.isByPassOtpValidation()){
                String defaultOtp = amcServiceConfiguration.getDefaultOtp();
                if(request.getVisitReport().getOtpReference() ==null || !request.getVisitReport().getOtpReference().trim().equals(defaultOtp)){
                    throw new CustomException("ERROR_OTP_GENERATION", "OTP validation unsuccessful");
                }
                log.info("OTP {} validated for default OTP", defaultOtp);
                existingVisit.getVisitReport().setOtpVerifiedAt(new Timestamp(System.currentTimeMillis()).getTime());
            }
            else{
                if (existingVisit.getFacilityId() == null || existingVisit.getFacilityId().trim().isEmpty()) {
                    log.error("SUBMIT_OTP failed: facilityId missing for visitId={}", existingVisit.getId());
                    throw new CustomException("UPDATE_WORKFLOW", "Facility ID is missing for visit: " + existingVisit.getId());
                }

                log.debug("Fetching facility for OTP validation visitId={} facilityId={}", existingVisit.getId(), existingVisit.getFacilityId());
                Facility facility = getFacilityById(existingVisit.getFacilityId());
                if (facility == null) {
                    log.error("SUBMIT_OTP failed: facility not found for facilityId={} visitId={}", existingVisit.getFacilityId(), existingVisit.getId());
                    throw new CustomException("UPDATE_WORKFLOW", "Facility not found for facilityId: " + existingVisit.getFacilityId());
                }

                log.info("Resolved HRMS lookup for OTP validation visitId={} facilityId={} boundaryCode={}",
                        existingVisit.getId(), existingVisit.getFacilityId(), facility.getBoundaryCode());

                Employee employee = getEmployeeByBoundaryCode(request, facility.getBoundaryCode());
                if (employee !=null && employee.getUser() !=null && employee.getUser().getMobileNumber()!=null && !employee.getUser().getMobileNumber().isEmpty()){
                    OtpResponse otpResponse = validateOTP(employee.getUser().getMobileNumber(), existingVisit.getTenantId(), request.getVisitReport().getOtpReference());
                    if (otpResponse !=null && otpResponse.getOtp()!=null){
                        log.info("OTP {} validated for this mobile number {}", otpResponse.getOtp().getOtp(), employee.getUser().getMobileNumber());
                        // We need to update visit report on existing visit after validation
                        existingVisit.getVisitReport().setOtpVerifiedAt(new Timestamp(System.currentTimeMillis()).getTime());
                    }
                }
            }
        }

        if (SCHEDULE_ACTION.equalsIgnoreCase(request.getWorkflow().getAction())
                && DRAFT_STATUS.equalsIgnoreCase(existingVisit.getStatus())) {
            validateVisitCanBeScheduled(existingVisit);
            expirePreviousDraftOrScheduledVisits(existingVisit, request.getRequestInfo());
        }

        // 3. Call workflow transition
        ProcessInstance updatedWorkflow;
        try {
            updatedWorkflow = workflowService.transitionWorkflow(
                    existingVisit,
                    request.getWorkflow().getAction(),
                    request.getWorkflow().getDocuments(),
                    request.getRequestInfo(),
                    request.getWorkflow().getComment()
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
                .facilityName(existingVisit.getFacilityName())
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

        // 8. Push the fully-enriched visit (facility, amcConfiguration, assignments) to the search index
        // topic, skipping DRAFT visits. existingVisit already carries the updated status/visitReport, but
        // the refreshed auditDetails (bumped lastModifiedTime) landed on updatedScheduledVisit - carry it over.
        existingVisit.setAuditDetails(updatedScheduledVisit.getAuditDetails());
        pushNonDraftVisitsToIndex(request.getRequestInfo(), List.of(existingVisit), amcServiceConfiguration.getUpdateScheduledVisitIndexTopic());

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
        Map<String, Boundary> listBlock = boundaryUtil.getBoundaryByCode();
        for (ScheduledVisit scheduledVisit : amcConfigurationList){
            Facility facility = scheduledVisit.getFacility();
            if (facility != null) {
                facilityPocPhoneUtil.decryptPocPhoneIfPresent(facility);
                String boundaryCode = facility.getBoundaryCode();
                if (boundaryCode != null && listBlock != null) {
                    Boundary boundary = listBlock.get(boundaryCode);
                    if (boundary != null) {
                        log.debug("✨ Enriching projectId={} with state={}, district={} and block={}", scheduledVisit.getId(), boundary.getState(), boundary.getDistrict(), boundary.getBlock());
                        Object additionalDetails = facility.getAdditionalDetails();
                        Object enrichedAdditionalDetails = mergeListIntoAdditionalDetails(additionalDetails, "boundary", boundary);
                        facility.setAdditionalDetails((Map<String, Object>) enrichedAdditionalDetails);
                    } else {
                        log.warn("⚠️ No boundary found for code={} in facility boundary={}", boundaryCode, scheduledVisit.getId());
                    }
                }
            }
            for(ScheduledVisitAssignment assignment : scheduledVisit.getAssignments()){
                try{
                    Employee employee =  getUserById(request, assignment.getAssignedUser());
                    if (employee !=null && employee.getUser() !=null){
                        assignment.setUser(employee.getUser());
                    }
                }
                catch(Exception e){
                    log.error("error while calling hrms {} ", e.getMessage());
                }
            }
        }
        return amcConfigurationList;
    }

    private Object mergeListIntoAdditionalDetails(Object additionalDetails, String key, Object value) {
        if (additionalDetails instanceof Map) {
            ((Map<String, Object>) additionalDetails).put(key, value);
            return additionalDetails;
        } else {
            // default to HashMap if null or unknown type
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            return map;
        }
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

            if (isDraftToScheduledTransition(scheduledVisit, visit)) {
                validateVisitCanBeScheduled(resolveScheduledDate(visit, scheduledVisit), visit.getId());
                expirePreviousDraftOrScheduledVisits(visit, request.getRequestInfo());
            }

            // Check if visit needs to be scheduled based on notice period
            checkAndScheduleVisitIfNeeded(visit, scheduledVisit, request.getRequestInfo());

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

    private boolean isDraftToScheduledTransition(ScheduledVisit visitFromDB, ScheduledVisit visitFromRequest) {
        return DRAFT_STATUS.equalsIgnoreCase(visitFromDB.getStatus())
                && SCHEDULED_STATUS.equalsIgnoreCase(visitFromRequest.getStatus());
    }

    private Long resolveScheduledDate(ScheduledVisit visitFromRequest, ScheduledVisit visitFromDB) {
        if (visitFromRequest.getScheduledDate() != null && visitFromRequest.getScheduledDate() != 0) {
            return visitFromRequest.getScheduledDate();
        }
        return visitFromDB.getScheduledDate();
    }

    private static final int SCHEDULE_MAX_MONTHS_AHEAD = 1;

    private void validateVisitCanBeScheduled(ScheduledVisit visit) {
        validateVisitCanBeScheduled(visit.getScheduledDate(), visit.getId());
    }

    private void validateVisitCanBeScheduled(Long scheduledDate, String visitId) {
        if (!isScheduledDateWithinSchedulingWindow(scheduledDate)) {
            throw new CustomException(
                    "SCHEDULE_DATE_TOO_FAR",
                    "Visit cannot be scheduled more than one month ahead of today: " + visitId
            );
        }
    }

    private boolean isScheduledDateWithinSchedulingWindow(Long scheduledDate) {
        if (scheduledDate == null || scheduledDate == 0) {
            return false;
        }
        LocalDate scheduledLocalDate = Instant.ofEpochMilli(scheduledDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate maxSchedulableDate = LocalDate.now(ZoneId.systemDefault()).plusMonths(SCHEDULE_MAX_MONTHS_AHEAD);
        return !scheduledLocalDate.isAfter(maxSchedulableDate);
    }

    /**
     * When scheduling a visit, expire any earlier visits for the same facility and AMC configuration
     * that are still in DRAFT or SCHEDULED status.
     */
    private void expirePreviousDraftOrScheduledVisits(ScheduledVisit visit, RequestInfo requestInfo) {
        if (visit.getFacilityId() == null || visit.getAmcConfigurationId() == null || visit.getVisitNumber() == null) {
            log.warn("Skipping expire of previous visits for visit {} - facilityId, amcConfigurationId or visitNumber is missing",
                    visit.getId());
            return;
        }

        ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder()
                .tenantId(visit.getTenantId())
                .amcConfigurationIds(List.of(visit.getAmcConfigurationId()))
                .facilityIds(List.of(visit.getFacilityId()))
                .statuses(List.of(DRAFT_STATUS, SCHEDULED_STATUS))
                .build();
        ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();

        List<ScheduledVisit> matchingVisits = searchScheduledVisit(
                searchRequest,
                amcServiceConfiguration.getMaxLimit(),
                amcServiceConfiguration.getDefaultOffset(),
                visit.getTenantId(),
                false,
                null
        );

        List<ScheduledVisit> previousVisits = matchingVisits.stream()
                .filter(candidate -> !visit.getId().equals(candidate.getId()))
                .filter(candidate -> candidate.getVisitNumber() != null && candidate.getVisitNumber() < visit.getVisitNumber())
                .toList();

        for (ScheduledVisit previousVisit : previousVisits) {
            expireVisit(previousVisit, requestInfo);
        }
    }

    private void expireVisit(ScheduledVisit visit, RequestInfo requestInfo) {
        log.info("Expiring previous visit {} (visitNumber={}) for facility {} and configuration {}",
                visit.getId(), visit.getVisitNumber(), visit.getFacilityId(), visit.getAmcConfigurationId());

        String expiredStatus = EXPIRED_STATUS;
        try {
            ProcessInstance updatedWorkflow = workflowService.transitionWorkflow(
                    visit,
                    EXPIRE_ACTION,
                    null,
                    requestInfo,
                    "Expired because a newer visit was scheduled for the same facility"
            );
            if (updatedWorkflow != null && updatedWorkflow.getState() != null) {
                expiredStatus = updatedWorkflow.getState().getState();
            }
        } catch (Exception e) {
            log.error("Workflow EXPIRE action failed for visit {}. Falling back to direct status update.", visit.getId(), e);
        }

        ScheduledVisit expiredVisit = ScheduledVisit.builder()
                .id(visit.getId())
                .tenantId(visit.getTenantId())
                .amcConfigurationId(visit.getAmcConfigurationId())
                .facilityId(visit.getFacilityId())
                .facilityName(visit.getFacilityName())
                .visitNumber(visit.getVisitNumber())
                .status(expiredStatus)
                .scheduledDate(visit.getScheduledDate())
                .actualVisitDate(visit.getActualVisitDate())
                .visitReport(visit.getVisitReport())
                .additionalDetails(visit.getAdditionalDetails())
                .assignments(visit.getAssignments())
                .build();

        scheduledVisitsEnrichment.enrichScheduledVisitRequestOnUpdate(expiredVisit, visit, requestInfo);

        ScheduledVisitRequest expireRequest = ScheduledVisitRequest.builder()
                .requestInfo(requestInfo)
                .scheduledVisits(List.of(expiredVisit))
                .build();
        producer.push(amcServiceConfiguration.getUpdateScheduledVisitTopic(), expireRequest);

        // Push the fully-enriched visit (facility, amcConfiguration, assignments) to the search index topic.
        // EXPIRED is always non-DRAFT, but route through the shared helper for consistency. Carry over the
        // new status and the refreshed auditDetails (bumped lastModifiedTime) that landed on expiredVisit.
        visit.setStatus(expiredStatus);
        visit.setAuditDetails(expiredVisit.getAuditDetails());
        pushNonDraftVisitsToIndex(requestInfo, List.of(visit), amcServiceConfiguration.getUpdateScheduledVisitIndexTopic());
    }

    /**
     * Check if visit is nearing scheduled date and apply SCHEDULE action if needed
     * This checks MDMS for notice period (amc.AMCThresholds.amc_visit_notice_period_in_days)
     * and applies workflow action if scheduled_date < current_date + notice_period
     */
    private void checkAndScheduleVisitIfNeeded(ScheduledVisit visit, ScheduledVisit visitFromDB, RequestInfo requestInfo) {
        // Only process DRAFT visits
        if (visitFromDB.getStatus() == null || !DRAFT_STATUS.equalsIgnoreCase(visitFromDB.getStatus())) {
            return;
        }

        try {
            if (!isScheduledDateWithinSchedulingWindow(visit.getScheduledDate())) {
                log.info("Visit {} has scheduled date more than one month from today (scheduledDate={}). Skipping auto-schedule.",
                        visit.getId(), visit.getScheduledDate());
                return;
            }

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

                expirePreviousDraftOrScheduledVisits(visit, requestInfo);

                try {
                    // This updates the workflow state and returns the new ProcessInstance
                    ProcessInstance updatedWorkflow = workflowService.transitionWorkflow(
                            visit,
                            SCHEDULE_ACTION,
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

    private void sendOtpSms(String mobileNumber, String otp, String tenantId) {
        if (Boolean.FALSE.equals(amcServiceConfiguration.getSmsEnabled())) {
            log.info("SMS notification is disabled. Skipping OTP SMS for mobile number {}", mobileNumber);
            return;
        }
        if (mobileNumber == null || mobileNumber.isEmpty() || otp == null || otp.isEmpty()) {
            log.info("Skipping OTP SMS due to missing mobile number or OTP value");
            return;
        }

        String smsMessage = amcServiceConfiguration.getOtpSmsTemplate().replace("{otp}", otp);
        SMSRequest smsRequest = SMSRequest.builder()
                .mobileNumber(mobileNumber)
                .message(smsMessage)
                .build();
        producer.push(amcServiceConfiguration.getSmsNotificationTopic(), smsRequest);
        log.info("OTP SMS queued for mobile number {} in tenant {}", mobileNumber, tenantId);
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

    public Employee getEmployeeByBoundaryCode(Object request, String boundaryCode) {
        if (boundaryCode == null || boundaryCode.trim().isEmpty()) {
            throw new CustomException("EMPLOYEE_NOT_FOUND", "Boundary code is required for employee search");
        }
        String url = amcServiceConfiguration.getHrmsHost() + amcServiceConfiguration.getHrmsSearchUrl()
                + "?tenantId=in&boundaryCodes=" + boundaryCode + "&searchOnlyInBoundary=true";
        log.debug("Calling HRMS employee search by boundaryCode={}", boundaryCode);
        Object response = requestRepository.fetchResult(new StringBuilder(url), request);

        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
            log.warn("No HRMS employee found for boundaryCode={}", boundaryCode);
            throw new CustomException("EMPLOYEE_NOT_FOUND", "Employee not found for boundaryCode: " + boundaryCode);
        }
        log.debug("HRMS employee found for boundaryCode={}", boundaryCode);
        return employeeResponse.getEmployees().get(0);
    }

    private Facility getFacilityById(String facilityId) {
//        String encodedFacilityId = URLEncoder.encode(facilityId, StandardCharsets.UTF_8);
        String url = amcServiceConfiguration.getFacilityServiceHost() + amcServiceConfiguration.getFacilityServiceSearchUrlV2() + "?facilityId=" + facilityId;
        log.debug("Calling facility search v2 for facilityId={}", facilityId);
        Object response = requestRepository.fetchResult(new StringBuilder(url));
        FacilitySearchResponse facilityResponse = mapper.convertValue(response, FacilitySearchResponse.class);
        if (facilityResponse == null || facilityResponse.getFacilities() == null || facilityResponse.getFacilities().isEmpty()) {
            log.warn("No facility found for facilityId={}", facilityId);
            return null;
        }
        log.debug("Facility found for facilityId={}", facilityId);
        return facilityResponse.getFacilities().get(0);
    }

}
