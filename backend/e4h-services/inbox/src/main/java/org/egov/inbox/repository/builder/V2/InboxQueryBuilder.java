package org.egov.inbox.repository.builder.V2;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.egov.inbox.util.ErrorConstants;
import org.egov.inbox.util.MDMSUtil;
import org.egov.inbox.web.model.InboxRequest;
import org.egov.inbox.web.model.V2.InboxQueryConfiguration;
import org.egov.inbox.web.model.V2.SearchParam;
import org.egov.inbox.web.model.V2.SearchRequest;
import org.egov.inbox.web.model.V2.SortParam;
import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import static org.egov.inbox.util.InboxConstants.*;


@Slf4j
@Component
public class InboxQueryBuilder implements QueryBuilderInterface {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MDMSUtil mdmsUtil;


    @Override
    public Map<String, Object> getESQuery(InboxRequest inboxRequest, Boolean isPaginationRequired, Boolean isSLA) {
        log.trace("Method invoked: getESQuery");
        String tenantId = inboxRequest.getInbox().getTenantId();
        String moduleName = inboxRequest.getInbox().getProcessSearchCriteria().getModuleName();
        
        log.info("Building ElasticSearch query - tenantId: {}, module: {}, pagination: {}, SLA: {}",
                tenantId, moduleName, isPaginationRequired, isSLA);

        InboxQueryConfiguration configuration = fetchInboxQueryConfiguration(inboxRequest);
        Map<String, Object> baseEsQuery = initializeBaseQuery(inboxRequest, isPaginationRequired, configuration);
        Map<String, String> nameToPathMap = buildSearchCriteriaMappings(configuration, inboxRequest);
        Map<String, SearchParam.Operator> nameToOperator = buildOperatorMappings(configuration, inboxRequest);
        
        Map<String, Object> innerBoolClause = extractInnerBoolClause(baseEsQuery);
        List<Object> mustClauseList = (ArrayList<Object>) innerBoolClause.get(MUST_KEY);
        List<Object> jurisdictionMustClauseList = new ArrayList<Object>();

        Map<String, Object> params = inboxRequest.getInbox().getModuleSearchCriteria();
        addAllSearchCriteriaToMustClauses(inboxRequest, nameToPathMap, nameToOperator, mustClauseList, jurisdictionMustClauseList);
        applyMergedMustClauses(innerBoolClause, mustClauseList, jurisdictionMustClauseList);
        applySlaFilterIfNeeded(params, isSLA, baseEsQuery, mustClauseList);

        log.info("ElasticSearch query built successfully - tenantId: {}, module: {}", tenantId, moduleName);
        if (log.isDebugEnabled()) {
            log.debug("Final ElasticSearch query structure completed");
        }

        return baseEsQuery;
    }

    private InboxQueryConfiguration fetchInboxQueryConfiguration(InboxRequest inboxRequest) {
        log.trace("Method invoked: fetchInboxQueryConfiguration");
        log.debug("Fetching inbox query configuration from MDMS");
        InboxQueryConfiguration configuration = mdmsUtil.getConfigFromMDMS(
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());
        log.debug("Configuration loaded - allowedCriteria: {}, sortParam: {}",
                configuration.getAllowedSearchCriteria().size(), 
                configuration.getSortParam() != null ? "present" : "null");
        return configuration;
    }

    private Map<String, Object> initializeBaseQuery(InboxRequest inboxRequest, Boolean isPaginationRequired, InboxQueryConfiguration configuration) {
        log.trace("Method invoked: initializeBaseQuery");
        Map<String, Object> params = inboxRequest.getInbox().getModuleSearchCriteria();
        log.debug("Initializing base ElasticSearch query");
        Map<String, Object> baseEsQuery = getBaseESQueryBody(inboxRequest, isPaginationRequired);
        log.debug("Base ElasticSearch query initialized");

        if (isPaginationRequired) {
            addPaginationClauses(baseEsQuery, configuration, params);
        }
        return baseEsQuery;
    }

    private void addPaginationClauses(Map<String, Object> baseEsQuery, InboxQueryConfiguration configuration, Map<String, Object> params) {
        log.trace("Method invoked: addPaginationClauses");
        String sortClauseFieldPath = configuration.getSortParam().getPath();
        SortParam.Order sortOrder = params.containsKey(SORT_ORDER_CONSTANT)
                ? SortParam.Order.valueOf((String) params.get(SORT_ORDER_CONSTANT))
                : configuration.getSortParam().getOrder();
        addSortClauseToBaseQuery(baseEsQuery, sortClauseFieldPath, sortOrder);
        log.debug("Sort clause added - field: {}, order: {}", sortClauseFieldPath, sortOrder);

        List<String> sourceFilterPathList = configuration.getSourceFilterPathList();
        addSourceFilterToBaseQuery(baseEsQuery, sourceFilterPathList);
        log.debug("Source filter added - filterCount: {}", sourceFilterPathList.size());
    }

    private Map<String, String> buildSearchCriteriaMappings(InboxQueryConfiguration configuration, InboxRequest inboxRequest) {
        log.trace("Method invoked: buildSearchCriteriaMappings");
        Map<String, String> nameToPathMap = new HashMap<>();
        configuration.getAllowedSearchCriteria().forEach(searchParam -> {
            nameToPathMap.put(searchParam.getName(), searchParam.getPath());
        });
        log.debug("Search criteria path mappings built - mappingCount: {}", nameToPathMap.size());
        return nameToPathMap;
    }

