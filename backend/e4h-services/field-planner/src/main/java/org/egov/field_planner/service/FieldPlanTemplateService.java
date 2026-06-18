package org.egov.field_planner.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.repository.FieldPlanTemplateRepository;
import org.egov.field_planner.repository.FieldPlannerRepository;
import org.egov.field_planner.service.enrichment.FieldPlanTemplateEnrichment;
import org.egov.field_planner.validator.FieldPlanTemplateValidator;
import org.egov.field_planner.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.getIdFieldName;
import static org.egov.common.utils.CommonUtils.getMethod;
import static org.egov.common.utils.CommonUtils.getObjClass;
import static org.egov.field_planner.Constants.GET_FIELDPLAN_ID;

@Service
@Slf4j
public class FieldPlanTemplateService {

    private final FieldPlanTemplateRepository fieldPlanTemplateRepository;
    private final FieldPlannerRepository fieldPlannerRepository;
    private final FieldPlanTemplateValidator validator;
    private final FieldPlanTemplateEnrichment enrichment;
    private final Producer producer;
    private final FieldPlannerConfiguration fieldPlannerConfiguration;

    @Autowired
    public FieldPlanTemplateService(
            FieldPlanTemplateRepository fieldPlanTemplateRepository,
            FieldPlannerRepository fieldPlannerRepository,
            FieldPlanTemplateValidator validator,
            FieldPlanTemplateEnrichment enrichment,
            Producer producer,
            FieldPlannerConfiguration fieldPlannerConfiguration) {
        this.fieldPlanTemplateRepository = fieldPlanTemplateRepository;
        this.fieldPlannerRepository = fieldPlannerRepository;
        this.validator = validator;
        this.enrichment = enrichment;
        this.producer = producer;
        this.fieldPlannerConfiguration = fieldPlannerConfiguration;
    }

    public List<FieldPlanTemplate> create(FieldPlanTemplateWriteRequest writeRequest) {
        FieldPlanTemplateBulkRequest request = writeRequest.getBulkRequest();
        validator.validateCreateRequest(request);
        validateFieldPlanIds(request);

        List<FieldPlanTemplate> templates = request.getFieldPlanTemplates();
        try {
            for (FieldPlanTemplate template : templates) {
                enrichment.enrichOnCreate(template, request.getRequestInfo());
            }
            producer.push(fieldPlannerConfiguration.getCreateFieldPlanTemplateTopic(), request);
            log.info(
                    "Successfully pushed {} field plan templates for creation (excelFileProvided={})",
                    templates.size(),
                    hasExcelFile(writeRequest.getExcelFile()));
        } catch (Exception exception) {
            log.error("Error occurred while creating field plan templates: {}", ExceptionUtils.getStackTrace(exception));
            throw new CustomException("FIELD_PLAN_TEMPLATE_CREATE", "Failed to create field plan templates");
        }
        return templates;
    }

    public FieldPlanTemplateBulkRequest update(FieldPlanTemplateWriteRequest writeRequest) {
        FieldPlanTemplateBulkRequest request = writeRequest.getBulkRequest();
        List<FieldPlanTemplate> templatesFromDb = search(
                buildSearchRequest(request.getFieldPlanTemplates(), request.getRequestInfo()),
                fieldPlannerConfiguration.getMaxLimit(),
                fieldPlannerConfiguration.getDefaultOffset(),
                request.getFieldPlanTemplates().get(0).getTenantId(),
                null);

        validator.validateUpdateRequest(request, templatesFromDb);

        for (FieldPlanTemplate template : request.getFieldPlanTemplates()) {
            FieldPlanTemplate templateFromDb = findTemplateById(template.getId(), templatesFromDb);
            if (templateFromDb != null) {
                mergeTemplateData(template, templateFromDb);
                enrichment.enrichOnUpdate(template, templateFromDb, request.getRequestInfo());
            }
        }

        producer.push(fieldPlannerConfiguration.getUpdateFieldPlanTemplateTopic(), request);
        return request;
    }

    private boolean hasExcelFile(MultipartFile excelFile) {
        return excelFile != null && !excelFile.isEmpty();
    }

    public List<FieldPlanTemplate> search(
            FieldPlanTemplateSearchRequest request,
            Integer limit,
            Integer offset,
            String tenantId,
            Long lastChangedSince) {
        validator.validateSearchRequest(request, limit, offset, tenantId);
        return fieldPlanTemplateRepository.getFieldPlanTemplates(request, limit, offset, tenantId, lastChangedSince);
    }

    public Integer count(FieldPlanTemplateSearchRequest request, String tenantId, Long lastChangedSince) {
        return fieldPlanTemplateRepository.getFieldPlanTemplateCount(request, tenantId, lastChangedSince);
    }

    private void validateFieldPlanIds(FieldPlanTemplateBulkRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        List<FieldPlanTemplate> templates = request.getFieldPlanTemplates();
        if (templates.isEmpty()) {
            return;
        }

        Class<?> objClass = getObjClass(templates);
        List<String> fieldPlanIds = templates.stream().map(FieldPlanTemplate::getFieldPlanId).toList();
        try {
            AtomicInteger counter = new AtomicInteger(1);
            List<String> existingFieldPlanIds = fieldPlannerRepository.validateIds(fieldPlanIds, getIdFieldName(getMethod(GET_FIELDPLAN_ID, objClass)));
            templates.stream()
                    .filter(template -> !existingFieldPlanIds.contains(template.getFieldPlanId()))
                    .forEach(template -> {
                        int i = counter.getAndIncrement();
                        errorMap.put("INVALID_FIELDPLAN_" + i, "fieldPlanId does not exist: " + template.getFieldPlanId());
                    });
        } catch (Exception e) {
            log.error("Error while validating field plan ids", ExceptionUtils.getStackTrace(e));
            throw new CustomException("FIELDPLAN_ERROR", "Error while validating field plan ids");
        }

        if (!errorMap.isEmpty()) {
            throw new CustomException(errorMap);
        }
    }

    private FieldPlanTemplateSearchRequest buildSearchRequest(List<FieldPlanTemplate> templates, RequestInfo requestInfo) {
        List<String> ids = templates.stream().map(FieldPlanTemplate::getId).collect(Collectors.toList());
        FieldPlanTemplateSearchCriteria criteria = FieldPlanTemplateSearchCriteria.builder()
                .ids(ids)
                .tenantId(templates.get(0).getTenantId())
                .build();
        return FieldPlanTemplateSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(criteria)
                .build();
    }

    private FieldPlanTemplate findTemplateById(String id, List<FieldPlanTemplate> templatesFromDb) {
        return templatesFromDb.stream()
                .filter(template -> Objects.equals(id, template.getId()))
                .findFirst()
                .orElse(null);
    }

    private void mergeTemplateData(FieldPlanTemplate template, FieldPlanTemplate templateFromDb) {
        if (template.getTemplateData() == null) {
            template.setTemplateData(templateFromDb.getTemplateData());
            return;
        }
        if (templateFromDb.getTemplateData() != null) {
            Map<String, Object> merged = new HashMap<>(templateFromDb.getTemplateData());
            merged.putAll(template.getTemplateData());
            template.setTemplateData(merged);
        }
    }
}
