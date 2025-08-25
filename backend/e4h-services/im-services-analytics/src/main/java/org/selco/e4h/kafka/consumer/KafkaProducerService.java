package org.selco.e4h.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.web.models.IncidentStatusAgregation;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ElasticSearchClient esClient;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate, ElasticSearchClient esClient) {
        this.kafkaTemplate = kafkaTemplate;
        this.esClient = esClient;
    }

    public void sendIncident(String topic, Object incident) {
        kafkaTemplate.send(topic, incident);
        System.out.println("Sent incident: " + incident);
    }

    public void getTicket(String tenantId){
        Map<String, Object> tickets = esClient.getHFByTenantId(tenantId);
        log.info("List tickets {}", tickets.size());
        if(tickets!=null){
            Map<String, Object> source = (Map<String, Object>)tickets.get("_source");
            Map<String, Object> data = (Map<String, Object>)source.get("Data");
            String block = (String)data.get("block");
            String code = (String)data.get("code");
            String district = (String)data.get("district");
            boolean isLive = (boolean)data.get("isLive");
            String name = (String)data.get("name");
            String phcType = (String)data.get("phcType");
            String type = (String)data.get("type");
//            String tenantId = (String)data.get("tenantId");

            IncidentStatusAgregation incidentStatusAgregation = new IncidentStatusAgregation();
            incidentStatusAgregation.setBlock(block);
            incidentStatusAgregation.setCode(code);
            incidentStatusAgregation.setDistrict(district);
            incidentStatusAgregation.setLive(isLive);
            incidentStatusAgregation.setName(name);
            incidentStatusAgregation.setPhcType(phcType);
            incidentStatusAgregation.setType(type);
            incidentStatusAgregation.setTenantId(tenantId);
        }
        log.info("List tickets {}", tickets.size());
    }
}

