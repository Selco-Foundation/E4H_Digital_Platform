package org.egov.field_planner.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.AssessmentFormField;
import org.egov.field_planner.web.models.AssessmentFormSchema;
import org.egov.field_planner.web.models.AssessmentOutcomeRule;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OutcomeEngineService {

    private final AssessmentMdmsService mdmsService;

    public String evaluate(String tenantId, org.egov.common.contract.request.RequestInfo requestInfo,
                           String formType, Map<String, Object> submissionData) {
        List<AssessmentOutcomeRule> rules = mdmsService.getOutcomeRules(requestInfo, tenantId, formType);
        for (AssessmentOutcomeRule rule : rules) {
            if (matchesRule(rule, submissionData)) {
                return AssessmentConstants.OUTCOME_NOT_QUALIFIED;
            }
        }
        return mdmsService.getDefaultOutcome(requestInfo, tenantId, formType);
    }

    public void validateSubmissionData(AssessmentFormSchema schema, Map<String, Object> submissionData) {
        if (schema == null || schema.getFields() == null) {
            return;
        }
        List<String> missing = new ArrayList<>();
        for (AssessmentFormField field : schema.getFields()) {
            if (!field.isRequired()) {
                continue;
            }
            Object value = resolveFieldValue(submissionData, field);
            if (value == null || StringUtils.isBlank(value.toString())) {
                missing.add(field.getFieldCode());
            }
        }
        if (!missing.isEmpty()) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_INVALID_FORM_DATA,
                    "Required field missing: " + missing.get(0));
        }
    }

    public List<String> buildResponseSummary(AssessmentFormSchema schema, Map<String, Object> submissionData) {
        List<String> summary = new ArrayList<>();
        if (schema == null || schema.getFields() == null || submissionData == null) {
            return summary;
        }
        for (AssessmentFormField field : schema.getFields()) {
            Object value = resolveFieldValue(submissionData, field);
            if (value != null && !StringUtils.isBlank(value.toString())) {
                summary.add(field.getLabel() + ": " + formatSummaryValue(field, value));
            }
        }
        return summary;
    }

    private Object resolveFieldValue(Map<String, Object> submissionData, AssessmentFormField field) {
        if (submissionData == null || field == null || StringUtils.isBlank(field.getFieldCode())) {
            return null;
        }
        Object value = submissionData.get(field.getFieldCode());
        if (value != null) {
            return value;
        }
        if (StringUtils.isNotBlank(field.getPageKey())) {
            Object page = submissionData.get(field.getPageKey());
            if (page instanceof Map<?, ?> pageMap) {
                return pageMap.get(field.getFieldCode());
            }
        }
        // Deep scan nested page maps when submission is page-keyed but field not found at top level.
        for (Object nested : submissionData.values()) {
            if (nested instanceof Map<?, ?> pageMap && pageMap.containsKey(field.getFieldCode())) {
                return pageMap.get(field.getFieldCode());
            }
        }
        return null;
    }

    private String formatSummaryValue(AssessmentFormField field, Object value) {
        Map<String, String> enumLabels = field.getEnumLabels();
        if (enumLabels != null && !enumLabels.isEmpty()) {
            if (value instanceof List<?> list) {
                return list.stream()
                        .map(item -> item != null ? resolveEnumLabel(enumLabels, item.toString()) : "")
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.joining(", "));
            }
            return resolveEnumLabel(enumLabels, value.toString());
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(item -> item != null ? item.toString() : "")
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining(", "));
        }
        return value.toString();
    }

    private String resolveEnumLabel(Map<String, String> enumLabels, String code) {
        String label = enumLabels.get(code);
        return label != null ? label : code;
    }

    private boolean matchesRule(AssessmentOutcomeRule rule, Map<String, Object> submissionData) {
        if (rule == null || submissionData == null || StringUtils.isBlank(rule.getFieldCode())) {
            return false;
        }
        Object rawValue = resolveSubmissionValue(submissionData, rule.getFieldCode());
        String value = rawValue != null ? rawValue.toString() : null;
        String operator = rule.getOperator() != null ? rule.getOperator().toUpperCase() : "EQ";
        return switch (operator) {
            case "IN" -> rule.getValues() != null && value != null
                    && rule.getValues().stream().anyMatch(v -> v.equalsIgnoreCase(value));
            case "NOT_IN" -> rule.getValues() != null && value != null
                    && rule.getValues().stream().noneMatch(v -> v.equalsIgnoreCase(value));
            case "NE" -> value != null && rule.getValues() != null && !rule.getValues().isEmpty()
                    && !value.equalsIgnoreCase(rule.getValues().get(0));
            case "IS_EMPTY" -> value == null || StringUtils.isBlank(value);
            case "IS_NOT_EMPTY" -> value != null && StringUtils.isNotBlank(value);
            case "BOOLEAN_TRUE" -> "true".equalsIgnoreCase(value) || "YES".equalsIgnoreCase(value);
            case "BOOLEAN_FALSE" -> "false".equalsIgnoreCase(value) || "NO".equalsIgnoreCase(value);
            default -> value != null && rule.getValues() != null && !rule.getValues().isEmpty()
                    && value.equalsIgnoreCase(rule.getValues().get(0));
        };
    }

    private Object resolveSubmissionValue(Map<String, Object> submissionData, String fieldCode) {
        Object value = submissionData.get(fieldCode);
        if (value != null) {
            return value;
        }
        for (Object nested : submissionData.values()) {
            if (nested instanceof Map<?, ?> pageMap && pageMap.containsKey(fieldCode)) {
                return pageMap.get(fieldCode);
            }
        }
        return null;
    }
}
