package org.egov.im.util;

import lombok.extern.slf4j.Slf4j;
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
}
