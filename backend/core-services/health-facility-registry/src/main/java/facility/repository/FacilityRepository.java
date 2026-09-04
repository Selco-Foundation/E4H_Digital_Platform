package facility.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import facility.kafka.Producer;
import facility.web.models.Facility;
import facility.web.models.FacilityCreateRequest;
import facility.web.models.FacilityKibanaIndex;
import facility.web.models.FacilityUpdateRequest;
import facility.web.models.FacilityUpdateRequestFacilityUpdate;
import facility.util.PocPhoneCipher;
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
    private final PocPhoneCipher pocPhoneCipher;

    @Value("${facility.create.topic}")
    private String createTopic;

    @Value("${facility.update.topic}")
    private String updateTopic;

    @Value("${kafka.topics.health.facility.kibana}")
    private String kibanaTopic;

    /**
     * Every facility write goes through {@link #pushCreateFacility} or {@link #pushUpdateFacility},
     * so encrypting the POC mobile number here is what guarantees {@code facility_poc_phone} is
     * never persisted in plaintext — callers must not encrypt it themselves. The value is replaced
     * in place (rather than on a copy) so callers observing the object after the push see the same
     * ciphertext that was persisted. {@link PocPhoneCipher#encrypt} is idempotent, so a value that
     * came straight off the facility table is passed through untouched.
     */
    public void pushCreateFacility(Facility facility) {
        facility.setFacilityPocPhone(pocPhoneCipher.encrypt(facility.getFacilityPocPhone()));
        producer.push(createTopic, facility);
    }

    public void pushUpdateFacility(FacilityUpdateRequest request) {
        FacilityUpdateRequestFacilityUpdate update = request.getFacilityUpdate();
        if (update != null) {
            update.setPocContact(pocPhoneCipher.encrypt(update.getPocContact()));
        }
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
