package org.egov.im.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.util.HRMSUtil;
import org.egov.im.util.MDMSUtils;
import org.egov.im.util.NotificationUtil;
import org.egov.im.web.models.Notification.*;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentWrapper;
import org.egov.im.web.models.RequestInfoWrapper;
import org.egov.im.web.models.workflow.ProcessInstance;
import org.egov.im.web.models.workflow.ProcessInstanceResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.egov.im.util.IMConstants.*;

@Service
@Slf4j
public class NotificationService {

    private IMConfiguration config;
    private NotificationUtil notificationUtil;
    private WorkflowService workflowService;
    private ServiceRequestRepository serviceRequestRepository;
    private MDMSUtils mdmsUtils;
    private HRMSUtil hrmsUtils;
    private ObjectMapper mapper;
    private MultiStateInstanceUtil centralInstanceUtil;

    @Autowired
    public NotificationService(IMConfiguration config,
                               NotificationUtil notificationUtil,
                               ServiceRequestRepository serviceRequestRepository,
                               MDMSUtils mdmsUtils,
                               HRMSUtil hrmsUtils,
                               ObjectMapper mapper,
                               MultiStateInstanceUtil centralInstanceUtil,
                               @Lazy WorkflowService workflowService) {
        this.config = config;
        this.notificationUtil = notificationUtil;
        this.serviceRequestRepository = serviceRequestRepository;
        this.mdmsUtils = mdmsUtils;
        this.hrmsUtils = hrmsUtils;
        this.mapper = mapper;
        this.centralInstanceUtil = centralInstanceUtil;
        this.workflowService = workflowService;
    }

    public void process(IncidentRequest request, String topic) {
        log.trace("NotificationService::process method invoked");
        try {
            log.info("Processing notification request for incidentId={}, tenantId={}, topic={}", 
                    request.getIncident().getIncidentId(), request.getIncident().getTenantId(), topic);
            String tenantId = request.getIncident().getTenantId();
            IncidentWrapper incidentWrapper = IncidentWrapper.builder().incident(request.getIncident()).workflow(request.getWorkflow()).build();
            String applicationStatus = request.getIncident().getApplicationStatus();
            String action = request.getWorkflow().getAction();

            if (!(NOTIFICATION_ENABLE_FOR_STATUS.contains(action + "_" + applicationStatus))) {
                log.debug("Notification disabled for state: {}, action: {}", applicationStatus, action);
                return;
            }

            Map<String, List<String>> finalMessage = getFinalMessage(request, topic, applicationStatus);
            String reporterMobileNumber = request.getIncident().getReporter().getMobileNumber();
            String employeeMobileNumber = null;
            String citizenMobileNumber = null;
            String crmMobileNumber = null;
            Boolean crmUser = false;

            if (applicationStatus.equalsIgnoreCase(PENDINGFORASSIGNMENT) && action.equalsIgnoreCase(APPLY)) {
                Map<String, String> reassigneeDetails = getHRMSEmployee(request, "COMPLAINANT");
                employeeMobileNumber = reassigneeDetails.get("employeeMobile");
            } else if (applicationStatus.equalsIgnoreCase(PENDINGATVENDOR) && action.equalsIgnoreCase(ASSIGN)) {
                request.getWorkflow().setAssignes(null);
                Map<String, String> reassigneeDetails = getHRMSEmployee(request, "COMPLAINANT");
                employeeMobileNumber = reassigneeDetails.get("employeeMobile");
                ProcessInstance processInstance = getEmployeeName(incidentWrapper.getIncident().getTenantId(), incidentWrapper.getIncident().getIncidentId(), request.getRequestInfo(), ASSIGN);
                citizenMobileNumber = processInstance.getAssignes().get(0).getMobileNumber();
            } else if (applicationStatus.equalsIgnoreCase(PENDINGFORASSIGNMENT) && action.equalsIgnoreCase(SENDBACK)) {
                Map<String, String> reassigneeDetails = getHRMSEmployee(request, "COMPLAINANT");
                employeeMobileNumber = reassigneeDetails.get("employeeMobile");
            } else if (applicationStatus.equalsIgnoreCase(REJECTED) && action.equalsIgnoreCase(REJECT)) {
                Map<String, String> reassigneeDetails = getHRMSEmployee(request, "COMPLAINANT");
                employeeMobileNumber = reassigneeDetails.get("employeeMobile");
            } else if (applicationStatus.equalsIgnoreCase(RESOLVED) && action.equalsIgnoreCase(IM_WF_RESOLVE)) {
                Map<String, String> reassigneeDetails = getHRMSEmployee(request, "COMPLAINANT");
                employeeMobileNumber = reassigneeDetails.get("employeeMobile");

                ProcessInstance processInstance = getEmployeeName(incidentWrapper.getIncident().getTenantId(), incidentWrapper.getIncident().getIncidentId(), request.getRequestInfo(), IM_WF_RESOLVE);
                citizenMobileNumber = processInstance.getAssigner().getMobileNumber();

                processInstance = getEmployeeName(incidentWrapper.getIncident().getTenantId(), incidentWrapper.getIncident().getIncidentId(), request.getRequestInfo(), ASSIGN);
                crmMobileNumber = processInstance.getAssigner().getMobileNumber();
            } else if (applicationStatus.equalsIgnoreCase(PENDINGFORASSIGNMENT) && action.equalsIgnoreCase(IM_WF_REOPEN)) {
                ProcessInstance processInstance = getEmployeeName(incidentWrapper.getIncident().getTenantId(), incidentWrapper.getIncident().getIncidentId(), request.getRequestInfo(), IM_WF_RESOLVE);
                if (processInstance == null || processInstance.getAssigner() == null)
                    processInstance = getEmployeeName(incidentWrapper.getIncident().getTenantId(), incidentWrapper.getIncident().getIncidentId(), request.getRequestInfo(), REJECT);

                employeeMobileNumber = processInstance.getAssigner().getMobileNumber();
                Map<String, String> reassigneeDetails = getHRMSEmployee(request, "COMPLAINANT");
                citizenMobileNumber = reassigneeDetails.get("employeeMobile");

            } else if (applicationStatus.equalsIgnoreCase(CLOSED_AFTER_RESOLUTION) && action.equalsIgnoreCase(CLOSE)) {
                ProcessInstance processInstance = getEmployeeName(incidentWrapper.getIncident().getTenantId(), incidentWrapper.getIncident().getIncidentId(), request.getRequestInfo(), IM_WF_RESOLVE);
                employeeMobileNumber = processInstance.getAssigner().getMobileNumber();

                Map<String, String> reassigneeDetails = getHRMSEmployee(request, "COMPLAINANT");
                citizenMobileNumber = reassigneeDetails.get("employeeMobile");
            } else if (applicationStatus.equalsIgnoreCase(PENDINGATVENDOR) && action.equalsIgnoreCase(REASSIGN)) {
                employeeMobileNumber = fetchUserByUUID(request.getWorkflow().getAssignes().get(0), request.getRequestInfo(), request.getIncident().getTenantId()).getMobileNumber();
            } else {
                employeeMobileNumber = fetchUserByUUID(request.getIncident().getAuditDetails().getCreatedBy(), request.getRequestInfo(), request.getIncident().getTenantId()).getMobileNumber();
            }

            if (!StringUtils.isEmpty(finalMessage)) {
//                if (config.getIsUserEventsNotificationEnabled() != null && config.getIsUserEventsNotificationEnabled()) {
//                    for (Map.Entry<String, List<String>> entry : finalMessage.entrySet()) {
//                        for (String msg : entry.getValue()) {
//                            EventRequest eventRequest = enrichEventRequest(request, msg);
//                            if (eventRequest != null) {
//                                notificationUtil.sendEventNotification(tenantId, eventRequest);
//                            }
//                        }
//                    }
//                }

                if (config.getIsSMSEnabled() != null && config.getIsSMSEnabled()) {

                    for (Map.Entry<String, List<String>> entry : finalMessage.entrySet()) {

                        if (entry.getKey().equalsIgnoreCase(CITIZEN)) {
                            for (String msg : entry.getValue()) {
                                List<SMSRequest> smsRequests = new ArrayList<>();
                                smsRequests = enrichSmsRequest(citizenMobileNumber, msg);
                                if (!CollectionUtils.isEmpty(smsRequests)) {
                                    notificationUtil.sendSMS(tenantId, smsRequests);
                                }
                            }
                        } else if (entry.getKey().equalsIgnoreCase(EMPLOYEE)) {
                            for (String msg : entry.getValue()) {
                                List<SMSRequest> smsRequests = new ArrayList<>();
                                smsRequests = enrichSmsRequest(employeeMobileNumber, msg);
                                if (!CollectionUtils.isEmpty(smsRequests)) {
                                    notificationUtil.sendSMS(tenantId, smsRequests);
                                }
                            }
                        } else {

                            for (String msg : entry.getValue()) {
                                List<SMSRequest> smsRequests = new ArrayList<>();
                                smsRequests = enrichSmsRequest(crmMobileNumber, msg);
                                if (!CollectionUtils.isEmpty(smsRequests)) {
                                    notificationUtil.sendSMS(tenantId, smsRequests);
                                }
                            }

                        }
                    }

                }
            }

        } catch (Exception ex) {
            log.error("Error occurred while processing notification from topic: {}", topic, ex);
        }
    }

