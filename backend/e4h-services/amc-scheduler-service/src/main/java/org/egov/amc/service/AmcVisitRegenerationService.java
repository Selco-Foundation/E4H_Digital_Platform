package org.egov.amc.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.repository.ScheduledVisitRepository;
import org.egov.amc.util.AmcConfigurationServiceUtil;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AmcConfigurationAssignment;
import org.egov.amc.web.models.ScheduledVisit;
import org.egov.amc.web.models.ScheduledVisitAssignment;
import org.egov.amc.web.models.ScheduledVisitRequest;
import org.egov.amc.web.models.ScheduledVisitSearchCriteria;
import org.egov.amc.web.models.ScheduledVisitSearchRequest;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.egov.amc.util.AmcConstants.APPROVED_STATUS;
import static org.egov.amc.util.AmcConstants.DRAFT_STATUS;
import static org.egov.amc.util.AmcConstants.EXPIRED_STATUS;

/**
 * Rebuilds the scheduled-visit series of an AMC configuration when its schedule changes
 * (durationMonths, visitFrequencyMonths and/or configurationStartDate).
 *
 * <p>This lives in its own bean rather than inside {@link AmcConfigurationService} on purpose:
 * {@code ScheduledVisitService} and {@code ScheduledVisitEnrichment} both inject
 * {@code AmcConfigurationService}, so reusing either of them from there would close a Spring
 * constructor-injection cycle and the context would fail to start. Everything this class depends on
 * (repository, producer, util, config) sits below the service layer, so no cycle is possible.
 */
@Service
@Slf4j
public class AmcVisitRegenerationService {

    /** Visits in these states represent work that actually happened - they are never rewritten. */
    private static final Set<String> TERMINAL_VISIT_STATUSES = Set.of(APPROVED_STATUS, EXPIRED_STATUS);

    private static final int VISIT_SEARCH_LIMIT = 1000;

    private final ScheduledVisitRepository scheduledVisitRepository;
    private final AmcConfigurationServiceUtil amcConfigurationServiceUtil;
    private final AMCServiceConfiguration amcServiceConfiguration;
    private final Producer producer;

    @Autowired
    public AmcVisitRegenerationService(ScheduledVisitRepository scheduledVisitRepository,
                                       AmcConfigurationServiceUtil amcConfigurationServiceUtil,
                                       AMCServiceConfiguration amcServiceConfiguration,
                                       Producer producer) {
        this.scheduledVisitRepository = scheduledVisitRepository;
        this.amcConfigurationServiceUtil = amcConfigurationServiceUtil;
        this.amcServiceConfiguration = amcServiceConfiguration;
        this.producer = producer;
    }

