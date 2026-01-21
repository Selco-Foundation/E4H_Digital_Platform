package org.egov.wf.repository;


import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.wf.repository.querybuilder.WorkflowQueryBuilder;
import org.egov.wf.repository.rowmapper.WorkflowRowMapper;
import org.egov.wf.web.models.ProcessInstance;
import org.egov.wf.web.models.ProcessInstanceSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Slf4j
public class WorKflowRepository {

    private WorkflowQueryBuilder queryBuilder;

    private JdbcTemplate jdbcTemplate;

    private WorkflowRowMapper rowMapper;


    @Autowired
    public WorKflowRepository(WorkflowQueryBuilder queryBuilder, JdbcTemplate jdbcTemplate, WorkflowRowMapper rowMapper) {
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }


    /**
     * Executes the search criteria on the db
     * @param criteria The object containing the params to search on
     * @return The parsed response from the search query
     */
    public List<ProcessInstance> getProcessInstances(ProcessInstanceSearchCriteria criteria){
        log.trace("Entering getProcessInstances method");
        List<Object> preparedStmtList = new ArrayList<>();

        List<String> ids = getProcessInstanceIds(criteria);

        if(CollectionUtils.isEmpty(ids)) {
            log.debug("No process instance IDs found for criteria");
            log.trace("Exiting getProcessInstances method - empty result");
            return new LinkedList<>();
        }

        log.info("Fetching {} process instance(s) from database", ids.size());
        String query = queryBuilder.getProcessInstanceSearchQueryById(ids, preparedStmtList);
        log.debug("Query for process instance search: {} with params: {}", query, preparedStmtList);

        List<ProcessInstance> result = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        log.debug("Retrieved {} process instance(s) from database", result != null ? result.size() : 0);
        log.trace("Exiting getProcessInstances method");
        return result;
    }



    /**
     *
     * @param criteria
     * @return
     */
    public List<ProcessInstance> getProcessInstancesForUserInbox(ProcessInstanceSearchCriteria criteria){
        log.trace("Entering getProcessInstancesForUserInbox method");

        List<Object> preparedStmtList = new ArrayList<>();

        if(CollectionUtils.isEmpty(criteria.getStatus()) && CollectionUtils.isEmpty(criteria.getTenantSpecifiStatus())) {
            log.debug("Empty status criteria for user inbox search");
            log.trace("Exiting getProcessInstancesForUserInbox method - empty criteria");
            return new LinkedList<>();
        }

        List<String> ids = getInboxSearchIds(criteria);

        if(CollectionUtils.isEmpty(ids)) {
            log.debug("No IDs found for user inbox search");
            log.trace("Exiting getProcessInstancesForUserInbox method - empty result");
            return new LinkedList<>();
        }

        log.info("Fetching {} process instance(s) for user inbox from database", ids.size());
        String query = queryBuilder.getProcessInstanceSearchQueryById(ids, preparedStmtList);
        log.debug("Query for user inbox search: {} with params: {}", query, preparedStmtList);
        
        List<ProcessInstance> result = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        log.debug("Retrieved {} process instance(s) for user inbox", result != null ? result.size() : 0);
        log.trace("Exiting getProcessInstancesForUserInbox method");
        return result;
    }

    public Integer getProcessInstancesForUserInboxCount(ProcessInstanceSearchCriteria criteria) {
        log.trace("Entering getProcessInstancesForUserInboxCount method");
        List<Object> preparedStmtList = new ArrayList<>();
        criteria.setIsAssignedToMeCount(true);
        String query = queryBuilder.getInboxIdCount(criteria, (ArrayList<Object>) preparedStmtList);
        log.debug("Query for user inbox count: {} with params: {}", query, preparedStmtList);
        Integer count =  jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
        log.info("User inbox count: {}", count);
        log.trace("Exiting getProcessInstancesForUserInboxCount method");
        return count;
    }

    /**
     * Returns the count based on the search criteria
     * @param criteria
     * @return
     */
    public Integer getInboxCount(ProcessInstanceSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getInboxCount(criteria, preparedStmtList,Boolean.FALSE);
        Integer count =  jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
        return count;
    }

