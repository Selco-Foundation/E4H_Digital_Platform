package org.egov.amc.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.repository.AmcConfigurationRepository;
import org.egov.amc.repository.ScheduledVisitRepository;
import org.egov.amc.util.MappedVendorUtil;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AmcConfigurationAssignment;
import org.egov.amc.web.models.AmcConfigurationSearchCriteria;
import org.egov.amc.web.models.AmcConfigurationSearchRequest;
import org.egov.amc.web.models.FacilityAmcIndexUpdateRequest;
import org.egov.amc.web.models.ScheduledVisit;
import org.egov.amc.web.models.ScheduledVisitSearchCriteria;
import org.egov.amc.web.models.ScheduledVisitSearchRequest;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Pushes a snapshot of a facility's AMC data (installation date, applicability, frequency,
 * valid-till, and up to 10 due/visit date cycles) into facility-service's {@code additionalDetails},
 * so it reaches {@code health-facility-index-v0001} via facility-registry's existing Kibana push -
 * the same pattern vendor-registry uses to sync mapped-vendor fields
 * ({@code org.egov.util.FacilityUtil} in vendor-registry).
 *
 * <p>Best-effort: every entry point catches and logs, never rethrows, so a facility-service or
 * search outage never breaks the AMC create/update/visit flow that triggered the sync.
 */
@Service
@Slf4j
public class FacilityAmcIndexSyncService {

    private static final int MAX_CYCLES = 10;
    private static final String AMC_APPLICABLE_YES = "Yes";
    private static final String AMC_APPLICABLE_NO = "No";
    private static final String ACTIVE_STATUS = "ACTIVE";

    private static final String INTERNAL_REQUEST_API_ID = "Rainmaker";
    private static final String INTERNAL_REQUEST_DID = "amc-scheduler-service-facility-sync";
    private static final String INTERNAL_REQUEST_KEY = "cronjob-key";
    private static final String INTERNAL_REQUEST_AUTH_TOKEN = "cronjob-token";
    private static final String INTERNAL_SYSTEM_USER_UUID = "b6e6b5a1-8f2b-4a9e-9c1a-3a6f7b2c9d4e";
    private static final String INTERNAL_SYSTEM_USER_NAME = "SYSTEM_AMC_FACILITY_SYNC";
    private static final String INTERNAL_SYSTEM_USER_DISPLAY_NAME = "System - AMC Facility Sync";
    private static final String INTERNAL_SYSTEM_USER_MOBILE = "0000000000";
    private static final String INTERNAL_SYSTEM_USER_EMAIL = "amc-scheduler@e4h.com";
    private static final String INTERNAL_SYSTEM_USER_TYPE = "SYSTEM";
    private static final String EMPLOYEE_ROLE_CODE = "EMPLOYEE";
    private static final String SYSTEM_USER_ROLE_CODE = "SYSTEM_USER";

    private final AMCServiceConfiguration amcServiceConfiguration;
    // Depends on the repository layer, not AmcConfigurationService/ScheduledVisitService: both of
    // those services are trigger points that call into this sync service, so depending back on
    // them here would create a circular Spring bean dependency.
    private final AmcConfigurationRepository amcConfigurationRepository;
    private final ScheduledVisitRepository scheduledVisitRepository;
    private final ServiceRequestRepository requestRepository;
    private final MappedVendorUtil mappedVendorUtil;

    @Autowired
    public FacilityAmcIndexSyncService(AMCServiceConfiguration amcServiceConfiguration,
                                        AmcConfigurationRepository amcConfigurationRepository,
                                        ScheduledVisitRepository scheduledVisitRepository,
                                        ServiceRequestRepository requestRepository,
                                        MappedVendorUtil mappedVendorUtil) {
        this.amcServiceConfiguration = amcServiceConfiguration;
        this.amcConfigurationRepository = amcConfigurationRepository;
        this.scheduledVisitRepository = scheduledVisitRepository;
        this.requestRepository = requestRepository;
        this.mappedVendorUtil = mappedVendorUtil;
    }

