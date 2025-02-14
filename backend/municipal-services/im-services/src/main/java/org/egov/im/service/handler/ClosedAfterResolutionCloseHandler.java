package org.egov.im.service.handler;


import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.egov.im.service.UserService;
import org.egov.im.service.WorkflowService;
import org.egov.im.util.IMConstants;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.egov.im.util.IMConstants.IM_WF_RESOLVE;

@Component("CLOSED_AFTER_RESOLUTION_CLOSE")
@RequiredArgsConstructor
public class ClosedAfterResolutionCloseHandler implements WorkflowActionHandler {

    private  WorkflowService workflowService;

    @Override
    public NotificationContext handle(IncidentRequest request, UserService userService) {
        String tenantId = request.getIncident().getTenantId();
        String incidentId = request.getIncident().getIncidentId();

        final StringBuilder url = workflowService.getprocessInstanceSearchURL(tenantId, incidentId);
        url.append("&").append("history=true");

        String employeeMobileNumber
                = userService.getAssignerMobileNumber(request, IM_WF_RESOLVE, url);

        Map<String, String> citizenMobileNumber
                = userService.getHRMSEmployee(request, IMConstants.ROLE_COMPLAINANT);

        return new NotificationContext(
                employeeMobileNumber,
                citizenMobileNumber.get("employeeMobile"),
                null);
    }
}
