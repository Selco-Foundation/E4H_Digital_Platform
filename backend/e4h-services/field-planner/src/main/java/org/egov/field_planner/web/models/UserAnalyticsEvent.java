package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User-analytics event published to Kafka so the indexer can build the user-analytics-report index.
 * The schema is shared across modules (im-services publishes SEM events,
 * health-facility-registry publishes facility events, boundary-service publishes boundary events,
 * amc-scheduler-service publishes AMC events, project publishes project events and
 * field-planner-activity publishes the installation-report events onto the same topic) and must
 * stay byte-compatible with {@code org.egov.im.web.models.UserAnalyticsEvent},
 * {@code facility.web.models.UserAnalyticsEvent}, {@code digit.web.models.UserAnalyticsEvent},
 * {@code org.egov.amc.web.models.UserAnalyticsEvent},
 * {@code org.egov.project.web.models.UserAnalyticsEvent} and
 * {@code org.egov.activity.web.models.UserAnalyticsEvent} — the indexer maps a single flat document.
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

    /** What happened — FIELD_PLAN_CREATE, FIELD_PLAN_SCHEDULED or ICC_REPORT_UPLOAD. */
    @JsonProperty("event_type")
    private String eventType;

    /** UTC timestamp (ISO-8601) at which the event was generated. */
    @JsonProperty("event_time")
    private String eventTime;

    /** Source application — always MANAGEMENT_HUB here, installation plans are authored there. */
    @JsonProperty("application")
    private String application;

    /** The user who performed the action (RequestInfo.userInfo). */
    @JsonProperty("user")
    private org.egov.common.contract.request.User user;

    /** The matched USER_ANALYTICS.USER_TYPE record's system role that the acting user holds. */
    @JsonProperty("system_role")
    private String systemRole;

    /** program_role from the matched USER_ANALYTICS.USER_TYPE record. */
    @JsonProperty("primary_role")
    private String primaryRole;

    /** user_category from the matched USER_ANALYTICS.USER_TYPE record. */
    @JsonProperty("user_category")
    private String userCategory;

    /** Localized state name, resolved the same way the other producers resolve it. */
    @JsonProperty("state")
    private String state;

    /** Always FIELD_PLANNER, to separate these events from the other MANAGEMENT_HUB producers. */
    @JsonProperty("module")
    private String module;

    /** Business entity id — the installation plan, or the ICC report for an upload. */
    @JsonProperty("entity_id")
    private String entityId;

    /** Business entity type — FIELD_PLAN or ICC_REPORT. */
    @JsonProperty("entity_type")
    private String entityType;
}
