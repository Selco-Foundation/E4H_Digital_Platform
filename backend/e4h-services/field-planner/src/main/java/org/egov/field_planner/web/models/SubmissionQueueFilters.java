package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionQueueFilters {

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("states")
    private List<String> states;

    @JsonProperty("districts")
    private List<String> districts;

    @JsonProperty("blocks")
    private List<String> blocks;

    @JsonIgnore
    public String getResolvedFacilityName() {
        return StringUtils.isNotBlank(facilityName) ? facilityName.trim() : null;
    }

    @JsonIgnore
    public List<String> getResolvedStates() {
        return normalizeList(states);
    }

    @JsonIgnore
    public List<String> getResolvedDistricts() {
        return normalizeList(districts);
    }

    @JsonIgnore
    public List<String> getResolvedBlocks() {
        return normalizeList(blocks);
    }

    private static List<String> normalizeList(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        values.stream().filter(StringUtils::isNotBlank).forEach(normalized::add);
        return new ArrayList<>(normalized);
    }
}
