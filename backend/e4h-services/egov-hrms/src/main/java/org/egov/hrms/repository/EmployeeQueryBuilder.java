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

@Service
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
		StringBuilder builder = new StringBuilder(EmployeeQueries.HRMS_GET_EMPLOYEES);
		addWhereClause(criteria, builder, preparedStmtList);
		return paginationClause(criteria, builder);
	}

	public String getEmployeeCountQuery(String tenantId, List <Object> preparedStmtList ) {
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
		return builder.toString();
	}
	
	public String getPositionSeqQuery() {
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
			List<String> codes = criteria.getCodes();
			builder.append(" and employee.code IN (").append(createQuery(codes)).append(")");
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
		StringBuilder builder = new StringBuilder(EmployeeQueries.HRMS_GET_ASSIGNMENT);
		addWhereClauseAssignment(criteria, builder, preparedStmtList);
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
		StringBuilder builder = new StringBuilder(EmployeeQueries.HRMS_GET_JURISDICTION);
		addWhereClauseJurisdiction(criteria, builder, preparedStmtList, requestInfo, tenantId);
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

		addRoleScopeClause(criteria, builder, preparedStmtList);
	}

	/**
	 * Narrows the boundary match to jurisdictions the requested roles are actually held at.
	 *
	 * Without this, "who is a PROJECT_MANAGER in Bihar?" is answered by anyone who is a project
	 * manager somewhere and has any jurisdiction in Bihar - which is a different question, and
	 * the wrong one once a user can hold different roles in different places.
	 *
	 * The NOT EXISTS arm is what makes this safe to switch on with no data migration: a
	 * jurisdiction with no mapping rows applies to every role the employee holds, which is every
	 * jurisdiction in the system today. Only once rows exist does the clause start excluding
	 * anything.
	 *
	 * Applies only when roles are part of the search. With no roles asked for there is nothing to
	 * scope by, and the boundary filter alone is the existing behaviour.
	 *
	 * @param criteria
	 * @param builder
	 * @param preparedStmtList
	 */
	private void addRoleScopeClause(EmployeeSearchCriteria criteria, StringBuilder builder, List<Object> preparedStmtList) {
		if(CollectionUtils.isEmpty(criteria.getRoles()))
			return;

		builder.append(" and (not exists (select 1 from {schema}.eg_hrms_jurisdiction_role jurisdictionrole")
				.append(" where jurisdictionrole.jurisdictionid = jurisdiction.uuid)")
				.append(" or exists (select 1 from {schema}.eg_hrms_jurisdiction_role jurisdictionrole")
				.append(" where jurisdictionrole.jurisdictionid = jurisdiction.uuid")
				.append(" and jurisdictionrole.rolecode IN (").append(createQuery(criteria.getRoles())).append(")))");
		addToPreparedStatement(preparedStmtList, criteria.getRoles());
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
