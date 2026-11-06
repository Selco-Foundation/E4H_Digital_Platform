package facility.web.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityBulkSearchCriteria {
    private List<String> tenantIds;
    private List<String> facilityIds;
    private List<String> facilityCategory;
    private List<String> facilityNames;
    private List<String> hfrIds;
    private List<String> ninIds;
    private List<String> facilityPocNames;
    private List<String> facilityPocPhones;
    private List<String> facilityPocEmails;
    private List<String> facilityPocUsernames;
    private List<String> facilityStatus;
    private List<String> userIds;
    private List<String> boundaryCodes;
    private List<String> state;
    private List<String> district;
    private List<String> block;
    @Builder.Default
    private Boolean sendNonPaginatedResponse = false;
    @Builder.Default
    private Integer limit = 10;
    @Builder.Default
    private Integer offset = 0;
    /** Optional: createdAt | updatedAt — used by CO2 batch job when set to createdAt with sortOrder asc */
    private String sortBy;
    /** asc | desc — default desc for updated_at in bulk search */
    private String sortOrder;
    private Boolean isOnmReady;

    /**
     * Whether {@code boundaryCodes} already holds the resolved facility-level codes.
     *
     * The search and the count run as two separate calls over this same object, so resolution has
     * to be idempotent: doing it twice would mean a second boundary-service round trip, and would
     * let the count be built from a different set than the rows if the hierarchy changed between
     * the two. Server-side only - never read from or written to the wire.
     */
    @JsonIgnore
    private boolean boundaryFiltersResolved;

    /**
     * Set when the client's boundary filters resolved to nothing at all.
     *
     * Needed because an empty {@code boundaryCodes} list means "no boundary filter" to the query
     * builder, which is the opposite of what an empty resolution means. Without this the search
     * would silently widen to every facility in the tenant.
     */
    @JsonIgnore
    private boolean boundaryFiltersMatchNothing;
}
