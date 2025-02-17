package org.egov.im.service.handler.notification;

import lombok.RequiredArgsConstructor;
import org.egov.im.service.UserService;
import org.egov.im.service.WorkflowService;
import org.egov.im.util.IMConstants;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.egov.im.util.IMConstants.IM_WF_RESOLVE;
import static org.egov.im.util.IMConstants.REJECT;

@Component("PENDINGFORASSIGNMENT_REOPEN")
@RequiredArgsConstructor
public class PendingForAssignmentImfWfReopenHandler implements WorkflowActionHandler {

    private final WorkflowService workflowService;

    @Override
    public NotificationContext handle(IncidentRequest request, UserService userService) {
        String tenantId = request.getIncident().getTenantId();
        String incidentId = request.getIncident().getIncidentId();

        final StringBuilder url = workflowService.getprocessInstanceSearchURL(tenantId, incidentId);
        url.append("&").append("history=true");

        String employeeMobileNumber
                = userService.getAssignerMobileNumber(request, IM_WF_RESOLVE, url);

        if(employeeMobileNumber == null)
            employeeMobileNumber = userService.getAssignerMobileNumber(request, REJECT, url);

        Map<String, String> citizenMobileNumber
                = userService.getHRMSEmployee(request, IMConstants.ROLE_COMPLAINANT);

        return new NotificationContext(
                employeeMobileNumber,
                citizenMobileNumber.get("employeeMobile"),
                null);
    }
}
