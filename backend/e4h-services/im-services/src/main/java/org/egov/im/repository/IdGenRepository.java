package org.egov.im.repository;


import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.web.models.Idgen.IdGenerationRequest;
import org.egov.im.web.models.Idgen.IdGenerationResponse;
import org.egov.im.web.models.Idgen.IdRequest;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
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
        log.trace("IdGenRepository::getId method invoked");
        log.debug("Generating {} IDs for tenantId: {}, name: {}, format: {}", count, tenantId, name, format);
        List<IdRequest> reqList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            reqList.add(IdRequest.builder().idName(name).format(format).tenantId(tenantId).build());
        }
        IdGenerationRequest req = IdGenerationRequest.builder().idRequests(reqList).requestInfo(requestInfo).build();
        IdGenerationResponse response = null;
        try {
            String url = config.getIdGenHost() + config.getIdGenPath();
            log.trace("Calling idgen service at URL: {}", url);
            response = restTemplate.postForObject(url, req, IdGenerationResponse.class);
            log.debug("Successfully generated {} IDs from idgen service", count);
        } catch (HttpClientErrorException e) {
            log.error("Idgen service returned error for tenantId: {}, name: {}, status: {}", tenantId, name, e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Exception while calling idgen service for tenantId: {}, name: {}", tenantId, name, e);
            Map<String, String> map = new HashMap<>();
            map.put(e.getCause().getClass().getName(),e.getMessage());
            throw new CustomException(map);
        }
        return response;
    }



}