    private Map<String, SearchParam.Operator> buildOperatorMappings(InboxQueryConfiguration configuration, InboxRequest inboxRequest) {
        log.trace("Method invoked: buildOperatorMappings");
        Map<String, SearchParam.Operator> nameToOperator = new HashMap<>();
        configuration.getAllowedSearchCriteria().forEach(searchParam -> {
            nameToOperator.put(searchParam.getName(), searchParam.getOperator());
        });
        
        // Special case for tenantId
        if (inboxRequest.getInbox().getProcessSearchCriteria().getTenantId().split("\\.").length == 1
                && !inboxRequest.getInbox().getModuleSearchCriteria().get("tenantId").toString().contains(",")) {
            nameToOperator.put("tenantId", SearchParam.Operator.WILDCARD);
            log.debug("Applied wildcard operator for tenantId");
        }
        log.debug("Search criteria operator mappings built - mappingCount: {}", nameToOperator.size());
        return nameToOperator;
    }

    private Map<String, Object> extractInnerBoolClause(Map<String, Object> baseEsQuery) {
        log.trace("Method invoked: extractInnerBoolClause");
        Map<String, Object> innerBoolClause =
                (HashMap<String, Object>) ((HashMap<String, Object>) baseEsQuery.get(QUERY_KEY)).get(BOOL_KEY);
        log.debug("Inner bool clause extracted");
        return innerBoolClause;
    }

    private void addAllSearchCriteriaToMustClauses(InboxRequest inboxRequest, Map<String, String> nameToPathMap,
                                                   Map<String, SearchParam.Operator> nameToOperator,
                                                   List<Object> mustClauseList, List<Object> jurisdictionMustClauseList) {
        log.trace("Method invoked: addAllSearchCriteriaToMustClauses");
        Map<String, Object> params = inboxRequest.getInbox().getModuleSearchCriteria();
        Map<String, Object> jurisdictionParams = inboxRequest.getInbox().getJurisdictionSearchCriteria();

        log.debug("Adding module search criteria to must clause");
        addModuleSearchCriteriaToBaseQuery(params, nameToPathMap, nameToOperator, mustClauseList);
        log.debug("Module search criteria added - mustClauseSize: {}", mustClauseList.size());

        log.debug("Adding jurisdiction search criteria to must clause");
        addJurisdictionSearchCriteriaToBaseQuery(jurisdictionParams, nameToPathMap, nameToOperator, jurisdictionMustClauseList);
        log.debug("Jurisdiction search criteria added - jurisdictionMustClauseSize: {}", jurisdictionMustClauseList.size());

        log.debug("Adding process search criteria to must clause");
        addProcessSearchCriteriaToBaseQuery(inboxRequest.getInbox().getProcessSearchCriteria(), nameToPathMap, nameToOperator, mustClauseList);
        log.debug("Process search criteria added - mustClauseSize: {}", mustClauseList.size());

        if (log.isDebugEnabled()) {
            log.debug("Must clause list size: {}", mustClauseList.size());
            log.debug("Jurisdiction must clause list size: {}", jurisdictionMustClauseList.size());
        }
    }

    private void applyMergedMustClauses(Map<String, Object> innerBoolClause, List<Object> mustClauseList,
                                        List<Object> jurisdictionMustClauseList) {
        log.trace("Method invoked: applyMergedMustClauses");
        // Group the different blocks of should into a single should block
        log.debug("Extracting should clauses from must clause lists");
        List<Map<String, Object>> updatedMustClauseList = extractShouldClauses(mustClauseList);
        List<Map<String, Object>> updatedJurisdictionMustClauseList = extractJurisdictionShouldClauses(jurisdictionMustClauseList);
        List<Map<String, Object>> mergedMustClause = mergeMustClauseLists(updatedMustClauseList, updatedJurisdictionMustClauseList);
        log.debug("Must clauses merged - mergedSize: {}", mergedMustClause.size());

        innerBoolClause.put(MUST_KEY, mergedMustClause);
    }

    private void applySlaFilterIfNeeded(Map<String, Object> params, Boolean isSLA, Map<String, Object> baseEsQuery,
                                        List<Object> mustClauseList) {
        log.trace("Method invoked: applySlaFilterIfNeeded");
        // Add SLA filter if required
        if (params.containsKey("nearingSLA") && isSLA) {
            log.info("Applying SLA filter - nearingSLA enabled");
            addSlaExclusionsToQuery(baseEsQuery);
            addSlaScriptFilter(mustClauseList);
        }
    }

    private void addSlaExclusionsToQuery(Map<String, Object> baseEsQuery) {
        log.trace("Method invoked: addSlaExclusionsToQuery");
        Map<String, Object> query = (Map<String, Object>) baseEsQuery.get("query");
        Map<String, Object> boolClause = (Map<String, Object>) query.get("bool");

        List<Map<String, Object>> mustNotClauseList =
                (List<Map<String, Object>>) boolClause.getOrDefault("must_not", new ArrayList<>());

        Map<String, Object> terminateClause = new HashMap<>();
        terminateClause.put("term", Collections.singletonMap("Data.currentProcessInstance.state.isTerminateState", true));
        mustNotClauseList.add(terminateClause);

        Map<String, Object> excludeIncidentTerm = new HashMap<>();
        excludeIncidentTerm.put("term", Collections.singletonMap("Data.currentProcessInstance.businessService.keyword", "Incident"));
        mustNotClauseList.add(excludeIncidentTerm);

        boolClause.put("must_not", mustNotClauseList);
        log.debug("SLA exclusions added - terminated tickets and Incident service");
    }

