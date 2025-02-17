package org.egov.im.service;

import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.util.NotificationUtil;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentWrapper;
import org.egov.im.web.models.Role;
import org.egov.im.web.models.workflow.ProcessInstance;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.egov.im.util.IMConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageBuilderService {

    private final NotificationUtil notificationUtil;
    private final UserService userService;
    private final IMConfiguration config;
    private final WorkflowService workflowService;

    /**
     * @param request           im Request
     * @param topic             Topic Name
     * @param applicationStatus Application Status
     * @return Returns list of SMSRequest
     */
    public Map<String, List<String>> getFinalMessage(IncidentRequest request, String topic, String applicationStatus) {
        String tenantId = request.getIncident().getTenantId();
        String localizationMessage = notificationUtil.getLocalizationMessages(tenantId, request.getRequestInfo(), IM_MODULE);

        IncidentWrapper incidentWrapper = IncidentWrapper.builder()
                .incident(request.getIncident())
                .workflow(request.getWorkflow())
                .build();
        Map<String, List<String>> message = new HashMap<>();

        String messageForCitizen = null;
        String messageForEmployee = null;
        String messageForCRM = null;
        String defaultMessage;
        boolean crmUser = false;

        String localisedStatus
                = notificationUtil.getCustomizedMsgForPlaceholder(localizationMessage,
                String.format("CS_COMMON_%s", incidentWrapper.getIncident().getApplicationStatus()));
        /**
         * Confirmation SMS to citizens, when they will raise any complaint
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(PENDINGFORASSIGNMENT)
                && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(APPLY)) {
            List<Role> roles = request.getRequestInfo().getUserInfo().getRoles();
            for (Role role : roles) {
                if (role.getTenantId().equalsIgnoreCase("pg")) {
                    crmUser = true;
                    break;
                }
            }
            if (Boolean.TRUE.equals(crmUser)) {
                messageForEmployee
                        = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(),
                        applicationStatus, CRM, localizationMessage);
            } else {
                messageForEmployee
                        = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(),
                        applicationStatus, EMPLOYEE, localizationMessage);
            }

            if (messageForEmployee == null) {
                log.info("No message Found For Employee On Topic : {}", topic);
                message.put(EMPLOYEE, Collections.emptyList());
                return message;
            }

        }
        /**
         * SMS to citizens and employee both, when a complaint is assigned to an employee
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(PENDINGATVENDOR)
                && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(ASSIGN)) {
            messageForCitizen = notificationUtil.getCustomizedMsg(
                    request.getWorkflow().getAction(), applicationStatus, CITIZEN, localizationMessage);
            if (messageForCitizen == null) {
                log.info("No message Found For Citizen On Topic : {}", topic);
                return Collections.emptyMap();
            }

            messageForEmployee = notificationUtil
                    .getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
            if (messageForEmployee == null) {
                log.info("No message Found For Employee On Topic : {}", topic);
                return Collections.emptyMap();
            }

            Map<String, String> reassigneeDetails = userService.getHRMSEmployee(request, "COMPLAINT_RESOLVER");

            if (messageForEmployee.contains("{emp_name}"))
                messageForEmployee = messageForEmployee.replace("{emp_name}", reassigneeDetails.get("employeeName"));

            if (messageForCitizen.contains("{emp_name}"))
                messageForCitizen = messageForCitizen.replace("{emp_name}", reassigneeDetails.get("employeeName"));
            if (messageForEmployee.contains("{ao_designation}")) {
                String localisationMessageForPlaceholder
                        = notificationUtil.getLocalizationMessages(request.getIncident().getTenantId(), request.getRequestInfo(), COMMON_MODULE);
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
        }

        /**
         * SMS to citizens, when complaint got rejected with reason
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(REJECTED)
                && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(REJECT)) {
            messageForEmployee = notificationUtil
                    .getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
            if (messageForEmployee == null) {
                log.info("No message Found For Employee On Topic : " + topic);
                return Collections.emptyMap();
            }
            if (messageForEmployee.contains("{additional_comments}"))
                messageForEmployee = messageForEmployee
                        .replace("{additional_comments}", incidentWrapper.getWorkflow().getComments());
        }

        /**
         * SMS to citizens and employee, when the complaint has been re-opened on citizen request
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(PENDINGFORASSIGNMENT)
                && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(IM_WF_REOPEN)) {
            String incidentId = request.getIncident().getIncidentId();

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
        }

        /**
         * SMS to citizens, when complaint got resolved
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(RESOLVED)
                && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(IM_WF_RESOLVE)) {

            String incidentId = request.getIncident().getIncidentId();

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
        }


        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(PENDINGFORASSIGNMENT)
                && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(IM_WF_SENDBACK)) {
            messageForEmployee = notificationUtil
                    .getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
            if (messageForEmployee == null) {
                log.info("No message Found For Employee On Topic : " + topic);
                return null;
            }
        }


        /**
         * SMS to citizens and employee, when the complaint has been re-opened on citizen request
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(CLOSED_AFTER_RESOLUTION)) {
            String incidentId = request.getIncident().getIncidentId();

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

            ProcessInstance processInstance
                    = userService.findProcessInstanceByAction(request, IM_WF_RESOLVE, url);
            if (messageForEmployee.contains("{emp_name}"))
                messageForEmployee = messageForEmployee.replace("{emp_name}", processInstance.getAssignes().get(0).getName());
        }

        /**
         * SMS to citizens and employee, when the complaint is re-assigned to LME
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(PENDINGATVENDOR)
                && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(REASSIGN)) {
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
        }

        Long createdTime = incidentWrapper.getIncident().getAuditDetails().getCreatedTime();
        LocalDate date = Instant.ofEpochMilli(createdTime > 10 ? createdTime : createdTime * 1000)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

        if (messageForCitizen != null) {
            messageForCitizen = messageForCitizen.replace("{ticket_type}", incidentWrapper.getIncident().getIncidentType());
            messageForCitizen = messageForCitizen.replace("{incidentId}", incidentWrapper.getIncident().getIncidentId());
            messageForCitizen = messageForCitizen.replace("{date}", date.format(formatter));
            messageForCitizen = messageForCitizen.replace("{download_link}", config.getMobileDownloadLink());
        }

        if (messageForEmployee != null) {
            messageForEmployee = messageForEmployee.replace("{ticket_type}", incidentWrapper.getIncident().getIncidentType());
            messageForEmployee = messageForEmployee.replace("{incidentId}", incidentWrapper.getIncident().getIncidentId());
            messageForEmployee = messageForEmployee.replace("{date}", date.format(formatter));
            messageForEmployee = messageForEmployee.replace("{download_link}", config.getMobileDownloadLink());
        }

        if (messageForCRM != null) {
            messageForCRM = messageForCRM.replace("{ticket_type}", incidentWrapper.getIncident().getIncidentType());
            messageForCRM = messageForCRM.replace("{incidentId}", incidentWrapper.getIncident().getIncidentId());
            messageForCRM = messageForCRM.replace("{date}", date.format(formatter));
            messageForCRM = messageForCRM.replace("{download_link}", config.getMobileDownloadLink());
        }
        if (messageForCitizen != null) {
            message.put(CITIZEN, List.of(messageForCitizen));
        }
        message.put(EMPLOYEE, Collections.singletonList(messageForEmployee));
        if (messageForCRM != null)
            message.put(CRM, List.of(messageForCRM));

        log.info("message being sent is  " + messageForEmployee + " , " + messageForCitizen + " , " + messageForCRM);
        return message;
    }
}
