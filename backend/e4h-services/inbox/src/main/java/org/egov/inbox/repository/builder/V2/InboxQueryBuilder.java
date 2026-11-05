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
import org.egov.inbox.util.RoleJurisdictionScope;
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

    @Autowired
    private RoleJurisdictionScope roleJurisdictionScope;


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

            List<String> sourceFilterPathList = withSlaSourceFields(configuration.getSourceFilterPathList());
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

        log.debug("Adding jurisdiction scope to must clause");
        jurisdictionMustClauseList.addAll(
                buildJurisdictionClauses(inboxRequest, jurisdictionParams, nameToPathMap));
        log.debug("Jurisdiction scope added - jurisdictionMustClauseSize: {}", jurisdictionMustClauseList.size());

        addProcessSearchCriteriaToBaseQuery(inboxRequest.getInbox().getProcessSearchCriteria(), nameToPathMap, nameToOperator, mustClauseList);
        log.debug("📥 Added process search criteria to mustClauseList");

        log.info("Final must clause list {} ", mustClauseList);
        log.info("Final jurisdiction must clause list {} ", jurisdictionMustClauseList);

        // Group the different blocks of should into a single should block
        List<Map<String, Object>> updatedMustClauseList = extractShouldClauses(mustClauseList);
        List<Map<String, Object>> jurisdictionClauses = jurisdictionMustClauseList.stream()
                .filter(o -> o instanceof Map)
                .map(o -> (Map<String, Object>) o)
                .collect(Collectors.toList());
        List<Map<String, Object>> mergedMustClause = new ArrayList<>(updatedMustClauseList);
        mergedMustClause.addAll(jurisdictionClauses);
        log.debug("Must clauses merged - mergedSize: {}", mergedMustClause.size());

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

            mustClauseList.add(getNearingSlaClause());
            log.debug("⏱️ Added nearing SLA filter");
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

    /**
     * The jurisdiction filter: what the caller is allowed to see, then what they asked to see.
     *
     * Two separate must clauses, so Elasticsearch does the intersecting. A caller holding
     * STATE_1 and STATE_2 who narrows to STATE_1 gets {@code stateCode IN (S1,S2)} AND
     * {@code stateCode IN (S1)} - STATE_1 only. Narrowing to a boundary they do not hold yields
     * nothing, which is the right failure: the client can shrink the result, never widen it.
     *
     * This replaces {@code extractJurisdictionShouldClauses}, which merged every jurisdiction
     * clause into one flat {@code should} built entirely from the request body. The body is still
     * read, but only as the narrowing request - the scope itself now comes from HRMS.
     *
     * Each grant is matched at its own level, which works because every incident is indexed with
     * its whole ancestor chain - see {@link RoleJurisdictionScope} for why that beats expanding a
     * grant to its descendants.
     */
    private List<Map<String, Object>> buildJurisdictionClauses(InboxRequest inboxRequest,
                                                               Map<String, Object> requestedNarrowing,
                                                               Map<String, String> nameToPathMap) {
        List<Map<String, Object>> clauses = new ArrayList<>();

        Map<String, Set<String>> statusesByRole = inboxRequest.getInbox().getActionableStatusesByRole();
        boolean onlyWaitingOnMe = Boolean.TRUE.equals(inboxRequest.getInbox().getAssignedToMe());

        Map<String, Map<String, Set<String>>> scopeByRole =
                roleJurisdictionScope.resolve(inboxRequest.getRequestInfo());
        if (scopeByRole == null) {
            // Fail open on *boundaries*: HRMS unreachable must not empty every inbox. Identity is
            // not part of that bargain - the "mine" clause below is emitted either way.
            log.warn("No resolvable jurisdiction scope; the inbox is not scoped for this request");
        } else if (scopeByRole.isEmpty()) {
            // A real answer, unlike null: the caller holds nothing, so they see nothing.
            log.info("Caller holds no jurisdiction for any role; returning an impossible clause");
            clauses.add(impossibleClause());
            return clauses;
        } else {
            List<Object> perRoleClauses = new ArrayList<>();
            scopeByRole.forEach((roleCode, byLevel) -> {
                /*
                 * A role that can act on nothing in this workflow contributes nothing.
                 *
                 * Holding a jurisdiction is not the same as having business here. A Facility Admin
                 * in STATE_2 who is also a Complaint Assessor in STATE_1 holds a jurisdiction in
                 * both, but only the assessor role appears in the incident workflow - so only
                 * STATE_1 belongs in this inbox.
                 */
                if (statusesByRole == null || CollectionUtils.isEmpty(statusesByRole.get(roleCode))) {
                    log.debug("Role {} acts on nothing in this workflow; it grants no inbox visibility",
                            roleCode);
                    return;
                }

                List<Object> levelClauses = termsPerLevel(byLevel, nameToPathMap);
                if (levelClauses.isEmpty()) {
                    return;
                }
                Map<String, Object> boundariesBool = new HashMap<>();
                boundariesBool.put("should", levelClauses);
                boundariesBool.put("minimum_should_match", 1);

                List<Object> pair = new ArrayList<>();
                pair.add(Collections.singletonMap("bool", boundariesBool));

                /*
                 * Paired with this role's own "mine" test, never a bare boundary branch.
                 *
                 * The pairing is what stops a Tech POC in one state being offered Tech POC work in
                 * another. The "never bare" part matters just as much: a CRM user holds
                 * COMPLAINT_ASSESSOR and COMPLAINANT, and if the complainant role contributed
                 * boundaries alone that branch would be unrestricted and would widen the whole
                 * query back to their entire jurisdiction, undoing the assessor pairing.
                 */
                if (onlyWaitingOnMe) {
                    Map<String, Object> mine = mineClauseForRole(roleCode, statusesByRole,
                            nameToPathMap, inboxRequest);
                    if (mine == null) {
                        return;
                    }
                    pair.add(mine);
                }

                Map<String, Object> roleBool = new HashMap<>();
                roleBool.put("must", pair);
                perRoleClauses.add(Collections.singletonMap("bool", roleBool));
            });

            if (perRoleClauses.isEmpty()) {
                // Jurisdictions, but none of them for a role that works on this module.
                log.info("No role of the caller's acts on this module; returning an impossible clause");
                clauses.add(impossibleClause());
                return clauses;
            }
            Map<String, Object> anyRole = new HashMap<>();
            anyRole.put("should", perRoleClauses);
            anyRole.put("minimum_should_match", 1);
            clauses.add(Collections.singletonMap("bool", anyRole));
        }

        /*
         * "Mine", again, as a clause of its own - deliberately not only inside the scope branch.
         *
         * `RoleJurisdictionScope.resolve` fails open, and when it does the per-role branch above is
         * skipped entirely. Since the browser no longer sends an assignee of its own, identity would
         * vanish with it and a vendor would see every ticket in the tenant in a default-on "mine"
         * view. Boundaries may fail open; identity may not.
         */
        if (onlyWaitingOnMe) {
            List<Object> mineBranches = mineBranchesForCaller(statusesByRole, nameToPathMap, inboxRequest);
            if (!mineBranches.isEmpty()) {
                Map<String, Object> mine = new HashMap<>();
                mine.put("should", mineBranches);
                mine.put("minimum_should_match", 1);
                clauses.add(Collections.singletonMap("bool", mine));
            }
        }

        List<Object> narrowingClauses = termsPerLevel(narrowingByLevel(requestedNarrowing), nameToPathMap);
        if (!narrowingClauses.isEmpty()) {
            Map<String, Object> narrowing = new HashMap<>();
            narrowing.put("should", narrowingClauses);
            narrowing.put("minimum_should_match", 1);
            clauses.add(Collections.singletonMap("bool", narrowing));
        }

        return clauses;
    }

    /** A clause that matches no document, for "the caller is entitled to nothing here". */
    private Map<String, Object> impossibleClause() {
        return Collections.singletonMap("bool",
                Collections.singletonMap("must_not",
                        Collections.singletonMap("match_all", new HashMap<>())));
    }

    /**
     * What "mine" means for one role, or null when it cannot be expressed.
     *
     * Pooled roles - the ones whose tickets carry no assignee, because im-services leaves their
     * states unassigned so any holder can pick the work up - are matched by the statuses that role
     * can act on. Every other role is matched by the assignee being the caller: that is the literal
     * reading of "assigned to me", and it is what a vendor and a complainant both expect.
     */
    private Map<String, Object> mineClauseForRole(String roleCode, Map<String, Set<String>> statusesByRole,
                                                  Map<String, String> nameToPathMap, InboxRequest inboxRequest) {
        if (POOLED_ROLES.contains(roleCode)) {
            String statusPath = nameToPathMap.get("status");
            if (statusPath == null || statusesByRole == null
                    || CollectionUtils.isEmpty(statusesByRole.get(roleCode))) {
                return null;
            }
            List<String> statuses = new ArrayList<String>(statusesByRole.get(roleCode));
            // On Hold is actionable but parked: out of the default bucket, back in for the counts.
            Set<String> excluded = inboxRequest.getInbox().getMineExcludedStatuses();
            if (!CollectionUtils.isEmpty(excluded)) {
                statuses.removeAll(excluded);
                if (statuses.isEmpty()) {
                    return null;
                }
            }
            return Collections.singletonMap("terms", Collections.singletonMap(statusPath, statuses));
        }
        String assigneePath = nameToPathMap.get("assignee");
        String userName = inboxRequest.getRequestInfo() == null
                || inboxRequest.getRequestInfo().getUserInfo() == null
                ? null : inboxRequest.getRequestInfo().getUserInfo().getUserName();
        if (assigneePath == null || StringUtils.isEmpty(userName)) {
            return null;
        }
        return Collections.singletonMap("term", Collections.singletonMap(assigneePath, userName));
    }

    /**
     * The same test as {@link #mineClauseForRole}, unioned over every role the caller holds and
     * detached from their boundaries, so it still applies when the boundary lookup failed open.
     *
     * Returns empty when the workflow lookup itself failed: without it a pooled role's statuses are
     * unknown, and falling back to assignee alone would empty the inbox of every pooled user, whose
     * tickets have no assignee by design.
     */
    private List<Object> mineBranchesForCaller(Map<String, Set<String>> statusesByRole,
                                               Map<String, String> nameToPathMap, InboxRequest inboxRequest) {
        List<Object> branches = new ArrayList<>();
        if (statusesByRole == null || inboxRequest.getRequestInfo() == null
                || inboxRequest.getRequestInfo().getUserInfo() == null
                || CollectionUtils.isEmpty(inboxRequest.getRequestInfo().getUserInfo().getRoles())) {
            return branches;
        }
        boolean assigneeAdded = false;
        for (org.egov.common.contract.request.Role role : inboxRequest.getRequestInfo().getUserInfo().getRoles()) {
            if (role == null || role.getCode() == null) {
                continue;
            }
            if (!POOLED_ROLES.contains(role.getCode()) && assigneeAdded) {
                continue;
            }
            Map<String, Object> clause = mineClauseForRole(role.getCode(), statusesByRole,
                    nameToPathMap, inboxRequest);
            if (clause == null) {
                continue;
            }
            if (!POOLED_ROLES.contains(role.getCode())) {
                assigneeAdded = true;
            }
            branches.add(clause);
        }
        return branches;
    }

    /** One {@code terms} clause per level, using the paths the inbox config already declares. */
    private List<Object> termsPerLevel(Map<String, Set<String>> byLevel, Map<String, String> nameToPathMap) {
        List<Object> clauses = new ArrayList<>();
        if (CollectionUtils.isEmpty(byLevel)) {
            return clauses;
        }
        byLevel.forEach((level, codes) -> {
            String path = nameToPathMap.get(level);
            if (path == null) {
                log.warn("No ES path configured for jurisdiction level {}; skipping it", level);
                return;
            }
            if (CollectionUtils.isEmpty(codes)) {
                return;
            }
            clauses.add(Collections.singletonMap("terms",
                    Collections.singletonMap(path, new ArrayList<>(codes))));
        });
        return clauses;
    }

    /**
     * The boundary codes the client asked to be narrowed to, by level.
     *
     * {@code "-"} is dropped: {@code useNewInbox.js} defaults to {@code {country: ["-"]}} when
     * session storage is empty, and treating that as a real boundary would AND an impossible
     * country code against everything and empty the inbox.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Set<String>> narrowingByLevel(Map<String, Object> requested) {
        Map<String, Set<String>> byLevel = new HashMap<>();
        if (CollectionUtils.isEmpty(requested)) {
            return byLevel;
        }
        requested.forEach((level, value) -> {
            if (SORT_ORDER_CONSTANT.equals(level) || SORT_BY_CONSTANT.equals(level)) {
                return;
            }
            Set<String> codes = new HashSet<>();
            if (value instanceof List) {
                ((List<Object>) value).forEach(item -> {
                    if (item != null) {
                        codes.add(String.valueOf(item));
                    }
                });
            } else if (value != null) {
                codes.add(String.valueOf(value));
            }
            codes.remove("-");
            if (!codes.isEmpty()) {
                byLevel.put(level, codes);
            }
        });
        return byLevel;
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

    /**
     * The fields the live SLA remaining values are computed from (see InboxServiceV2). Added here rather
     * than relying on the MDMS sourceFilterPathList, which would silently drop them and leave the inbox
     * returning the values stored at the last state change.
     */
    private static final List<String> SLA_SOURCE_FIELDS = Arrays.asList(
            DATA_PREFIX + INDEXED_STATE_SLA, DATA_PREFIX + STATE_ENTERED_TIME, DATA_PREFIX + TOTAL_SLA_BASE, DATA_PREFIX + STATE_SLA_DUE_AT);

    /** A copy of the configured list with the SLA fields appended; the configured list itself is cached and shared. */
    private List<String> withSlaSourceFields(List<String> sourceFilterPathList) {
        if (CollectionUtils.isEmpty(sourceFilterPathList))
            return sourceFilterPathList;
        List<String> paths = new ArrayList<>(sourceFilterPathList);
        SLA_SOURCE_FIELDS.stream().filter(field -> !paths.contains(field)).forEach(paths::add);
        return paths;
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



    @Override
    public Map<String, Object> getStatusCountQuery(InboxRequest inboxRequest) {
        /*
         * The counts are built without the status the user ticked.
         *
         * This query both filters by status and then aggregates on it, so including the user's
         * selection makes every other checkbox in the sidebar read (0) the moment one is ticked -
         * the counts would only ever describe the filter already applied. Everything else still
         * applies, so the numbers remain the caller's own: their jurisdiction, and their "mine"
         * clause, which lives in buildJurisdictionClauses rather than here.
         *
         * Suppressed by clearing the field for the duration of the build and putting it back
         * afterwards: the alternative, matching the clause by its ES path once the query is built,
         * would also strip the per-role status clause, which is nested but uses the same path.
         */
        ProcessInstanceSearchCriteria criteria = inboxRequest.getInbox().getProcessSearchCriteria();
        List<String> chosenStatuses = criteria == null ? null : criteria.getStatus();
        // Same trick for the On Hold carve-out: the counts report it, the default list does not.
        Set<String> heldBack = inboxRequest.getInbox().getMineExcludedStatuses();
        try {
            if (criteria != null) {
                criteria.setStatus(null);
            }
            inboxRequest.getInbox().setMineExcludedStatuses(null);
            Map<String, Object> baseEsQuery = getESQuery(inboxRequest, Boolean.FALSE, Boolean.FALSE);
            appendStatusCountAggsNode(baseEsQuery);
            return baseEsQuery;
        } finally {
            if (criteria != null) {
                criteria.setStatus(chosenStatuses);
            }
            inboxRequest.getInbox().setMineExcludedStatuses(heldBack);
        }
    }

    /**
     * A ticket is nearing SLA once 30% or less of its state SLA is left, breached ones included.
     * im-services stores the instant that happens as stateSlaWarnAt (business hours applied) and only
     * while the state SLA clock runs, so closed tickets and states without an SLA never match.
     * Documents indexed before those anchors existed fall back to the stored, cron-refreshed values.
     */
    private Map<String, Object> getNearingSlaClause() {
        Map<String, Object> warnAtRange = new HashMap<>();
        warnAtRange.put("lte", System.currentTimeMillis());
        Map<String, Object> anchored = Collections.singletonMap("range",
                Collections.singletonMap(DATA_PREFIX + STATE_SLA_WARN_AT, warnAtRange));

        Map<String, Object> legacyScript = new HashMap<>();
        legacyScript.put("source",
                "doc.containsKey('Data.slaRemaining') && " +
                        "doc.containsKey('Data.stateSla') && " +
                        "doc['Data.stateSla'].size() > 0 && " +
                        "doc['Data.stateSla'].value > 0 && " +
                        "((double) doc['Data.slaRemaining'].value / doc['Data.stateSla'].value) <= 0.3");
        legacyScript.put("lang", "painless");
        Map<String, Object> legacyBool = new HashMap<>();
        legacyBool.put("must_not", Collections.singletonList(Collections.singletonMap("exists",
                Collections.singletonMap("field", DATA_PREFIX + STATE_ENTERED_TIME))));
        legacyBool.put("filter", Collections.singletonList(Collections.singletonMap("script",
                Collections.singletonMap("script", legacyScript))));

        Map<String, Object> should = new HashMap<>();
        should.put("should", Arrays.asList(anchored, Collections.singletonMap("bool", legacyBool)));
        should.put("minimum_should_match", 1);
        return Collections.singletonMap("bool", should);
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

        mustClauseList.add(getNearingSlaClause());

        bool.put("must", mustClauseList);

        log.info("Nearing SLA Query: " + baseEsQuery);
        return baseEsQuery;
    }


    /**
     * Terms aggregation over the mapped vendor of every ticket the caller's inbox query matches, so the
     * dropdown only ever offers vendors the caller can actually act on. Deliberately reuses
     * {@link #getESQuery} rather than rebuilding the filters, so tenant, jurisdiction and status scoping
     * stay identical to the inbox listing - if the two drift apart, the dropdown starts offering vendors
     * whose tickets the caller cannot see.
     */
    public Map<String, Object> getMappedVendorAggregationQuery(InboxRequest inboxRequest) {
        Map<String, Object> baseEsQuery = getESQuery(inboxRequest, Boolean.FALSE, Boolean.FALSE);

        // Only the aggregation is of interest, the hits themselves are not.
        baseEsQuery.put(SIZE_KEY, 0);

        Map<String, Object> innerTermsQuery = new HashMap<>();
        innerTermsQuery.put(FIELD_KEY, MAPPED_VENDOR_NAME_KEYWORD_PATH);
        innerTermsQuery.put(SIZE_KEY, MAPPED_VENDOR_AGGREGATION_SIZE);
        // Alphabetical, so the UI can render the list as it comes.
        innerTermsQuery.put(ORDER_KEY, Collections.singletonMap("_key", "asc"));

        baseEsQuery.put(AGGS_KEY, Collections.singletonMap(MAPPED_VENDOR_AGGREGATION_KEY,
                Collections.singletonMap(TERMS_KEY, innerTermsQuery)));

        log.info("📄 Mapped vendor aggregation query: {}", baseEsQuery);
        return baseEsQuery;
    }

    private void appendStatusCountAggsNode(Map<String, Object> baseEsQuery) {
        Map<String, Object> aggsNode = new HashMap<>();
        aggsNode.put("statusCount", new HashMap<>());
        Map<String, Object> statusCountNode = (Map<String, Object>) aggsNode.get("statusCount");
        statusCountNode.put("terms", new HashMap<>());
        Map<String, Object> innerTermsQuery = (Map<String, Object>) statusCountNode.get("terms");
        innerTermsQuery.put("field", "Data.incident.applicationStatus.keyword");
        innerTermsQuery.put("size", 50);
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
            path = DATA_PREFIX + key + ".keyword";

        return path;
    }

}
