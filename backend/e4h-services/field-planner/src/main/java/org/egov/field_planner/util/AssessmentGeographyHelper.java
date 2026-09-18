package org.egov.field_planner.util;

import org.apache.commons.lang3.StringUtils;
import org.egov.field_planner.web.models.AssessmentPlan;
import org.egov.tracer.model.CustomException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AssessmentGeographyHelper {

    private AssessmentGeographyHelper() {
    }

    public static Map<String, Object> resolveGeographyScope(AssessmentPlan plan) {
        if (plan.getGeographyDetails() != null && !plan.getGeographyDetails().isEmpty()) {
            return new LinkedHashMap<>(plan.getGeographyDetails());
        }
        Map<String, Object> scope = new LinkedHashMap<>();
        scope.put("state", plan.getState() != null ? plan.getState() : "");
        return scope;
    }

    public static void syncPlanGeography(AssessmentPlan plan) {
        Map<String, Object> scope = resolveGeographyScope(plan);
        plan.setGeographyDetails(scope);
        Object state = scope.get("state");
        if (state != null && StringUtils.isNotBlank(state.toString())) {
            plan.setState(state.toString());
        }
    }

    public static void validateGeography(AssessmentPlan plan) {
        if (plan.getGeographyDetails() == null || plan.getGeographyDetails().isEmpty()) {
            if (StringUtils.isBlank(plan.getState())) {
                throw new CustomException(
                        AssessmentConstants.ASSESSMENT_INVALID_GEOGRAPHY,
                        "geographyDetails or state is required");
            }
            return;
        }

        Map<String, Object> geography = plan.getGeographyDetails();
        String state = asNonBlankString(geography.get("state"));
        if (StringUtils.isBlank(state)) {
            throw new CustomException(
                    AssessmentConstants.ASSESSMENT_INVALID_GEOGRAPHY,
                    "geographyDetails.state is required");
        }

        List<String> districts = asStringList(geography.get("districts"));
        if (districts.isEmpty()) {
            throw new CustomException(
                    AssessmentConstants.ASSESSMENT_INVALID_GEOGRAPHY,
                    "geographyDetails.districts is required");
        }

        List<String> blocks = asStringList(geography.get("blocks"));
        if (blocks.isEmpty()) {
            throw new CustomException(
                    AssessmentConstants.ASSESSMENT_INVALID_GEOGRAPHY,
                    "geographyDetails.blocks is required");
        }
    }

    private static String asNonBlankString(Object value) {
        return value == null ? null : value.toString().trim();
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            List<String> codes = new ArrayList<>();
            for (Object item : list) {
                if (item == null) {
                    continue;
                }
                String code = item.toString().trim();
                if (!code.isEmpty()) {
                    codes.add(code);
                }
            }
            return codes;
        }
        String single = value.toString().trim();
        return single.isEmpty() ? List.of() : List.of(single);
    }
}
