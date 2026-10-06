package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.egov.rms.model.FacilityDetails;
import org.egov.rms.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * Enforces business rules that suppress new IM tickets when certain open incidents
 * already exist for the same RMS facility.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketCreationGuardService {

    private final AlertRepository alertRepository;
    private final TicketPauseService ticketPauseService;
    private final FacilityEligibilityService facilityEligibilityService;
    private final FacilityServiceClient facilityServiceClient;
    private final RMSConfiguration config;

    /**
     * @return true when this alert must not result in a new ticket (open blocking incidents in IM).
     */
    public boolean shouldBlockTicketCreation(Alert alert) {
        if (alert == null || !StringUtils.hasText(alert.getFacilityId())) {
            return false;
        }
        String facilityId = alert.getFacilityId();
        String hfrId = alert.getHfrId();

        if (!facilityEligibilityService.isEligibleByHfrOrFacilityId(hfrId, facilityId)) {
            log.info(
                    "TICKET POLICY: Skipping ticket — facility not eligible under MDMS district allowlist (facilityId={}, hfrId={}, alert type {}, subType {})",
                    facilityId, hfrId, alert.getAlertType(), alert.getAlertSubType());
            return true;
        }

        if (isPausedUnderAnyKnownId(facilityId, hfrId, Instant.now())) {
            log.info(
                    "TICKET POLICY: Skipping ticket — facility {} is currently paused for RMS auto ticket creation (alert type {}, subType {})",
                    facilityId, alert.getAlertType(), alert.getAlertSubType());
            return true;
        }

        // Rule set 2: Any open inverter shutdown incident blocks all new RMS tickets.
        if (hasOpenInverterShutdownIncident(facilityId)) {
            log.info(
                    "TICKET POLICY: Skipping ticket — open inverter shutdown incident for facility {} (alert type {}, subType {})",
                    facilityId, alert.getAlertType(), alert.getAlertSubType());
            return true;
        }

        // Rule set 1: Open battery disconnected OR deep-discharge incident blocks battery, inverter shutdown, and running-on-grid tickets.
        if (hasBatteryRootCauseBlockingIncident(facilityId) && isInBatteryIsolationScope(alert)) {
            log.info(
                    "TICKET POLICY: Skipping ticket — open battery disconnected or deep-discharge incident for facility {} blocks this category (alert type {}, subType {})",
                    facilityId, alert.getAlertType(), alert.getAlertSubType());
            return true;
        }

        return false;
    }

    /**
     * Alerts carry the RMS center id in {@code facilityId}, while pause records are keyed on the
     * registry facility id captured by the pause screen. Matching on the alert id alone therefore
     * never hits a pause created from the UI, so resolve the registry id from the alert's
     * identifier and check that too.
     */
    private boolean isPausedUnderAnyKnownId(String facilityId, String identifier, Instant now) {
        if (ticketPauseService.isFacilityPaused(facilityId, now)) {
            return true;
        }
        if (!StringUtils.hasText(identifier)) {
            return false;
        }

        String registryFacilityId = resolveRegistryFacilityId(identifier);
        if (registryFacilityId == null || registryFacilityId.equals(facilityId)) {
            return false;
        }
        return ticketPauseService.isFacilityPaused(registryFacilityId, now);
    }

    /**
     * RMS reports one identifier per facility in its HFRID field, and for some facilities that
     * value is actually a NIN, so an unmatched identifier is retried as a NIN before giving up.
     */
    private String resolveRegistryFacilityId(String identifier) {
        String tenantId = config.getDefaultTenantId();

        FacilityDetails byHfr = facilityServiceClient.getFacilityByHfrId(identifier, tenantId);
        if (byHfr != null && StringUtils.hasText(byHfr.getFacilityId())) {
            return byHfr.getFacilityId().trim();
        }

        FacilityDetails byNin = facilityServiceClient.getFacilityByNinId(identifier, tenantId);
        if (byNin != null && StringUtils.hasText(byNin.getFacilityId())) {
            log.info("TICKET POLICY: Alert identifier {} resolved as a NIN, not an HFR id", identifier);
            return byNin.getFacilityId().trim();
        }

        log.warn(
                "TICKET POLICY: Could not resolve registry facility id for identifier {} as either hfrId or ninId — pause check considered the RMS center id only",
                identifier);
        return null;
    }

    private boolean hasOpenInverterShutdownIncident(String facilityId) {
        return alertRepository.hasOpenIncidentForRmsFacility(facilityId, "INVERTER", "ShutdownInverter");
    }

    private boolean hasBatteryRootCauseBlockingIncident(String facilityId) {
        return alertRepository.hasOpenIncidentForRmsFacility(facilityId, "BATTERY", "BatteryDisconnected")
                || alertRepository.hasOpenIncidentForRmsFacility(facilityId, "BATTERY", "DeepDischarge");
    }

    private boolean isInBatteryIsolationScope(Alert alert) {
        if (alert.getAlertType() == Alert.AlertType.BATTERY) {
            return true;
        }
        if (alert.getAlertType() == Alert.AlertType.INVERTER
                && alert.getAlertSubType() == Alert.AlertSubType.SHUTDOWN) {
            return true;
        }
        return alert.getAlertType() == Alert.AlertType.PANEL
                && alert.getAlertSubType() == Alert.AlertSubType.LOW_GENERATION;
    }
}
