package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egov.common.contract.request.User;

/**
 * User-analytics event published to Kafka so the indexer can build the
 * user-analytics-report index. Shared across modules (im-services being the first
 * producer, via SEM); the event schema is intentionally module-agnostic.
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

    /** action_analytics of the matched USER_ANALYTICS.SEM record (e.g. TICKET_RESOLVE). */
    @JsonProperty("event_type")
    private String eventType;

    /** UTC timestamp (ISO-8601) at which the event was generated. */
    @JsonProperty("event_time")
    private String eventTime;

    /** Source application, always SAURA_EMITRA for this flow. */
    @JsonProperty("application")
    private String application;

    /** The user who performed the action. */
    @JsonProperty("user")
    private User user;

    /** The user's role that matched the action's action_roles in USER_ANALYTICS.SEM. */
    @JsonProperty("system_role")
    private String systemRole;

    /** program_role from USER_ANALYTICS.USER_TYPE matched against the user's roles. */
    @JsonProperty("primary_role")
    private String primaryRole;

    /** user_category from USER_ANALYTICS.USER_TYPE for the matched program_role. */
    @JsonProperty("user_category")
    private String userCategory;

    /** Workflow state - not populated yet. */
    @JsonProperty("state")
    private String state;

    /** Module - not populated yet. */
    @JsonProperty("module")
    private String module;

    /** Business entity id, i.e. the incidentId. */
    @JsonProperty("entity_id")
    private String entityId;

    /** Business entity type, always TICKET for incidents. */
    @JsonProperty("entity_type")
    private String entityType;
}