    /**
     * @param request           im Request
     * @param topic             Topic Name
     * @param applicationStatus Application Status
     * @return Returns list of SMSRequest
     */
    private Map<String, List<String>> getFinalMessage(IncidentRequest request, String topic, String applicationStatus) {
        log.trace("NotificationService::getFinalMessage method invoked");
        String tenantId = request.getIncident().getTenantId();
        String localizationMessage = notificationUtil.getLocalizationMessages(tenantId, request.getRequestInfo(), IM_MODULE);

        IncidentWrapper incidentWrapper = IncidentWrapper.builder().incident(request.getIncident()).workflow(request.getWorkflow()).build();
        Map<String, List<String>> message = new HashMap<>();

        String messageForCitizen = null;
        String messageForEmployee = null;
        String messageForCRM = null;
        String defaultMessage = null;
        Boolean crmUser = false;

        String localisedStatus = notificationUtil.getCustomizedMsgForPlaceholder(localizationMessage, "CS_COMMON_" + incidentWrapper.getIncident().getApplicationStatus());
        /**
         * Confirmation SMS to citizens, when they will raise any complaint
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(PENDINGFORASSIGNMENT) && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(APPLY)) {
            List<Role> roles = request.getRequestInfo().getUserInfo().getRoles();
            for (Role role : roles) {
                if (role.getTenantId().equalsIgnoreCase("pg")) {
                    crmUser = true;
                    break;
                }
            }
            if (crmUser)
                messageForEmployee = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CRM, localizationMessage);
            else
                messageForEmployee = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);

            if (messageForEmployee == null) {
                log.warn("No message found for employee on topic: {}", topic);
                return null;
            }

        }
        /**
         * SMS to citizens and employee both, when a complaint is assigned to an employee
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(PENDINGATVENDOR) && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(ASSIGN)) {
            messageForCitizen = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CITIZEN, localizationMessage);
            if (messageForCitizen == null) {
                log.warn("No message found for citizen on topic: {}", topic);
                return null;
            }

            messageForEmployee = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
            if (messageForEmployee == null) {
                log.warn("No message found for employee on topic: {}", topic);
                return null;
            }

//            defaultMessage = notificationUtil.getDefaultMsg(CITIZEN, localizationMessage);
//            if (defaultMessage == null) {
//                log.info("No default message Found For Topic : " + topic);
//                return null;
//            }
//
//            if(defaultMessage.contains("{status}"))
//                defaultMessage = defaultMessage.replace("{status}", localisedStatus);


            Map<String, String> reassigneeDetails = getHRMSEmployee(request, "COMPLAINT_RESOLVER");

            if (messageForEmployee.contains("{ulb}")) {
                String localisationMessageForPlaceholder = notificationUtil.getLocalizationMessages(request.getIncident().getTenantId(), request.getRequestInfo(), COMMON_MODULE);
                // String localisedULB = notificationUtil.getCustomizedMsgForPlaceholder(localisationMessageForPlaceholder,incidentWrapper.getIncident().getAddress().getDistrict());
                // messageForEmployee = messageForEmployee.replace("{ulb}",localisedULB);
            }

            if (messageForEmployee.contains("{emp_name}"))
                messageForEmployee = messageForEmployee.replace("{emp_name}", reassigneeDetails.get("employeeName"));

            if (messageForCitizen.contains("{emp_name}"))
                messageForCitizen = messageForCitizen.replace("{emp_name}", reassigneeDetails.get("employeeName"));
            //messageForEmployee = messageForEmployee.replace("{emp_name}",fetchUserByUUID(request.getWorkflow().getAssignes().get(0), request.getRequestInfo(), request.getIncident().getTenantId()).getName());

            if (messageForEmployee.contains("{ao_designation}")) {
                String localisationMessageForPlaceholder = notificationUtil.getLocalizationMessages(request.getIncident().getTenantId(), request.getRequestInfo(), COMMON_MODULE);
                String path = "$..messages[?(@.code==\"COMMON_MASTERS_DESIGNATION_AO\")].message";

                try {
                    ArrayList<String> messageObj = JsonPath.parse(localisationMessageForPlaceholder).read(path);
                    if (messageObj != null && messageObj.size() > 0) {
                        messageForEmployee = messageForEmployee.replace("{ao_designation}", messageObj.get(0));
                    }
                } catch (Exception e) {
                    log.warn("Fetching from localization failed", e);
                }
            }
        }

        /**
         * SMS to citizens and employee, when the complaint is re-assigned to an employee
         */
//        if(incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(PENDING_FOR_REASSIGNMENT) && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(REASSIGN)){
//            messageForCitizen = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CITIZEN, localizationMessage);
//            if (messageForCitizen == null) {
//                log.warn("No message found for citizen on topic: {}", topic);
//                return null;
//            }
//
//            messageForEmployee = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
//            if (messageForEmployee == null) {
//                log.warn("No message found for employee on topic: {}", topic);
//                return null;
//            }
//
//            defaultMessage = notificationUtil.getDefaultMsg(CITIZEN, localizationMessage);
//            if (defaultMessage == null) {
//                log.info("No default message Found For Topic : " + topic);
//                return null;
//            }
//
//            if(defaultMessage.contains("{status}"))
//                defaultMessage = defaultMessage.replace("{status}", localisedStatus);
//
//
//            Map<String, String> reassigneeDetails  = getHRMSEmployee(request);
//            if (messageForCitizen.contains("{emp_department}"))
//                messageForCitizen = messageForCitizen.replace("{emp_department}",reassigneeDetails.get(DEPARTMENT));
//
//            if (messageForCitizen.contains("{emp_designation}"))
//                messageForCitizen = messageForCitizen.replace("{emp_designation}",reassigneeDetails.get(DESIGNATION));
//
//
//            if (messageForCitizen.contains("{emp_name}"))
//                messageForCitizen = messageForCitizen.replace("{emp_name}", fetchUserByUUID(request.getWorkflow().getAssignes().get(0), request.getRequestInfo(), request.getIncident().getTenantId()).getName());
//
//            if(messageForEmployee.contains("{ulb}")) {
//                String localisationMessageForPlaceholder =  notificationUtil.getLocalizationMessages(request.getIncident().getTenantId(), request.getRequestInfo(),COMMON_MODULE);
//                String localisedULB = notificationUtil.getCustomizedMsgForPlaceholder(localisationMessageForPlaceholder,incidentWrapper.getIncident().getDistrict());
//                messageForEmployee = messageForEmployee.replace("{ulb}",localisedULB);
//            }
//
//            if (messageForEmployee.contains("{emp_name}"))
//                messageForEmployee = messageForEmployee.replace("{emp_name}", fetchUserByUUID(request.getRequestInfo().getUserInfo().getUuid(), request.getRequestInfo(), request.getIncident().getTenantId()).getName());
//
//            if(messageForEmployee.contains("{ao_designation}")){
//                String localisationMessageForPlaceholder =  notificationUtil.getLocalizationMessages(request.getIncident().getTenantId(), request.getRequestInfo(),COMMON_MODULE);
//                String path = "$..messages[?(@.code==\"COMMON_MASTERS_DESIGNATION_AO\")].message";
//
//                try {
//                    ArrayList<String> messageObj = JsonPath.parse(localisationMessageForPlaceholder).read(path);
//                    if(messageObj != null && messageObj.size() > 0) {
//                        messageForEmployee = messageForEmployee.replace("{ao_designation}", messageObj.get(0));
//                    }
//                } catch (Exception e) {
//                    log.warn("Fetching from localization failed", e);
//                }
//            }
//        }

        /**
         * SMS to citizens, when complaint got rejected with reason
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(REJECTED) && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(REJECT)) {
            messageForEmployee = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
            if (messageForEmployee == null) {
                log.warn("No message found for employee on topic: {}", topic);
                return null;
            }
//
//            defaultMessage = notificationUtil.getDefaultMsg(CITIZEN, localizationMessage);
//            if (defaultMessage == null) {
//                log.info("No default message Found For Topic : " + topic);
//                return null;
//            }
//
//            if(defaultMessage.contains("{status}"))
//                defaultMessage = defaultMessage.replace("{status}", localisedStatus);

            if (messageForEmployee.contains("{additional_comments}"))
                messageForEmployee = messageForEmployee.replace("{additional_comments}", incidentWrapper.getWorkflow().getComments());
        }

        /**
         * SMS to citizens and employee, when the complaint has been re-opened on citizen request
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(PENDINGFORASSIGNMENT) && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(IM_WF_REOPEN)) {
            messageForCitizen = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CITIZEN, localizationMessage);
            if (messageForCitizen == null) {
                log.warn("No message found for citizen on topic: {}", topic);
                return null;
            }

            messageForEmployee = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
            if (messageForEmployee == null) {
                log.warn("No message found for employee on topic: {}", topic);
                return null;
            }

//            defaultMessage = notificationUtil.getDefaultMsg(CITIZEN, localizationMessage);
//            if (defaultMessage == null) {
//                log.info("No default message Found For Topic : " + topic);
//                return null;
//            }

            ProcessInstance processInstance = getEmployeeName(incidentWrapper.getIncident().getTenantId(), incidentWrapper.getIncident().getIncidentId(), request.getRequestInfo(), IM_WF_RESOLVE);
            ProcessInstance processInstanceReject = getEmployeeName(incidentWrapper.getIncident().getTenantId(), incidentWrapper.getIncident().getIncidentId(), request.getRequestInfo(), REJECT);

//            if(defaultMessage.contains("{status}"))
//                defaultMessage = defaultMessage.replace("{status}", localisedStatus);

            if (messageForEmployee.contains("{ulb}")) {
                String localisationMessageForPlaceholder = notificationUtil.getLocalizationMessages(request.getIncident().getTenantId(), request.getRequestInfo(), COMMON_MODULE);
                String localisedULB = notificationUtil.getCustomizedMsgForPlaceholder(localisationMessageForPlaceholder, incidentWrapper.getIncident().getDistrict());
                messageForEmployee = messageForEmployee.replace("{ulb}", localisedULB);
            }

            if (messageForEmployee.contains("{emp_name}"))
                messageForEmployee = messageForEmployee.replace("{emp_name}", processInstance.getAssigner() != null ? processInstance.getAssigner().getName() : processInstanceReject.getAssigner().getName());
        }

        /**
         * SMS to citizens, when complaint got resolved
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(RESOLVED) && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(IM_WF_RESOLVE)) {
            messageForEmployee = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
            if (messageForEmployee == null) {
                log.warn("No message found for employee on topic: {}", topic);
                return null;
            }
            messageForCitizen = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CITIZEN, localizationMessage);
            if (messageForCitizen == null) {
                log.warn("No message found for citizen on topic: {}", topic);
                return null;
            }

            messageForCRM = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CRM, localizationMessage);
            if (messageForCRM == null) {
                log.warn("No message found for CRM on topic: {}", topic);
                return null;
            }

//            defaultMessage = notificationUtil.getDefaultMsg(CITIZEN, localizationMessage);
//            if (defaultMessage == null) {
//                log.info("No default message Found For Topic : " + topic);
//                return null;
//            }

            ProcessInstance processInstance = getEmployeeName(incidentWrapper.getIncident().getTenantId(), incidentWrapper.getIncident().getIncidentId(), request.getRequestInfo(), IM_WF_RESOLVE);

//            if(defaultMessage.contains("{status}"))
//                defaultMessage = defaultMessage.replace("{status}", localisedStatus);
//            
            if (messageForEmployee.contains("{emp_name}"))
                messageForEmployee = messageForEmployee.replace("{emp_name}", request.getRequestInfo().getUserInfo() != null ? request.getRequestInfo().getUserInfo().getName() : processInstance.getAssigner().getName());
            if (messageForCitizen.contains("{emp_name}"))
                messageForCitizen = messageForCitizen.replace("{emp_name}", request.getRequestInfo().getUserInfo() != null ? request.getRequestInfo().getUserInfo().getName() : processInstance.getAssigner().getName());
        }


        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(PENDINGFORASSIGNMENT) && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(IM_WF_SENDBACK)) {
            messageForEmployee
                    = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);

            log.info("Processing sendback action - resolver: {}, incidentId: {}, reason: {}",
                    request.getRequestInfo().getUserInfo().getUserName(),
                    request.getIncident().getIncidentId(),
                    request.getWorkflow().getSendBackReason().getReason()
                    );
            if (messageForEmployee == null) {
                log.warn("No message found for employee on topic: {}", topic);
                return null;
            }

        }


        /**
         * SMS to citizens and employee, when the complaint has been re-opened on citizen request
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(CLOSED_AFTER_RESOLUTION)) {
            messageForEmployee = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
            if (messageForEmployee == null) {
                log.warn("No message found for employee on topic: {}", topic);
                return null;
            }

            messageForCitizen = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CITIZEN, localizationMessage);
            if (messageForCitizen == null) {
                log.warn("No message found for citizen on topic: {}", topic);
                return null;
            }

            ProcessInstance processInstance = getEmployeeName(incidentWrapper.getIncident().getTenantId(), incidentWrapper.getIncident().getIncidentId(), request.getRequestInfo(), IM_WF_RESOLVE);

//            if(defaultMessage.contains("{status}"))
//                defaultMessage = defaultMessage.replace("{status}", localisedStatus);
//

//            if(messageForEmployee.contains("{rating}"))
//                messageForEmployee=messageForEmployee.replace("{rating}",incidentWrapper.getIncident().getRating().toString());

            if (messageForEmployee.contains("{emp_name}"))
                messageForEmployee = messageForEmployee.replace("{emp_name}", processInstance.getAssignes().get(0).getName());

            if (messageForEmployee.contains("ticket_id"))
                messageForEmployee = messageForEmployee.replace("{ticket_id}", incidentWrapper.getIncident().getIncidentId());

            if (messageForEmployee.contains("{survey_link}")) {
                String hyerplink = String.format("%s/%s/%s/%s/%s",
                        config.getDigitUIHost(),
                        config.getDigitUITenant().get(tenantId),
                        config.getDigitUIFeedback(),
                        tenantId,
                        tenantId);

                log.debug("Derived survey hyperlink: {} for tenant {}", hyerplink, tenantId);
                messageForEmployee = messageForEmployee.replace("{survey_link}", hyerplink);
            }
        }

        /**
         * SMS to citizens and employee, when the complaint is re-assigned to LME
         */
        if (incidentWrapper.getIncident().getApplicationStatus().equalsIgnoreCase(PENDINGATVENDOR) && incidentWrapper.getWorkflow().getAction().equalsIgnoreCase(REASSIGN)) {
            messageForCitizen = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, CITIZEN, localizationMessage);
            if (messageForCitizen == null) {
                log.warn("No message found for citizen on topic: {}", topic);
                return null;
            }

            messageForEmployee = notificationUtil.getCustomizedMsg(request.getWorkflow().getAction(), applicationStatus, EMPLOYEE, localizationMessage);
            if (messageForEmployee == null) {
                log.warn("No message found for employee on topic: {}", topic);
                return null;
            }

            defaultMessage = notificationUtil.getDefaultMsg(CITIZEN, localizationMessage);
            if (defaultMessage == null) {
                log.warn("No default message found for topic: {}", topic);
                return null;
            }

            if (defaultMessage.contains("{status}"))
                defaultMessage = defaultMessage.replace("{status}", localisedStatus);


            // Map<String, String> reassigneeDetails  = getHRMSEmployee(request,"COMPLAINT_RESOLVER");
//            if (messageForCitizen.contains("{emp_department}"))
//                messageForCitizen = messageForCitizen.replace("{emp_department}",reassigneeDetails.get(DEPARTMENT));
//
//            if (messageForCitizen.contains("{emp_designation}"))
//                messageForCitizen = messageForCitizen.replace("{emp_designation}",reassigneeDetails.get(DESIGNATION));

            if (messageForCitizen.contains("{emp_name}"))
                messageForCitizen = messageForCitizen.replace("{emp_name}", fetchUserByUUID(request.getWorkflow().getAssignes().get(0), request.getRequestInfo(), request.getIncident().getTenantId()).getName());

            if (messageForEmployee.contains("{ulb}")) {
                String localisationMessageForPlaceholder = notificationUtil.getLocalizationMessages(request.getIncident().getTenantId(), request.getRequestInfo(), COMMON_MODULE);
                String localisedULB = notificationUtil.getCustomizedMsgForPlaceholder(localisationMessageForPlaceholder, incidentWrapper.getIncident().getDistrict());
                messageForEmployee = messageForEmployee.replace("{ulb}", localisedULB);
            }

            if (messageForEmployee.contains("{emp_name}"))
                messageForEmployee = messageForEmployee.replace("{emp_name}", fetchUserByUUID(request.getRequestInfo().getUserInfo().getUuid(), request.getRequestInfo(), request.getIncident().getTenantId()).getName());

            if (messageForEmployee.contains("{ao_designation}")) {
                String localisationMessageForPlaceholder = notificationUtil.getLocalizationMessages(request.getIncident().getTenantId(), request.getRequestInfo(), COMMON_MODULE);
                String path = "$..messages[?(@.code==\"COMMON_MASTERS_DESIGNATION_AO\")].message";

                try {
                    ArrayList<String> messageObj = JsonPath.parse(localisationMessageForPlaceholder).read(path);
                    if (messageObj != null && messageObj.size() > 0) {
                        messageForEmployee = messageForEmployee.replace("{ao_designation}", messageObj.get(0));
                    }
                } catch (Exception e) {
                    log.warn("Fetching from localization failed", e);
                }
            }
        }


