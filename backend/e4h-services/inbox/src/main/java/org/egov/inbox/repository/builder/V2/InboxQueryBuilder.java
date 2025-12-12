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
        log.info("➡️ Building ES query | tenantId='{}' | module='{}' | pagination={} | SLA={}",
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName(),
                isPaginationRequired, isSLA);

        InboxQueryConfiguration configuration = mdmsUtil.getConfigFromMDMS(
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());
        log.debug("✅ Loaded configuration: allowedCriteria={}, sortParam={}",
                configuration.getAllowedSearchCriteria(), configuration.getSortParam());

        Map<String, Object> params = inboxRequest.getInbox().getModuleSearchCriteria();
        Map<String, Object> jurisdictionParams = inboxRequest.getInbox().getJurisdictionSearchCriteria();
        Map<String, Object> baseEsQuery = getBaseESQueryBody(inboxRequest, isPaginationRequired);
        log.debug("📝 Base ES query initialized: {}", baseEsQuery);

        if (isPaginationRequired) {
            String sortClauseFieldPath = configuration.getSortParam().getPath();
            SortParam.Order sortOrder = params.containsKey(SORT_ORDER_CONSTANT)
                    ? SortParam.Order.valueOf((String) params.get(SORT_ORDER_CONSTANT))
                    : configuration.getSortParam().getOrder();
            addSortClauseToBaseQuery(baseEsQuery, sortClauseFieldPath, sortOrder);
            log.debug("🔃 Added sort clause: field='{}', order='{}'", sortClauseFieldPath, sortOrder);

            List<String> sourceFilterPathList = configuration.getSourceFilterPathList();
            addSourceFilterToBaseQuery(baseEsQuery, sourceFilterPathList);
            log.debug("📌 Added source filter: {}", sourceFilterPathList);
        }

        Map<String, Object> innerBoolClause =
                (HashMap<String, Object>) ((HashMap<String, Object>) baseEsQuery.get(QUERY_KEY)).get(BOOL_KEY);
        List<Object> mustClauseList = (ArrayList<Object>) innerBoolClause.get(MUST_KEY);
        List<Object> jurisdictionMustClauseList = new ArrayList<Object>();

        Map<String, String> nameToPathMap = new HashMap<>();
        Map<String, SearchParam.Operator> nameToOperator = new HashMap<>();

        configuration.getAllowedSearchCriteria().forEach(searchParam -> {
            nameToPathMap.put(searchParam.getName(), searchParam.getPath());
            nameToOperator.put(searchParam.getName(), searchParam.getOperator());
        });
        log.debug("⚙️ Search criteria mappings: {}", nameToPathMap);

        // Special case for tenantId
        if (inboxRequest.getInbox().getProcessSearchCriteria().getTenantId().split("\\.").length == 1
                && !inboxRequest.getInbox().getModuleSearchCriteria().get("tenantId").toString().contains(",")) {
            nameToOperator.put("tenantId", SearchParam.Operator.WILDCARD);
            log.debug("🔍 Applied wildcard operator for tenantId");
        }

        addModuleSearchCriteriaToBaseQuery(params, nameToPathMap, nameToOperator, mustClauseList);
        log.debug("📥 Added module search criteria to mustClauseList");

        addJurisdictionSearchCriteriaToBaseQuery(jurisdictionParams, nameToPathMap, nameToOperator, jurisdictionMustClauseList);
        log.debug("📥 Added jurisdiction search criteria to mustClauseList");

        addProcessSearchCriteriaToBaseQuery(inboxRequest.getInbox().getProcessSearchCriteria(), nameToPathMap, nameToOperator, mustClauseList);
        log.debug("📥 Added process search criteria to mustClauseList");

        log.info("Final must clause list {} ", mustClauseList);
        log.info("Final jurisdiction must clause list {} ", jurisdictionMustClauseList);

        // Group the different blocks of should into a single should block
        List<Map<String, Object>> updatedMustClauseList = extractShouldClauses(mustClauseList);
        List<Map<String, Object>> updatedJurisdictionMustClauseList = extractJurisdictionShouldClauses(jurisdictionMustClauseList);
        List<Map<String, Object>> mergedMustClause = mergeMustClauseLists(updatedJurisdictionMustClauseList, updatedMustClauseList);
        log.info("Final must clause list after conversion {} ", updatedMustClauseList);
        log.info("Final jurisdiction must clause list after conversion {} ", updatedJurisdictionMustClauseList);
        log.info("Final merge must clause list {} ", updatedJurisdictionMustClauseList);

        innerBoolClause.put(MUST_KEY, mergedMustClause);

        // Add SLA filter if required
        if (params.containsKey("nearingSLA") && isSLA) {
            log.info("⏳ Applying SLA filter (nearingSLA enabled)");

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
            log.debug("🚫 Added SLA exclusions: terminated tickets + Incident service");

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
            log.debug("⏱️ Added SLA painless script filter");
        }

        log.info("✅ ES query built successfully for tenantId='{}' | module='{}'",
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());
        log.info("📄 Final ES query: {}", baseEsQuery);

        return baseEsQuery;
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
        String tenantId = inboxRequest.getInbox().getTenantId();
        String moduleName = inboxRequest.getInbox().getProcessSearchCriteria().getModuleName();

        log.info("➡️ Starting ES query build | tenantId={} | module={}", tenantId, moduleName);

        // Récupération de la configuration depuis MDMS
        InboxQueryConfiguration configuration = mdmsUtil.getConfigFromMDMS(tenantId, moduleName);
        log.debug("📄 MDMS configuration fetched: {}", configuration);

        Map<String, Object> params = inboxRequest.getInbox().getModuleSearchCriteria();
        Map<String, Object> baseEsQuery = getBaseESQueryBody(inboxRequest, isPaginationRequired);
        log.debug("🔹 Base ES query body initialized");

        if (isPaginationRequired) {
            // Sort
            String sortClauseFieldPath = configuration.getSortParam().getPath();
            SortParam.Order sortOrder = params.containsKey(SORT_ORDER_CONSTANT)
                    ? SortParam.Order.valueOf((String) params.get(SORT_ORDER_CONSTANT))
                    : configuration.getSortParam().getOrder();

            log.debug("📌 Adding sort clause | field={} | order={}", sortClauseFieldPath, sortOrder);
            addSortClauseToBaseQuery(baseEsQuery, sortClauseFieldPath, sortOrder);

            // Source filter
            List<String> sourceFilterPathList = configuration.getSourceFilterPathList();
            log.debug("📌 Adding source filter paths: {}", sourceFilterPathList);
            addSourceFilterToBaseQuery(baseEsQuery, sourceFilterPathList);
        }

        // Construction des clauses booléennes
        Map<String, Object> innerBoolClause = (HashMap<String, Object>) ((HashMap<String, Object>) baseEsQuery.get(QUERY_KEY)).get(BOOL_KEY);
        List<Object> mustClauseList = (ArrayList<Object>) innerBoolClause.get(MUST_KEY);

        Map<String, String> nameToPathMap = new HashMap<>();
        Map<String, SearchParam.Operator> nameToOperator = new HashMap<>();

        configuration.getAllowedSearchCriteria().forEach(searchParam -> {
            nameToPathMap.put(searchParam.getName(), searchParam.getPath());
            nameToOperator.put(searchParam.getName(), searchParam.getOperator());
        });

        log.debug("🗺 Name to path map: {}", nameToPathMap);
        log.debug("⚙ Name to operator map: {}", nameToOperator);

        log.info("🔹 Adding module search criteria to ES query");
        addModuleSearchCriteriaToBaseQuery(params, nameToPathMap, nameToOperator, mustClauseList);

        log.info("🔹 Adding process search criteria to ES query");
        addProcessSearchCriteriaToBaseQuery(inboxRequest.getInbox().getProcessSearchCriteria(), nameToPathMap, nameToOperator, mustClauseList);

        innerBoolClause.put(MUST_KEY, mustClauseList);

        log.info("✅ ES query build completed | tenantId={} | module={}", tenantId, moduleName);
        log.debug("📄 Final ES query: {}", baseEsQuery);

        return baseEsQuery;
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
        if (!ObjectUtils.isEmpty(processSearchCriteria.getStatus())) {
            String key = "status";
            Map<String, Object> mustClauseChild = null;
            Map<String, Object> params = new HashMap<>();

            processSearchCriteria.getStatus().removeAll(Collections.singleton(null));
            params.put(key, processSearchCriteria.getStatus());
            mustClauseChild = (Map<String, Object>) prepareMustClauseChild(params, key, nameToPathMap, nameToOperator);
            if (CollectionUtils.isEmpty(mustClauseChild)) {
                log.info("Error occurred while preparing filter for must clause. Filter for key " + key + " will not be added.");
            } else {
                mustClauseList.add(mustClauseChild);
            }
        }

        if (!ObjectUtils.isEmpty(processSearchCriteria.getAssignee())) {
            String key = "assignee";
            Map<String, Object> mustClauseChild = null;
            Map<String, Object> params = new HashMap<>();
            params.put(key, processSearchCriteria.getAssignee());
            mustClauseChild = (Map<String, Object>) prepareMustClauseChild(params, key, nameToPathMap, nameToOperator);
            if (CollectionUtils.isEmpty(mustClauseChild)) {
                log.info("Error occurred while preparing filter for must clause. Filter for key " + key + " will not be added.");
            } else {
                mustClauseList.add(mustClauseChild);
            }
        }

        if (!ObjectUtils.isEmpty(processSearchCriteria.getFromDate())) {
            String key = "fromDate";
            Map<String, Object> mustClauseChild = null;
            Map<String, Object> params = new HashMap<>();
            params.put(key, processSearchCriteria.getFromDate());
            mustClauseChild = (Map<String, Object>) prepareMustClauseChild(params, key, nameToPathMap, nameToOperator);
            if (CollectionUtils.isEmpty(mustClauseChild)) {
                log.info("Error occurred while preparing filter for must clause. Filter for key " + key + " will not be added.");
            } else {
                mustClauseList.add(mustClauseChild);
            }
        }

        if (!ObjectUtils.isEmpty(processSearchCriteria.getToDate())) {
            String key = "toDate";
            Map<String, Object> mustClauseChild = null;
            Map<String, Object> params = new HashMap<>();
            params.put(key, processSearchCriteria.getToDate());
            mustClauseChild = (Map<String, Object>) prepareMustClauseChild(params, key, nameToPathMap, nameToOperator);
            if (CollectionUtils.isEmpty(mustClauseChild)) {
                log.info("Error occurred while preparing filter for must clause. Filter for key " + key + " will not be added.");
            } else {
                mustClauseList.add(mustClauseChild);
            }
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
        log.debug("🔹 Preparing must clause for key='{}' with operator='{}' and value='{}'", key, operator, params.get(key));

        if (operator == null || operator.equals(SearchParam.Operator.EQUAL)) {
            if (params.get(key) instanceof List) {
                log.debug("📌 Adding TERMS clause for key='{}' with values={}", key, params.get(key));
                Map<String, Object> termsClause = new HashMap<>();
                termsClause.put("terms", new HashMap<>());
                Map<String, Object> innerTermsClause = (Map<String, Object>) termsClause.get("terms");
                innerTermsClause.put(addDataPathToSearchParamKey(key, nameToPathMap), params.get(key));
                return termsClause;
            } else {
                log.debug("📌 Adding TERM clause for key='{}' with value={}", key, params.get(key));
                Map<String, Object> termClause = new HashMap<>();
                termClause.put("term", new HashMap<>());
                Map<String, Object> innerTermClause = (Map<String, Object>) termClause.get("term");
                innerTermClause.put(addDataPathToSearchParamKey(key, nameToPathMap), params.get(key));
                return termClause;
            }
        } else if (operator.equals(SearchParam.Operator.LTE) || operator.equals(SearchParam.Operator.GTE)) {
            log.debug("📌 Adding RANGE clause for key='{}' with operator='{}' and value={}", key, operator, params.get(key));
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
            log.debug("📌 Adding MUST_NOT clause for key='{}' with values={}", key, params.get(key));
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
            log.debug("📌 Adding SHOULD clause for key='{}' with values={}", key, params.get(key));
            Map<String, Object> shouldClause = new HashMap<>();
            shouldClause.put("should", new HashMap<>());
            Map<String, Object> termsClause = (Map<String, Object>) shouldClause.get("should");
            termsClause.put("terms", new HashMap<>());
            Map<String, Object> innerShouldClause = (Map<String, Object>) termsClause.get("terms");
            innerShouldClause.put(addDataPathToSearchParamKey(key, nameToPathMap), params.get(key));
            log.info("SHOULD request : {}", shouldClause);
            return shouldClause;
        } else if (operator.equals(SearchParam.Operator.SLA_COMPARE)) {
            log.debug("📌 SLA_COMPARE operator detected for key='{}', returning empty clause", key);
            return new HashMap<>();
        } else if (operator.equals(SearchParam.Operator.MULTI_MATCH)) {
            String searchValue = params.get("search").toString();
            log.debug("📌 Adding MULTI_MATCH clause for search='{}'", searchValue);
            Map<String, Object> multiMatch = new HashMap<>();
            multiMatch.put("query", searchValue);
            multiMatch.put("fields", nameToPathMap.get("search").split(","));
            multiMatch.put("fuzziness", 2);
            Map<String, Object> parent = new HashMap<>();
            parent.put("multi_match", multiMatch);
            return parent;
        } else {
            log.error("❌ Unsupported operator '{}' for key='{}'", operator, key);
            throw new CustomException(ErrorConstants.INVALID_OPERATOR_DATA, "Unsupported Operator : " + operator);
        }
    }

    private List<Map<String, Object>> prepareMustClauseWildCardChild(Map<String, Object> params, String key,
                                                                     Map<String, String> nameToPathMap,
                                                                     Map<String, SearchParam.Operator> nameToOperatorMap) {
        Object value = params.get(key);
        log.debug("🔹 Preparing wildcard clause for key='{}' with value='{}'", key, value);

        List<Map<String, Object>> wildcardClauses = new ArrayList<>();

        if (value instanceof List) {
            List<Object> values = (List<Object>) value;
            log.debug("📌 Value is a list with {} items for key='{}'", values.size(), key);

            for (Object item : values) {
                log.debug("   🔸 Adding wildcard for list item='{}'", item);

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
                        log.error("❌ Error while processing wildcard JSON for key='{}' and item='{}'", key, item, e);
                        throw new RuntimeException(e);
                    }
                }

                wildcardClauses.add(wildcardClause);
            }
        } else {
            log.debug("📌 Value is a single object for key='{}', value='{}'", key, value);

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

        log.debug("✅ Total {} wildcard clause(s) prepared for key='{}'", wildcardClauses.size(), key);
        return wildcardClauses;
    }


    private String addDataPathToSearchParamKey(String key, Map<String, String> nameToPathMap) {

        String path = nameToPathMap.get(key);

        if (StringUtils.isEmpty(path))
            path = "Data." + key + ".keyword";

        return path;
    }

}