    private void addSlaScriptFilter(List<Object> mustClauseList) {
        log.trace("Method invoked: addSlaScriptFilter");
        Map<String, Object> scriptInner = new HashMap<>();
        scriptInner.put("source",
                "doc.containsKey('Data.slaRemaining') && " +
                        "doc.containsKey('Data.stateSla') && " +
                        "doc['Data.stateSla'].size() > 0 && " +
                        "doc['Data.stateSla'].value > 0 && " +
                        "((double) doc['Data.slaRemaining'].value / doc['Data.stateSla'].value) <= 0.3");
        scriptInner.put("lang", "painless");

        Map<String, Object> scriptClause = new HashMap<>();
        scriptClause.put("script", scriptInner);

        mustClauseList.add(Collections.singletonMap("script", scriptClause));
        log.debug("SLA painless script filter added");
    }

    // Group the different blocks of should into a single should block
    public List<Map<String, Object>> extractShouldClauses(List<Object> mustClauseList) {
        List<Map<String, Object>> result =
                mustClauseList.stream()
                        .filter(o -> o instanceof Map)
                        .map(o -> (Map<String, Object>) o)
                        .collect(Collectors.toList());

        List<Map<String, Object>> finalClauses = new ArrayList<>();
        List<Object> mergedShouldList = new ArrayList<>();

        for (Map<String, Object> clause : result) {

            if (clause.containsKey("should")) {
                Object shouldObj = clause.get("should");

                // C’est soit un map direct, soit une liste de maps
                if (shouldObj instanceof Map) {
                    mergedShouldList.add(shouldObj);
                } else if (shouldObj instanceof List) {
                    mergedShouldList.addAll((List<?>) shouldObj);
                }

            } else {
                // Clause normale → on garde
                finalClauses.add(clause);
            }
        }

        // Ajouter le should regroupé si non vide
        if (!mergedShouldList.isEmpty()) {
            Map<String, Object> groupedShould = new HashMap<>();
            groupedShould.put("bool", new HashMap<>());
            Map<String, Object> boolShouldList = (Map<String, Object>) groupedShould.get("bool");
            boolShouldList.put("should", mergedShouldList);
            boolShouldList.put("minimum_should_match", 1);
            finalClauses.add(groupedShould);
        }

        return finalClauses;
    }

    // Group the different blocks of should into a single should block
    public List<Map<String, Object>> extractJurisdictionShouldClauses(List<Object> mustClauseList) {
        List<Map<String, Object>> result =
                mustClauseList.stream()
                        .filter(o -> o instanceof Map)
                        .map(o -> (Map<String, Object>) o)
                        .collect(Collectors.toList());

        List<Map<String, Object>> finalClauses = new ArrayList<>();
        List<Object> mergedShouldList = new ArrayList<>();

        for (Map<String, Object> clause : result) {

            if (clause.containsKey("should")) {
                Object shouldObj = clause.get("should");

                // C’est soit un map direct, soit une liste de maps
                if (shouldObj instanceof Map) {
                    mergedShouldList.add(shouldObj);
                } else if (shouldObj instanceof List) {
                    mergedShouldList.addAll((List<?>) shouldObj);
                }

            }
        }

        // Ajouter le should regroupé si non vide
        if (!mergedShouldList.isEmpty()) {
            Map<String, Object> groupedShould = new HashMap<>();
            groupedShould.put("bool", new HashMap<>());
            Map<String, Object> boolShouldList = (Map<String, Object>) groupedShould.get("bool");
            boolShouldList.put("should", mergedShouldList);
            boolShouldList.put("minimum_should_match", 1);
            finalClauses.add(groupedShould);
        }

        return finalClauses;
    }

    public List<Map<String, Object>> mergeMustClauseLists(
            List<Map<String, Object>> list1,
            List<Map<String, Object>> list2) {

        List<Map<String, Object>> result = new ArrayList<>();

        // Conserver les éléments non-bool de list1 (ex: wildcard)
        list1.forEach(clause -> {
            if (!clause.containsKey("bool")) {
                result.add(clause);
            }
        });

        // Fonction utilitaire pour compléter should avec toutes les clés nécessaires
        BiFunction<Map<String, Object>, List<String>, Map<String, Object>> normalizeBool = (boolClause, allKeys) -> {
            Map<String, Object> boolCopy = new HashMap<>();
            Map<String, Object> innerBool = new HashMap<>();

            List<Map<String, Object>> shouldList = new ArrayList<>();
            if (boolClause.containsKey("bool")) {
                Map<String, Object> existingBool = (Map<String, Object>) boolClause.get("bool");
                List<Map<String, Object>> existingShould = (List<Map<String, Object>>) existingBool.getOrDefault("should", new ArrayList<>());

                // Ajouter toutes les clés manquantes avec empty lists
                for (String key : allKeys) {
                    boolean keyExists = existingShould.stream().anyMatch(map -> {
                        if (map.containsKey("terms")) {
                            return ((Map<String,Object>) map.get("terms")).containsKey(key);
                        }
                        return false;
                    });
                    if (!keyExists) {
                        Map<String, Object> emptyTerms = new HashMap<>();
                        emptyTerms.put("terms", Collections.singletonMap(key, new ArrayList<>()));
                        existingShould.add(emptyTerms);
                    }
                }

                shouldList.addAll(existingShould);
                innerBool.put("should", shouldList);
                innerBool.put("minimum_should_match", 1);
                boolCopy.put("bool", innerBool);
            }

            return boolCopy;
        };

        // Déterminer toutes les clés possibles pour uniformiser
        List<String> allKeys = Arrays.asList(
                "Data.incident.boundary.countryCode.keyword",
                "Data.incident.boundary.stateCode.keyword",
                "Data.incident.boundary.districtCode.keyword",
                "Data.incident.boundary.blockCode.keyword",
                "Data.incident.boundary.facilityCode.keyword"
        );

        // Normaliser list2 et ajouter à la liste finale
        list2.forEach(clause -> {
            if (clause.containsKey("bool")) {
                result.add(normalizeBool.apply(clause, allKeys));
            }
        });

        // Normaliser list1 bools et ajouter à la liste finale
        list1.forEach(clause -> {
            if (clause.containsKey("bool")) {
                result.add(normalizeBool.apply(clause, allKeys));
            }
        });

        return result;
    }

