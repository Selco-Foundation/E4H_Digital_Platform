package org.egov.hrms.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.hrms.config.PropertiesManager;
import org.egov.hrms.service.BoundaryService;
import org.egov.hrms.web.contract.EmployeeSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmployeeQueryBuilder {
	
	@Value("${egov.hrms.default.pagination.limit}")
	private Integer defaultLimit;

	@Autowired
	private PropertiesManager properties;
	
	@Autowired
	private BoundaryService boundaryService;
	
	/**
	 * Returns query for searching employees
	 * 
	 * @param criteria
	 * @return
	 */
	public String getEmployeeSearchQuery(EmployeeSearchCriteria criteria,List <Object> preparedStmtList ) {
		log.trace("EmployeeQueryBuilder.getEmployeeSearchQuery invoked");
		StringBuilder builder = new StringBuilder(EmployeeQueries.HRMS_GET_EMPLOYEES);
		addWhereClause(criteria, builder, preparedStmtList);
		return paginationClause(criteria, builder);
	}

	public String getEmployeeCountQuery(String tenantId, List <Object> preparedStmtList ) {
		log.trace("EmployeeQueryBuilder.getEmployeeCountQuery invoked for tenant: {}", tenantId);
		StringBuilder builder = new StringBuilder(EmployeeQueries.HRMS_COUNT_EMP_QUERY);
		if(tenantId.equalsIgnoreCase(properties.stateLevelTenantId)){
			builder.append("LIKE ? ");
			preparedStmtList.add(tenantId+"%");
		}
		else{
			builder.append("= ? ");
			preparedStmtList.add(tenantId);
		}
		builder.append("GROUP BY active");
		log.debug("Employee count query built successfully for tenant: {}", tenantId);
		return builder.toString();
	}
	
	public String getPositionSeqQuery() {
		log.trace("EmployeeQueryBuilder.getPositionSeqQuery invoked");
		return EmployeeQueries.HRMS_POSITION_SEQ;
	}
	
	/**
	 * Adds where clause to the query based on the requirement.
	 *  @param criteria
	 * @param builder
	 * @param preparedStmtList
	 */
	public void addWhereClause(EmployeeSearchCriteria criteria, StringBuilder builder, List<Object> preparedStmtList) {
		
		if(!StringUtils.isEmpty(criteria.getTenantId())) {
			builder.append(" employee.tenantid = ?");
			preparedStmtList.add(criteria.getTenantId());
		}
			else
			builder.append(" employee.tenantid NOTNULL");
		
		if(!CollectionUtils.isEmpty(criteria.getCodes())){
			List<String> codes = criteria.getCodes().stream().map(String::toLowerCase).collect(Collectors.toList());
			builder.append(" and lower(employee.code) IN (").append(createQuery(codes)).append(")");
			addToPreparedStatement(preparedStmtList, codes);
		}
		if(!CollectionUtils.isEmpty(criteria.getIds())){
			builder.append(" and employee.id IN (").append(createQuery(criteria.getIds())).append(")");
			addToPreparedStatement(preparedStmtList, criteria.getIds());
		}
		if(!CollectionUtils.isEmpty(criteria.getUuids())){
			builder.append(" and employee.uuid IN (").append(createQuery(criteria.getUuids())).append(")");
			addToPreparedStatement(preparedStmtList, criteria.getUuids());
		}
		if(!CollectionUtils.isEmpty(criteria.getEmployeestatuses())){
			builder.append(" and employee.employeestatus IN (").append(createQuery(criteria.getEmployeestatuses())).append(")");
			addToPreparedStatement(preparedStmtList, criteria.getEmployeestatuses());
		}
		if(!CollectionUtils.isEmpty(criteria.getEmployeetypes())){
			builder.append(" and employee.employeetype IN (").append(createQuery(criteria.getEmployeetypes())).append(")");
			addToPreparedStatement(preparedStmtList, criteria.getEmployeetypes());
		}
		if(criteria.getIsActive() != null){
			builder.append(" and employee.active = ?");
			preparedStmtList.add(criteria.getIsActive());
		}
	}
	
	public String paginationClause(EmployeeSearchCriteria criteria, StringBuilder builder) {
		String pagination = EmployeeQueries.HRMS_PAGINATION_WRAPPER;
		pagination = pagination.replace("{}", builder.toString());
		if(null != criteria.getOffset())
			pagination = pagination.replace("$offset", criteria.getOffset().toString());
		else
			pagination = pagination.replace("$offset", "0");
		
		if(null != criteria.getLimit()){
			Integer limit = criteria.getLimit() + criteria.getOffset();
			pagination = pagination.replace("$limit", limit.toString());
		}
		else
			pagination = pagination.replace("$limit", defaultLimit.toString());
		
		return pagination;
	}

	public String getAssignmentSearchQuery(EmployeeSearchCriteria criteria, List<Object> preparedStmtList) {
		log.trace("EmployeeQueryBuilder.getAssignmentSearchQuery invoked");
		StringBuilder builder = new StringBuilder(EmployeeQueries.HRMS_GET_ASSIGNMENT);
		addWhereClauseAssignment(criteria, builder, preparedStmtList);
		log.debug("Assignment search query built successfully");
		return builder.toString();
	}

	private void addWhereClauseAssignment(EmployeeSearchCriteria criteria, StringBuilder builder, List<Object> preparedStmtList) {
		if(!CollectionUtils.isEmpty(criteria.getDepartments())){
			builder.append(" and assignment.department IN (").append(createQuery(criteria.getDepartments())).append(")");
			addToPreparedStatement(preparedStmtList, criteria.getDepartments());
		}
		if(!CollectionUtils.isEmpty(criteria.getDesignations())){
			builder.append(" and assignment.designation IN (").append(createQuery(criteria.getDesignations())+")");
			addToPreparedStatement(preparedStmtList,criteria.getDesignations());
		}
		if(!CollectionUtils.isEmpty(criteria.getPositions())){
			builder.append(" and assignment.position IN (").append(createQuery(criteria.getPositions())+")");
			addToPreparedStatement(preparedStmtList,criteria.getPositions());
		}
		if(null != criteria.getAsOnDate()) {
			builder.append( " and case when assignment.todate is null then assignment.fromdate <= ? else assignment.fromdate <= ? and assignment.todate > ? end");
			preparedStmtList.add(criteria.getAsOnDate());
			preparedStmtList.add(criteria.getAsOnDate());
			preparedStmtList.add(criteria.getAsOnDate());
		}


	}

	public String getJurisdictionSearchQuery(EmployeeSearchCriteria criteria, List<Object> preparedStmtList, RequestInfo requestInfo, String tenantId) {
		log.trace("EmployeeQueryBuilder.getJurisdictionSearchQuery invoked for tenant: {}", tenantId);
		StringBuilder builder = new StringBuilder(EmployeeQueries.HRMS_GET_JURISDICTION);
		addWhereClauseJurisdiction(criteria, builder, preparedStmtList, requestInfo, tenantId);
		log.debug("Jurisdiction search query built successfully");
		return builder.toString();
	}

	private void addWhereClauseJurisdiction(EmployeeSearchCriteria criteria, StringBuilder builder, List<Object> preparedStmtList, RequestInfo requestInfo, String tenantId) {
		if(!CollectionUtils.isEmpty(criteria.getBoundaryCodes())){
			List<String> boundariesToSearch;
			
			// If searchOnlyInBoundary is true, use only the specified boundary codes
			// Otherwise, fetch all ancestor boundaries (default behavior)
			if(Boolean.TRUE.equals(criteria.getSearchOnlyInBoundary())) {
				// Search only in the specified boundary codes, exclude ancestor boundaries
				boundariesToSearch = criteria.getBoundaryCodes();
			} else {
				// Fetch hierarchical boundary codes from boundary service (all ancestor boundaries)
				List<String> hierarchicalBoundaries = boundaryService.getAncestorBoundaries(
						requestInfo, 
						tenantId,
						criteria.getBoundaryCodes(),
						null  // hierarchyType - defaults to ADMIN if null
				);

				boundariesToSearch = new ArrayList<>(hierarchicalBoundaries);
				// Ensure original boundary codes are included
				for(String boundaryCode : criteria.getBoundaryCodes()) {
					if(!boundariesToSearch.contains(boundaryCode)) {
						boundariesToSearch.add(boundaryCode);
					}
				}
			}
			
			builder.append(" and jurisdiction.boundary IN (").append(createQuery(boundariesToSearch)).append(")");
			addToPreparedStatement(preparedStmtList, boundariesToSearch);
		}
		// Only consider active jurisdictions
		builder.append(" and jurisdiction.isactive = ?");
		preparedStmtList.add(true);
	}



	private String createQuery(List<?> ids) {
		StringBuilder builder = new StringBuilder();
		int length = ids.size();
		for (int i = 0; i < length; i++) {
			builder.append(" ?");
			if (i != length - 1)
				builder.append(",");
		}
		return builder.toString();
	}

	private void addToPreparedStatement(List<Object> preparedStmtList, List<?> ids) {
		ids.forEach(id -> {
			preparedStmtList.add(id);
		});
	}


}
