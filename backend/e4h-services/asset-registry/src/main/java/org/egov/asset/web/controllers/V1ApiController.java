package org.egov.asset.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.asset.service.AssetService;
import org.egov.asset.web.models.*;
import org.egov.asset.web.validator.AssetValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.673231117+05:30[Asia/Kolkata]")
@Controller
@Slf4j
@RequestMapping("")
public class V1ApiController {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final AssetValidator validator;

    private final AssetService assetService;

    @Autowired
    public V1ApiController(ObjectMapper objectMapper, HttpServletRequest request, AssetValidator validator, AssetService assetService) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.validator = validator;
        this.assetService = assetService;
    }

    @RequestMapping(value = "/v1/asset/bulk/_create", method = RequestMethod.POST)
    public ResponseEntity<BulkAssetCreateResponse> bulkCreateAsset(@Parameter(in = ParameterIn.DEFAULT, description = "Asset data to be added to the registry", required = true, schema = @Schema()) @Valid @RequestBody BulkAssetCreateRequest body) {
        // TODO: Implement the actual bulk asset creation logic
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                // Create a proper response object instead of using a hardcoded JSON string
                BulkAssetCreateResponse response = new BulkAssetCreateResponse();
                // Set appropriate fields in the response
                return new ResponseEntity<BulkAssetCreateResponse>(response, HttpStatus.NOT_IMPLEMENTED);
            } catch (Exception e) {
                // Log the error
                // log.error("Error creating bulk assets", e);
                return new ResponseEntity<BulkAssetCreateResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<BulkAssetCreateResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/v1/asset/amc/visit/_create", method = RequestMethod.POST)
    public ResponseEntity<Void> createAMCVisit(@Parameter(in = ParameterIn.DEFAULT, description = "AMC visit details to be logged", required = true, schema = @Schema()) @Valid @RequestBody AssetAMCVisitRequest body) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }


    @RequestMapping(value = "/v1/asset/_create", method = RequestMethod.POST)
    public ResponseEntity<AssetCreateResponse> createAsset(
            @Parameter(in = ParameterIn.DEFAULT, description = "Asset data to be added to the registry", required = true, schema = @Schema())
            @Valid @RequestBody AssetCreateRequest assetCreateRequest) {
        validator.validateCreateAsset(assetCreateRequest);
        AssetCreateResponse asset = assetService.createAsset(assetCreateRequest);
        return new ResponseEntity<>(asset, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/v1/asset/amc/_create", method = RequestMethod.POST)
    public ResponseEntity<Void> createAssetAMC(@Parameter(in = ParameterIn.DEFAULT, description = "AMC contract details and visit history", required = true, schema = @Schema()) @Valid @RequestBody AssetAMCRequest body) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/v1/asset/amc/visit/_search", method = RequestMethod.GET)
    public ResponseEntity<Object> searchAMCVisits(@Parameter(in = ParameterIn.QUERY, description = "", schema = @Schema()) @Valid @RequestParam(value = "assetID", required = false) Object assetID, @Parameter(in = ParameterIn.QUERY, description = "", schema = @Schema()) @Valid @RequestParam(value = "facilityID", required = false) Object facilityID, @Parameter(in = ParameterIn.QUERY, description = "", schema = @Schema()) @Valid @RequestParam(value = "visitDate", required = false) Object visitDate) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<Object>(objectMapper.readValue("\"\"", Object.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<Object>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/v1/asset/amc/_search", method = RequestMethod.GET)
    public ResponseEntity<Object> searchAssetAMC(@Parameter(in = ParameterIn.QUERY, description = "Filter AMC records for a specific asset", schema = @Schema()) @Valid @RequestParam(value = "assetID", required = false) Object assetID, @Parameter(in = ParameterIn.QUERY, description = "Filter by AMC contract number", schema = @Schema()) @Valid @RequestParam(value = "contractNumber", required = false) Object contractNumber) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<Object>(objectMapper.readValue("\"\"", Object.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<Object>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/v1/asset/_search", method = RequestMethod.POST)
    public ResponseEntity<List<Asset>> searchAssets(
            @Parameter(in = ParameterIn.DEFAULT, description = "Asset data to be searched for", required = true, schema = @Schema())
            @Valid @RequestBody AssetSearchRequest searchRequest,
            @Parameter(in = ParameterIn.QUERY, description = "Offset for pagination", schema = @Schema(type = "integer", format = "int32"))
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @Parameter(in = ParameterIn.QUERY, description = "Limit for pagination", schema = @Schema(type = "integer", format = "int32"))
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        AssetSearchCriteria criteria = searchRequest.getCriteria();
        Asset asset = Asset.builder()
                .tenantId(criteria.getTenantId())
                .assetId(criteria.getAssetID())
                .wfStatus(criteria.getWfStatus())
                .facilityID(criteria.getFacilityID())
                .assetTypeSearch(criteria.getAssetType())
                .activityFacilityID(criteria.getActivityFacilityID())
                .isOperational(criteria.getIsOperational())
                .serialNumberSearch(criteria.getSerialNumber())
                .modelNumber(criteria.getModelNumber())
                .brandID(criteria.getBrandID())
                .build();
        List<Asset> searchResponse = assetService.fetchAssetsWithDocuments(asset,limit, offset);
        Integer count = assetService.getAssetsCount(asset);
        return new ResponseEntity<>(searchResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/v1/asset/amc/visit/{visitID}/_update", method = RequestMethod.POST)
    public ResponseEntity<Void> updateAMCVisit(@Parameter(in = ParameterIn.DEFAULT, description = "Updated AMC visit information", required = true, schema = @Schema()) @Valid @RequestBody AssetAMCVisitRequest body, @Parameter(in = ParameterIn.PATH, description = "Unique identifier of the AMC visit record", required = true, schema = @Schema()) @PathVariable("visitID") Object visitID) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/v1/asset/_update", method = RequestMethod.POST)
    public ResponseEntity<AssetCreateUpdateResponse> updateAsset(@Parameter(in = ParameterIn.DEFAULT, description = "Updated asset information", required = true, schema = @Schema())
        @Valid @RequestBody AssetCreateRequest body,
        @Parameter(in = ParameterIn.QUERY, description = "Unique identifier of the asset", required = true, schema = @Schema())
        @RequestParam("assetID") String assetID) {
        validator.validateAsset(assetID, body);
        Asset updatedAsset = assetService.updateAsset(assetID, body);
        AssetCreateUpdateResponse response = new AssetCreateUpdateResponse();
        response.setAsset(updatedAsset);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/v1/asset/amc/_update", method = RequestMethod.POST)
    public ResponseEntity<Void> updateAssetAMC(@Parameter(in = ParameterIn.DEFAULT, description = "Updated AMC contract or visit information", required = true, schema = @Schema()) @Valid @RequestBody AssetAMCRequest body, @Parameter(in = ParameterIn.PATH, description = "System-generated unique identifier for the AMC", required = true, schema = @Schema()) @PathVariable("amcId") Object amcId) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/v1/asset/workflow/{assetID}/_update", method = RequestMethod.POST)
    public ResponseEntity<Void> updateAssetWorkflow(@Parameter(in = ParameterIn.DEFAULT, description = "Workflow status update for an asset", required = true, schema = @Schema()) @Valid @RequestBody AssetWorkflowRequest body, @Parameter(in = ParameterIn.PATH, description = "System-generated unique identifier for the asset", required = true, schema = @Schema()) @PathVariable("assetID") Object assetID) {
        // TODO: Implement the actual asset workflow update logic
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

}
