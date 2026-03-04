package org.egov.amc.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.web.models.AssetAmc;
import org.egov.amc.web.models.AssetAmcRequest;
import org.egov.amc.web.models.AssetAmcResponse;
import org.egov.amc.web.models.AssetAmcSearchRequest;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.models.core.URLParams;
import org.egov.common.utils.ResponseInfoFactory;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.service.AssetAmcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/v1/asset")
@Validated
@Slf4j
public class AssetAmcController {

    private final AssetAmcService assetAmcService;

    @Autowired
    public AssetAmcController(AssetAmcService assetAmcService) {
        this.assetAmcService = assetAmcService;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<AssetAmcResponse> createAssetAmc(@ApiParam(value = "Capture details of benificiary type.", required = true) @Valid @RequestBody AssetAmcRequest request) {
        log.trace("Entering createAssetAmc controller method");
        log.info("Received request to create {} asset AMC record(s)", request.getAssetAmcs().size());
        AssetAmcRequest enrichedAssetAmcRequest = assetAmcService.createAssetAmc(request);
        log.info("Successfully created {} asset AMC record(s)", enrichedAssetAmcRequest.getAssetAmcs().size());
        AssetAmcResponse response = AssetAmcResponse.builder()
                .assetAmcs(enrichedAssetAmcRequest.getAssetAmcs())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(enrichedAssetAmcRequest.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<AssetAmcResponse> updateAssetAmc(@ApiParam(value = "Details for the updated Field Plan.", required = true) @Valid @RequestBody AssetAmcRequest request) {
        log.trace("Entering updateAssetAmc controller method");
        log.info("Received request to update {} asset AMC record(s)", request.getAssetAmcs().size());
        AssetAmcRequest enrichedAssetAmcRequest = assetAmcService.updateAssetAmc(request);
        log.info("Successfully updated {} asset AMC record(s)", enrichedAssetAmcRequest.getAssetAmcs().size());

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        AssetAmcResponse assetAmcResponse = AssetAmcResponse.builder().responseInfo(responseInfo).assetAmcs(enrichedAssetAmcRequest.getAssetAmcs()).build();
        return new ResponseEntity<AssetAmcResponse>(assetAmcResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<AssetAmcResponse> searchAssetAmc(
            @ApiParam(value = "Details for the assetAmc.", required = true) @Valid @RequestBody AssetAmcSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        log.trace("Entering searchAssetAmc controller method");
        log.info("Received search request for asset AMC, tenantId: {}", urlParams.getTenantId());
        List<AssetAmc> assetAMC = assetAmcService.searchAssetAmc(
                request,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getIncludeDeleted(),
                urlParams.getLastChangedSince()
        );
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        Integer count = assetAmcService.countAllAssetAmcs(request, urlParams.getTenantId(), urlParams.getLastChangedSince(), urlParams.getIncludeDeleted());
        AssetAmcResponse assetAmcResponse = AssetAmcResponse.builder().responseInfo(responseInfo).assetAmcs(assetAMC).totalCount(count).build();
        log.info("Asset AMC search completed, found {} record(s), total count: {}", assetAMC.size(), count);
        return new ResponseEntity<AssetAmcResponse>(assetAmcResponse, HttpStatus.OK);
    }
}
