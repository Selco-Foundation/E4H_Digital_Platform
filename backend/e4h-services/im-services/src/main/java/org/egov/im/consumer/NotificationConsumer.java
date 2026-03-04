
package org.egov.im.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.im.service.IMService;
import org.egov.im.service.NotificationService;
import org.egov.im.util.IMConstants;
import org.egov.im.web.models.IMEscalationInstance;
import org.egov.im.web.models.IMEscalationRequest;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentWrapper;
import org.egov.im.web.models.RequestSearchCriteria;
import org.egov.im.web.models.Workflow;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationConsumer {
    @Autowired
    NotificationService notificationService;

    @Autowired
    private ObjectMapper mapper;
    
    @Autowired
	private IMService imService;


/**
     * Consumes record and send notification
     *
     * @param record
     * @param topic
     */

    @KafkaListener(topics = {"${im.kafka.create.topic}","${im.kafka.update.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.trace("NotificationConsumer::listen method invoked");
        try {
        	IncidentRequest request = mapper.convertValue(record, IncidentRequest.class);

            String tenantId = request.getIncident().getTenantId();
            String incidentId = request.getIncident().getIncidentId();

            log.info("Processing notification for incidentId={}, tenantId={}, topic={}", incidentId, tenantId, topic);

            // Adding in MDC so that tracer can add it in header
            MDC.put(IMConstants.TENANTID_MDC_STRING, tenantId);

            notificationService.process(request, topic);
        } catch (Exception ex) {
            log.error("Error while processing notification from topic: {}", topic, ex);
        }
    }
    
    
    @KafkaListener(topics = { "${persister.auto.escalation.topic}"})
	public void generateEscalationDemand(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		log.trace("NotificationConsumer::generateEscalationDemand method invoked");
		ObjectMapper mapper = new ObjectMapper();
		IMEscalationRequest processInstanceRequest = new IMEscalationRequest();
		List<IncidentWrapper> incidents=new ArrayList<IncidentWrapper>();
		RequestInfo requestInfo=new RequestInfo();
		Workflow workflow = new Workflow();
		try {
			log.info("Consuming escalation record from topic: {}", topic);
			processInstanceRequest = mapper.convertValue(record, IMEscalationRequest.class);
			requestInfo.setAuthToken(processInstanceRequest.getImEscalationInstance().get(0).getAuthToken());
			requestInfo.setUserInfo(processInstanceRequest.getImEscalationInstance().get(0).getUserInfo());

			RequestSearchCriteria criteria = new RequestSearchCriteria();
			criteria.setTenantId(processInstanceRequest.getImEscalationInstance().get(0).getTenantId());
			String businessId = processInstanceRequest.getImEscalationInstance().get(0).getBusinessId();
			criteria.setIncidentId(businessId);
			log.debug("Searching for incident with businessId: {}", businessId);
			incidents = imService.search(requestInfo,criteria);
			log.debug("Search completed, found {} incidents", incidents.size());

		} catch (final Exception e) {
			log.error("Error while processing escalation record from topic: {}", topic, e);
		}
		log.debug("Received escalation record with businessId: {}", 
				processInstanceRequest.getImEscalationInstance().get(0).getBusinessId());

        if (!incidents.isEmpty()) {
        	log.info("Processing escalation update for incidentId: {}", incidents.get(0).getIncident().getIncidentId());
			workflow.setAssignes(new ArrayList<>());
			workflow.setAction("CLOSE");
			workflow.setVerificationDocuments(null);
        	IncidentRequest incidentRequest=new IncidentRequest();
        	incidentRequest.setIncident(incidents.get(0).getIncident());
        	incidentRequest.setRequestInfo(requestInfo);
        	incidentRequest.setWorkflow(workflow);
			log.trace("Calling update service for escalation");
            imService.update(incidentRequest);
            log.info("Escalation update completed successfully");
		} else {
				log.warn("No incidents found for escalation update");
		}
	}
}

