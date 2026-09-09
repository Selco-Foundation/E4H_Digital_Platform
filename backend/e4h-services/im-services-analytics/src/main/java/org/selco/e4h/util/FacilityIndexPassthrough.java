package org.selco.e4h.util;

import org.selco.e4h.web.models.IncidentStatusAgregation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Carries every field of an already-indexed health facility document onto the
 * {@link IncidentStatusAgregation} that is about to replace it.
 *
 * <p>The incident flow republishes a facility's <em>whole</em> document to the indexer topic, and the
 * indexer replaces the document at that id rather than merging into it. Any field this service does
 * not carry forward is therefore dropped from the index on the very next ticket event. Most of the
 * document is owned by other services - health-facility-registry owns the facility attributes,
 * amc-scheduler-service owns the AMC namespace - and this service has no source to rebuild them from.
 *
 * <p>This used to be a hand-maintained copy list plus an {@code amc}-prefix sweep, which silently
 * dropped every field that was neither listed nor AMC-prefixed ({@code solutionDesignType},
 * {@code phcName} and {@code facilityCategory} were all being lost on each ticket event). Copying
 * everything that is not recomputed inverts the default: a field added to the index later is
 * preserved automatically instead of needing a matching change here.
 *
 * <p>Values are passed through as-is, never coerced. This service is a passthrough and has no
 * business reinterpreting a value's indexed type - a cast or a {@code String.valueOf} here would
 * either throw on, or quietly rewrite, the numeric AMC fields, the nested {@code boundary} object,
 * the {@code phcName} map, and legacy documents whose dates are still epoch millis.
 */
public final class FacilityIndexPassthrough {

    /**
     * Index field name to outbound payload field name, for the handful of fields the indexer config
     * renames on the way in ({@code egov-indexer/im-services.yml}, topic
     * {@code save-phc-master-list-indexer}). Reading {@code Data.geo-point} and republishing it as
     * {@code geo-point} would land it at {@code Data.geo-point} only by accident - the indexer maps
     * {@code $.geoPoint}, so the payload has to use the inbound name.
     *
     * <p>Every other index field is mapped identically in and out and needs no entry here.
     */
    private static final Map<String, String> INDEX_KEY_TO_PAYLOAD_KEY = Map.of(
            "tenantId_localized", "tenantIdLocalized",
            "geo-point", "geoPoint",
            "total_tickets", "totalTickets",
            "open_tickets", "openTickets",
            "closed_tickets", "closedTickets",
            "solar_panel_status", "solarPanelStatus");

    /**
     * Payload fields {@link IncidentStatusAgregation} declares and recomputes on every republish.
     * These are excluded from the passthrough map: emitting them twice would put duplicate keys in
     * the JSON, and the stale indexed value must lose to the freshly computed one anyway.
     */
    private static final Set<String> RECOMPUTED_PAYLOAD_KEYS = Set.of(
            "tenantId",
            "facilityId",
            "totalTickets",
            "openTickets",
            "closedTickets",
            "solarPanelStatus",
            "nonFunctionalTimestamp",
            "lastModifiedTime",
            "projectName");

    private FacilityIndexPassthrough() {
    }

    /**
     * Copies every non-recomputed field from an indexed document's {@code Data} map onto
     * {@code target}.
     *
     * <p>Both arguments are required. A facility with no indexed document has no document to
     * republish at all, so callers skip it entirely rather than passing a null through here -
     * quietly no-op'ing instead would let the caller publish a document stripped of every field
     * this helper exists to preserve, wiping the facility's index entry.
     *
     * @param data   the {@code _source.Data} map of the facility's current index document; non-null
     * @param target the aggregation about to be published to the indexer topic; non-null
     */
    public static void copyInto(Map<String, Object> data, IncidentStatusAgregation target) {
        Map<String, Object> passthrough = new LinkedHashMap<>();
        data.forEach((indexKey, value) -> {
            if (indexKey == null) {
                return;
            }
            String payloadKey = INDEX_KEY_TO_PAYLOAD_KEY.getOrDefault(indexKey, indexKey);
            if (RECOMPUTED_PAYLOAD_KEYS.contains(payloadKey)) {
                return;
            }
            passthrough.put(payloadKey, value);
        });
        target.setIndexPassthrough(passthrough);
    }
}
