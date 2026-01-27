package org.egov.inbox.service.V2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;

import org.egov.common.contract.request.Role;
import org.egov.hash.HashService;
import org.egov.inbox.config.InboxConfiguration;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.egov.inbox.repository.builder.V2.InboxQueryBuilder;
import org.egov.inbox.service.V2.validator.ValidatorDefaultImplementation;
import org.egov.inbox.service.WorkflowService;
import org.egov.inbox.util.BoundaryUtil;
import org.egov.inbox.util.MDMSUtil;
import org.egov.inbox.web.model.*;
import org.egov.inbox.web.model.V2.*;
import org.egov.inbox.web.model.workflow.BusinessService;
import org.egov.inbox.web.model.workflow.ProcessInstance;
import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.inbox.util.InboxConstants.*;

@Service
@Slf4j
public class InboxServiceV2 {

    @Autowired
    private InboxConfiguration config;

    @Autowired
    private InboxQueryBuilder queryBuilder;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ValidatorDefaultImplementation validator;

    @Autowired
    private MDMSUtil mdmsUtil;

    @Autowired
    private BoundaryUtil boundaryUtil;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private HashService hashService;


    /**
     *
     * @param inboxRequest
     * @return
     */

    public InboxResponse getInboxResponse(InboxRequest inboxRequest) {
        log.trace("Method invoked: getInboxResponse");
        String tenantId = inboxRequest.getInbox().getTenantId();
        String moduleName = inboxRequest.getInbox().getProcessSearchCriteria().getModuleName();
        String userId = inboxRequest.getRequestInfo().getUserInfo().getUuid();
        
        log.info("Building InboxResponse - tenantId: {}, module: {}, userId: {}", tenantId, moduleName, userId);

        validateAndProcessVendorTenant(inboxRequest);
        InboxQueryConfiguration inboxQueryConfiguration = fetchAndProcessConfiguration(inboxRequest);
        List<Inbox> items = fetchAndEnrichInboxItems(inboxRequest, inboxQueryConfiguration);
        InboxMetrics metrics = calculateInboxMetrics(inboxRequest, inboxQueryConfiguration);
        
        InboxResponse response = buildInboxResponse(items, metrics);
        log.info("InboxResponse built successfully - items: {}, totalCount: {}, nearingSLA: {}",
                response.getItems().size(), response.getTotalCount(), response.getNearingSlaCount());

        return response;
    }

    private void validateAndProcessVendorTenant(InboxRequest inboxRequest) {
        log.trace("Method invoked: validateAndProcessVendorTenant");
        log.debug("Validating search criteria");
        validator.validateSearchCriteria(inboxRequest);
        log.debug("Search criteria validation completed");

        List<Role> roles = inboxRequest.getRequestInfo().getUserInfo().getRoles();
        List<String> tenantIds = roles.stream()
                .filter(role -> role.getCode().equals("COMPLAINT_RESOLVER"))
                .map(Role::getTenantId)
                .collect(Collectors.toList());
        boolean isVendor = !tenantIds.isEmpty();
        log.debug("User role check completed - isVendor: {}, roleCount: {}", isVendor, roles.size());

        Object tenantIdFromRequest = inboxRequest.getInbox().getModuleSearchCriteria().get("tenantId");
        if (isVendor && tenantIdFromRequest instanceof String) {
            Set<String> tenantsFromRequest = new HashSet<>(Arrays.asList(((String) tenantIdFromRequest).split("\\.")));
            if (tenantsFromRequest.size() == 1) {
                inboxRequest.getInbox().getModuleSearchCriteria().put("tenantId", tenantIds);
                log.debug("Overridden tenantId for vendor - tenantIds count: {}", tenantIds.size());
            }
        }
    }

    private InboxQueryConfiguration fetchAndProcessConfiguration(InboxRequest inboxRequest) {
        log.trace("Method invoked: fetchAndProcessConfiguration");
        log.debug("Fetching inbox query configuration from MDMS");
        InboxQueryConfiguration inboxQueryConfiguration = mdmsUtil.getConfigFromMDMS(
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());
        log.debug("InboxQueryConfiguration loaded - index: {}", inboxQueryConfiguration.getIndex());

        hashParamsWhereverRequiredBasedOnConfiguration(
                inboxRequest.getInbox().getModuleSearchCriteria(), inboxQueryConfiguration);
        log.debug("Parameter hashing applied if required");
        return inboxQueryConfiguration;
    }

    private List<Inbox> fetchAndEnrichInboxItems(InboxRequest inboxRequest, InboxQueryConfiguration inboxQueryConfiguration) {
        log.trace("Method invoked: fetchAndEnrichInboxItems");
        log.info("Fetching inbox items from ElasticSearch");
        List<Inbox> items = getInboxItems(inboxRequest, inboxQueryConfiguration.getIndex());
        log.info("Retrieved inbox items - count: {}", items.size());

        log.debug("Enriching inbox items with process instance details");
        enrichProcessInstanceInInboxItems(items);
        log.debug("Inbox items enrichment completed - items count: {}", items.size());
        return items;
    }

    private InboxMetrics calculateInboxMetrics(InboxRequest inboxRequest, InboxQueryConfiguration inboxQueryConfiguration) {
        log.trace("Method invoked: calculateInboxMetrics");
        log.info("Calculating total application count");
        Integer totalCount = getTotalApplicationCount(inboxRequest, inboxQueryConfiguration.getIndex());
        log.info("Total applications count: {}", totalCount);

        log.debug("Fetching status count map");
        List<HashMap<String, Object>> statusCountMap =
                getStatusCountMap(inboxRequest, inboxQueryConfiguration.getIndex());
        log.debug("Status count map retrieved - entries: {}", statusCountMap != null ? statusCountMap.size() : 0);

        log.info("Calculating applications nearing SLA");
        Integer nearingSlaCount = getApplicationsNearingSlaCount(inboxRequest, inboxQueryConfiguration.getIndex());
        log.info("Applications nearing SLA count: {}", nearingSlaCount);

        InboxMetrics metrics = new InboxMetrics();
        metrics.totalCount = totalCount;
        metrics.statusCountMap = statusCountMap;
        metrics.nearingSlaCount = nearingSlaCount;
        return metrics;
    }

