package org.egov.im.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.IMRepository;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.util.JurisdictionAuthorizer;
import org.egov.im.web.models.IncidentWrapper;
import org.egov.im.web.models.RequestInfoWrapper;
import org.egov.im.web.models.RequestSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflow's process search, with actions the caller may not take where the ticket is removed.
 *
 * <h3>Why this exists as a proxy</h3>
 * The ticket details page asks egov-workflow-v2 directly which actions it may offer. Workflow
 * filters by the caller's roles, but it only ever sees a {@code businessId} - it has no idea what
 * boundary a ticket is in, so it cannot know that a Tech POC in one state should not be offered
 * Tech POC actions on another state's ticket. Teaching it would mean giving a forked core service a
 * dependency on HRMS and boundary-service for a fact it has no business owning.
 *
 * <p>So the page asks im-services instead, which knows the ticket's boundary, and im-services
 * forwards to workflow and prunes the answer. The request and response shapes are workflow's,
 * unchanged, so the client cannot tell the difference apart from the shorter action list.
 *
 * <h3>This is presentation, not enforcement</h3>
 * Nothing here is load-bearing: a caller who ignores it and posts the action anyway is refused by
 * {@code ServiceRequestValidator}. Its job is that the button should not be there in the first
 * place.
 */
@Slf4j
@Service
public class WorkflowActionFilterService {

    @Autowired
    private IMConfiguration config;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private IMRepository repository;

    @Autowired
    private JurisdictionAuthorizer jurisdictionAuthorizer;

    @Autowired
    private ObjectMapper mapper;

    /**
     * @param queryParams forwarded to workflow verbatim, so the caller controls history, paging and
     *                    anything else workflow supports without this needing to know about it
     */
    @SuppressWarnings("unchecked")
    public Object searchAndFilter(RequestInfo requestInfo, Map<String, String> queryParams) {
        StringBuilder url = new StringBuilder(config.getWfHost())
                .append(config.getWfProcessInstanceSearchPath());
        char separator = '?';
        for (Map.Entry<String, String> param : queryParams.entrySet()) {
            url.append(separator).append(param.getKey()).append('=').append(param.getValue());
            separator = '&';
        }

        Object raw = serviceRequestRepository.fetchResult(
                url, RequestInfoWrapper.builder().requestInfo(requestInfo).build());
        if (raw == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> response = mapper.convertValue(raw, Map.class);
        List<Map<String, Object>> processInstances =
                (List<Map<String, Object>>) response.get("ProcessInstances");
        if (CollectionUtils.isEmpty(processInstances)) {
            return response;
        }

        String tenantId = queryParams.get("tenantId");
        // Resolved once per request, not per process instance: the history of one ticket can be
        // dozens of entries and they all share a boundary.
        Map<String, List<String>> boundaryByBusinessId = new HashMap<>();
        Map<String, Boolean> decisionCache = new HashMap<>();

        for (Map<String, Object> processInstance : processInstances) {
            List<Map<String, Object>> nextActions =
                    (List<Map<String, Object>>) processInstance.get("nextActions");
            if (CollectionUtils.isEmpty(nextActions)) {
                continue;
            }
            String businessId = asText(processInstance.get("businessId"));
            String boundaryCode = boundaryFor(businessId, tenantId, boundaryByBusinessId);
            if (!StringUtils.hasText(boundaryCode)) {
                // No boundary means no way to tell - offer nothing rather than everything. The
                // update path refuses anyway, so this only decides what the page shows.
                log.warn("No boundary for ticket {}; offering no actions", businessId);
                processInstance.put("nextActions", new ArrayList<>());
                continue;
            }
            List<Map<String, Object>> permitted = new ArrayList<>();
            for (Map<String, Object> action : nextActions) {
                if (mayTake(requestInfo, action, boundaryCode, tenantId, decisionCache)) {
                    permitted.add(action);
                }
            }
            processInstance.put("nextActions", permitted);
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    private boolean mayTake(RequestInfo requestInfo, Map<String, Object> action, String boundaryCode,
                            String tenantId, Map<String, Boolean> decisionCache) {
        List<String> roles = (List<String>) action.get("roles");
        if (CollectionUtils.isEmpty(roles)) {
            return true;
        }
        if (roles.stream().allMatch(role -> config.getMachineRoles().stream().anyMatch(role::equalsIgnoreCase))) {
            // Machine-driven; workflow already keeps it away from humans.
            return true;
        }
        String key = boundaryCode + "|" + String.join(",", roles);
        return decisionCache.computeIfAbsent(key,
                k -> jurisdictionAuthorizer.mayActAt(requestInfo, roles, boundaryCode, tenantId));
    }

    private String boundaryFor(String businessId, String tenantId, Map<String, List<String>> cache) {
        if (!StringUtils.hasText(businessId)) {
            return null;
        }
        List<String> cached = cache.get(businessId);
        if (cached != null) {
            return cached.isEmpty() ? null : cached.get(0);
        }
        String boundaryCode = null;
        try {
            RequestSearchCriteria criteria = RequestSearchCriteria.builder()
                    .incidentId(businessId).tenantId(tenantId).build();
            criteria.setIsPlainSearch(false);
            List<IncidentWrapper> wrappers = repository.getIncidentWrappers(criteria);
            if (!CollectionUtils.isEmpty(wrappers) && wrappers.get(0).getIncident() != null) {
                boundaryCode = wrappers.get(0).getIncident().getBoundaryCode();
            }
        } catch (Exception e) {
            log.error("Could not load ticket {} to resolve its boundary", businessId, e);
        }
        cache.put(businessId, boundaryCode == null ? Collections.emptyList()
                : Collections.singletonList(boundaryCode));
        return boundaryCode;
    }

    private static String asText(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
