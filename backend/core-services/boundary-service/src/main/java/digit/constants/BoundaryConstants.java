package digit.constants;


import org.springframework.stereotype.Component;


@Component
public class BoundaryConstants {
    public static final String RES_MSG_ID = "uief87324";
    public static final String SUCCESSFUL = "successful";
    public static final String FAILED = "failed";
    public static final String TYPE = "type";
    public static final String POINT = "Point";
    public static final String POLYGON = "Polygon";
    public static final String OPENING_BRACKET = " {";
    public static final String CLOSING_BRACKET = "} ";
    public static final String MDMS_COMMON_MASTERS_MODULE_NAME = "common-masters";
    public static final String MASTER_STATE_INFO = "StateInfo";

    // User-analytics event (shared user-analytics-report index, see BoundaryAnalyticsService)
    public static final String USER_ANALYTICS_MODULE = "USER_ANALYTICS";
    public static final String MDMS_MASTER_USER_TYPE = "USER_TYPE";
    public static final String ANALYTICS_APPLICATION = "MANAGEMENT_HUB";
    public static final String ANALYTICS_ENTITY_TYPE_BOUNDARY = "BOUNDARY";
    public static final String ANALYTICS_EVENT_BOUNDARY_CREATE = "BOUNDARY_CREATE";
}
