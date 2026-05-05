package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.rms.config.RMSConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MDMSDistrictConfigService {

    private static final String MODULE_NAME = "rms-service";
    private static final String MASTER_NAME = "DistrictTicketCreationAllowlist";
    private static final String ACTIVE_FILTER = "$.[?(@.active==true)]";

    private final RMSConfiguration config;
    private final RestTemplate restTemplate;

    public Set<String> getAllowedDistrictBoundaries(RequestInfo requestInfo, String tenantId) {
        String resolvedTenantId = StringUtils.hasText(tenantId) ? tenantId.trim() : config.getDefaultTenantId();
        try {
            MdmsCriteriaReq mdmsRequest = buildMdmsRequest(requestInfo, resolvedTenantId);
            Map<String, Object> response = fetchMdmsResponse(mdmsRequest);
            Set<String> boundaries = extractDistrictBoundaryCodes(response);
            log.info("MDMS district allowlist fetched: tenantId={}, districtCount={}", resolvedTenantId, boundaries.size());
            return boundaries;
        } catch (Exception e) {
            log.error("Failed to fetch district allowlist from MDMS for tenantId={}", resolvedTenantId, e);
            return Collections.emptySet();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchMdmsResponse(MdmsCriteriaReq mdmsRequest) {
        String url = config.getMdmsHost() + config.getMdmsSearchEndpoint();
        return restTemplate.postForObject(url, mdmsRequest, Map.class);
    }

    private MdmsCriteriaReq buildMdmsRequest(RequestInfo requestInfo, String tenantId) {
        MasterDetail masterDetail = MasterDetail.builder()
                .name(MASTER_NAME)
                .filter(ACTIVE_FILTER)
                .build();

        ModuleDetail moduleDetail = ModuleDetail.builder()
                .moduleName(MODULE_NAME)
                .masterDetails(List.of(masterDetail))
                .build();

        MdmsCriteria criteria = MdmsCriteria.builder()
                .tenantId(tenantId)
                .moduleDetails(List.of(moduleDetail))
                .build();

        return MdmsCriteriaReq.builder()
                .requestInfo(requestInfo)
                .mdmsCriteria(criteria)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractDistrictBoundaryCodes(Map<String, Object> response) {
        if (CollectionUtils.isEmpty(response)) {
            return Collections.emptySet();
        }
        Object mdmsResObject = response.get("MdmsRes");
        if (!(mdmsResObject instanceof Map)) {
            return Collections.emptySet();
        }
        Map<String, Object> mdmsRes = (Map<String, Object>) mdmsResObject;
        Object moduleObject = mdmsRes.get(MODULE_NAME);
        if (!(moduleObject instanceof Map)) {
            return Collections.emptySet();
        }
        Map<String, Object> moduleMap = (Map<String, Object>) moduleObject;
        Object masterObject = moduleMap.get(MASTER_NAME);
        if (!(masterObject instanceof List)) {
            return Collections.emptySet();
        }
        List<Map<String, Object>> rows = (List<Map<String, Object>>) masterObject;
        Set<String> districtBoundaries = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            if (row == null) {
                continue;
            }
            Object districtBoundaryCode = row.get("districtBoundaryCode");
            if (districtBoundaryCode instanceof String && StringUtils.hasText((String) districtBoundaryCode)) {
                districtBoundaries.add(((String) districtBoundaryCode).trim());
            }
        }
        return districtBoundaries;
    }
}
