package org.egov.im.service.handler.notification;

import lombok.RequiredArgsConstructor;
import org.egov.im.service.UserService;
import org.egov.im.service.WorkflowService;
import org.egov.im.util.IMConstants;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.egov.im.util.IMConstants.ASSIGN;

@Component("PENDINGATVENDOR_ASSIGN")
@RequiredArgsConstructor
public class PendingAtVendorAssignHandler implements WorkflowActionHandler {

    private final UserService userService;
    private final WorkflowService workflowService;

    @Override
    public NotificationContext handle(IncidentRequest request, UserService userService) {
        String tenantId = request.getIncident().getTenantId();
        String incidentId = request.getIncident().getIncidentId();

        final StringBuilder url = workflowService.getprocessInstanceSearchURL(tenantId, incidentId);
        url.append("&").append("history=true");

        Map<String, String> employeeMobileNumber
                = userService.getHRMSEmployee(request, IMConstants.ROLE_COMPLAINANT);

        String citizenMobileNumber
                = userService.getAssigneeMobileNumber(request, ASSIGN, url);

        return new NotificationContext(
                employeeMobileNumber.get("employeeMobile"),
                citizenMobileNumber,
                null);
    }
}