    /**
     * Recomputes the full AMC snapshot for {@code facilityId} from the DB and pushes it to
     * facility-service. Never throws - failures are logged and swallowed so the caller's own
     * create/update/visit flow is never affected.
     */
    public void syncFacilityAmcSnapshot(String facilityId, String tenantId, RequestInfo requestInfo) {
        syncFacilityAmcSnapshot(facilityId, tenantId, requestInfo, List.of());
    }

    /**
     * As {@link #syncFacilityAmcSnapshot(String, String, RequestInfo)}, but overlays
     * {@code visitsInFlight} on top of what the DB currently holds.
     *
     * <p>Callers must pass the visits they have just changed. Visits are persisted through Kafka
     * (egov-persister owns the {@code UPDATE scheduled_visits}), so a snapshot taken in the same
     * request would otherwise read the pre-update row and index a stale value - most visibly, the
     * {@code actualVisitDate} stamped on SUBMIT_VISIT_REPORT would reach the index as null and only
     * appear later, by accident, on some subsequent sync for the same facility. This mirrors what
     * {@code ScheduledVisitService#pushNonDraftVisitsToIndex} already does for the visit index:
     * index the in-memory object, not a re-read.
     */
    public void syncFacilityAmcSnapshot(String facilityId, String tenantId, RequestInfo requestInfo,
                                        List<ScheduledVisit> visitsInFlight) {
        try {
            if (facilityId == null || facilityId.isBlank()) {
                return;
            }
            AmcConfiguration activeConfig = fetchActiveAmcConfiguration(facilityId, tenantId, requestInfo);
            Map<String, Object> amcFields =
                    buildAmcIndexFields(activeConfig, tenantId, requestInfo, visitsInFlight);
            pushToFacilityIndex(facilityId, tenantId, amcFields);
        } catch (Exception e) {
            log.error("Best-effort AMC facility-index sync failed for facilityId={}: {}", facilityId, e.getMessage(), e);
        }
    }

    private AmcConfiguration fetchActiveAmcConfiguration(String facilityId, String tenantId, RequestInfo requestInfo) {
        AmcConfigurationSearchCriteria criteria = AmcConfigurationSearchCriteria.builder()
                .facilityIds(List.of(facilityId))
                .statuses(List.of(ACTIVE_STATUS))
                .isActive(true)
                .build();
        AmcConfigurationSearchRequest request = AmcConfigurationSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();
        List<AmcConfiguration> configs = amcConfigurationRepository.getAmcConfiguration(request, 5, 0, tenantId, false, null);
        return configs.isEmpty() ? null : configs.get(0);
    }

    private Map<String, Object> buildAmcIndexFields(AmcConfiguration config, String tenantId,
                                                    RequestInfo requestInfo, List<ScheduledVisit> visitsInFlight) {
        // Every AMC key is seeded to null first so the snapshot fully replaces the AMC namespace.
        // Without this, keys omitted from a later snapshot would keep their previously indexed value:
        // deleting an AMC, or shortening a cadence from 10 cycles to 5, would leave orphaned due/visit
        // dates behind in the index.
        Map<String, Object> fields = blankAmcFields();
        if (config == null) {
            fields.put("amcApplicable", AMC_APPLICABLE_NO);
            return fields;
        }

        fields.put("amcApplicable", AMC_APPLICABLE_YES);
        fields.put("amcInstallationDate", config.getConfigurationStartDate());
        Integer durationMonths = config.getDurationMonths();
        fields.put("amcApplicableYears", durationMonths != null ? durationMonths / 12 : null);
        fields.put("amcFrequencyMonths", config.getVisitFrequencyMonths());
        fields.put("amcValidTill", config.getConfigurationEndDate());
        enrichAmcMappedVendor(config, fields, requestInfo);

        List<ScheduledVisit> visits =
                fetchActiveScheduledVisits(config.getId(), tenantId, requestInfo, visitsInFlight);
        visits.sort(Comparator.comparing(ScheduledVisit::getVisitNumber, Comparator.nullsLast(Integer::compareTo)));
        if (visits.size() > MAX_CYCLES) {
            log.warn("AMC configuration {} has {} active scheduled visit(s); capping facility-index sync at the first {} cycles",
                    config.getId(), visits.size(), MAX_CYCLES);
        }
        for (ScheduledVisit visit : visits) {
            Integer visitNumber = visit.getVisitNumber();
            if (visitNumber == null || visitNumber < 1 || visitNumber > MAX_CYCLES) {
                continue;
            }
            fields.put("amcDueDate" + visitNumber, visit.getScheduledDate());
            fields.put("amcVisitDate" + visitNumber, visit.getActualVisitDate());
        }
        return fields;
    }

