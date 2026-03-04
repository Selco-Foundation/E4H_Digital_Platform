/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) 2016  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.hrms.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.hrms.config.PropertiesManager;
import org.egov.hrms.model.AuditDetails;
import org.egov.hrms.model.Employee;
import org.egov.hrms.model.enums.UserType;
import org.egov.hrms.producer.HRMSProducer;
import org.egov.hrms.repository.EmployeeRepository;
import org.egov.hrms.utils.ErrorConstants;
import org.egov.hrms.utils.HRMSConstants;
import org.egov.hrms.utils.HRMSUtils;
import org.egov.hrms.utils.ResponseInfoFactory;
import org.egov.hrms.web.contract.EmployeeRequest;
import org.egov.hrms.web.contract.EmployeeResponse;
import org.egov.hrms.web.contract.EmployeeSearchCriteria;
import org.egov.hrms.web.contract.User;
import org.egov.hrms.web.contract.UserRequest;
import org.egov.hrms.web.contract.UserResponse;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
public class EmployeeService {


	@Autowired
	private UserService userService;

	@Autowired
	private IdGenService idGenService;

	@Autowired
	private ResponseInfoFactory factory;

	@Autowired
	private LogAwareKafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private HRMSProducer hrmsProducer;
	
	@Autowired
	private EmployeeRepository repository;
	
	@Autowired
	private HRMSUtils hrmsUtils;
	
	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private MultiStateInstanceUtil centralInstanceUtil;

	/**
	 * Service method for create employee. Does following:
	 * 1. Sets ids to all the objects using idgen service.
	 * 2. Enriches the employee object with required parameters
	 * 3. Creates user in the egov-user service.
	 * 4. Sends notification upon successful creation
	 * 
	 * @param employeeRequest
	 * @return
	 */
	public EmployeeResponse create(EmployeeRequest employeeRequest) {
		log.trace("EmployeeService.create invoked");
		RequestInfo requestInfo = employeeRequest.getRequestInfo();
		String tenantId = employeeRequest.getEmployees().get(0).getTenantId();
		int employeeCount = employeeRequest.getEmployees().size();
		log.info("Creating {} employee(s) for tenant {}", employeeCount, tenantId);
		
		Map<String, String> pwdMap = new HashMap<>();
		log.debug("Generating IDs for employees");
		idGenService.setIds(employeeRequest);
		employeeRequest.getEmployees().stream().forEach(employee -> {
			enrichCreateRequest(employee, requestInfo);
			createUser(employee, requestInfo);
			pwdMap.put(employee.getUuid(), employee.getUser().getPassword());
			employee.getUser().setPassword(null);
		});
		log.debug("Enriched {} employee(s) and created user accounts", employeeCount);
		String hrmsCreateTopic = propertiesManager.getSaveEmployeeTopic();
		log.info("Pushing employee data to Kafka topic: {}", hrmsCreateTopic);
		hrmsProducer.push(tenantId, hrmsCreateTopic, employeeRequest);
		log.debug("Sending notifications to employees");
		notificationService.sendNotification(employeeRequest, pwdMap);
		log.info("Successfully created {} employee(s)", employeeCount);
		return generateResponse(employeeRequest);
	}


	/**
	 * Searches employees on a given criteria.
	 * 
	 * @param criteria
	 * @param requestInfo
	 * @return
	 */
	public EmployeeResponse search(EmployeeSearchCriteria criteria, RequestInfo requestInfo) {
		log.trace("EmployeeService.search invoked");
		String tenantId = criteria.getTenantId();
		log.info("Searching employees for tenant {}", tenantId);
		boolean  userChecked = false;
		/*if(null == criteria.getIsActive() || criteria.getIsActive())
			criteria.setIsActive(true);
		else
			criteria.setIsActive(false);*/
        Map<String, User> mapOfUsers = new HashMap<String, User>();
		if(!StringUtils.isEmpty(criteria.getPhone()) || !CollectionUtils.isEmpty(criteria.getRoles())) {
            Map<String, Object> userSearchCriteria = new HashMap<>();
            userSearchCriteria.put(HRMSConstants.HRMS_USER_SEARCH_CRITERA_TENANTID,criteria.getTenantId());
            if(!StringUtils.isEmpty(criteria.getPhone()))
                userSearchCriteria.put(HRMSConstants.HRMS_USER_SEARCH_CRITERA_MOBILENO,criteria.getPhone());
            if( !CollectionUtils.isEmpty(criteria.getRoles()) )
                userSearchCriteria.put(HRMSConstants.HRMS_USER_SEARCH_CRITERA_ROLECODES,criteria.getRoles());
            UserResponse userResponse = userService.getUser(requestInfo, userSearchCriteria);
			userChecked =true;
            if(!CollectionUtils.isEmpty(userResponse.getUser())) {
                 mapOfUsers.putAll(userResponse.getUser().stream()
                        .collect(Collectors.toMap(User::getUuid, Function.identity())));
            }
			List<String> userUUIDs = userResponse.getUser().stream().map(User :: getUuid).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(criteria.getUuids()))
                criteria.setUuids(criteria.getUuids().stream().filter(userUUIDs::contains).collect(Collectors.toList()));
            else
                criteria.setUuids(userUUIDs);
		}
		//checks if above criteria met and result is not  null will check for name search if list of names are given as user search on name is not bulk api

