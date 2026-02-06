package org.egov.asset.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.asset.config.Configuration;
import org.egov.asset.repository.ServiceRequestRepository;
import org.egov.asset.web.models.ActivityFacilitySearchCriteria;
import org.egov.asset.web.models.ActivityFacilitySearchRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class FacilityUtil {
    private final RestTemplate restTemplate;

    private final Configuration configuration;
    private final ServiceRequestRepository serviceRequestRepository;

    @Autowired
    public FacilityUtil(RestTemplate restTemplate, Configuration configuration, ServiceRequestRepository serviceRequestRepository) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public List<Object> searchFacility(String tenantId, String facilityId) {
        log.trace("FacilityUtil::searchFacility called");
        log.info("Searching facility | tenantId={} facilityId={}", tenantId, facilityId);
        if (tenantId == null || tenantId.isEmpty()) {
            log.error("Invalid tenant ID for facility search | tenantId={}", tenantId);
            throw new CustomException(ErrorConstants.FACILITY_SEARCH_REQUIRED_PARAMS_CODE, ErrorConstants.FACILITY_SEARCH_REQUIRED_PARAMS_MSG);
        }
        String url = prepareFacilityRequest(tenantId, facilityId);
        ResponseEntity<Map<String,Object>> response = null;
        try {
            log.debug("Calling facility service | url={}", url);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String,Object>>() {
            });

            log.debug("Facility service call successful | tenantId={} facilityId={}", tenantId, facilityId);
            return Collections.singletonList(response.getBody().get("facilities"));
        } catch (Exception e) {
            log.error("Error fetching from facility service | tenantId={} facilityId={} url={} error={}", 
                    tenantId, facilityId, url, e.getMessage(), e);
            throw new CustomException(ErrorConstants.FACILITY_SERVICE_ERROR_CODE, ErrorConstants.FACILITY_SERVICE_ERROR_MSG);
        }
    }


    private String prepareFacilityRequest(String tenantId, String facilityId) {
        log.trace("FacilityUtil::prepareFacilityRequest called");
        log.debug("Preparing facility request URL | tenantId={} facilityId={}", tenantId, facilityId);
        String url = configuration.getFacilityHost() + configuration.getFacilitySearchPath();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("tenantId", tenantId);

        if (facilityId != null && !facilityId.isEmpty()) {
            builder.queryParam("facilityId", facilityId);
        }
        String finalUrl = builder.toUriString();
        log.debug("Facility request URL prepared | url={}", finalUrl);
        return finalUrl;
    }

    public List<Object> getActivityFacilityById(RequestInfo request, String activityFacilityId, String tenantId) {
        log.trace("FacilityUtil::getActivityFacilityById called");
        log.info("Getting activity facility by ID | tenantId={} activityFacilityId={}", tenantId, activityFacilityId);
        ActivityFacilitySearchCriteria searchCriteria = ActivityFacilitySearchCriteria.builder().ids(List.of(activityFacilityId)).tenantId(tenantId).build();
        ActivityFacilitySearchRequest fieldPlanRequest = ActivityFacilitySearchRequest.builder().requestInfo(request).criteria(searchCriteria).build();
        String url = configuration.getActivityFacilityHost() + configuration.getActivityFacilitySearchPath()+ "?tenantId="+tenantId+"&offset=0&limit=100";
        log.debug("Calling activity facility service | url={}", url);
        Map<String,Object> response = serviceRequestRepository.fetchResult(new StringBuilder(url), fieldPlanRequest, Map.class);
        log.debug("Activity facility fetched successfully | tenantId={} activityFacilityId={}", tenantId, activityFacilityId);
        return Collections.singletonList(response.get("facility"));
    }
}
