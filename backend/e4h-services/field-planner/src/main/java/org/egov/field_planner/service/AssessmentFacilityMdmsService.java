package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentFacilityMdmsService {

    private static final String FACILITY_MODULE = "facility";
    private static final String CATEGORY_MASTER = "FacilityCategory";
    private static final String TYPE_MASTER = "FacilityType";

    private final ServiceRequestClient serviceRequestClient;
    private final FieldPlannerConfiguration configuration;
    private final ObjectMapper objectMapper;

    private final Map<String, List<MdmsEntry>> categoryCache = new ConcurrentHashMap<>();
    private final Map<String, List<MdmsEntry>> typeCache = new ConcurrentHashMap<>();

    public record MdmsEntry(String code, String name) {
    }

    public String resolveCategoryCode(RequestInfo requestInfo, String tenantId, String value) {
        return resolveCode(loadCategories(requestInfo, tenantId), value);
    }

    public String resolveTypeCode(RequestInfo requestInfo, String tenantId, String value) {
        return resolveCode(loadTypes(requestInfo, tenantId), value);
    }

    public String toCategoryDisplayName(RequestInfo requestInfo, String tenantId, String stored) {
        return toDisplayName(loadCategories(requestInfo, tenantId), stored);
    }

    public String toTypeDisplayName(RequestInfo requestInfo, String tenantId, String stored) {
        return toDisplayName(loadTypes(requestInfo, tenantId), stored);
    }

    public List<String> expandCategoryFilterValues(RequestInfo requestInfo, String tenantId, List<String> values) {
        return expandFilterValues(loadCategories(requestInfo, tenantId), values);
    }

    public List<String> expandTypeFilterValues(RequestInfo requestInfo, String tenantId, List<String> values) {
        return expandFilterValues(loadTypes(requestInfo, tenantId), values);
    }

    private List<String> expandFilterValues(List<MdmsEntry> entries, List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> expanded = new LinkedHashSet<>();
        for (String value : values) {
            if (StringUtils.isBlank(value)) {
                continue;
            }
            expanded.add(value);
            expanded.add(value.trim().toUpperCase());
            for (MdmsEntry entry : entries) {
                if (entry.code().equalsIgnoreCase(value) || entry.name().equalsIgnoreCase(value)) {
                    expanded.add(entry.code());
                    expanded.add(entry.name());
                }
            }
        }
        return new ArrayList<>(expanded);
    }

    private String resolveCode(List<MdmsEntry> entries, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        for (MdmsEntry entry : entries) {
            if (entry.code().equalsIgnoreCase(trimmed) || entry.name().equalsIgnoreCase(trimmed)) {
                return entry.code();
            }
        }
        return trimmed.toUpperCase();
    }

    private String toDisplayName(List<MdmsEntry> entries, String stored) {
        if (StringUtils.isBlank(stored)) {
            return stored;
        }
        for (MdmsEntry entry : entries) {
            if (entry.code().equalsIgnoreCase(stored)) {
                return entry.name();
            }
        }
        return stored;
    }

    private List<MdmsEntry> loadCategories(RequestInfo requestInfo, String tenantId) {
        return categoryCache.computeIfAbsent(resolveTenantId(tenantId),
                key -> loadMaster(requestInfo, key, CATEGORY_MASTER));
    }

    private List<MdmsEntry> loadTypes(RequestInfo requestInfo, String tenantId) {
        return typeCache.computeIfAbsent(resolveTenantId(tenantId),
                key -> loadMaster(requestInfo, key, TYPE_MASTER));
    }

    private String resolveTenantId(String tenantId) {
        if (StringUtils.isBlank(tenantId)) {
            return "in";
        }
        return tenantId.contains(".") ? tenantId.substring(0, tenantId.indexOf('.')) : tenantId;
    }

    @SuppressWarnings("unchecked")
    private List<MdmsEntry> loadMaster(RequestInfo requestInfo, String tenantId, String masterName) {
        try {
            MasterDetail masterDetail = MasterDetail.builder()
                    .name(masterName)
                    .filter("[?(@.active==true)]")
                    .build();
            ModuleDetail moduleDetail = ModuleDetail.builder()
                    .moduleName(FACILITY_MODULE)
                    .masterDetails(List.of(masterDetail))
                    .build();
            MdmsCriteria criteria = MdmsCriteria.builder()
                    .tenantId(tenantId)
                    .moduleDetails(List.of(moduleDetail))
                    .build();
            MdmsCriteriaReq request = MdmsCriteriaReq.builder()
                    .requestInfo(requestInfo)
                    .mdmsCriteria(criteria)
                    .build();
            String url = configuration.getMdmsHost() + configuration.getMdmsEndPoint();
            LinkedHashMap<String, Object> response = serviceRequestClient.fetchResult(
                    new StringBuilder(url), request, LinkedHashMap.class);
            Object mdmsRes = response.get("MdmsRes");
            if (!(mdmsRes instanceof Map<?, ?> mdmsMap)) {
                return List.of();
            }
            Object module = mdmsMap.get(FACILITY_MODULE);
            if (!(module instanceof Map<?, ?> moduleMap)) {
                return List.of();
            }
            Object master = moduleMap.get(masterName);
            if (!(master instanceof List<?> records)) {
                return List.of();
            }
            List<MdmsEntry> entries = new ArrayList<>();
            for (Object record : records) {
                Map<String, Object> map = objectMapper.convertValue(record, Map.class);
                String code = map.get("code") != null ? map.get("code").toString() : null;
                String name = map.get("name") != null ? map.get("name").toString() : null;
                if (StringUtils.isNotBlank(code)) {
                    entries.add(new MdmsEntry(code, name));
                }
            }
            return entries;
        } catch (Exception e) {
            log.warn("Failed to load MDMS master {} for tenant {}: {}", masterName, tenantId, e.getMessage());
            return List.of();
        }
    }
}
