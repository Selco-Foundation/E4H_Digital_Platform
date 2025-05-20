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

    @Value("${egov.boundary.host}")
    private String boundaryHost;

    @Value("${egov.boundary.path:/boundary-service/boundary/_search}")
    private String boundaryPath;

    public void validateBoundaries(Set<String> boundaryCodes, String tenantId, RequestInfo requestInfo) {
        Objects.requireNonNull(boundaryCodes, "boundaryCodes cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(requestInfo, "RequestInfo cannot be null");

        if (boundaryCodes.isEmpty()) return;

        String codes = String.join(",", boundaryCodes);

        String uri = UriComponentsBuilder.fromUriString(boundaryHost)
                .path(boundaryPath)
                .queryParam("tenantId", tenantId)
                .queryParam("codes", codes)
                .toUriString();

        Map<String, Object> requestBody = Map.of("RequestInfo", requestInfo);

        try {
            Object rawResponse = serviceRequestRepository.fetchResult(new StringBuilder(uri), requestBody);
            Map<String, Object> response = mapper.convertValue(rawResponse, new TypeReference<>() {});

            for (String code : boundaryCodes) {
                validateResponse(code, response);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error validating boundary codes: " + boundaryCodes, e);
        }
    }


    private void validateResponse(String code, Map<String, Object> response) {
        if (response == null || !response.containsKey("Boundary")) {
            throw new IllegalArgumentException("Boundary response is missing 'Boundary' field");
        }

        Object boundaries = response.get("Boundary");
        if (!(boundaries instanceof List<?> list) || list.isEmpty()) {
            throw new IllegalArgumentException("Invalid or empty 'Boundary' response for code: " + code);
        }
    }
}
