package org.egov.activity.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.service.BomService;
import org.egov.activity.web.models.*;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.models.core.URLParams;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.ProjectRequest;
import org.egov.common.models.project.ProjectResponse;
import org.egov.common.producer.Producer;
import org.egov.common.utils.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/v1/bom")
@Validated
public class BOMApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest httpServletRequest;

    private final Producer producer;

    private final ActivityConfiguration fieldPlannerConfiguration;

    private final BomService bomService;

    @Autowired
    public BOMApiController(ObjectMapper objectMapper, HttpServletRequest httpServletRequest,
                            Producer producer,
                            ActivityConfiguration fieldPlannerConfiguration,
                            BomService bomService) {
        this.objectMapper = objectMapper;
        this.httpServletRequest = httpServletRequest;
        this.producer = producer;
        this.fieldPlannerConfiguration = fieldPlannerConfiguration;
        this.bomService = bomService;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<BomResponse> createBOMActivity(@ApiParam(value = "Capture linkage of Project and facility.", required = true) @Valid @RequestBody BomBulkRequest request) {

        List<BillOfMaterial> billOfMaterials = bomService.createBillOfMaterial(request);
        BomResponse response = BomResponse.builder()
                .billOfMaterials(billOfMaterials)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<BomResponse> updateBillOfMaterials(@ApiParam(value = "Details for the updated Project.", required = true) @Valid @RequestBody BomBulkRequest request) {
        BomBulkRequest enrichedBOMRequest = bomService.updateBillOfMaterials(request);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        BomResponse bomResponse = BomResponse.builder().responseInfo(responseInfo).billOfMaterials(enrichedBOMRequest.getBillOfMaterials()).build();
        return new ResponseEntity<BomResponse>(bomResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<BomResponse> searchBillOfMaterials(
            @ApiParam(value = "Details for the fieldPlan.", required = true) @Valid @RequestBody BomSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        List<BillOfMaterial> billOfMaterials = bomService.searchBillOfMaterials(
                request,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getIncludeDeleted(),
                urlParams.getLastChangedSince()
        );
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        Integer count = bomService.countAllFieldPlans(request, urlParams.getTenantId(), urlParams.getLastChangedSince(), urlParams.getIncludeDeleted());
        BomResponse bomResponse = BomResponse.builder().responseInfo(responseInfo).billOfMaterials(billOfMaterials).totalCount(count).build();
        return new ResponseEntity<BomResponse>(bomResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_generate_pdf", method = RequestMethod.POST)
    public ResponseEntity<byte[]> generatePDF(
            @ApiParam(value = "Generate pdf file for BOM", required = true) @Valid @RequestBody GenerateBOMPdfRequest request,
            @NotNull @ApiParam(value = "Unique key for pdf format generation", required = true) @Valid @RequestParam(value = "key", required = true) String key,
            @NotNull @ApiParam(value = "Unique id for a tenant.", required = true) @Valid @RequestParam(value = "tenantId", required = true) String tenantId
    ) {
        byte[] pdfBytes = bomService.generateBOMPdf(request, key, tenantId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition
                .attachment()
                .filename("report.pdf")
                .build());
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
