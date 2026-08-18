package org.egov.activity.util;

import org.egov.common.models.project.TaskStatus;

public class ActivityConstants {
    public static final String MASTER_TENANTS = "tenants";
    public static final String MDMS_TENANT_MODULE_NAME = "tenant";
    public static final String MDMS_COMMON_MASTERS_MODULE_NAME = "common-masters";
    public static final String MDMS_HCM_ATTENDANCE_MODULE_NAME = "HCM-ATTENDANCE";
    public static final String MASTER_STATE_INFO = "StateInfo";
    public static final String MASTER_ACTIVITIES = "Activities";
    public static final String MASTER_INSTALLATION_IMAGES = "InstallationImages";
    public static final String INSTALLATION_IMAGE_FIELD = "InstallationImage";
    public static final String INSTALLATION_IMAGE_DOCUMENT_TYPE_PREFIX = "INSTALLATION_IMAGE-";
    public static final String INSTALLATION_IMAGE_SYSTEM_TYPES_FIELD = "system_types";
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
    public static final String FACILITY_ADMIN = "FACILITY_ADMIN";
    public static final String INSTALLATION_REPORT_APPROVER_QC_TEAM = "INSTALLATION_REPORT_APPROVER_QC_TEAM";
    public static final String INSTALLATION_REPORT_PART_B_EDITOR = "INSTALLATION_REPORT_PART_B_EDITOR";
    public static final String SUBMITTED_BY_SUPERVISOR = "SUBMITTED_BY_SUPERVISOR";

    // User-analytics event (shared user-analytics-report index, see ActivityAnalyticsService)
    public static final String USER_ANALYTICS_MODULE = "USER_ANALYTICS";
    public static final String MDMS_MASTER_FIELD_PLANNER = "FIELD_PLANNER";
    public static final String MDMS_MASTER_USER_TYPE = "USER_TYPE";
    public static final String MDMS_RESPONSE = "MdmsRes";
    /**
     * Fallback when a FIELD_PLANNER record does not declare an application. Report submissions come
     * from Field Assist; the QC review actions carry MANAGEMENT_HUB on their own MDMS records.
     */
    public static final String ANALYTICS_APPLICATION_DEFAULT = "FIELD_ASSIST";
    /** Staffing a field plan is a Management Hub action, unlike the Field Assist report submissions. */
    public static final String ANALYTICS_APPLICATION_MANAGEMENT_HUB = "MANAGEMENT_HUB";
    /** Always FIELD_PLANNER, to separate these events from the FACILITY/BOUNDARY/AMC/PROJECT ones. */
    public static final String ANALYTICS_MODULE_FIELD_PLANNER = "FIELD_PLANNER";
    public static final String ANALYTICS_ENTITY_TYPE_ACTIVITY_FACILITY = "ACTIVITY_FACILITY";
    public static final String ANALYTICS_ENTITY_TYPE_ACTIVITY_ASSIGNMENT = "ACTIVITY_ASSIGNMENT";
    /** Fixed event type for /_assign-activity — there is no workflow action to map it from. */
    public static final String ANALYTICS_EVENT_ACTIVITY_ASSIGNED = "ACTIVITY_ASSIGNED";
    /** Key holding the field plan's state boundary code inside FieldPlan.geographyDetails. */
    public static final String GEOGRAPHY_DETAILS_STATE = "state";
    public static final String BOUNDARY_LOCALIZATION_MODULE = "rainmaker-in";
    public static final String LOCALIZATION_LOCALE = "en_IN";
    public static final String LOCALIZATION_TENANT_ID = "in";

}