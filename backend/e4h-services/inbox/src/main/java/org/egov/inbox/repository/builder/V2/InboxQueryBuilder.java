package org.egov.inbox.repository.builder.V2;

import java.util.*;

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

        InboxQueryConfiguration configuration = mdmsUtil.getConfigFromMDMS(
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());

        Map<String, Object> params = inboxRequest.getInbox().getModuleSearchCriteria();
        Map<String, Object> baseEsQuery = getBaseESQueryBody(inboxRequest, isPaginationRequired);

        if (isPaginationRequired) {
            // Adds sort clause to the inbox ES query only in case pagination is present, else not
            String sortClauseFieldPath = configuration.getSortParam().getPath();
            SortParam.Order sortOrder = inboxRequest.getInbox().getModuleSearchCriteria().containsKey(SORT_ORDER_CONSTANT)
                    ? SortParam.Order.valueOf((String) inboxRequest.getInbox().getModuleSearchCriteria().get(SORT_ORDER_CONSTANT))
                    : configuration.getSortParam().getOrder();
            addSortClauseToBaseQuery(baseEsQuery, sortClauseFieldPath, sortOrder);

            // Adds source filter only when requesting for inbox items.
            List<String> sourceFilterPathList = configuration.getSourceFilterPathList();
            addSourceFilterToBaseQuery(baseEsQuery, sourceFilterPathList);
        }

        Map<String, Object> innerBoolClause =
                (HashMap<String, Object>) ((HashMap<String, Object>) baseEsQuery.get(QUERY_KEY)).get(BOOL_KEY);
        List<Object> mustClauseList = (ArrayList<Object>) innerBoolClause.get(MUST_KEY);

        Map<String, String> nameToPathMap = new HashMap<>();
        Map<String, SearchParam.Operator> nameToOperator = new HashMap<>();

        configuration.getAllowedSearchCriteria().forEach(searchParam -> {
            nameToPathMap.put(searchParam.getName(), searchParam.getPath());
            nameToOperator.put(searchParam.getName(), searchParam.getOperator());
        });

        if (inboxRequest.getInbox().getProcessSearchCriteria().getTenantId().split("\\.").length == 1
                && !inboxRequest.getInbox().getModuleSearchCriteria().get("tenantId").toString().contains(",")) {
            nameToOperator.put("tenantId", SearchParam.Operator.WILDCARD);

        }
        addModuleSearchCriteriaToBaseQuery(params, nameToPathMap, nameToOperator, mustClauseList);
        addProcessSearchCriteriaToBaseQuery(inboxRequest.getInbox().getProcessSearchCriteria(), nameToPathMap, nameToOperator, mustClauseList);

        innerBoolClause.put(MUST_KEY, mustClauseList);

        //add filter for inbox SLA
        if (inboxRequest.getInbox().getModuleSearchCriteria().containsKey("nearingSLA") && isSLA) {

            Map<String, Object> query = (Map<String, Object>) baseEsQuery.get("query");
            Map<String, Object> boolClause = (Map<String, Object>) query.get("bool");

            List<Map<String, Object>> mustNotClauseList = (List<Map<String, Object>>) boolClause.getOrDefault("must_not", new ArrayList<>());

            // Exclude terminated tickets
            Map<String, Object> terminateClause = new HashMap<>();
            terminateClause.put("term", Collections.singletonMap("Data.currentProcessInstance.state.isTerminateState", true));
            mustNotClauseList.add(terminateClause);

            // Exclude businessService = 'Incident'
            Map<String, Object> excludeIncidentTerm = new HashMap<>();
            excludeIncidentTerm.put("term", Collections.singletonMap("Data.currentProcessInstance.businessService.keyword", "Incident"));
            mustNotClauseList.add(excludeIncidentTerm);

            boolClause.put("must_not", mustNotClauseList);

            // Add nearing SLA painless script
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
        }


        return baseEsQuery;

    }

    public Map<String, Object> getESQueryProject(InboxRequest inboxRequest, Boolean isPaginationRequired) {

        InboxQueryConfiguration configuration = mdmsUtil.getConfigFromMDMS(
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());

        Map<String, Object> params = inboxRequest.getInbox().getModuleSearchCriteria();
        Map<String, Object> baseEsQuery = getBaseESQueryBody(inboxRequest, isPaginationRequired);

        if (isPaginationRequired) {
            // Adds sort clause to the inbox ES query only in case pagination is present, else not
            String sortClauseFieldPath = configuration.getSortParam().getPath();
            SortParam.Order sortOrder = inboxRequest.getInbox().getModuleSearchCriteria().containsKey(SORT_ORDER_CONSTANT)
                    ? SortParam.Order.valueOf((String) inboxRequest.getInbox().getModuleSearchCriteria().get(SORT_ORDER_CONSTANT))
                    : configuration.getSortParam().getOrder();
            addSortClauseToBaseQuery(baseEsQuery, sortClauseFieldPath, sortOrder);

            // Adds source filter only when requesting for inbox items.
            List<String> sourceFilterPathList = configuration.getSourceFilterPathList();
            addSourceFilterToBaseQuery(baseEsQuery, sourceFilterPathList);
        }

        Map<String, Object> innerBoolClause =
                (HashMap<String, Object>) ((HashMap<String, Object>) baseEsQuery.get(QUERY_KEY)).get(BOOL_KEY);
        List<Object> mustClauseList = (ArrayList<Object>) innerBoolClause.get(MUST_KEY);

        Map<String, String> nameToPathMap = new HashMap<>();
        Map<String, SearchParam.Operator> nameToOperator = new HashMap<>();

        configuration.getAllowedSearchCriteria().forEach(searchParam -> {
            nameToPathMap.put(searchParam.getName(), searchParam.getPath());
            nameToOperator.put(searchParam.getName(), searchParam.getOperator());
        });

//        if (inboxRequest.getInbox().getProcessSearchCriteria().getTenantId().split("\\.").length == 1
//                && !inboxRequest.getInbox().getModuleSearchCriteria().get("tenantId").toString().contains(",")) {
//            nameToOperator.put("tenantId", SearchParam.Operator.WILDCARD);
//
//        }
        addModuleSearchCriteriaToBaseQuery(params, nameToPathMap, nameToOperator, mustClauseList);
        addProcessSearchCriteriaToBaseQuery(inboxRequest.getInbox().getProcessSearchCriteria(), nameToPathMap, nameToOperator, mustClauseList);

        innerBoolClause.put(MUST_KEY, mustClauseList);

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
//        if(!ObjectUtils.isEmpty(processSearchCriteria.getTenantId())){
//            String key = "tenantId";
//            Map<String, Object> mustClauseChild = null;
//        	List<Map<String, Object>> mustClauseChilds = null;
//
//            Map<String, Object> params = new HashMap<>();
//            params.put(key, processSearchCriteria.getTenantId());
//            if(processSearchCriteria.getTenantId().split("\\.").length==1)
//            {
//
//			mustClauseChilds = (List<Map<String, Object>>) prepareMustClauseWildCardChild(params, key,
//					nameToPathMap, nameToOperator);
//			 if(CollectionUtils.isEmpty(mustClauseChilds)){
//	                log.info("Error occurred while preparing filter for must clause. Filter for key " + key + " will not be added.");
//	            }else {
//	                mustClauseList.add(mustClauseChilds);
//	            }
//            }
//            
//            else
//            {
//                mustClauseChild = (Map<String, Object>) prepareMustClauseChild(params, key, nameToPathMap, nameToOperator);
//                if(CollectionUtils.isEmpty(mustClauseChild)){
//	                log.info("Error occurred while preparing filter for must clause. Filter for key " + key + " will not be added.");
//	            }else {
//	                mustClauseList.add(mustClauseChild);
//	            }
//            }
//
//           
//        }

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
        if (operator == null || operator.equals(SearchParam.Operator.EQUAL)) {
            // Add terms clause in case the search criteria has a list of values
            if (params.get(key) instanceof List) {
                Map<String, Object> termsClause = new HashMap<>();
                termsClause.put("terms", new HashMap<>());
                Map<String, Object> innerTermsClause = (Map<String, Object>) termsClause.get("terms");
                innerTermsClause.put(addDataPathToSearchParamKey(key, nameToPathMap), params.get(key));
                return termsClause;
            }
            // Add term clause in case the search criteria has a single value
            else {
                Map<String, Object> termClause = new HashMap<>();
                termClause.put("term", new HashMap<>());
                Map<String, Object> innerTermClause = (Map<String, Object>) termClause.get("term");
                innerTermClause.put(addDataPathToSearchParamKey(key, nameToPathMap), params.get(key));
                return termClause;
            }
        } else if (operator.equals(SearchParam.Operator.LTE) || operator.equals(SearchParam.Operator.GTE)) {
            return new HashMap<>();
        } else if (operator.equals(SearchParam.Operator.SLA_COMPARE)) {
            return new HashMap<>();
        } else if (operator.equals(SearchParam.Operator.MULTI_MATCH)) {
            String searchValue = params.get("search").toString();
            Map<String, Object> multiMatch = new HashMap<>();
            multiMatch.put("query", searchValue);
            multiMatch.put("fields", nameToPathMap.get("search").split(","));
            multiMatch.put("fuzziness", 2);
            Map<String, Object> parent = new HashMap<>();
            parent.put("multi_match", multiMatch);
            return parent;
        } else
            throw new CustomException(ErrorConstants.INVALID_OPERATOR_DATA, " Unsupported Operator : " + operator);

    }

    private List<Map<String, Object>> prepareMustClauseWildCardChild(Map<String, Object> params, String key,
                                                                     Map<String, String> nameToPathMap, Map<String, SearchParam.Operator> nameToOperatorMap) {
        // Add wildcard clause in case the search criteria has a list of values
        Object value = params.get(key);
        List<Map<String, Object>> wildcardClauses = new ArrayList<>();
        if (value instanceof List) {
            List<Object> values = (List<Object>) value;
            for (Object item : values) {
                Map<String, Object> wildcardClause = new HashMap<>();
                wildcardClause.put("wildcard", new HashMap<>());
                Map<String, Object> innerWildcardClause = (Map<String, Object>) wildcardClause.get("wildcard");
                if(key.equals("tenantId")) {
                    innerWildcardClause.put(addDataPathToSearchParamKey(key, nameToPathMap), item + ".*");
                }
                else{
                    try {
                        ObjectNode root = objectMapper.createObjectNode();
                        root.put("value", "*" + item + "*");
                        root.put("case_insensitive", true);

                        String json = objectMapper.writeValueAsString(root);
                        JsonNode node = objectMapper.readTree(json);
                        innerWildcardClause.put(addDataPathToSearchParamKey(key, nameToPathMap),  node);
//                      innerWildcardClause.put(addDataPathToSearchParamKey(key, nameToPathMap),  "*" + item + "*");
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
                wildcardClauses.add(wildcardClause);
            }

            return wildcardClauses;
        } else {
            Map<String, Object> wildcardClause = new HashMap<>();
            wildcardClause.put("wildcard", new HashMap<>());
            Map<String, Object> innerWildcardClause = (Map<String, Object>) wildcardClause.get("wildcard");
            if(key.equals("tenantId")) {
                innerWildcardClause.put(addDataPathToSearchParamKey(key, nameToPathMap), value + ".*");
            }
            else{
                innerWildcardClause.put(addDataPathToSearchParamKey(key, nameToPathMap), "*" + value + "*");
            }
            wildcardClauses.add(wildcardClause);
            return wildcardClauses;
        }
    }

    private String addDataPathToSearchParamKey(String key, Map<String, String> nameToPathMap) {

        String path = nameToPathMap.get(key);

        if (StringUtils.isEmpty(path))
            path = "Data." + key + ".keyword";

        return path;
    }

}
