package org.egov.im.service.handler.notification;

import lombok.RequiredArgsConstructor;
import org.egov.im.service.UserService;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("DEFAULTHANDLER")
public class DefaultHandler implements WorkflowActionHandler {

    @Override
    public NotificationContext handle(IncidentRequest request, UserService userService) {
        String employeeMobileNumber = userService.fetchUserByUUID(
                        request, request.getIncident().getAuditDetails().getCreatedBy())
                .getMobileNumber();
        return new NotificationContext(
                employeeMobileNumber,
                null,
                null);
    }
}
