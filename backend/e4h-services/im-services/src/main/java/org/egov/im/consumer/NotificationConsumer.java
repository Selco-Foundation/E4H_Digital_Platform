
package org.egov.im.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.im.service.IMService;
import org.egov.im.service.NotificationService;
import org.egov.im.util.IMConstants;
import org.egov.im.web.models.*;
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
        try {
        	IncidentRequest request = mapper.convertValue(record, IncidentRequest.class);

            String tenantId = request.getIncident().getTenantId();

            // Adding in MDC so that tracer can add it in header
            MDC.put(IMConstants.TENANTID_MDC_STRING, tenantId);

            notificationService.process(request, topic);
        } catch (Exception ex) {
            StringBuilder builder = new StringBuilder("Error while listening to value: ").append(record)
                    .append("on topic: ").append(topic);
            log.error(builder.toString(), ex);
        }
    }
    
    
    @KafkaListener(topics = { "${persister.auto.escalation.topic}"})
	public void generateEscalationDemand(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		ObjectMapper mapper = new ObjectMapper();
		IMEscalationRequest processInstanceRequest = new IMEscalationRequest();
		List<IncidentWrapper> incidents=new ArrayList<IncidentWrapper>();
		RequestInfo requestInfo=new RequestInfo();
       		Workflow workflow = new Workflow();
			try {
				log.info("Consuming record: " + record);
				List<Map<String, Object>> processInstancesRaw = (List<Map<String, Object>>) record.get("ProcessInstances");
				List<IMEscalationInstance> escalationInstances = processInstancesRaw.stream()
						.map(instanceRaw -> mapper.convertValue(instanceRaw, IMEscalationInstance.class))
						.toList();

				processInstanceRequest.setImEscalationInstance(escalationInstances);

				Map<String, Object> requestInfoRaw = (Map<String, Object>) record.get("RequestInfo");
				requestInfo = mapper.convertValue(requestInfoRaw, RequestInfo.class);

				for (IMEscalationInstance instance : escalationInstances) {
					instance.setAuthToken((String) requestInfoRaw.get("authToken"));
					instance.setUserInfo(requestInfo.getUserInfo());
				}
			RequestSearchCriteria criteria = new RequestSearchCriteria();
			criteria.setTenantId(processInstanceRequest.getImEscalationInstance().get(0).getTenantId());
			criteria.setIncidentId(processInstanceRequest.getImEscalationInstance().get(0).getBusinessId());
			incidents = imService.search(requestInfo,criteria);
			log.info("exiting Search function2");

			log.debug("BPA Received: " + processInstanceRequest.getImEscalationInstance().get(0).getBusinessId());

		} catch (final Exception e) {
			log.error("Error while listening to valueeee : " + record + " on topic: " + topic + ": " + e);
		}
		log.debug("BPA Received: " + processInstanceRequest.getImEscalationInstance().get(0).getBusinessId());

        if (!incidents.isEmpty()) {
        	log.info("inside update");
			workflow.setAssignes(new ArrayList<>());
			workflow.setAction("CLOSE");
			workflow.setVerificationDocuments(null);
        	IncidentRequest incidentRequest=new IncidentRequest();
        	incidentRequest.setIncident(incidents.get(0).getIncident());
        	incidentRequest.setRequestInfo(requestInfo);
        	incidentRequest.setWorkflow(workflow);
		log.info("Proceeding for Update call");

        	//bpasearch (response)  -> Approve bpa request
		log.info("Proceeding for Update call2");
            imService.update(incidentRequest);
	}
    }
}