        //String localisedComplaint = notificationUtil.getCustomizedMsgForPlaceholder(localizationMessage,"im.complaint.category."+request.getIncident().getIncidentType());

        Long createdTime = incidentWrapper.getIncident().getAuditDetails().getCreatedTime();
        LocalDate date = Instant.ofEpochMilli(createdTime > 10 ? createdTime : createdTime * 1000)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

        //String appLink = notificationUtil.getShortnerURL(config.getMobileDownloadLink());

        if (messageForCitizen != null) {
            messageForCitizen = messageForCitizen.replace("{ticket_type}", incidentWrapper.getIncident().getIncidentType());
            messageForCitizen = messageForCitizen.replace("{incidentId}", incidentWrapper.getIncident().getIncidentId());
            messageForCitizen = messageForCitizen.replace("{date}", date.format(formatter));
            messageForCitizen = messageForCitizen.replace("{download_link}", config.getMobileDownloadLink());
            if (messageForCitizen.contains("{url}")) {
                String url = notificationUtil.getUrlByTenantId(localizationMessage);
                messageForCitizen = messageForCitizen.replace("{url}",url );
            }
        }

        if (messageForEmployee != null) {
            messageForEmployee = messageForEmployee.replace("{ticket_type}", incidentWrapper.getIncident().getIncidentType());
            messageForEmployee = messageForEmployee.replace("{incidentId}", incidentWrapper.getIncident().getIncidentId());
            messageForEmployee = messageForEmployee.replace("{date}", date.format(formatter));
            if(request.getWorkflow() != null && request.getWorkflow().getSendBackReason() != null) {
                messageForEmployee = messageForEmployee.replace("{dropDownValue}", request.getWorkflow().getSendBackReason().getReason());
            }
            messageForEmployee = messageForEmployee.replace("{download_link}", config.getMobileDownloadLink());
            if (messageForEmployee.contains("{url}")) {
                String url = notificationUtil.getUrlByTenantId(localizationMessage);
                messageForEmployee = messageForEmployee.replace("{url}",url );
            }
        }


