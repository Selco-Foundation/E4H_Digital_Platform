package org.selco.e4h.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Index name and field paths for the weekly user-analytics report.
 * <p>
 * The field paths are configuration rather than constants because the {@code user-analytics-report}
 * mapping decides whether a string field is aggregatable directly or only through a
 * {@code .keyword} sub-field — {@code Data.application} and friends are mapped as {@code keyword},
 * while {@code Data.user.uuid} is {@code text} with a {@code .keyword} sibling. Keeping them in
 * properties means a mapping change is a redeploy of config, not of code.
 */
@Component
@Getter
@Setter
public class UserAnalyticsProperties {

    @Value("${user.analytics.es.index}")
    private String index;

    @Value("${user.analytics.field.event.time}")
    private String eventTimeField;

    @Value("${user.analytics.field.event.type}")
    private String eventTypeField;

    @Value("${user.analytics.field.application}")
    private String applicationField;

    @Value("${user.analytics.field.state}")
    private String stateField;

    @Value("${user.analytics.field.role}")
    private String roleField;

    /** The role the action itself was matched on, as opposed to the user's programme role. */
    @Value("${user.analytics.field.system.role}")
    private String systemRoleField;

    /** Field the distinct active-user count is taken over — the user's uuid. */
    @Value("${user.analytics.field.user}")
    private String userField;

    /** Login id the Kibana login sheet groups on; Kibana accounts have no uuid to group by. */
    @Value("${user.analytics.field.user.name}")
    private String userNameField;

    /**
     * {@code cardinality} is approximate above this many distinct values and exact at or below it,
     * so this is effectively the active-user count the report stays exact up to.
     */
    @Value("${user.analytics.cardinality.precision.threshold}")
    private int cardinalityPrecisionThreshold;

    /** Cap on state / role buckets returned per week. */
    @Value("${user.analytics.terms.size}")
    private int termsSize;

    /**
     * Cap on event-type buckets, kept separate from {@link #termsSize} because the event-type terms
     * is nested per state and again per state-and-application — a 500-wide cap there would blow the
     * response up for a vocabulary that only has a few dozen values.
     */
    @Value("${user.analytics.event.type.terms.size}")
    private int eventTypeTermsSize;

    /** How many champion users to list per role and per application. */
    @Value("${user.analytics.champion.count}")
    private int championCount;

    /** IANA zone whose calendar week the report is cut on, e.g. Asia/Kolkata. */
    @Value("${user.analytics.report.zone}")
    private String reportZone;
}
