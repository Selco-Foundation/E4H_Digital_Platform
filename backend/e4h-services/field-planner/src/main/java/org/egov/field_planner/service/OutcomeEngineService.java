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
            Object value = submissionData != null ? submissionData.get(field.getFieldCode()) : null;
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
            Object value = submissionData.get(field.getFieldCode());
            if (value != null) {
                summary.add(field.getLabel() + ": " + value);
            }
        }
        return summary;
    }

    private boolean matchesRule(AssessmentOutcomeRule rule, Map<String, Object> submissionData) {
        if (rule == null || submissionData == null || StringUtils.isBlank(rule.getFieldCode())) {
            return false;
        }
        Object rawValue = submissionData.get(rule.getFieldCode());
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
}
