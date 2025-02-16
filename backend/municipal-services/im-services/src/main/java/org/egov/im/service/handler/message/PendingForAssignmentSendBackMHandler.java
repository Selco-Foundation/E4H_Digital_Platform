package org.egov.im.service.handler.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.util.NotificationUtil;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentWrapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.egov.im.util.IMConstants.EMPLOYEE;

@Slf4j
@RequiredArgsConstructor
@Component("PENDINGFORASSIGNMENT_SENDBACK")
public class PendingForAssignmentSendBackMHandler extends  MessageBuilderHandler {

    private final NotificationUtil notificationUtil;
    private final IMConfiguration config;

    @Override
    public Map<String, List<String>> handle(IncidentRequest request,
                                            IncidentWrapper incidentWrapper,
                                            String localizationMessage,
                                            String localisedStatus, String topic) {

        String applicationStatus = request.getIncident().getApplicationStatus();

        messageForEmployee = notificationUtil
                .getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
        if (messageForEmployee == null) {
            log.info("No message Found For Employee On Topic : " + topic);
            return null;
        }

        return getMessage(incidentWrapper, config);
    }
}
