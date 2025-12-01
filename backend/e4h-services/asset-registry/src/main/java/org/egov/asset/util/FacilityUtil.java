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
        log.info("FacilityUtil::searchFacility | tenantId={} facilityId={}", tenantId, facilityId);
        if (tenantId == null || tenantId.isEmpty()) {
            throw new CustomException(ErrorConstants.FACILITY_SEARCH_REQUIRED_PARAMS_CODE, ErrorConstants.FACILITY_SEARCH_REQUIRED_PARAMS_MSG);
        }
        String url = prepareFacilityRequest(tenantId, facilityId);
        ResponseEntity<Map<String,Object>> response = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String,Object>>() {
            });

            return Collections.singletonList(response.getBody().get("facilities"));
        } catch (Exception e) {
            log.error("Exception while fetching from facility: ", e);
            throw new CustomException(ErrorConstants.FACILITY_SERVICE_ERROR_CODE, ErrorConstants.FACILITY_SERVICE_ERROR_MSG);
        }
    }


    private String prepareFacilityRequest(String tenantId, String facilityId) {
        log.info("FacilityUtil::prepareFacilityRequest | tenantId={} facilityId={}", tenantId, facilityId);
        String url = configuration.getFacilityHost() + configuration.getFacilitySearchPath();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("tenantId", tenantId);

        if (facilityId != null && !facilityId.isEmpty()) {
            builder.queryParam("facilityId", facilityId);
        }
        return builder.toUriString();
    }

    public List<Object> getActivityFacilityById(RequestInfo request, String activityFacilityId, String tenantId) {
        ActivityFacilitySearchCriteria searchCriteria = ActivityFacilitySearchCriteria.builder().ids(List.of(activityFacilityId)).tenantId(tenantId).build();
        ActivityFacilitySearchRequest fieldPlanRequest = ActivityFacilitySearchRequest.builder().requestInfo(request).criteria(searchCriteria).build();
        String url = configuration.getActivityFacilityHost() + configuration.getActivityFacilitySearchPath()+ "?tenantId="+tenantId+"&offset=0&limit=100";
        Map<String,Object> response = serviceRequestRepository.fetchResult(new StringBuilder(url), fieldPlanRequest, Map.class);

        return Collections.singletonList(response.get("facility"));
    }
}
