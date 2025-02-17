package org.egov.im.service.handler.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.service.UserService;
import org.egov.im.service.WorkflowService;
import org.egov.im.util.NotificationUtil;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentWrapper;
import org.egov.im.web.models.workflow.ProcessInstance;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.egov.im.util.IMConstants.*;

@Slf4j
@RequiredArgsConstructor
@Component("PENDINGFORASSIGNMENT_REOPEN")
public class PendingForAssignmentReOpenMHandler extends  MessageBuilderHandler {

    private final NotificationUtil notificationUtil;
    private final IMConfiguration config;
    private  final WorkflowService workflowService;
    private final UserService userService;

    @Override
    public Map<String, List<String>> handle(IncidentRequest request,
                                            IncidentWrapper incidentWrapper,
                                            String localizationMessage,
                                            String localisedStatus, String topic) {

        String applicationStatus = request.getIncident().getApplicationStatus();
        String incidentId = request.getIncident().getIncidentId();
        String tenantId = request.getIncident().getTenantId();

        final StringBuilder url = workflowService.getprocessInstanceSearchURL(tenantId, incidentId);
        url.append("&").append("history=true");

        messageForCitizen = notificationUtil
                .getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CITIZEN, localizationMessage);
        if (messageForCitizen == null) {
            log.info("No message Found For Citizen On Topic: {}", topic);
            return Collections.emptyMap();
        }

        messageForEmployee
                = notificationUtil.getCustomizedMsg(
                request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
        if (messageForEmployee == null) {
            log.info("No message Found For Employee On Topic: {}", topic);
            return Collections.emptyMap();
        }

        ProcessInstance processInstance
                = userService.findProcessInstanceByAction(request, IM_WF_RESOLVE, url);
        ProcessInstance processInstanceReject
                = userService.findProcessInstanceByAction(request, REJECT, url);

        if (messageForEmployee.contains("{ulb}")) {
            String localisationMessageForPlaceholder
                    = notificationUtil.getLocalizationMessages(request.getIncident().getTenantId(),
                    request.getRequestInfo(), COMMON_MODULE);
            String localisedULB
                    = notificationUtil.getCustomizedMsgForPlaceholder(localisationMessageForPlaceholder,
                    incidentWrapper.getIncident().getDistrict());
            messageForEmployee = messageForEmployee.replace("{ulb}", localisedULB);
        }

        if (messageForEmployee.contains("{emp_name}"))
            messageForEmployee
                    = messageForEmployee.replace("{emp_name}",
                    processInstance.getAssigner() != null
                            ? processInstance.getAssigner().getName()
                            : processInstanceReject.getAssigner().getName());


        return getMessage(incidentWrapper, config);
    }
}
