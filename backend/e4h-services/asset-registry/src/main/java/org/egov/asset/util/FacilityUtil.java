package org.egov.asset.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.egov.asset.config.Configuration;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.MdmsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@Slf4j
public class FacilityUtil {
    private final RestTemplate restTemplate;

    private final Configuration configuration;

    @Autowired
    public FacilityUtil(RestTemplate restTemplate, Configuration configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }

    public List<Object> searchFacility(String tenantId, String facilityId) {
        String url = prepareFacilityRequest(tenantId, facilityId);
        ResponseEntity<List<Object>> response = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Object>>() {});

            return response.getBody();
        } catch (Exception e) {
            log.error("Exception while fetching from facility: ", e);
            throw new RuntimeException("Failed to fetch facility data", e);
        }
    }


    private String prepareFacilityRequest(String tenantId, String facilityId) {
        String url = configuration.getFacilityHost() + configuration.getFacilitySearchPath();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
              .queryParam("tenant_id", tenantId);

        if (facilityId != null && !facilityId.isEmpty()) {
            builder.queryParam("facility_id", facilityId);
        }
        return builder.toUriString();
    }
}
