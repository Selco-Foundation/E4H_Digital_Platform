package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionQueueSort {

    public static final String SORT_FACILITY_NAME = "facilityName";
    public static final String SORT_DISTRICT = "district";
    public static final String SORT_BLOCK = "block";
    public static final String SORT_STATE = "state";
    public static final String SORT_PLAN_NAME = "planName";
    public static final String SORT_LAST_MODIFIED_TIME = "lastModifiedTime";

    @JsonProperty("sortBy")
    private String sortBy;

    @JsonProperty("sortOrder")
    private String sortOrder;

    public String resolveSortBy() {
        if (StringUtils.isBlank(sortBy)) {
            return SORT_LAST_MODIFIED_TIME;
        }
        return switch (sortBy) {
            case SORT_FACILITY_NAME, SORT_DISTRICT, SORT_BLOCK, SORT_STATE, SORT_PLAN_NAME, SORT_LAST_MODIFIED_TIME ->
                    sortBy;
            default -> SORT_LAST_MODIFIED_TIME;
        };
    }

    public boolean isDescending() {
        return "DESC".equalsIgnoreCase(sortOrder);
    }
}