    private InboxResponse buildInboxResponse(List<Inbox> items, InboxMetrics metrics) {
        log.trace("Method invoked: buildInboxResponse");
        InboxResponse response = InboxResponse.builder()
                .items(items)
                .totalCount(metrics.totalCount)
                .statusMap(metrics.statusCountMap)
                .nearingSlaCount(metrics.nearingSlaCount)
                .build();
        log.debug("InboxResponse built");
        return response;
    }

    private static class InboxMetrics {
        Integer totalCount;
        List<HashMap<String, Object>> statusCountMap;
        Integer nearingSlaCount;
    }


    private void hashParamsWhereverRequiredBasedOnConfiguration(Map<String, Object> moduleSearchCriteria, InboxQueryConfiguration inboxQueryConfiguration) {
        log.trace("Method invoked: hashParamsWhereverRequiredBasedOnConfiguration");
        
        inboxQueryConfiguration.getAllowedSearchCriteria().forEach(searchParam -> {
            if(!ObjectUtils.isEmpty(searchParam.getIsHashingRequired()) && searchParam.getIsHashingRequired()){
                if(moduleSearchCriteria.containsKey(searchParam.getName())){
                    if(moduleSearchCriteria.get(searchParam.getName()) instanceof List){
                        List<Object> hashedParams = new ArrayList<>();
                        ((List<?>) moduleSearchCriteria.get(searchParam.getName())).forEach(object -> {
                            hashedParams.add(hashService.getHashValue(object));
                        });
                        moduleSearchCriteria.put(searchParam.getName(), hashedParams);
                    }else{
                        Object hashedValue = hashService.getHashValue(moduleSearchCriteria.get(searchParam.getName()));
                        moduleSearchCriteria.put(searchParam.getName(), hashedValue);
                    }
                }
            }
        });
    }

    public ProjectResponse getInboxResponseProject(InboxRequest inboxRequest) {
        log.trace("Method invoked: getInboxResponseProject");
        String tenantId = inboxRequest.getInbox().getTenantId();
        String moduleName = inboxRequest.getInbox().getProcessSearchCriteria().getModuleName();
        
        log.info("Starting project inbox search - tenantId: {}, module: {}", tenantId, moduleName);

        validateProjectSearchCriteria(inboxRequest);
        InboxQueryConfiguration inboxQueryConfiguration = fetchProjectConfiguration(inboxRequest);
        List<Project> items = fetchProjectItems(inboxRequest, inboxQueryConfiguration);
        Integer totalCount = getTotalProjectCount(inboxRequest, inboxQueryConfiguration.getIndex());
        enrichProjectsWithBoundaries(items);

        ProjectResponse response = ProjectResponse.builder().items(items).totalCount(totalCount).build();
        log.info("Project inbox search completed - items: {}, totalCount: {}",
                response.getItems().size(), response.getTotalCount());

        return response;
    }

    private void validateProjectSearchCriteria(InboxRequest inboxRequest) {
        log.trace("Method invoked: validateProjectSearchCriteria");
        log.debug("Validating search criteria for project inbox");
        validator.validateSearchCriteria(inboxRequest);
        log.debug("Search criteria validation completed");
    }

    private InboxQueryConfiguration fetchProjectConfiguration(InboxRequest inboxRequest) {
        log.trace("Method invoked: fetchProjectConfiguration");
        log.debug("Fetching inbox query configuration from MDMS");
        InboxQueryConfiguration inboxQueryConfiguration = mdmsUtil.getConfigFromMDMS(
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());
        log.debug("InboxQueryConfiguration loaded - index: {}", inboxQueryConfiguration.getIndex());

        hashParamsWhereverRequiredBasedOnConfiguration(inboxRequest.getInbox().getModuleSearchCriteria(),
                inboxQueryConfiguration);
        log.debug("Parameter hashing applied if required");
        return inboxQueryConfiguration;
    }

    private List<Project> fetchProjectItems(InboxRequest inboxRequest, InboxQueryConfiguration inboxQueryConfiguration) {
        log.trace("Method invoked: fetchProjectItems");
        log.info("Fetching project inbox items from ElasticSearch");
        List<Project> items = getProjectInboxItems(inboxRequest, inboxQueryConfiguration.getIndex());
        log.info("Retrieved project inbox items - count: {}", items.size());
        return items;
    }

    private void enrichProjectsWithBoundaries(List<Project> items) {
        log.trace("Method invoked: enrichProjectsWithBoundaries");
        log.debug("Fetching boundaries for enrichment");
        Map<String, Boundary> listBlock = boundaryUtil.getBoundaryByCode();
        if (listBlock != null) {
            log.debug("Boundaries loaded - count: {}", listBlock.size());
            items.forEach(item -> enrichProjectItemWithBoundary(item, listBlock));
        } else {
            log.warn("No boundaries returned by boundaryUtil.getBoundaryByCode()");
        }
    }

    private void enrichProjectItemWithBoundary(Project item, Map<String, Boundary> listBlock) {
        log.trace("Method invoked: enrichProjectItemWithBoundary");
        Object additionalDetails = item.getProject().get("additionalDetails");
        Object boundaryCodeObject = item.getProject().get("address");

        if (boundaryCodeObject != null) {
            Address address = mapper.convertValue(boundaryCodeObject, Address.class);

            if (address != null) {
                String boundaryCode = address.getBoundary();
                Object projectId = item.getProject().get("id");
                log.trace("Processing project - projectId: {}, boundaryCode: {}", projectId, boundaryCode);

                if (boundaryCode != null) {
                    Boundary boundary = listBlock.get(boundaryCode);

                    if (boundary != null) {
                        enrichProjectWithBoundaryData(item, additionalDetails, boundary, projectId);
                    } else {
                        log.warn("No boundary found for code: {} in projectId: {}", boundaryCode, projectId);
                    }
                }
            }
        }
    }