    public Integer getProcessInstancesCount(ProcessInstanceSearchCriteria criteria){
        log.trace("Entering getProcessInstancesCount method");
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getProcessInstanceCount(criteria, preparedStmtList,Boolean.FALSE);
        log.debug("Query for process instances count: {} with params: {}", query, preparedStmtList);
        Integer count = jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
        log.info("Process instances count: {}", count);
        log.trace("Exiting getProcessInstancesCount method");
        return count;
    }

    /**
     * Returns the count based on the search criteria
     * @param criteria
     * @return
     */
    public List getInboxStatusCount(ProcessInstanceSearchCriteria criteria) {
        log.trace("Entering getInboxStatusCount method");
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getInboxCount(criteria, preparedStmtList,Boolean.TRUE);
        log.debug("Query for inbox status count: {} with params: {}", query, preparedStmtList);
        List result = jdbcTemplate.queryForList(query, preparedStmtList.toArray());
        log.info("Inbox status count query completed, returning {} status count(s)", result != null ? result.size() : 0);
        log.trace("Exiting getInboxStatusCount method");
        return result;
    }

    public List getProcessInstancesStatusCount(ProcessInstanceSearchCriteria criteria){
        log.trace("Entering getProcessInstancesStatusCount method");
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getProcessInstanceCount(criteria, preparedStmtList,Boolean.TRUE);
        log.debug("Query for process instances status count: {} with params: {}", query, preparedStmtList);
        List result = jdbcTemplate.queryForList(query, preparedStmtList.toArray());
        log.info("Process instances status count query completed, returning {} status count(s)", result != null ? result.size() : 0);
        log.trace("Exiting getProcessInstancesStatusCount method");
        return result;
    }



    private List<String> getInboxSearchIds(ProcessInstanceSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        criteria.setIsAssignedToMeCount(false);
        String query = queryBuilder.getInboxIdQuery(criteria,preparedStmtList,true);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), new SingleColumnRowMapper<>(String.class));
    }

    private List<String> getProcessInstanceIds(ProcessInstanceSearchCriteria criteria) {
        log.trace("Entering getProcessInstanceIds method");
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getProcessInstanceIds(criteria,preparedStmtList);
        log.debug("Query for process instance IDs: {} with params: {}", query, preparedStmtList);
        List<String> result = jdbcTemplate.query(query, preparedStmtList.toArray(), new SingleColumnRowMapper<>(String.class));
        log.debug("Retrieved {} process instance ID(s)", result != null ? result.size() : 0);
        log.trace("Exiting getProcessInstanceIds method");
        return result;
    }


    public List<String> fetchEscalatedApplicationsBusinessIdsFromDb(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria) {
        log.trace("Entering fetchEscalatedApplicationsBusinessIdsFromDb method");
        ArrayList<Object> preparedStmtList = new ArrayList<>();

        String query = queryBuilder.getAutoEscalatedApplicationsFinalQuery(requestInfo,criteria, preparedStmtList);
        log.debug("Query for escalated applications business IDs: {} with params: {}", query, preparedStmtList);
        List<String> escalatedApplicationsBusinessIds = jdbcTemplate.query(query, preparedStmtList.toArray(), new SingleColumnRowMapper<>(String.class));
        preparedStmtList.clear();
        
        int businessIdCount = escalatedApplicationsBusinessIds != null ? escalatedApplicationsBusinessIds.size() : 0;
        log.info("Retrieved {} escalated application business ID(s) from database", businessIdCount);
        log.trace("Exiting fetchEscalatedApplicationsBusinessIdsFromDb method");
        return escalatedApplicationsBusinessIds;
    }

    public Integer getEscalatedApplicationsCount(RequestInfo requestInfo,ProcessInstanceSearchCriteria criteria) {
        log.trace("Entering getEscalatedApplicationsCount method");
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEscalatedApplicationsCount(requestInfo,criteria, (ArrayList<Object>) preparedStmtList);
        log.debug("Query for escalated applications count: {} with params: {}", query, preparedStmtList);
        Integer count =  jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
        log.info("Escalated applications count: {}", count);
        log.trace("Exiting getEscalatedApplicationsCount method");
        return count;
    }
}