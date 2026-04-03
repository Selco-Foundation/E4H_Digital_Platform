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

package org.egov.hrms.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.hrms.service.EmployeeService;
import org.egov.hrms.web.contract.EmployeeRequest;
import org.egov.hrms.web.contract.EmployeeResponse;
import org.egov.hrms.web.contract.EmployeeSearchCriteria;
import org.egov.hrms.web.contract.RequestInfoWrapper;
import org.egov.hrms.web.validator.EmployeeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/employees")
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;
	
	@Autowired
	private EmployeeValidator validator;


	/**
	 * Maps Post Requests for _create & returns ResponseEntity of either
	 * EmployeeResponse type or ErrorResponse type
	 *
	 * @param employeeRequest
	 * @param bindingResult
	 * @return ResponseEntity<?>
	 */
	@PostMapping(value = "/_create")
	@ResponseBody
	public ResponseEntity<?> create(@RequestBody @Valid EmployeeRequest employeeRequest) {
		log.trace("EmployeeController.create invoked");
		String tenantId = employeeRequest.getEmployees() != null && !employeeRequest.getEmployees().isEmpty() 
				? employeeRequest.getEmployees().get(0).getTenantId() : "unknown";
		int employeeCount = employeeRequest.getEmployees() != null ? employeeRequest.getEmployees().size() : 0;
		log.info("Employee create request received for {} employee(s), tenant: {}", employeeCount, tenantId);
		validator.validateCreateEmployee(employeeRequest);
		log.debug("Employee create request validated successfully");
		EmployeeResponse employeeResponse = employeeService.create(employeeRequest);
		log.info("Employee create request completed successfully");
        return new ResponseEntity<>(employeeResponse, HttpStatus.ACCEPTED);
	}


	/**
	 * Maps Post Requests for _update & returns ResponseEntity of either
	 * EmployeeResponse type or ErrorResponse type
	 *
	 * @param employeeRequest
	 * @param bindingResult
	 * @return ResponseEntity<?>
	 */
	@PostMapping(value = "/_update")
	@ResponseBody
	public ResponseEntity<?> update(@RequestBody @Valid EmployeeRequest employeeRequest) {
		log.trace("EmployeeController.update invoked");
		String tenantId = employeeRequest.getEmployees().get(0).getTenantId();
		log.info("Employee update request received, tenant: {}", tenantId);
		validator.validateUpdateEmployee(employeeRequest);
		log.debug("Employee update request validated successfully");
		EmployeeResponse employeeResponse = employeeService.update(employeeRequest);
		log.info("Employee update request completed successfully");
		return new ResponseEntity<>(employeeResponse, HttpStatus.ACCEPTED);
	}
	
	
	/**
	 * Maps Post Requests for _search & returns ResponseEntity of either
	 * EmployeeResponse type or ErrorResponse type
	 *
	 * @param criteria
	 * @param bindingResult
	 * @return ResponseEntity<?>
	 */
	@PostMapping(value = "/_search")
	@ResponseBody
	public ResponseEntity<?> search(@RequestBody @Valid RequestInfoWrapper requestInfoWrapper, @ModelAttribute @Valid EmployeeSearchCriteria criteria, @RequestHeader Map<String, String> headers) {
		log.trace("EmployeeController.search invoked");
		String tenantId = criteria.getTenantId();
		log.info("Employee search request received for tenant: {}", tenantId);
		validator.validateSearchRequest(requestInfoWrapper.getRequestInfo(), criteria);
		log.debug("Employee search request validated successfully");
		EmployeeResponse employeeResponse = employeeService.search(criteria, requestInfoWrapper.getRequestInfo());
		log.info("Employee search request completed successfully");
		return new ResponseEntity<>(employeeResponse,HttpStatus.OK);
	}

	@PostMapping("_count")
	@ResponseBody
	private ResponseEntity<?> count(@RequestParam("tenantId") String tenantId, @RequestBody RequestInfo requestInfo) {
		log.trace("EmployeeController.count invoked for tenant: {}", tenantId);
		log.info("Employee count request received for tenant: {}", tenantId);
		Map<String,Object> response = new HashMap<>();
		validator.validateEmployeeCountRequest(tenantId);
		log.debug("Employee count request validated successfully");
		response = employeeService.getEmployeeCountResponse(requestInfo,tenantId);
		log.info("Employee count request completed successfully for tenant: {}", tenantId);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}


}