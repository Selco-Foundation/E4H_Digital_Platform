package org.egov.amc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.repository.AmcConfigurationRepository;
import org.egov.amc.repository.ScheduledVisitRepository;
import org.egov.amc.util.MappedVendorUtil;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AmcConfigurationAssignment;
import org.egov.amc.web.models.AmcConfigurationSearchCriteria;
import org.egov.amc.web.models.AmcConfigurationSearchRequest;
import org.egov.amc.web.models.Facility;
import org.egov.amc.web.models.FacilityAmcBackfillResponse;
import org.egov.amc.web.models.FacilityAmcIndexUpdateRequest;
import org.egov.amc.web.models.FacilityBulkSearchApiRequest;
import org.egov.amc.web.models.FacilityBulkSearchCriteria;
import org.egov.amc.web.models.FacilitySearchResponse;
import org.egov.amc.web.models.ScheduledVisit;
import org.egov.amc.web.models.ScheduledVisitSearchCriteria;
import org.egov.amc.web.models.ScheduledVisitSearchRequest;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
 * <p>Best-effort: the per-facility sync catches and logs, never rethrows, so a facility-service or
 * search outage never breaks the AMC create/update/visit flow that triggered the sync.
 *
 * <p>{@link #backfillFacilityAmcIndex} is the deliberate exception. It has no host flow to protect,
 * and a script that reports a tidy set of counts after facility-service stopped answering would be
 * worse than one that fails: individual facilities are still skipped and counted, but losing the
 * facility listing itself aborts the run so the caller knows the backfill did not complete.
 */
@Service
@Slf4j
public class FacilityAmcIndexSyncService {

    private static final int MAX_CYCLES = 10;

    /**
     * How many of a facility's AMC configurations are pulled before picking the latest. Sized at the
     * repository's own page cap: a facility realistically holds a handful, and reading them all is
     * what makes {@link #pickLatestConfiguration} able to judge on start date rather than inheriting
     * the query's last-modified-time ordering.
     */
    private static final int MAX_CONFIGS_PER_FACILITY = 200;

    /** Facilities pulled from facility-service per backfill page. */
    private static final int BACKFILL_FACILITY_PAGE_SIZE = 100;

    /**
     * Stops a backfill that never stops paging - a facility-service that ignores offset would
     * otherwise loop forever re-indexing page one.
     */
    private static final int BACKFILL_MAX_PAGES = 10_000;

    /** Fallback page size for the prefetch queries when no repository cap is configured. */
    private static final int BACKFILL_DB_PAGE_SIZE_FALLBACK = 100;

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
    private final ActualVisitDateEnricher actualVisitDateEnricher;
    private final ObjectMapper mapper;

    @Autowired
    public FacilityAmcIndexSyncService(AMCServiceConfiguration amcServiceConfiguration,
                                        AmcConfigurationRepository amcConfigurationRepository,
                                        ScheduledVisitRepository scheduledVisitRepository,
                                        ServiceRequestRepository requestRepository,
                                        MappedVendorUtil mappedVendorUtil,
                                        ActualVisitDateEnricher actualVisitDateEnricher,
                                        @Qualifier("objectMapper") ObjectMapper mapper) {
        this.amcServiceConfiguration = amcServiceConfiguration;
        this.amcConfigurationRepository = amcConfigurationRepository;
        this.scheduledVisitRepository = scheduledVisitRepository;
        this.requestRepository = requestRepository;
        this.mappedVendorUtil = mappedVendorUtil;
        this.actualVisitDateEnricher = actualVisitDateEnricher;
        this.mapper = mapper;
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
            AmcConfiguration latestConfig = fetchLatestAmcConfiguration(facilityId, tenantId, requestInfo);
            List<ScheduledVisit> visits = latestConfig == null ? List.of()
                    : fetchActiveScheduledVisits(latestConfig.getId(), tenantId, requestInfo, visitsInFlight);
            Map<String, Object> amcFields =
                    buildAmcIndexFields(latestConfig, visits, resolveMappedVendor(latestConfig, requestInfo));
            pushToFacilityIndex(facilityId, tenantId, amcFields);
        } catch (Exception e) {
            log.error("Best-effort AMC facility-index sync failed for facilityId={}: {}", facilityId, e.getMessage(), e);
        }
    }

    /**
     * Walks every facility in the tenant's registry and rewrites its AMC snapshot on the health
     * facility index - the backfill counterpart to the per-facility sync the AMC flows trigger.
     *
     * <p>Facility-driven rather than configuration-driven on purpose. Iterating AMC configurations
     * would only ever touch facilities that have one, leaving a facility whose AMC was deleted (or
     * that was indexed before AMC fields existed) carrying stale or absent values forever. Walking
     * facilities instead means every document ends up either with its real AMC data or with an
     * explicit {@code amcApplicable = No} and blanked cycles.
     *
     * <p>Safe to re-run: each facility's snapshot is recomputed from the database and overwrites
     * whatever the index held, so a partial run is repaired simply by running it again.
     *
     * <p>Per-facility failures are counted and skipped rather than aborting - one unreachable
     * facility must not cost the remaining thousands.
     */
    public FacilityAmcBackfillResponse backfillFacilityAmcIndex(RequestInfo requestInfo, String tenantId) {
        FacilityAmcBackfillResponse result = FacilityAmcBackfillResponse.builder()
                .facilitiesScanned(0)
                .facilitiesWithAmc(0)
                .facilitiesIndexed(0)
                .facilitiesNotInIndex(0)
                .facilitiesFailed(0)
                .build();

        int offset = 0;
        int page = 0;
        while (page < BACKFILL_MAX_PAGES) {
            List<Facility> facilities = fetchFacilityPage(requestInfo, tenantId, offset);
            if (facilities.isEmpty()) {
                break;
            }
            backfillFacilityBatch(requestInfo, tenantId, facilities, result);
            log.info("AMC facility-index backfill tenantId={} offset={} pageSize={} -> scanned={} withAmc={} "
                            + "indexed={} notInIndex={} failed={}", tenantId, offset, facilities.size(),
                    result.getFacilitiesScanned(), result.getFacilitiesWithAmc(), result.getFacilitiesIndexed(),
                    result.getFacilitiesNotInIndex(), result.getFacilitiesFailed());

            if (facilities.size() < BACKFILL_FACILITY_PAGE_SIZE) {
                break;
            }
            offset += BACKFILL_FACILITY_PAGE_SIZE;
            page++;
        }
        if (page >= BACKFILL_MAX_PAGES) {
            log.error("AMC facility-index backfill for tenantId={} stopped at the {}-page safety cap; the run is "
                    + "incomplete.", tenantId, BACKFILL_MAX_PAGES);
        }

        log.info("AMC facility-index backfill complete for tenantId={}: scanned={} withAmc={} indexed={} "
                        + "notInIndex={} failed={}", tenantId, result.getFacilitiesScanned(),
                result.getFacilitiesWithAmc(), result.getFacilitiesIndexed(), result.getFacilitiesNotInIndex(),
                result.getFacilitiesFailed());
        return result;
    }

    /**
     * Indexes one page of facilities, prefetching the AMC data the whole page needs in a handful of
     * queries instead of per facility: the configurations in one paged search, their visits in
     * another, and every mapped vendor in one batched HRMS lookup. The snapshot itself still goes
     * through {@link #buildAmcIndexFields}, so the field-by-field shape matches the live sync.
     *
     * <p>One value can legitimately differ: the backfill recovers a missing {@code actualVisitDate}
     * from workflow history, which the live sync does not do (it would cost a workflow call on every
     * AMC transition, and the visit it was triggered by already carries a freshly stamped date). So a
     * backfilled document can hold an {@code amcVisitDate} the live sync would have left null, for
     * legacy visits whose date was never written to the visits table.
     */
    private void backfillFacilityBatch(RequestInfo requestInfo, String tenantId, List<Facility> facilities,
                                       FacilityAmcBackfillResponse result) {
        // Keyed by id so a facility repeated within a page is indexed once, while still keeping the
        // Facility itself around - the index update has to be addressed to the facility's own tenant.
        Map<String, Facility> facilityById = new LinkedHashMap<>();
        for (Facility facility : facilities) {
            String facilityId = facility.getFacilityId();
            if (facilityId != null && !facilityId.isBlank()) {
                facilityById.putIfAbsent(facilityId, facility);
            }
        }
        List<String> facilityIds = new ArrayList<>(facilityById.keySet());

        Map<String, AmcConfiguration> configByFacilityId =
                fetchLatestAmcConfigurationsByFacilityId(facilityIds, tenantId, requestInfo);
        Map<String, List<ScheduledVisit>> visitsByConfigId =
                fetchActiveVisitsByConfigurationId(configByFacilityId.values(), tenantId, requestInfo);
        Map<String, org.egov.amc.web.models.User> vendorByConfigId =
                resolveMappedVendors(configByFacilityId.values(), requestInfo);

        for (Map.Entry<String, Facility> entry : facilityById.entrySet()) {
            String facilityId = entry.getKey();
            result.setFacilitiesScanned(result.getFacilitiesScanned() + 1);
            try {
                AmcConfiguration config = configByFacilityId.get(facilityId);
                List<ScheduledVisit> visits = config == null ? List.of()
                        : visitsByConfigId.getOrDefault(config.getId(), List.of());
                org.egov.amc.web.models.User mappedVendor =
                        config == null ? null : vendorByConfigId.get(config.getId());
                if (config != null) {
                    result.setFacilitiesWithAmc(result.getFacilitiesWithAmc() + 1);
                }

                // The facility's own tenant, not the requested one: a state-level tenantId matches
                // sub-tenant facilities in the search, but the index update matches Data.tenantId
                // exactly, so pushing "in" for a facility living in "in.karnataka" would hit nothing.
                String facilityTenantId = entry.getValue().getTenantId() != null
                        ? entry.getValue().getTenantId() : tenantId;
                int updated = pushToFacilityIndex(facilityId, facilityTenantId,
                        buildAmcIndexFields(config, visits, mappedVendor));
                if (updated > 0) {
                    result.setFacilitiesIndexed(result.getFacilitiesIndexed() + 1);
                } else {
                    result.setFacilitiesNotInIndex(result.getFacilitiesNotInIndex() + 1);
                }
            } catch (Exception e) {
                result.setFacilitiesFailed(result.getFacilitiesFailed() + 1);
                log.error("AMC facility-index backfill failed for facilityId={} tenantId={}: {}",
                        facilityId, tenantId, e.getMessage(), e);
            }
        }
    }

    /**
     * Rows per page for the prefetch queries, read from the repository's own cap rather than
     * hardcoded. Both query builders silently clamp a larger limit down to {@code project.search.max.limit},
     * so asking for more than the cap would return exactly the cap - and the "a short page means the
     * last page" check would read that as the end of the data and quietly drop everything after it.
     */
    private int backfillDbPageSize() {
        Integer maxLimit = amcServiceConfiguration.getMaxLimit();
        return maxLimit == null || maxLimit < 1 ? BACKFILL_DB_PAGE_SIZE_FALLBACK : maxLimit;
    }

    /** One page of the tenant's facilities, or an empty list when facility-service returns nothing. */
    private List<Facility> fetchFacilityPage(RequestInfo requestInfo, String tenantId, int offset) {
        String url = amcServiceConfiguration.getFacilityServiceHost()
                + amcServiceConfiguration.getFacilityBulkSearchPath();
        FacilityBulkSearchApiRequest body = FacilityBulkSearchApiRequest.builder()
                .requestInfo(requestInfo)
                .facility(FacilityBulkSearchCriteria.forTenantPage(
                        List.of(tenantId), BACKFILL_FACILITY_PAGE_SIZE, offset))
                .build();
        Object response = requestRepository.fetchResult(new StringBuilder(url), body);
        FacilitySearchResponse parsed = mapper.convertValue(response, FacilitySearchResponse.class);
        return parsed == null || parsed.getFacilities() == null ? List.of() : parsed.getFacilities();
    }

    /**
     * The latest active AMC configuration per facility for a whole batch, in one paged search rather
     * than one search per facility. Facilities with no configuration are simply absent from the map.
     */
    private Map<String, AmcConfiguration> fetchLatestAmcConfigurationsByFacilityId(
            List<String> facilityIds, String tenantId, RequestInfo requestInfo) {
        Map<String, AmcConfiguration> latestByFacilityId = new LinkedHashMap<>();
        if (facilityIds.isEmpty()) {
            return latestByFacilityId;
        }

        Map<String, List<AmcConfiguration>> configsByFacilityId = new LinkedHashMap<>();
        int pageSize = backfillDbPageSize();
        int offset = 0;
        while (true) {
            AmcConfigurationSearchCriteria criteria = AmcConfigurationSearchCriteria.builder()
                    .facilityIds(facilityIds)
                    .statuses(List.of(ACTIVE_STATUS))
                    .isActive(true)
                    .build();
            AmcConfigurationSearchRequest request = AmcConfigurationSearchRequest.builder()
                    .RequestInfo(requestInfo)
                    .searchCriteria(criteria)
                    .build();
            List<AmcConfiguration> pageOfConfigs = amcConfigurationRepository
                    .getAmcConfiguration(request, pageSize, offset, tenantId, false, null);
            if (pageOfConfigs == null || pageOfConfigs.isEmpty()) {
                break;
            }
            for (AmcConfiguration config : pageOfConfigs) {
                if (config.getFacilityId() != null) {
                    configsByFacilityId.computeIfAbsent(config.getFacilityId(), k -> new ArrayList<>()).add(config);
                }
            }
            if (pageOfConfigs.size() < pageSize) {
                break;
            }
            offset += pageSize;
        }

        configsByFacilityId.forEach((facilityId, configs) -> {
            AmcConfiguration latest = pickLatestConfiguration(configs);
            if (latest != null) {
                latestByFacilityId.put(facilityId, latest);
            }
        });
        return latestByFacilityId;
    }

    /** Active scheduled visits for a batch's configurations, grouped by configuration id. */
    private Map<String, List<ScheduledVisit>> fetchActiveVisitsByConfigurationId(
            Collection<AmcConfiguration> configs, String tenantId, RequestInfo requestInfo) {
        Map<String, List<ScheduledVisit>> visitsByConfigId = new LinkedHashMap<>();
        List<String> configIds = configs.stream()
                .map(AmcConfiguration::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (configIds.isEmpty()) {
            return visitsByConfigId;
        }

        int pageSize = backfillDbPageSize();
        int offset = 0;
        while (true) {
            ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder()
                    .amcConfigurationIds(configIds)
                    .isActive(true)
                    .build();
            ScheduledVisitSearchRequest request = ScheduledVisitSearchRequest.builder()
                    .RequestInfo(requestInfo)
                    .searchCriteria(criteria)
                    .build();
            List<ScheduledVisit> pageOfVisits = scheduledVisitRepository
                    .getScheduledVisit(request, pageSize, offset, tenantId, false, null);
            if (pageOfVisits == null || pageOfVisits.isEmpty()) {
                break;
            }
            for (ScheduledVisit visit : pageOfVisits) {
                if (visit.getAmcConfigurationId() != null) {
                    visitsByConfigId.computeIfAbsent(visit.getAmcConfigurationId(), k -> new ArrayList<>()).add(visit);
                }
            }
            if (pageOfVisits.size() < pageSize) {
                break;
            }
            offset += pageSize;
        }

        // Same recovery the visit reindex performs. actual_visit_date only started being stamped part
        // way through, so legacy visits hold NULL in the table and the real date survives only in
        // workflow history. Without this the backfill would write null over an amcVisitDate that a
        // previous visit reindex had already recovered onto the index - a repair job that quietly
        // undoes a repair. Enrichment is in memory, so it has to be redone by every path that indexes.
        visitsByConfigId.values().forEach(visits ->
                actualVisitDateEnricher.enrichActualVisitDateFromWorkflow(requestInfo, visits));
        return visitsByConfigId;
    }

    /**
     * Mapped vendor per configuration for a whole batch, resolved from a single HRMS lookup over
     * every assignee in the batch instead of one lookup per configuration. Selection matches the
     * per-facility path exactly: the configuration's first active assignee holding the AMC field-staff
     * role wins, and anything unresolved is left out so the snapshot writes a null.
     */
    private Map<String, org.egov.amc.web.models.User> resolveMappedVendors(Collection<AmcConfiguration> configs,
                                                                           RequestInfo requestInfo) {
        Map<String, org.egov.amc.web.models.User> vendorByConfigId = new LinkedHashMap<>();
        String roleCode = mappedVendorUtil.getMappedVendorRoleCode();
        if (roleCode == null || roleCode.trim().isEmpty()) {
            log.warn("amc.mapped.vendor.role.code is not configured; backfilling {} AMC configuration(s) without "
                    + "an AMC mapped vendor.", configs.size());
            return vendorByConfigId;
        }

        LinkedHashSet<String> allAssigneeUuids = new LinkedHashSet<>();
        for (AmcConfiguration config : configs) {
            allAssigneeUuids.addAll(activeAssigneeUuids(config));
        }
        if (allAssigneeUuids.isEmpty()) {
            return vendorByConfigId;
        }

        Map<String, org.egov.amc.web.models.User> fieldStaffByUuid =
                mappedVendorUtil.getFieldStaffByUuid(requestInfo, new ArrayList<>(allAssigneeUuids), roleCode);
        for (AmcConfiguration config : configs) {
            if (config.getId() == null) {
                continue;
            }
            for (String assigneeUuid : activeAssigneeUuids(config)) {
                org.egov.amc.web.models.User fieldStaff = fieldStaffByUuid.get(assigneeUuid);
                if (fieldStaff != null) {
                    vendorByConfigId.put(config.getId(), fieldStaff);
                    break;
                }
            }
        }
        return vendorByConfigId;
    }

    private AmcConfiguration fetchLatestAmcConfiguration(String facilityId, String tenantId, RequestInfo requestInfo) {
        AmcConfigurationSearchCriteria criteria = AmcConfigurationSearchCriteria.builder()
                .facilityIds(List.of(facilityId))
                .statuses(List.of(ACTIVE_STATUS))
                .isActive(true)
                .build();
        AmcConfigurationSearchRequest request = AmcConfigurationSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();
        List<AmcConfiguration> configs = amcConfigurationRepository
                .getAmcConfiguration(request, MAX_CONFIGS_PER_FACILITY, 0, tenantId, false, null);
        return pickLatestConfiguration(configs);
    }

    /**
     * The one configuration the index should reflect when a facility carries several.
     *
     * <p>"Latest" is judged on the configuration's own start date, not on when its row was last
     * touched: editing a long-finished contract must not let it outrank the contract actually running
     * today. Created-time then id break ties, so a facility with two configurations starting on the
     * same day resolves to the same one on every run instead of flipping between them - which would
     * otherwise make the indexed AMC dates change on each backfill for no real reason.
     */
    static AmcConfiguration pickLatestConfiguration(List<AmcConfiguration> configs) {
        if (configs == null || configs.isEmpty()) {
            return null;
        }
        return configs.stream()
                .filter(Objects::nonNull)
                .max(Comparator
                        .comparing(AmcConfiguration::getConfigurationStartDate, Comparator.nullsFirst(Long::compareTo))
                        .thenComparing(FacilityAmcIndexSyncService::createdTimeOf, Comparator.nullsFirst(Long::compareTo))
                        .thenComparing(AmcConfiguration::getId, Comparator.nullsFirst(String::compareTo)))
                .orElse(null);
    }

    private static Long createdTimeOf(AmcConfiguration config) {
        return config.getAuditDetails() == null ? null : config.getAuditDetails().getCreatedTime();
    }

    /**
     * The AMC snapshot for one facility, built purely from what it is handed. Both the per-facility
     * live sync and the bulk backfill go through here so the two can never drift into indexing
     * different values for the same underlying data.
     *
     * @param config       the facility's latest AMC configuration, or null when it has none
     * @param visits       the configuration's active scheduled visits (ignored when config is null)
     * @param mappedVendor the resolved AMC field staff, or null when there is none
     */
    Map<String, Object> buildAmcIndexFields(AmcConfiguration config, List<ScheduledVisit> visits,
                                            org.egov.amc.web.models.User mappedVendor) {
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
        // Written unconditionally, null included, so clearing an AMC's assignment actually clears the
        // indexed value rather than leaving a stale name behind.
        fields.put("amcMappedVendorName", mappedVendor == null ? null : mappedVendor.getName());
        fields.put("amcMappedVendorUserName", mappedVendor == null ? null : mappedVendor.getUserName());

        // Copied before sorting: the bulk backfill shares one visit list per configuration across the
        // facilities it fans out to, so sorting in place would mutate a caller's collection.
        List<ScheduledVisit> orderedVisits = new ArrayList<>(visits);
        orderedVisits.sort(Comparator.comparing(ScheduledVisit::getVisitNumber, Comparator.nullsLast(Integer::compareTo)));
        if (orderedVisits.size() > MAX_CYCLES) {
            log.warn("AMC configuration {} has {} active scheduled visit(s); capping facility-index sync at the first {} cycles",
                    config.getId(), orderedVisits.size(), MAX_CYCLES);
        }
        for (ScheduledVisit visit : orderedVisits) {
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
     * <p>A missing role config or a failed HRMS lookup degrades to null rather than throwing, so the
     * index distinguishes "nobody mapped" from a real name instead of losing the whole snapshot.
     */
    private org.egov.amc.web.models.User resolveMappedVendor(AmcConfiguration config, RequestInfo requestInfo) {
        if (config == null) {
            return null;
        }
        String roleCode = mappedVendorUtil.getMappedVendorRoleCode();
        if (roleCode == null || roleCode.trim().isEmpty()) {
            log.warn("amc.mapped.vendor.role.code is not configured; indexing AMC configuration {} without "
                    + "an AMC mapped vendor.", config.getId());
            return null;
        }
        org.egov.amc.web.models.User fieldStaff =
                mappedVendorUtil.resolveFieldStaff(requestInfo, activeAssigneeUuids(config), roleCode);
        if (fieldStaff == null) {
            log.info("No active assignee of AMC configuration {} holds role {}; indexing it without an "
                    + "AMC mapped vendor.", config.getId(), roleCode);
        }
        return fieldStaff;
    }

    /** The configuration's active assignees, in assignment order - first match wins as mapped vendor. */
    private static List<String> activeAssigneeUuids(AmcConfiguration config) {
        if (config.getAssignments() == null) {
            return List.of();
        }
        return config.getAssignments().stream()
                .filter(AmcConfigurationAssignment::isActive)
                .map(AmcConfigurationAssignment::getAssignedUser)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
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
     *
     * @return indexed documents the endpoint reported updating. {@code 0} is a normal outcome, not a
     *         failure: it means the facility has no document in the index yet (typically because it
     *         is not ONM-ready). The backfill counts those separately so a run that quietly matched
     *         nothing is visible instead of reading as a clean sweep.
     */
    private int pushToFacilityIndex(String facilityId, String tenantId, Map<String, Object> amcFields) {
        FacilityAmcIndexUpdateRequest updateRequest = FacilityAmcIndexUpdateRequest.builder()
                .requestInfo(buildFacilityServiceSystemRequestInfo(tenantId))
                .facilityId(facilityId)
                .tenantId(tenantId)
                .amcFields(amcFields)
                .build();

        String url = amcServiceConfiguration.getFacilityServiceHost()
                + amcServiceConfiguration.getFacilityAmcIndexUpdateUrl();
        Object response = requestRepository.fetchResult(new StringBuilder(url), updateRequest);
        log.info("Pushed AMC snapshot to the health facility index for facilityId={}", facilityId);
        return extractUpdatedCount(response);
    }

    private static int extractUpdatedCount(Object response) {
        if (response instanceof Map<?, ?> body && body.get("updated") instanceof Number updated) {
            return updated.intValue();
        }
        return 0;
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
