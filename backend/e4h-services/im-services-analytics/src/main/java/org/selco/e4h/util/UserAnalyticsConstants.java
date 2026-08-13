package org.selco.e4h.util;

import java.util.List;

/**
 * Constants for the weekly user-analytics report built over the {@code user-analytics-report}
 * Elasticsearch index, which every module's {@code UserAnalyticsEvent} producer writes into via the
 * shared {@code user-analytics-event} Kafka topic.
 */
public class UserAnalyticsConstants {

    private UserAnalyticsConstants() {}

    /** {@code event_type} published by im-services' LoginAnalyticsService on every reported login. */
    public static final String USER_LOGIN_EVENT_TYPE = "USER_LOGIN";

    /**
     * The three front-ends an event can originate from, in report column order. Any other value
     * found in the index is appended after these so a new application never silently disappears
     * from the report.
     */
    public static final List<String> APPLICATIONS = List.of("SAURA_EMITRA", "FIELD_ASSIST", "MANAGEMENT_HUB");

    /** Bucket key substituted for documents whose state / role / application is null. */
    public static final String UNKNOWN = "UNKNOWN";

    /** Dimension key of the single all-up bucket, distinguishing it from a state or role name. */
    public static final String OVERALL = "OVERALL";

    //.......................... Elasticsearch aggregation names ..........................//

    public static final String AGG_ACTIVE_USERS = "active_users";
    public static final String AGG_LOGINS = "logins";
    public static final String AGG_BY_APPLICATION = "by_application";
    public static final String AGG_BY_STATE = "by_state";
    public static final String AGG_BY_ROLE = "by_role";
    /**
     * {@code event_type} terms, requested only under the state dimension — the By State sheet
     * cross-tabs state against event type, and nesting it under role too would multiply the
     * aggregation out for a breakdown nothing reads.
     */
    public static final String AGG_BY_EVENT_TYPE = "by_event_type";

    public static final String AGG_CHAMPIONS_BY_ROLE = "champions_by_role";
    public static final String AGG_CHAMPIONS_BY_APPLICATION = "champions_by_application";
    /** The role / application {@code terms} nested inside a champions filter. */
    public static final String AGG_BY_GROUP = "by_group";
    public static final String AGG_TOP_USERS = "top_users";
    public static final String AGG_USER_DETAILS = "user_details";

    /** {@code _source} paths the champion {@code top_hits} pulls the user's identity from. */
    public static final String USER_NAME_SOURCE_PATH = "Data.user.name";
    public static final String USER_USERNAME_SOURCE_PATH = "Data.user.userName";

    //.................................. Excel report .....................................//

    public static final String SHEET_SUMMARY = "Summary";
    public static final String SHEET_BY_STATE = "By State";
    public static final String SHEET_BY_ROLE = "By Role";
    public static final String SHEET_CHAMPIONS = "Top Champions";

    public static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    //.................................. Report mail ......................................//

    /** HRMS role whose holders are mailed the workbook whenever the report is generated. */
    public static final String REPORT_RECIPIENT_ROLE = "USER_ANALYTICS_REPORT";

    /** Tenant the report mail is raised under; every module now lives under the {@code in} tenant. */
    public static final String REPORT_MAIL_TENANT_ID = "in";

    public static final String REPORT_MAIL_TEMPLATE_PATH = "templates/user_analytics_report_email.html";
}
