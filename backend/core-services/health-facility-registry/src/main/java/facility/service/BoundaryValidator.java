package facility.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class BoundaryValidator {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper mapper;

    // Base URL for the boundary service (e.g., http://localhost:8082)
    @Value("${egov.boundary.host}")
    private String boundaryHost;

    // Path to the boundary search endpoint
    @Value("${egov.boundary.path:/boundary-service/boundary/_search}")
    private String boundaryPath;

    /**
     * Validates that each boundaryCode in the given set exists for the specified tenant.
     * Makes a call to the boundary service using the provided tenantId and RequestInfo.
     *
     * @param boundaryCodes Set of boundary codes to validate
     * @param tenantId Tenant identifier (e.g., "pg.city")
     * @param requestInfo Metadata about the user and request context
     */
    public void validateBoundaries(Set<String> boundaryCodes, String tenantId, RequestInfo requestInfo) {
        Objects.requireNonNull(boundaryCodes, "boundaryCodes cannot be null");
        boundaryCodes.forEach(boundaryCode -> Objects.requireNonNull(boundaryCode, "boundary codes cannot be null"));
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(requestInfo, "RequestInfo cannot be null");

        // If no boundary codes are provided, nothing to validate
        if (boundaryCodes.isEmpty()) return;

        // Join boundary codes into comma-separated string for the query parameter
        String codes = String.join(",", boundaryCodes);

        // Construct the complete URI for boundary search
        String uri = UriComponentsBuilder.fromUriString(boundaryHost)
                .path(boundaryPath)
                .queryParam("tenantId", tenantId)
                .queryParam("codes", codes)
                .queryParam("offset", 0)
                .queryParam("limit", boundaryCodes.size())
                .toUriString();

        Map<String, Object> requestBody = Map.of("RequestInfo", requestInfo);

        try {
            // Call boundary service and parse the response
            Object rawResponse = serviceRequestRepository.fetchResult(new StringBuilder(uri), requestBody);
            Map<String, Object> response = mapper.convertValue(rawResponse, new TypeReference<Map<String, Object>>() {});

            // Validate each boundary code individually against the response
            for (String code : boundaryCodes) {
                validateResponse(code, response);
            }
        } catch (Exception e) {
            // Wrap and rethrow exceptions with contextual information
            throw new IllegalArgumentException("Error validating boundary codes: " + boundaryCodes, e);
        }
    }

    /**
     * Validates that the response contains a non-empty list of boundaries for a given code.
     *
     * @param code Boundary code to validate
     * @param response Parsed response from boundary service
     */
    @SuppressWarnings("unchecked")
    private void validateResponse(String code, Map<String, Object> response) {
        if (response == null || !response.containsKey("Boundary")) {
            throw new IllegalArgumentException("Boundary response is missing 'Boundary' field");
        }

        Object boundariesObj = response.get("Boundary");
        if (!(boundariesObj instanceof List<?> boundaries) || boundaries.isEmpty()) {
            throw new IllegalArgumentException("Boundary response is empty");
        }

        // Check if any returned boundary matches the requested code
        boolean found = boundaries.stream()
                .filter(Objects::nonNull)
                .filter(Map.class::isInstance)
                .map(obj -> (Map<String, Object>) obj)
                .anyMatch(boundary -> code.equals(boundary.get("code")));

        if (!found) {
            throw new IllegalArgumentException("Boundary code not found in response: " + code);
        }
    }
}
