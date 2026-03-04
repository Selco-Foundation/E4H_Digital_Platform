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
        log.trace("Entering validateBoundaries method");
        Objects.requireNonNull(boundaryCodes, "boundaryCodes cannot be null");
        boundaryCodes.forEach(boundaryCode -> Objects.requireNonNull(boundaryCode, "boundary codes cannot be null"));
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(requestInfo, "RequestInfo cannot be null");

        // If no boundary codes are provided, nothing to validate
        if (boundaryCodes.isEmpty()) {
            log.debug("No boundary codes provided for validation, skipping");
            return;
        }

        log.info("Validating {} boundary codes for tenant {}", boundaryCodes.size(), tenantId);
        log.debug("Boundary codes to validate: {}", boundaryCodes);

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
        log.debug("Boundary validation URI: {}", uri);

        Map<String, Object> requestBody = Map.of("RequestInfo", requestInfo);

        try {
            // Call boundary service and parse the response
            Object rawResponse = serviceRequestRepository.fetchResult(new StringBuilder(uri), requestBody);
            Map<String, Object> response = mapper.convertValue(rawResponse, new TypeReference<Map<String, Object>>() {});
            log.debug("Received boundary validation response from service");

            // Validate each boundary code individually against the response
            int validatedCount = 0;
            for (String code : boundaryCodes) {
                validateResponse(code, response);
                validatedCount++;
            }
            log.info("Successfully validated {} boundary codes for tenant {}", validatedCount, tenantId);
        } catch (Exception e) {
            log.error("Error validating boundary codes {} for tenant {}: {}", boundaryCodes, tenantId, e.getMessage(), e);
            // Wrap and rethrow exceptions with contextual information
            throw new IllegalArgumentException("Error validating boundary codes: " + boundaryCodes, e);
        }
        log.trace("Exiting validateBoundaries method");
    }

    /**
     * Validates that the response contains a non-empty list of boundaries for a given code.
     *
     * @param code Boundary code to validate
     * @param response Parsed response from boundary service
     */
    @SuppressWarnings("unchecked")
    private void validateResponse(String code, Map<String, Object> response) {
        log.trace("Entering validateResponse method for boundary code: {}", code);
        if (response == null || !response.containsKey("Boundary")) {
            log.error("Boundary validation failed: Response is missing 'Boundary' field for code: {}", code);
            throw new IllegalArgumentException("Boundary response is missing 'Boundary' field");
        }

        Object boundariesObj = response.get("Boundary");
        if (!(boundariesObj instanceof List<?> boundaries) || boundaries.isEmpty()) {
            log.error("Boundary validation failed: Response is empty for code: {}", code);
            throw new IllegalArgumentException("Boundary response is empty");
        }

        log.debug("Validating boundary code {} against {} boundaries in response", code, boundaries.size());

        // Check if any returned boundary matches the requested code
        boolean found = boundaries.stream()
                .filter(Objects::nonNull)
                .filter(Map.class::isInstance)
                .map(obj -> (Map<String, Object>) obj)
                .anyMatch(boundary -> code.equals(boundary.get("code")));

        if (!found) {
            log.error("Boundary validation failed: Code {} not found in response", code);
            throw new IllegalArgumentException("Boundary code not found in response: " + code);
        }
        log.debug("Boundary code {} validated successfully", code);
        log.trace("Exiting validateResponse method");
    }
}
