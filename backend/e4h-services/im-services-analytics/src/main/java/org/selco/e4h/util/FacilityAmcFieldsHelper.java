package org.selco.e4h.util;

import org.selco.e4h.web.models.IncidentStatusAgregation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carries the AMC fields of an already-indexed health facility document onto the
 * {@link IncidentStatusAgregation} that is about to replace it.
 *
 * <p>AMC data is owned by amc-scheduler-service and lives <em>only</em> on the health facility index -
 * it is never persisted in this service's database, nor in the facility table. The incident flow
 * republishes a facility's whole document to the indexer topic, so any field it does not carry
 * forward is dropped from the index on the next ticket event. That is why {@code mappedVendorName}
 * and {@code projectName} are already copied straight through in {@code IncidentService}; the AMC
 * fields need exactly the same treatment.
 *
 * <p>Mirrors {@code facility.util.FacilityAmcFieldsHelper} in health-facility-registry, which does
 * the same job for that service's own full-document re-index paths.
 */
public final class FacilityAmcFieldsHelper {

    /**
     * Prefix identifying the AMC namespace on the index document. Every field amc-scheduler-service
     * owns is named {@code amc*} and it rewrites that namespace wholesale on each sync, so matching on
     * the prefix preserves exactly the set it owns - including any cycle or field added later, which a
     * hardcoded list here would silently start dropping.
     */
    private static final String AMC_FIELD_PREFIX = "amc";

    private FacilityAmcFieldsHelper() {
    }

    /**
     * Copies the AMC namespace from an indexed document's {@code Data} map onto {@code target}.
     * No-op when either side is null; a facility that has never been AMC-synced simply contributes
     * nothing, which is the correct outcome.
     *
     * <p>Values are passed through as-is, never coerced. This service is a passthrough and has no
     * business reinterpreting a value's indexed type - a cast or a {@code String.valueOf} here would
     * either throw on, or quietly rewrite, the numeric AMC fields and legacy documents whose dates are
     * still epoch millis.
     *
     * @param data   the {@code _source.Data} map of the facility's current index document
     * @param target the aggregation about to be published to the indexer topic
     */
    public static void copyAmcFields(Map<String, Object> data, IncidentStatusAgregation target) {
        if (data == null || target == null) {
            return;
        }

        Map<String, Object> amcFields = new LinkedHashMap<>();
        data.forEach((key, value) -> {
            if (key != null && key.startsWith(AMC_FIELD_PREFIX)) {
                amcFields.put(key, value);
            }
        });
        target.setAmcFields(amcFields);
    }
}
