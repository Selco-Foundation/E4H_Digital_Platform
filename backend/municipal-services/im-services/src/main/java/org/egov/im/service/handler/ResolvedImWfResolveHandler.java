package org.egov.im.service.handler;

import lombok.NoArgsConstructor;
import org.egov.im.service.UserService;
import org.egov.im.service.WorkflowService;
import org.egov.im.util.IMConstants;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.egov.im.util.IMConstants.ASSIGN;
import static org.egov.im.util.IMConstants.IM_WF_RESOLVE;

@Component("RESOLVED_IM_WF_RESOLVE")
@NoArgsConstructor
public class ResolvedImWfResolveHandler implements WorkflowActionHandler {

    @Autowired
    private WorkflowService workflowService;

    @Override
    public NotificationContext handle(IncidentRequest request, UserService userService) {
        String tenantId = request.getIncident().getTenantId();
        String incidentId = request.getIncident().getIncidentId();

        final StringBuilder url = workflowService.getprocessInstanceSearchURL(tenantId, incidentId);
        url.append("&").append("history=true");

        Map<String, String> employeeMobileNumber
                = userService.getHRMSEmployee(request, IMConstants.ROLE_COMPLAINANT);
        String citizenMobileNumber
                = userService.getAssignerMobileNumber(request, IM_WF_RESOLVE, url);
        String crmMobileNumber
                = userService.getAssignerMobileNumber(request, ASSIGN, url);
        return new NotificationContext(
                employeeMobileNumber.get("employeeMobile"),
                citizenMobileNumber,
                crmMobileNumber);
    }
}
