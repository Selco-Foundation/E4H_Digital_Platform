package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User-analytics event published to Kafka so the indexer can build the
 * user-analytics-report index. The schema is shared across modules (im-services publishes SEM
 * events, health-facility-registry publishes facility events and boundary-service publishes
 * boundary events onto the same topic) and must stay byte-compatible with
 * {@code org.egov.im.web.models.UserAnalyticsEvent},
 * {@code facility.web.models.UserAnalyticsEvent} and
 * {@code digit.web.models.UserAnalyticsEvent} — the indexer maps a single flat document.
 * <p>
 * Field names are snake_case to match the indexer / MDMS convention.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAnalyticsEvent {

    /** Unique id for this event (UUID). */
    @JsonProperty("event_id")
    private String eventId;

    /**
     * AMC_SCHEDULED for a configuration create; for a visit workflow transition the value comes
     * from {@code action_analytics} on the matched USER_ANALYTICS.AMC record.
     */
    @JsonProperty("event_type")
    private String eventType;

    /** UTC timestamp (ISO-8601) at which the event was generated. */
    @JsonProperty("event_time")
    private String eventTime;

    /** Source application, always MANAGEMENT_HUB for this flow. */
    @JsonProperty("application")
    private String application;

    /** The user who performed the action (RequestInfo.userInfo). */
    @JsonProperty("user")
    private org.egov.common.contract.request.User user;

    /**
     * The acting user's role that the event is attributed to — taken from the matched action's
     * {@code action_roles} for visit events, or from the matched USER_ANALYTICS.USER_TYPE record
     * for configuration creates.
     */
    @JsonProperty("system_role")
    private String systemRole;

    /** program_role from the matched USER_ANALYTICS.USER_TYPE record. */
    @JsonProperty("primary_role")
    private String primaryRole;

    /** user_category from the matched USER_ANALYTICS.USER_TYPE record. */
    @JsonProperty("user_category")
    private String userCategory;

    /** Always AMC, to separate these events from the FACILITY/BOUNDARY events of MANAGEMENT_HUB. */
    @JsonProperty("module")
    private String module;

    /** Localized state name, resolved the same way the other producers resolve it. */
    @JsonProperty("state")
    private String state;

    /** Business entity id — the AMC configuration id or the scheduled visit id. */
    @JsonProperty("entity_id")
    private String entityId;

    /** Business entity type, AMC_CONFIGURATION or AMC_VISIT. */
    @JsonProperty("entity_type")
    private String entityType;
}
