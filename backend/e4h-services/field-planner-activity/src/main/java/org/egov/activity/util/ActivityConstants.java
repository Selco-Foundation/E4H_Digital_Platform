package org.egov.activity.util;

import org.egov.common.models.project.TaskStatus;

public class ActivityConstants {
    public static final String MASTER_TENANTS = "tenants";
    public static final String MDMS_TENANT_MODULE_NAME = "tenant";
    public static final String MDMS_COMMON_MASTERS_MODULE_NAME = "common-masters";
    public static final String MDMS_HCM_ATTENDANCE_MODULE_NAME = "HCM-ATTENDANCE";
    public static final String MASTER_STATE_INFO = "StateInfo";
    public static final String MASTER_ACTIVITIES = "Activities";
    public static final String BOM_FORM = "BOM_FORM";
    //location
    public static final String DRAFT_STATUS = "DRAFT";

    public static final String ACTIVE_STATUS = "ACTIVE";

    public static final String SCHEDULED_STATUS = "SCHEDULED";
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


}