		if(!((!CollectionUtils.isEmpty(criteria.getRoles()) || !StringUtils.isEmpty(criteria.getPhone())) && CollectionUtils.isEmpty(criteria.getUuids()))){
			if(!CollectionUtils.isEmpty(criteria.getNames())) {
				List<String> userUUIDs = new ArrayList<>();
				for(String name: criteria.getNames()) {
					Map<String, Object> userSearchCriteria = new HashMap<>();
					userSearchCriteria.put(HRMSConstants.HRMS_USER_SEARCH_CRITERA_TENANTID,criteria.getTenantId());
					userSearchCriteria.put(HRMSConstants.HRMS_USER_SEARCH_CRITERA_NAME,name);
					UserResponse userResponse = userService.getUser(requestInfo, userSearchCriteria);
					userChecked =true;
					if(!CollectionUtils.isEmpty(userResponse.getUser())) {
						mapOfUsers.putAll(userResponse.getUser().stream()
								.collect(Collectors.toMap(User::getUuid, Function.identity())));
					}
					List<String> uuids = userResponse.getUser().stream().map(User :: getUuid).collect(Collectors.toList());
					userUUIDs.addAll(uuids);
				}
				if(!CollectionUtils.isEmpty(criteria.getUuids()))
					criteria.setUuids(criteria.getUuids().stream().filter(userUUIDs::contains).collect(Collectors.toList()));
				else
					criteria.setUuids(userUUIDs);
			}
		}

		String stateLevelTenantId = criteria.getTenantId();
		if(userChecked)
			criteria.setTenantId(null);

		// Early return: If both roles and boundaryCodes are provided, and no users found with those roles, return empty list
		if(!CollectionUtils.isEmpty(criteria.getRoles()) && !CollectionUtils.isEmpty(criteria.getBoundaryCodes()) 
				&& CollectionUtils.isEmpty(criteria.getUuids())) {
			return EmployeeResponse.builder().responseInfo(factory.createResponseInfoFromRequestInfo(requestInfo, true))
					.employees(new ArrayList<>()).build();
		}

		List <Employee> employees = new ArrayList<>();
        if(!((!CollectionUtils.isEmpty(criteria.getRoles()) || !CollectionUtils.isEmpty(criteria.getNames()) || !StringUtils.isEmpty(criteria.getPhone())) && CollectionUtils.isEmpty(criteria.getUuids()))) {
            log.debug("Fetching employees from repository");
            employees = repository.fetchEmployees(criteria, requestInfo, stateLevelTenantId);
        }
        
        // If both roles and boundaryCodes are provided, and no employees found in that boundary, return empty list
        if(!CollectionUtils.isEmpty(criteria.getRoles()) && !CollectionUtils.isEmpty(criteria.getBoundaryCodes()) 
        		&& CollectionUtils.isEmpty(employees)) {
        	return EmployeeResponse.builder().responseInfo(factory.createResponseInfoFromRequestInfo(requestInfo, true))
        			.employees(new ArrayList<>()).build();
        }
        
