package facility.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import facility.kafka.Producer;
import facility.web.models.Facility;
import facility.web.models.FacilityCreateRequest;
import facility.web.models.FacilityKibanaIndex;
import facility.web.models.FacilityUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FacilityRepository {

    private final Producer producer;
    private final ObjectMapper objectMapper;

    @Value("${facility.create.topic}")
    private String createTopic;

    @Value("${facility.update.topic}")
    private String updateTopic;

    @Value("${kafka.topics.health.facility.kibana}")
    private String kibanaTopic;

    public void pushCreateFacility(Facility facility) {
        log.trace("Entering pushCreateFacility method");
        String facilityId = facility != null ? facility.getFacilityId() : null;
        log.info("Pushing facility create event to Kafka topic {} for facility {}", createTopic, facilityId);
        producer.push(createTopic, facility);
        log.trace("Exiting pushCreateFacility method");
    }

    public void pushUpdateFacility(FacilityUpdateRequest request) {
        log.trace("Entering pushUpdateFacility method");
        String facilityId = request != null && request.getFacilityUpdate() != null 
                ? request.getFacilityUpdate().getFacilityId() : null;
        log.info("Pushing facility update event to Kafka topic {} for facility {}", updateTopic, facilityId);
        producer.push(updateTopic, request);
        log.trace("Exiting pushUpdateFacility method");
    }

    public void pushToKibana(FacilityKibanaIndex kibanaIndex) {
        log.trace("Entering pushToKibana method");
        String facilityId = kibanaIndex != null ? kibanaIndex.getFacilityId() : null;
        log.info("Pushing facility {} to Kibana topic {}", facilityId, kibanaTopic);
        
        try {
            // Log boundary status without full object (avoid logging full objects in INFO)
            if (kibanaIndex.getBoundary() != null) {
                log.debug("Boundary object present in FacilityKibanaIndex for facility {}", facilityId);
            } else {
                log.warn("Boundary object is NULL in FacilityKibanaIndex being pushed to Kafka for facility {}", facilityId);
            }
            
            // Only log full JSON in DEBUG level
            if (log.isDebugEnabled()) {
                String json = objectMapper.writeValueAsString(kibanaIndex);
                log.debug("Kibana index JSON for facility {}: {}", facilityId, json);
            }
        } catch (Exception e) {
            log.error("Error serializing FacilityKibanaIndex to JSON for logging for facility {}: {}", facilityId, e.getMessage(), e);
        }
        producer.push(kibanaTopic, kibanaIndex);
        log.trace("Exiting pushToKibana method");
    }
}
