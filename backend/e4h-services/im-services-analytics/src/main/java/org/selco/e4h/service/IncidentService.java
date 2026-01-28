package org.selco.e4h.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.kafka.consumer.KafkaProducerService;
import org.selco.e4h.repository.IncidentRepository;
import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.web.models.Boundary;
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
        log.trace("Kafka message received, message type: {}", message != null ? message.getClass().getSimpleName() : "null");
        log.info("Processing Kafka message for incident indexing");

        try {
            if (message instanceof ConsumerRecord<?, ?> record) {
                Object recordValue = record.value();
                log.debug("Processing ConsumerRecord with value type: {}", recordValue != null ? recordValue.getClass().getSimpleName() : "null");
                IncidentRequest request = null;
                String mappedVendorName = null;
                String mappedVendorUserName = null;

                if (recordValue instanceof Map<?, ?> map) {
                    // Try it first as IncidentRequestWrapper
                    IncidentRequestWrapper wrapper = objectMapper.convertValue(map, IncidentRequestWrapper.class);

                    if (wrapper.getIncidentRequest() != null && wrapper.getIndexView() != null) {
                        log.debug("Message type identified as IncidentRequestWrapper");
                        request = wrapper.getIncidentRequest();
                        mappedVendorName = wrapper.getIndexView().getMappedVendorName();
                        mappedVendorUserName = wrapper.getIndexView().getMappedVendorUserName();
                        log.debug("Extracted vendor info: name={}, userName={}", mappedVendorName, mappedVendorUserName);
                    } else {
                        // Otherwise, it's a process-audit-records
                        log.debug("Message type identified as Map<String,Object>");
                        String topic = (String) map.get("topic");
                        if (topic == null || topic.isBlank()) {
                            log.warn("Topic field is missing or blank in message");
                            return;
                        }

                        if (!topic.equals("save-im-request") &&
                                !topic.equals("update-im-request") &&
                                !topic.equals("save-im-request-indexer") &&
                                !topic.equals("update-im-request-indexer")) {
                            log.debug("Topic {} not in allowed list, skipping", topic);
                            return;
                        }

                        Object value = map.get("value");
                        request = objectMapper.convertValue(value, IncidentRequest.class);
                        log.debug("Converted message value to IncidentRequest for topic: {}", topic);
                    }
                }

                if (request == null || request.getIncident() == null) {
                    log.warn("Incident request or incident is null, skipping processing");
                    return;
                }

                log.info("Processing incident: {}", request.getIncident().getIncidentId());
                processIncident(request, mappedVendorName, mappedVendorUserName);
            }
            else{
                log.warn("Received message is not a ConsumerRecord, type: {}", message != null ? message.getClass().getSimpleName() : "null");
            }

        } catch (Exception e) {
            log.error("Error while processing Kafka message", e);
        }
    }

    private void processIncident(IncidentRequest request, String mappedVendorName, String mappedVendorUserName) {
        log.trace("Processing incident: {}", request.getIncident().getIncidentId());
        String tenantId = request.getIncident().getTenantId();
        String boundaryCode = request.getIncident().getBoundaryCode();
        log.debug("Processing incident with tenantId: {}, boundaryCode: {}", tenantId, boundaryCode);
        String facilityId = extractAndEncodeFacilityCode(boundaryCode);
        log.debug("Extracted facility ID: {}", facilityId);
        
        List<IncidentStatusAgregation> statusAgregations = incidentRepository.getStatusIncidentsAgregation(boundaryCode);
        List<IncidentStatusAgregation> systemFunctional = incidentRepository.getStatusSystemFunctional(boundaryCode);
        log.debug("Retrieved status aggregations: {}, system functional: {}", statusAgregations.size(), systemFunctional.size());
        log.info("Processing incident aggregation: statusCount={}, systemFunctionalCount={}", 
            statusAgregations.size(), systemFunctional.size());


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

            Map<String, Object> tickets = esClient.getHFByBoundaryCode(facilityId);
            log.debug("Retrieved tickets from Elasticsearch for facilityID: {}, found: {}", facilityId, tickets != null && !tickets.isEmpty());
            if (tickets != null && !tickets.isEmpty()) {
                log.info("Found tickets for facilityID: {}", facilityId);
                Map<String, Object> source = (Map<String, Object>) tickets.get("_source");
                if (source != null) {
                    Map<String, Object> data = (Map<String, Object>) source.get("Data");
                    if (data != null) {
                        Boundary boundary = objectMapper.convertValue(data.get("boundary"), Boundary.class);
                        incidentStatusAgregation.setBlock((String) data.get("block"));
                        incidentStatusAgregation.setCode(String.valueOf(data.get("code")));
                        incidentStatusAgregation.setState((String) data.get("state"));
                        incidentStatusAgregation.setDistrict((String) data.get("district"));
                        incidentStatusAgregation.setLive((boolean) data.get("isLive"));
                        Boolean synced = (Boolean) data.get("synced");
                        incidentStatusAgregation.setSynced(Boolean.TRUE.equals(synced));
                        incidentStatusAgregation.setName((String) data.get("name"));
                        incidentStatusAgregation.setPhcType((String) data.get("phcType"));
                        incidentStatusAgregation.setType((String) data.get("type"));
                        incidentStatusAgregation.setFacilityId((String) data.get("facilityId"));
                        incidentStatusAgregation.setTenantId(tenantId);
                        incidentStatusAgregation.setBoundary(boundary);
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

                        log.debug("Prepared incident status aggregation for Kafka: incidentId={}, totalOccurrences={}", 
                            incidentStatusAgregation.getCode(), incidentStatusAgregation.getTotalOccurences());
                        log.info("Sending incident status aggregation to Kafka for facility: {}", facilityId);
                        producerService.sendIncident(config.getUpdateTopicIndexer(), incidentStatusAgregation);
                    }
                }
            }
        }
    }

    public static String extractAndEncodeFacilityCode(String boundaryCode) {
        log.trace("Extracting facility code from boundary code: {}", boundaryCode);
        if (boundaryCode == null || boundaryCode.isBlank()) {
            log.debug("Boundary code is null or blank");
            return null;
        }

        int index = boundaryCode.indexOf("FAC/");
        if (index == -1) {
            log.debug("FAC/ pattern not found in boundary code");
            return null;
        }

        String facilityCode = boundaryCode.substring(index);
        log.debug("Extracted facility code: {}", facilityCode);
        return facilityCode;
    }


    public void scriptUpdatePHCAgregation() {
        log.trace("Script update PHC aggregation method invoked");
        log.info("Starting PHC aggregation update script");
        try{
            int totalDocs = esClient.getPHCDocsSize();
            log.debug("Total PHC documents in Elasticsearch: {}", totalDocs);
            if(totalDocs>0){
                List<Map<String, Object>> listPHCs = esClient.getAllPHC(0, totalDocs);
                log.debug("Retrieved {} PHC documents from Elasticsearch", listPHCs != null ? listPHCs.size() : 0);
                log.info("Processing {} PHC documents for aggregation update", listPHCs != null ? listPHCs.size() : 0);
                if(listPHCs!=null && !listPHCs.isEmpty()){
                    for (Map<String, Object> phc : listPHCs){
                        Map<String, Object> data = (Map<String, Object>)phc.get("Data");
                        Boundary boundary = objectMapper.convertValue(data.get("boundary"), Boundary.class);
                        String block = (String)data.get("block");
                        String code = String.valueOf(data.get("code"));
                        String state = (String)data.get("state");
                        String district = (String)data.get("district");
                        boolean isLive = (boolean)data.get("isLive");
                        String name = (String)data.get("name");
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
                        Boolean synced = (Boolean) data.get("synced");
                        incidentStatusAgregation.setSynced(Boolean.TRUE.equals(synced));
                        incidentStatusAgregation.setName(name);
                        incidentStatusAgregation.setBoundary(boundary);
                        incidentStatusAgregation.setPhcType(phcType);
                        incidentStatusAgregation.setType(type);
                        incidentStatusAgregation.setFacilityId((String) data.get("facilityId"));
                        incidentStatusAgregation.setTenantId(tenantId);
                        incidentStatusAgregation.setTenantIdLocalized(tenantIdLocalized);
                        incidentStatusAgregation.setGeoPoint(geoPoint);
                        incidentStatusAgregation.setState(state);
                        incidentStatusAgregation.setMappedVendorName((String) data.get("mappedVendorName"));
                        incidentStatusAgregation.setMappedVendorUserName((String) data.get("mappedVendorUserName"));

                        if(boundary ==null || boundary.getFacilityCode()==null || boundary.getFacilityCode().isEmpty()){
                            continue;
                        }
                        String boundaryCode = boundary.getFacilityCode();
                        List<IncidentStatusAgregation> statusAgregations = incidentRepository.getStatusIncidentsAgregation(boundaryCode);
                        List<IncidentStatusAgregation> systemFunctional = incidentRepository.getStatusSystemFunctional(boundaryCode);
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

                        log.debug("Prepared PHC aggregation for Kafka: code={}, totalOccurrences={}", 
                            incidentStatusAgregation.getCode(), incidentStatusAgregation.getTotalOccurences());
                        log.info("Sending PHC aggregation to Kafka for code: {}", incidentStatusAgregation.getCode());
                        producerService.sendIncident(config.getUpdateTopicIndexer(), incidentStatusAgregation);
                    }
                }
            }
            log.info("PHC aggregation update script completed successfully");
        }
        catch (Exception e){
            log.error("Error while processing PHC aggregation update script", e);
        }
    }
}
