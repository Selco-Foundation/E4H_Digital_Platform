package org.egov.inbox.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.inbox.config.InboxConfiguration;
import org.egov.inbox.web.model.InboxSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.egov.encryption.EncryptionService;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class ElasticSearchQueryBuilder {


    private ObjectMapper mapper;

    private InboxConfiguration config;

    private EncryptionService encryptionService;

    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;

    @Autowired
    public ElasticSearchQueryBuilder(ObjectMapper mapper, InboxConfiguration config, EncryptionService encryptionService) {
        this.mapper = mapper;
        this.config = config;
        this.encryptionService = encryptionService;
    }


    private static final String BASE_QUERY = "{\n" +
            "  \"from\": {{OFFSET}},\n" +
            "  \"size\": {{LIMIT}},\n" +
            "  \"sort\": {\n" +
            "    \"{{SORT_BY}}\": {\n" +
            "      \"order\": \"{{SORT_ORDER}}\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"query\": {\n" +
            "  }\n" +
            "}";

    private static final String wildCardQueryTemplate = "{\n" +
            "          \"query_string\": {\n" +
            "            \"default_field\": \"{{VAR}}\",\n" +
            "            \"query\": \"*{{PARAM}}*\"\n" +
            "          }\n" +
            "        }";
    private static final String mustClause = "{\n" +
            "            \"terms\": { \"{{VAR}}\" : \n" +
            "            [ \"{{PARAM}}\"]\n" +
            "           }\n" +
            "        }";

    private static final String filterTemplate = "\"filter\": { " +
            " \"bool\":{} \n" +
            "      }";

    private static final String existsClause = "{\n" +
            "            \"field\":  \"{{VAR}}\" \n" +
            "        }";

    private static final String shouldClause1 = "{\n" +
            "            \"bool\": { \n" +
            "            \"{{MUST_CLAUSE}}\" : [ \n" +
            "            { \n " +
            "             \"exists\": \n " +
            existsClause + "  \n" +
            "             },\n" +
            mustClause +
            "           ]\n" +
            "           }\n" +
            "        }";
    private static final String shouldClause2 = "{\n" +
            "            \"bool\": { \n" +
            "            \"{{MUSTNOT_CLAUSE}}\" : [ \n" +
            "            { \n " +
            "             \"exists\": \n " +
            existsClause + "  \n" +
            "             }\n],\n" +
            "            \"{{MUST_CLAUSE}}\" : [ \n" +
            "{\n" +
            "            \"terms\": { \"{{VAR2}}\" : \n" +
            "            [ \"{{PARAM}}\"]\n" +
            "           }\n" +
            "        }" +
            "           ]\n" +
            "           }\n" +
            "        }";


    /**
     * Builds a elasticsearch search query based on the search criteria
     *
     * @param criteria
     * @return
     */
    public String getSearchQuery(InboxSearchCriteria criteria, List<String> ids) {
        log.trace("Method invoked: getSearchQuery");
        String finalQuery;

        try {
            validateCriteria(criteria);
            String baseQuery = buildBaseQueryWithPagination(criteria);
            JsonNode node = mapper.readTree(baseQuery);
            ObjectNode insideMatch = (ObjectNode) node.get("query");
            
            HashMap<String, Object> moduleSearchCriteria = encryptSearchCriteria(criteria);
            List<JsonNode> clauses = buildSearchClauses(moduleSearchCriteria);
            List<JsonNode> mobileClause = buildMobileClause(moduleSearchCriteria);
            
            JsonNode mustNode = buildMustNode(clauses, mobileClause, node);
            mustNode = applyMobileNumberFilterIfNeeded(moduleSearchCriteria, mobileClause, node, mustNode);
            
            insideMatch.put("bool", mustNode);
            ObjectNode boolNode = (ObjectNode) insideMatch.get("bool");
            applyIdFilterIfNeeded(ids, boolNode);

            finalQuery = mapper.writeValueAsString(node);
            log.debug("Search query built successfully");

        } catch (Exception e) {
            log.error("ES_ERROR", e);
            throw new CustomException("JSONNODE_ERROR", "Failed to build json query for fuzzy search");
        }

        return finalQuery;
    }

    private void validateCriteria(InboxSearchCriteria criteria) {
        log.trace("Method invoked: validateCriteria");
        if (criteria == null) {
            log.warn("Criteria is null");
        }
    }

    private String buildBaseQueryWithPagination(InboxSearchCriteria criteria) {
        log.trace("Method invoked: buildBaseQueryWithPagination");
        String baseQuery = addPagination(criteria);
        baseQuery = baseQuery.replace("{{SORT_BY}}", "Data." + criteria.getModuleSearchCriteria().get("sortBy").toString());
        baseQuery = baseQuery.replace("{{SORT_ORDER}}", criteria.getModuleSearchCriteria().get("sortOrder").toString());
        log.debug("Base query with pagination built");
        return baseQuery;
    }

    private HashMap<String, Object> encryptSearchCriteria(InboxSearchCriteria criteria) {
        log.trace("Method invoked: encryptSearchCriteria");
        try {
            if (criteria == null) {
                return null;
            }
            criteria.setModuleSearchCriteria(encryptionService.encryptJson(criteria.getModuleSearchCriteria(), "InboxWnS",centralInstanceUtil.getStateLevelTenant(criteria.getTenantId()), HashMap.class));
            if (criteria == null) {
                throw new CustomException("ENCRYPTION_NULL_ERROR", "Null object found on performing encryption");
            }
            log.debug("Search criteria encrypted successfully");
        } catch (Exception e) {
            log.error("Error occurred while encrypting W&S search criteria", e);
            throw new CustomException("WnS_CRITERIA_ENCRYPTION_ERROR", "Unknown error occurred in encryption process");
        }
        return criteria.getModuleSearchCriteria();
    }

    private List<JsonNode> buildSearchClauses(HashMap<String, Object> moduleSearchCriteria) {
        log.trace("Method invoked: buildSearchClauses");
        List<JsonNode> clauses = new LinkedList<>();
        addClauseIfPresent(moduleSearchCriteria, "locality", "Data.additionalDetails.locality.keyword", clauses);
        addClauseIfPresent(moduleSearchCriteria, "applicationNumber", "Data.applicationNo.keyword", clauses);
        addClauseIfPresent(moduleSearchCriteria, "applicationType", "Data.applicationType.keyword", clauses);
        addClauseIfPresent(moduleSearchCriteria, "consumerNo", "Data.connectionNo.keyword", clauses);
        addClauseIfPresent(moduleSearchCriteria, "propertyId", "Data.propertyId.keyword", clauses);
        addClauseIfPresent(moduleSearchCriteria, "applicationStatus", "Data.applicationStatus.keyword", clauses);
        addClauseIfPresent(moduleSearchCriteria, "assignee", "Data.workflow.assignes.keyword", clauses);
        addClauseIfPresent(moduleSearchCriteria, "businessService", "Data.history.businessService.keyword", clauses);
        log.debug("Search clauses built - clauseCount: {}", clauses.size());
        return clauses;
    }

    private void addClauseIfPresent(HashMap<String, Object> moduleSearchCriteria, String key, String fieldPath, List<JsonNode> clauses) {
        log.trace("Method invoked: addClauseIfPresent - key: {}", key);
        if (!StringUtils.isEmpty(moduleSearchCriteria.get(key))) {
            try {
                clauses.add(getInnerNode(moduleSearchCriteria.get(key).toString(), fieldPath));
                log.debug("Clause added - key: {}, fieldPath: {}", key, fieldPath);
            } catch (Exception e) {
                log.error("Error adding clause for key: {}", key, e);
            }
        }
    }

    private List<JsonNode> buildMobileClause(HashMap<String, Object> moduleSearchCriteria) {
        log.trace("Method invoked: buildMobileClause");
        List<JsonNode> mobileClause = new LinkedList<>();
        if (!StringUtils.isEmpty(moduleSearchCriteria.get("mobileNumber"))) {
            try {
                mobileClause.add(getInnerNodeForMobileNumber(moduleSearchCriteria.get("mobileNumber").toString(), "Data.connectionHolders.mobileNumber.keyword", "Data.ownerMobileNumbers.keyword"));
                log.debug("Mobile clause built");
            } catch (Exception e) {
                log.error("Error building mobile clause", e);
            }
        }
        return mobileClause;
    }

    private JsonNode buildMustNode(List<JsonNode> clauses, List<JsonNode> mobileClause, JsonNode node) {
        log.trace("Method invoked: buildMustNode");
        JsonNode mustNode = mapper.convertValue(new HashMap<String, List<JsonNode>>() {{
            put("must", clauses);
        }}, JsonNode.class);
        log.debug("Must node built - clauseCount: {}", clauses.size());
        return mustNode;
    }

    private JsonNode applyMobileNumberFilterIfNeeded(HashMap<String, Object> moduleSearchCriteria, List<JsonNode> mobileClause, JsonNode node, JsonNode mustNode) {
        log.trace("Method invoked: applyMobileNumberFilterIfNeeded");
        if (!StringUtils.isEmpty(moduleSearchCriteria.get("mobileNumber"))) {
            try {
                ObjectNode insideNode = (ObjectNode) node.get("query");
                insideNode.put("bool", mobileClause.get(0));
                mobileClause.remove(0);
                ObjectNode parentNode = mapper.createObjectNode();
                parentNode.put("filter", insideNode);
                JsonNode mergedNode = merge(mustNode, (JsonNode) new ObjectMapper().readTree(parentNode.toString()));
                log.debug("Mobile number filter applied");
                return mergedNode;
            } catch (Exception e) {
                log.error("Error applying mobile number filter", e);
            }
        }
        return mustNode;
    }

    private void applyIdFilterIfNeeded(List<String> ids, ObjectNode boolNode) {
        log.trace("Method invoked: applyIdFilterIfNeeded");
        if (!CollectionUtils.isEmpty(ids)) {
            try {
                JsonNode jsonNode = mapper.convertValue(new HashMap<String, List<String>>() {{
                    put("Data.id.keyword", ids);
                }}, JsonNode.class);
                ObjectNode parentNode = mapper.createObjectNode();
                parentNode.put("terms", jsonNode);
                boolNode.put("filter", parentNode);
                log.debug("ID filter applied - idCount: {}", ids.size());
            } catch (Exception e) {
                log.error("Error applying ID filter", e);
            }
        }
    }


    /**
     * Creates inner query using the query template
     *
     * @param param
     * @param var
     * @return
     * @throws JsonProcessingException
     */
    private JsonNode getInnerNode(String param, String var) throws JsonProcessingException {

        String template;
        template = mustClause;

        String innerQuery = new String();
        if (param.contains(",")) {
            String[] splitted = param.split(",");
            StringBuilder stringArray = new StringBuilder();
            for (int i = 0; i < splitted.length; i++) {
                if (i < splitted.length - 1) {
                    if (i == 0)
                        stringArray.append("" + splitted[i].trim() + "\",");
                    else
                        stringArray.append("\"" + splitted[i].trim() + "\",");
                } else {
                    stringArray.append("\"" + splitted[i].trim() + "");
                }
            }

            param = stringArray.toString();
            innerQuery = template.replace("{{PARAM}}", param);
        } else
            innerQuery = template.replace("{{PARAM}}", param);
        innerQuery = innerQuery.replace("{{VAR}}", var);


        JsonNode innerNode = mapper.readTree(innerQuery);
        return innerNode;
    }

    /**
     * Creates inner query using the query template for mobileNumber search in WnS module
     *
     * @param param
     * @param var
     * @param var2
     * @return
     * @throws JsonProcessingException
     */
    private JsonNode getInnerNodeForMobileNumber(String param, String var, String var2) throws JsonProcessingException {
        List<JsonNode> mobileClause = new LinkedList<>();
        String template = shouldClause1;
        String template2 = shouldClause2;
        String innerQuery = new String();
        String innerQuery2 = new String();
        innerQuery = template.replace("{{MUST_CLAUSE}}", "must");
        innerQuery = innerQuery.replace("{{PARAM}}", param);
        innerQuery = innerQuery.replace("{{VAR}}", var);

        innerQuery2 = template2.replace("{{MUSTNOT_CLAUSE}}", "must_not");
        innerQuery2 = innerQuery2.replace("{{MUST_CLAUSE}}", "must");
        innerQuery2 = innerQuery2.replace("{{PARAM}}", param);
        innerQuery2 = innerQuery2.replace("{{VAR}}", var);
        innerQuery2 = innerQuery2.replace("{{VAR2}}", var2);

        JsonNode innerNode = mapper.readTree(innerQuery);
        mobileClause.add(innerNode);
        mobileClause.add(mapper.readTree(innerQuery2));
        JsonNode mobileClauseNode = mapper.convertValue(new HashMap<String, List<JsonNode>>() {{
            put("should", mobileClause);
        }}, JsonNode.class);

        return mobileClauseNode;
    }

    /**
     * Adds pagination
     *
     * @param criteria
     * @return baseQuery with pagination
     */
    private String addPagination(InboxSearchCriteria criteria) {
        Long limit = config.getDefaultLimit();
        Long offset = config.getDefaultOffset();

        if (criteria.getLimit() != null && criteria.getLimit() <= config.getMaxSearchLimit())
            limit = Long.valueOf(criteria.getLimit());

        if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit())
            limit = Long.valueOf(config.getMaxSearchLimit());

        if (criteria.getOffset() != null)
            offset = Long.valueOf(criteria.getOffset());

        String baseQuery = BASE_QUERY.replace("{{OFFSET}}", offset.toString());
        baseQuery = baseQuery.replace("{{LIMIT}}", limit.toString());

        return baseQuery;
    }

    /**
     * Escapes special characters in given string
     *
     * @param inputString
     * @return
     */
    private String getEscapedString(String inputString) {
        final String[] metaCharacters = {"\\", "/", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+", "?", "|", "<", ">", "-", "&", "%"};
        for (int i = 0; i < metaCharacters.length; i++) {
            if (inputString.contains(metaCharacters[i])) {
                inputString = inputString.replace(metaCharacters[i], "\\\\" + metaCharacters[i]);
            }
        }
        return inputString;
    }

    /**
     * Merges 2 JSONNodes
     *
     * @param mainNode
     * @param updateNode
     * @return JsonNode
     */
    public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {

        Iterator<String> fieldNames = updateNode.fieldNames();

        while (fieldNames.hasNext()) {
            String updatedFieldName = fieldNames.next();
            JsonNode valueToBeUpdated = mainNode.get(updatedFieldName);
            JsonNode updatedValue = updateNode.get(updatedFieldName);

            // If the node is an @ArrayNode
            if (valueToBeUpdated != null && valueToBeUpdated.isArray() &&
                    updatedValue.isArray()) {
                // running a loop for all elements of the updated ArrayNode
                for (int i = 0; i < updatedValue.size(); i++) {
                    JsonNode updatedChildNode = updatedValue.get(i);
                    // Create a new Node in the node that should be updated, if there was no corresponding node in it
                    // Use-case - where the updateNode will have a new element in its Array
                    if (valueToBeUpdated.size() <= i) {
                        ((ArrayNode) valueToBeUpdated).add(updatedChildNode);
                    }
                    // getting reference for the node to be updated
                    JsonNode childNodeToBeUpdated = valueToBeUpdated.get(i);
                    merge(childNodeToBeUpdated, updatedChildNode);
                }
                // if the Node is an @ObjectNode
            } else if (valueToBeUpdated != null && valueToBeUpdated.isObject()) {
                merge(valueToBeUpdated, updatedValue);
            } else {
                if (mainNode instanceof ObjectNode) {
                    ((ObjectNode) mainNode).replace(updatedFieldName, updatedValue);
                }
            }
        }
        return mainNode;
    }

}