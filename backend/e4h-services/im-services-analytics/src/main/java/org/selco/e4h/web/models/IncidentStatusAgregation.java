package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * The full health facility document this service republishes to the indexer topic
 * {@code save-phc-master-list-indexer} on every ticket event.
 *
 * <p>Only the fields this service actually derives are declared here. Everything else the index
 * holds is carried opaquely in {@link #indexPassthrough} - see {@code FacilityIndexPassthrough} for
 * why. Declaring a field here means claiming ownership of it, so anything added below must also be
 * added to that helper's recomputed-key set or it will be emitted twice.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidentStatusAgregation {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("totalTickets")
    private int totalOccurences;

    @JsonProperty("openTickets")
    private int totalOpenOccurrences;

    @JsonProperty("closedTickets")
    private int totalCloseOccurrences;

    @JsonProperty("solarPanelStatus")
    private String systemFunctional;

    /**
     * When the facility went non-functional: the creation time (epoch millis) of the oldest still-open
     * ticket reporting the system as non-functional.
     *
     * <p>{@code null} whenever {@link #systemFunctional} is {@code FUNCTIONAL}. The null is published
     * rather than omitted - the indexer replaces the whole document, so a facility that has just been
     * restored must overwrite its previous timestamp instead of leaving a stale one behind.
     */
    @JsonProperty("nonFunctionalTimestamp")
    private Long nonFunctionalTimestamp;

    @JsonProperty("lastModifiedTime")
    private long lastModifiedTime;

    @JsonProperty("projectName")
    private String projectName;

    /**
     * Every other field of the facility's current index document, keyed by outbound payload field
     * name. Populated by {@code FacilityIndexPassthrough} and written straight back unchanged so this
     * full-document republish does not drop fields owned by health-facility-registry and
     * amc-scheduler-service.
     */
    private Map<String, Object> indexPassthrough;

    /**
     * Flattens {@link #indexPassthrough} into the top level of the emitted JSON, so each entry lands
     * as its own index field rather than nested under an {@code indexPassthrough} object.
     *
     * <p>Never returns null: Jackson rejects a null any-getter.
     */
    @JsonAnyGetter
    public Map<String, Object> getIndexPassthrough() {
        return indexPassthrough == null ? Collections.emptyMap() : indexPassthrough;
    }
}
