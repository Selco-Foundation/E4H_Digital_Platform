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
import java.util.stream.Collectors;

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
    private final AmcVisitReportPdfService amcVisitReportPdfService;
    private final AmcAnalyticsService amcAnalyticsService;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public ScheduledVisitService(
            ScheduledVisitRepository scheduledVisitsRepository, ScheduledVisitValidator scheduledVisitsValidator, ServiceRequestRepository requestRepository, ScheduledVisitEnrichment scheduledVisitsEnrichment, AMCServiceConfiguration scheduledVisitsConfiguration,
            Producer producer, AmcConfigurationServiceUtil scheduledVisitsServiceUtil, AmcConfigurationService amcConfigurationService, VisitWorkflowService workflowService, JdbcTemplate jdbcTemplate, MDMSUtils mdmsUtils, BoundaryUtil boundaryUtil,
            FacilityPocPhoneUtil facilityPocPhoneUtil, AmcVisitReportPdfService amcVisitReportPdfService) {
            FacilityPocPhoneUtil facilityPocPhoneUtil, AmcAnalyticsService amcAnalyticsService) {
            this.scheduledVisitsValidator = scheduledVisitsValidator;
        this.requestRepository = requestRepository;
        this.producer = producer;
            this.amcServiceConfiguration = scheduledVisitsConfiguration;
            this.scheduledVisitsRepository = scheduledVisitsRepository;
            this.scheduledVisitsEnrichment = scheduledVisitsEnrichment;
            this.amcConfigurationServiceUtil = scheduledVisitsServiceUtil;
        this.amcConfigurationService = amcConfigurationService;
        this.workflowService = workflowService;
        this.amcVisitReportPdfService = amcVisitReportPdfService;
        this.jdbcTemplate = jdbcTemplate;
        this.mdmsUtils = mdmsUtils;
        this.boundaryUtil = boundaryUtil;
        this.facilityPocPhoneUtil = facilityPocPhoneUtil;
        this.amcAnalyticsService = amcAnalyticsService;
    }

    public ScheduledVisitRequest createScheduledVisit(ScheduledVisitRequest request) {
        log.trace("Entering createScheduledVisit method");
        log.info("Creating {} scheduled visit(s)", request.getScheduledVisits().size());
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
                log.warn("Visit number {} already exists for configuration {}, visitId: {}",
                        scheduledVisit.getVisitNumber(), scheduledVisit.getAmcConfigurationId(),
                        scheduledVisits.get(0).getId());
                throw new CustomException("CREATE_VISIT_ERROR", "A visit number: "+ scheduledVisit.getVisitNumber()+" already exist for configuration "+scheduledVisit.getAmcConfigurationId());
            }

            Facility facility = getFacilityById(scheduledVisit.getFacilityId());
            if (facility == null) {
                throw new CustomException("CREATE_VISIT_ERROR", "Facility not found for facilityId: " + scheduledVisit.getFacilityId());
            }
            scheduledVisit.setFacilityName(facility.getFacilityName());

            // remove Duplicate Assignments
            Set<String> seenUsers = new HashSet<>();
            List<ScheduledVisitAssignment> assignments = scheduledVisit.getAssignments().stream().filter(a -> seenUsers.add(a.getAssignedUser()))
                    .toList();
            scheduledVisit.setAssignments(assignments);
            scheduledVisitsEnrichment.enrichScheduledVisitOnCreate(scheduledVisit, request.getRequestInfo());
            log.trace("Enriching scheduled visit on create for visitId: {}", scheduledVisit.getId());
            log.info("Scheduled visit enriched with AMC configuration ID: {}", scheduledVisit.getAmcConfigurationId());
            log.debug("Enriched scheduled visit details - visitNumber: {}, projectId: {}, facilityId: {}",
                    scheduledVisit.getVisitNumber(), scheduledVisit.getProjectId(), scheduledVisit.getFacilityId());
            log.info("Pushed scheduled visit to kafka topic");
        }
        producer.push(amcServiceConfiguration.getSaveScheduledVisitTopic(), request);
        return request;
    }

    public ScheduledVisitResponse generateScheduledVisits(VisitGenerationRequest request) {
        if (request == null)
            throw new CustomException("GENERATE_VISIT_ERROR", "The request is empty");

        if (request.getConfigurationId() == null || request.getConfigurationId().isEmpty())
            throw new CustomException("GENERATE_VISIT_ERROR", "Configuration ID is mandatory");

        log.trace("Entering generateScheduledVisits method for configurationId: {}", request.getConfigurationId());
        log.info("Generating scheduled visits for AMC configuration: {}", request.getConfigurationId());
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
            endDate = request.getGenerationEndDate();

        if (startDate == null || endDate == null)
            throw new CustomException("GENERATE_VISIT_ERROR", "The configuration " + request.getConfigurationId() + " has no start or end date to generate visits from");

        List<ScheduledVisit> existingVisits = getVisitsForConfiguration(amcConfiguration, request.getRequestInfo());

        // Regenerating replaces the whole series; otherwise we only append after the existing visits,
        // because ux_scheduled_visits_unique_visit_per_amc forbids reusing a visit number.
        if (Boolean.TRUE.equals(request.getRegenerateExisting()) && !existingVisits.isEmpty()) {
            log.info("regenerateExisting=true: deactivating {} existing visit(s) of configuration {}",
                    existingVisits.size(), request.getConfigurationId());
            for (ScheduledVisit existingVisit : existingVisits) {
                existingVisit.setIsActive(Boolean.FALSE);
                existingVisit.setAuditDetails(amcConfigurationServiceUtil.getAuditDetails(
                        request.getRequestInfo().getUserInfo().getUuid(), existingVisit.getAuditDetails(),
                        existingVisit.getAuditDetails() == null));
            }
            producer.push(amcServiceConfiguration.getDeleteScheduledVisitTopic(),
                    ScheduledVisitRequest.builder().requestInfo(request.getRequestInfo()).scheduledVisits(existingVisits).build());
            existingVisits = new ArrayList<>();
        }

        // Generate scheduled visit based on startDate and Frequency
        List<Long> generateAmcVisits = amcConfigurationServiceUtil.generateAmcVisits(startDate, endDate, amcConfiguration.getVisitFrequencyMonths());
        if (generateAmcVisits ==null || generateAmcVisits.isEmpty())
            throw new CustomException("GENERATE_VISIT_ERROR", "Cannot generate scheduled visit for this configuration");

        Set<Long> alreadyScheduledDates = existingVisits.stream()
                .map(ScheduledVisit::getScheduledDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        generateAmcVisits = generateAmcVisits.stream().filter(date -> !alreadyScheduledDates.contains(date)).toList();
        if (generateAmcVisits.isEmpty())
            throw new CustomException("GENERATE_VISIT_ERROR", "All visits for this configuration have already been generated");

        List<ScheduledVisit> scheduledVisitList = new ArrayList<>();
        Long previousVisitDate = existingVisits.stream()
                .map(ScheduledVisit::getScheduledDate)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(null);
        int i = existingVisits.stream()
                .map(ScheduledVisit::getVisitNumber)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
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

        log.debug("Generated {} scheduled visits for configuration {}", scheduledVisitList.size(), request.getConfigurationId());
        ScheduledVisitRequest scheduledVisitRequest = ScheduledVisitRequest.builder().requestInfo(request.getRequestInfo()).scheduledVisits(scheduledVisitList).build();
        ScheduledVisitRequest response = createScheduledVisit(scheduledVisitRequest);
        log.info("Successfully generated {} scheduled visits for configuration {}", response.getScheduledVisits().size(), request.getConfigurationId());

        return ScheduledVisitResponse.builder()
                .scheduledVisits(response.getScheduledVisits())
                .totalCount(response.getScheduledVisits().size())
                .build();
    }

    /* Raw visits of a configuration, straight from the repository - no boundary/employee enrichment needed here. */
    private List<ScheduledVisit> getVisitsForConfiguration(AmcConfiguration amcConfiguration, RequestInfo requestInfo) {
        ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder()
                .tenantId(amcConfiguration.getTenantId())
                .amcConfigurationIds(List.of(amcConfiguration.getId()))
                .build();
        ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();
        List<ScheduledVisit> visits = scheduledVisitsRepository.getScheduledVisit(
                searchRequest, amcServiceConfiguration.getMaxLimit(), 0, amcConfiguration.getTenantId(), null, null);
        return visits == null ? new ArrayList<>() : new ArrayList<>(visits);
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
        log.trace("Entering updateVisitWorkflow method for visitId: {}", request.getVisitId());
        log.info("Updating visit workflow for visitId: {}, action: {}", request.getVisitId(), request.getWorkflow().getAction());
        if(request.getRequestInfo()==null || request.getRequestInfo().getUserInfo() ==null || request.getRequestInfo().getUserInfo().getUuid().isEmpty())
            throw new CustomException("UPDATE_WORKFLOW", "User ID is not found in requestInfo");

        // 1. Fetch the existing visit
        ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder().ids(List.of(request.getVisitId())).tenantId(request.getRequestInfo().getUserInfo().getTenantId()).build();
        ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder().RequestInfo(request.getRequestInfo()).searchCriteria(criteria).build();
        List<ScheduledVisit> scheduledVisitsList = searchScheduledVisit(searchRequest, 10, 0, request.getRequestInfo().getUserInfo().getTenantId(), false, null);
        if(scheduledVisitsList==null || scheduledVisitsList.isEmpty())
            throw new CustomException("GENERATE_VISIT_ERROR", "The Visit ID: "+ request.getVisitId() +" is not found");

        ScheduledVisit existingVisit = scheduledVisitsList.get(0);
        // Captured before the transition overwrites it below: analytics needs the pre-transition
        // status to tell a first report submission (out of SCHEDULED) from a re-submission
        // (out of REJECTED).
        String priorStatus = existingVisit.getStatus();

        // Step 2: if action is SUBMIT_VISIT_REPORT, check if send OTP is successful or not
        log.debug("Processing workflow action: {}", request.getWorkflow().getAction());
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
                log.trace("Creating OTP for visit report submission, visitId: {}", existingVisit.getId());
                OtpResponse otpResponse = createOTP(employee.getUser().getMobileNumber(), existingVisit.getTenantId());
                if (otpResponse !=null && otpResponse.getOtp()!=null){
                    log.debug("OTP generated successfully for visitId: {}", existingVisit.getId());
                    log.info("OTP generated for visit report submission, visitId: {}", existingVisit.getId());
                    existingVisit.getVisitReport().setOtpReference(otpResponse.getOtp().getOtp());
                    sendOtpSms(employee.getUser().getMobileNumber(), otpResponse.getOtp().getOtp(), existingVisit.getTenantId());
                }
                else {
                    log.warn("OTP generation returned null response for visit: {}", existingVisit.getId());
                }
            }
            else
                log.warn("Cannot send OTP - employee or mobile number not found for user ID: {}", request.getRequestInfo().getUserInfo().getUuid());

            attachAmcInstallationFormDocument(request, existingVisit, facility);
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
            log.error("Workflow transition failed for visitId: {}, action: {}", request.getVisitId(), request.getWorkflow().getAction(), e);
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
        log.info("Successfully updated visit workflow for visitId: {}, new status: {}", updatedScheduledVisit.getId(), updatedScheduledVisit.getStatus());

        // 8. Publish the user-analytics event - best-effort, never breaks the workflow update.
        amcAnalyticsService.publishVisitWorkflowEvent(existingVisit, request.getWorkflow().getAction(), priorStatus,
                request.getRequestInfo());

        return List.of(updatedScheduledVisit);
    }

    private void attachAmcInstallationFormDocument(VisitReportSubmissionRequest request, ScheduledVisit existingVisit, Facility facility) {
        log.trace("Entering attachAmcInstallationFormDocument method for visitId: {}", existingVisit.getId());
        String fileStoreId = amcVisitReportPdfService.generateAmcVisitReportPdf(request, existingVisit, facility);
        GeoLocation geoLocation = amcVisitReportPdfService.resolveGeoLocation(request.getVisitReport());

        Document pdfDocument = Document.builder()
                .documentType("AMC_INSTALLATION_FORM")
                .fileStoreId(fileStoreId)
                .documentUid("AMC-FORM-" + existingVisit.getId() + "-" + System.currentTimeMillis())
                .geoLocation(geoLocation)
                .build();

        List<Document> documents = request.getWorkflow().getDocuments();
        documents = (documents == null) ? new ArrayList<>() : new ArrayList<>(documents);
        documents.add(pdfDocument);
        request.getWorkflow().setDocuments(documents);
        log.info("AMC installation form document attached to workflow for visitId: {}", existingVisit.getId());
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
        log.debug("Pushing transaction to kafka for visitId: {}, transactionId: {}", transaction.getVisitId(), transaction.getTransactionId());
        producer.push(amcServiceConfiguration.getTransactionPersistTopic(), new TransactionRequest(List.of(transaction)));
        log.info("Transaction persisted for visitId: {}", transaction.getVisitId());
    }

    public ScheduledVisitRequest updateScheduledVisit(ScheduledVisitRequest request) {
        /*
         * Validate the update scheduledVisits request
         */
        log.trace("Entering updateScheduledVisit method");
        scheduledVisitsValidator.validateUpdateScheduledVisitRequest(request);
        log.info("Update scheduled visit request validated, visit count: {}", request.getScheduledVisits().size());

        /*
         * Search for asset_amc based on asset_amc IDs provided in the request
         */
        List<ScheduledVisit> scheduleVisitFromDB = searchScheduledVisit(
                getSearchScheduledVisitRequest(request.getScheduledVisits(), request.getRequestInfo()),
                amcServiceConfiguration.getMaxLimit(), amcServiceConfiguration.getDefaultOffset(),
                request.getScheduledVisits().get(0).getTenantId(), false, null);
        log.debug("Fetched {} scheduled visits from database for update request", scheduleVisitFromDB.size());
        log.info("Fetched scheduled visits for update request");

        /*
         * Validate the update asset_amc request against the asset_amcs fetched from the database
         */
        scheduledVisitsValidator.validateUpdateAgainstDB(request.getScheduledVisits(), scheduleVisitFromDB);

        /*
         * Process each scheduledVisits in the update request
         */
        log.debug("Processing {} scheduled visit(s) for update", request.getScheduledVisits().size());
        for (ScheduledVisit scheduledVisit : request.getScheduledVisits()) {
            processScheduledVisitUpdate(request, scheduledVisit, scheduleVisitFromDB);
        }
        log.info("Successfully processed update for {} scheduled visit(s)", request.getScheduledVisits().size());

        return request;
    }

    public List<Transaction> getTransactionsForVisit(List<String> projectIds) {
        log.trace("Entering getTransactionsForVisit method for {} visit IDs", projectIds != null ? projectIds.size() : 0);
        if (projectIds == null || projectIds.isEmpty()) {
            log.debug("No visit IDs provided, returning empty transaction list");
            return Collections.emptyList();
        }

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
        log.trace("Entering searchScheduledVisit method, tenantId: {}, limit: {}, offset: {}", tenantId, limit, offset);
        scheduledVisitsValidator.validateSearchScheduledVisitRequest(request, limit, offset, tenantId);
        List<ScheduledVisit> amcConfigurationList = scheduledVisitsRepository.getScheduledVisit(request, limit, offset, tenantId, includeDeleted, lastChangedSince);
        log.debug("Found {} scheduled visits matching search criteria", amcConfigurationList.size());
        Map<String, Boundary> listBlock = boundaryUtil.getBoundaryByCode();
        log.info("Enriching {} scheduled visits with boundary and employee data", amcConfigurationList.size());
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
                    log.error("Error while calling HRMS service for userId: {}, visitId: {}", assignment.getAssignedUser(), scheduledVisit.getId(), e);
                }
            }
        }
        log.info("Completed enrichment of scheduled visits");
        return amcConfigurationList;
    }

    /**
     * Builds an AMC visit summary (current visit number vs total planned visits, completed and lapsed
     * visit numbers) per requested facilityId.
     */
    public List<FacilityAmcSummary> getFacilityAmcSummary(ScheduledVisitSearchRequest request, String tenantId) {
        if (request.getSearchCriteria() == null
                || request.getSearchCriteria().getFacilityIds() == null
                || request.getSearchCriteria().getFacilityIds().isEmpty()) {
            throw new CustomException("AMC_SUMMARY_ERROR", "facilityIds is mandatory for AMC summary search");
        }
        scheduledVisitsValidator.validateSearchScheduledVisitRequest(request, amcServiceConfiguration.getMaxLimit(), amcServiceConfiguration.getDefaultOffset(), tenantId);

        List<String> facilityIds = request.getSearchCriteria().getFacilityIds();
        List<ScheduledVisit> scheduledVisits = scheduledVisitsRepository.getScheduledVisit(
                request, amcServiceConfiguration.getMaxLimit(), amcServiceConfiguration.getDefaultOffset(), tenantId, false, null);

        Map<String, List<ScheduledVisit>> visitsByFacility = scheduledVisits.stream()
                .collect(Collectors.groupingBy(ScheduledVisit::getFacilityId));

        List<FacilityAmcSummary> summaries = new ArrayList<>();
        for (String facilityId : facilityIds) {
            List<ScheduledVisit> visits = visitsByFacility.getOrDefault(facilityId, Collections.emptyList());

            List<Integer> completedAmcNumbers = visits.stream()
                    .filter(v -> APPROVED_STATUS.equalsIgnoreCase(v.getStatus()))
                    .map(ScheduledVisit::getVisitNumber)
                    .sorted()
                    .toList();

            List<Integer> lapsedAmcNumbers = visits.stream()
                    .filter(v -> EXPIRED_STATUS.equalsIgnoreCase(v.getStatus()))
                    .map(ScheduledVisit::getVisitNumber)
                    .sorted()
                    .toList();

            ScheduledVisit scheduledVisit = visits.stream()
                    .filter(v -> SCHEDULED_STATUS.equalsIgnoreCase(v.getStatus()))
                    .findFirst()
                    .orElse(null);

            Integer currentVisitNumber = scheduledVisit != null ? scheduledVisit.getVisitNumber() : 0;
            AmcConfiguration amcConfiguration = scheduledVisit != null
                    ? scheduledVisit.getAmcConfiguration()
                    : visits.stream().map(ScheduledVisit::getAmcConfiguration).filter(Objects::nonNull).findFirst().orElse(null);

            int totalVisits = 0;
            if (amcConfiguration != null && amcConfiguration.getDurationMonths() != null
                    && amcConfiguration.getVisitFrequencyMonths() != null && amcConfiguration.getVisitFrequencyMonths() != 0) {
                totalVisits = amcConfiguration.getDurationMonths() / amcConfiguration.getVisitFrequencyMonths();
            }

            summaries.add(FacilityAmcSummary.builder()
                    .facilityId(facilityId)
                    .amcNumber(currentVisitNumber + "/" + totalVisits)
                    .completedAmcNumbers(completedAmcNumbers)
                    .lapsedAmcNumbers(lapsedAmcNumbers)
                    .build());
        }

        return summaries;
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
        log.debug("Pushing scheduled visit update to kafka for visitId: {}", scheduledVisits.getId());
        producer.push(amcServiceConfiguration.getUpdateScheduledVisitTopic(), request);
        log.info("Scheduled visit update pushed to kafka for visitId: {}", scheduledVisits.getId());
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
        log.trace("Entering checkAndScheduleVisitIfNeeded for visitId: {}, status: {}", visit.getId(), visit.getStatus());
        // Only process DRAFT visits
        if (visit.getStatus() == null || !visit.getStatus().equals("DRAFT")) {
            log.debug("Skipping auto-schedule check for visitId: {} with status: {}", visit.getId(), visit.getStatus());
            return;
        }

        try {
            // Fetch notice period from MDMS
            log.debug("Fetching notice period from MDMS for tenantId: {}", visit.getTenantId());
            AmcConfigurationRequest mdmsRequest = AmcConfigurationRequest.builder()
                    .requestInfo(requestInfo)
                    .amcConfigurations(new ArrayList<>())
                    .build();
            Object mdmsData = mdmsUtils.mDMSCall(mdmsRequest, visit.getTenantId());
            Integer noticePeriod = parseNoticePeriodFromMDMS(mdmsData, visit.getTenantId());
            log.debug("Notice period from MDMS: {} days for tenantId: {}", noticePeriod, visit.getTenantId());

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
                log.debug("Visit scheduled date {} is before threshold date {}, applying SCHEDULE action",
                        visit.getScheduledDate(), thresholdDateMillis);
                log.info("Visit {} is nearing scheduled date. Applying SCHEDULE workflow action.", visit.getId());
                
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
        log.trace("Entering createOTP method for tenantId: {}", tenantId);
        String url = amcServiceConfiguration.getOtpServiceHost() + amcServiceConfiguration.getOtpServiceCreateUrl();
        log.debug("Calling OTP service at URL: {}", url);
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
        log.trace("Entering validateOTP method for tenantId: {}", tenantId);
        String url = amcServiceConfiguration.getOtpServiceHost() + amcServiceConfiguration.getOtpServiceValidateUrl();
        log.debug("Calling OTP validation service at URL: {}", url);
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
        log.trace("Entering getUserById method for userId: {}", userId);
        String url = amcServiceConfiguration.getHrmsHost() + amcServiceConfiguration.getHrmsSearchUrl()+ "?tenantId=in&uuids="+userId;
        log.debug("Calling HRMS service to fetch employee details for userId: {}", userId);
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
