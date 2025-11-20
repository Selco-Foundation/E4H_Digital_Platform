package facility.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import facility.repository.ServiceRequestRepository;
import facility.web.models.BoundaryCreateRequest;
import facility.web.models.BoundaryCreateResponse;
import facility.web.models.BoundaryRelationshipRequest;
import facility.web.models.BoundaryRelationshipResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
@RequiredArgsConstructor
public class BoundaryService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper mapper;

    // Base URL for the boundary service (e.g., http://localhost:8082)
    @Value("${egov.boundary.host}")
    private String boundaryHost;

    // Path to the boundary create endpoint
    @Value("${egov.boundary.create.path:/boundary-service/boundary/_create}")
    private String boundaryCreatePath;

    // Path to the boundary relationship create endpoint
    @Value("${egov.boundary.relationship.create.path:/boundary-service/boundary-relationships/_create}")
    private String boundaryRelationshipCreatePath;

    public BoundaryCreateResponse createBoundaries(BoundaryCreateRequest boundaryCreateRequest) {

        // Construct the complete URI for boundary search
        String uri = UriComponentsBuilder
                .fromUriString(boundaryHost)
                .path(boundaryCreatePath)
                .toUriString();

        return mapper.convertValue(
                serviceRequestRepository.fetchResult(new StringBuilder(uri), boundaryCreateRequest),
                BoundaryCreateResponse.class
        );
    }

    public BoundaryRelationshipResponse createBoundaryRelationship(BoundaryRelationshipRequest boundaryRelationshipRequest) {

        // Construct the complete URI for boundary search
        String uri = UriComponentsBuilder
                .fromUriString(boundaryHost)
                .path(boundaryRelationshipCreatePath)
                .toUriString();

        return mapper.convertValue(
                serviceRequestRepository.fetchResult(new StringBuilder(uri), boundaryRelationshipRequest),
                BoundaryRelationshipResponse.class
        );
    }

}
