package facility.repository;


import facility.kafka.Producer;
import facility.web.models.Facility;
import facility.web.models.FacilityKibanaIndex;
import facility.web.models.FacilityUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FacilityRepository {

    private final Producer producer;

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
        producer.push(kibanaTopic, kibanaIndex);
    }
}
