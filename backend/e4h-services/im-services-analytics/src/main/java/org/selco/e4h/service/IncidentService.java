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
import org.selco.e4h.util.FacilityIndexPassthrough;
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
        log.info("Status aggregation result size: {}", statusAgregations.size());

        if (statusAgregations != null && !statusAgregations.isEmpty()) {
            IncidentStatusAgregation incidentStatusAgregation = statusAgregations.get(0);

            Map<String, Object> tickets = esClient.getHFByBoundaryCode(facilityId);
            log.info("Ticket with facilityID {} found: {}", facilityId, tickets);
            if (tickets != null && !tickets.isEmpty()) {
                Map<String, Object> source = (Map<String, Object>) tickets.get("_source");
                if (source != null) {
                    Map<String, Object> data = (Map<String, Object>) source.get("Data");
                    if (data != null) {
                        applyIndexedDocumentAndStatus(incidentStatusAgregation, data, boundaryCode, tenantId);

                        log.info("Tickets sent to kafka {}", incidentStatusAgregation);
                        producerService.sendIncident(config.getUpdateTopicIndexer(), incidentStatusAgregation);
                    }
                }
            }
        }
    }

    /**
     * Fills {@code target} with everything the index already holds for the facility, then overlays the
     * fields this service derives: solar panel status, the timestamp the facility went non-functional,
     * a freshly resolved project name, and the modification stamp.
     *
     * <p>Shared by the ticket-event flow and the backfill scripts so both publish an identical,
     * lossless document - the two used to maintain separate copy lists that had already drifted.
     *
     * @param data     the facility's current {@code _source.Data}; must be non-null. There is no
     *                 meaningful document to republish without it, and publishing a partial one would
     *                 wipe the facility's index entry - so both callers check for it and skip the
     *                 facility entirely rather than letting a null reach here.
     * @param tenantId tenant to stamp on the document; when null the indexed value is kept
     */
    private void applyIndexedDocumentAndStatus(IncidentStatusAgregation target,
                                               Map<String, Object> data,
                                               String boundaryCode,
                                               String tenantId) {
        if (data == null) {
            throw new IllegalStateException(
                    "indexed facility Data must not be null; caller must guard before invoking");
        }

        // Carry the whole indexed document forward first: the indexer replaces the document at this
        // id, so any field not republished here is dropped from the index.
        FacilityIndexPassthrough.copyInto(data, target);

        target.setFacilityId((String) data.get("facilityId"));
        target.setTenantId(tenantId != null ? tenantId : (String) data.get("tenantId"));

        // solarPanelStatus=NON_FUNCTIONAL if at least one open ticket reports NON_FUNCTIONAL.
        List<IncidentStatusAgregation> systemFunctional = incidentRepository.getStatusSystemFunctional(boundaryCode);
        boolean hasNonFunctional = systemFunctional != null && systemFunctional.stream()
                .anyMatch(item -> NON_FUNCTIONAL.equals(item.getSystemFunctional()));
        target.setSystemFunctional(hasNonFunctional ? NON_FUNCTIONAL : FUNCTIONAL);

        // Derived from the same open/non-functional ticket set as the status above, so the two can
        // never disagree: a timestamp exactly when non-functional, null when functional.
        target.setNonFunctionalTimestamp(hasNonFunctional
                ? incidentRepository.getOldestOpenNonFunctionalCreatedTime(boundaryCode)
                : null);

        // Resolve projectName from the project service so it is preserved on this full-document
        // re-index; fall back to the value already indexed when the lookup yields nothing.
        target.setProjectName(resolveProjectName(
                target.getTenantId(), target.getFacilityId(), (String) data.get("projectName")));
        target.setLastModifiedTime(System.currentTimeMillis());
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


    /** Documents fetched per Elasticsearch request by the backfill scripts. */
    private static final int BACKFILL_PAGE_SIZE = 1000;

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

    /**
     * Backfills {@code nonFunctionalTimestamp} onto every document in the health facility index.
     *
     * <p>Republishes each facility's full document with the field derived from its current open
     * tickets, so facilities that are non-functional get the creation time of the ticket that took
     * them down and functional ones get an explicit null. Every other field is carried through from
     * the index untouched (see {@code FacilityIndexPassthrough}), so this is safe to re-run.
     *
     * <p>Pages through the index rather than requesting every document at once: the facility count
     * will eventually cross Elasticsearch's {@code max_result_window} (10k by default), at which
     * point a single oversized request starts failing outright.
     *
     * @return the number of facility documents republished
     */
    public int scriptPopulateNonFunctionalTimestamp() {
        log.info("Non-functional timestamp backfill started");
        int processed = 0;
        try {
            int totalDocs = esClient.getPHCDocsSize();
            log.info("Non-functional timestamp backfill: {} facility documents to process", totalDocs);

            for (int from = 0; from < totalDocs; from += BACKFILL_PAGE_SIZE) {
                List<Map<String, Object>> page = esClient.getAllPHC(from, BACKFILL_PAGE_SIZE);
                if (page == null || page.isEmpty()) {
                    log.warn("Non-functional timestamp backfill: empty page at from={}, stopping early", from);
                    break;
                }
                for (Map<String, Object> phc : page) {
                    if (processSinglePhcDocument(phc)) {
                        processed++;
                    }
                }
                log.info("Non-functional timestamp backfill: {}/{} documents processed",
                        Math.min(from + BACKFILL_PAGE_SIZE, totalDocs), totalDocs);
            }
        } catch (Exception e) {
            log.error("Error while processing non-functional timestamp backfill", e);
        }
        log.info("Non-functional timestamp backfill finished, {} documents republished", processed);
        return processed;
    }

    /**
     * Republishes one indexed facility document with freshly derived ticket counts, solar panel
     * status and non-functional timestamp.
     *
     * @return {@code true} when the document was published, {@code false} when it was skipped
     */
    private boolean processSinglePhcDocument(Map<String, Object> phc) {
        try {
            Map<String, Object> data = (Map<String, Object>) phc.get("Data");
            if (data == null) {
                return false;
            }
            Boundary boundary = objectMapper.convertValue(data.get("boundary"), Boundary.class);
            if (boundary == null || boundary.getFacilityCode() == null || boundary.getFacilityCode().isEmpty()) {
                return false;
            }
            String boundaryCode = boundary.getFacilityCode();

            IncidentStatusAgregation incidentStatusAgregation = new IncidentStatusAgregation();
            List<IncidentStatusAgregation> statusAgregations = incidentRepository.getStatusIncidentsAgregation(boundaryCode);
            if (statusAgregations != null && !statusAgregations.isEmpty()) {
                IncidentStatusAgregation incidentStatusAgregationDB = statusAgregations.get(0);
                incidentStatusAgregation.setTotalOccurences(incidentStatusAgregationDB.getTotalOccurences());
                incidentStatusAgregation.setTotalOpenOccurrences(incidentStatusAgregationDB.getTotalOpenOccurrences());
                incidentStatusAgregation.setTotalCloseOccurrences(incidentStatusAgregationDB.getTotalCloseOccurrences());
            }

            // Keep the indexed tenantId: unlike the ticket flow there is no incoming incident to take
            // a tenant from here.
            applyIndexedDocumentAndStatus(incidentStatusAgregation, data, boundaryCode, null);

            log.info("Tickets sent to kafka {}", incidentStatusAgregation);
            producerService.sendIncident(config.getUpdateTopicIndexer(), incidentStatusAgregation);
            return true;
        } catch (Exception e) {
            log.error("Error processing PHC document, skipping: {}", phc, e);
            return false;
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
}