    public Map<String, Object> getESQueryProject(InboxRequest inboxRequest, Boolean isPaginationRequired) {
        log.trace("Method invoked: getESQueryProject");
        String tenantId = inboxRequest.getInbox().getTenantId();
        String moduleName = inboxRequest.getInbox().getProcessSearchCriteria().getModuleName();
        log.info("Starting ElasticSearch query build for project - tenantId: {}, module: {}", tenantId, moduleName);

        InboxQueryConfiguration configuration = fetchConfigurationForProject(tenantId, moduleName);
        Map<String, Object> baseEsQuery = initializeProjectBaseQuery(inboxRequest, isPaginationRequired, configuration);
        Map<String, String> nameToPathMap = buildSearchCriteriaMappings(configuration, inboxRequest);
        Map<String, SearchParam.Operator> nameToOperator = buildOperatorMappingsForProject(configuration);
        
        Map<String, Object> innerBoolClause = extractInnerBoolClause(baseEsQuery);
        List<Object> mustClauseList = (ArrayList<Object>) innerBoolClause.get(MUST_KEY);
        
        addProjectSearchCriteriaToMustClauses(inboxRequest, nameToPathMap, nameToOperator, mustClauseList);
        innerBoolClause.put(MUST_KEY, mustClauseList);

        log.info("ElasticSearch query build completed for project - tenantId: {}, module: {}", tenantId, moduleName);
        if (log.isDebugEnabled()) {
            log.debug("Final ElasticSearch query structure completed");
        }

        return baseEsQuery;
    }

    private InboxQueryConfiguration fetchConfigurationForProject(String tenantId, String moduleName) {
        log.trace("Method invoked: fetchConfigurationForProject");
        log.debug("Fetching MDMS configuration");
        InboxQueryConfiguration configuration = mdmsUtil.getConfigFromMDMS(tenantId, moduleName);
        log.debug("MDMS configuration fetched - allowedCriteria: {}", 
                configuration != null ? configuration.getAllowedSearchCriteria().size() : 0);
        return configuration;
    }

    private Map<String, Object> initializeProjectBaseQuery(InboxRequest inboxRequest, Boolean isPaginationRequired, InboxQueryConfiguration configuration) {
        log.trace("Method invoked: initializeProjectBaseQuery");
        Map<String, Object> params = inboxRequest.getInbox().getModuleSearchCriteria();
        log.debug("Initializing base ElasticSearch query body");
        Map<String, Object> baseEsQuery = getBaseESQueryBody(inboxRequest, isPaginationRequired);
        log.debug("Base ElasticSearch query body initialized");

        if (isPaginationRequired) {
            addPaginationClauses(baseEsQuery, configuration, params);
        }
        return baseEsQuery;
    }

    private Map<String, SearchParam.Operator> buildOperatorMappingsForProject(InboxQueryConfiguration configuration) {
        log.trace("Method invoked: buildOperatorMappingsForProject");
        Map<String, SearchParam.Operator> nameToOperator = new HashMap<>();
        configuration.getAllowedSearchCriteria().forEach(searchParam -> {
            nameToOperator.put(searchParam.getName(), searchParam.getOperator());
        });
        log.debug("Name to operator map built - mappingCount: {}", nameToOperator.size());
        return nameToOperator;
    }

    private void addProjectSearchCriteriaToMustClauses(InboxRequest inboxRequest, Map<String, String> nameToPathMap,
                                                       Map<String, SearchParam.Operator> nameToOperator,
                                                       List<Object> mustClauseList) {
        log.trace("Method invoked: addProjectSearchCriteriaToMustClauses");
        Map<String, Object> params = inboxRequest.getInbox().getModuleSearchCriteria();
        
        log.debug("Adding module search criteria to ElasticSearch query");
        addModuleSearchCriteriaToBaseQuery(params, nameToPathMap, nameToOperator, mustClauseList);

        log.debug("Adding process search criteria to ElasticSearch query");
        addProcessSearchCriteriaToBaseQuery(inboxRequest.getInbox().getProcessSearchCriteria(), nameToPathMap, nameToOperator, mustClauseList);
        log.debug("Project search criteria added to must clauses");
    }

