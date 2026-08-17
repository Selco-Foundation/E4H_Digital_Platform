package org.egov.field_planner.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentFormField {

    @JsonProperty("fieldCode")
    private String fieldCode;

    @JsonProperty("label")
    private String label;

    @JsonProperty("type")
    private String type;

    @JsonProperty("required")
    private boolean required;

    @JsonProperty("options")
    private List<String> options;

    /** Mobile form page key for nested submissionData (e.g. facilityServices). */
    @JsonProperty("pageKey")
    private String pageKey;

    /** Enum code → display label from AssessmentMobileFormSchema. */
    @JsonProperty("enumLabels")
    private Map<String, String> enumLabels;
}
