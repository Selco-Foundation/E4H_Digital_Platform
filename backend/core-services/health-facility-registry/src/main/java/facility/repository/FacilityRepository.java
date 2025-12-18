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
        producer.push(createTopic, facility);
    }

    public void pushUpdateFacility(FacilityUpdateRequest request) {
        producer.push(updateTopic, request);
    }

    public void pushToKibana(FacilityKibanaIndex kibanaIndex) {
        try {
            // Log the JSON being sent to verify boundary object is included
            String json = objectMapper.writeValueAsString(kibanaIndex);
            log.info("Pushing to Kibana topic {}: {}", kibanaTopic, json);
            if (kibanaIndex.getBoundary() != null) {
                log.info("Boundary object in JSON: {}", objectMapper.writeValueAsString(kibanaIndex.getBoundary()));
            } else {
                log.warn("Boundary object is NULL in FacilityKibanaIndex being pushed to Kafka!");
            }
        } catch (Exception e) {
            log.error("Error serializing FacilityKibanaIndex to JSON for logging", e);
        }
        producer.push(kibanaTopic, kibanaIndex);
    }
}
