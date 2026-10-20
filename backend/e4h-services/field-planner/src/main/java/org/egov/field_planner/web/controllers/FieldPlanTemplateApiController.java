package org.egov.field_planner.web.controllers;

import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import org.egov.common.models.core.URLParams;
import org.egov.common.utils.ResponseInfoFactory;
import org.egov.field_planner.service.FieldPlanTemplateService;
import org.egov.field_planner.validator.FieldPlanTemplateValidator;
import org.egov.field_planner.web.models.FieldPlanTemplateBulkRequest;
import org.egov.field_planner.web.models.FieldPlanTemplateResponse;
import org.egov.field_planner.web.models.FieldPlanTemplateSearchRequest;
import org.egov.field_planner.web.models.FieldPlanTemplateWriteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/v1/field-plan-templates")
@Validated
public class FieldPlanTemplateApiController {

    private final FieldPlanTemplateService fieldPlanTemplateService;
    private final FieldPlanTemplateValidator fieldPlanTemplateValidator;

    @Autowired
    public FieldPlanTemplateApiController(
            FieldPlanTemplateService fieldPlanTemplateService,
            FieldPlanTemplateValidator fieldPlanTemplateValidator) {
        this.fieldPlanTemplateService = fieldPlanTemplateService;
        this.fieldPlanTemplateValidator = fieldPlanTemplateValidator;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FieldPlanTemplateResponse> createFieldPlanTemplates(
            @ApiParam(value = "Installation plan template payload.", required = true)
            @Valid @RequestPart("request") FieldPlanTemplateBulkRequest request,
            @ApiParam(value = "ICC report template Excel files (.xls/.xlsx), one per template, matched positionally to request.FieldPlanTemplates.", required = true)
            @RequestPart("excelFiles") List<MultipartFile> excelFiles) {

        fieldPlanTemplateValidator.validateTemplateFileForWrite(request, excelFiles, true);

        FieldPlanTemplateWriteRequest writeRequest = FieldPlanTemplateWriteRequest.builder()
                .bulkRequest(request)
                .excelFiles(excelFiles)
                .build();

        FieldPlanTemplateResponse response = FieldPlanTemplateResponse.builder()
                .fieldPlanTemplates(fieldPlanTemplateService.create(writeRequest))
                .responseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FieldPlanTemplateResponse> updateFieldPlanTemplates(
            @ApiParam(value = "Installation plan template payload.", required = true)
            @Valid @RequestPart("request") FieldPlanTemplateBulkRequest request,
            @ApiParam(value = "ICC report template Excel files (.xls/.xlsx), one per template. Omit entirely to leave every template's file unchanged.")
            @RequestPart(value = "excelFiles", required = false) List<MultipartFile> excelFiles) {

        List<MultipartFile> safeExcelFiles = excelFiles != null ? excelFiles : new ArrayList<>();
        fieldPlanTemplateValidator.validateTemplateFileForWrite(request, safeExcelFiles, false);

        FieldPlanTemplateWriteRequest writeRequest = FieldPlanTemplateWriteRequest.builder()
                .bulkRequest(request)
                .excelFiles(safeExcelFiles)
                .build();

        FieldPlanTemplateBulkRequest enrichedRequest = fieldPlanTemplateService.update(writeRequest);
        FieldPlanTemplateResponse response = FieldPlanTemplateResponse.builder()
                .fieldPlanTemplates(enrichedRequest.getFieldPlanTemplates())
                .responseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<FieldPlanTemplateResponse> searchFieldPlanTemplates(
            @ApiParam(value = "Search installation plan templates.", required = true)
            @Valid @RequestBody FieldPlanTemplateSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams) {

        FieldPlanTemplateResponse response = FieldPlanTemplateResponse.builder()
                .fieldPlanTemplates(fieldPlanTemplateService.search(
                        request,
                        urlParams.getLimit(),
                        urlParams.getOffset(),
                        urlParams.getTenantId(),
                        urlParams.getLastChangedSince()))
                .totalCount(fieldPlanTemplateService.count(
                        request, urlParams.getTenantId(), urlParams.getLastChangedSince()))
                .responseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
