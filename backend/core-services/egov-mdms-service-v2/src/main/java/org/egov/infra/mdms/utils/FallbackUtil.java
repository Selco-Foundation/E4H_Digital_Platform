package org.egov.infra.mdms.utils;

import net.minidev.json.JSONArray;
import org.egov.infra.mdms.model.Mdms;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.infra.mdms.utils.MDMSConstants.DOT_SEPARATOR;

@Slf4j
public class FallbackUtil {

    private FallbackUtil() {}

    public static List<String> getSubTenantListForFallBack(String tenantId) {
        log.trace("FallbackUtil.getSubTenantListForFallBack: method invoked with tenantId: {}", tenantId);
        List<String> subTenantList = new ArrayList<>();

        subTenantList.add(tenantId);
        String currentTenant = tenantId;

        while(currentTenant.contains(DOT_SEPARATOR)){
            currentTenant = currentTenant.substring(0, currentTenant.lastIndexOf(DOT_SEPARATOR));
            subTenantList.add(currentTenant);
        }

        log.debug("Generated fallback tenant list with count: {}", subTenantList.size());
        return subTenantList;
    }

    public static Map<String, JSONArray> backTrackTenantMasterDataMap(Map<String, Map<String, JSONArray>> tenantMasterMap, String tenantId) {
        log.trace("FallbackUtil.backTrackTenantMasterDataMap: method invoked with tenantId: {}", tenantId);
        List<String> subTenantListForFallback = FallbackUtil.getSubTenantListForFallBack(tenantId);
        Map<String, JSONArray> masterDataPostFallBack = new HashMap<>();
        
        for (String subTenant : subTenantListForFallback) {
            if(tenantMasterMap.containsKey(subTenant)) {
                log.debug("Found data for tenant: {}", subTenant);
                for (Map.Entry<String, JSONArray> entry : tenantMasterMap.get(subTenant).entrySet()) {
                    String schemaCode = entry.getKey();
                    if(!masterDataPostFallBack.containsKey(schemaCode)) {
                        masterDataPostFallBack.put(schemaCode, entry.getValue());
                    }
                }
            }
        }

        log.debug("Fallback completed, master data count: {}", masterDataPostFallBack.size());
        return masterDataPostFallBack;
    }

    public static List<Mdms> backTrackTenantMasterDataList(List<Mdms> masterDataList, String tenantId) {
        log.trace("FallbackUtil.backTrackTenantMasterDataList: method invoked with tenantId: {}", tenantId);
        List<Mdms> masterDataListAfterFallback = new ArrayList<>();
        List<String> subTenantListForFallback = FallbackUtil.getSubTenantListForFallBack(tenantId);
        log.debug("Processing fallback for {} master data records", masterDataList != null ? masterDataList.size() : 0);

        Map<String, List<Mdms>> schemaMasterMap = masterDataList.parallelStream().collect(Collectors.groupingBy(Mdms::getSchemaCode));

        Map<String, Map<String, List<Mdms>>> schemaGroupedTenantMasterMap = new HashMap<>();
        schemaMasterMap.keySet().forEach(schemaCode -> {
            Map<String, List<Mdms>> tenantMasterMap = schemaMasterMap.get(schemaCode).stream().collect(Collectors.groupingBy(Mdms::getTenantId));
            schemaGroupedTenantMasterMap.put(schemaCode, tenantMasterMap);
        });

        for (String schemaCode : schemaGroupedTenantMasterMap.keySet()) {
            Map<String, List<Mdms>> tenantMasterMap = schemaGroupedTenantMasterMap.get(schemaCode);

            for (String subTenant : subTenantListForFallback) {
                if (tenantMasterMap.containsKey(subTenant)) {
                    log.debug("Adding data for schemaCode: {}, tenant: {}", schemaCode, subTenant);
                    masterDataListAfterFallback.addAll(tenantMasterMap.get(subTenant));
                }
            }
        }

        log.debug("Fallback completed, master data count after fallback: {}", masterDataListAfterFallback.size());
        return masterDataListAfterFallback;
    }
}
