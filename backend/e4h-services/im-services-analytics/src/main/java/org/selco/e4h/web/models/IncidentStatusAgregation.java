package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @JsonProperty("phcName")
    private String phcName;

    @JsonProperty("totalTickets")
    private int totalOccurences;

    @JsonProperty("openTickets")
    private int totalOpenOccurrences;

    @JsonProperty("closedTickets")
    private int totalCloseOccurrences;

    @JsonProperty("solarPanelStatus")
    private String systemFunctional;

    @JsonProperty("lastModifiedTime")
    private long lastModifiedTime;

    @JsonProperty("block")
    private String block;

    @JsonProperty("code")
    private String code;

    @JsonProperty("state")
    private String state;

    @JsonProperty("district")
    private String district;

    @JsonProperty("isLive")
    private boolean isLive;

    @JsonProperty("synced")
    private boolean synced;

    @JsonProperty("name")
    private String name;

    @JsonProperty("phcType")
    private String phcType;

    @JsonProperty("facilityCategory")
    private String facilityCategory;

    @JsonProperty("type")
    private String type;

    @JsonProperty("tenantIdLocalized")
    private String tenantIdLocalized;

    @JsonProperty("geoPoint")
    private List<Double> geoPoint;

    @JsonProperty("boundary")
    private Boundary boundary;

    @JsonProperty("mappedVendorUserName")
    private String mappedVendorUserName;

    @JsonProperty("mappedVendorName")
    private String mappedVendorName;

    @JsonProperty("projectName")
    private String projectName;

    /**
     * The AMC namespace exactly as the health facility index currently holds it, keyed by index field
     * name ({@code amcApplicable}, {@code amcDueDate1..10}, {@code amcVisitDate1..10}, and the rest).
     *
     * <p>AMC data is owned by amc-scheduler-service and lives only on the index - this service has no
     * source to rebuild it from. It is carried here purely so the full-document re-index this object
     * drives writes it back unchanged instead of dropping it, exactly as {@code mappedVendorName} and
     * {@code projectName} above are. See {@code FacilityAmcFieldsHelper}.
     *
     * <p>Held as an opaque map rather than as ~27 declared fields on purpose. The values are only ever
     * read from the index and written straight back, so naming and typing each one here would buy
     * nothing while duplicating the whole block of {@code FacilityKibanaIndex} in health-facility-registry
     * - and it would force a lossy conversion on values whose indexed type this service has no business
     * asserting (the numeric AMC fields, and legacy documents whose dates are still epoch millis).
     * Passing them through untouched is both simpler and more faithful.
     */
    private Map<String, Object> amcFields;

    /**
     * Flattens {@link #amcFields} into the top level of the emitted JSON, so each entry lands as its
     * own index field rather than nested under an {@code amcFields} object.
     *
     * <p>Never returns null: Jackson rejects a null any-getter.
     */
    @JsonAnyGetter
    public Map<String, Object> getAmcFields() {
        return amcFields == null ? Collections.emptyMap() : amcFields;
    }
}
