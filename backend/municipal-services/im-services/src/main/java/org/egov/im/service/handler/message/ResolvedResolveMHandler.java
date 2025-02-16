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
@Component("RESOLVED_RESOLVE")
@RequiredArgsConstructor
public class ResolvedResolveMHandler extends MessageBuilderHandler {

    private final NotificationUtil notificationUtil;
    private final IMConfiguration config;
    private final WorkflowService workflowService;
    private final UserService userService;


    @Override
    public Map<String, List<String>> handle(IncidentRequest request,
                                            IncidentWrapper incidentWrapper,
                                            String localizationMessage,
                                            String localisedStatus, String topic) {

        String incidentId = request.getIncident().getIncidentId();
        String tenantId = request.getIncident().getTenantId();
        String applicationStatus = request.getIncident().getApplicationStatus();

        final StringBuilder url = workflowService.getprocessInstanceSearchURL(tenantId, incidentId);
        url.append("&").append("history=true");

        messageForEmployee = notificationUtil.getCustomizedMsg(
                request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
        if (messageForEmployee == null) {
            log.info("No message Found For Employee On Topic : " + topic);
            return Collections.emptyMap();
        }
        messageForCitizen = notificationUtil
                .getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CITIZEN, localizationMessage);
        if (messageForCitizen == null) {
            log.info("No message Found For Citizen On Topic : " + topic);
            return Collections.emptyMap();
        }

        messageForCRM = notificationUtil
                .getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CRM, localizationMessage);
        if (messageForCRM == null) {
            log.info("No message Found For CRM On Topic : " + topic);
            return Collections.emptyMap();
        }

        ProcessInstance processInstance
                = userService.findProcessInstanceByAction(request, IM_WF_RESOLVE, url);

        if (messageForEmployee.contains("{emp_name}"))
            messageForEmployee = messageForEmployee.replace("{emp_name}",
                    request.getRequestInfo().getUserInfo() != null ?
                            request.getRequestInfo().getUserInfo().getName() : processInstance.getAssigner().getName());
        if (messageForCitizen.contains("{emp_name}"))
            messageForCitizen = messageForCitizen.replace("{emp_name}",
                    request.getRequestInfo().getUserInfo() != null
                            ? request.getRequestInfo().getUserInfo().getName()
                            : processInstance.getAssigner().getName());

        return getMessage(incidentWrapper, config);
    }
}
