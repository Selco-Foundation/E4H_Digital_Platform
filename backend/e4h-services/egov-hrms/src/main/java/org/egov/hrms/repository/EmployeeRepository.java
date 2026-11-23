package org.egov.hrms.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.exception.InvalidTenantIdException;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.hrms.model.Employee;
import org.egov.hrms.model.Jurisdiction;
import org.egov.hrms.model.JurisdictionRole;
import org.egov.hrms.utils.HRMSUtils;
import org.egov.hrms.web.contract.EmployeeSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class EmployeeRepository {
	
	@Autowired
	private EmployeeQueryBuilder queryBuilder;
	
	@Autowired
	private MultiStateInstanceUtil centralInstanceUtil;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private EmployeeRowMapper rowMapper;

	@Autowired
	private EmployeeCountRowMapper countRowMapper;

	@Autowired
	private JurisdictionRoleRowMapper jurisdictionRoleRowMapper;

	@Autowired
	private HRMSUtils hrmsUtils;

	/**
	 * DB Repository that makes jdbc calls to the db and fetches employees.
	 * 
	 * @param criteria
	 * @param requestInfo
	 * @param stateLevelTenantId
	 * @return
	 */
	public List<Employee> fetchEmployees(EmployeeSearchCriteria criteria, RequestInfo requestInfo, String stateLevelTenantId){
		List<Employee> employees = new ArrayList<>();
		List<Object> preparedStmtList = new ArrayList<>();

		if(hrmsUtils.isAssignmentSearchReqd(criteria)) {
			List<String> empUuids = fetchEmployeesforAssignment(criteria, requestInfo, stateLevelTenantId);
			if (CollectionUtils.isEmpty(empUuids))
				return employees;
			else {
				if(!CollectionUtils.isEmpty(criteria.getUuids()))
					criteria.setUuids(criteria.getUuids().stream().filter(empUuids::contains).collect(Collectors.toList()));
				else
					criteria.setUuids(empUuids);
			}
		}

		if(hrmsUtils.isJurisdictionSearchReqd(criteria)) {
			List<String> empUuids = fetchEmployeesforJurisdiction(criteria, requestInfo, stateLevelTenantId);
			if (CollectionUtils.isEmpty(empUuids))
				return employees;
			else {
				if(!CollectionUtils.isEmpty(criteria.getUuids())) {
					List<String> filteredUuids = criteria.getUuids().stream().filter(empUuids::contains).collect(Collectors.toList());
					// If both roles and boundaryCodes are provided, and no employees match both criteria, return empty list
					if(!CollectionUtils.isEmpty(criteria.getRoles()) && CollectionUtils.isEmpty(filteredUuids)) {
						return employees;
					}
					criteria.setUuids(filteredUuids);
				} else {
					criteria.setUuids(empUuids);
				}
			}
		}
		
		String query = queryBuilder.getEmployeeSearchQuery(criteria, preparedStmtList);
		String finalQuery;
		try {
			finalQuery = centralInstanceUtil.replaceSchemaPlaceholder(query, stateLevelTenantId);
		} catch (InvalidTenantIdException e1) {
			throw new CustomException("HRMS_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");		
		}

		try {
			employees = jdbcTemplate.query(finalQuery, preparedStmtList.toArray(),rowMapper);
		}catch(Exception e) {
			log.error("Exception while making the db call: ",e);
			log.error("query; "+ finalQuery);
		}

		if(Boolean.TRUE.equals(criteria.getIncludeJurisdictionRoles()))
			attachJurisdictionRoles(employees, stateLevelTenantId);

		return employees;
	}

	/**
	 * Loads eg_hrms_jurisdiction_role for every jurisdiction across the given employees in one
	 * query and hangs the results off each jurisdiction. Separate from the main search for the
	 * reason given on HRMS_GET_JURISDICTION_ROLES.
	 *
	 * Only the code is stored, so roles come back with a null name. The name is filled in
	 * EmployeeService.fillJurisdictionRoleNames, not here: the employee's user is attached after
	 * this method runs, so there is nothing to read a name from at this point.
	 *
	 * @param employees
	 * @param stateLevelTenantId
	 */
	private void attachJurisdictionRoles(List<Employee> employees, String stateLevelTenantId) {
		List<String> jurisdictionIds = employees.stream()
				.filter(employee -> !CollectionUtils.isEmpty(employee.getJurisdictions()))
				.flatMap(employee -> employee.getJurisdictions().stream())
				.map(Jurisdiction::getId)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());

		if(CollectionUtils.isEmpty(jurisdictionIds))
			return;

		StringBuilder query = new StringBuilder(EmployeeQueries.HRMS_GET_JURISDICTION_ROLES);
		query.append(jurisdictionIds.stream().map(id -> "?").collect(Collectors.joining(","))).append(")");

		String finalQuery;
		try {
			finalQuery = centralInstanceUtil.replaceSchemaPlaceholder(query.toString(), stateLevelTenantId);
		} catch (InvalidTenantIdException e) {
			log.error("Invalid tenant ID exception for state level tenant: {}", stateLevelTenantId, e);
			throw new CustomException("HRMS_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		}

		Map<String, List<JurisdictionRole>> rolesByJurisdiction;
		try {
			rolesByJurisdiction = jdbcTemplate.query(finalQuery, jurisdictionIds.toArray(), jurisdictionRoleRowMapper);
		} catch (Exception e) {
			// Deliberately not swallowed the way the search above is: a jurisdiction silently
			// missing its roles reads as "applies to every role", which widens scope instead of
			// narrowing it. Failing loudly is the safer direction.
			log.error("Exception while loading jurisdiction roles for tenant: {}", stateLevelTenantId, e);
			throw new CustomException("HRMS_JURISDICTION_ROLE_FETCH_FAILED",
					"Could not load jurisdiction roles");
		}

		for(Employee employee : employees) {
			if(CollectionUtils.isEmpty(employee.getJurisdictions()))
				continue;
			for(Jurisdiction jurisdiction : employee.getJurisdictions()) {
				List<JurisdictionRole> roles = rolesByJurisdiction.get(jurisdiction.getId());
				jurisdiction.setRoles(CollectionUtils.isEmpty(roles) ? new ArrayList<>() : roles);
			}
		}
	}

	private List<String> fetchEmployeesforAssignment(EmployeeSearchCriteria criteria, RequestInfo requestInfo, String stateLevelTenantId) {
		List<String> employeesIds = new ArrayList<>();
		List <Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getAssignmentSearchQuery(criteria, preparedStmtList);

		try {
			query = centralInstanceUtil.replaceSchemaPlaceholder(query, stateLevelTenantId);
		} catch (InvalidTenantIdException e1) {
			throw new CustomException("HRMS_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		}

		try {

			employeesIds = jdbcTemplate.queryForList(query, preparedStmtList.toArray(),String.class);
		}catch(Exception e) {
			log.error("Exception while making the db call: ",e);
			log.error("query; "+query);
		}
		return employeesIds;
	}

	private List<String> fetchEmployeesforJurisdiction(EmployeeSearchCriteria criteria, RequestInfo requestInfo, String stateLevelTenantId) {
		List<String> employeesIds = new ArrayList<>();
		List <Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getJurisdictionSearchQuery(criteria, preparedStmtList, requestInfo, stateLevelTenantId);

		try {
			query = centralInstanceUtil.replaceSchemaPlaceholder(query, stateLevelTenantId);
		} catch (InvalidTenantIdException e1) {
			throw new CustomException("HRMS_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		}

		try {
			employeesIds = jdbcTemplate.queryForList(query, preparedStmtList.toArray(),String.class);
		}catch(Exception e) {
			log.error("Exception while making the db call: ",e);
			log.error("query; "+query);
		}
		return employeesIds;
	}

	/**
	 * Fetches next value in the position seq table
	 * 
	 * @return
	 */
	public Long fetchPosition(){
		String query = queryBuilder.getPositionSeqQuery();
		Long id = null;
		try {
			id = jdbcTemplate.queryForObject(query, Long.class);
		}catch(Exception e) {
			log.error("Exception while making the db call: ",e);
			log.error("query; "+query);
		}
		return id;
	}

	/**
	 * DB Repository that makes jdbc calls to the db and fetches employee count.
	 *
	 * @param tenantId
	 * @return
	 */
	public Map<String,String> fetchEmployeeCount(String tenantId){
		Map<String,String> response = new HashMap<>();
		List<Object> preparedStmtList = new ArrayList<>();

		String query = queryBuilder.getEmployeeCountQuery(tenantId, preparedStmtList);
		String finalQuery;
		try {
			finalQuery = centralInstanceUtil.replaceSchemaPlaceholder(query, centralInstanceUtil.getStateLevelTenant(tenantId));
		} catch (InvalidTenantIdException e1) {
			throw new CustomException("HRMS_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		}


		log.info("query; "+finalQuery);
		try {
			response=jdbcTemplate.query(finalQuery, preparedStmtList.toArray(),countRowMapper);
		}catch(Exception e) {
			log.error("Exception while making the db call: ",e);
			log.error("query; "+ finalQuery);
		}
		return response;
	}

}
