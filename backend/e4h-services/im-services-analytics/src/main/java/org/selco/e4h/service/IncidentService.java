package org.selco.e4h.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.kafka.consumer.KafkaProducerService;
import org.selco.e4h.repository.IncidentRepository;
import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.web.models.IncidentRequest;
import org.selco.e4h.web.models.IncidentRequestWrapper;
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
    private final EscalationMasterDataService masterDataService;
    private final KafkaProducerService producerService;

    private ConsumerConfiguration config;

    private final ObjectMapper objectMapper;
    private final ElasticSearchClient esClient;

    public IncidentService(IncidentRepository incidentRepository, EscalationMasterDataService masterDataService, ConsumerConfiguration config, @Qualifier("objectMapper") ObjectMapper objectMapper,
                           KafkaProducerService producerService, ElasticSearchClient esClient){
        this.incidentRepository = incidentRepository;
        this.masterDataService = masterDataService;
        this.producerService = producerService;
        this.config = config;
        this.objectMapper = objectMapper;
        this.esClient = esClient;
    }

    @KafkaListener(topics = { "save-im-request-indexer", "update-im-request-indexer", "process-audit-records" }, groupId = "im-consumer-group")
    public void handleKafkaMessage(Object message) {
        log.info("Received message from Kafka: {}", message);

        try {
            if (message instanceof ConsumerRecord<?, ?> record) {
                Object recordValue = record.value();
                IncidentRequest request = null;
                String mappedVendorName = null;
                String mappedVendorUserName = null;

                if (recordValue instanceof Map<?, ?> map) {
                    // Try it first as IncidentRequestWrapper
                    IncidentRequestWrapper wrapper = objectMapper.convertValue(map, IncidentRequestWrapper.class);

                    if (wrapper.getIncidentRequest() != null && wrapper.getIndexView() != null) {
                        log.info("Message is IncidentRequestWrapper");

                        request = wrapper.getIncidentRequest();
                        mappedVendorName = wrapper.getIndexView().getMappedVendorName();
                        mappedVendorUserName = wrapper.getIndexView().getMappedVendorUserName();
                    } else {
                        // Otherwise, it's a process-audit-records
                        log.info("Message is Map<String,Object>");
                        String topic = (String) map.get("topic");
                        if (topic == null || topic.isBlank()) return;

                        if (!topic.equals("save-im-request") &&
                                !topic.equals("update-im-request") &&
                                !topic.equals("save-im-request-indexer") &&
                                !topic.equals("update-im-request-indexer")) {
                            return;
                        }

                        Object value = map.get("value");
                        request = objectMapper.convertValue(value, IncidentRequest.class);
                    }
                }

                if (request == null || request.getIncident() == null) return;

                processIncident(request, mappedVendorName, mappedVendorUserName);
            }
            else{
                log.info("Received message is not a consumer object: {}", message);
            }

        } catch (Exception e) {
            log.error("Error while processing Kafka message", e);
        }
    }

    private void processIncident(IncidentRequest request, String mappedVendorName, String mappedVendorUserName) {
        String tenantId = request.getIncident().getTenantId();
        String boundaryCode = request.getIncident().getBoundaryCode();
        List<IncidentStatusAgregation> statusAgregations = incidentRepository.getStatusIncidentsAgregation(boundaryCode);
        List<IncidentStatusAgregation> systemFunctional = incidentRepository.getStatusSystemFunctional(boundaryCode);

        if (statusAgregations != null && !statusAgregations.isEmpty()) {
            IncidentStatusAgregation incidentStatusAgregation = statusAgregations.get(0);

            // systemFunctional = NON_FUNCTIONAL if at least one NON_FUNCTIONAL, otherwise FUNCTIONAL
            boolean hasNonFunctional = false;
            if (systemFunctional != null) {
                hasNonFunctional = systemFunctional.stream()
                        .anyMatch(item -> NON_FUNCTIONAL.equals(item.getSystemFunctional()));
            }
            incidentStatusAgregation.setSystemFunctional(hasNonFunctional ? NON_FUNCTIONAL : FUNCTIONAL);
            incidentStatusAgregation.setLastModifiedTime(System.currentTimeMillis());

            Map<String, Object> tickets = esClient.getHFByBoundaryCode(boundaryCode);
            if (tickets != null && !tickets.isEmpty()) {
                Map<String, Object> source = (Map<String, Object>) tickets.get("_source");
                if (source != null) {
                    Map<String, Object> data = (Map<String, Object>) source.get("Data");
                    if (data != null) {
                        incidentStatusAgregation.setBlock((String) data.get("block"));
                        incidentStatusAgregation.setCode(String.valueOf(data.get("code")));
                        incidentStatusAgregation.setState((String) data.get("state"));
                        incidentStatusAgregation.setDistrict((String) data.get("district"));
                        incidentStatusAgregation.setLive((boolean) data.get("isLive"));
                        incidentStatusAgregation.setName((String) data.get("name"));
                        incidentStatusAgregation.setPhcType((String) data.get("phcType"));
                        incidentStatusAgregation.setType((String) data.get("type"));
                        incidentStatusAgregation.setTenantId(tenantId);
                        incidentStatusAgregation.setBoundaryCode(boundaryCode);
                        incidentStatusAgregation.setTenantIdLocalized((String) data.get("tenantId_localized"));
                        incidentStatusAgregation.setGeoPoint((List<Double>) data.get("geo-point"));
                        incidentStatusAgregation.setMappedVendorName((String) data.get("mappedVendorName"));
                        incidentStatusAgregation.setMappedVendorUserName((String) data.get("mappedVendorUserName"));

                        // fields coming only from the wrapper
                        if (mappedVendorName != null) {
                            incidentStatusAgregation.setMappedVendorName(mappedVendorName);
                        }
                        if (mappedVendorUserName != null) {
                            incidentStatusAgregation.setMappedVendorUserName(mappedVendorUserName);
                        }

                        log.info("Tickets sent to kafka {}", incidentStatusAgregation);
                        producerService.sendIncident(config.getUpdateTopicIndexer(), incidentStatusAgregation);
                    }
                }
            }
        }
    }


    public void scriptUpdatePHCAgregation() {
        log.info("Script function called");
        try{
            List<Map<String, Object>> listPHCs = esClient.getAllPHC(0, 6000);
            log.info("List tickets size {}", listPHCs.size());
            if(listPHCs!=null && !listPHCs.isEmpty()){
                for (Map<String, Object> phc : listPHCs){
                    Map<String, Object> data = (Map<String, Object>)phc.get("Data");
                    String block = (String)data.get("block");
                    String code = String.valueOf(data.get("code"));
                    String state = (String)data.get("state");
                    String district = (String)data.get("district");
                    boolean isLive = (boolean)data.get("isLive");
                    String name = (String)data.get("name");
                    String existBoundaryCode = (String)data.get("boundaryCode");
                    String phcType = (String)data.get("phcType");
                    String type = (String)data.get("type");
                    String tenantId = (String)data.get("tenantId");
                    String tenantIdLocalized = (String)data.get("tenantId_localized");
                    List<Double> geoPoint = (List<Double>) data.get("geo-point");

                    IncidentStatusAgregation incidentStatusAgregation = new IncidentStatusAgregation();
                    incidentStatusAgregation.setBlock(block);
                    incidentStatusAgregation.setCode(code);
                    incidentStatusAgregation.setDistrict(district);
                    incidentStatusAgregation.setLive(isLive);
                    incidentStatusAgregation.setName(name);
                    incidentStatusAgregation.setBoundaryCode(existBoundaryCode);
                    incidentStatusAgregation.setPhcType(phcType);
                    incidentStatusAgregation.setType(type);
                    incidentStatusAgregation.setTenantId(tenantId);
                    incidentStatusAgregation.setTenantIdLocalized(tenantIdLocalized);
                    incidentStatusAgregation.setGeoPoint(geoPoint);
                    incidentStatusAgregation.setState(state);
                    incidentStatusAgregation.setMappedVendorName((String) data.get("mappedVendorName"));
                    incidentStatusAgregation.setMappedVendorUserName((String) data.get("mappedVendorUserName"));

                    List<IncidentStatusAgregation> statusAgregations = incidentRepository.getStatusIncidentsAgregation(tenantId);
                    List<IncidentStatusAgregation> systemFunctional = incidentRepository.getStatusSystemFunctional(tenantId);
                    if(statusAgregations !=null && !statusAgregations.isEmpty()){
                        IncidentStatusAgregation incidentStatusAgregationDB = statusAgregations.get(0);
                        incidentStatusAgregation.setTotalOccurences(incidentStatusAgregationDB.getTotalOccurences());
                        incidentStatusAgregation.setTotalOpenOccurrences(incidentStatusAgregationDB.getTotalOpenOccurrences());
                        incidentStatusAgregation.setTotalCloseOccurrences(incidentStatusAgregationDB.getTotalCloseOccurrences());

                    }

                    boolean hasNonFunctional = false;
                    // true if at least one element is NON_FUNCTIONAL
                    if (systemFunctional !=null){
                        hasNonFunctional = systemFunctional.stream()
                                .anyMatch(item -> NON_FUNCTIONAL.equals(item.getSystemFunctional()));
                    }
                    incidentStatusAgregation.setSystemFunctional(hasNonFunctional ? NON_FUNCTIONAL : FUNCTIONAL);
                    incidentStatusAgregation.setLastModifiedTime(System.currentTimeMillis());

                    log.info("Tickets sent to kafka {}", incidentStatusAgregation);
                    producerService.sendIncident(config.getUpdateTopicIndexer(), incidentStatusAgregation);
                }
            }
        }
        catch (Exception e){
            log.error("Error while processing script update", e);
        }
    }
}
