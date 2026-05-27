package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.util.MdmsUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarbonEmissionMdmsClient {

    private static final String MODULE = "co2-dashboard";
    private static final String MASTER = "CarbonEmissionFacilityList";

    private final MdmsUtil mdmsUtil;

    public List<String> fetchVisibleFacilityIds(RequestInfo requestInfo, String tenantId) {
        try {
            Map<String, Map<String, JSONArray>> mdmsRes = mdmsUtil.fetchMdmsData(
                    requestInfo, tenantId, MODULE, List.of(MASTER));
            JSONArray rows = mdmsRes.getOrDefault(MODULE, Collections.emptyMap()).get(MASTER);
            if (rows == null || rows.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> ids = new ArrayList<>();
            for (Object row : rows) {
                if (row instanceof Map<?, ?> map) {
                    Object facilityId = map.get("facilityId");
                    Object active = map.get("active");
                    if (facilityId instanceof String s && !s.isBlank()
                            && (active == null || Boolean.TRUE.equals(active) || "true".equalsIgnoreCase(String.valueOf(active)))) {
                        ids.add(s.trim());
                    }
                }
            }
            return ids;
        } catch (Exception e) {
            log.error("MDMS CO2 facility list fetch failed for tenantId={}", tenantId, e);
            return Collections.emptyList();
        }
    }
}
