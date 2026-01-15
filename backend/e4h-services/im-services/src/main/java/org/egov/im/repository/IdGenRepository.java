package org.egov.im.repository;


import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.web.models.Idgen.IdGenerationRequest;
import org.egov.im.web.models.Idgen.IdGenerationResponse;
import org.egov.im.web.models.Idgen.IdRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class IdGenRepository {



    private RestTemplate restTemplate;

    private IMConfiguration config;

    @Autowired
    public IdGenRepository(RestTemplate restTemplate, IMConfiguration config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }


    /**
     * Call iDgen to generateIds
     * @param requestInfo The rquestInfo of the request
     * @param tenantId The tenantiD of the service request
     * @param name Name of the foramt
     * @param format Format of the ids
     * @param count Total Number of idGen ids required
     * @return
     */
    public IdGenerationResponse getId(RequestInfo requestInfo, String tenantId, String name, String format, int count) {

        List<IdRequest> reqList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            reqList.add(IdRequest.builder().idName(name).format(format).tenantId(tenantId).build());
        }
        IdGenerationRequest req = IdGenerationRequest.builder().idRequests(reqList).requestInfo(requestInfo).build();
        IdGenerationResponse response = null;
        try {
            response = restTemplate.postForObject( config.getIdGenHost()+ config.getIdGenPath(), req, IdGenerationResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error while generating ID: ", e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error while generating ID: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            Map<String, String> map = new HashMap<>();
            map.put("ID_GEN_SERVER_ERROR", "Server error while generating ID: " + e.getMessage());
            throw new CustomException(map);
        } catch (ResourceAccessException e) {
            log.error("Network error while generating ID: ", e);
            Map<String, String> map = new HashMap<>();
            map.put("ID_GEN_NETWORK_ERROR", "Network error while generating ID: " + e.getMessage());
            throw new CustomException(map);
        } catch (RestClientException e) {
            log.error("Error while generating ID: ", e);
            Map<String, String> map = new HashMap<>();
            map.put("ID_GEN_ERROR", "Error while generating ID: " + e.getMessage());
            throw new CustomException(map);
        }
        return response;
    }



}
