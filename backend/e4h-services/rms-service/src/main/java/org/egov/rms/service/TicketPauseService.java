package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.PausedFacilityItem;
import org.egov.rms.model.TicketPauseManageRequest;
import org.egov.rms.model.TicketPausePayload;
import org.egov.rms.model.TicketPauseResponse;
import org.egov.rms.model.TicketPauseSearchRequest;
import org.egov.rms.model.TicketPausedFacilityListRequest;
import org.egov.rms.model.TicketPausedFacilityListResponse;
import org.egov.rms.repository.TicketPauseRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketPauseService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final TicketPauseRepository ticketPauseRepository;
    private final RMSConfiguration config;
    private final TicketPauseAuditEventPublisher ticketPauseAuditEventPublisher;
    private final UserDirectoryService userDirectoryService;

    public TicketPauseResponse managePause(TicketPauseManageRequest request) {
        if (!isPauseEnabled()) {
            log.warn("Ticket pause manage requested while feature is disabled; ignoring request");
            throw new IllegalArgumentException("RMS ticket pause feature is disabled");
        }
        if (request == null || request.getTicketPause() == null || request.getTicketPause().getAction() == null) {
            throw new IllegalArgumentException("action is required");
        }
        TicketPausePayload payload = request.getTicketPause();
        validateFacilityId(payload.getFacilityId());
        log.info("Ticket pause manage request received: action={}, facilityId={}",
                payload.getAction(), payload.getFacilityId());

        if (payload.getAction() == TicketPauseManageRequest.Action.PAUSE) {
            return pause(request.getRequestInfo(), payload);
        }
        if (payload.getAction() == TicketPauseManageRequest.Action.RESUME) {
            String facilityId = payload.getFacilityId().trim();
            Optional<TicketPauseRepository.TicketPauseRecord> existingPause =
                    ticketPauseRepository.findActivePauseByFacility(facilityId, Instant.now());
            String tenantId = extractTenantId(request.getRequestInfo(), payload.getTenantId());
            String requestedByDisplay = extractRequestedByForAudit(request.getRequestInfo());
            if (!existingPause.isPresent()) {
                log.info("Ticket resume skipped: no active pause found for facilityId={}", facilityId);
                return TicketPauseResponse.success(
                        facilityId,
                        false,
                        null,
                        0L,
                        null,
                        "Facility was not paused"
                );
            }

            TicketPauseRepository.TicketPauseRecord record = existingPause.get();
            int updated = ticketPauseRepository.deactivatePause(facilityId, record.getPausedUntil());
            log.info("Ticket resume processed: facilityId={}, updatedRows={}, pausedUntilSnapshot={}",
                    facilityId, updated, record.getPausedUntil());
            publishPauseAuditSafely(
                    request.getRequestInfo(),
                    TicketPauseManageRequest.Action.RESUME,
                    facilityId,
                    normalize(payload.getFacilityName()),
                    normalize(payload.getBoundaryCode()),
                    null,
                    normalize(payload.getReason()),
                    requestedByDisplay,
                    false,
                    tenantId,
                    existingPause
            );
            return TicketPauseResponse.success(
                    facilityId,
                    false,
                    null,
                    null,
                    null,
                    "Auto ticket creation resumed successfully"
            );
        }
        throw new IllegalArgumentException("Unsupported action: " + payload.getAction());
    }

    public TicketPauseResponse getPauseState(TicketPauseSearchRequest request) {
        if (!isPauseEnabled()) {
            log.warn("Ticket pause search requested while feature is disabled; treating as not paused");
            if (request != null && request.getTicketPause() != null
                    && StringUtils.hasText(request.getTicketPause().getFacilityId())) {
                String facilityId = request.getTicketPause().getFacilityId().trim();
                return TicketPauseResponse.success(
                        facilityId,
                        false,
                        null,
                        0L,
                        null,
                        "Facility is not paused"
                );
            }
            throw new IllegalArgumentException("RMS ticket pause feature is disabled");
        }
        if (request == null || request.getTicketPause() == null) {
            throw new IllegalArgumentException("request is required");
        }
        validateFacilityId(request.getTicketPause().getFacilityId());
        String facilityId = request.getTicketPause().getFacilityId().trim();
        log.debug("Pause state lookup started: facilityId={}", facilityId);
        Optional<TicketPauseRepository.TicketPauseRecord> record = ticketPauseRepository.findActivePauseByFacility(facilityId, Instant.now());
        if (!record.isPresent()) {
            log.debug("Pause state lookup result: facilityId={}, isPaused=false", facilityId);
            return TicketPauseResponse.success(
                    facilityId,
                    false,
                    null,
                    0L,
                    null,
                    "Facility is not paused"
            );
        }
        TicketPauseRepository.TicketPauseRecord row = record.get();
        long daysLeft = Math.max(0, ChronoUnit.DAYS.between(Instant.now(), row.getPausedUntil()));
        log.debug("Pause state lookup result: facilityId={}, isPaused=true, pausedUntil={}, daysLeft={}",
                facilityId, row.getPausedUntil(), daysLeft);
        return TicketPauseResponse.success(
                facilityId,
                true,
                row.getPausedUntil(),
                daysLeft,
                row.getReason(),
                "Facility is paused"
        );
    }

    public TicketPausedFacilityListResponse listPausedFacilities(TicketPausedFacilityListRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getFacility() == null) {
            throw new IllegalArgumentException("Facility is required");
        }
        List<String> boundaryCodes = extractBoundaryFilters(request);
        if (boundaryCodes.isEmpty()) {
            throw new IllegalArgumentException("At least one of Facility.state, Facility.district, Facility.block or Facility.boundaryCodes is required");
        }
        int offset = extractOffset(request);
        int limit = extractLimit(request);
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        limit = Math.min(limit, MAX_LIMIT);
        log.info("Paused facilities list request: boundaryFiltersCount={}, offset={}, limit={}",
                boundaryCodes.size(), offset, limit);

        List<PausedFacilityItem> items = ticketPauseRepository.listActivePausedFacilities(boundaryCodes, offset, limit);
        enrichPausedByDisplayNames(request, items);
        long totalCount = ticketPauseRepository.countActivePausedFacilities(boundaryCodes);
        log.info("Paused facilities list response: totalCount={}, returnedCount={}", totalCount, items.size());
        return TicketPausedFacilityListResponse.success(totalCount, items);
    }

    private int extractOffset(TicketPausedFacilityListRequest request) {
        return request.getFacility().getOffset() == null ? 0 : Math.max(0, request.getFacility().getOffset());
    }

    private int extractLimit(TicketPausedFacilityListRequest request) {
        return request.getFacility().getLimit() == null ? DEFAULT_LIMIT : request.getFacility().getLimit();
    }

    private List<String> extractBoundaryFilters(TicketPausedFacilityListRequest request) {
        Set<String> merged = new LinkedHashSet<>();
        merged.addAll(normalizeBoundaryCodes(request.getFacility().getState()));
        merged.addAll(normalizeBoundaryCodes(request.getFacility().getDistrict()));
        merged.addAll(normalizeBoundaryCodes(request.getFacility().getBlock()));
        merged.addAll(normalizeBoundaryCodes(request.getFacility().getBoundaryCodes()));
        return new ArrayList<>(merged);
    }

    public boolean isFacilityPaused(String facilityId, Instant now) {
        if (!isPauseEnabled() || !StringUtils.hasText(facilityId)) {
            return false;
        }
        boolean paused = ticketPauseRepository.findActivePauseByFacility(facilityId.trim(), now != null ? now : Instant.now()).isPresent();
        if (paused) {
            log.debug("Facility pause check matched active pause: facilityId={}", facilityId);
        }
        return paused;
    }

    private TicketPauseResponse pause(RequestInfo requestInfo, TicketPausePayload payload) {
        if (payload.getPausedUntil() == null) {
            throw new IllegalArgumentException("pausedUntil is required when action is PAUSE");
        }
        Instant now = Instant.now();
        Instant pausedUntil = payload.getPausedUntil();
        if (!pausedUntil.isAfter(now)) {
            throw new IllegalArgumentException("pausedUntil must be a future timestamp when action is PAUSE");
        }

        String facilityId = payload.getFacilityId().trim();
        String tenantId = extractTenantId(requestInfo, payload.getTenantId());
        String requestedBy = extractRequestedBy(requestInfo);
        String requestedByDisplay = extractRequestedByForAudit(requestInfo);
        String normalizedFacilityName = normalize(payload.getFacilityName());
        String normalizedBoundaryCode = normalize(payload.getBoundaryCode());
        String normalizedReason = normalize(payload.getReason());
        ticketPauseRepository.upsertPause(
                facilityId,
                normalizedFacilityName,
                normalizedBoundaryCode,
                pausedUntil,
                normalizedReason,
                requestedBy,
                tenantId
        );
        log.info("Ticket pause applied: facilityId={}, pausedUntil={}, requestedBy={}", facilityId, pausedUntil, requestedBy);
        publishPauseAuditSafely(
                requestInfo,
                TicketPauseManageRequest.Action.PAUSE,
                facilityId,
                normalizedFacilityName,
                normalizedBoundaryCode,
                pausedUntil,
                normalizedReason,
                requestedByDisplay,
                true,
                tenantId,
                Optional.empty()
        );
        return TicketPauseResponse.success(
                facilityId,
                true,
                pausedUntil,
                Math.max(0, ChronoUnit.DAYS.between(now, pausedUntil)),
                normalizedReason,
                "Auto ticket creation paused successfully"
        );
    }

    private String extractTenantId(RequestInfo requestInfo, String payloadTenantId) {
        if (StringUtils.hasText(payloadTenantId)) {
            return payloadTenantId.trim();
        }
        if (requestInfo != null
                && requestInfo.getUserInfo() != null
                && StringUtils.hasText(requestInfo.getUserInfo().getTenantId())) {
            return requestInfo.getUserInfo().getTenantId().trim();
        }
        return config.getDefaultTenantId();
    }

    private void enrichPausedByDisplayNames(TicketPausedFacilityListRequest request, List<PausedFacilityItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<String> pausedByUuids = new LinkedHashSet<>();
        for (PausedFacilityItem item : items) {
            if (item != null && StringUtils.hasText(item.getPausedBy())) {
                pausedByUuids.add(item.getPausedBy().trim());
            }
        }
        if (pausedByUuids.isEmpty()) {
            return;
        }

        String tenantId = extractTenantId(
                request != null ? request.getRequestInfo() : null,
                request != null && request.getFacility() != null
                        && request.getFacility().getTenantId() != null
                        && !request.getFacility().getTenantId().isEmpty()
                        ? request.getFacility().getTenantId().get(0)
                        : null
        );

        Map<String, String> uuidToDisplayName = userDirectoryService.getDisplayNamesByUuids(
                request != null ? request.getRequestInfo() : null,
                pausedByUuids,
                tenantId
        );

        for (PausedFacilityItem item : items) {
            if (item == null || !StringUtils.hasText(item.getPausedBy())) {
                continue;
            }
            String uuid = item.getPausedBy().trim();
            String displayName = uuidToDisplayName.get(uuid);
            if (StringUtils.hasText(displayName)) {
                item.setPausedBy(displayName);
            }
        }
    }

    private void publishPauseAuditSafely(
            RequestInfo requestInfo,
            TicketPauseManageRequest.Action action,
            String facilityId,
            String facilityName,
            String boundaryCode,
            Instant pausedUntil,
            String reason,
            String requestedBy,
            boolean isPaused,
            String tenantIdOverride,
            Optional<TicketPauseRepository.TicketPauseRecord> existingPause
    ) {
        try {
            String fallbackFacilityName = existingPause.map(TicketPauseRepository.TicketPauseRecord::getFacilityName).orElse(null);
            String fallbackBoundaryCode = existingPause.map(TicketPauseRepository.TicketPauseRecord::getBoundaryCode).orElse(null);
            String fallbackReason = existingPause.map(TicketPauseRepository.TicketPauseRecord::getReason).orElse(null);
            ticketPauseAuditEventPublisher.publishPauseEvent(
                    requestInfo,
                    action,
                    facilityId,
                    StringUtils.hasText(facilityName) ? facilityName : fallbackFacilityName,
                    StringUtils.hasText(boundaryCode) ? boundaryCode : fallbackBoundaryCode,
                    pausedUntil,
                    StringUtils.hasText(reason) ? reason : fallbackReason,
                    requestedBy,
                    isPaused,
                    tenantIdOverride
            );
        } catch (Exception e) {
            log.error("Failed to publish pause audit event for facilityId={}", facilityId, e);
        }
    }

    private void validateFacilityId(String facilityId) {
        if (!StringUtils.hasText(facilityId)) {
            throw new IllegalArgumentException("facilityId is required");
        }
    }

    private List<String> normalizeBoundaryCodes(List<String> boundaryCodes) {
        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (String code : boundaryCodes) {
            if (StringUtils.hasText(code)) {
                normalized.add(code.trim());
            }
        }
        return normalized;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String extractRequestedBy(RequestInfo requestInfo) {
        if (requestInfo == null) {
            return "SYSTEM";
        }
        try {
            User user = requestInfo.getUserInfo();
            if (user == null) {
                return "SYSTEM";
            }
            if (StringUtils.hasText(user.getUuid())) {
                return user.getUuid().trim();
            }
            if (StringUtils.hasText(user.getUserName())) {
                return user.getUserName().trim();
            }
            if (StringUtils.hasText(user.getName())) {
                return user.getName().trim();
            }
        } catch (Exception e) {
            log.warn("Failed to extract requestedBy from RequestInfo", e);
        }
        return "SYSTEM";
    }

    private String extractRequestedByForAudit(RequestInfo requestInfo) {
        if (requestInfo == null) {
            return "SYSTEM";
        }
        try {
            User user = requestInfo.getUserInfo();
            if (user == null) {
                return "SYSTEM";
            }
            if (StringUtils.hasText(user.getName())) {
                return user.getName().trim();
            }
            if (StringUtils.hasText(user.getUserName())) {
                return user.getUserName().trim();
            }
            if (StringUtils.hasText(user.getUuid())) {
                return user.getUuid().trim();
            }
        } catch (Exception e) {
            log.warn("Failed to extract audit requestedBy from RequestInfo", e);
        }
        return "SYSTEM";
    }

    private boolean isPauseEnabled() {
        return config.getTicketPauseEnabled() == null || config.getTicketPauseEnabled();
    }
}

