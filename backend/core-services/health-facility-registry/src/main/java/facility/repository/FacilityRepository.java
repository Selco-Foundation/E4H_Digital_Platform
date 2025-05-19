package facility.repository;


import facility.kafka.Producer;
import facility.web.models.Facility;
import facility.web.models.FacilityCreateRequest;
import facility.web.models.FacilityUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class FacilityRepository {

    @Autowired
    private Producer producer;

    @Value("${facility.create.topic}")
    private String createTopic;

    @Value("${facility.update.topic}")
    private String updateTopic;

    public void pushCreateFacility(Facility facility) {
        producer.push(createTopic, facility);
    }

    public void pushUpdateFacility(FacilityUpdateRequest request) {
        producer.push(updateTopic, request);
    }
}
