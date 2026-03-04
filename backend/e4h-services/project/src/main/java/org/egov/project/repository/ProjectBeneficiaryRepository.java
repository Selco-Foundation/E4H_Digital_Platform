package org.egov.project.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.data.query.builder.GenericQueryBuilder;
import org.egov.common.data.query.builder.QueryFieldChecker;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.repository.GenericRepository;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.models.project.ProjectBeneficiary;
import org.egov.common.models.project.ProjectBeneficiarySearch;
import org.egov.common.producer.Producer;
import org.egov.project.repository.rowmapper.ProjectBeneficiaryRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.constructTotalCountCTEAndReturnResult;
import static org.egov.common.utils.CommonUtils.getIdMethod;

@Repository
@Slf4j
public class ProjectBeneficiaryRepository extends GenericRepository<ProjectBeneficiary> {

    @Autowired
    public ProjectBeneficiaryRepository(Producer producer, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                        RedisTemplate<String, Object> redisTemplate,
                                        SelectQueryBuilder selectQueryBuilder, ProjectBeneficiaryRowMapper projectBeneficiaryRowMapper) {
        super(producer, namedParameterJdbcTemplate, redisTemplate, selectQueryBuilder,
                projectBeneficiaryRowMapper, Optional.of("project_beneficiary"));
    }

    public SearchResponse<ProjectBeneficiary> find(ProjectBeneficiarySearch householdMemberSearch,
                                                   Integer limit,
                                                   Integer offset,
                                                   String tenantId,
                                                   Long lastChangedSince,
                                                   Boolean includeDeleted) {
        log.trace("Entering find");
        log.info("Searching project beneficiaries with criteria");
        log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}, includeDeleted: {}", limit, offset, tenantId, includeDeleted);

        Map<String, Object> paramsMap = new HashMap<>();
        StringBuilder queryBuilder = new StringBuilder();

        String query = "SELECT * FROM project_beneficiary ";

        List<String> whereFields = GenericQueryBuilder.getFieldsWithCondition(householdMemberSearch, QueryFieldChecker.isNotNull, paramsMap);
        query = GenericQueryBuilder.generateQuery(query, whereFields).toString().trim();

        query = query + " AND tenantId=:tenantId ";

        if (query.contains(this.tableName + " AND")) {
            query = query.replace(this.tableName + " AND", this.tableName + " WHERE");
        }

        queryBuilder.append(query);

        if (Boolean.FALSE.equals(includeDeleted)) {
            queryBuilder.append("AND isDeleted=:isDeleted ");
        }

        if (lastChangedSince != null) {
            queryBuilder.append("AND lastModifiedTime>=:lastModifiedTime ");
        }

        paramsMap.put("tenantId", tenantId);
        paramsMap.put("isDeleted", includeDeleted);
        paramsMap.put("lastModifiedTime", lastChangedSince);

        queryBuilder.append(" ORDER BY id ASC ");

        log.debug("Executing count query");
        Long totalCount = constructTotalCountCTEAndReturnResult(queryBuilder.toString(), paramsMap, this.namedParameterJdbcTemplate);
        log.debug("Total count: {}", totalCount);

        queryBuilder.append(" LIMIT :limit OFFSET :offset");
        paramsMap.put("limit", limit);
        paramsMap.put("offset", offset);

        log.debug("Executing search query with limit and offset");
        List<ProjectBeneficiary> projectBeneficiaries = this.namedParameterJdbcTemplate.query(queryBuilder.toString(), paramsMap, this.rowMapper);
        log.info("Found {} project beneficiaries", projectBeneficiaries != null ? projectBeneficiaries.size() : 0);
        log.trace("Exiting find");
        return SearchResponse.<ProjectBeneficiary>builder().totalCount(totalCount).response(projectBeneficiaries).build();
    }

    public SearchResponse<ProjectBeneficiary> findById(List<String> ids, String columnName, Boolean includeDeleted) {
        log.trace("Entering findById for {} IDs, columnName: {}", ids != null ? ids.size() : 0, columnName);
        log.info("Finding project beneficiaries by ID");
        log.debug("Searching cache for {} IDs", ids != null ? ids.size() : 0);
        List<ProjectBeneficiary> objFound = findInCache(ids);
        log.debug("Found {} beneficiaries in cache", objFound != null ? objFound.size() : 0);
        
        if (!includeDeleted) {
            log.debug("Filtering out deleted beneficiaries");
            objFound = objFound.stream()
                    .filter(entity -> entity.getIsDeleted().equals(false))
                    .toList();
        }
        if (!objFound.isEmpty()) {
            Method idMethod = getIdMethod(objFound, columnName);
            ids.removeAll(objFound.stream()
                    .map(obj -> (String) ReflectionUtils.invokeMethod(idMethod, obj))
                    .toList());
            if (ids.isEmpty()) {
                log.info("All objects were found in the cache, returning objects");
                log.trace("Exiting findById");
                return SearchResponse.<ProjectBeneficiary>builder().response(objFound).build();
            }
            log.debug("{} IDs not found in cache, querying database", ids.size());
        }

        String query = String.format("SELECT * FROM project_beneficiary where %s IN (:ids) AND isDeleted = false", columnName);
        if (null != includeDeleted && includeDeleted) {
            query = String.format("SELECT * FROM project_beneficiary WHERE %s IN (:ids)", columnName);
        }
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("ids", ids);

        log.debug("Querying database for remaining {} IDs", ids.size());
        objFound.addAll(this.namedParameterJdbcTemplate.query(query, paramMap, this.rowMapper));
        log.debug("Found {} additional beneficiaries from database", objFound.size() - (objFound.size() - ids.size()));
        putInCache(objFound);
        log.info("Returning {} objects from the database", objFound.size());
        log.trace("Exiting findById");
        return SearchResponse.<ProjectBeneficiary>builder().response(objFound).build();
    }
}

