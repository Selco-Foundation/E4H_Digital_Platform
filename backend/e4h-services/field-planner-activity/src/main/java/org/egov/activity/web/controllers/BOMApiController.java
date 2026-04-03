package org.egov.activity.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.activity.service.BomService;
import org.egov.activity.web.models.*;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.models.core.URLParams;
import org.egov.common.producer.Producer;
import org.egov.common.utils.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/v1/bom")
@Validated
@Slf4j
public class BOMApiController {

    private final BomService bomService;

    @Autowired
    public BOMApiController(BomService bomService) {
        this.bomService = bomService;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<BomResponse> createBOMActivity(@ApiParam(value = "Capture linkage of Project and facility.", required = true) @Valid @RequestBody BomBulkRequest request) {
        log.trace("createBOMActivity endpoint invoked");
        int bomCount = request.getBillOfMaterials() != null ? request.getBillOfMaterials().size() : 0;
        log.info("Received request to create {} bill of materials", bomCount);
        List<BillOfMaterial> billOfMaterials = bomService.createBillOfMaterial(request);
        BomResponse response = BomResponse.builder()
                .billOfMaterials(billOfMaterials)
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(request.getRequestInfo(), true))
                .build();
        log.debug("Returning response with {} bill of materials", billOfMaterials != null ? billOfMaterials.size() : 0);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<BomResponse> updateBillOfMaterials(@ApiParam(value = "Details for the updated Project.", required = true) @Valid @RequestBody BomBulkRequest request) {
        log.trace("updateBillOfMaterials endpoint invoked");
        int bomCount = request.getBillOfMaterials() != null ? request.getBillOfMaterials().size() : 0;
        log.info("Received request to update {} bill of materials", bomCount);
        BomBulkRequest enrichedBOMRequest = bomService.updateBillOfMaterials(request);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        BomResponse bomResponse = BomResponse.builder().responseInfo(responseInfo).billOfMaterials(enrichedBOMRequest.getBillOfMaterials()).build();
        log.debug("Returning response with {} updated bill of materials", enrichedBOMRequest.getBillOfMaterials() != null ? enrichedBOMRequest.getBillOfMaterials().size() : 0);
        return new ResponseEntity<BomResponse>(bomResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<BomResponse> searchBillOfMaterials(
            @ApiParam(value = "Details for the fieldPlan.", required = true) @Valid @RequestBody BomSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        log.trace("searchBillOfMaterials endpoint invoked with limit: {}, offset: {}, tenantId: {}", urlParams.getLimit(), urlParams.getOffset(), urlParams.getTenantId());
        log.info("Received request to search bill of materials");
        List<BillOfMaterial> billOfMaterials = bomService.searchBillOfMaterials(
                request,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getIncludeDeleted(),
                urlParams.getLastChangedSince()
        );
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        Integer count = bomService.countAllBillOfMaterials(request, urlParams.getTenantId(), urlParams.getLastChangedSince(), urlParams.getIncludeDeleted());
        log.debug("Retrieved {} bill of materials from search, total count: {}", billOfMaterials != null ? billOfMaterials.size() : 0, count);
        BomResponse bomResponse = BomResponse.builder().responseInfo(responseInfo).billOfMaterials(billOfMaterials).totalCount(count).build();
        return new ResponseEntity<BomResponse>(bomResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_generate_pdf", method = RequestMethod.POST)
    public ResponseEntity<byte[]> generatePDF(
            @ApiParam(value = "Generate pdf file for BOM", required = true) @Valid @RequestBody GenerateBOMPdfRequest request,
            @NotNull @ApiParam(value = "Unique id for a tenant.", required = true) @Valid @RequestParam(value = "tenantId", required = true) String tenantId
    ) {
        log.trace("generatePDF endpoint invoked for tenantId: {}", tenantId);
        log.info("Received request to generate BOM PDF, system: {}", request.getSystem());
        byte[] pdfBytes = bomService.generateBOMPdf(request, tenantId);
        log.debug("Generated PDF with size: {} bytes", pdfBytes != null ? pdfBytes.length : 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition
                .attachment()
                .filename("report.pdf")
                .build());
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/_save_pdf", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> createAndSavePDF(
            @ApiParam(value = "Generate pdf file for BOM", required = true) @Valid @RequestBody GenerateBOMPdfRequest request,
            @NotNull @ApiParam(value = "Unique id for a tenant.", required = true) @Valid @RequestParam(value = "tenantId", required = true) String tenantId
    ) {
        log.trace("createAndSavePDF endpoint invoked for tenantId: {}", tenantId);
        log.info("Received request to generate and save BOM PDF, system: {}", request.getSystem());
        String filestoreId = bomService.generateAndSaveBOMPdfToFilestore(request, tenantId);
        log.debug("Generated and saved PDF to filestore, filestoreId: {}", filestoreId);

        Map<String, String> response = new HashMap<>();
        response.put("filestoreId", filestoreId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
