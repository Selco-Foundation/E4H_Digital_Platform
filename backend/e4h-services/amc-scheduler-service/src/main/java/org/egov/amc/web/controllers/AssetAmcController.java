package org.egov.amc.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
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
public class AssetAmcController {

    private final AssetAmcService assetAmcService;

    @Autowired
    public AssetAmcController(AssetAmcService assetAmcService) {
        this.assetAmcService = assetAmcService;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<AssetAmcResponse> createAssetAmc(@ApiParam(value = "Capture details of benificiary type.", required = true) @Valid @RequestBody AssetAmcRequest request) {
        AssetAmcRequest enrichedAssetAmcRequest = assetAmcService.createAssetAmc(request);
        AssetAmcResponse response = AssetAmcResponse.builder()
                .assetAmcs(enrichedAssetAmcRequest.getAssetAmcs())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(enrichedAssetAmcRequest.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<AssetAmcResponse> updateAssetAmc(@ApiParam(value = "Details for the updated Field Plan.", required = true) @Valid @RequestBody AssetAmcRequest request) {
        AssetAmcRequest enrichedAssetAmcRequest = assetAmcService.updateAssetAmc(request);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        AssetAmcResponse fieldPlanResponse = AssetAmcResponse.builder().responseInfo(responseInfo).assetAmcs(enrichedAssetAmcRequest.getAssetAmcs()).build();
        return new ResponseEntity<AssetAmcResponse>(fieldPlanResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<AssetAmcResponse> searchAssetAmc(
            @ApiParam(value = "Details for the fieldPlan.", required = true) @Valid @RequestBody AssetAmcSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
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
        AssetAmcResponse fieldPlanResponse = AssetAmcResponse.builder().responseInfo(responseInfo).assetAmcs(assetAMC).totalCount(count).build();
        return new ResponseEntity<AssetAmcResponse>(fieldPlanResponse, HttpStatus.OK);
    }
}
