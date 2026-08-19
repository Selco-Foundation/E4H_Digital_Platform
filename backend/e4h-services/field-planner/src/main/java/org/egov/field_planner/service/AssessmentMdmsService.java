package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.AssessmentFormSchema;
import org.egov.field_planner.web.models.AssessmentOutcomeRule;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.apache.commons.lang3.StringUtils;
import org.egov.field_planner.web.models.AssessmentFormField;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentMdmsService {

    private static final String MDMS_MODULE = "assessment";
    private static final String FORM_SCHEMA_MASTER = "AssessmentFormSchema";
    private static final String MOBILE_FORM_SCHEMA_MASTER = "AssessmentMobileFormSchema";
    private static final String OUTCOME_RULES_MASTER = "AssessmentOutcomeRules";

    private final ServiceRequestClient serviceRequestClient;
    private final FieldPlannerConfiguration configuration;
    private final ObjectMapper objectMapper;

    public String resolveFormType(String facilityCategory, String assessmentPhase) {
        String category = facilityCategory != null ? facilityCategory.toUpperCase() : "";
        if (AssessmentConstants.PHASE_PHONE.equals(assessmentPhase)) {
            return AssessmentConstants.CATEGORY_HEALTH.equals(category) ? "HF_PHONE" : "AWC_PHONE";
        }
        return AssessmentConstants.CATEGORY_HEALTH.equals(category) ? "HF_FIELD" : "AWC_FIELD";
    }

    public AssessmentFormSchema getFormSchema(RequestInfo requestInfo, String tenantId, String formType) {
        Map<String, Object> record = fetchRecord(requestInfo, tenantId, FORM_SCHEMA_MASTER, "formType", formType);
        if (record == null) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_FORM_NOT_AVAILABLE,
                    "MDMS form schema not found for formType: " + formType
                            + ". Seed assessment.AssessmentFormSchema in MDMS (see docs/assessment-module/master-data-schema).");
        }
        return objectMapper.convertValue(record, AssessmentFormSchema.class);
    }

    /**
     * Full mobile form flattened from {@code AssessmentMobileFormSchema} pages/properties.
     * Used for Excel response summaries. Falls back to criteria {@link #getFormSchema} if absent.
     */
    public AssessmentFormSchema getMobileFormSchema(RequestInfo requestInfo, String tenantId, String formType) {
        Map<String, Object> record = fetchRecord(
                requestInfo, tenantId, MOBILE_FORM_SCHEMA_MASTER, "formType", formType);
        if (record != null) {
            return flattenMobileFormRecord(record);
        }
        log.warn("MDMS {} not found for formType {} — falling back to {}", MOBILE_FORM_SCHEMA_MASTER, formType,
                FORM_SCHEMA_MASTER);
        return getFormSchema(requestInfo, tenantId, formType);
    }

    @SuppressWarnings("unchecked")
    private AssessmentFormSchema flattenMobileFormRecord(Map<String, Object> record) {
        List<AssessmentFormField> fields = new ArrayList<>();
        Object pagesObj = record.get("pages");
        if (pagesObj instanceof List<?> pageList) {
            for (Object pageObj : pageList) {
                Map<String, Object> page = objectMapper.convertValue(pageObj, Map.class);
                String pageKey = page.get("page") != null ? page.get("page").toString() : null;
                Object propertiesObj = page.get("properties");
                if (!(propertiesObj instanceof List<?> propertyList)) {
                    continue;
                }
                for (Object propertyObj : propertyList) {
                    Map<String, Object> property = objectMapper.convertValue(propertyObj, Map.class);
                    if (!Boolean.TRUE.equals(property.get("includeInForm"))) {
                        continue;
                    }
                    String fieldName = property.get("fieldName") != null
                            ? property.get("fieldName").toString() : null;
                    if (StringUtils.isBlank(fieldName)) {
                        continue;
                    }
                    fields.add(AssessmentFormField.builder()
                            .fieldCode(fieldName)
                            .label(property.get("label") != null ? property.get("label").toString() : fieldName)
                            .type(property.get("type") != null ? property.get("type").toString() : null)
                            .required(hasRequiredValidation(property))
                            .pageKey(pageKey)
                            .enumLabels(buildEnumLabels(property))
                            .build());
                }
            }
        }
        String formType = record.get("formType") != null ? record.get("formType").toString() : null;
        return AssessmentFormSchema.builder().formType(formType).fields(fields).build();
    }

    @SuppressWarnings("unchecked")
    private boolean hasRequiredValidation(Map<String, Object> property) {
        Object validationsObj = property.get("validations");
        if (!(validationsObj instanceof List<?> validations)) {
            return false;
        }
        for (Object validationObj : validations) {
            Map<String, Object> validation = objectMapper.convertValue(validationObj, Map.class);
            if ("required".equals(validation.get("type")) && Boolean.TRUE.equals(validation.get("value"))) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> buildEnumLabels(Map<String, Object> property) {
        Object enumsObj = property.get("enums");
        if (!(enumsObj instanceof List<?> enums) || enums.isEmpty()) {
            return null;
        }
        Map<String, String> labels = new LinkedHashMap<>();
        for (Object enumObj : enums) {
            Map<String, Object> enumEntry = objectMapper.convertValue(enumObj, Map.class);
            Object code = enumEntry.get("code");
            if (code == null) {
                continue;
            }
            Object name = enumEntry.get("name");
            labels.put(code.toString(), name != null ? name.toString() : code.toString());
        }
        return labels.isEmpty() ? null : labels;
    }

    public List<AssessmentOutcomeRule> getOutcomeRules(RequestInfo requestInfo, String tenantId, String formType) {
        Map<String, Object> record = fetchRecord(requestInfo, tenantId, OUTCOME_RULES_MASTER, "formType", formType);
        if (record == null) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_FORM_NOT_AVAILABLE,
                    "MDMS outcome rules not found for formType: " + formType
                            + ". Seed assessment.AssessmentOutcomeRules in MDMS.");
        }
        Object rules = record.get("rules");
        if (!(rules instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        return list.stream()
                .map(item -> objectMapper.convertValue(item, AssessmentOutcomeRule.class))
                .sorted(Comparator.comparingInt(r -> r.getPriority() != null ? r.getPriority() : Integer.MAX_VALUE))
                .collect(Collectors.toList());
    }

    public String getDefaultOutcome(RequestInfo requestInfo, String tenantId, String formType) {
        Map<String, Object> record = fetchRecord(requestInfo, tenantId, OUTCOME_RULES_MASTER, "formType", formType);
        if (record != null && record.get("defaultOutcome") != null) {
            return record.get("defaultOutcome").toString();
        }
        return AssessmentConstants.OUTCOME_QUALIFIED;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchRecord(RequestInfo requestInfo, String tenantId,
                                             String masterName, String keyField, String keyValue) {
        try {
            MasterDetail masterDetail = MasterDetail.builder()
                    .name(masterName)
                    .filter("[?(@." + keyField + "=='" + keyValue + "')]")
                    .build();
            ModuleDetail moduleDetail = ModuleDetail.builder()
                    .moduleName(MDMS_MODULE)
                    .masterDetails(List.of(masterDetail))
                    .build();
            MdmsCriteria criteria = MdmsCriteria.builder()
                    .tenantId(tenantId)
                    .moduleDetails(List.of(moduleDetail))
                    .build();
            MdmsCriteriaReq request = MdmsCriteriaReq.builder()
                    .requestInfo(requestInfo)
                    .mdmsCriteria(criteria)
                    .build();
            String url = configuration.getMdmsHost() + configuration.getMdmsEndPoint();
            LinkedHashMap<String, Object> response = serviceRequestClient.fetchResult(
                    new StringBuilder(url), request, LinkedHashMap.class);
            Object mdmsRes = response.get("MdmsRes");
            if (!(mdmsRes instanceof Map<?, ?> mdmsMap)) {
                return null;
            }
            Object module = mdmsMap.get(MDMS_MODULE);
            if (!(module instanceof Map<?, ?> moduleMap)) {
                return null;
            }
            Object master = moduleMap.get(masterName);
            if (master instanceof List<?> records && !records.isEmpty()) {
                return objectMapper.convertValue(records.get(0), Map.class);
            }
        } catch (Exception e) {
            log.warn("MDMS fetch failed for {} / {}: {}", masterName, keyValue, e.getMessage());
        }
        return null;
    }
}
