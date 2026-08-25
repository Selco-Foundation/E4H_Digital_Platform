package org.egov.amc.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.service.AmcConfigurationService;
import org.egov.amc.service.FacilityAmcIndexSyncService;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AmcConfigurationRequest;
import org.egov.amc.web.models.AmcConfigurationResponse;
import org.egov.amc.web.models.AmcConfigurationSearchRequest;
import org.egov.amc.web.models.FacilityAmcBackfillResponse;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.models.core.URLParams;
import org.egov.common.utils.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@RequestMapping("/v1/configuration")
@Validated
@Slf4j
public class AmcConfigurationController {
    private final AmcConfigurationService amcConfigurationService;
    private final FacilityAmcIndexSyncService facilityAmcIndexSyncService;

    @Autowired
    public AmcConfigurationController(AmcConfigurationService amcConfigurationService,
                                      FacilityAmcIndexSyncService facilityAmcIndexSyncService) {
        this.amcConfigurationService = amcConfigurationService;
        this.facilityAmcIndexSyncService = facilityAmcIndexSyncService;
    }

    /**
     * Script endpoint: rewrites the AMC snapshot on the health facility index for every facility in
     * the tenant. Runs synchronously and can take a while on a large registry - it is a one-off
     * maintenance call, not something the UI issues.
     *
     * <p>Idempotent: each facility's snapshot is recomputed from the database and overwrites whatever
     * the index held, so re-running after a partial or failed run is how it gets repaired.
     */
    @RequestMapping(value = "/facility-index/_backfill", method = RequestMethod.POST)
    public ResponseEntity<FacilityAmcBackfillResponse> backfillFacilityAmcIndex(
            @ApiParam(value = "RequestInfo for the backfill trigger.", required = true)
            @Valid @RequestBody RequestInfoWrapper request,
            @RequestParam(name = "tenantId") String tenantId) {
        log.info("Received request to backfill the AMC facility index for tenantId={}", tenantId);
        FacilityAmcBackfillResponse response =
                facilityAmcIndexSyncService.backfillFacilityAmcIndex(request.getRequestInfo(), tenantId);
        response.setResponseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<AmcConfigurationResponse> createAmcConfiguration(@ApiParam(value = "Capture details of benificiary type.", required = true) @Valid @RequestBody AmcConfigurationRequest request) {
        AmcConfigurationRequest enrichedamcConfigurationRequest = amcConfigurationService.createAmcConfiguration(request);
        AmcConfigurationResponse response = AmcConfigurationResponse.builder()
                .amcConfigurations(enrichedamcConfigurationRequest.getAmcConfigurations())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(enrichedamcConfigurationRequest.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<AmcConfigurationResponse> updateAmcConfiguration(@ApiParam(value = "Details for the updated Field Plan.", required = true) @Valid @RequestBody AmcConfigurationRequest request) {
        AmcConfigurationRequest enrichedAmcConfigurationRequest = amcConfigurationService.updateAmcConfiguration(request);

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        AmcConfigurationResponse amcConfigurationResponse = AmcConfigurationResponse.builder().responseInfo(responseInfo).amcConfigurations(enrichedAmcConfigurationRequest.getAmcConfigurations()).build();
        return new ResponseEntity<AmcConfigurationResponse>(amcConfigurationResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_delete", method = RequestMethod.POST)
    public ResponseEntity<AmcConfigurationResponse> deleteAmcConfiguration(@ApiParam(value = "AMC configurations to delete; only id and tenantId are required.", required = true) @Valid @RequestBody AmcConfigurationRequest request) {
        log.trace("Entering deleteAmcConfiguration controller method");
        log.info("Received request to delete {} AMC configuration(s)", request.getAmcConfigurations().size());
        AmcConfigurationRequest deletedAmcConfigurationRequest = amcConfigurationService.deleteAmcConfiguration(request);
        log.info("Successfully deleted {} AMC configuration(s)", deletedAmcConfigurationRequest.getAmcConfigurations().size());

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        AmcConfigurationResponse amcConfigurationResponse = AmcConfigurationResponse.builder().responseInfo(responseInfo).amcConfigurations(deletedAmcConfigurationRequest.getAmcConfigurations()).build();
        return new ResponseEntity<AmcConfigurationResponse>(amcConfigurationResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<AmcConfigurationResponse> searchAmcConfiguration(
            @ApiParam(value = "Details for the amcConfiguration.", required = true) @Valid @RequestBody AmcConfigurationSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        List<AmcConfiguration> amcConfigurations = amcConfigurationService.searchAmcConfiguration(
                request,
                urlParams.getLimit(),
                urlParams.getOffset(),
                urlParams.getTenantId(),
                urlParams.getIncludeDeleted(),
                urlParams.getLastChangedSince()
        );
        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        Integer count = amcConfigurationService.countAllAmcConfiguration(request, urlParams.getTenantId(), urlParams.getLastChangedSince(), urlParams.getIncludeDeleted());
        AmcConfigurationResponse amcConfigurationResponse = AmcConfigurationResponse.builder().responseInfo(responseInfo).amcConfigurations(amcConfigurations).totalCount(count).build();
        return new ResponseEntity<AmcConfigurationResponse>(amcConfigurationResponse, HttpStatus.OK);
    }
}
