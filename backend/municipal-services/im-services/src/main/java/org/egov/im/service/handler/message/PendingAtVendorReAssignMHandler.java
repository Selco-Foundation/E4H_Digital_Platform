package org.egov.im.service.handler.message;

import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.service.UserService;
import org.egov.im.util.NotificationUtil;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentWrapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.egov.im.util.IMConstants.*;
import static org.egov.im.util.IMConstants.COMMON_MODULE;

@Slf4j
@Component("PENDINGATVENDOR_REASSIGN")
@RequiredArgsConstructor
public class PendingAtVendorReAssignMHandler extends MessageBuilderHandler  {

    private final NotificationUtil notificationUtil;
    private final IMConfiguration config;
    private final UserService userService;

    @Override
    public Map<String, List<String>> handle(IncidentRequest request,
                                            IncidentWrapper incidentWrapper,
                                            String localizationMessage,
                                            String localisedStatus, String topic) {

        String applicationStatus = request.getIncident().getApplicationStatus();
        String defaultMessage;

        messageForCitizen = notificationUtil
                .getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CITIZEN, localizationMessage);
        if (messageForCitizen == null) {
            log.info("No message Found For Citizen On Topic : " + topic);
            return Collections.emptyMap();
        }

        messageForEmployee = notificationUtil
                .getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
        if (messageForEmployee == null) {
            log.info("No message Found For Employee On Topic : " + topic);
            return Collections.emptyMap();
        }

        defaultMessage = notificationUtil.getDefaultMsg(CITIZEN, localizationMessage);
        if (defaultMessage == null) {
            log.info("No default message Found For Topic : " + topic);
            return Collections.emptyMap();
        }

        if (defaultMessage.contains("{status}"))
            defaultMessage = defaultMessage.replace("{status}", localisedStatus);

        if (messageForCitizen.contains("{emp_name}"))
            messageForCitizen = messageForCitizen.replace("{emp_name}",
                    userService.fetchUserByUUID(request, request.getWorkflow().getAssignes().get(0))
                            .getName());

        if (messageForEmployee.contains("{ulb}")) {
            String localisationMessageForPlaceholder
                    = notificationUtil.getLocalizationMessages(
                    request.getIncident().getTenantId(), request.getRequestInfo(), COMMON_MODULE);
            String localisedULB
                    = notificationUtil.getCustomizedMsgForPlaceholder(localisationMessageForPlaceholder,
                    incidentWrapper.getIncident().getDistrict());
            messageForEmployee = messageForEmployee.replace("{ulb}", localisedULB);
        }

        if (messageForEmployee.contains("{emp_name}"))
            messageForEmployee = messageForEmployee.replace("{emp_name}",
                    userService.fetchUserByUUID(request, request.getRequestInfo().getUserInfo().getUuid())
                            .getName());

        if (messageForEmployee.contains("{ao_designation}")) {
            String localisationMessageForPlaceholder
                    = notificationUtil.getLocalizationMessages(request.getIncident().getTenantId(),
                    request.getRequestInfo(), COMMON_MODULE);
            String path = "$..messages[?(@.code==\"COMMON_MASTERS_DESIGNATION_AO\")].message";

            try {
                List<String> messageObj = JsonPath.parse(localisationMessageForPlaceholder).read(path);
                if (messageObj != null && messageObj.size() > 0) {
                    messageForEmployee = messageForEmployee.replace("{ao_designation}", messageObj.get(0));
                }
            } catch (Exception e) {
                log.warn("Fetching from localization failed", e);
            }
        }
        return getMessage(incidentWrapper, config);
    }
}
