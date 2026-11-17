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

    // Path to the boundary delete endpoint
    @Value("${egov.boundary.delete.path:/boundary-service/boundary/_delete}")
    private String boundaryDeletePath;

    // Path to the boundary relationship delete endpoint
    @Value("${egov.boundary.relationship.delete.path:/boundary-service/boundary-relationships/_delete}")
    private String boundaryRelationshipDeletePath;

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

    public BoundaryRelationshipResponse deleteBoundaryRelationship(BoundaryRelationshipRequest boundaryRelationshipRequest) {
        log.trace("Entering deleteBoundaryRelationship method");
        String boundaryCode = boundaryRelationshipRequest.getBoundaryRelationship() != null
                ? boundaryRelationshipRequest.getBoundaryRelationship().getCode() : null;
        log.info("Deleting boundary relationship for boundary code: {}", boundaryCode);

        String uri = UriComponentsBuilder
                .fromUriString(boundaryHost)
                .path(boundaryRelationshipDeletePath)
                .toUriString();
        log.debug("Boundary relationship delete URI: {}", uri);

        try {
            BoundaryRelationshipResponse response = mapper.convertValue(
                    serviceRequestRepository.fetchResult(new StringBuilder(uri), boundaryRelationshipRequest),
                    BoundaryRelationshipResponse.class
            );
            log.info("Successfully deleted boundary relationship for boundary code: {}", boundaryCode);
            log.trace("Exiting deleteBoundaryRelationship method");
            return response;
        } catch (Exception e) {
            log.error("Error deleting boundary relationship for boundary code {}: {}", boundaryCode, e.getMessage(), e);
            throw e;
        }
    }

    public BoundaryCreateResponse deleteBoundaries(BoundaryCreateRequest boundaryDeleteRequest) {
        log.trace("Entering deleteBoundaries method");
        int boundaryCount = boundaryDeleteRequest.getBoundary() != null ? boundaryDeleteRequest.getBoundary().size() : 0;
        log.info("Deleting {} boundaries via boundary service", boundaryCount);

        String uri = UriComponentsBuilder
                .fromUriString(boundaryHost)
                .path(boundaryDeletePath)
                .toUriString();
        log.debug("Boundary delete URI: {}", uri);

        try {
            BoundaryCreateResponse response = mapper.convertValue(
                    serviceRequestRepository.fetchResult(new StringBuilder(uri), boundaryDeleteRequest),
                    BoundaryCreateResponse.class
            );
            log.info("Successfully deleted boundaries via boundary service");
            log.trace("Exiting deleteBoundaries method");
            return response;
        } catch (Exception e) {
            log.error("Error deleting boundaries via boundary service: {}", e.getMessage(), e);
            throw e;
        }
    }

}
