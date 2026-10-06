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
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final FileStoreService fileStoreService;

    @Autowired
    public FieldPlanTemplateService(
            FieldPlanTemplateRepository fieldPlanTemplateRepository,
            FieldPlannerRepository fieldPlannerRepository,
            FieldPlanTemplateValidator validator,
            FieldPlanTemplateEnrichment enrichment,
            Producer producer,
            FieldPlannerConfiguration fieldPlannerConfiguration,
            FileStoreService fileStoreService) {
        this.fieldPlanTemplateRepository = fieldPlanTemplateRepository;
        this.fieldPlannerRepository = fieldPlannerRepository;
        this.validator = validator;
        this.enrichment = enrichment;
        this.producer = producer;
        this.fieldPlannerConfiguration = fieldPlannerConfiguration;
        this.fileStoreService = fileStoreService;
    }

    public List<FieldPlanTemplate> create(FieldPlanTemplateWriteRequest writeRequest) {
        FieldPlanTemplateBulkRequest request = writeRequest.getBulkRequest();
        validator.validateCreateRequest(request);
        validateFieldPlanIds(request);

        List<FieldPlanTemplate> templates = request.getFieldPlanTemplates();
        List<MultipartFile> excelFiles = writeRequest.getExcelFiles();
        // Excel files are required for create and length-validated 1:1 against templates by
        // validateTemplateFileForWrite in the controller, so it's safe to index files by
        // position. Each template is stamped with its own fileStoreId - letting the user
        // re-open the exact file they uploaded, with the filled-in data, later.
        try {
            for (int i = 0; i < templates.size(); i++) {
                FieldPlanTemplate template = templates.get(i);
                String fileStoreId = uploadExcelFile(request.getRequestInfo(), excelFiles.get(i));
                template.setFileStoreId(fileStoreId);
                enrichment.enrichOnCreate(template, request.getRequestInfo());
            }
            producer.push(fieldPlannerConfiguration.getCreateFieldPlanTemplateTopic(), request);
            log.info("Successfully pushed {} installation plan templates for creation", templates.size());
        } catch (Exception exception) {
            log.error("Error occurred while creating installation plan templates: {}", ExceptionUtils.getStackTrace(exception));
            throw new CustomException("FIELD_PLAN_TEMPLATE_CREATE", "Failed to create installation plan templates");
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

        // The filestore has no delete API (files are kept immutable for audit), so "replacing"
        // a template's file means uploading the new one and swapping the fileStoreId reference
        // - the old file is simply left unreferenced rather than deleted. If no new excelFiles
        // list is provided, every template's existing fileStoreId from the DB is carried over
        // unchanged.
        List<MultipartFile> excelFiles = writeRequest.getExcelFiles();
        boolean hasNewFiles = !CollectionUtils.isEmpty(excelFiles);

        List<FieldPlanTemplate> templates = request.getFieldPlanTemplates();
        for (int i = 0; i < templates.size(); i++) {
            FieldPlanTemplate template = templates.get(i);
            FieldPlanTemplate templateFromDb = findTemplateById(template.getId(), templatesFromDb);
            if (templateFromDb != null) {
                mergeTemplateData(template, templateFromDb);
                String fileStoreId = hasNewFiles
                        ? uploadExcelFile(request.getRequestInfo(), excelFiles.get(i))
                        : templateFromDb.getFileStoreId();
                template.setFileStoreId(fileStoreId);
                enrichment.enrichOnUpdate(template, templateFromDb, request.getRequestInfo());
            }
        }

        producer.push(fieldPlannerConfiguration.getUpdateFieldPlanTemplateTopic(), request);
        return request;
    }

    private String uploadExcelFile(RequestInfo requestInfo, MultipartFile excelFile) {
        try {
            return fileStoreService.upload(requestInfo, excelFile);
        } catch (IOException e) {
            log.error("Failed to upload installation plan template Excel file", e);
            throw new CustomException("ERROR_FIELD_PLAN_TEMPLATE_UPLOAD", "Failed to upload Excel file: " + e.getMessage());
        }
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
            log.error("Error while validating installation plan ids", ExceptionUtils.getStackTrace(e));
            throw new CustomException("FIELDPLAN_ERROR", "Error while validating installation plan ids");
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