    public Map<String, Object> getESQueryForSimpleSearch(SearchRequest searchRequest, Boolean isPaginationRequired) {

        InboxQueryConfiguration configuration = mdmsUtil.getConfigFromMDMS(
                searchRequest.getIndexSearchCriteria().getTenantId(), searchRequest.getIndexSearchCriteria().getModuleName());
        Map<String, Object> params = searchRequest.getIndexSearchCriteria().getModuleSearchCriteria();
        Map<String, Object> baseEsQuery = getBaseESQueryBody(searchRequest, isPaginationRequired);

        if (isPaginationRequired) {
            // Adds sort clause to the inbox ES query only in case pagination is present, else not
            String sortClauseFieldPath = configuration.getSortParam().getPath();
            SortParam.Order sortOrder = searchRequest.getIndexSearchCriteria().getModuleSearchCriteria().containsKey(SORT_ORDER_CONSTANT) ? SortParam.Order.valueOf((String) searchRequest.getIndexSearchCriteria().getModuleSearchCriteria().get(SORT_ORDER_CONSTANT)) : configuration.getSortParam().getOrder();
            addSortClauseToBaseQuery(baseEsQuery, sortClauseFieldPath, sortOrder);

            // Adds source filter only when requesting for inbox items.
            List<String> sourceFilterPathList = configuration.getSourceFilterPathList();
            addSourceFilterToBaseQuery(baseEsQuery, sourceFilterPathList);
        }

        Map<String, Object> innerBoolClause = (HashMap<String, Object>) ((HashMap<String, Object>) baseEsQuery.get(QUERY_KEY)).get(BOOL_KEY);
        List<Object> mustClauseList = (ArrayList<Object>) innerBoolClause.get(MUST_KEY);

        Map<String, String> nameToPathMap = new HashMap<>();
        Map<String, SearchParam.Operator> nameToOperator = new HashMap<>();

        configuration.getAllowedSearchCriteria().forEach(searchParam -> {
            nameToPathMap.put(searchParam.getName(), searchParam.getPath());
            nameToOperator.put(searchParam.getName(), searchParam.getOperator());
        });

        addModuleSearchCriteriaToBaseQuery(params, nameToPathMap, nameToOperator, mustClauseList);

        innerBoolClause.put(MUST_KEY, mustClauseList);

        return baseEsQuery;
    }

    private void addSourceFilterToBaseQuery(Map<String, Object> baseEsQuery, List<String> sourceFilterPathList) {
        if (!CollectionUtils.isEmpty(sourceFilterPathList))
            baseEsQuery.put(SOURCE_KEY, sourceFilterPathList);
    }

    private void addSortClauseToBaseQuery(Map<String, Object> baseEsQuery, String sortClauseFieldPath, SortParam.Order sortOrder) {
        List<Map> sortClause = new ArrayList<>();
        Map<String, Object> innerSortOrderClause = new HashMap<>();
        innerSortOrderClause.put(ORDER_KEY, sortOrder);
        Map<String, Map> outerSortClauseChild = new HashMap<>();
        outerSortClauseChild.put(sortClauseFieldPath, innerSortOrderClause);
        sortClause.add(outerSortClauseChild);
        baseEsQuery.put(SORT_KEY, sortClause);
    }

    private void addProcessSearchCriteriaToBaseQuery(ProcessInstanceSearchCriteria processSearchCriteria, Map<String, String> nameToPathMap, Map<String, SearchParam.Operator> nameToOperator, List<Object> mustClauseList) {
        log.trace("Method invoked: addProcessSearchCriteriaToBaseQuery");
        addStatusCriteriaIfPresent(processSearchCriteria, nameToPathMap, nameToOperator, mustClauseList);
        addAssigneeCriteriaIfPresent(processSearchCriteria, nameToPathMap, nameToOperator, mustClauseList);
        addFromDateCriteriaIfPresent(processSearchCriteria, nameToPathMap, nameToOperator, mustClauseList);
        addToDateCriteriaIfPresent(processSearchCriteria, nameToPathMap, nameToOperator, mustClauseList);
    }

    private void addStatusCriteriaIfPresent(ProcessInstanceSearchCriteria processSearchCriteria, Map<String, String> nameToPathMap, Map<String, SearchParam.Operator> nameToOperator, List<Object> mustClauseList) {
        log.trace("Method invoked: addStatusCriteriaIfPresent");
        if (!ObjectUtils.isEmpty(processSearchCriteria.getStatus())) {
            String key = "status";
            processSearchCriteria.getStatus().removeAll(Collections.singleton(null));
            addProcessCriteriaToMustClause(key, processSearchCriteria.getStatus(), nameToPathMap, nameToOperator, mustClauseList);
        }
    }

    private void addAssigneeCriteriaIfPresent(ProcessInstanceSearchCriteria processSearchCriteria, Map<String, String> nameToPathMap, Map<String, SearchParam.Operator> nameToOperator, List<Object> mustClauseList) {
        log.trace("Method invoked: addAssigneeCriteriaIfPresent");
        if (!ObjectUtils.isEmpty(processSearchCriteria.getAssignee())) {
            String key = "assignee";
            addProcessCriteriaToMustClause(key, processSearchCriteria.getAssignee(), nameToPathMap, nameToOperator, mustClauseList);
        }
    }

    private void addFromDateCriteriaIfPresent(ProcessInstanceSearchCriteria processSearchCriteria, Map<String, String> nameToPathMap, Map<String, SearchParam.Operator> nameToOperator, List<Object> mustClauseList) {
        log.trace("Method invoked: addFromDateCriteriaIfPresent");
        if (!ObjectUtils.isEmpty(processSearchCriteria.getFromDate())) {
            String key = "fromDate";
            addProcessCriteriaToMustClause(key, processSearchCriteria.getFromDate(), nameToPathMap, nameToOperator, mustClauseList);
        }
    }

