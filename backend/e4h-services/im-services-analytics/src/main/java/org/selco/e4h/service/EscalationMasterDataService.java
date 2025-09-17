package org.selco.e4h.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.util.MdmsUtil;
import org.selco.e4h.web.models.EscalationLevel;
import org.selco.e4h.web.models.EscalationRecipient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service to fetch escalation master data from MDMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationMasterDataService {
    
    private final MdmsUtil mdmsUtil;
    private final ObjectMapper objectMapper;
    
    private static final String INCIDENT_MODULE = "Incident";
    private static final String TENANT_MODULE = "tenant";
    private static final String ESCALATION_LEVEL_MASTER = "EscalationLevel";
    private static final String ESCALATION_RECIPIENT_MASTER = "EscalationRecipient";
    private static final String TENANT_MASTER = "tenants";
    
    /**
     * Fetch all escalation levels from MDMS
     */
    public List<EscalationLevel> fetchEscalationLevels(RequestInfo requestInfo) {
        try {
            log.info("Fetching escalation levels from MDMS");
            Map<String, Map<String, JSONArray>> mdmsData = mdmsUtil.fetchMdmsData(
                requestInfo, 
                "in", 
                INCIDENT_MODULE, 
                List.of(ESCALATION_LEVEL_MASTER)
            );
            
            JSONArray escalationLevels = mdmsData.get(INCIDENT_MODULE).get(ESCALATION_LEVEL_MASTER);
            if (escalationLevels != null && !escalationLevels.isEmpty()) {
                try {
                    return objectMapper.convertValue(escalationLevels, new TypeReference<List<EscalationLevel>>() {});
                } catch (Exception conversionError) {
                    log.error("Error converting escalation levels JSONArray to List<EscalationLevel>", conversionError);
                    log.debug("Escalation levels JSONArray content: {}", escalationLevels);
                    return new ArrayList<>();
                }
            }
            
            log.warn("No escalation levels found in MDMS");
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error fetching escalation levels from MDMS", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Fetch all escalation recipients from MDMS
     */
    public List<EscalationRecipient> fetchEscalationRecipients(RequestInfo requestInfo) {
        try {
            log.info("Fetching escalation recipients from MDMS");
            Map<String, Map<String, JSONArray>> mdmsData = mdmsUtil.fetchMdmsData(
                requestInfo, 
                "in", 
                INCIDENT_MODULE, 
                List.of(ESCALATION_RECIPIENT_MASTER)
            );
            
            JSONArray escalationRecipients = mdmsData.get(INCIDENT_MODULE).get(ESCALATION_RECIPIENT_MASTER);
            if (escalationRecipients != null && !escalationRecipients.isEmpty()) {
                try {
                    return objectMapper.convertValue(escalationRecipients, new TypeReference<List<EscalationRecipient>>() {});
                } catch (Exception conversionError) {
                    log.error("Error converting escalation recipients JSONArray to List<EscalationRecipient>", conversionError);
                    log.debug("Escalation recipients JSONArray content: {}", escalationRecipients);
                    return new ArrayList<>();
                }
            }
            
            log.warn("No escalation recipients found in MDMS");
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error fetching escalation recipients from MDMS", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Fetch all active tenant IDs from MDMS
     */
    public List<String> fetchActiveTenantIds(RequestInfo requestInfo) {
        try {
            log.info("Fetching active tenant IDs from MDMS");
            Map<String, Map<String, JSONArray>> mdmsData = mdmsUtil.fetchMdmsData(
                requestInfo, 
                "in", 
                TENANT_MODULE, 
                List.of(TENANT_MASTER)
            );
            
            JSONArray tenants = mdmsData.get(TENANT_MODULE).get(TENANT_MASTER);
            if (tenants != null && !tenants.isEmpty()) {
                List<String> activeTenantIds = new ArrayList<>();
                
                for (Object tenantObj : tenants) {
                    try {
                        Map<String, Object> tenant = (Map<String, Object>) tenantObj;
                        
                        // Extract tenant code (ID) from the tenant object
                        String tenantId = (String) tenant.get("code");
                        if (tenantId != null && !tenantId.trim().isEmpty()) {
                            // Only include state-level tenants (exclude 'in' which is country-level)
                            if (!"in".equals(tenantId)) {
                                activeTenantIds.add(tenantId);
                                log.debug("Added tenant: {}", tenantId);
                            } else {
                                log.debug("Skipping country-level tenant: {}", tenantId);
                            }
                        } else {
                            log.warn("Tenant object missing 'code' field: {}", tenantObj);
                        }
                    } catch (Exception e) {
                        log.warn("Error processing tenant object: {}", tenantObj, e);
                    }
                }
                
                log.info("Found {} active state-level tenants: {}", activeTenantIds.size(), activeTenantIds);
                return activeTenantIds;
            }
            
            log.warn("No tenants found in MDMS");
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error fetching tenants from MDMS", e);
            return new ArrayList<>();
        }
    }
}
