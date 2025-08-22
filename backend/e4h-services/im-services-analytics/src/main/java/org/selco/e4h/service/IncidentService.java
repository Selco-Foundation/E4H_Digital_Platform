package org.selco.e4h.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.kafka.consumer.KafkaProducerService;
import org.selco.e4h.repository.IncidentRepository;
import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.web.models.IncidentRequest;
import org.selco.e4h.web.models.IncidentStatusAgregation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.selco.e4h.config.ServiceConstants.FUNCTIONAL;
import static org.selco.e4h.config.ServiceConstants.NON_FUNCTIONAL;

@Slf4j
@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final KafkaProducerService producerService;

    private ConsumerConfiguration config;

    private final ObjectMapper objectMapper;
    private final ElasticSearchClient esClient;

    public IncidentService(IncidentRepository incidentRepository, ConsumerConfiguration config, @Qualifier("objectMapper") ObjectMapper objectMapper,
                           KafkaProducerService producerService, ElasticSearchClient esClient){
        this.incidentRepository = incidentRepository;
        this.producerService = producerService;
        this.config = config;
        this.objectMapper = objectMapper;
        this.esClient = esClient;
    }
    @KafkaListener(topics = "process-audit-records")
    public void getStatusIncidentsAgregation(Map<String, Object> producerRecord) {
        log.info("Received topic from process-audit-records");
        if(producerRecord !=null && !producerRecord.isEmpty()){
            String topic = (String)producerRecord.get("topic");
            if(topic !=null && !topic.isEmpty() && (topic.trim().equals("save-im-request") || topic.trim().equals("update-im-request"))){
                log.info("Received topic from save-im-request");
                Object value = producerRecord.get("value");
                IncidentRequest request = objectMapper.convertValue(value, IncidentRequest.class);
                if (request!=null && request.getIncident()!=null){
                    String tenantId = request.getIncident().getTenantId();
                    List<IncidentStatusAgregation> statusAgregations = incidentRepository.getStatusIncidentsAgregation(tenantId);
                    List<IncidentStatusAgregation> systemFunctional = incidentRepository.getStatusSystemFunctional(tenantId);
                    if(statusAgregations !=null && !statusAgregations.isEmpty()){
                        IncidentStatusAgregation incidentStatusAgregation = statusAgregations.get(0);
                        // true if at least one element is NON_FUNCTIONAL
                        boolean hasNonFunctional = systemFunctional.stream()
                                .anyMatch(item -> NON_FUNCTIONAL.equals(item.getSystemFunctional()));

                        incidentStatusAgregation.setSystemFunctional(hasNonFunctional ? NON_FUNCTIONAL : FUNCTIONAL);
                        incidentStatusAgregation.setLastModifiedTime(System.currentTimeMillis());
                        Map<String, Object> tickets = esClient.getTicketsByTenantId(0, 1000, tenantId);
                        log.info("List tickets from tenant id {} {}", tenantId, tickets.size());
                        if(tickets!=null){
                            Map<String, Object> source = (Map<String, Object>)tickets.get("_source");
                            Map<String, Object> data = (Map<String, Object>)source.get("Data");
                            String block = (String)data.get("block");
                            String code = (String)data.get("code");
                            String state = (String)data.get("state");
                            String district = (String)data.get("district");
                            boolean isLive = (boolean)data.get("isLive");
                            String name = (String)data.get("name");
                            String phcType = (String)data.get("phcType");
                            String type = (String)data.get("type");
                            String tenantIdLocalized = (String)data.get("tenantId_localized");
                            List<Double> geoPoint = (List<Double>) data.get("geo-point");

                            incidentStatusAgregation.setBlock(block);
                            incidentStatusAgregation.setCode(code);
                            incidentStatusAgregation.setDistrict(district);
                            incidentStatusAgregation.setLive(isLive);
                            incidentStatusAgregation.setName(name);
                            incidentStatusAgregation.setPhcType(phcType);
                            incidentStatusAgregation.setType(type);
                            incidentStatusAgregation.setTenantId(tenantId);
                            incidentStatusAgregation.setTenantIdLocalized(tenantIdLocalized);
                            incidentStatusAgregation.setGeoPoint(geoPoint);
                            incidentStatusAgregation.setState(state);

                            log.info("Tickets sent to kafka {}", incidentStatusAgregation);
                            producerService.sendIncident(config.getUpdateTopicIndexer(), incidentStatusAgregation);
                        }
                    }
                }
            }

        }
    }
}