    private void addToDateCriteriaIfPresent(ProcessInstanceSearchCriteria processSearchCriteria, Map<String, String> nameToPathMap, Map<String, SearchParam.Operator> nameToOperator, List<Object> mustClauseList) {
        log.trace("Method invoked: addToDateCriteriaIfPresent");
        if (!ObjectUtils.isEmpty(processSearchCriteria.getToDate())) {
            String key = "toDate";
            addProcessCriteriaToMustClause(key, processSearchCriteria.getToDate(), nameToPathMap, nameToOperator, mustClauseList);
        }
    }

    private void addProcessCriteriaToMustClause(String key, Object value, Map<String, String> nameToPathMap, Map<String, SearchParam.Operator> nameToOperator, List<Object> mustClauseList) {
        log.trace("Method invoked: addProcessCriteriaToMustClause - key: {}", key);
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        Map<String, Object> mustClauseChild = (Map<String, Object>) prepareMustClauseChild(params, key, nameToPathMap, nameToOperator);
        if (CollectionUtils.isEmpty(mustClauseChild)) {
            log.info("Error occurred while preparing filter for must clause. Filter for key " + key + " will not be added.");
        } else {
            mustClauseList.add(mustClauseChild);
            log.debug("Process criteria added to must clause - key: {}", key);
        }
    }


    private void addModuleSearchCriteriaToBaseQuery(Map<String, Object> params, Map<String, String> nameToPathMap,
                                                    Map<String, SearchParam.Operator> nameToOperator, List<Object> mustClauseList) {
        params.keySet().forEach(key -> {
            if (!(key.equals(SORT_ORDER_CONSTANT) || key.equals(SORT_BY_CONSTANT))) {

                SearchParam.Operator operator = nameToOperator.get(key);
                if (operator != null && operator.equals(SearchParam.Operator.WILDCARD)) {
                    List<Map<String, Object>> mustClauseChild = null;

                    mustClauseChild = (List<Map<String, Object>>) prepareMustClauseWildCardChild(params, key,
                            nameToPathMap, nameToOperator);

                    if (CollectionUtils.isEmpty(mustClauseChild)) {
                        log.info("Error occurred while preparing filter for must clause. Filter for key " + key
                                + " will not be added.");
                    } else {
                        mustClauseList.addAll(mustClauseChild);
                    }
                } else {

                    Map<String, Object> mustClauseChild = null;
                    mustClauseChild = (Map<String, Object>) prepareMustClauseChild(params, key, nameToPathMap,
                            nameToOperator);
                    if (CollectionUtils.isEmpty(mustClauseChild)) {
                        log.info("Error occurred while preparing filter for must clause. Filter for key " + key
                                + " will not be added.");
                    } else {
                        mustClauseList.add(mustClauseChild);
                    }

                }
            }
        });
    }

    private void addJurisdictionSearchCriteriaToBaseQuery(Map<String, Object> params, Map<String, String> nameToPathMap,
                                                    Map<String, SearchParam.Operator> nameToOperator, List<Object> mustClauseList) {
        params.keySet().forEach(key -> {
            if (!(key.equals(SORT_ORDER_CONSTANT) || key.equals(SORT_BY_CONSTANT))) {

                SearchParam.Operator operator = nameToOperator.get(key);
                if (operator != null && operator.equals(SearchParam.Operator.WILDCARD)) {
                    List<Map<String, Object>> mustClauseChild = null;

                    mustClauseChild = (List<Map<String, Object>>) prepareMustClauseWildCardChild(params, key,
                            nameToPathMap, nameToOperator);

                    if (CollectionUtils.isEmpty(mustClauseChild)) {
                        log.info("Error occurred while preparing filter for must clause. Filter for key " + key
                                + " will not be added.");
                    } else {
                        mustClauseList.addAll(mustClauseChild);
                    }
                } else {

                    Map<String, Object> mustClauseChild = null;
                    mustClauseChild = (Map<String, Object>) prepareMustClauseChild(params, key, nameToPathMap,
                            nameToOperator);
                    if (CollectionUtils.isEmpty(mustClauseChild)) {
                        log.info("Error occurred while preparing filter for must clause. Filter for key " + key
                                + " will not be added.");
                    } else {
                        mustClauseList.add(mustClauseChild);
                    }

                }
            }
        });
    }

    @Override
    public Map<String, Object> getStatusCountQuery(InboxRequest inboxRequest) {
        Map<String, Object> baseEsQuery = getESQuery(inboxRequest, Boolean.FALSE, Boolean.FALSE);
        appendStatusCountAggsNode(baseEsQuery);
        log.info("status query====", baseEsQuery);
        return baseEsQuery;
    }

