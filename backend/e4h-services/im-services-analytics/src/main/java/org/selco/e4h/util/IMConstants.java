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
     * Exact key representing business service.
     */
    public static final String BUSINESS_SERVICE = "businessService";
}