    private void enrichProjectWithBoundaryData(Project item, Object additionalDetails, Boundary boundary, Object projectId) {
        log.trace("Method invoked: enrichProjectWithBoundaryData");
        log.debug("Enriching project with boundary data - projectId: {}, state: {}, district: {}",
                projectId, boundary.getState(), boundary.getDistrict());

        Object enrichedAdditionalDetails =
                mergeListIntoAdditionalDetails(additionalDetails, "state", boundary.getState());
        item.getProject().put("additionalDetails", enrichedAdditionalDetails);

        additionalDetails = item.getProject().get("additionalDetails");
        enrichedAdditionalDetails =
                mergeListIntoAdditionalDetails(additionalDetails, "district", boundary.getDistrict());
        item.getProject().put("additionalDetails", enrichedAdditionalDetails);
        log.debug("Project enriched with boundary data");
    }

    private Object mergeListIntoAdditionalDetails(Object additionalDetails, String key, Object value) {
        log.trace("Method invoked: mergeListIntoAdditionalDetails - key: {}", key);
        if (additionalDetails instanceof Map) {
            ((Map<String, Object>) additionalDetails).put(key, value);
            return additionalDetails;
        } else {
            // default to HashMap if null or unknown type
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            return map;
        }
    }

    private void enrichProcessInstanceInInboxItems(List<Inbox> items) {
        log.trace("Method invoked: enrichProcessInstanceInInboxItems - itemCount: {}", items.size());
        /*
          As part of the new inbox, having currentProcessInstance as part of the index is mandated. This has been
          done to avoid having redundant network calls which could hog the performance.
        */
        log.debug("Enriching inbox items with process instance details");
        items.forEach(item -> {
            if(item.getBusinessObject().containsKey(CURRENT_PROCESS_INSTANCE_CONSTANT)) {
                // Set process instance object in the native process instance field declared in the model inbox class.
                ProcessInstance processInstance = mapper.convertValue(item.getBusinessObject().get(CURRENT_PROCESS_INSTANCE_CONSTANT), ProcessInstance.class);
                ProcessInstance updatedProcessInstance = trimRolesFromProcessInstance(processInstance);
                item.setProcessInstance(updatedProcessInstance);

                // Remove current process instance from business object in order to avoid having redundant data in response.
                item.getBusinessObject().remove(CURRENT_PROCESS_INSTANCE_CONSTANT);
            }
        });
    }

    private ProcessInstance trimRolesFromProcessInstance(ProcessInstance processInstance) {
        log.trace("Method invoked: trimRolesFromProcessInstance");
        if(processInstance.getAssigner()!=null)
            processInstance.getAssigner().setRoles(new ArrayList<>());

        if (processInstance.getAssignes() != null) {
            processInstance.getAssignes().stream()
                    .filter(Objects::nonNull)
                    .forEach(assignee -> assignee.setRoles(new ArrayList<>()));
        }
        log.debug("Roles trimmed from process instance");
        return processInstance;
    }

    private List<Inbox> getInboxItems(InboxRequest inboxRequest, String indexName) {
        log.trace("Method invoked: getInboxItems - index: {}", indexName);
        log.info("Fetching inbox items from ElasticSearch - index: {}", indexName);

        log.debug("Retrieving business services");
        List<BusinessService> businessServices = workflowService.getBusinessServices(inboxRequest);
        log.debug("Business services retrieved - count: {}", businessServices.size());

        log.debug("Building ElasticSearch query");
        Map<String, Object> finalQueryBody = queryBuilder.getESQuery(inboxRequest, Boolean.TRUE, Boolean.TRUE);

        try {
            if (log.isDebugEnabled()) {
                String q = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalQueryBody);
                log.debug("ElasticSearch query for inbox: {}", q);
            }
        } catch (Exception e) {
            log.warn("Failed to serialize ElasticSearch query for inbox", e);
        }

        StringBuilder uri = getURI(indexName, SEARCH_PATH);
        log.info("Calling ElasticSearch - URI: {}", uri);

        Object result = serviceRequestRepository.fetchESResult(uri, finalQueryBody);

        if (log.isDebugEnabled()) {
            log.debug("Raw ElasticSearch result received");
        }

        log.debug("Parsing inbox items from ElasticSearch response");
        List<Inbox> inboxItemsList = parseInboxItemsFromSearchResponse(result, businessServices);
        log.info("Parsed inbox items from ElasticSearch - count: {}", inboxItemsList.size());

