package org.egov.field_planner.util;

import org.egov.common.models.project.TaskStatus;

public class FieldPlannerConstants {
    public static final String MASTER_TENANTS = "tenants";
    public static final String MDMS_TENANT_MODULE_NAME = "tenant";
    public static final String MDMS_COMMON_MASTERS_MODULE_NAME = "common-masters";
    public static final String MDMS_HCM_ATTENDANCE_MODULE_NAME = "HCM-ATTENDANCE";
    public static final String MASTER_STATE_INFO = "StateInfo";
    public static final String MASTER_ACTIVITIES = "Activities";
    //location
    public static final String DRAFT_STATUS = "DRAFT";
    public static final String FIELD_STAFF_ROLE = "INSTALLATION_REPORT_PART_A_EDITOR";
    public static final String FIELD_SUPERVISOR_ROLE = "INSTALLATION_REPORT_PART_B_EDITOR";
    public static final String INSTALLATION_REVIEWER_ROLE = "INSTALLATION_REPORT_APPROVER_QC_TEAM";
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

    // User-analytics event (shared user-analytics-report index, see FieldPlannerAnalyticsService)
    public static final String USER_ANALYTICS_MODULE = "USER_ANALYTICS";
    public static final String MDMS_MASTER_USER_TYPE = "USER_TYPE";
    /** Field plans are authored in the Management Hub, not in Field Assist. */
    public static final String ANALYTICS_APPLICATION = "MANAGEMENT_HUB";
    /** Always FIELD_PLANNER, to separate these events from the FACILITY/BOUNDARY/AMC/PROJECT ones. */
    public static final String ANALYTICS_MODULE_FIELD_PLANNER = "FIELD_PLANNER";
    public static final String ANALYTICS_ENTITY_TYPE_FIELD_PLAN = "FIELD_PLAN";
    public static final String ANALYTICS_ENTITY_TYPE_ICC_REPORT = "ICC_REPORT";
    /**
     * Carried by both the create and the update that moves a plan into SCHEDULED — analytics reports
     * on the two together, so they share one event type. Plain edits are deliberately not tracked; a
     * plan is edited any number of times while in DRAFT and those edits are not business events.
     */
    public static final String ANALYTICS_EVENT_FIELD_PLAN_CREATE = "FIELD_PLAN_CREATE";
    public static final String ANALYTICS_EVENT_ICC_REPORT_UPLOAD = "ICC_REPORT_UPLOAD";
    public static final String SCHEDULED_STATUS = "SCHEDULED";
    /** Key holding the field plan's state boundary code inside FieldPlan.geographyDetails. */
    public static final String GEOGRAPHY_DETAILS_STATE = "state";
    public static final String BOUNDARY_LOCALIZATION_MODULE = "rainmaker-in";
    public static final String LOCALIZATION_LOCALE = "en_IN";
    public static final String LOCALIZATION_TENANT_ID = "in";
    public static final String PLAN_TYPE_FIELD_PLAN = "FIELD_PLAN";

}