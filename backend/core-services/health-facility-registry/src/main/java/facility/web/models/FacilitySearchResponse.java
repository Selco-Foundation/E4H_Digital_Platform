package facility.web.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacilitySearchResponse {
    private List<Facility> facilities;
    private int totalCount;

    /**
     * Whether a jurisdiction ceiling was applied to this response.
     *
     * Lets a client tell "there is nothing here" apart from "there is nothing here that you may
     * see", which are the same empty list otherwise.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean scopeApplied;

    /**
     * Facility ids the caller asked for by name that the ceiling excluded.
     *
     * Null rather than empty when it cannot be computed honestly - which is the case whenever the
     * page could have truncated the result, because then "requested but not returned" would name
     * the ids that simply fell on another page. A loud wrong answer is worse than a quiet absent
     * one, and callers treat a missing id as "does not exist" today.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> outOfScopeFacilityIds;

    /** Kept so existing call sites that predate jurisdiction scoping still compile. */
    public FacilitySearchResponse(List<Facility> facilities, int totalCount) {
        this.facilities = facilities;
        this.totalCount = totalCount;
    }
}
