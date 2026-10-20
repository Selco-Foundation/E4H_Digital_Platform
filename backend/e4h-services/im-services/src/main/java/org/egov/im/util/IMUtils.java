package org.egov.im.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.im.service.SLAService;
import org.egov.im.web.models.AuditDetails;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.IncidentRequestWrapper;
import org.egov.im.web.models.Priority;
import org.egov.im.web.models.workflow.ProcessInstance;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Component
public class IMUtils {


    private MultiStateInstanceUtil multiStateInstanceUtil;
    private SLAService slaService;

    @Autowired
    public IMUtils(MultiStateInstanceUtil multiStateInstanceUtil, SLAService slaService) {
        this.multiStateInstanceUtil = multiStateInstanceUtil;
        this.slaService = slaService;
    }

    /**
     * Method to return auditDetails for create/update flows
     *
     * @param by
     * @param isCreate
     * @return AuditDetails
     */
    public AuditDetails getAuditDetails(String by, Incident incident, Boolean isCreate) {
        log.trace("IMUtils::getAuditDetails method invoked, isCreate={}", isCreate);
        Long time = System.currentTimeMillis();
        if(isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().createdBy(incident.getAuditDetails().getCreatedBy()).lastModifiedBy(by)
                    .createdTime(incident.getAuditDetails().getCreatedTime()).lastModifiedTime(time).build();
    }

    /**
     * Method to fetch the state name from the tenantId
     *
     * @param query
     * @param tenantId
     * @return
     */
    public String replaceSchemaPlaceholder(String query, String tenantId) {
        log.trace("IMUtils::replaceSchemaPlaceholder method invoked");
        String finalQuery = null;

        try {
            finalQuery = multiStateInstanceUtil.replaceSchemaPlaceholder(query, tenantId);
        }
        catch (Exception e){
            log.error("Invalid tenantId for schema replacement: {}", tenantId, e);
            throw new CustomException("INVALID_TENANTID","Invalid tenantId: "+tenantId);
        }
        return finalQuery;
    }

    public ProcessInstance trimRolesFromProcessInstance(ProcessInstance processInstance) {
        log.trace("IMUtils::trimRolesFromProcessInstance method invoked");
        if(processInstance.getAssigner()!=null)
            processInstance.getAssigner().setRoles(new ArrayList<>());
        if (processInstance.getAssignes() != null) {
            processInstance.getAssignes().stream()
                    .filter(Objects::nonNull)
                    .forEach(assignee -> assignee.setRoles(new ArrayList<>()));
        }
        return processInstance;
    }

    public void updateBusinessService(IncidentRequestWrapper wrapper, Object mdmsData) {
        log.trace("IMUtils::updateBusinessService method invoked");
        if(wrapper.getProcessInstance().getBusinessService().equals("Incident")) {
            log.debug("Updating business service based on priority");
            Priority priority = slaService.getPriorityFromIMPriorityTable(wrapper.getIncidentRequest().getIncident());
            String businessService = "Incident_" + priority.toFormattedString();
            wrapper.getProcessInstance().setBusinessService(businessService);
            log.debug("Business service updated to: {}", businessService);
        }
    }

    /**
     * Adds a role to the caller for the duration of one downstream call, and reports whether it had to
     * be added. Used for system-driven workflow transitions, whose action is configured with the
     * SYSTEM role so that no human is offered it: without the role, egov-workflow-v2 rejects the
     * caller with INVALID ROLE.
     * <p>
     * The role carries the given tenantId because workflow matches roles per tenant
     * (WorkflowUtil.isRoleAvailable). Returns false when the caller already holds the role, so that
     * {@link #removeTransientRole} never strips a role the user genuinely has.
     */
    public boolean addTransientRole(RequestInfo requestInfo, String roleCode, String tenantId) {
        log.trace("IMUtils::addTransientRole method invoked for role: {}", roleCode);
        if (requestInfo == null || requestInfo.getUserInfo() == null) {
            log.warn("Cannot add transient role {}: no userInfo on the request", roleCode);
            return false;
        }
        User userInfo = requestInfo.getUserInfo();
        if (userInfo.getRoles() == null) {
            userInfo.setRoles(new ArrayList<>());
        }
        boolean alreadyHeld = userInfo.getRoles().stream()
                .filter(Objects::nonNull)
                .anyMatch(role -> roleCode.equals(role.getCode()) && isSameTenant(role.getTenantId(), tenantId));
        if (alreadyHeld) {
            return false;
        }
        userInfo.getRoles().add(Role.builder().code(roleCode).name(roleCode).tenantId(tenantId).build());
        log.debug("Added transient role {} for tenant {} to the request", roleCode, tenantId);
        return true;
    }

    /** Undoes {@link #addTransientRole}; the same RequestInfo is later serialised onto Kafka. */
    public void removeTransientRole(RequestInfo requestInfo, String roleCode, String tenantId) {
        log.trace("IMUtils::removeTransientRole method invoked for role: {}", roleCode);
        if (requestInfo == null || requestInfo.getUserInfo() == null || requestInfo.getUserInfo().getRoles() == null) {
            return;
        }
        requestInfo.getUserInfo().getRoles().removeIf(role -> role != null
                && roleCode.equals(role.getCode()) && isSameTenant(role.getTenantId(), tenantId));
        log.debug("Removed transient role {} for tenant {} from the request", roleCode, tenantId);
    }

    private boolean isSameTenant(String roleTenantId, String tenantId) {
        return roleTenantId == null ? tenantId == null : roleTenantId.equals(tenantId);
    }

    public String extractFacilityCode(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isEmpty()) {
            return null;
        }

        String[] parts = boundaryCode.split("_");
        String lastPart = parts[parts.length - 1];

        return lastPart.startsWith("FAC/") ? lastPart : null;
    }

}
