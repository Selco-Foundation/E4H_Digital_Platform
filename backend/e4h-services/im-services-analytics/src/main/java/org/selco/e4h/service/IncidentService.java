package org.selco.e4h.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.egov.common.producer.Producer;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.kafka.consumer.KafkaProducerService;
import org.selco.e4h.repository.IncidentRepository;
import org.selco.e4h.web.models.IncidentRequest;
import org.selco.e4h.web.models.IncidentStatusAgregation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.selco.e4h.config.ServiceConstants.FUNCTIONAL;
import static org.selco.e4h.config.ServiceConstants.NON_FUNCTIONAL;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final KafkaProducerService producerService;

    private ConsumerConfiguration config;

    private final ObjectMapper objectMapper;

    public IncidentService(IncidentRepository incidentRepository, ConsumerConfiguration config, @Qualifier("objectMapper") ObjectMapper objectMapper, KafkaProducerService producerService){
        this.incidentRepository = incidentRepository;
        this.producerService = producerService;
        this.config = config;
        this.objectMapper = objectMapper;
    }
    @KafkaListener(topics = "process-audit-records")
    public void getStatusIncidentsAgregation(Map<String, Object> producerRecord) {
        if(producerRecord !=null && !producerRecord.isEmpty()){
            Object value = producerRecord.get("value");
            IncidentRequest request = objectMapper.convertValue(value, IncidentRequest.class);
            if (request!=null && request.getIncident()!=null){
                List<IncidentStatusAgregation> statusAgregations = incidentRepository.getStatusIncidentsAgregation(request.getIncident().getTenantId());
                List<IncidentStatusAgregation> systemFunctional = incidentRepository.getStatusSystemFunctional(request.getIncident().getTenantId());
                if(statusAgregations !=null && !statusAgregations.isEmpty()){
                    IncidentStatusAgregation incidentStatusAgregation = statusAgregations.get(0);
                    // true if at least one element is NON_FUNCTIONAL
                    boolean hasNonFunctional = systemFunctional.stream()
                            .anyMatch(item -> NON_FUNCTIONAL.equals(item.getSystemFunctional()));

                    incidentStatusAgregation.setSystemFunctional(hasNonFunctional ? NON_FUNCTIONAL : FUNCTIONAL);
                    producerService.sendIncident(config.getUpdateTopicIndexer(), incidentStatusAgregation);
                }
            }

        }
    }
}
