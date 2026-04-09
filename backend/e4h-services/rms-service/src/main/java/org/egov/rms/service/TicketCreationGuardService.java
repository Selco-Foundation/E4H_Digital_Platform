package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.model.Alert;
import org.egov.rms.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Enforces business rules that suppress new IM tickets when certain open incidents
 * already exist for the same RMS facility.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketCreationGuardService {

    private final AlertRepository alertRepository;

    /**
     * @return true when this alert must not result in a new ticket (open blocking incidents in IM).
     */
    public boolean shouldBlockTicketCreation(Alert alert) {
        if (alert == null || !StringUtils.hasText(alert.getFacilityId())) {
            return false;
        }
        String facilityId = alert.getFacilityId();

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