        if (messageForCRM != null) {
            messageForCRM = messageForCRM.replace("{ticket_type}", incidentWrapper.getIncident().getIncidentType());
            messageForCRM = messageForCRM.replace("{incidentId}", incidentWrapper.getIncident().getIncidentId());
            messageForCRM = messageForCRM.replace("{date}", date.format(formatter));
            messageForCRM = messageForCRM.replace("{download_link}", config.getMobileDownloadLink());
            if (messageForCRM.contains("{url}")) {
                String url = notificationUtil.getUrlByTenantId(localizationMessage);
                messageForCRM = messageForCRM.replace("{url}",url );
            }
        }
        if (messageForCitizen != null)
            message.put(CITIZEN, Arrays.asList(new String[]{messageForCitizen}));
        message.put(EMPLOYEE, Arrays.asList(messageForEmployee));
        if (messageForCRM != null)
            message.put(CRM, Arrays.asList(messageForCRM));

        log.debug("Notification messages prepared - employee: {}, citizen: {}, crm: {}", 
                messageForEmployee != null ? "present" : "null",
                messageForCitizen != null ? "present" : "null",
                messageForCRM != null ? "present" : "null");
        return message;
    }

    /**
     * Fetches User Object based on the UUID.
     *
     * @param uuidstring  - UUID of User
     * @param requestInfo - Request Info Object
     * @param tenantId    - Tenant Id
     * @return - Returns User object with given UUID
     */
    public User fetchUserByUUID(String uuidstring, RequestInfo requestInfo, String tenantId) {
        log.trace("NotificationService::fetchUserByUUID method invoked");
        log.debug("Fetching user by UUID: {}", uuidstring);
        User userInfoCopy = requestInfo.getUserInfo();

        User userInfo = getInternalMicroserviceUser(tenantId);

        requestInfo.setUserInfo(userInfo);

        StringBuilder uri = new StringBuilder();
        uri.append(config.getUserHost()).append(config.getUserSearchEndpoint());
        Map<String, Object> userSearchRequest = new HashMap<>();
        userSearchRequest.put("RequestInfo", requestInfo);
        userSearchRequest.put("tenantId", tenantId);
        userSearchRequest.put("userType", "EMPLOYEE");
        Set<String> uuid = new HashSet<>();
        uuid.add(uuidstring);
        userSearchRequest.put("uuid", uuid);
        User user = null;
        try {
            LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) serviceRequestRepository.fetchResult(uri, userSearchRequest);
            List<LinkedHashMap<String, Object>> users = (List<LinkedHashMap<String, Object>>) responseMap.get("user");
            String dobFormat = "yyyy-MM-dd";
            parseResponse(responseMap, dobFormat);
            user = mapper.convertValue(users.get(0), User.class);

        } catch (Exception e) {
            log.error("Exception while parsing user object", e);
        }

        requestInfo.setUserInfo(userInfoCopy);
        return user;
    }

    /**
     * Parses date formats to long for all users in responseMap
     *
     * @param responeMap LinkedHashMap got from user api response
     */
    private void parseResponse(LinkedHashMap responeMap, String dobFormat) {
        log.trace("NotificationService::parseResponse method invoked");
        List<LinkedHashMap> users = (List<LinkedHashMap>) responeMap.get("user");
        String formatForDate = "dd-MM-yyyy HH:mm:ss";
        if (users != null) {
            users.forEach(map -> {
                        map.put("createdDate", dateTolong((String) map.get("createdDate"), formatForDate));
                        if ((String) map.get("lastModifiedDate") != null)
                            map.put("lastModifiedDate", dateTolong((String) map.get("lastModifiedDate"), formatForDate));
                        if ((String) map.get("dob") != null)
                            map.put("dob", dateTolong((String) map.get("dob"), dobFormat));
                        if ((String) map.get("pwdExpiryDate") != null)
                            map.put("pwdExpiryDate", dateTolong((String) map.get("pwdExpiryDate"), formatForDate));
                    }
            );
        }
    }

    /**
     * Converts date to long
     *
     * @param date   date to be parsed
     * @param format Format of the date
     * @return Long value of date
     */
    private Long dateTolong(String date, String format) {
        log.trace("NotificationService::dateTolong method invoked");
        SimpleDateFormat simpleDateFormatObject = new SimpleDateFormat(format);
        Date returnDate = null;
        try {
            returnDate = simpleDateFormatObject.parse(date);
        } catch (ParseException e) {
            log.error("Failed to parse date: {} with format: {}", date, format, e);
            throw new CustomException("DATE_PARSE_ERROR", "Failed to parse date: " + date);
        }
        return returnDate.getTime();
    }

    public ProcessInstance getEmployeeName(String tenantId, String IncidentId, RequestInfo requestInfo, String action) {
        log.trace("NotificationService::getEmployeeName method invoked");
        log.debug("Fetching employee name for tenantId: {}, incidentId: {}, action: {}", tenantId, IncidentId, action);
        ProcessInstance processInstanceToReturn = new ProcessInstance();
        User userInfoCopy = requestInfo.getUserInfo();

        User userInfo = getInternalMicroserviceUser(tenantId);

        requestInfo.setUserInfo(userInfo);

        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        StringBuilder URL = workflowService.getprocessInstanceSearchURL(tenantId, IncidentId);
        URL.append("&").append("history=true");

        Object result = serviceRequestRepository.fetchResult(URL, requestInfoWrapper);
        ProcessInstanceResponse processInstanceResponse = null;
        try {
            processInstanceResponse = mapper.convertValue(result, ProcessInstanceResponse.class);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse process instance response", e);
            throw new CustomException("PARSING ERROR", "Failed to parse response of workflow processInstance search");
        }
        if (CollectionUtils.isEmpty(processInstanceResponse.getProcessInstances())) {
            log.warn("No process instances found for tenantId: {}, incidentId: {}, action: {}", tenantId, IncidentId, action);
            throw new CustomException("WORKFLOW_NOT_FOUND", "The workflow object is not found");
        }

        for (ProcessInstance processInstance : processInstanceResponse.getProcessInstances()) {
            if (processInstance.getAction().equalsIgnoreCase(action))
                processInstanceToReturn = processInstance;
        }
        requestInfo.setUserInfo(userInfoCopy);
        return processInstanceToReturn;
    }

    public String getDepartment(IncidentRequest request) {
        log.trace("NotificationService::getDepartment method invoked");
        log.debug("Fetching department for incident type: {}", request.getIncident().getIncidentType());
        Object mdmsData = mdmsUtils.mDMSCall(request);
        String serviceCode = request.getIncident().getIncidentType();
        String jsonPath = MDMS_SERVICEDEF_SEARCH.replace("{SERVICEDEF}", serviceCode);

        List<Object> res = null;

        try {
            res = JsonPath.read(mdmsData, jsonPath);
        } catch (Exception e) {
            throw new CustomException("JSONPATH_ERROR", "Failed to parse mdms response");
        }

        if (CollectionUtils.isEmpty(res))
            throw new CustomException("INVALID_SERVICECODE", "The service code: " + serviceCode + " is not present in MDMS");

        return res.get(0).toString();

    }

    public Map<String, String> getHRMSEmployee(IncidentRequest request, String role) {
        log.trace("NotificationService::getHRMSEmployee method invoked");
        log.debug("Fetching HRMS employee for role: {}, tenantId: {}", role, request.getIncident().getTenantId());
        Map<String, String> reassigneeDetails = new HashMap<>();

        List<String> employeeName = null;
        List<String> employeeMobile = null;
        List<String> employeeUUID = null;

        StringBuilder url = null;
        String tenantId = request.getIncident().getTenantId();
        if ("COMPLAINT_FACILITATOR_1".equals(role) && tenantId != null && tenantId.contains(".")) {
            tenantId = tenantId.split("\\.")[0];
        }
        if (request.getWorkflow().getAssignes() != null)
            url = hrmsUtils.getHRMSURI(request.getWorkflow().getAssignes(), tenantId, role, request.getIncident().getBoundaryCode());
        else
            url = hrmsUtils.getHRMSURI(null, tenantId, role, request.getIncident().getBoundaryCode());
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(request.getRequestInfo()).build();
        Object response = serviceRequestRepository.fetchResult(url, requestInfoWrapper);

        //MDMS CALL
//        Object mdmsData = mdmsUtils.mDMSCall(request);
//        String jsonPath = MDMS_DEPARTMENT_SEARCH.replace("{SERVICEDEF}",request.getIncident().getIncidentType());
//
//        try{
//            mdmsDepartmentList = JsonPath.read(mdmsData,jsonPath);
//            hrmsDepartmentList = JsonPath.read(response, HRMS_DEPARTMENT_JSONPATH);
//        }
//        catch (Exception e){
//            throw new CustomException("JSONPATH_ERROR","Failed to parse mdms response for department");
//        }
//
//        if(CollectionUtils.isEmpty(mdmsDepartmentList))
//            throw new CustomException("PARSING_ERROR","Failed to fetch department from mdms data for serviceCode: "+request.getIncident().getIncidentType());
//        else departmentFromMDMS = mdmsDepartmentList.get(0);
//
//        if(hrmsDepartmentList.contains(departmentFromMDMS)){
//            String localisedDept = notificationUtil.getCustomizedMsgForPlaceholder(localisationMessageForPlaceholder,"COMMON_MASTERS_DEPARTMENT_"+departmentFromMDMS);
//            reassigneeDetails.put("department",localisedDept);
//        }
//
//        String designationJsonPath = HRMS_DESIGNATION_JSONPATH.replace("{department}",departmentFromMDMS);
//
//        try{
//            designation = JsonPath.read(response, designationJsonPath);
        try {
            employeeName = JsonPath.read(response, HRMS_EMP_NAME_JSONPATH);
            employeeMobile = JsonPath.read(response, HRMS_EMP_MOBILE_JSONPATH);
            employeeUUID = JsonPath.read(response, HRMS_EMP_UUID_JSONPATH);
        } catch (Exception e) {
            log.error("Failed to parse HRMS response for employee details", e);
            throw new CustomException("JSONPATH_ERROR", "Failed to parse HRMS response for employee");
        }

        if (CollectionUtils.isEmpty(employeeName) || CollectionUtils.isEmpty(employeeMobile) || CollectionUtils.isEmpty(employeeUUID)) {
            log.warn("Empty employee details returned from HRMS for role: {}", role);
            throw new CustomException("EMPLOYEE_NOT_FOUND", "Employee details not found for role: " + role);
        }

        reassigneeDetails.put("employeeName", employeeName.get(0));
        reassigneeDetails.put("employeeMobile", employeeMobile.get(0));
        reassigneeDetails.put("employeeUUID", employeeUUID.get(0));
        log.debug("Successfully fetched HRMS employee details for role: {}", role);

        return reassigneeDetails;
    }

    public Map<String, String> getHRMSEmployeeForIndexing(IncidentRequest request, List<String> uuids, String role) {
        log.trace("NotificationService::getHRMSEmployeeForIndexing method invoked");
        log.debug("Fetching HRMS employee for indexing - role: {}, tenantId: {}", role, request.getIncident().getTenantId());
        Map<String, String> employeeDetails = new HashMap<>();

        String tenantId = request.getIncident().getTenantId();

        StringBuilder url = hrmsUtils.getHRMSURI(uuids, tenantId, role, request.getIncident().getBoundaryCode());
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder()
                .requestInfo(request.getRequestInfo())
                .build();

        Object response = serviceRequestRepository.fetchResult(url, requestInfoWrapper);

        List<String> employeeName = JsonPath.read(response, HRMS_EMP_NAME_JSONPATH);
        List<String> employeeUserName = JsonPath.read(response, HRMS_EMP_USERNAME_JSONPATH);

        if (employeeName != null && !employeeName.isEmpty()) {
            employeeDetails.put("employeeName", employeeName.get(0));
        }

        if (employeeUserName != null && !employeeUserName.isEmpty()) {
            employeeDetails.put("employeeUserName", employeeUserName.get(0));
        }

        return employeeDetails;
    }


    private List<SMSRequest> enrichSmsRequest(String mobileNumber, String finalMessage) {
        log.trace("NotificationService::enrichSmsRequest method invoked");
        List<SMSRequest> smsRequest = new ArrayList<>();
        SMSRequest req = SMSRequest.builder().mobileNumber(mobileNumber).message(finalMessage).build();
        smsRequest.add(req);
        return smsRequest;
    }

    private EventRequest enrichEventRequest(IncidentRequest request, String finalMessage) {
        log.trace("NotificationService::enrichEventRequest method invoked");
        String tenantId = request.getIncident().getTenantId();
        String mobileNumber = request.getIncident().getReporter().getMobileNumber();

        Map<String, String> mapOfPhoneNoAndUUIDs = fetchUserUUIDs(mobileNumber, request.getRequestInfo(), tenantId);

        if (CollectionUtils.isEmpty(mapOfPhoneNoAndUUIDs.keySet())) {
            log.warn("UUID search failed for mobileNumber: {}", mobileNumber);
        }

        List<Event> events = new ArrayList<>();
        List<String> toUsers = new ArrayList<>();
        toUsers.add(mapOfPhoneNoAndUUIDs.get(mobileNumber));

        Action action = null;
        if (request.getWorkflow().getAction().equals("RESOLVE")) {

            List<ActionItem> items = new ArrayList<>();
            String rateLink = "";
            String reopenLink = "";
            String rateUrl = config.getRateLink();
            String reopenUrl = config.getReopenLink();
            rateLink = rateUrl.replace("{application-id}", request.getIncident().getIncidentId());
            reopenLink = reopenUrl.replace("{application-id}", request.getIncident().getIncidentId());
            rateLink = getUiAppHost(tenantId) + rateLink;
            reopenLink = getUiAppHost(tenantId) + reopenLink;
            ActionItem rateItem = ActionItem.builder().actionUrl(rateLink).code(config.getRateCode()).build();
            ActionItem reopenItem = ActionItem.builder().actionUrl(reopenLink).code(config.getReopenCode()).build();
            items.add(rateItem);
            items.add(reopenItem);

            action = Action.builder().actionUrls(items).build();
        }
        Recepient recepient = Recepient.builder().toUsers(toUsers).toRoles(null).build();
        events.add(Event.builder().tenantId(tenantId).description(finalMessage).eventType(USREVENTS_EVENT_TYPE)
                .name(USREVENTS_EVENT_NAME).postedBy(USREVENTS_EVENT_POSTEDBY)
                .source(Source.WEBAPP).recepient(recepient).actions(action).eventDetails(null).build());

        if (!CollectionUtils.isEmpty(events)) {
            return EventRequest.builder().requestInfo(request.getRequestInfo()).events(events).build();
        } else {
            return null;
        }
    }

    /**
     * Fetches UUIDs of CITIZEN based on the phone number.
     *
     * @param mobileNumber - Mobile Numbers
     * @param requestInfo  - Request Information
     * @param tenantId     - Tenant Id
     * @return Returns List of MobileNumbers and UUIDs
     */
    public Map<String, String> fetchUserUUIDs(String mobileNumber, RequestInfo requestInfo, String tenantId) {
        log.trace("NotificationService::fetchUserUUIDs method invoked");
        log.debug("Fetching user UUIDs for mobileNumber: {}, tenantId: {}", mobileNumber, tenantId);
        Map<String, String> mapOfPhoneNoAndUUIDs = new HashMap<>();
        StringBuilder uri = new StringBuilder();
        uri.append(config.getUserHost()).append(config.getUserSearchEndpoint());
        Map<String, Object> userSearchRequest = new HashMap<>();
        userSearchRequest.put("RequestInfo", requestInfo);
        userSearchRequest.put("tenantId", tenantId);
        userSearchRequest.put("userType", "CITIZEN");
        userSearchRequest.put("userName", mobileNumber);
        try {
            Object user = serviceRequestRepository.fetchResult(uri, userSearchRequest);
            if (null != user) {
                String uuid = JsonPath.read(user, "$.user[0].uuid");
                mapOfPhoneNoAndUUIDs.put(mobileNumber, uuid);
            } else {
                log.error("Service returned null while fetching user for username: {}", mobileNumber);
            }
        } catch (Exception e) {
            log.error("Exception while fetching user for username: {}", mobileNumber, e);
        }

        return mapOfPhoneNoAndUUIDs;
    }

    private User getInternalMicroserviceUser(String tenantId) {
        log.trace("NotificationService::getInternalMicroserviceUser method invoked");
        //Creating role with INTERNAL_MICROSERVICE_ROLE
        Role role = Role.builder()
                .name("Internal Microservice Role").code("INTERNAL_MICROSERVICE_ROLE")
                .tenantId(tenantId).build();

        //Creating userinfo with uuid and role of internal micro service role
        User userInfo = User.builder()
                .uuid(config.getEgovInternalMicroserviceUserUuid())
                .type("SYSTEM")
                .roles(Collections.singletonList(role)).id(0L).build();

        return userInfo;
    }

    public String getUiAppHost(String tenantId) {
        log.trace("NotificationService::getUiAppHost method invoked");
        String stateLevelTenantId = centralInstanceUtil.getStateLevelTenant(tenantId);
        String uiAppHost = config.getUiAppHostMap().get(stateLevelTenantId);
        log.debug("Retrieved UI app host for tenantId: {}, stateLevelTenantId: {}", tenantId, stateLevelTenantId);
        return uiAppHost;
    }

}