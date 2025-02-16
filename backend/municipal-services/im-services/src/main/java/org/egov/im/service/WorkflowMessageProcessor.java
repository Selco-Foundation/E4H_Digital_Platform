package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.service.factory.WorkflowMessageHandlerFactory;
import org.egov.im.util.NotificationUtil;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentWrapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowMessageProcessor {

    private final WorkflowMessageHandlerFactory factory;
    private final NotificationUtil notificationUtil;

    /**
     * constructs the Incident condition using status and action respectively,
     * example: (status = PENDINGFORASSIGNMENT) and (action = APPLY) = PENDINGFORASSIGNMENT_APPLY
     */
    public Map<String, List<String>> process(IncidentRequest request, String localizationMessage, String topic) {
        final String KEY = String.format("%s_%s", request.getIncident().getApplicationStatus(),
                request.getWorkflow().getAction());

        //create Incident wrapper object
        IncidentWrapper incidentWrapper = IncidentWrapper.builder()
                .incident(request.getIncident())
                .workflow(request.getWorkflow())
                .build();

        String localisedStatus
                = notificationUtil.getCustomizedMsgForPlaceholder(localizationMessage,
                String.format("CS_COMMON_%s", incidentWrapper.getIncident().getApplicationStatus()));

        log.info("searching for handler: {}", KEY);
        return factory.getHandler(KEY)
                .handle(request, incidentWrapper, localizationMessage, localisedStatus, topic);
    }
}
