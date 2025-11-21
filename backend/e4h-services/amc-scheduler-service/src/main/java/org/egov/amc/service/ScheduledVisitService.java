package org.egov.amc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.repository.ScheduledVisitRepository;
import org.egov.amc.service.enrichment.ScheduledVisitEnrichment;
import org.egov.amc.util.AmcConfigurationServiceUtil;
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

import java.sql.Array;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

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

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    @Autowired
    public ScheduledVisitService(
            ScheduledVisitRepository scheduledVisitsRepository, ScheduledVisitValidator scheduledVisitsValidator, ServiceRequestRepository requestRepository, ScheduledVisitEnrichment scheduledVisitsEnrichment, AMCServiceConfiguration scheduledVisitsConfiguration,
            Producer producer, AmcConfigurationServiceUtil scheduledVisitsServiceUtil, AmcConfigurationService amcConfigurationService, VisitWorkflowService workflowService, JdbcTemplate jdbcTemplate) {
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
    }

    public ScheduledVisitRequest createScheduledVisit(ScheduledVisitRequest request) {
        scheduledVisitsValidator.validateCreateScheduledVisitRequest(request);
        for (ScheduledVisit amcConfiguration : request.getScheduledVisits()) {
            scheduledVisitsEnrichment.enrichScheduledVisitOnCreate(amcConfiguration, request.getRequestInfo());
            log.info("Enriched with AMC Ids and AuditDetails {}", amcConfiguration);
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
        int i =1;
        for (Long visitDate : generateAmcVisits){
            List<ScheduledVisitAssignment> assignments = new ArrayList<>();
            for (AmcConfigurationAssignment amcConfigurationAssignment : amcConfiguration.getAssignments()){
                ScheduledVisitAssignment scheduledVisitAssignment = ScheduledVisitAssignment.builder().assignedUser(amcConfigurationAssignment.getAssignedUser()).build();
                assignments.add(scheduledVisitAssignment);
            }
            ScheduledVisit visit = ScheduledVisit.builder()
                    .tenantId(amcConfiguration.getTenantId())
                    .amcConfigurationId(amcConfiguration.getId())
                    .facilityId(amcConfiguration.getFacilityId())
                    .visitNumber(i)
                    .scheduledDate(visitDate)
                    .assignments(assignments)
                    .status("DRAFT")
                    .build();

            scheduledVisitList.add(visit);
            i++;
        }

        ScheduledVisitRequest scheduledVisitRequest = ScheduledVisitRequest.builder().requestInfo(request.getRequestInfo()).scheduledVisits(scheduledVisitList).build();
        ScheduledVisitRequest response = createScheduledVisit(scheduledVisitRequest);

        return ScheduledVisitResponse.builder()
                .scheduledVisits(response.getScheduledVisits())
                .totalCount(response.getScheduledVisits().size())
                .build();
    }

    public List<ScheduledVisit> updateVisitWorkflow(VisitReportSubmissionRequest request) throws Exception {
        // 1. Fetch the existing visit
        ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder().ids(List.of(request.getVisitId())).tenantId(request.getRequestInfo().getUserInfo().getTenantId()).build();
        ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder().RequestInfo(request.getRequestInfo()).searchCriteria(criteria).build();
        List<ScheduledVisit> scheduledVisitsList = searchScheduledVisit(searchRequest, 10, 0, request.getRequestInfo().getUserInfo().getTenantId(), false, null);
        if(scheduledVisitsList==null || scheduledVisitsList.isEmpty())
            throw new CustomException("GENERATE_VISIT_ERROR", "The Visit ID: "+ request.getVisitId() +" is not found");

        ScheduledVisit existingVisit = scheduledVisitsList.get(0);

        // 2. Call workflow transition
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
            e.printStackTrace();
            log.error(e.getMessage());
            throw new CustomException("WORKFLOW_TRANSITION_FAILED",
                    "Failed to transition workflow for facility: " + request.getVisitId());
        }

        if(request.getVisitReport() != null) {
            handleTransactions(request, updatedWorkflow);
        }

        // 3. Inject workflow status into activity facility
        existingVisit.setStatus(updatedWorkflow.getState().getState());

        // 4. Create a new Visit Instance instance with enriched additionalDetails
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

        // 5. Create Schedule visit request wrapper
        ScheduledVisitRequest enrichedRequest = ScheduledVisitRequest.builder()
                .requestInfo(request.getRequestInfo())
                .scheduledVisits(List.of(updatedScheduledVisit))
                .build();

        // 6. Perform enriched update using standard handler
        handleUpdateScheduledVisit(enrichedRequest, updatedScheduledVisit, existingVisit);

        // Step 7: After successful workflow transition, if action is APPROVED_BY_QC_SPOC
//        if ("APPROVE".equalsIgnoreCase(request.getWorkflow().getAction())) {
//            // once facility is fetched we need to fetch assets for that facility
//            String activityFacilityId = existingActivityFacitlity.getId();
//            if (activityFacilityId != null) {
//                updateAssetsForFacility(existingActivityFacitlity, request.getRequestInfo(), activityFacilityId);
//            }
//        }

        return List.of(updatedScheduledVisit);
    }

    private void handleTransactions(VisitReportSubmissionRequest request, ProcessInstance updatedWorkflow) {
        Transaction transaction = new Transaction();
        transaction.setProcessInstanceId(updatedWorkflow.getId());
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
        List<ScheduledVisit> amcConfigurationsFromDB = searchScheduledVisit(
                getSearchScheduledVisitRequest(request.getScheduledVisits(), request.getRequestInfo()),
                amcServiceConfiguration.getMaxLimit(), amcServiceConfiguration.getDefaultOffset(),
                request.getScheduledVisits().get(0).getTenantId(), false, null);
        log.info("Fetched scheduledVisits for update request");

        /*
         * Validate the update asset_amc request against the asset_amcs fetched from the database
         */
        scheduledVisitsValidator.validateUpdateAgainstDB(request.getScheduledVisits(), amcConfigurationsFromDB);

        /*
         * Process each scheduledVisits in the update request
         */
        for (ScheduledVisit amcConfiguration : request.getScheduledVisits()) {
            processScheduledVisitUpdate(request, amcConfiguration, amcConfigurationsFromDB);
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
                transaction.setVisitReport(mapper.readValue(rs.getString("sv_visit_report"), VisitReport.class));
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
    private ScheduledVisitSearchRequest getSearchScheduledVisitRequest(List<ScheduledVisit> amcConfigurations, RequestInfo requestInfo) {
        List<String> scheduledVisitsIds = amcConfigurations.stream().map(ScheduledVisit::getId).toList();
        ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder().ids(scheduledVisitsIds).tenantId(amcConfigurations.get(0).getTenantId()).build();
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

    private void processScheduledVisitUpdate(ScheduledVisitRequest request, ScheduledVisit amcConfiguration, List<ScheduledVisit> amcConfigurationsFromDB) {
        /*
         * Convert asset_amc ID to string for comparison
         */
        String scheduledVisitsId = String.valueOf(amcConfiguration.getId());

        /*
         * Find the scheduledVisits from the database that matches the current scheduledVisits ID
         */
        ScheduledVisit amcConfigurationFromDB = findScheduledVisitById(scheduledVisitsId, amcConfigurationsFromDB);

        if (amcConfigurationFromDB != null) {
            /*
             * Merge additional details of the scheduledVisits from the request and scheduledVisits from DB
             */
            amcConfigurationServiceUtil.mergeScheduledVisitAdditionalDetails(amcConfiguration, amcConfigurationFromDB);

            handleUpdateScheduledVisit(request, amcConfiguration, amcConfigurationFromDB);
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
                Objects.equals(scheduledVisitsFromDB.getFacility(), scheduledVisits.getFacility());
        // Note: We allow startDate, endDate, vendorId, geographyDetails, activities and auditDetails to be different
    }

    private ScheduledVisit findScheduledVisitById(String scheduledVisitsId, List<ScheduledVisit> amcConfigurationsFromDB) {
        /*
         * Find and return the scheduledVisits with the matching ID from the list of asset_amc fetched from the database
         */
        return amcConfigurationsFromDB.stream()
                .filter(p -> scheduledVisitsId.equals(String.valueOf(p.getId())))
                .findFirst()
                .orElse(null);
    }

    public List<ProcessInstance> getProcessInstanceById(String businessId, String tenantId, RequestInfo requestInfo) {
        return workflowService.getProcessInstanceById(businessId, tenantId, requestInfo);
    }

//    public Employee getUserById(Object request, String userId) {
//
//        String url = amcServiceConfiguration.getHrmsHost() + amcServiceConfiguration.getHrmsSearchUrl()+ "?tenantId=in&uuids="+userId;
//        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
//
//        EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
//        if (employeeResponse == null || employeeResponse.getEmployees() == null || employeeResponse.getEmployees().isEmpty()) {
//            throw new CustomException("EMPLOYEE_NOT_FOUND", "Employee not found with ID: " + userId);
//        }
//        return employeeResponse.getEmployees().get(0);
//    }

}
