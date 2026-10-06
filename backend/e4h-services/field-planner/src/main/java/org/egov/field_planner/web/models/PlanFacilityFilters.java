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
public class PlanFacilityFilters {

    @JsonProperty("districts")
    private List<String> districts;

    @JsonProperty("blocks")
    private List<String> blocks;

    @JsonProperty("facilityCategories")
    private List<String> facilityCategories;

    @JsonProperty("facilityTypes")
    private List<String> facilityTypes;

    @JsonProperty("phoneStatuses")
    private List<String> phoneStatuses;

    @JsonProperty("fieldStatuses")
    private List<String> fieldStatuses;

    @JsonProperty("overallStatuses")
    private List<String> overallStatuses;

    @JsonIgnore
    public List<String> getResolvedDistricts() {
        return normalizeList(districts);
    }

    @JsonIgnore
    public List<String> getResolvedBlocks() {
        return normalizeList(blocks);
    }

    @JsonIgnore
    public List<String> getResolvedFacilityCategories() {
        return normalizeList(facilityCategories);
    }

    @JsonIgnore
    public List<String> getResolvedFacilityTypes() {
        return normalizeList(facilityTypes);
    }

    @JsonIgnore
    public List<String> getResolvedPhoneStatuses() {
        return normalizeList(phoneStatuses);
    }

    @JsonIgnore
    public List<String> getResolvedFieldStatuses() {
        return normalizeList(fieldStatuses);
    }

    @JsonIgnore
    public List<String> getResolvedOverallStatuses() {
        return normalizeList(overallStatuses);
    }

    @JsonIgnore
    public boolean includesNotInitiatedFieldStatus() {
        return getResolvedFieldStatuses().stream()
                .anyMatch(this::isNotInitiatedFieldStatusToken);
    }

    @JsonIgnore
    public List<String> getResolvedConcreteFieldStatuses() {
        return getResolvedFieldStatuses().stream()
                .filter(value -> !isNotInitiatedFieldStatusToken(value))
                .toList();
    }

    private boolean isNotInitiatedFieldStatusToken(String value) {
        return "NULL".equalsIgnoreCase(value) || "NOT_INITIATED".equalsIgnoreCase(value);
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
