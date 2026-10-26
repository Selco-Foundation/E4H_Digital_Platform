package org.egov.rms.util;

import org.egov.rms.model.Alert;

import java.util.List;

/**
 * Single source for how RMS alerts translate into IM incidents, shared by ticket creation and the
 * open-ticket checks so both always talk about the same incident type, sub-type and status set.
 */
public final class IncidentTypeMapper {

    /** IM workflow statuses in which an incident is still open. */
    public static final List<String> OPEN_STATUSES = List.of(
            "PENDINGFORASSIGNMENT",
            "PENDING_ASSIGNMENT_SPARE_PART_NEEDED",
            "PENDING_ASSIGNMENT_OUT_OF_WARRANTY",
            "PENDING_RESOLUTION_SPARE_PART_NEEDED",
            "PENDING_RESOLUTION_OUT_OF_WARRANTY",
            "PENDINGRESOLUTION",
            "RMS_DEVICE_PENDING_TECH_POC",
            "PENDINGFORASSIGNMENT_THEFT",
            "RMS_DEVICE_PENDINGRESOLUTION",
            "OUT_OF_SCOPE",
            "OUT_OF_WARRANTY_PENDING_TECH_POC",
            "PENDINGFORASSIGNMENT_RMS_DEVICE",
            "PENDING_REVISION",
            "OUT_OF_WARRANTY_PENDING_TECH_POC_ROUND_2",
            "PENDING_RESOLUTION_OUT_OF_SCOPE"
    );

    private IncidentTypeMapper() {
    }

    public static String toIncidentType(Alert.AlertType alertType) {
        switch (alertType) {
            case PANEL:
                return "PANEL";
            case INVERTER:
                return "INVERTER";
            case BATTERY:
                return "BATTERY";
            case GRID:
                return "GRID";
            default:
                return "GIPB";
        }
    }

    public static String toIncidentSubType(Alert.AlertSubType alertSubType) {
        switch (alertSubType) {
            case LOW_GENERATION:
                return "RunningOnGrid";
            case SHUTDOWN:
                return "ShutdownInverter";
            case HIGH_VOLTAGE:
                return "VoltageInverter";
            case BURNT_DISCONNECTED:
                return "BatteryDisconnected";
            case DEEP_DISCHARGING:
                return "DeepDischarge";
            case OVERCHARGING:
                return "Overcharge";
            case VOLTAGE_VARIATION_REVERSE:
                return "ReverseVoltage";
            case VOLTAGE_VARIATION_HIGH:
                return "HighVoltage";
            default:
                return "RMS Data Alert";
        }
    }
}
