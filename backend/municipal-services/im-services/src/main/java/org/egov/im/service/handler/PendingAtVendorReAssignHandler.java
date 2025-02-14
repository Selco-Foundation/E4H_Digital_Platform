package org.egov.im.service.handler;

import lombok.RequiredArgsConstructor;
import org.egov.im.service.UserService;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.stereotype.Component;

@Component("PENDINGATVENDOR_REASSIGN")
@RequiredArgsConstructor
public class PendingAtVendorReAssignHandler implements WorkflowActionHandler {

    @Override
    public NotificationContext handle(IncidentRequest request, UserService userService) {
        String employeeMobileNumber = userService.fetchUserByUUID(
                request, request.getWorkflow().getAssignes().get(0))
                .getMobileNumber();
        return new NotificationContext(
                employeeMobileNumber,
                null,
                null);
    }
}