        return inboxItemsList;
    }


    private List<Project> getProjectInboxItems(InboxRequest inboxRequest, String indexName) {
        log.trace("Method invoked: getProjectInboxItems - index: {}", indexName);
        log.info("Fetching project inbox items from ElasticSearch - index: {}", indexName);

        log.debug("Building ElasticSearch query for project inbox");
        Map<String, Object> finalQueryBody = queryBuilder.getESQueryProject(inboxRequest, Boolean.TRUE);

        try {
            if (log.isDebugEnabled()) {
                String q = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalQueryBody);
                log.debug("ElasticSearch query for project inbox: {}", q);
            }
        } catch (Exception e) {
            log.warn("Failed to serialize ElasticSearch query for project inbox", e);
        }

        StringBuilder uri = getURI(indexName, SEARCH_PATH);
        log.info("Calling ElasticSearch for project inbox - URI: {}", uri);

        Object result = serviceRequestRepository.fetchESResult(uri, finalQueryBody);

        if (log.isDebugEnabled()) {
            log.debug("Raw ElasticSearch result received for project inbox");
        }

        log.debug("Parsing project items from ElasticSearch response");
        List<Project> inboxItemsList = parseProjectItemsFromSearchResponse(result);
        log.info("Parsed project inbox items from ElasticSearch - count: {}", inboxItemsList.size());

        return inboxItemsList;
    }


    private void enrichActionableStatusesFromRole(InboxRequest inboxRequest, List<BusinessService> businessServices) {
        log.trace("Method invoked: enrichActionableStatusesFromRole");
        ProcessInstanceSearchCriteria processCriteria = inboxRequest.getInbox().getProcessSearchCriteria();
        String tenantId = inboxRequest.getInbox().getTenantId();
        processCriteria.setTenantId(tenantId);

        log.debug("Retrieving actionable statuses for role");
        HashMap<String, String> StatusIdNameMap = workflowService.getActionableStatusesForRole(inboxRequest.getRequestInfo(), businessServices,
                inboxRequest.getInbox().getProcessSearchCriteria());
        log.debug("Actionable statuses retrieved - count: {}", StatusIdNameMap.size());
        List<String> actionableStatusUuid = new ArrayList<>();
        if (StatusIdNameMap.values().size() > 0) {
            if (!CollectionUtils.isEmpty(processCriteria.getStatus())) {
                processCriteria.getStatus().forEach(statusUuid -> {
                    if(StatusIdNameMap.values().contains(statusUuid)){
                        actionableStatusUuid.add(statusUuid);
                    }
                });
                inboxRequest.getInbox().getProcessSearchCriteria().setStatus(actionableStatusUuid);
            } else {
            	inboxRequest.getInbox().getProcessSearchCriteria().setStatus(new ArrayList<>(StatusIdNameMap.values()));
            }
        }else{
            inboxRequest.getInbox().getProcessSearchCriteria().setStatus(new ArrayList<>());
        }
    }

    public Integer getTotalApplicationCount(InboxRequest inboxRequest, String indexName) {
        log.trace("Method invoked: getTotalApplicationCount - index: {}", indexName);
        log.debug("Fetching total application count - index: {}", indexName);

        log.debug("Building ElasticSearch count query");
        Map<String, Object> finalQueryBody = queryBuilder.getESQuery(inboxRequest, Boolean.FALSE, Boolean.FALSE);
        try {
            if (log.isDebugEnabled()) {
                log.debug("ElasticSearch count query: {}", mapper.writeValueAsString(finalQueryBody));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize application count query", e);
            throw new RuntimeException(e);
        }

        StringBuilder uri = getURI(indexName, COUNT_PATH);
        log.debug("Calling ElasticSearch count endpoint - URI: {}", uri);
        Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchESResult(uri, finalQueryBody);
        log.debug("ElasticSearch count response received");

        Integer totalCount = 0;
        if (response.containsKey(COUNT_CONSTANT)) {
            totalCount = (Integer) response.get(COUNT_CONSTANT);
            log.info("Total application count retrieved: {}", totalCount);
        } else {
            log.error("COUNT_CONSTANT not found in ElasticSearch response for applications");
            throw new CustomException("INBOX_COUNT_ERR", "Error occurred while executing ES count query");
        }
        return totalCount;
    }

    public Integer getTotalProjectCount(InboxRequest inboxRequest, String indexName) {
        log.trace("Method invoked: getTotalProjectCount - index: {}", indexName);
        log.debug("Fetching total project count - index: {}", indexName);

        log.debug("Building ElasticSearch count query for projects");
        Map<String, Object> finalQueryBody = queryBuilder.getESQueryProject(inboxRequest, Boolean.FALSE);
        try {
            if (log.isDebugEnabled()) {
                log.debug("ElasticSearch project count query: {}", mapper.writeValueAsString(finalQueryBody));
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize project count query", e);
            throw new RuntimeException(e);
        }

        StringBuilder uri = getURI(indexName, COUNT_PATH);
        log.debug("Calling ElasticSearch count endpoint for projects - URI: {}", uri);
        Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchESResult(uri, finalQueryBody);
        log.debug("ElasticSearch project count response received");

        Integer totalCount = 0;
        if (response.containsKey(COUNT_CONSTANT)) {
            totalCount = (Integer) response.get(COUNT_CONSTANT);
            log.info("Total project count retrieved: {}", totalCount);
        } else {
            log.error("COUNT_CONSTANT not found in ElasticSearch response for projects");
            throw new CustomException("INBOX_COUNT_ERR", "Error occurred while executing ES count query");
        }
        return totalCount;
    }

    public List<HashMap<String, Object>> getStatusCountMap(InboxRequest inboxRequest, String indexName) {
        log.trace("Method invoked: getStatusCountMap - index: {}", indexName);
        log.debug("Fetching status count map - index: {}", indexName);

        log.debug("Building ElasticSearch status count query");
        Map<String, Object> finalQueryBody = queryBuilder.getStatusCountQuery(inboxRequest);
        if (log.isDebugEnabled()) {
            log.debug("ElasticSearch status count query built");
        }

        StringBuilder uri = getURI(indexName, SEARCH_PATH);
        log.debug("Calling ElasticSearch for status count - URI: {}", uri);
        Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchESResult(uri, finalQueryBody);
        log.debug("ElasticSearch status count response received");

        log.debug("Parsing status count map from aggregation response");
        HashMap<String, Object> statusCountMap = parseStatusCountMapFromAggregationResponse(response);
        log.debug("Transforming status map");
        List<HashMap<String, Object>> transformedStatusMap = transformStatusMap(inboxRequest, statusCountMap);

        if (transformedStatusMap != null) {
            log.info("Status count map retrieved - entries: {}", transformedStatusMap.size());
        } else {
            log.warn("Status count map was empty or null");
        }

        return transformedStatusMap;
    }

    private Long getApplicationServiceSla(Map<String, Long> businessServiceSlaMap, Map<String, Long> stateUuidSlaMap, Object data) {
        Long currentDate = System.currentTimeMillis(); // current time
        Map<String, Object> auditDetails = (Map<String, Object>) ((Map<String, Object>) data).get(AUDIT_DETAILS_KEY);

        String stateUuid = null;
        if (JsonPath.read(data, "$.currentProcessInstance") != null)
            stateUuid = JsonPath.read(data, STATE_UUID_PATH);

        if (stateUuid != null) {
            if (stateUuidSlaMap.containsKey(stateUuid)) {
                if (!ObjectUtils.isEmpty(auditDetails.get(LAST_MODIFIED_TIME_KEY))) {
                    Long lastModifiedTime = ((Number) auditDetails.get(LAST_MODIFIED_TIME_KEY)).longValue();
                    Long remaining = Math.round((stateUuidSlaMap.get(stateUuid) - (currentDate - lastModifiedTime)) / ((double) (24 * 60 * 60 * 1000)));
                    log.debug("SLA calculated by state - stateUuid: {}, remainingDays: {}", stateUuid, remaining);
                    return remaining;
                }
            } else {
                if (!ObjectUtils.isEmpty(auditDetails.get(CREATED_TIME_KEY))) {
                    Long createdTime = ((Number) auditDetails.get(CREATED_TIME_KEY)).longValue();
                    String businessService = JsonPath.read(data, BUSINESS_SERVICE_PATH);
                    Long businessServiceSLA = businessServiceSlaMap.get(businessService);
                    Long remaining = Math.round((businessServiceSLA - (currentDate - createdTime)) / ((double) (24 * 60 * 60 * 1000)));
                    log.debug("SLA calculated by businessService - businessService: {}, remainingDays: {}", businessService, remaining);
                    return remaining;
                }
            }
        }
        log.warn("SLA could not be calculated - stateUuid: {}", stateUuid);
        return null;
    }

    private List<HashMap<String, Object>> transformStatusMap(InboxRequest request, HashMap<String, Object> statusCountMap) {
        log.trace("Method invoked: transformStatusMap");
        if (CollectionUtils.isEmpty(statusCountMap)) {
            log.warn("transformStatusMap received an empty or null statusCountMap");
            return null;
        }

        log.debug("Retrieving business services for status transformation");
        List<BusinessService> businessServices = workflowService.getBusinessServices(request);
        Map<String, String> statusIdToBusinessServiceMap = workflowService.getStatusIdToBusinessServiceMap(businessServices);
        Map<String, String> statusIdToApplicationStatusMap = workflowService.getApplicationStatusIdToStatusMap(businessServices);
        log.debug("Status mapping maps created - businessServices count: {}", businessServices.size());

        List<HashMap<String, Object>> statusCountMapTransformed = new ArrayList<>();

        for (Map.Entry<String, Object> entry : statusCountMap.entrySet()) {
            String statusId = entry.getKey();
            Integer count = (Integer) entry.getValue();

            HashMap<String, Object> map = new HashMap<>();
            map.put(COUNT_CONSTANT, count);
            map.put(APPLICATION_STATUS_KEY, statusIdToApplicationStatusMap.get(statusId));
            map.put(BUSINESSSERVICE_KEY, statusIdToBusinessServiceMap.get(statusId));
            map.put(STATUSID_KEY, statusId);

            statusCountMapTransformed.add(map);

            log.trace("Transformed status entry - statusId: {}, count: {}", statusId, count);
        }

        log.info("Status count map transformation completed - entries: {}", statusCountMapTransformed.size());
        return statusCountMapTransformed;
    }

    private HashMap<String, Object> parseStatusCountMapFromAggregationResponse(Map<String, Object> response) {
        log.trace("Method invoked: parseStatusCountMapFromAggregationResponse");
        if (CollectionUtils.isEmpty((Map<String, Object>) response.get(AGGREGATIONS_KEY))) {
            log.warn("No aggregations found in ElasticSearch response");
            return null;
        }

        log.debug("Extracting status count buckets from aggregation response");
        List<Map<String, Object>> statusCountBuckets = JsonPath.read(response, STATUS_COUNT_AGGREGATIONS_BUCKETS_PATH);
        HashMap<String, Object> statusCountMap = new HashMap<>();

        statusCountBuckets.forEach(bucket -> {
            statusCountMap.put((String) bucket.get(KEY), bucket.get(DOC_COUNT_KEY));
        });

        log.info("Parsed status count buckets - entries: {}", statusCountMap.size());
        log.debug("Status count map parsed - bucketCount: {}", statusCountBuckets.size());

        return statusCountMap;
    }

    private List<Inbox> parseInboxItemsFromSearchResponse(Object result, List<BusinessService> businessServices) {
        log.trace("Method invoked: parseInboxItemsFromSearchResponse");
        log.info("Parsing inbox items from ElasticSearch search response");

        List<Map<String, Object>> nestedHits = extractNestedHitsFromResponse(result);
        if (CollectionUtils.isEmpty(nestedHits)) {
            log.warn("No hits found in ElasticSearch response");
            return new ArrayList<>();
        }

        log.info("Found hits in ElasticSearch response - count: {}", nestedHits.size());

        Map<String, Long> businessServiceSlaMap = buildBusinessServiceSlaMap(businessServices);
        Map<String, Long> stateUuidVsSlaMap = buildStateUuidSlaMap(businessServices);

        List<Inbox> inboxItemList = buildInboxItemsFromHits(nestedHits, businessServiceSlaMap, stateUuidVsSlaMap);

        log.info("Successfully parsed inbox items - count: {}", inboxItemList.size());
        return inboxItemList;
    }

    private List<Map<String, Object>> extractNestedHitsFromResponse(Object result) {
        log.trace("Method invoked: extractNestedHitsFromResponse");
        Map<String, Object> hits = (Map<String, Object>) ((Map<String, Object>) result).get(HITS);
        List<Map<String, Object>> nestedHits = (List<Map<String, Object>>) hits.get(HITS);
        log.debug("Extracted nested hits from ElasticSearch response");
        return nestedHits;
    }

    private Map<String, Long> buildBusinessServiceSlaMap(List<BusinessService> businessServices) {
        log.trace("Method invoked: buildBusinessServiceSlaMap");
        log.debug("Building SLA maps from business services");
        Map<String, Long> businessServiceSlaMap = new HashMap<>();
        businessServices.forEach(businessService -> {
            businessServiceSlaMap.put(businessService.getBusinessService(), businessService.getBusinessServiceSla());
            log.debug("BusinessService SLA mapped - businessService: {}, sla: {}", 
                    businessService.getBusinessService(), businessService.getBusinessServiceSla());
        });
        log.debug("Business service SLA map built - count: {}", businessServiceSlaMap.size());
        return businessServiceSlaMap;
    }

    private Map<String, Long> buildStateUuidSlaMap(List<BusinessService> businessServices) {
        log.trace("Method invoked: buildStateUuidSlaMap");
        Map<String, Long> stateUuidVsSlaMap = new HashMap<>();
        businessServices.forEach(businessService -> {
            businessService.getStates().forEach(state -> {
                if (!ObjectUtils.isEmpty(state.getSla())) {
                    stateUuidVsSlaMap.put(state.getUuid(), state.getSla());
                    log.debug("State SLA mapped - stateUuid: {}, sla: {}", state.getUuid(), state.getSla());
                }
            });
        });
        log.debug("State UUID SLA map built - count: {}", stateUuidVsSlaMap.size());
        return stateUuidVsSlaMap;
    }

    private List<Inbox> buildInboxItemsFromHits(List<Map<String, Object>> nestedHits, Map<String, Long> businessServiceSlaMap, Map<String, Long> stateUuidVsSlaMap) {
        log.trace("Method invoked: buildInboxItemsFromHits");
        log.debug("Building inbox items from hits");
        List<Inbox> inboxItemList = new ArrayList<>();
        nestedHits.forEach(hit -> {
            Inbox inbox = createInboxItemFromHit(hit, businessServiceSlaMap, stateUuidVsSlaMap);
            inboxItemList.add(inbox);
        });
        log.debug("Inbox items built - count: {}", inboxItemList.size());
        return inboxItemList;
    }

    private Inbox createInboxItemFromHit(Map<String, Object> hit, Map<String, Long> businessServiceSlaMap, Map<String, Long> stateUuidVsSlaMap) {
        log.trace("Method invoked: createInboxItemFromHit");
        Inbox inbox = new Inbox();
        Map<String, Object> businessObject = (Map<String, Object>) hit.get(SOURCE_KEY);
        Map<String, Object> dataBusinessObject = (Map<String, Object>) businessObject.get(DATA_KEY);

        inbox.setBusinessObject(dataBusinessObject);

        Long serviceSla = getApplicationServiceSla(businessServiceSlaMap, stateUuidVsSlaMap, inbox.getBusinessObject());
        enrichInboxItemWithSlaData(inbox, dataBusinessObject, serviceSla);

        log.debug("Parsed inbox item - serviceSla: {}, stateSla: {}, slaRemaining: {}",
                serviceSla,
                dataBusinessObject.get(STATE_SLA),
                dataBusinessObject.get(SLA_REMAINING));

        return inbox;
    }

    private void enrichInboxItemWithSlaData(Inbox inbox, Map<String, Object> dataBusinessObject, Long serviceSla) {
        log.trace("Method invoked: enrichInboxItemWithSlaData");
        inbox.getBusinessObject().put(SERVICESLA_KEY, serviceSla);
        inbox.getBusinessObject().put(SLA_REMAINING, dataBusinessObject.get(SLA_REMAINING));
        inbox.getBusinessObject().put(STATE_SLA, dataBusinessObject.get(STATE_SLA));
        inbox.getBusinessObject().put(TOTAL_SLA_REMAINING, dataBusinessObject.get(TOTAL_SLA_REMAINING));
        log.debug("Inbox item enriched with SLA data");
    }

    @SuppressWarnings("unchecked")
    private List<Project> parseProjectItemsFromSearchResponse(Object result) {
        log.trace("Method invoked: parseProjectItemsFromSearchResponse");
        log.debug("Parsing project items from ElasticSearch response");

        Map<String, Object> hits = (Map<String, Object>) ((Map<String, Object>) result).get(HITS);
        List<Map<String, Object>> nestedHits = (List<Map<String, Object>>) hits.get(HITS);

        if (CollectionUtils.isEmpty(nestedHits)) {
            log.info("No project items found in ElasticSearch response");
            return new ArrayList<>();
        }

        log.debug("Found project hits in ElasticSearch response - count: {}", nestedHits.size());

        List<Project> inboxItemList = new ArrayList<>();
        nestedHits.forEach(hit -> {
            Project project = new Project();
            Map<String, Object> businessObject = (Map<String, Object>) hit.get(SOURCE_KEY);
            Map<String, Object> dataBusinessObject = (Map<String, Object>) businessObject.get(DATA_KEY);

            project.setProject(dataBusinessObject);
            inboxItemList.add(project);

            if (log.isTraceEnabled()) {
                Object projectId = dataBusinessObject.get("id");
                log.trace("Parsed project item - projectId: {}", projectId);
            }
        });

        log.info("Parsed project items successfully - count: {}", inboxItemList.size());
        return inboxItemList;
    }


    public Integer getApplicationsNearingSlaCount(InboxRequest inboxRequest, String indexName) {
        log.trace("Method invoked: getApplicationsNearingSlaCount - index: {}", indexName);
        log.info("Calculating applications nearing SLA - index: {}", indexName);

        List<BusinessService> businessServicesObjs = fetchBusinessServicesForSlaCalculation(inboxRequest);
        Map<String, Long> businessServiceSlaMap = buildBusinessServiceSlaMapForNearingSla(businessServicesObjs);
        Map<String, HashSet<String>> businessServiceVsStateUuids = buildBusinessServiceStateUuidsMap(businessServicesObjs);
        Map<String, List<String>> businessServiceVsUuidsBasedOnSearchCriteria = 
                buildBusinessServiceUuidsBasedOnSearchCriteria(inboxRequest, businessServiceVsStateUuids);
        
        Integer totalCount = calculateTotalNearingSlaCount(inboxRequest, indexName, businessServiceVsUuidsBasedOnSearchCriteria, businessServiceSlaMap);

        log.info("Total nearing SLA applications calculated - count: {}", totalCount);
        return totalCount;
    }

    private List<BusinessService> fetchBusinessServicesForSlaCalculation(InboxRequest inboxRequest) {
        log.trace("Method invoked: fetchBusinessServicesForSlaCalculation");
        log.debug("Retrieving business services for SLA calculation");
        List<BusinessService> businessServicesObjs = workflowService.getBusinessServices(inboxRequest);
        log.debug("Business services retrieved - count: {}", businessServicesObjs.size());
        return businessServicesObjs;
    }

    private Map<String, Long> buildBusinessServiceSlaMapForNearingSla(List<BusinessService> businessServicesObjs) {
        log.trace("Method invoked: buildBusinessServiceSlaMapForNearingSla");
        Map<String, Long> businessServiceSlaMap = new HashMap<>();
        businessServicesObjs.forEach(businessService -> {
            businessServiceSlaMap.put(businessService.getBusinessService(), businessService.getBusinessServiceSla());
            log.debug("BusinessService SLA mapped - businessService: {}, sla: {}",
                    businessService.getBusinessService(),
                    businessService.getBusinessServiceSla());
        });
        log.debug("Business service SLA map built - services: {}", businessServicesObjs.size());
        return businessServiceSlaMap;
    }

    private Map<String, HashSet<String>> buildBusinessServiceStateUuidsMap(List<BusinessService> businessServicesObjs) {
        log.trace("Method invoked: buildBusinessServiceStateUuidsMap");
        Map<String, HashSet<String>> businessServiceVsStateUuids = new HashMap<>();
        businessServicesObjs.forEach(businessService -> {
            List<String> listOfUuids = new ArrayList<>();
            businessService.getStates().forEach(state -> {
                listOfUuids.add(state.getUuid());
            });
            businessServiceVsStateUuids.put(businessService.getBusinessService(), new HashSet<>(listOfUuids));
            log.debug("BusinessService state UUIDs mapped - businessService: {}, stateCount: {}",
                    businessService.getBusinessService(),
                    listOfUuids.size());
        });
        log.debug("Business service state UUIDs map built");
        return businessServiceVsStateUuids;
    }

    private Map<String, List<String>> buildBusinessServiceUuidsBasedOnSearchCriteria(InboxRequest inboxRequest, 
                                                                                       Map<String, HashSet<String>> businessServiceVsStateUuids) {
        log.trace("Method invoked: buildBusinessServiceUuidsBasedOnSearchCriteria");
        List<String> uuidsInSearchCriteria = inboxRequest.getInbox().getProcessSearchCriteria().getStatus();
        Map<String, List<String>> businessServiceVsUuidsBasedOnSearchCriteria = new HashMap<>();

        if (!CollectionUtils.isEmpty(uuidsInSearchCriteria)) {
            log.info("Using status filter - uuidCount: {}", uuidsInSearchCriteria.size());
            filterUuidsBySearchCriteria(uuidsInSearchCriteria, businessServiceVsStateUuids, businessServiceVsUuidsBasedOnSearchCriteria);
        } else {
            log.info("No status filter provided - using all states for each BusinessService");
            businessServiceVsStateUuids.forEach((businessService, setOfUuids) -> {
                businessServiceVsUuidsBasedOnSearchCriteria.put(businessService, new ArrayList<>(setOfUuids));
            });
        }
        log.debug("Business service UUIDs based on search criteria built - serviceCount: {}", 
                businessServiceVsUuidsBasedOnSearchCriteria.size());
        return businessServiceVsUuidsBasedOnSearchCriteria;
    }

    private void filterUuidsBySearchCriteria(List<String> uuidsInSearchCriteria, 
                                             Map<String, HashSet<String>> businessServiceVsStateUuids,
                                             Map<String, List<String>> businessServiceVsUuidsBasedOnSearchCriteria) {
        log.trace("Method invoked: filterUuidsBySearchCriteria");
        uuidsInSearchCriteria.forEach(uuid -> {
            businessServiceVsStateUuids.keySet().forEach(businessService -> {
                HashSet<String> setOfUuids = businessServiceVsStateUuids.get(businessService);
                if (setOfUuids.contains(uuid)) {
                    businessServiceVsUuidsBasedOnSearchCriteria
                            .computeIfAbsent(businessService, k -> new ArrayList<>())
                            .add(uuid);
                }
            });
        });
        log.debug("UUIDs filtered by search criteria");
    }

    private Integer calculateTotalNearingSlaCount(InboxRequest inboxRequest, String indexName,
                                                   Map<String, List<String>> businessServiceVsUuidsBasedOnSearchCriteria,
                                                   Map<String, Long> businessServiceSlaMap) {
        log.trace("Method invoked: calculateTotalNearingSlaCount");
        List<String> businessServices = new ArrayList<>(businessServiceVsUuidsBasedOnSearchCriteria.keySet());
        Integer totalCount = 0;

        for (String businessService : businessServices) {
            Long businessServiceSla = businessServiceSlaMap.get(businessService);
            Integer count = getNearingSlaCountForService(inboxRequest, indexName, businessService, businessServiceSla);
            totalCount += count;
        }
        log.debug("Total nearing SLA count calculated - totalCount: {}", totalCount);
        return totalCount;
    }

    private Integer getNearingSlaCountForService(InboxRequest inboxRequest, String indexName, 
                                                String businessService, Long businessServiceSla) {
        log.trace("Method invoked: getNearingSlaCountForService - businessService: {}", businessService);
        log.debug("Building nearing SLA count query - businessService: {}, sla: {}", businessService, businessServiceSla);
        Map<String, Object> finalQueryBody = queryBuilder.getNearingSlaCountQuery(inboxRequest, businessServiceSla, businessService);
        StringBuilder uri = getURI(indexName, COUNT_PATH);

        if (log.isDebugEnabled()) {
            try {
                log.debug("ElasticSearch nearing SLA query - businessService: {}, sla: {}",
                        businessService,
                        businessServiceSla);
            } catch (Exception e) {
                log.warn("Failed to log ElasticSearch query for service: {}", businessService, e);
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchESResult(uri, finalQueryBody);

        if (!response.containsKey("count")) {
            log.error("ElasticSearch response missing 'count' key for service: {}", businessService);
            throw new CustomException("INBOX_COUNT_ERR", "Error occurred while executing ES count query");
        }

        Integer cnt = (Integer) response.get("count");
        log.info("ElasticSearch count for service - businessService: {}, count: {}", businessService, cnt);
        return cnt;
    }



    private StringBuilder getURI(String indexName, String endpoint){
        log.trace("Method invoked: getURI - index: {}, endpoint: {}", indexName, endpoint);
        StringBuilder uri = new StringBuilder(config.getIndexServiceHost());
        uri.append(indexName);
        uri.append(endpoint);
        log.debug("URI built: {}", uri.toString());
        return uri;
    }

    public SearchResponse getSpecificFieldsFromESIndex(SearchRequest searchRequest) {
        log.trace("Method invoked: getSpecificFieldsFromESIndex");
        String tenantId = searchRequest.getIndexSearchCriteria().getTenantId();
        String moduleName = searchRequest.getIndexSearchCriteria().getModuleName();
        Map<String, Object> moduleSearchCriteria = searchRequest.getIndexSearchCriteria().getModuleSearchCriteria();

        log.info("Getting specific fields from ElasticSearch index - tenantId: {}, module: {}", tenantId, moduleName);
        log.debug("Validating search criteria");
        validator.validateSearchCriteria(tenantId, moduleName, moduleSearchCriteria);
        log.debug("Fetching inbox query configuration from MDMS");
        InboxQueryConfiguration inboxQueryConfiguration = mdmsUtil.getConfigFromMDMS(tenantId, moduleName);
        hashParamsWhereverRequiredBasedOnConfiguration(moduleSearchCriteria, inboxQueryConfiguration);
        log.debug("Fetching data from simple search - index: {}", inboxQueryConfiguration.getIndex());
        List<Data> data = getDataFromSimpleSearch(searchRequest, inboxQueryConfiguration.getIndex());
        SearchResponse searchResponse = SearchResponse.builder().data(data).build();
        log.info("Specific fields retrieved - dataCount: {}", data != null ? data.size() : 0);
        return searchResponse;
    }

    private List<Data> getDataFromSimpleSearch(SearchRequest searchRequest, String index) {
        Map<String, Object> finalQueryBody = queryBuilder.getESQueryForSimpleSearch(searchRequest, Boolean.TRUE);
        try {
            String q = mapper.writeValueAsString(finalQueryBody);
            log.debug("ElasticSearch query for simple search: {}", q);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        StringBuilder uri = getURI(index, SEARCH_PATH);
        Object result = serviceRequestRepository.fetchESResult(uri, finalQueryBody);
        List<Data> dataList = parseSearchResponseForSimpleSearch(result);
        return dataList;
    }

    private List<Data> parseSearchResponseForSimpleSearch(Object result) {
        log.trace("Method invoked: parseSearchResponseForSimpleSearch");
        Map<String, Object> hits = (Map<String, Object>)((Map<String, Object>) result).get(HITS);
        List<Map<String, Object>> nestedHits = (List<Map<String, Object>>) hits.get(HITS);
        if(CollectionUtils.isEmpty(nestedHits)){
            log.debug("No hits found in search response for simple search");
            return new ArrayList<>();
        }

        log.debug("Parsing search response - hitCount: {}", nestedHits.size());
        List<Data> dataList = new ArrayList<>();
        nestedHits.forEach(hit -> {
            Data data = new Data();
            Map<String, Object> sourceObject = (Map<String, Object>) hit.get(SOURCE_KEY);
            Map<String, Object> dataObject = (Map<String, Object>)sourceObject.get(DATA_KEY);
            List<Field> fields = getFieldsFromDataObject(dataObject);
            data.setFields(fields);
            dataList.add(data);
        });

        log.debug("Search response parsed - dataCount: {}", dataList.size());
        return dataList;
    }

    private List<Field> getFieldsFromDataObject(Map<String, Object> dataObject) {
        log.trace("Method invoked: getFieldsFromDataObject");
        List<Field> listOfFields = new ArrayList<>();
        try {
            log.debug("Flattening data object to extract fields");
            Map<String, Object> flattenedDataObject = JsonFlattener.flattenAsMap(mapper.writeValueAsString(dataObject));
            flattenedDataObject.keySet().forEach(key -> {
                Field field = new Field();
                field.setKey(key);
                field.setValue(flattenedDataObject.get(key));
                listOfFields.add(field);
            });
            log.debug("Fields extracted from data object - fieldCount: {}", listOfFields.size());
        }catch (JsonProcessingException ex){
            log.error("Error while processing JSON to extract fields", ex);
            throw new CustomException("EG_INBOX_GET_FIELDS_ERR", "Error while processing JSON.");
        }
        return listOfFields;
    }
}
