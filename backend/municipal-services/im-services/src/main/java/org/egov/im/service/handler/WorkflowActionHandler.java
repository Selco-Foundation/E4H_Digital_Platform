package org.egov.im.service.handler;

import org.egov.im.service.UserService;
import org.egov.im.web.models.IncidentRequest;

public interface WorkflowActionHandler {
    NotificationContext handle(IncidentRequest request, UserService userService);
}
