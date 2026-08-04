package org.egov.amc.util;

import org.egov.common.models.project.TaskStatus;

public class AmcConstants {
    public static final String MASTER_TENANTS = "tenants";
    public static final String MDMS_TENANT_MODULE_NAME = "tenant";
    public static final String MDMS_COMMON_MASTERS_MODULE_NAME = "common-masters";
    public static final String MDMS_AMC_MODULE_NAME = "amc";
    public static final String MDMS_AMC_THRESHOLD_MODULE_NAME = "AMCThresholds";
    public static final String MDMS_HCM_ATTENDANCE_MODULE_NAME = "HCM-ATTENDANCE";
    public static final String MASTER_STATE_INFO = "StateInfo";
    public static final String MASTER_ACTIVITIES = "Activities";
    //location
    public static final String DRAFT_STATUS = "DRAFT";
    public static final String MASTER_ATTENDANCE_SESSION = "AttendanceSessions";
    public static final String CODE = "code";
    public static final String PROJECT_TYPE_FIELDPLAN = "FieldPlan";
    public static final String PROJECT_TYPE_FACILITY = "Facility";
    public static final String HIERARCHY_TYPE = "SELCO";
    public static final String TENANTID = "in";
    //General
    public static final String SEMICOLON = ":";
    public static final String DOT = ".";
    public static final String PROJECT_PARENT_HIERARCHY_SEPERATOR = ".";
    public static final String TASK_NOT_ALLOWED = "TASK_NOT_ALLOWED";
    public static final String TASK_NOT_ALLOWED_BENEFICIARY_REFUSED_RESOURCE_EMPTY_ERROR_MESSAGE = "Task not allowed as resources can not be provided when " + TaskStatus.BENEFICIARY_REFUSED;
    public static final String TASK_NOT_ALLOWED_RESOURCE_CANNOT_EMPTY_ERROR_MESSAGE = "Task not allowed as resources can not be empty when ";
    public static final String NUMBER_OF_SESSIONS = "numberOfSessions";
    public static final String OR = " OR ";
    public static final String PROJECT_MANAGER = "PROJECT_MANAGER";
    public static final String SUBMITTED_BY_SUPERVISOR = "SUBMITTED_BY_SUPERVISOR";

    // User-analytics event (shared user-analytics-report index, see AmcAnalyticsService)
    public static final String USER_ANALYTICS_MODULE = "USER_ANALYTICS";
    public static final String MDMS_MASTER_AMC = "AMC";
    public static final String MDMS_MASTER_USER_TYPE = "USER_TYPE";
    public static final String ANALYTICS_APPLICATION = "MANAGEMENT_HUB";
    public static final String ANALYTICS_MODULE_AMC = "AMC";
    public static final String ANALYTICS_ENTITY_TYPE_AMC_CONFIGURATION = "AMC_CONFIGURATION";
    public static final String ANALYTICS_ENTITY_TYPE_AMC_VISIT = "AMC_VISIT";
    public static final String ANALYTICS_EVENT_AMC_SCHEDULED = "AMC_SCHEDULED";
    // Boundary localizations live in this module at the national tenant.
    public static final String BOUNDARY_LOCALIZATION_MODULE = "rainmaker-in";
    public static final String LOCALIZATION_LOCALE = "en_IN";
    public static final String LOCALIZATION_TENANT_ID = "in";

}