package org.selco.e4h.util;

/**
 * Constants used throughout the incident management analytics module.
 * Contains module names, master data keys, and status identifiers for SLA computation.
 */
public class IMConstants {

    private IMConstants() {}

    /**
     * Exact module name for common masters data.
     */
    public static final String MODULE_NAME_COMMON_MASTERS = "common-masters";

    /**
     * Exact master data key for business hours configuration.
     */
    public static final String BUSINESS_HOUR_MASTER = "BusinessHours";

    /**
     * Exact identifier for incidents.
     */
    public static final String INCIDENT = "Incident";

    /**
     * Exact identifier for service definitions.
     */
    public static final String SERVICE_DEF = "ServiceDefs";

    /**
     * Prefix used for statuses related to pending assignments.
     * Used to match any state starting with this prefix.
     */
    public static final String PENDING_ASSIGNMENT_PREFIX = "PENDING_ASSIGNMENT_";

    /**
     * Prefix used for statuses related to pending resolutions.
     * Used to match any state starting with this prefix.
     */
    public static final String PENDING_RESOLUTION_PREFIX = "PENDING_RESOLUTION_";

    /**
     * Exact status indicating pending assignment.
     */
    public static final String PENDING_FOR_ASSIGNMENT = "PENDINGFORASSIGNMENT";

    /**
     * Exact status indicating pending resolution.
     */
    public static final String PENDING_RESOLUTION = "PENDINGRESOLUTION";

    /**
     * Exact status indicating pending assignment for theft incidents.
     */
    public static final String PENDINGFORASSIGNMENT_THEFT = "PENDINGFORASSIGNMENT_THEFT";

    /**
     * Exact status indicating pending assignment for RMS device incidents.
     */
    public static final String PENDINGFORASSIGNMENT_RMS_DEVICE = "PENDINGFORASSIGNMENT_RMS_DEVICE";

    /**
     * Exact status indicating RMS device is pending technical POC.
     */
    public static final String RMS_DEVICE_PENDING_TECH_POC = "RMS_DEVICE_PENDING_TECH_POC";

    /**
     * Exact status indicating RMS device is pending resolution.
     */
    public static final String RMS_DEVICE_PENDINGRESOLUTION = "RMS_DEVICE_PENDINGRESOLUTION";

    /**
     * Exact status indicating incident is out of scope.
     */
    public static final String OUT_OF_SCOPE = "OUT_OF_SCOPE";

    /**
     * Exact status indicating out-of-warranty case pending tech POC (round 1).
     */
    public static final String OUT_OF_WARRANTY_PENDING_TECH_POC = "OUT_OF_WARRANTY_PENDING_TECH_POC";

    /**
     * Exact status indicating revision is pending for out-of-warranty cases.
     */
    public static final String PENDING_REVISION = "PENDING_REVISION";

    /**
     * Exact status indicating out-of-warranty case pending tech POC (round 2).
     */
    public static final String OUT_OF_WARRANTY_PENDING_TECH_POC_ROUND_2 = "OUT_OF_WARRANTY_PENDING_TECH_POC_ROUND_2";

    /**
     * Exact status indicating pending assignment for out-of-warranty cases.
     */
    public static final String PENDING_ASSIGNMENT_OUT_OF_WARRANTY = "PENDING_ASSIGNMENT_OUT_OF_WARRANTY";

    /**
     * Exact status indicating pending resolution for out-of-warranty cases.
     */
    public static final String PENDING_RESOLUTION_OUT_OF_WARRANTY = "PENDING_RESOLUTION_OUT_OF_WARRANTY";

    /**
     * Exact status indicating pending resolution for out-of-scope cases.
     */
    public static final String PENDING_RESOLUTION_OUT_OF_SCOPE = "PENDING_RESOLUTION_OUT_OF_SCOPE";

    /**
     * Exact status indicating pending resolution when spare part is needed.
     */
    public static final String PENDING_RESOLUTION_SPARE_PART_NEEDED = "PENDING_RESOLUTION_SPARE_PART_NEEDED";

    /**
     * Exact key representing business service.
     */
    public static final String BUSINESS_SERVICE = "businessService";

    public static final String REJECTED = "REJECTED";

    public static final String RESOLVED = "RESOLVED";

    public static final String CLOSED_AFTER_RESOLUTION = "CLOSEDAFTERRESOLUTION";

    public static final String CLOSED_AFTER_REJECTION = "CLOSEDAFTERREJECTION";

    public static final String INCIDENT_UNDERSCORE = "Incident_";
}