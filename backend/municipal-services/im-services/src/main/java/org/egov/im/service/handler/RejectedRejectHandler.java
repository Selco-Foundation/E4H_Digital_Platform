package org.egov.im.service.handler;

import org.egov.im.service.UserService;
import org.egov.im.util.IMConstants;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("REJECTED_REJECT")
public class RejectedRejectHandler implements WorkflowActionHandler {

    @Override
    public NotificationContext handle(IncidentRequest request, UserService userService) {
        Map<String, String> mobileNumber
                = userService.getHRMSEmployee(request, IMConstants.ROLE_COMPLAINANT);
        return new NotificationContext(
                mobileNumber.get("employeeMobile"),
                null,
                null);
    }
}
