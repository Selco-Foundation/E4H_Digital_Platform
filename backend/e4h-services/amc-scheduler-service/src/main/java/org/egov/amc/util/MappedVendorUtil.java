package org.egov.amc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.service.ServiceRequestRepository;
import org.egov.amc.web.models.Employee;
import org.egov.amc.web.models.EmployeeResponse;
import org.egov.amc.web.models.User;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resolves which of a set of assignees is the "mapped vendor" - the AMC field staff, i.e. the
 * assignee whose HRMS user holds the role configured in {@code amc.mapped.vendor.role.code}.
 *
 * <p>Shared by the scheduled-visit search index (assignees of a visit) and the health facility
 * index (assignees of an AMC configuration), so both resolve the mapped vendor identically.
 */
@Slf4j
@Component
public class MappedVendorUtil {

    /**
     * Distinct assignee uuids resolved per egov-hrms search. Kept at the service's page cap so a
     * normal batch resolves in a single call.
     */
    private static final int MAPPED_VENDOR_UUID_BATCH_SIZE = 100;

    /** HRMS employees are registered against the national tenant. */
    private static final String HRMS_TENANT_ID = "in";

    private final AMCServiceConfiguration amcServiceConfiguration;
    private final ServiceRequestRepository requestRepository;
    private final ObjectMapper mapper;

    public MappedVendorUtil(AMCServiceConfiguration amcServiceConfiguration,
                            ServiceRequestRepository requestRepository,
                            @Qualifier("objectMapper") ObjectMapper mapper) {
        this.amcServiceConfiguration = amcServiceConfiguration;
        this.requestRepository = requestRepository;
        this.mapper = mapper;
    }

    /** The configured AMC field-staff role code, or null/blank when it is not configured. */
    public String getMappedVendorRoleCode() {
        return amcServiceConfiguration.getMappedVendorRoleCode();
    }

    /**
     * Looks up {@code assigneeUuids} in egov-hrms and returns, keyed by uuid, only those users that
     * hold {@code roleCode}. Degrades gracefully: a failed batch is logged and skipped, so callers
     * simply see fewer field staff rather than an exception.
     */
    public Map<String, User> getFieldStaffByUuid(RequestInfo requestInfo, List<String> assigneeUuids, String roleCode) {
        RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
        requestInfoWrapper.setRequestInfo(requestInfo);

        Map<String, User> fieldStaffByUuid = new HashMap<>();
        for (int from = 0; from < assigneeUuids.size(); from += MAPPED_VENDOR_UUID_BATCH_SIZE) {
            List<String> batch = assigneeUuids.subList(from,
                    Math.min(from + MAPPED_VENDOR_UUID_BATCH_SIZE, assigneeUuids.size()));
            String url = amcServiceConfiguration.getHrmsHost() + amcServiceConfiguration.getHrmsSearchUrl()
                    + "?tenantId=" + HRMS_TENANT_ID
                    + "&uuids=" + String.join(",", batch)
                    // Explicit limit: HRMS would otherwise fall back to its own default page size, which
                    // an environment is free to configure below the batch size and silently truncate.
                    // offset must be sent alongside it: egov-hrms computes its page bound as
                    // limit + offset (EmployeeQueryBuilder#paginationClause) and neither the controller
                    // nor its validator defaults offset, so a limit without one NPEs the whole search.
                    + "&offset=0"
                    + "&limit=" + MAPPED_VENDOR_UUID_BATCH_SIZE;

            try {
                Object response = requestRepository.fetchResult(new StringBuilder(url), requestInfoWrapper);
                EmployeeResponse employeeResponse = mapper.convertValue(response, EmployeeResponse.class);
                if (employeeResponse == null || employeeResponse.getEmployees() == null) {
                    continue;
                }
                for (Employee employee : employeeResponse.getEmployees()) {
                    User user = employee.getUser();
                    if (user != null && user.getUuid() != null && hasRole(user, roleCode)) {
                        fieldStaffByUuid.put(user.getUuid(), user);
                    }
                }
            } catch (Exception e) {
                log.error("HRMS lookup failed for {} assignee(s). Those records will carry no "
                        + "mapped vendor.", batch.size(), e);
            }
        }
        return fieldStaffByUuid;
    }

    /**
     * The first assignee in {@code assigneeUuids} that holds {@code roleCode}, or null when none do.
     * First match wins so the index carries a single, stable mapped vendor even when several
     * assignees (e.g. a reviewer also on the record) qualify.
     */
    public User resolveFieldStaff(RequestInfo requestInfo, List<String> assigneeUuids, String roleCode) {
        if (assigneeUuids == null || assigneeUuids.isEmpty()) {
            return null;
        }
        Map<String, User> fieldStaffByUuid = getFieldStaffByUuid(requestInfo, assigneeUuids, roleCode);
        for (String assigneeUuid : assigneeUuids) {
            User fieldStaff = fieldStaffByUuid.get(assigneeUuid);
            if (fieldStaff != null) {
                return fieldStaff;
            }
        }
        return null;
    }

    public boolean hasRole(User user, String roleCode) {
        if (user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream()
                .filter(Objects::nonNull)
                .anyMatch(role -> roleCode.equalsIgnoreCase(role.getCode()));
    }
}
