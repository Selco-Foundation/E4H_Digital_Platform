package org.egov.im.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.im.repository.rowmapper.IMQueryBuilder;
import org.egov.im.repository.rowmapper.IMRowMapper;
import org.egov.im.util.IMUtils;
import org.egov.im.util.IMConstants;
import org.egov.im.web.models.IncidentWrapper;
import org.egov.im.web.models.RequestSearchCriteria;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.Workflow;
import org.egov.tracer.model.CustomException;
import org.egov.common.exception.InvalidTenantIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class IMRepository {


    private IMQueryBuilder queryBuilder;

    private IMRowMapper rowMapper;

    private JdbcTemplate jdbcTemplate;

    private IMUtils utils;


    @Autowired
    public IMRepository(IMQueryBuilder queryBuilder, IMRowMapper rowMapper, JdbcTemplate jdbcTemplate, IMUtils utils) {
        this.queryBuilder = queryBuilder;
        this.rowMapper = rowMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.utils = utils;
    }




    /**
     * searches services based on search criteria and then wraps it into serviceWrappers
     * @param criteria
     * @return
     */
    public List<IncidentWrapper> getIncidentWrappers(RequestSearchCriteria criteria){
        List<Incident> incidents = getIncidents(criteria);
        List<String> serviceRequestids = incidents.stream().map(Incident::getIncidentId).collect(Collectors.toList());
        Map<String, Workflow> idToWorkflowMap = new HashMap<>();
        List<IncidentWrapper> serviceWrappers = new ArrayList<>();

        for(Incident incident : incidents){
        	IncidentWrapper serviceWrapper = IncidentWrapper.builder().incident(incident).workflow(idToWorkflowMap.get(incident.getIncidentId())).build();
            serviceWrappers.add(serviceWrapper);
        }
        return serviceWrappers;
    }

    /**
     * searches services based on search criteria
     * @param criteria
     * @return
     */
    public List<Incident> getIncidents(RequestSearchCriteria criteria) {

        String tenantId = criteria.getTenantId();
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getPGRSearchQuery(criteria, preparedStmtList);
        try {
            query = utils.replaceSchemaPlaceholder(query, tenantId);
        } catch (Exception e) {
            throw new CustomException("PGR_UPDATE_ERROR",
                    "TenantId length is not sufficient to replace query schema in a multi state instance");
        }
        
        List<Incident> services =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return services;
    }

    /**
     * Returns the count based on the search criteria
     * @param criteria
     * @return
     */
    public Integer getCount(RequestSearchCriteria criteria) {

        String tenantId = criteria.getTenantId();
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getCountQuery(criteria, preparedStmtList);
        try {
            query = utils.replaceSchemaPlaceholder(query, tenantId);
        } catch (Exception e) {
            throw new CustomException("PGR_REQUEST_COUNT_ERROR",
                    "TenantId length is not sufficient to replace query schema in a multi state instance");
        }
        Integer count =  jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
        return count;
    }


	public Map<String, Integer> fetchDynamicData(String tenantId) {
		List<Object> preparedStmtListCompalintsResolved = new ArrayList<>();
		String query = queryBuilder.getResolvedComplaints(tenantId,preparedStmtListCompalintsResolved );
        try {
            query = utils.replaceSchemaPlaceholder(query, tenantId);
        } catch (Exception e) {
            throw new CustomException("PGR_SEARCH_ERROR",
                    "TenantId length is not sufficient to replace query schema in a multi state instance");
        }
		int complaintsResolved = jdbcTemplate.queryForObject(query,preparedStmtListCompalintsResolved.toArray(),Integer.class);

		List<Object> preparedStmtListAverageResolutionTime = new ArrayList<>();
		query = queryBuilder.getAverageResolutionTime(tenantId, preparedStmtListAverageResolutionTime);
        try {
            query = utils.replaceSchemaPlaceholder(query, tenantId);
        } catch (Exception e) {
            throw new CustomException("PGR_SEARCH_ERROR",
                    "TenantId length is not sufficient to replace query schema in a multi state instance");
        }
		int averageResolutionTime = jdbcTemplate.queryForObject(query, preparedStmtListAverageResolutionTime.toArray(),Integer.class);

		Map<String, Integer> dynamicData = new HashMap<String,Integer>();
		dynamicData.put(IMConstants.COMPLAINTS_RESOLVED, complaintsResolved);
		dynamicData.put(IMConstants.AVERAGE_RESOLUTION_TIME, averageResolutionTime);

		return dynamicData;
	}



}