    /** Every AMC key this service owns, mapped to null - the baseline a snapshot fills in. */
    private Map<String, Object> blankAmcFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("amcApplicable", null);
        fields.put("amcInstallationDate", null);
        fields.put("amcApplicableYears", null);
        fields.put("amcFrequencyMonths", null);
        fields.put("amcValidTill", null);
        fields.put("amcMappedVendorName", null);
        fields.put("amcMappedVendorUserName", null);
        for (int cycle = 1; cycle <= MAX_CYCLES; cycle++) {
            fields.put("amcDueDate" + cycle, null);
            fields.put("amcVisitDate" + cycle, null);
        }
        return fields;
    }

    /**
     * Resolves the AMC mapped vendor from the configuration's own assignments: the first active
     * assignee whose HRMS user holds {@code amc.mapped.vendor.role.code} (the AMC field staff).
     * Same resolution the scheduled-visit index uses (see {@code ScheduledVisitService#enrichMappedVendor}),
     * just sourced from the AMC configuration's assignments instead of a visit's.
     *
     * <p>Both keys are written unconditionally, {@code null} included, so clearing the assignment on
     * an AMC actually clears the indexed value rather than leaving a stale name behind. A missing
     * role config or a failed HRMS lookup degrades to null for the same reason - the index then
     * distinguishes "nobody mapped" from a real name.
     */
    private void enrichAmcMappedVendor(AmcConfiguration config, Map<String, Object> fields, RequestInfo requestInfo) {
        String amcMappedVendorName = null;
        String amcMappedVendorUserName = null;

        String roleCode = mappedVendorUtil.getMappedVendorRoleCode();
        if (roleCode == null || roleCode.trim().isEmpty()) {
            log.warn("amc.mapped.vendor.role.code is not configured; indexing AMC configuration {} without "
                    + "an AMC mapped vendor.", config.getId());
        } else {
            List<String> assigneeUuids = config.getAssignments() == null ? List.of()
                    : config.getAssignments().stream()
                            .filter(AmcConfigurationAssignment::isActive)
                            .map(AmcConfigurationAssignment::getAssignedUser)
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
            org.egov.amc.web.models.User fieldStaff =
                    mappedVendorUtil.resolveFieldStaff(requestInfo, assigneeUuids, roleCode);
            if (fieldStaff != null) {
                amcMappedVendorName = fieldStaff.getName();
                amcMappedVendorUserName = fieldStaff.getUserName();
            } else {
                log.info("No active assignee of AMC configuration {} holds role {}; indexing it without an "
                        + "AMC mapped vendor.", config.getId(), roleCode);
            }
        }

        fields.put("amcMappedVendorName", amcMappedVendorName);
        fields.put("amcMappedVendorUserName", amcMappedVendorUserName);
    }

    /**
     * The AMC's active visits as the index should see them: what the DB holds, with
     * {@code visitsInFlight} (the caller's just-changed, not-yet-persisted objects) layered on top by
     * visit id. An in-flight visit flagged inactive is dropped, and one the DB has not seen yet is
     * added, so a freshly created or regenerated series is reflected immediately.
     */
    private List<ScheduledVisit> fetchActiveScheduledVisits(String amcConfigurationId, String tenantId,
                                                            RequestInfo requestInfo,
                                                            List<ScheduledVisit> visitsInFlight) {
        ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder()
                .amcConfigurationIds(List.of(amcConfigurationId))
                .isActive(true)
                .build();
        ScheduledVisitSearchRequest request = ScheduledVisitSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();
        List<ScheduledVisit> fromDb =
                scheduledVisitRepository.getScheduledVisit(request, 50, 0, tenantId, false, null);

        Map<String, ScheduledVisit> byVisitId = new LinkedHashMap<>();
        if (fromDb != null) {
            for (ScheduledVisit visit : fromDb) {
                if (visit.getId() != null) {
                    byVisitId.put(visit.getId(), visit);
                }
            }
        }
        for (ScheduledVisit visit : visitsInFlight) {
            if (visit.getId() == null || !amcConfigurationId.equals(visit.getAmcConfigurationId())) {
                continue;
            }
            if (Boolean.FALSE.equals(visit.getIsActive())) {
                byVisitId.remove(visit.getId());
            } else {
                byVisitId.put(visit.getId(), visit);
            }
        }
        return new ArrayList<>(byVisitId.values());
    }

    /**
     * Sends the snapshot to facility-service's index-only AMC endpoint, which writes it straight to
     * the health facility index.
     *
     * <p>Deliberately not the facility {@code _update} API: that persists its payload through the
     * facility persister ({@code additional_details}), and AMC data is meant to live on the index
     * only. Nothing here reads or writes the facility table, so no facility lookup is needed either.
     */
    private void pushToFacilityIndex(String facilityId, String tenantId, Map<String, Object> amcFields) {
        FacilityAmcIndexUpdateRequest updateRequest = FacilityAmcIndexUpdateRequest.builder()
                .requestInfo(buildFacilityServiceSystemRequestInfo(tenantId))
                .facilityId(facilityId)
                .tenantId(tenantId)
                .amcFields(amcFields)
                .build();

        String url = amcServiceConfiguration.getFacilityServiceHost()
                + amcServiceConfiguration.getFacilityAmcIndexUpdateUrl();
        requestRepository.fetchResult(new StringBuilder(url), updateRequest);
        log.info("Pushed AMC snapshot to the health facility index for facilityId={}", facilityId);
    }

    private RequestInfo buildFacilityServiceSystemRequestInfo(String tenantId) {
        List<Role> roles = List.of(
                Role.builder().name("Employee").code(EMPLOYEE_ROLE_CODE).tenantId(tenantId).build(),
                Role.builder().name("System User").code(SYSTEM_USER_ROLE_CODE).tenantId(tenantId).build()
        );
        User systemUser = User.builder()
                .uuid(INTERNAL_SYSTEM_USER_UUID)
                .userName(INTERNAL_SYSTEM_USER_NAME)
                .name(INTERNAL_SYSTEM_USER_DISPLAY_NAME)
                .mobileNumber(INTERNAL_SYSTEM_USER_MOBILE)
                .emailId(INTERNAL_SYSTEM_USER_EMAIL)
                .type(INTERNAL_SYSTEM_USER_TYPE)
                .roles(roles)
                .tenantId(tenantId)
                .build();
        return RequestInfo.builder()
                .apiId(INTERNAL_REQUEST_API_ID)
                .ver("1.0")
                .ts(System.currentTimeMillis())
                .action("_update")
                .did(INTERNAL_REQUEST_DID)
                .key(INTERNAL_REQUEST_KEY)
                .msgId(UUID.randomUUID().toString())
                .authToken(INTERNAL_REQUEST_AUTH_TOKEN)
                .userInfo(systemUser)
                .build();
    }
}
