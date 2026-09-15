package org.selco.e4h.util;

import java.util.List;
import java.util.Set;

public final class EscalationWorkflowStates {

    public static final String PENDING_FOR_ASSIGNMENT = "PENDINGFORASSIGNMENT";
    public static final String RMS_DEVICE_PENDING_TECH_POC = "RMS_DEVICE_PENDING_TECH_POC";
    public static final String OUT_OF_WARRANTY_PENDING_TECH_POC = "OUT_OF_WARRANTY_PENDING_TECH_POC";
    public static final String OUT_OF_SCOPE = "OUT_OF_SCOPE";
    public static final String PENDING_ASSIGNMENT_OUT_OF_WARRANTY = "PENDING_ASSIGNMENT_OUT_OF_WARRANTY";
    public static final String PENDING_RESOLUTION = "PENDINGRESOLUTION";
    public static final String PENDING_RESOLUTION_SPARE_PART_NEEDED = "PENDING_RESOLUTION_SPARE_PART_NEEDED";
    public static final String PENDING_RESOLUTION_OUT_OF_WARRANTY = "PENDING_RESOLUTION_OUT_OF_WARRANTY";
    public static final String PENDING_RESOLUTION_OUT_OF_SCOPE = "PENDING_RESOLUTION_OUT_OF_SCOPE";

    public static final List<String> STATE_POC_DAILY_STATES = List.of(
            PENDING_FOR_ASSIGNMENT,
            RMS_DEVICE_PENDING_TECH_POC,
            OUT_OF_WARRANTY_PENDING_TECH_POC
    );

    public static final List<String> SENIOR_PROGRAM_MANAGER_DAILY_STATES = List.of(
            OUT_OF_SCOPE,
            PENDING_ASSIGNMENT_OUT_OF_WARRANTY,
            PENDING_RESOLUTION,
            PENDING_RESOLUTION_SPARE_PART_NEEDED,
            PENDING_RESOLUTION_OUT_OF_WARRANTY,
            PENDING_RESOLUTION_OUT_OF_SCOPE
    );

    public static final List<String> PROCUREMENT_DAILY_STATES = List.of(
            PENDING_RESOLUTION,
            PENDING_RESOLUTION_SPARE_PART_NEEDED,
            PENDING_RESOLUTION_OUT_OF_WARRANTY,
            PENDING_RESOLUTION_OUT_OF_SCOPE
    );

    public static final Set<String> CRM_STATES = Set.of(PENDING_FOR_ASSIGNMENT);
    public static final Set<String> TECH_POC_STATES = Set.of(
            RMS_DEVICE_PENDING_TECH_POC,
            OUT_OF_WARRANTY_PENDING_TECH_POC
    );
    public static final Set<String> STATE_SPOC_STATES = Set.of(
            OUT_OF_SCOPE,
            PENDING_ASSIGNMENT_OUT_OF_WARRANTY
    );
    public static final Set<String> VENDOR_STATES = Set.of(
            PENDING_RESOLUTION,
            PENDING_RESOLUTION_SPARE_PART_NEEDED,
            PENDING_RESOLUTION_OUT_OF_WARRANTY,
            PENDING_RESOLUTION_OUT_OF_SCOPE
    );

    private EscalationWorkflowStates() {
    }
}
