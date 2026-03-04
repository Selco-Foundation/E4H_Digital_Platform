package org.egov.asset.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.models.coremodels.IdGenerationRequest;
import digit.models.coremodels.IdGenerationResponse;
import digit.models.coremodels.IdRequest;
import digit.models.coremodels.IdResponse;
import lombok.extern.slf4j.Slf4j;
import org.egov.asset.config.Configuration;
import org.egov.asset.repository.ServiceRequestRepository;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.egov.asset.config.ServiceConstants.IDGEN_ERROR;
import static org.egov.asset.config.ServiceConstants.NO_IDS_FOUND_ERROR;

@Component
@Slf4j
public class IdgenUtil {

    private final ObjectMapper mapper;
    private final ServiceRequestRepository restRepo;
    private final Configuration configs;

    @Autowired
    public IdgenUtil(ObjectMapper mapper, ServiceRequestRepository restRepo, Configuration configs) {
        this.mapper = mapper;
        this.restRepo = restRepo;
        this.configs = configs;
    }

    public List<String> getIdList(RequestInfo requestInfo, String tenantId, String idName, String idformat, Integer count) {
        log.trace("IdgenUtil::getIdList called");
        log.info("Generating IDs | tenantId={} idName={} idformat={} count={}", tenantId, idName, idformat, count);
        List<IdRequest> reqList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            reqList.add(IdRequest.builder().idName(idName).format(idformat).tenantId(tenantId).build());
        }

        IdGenerationRequest request = IdGenerationRequest.builder().idRequests(reqList).requestInfo(requestInfo).build();
        StringBuilder uri = new StringBuilder(configs.getIdGenHost()).append(configs.getIdGenPath());
        log.debug("Fetching IDs from idgen service | uri={} requestCount={}", uri.toString(), reqList.size());
        IdGenerationResponse response = restRepo.fetchResult(uri, request, IdGenerationResponse.class);

        List<IdResponse> idResponses = response.getIdResponses();

        if (CollectionUtils.isEmpty(idResponses)) {
            log.error("No IDs returned from idgen service | tenantId={} idName={} count={}", tenantId, idName, count);
            throw new CustomException(IDGEN_ERROR, NO_IDS_FOUND_ERROR);
        }

        log.debug("Successfully generated IDs | tenantId={} idName={} idsCount={}", tenantId, idName, idResponses.size());
        return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
    }
}