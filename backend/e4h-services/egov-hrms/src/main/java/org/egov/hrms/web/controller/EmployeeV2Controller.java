package org.egov.hrms.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.hrms.service.EmployeeService;
import org.egov.hrms.web.contract.EmployeeRequest;
import org.egov.hrms.web.contract.EmployeeResponse;
import org.egov.hrms.web.contract.EmployeeSearchCriteria;
import org.egov.hrms.web.contract.RequestInfoWrapper;
import org.egov.hrms.web.validator.EmployeeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * The role-aware employee API.
 *
 * The only difference from {@link EmployeeController} is that jurisdictions here carry the roles
 * the employee holds at each boundary, on both the request and the response. v1 keeps its original
 * shape so its callers are unaffected, and is not being deprecated.
 *
 * Versioning the contract does not version the storage: both controllers write the same tables, so
 * a v1 update can still remove roles a v2 caller set. What prevents that is the merge in
 * EmployeeService.enrichUpdateRequest, which is shared by both and is permanent for as long as v1
 * accepts writes. Do not treat it as transitional.
 *
 * _count and _update_username do not touch jurisdictions and stay on v1 only.
 */
@Slf4j
@RestController
@RequestMapping("/v2/employees")
public class EmployeeV2Controller {

	@Autowired
	private EmployeeService employeeService;

	@Autowired
	private EmployeeValidator validator;

	/**
	 * Maps Post Requests for _create & returns ResponseEntity of either
	 * EmployeeResponse type or ErrorResponse type
	 *
	 * @param employeeRequest
	 * @return ResponseEntity<?>
	 */
	@PostMapping(value = "/_create")
	@ResponseBody
	public ResponseEntity<?> create(@RequestBody @Valid EmployeeRequest employeeRequest) {
		log.trace("EmployeeV2Controller.create invoked");
		String tenantId = employeeRequest.getEmployees() != null && !employeeRequest.getEmployees().isEmpty()
				? employeeRequest.getEmployees().get(0).getTenantId() : "unknown";
		int employeeCount = employeeRequest.getEmployees() != null ? employeeRequest.getEmployees().size() : 0;
		log.info("Employee v2 create request received for {} employee(s), tenant: {}", employeeCount, tenantId);
		validator.validateCreateEmployee(employeeRequest);
		log.debug("Employee v2 create request validated successfully");
		EmployeeResponse employeeResponse = employeeService.create(employeeRequest);
		log.info("Employee v2 create request completed successfully");
		return new ResponseEntity<>(employeeResponse, HttpStatus.ACCEPTED);
	}

	/**
	 * Maps Post Requests for _update & returns ResponseEntity of either
	 * EmployeeResponse type or ErrorResponse type
	 *
	 * @param employeeRequest
	 * @return ResponseEntity<?>
	 */
	@PostMapping(value = "/_update")
	@ResponseBody
	public ResponseEntity<?> update(@RequestBody @Valid EmployeeRequest employeeRequest) {
		log.trace("EmployeeV2Controller.update invoked");
		String tenantId = employeeRequest.getEmployees().get(0).getTenantId();
		int employeeCount = employeeRequest.getEmployees().size();
		log.info("Employee v2 update request received for {} employee(s), tenant: {}", employeeCount, tenantId);
		validator.validateUpdateEmployee(employeeRequest);
		log.debug("Employee v2 update request validated successfully");
		EmployeeResponse employeeResponse = employeeService.update(employeeRequest);
		log.info("Employee v2 update request completed successfully");
		return new ResponseEntity<>(employeeResponse, HttpStatus.ACCEPTED);
	}

	/**
	 * Maps Post Requests for _search & returns ResponseEntity of either
	 * EmployeeResponse type or ErrorResponse type
	 *
	 * @param requestInfoWrapper
	 * @param criteria
	 * @return ResponseEntity<?>
	 */
	@PostMapping(value = "/_search")
	@ResponseBody
	public ResponseEntity<?> search(@RequestBody @Valid RequestInfoWrapper requestInfoWrapper, @ModelAttribute @Valid EmployeeSearchCriteria criteria, @RequestHeader Map<String, String> headers) {
		log.trace("EmployeeV2Controller.search invoked");
		String tenantId = criteria.getTenantId();
		log.info("Employee v2 search request received for tenant: {}", tenantId);
		// Set here rather than taken from the request: carrying the roles is what this endpoint is
		// for, and leaving it to the caller would make the response shape depend on a query param.
		criteria.setIncludeJurisdictionRoles(true);
		validator.validateSearchRequest(requestInfoWrapper.getRequestInfo(), criteria);
		log.debug("Employee v2 search request validated successfully");
		EmployeeResponse employeeResponse = employeeService.search(criteria, requestInfoWrapper.getRequestInfo());
		log.info("Employee v2 search request completed successfully");
		return new ResponseEntity<>(employeeResponse, HttpStatus.OK);
	}

}