    @Override
    public Map<String, Object> getNearingSlaCountQuery(InboxRequest inboxRequest, Long businessServiceSla, String businessService) {
        Map<String, Object> baseEsQuery = getESQuery(inboxRequest, Boolean.FALSE, Boolean.FALSE);

        Map<String, Object> query = (Map<String, Object>) baseEsQuery.get("query");
        Map<String, Object> bool = (Map<String, Object>) query.get("bool");

        // Ensure must_not clause exists
        List<Object> mustNotClauseList = (List<Object>) bool.getOrDefault("must_not", new ArrayList<>());

        // Add isTerminateState filter to must_not
        Map<String, Object> terminateTerm = new HashMap<>();
        terminateTerm.put("Data.currentProcessInstance.state.isTerminateState", true);
        Map<String, Object> mustNotTermWrapper = new HashMap<>();
        mustNotTermWrapper.put("term", terminateTerm);
        mustNotClauseList.add(mustNotTermWrapper);

        bool.put("must_not", mustNotClauseList);

        // Add to must clause
        List<Object> mustClauseList = (List<Object>) bool.get("must");

        // Add businessService term filter
        Map<String, Object> serviceTerm = new HashMap<>();
        serviceTerm.put("Data.currentProcessInstance.businessService.keyword", businessService);
        Map<String, Object> termWrapper = new HashMap<>();
        termWrapper.put("term", serviceTerm);
        mustClauseList.add(termWrapper);

        // Build the painless script
        Map<String, Object> innerScript = new HashMap<>();
        innerScript.put("source",
                "doc.containsKey('Data.slaRemaining') && " +
                        "doc.containsKey('Data.stateSla') && " +
                        "doc['Data.stateSla'].size() > 0 && " +
                        "doc['Data.stateSla'].value > 0 && " +
                        "((double) doc['Data.slaRemaining'].value / doc['Data.stateSla'].value) <= 0.3");
        innerScript.put("lang", "painless");

        Map<String, Object> script = new HashMap<>();
        script.put("script", innerScript);

        mustClauseList.add(Collections.singletonMap("script", script));

        bool.put("must", mustClauseList);

        log.info("Nearing SLA Query: " + baseEsQuery);
        return baseEsQuery;
    }


    private void appendStatusCountAggsNode(Map<String, Object> baseEsQuery) {
        Map<String, Object> aggsNode = new HashMap<>();
        aggsNode.put("statusCount", new HashMap<>());
        Map<String, Object> statusCountNode = (Map<String, Object>) aggsNode.get("statusCount");
        statusCountNode.put("terms", new HashMap<>());
        Map<String, Object> innerTermsQuery = (Map<String, Object>) statusCountNode.get("terms");
        innerTermsQuery.put("field", "Data.incident.applicationStatus.keyword");
        baseEsQuery.put("aggs", aggsNode);
    }

    private Map<String, Object> getBaseESQueryBody(InboxRequest inboxRequest, Boolean isPaginationRequired) {
        Map<String, Object> baseEsQuery = new HashMap<>();
        Map<String, Object> boolQuery = new HashMap<>();
        Map<String, Object> mustClause = new HashMap<>();

        // Prepare bool query
        boolQuery.put("bool", new HashMap<>());
        Map<String, Object> innerBoolBody = (Map<String, Object>) boolQuery.get("bool");
        innerBoolBody.put("must", new ArrayList<>());

        // Prepare base ES query
        if (isPaginationRequired) {
            baseEsQuery.put("from", inboxRequest.getInbox().getOffset());
            baseEsQuery.put("size", inboxRequest.getInbox().getLimit());
        }
        baseEsQuery.put("query", boolQuery);

        return baseEsQuery;
    }

    private Map<String, Object> getBaseESQueryBody(SearchRequest searchRequest, Boolean isPaginationRequired) {
        Map<String, Object> baseEsQuery = new HashMap<>();
        Map<String, Object> boolQuery = new HashMap<>();

        // Prepare bool query
        boolQuery.put("bool", new HashMap<>());
        Map<String, Object> innerBoolBody = (Map<String, Object>) boolQuery.get("bool");
        innerBoolBody.put("must", new ArrayList<>());

        // Prepare base ES query
        if (isPaginationRequired) {
            baseEsQuery.put("from", searchRequest.getIndexSearchCriteria().getOffset());
            baseEsQuery.put("size", searchRequest.getIndexSearchCriteria().getLimit());
        }
        baseEsQuery.put("query", boolQuery);

        return baseEsQuery;
    }

