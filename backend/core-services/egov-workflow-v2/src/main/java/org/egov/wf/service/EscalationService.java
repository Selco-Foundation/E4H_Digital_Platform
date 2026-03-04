package org.egov.wf.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.producer.Producer;
import org.egov.wf.repository.EscalationRepository;
import org.egov.wf.util.EscalationUtil;
import org.egov.wf.web.models.IMEscalationInstance;
import org.egov.wf.web.models.IMEscalationRequest;
import org.egov.wf.web.models.Escalation;
import org.egov.wf.web.models.EscalationSearchCriteria;
import org.egov.wf.web.models.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EscalationService {



    private EscalationUtil escalationUtil;

    private MDMSService mdmsService;

    private EscalationRepository escalationRepository;

    private WorkflowService workflowService;

    private Producer producer;

    private WorkflowConfig config;

    @Autowired
    public EscalationService(EscalationUtil escalationUtil, MDMSService mdmsService, EscalationRepository escalationRepository,
                             WorkflowService workflowService, Producer producer, WorkflowConfig config) {
        this.escalationUtil = escalationUtil;
        this.mdmsService = mdmsService;
        this.escalationRepository = escalationRepository;
        this.workflowService = workflowService;
        this.producer = producer;
        this.config = config;
    }


    /**
     * Fetches all escalations defined for the given businessService and escalates
     * the applications which have breached the SLA based on the escalation config defined
     * @param requestInfo
     * @param businessService
     */
    public void escalateApplications(RequestInfo requestInfo, String businessService){
        log.trace("Entering escalateApplications method");
        log.info("Starting escalation process for businessService: {}", businessService);

        Object mdmsData = mdmsService.mDMSCall(requestInfo);
        List<Escalation> escalations = escalationUtil.getEscalationsFromConfig(businessService, mdmsData);
        List<String> tenantIds = escalationUtil.getTenantIds(mdmsData);
        log.debug("Retrieved {} escalation(s) and {} tenant ID(s)", escalations.size(), tenantIds.size());

        for(Escalation escalation : escalations){
            log.debug("Processing escalation with status: {}", escalation.getStatus());
            processEscalation(requestInfo, escalation, tenantIds);
        }

        log.info("Completed escalation process for businessService: {}", businessService);
        log.trace("Exiting escalateApplications method");
    }


    /**
     * Processes the escalation
     * @param escalation
     * @param tenantIds
     */
    private void processEscalation(RequestInfo requestInfo, Escalation escalation, List<String> tenantIds){
        log.trace("Entering processEscalation method");

        for(String tenantId: tenantIds){
            log.debug("Processing escalation for tenantId: {}", tenantId);

            String stateUUID = escalationUtil.getStatusUUID(escalation.getStatus(), tenantId, escalation.getBusinessService());

            EscalationSearchCriteria criteria = EscalationSearchCriteria.builder().tenantId(tenantId)
                                                .status(stateUUID)
                                                .businessService(escalation.getBusinessService())
                                                .businessSlaExceededBy(escalation.getBusinessSlaExceededBy())
                                                .stateSlaExceededBy(escalation.getStateSlaExceededBy())
                                                .build();

            List<String> businessIds = escalationRepository.getBusinessIds(criteria);
            Integer numberOfBusinessIds = businessIds.size();
            Integer batchSize = config.getEscalationBatchSize();
            log.info("Found {} business ID(s) to escalate for tenantId: {}", numberOfBusinessIds, tenantId);

            for(int i = 0; i < numberOfBusinessIds; i = i +1){

                // Processing the businessIds in batches
//                Integer start = i;
//                Integer end = ((i + batchSize) < numberOfBusinessIds ? (i + batchSize) : numberOfBusinessIds) ;

                IMEscalationInstance processInstance=new IMEscalationInstance();
            	processInstance.setBusinessId(businessIds.get(i));
            	processInstance.setTenantId(tenantId);
            	processInstance.setAuthToken(requestInfo.getAuthToken());
            	processInstance.setUserInfo(requestInfo.getUserInfo());
            	List<IMEscalationInstance> processInstances=new ArrayList<IMEscalationInstance>();
            	processInstances.add(processInstance);
            	IMEscalationRequest processInstanceRequest=new IMEscalationRequest();
            	processInstanceRequest.setImEscalationInstance(processInstances);
            	String topic = escalation.getTopic();
            	log.debug("Pushing escalation request for businessId: {} to topic: {}", businessIds.get(i), topic);
            	producer.push(topic,processInstanceRequest);
            }

            log.info("Completed processing escalation for tenantId: {}, pushed {} business ID(s) to topic: {}", 
                    tenantId, numberOfBusinessIds, escalation.getTopic());
        }
        log.trace("Exiting processEscalation method");
    }

    /**
     * Temporary added for testing
     * @param requestInfo
     * @param businessService
     */
    public List<String> escalateApplicationsTest(RequestInfo requestInfo, String businessService){
        log.trace("Entering escalateApplicationsTest method");
        log.info("Testing escalation process for businessService: {}", businessService);

        Object mdmsData = mdmsService.mDMSCall(requestInfo);
        List<Escalation> escalations = escalationUtil.getEscalationsFromConfig(businessService, mdmsData);
        List<String> tenantIds = escalationUtil.getTenantIds(mdmsData);
        log.debug("Retrieved {} escalation(s) and {} tenant ID(s) for test", escalations.size(), tenantIds.size());

        List<String> ids = new LinkedList<>();

        for(Escalation escalation : escalations){
            ids.addAll(getEscalations(requestInfo, escalation, tenantIds));
        }

        log.info("Test escalation completed, returning {} business ID(s)", ids.size());
        log.trace("Exiting escalateApplicationsTest method");
        return ids;
    }

    /**
     * Temporary added for testing
     * @param escalation
     * @param tenantIds
     */
    private List<String> getEscalations(RequestInfo requestInfo, Escalation escalation, List<String> tenantIds){
        log.trace("Entering getEscalations method");

        List<String> ids = new LinkedList<>();

        for(String tenantId: tenantIds){
            log.debug("Getting escalations for tenantId: {}", tenantId);

            String stateUUID = escalationUtil.getStatusUUID(escalation.getStatus(), tenantId, escalation.getBusinessService());

            EscalationSearchCriteria criteria = EscalationSearchCriteria.builder().tenantId(tenantId)
                    .status(stateUUID)
                    .businessService(escalation.getBusinessService())
                    .businessSlaExceededBy(escalation.getBusinessSlaExceededBy())
                    .stateSlaExceededBy(escalation.getStateSlaExceededBy())
                    .build();

            List<String> businessIds = escalationRepository.getBusinessIds(criteria);
            Integer numberOfBusinessIds = businessIds.size();
            Integer batchSize = config.getEscalationBatchSize();
            log.debug("Found {} business ID(s) for escalation test, batch size: {}", numberOfBusinessIds, batchSize);

            for(int i = 0; i < numberOfBusinessIds; i = i + batchSize){

                // Processing the businessIds in batches
                Integer start = i;
                Integer end = ((i + batchSize) < numberOfBusinessIds ? (i + batchSize) : numberOfBusinessIds) ;

                List<ProcessInstance> processInstances = escalationUtil.getProcessInstances(tenantId, businessIds.subList(start,end), escalation);
                ids.addAll(processInstances.stream().map(ProcessInstance::getBusinessId).collect(Collectors.toList()));
                log.debug("Processed batch {} to {}, collected {} business ID(s)", start, end, processInstances.size());
            }

        }

        log.trace("Exiting getEscalations method");
        return ids;

    }




}