    /**
     * Regenerates the future visits of a configuration if - and only if - its schedule changed:
     * duration, visit frequency, or the AMC Start Date (configurationStartDate) itself. The latter
     * is set from the Installation Report Submission Date by default and can be corrected later by
     * re-uploading the AMC configuration Excel (see /amcConfigurationBulkIngest) - either kind of
     * change moves every not-yet-due visit date, so both need the same rebuild.
     *
     * <p>Visits that are terminal (APPROVED/EXPIRED) or already due are kept untouched; the
     * remaining ones are deleted and replaced by a fresh series computed from the updated start
     * date, end date and frequency. New visit numbers continue from the highest kept one, because
     * {@code ux_scheduled_visits_unique_visit_per_amc (amc_configuration_id, visit_number)} forbids
     * restarting at 1.
     *
     * @return the visits whose rows were rewritten - deactivated ones carry {@code isActive = false},
     *         regenerated ones are active - or an empty list when the series was left as-is. Callers
     *         need these to index the new schedule without re-reading it: the rows are persisted
     *         asynchronously through Kafka, so the DB does not hold them yet on return.
     */
    public List<ScheduledVisit> regenerateIfCadenceChanged(AmcConfiguration configurationFromDB,
                                                           AmcConfiguration updatedConfiguration,
                                                           RequestInfo requestInfo) {
        if (configurationFromDB == null || updatedConfiguration == null) {
            return List.of();
        }

        boolean scheduleChanged =
                !Objects.equals(configurationFromDB.getDurationMonths(), updatedConfiguration.getDurationMonths())
                        || !Objects.equals(configurationFromDB.getVisitFrequencyMonths(), updatedConfiguration.getVisitFrequencyMonths())
                        || !Objects.equals(configurationFromDB.getConfigurationStartDate(), updatedConfiguration.getConfigurationStartDate());
        if (!scheduleChanged) {
            log.debug("Schedule unchanged for configurationId: {}, visits left as-is", updatedConfiguration.getId());
            List.of();
        }

        Long startDate = updatedConfiguration.getConfigurationStartDate() != null
                ? updatedConfiguration.getConfigurationStartDate()
                : configurationFromDB.getConfigurationStartDate();
        Long endDate = updatedConfiguration.getConfigurationEndDate() != null
                ? updatedConfiguration.getConfigurationEndDate()
                : configurationFromDB.getConfigurationEndDate();
        Integer frequencyMonths = updatedConfiguration.getVisitFrequencyMonths() != null
                ? updatedConfiguration.getVisitFrequencyMonths()
                : configurationFromDB.getVisitFrequencyMonths();

        if (startDate == null || endDate == null || frequencyMonths == null || frequencyMonths <= 0) {
            log.warn("Skipping visit regeneration for configurationId: {} - incomplete schedule "
                            + "(startDate: {}, endDate: {}, frequencyMonths: {})",
                    updatedConfiguration.getId(), startDate, endDate, frequencyMonths);
            return List.of();
        }

        long now = System.currentTimeMillis();
        List<ScheduledVisit> existingVisits = fetchVisits(configurationFromDB, requestInfo);

        List<ScheduledVisit> visitsToKeep = new ArrayList<>();
        List<ScheduledVisit> visitsToDelete = new ArrayList<>();
        for (ScheduledVisit visit : existingVisits) {
            if (isImmutableVisit(visit, now)) {
                visitsToKeep.add(visit);
            } else {
                visitsToDelete.add(visit);
            }
        }

        // Anchor the new series after everything we are keeping, so a regenerated date never collides
        // with a visit that already took place.
        long lastKeptDate = visitsToKeep.stream()
                .map(ScheduledVisit::getScheduledDate)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L);
        int lastKeptVisitNumber = visitsToKeep.stream()
                .map(ScheduledVisit::getVisitNumber)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        long cutOff = Math.max(now, lastKeptDate);

        List<Long> regeneratedDates = endDate > startDate
                ? amcConfigurationServiceUtil.generateAmcVisits(startDate, endDate, frequencyMonths)
                : Collections.emptyList();
        List<Long> futureDates = regeneratedDates.stream().filter(date -> date > cutOff).toList();

        if (visitsToDelete.isEmpty() && futureDates.isEmpty()) {
            log.info("Cadence changed for configurationId: {} but no future visit to rewrite", updatedConfiguration.getId());
            return List.of();
        }

        List<ScheduledVisit> rewrittenVisits = new ArrayList<>();

        if (!visitsToDelete.isEmpty()) {
            log.info("Deactivating {} not-yet-due visit(s) of configurationId: {} before regeneration",
                    visitsToDelete.size(), updatedConfiguration.getId());
            deactivate(visitsToDelete, requestInfo);
            producer.push(amcServiceConfiguration.getDeleteScheduledVisitTopic(),
                    ScheduledVisitRequest.builder().requestInfo(requestInfo).scheduledVisits(visitsToDelete).build());
            rewrittenVisits.addAll(visitsToDelete);
        }

        if (!futureDates.isEmpty()) {
            List<ScheduledVisit> newVisits = buildVisits(
                    configurationFromDB, updatedConfiguration, requestInfo, futureDates,
                    lastKeptVisitNumber, lastKeptDate > 0 ? lastKeptDate : null, resolveFacilityName(existingVisits));
            log.info("Creating {} regenerated visit(s) for configurationId: {}", newVisits.size(), updatedConfiguration.getId());
            producer.push(amcServiceConfiguration.getSaveScheduledVisitTopic(),
                    ScheduledVisitRequest.builder().requestInfo(requestInfo).scheduledVisits(newVisits).build());
            rewrittenVisits.addAll(newVisits);
        }

