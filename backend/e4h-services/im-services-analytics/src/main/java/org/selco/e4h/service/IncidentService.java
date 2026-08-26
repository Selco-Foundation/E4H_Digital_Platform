package org.selco.e4h.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.kafka.consumer.KafkaProducerService;
import org.selco.e4h.repository.IncidentRepository;
import org.selco.e4h.util.ElasticSearchClient;
import org.selco.e4h.util.FacilityAmcFieldsHelper;
import org.selco.e4h.web.models.Boundary;
import org.selco.e4h.web.models.IncidentRequest;
import org.selco.e4h.web.models.IncidentRequestWrapper;
import org.selco.e4h.web.models.IncidentStatusAgregation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private final ProjectCo2Client projectCo2Client;

    public IncidentService(IncidentRepository incidentRepository, EscalationMasterDataService masterDataService, ConsumerConfiguration config, @Qualifier("objectMapper") ObjectMapper objectMapper,
                           KafkaProducerService producerService, ElasticSearchClient esClient, ProjectCo2Client projectCo2Client){
        this.incidentRepository = incidentRepository;
        this.masterDataService = masterDataService;
        this.producerService = producerService;
        this.config = config;
        this.objectMapper = objectMapper;
        this.esClient = esClient;
        this.projectCo2Client = projectCo2Client;
    }

    @KafkaListener(topics = { "save-im-request-indexer", "update-im-request-indexer", "process-audit-records" }, groupId = "im-consumer-group")
    public void handleKafkaMessage(Object message) {
        log.info("Received message from Kafka: {}", message);

        try {
            if (message instanceof ConsumerRecord<?, ?> record) {
                Object recordValue = record.value();
                IncidentRequest request = null;

                if (recordValue instanceof Map<?, ?> map) {
                    // Try it first as IncidentRequestWrapper
                    IncidentRequestWrapper wrapper = objectMapper.convertValue(map, IncidentRequestWrapper.class);

                    if (wrapper.getIncidentRequest() != null && wrapper.getIndexView() != null) {
                        log.info("Message is IncidentRequestWrapper");

                        request = wrapper.getIncidentRequest();
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

                processIncident(request);
            }
            else{
                log.info("Received message is not a consumer object: {}", message);
            }

        } catch (Exception e) {
            log.error("Error while processing Kafka message", e);
        }
    }

    private void processIncident(IncidentRequest request) {
        String tenantId = request.getIncident().getTenantId();
        String boundaryCode = request.getIncident().getBoundaryCode();
        String facilityId = extractAndEncodeFacilityCode(boundaryCode);
        List<IncidentStatusAgregation> statusAgregations = incidentRepository.getStatusIncidentsAgregation(boundaryCode);
        List<IncidentStatusAgregation> systemFunctional = incidentRepository.getStatusSystemFunctional(boundaryCode);
        log.info("Status aggregation result size: {}", statusAgregations.size());
        log.info("systemFunctional aggregation result size: {}", systemFunctional.size());


        if (statusAgregations != null && !statusAgregations.isEmpty()) {
            IncidentStatusAgregation incidentStatusAgregation = statusAgregations.get(0);

            // systemFunctional=NON_FUNCTIONAL if at least one NON_FUNCTIONAL, otherwise FUNCTIONAL
            boolean hasNonFunctional = false;
            if (systemFunctional != null) {
                hasNonFunctional = systemFunctional.stream()
                        .anyMatch(item -> NON_FUNCTIONAL.equals(item.getSystemFunctional()));
            }
            incidentStatusAgregation.setSystemFunctional(hasNonFunctional ? NON_FUNCTIONAL : FUNCTIONAL);
            incidentStatusAgregation.setLastModifiedTime(System.currentTimeMillis());

            Map<String, Object> tickets = esClient.getHFByBoundaryCode(facilityId);
            log.info("Ticket with facilityID {} found: {}", facilityId, tickets);
            if (tickets != null && !tickets.isEmpty()) {
                Map<String, Object> source = (Map<String, Object>) tickets.get("_source");
                if (source != null) {
                    Map<String, Object> data = (Map<String, Object>) source.get("Data");
                    if (data != null) {
                        Boundary boundary = objectMapper.convertValue(data.get("boundary"), Boundary.class);
                        incidentStatusAgregation.setBlock((String) data.get("block"));
                        incidentStatusAgregation.setCode(String.valueOf(data.get("code")));
                        incidentStatusAgregation.setState((String) data.get("state"));
                        incidentStatusAgregation.setDistrict((String) data.get("district"));
                        incidentStatusAgregation.setLive(!Boolean.FALSE.equals(data.get("isLive")));
                        Boolean synced = (Boolean) data.get("synced");
                        incidentStatusAgregation.setSynced(Boolean.TRUE.equals(synced));
                        incidentStatusAgregation.setName((String) data.get("name"));
                        incidentStatusAgregation.setPhcType((String) data.get("phcType"));
                        incidentStatusAgregation.setType((String) data.get("type"));
                        incidentStatusAgregation.setFacilityId((String) data.get("facilityId"));
                        incidentStatusAgregation.setTenantId(tenantId);
                        incidentStatusAgregation.setBoundary(boundary);
                        incidentStatusAgregation.setTenantIdLocalized((String) data.get("tenantId_localized"));
                        incidentStatusAgregation.setGeoPoint(parseGeoPoint(data.get("geo-point")));
                        // The facility's mapped vendor is owned by the facility registry, not by the ticket
                        // flow. This is a full-document re-index, so copy the already-indexed value straight
                        // through instead of deriving it from the incident wrapper.
                        incidentStatusAgregation.setMappedVendorName((String) data.get("mappedVendorName"));
                        incidentStatusAgregation.setMappedVendorUserName((String) data.get("mappedVendorUserName"));
                        // Resolve projectName from the project service so it is preserved on this full-document
                        // re-index; fall back to the value already indexed when the lookup yields nothing.
                        incidentStatusAgregation.setProjectName(resolveProjectName(
                                tenantId, (String) data.get("facilityId"), (String) data.get("projectName")));
                        // AMC data lives only on the index and this is a full-document re-index, so
                        // carry the indexed values forward or the next ticket event wipes them.
                        FacilityAmcFieldsHelper.copyAmcFields(data, incidentStatusAgregation);

                        log.info("Tickets sent to kafka {}", incidentStatusAgregation);
                        producerService.sendIncident(config.getUpdateTopicIndexer(), incidentStatusAgregation);
                    }
                }
            }
        }
    }

    public static String extractAndEncodeFacilityCode(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return null;
        }

        int index = boundaryCode.indexOf("FAC/");
        if (index == -1) {
            return null;
        }

        String facilityCode = boundaryCode.substring(index);

        return facilityCode;
    }


    public void scriptUpdatePHCAgregation() {
        log.info("Script function called");
        try{
            int totalDocs = esClient.getPHCDocsSize();
            if(totalDocs>0){
                List<Map<String, Object>> listPHCs = esClient.getAllPHC(0, totalDocs);
                log.info("List tickets size {}", listPHCs.size());
                if(listPHCs!=null && !listPHCs.isEmpty()){
                    for (Map<String, Object> phc : listPHCs){
                        processSinglePhcDocument(phc);
                    }
                }
            }
        }
        catch (Exception e){
            log.error("Error while processing script update", e);
        }
    }

    private void processSinglePhcDocument(Map<String, Object> phc) {
        try {
            Map<String, Object> data = (Map<String, Object>)phc.get("Data");
            Boundary boundary = objectMapper.convertValue(data.get("boundary"), Boundary.class);
            String block = (String)data.get("block");
            String code = String.valueOf(data.get("code"));
            String state = (String)data.get("state");
            String district = (String)data.get("district");
            Boolean isLive = (Boolean) data.get("isLive");
            String name = (String)data.get("name");
            String phcType = (String)data.get("phcType");
            String type = (String)data.get("type");
            String tenantId = (String)data.get("tenantId");
            String tenantIdLocalized = (String)data.get("tenantId_localized");
            List<Double> geoPoint = parseGeoPoint(data.get("geo-point"));

            IncidentStatusAgregation incidentStatusAgregation = new IncidentStatusAgregation();
            incidentStatusAgregation.setBlock(block);
            incidentStatusAgregation.setCode(code);
            incidentStatusAgregation.setDistrict(district);
            incidentStatusAgregation.setLive(!Boolean.FALSE.equals(isLive));
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
            incidentStatusAgregation.setProjectName(resolveProjectName(
                    tenantId, (String) data.get("facilityId"), (String) data.get("projectName")));
            // AMC data lives only on the index and this is a full-document re-index, so carry the
            // indexed values forward or this republish wipes them.
            FacilityAmcFieldsHelper.copyAmcFields(data, incidentStatusAgregation);

            if(boundary ==null || boundary.getFacilityCode()==null || boundary.getFacilityCode().isEmpty()){
                return;
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
            if (systemFunctional !=null){
                hasNonFunctional = systemFunctional.stream()
                        .anyMatch(item -> NON_FUNCTIONAL.equals(item.getSystemFunctional()));
            }
            incidentStatusAgregation.setSystemFunctional(hasNonFunctional ? NON_FUNCTIONAL : FUNCTIONAL);
            incidentStatusAgregation.setLastModifiedTime(System.currentTimeMillis());

            log.info("Tickets sent to kafka {}", incidentStatusAgregation);
            producerService.sendIncident(config.getUpdateTopicIndexer(), incidentStatusAgregation);
        } catch (Exception e) {
            log.error("Error processing PHC document, skipping: {}", phc, e);
        }
    }

    /**
     * Resolves the project name mapped to a facility via the project service. Falls back to
     * {@code existingProjectName} (the value already indexed) when the lookup yields nothing,
     * so the projectName is never lost on this full-document re-index.
     */
    private String resolveProjectName(String tenantId, String facilityId, String existingProjectName) {
        if (facilityId == null || facilityId.isBlank()) {
            return existingProjectName;
        }
        try {
            Map<String, String> names = projectCo2Client.fetchProjectNamesByFacility(
                    new RequestInfo(), tenantId, List.of(facilityId));
            String fetched = names.get(facilityId);
            if (fetched != null && !fetched.isBlank()) {
                return fetched;
            }
        } catch (Exception e) {
            log.warn("projectName lookup failed for facilityId={}: {}", facilityId, e.getMessage());
        }
        return existingProjectName;
    }

    private List<Double> parseGeoPoint(Object geoPointValue) {
        if (geoPointValue == null) {
            return null;
        }

        try {
            if (geoPointValue instanceof List<?> listValue) {
                return toDoubleList(listValue);
            }

            if (geoPointValue instanceof String stringValue) {
                String trimmed = stringValue.trim();
                if (trimmed.isEmpty()) {
                    return null;
                }
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    trimmed = trimmed.substring(1, trimmed.length() - 1);
                }
                if (trimmed.isBlank()) {
                    return null;
                }

                String[] tokens = trimmed.split(",");
                List<Object> rawValues = new ArrayList<>();
                for (String token : tokens) {
                    rawValues.add(token.trim());
                }
                return toDoubleList(rawValues);
            }

            return objectMapper.convertValue(geoPointValue, new TypeReference<List<Double>>() {});
        } catch (Exception e) {
            log.warn("Unable to parse geo-point value: {}", geoPointValue, e);
            return null;
        }
    }

    private List<Double> toDoubleList(List<?> rawValues) {
        List<Double> parsedValues = new ArrayList<>();
        for (Object value : rawValues) {
            Double parsedValue = toDouble(value);
            if (parsedValue != null) {
                parsedValues.add(parsedValue);
            }
        }
        return parsedValues.isEmpty() ? null : parsedValues;
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number numberValue) {
            return numberValue.doubleValue();
        }
        if (value instanceof String stringValue) {
            String trimmed = stringValue.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            try {
                return Double.parseDouble(trimmed);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
