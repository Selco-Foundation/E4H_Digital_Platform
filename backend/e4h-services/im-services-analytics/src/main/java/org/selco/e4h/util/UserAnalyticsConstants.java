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

    //.................................. Excel report .....................................//

    public static final String SHEET_SUMMARY = "Summary";
    public static final String SHEET_BY_STATE = "By State";
    public static final String SHEET_BY_ROLE = "By Role";

    public static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
}
