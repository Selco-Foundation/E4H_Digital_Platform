package org.egov.im.service.handler.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.util.NotificationUtil;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentWrapper;
import org.egov.im.web.models.Role;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.egov.im.util.IMConstants.*;

@Slf4j
@RequiredArgsConstructor
@Component("PENDINGFORASSIGNMENT_APPLY")
public class PendingForAssignmentAssignMHandler extends MessageBuilderHandler {

    private final NotificationUtil notificationUtil;
    private final IMConfiguration config;

    @Override
    public Map<String, List<String>> handle(IncidentRequest request,
                                            IncidentWrapper incidentWrapper,
                                            String localizationMessage,
                                            String localisedStatus,
                                            String topic) {

        String applicationStatus = request.getIncident().getApplicationStatus();
        boolean crmUser;

        List<Role> roles = request.getRequestInfo().getUserInfo().getRoles();
        crmUser = roles.stream()
                .anyMatch(role -> "pg".equalsIgnoreCase(role.getTenantId()));

        if (Boolean.TRUE.equals(crmUser)) {
            messageForEmployee
                    = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(),
                    applicationStatus, CRM, localizationMessage);
        } else {
            messageForEmployee
                    = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(),
                    applicationStatus, EMPLOYEE, localizationMessage);
        }

        //if no message return empty map
        if (messageForEmployee == null) {
            log.warn("No message Found For Employee On Topic : {}",  topic);
            message.put(EMPLOYEE, Collections.emptyList());
            return message;
        }

        return getMessage(incidentWrapper, config);
    }
}
