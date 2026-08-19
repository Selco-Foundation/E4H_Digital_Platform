package org.egov.field_planner.validator;

import org.apache.commons.lang3.StringUtils;
import org.egov.field_planner.web.models.FieldPlanTemplate;
import org.egov.field_planner.web.models.FieldPlanTemplateBulkRequest;
import org.egov.field_planner.web.models.FieldPlanTemplateSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FieldPlanTemplateValidator {

    private static final Set<String> ALLOWED_EXCEL_EXTENSIONS = Set.of("xls", "xlsx");

    public void validateTemplateFileForWrite(
            FieldPlanTemplateBulkRequest request,
            List<MultipartFile> excelFiles,
            boolean requireTemplateFiles) {
        Map<String, String> errorMap = new HashMap<>();
        List<FieldPlanTemplate> templates = request.getFieldPlanTemplates();

        if (CollectionUtils.isEmpty(templates)) {
            throw new CustomException("FIELD_PLAN_TEMPLATE", "At least one template is required");
        }

        boolean noFilesProvided = CollectionUtils.isEmpty(excelFiles);

        if (requireTemplateFiles && noFilesProvided) {
            throw new CustomException("MISSING_TEMPLATE_FILES", "At least one template file is required");
        }

        // Update allows a fully-omitted excelFiles list (no template in this batch gets a new
        // file). But once ANY file is supplied, the list must exactly match the templates list
        // 1:1 - partial/sparse lists are rejected rather than guessed at positionally.
        if (!noFilesProvided && excelFiles.size() != templates.size()) {
            throw new CustomException(
                    "TEMPLATE_FILE_COUNT_MISMATCH",
                    "Expected " + templates.size() + " template file(s) but received " + excelFiles.size());
        }

        if (!noFilesProvided) {
            int index = 1;
            for (int i = 0; i < templates.size(); i++) {
                validateSystemTypeTemplateFile(templates.get(i).getSystemType(), excelFiles.get(i), true, errorMap, index++);
            }
        }

        if (!errorMap.isEmpty()) {
            throw new CustomException(errorMap);
        }
    }

    private void validateSystemTypeTemplateFile(
            String systemType,
            MultipartFile templateFile,
            boolean requireTemplateFile,
            Map<String, String> errorMap,
            int index) {
        if (templateFile == null || templateFile.isEmpty()) {
            if (requireTemplateFile) {
                errorMap.put(
                        "MISSING_TEMPLATE_FILE_" + index,
                        "Template file is required for systemType '" + systemType + "'");
            }
            return;
        }

        String originalFilename = templateFile.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename) || !originalFilename.contains(".")) {
            errorMap.put("INVALID_TEMPLATE_FILE_" + index, "Template file must have a valid extension");
            return;
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXCEL_EXTENSIONS.contains(extension)) {
            errorMap.put("INVALID_TEMPLATE_FILE_" + index, "Only .xls and .xlsx template files are supported");
        }
    }

    public void validateCreateRequest(FieldPlanTemplateBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        List<FieldPlanTemplate> templates = request.getFieldPlanTemplates();

        if (CollectionUtils.isEmpty(templates)) {
            throw new CustomException("FIELD_PLAN_TEMPLATE", "At least one template is required");
        }

        int index = 1;
        for (FieldPlanTemplate template : templates) {
            validateTemplate(template, errorMap, index++);
        }

        if (!errorMap.isEmpty()) {
            throw new CustomException(errorMap);
        }
    }

    public void validateUpdateRequest(FieldPlanTemplateBulkRequest request, List<FieldPlanTemplate> templatesFromDb) {
        Map<String, String> errorMap = new HashMap<>();
        List<FieldPlanTemplate> templates = request.getFieldPlanTemplates();

        if (CollectionUtils.isEmpty(templates)) {
            throw new CustomException("FIELD_PLAN_TEMPLATE", "At least one template is required");
        }

        int index = 1;
        for (FieldPlanTemplate template : templates) {
            if (StringUtils.isBlank(template.getId())) {
                errorMap.put("MISSING_ID_" + index, "id is required for update");
            }
            validateTemplate(template, errorMap, index++);
        }

        for (FieldPlanTemplate template : templates) {
            if (StringUtils.isNotBlank(template.getId())) {
                boolean exists = templatesFromDb.stream()
                        .anyMatch(existing -> existing.getId().equals(template.getId()));
                if (!exists) {
                    errorMap.put("INVALID_TEMPLATE_ID", "Template does not exist: " + template.getId());
                }
            }
        }

        if (!errorMap.isEmpty()) {
            throw new CustomException(errorMap);
        }
    }

    public void validateSearchRequest(FieldPlanTemplateSearchRequest request, Integer limit, Integer offset, String tenantId) {
        if (request == null || request.getCriteria() == null) {
            throw new CustomException("FIELD_PLAN_TEMPLATE_SEARCH", "Search criteria is required");
        }
        if (StringUtils.isBlank(tenantId)) {
            throw new CustomException("FIELD_PLAN_TEMPLATE_SEARCH", "tenantId is required");
        }
        if (limit != null && limit <= 0) {
            throw new CustomException("FIELD_PLAN_TEMPLATE_SEARCH", "limit must be greater than 0");
        }
        if (offset != null && offset < 0) {
            throw new CustomException("FIELD_PLAN_TEMPLATE_SEARCH", "offset must be greater than or equal to 0");
        }
    }

    private void validateTemplate(FieldPlanTemplate template, Map<String, String> errorMap, int index) {
        if (StringUtils.isBlank(template.getTenantId())) {
            errorMap.put("MISSING_TENANT_ID_" + index, "tenantId is required");
        }
        if (StringUtils.isBlank(template.getFieldPlanId())) {
            errorMap.put("MISSING_FIELD_PLAN_ID_" + index, "fieldPlanId is required");
        }
        if (StringUtils.isBlank(template.getSystemType())) {
            errorMap.put("MISSING_SYSTEM_TYPE_" + index, "systemType is required");
        }
        if (StringUtils.isBlank(template.getTotalCapacity())) {
            errorMap.put("MISSING_TOTAL_CAPACITY_" + index, "totalCapacity is required");
        }
    }
}