        return rewrittenVisits;
    }

    /**
     * Marks visits as soft-deleted and stamps who did it. The audit details coming back from the
     * repository are those of the last real change, so they must be refreshed before the persister
     * writes them - otherwise the deactivation would be attributed to whoever touched the visit last.
     */
    private void deactivate(List<ScheduledVisit> visits, RequestInfo requestInfo) {
        for (ScheduledVisit visit : visits) {
            visit.setIsActive(Boolean.FALSE);
            visit.setAuditDetails(amcConfigurationServiceUtil.getAuditDetails(
                    requestInfo.getUserInfo().getUuid(), visit.getAuditDetails(), visit.getAuditDetails() == null));
        }
    }

    /** A visit is immutable once it is terminal, or once its scheduled date has come. */
    private boolean isImmutableVisit(ScheduledVisit visit, long now) {
        String status = visit.getStatus() == null ? "" : visit.getStatus().trim().toUpperCase();
        if (TERMINAL_VISIT_STATUSES.contains(status)) {
            return true;
        }
        return visit.getScheduledDate() != null && visit.getScheduledDate() <= now;
    }

    private List<ScheduledVisit> fetchVisits(AmcConfiguration configuration, RequestInfo requestInfo) {
        ScheduledVisitSearchCriteria criteria = ScheduledVisitSearchCriteria.builder()
                .tenantId(configuration.getTenantId())
                .amcConfigurationIds(List.of(configuration.getId()))
                .build();
        ScheduledVisitSearchRequest searchRequest = ScheduledVisitSearchRequest.builder()
                .RequestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();
        List<ScheduledVisit> visits = scheduledVisitRepository.getScheduledVisit(
                searchRequest, VISIT_SEARCH_LIMIT, 0, configuration.getTenantId(), null, null);
        return visits == null ? Collections.emptyList() : visits;
    }

    /** All visits of a configuration share one facility, so any existing row carries the right name. */
    private String resolveFacilityName(List<ScheduledVisit> existingVisits) {
        return existingVisits.stream()
                .map(ScheduledVisit::getFacilityName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private List<ScheduledVisit> buildVisits(AmcConfiguration configurationFromDB,
                                             AmcConfiguration updatedConfiguration,
                                             RequestInfo requestInfo,
                                             List<Long> visitDates,
                                             int startingVisitNumber,
                                             Long previousVisitDate,
                                             String facilityName) {
        List<AmcConfigurationAssignment> configurationAssignments =
                updatedConfiguration.getAssignments() != null && !updatedConfiguration.getAssignments().isEmpty()
                        ? updatedConfiguration.getAssignments()
                        : configurationFromDB.getAssignments();

        List<ScheduledVisit> visits = new ArrayList<>();
        int visitNumber = startingVisitNumber;
        Long lastVisitDate = previousVisitDate;

        for (Long visitDate : visitDates) {
            visitNumber++;
            AuditDetails auditDetails = amcConfigurationServiceUtil.getAuditDetails(
                    requestInfo.getUserInfo().getUuid(), null, true);

            List<ScheduledVisitAssignment> assignments = new ArrayList<>();
            if (configurationAssignments != null) {
                for (AmcConfigurationAssignment configurationAssignment : configurationAssignments) {
                    assignments.add(ScheduledVisitAssignment.builder()
                            .id(UUID.randomUUID().toString())
                            .tenantId(configurationFromDB.getTenantId())
                            .assignedUser(configurationAssignment.getAssignedUser())
                            .isActive(true)
                            .auditDetails(auditDetails)
                            .build());
                }
            }

            visits.add(ScheduledVisit.builder()
                    .id(UUID.randomUUID().toString())
                    .tenantId(configurationFromDB.getTenantId())
                    .amcConfigurationId(configurationFromDB.getId())
                    .projectId(configurationFromDB.getProjectId())
                    .facilityId(configurationFromDB.getFacilityId())
                    .facilityName(facilityName)
                    .lastVisitDate(lastVisitDate)
                    .visitNumber(visitNumber)
                    .scheduledDate(visitDate)
                    .status(DRAFT_STATUS)
                    .assignments(assignments)
                    .auditDetails(auditDetails)
                    .build());
            lastVisitDate = visitDate;
        }
        return visits;
    }
}
