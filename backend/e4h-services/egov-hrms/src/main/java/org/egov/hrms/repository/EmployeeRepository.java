package org.egov.hrms.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.exception.InvalidTenantIdException;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.hrms.model.Employee;
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
		log.trace("EmployeeRepository.fetchEmployees invoked");
		List<Employee> employees = new ArrayList<>();
		List<Object> preparedStmtList = new ArrayList<>();

		if(hrmsUtils.isAssignmentSearchReqd(criteria)) {
			log.debug("Assignment search required, fetching employees by assignment");
			List<String> empUuids = fetchEmployeesforAssignment(criteria, requestInfo, stateLevelTenantId);
			if (CollectionUtils.isEmpty(empUuids)) {
				log.debug("No employees found for assignment criteria");
				return employees;
			}
			else {
				log.debug("Found {} employee uuid(s) for assignment criteria", empUuids.size());
				if(!CollectionUtils.isEmpty(criteria.getUuids()))
					criteria.setUuids(criteria.getUuids().stream().filter(empUuids::contains).collect(Collectors.toList()));
				else
					criteria.setUuids(empUuids);
			}
		}

		if(hrmsUtils.isJurisdictionSearchReqd(criteria)) {
			log.debug("Jurisdiction search required, fetching employees by jurisdiction");
			List<String> empUuids = fetchEmployeesforJurisdiction(criteria, requestInfo, stateLevelTenantId);
			if (CollectionUtils.isEmpty(empUuids)) {
				log.debug("No employees found for jurisdiction criteria");
				return employees;
			}
			else {
				log.debug("Found {} employee uuid(s) for jurisdiction criteria", empUuids.size());
				if(!CollectionUtils.isEmpty(criteria.getUuids())) {
					List<String> filteredUuids = criteria.getUuids().stream().filter(empUuids::contains).collect(Collectors.toList());
					// If both roles and boundaryCodes are provided, and no employees match both criteria, return empty list
					if(!CollectionUtils.isEmpty(criteria.getRoles()) && CollectionUtils.isEmpty(filteredUuids)) {
						log.debug("No employees match both roles and boundary criteria");
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
			log.error("Invalid tenant ID exception for state level tenant: {}", stateLevelTenantId, e1);
			throw new CustomException("HRMS_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");		
		}

		try {
			log.debug("Executing employee search query for tenant: {}", stateLevelTenantId);
			employees = jdbcTemplate.query(finalQuery, preparedStmtList.toArray(),rowMapper);
			log.debug("Employee search query executed successfully, returned {} employee(s)", employees.size());
		}catch(Exception e) {
			log.error("Exception while executing employee search query for tenant: {}", stateLevelTenantId, e);
		}
		return employees;
	}

	private List<String> fetchEmployeesforAssignment(EmployeeSearchCriteria criteria, RequestInfo requestInfo, String stateLevelTenantId) {
		log.trace("EmployeeRepository.fetchEmployeesforAssignment invoked");
		List<String> employeesIds = new ArrayList<>();
		List <Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getAssignmentSearchQuery(criteria, preparedStmtList);

		try {
			query = centralInstanceUtil.replaceSchemaPlaceholder(query, stateLevelTenantId);
		} catch (InvalidTenantIdException e1) {
			log.error("Invalid tenant ID exception for assignment search, tenant: {}", stateLevelTenantId, e1);
			throw new CustomException("HRMS_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		}

		try {
			log.debug("Executing assignment search query for tenant: {}", stateLevelTenantId);
			employeesIds = jdbcTemplate.queryForList(query, preparedStmtList.toArray(),String.class);
			log.debug("Assignment search query executed successfully, returned {} uuid(s)", employeesIds.size());
		}catch(Exception e) {
			log.error("Exception while executing assignment search query for tenant: {}", stateLevelTenantId, e);
		}
		return employeesIds;
	}

	private List<String> fetchEmployeesforJurisdiction(EmployeeSearchCriteria criteria, RequestInfo requestInfo, String stateLevelTenantId) {
		log.trace("EmployeeRepository.fetchEmployeesforJurisdiction invoked");
		List<String> employeesIds = new ArrayList<>();
		List <Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getJurisdictionSearchQuery(criteria, preparedStmtList, requestInfo, stateLevelTenantId);

		try {
			query = centralInstanceUtil.replaceSchemaPlaceholder(query, stateLevelTenantId);
		} catch (InvalidTenantIdException e1) {
			log.error("Invalid tenant ID exception for jurisdiction search, tenant: {}", stateLevelTenantId, e1);
			throw new CustomException("HRMS_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		}

		try {
			log.debug("Executing jurisdiction search query for tenant: {}", stateLevelTenantId);
			employeesIds = jdbcTemplate.queryForList(query, preparedStmtList.toArray(),String.class);
			log.debug("Jurisdiction search query executed successfully, returned {} uuid(s)", employeesIds.size());
		}catch(Exception e) {
			log.error("Exception while executing jurisdiction search query for tenant: {}", stateLevelTenantId, e);
		}
		return employeesIds;
	}

	/**
	 * Fetches next value in the position seq table
	 * 
	 * @return
	 */
	public Long fetchPosition(){
		log.trace("EmployeeRepository.fetchPosition invoked");
		String query = queryBuilder.getPositionSeqQuery();
		Long id = null;
		try {
			log.debug("Fetching position from sequence");
			id = jdbcTemplate.queryForObject(query, Long.class);
			log.debug("Position fetched successfully: {}", id);
		}catch(Exception e) {
			log.error("Exception while fetching position from sequence", e);
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
		log.trace("EmployeeRepository.fetchEmployeeCount invoked for tenant: {}", tenantId);
		Map<String,String> response = new HashMap<>();
		List<Object> preparedStmtList = new ArrayList<>();

		String query = queryBuilder.getEmployeeCountQuery(tenantId, preparedStmtList);
		String finalQuery;
		try {
			finalQuery = centralInstanceUtil.replaceSchemaPlaceholder(query, centralInstanceUtil.getStateLevelTenant(tenantId));
		} catch (InvalidTenantIdException e1) {
			log.error("Invalid tenant ID exception for employee count, tenant: {}", tenantId, e1);
			throw new CustomException("HRMS_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		}

		try {
			log.debug("Executing employee count query for tenant: {}", tenantId);
			response=jdbcTemplate.query(finalQuery, preparedStmtList.toArray(),countRowMapper);
			log.debug("Employee count query executed successfully");
		}catch(Exception e) {
			log.error("Exception while executing employee count query for tenant: {}", tenantId, e);
		}
		return response;
	}

}
