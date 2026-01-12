package org.egov.amc.web.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.service.AmcConfigurationService;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AmcConfigurationRequest;
import org.egov.amc.web.models.AmcConfigurationResponse;
import org.egov.amc.web.models.AmcConfigurationSearchRequest;
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

import java.util.List;


@Controller
@RequestMapping("/v1/configuration")
@Validated
@Slf4j
public class AmcConfigurationController {
    private final AmcConfigurationService amcConfigurationService;

    @Autowired
    public AmcConfigurationController(AmcConfigurationService amcConfigurationService) {
        this.amcConfigurationService = amcConfigurationService;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<AmcConfigurationResponse> createAmcConfiguration(@ApiParam(value = "Capture details of benificiary type.", required = true) @Valid @RequestBody AmcConfigurationRequest request) {
        log.trace("Entering createAmcConfiguration controller method");
        log.info("Received request to create {} AMC configuration(s)", request.getAmcConfigurations().size());
        AmcConfigurationRequest enrichedamcConfigurationRequest = amcConfigurationService.createAmcConfiguration(request);
        log.info("Successfully created {} AMC configuration(s)", enrichedamcConfigurationRequest.getAmcConfigurations().size());
        AmcConfigurationResponse response = AmcConfigurationResponse.builder()
                .amcConfigurations(enrichedamcConfigurationRequest.getAmcConfigurations())
                .responseInfo(ResponseInfoFactory
                        .createResponseInfo(enrichedamcConfigurationRequest.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<AmcConfigurationResponse> updateAmcConfiguration(@ApiParam(value = "Details for the updated Field Plan.", required = true) @Valid @RequestBody AmcConfigurationRequest request) {
        log.trace("Entering updateAmcConfiguration controller method");
        log.info("Received request to update {} AMC configuration(s)", request.getAmcConfigurations().size());
        AmcConfigurationRequest enrichedAmcConfigurationRequest = amcConfigurationService.updateAmcConfiguration(request);
        log.info("Successfully updated {} AMC configuration(s)", enrichedAmcConfigurationRequest.getAmcConfigurations().size());

        ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true);
        AmcConfigurationResponse amcConfigurationResponse = AmcConfigurationResponse.builder().responseInfo(responseInfo).amcConfigurations(enrichedAmcConfigurationRequest.getAmcConfigurations()).build();
        return new ResponseEntity<AmcConfigurationResponse>(amcConfigurationResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<AmcConfigurationResponse> searchAmcConfiguration(
            @ApiParam(value = "Details for the amcConfiguration.", required = true) @Valid @RequestBody AmcConfigurationSearchRequest request,
            @Valid @ModelAttribute URLParams urlParams
    ) {
        log.trace("Entering searchAmcConfiguration controller method");
        log.info("Received search request for AMC configurations, tenantId: {}", urlParams.getTenantId());
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
        log.info("AMC configuration search completed, found {} configuration(s), total count: {}", amcConfigurations.size(), count);
        return new ResponseEntity<AmcConfigurationResponse>(amcConfigurationResponse, HttpStatus.OK);
    }
}