        List<String> uuids = employees.stream().map(Employee :: getUuid).collect(Collectors.toList());
		if(!CollectionUtils.isEmpty(uuids)){
            Map<String, Object> UserSearchCriteria = new HashMap<>();
            UserSearchCriteria.put(HRMSConstants.HRMS_USER_SEARCH_CRITERA_UUID,uuids);
            UserResponse userResponse = userService.getUser(requestInfo, UserSearchCriteria);
			if(!CollectionUtils.isEmpty(userResponse.getUser())) {
				// Merge with existing mapOfUsers instead of replacing it
				Map<String, User> fetchedUsers = userResponse.getUser().stream()
						.collect(Collectors.toMap(User :: getUuid, Function.identity()));
				mapOfUsers.putAll(fetchedUsers);
            }
            for(Employee employee: employees){
                employee.setUser(mapOfUsers.get(employee.getUuid()));
            }
            
            // Filter employees to ensure they have the requested roles
            if(!CollectionUtils.isEmpty(criteria.getRoles()) && !CollectionUtils.isEmpty(employees)) {
                List<String> requestedRoleCodes = criteria.getRoles();
                employees = employees.stream()
                    .filter(employee -> {
                        if(employee.getUser() == null || CollectionUtils.isEmpty(employee.getUser().getRoles())) {
                            return false;
                        }
                        // Check if employee's user has any of the requested roles
                        return employee.getUser().getRoles().stream()
                            .anyMatch(role -> requestedRoleCodes.contains(role.getCode()));
                    })
                    .collect(Collectors.toList());
            }
		}
		log.info("Employee search completed, found {} employee(s)", employees.size());
		return EmployeeResponse.builder().responseInfo(factory.createResponseInfoFromRequestInfo(requestInfo, true))
				.employees(employees).build();
	}
	
	
	/**
	 * Creates user by making call to egov-user.
	 * 
	 * @param employee
	 * @param requestInfo
	 */
	private void createUser(Employee employee, RequestInfo requestInfo) {
		log.trace("EmployeeService.createUser invoked for employee code: {}", employee.getCode());
		enrichUser(employee);
		UserRequest request = UserRequest.builder().requestInfo(requestInfo).user(employee.getUser()).build();
		try {
			log.debug("Creating user for employee code: {}, tenant: {}", employee.getCode(), employee.getTenantId());
			UserResponse response = userService.createUser(request);
			User user = response.getUser().get(0);
			employee.setId(user.getId());
			employee.setUuid(user.getUuid());
			employee.getUser().setId(user.getId());
			employee.getUser().setUuid(user.getUuid());
			log.debug("User created successfully for employee code: {}, uuid: {}", employee.getCode(), user.getUuid());
		}catch(Exception e) {
			log.error("Exception while creating user for employee code: {}, tenant: {}", 
					employee.getCode(), employee.getTenantId(), e);
			throw new CustomException(ErrorConstants.HRMS_USER_CREATION_FAILED_CODE, ErrorConstants.HRMS_USER_CREATION_FAILED_MSG);
		}

	}

	/**
	 * Enriches the user object.
	 * 
	 * @param employee
	 */
	private void enrichUser(Employee employee) {
		log.trace("EmployeeService.enrichUser invoked for employee code: {}", employee.getCode());
		List<String> pwdParams = new ArrayList<>();
		pwdParams.add(employee.getCode());
		pwdParams.add(employee.getUser().getMobileNumber());
		pwdParams.add(employee.getTenantId());
		pwdParams.add(employee.getUser().getName().toUpperCase());
		employee.getUser().setPassword(hrmsUtils.generatePassword(pwdParams));
		employee.getUser().setUserName(employee.getCode());
		employee.getUser().setActive(true);
		employee.getUser().setType(UserType.EMPLOYEE.toString());
		log.debug("User enriched for employee code: {}, username: {}", employee.getCode(), employee.getUser().getUserName());
	}

	/**
	 * Enriches employee object by setting parent ids to all the child objects
	 * 
	 * @param employee
	 * @param requestInfo
	 */
	private void enrichCreateRequest(Employee employee, RequestInfo requestInfo) {
		log.trace("EmployeeService.enrichCreateRequest invoked for employee code: {}", employee.getCode());

		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(requestInfo.getUserInfo().getUuid())
				.createdDate(new Date().getTime())
				.build();
		
		employee.getJurisdictions().stream().forEach(jurisdiction -> {
			jurisdiction.setId(UUID.randomUUID().toString());
			jurisdiction.setAuditDetails(auditDetails);
			if(null == jurisdiction.getIsActive())
				jurisdiction.setIsActive(true);
		});
		employee.getAssignments().stream().forEach(assignment -> {
			assignment.setId(UUID.randomUUID().toString());
			assignment.setAuditDetails(auditDetails);
			assignment.setPosition(getPosition());
		});
		if(!CollectionUtils.isEmpty(employee.getServiceHistory())) {
			employee.getServiceHistory().stream().forEach(serviceHistory -> {
				serviceHistory.setId(UUID.randomUUID().toString());
				serviceHistory.setAuditDetails(auditDetails);
				if(null == serviceHistory.getIsCurrentPosition())
					serviceHistory.setIsCurrentPosition(false);
			});
		}
		if(!CollectionUtils.isEmpty(employee.getEducation())) {
			employee.getEducation().stream().forEach(educationalQualification -> {
				educationalQualification.setId(UUID.randomUUID().toString());
				educationalQualification.setAuditDetails(auditDetails);
				if(null == educationalQualification.getIsActive())
					educationalQualification.setIsActive(true);
			});
		}
		if(!CollectionUtils.isEmpty(employee.getTests())) {
			employee.getTests().stream().forEach(departmentalTest -> {
				departmentalTest.setId(UUID.randomUUID().toString());
				departmentalTest.setAuditDetails(auditDetails);
				if(null == departmentalTest.getIsActive())
					departmentalTest.setIsActive(true);
			});
		}
		if(!CollectionUtils.isEmpty(employee.getDocuments())) {
			employee.getDocuments().stream().forEach(document -> {
				document.setId(UUID.randomUUID().toString());
				document.setAuditDetails(auditDetails);
			});
		}
		employee.setAuditDetails(auditDetails);
		employee.setIsActive(true);
	}
	
	/**
	 * Fetches next value from the position sequence table
	 * @return
	 */
	public Long getPosition() {
		return repository.fetchPosition();
	}

	/**
	 * Service method to update user. Performs the following:
	 * 1. Enriches the employee object with required parameters.
	 * 2. Updates user by making call to the user service.
	 * 
	 * @param employeeRequest
	 * @return
	 */
	public EmployeeResponse update(EmployeeRequest employeeRequest) {
		log.trace("EmployeeService.update invoked");
		RequestInfo requestInfo = employeeRequest.getRequestInfo();
		String tenantId = employeeRequest.getEmployees().get(0).getTenantId();
		int employeeCount = employeeRequest.getEmployees().size();
		log.info("Updating {} employee(s) for tenant {}", employeeCount, tenantId);
		
		List <String> uuidList= new ArrayList<>();
		for(Employee employee: employeeRequest.getEmployees()) {
			uuidList.add(employee.getUuid());
		}
		log.debug("Fetching existing employee data for {} uuid(s)", uuidList.size());
		EmployeeResponse existingEmployeeResponse = search(EmployeeSearchCriteria.builder().uuids(uuidList).tenantId(tenantId).build(),requestInfo);
		List <Employee> existingEmployees = existingEmployeeResponse.getEmployees();
		log.debug("Found {} existing employee(s)", existingEmployees.size());
		employeeRequest.getEmployees().stream().forEach(employee -> {
			enrichUpdateRequest(employee, requestInfo, existingEmployees);
			updateUser(employee, requestInfo);
		});
		log.debug("Enriched {} employee(s) and updated user accounts", employeeCount);
		String hrmsUpdateTopic = propertiesManager.getUpdateEmployeeTopic();
		log.info("Pushing updated employee data to Kafka topic: {}", hrmsUpdateTopic);
		hrmsProducer.push(tenantId, hrmsUpdateTopic, employeeRequest);
		//notificationService.sendReactivationNotification(employeeRequest);
		log.info("Successfully updated {} employee(s)", employeeCount);
		return generateResponse(employeeRequest);
	}
	
	/**
	 * Updates the user by making call to the user service.
	 * 
	 * @param employee
	 * @param requestInfo
	 */
	private void updateUser(Employee employee, RequestInfo requestInfo) {
		log.trace("EmployeeService.updateUser invoked for employee uuid: {}", employee.getUuid());
		UserRequest request = UserRequest.builder().requestInfo(requestInfo).user(employee.getUser()).build();
		try {
			log.debug("Updating user for employee uuid: {}, tenant: {}", employee.getUuid(), employee.getTenantId());
			userService.updateUser(request);
			log.debug("User updated successfully for employee uuid: {}", employee.getUuid());
		}catch(Exception e) {
			log.error("Exception while updating user for employee uuid: {}, tenant: {}", 
					employee.getUuid(), employee.getTenantId(), e);
			throw new CustomException(ErrorConstants.HRMS_USER_UPDATION_FAILED_CODE, ErrorConstants.HRMS_USER_UPDATION_FAILED_MSG);
		}

	}

	/**
	 * Enriches update request with required parameters.
	 * 
	 * @param employee
	 * @param requestInfo
	 * @param existingEmployeesData
	 */
	private void enrichUpdateRequest(Employee employee, RequestInfo requestInfo, List<Employee> existingEmployeesData) {
		log.trace("EmployeeService.enrichUpdateRequest invoked for employee uuid: {}", employee.getUuid());
		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(requestInfo.getUserInfo().getUserName())
				.createdDate(new Date().getTime())
				.build();
		Employee existingEmpData = existingEmployeesData.stream().filter(existingEmployee -> existingEmployee.getUuid().equals(employee.getUuid())).findFirst().get();

		employee.getUser().setUserName(employee.getCode());
		if(!employee.getIsActive())
			employee.getUser().setActive(false);
		else
			employee.getUser().setActive(true);

		employee.getJurisdictions().stream().forEach(jurisdiction -> {

			if(null == jurisdiction.getIsActive())
				jurisdiction.setIsActive(true);
			if(jurisdiction.getId()==null) {
				jurisdiction.setId(UUID.randomUUID().toString());
				jurisdiction.setAuditDetails(auditDetails);
			}else{
				if(!existingEmpData.getJurisdictions().stream()
						.filter(jurisdictionData ->jurisdictionData.getId().equals(jurisdiction.getId() ))
						.findFirst().orElse(null)
						.equals(jurisdiction)){
					jurisdiction.getAuditDetails().setLastModifiedBy(requestInfo.getUserInfo().getUserName());
					jurisdiction.getAuditDetails().setLastModifiedDate(new Date().getTime());
				}
			}
		});
		employee.getAssignments().stream().forEach(assignment -> {
			if(assignment.getId()==null) {
				assignment.setId(UUID.randomUUID().toString());
				assignment.setAuditDetails(auditDetails);
			}else {
				if(!existingEmpData.getAssignments().stream()
						.filter(assignmentData -> assignmentData.getId().equals(assignment.getId()))
						.findFirst().orElse(null)
						.equals(assignment)){
					assignment.getAuditDetails().setLastModifiedBy(requestInfo.getUserInfo().getUserName());
					assignment.getAuditDetails().setLastModifiedDate(new Date().getTime());
				}
			}
		});

		if(employee.getServiceHistory()!=null){
			employee.getServiceHistory().stream().forEach(serviceHistory -> {
				if(null == serviceHistory.getIsCurrentPosition())
					serviceHistory.setIsCurrentPosition(false);
				if(serviceHistory.getId()==null) {
					serviceHistory.setId(UUID.randomUUID().toString());
					serviceHistory.setAuditDetails(auditDetails);
				}else {
					if(!existingEmpData.getServiceHistory().stream()
							.filter(serviceHistoryData -> serviceHistoryData.getId().equals(serviceHistory.getId()))
							.findFirst().orElse(null)
							.equals(serviceHistory)){
						serviceHistory.getAuditDetails().setLastModifiedBy(requestInfo.getUserInfo().getUserName());
						serviceHistory.getAuditDetails().setLastModifiedDate(new Date().getTime());
					}
				}
			});

		}

		if(employee.getEducation() != null){
			employee.getEducation().stream().forEach(educationalQualification -> {
				if(null == educationalQualification.getIsActive())
					educationalQualification.setIsActive(true);
				if(educationalQualification.getId()==null) {
					educationalQualification.setId(UUID.randomUUID().toString());
					educationalQualification.setAuditDetails(auditDetails);
				}else {

					if(!existingEmpData.getEducation().stream()
							.filter(educationalQualificationData -> educationalQualificationData.getId().equals(educationalQualification.getId()))
							.findFirst().orElse(null)
							.equals(educationalQualification)){
						educationalQualification.getAuditDetails().setLastModifiedBy(requestInfo.getUserInfo().getUserName());
						educationalQualification.getAuditDetails().setLastModifiedDate(new Date().getTime());
					}
				}
			});

		}

		if(employee.getTests() != null){
			employee.getTests().stream().forEach(departmentalTest -> {

				if(null == departmentalTest.getIsActive())
					departmentalTest.setIsActive(true);
				if(departmentalTest.getId()==null) {
					departmentalTest.setId(UUID.randomUUID().toString());
					departmentalTest.setAuditDetails(auditDetails);
				}else {
					if(!existingEmpData.getTests().stream()
							.filter(departmentalTestData -> departmentalTestData.getId().equals(departmentalTest.getId()))
							.findFirst().orElse(null)
							.equals(departmentalTest)){
						departmentalTest.getAuditDetails().setLastModifiedBy(requestInfo.getUserInfo().getUserName());
						departmentalTest.getAuditDetails().setLastModifiedDate(new Date().getTime());
					}
				}
			});

		}

		if(employee.getDocuments() != null){
			employee.getDocuments().stream().forEach(document -> {
				if(document.getId()==null) {
					document.setId(UUID.randomUUID().toString());
					document.setAuditDetails(auditDetails);
				}else {
					if(!existingEmpData.getDocuments().stream()
							.filter(documentData -> documentData.getId().equals(document.getId()))
							.findFirst().orElse(null)
							.equals(document)){
						document.getAuditDetails().setLastModifiedBy(requestInfo.getUserInfo().getUserName());
						document.getAuditDetails().setLastModifiedDate(new Date().getTime());
					}
				}
			});

		}

		if(employee.getDeactivationDetails() != null){
			employee.getDeactivationDetails().stream().forEach(deactivationDetails -> {
				if(deactivationDetails.getId()==null) {
					deactivationDetails.setId(UUID.randomUUID().toString());
					deactivationDetails.setAuditDetails(auditDetails);
					employee.getDocuments().forEach(employeeDocument -> {
						employeeDocument.setReferenceId( deactivationDetails.getId());
					});
				}else {
					if(!existingEmpData.getDeactivationDetails().stream()
							.filter(deactivationDetailsData -> deactivationDetailsData.getId().equals(deactivationDetails.getId()))
							.findFirst().orElse(null)
							.equals(deactivationDetails)){
						deactivationDetails.getAuditDetails().setLastModifiedBy(requestInfo.getUserInfo().getUserName());
						deactivationDetails.getAuditDetails().setLastModifiedDate(new Date().getTime());
					}
				}
			});

		}
		if(employee.getReactivationDetails() != null){
			employee.getReactivationDetails().stream().forEach(reactivationDetails -> {
				if(reactivationDetails.getId() == null){
					reactivationDetails.setId(UUID.randomUUID().toString());
					reactivationDetails.setAuditDetails(auditDetails);
					employee.getDocuments().forEach(employeeDocument -> {
						employeeDocument.setReferenceId(reactivationDetails.getId());
					});
				}
				else{
					if(!existingEmpData.getReactivationDetails().stream()
							.filter(reactivationDetails1 -> reactivationDetails1.getId().equals(reactivationDetails.getId()))
							.findFirst().orElse(null)
							.equals(reactivationDetails)){
						reactivationDetails.getAuditDetails().setLastModifiedBy(requestInfo.getUserInfo().getUserName());
						reactivationDetails.getAuditDetails().setLastModifiedDate(new Date().getTime());
					}
				}
			});

		}


	}

	private EmployeeResponse generateResponse(EmployeeRequest employeeRequest) {
		log.trace("EmployeeService.generateResponse invoked");
		return EmployeeResponse.builder()
				.responseInfo(factory.createResponseInfoFromRequestInfo(employeeRequest.getRequestInfo(), true))
				.employees(employeeRequest.getEmployees()).build();
	}

	public Map<String,Object> getEmployeeCountResponse(RequestInfo requestInfo, String tenantId){
		log.trace("EmployeeService.getEmployeeCountResponse invoked for tenant: {}", tenantId);
		Map<String,Object> response = new HashMap<>();
		Map<String,String> results = new HashMap<>();
		ResponseInfo responseInfo = factory.createResponseInfoFromRequestInfo(requestInfo, true);

		response.put("ResponseInfo",responseInfo);
		log.debug("Fetching employee count for tenant: {}", tenantId);
		results	= repository.fetchEmployeeCount(tenantId);

		if(CollectionUtils.isEmpty(results) || results.get("totalEmployee").equalsIgnoreCase("0")){
			log.warn("No employee records found for tenant: {}", tenantId);
			Map<String,String> error = new HashMap<>();
			error.put("NO_RECORDS","No records found for the tenantId: "+tenantId);
			throw new CustomException(error);
		}

		log.info("Employee count retrieved for tenant: {}, count: {}", tenantId, results.get("totalEmployee"));
		response.put("EmployeCount",results);
		return  response;
	}

}