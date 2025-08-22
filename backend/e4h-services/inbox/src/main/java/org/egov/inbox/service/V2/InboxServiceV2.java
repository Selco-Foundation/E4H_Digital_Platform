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
        log.info("➡️ Start building InboxResponse | tenantId='{}' | module='{}' | user='{}'",
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName(),
                inboxRequest.getRequestInfo().getUserInfo().getUuid());

        // Validation
        validator.validateSearchCriteria(inboxRequest);
        log.debug("✅ Search criteria validated");

        // Vérification des rôles
        List<Role> roles = inboxRequest.getRequestInfo().getUserInfo().getRoles();
        List<String> tenantIds = roles.stream()
                .filter(role -> role.getCode().equals("COMPLAINT_RESOLVER"))
                .map(Role::getTenantId)
                .collect(Collectors.toList());
        boolean isVendor = !tenantIds.isEmpty();
        log.debug("👤 User roles found: {} | isVendor={}", roles, isVendor);

        // Gestion du tenantId pour les vendors
        Object tenantIdFromRequest = inboxRequest.getInbox().getModuleSearchCriteria().get("tenantId");
        if (isVendor && tenantIdFromRequest instanceof String) {
            Set<String> tenantsFromRequest = new HashSet<>(Arrays.asList(((String) tenantIdFromRequest).split("\\.")));
            if (tenantsFromRequest.size() == 1) {
                inboxRequest.getInbox().getModuleSearchCriteria().put("tenantId", tenantIds);
                log.debug("🔄 Overridden tenantId from request with vendor tenantIds={}", tenantIds);
            }
        }

        // Récupération de la configuration
        InboxQueryConfiguration inboxQueryConfiguration = mdmsUtil.getConfigFromMDMS(
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());
        log.debug("⚙️ Loaded InboxQueryConfiguration: {}", inboxQueryConfiguration);

        // Hash params si nécessaire
        hashParamsWhereverRequiredBasedOnConfiguration(
                inboxRequest.getInbox().getModuleSearchCriteria(), inboxQueryConfiguration);
        log.debug("🔐 Applied hashing to sensitive params if required");

        // Récupération des items
        List<Inbox> items = getInboxItems(inboxRequest, inboxQueryConfiguration.getIndex());
        log.info("📥 Retrieved inbox items: count={}", items.size());

        // Enrichissement des items
        enrichProcessInstanceInInboxItems(items);
        log.debug("✨ Enriched inbox items with process instance details");

        // Compteurs
        Integer totalCount = getTotalApplicationCount(inboxRequest, inboxQueryConfiguration.getIndex());
        log.info("📊 Total applications count={}", totalCount);

        List<HashMap<String, Object>> statusCountMap =
                getStatusCountMap(inboxRequest, inboxQueryConfiguration.getIndex());
        log.debug("📌 Status count map={}", statusCountMap);

        Integer nearingSlaCount = getApplicationsNearingSlaCount(inboxRequest, inboxQueryConfiguration.getIndex());
        log.info("⏳ Applications nearing SLA={}", nearingSlaCount);

        InboxResponse response = InboxResponse.builder()
                .items(items)
                .totalCount(totalCount)
                .statusMap(statusCountMap)
                .nearingSlaCount(nearingSlaCount)
                .build();

        log.info("✅ Successfully built InboxResponse | items={} | totalCount={} | nearingSLA={}",
                response.getItems().size(), response.getTotalCount(), response.getNearingSlaCount());

        return response;
    }


    private void hashParamsWhereverRequiredBasedOnConfiguration(Map<String, Object> moduleSearchCriteria, InboxQueryConfiguration inboxQueryConfiguration) {

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
        log.info("➡️ Starting project inbox search for tenantId={} and module={}",
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());

        // Validation des critères
        validator.validateSearchCriteria(inboxRequest);
        log.debug("✅ Validation passed for inboxRequest: {}", inboxRequest);

        // Récupération configuration depuis MDMS
        InboxQueryConfiguration inboxQueryConfiguration = mdmsUtil.getConfigFromMDMS(
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());
        log.debug("📄 Loaded inboxQueryConfiguration: {}", inboxQueryConfiguration);

        // Hashing si besoin
        hashParamsWhereverRequiredBasedOnConfiguration(inboxRequest.getInbox().getModuleSearchCriteria(),
                inboxQueryConfiguration);

        // Récupération des projets
        List<Project> items = getProjectInboxItems(inboxRequest, inboxQueryConfiguration.getIndex());
        Integer totalCount = getTotalProjectCount(inboxRequest, inboxQueryConfiguration.getIndex());
        log.info("📊 Retrieved {} project(s), totalCount={}", items.size(), totalCount);

        // Récupération des boundaries
        Map<String, Boundary> listBlock = boundaryUtil.getBoundaryByCode();
        if (listBlock != null) {
            log.debug("🌍 Loaded {} boundaries for enrichment", listBlock.size());

            for (Project item : items) {
                Object additionalDetails = item.getProject().get("additionalDetails");
                Object boundaryCodeObject = item.getProject().get("address");

                if (boundaryCodeObject != null) {
                    Address address = mapper.convertValue(boundaryCodeObject, Address.class);

                    if (address != null) {
                        String boundaryCode = address.getBoundary();
                        log.trace("🔎 Processing projectId={} with boundaryCode={}",
                                item.getProject().get("id"), boundaryCode);

                        if (boundaryCode != null) {
                            Boundary boundary = listBlock.get(boundaryCode);

                            if (boundary != null) {
                                log.debug("✨ Enriching projectId={} with state={} and district={}",
                                        item.getProject().get("id"), boundary.getState(), boundary.getDistrict());

                                Object enrichedAdditionalDetails =
                                        mergeListIntoAdditionalDetails(additionalDetails, "state", boundary.getState());
                                item.getProject().put("additionalDetails", enrichedAdditionalDetails);

                                additionalDetails = item.getProject().get("additionalDetails");
                                enrichedAdditionalDetails =
                                        mergeListIntoAdditionalDetails(additionalDetails, "district", boundary.getDistrict());
                                item.getProject().put("additionalDetails", enrichedAdditionalDetails);
                            } else {
                                log.warn("⚠️ No boundary found for code={} in projectId={}", boundaryCode, item.getProject().get("id"));
                            }
                        }
                    }
                }
            }
        } else {
            log.warn("⚠️ No boundaries returned by boundaryUtil.getBoundaryByCode()");
        }

        ProjectResponse response = ProjectResponse.builder().items(items).totalCount(totalCount).build();
        log.info("✅ Completed project inbox search. Returning {} items (totalCount={})",
                response.getItems().size(), response.getTotalCount());

        return response;
    }

    private Object mergeListIntoAdditionalDetails(Object additionalDetails, String key, Object value) {
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
        /*
          As part of the new inbox, having currentProcessInstance as part of the index is mandated. This has been
          done to avoid having redundant network calls which could hog the performance.
        */
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
        if(processInstance.getAssigner()!=null)
            processInstance.getAssigner().setRoles(new ArrayList<>());

        if (processInstance.getAssignes() != null) {
            processInstance.getAssignes().stream()
                    .filter(Objects::nonNull)
                    .forEach(assignee -> assignee.setRoles(new ArrayList<>()));
        }
        return processInstance;
    }

    private List<Inbox> getInboxItems(InboxRequest inboxRequest, String indexName) {
        log.info("➡️ Fetching Inbox items for index={}", indexName);

        List<BusinessService> businessServices = workflowService.getBusinessServices(inboxRequest);
        log.debug("🔧 Retrieved {} business services", businessServices.size());

        Map<String, Object> finalQueryBody = queryBuilder.getESQuery(inboxRequest, Boolean.TRUE, Boolean.TRUE);

        try {
            if (log.isDebugEnabled()) {
                String q = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalQueryBody);
                log.debug("📑 ES Query for Inbox:\n{}", q);
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to serialize ES query for Inbox", e);
        }

        StringBuilder uri = getURI(indexName, SEARCH_PATH);
        log.info("🌐 Calling ES at URI={} for Inbox", uri);

        Object result = serviceRequestRepository.fetchESResult(uri, finalQueryBody);

        if (log.isDebugEnabled()) {
            log.debug("📥 Raw ES result for Inbox: {}", result);
        }

        List<Inbox> inboxItemsList = parseInboxItemsFromSearchResponse(result, businessServices);
        log.info("✅ Parsed {} Inbox items from ES response", inboxItemsList.size());

        return inboxItemsList;
    }


    private List<Project> getProjectInboxItems(InboxRequest inboxRequest, String indexName) {
        log.info("➡️ Fetching Project Inbox items for index={}", indexName);

        Map<String, Object> finalQueryBody = queryBuilder.getESQueryProject(inboxRequest, Boolean.TRUE);

        try {
            if (log.isDebugEnabled()) {
                String q = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalQueryBody);
                log.debug("📑 ES Query for Project Inbox:\n{}", q);
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to serialize ES query for Project Inbox", e);
        }

        StringBuilder uri = getURI(indexName, SEARCH_PATH);
        log.info("🌐 Calling ES at URI={} for Project Inbox", uri);

        Object result = serviceRequestRepository.fetchESResult(uri, finalQueryBody);

        if (log.isDebugEnabled()) {
            log.debug("📥 Raw ES result for Project Inbox: {}", result);
        }

        List<Project> inboxItemsList = parseProjectItemsFromSearchResponse(result);
        log.info("✅ Parsed {} Project Inbox items from ES response", inboxItemsList.size());

        return inboxItemsList;
    }


    private void enrichActionableStatusesFromRole(InboxRequest inboxRequest, List<BusinessService> businessServices) {
        ProcessInstanceSearchCriteria processCriteria = inboxRequest.getInbox().getProcessSearchCriteria();
        String tenantId = inboxRequest.getInbox().getTenantId();
        processCriteria.setTenantId(tenantId);

        HashMap<String, String> StatusIdNameMap = workflowService.getActionableStatusesForRole(inboxRequest.getRequestInfo(), businessServices,
                inboxRequest.getInbox().getProcessSearchCriteria());
        log.info(StatusIdNameMap.toString());
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
        log.debug("➡️ Fetching total Application count for index: {}", indexName);

        Map<String, Object> finalQueryBody = queryBuilder.getESQuery(inboxRequest, Boolean.FALSE, Boolean.FALSE);
        try {
            log.debug("ES Query (Application Count): {}", mapper.writeValueAsString(finalQueryBody));
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize Application count query", e);
            throw new RuntimeException(e);
        }

        StringBuilder uri = getURI(indexName, COUNT_PATH);
        Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchESResult(uri, finalQueryBody);
        log.debug("ES Response (Application Count): {}", response);

        Integer totalCount = 0;
        if (response.containsKey(COUNT_CONSTANT)) {
            totalCount = (Integer) response.get(COUNT_CONSTANT);
            log.info("✅ Total Application count = {}", totalCount);
        } else {
            log.error("❌ COUNT_CONSTANT not found in ES response for Applications");
            throw new CustomException("INBOX_COUNT_ERR", "Error occurred while executing ES count query");
        }
        return totalCount;
    }

    public Integer getTotalProjectCount(InboxRequest inboxRequest, String indexName) {
        log.debug("➡️ Fetching total Project count for index: {}", indexName);

        Map<String, Object> finalQueryBody = queryBuilder.getESQueryProject(inboxRequest, Boolean.FALSE);
        try {
            log.debug("ES Query (Project Count): {}", mapper.writeValueAsString(finalQueryBody));
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize Project count query", e);
            throw new RuntimeException(e);
        }

        StringBuilder uri = getURI(indexName, COUNT_PATH);
        Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchESResult(uri, finalQueryBody);
        log.debug("ES Response (Project Count): {}", response);

        Integer totalCount = 0;
        if (response.containsKey(COUNT_CONSTANT)) {
            totalCount = (Integer) response.get(COUNT_CONSTANT);
            log.info("✅ Total Project count = {}", totalCount);
        } else {
            log.error("❌ COUNT_CONSTANT not found in ES response for Projects");
            throw new CustomException("INBOX_COUNT_ERR", "Error occurred while executing ES count query");
        }
        return totalCount;
    }

    public List<HashMap<String, Object>> getStatusCountMap(InboxRequest inboxRequest, String indexName) {
        log.debug("➡️ Fetching Status Count Map for index: {}", indexName);

        Map<String, Object> finalQueryBody = queryBuilder.getStatusCountQuery(inboxRequest);
        log.debug("ES Query (Status Count): {}", finalQueryBody);

        StringBuilder uri = getURI(indexName, SEARCH_PATH);
        Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchESResult(uri, finalQueryBody);
        log.debug("ES Response (Status Count): {}", response);

        HashMap<String, Object> statusCountMap = parseStatusCountMapFromAggregationResponse(response);
        List<HashMap<String, Object>> transformedStatusMap = transformStatusMap(inboxRequest, statusCountMap);

        if (transformedStatusMap != null) {
            log.info("✅ Retrieved {} status count entries", transformedStatusMap.size());
        } else {
            log.warn("⚠️ Status count map was empty or null");
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
                    log.debug("📌 Calculated SLA (by state) for stateUuid {} = {} days", stateUuid, remaining);
                    return remaining;
                }
            } else {
                if (!ObjectUtils.isEmpty(auditDetails.get(CREATED_TIME_KEY))) {
                    Long createdTime = ((Number) auditDetails.get(CREATED_TIME_KEY)).longValue();
                    String businessService = JsonPath.read(data, BUSINESS_SERVICE_PATH);
                    Long businessServiceSLA = businessServiceSlaMap.get(businessService);
                    Long remaining = Math.round((businessServiceSLA - (currentDate - createdTime)) / ((double) (24 * 60 * 60 * 1000)));
                    log.debug("📌 Calculated SLA (by businessService) for {} = {} days", businessService, remaining);
                    return remaining;
                }
            }
        }
        log.warn("⚠️ SLA could not be calculated for data: {}", data);
        return null;
    }

    private List<HashMap<String, Object>> transformStatusMap(InboxRequest request, HashMap<String, Object> statusCountMap) {
        if (CollectionUtils.isEmpty(statusCountMap)) {
            log.warn("⚠️ transformStatusMap received an empty/null statusCountMap");
            return null;
        }

        List<BusinessService> businessServices = workflowService.getBusinessServices(request);
        Map<String, String> statusIdToBusinessServiceMap = workflowService.getStatusIdToBusinessServiceMap(businessServices);
        Map<String, String> statusIdToApplicationStatusMap = workflowService.getApplicationStatusIdToStatusMap(businessServices);

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

            log.trace("🔹 Transformed status {} => {}", statusId, map);
        }

        log.info("✅ Transformed {} status count entries", statusCountMapTransformed.size());
        return statusCountMapTransformed;
    }

    private HashMap<String, Object> parseStatusCountMapFromAggregationResponse(Map<String, Object> response) {
        if (CollectionUtils.isEmpty((Map<String, Object>) response.get(AGGREGATIONS_KEY))) {
            log.warn("⚠️ No aggregations found in ES response");
            return null;
        }

        List<Map<String, Object>> statusCountBuckets = JsonPath.read(response, STATUS_COUNT_AGGREGATIONS_BUCKETS_PATH);
        HashMap<String, Object> statusCountMap = new HashMap<>();

        statusCountBuckets.forEach(bucket -> {
            statusCountMap.put((String) bucket.get(KEY), bucket.get(DOC_COUNT_KEY));
        });

        log.info("✅ Parsed {} buckets into statusCountMap", statusCountMap.size());
        log.debug("📑 Status count map: {}", statusCountMap);

        return statusCountMap;
    }

    private List<Inbox> parseInboxItemsFromSearchResponse(Object result, List<BusinessService> businessServices) {
        log.info("➡️ Parsing inbox items from ES search response");

        Map<String, Object> hits = (Map<String, Object>) ((Map<String, Object>) result).get(HITS);
        List<Map<String, Object>> nestedHits = (List<Map<String, Object>>) hits.get(HITS);

        if (CollectionUtils.isEmpty(nestedHits)) {
            log.warn("⚠️ No hits found in ES response");
            return new ArrayList<>();
        }

        log.info("📥 Found {} hits in ES response", nestedHits.size());

        // Préparer les maps SLA
        Map<String, Long> businessServiceSlaMap = new HashMap<>();
        Map<String, Long> stateUuidVsSlaMap = new HashMap<>();

        businessServices.forEach(businessService -> {
            businessServiceSlaMap.put(businessService.getBusinessService(), businessService.getBusinessServiceSla());
            log.debug("🔧 BusinessService={} SLA={}", businessService.getBusinessService(), businessService.getBusinessServiceSla());

            businessService.getStates().forEach(state -> {
                if (!ObjectUtils.isEmpty(state.getSla())) {
                    stateUuidVsSlaMap.put(state.getUuid(), state.getSla());
                    log.debug("   ↪ State={} SLA={}", state.getUuid(), state.getSla());
                }
            });
        });

        // Construire les inbox items
        List<Inbox> inboxItemList = new ArrayList<>();
        nestedHits.forEach(hit -> {
            Inbox inbox = new Inbox();
            Map<String, Object> businessObject = (Map<String, Object>) hit.get(SOURCE_KEY);
            Map<String, Object> dataBusinessObject = (Map<String, Object>) businessObject.get(DATA_KEY);

            inbox.setBusinessObject(dataBusinessObject);

            Long serviceSla = getApplicationServiceSla(businessServiceSlaMap, stateUuidVsSlaMap, inbox.getBusinessObject());
            inbox.getBusinessObject().put(SERVICESLA_KEY, serviceSla);
            inbox.getBusinessObject().put(SLA_REMAINING, dataBusinessObject.get(SLA_REMAINING));
            inbox.getBusinessObject().put(STATE_SLA, dataBusinessObject.get(STATE_SLA));
            inbox.getBusinessObject().put(TOTAL_SLA_REMAINING, dataBusinessObject.get(TOTAL_SLA_REMAINING));

            log.debug("📌 Parsed inbox item with serviceSla={} | stateSla={} | slaRemaining={}",
                    serviceSla,
                    dataBusinessObject.get(STATE_SLA),
                    dataBusinessObject.get(SLA_REMAINING));

            inboxItemList.add(inbox);
        });

        log.info("✅ Successfully parsed {} inbox items", inboxItemList.size());

        return inboxItemList;
    }

    @SuppressWarnings("unchecked")
    private List<Project> parseProjectItemsFromSearchResponse(Object result) {
        log.debug("➡️ Parsing Project items from ES response...");

        Map<String, Object> hits = (Map<String, Object>) ((Map<String, Object>) result).get(HITS);
        List<Map<String, Object>> nestedHits = (List<Map<String, Object>>) hits.get(HITS);

        if (CollectionUtils.isEmpty(nestedHits)) {
            log.info("📭 No Project items found in ES response");
            return new ArrayList<>();
        }

        log.debug("📑 Found {} raw Project hits in ES response", nestedHits.size());

        List<Project> inboxItemList = new ArrayList<>();
        nestedHits.forEach(hit -> {
            Project project = new Project();
            Map<String, Object> businessObject = (Map<String, Object>) hit.get(SOURCE_KEY);
            Map<String, Object> dataBusinessObject = (Map<String, Object>) businessObject.get(DATA_KEY);

            project.setProject(dataBusinessObject);
            inboxItemList.add(project);

            if (log.isTraceEnabled()) {
                log.trace("📝 Parsed Project: {}", dataBusinessObject);
            }
        });

        log.info("✅ Parsed {} Project items successfully", inboxItemList.size());
        return inboxItemList;
    }


    public Integer getApplicationsNearingSlaCount(InboxRequest inboxRequest, String indexName) {
        log.info("➡️ Calculating applications nearing SLA for index={}", indexName);

        List<BusinessService> businessServicesObjs = workflowService.getBusinessServices(inboxRequest);
        Map<String, Long> businessServiceSlaMap = new HashMap<>();
        Map<String, HashSet<String>> businessServiceVsStateUuids = new HashMap<>();

        businessServicesObjs.forEach(businessService -> {
            List<String> listOfUuids = new ArrayList<>();
            businessService.getStates().forEach(state -> {
                listOfUuids.add(state.getUuid());
            });
            businessServiceVsStateUuids.put(businessService.getBusinessService(), new HashSet<>(listOfUuids));
            businessServiceSlaMap.put(businessService.getBusinessService(), businessService.getBusinessServiceSla());

            log.debug("🔧 BusinessService={} SLA={} | states={}",
                    businessService.getBusinessService(),
                    businessService.getBusinessServiceSla(),
                    listOfUuids.size());
        });

        List<String> uuidsInSearchCriteria = inboxRequest.getInbox().getProcessSearchCriteria().getStatus();
        Map<String, List<String>> businessServiceVsUuidsBasedOnSearchCriteria = new HashMap<>();

        if (!CollectionUtils.isEmpty(uuidsInSearchCriteria)) {
            log.info("📌 Using status filter with {} UUID(s)", uuidsInSearchCriteria.size());
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
        } else {
            log.info("📌 No status filter provided → using all states for each BusinessService");
            businessServiceVsStateUuids.forEach((businessService, setOfUuids) -> {
                businessServiceVsUuidsBasedOnSearchCriteria.put(businessService, new ArrayList<>(setOfUuids));
            });
        }

        List<String> businessServices = new ArrayList<>(businessServiceVsUuidsBasedOnSearchCriteria.keySet());
        Integer totalCount = 0;

        for (String businessService : businessServices) {
            Long businessServiceSla = businessServiceSlaMap.get(businessService);
            Map<String, Object> finalQueryBody = queryBuilder.getNearingSlaCountQuery(inboxRequest, businessServiceSla, businessService);
            StringBuilder uri = getURI(indexName, COUNT_PATH);

            if (log.isDebugEnabled()) {
                try {
                    log.debug("🔍 ES Query for service={} SLA={}:\n{}",
                            businessService,
                            businessServiceSla,
                            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(finalQueryBody));
                } catch (Exception e) {
                    log.warn("⚠️ Failed to pretty print ES query for service={}", businessService, e);
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) serviceRequestRepository.fetchESResult(uri, finalQueryBody);

            if (!response.containsKey("count")) {
                log.error("❌ ES response missing 'count' key for service={} response={}", businessService, response);
                throw new CustomException("INBOX_COUNT_ERR", "Error occurred while executing ES count query");
            }

            Integer cnt = (Integer) response.get("count");
            log.info("📊 ES count for service={} → {}", businessService, cnt);

            totalCount += cnt;
        }

        log.info("✅ Total nearing SLA applications = {}", totalCount);
        return totalCount;
    }



    private StringBuilder getURI(String indexName, String endpoint){
        StringBuilder uri = new StringBuilder(config.getIndexServiceHost());
        uri.append(indexName);
        uri.append(endpoint);
        return uri;
    }

    public SearchResponse getSpecificFieldsFromESIndex(SearchRequest searchRequest) {
        String tenantId = searchRequest.getIndexSearchCriteria().getTenantId();
        String moduleName = searchRequest.getIndexSearchCriteria().getModuleName();
        Map<String, Object> moduleSearchCriteria = searchRequest.getIndexSearchCriteria().getModuleSearchCriteria();

        validator.validateSearchCriteria(tenantId, moduleName, moduleSearchCriteria);
        InboxQueryConfiguration inboxQueryConfiguration = mdmsUtil.getConfigFromMDMS(tenantId, moduleName);
        hashParamsWhereverRequiredBasedOnConfiguration(moduleSearchCriteria, inboxQueryConfiguration);
        List<Data> data = getDataFromSimpleSearch(searchRequest, inboxQueryConfiguration.getIndex());
        SearchResponse searchResponse = SearchResponse.builder().data(data).build();
        return searchResponse;
    }

    private List<Data> getDataFromSimpleSearch(SearchRequest searchRequest, String index) {
        Map<String, Object> finalQueryBody = queryBuilder.getESQueryForSimpleSearch(searchRequest, Boolean.TRUE);
        try {
            String q = mapper.writeValueAsString(finalQueryBody);
            log.info("Query: "+q);
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
        Map<String, Object> hits = (Map<String, Object>)((Map<String, Object>) result).get(HITS);
        List<Map<String, Object>> nestedHits = (List<Map<String, Object>>) hits.get(HITS);
        if(CollectionUtils.isEmpty(nestedHits)){
            return new ArrayList<>();
        }

        List<Data> dataList = new ArrayList<>();
        nestedHits.forEach(hit -> {
            Data data = new Data();
            Map<String, Object> sourceObject = (Map<String, Object>) hit.get(SOURCE_KEY);
            Map<String, Object> dataObject = (Map<String, Object>)sourceObject.get(DATA_KEY);
            List<Field> fields = getFieldsFromDataObject(dataObject);
            data.setFields(fields);
            dataList.add(data);
        });

        return dataList;
    }

    private List<Field> getFieldsFromDataObject(Map<String, Object> dataObject) {
        List<Field> listOfFields = new ArrayList<>();
        try {
            Map<String, Object> flattenedDataObject = JsonFlattener.flattenAsMap(mapper.writeValueAsString(dataObject));
            flattenedDataObject.keySet().forEach(key -> {
                Field field = new Field();
                field.setKey(key);
                field.setValue(flattenedDataObject.get(key));
                listOfFields.add(field);
            });
        }catch (JsonProcessingException ex){
            throw new CustomException("EG_INBOX_GET_FIELDS_ERR", "Error while processing JSON.");
        }
        return listOfFields;
    }
}