    private Object prepareMustClauseChild(Map<String, Object> params, String key, Map<String, String> nameToPathMap,
                                          Map<String, SearchParam.Operator> nameToOperatorMap) {

        SearchParam.Operator operator = nameToOperatorMap.get(key);
        log.trace("Preparing must clause - key: {}, operator: {}", key, operator);

        if (operator == null || operator.equals(SearchParam.Operator.EQUAL)) {
            if (params.get(key) instanceof List) {
                log.debug("Adding TERMS clause - key: {}, valueCount: {}", key, ((List<?>) params.get(key)).size());
                Map<String, Object> termsClause = new HashMap<>();
                termsClause.put("terms", new HashMap<>());
                Map<String, Object> innerTermsClause = (Map<String, Object>) termsClause.get("terms");
                innerTermsClause.put(addDataPathToSearchParamKey(key, nameToPathMap), params.get(key));
                return termsClause;
            } else {
                log.debug("Adding TERM clause - key: {}", key);
                Map<String, Object> termClause = new HashMap<>();
                termClause.put("term", new HashMap<>());
                Map<String, Object> innerTermClause = (Map<String, Object>) termClause.get("term");
                innerTermClause.put(addDataPathToSearchParamKey(key, nameToPathMap), params.get(key));
                return termClause;
            }
        } else if (operator.equals(SearchParam.Operator.LTE) || operator.equals(SearchParam.Operator.GTE)) {
            log.debug("Adding RANGE clause - key: {}, operator: {}", key, operator);
            Map<String, Object> rangeClause = new HashMap<>();
            rangeClause.put("range", new HashMap<>());
//            Map<String, Object> innerRangeClause = (Map<String, Object>) rangeClause.get("range");
//            Map<String, Object> innerRangeClauseBis = new HashMap<>();
//            innerRangeClauseBis.put("lte", 0);
//            innerRangeClauseBis.put(operator.toString(), params.get(key));
//            innerRangeClause.put(addDataPathToSearchParamKey(key, nameToPathMap), innerRangeClauseBis);
            Map<String, Object> innerRangeClause = new HashMap<>();
            innerRangeClause.put(operator.toString(), params.get(key));
            ((Map<String, Object>) rangeClause.get("range")).put(addDataPathToSearchParamKey(key, nameToPathMap), innerRangeClause);
            return rangeClause;
        } else if (operator.equals(SearchParam.Operator.MUST_NOT)) {
            log.debug("Adding MUST_NOT clause - key: {}", key);
            Map<String, Object> boolClause = new HashMap<>();
            boolClause.put("bool", new HashMap<>());
            Map<String, Object> mustNotClause = (Map<String, Object>) boolClause.get("bool");
            mustNotClause.put("must_not", new HashMap<>());
            Map<String, Object> termClause = (Map<String, Object>) mustNotClause.get("must_not");
            termClause.put("terms", new HashMap<>());
            Map<String, Object> innerTermClause = (Map<String, Object>) termClause.get("terms");
            innerTermClause.put(addDataPathToSearchParamKey(key, nameToPathMap), params.get(key));
            return boolClause;
        } else if (operator.equals(SearchParam.Operator.SHOULD)) {
            log.debug("Adding SHOULD clause - key: {}", key);
            Map<String, Object> shouldClause = new HashMap<>();
            shouldClause.put("should", new HashMap<>());
            Map<String, Object> termsClause = (Map<String, Object>) shouldClause.get("should");
            termsClause.put("terms", new HashMap<>());
            Map<String, Object> innerShouldClause = (Map<String, Object>) termsClause.get("terms");
            innerShouldClause.put(addDataPathToSearchParamKey(key, nameToPathMap), params.get(key));
            log.debug("SHOULD clause created - key: {}", key);
            return shouldClause;
        } else if (operator.equals(SearchParam.Operator.SLA_COMPARE)) {
            log.debug("SLA_COMPARE operator detected - key: {}, returning empty clause", key);
            return new HashMap<>();
        } else if (operator.equals(SearchParam.Operator.MULTI_MATCH)) {
            String searchValue = params.get("search").toString();
            log.debug("Adding MULTI_MATCH clause - searchValue length: {}", searchValue.length());
            Map<String, Object> multiMatch = new HashMap<>();
            multiMatch.put("query", searchValue);
            multiMatch.put("fields", nameToPathMap.get("search").split(","));
            multiMatch.put("fuzziness", 2);
            Map<String, Object> parent = new HashMap<>();
            parent.put("multi_match", multiMatch);
            return parent;
        } else {
            log.error("Unsupported operator - operator: {}, key: {}", operator, key);
            throw new CustomException(ErrorConstants.INVALID_OPERATOR_DATA, "Unsupported Operator : " + operator);
        }
    }

    private List<Map<String, Object>> prepareMustClauseWildCardChild(Map<String, Object> params, String key,
                                                                     Map<String, String> nameToPathMap,
                                                                     Map<String, SearchParam.Operator> nameToOperatorMap) {
        Object value = params.get(key);
        log.trace("Preparing wildcard clause - key: {}", key);

        List<Map<String, Object>> wildcardClauses = new ArrayList<>();

        if (value instanceof List) {
            List<Object> values = (List<Object>) value;
            log.debug("Value is a list - key: {}, itemCount: {}", key, values.size());

            for (Object item : values) {
                log.trace("Adding wildcard for list item - key: {}", key);

                Map<String, Object> wildcardClause = new HashMap<>();
                wildcardClause.put("wildcard", new HashMap<>());
                Map<String, Object> innerWildcardClause = (Map<String, Object>) wildcardClause.get("wildcard");

                if (key.equals("tenantId")) {
                    innerWildcardClause.put(addDataPathToSearchParamKey(key, nameToPathMap), item);
                } else {
                    try {
                        ObjectNode root = objectMapper.createObjectNode();
                        root.put("value", "*" + item + "*");
                        root.put("case_insensitive", true);

                        String json = objectMapper.writeValueAsString(root);
                        JsonNode node = objectMapper.readTree(json);
                        innerWildcardClause.put(addDataPathToSearchParamKey(key, nameToPathMap), node);
                    } catch (JsonProcessingException e) {
                        log.error("Error while processing wildcard JSON - key: {}", key, e);
                        throw new RuntimeException(e);
                    }
                }

                wildcardClauses.add(wildcardClause);
            }
        } else {
            log.debug("Value is a single object - key: {}", key);

            Map<String, Object> wildcardClause = new HashMap<>();
            wildcardClause.put("wildcard", new HashMap<>());
            Map<String, Object> innerWildcardClause = (Map<String, Object>) wildcardClause.get("wildcard");

            if (key.equals("tenantId")) {
                innerWildcardClause.put(addDataPathToSearchParamKey(key, nameToPathMap), value);
            } else {
                innerWildcardClause.put(addDataPathToSearchParamKey(key, nameToPathMap), "*" + value + "*");
            }

            wildcardClauses.add(wildcardClause);
        }

        log.debug("Wildcard clauses prepared - key: {}, clauseCount: {}", key, wildcardClauses.size());
        return wildcardClauses;
    }


    private String addDataPathToSearchParamKey(String key, Map<String, String> nameToPathMap) {

        String path = nameToPathMap.get(key);

        if (StringUtils.isEmpty(path))
            path = "Data." + key + ".keyword";

        return path;
    }

}
